package com.recomdata.transmart.data.export

import com.recomdata.transmart.domain.i2b2.AsyncJob

class SweepingService {

    boolean transactional = true

    def grailsApplication

    def sweep() {
        log.info "Triggering file sweep"
        def fileAge = grailsApplication.config.com.recomdata.export.jobs.sweep.fileAge;
        def now = new Date()
        def c = AsyncJob.createCriteria()
        def jobList = c.list {
            eq("jobType", "DataExport")
            eq("jobStatus", "Completed")
            lt('lastRunOn', now - fileAge)
            //between('lastRunOn',now-fileAge, now)
        }

        def deleteDataFilesProcessor = new DeleteDataFilesProcessor()
        jobList.each { job ->
            if (deleteDataFilesProcessor.deleteDataFile(job.viewerURL, job.jobName)) {
                job.delete()
            }
        }
    }
}
