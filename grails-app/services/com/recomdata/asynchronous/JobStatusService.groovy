package com.recomdata.asynchronous

import com.recomdata.transmart.domain.i2b2.AsyncJob
import groovy.time.TimeCategory
import groovy.time.TimeDuration

class JobStatusService {

    boolean transactional = true

    def jobResultsService

    /**
     * Helper to update the status of the job and log it
     *
     * @param jobName - the unique job name
     * @param status - the new status
     * @param viewerURL - optional, store the viewer URL if the job is completed
     * @param altViewerURL - optional, store the alternate viewer URL for CMS heatmaps
     * @param results - optional, store the results from survival analysis, haploview, etc.
     *
     * @return true if the job was cancelled
     */
    def updateStatus(jobName, status, viewerURL = null, altViewerURL = null, results = null) {
        def retValue = false   // true if the job was cancelled
        def jobNameArray = jobName.split("-")
        def jobID = jobNameArray[2]

        //log.debug("Checking to see if the user cancelled the job")
        if (jobResultsService[jobName]["Status"] == "Cancelled") {
            log.warn("${jobName} has been cancelled")
            retValue = true
        } else {
            jobResultsService[jobName]["Status"] = status
        }

        //If the job isn't already cancelled, update the job info.
        if (!retValue) {
            def asyncJob = AsyncJob.get(jobID)

            TimeDuration td = TimeCategory.minus(new Date(), asyncJob.lastRunOn)
            //log.debug("Job has been running for ${td}}")
            asyncJob.runTime = td
            asyncJob.jobStatus = status
            if (viewerURL && viewerURL != '') asyncJob.viewerURL = viewerURL
            if (altViewerURL && altViewerURL != '' && asyncJob.altViewerURL != null) asyncJob.altViewerURL = altViewerURL
            if (results && results != '') asyncJob.results = results

            //We need to flush so that the value doesn't overwrite cancelled when the controller finishes.
            asyncJob.save(flush: true)
        }

        return retValue
    }
}
