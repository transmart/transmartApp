package org.transmart

import com.google.common.escape.Escaper
import com.google.common.escape.Escapers
import grails.converters.JSON
import org.apache.log4j.Level
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.Category
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import static org.apache.commons.io.FileUtils.readFileToString
import static org.apache.commons.io.FileUtils.writeStringToFile
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

class ExternalProcessAppenderTests {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    static String TESTSTRING = "hello world! testing org.transmart.ExternalProcessAppender\n"

    static sh(cmd) { return ['sh', '-c', cmd] }
    static Escaper SHELL_ESCAPE = Escapers.builder().addEscape("'" as char, "'\"'\"'").build()
    static path(File file) { "'${SHELL_ESCAPE.escape(file.path)}'" }

    static void waitForChild(ExternalProcessAppender a) {
        a.input.close()
        a.process.waitFor()
    }

    @Test
    void loggingEventTest() {
        File output = temp.newFile('output')
        def p = new ExternalProcessAppender(command: sh("cat >"+path(output)))
        LoggingEvent e = new LoggingEvent("", new Category('debug'), Level.DEBUG, TESTSTRING, null)
        p.doAppend(e)
        p.close()
        waitForChild(p)
        assertThat JSON.parse(readFileToString(output)).message, is(TESTSTRING)
    }

    @Test
    void outputTest() {
        File output = temp.newFile('output')
        def p = new ExternalProcessAppender(command: sh("cat > "+path(output)))
        p.write(TESTSTRING)
        waitForChild(p)
        assertThat readFileToString(output), is(TESTSTRING)
    }

    @Test(expected=Exception.class)
    void testFail() {
        def p = new ExternalProcessAppender(command: ["false"], restartLimit: 3, throwOnFailure: true)
        p.write(TESTSTRING)
        waitForChild(p)
        p.write(TESTSTRING)
        waitForChild(p)
        p.write(TESTSTRING)
        waitForChild(p)
        p.write(TESTSTRING)
        waitForChild(p)
        // unreachable
        assert false
    }

    @Test
    void testRestart() {
        testRestart(3, 15)
    }

    @Test(expected=Exception.class)
    void testRestartLimit() {
        testRestart(5, 3)
    }

    void testRestart(int restarts, int limit) {
        File runcount = temp.newFile('count')
        File output = temp.newFile('output')
        writeStringToFile(runcount, '0\n')
        String command ="""
            countfile=${path(runcount)}
            count=`cat "\$countfile"`
            if [ "\$count" -le ${restarts} ]
            then
                echo `expr "\$count" + 1` > "\$countfile"
                exit
            else
                cat > ${path(output)}
            fi"""
        def p = new ExternalProcessAppender(command: sh(command), restartLimit: limit, throwOnFailure: true)
        int count = -1
        while (count <= restarts) {
            p.write(TESTSTRING)
            def countstr = readFileToString(runcount).trim()
            if (countstr) count = Integer.parseInt(countstr)
        }
        p.write(TESTSTRING)
        p.close()
        waitForChild(p)

        // restarting a child process may lose some messages, so we can not be sure of how many copies of TESTSTRING
        // there are
        assertThat readFileToString(output), containsString(TESTSTRING)
    }
}
