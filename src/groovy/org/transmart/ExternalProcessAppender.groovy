package org.transmart

import com.google.common.base.Charsets
import grails.converters.JSON
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import org.apache.log4j.AppenderSkeleton
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.helpers.LogLog

import static java.lang.ProcessBuilder.Redirect.*

@CompileStatic
@Log4j
class ExternalProcessAppender extends AppenderSkeleton {

    List<String> command
    int restartLimit = 15
    int restartWindow = 1800
    boolean throwOnFailure = false

    protected long starttime
    protected int failcount = 0
    protected Process process
    protected Writer input

    // The appender being broken should be unlikely. It indicates a misconfiguration or an error in the child program.
    protected boolean broken = false

    private ThreadLocal<int[]> recursionCount = [
        initialValue: { [0] as int[] }
    ] as ThreadLocal<int[]>

    void setRestartLimit(int l) {
        if (l < 0) throw new IllegalArgumentException("restartLimit cannot be negative (use 0 to disable the limit)")
        this.restartLimit = l
    }

    void setRestartWindow(int w) {
        if (w <= 0) throw new IllegalArgumentException("restartWindow must be larger than 0")
        this.restartWindow = w
    }

    boolean isBroken() { return broken }

    ExternalProcessAppender() {}

    ExternalProcessAppender(Map<String,Object> opts) {
        opts?.each { String prop, val ->
            setProperty(prop, val)
        }
    }

    /* We cannot call into the normal logging system while we have this appender locked (that risks deadlock), so use
     * the backup logging system.*/
    private void debug(String msg) {
        LogLog.debug("${this.class.name}(name: $name): $msg")
    }

    private synchronized startProcess() {
        if (process == null) {
            starttime = System.currentTimeMillis()
        }
        process = new ProcessBuilder(command).redirectOutput(INHERIT).redirectError(INHERIT).start()
        input = new OutputStreamWriter(process.getOutputStream(), Charsets.UTF_8)
        // Give child some time to fail if it fails quickly
        sleep(1)
    }

    synchronized boolean getChildAlive() {
        if (process == null) return false
        try {
            process.exitValue()
            return false
        } catch (IllegalThreadStateException _) {
            return true
        }
    }

    private synchronized String restartChild() {
        failcount++
        long restartWindowEnd = starttime + restartWindow * 1000L
        long now = System.currentTimeMillis()
        boolean inWindow = restartLimit == 0 ? false : now <= restartWindowEnd
        if (restartLimit != 0 && inWindow && failcount > restartLimit) {
            broken = true
            // Don't log from here while we are synchronized, that would cause a deadlock condition
            return "Failed to restart external log handling process \"${command.join(' ')}\", failed $failcount times" +
                    " in less than $restartWindow seconds"
        }
        input.close()
        if (!inWindow) {
            starttime = now
            failcount = 0
        }
        debug("Restarting external logging process ${command.join(' ')}")
        startProcess()
        return null
    }

    void write(String str) {
        int[] rc = recursionCount.get()
        int oldrc = rc[0]
        try {
            rc[0]++
            // Recursive call, ignore
            if (rc[0] > 1) return

            if (broken) {
                log.warn("Attempting to write to broken external log handling process")
                return
            }

            String errmsg = null
            // input.write and input.flush synchronize, so there is no use in optimizing away this synchronized block
            // in the normal flow.
            synchronized (this) {
                if (!input) {
                    startProcess()
                }
                while (errmsg == null) {
                    try {
                        input.write(str)
                        input.flush()
                        return
                    } catch (IOException e) {
                        debug("Caught exception while writing to child process: $e")
                        errmsg = restartChild()
                    }
                }
            }

            assert errmsg != null
            // Log outside of the synchronized block
            log.error(errmsg)
            if (throwOnFailure) {
                throw new Exception(errmsg)
            }
        } finally {
            rc[0] = oldrc
        }
    }

    @Override
    void append(LoggingEvent event) {
        // Check for recursive invocation
        if (recursionCount.get()[0] > 0) return;

        // convert to JSON outside of the lock
        JSON msg = [message: event.toString()] as JSON
        write(msg.toString())
    }

    @Override
    boolean requiresLayout() {return false}

    @Override
    synchronized void close() {
        closed = true
        input?.close()
    }
}
