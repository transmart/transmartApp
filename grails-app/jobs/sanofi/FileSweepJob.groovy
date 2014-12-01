package sanofi

import grails.util.Holders
import org.apache.commons.logging.LogFactory

class FileSweepJob {

    def sweepingService
    private static def log = LogFactory.getLog(this)

    //def timeout = 5000l

    static triggers = {
        def startDelay = Holders.config.com.recomdata.export.jobs.sweep.startDelay
        def repeatInterval = Holders.config.com.recomdata.export.jobs.sweep.repeatInterval
        if (startDelay instanceof String) {
            try {
                startDelay = Integer.parseInt(startDelay)
                repeatInterval = Integer.parseInt(repeatInterval)
            } catch (NumberFormatException nfe) {
                // do nothing
            }
        }
        if (startDelay instanceof Integer) {
            simple name: 'fileSweepTrigger', startDelay: startDelay, repeatInterval: repeatInterval
        }
    }

    def execute() {
        sweepingService.sweep()
    }
}
