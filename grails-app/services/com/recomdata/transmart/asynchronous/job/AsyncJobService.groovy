package com.recomdata.transmart.asynchronous.job

import com.recomdata.transmart.domain.i2b2.AsyncJob
import grails.transaction.Transactional
import groovy.json.JsonSlurper
import org.apache.commons.lang.StringUtils
import org.json.JSONArray
import org.json.JSONObject
import org.springframework.transaction.annotation.Propagation
import org.transmartproject.core.users.User

class AsyncJobService {

    boolean transactional = true

    def quartzScheduler
    def springSecurityService
    def jobResultsService
    def dataExportService

    /**
     * Method that will get the list of jobs to show in the jobs tab
     */
    def getjobs(jobType = null) {
        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()

        def userName = springSecurityService.getPrincipal().username
        def jobResults = null
        def c = AsyncJob.createCriteria()
        if (StringUtils.isNotEmpty(jobType)) {
            jobResults = c {
                like("jobName", "${userName}%")
                eq("jobType", "${jobType}")
                ge("lastRunOn", new Date() - 7)
                order("id", "desc")
            }
        } else {
            jobResults = c {
                like("jobName", "${userName}%")
                or {
                    ne("jobType", "DataExport")
                    isNull("jobType")
                }
                ge("lastRunOn", new Date() - 7)
                order("id", "desc")
            }
        }
        def m = [:]
        for (jobResult in jobResults) {
            m = [:]
            m["name"] = jobResult.jobName
            m["type"] = jobResult.jobType
            m["status"] = jobResult.jobStatus
            m["runTime"] = jobResult.jobStatusTime
            m["startDate"] = jobResult.lastRunOn
            m["viewerURL"] = jobResult.viewerURL
            m["altViewerURL"] = jobResult.altViewerURL
            rows.put(m)
        }

        result.put("success", true)
        result.put("totalCount", jobResults.size())
        result.put("jobs", rows)

        return result
    }

    /**
     * get job info by job name
     * @param jobName
     * @return
     */
    def getjobbyname(jobName = '') {

        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()
        def jobResults = null

        def c = AsyncJob.createCriteria()

        if (StringUtils.isNotEmpty(jobName)) {
            jobResults = c {
                like("jobName", "%${jobName}%")
            }
        }

        def m = [:]
        for (jobResult in jobResults) {
            m = [:]
            m["name"] = jobResult.jobName
            m["status"] = jobResult.jobStatus
            m["runTime"] = jobResult.runTime
            m["startDate"] = jobResult.lastRunOn
            m["viewerURL"] = jobResult.viewerURL
            m["altViewerURL"] = jobResult.altViewerURL
            m["jobInputsJson"] = new JSONObject(jobResult.jobInputsJson ?: "{}")
            rows.put(m)
        }

        result.put("success", true)
        result.put("totalCount", jobResults.size())
        result.put("jobs", rows)

        return result

    }

    /**
     * Called to retrieve the job results (HTML) stored in the JOB_RESULTS field for Haploview and Survival Analysis
     */
    def getjobresults(jobName) {
        JSONObject result = new JSONObject()
        def jobResults = AsyncJob.findByJobName(jobName).results
        result.put("jobResults", jobResults)
        return result
    }

    /**
     * Method that will create the new asynchronous job name
     * Current methodology is username-jobtype-ID from sequence generator
     */
    def createnewjob(params) {
        def userName = springSecurityService.getPrincipal().username
        def jobStatus = "Started"

        def newJob = new AsyncJob(lastRunOn: new Date())
        newJob.save()

        def jobName = params?.jobName
        if (StringUtils.isEmpty(jobName)) {
            def jobNameBuf = new StringBuffer(userName)
            jobNameBuf.append('-')
            if (StringUtils.isNotEmpty(params.jobType)) jobNameBuf.append(params.jobType)
            jobNameBuf.append('-').append(newJob.id)
            jobName = jobNameBuf.toString()
        }
        newJob.jobName = jobName
        newJob.jobType = params?.jobType
        newJob.jobStatus = jobStatus
        newJob.jobInputsJson = new JSONObject(params).toString()
        newJob.save()

        jobResultsService[jobName] = [:]
        updateStatus(jobName, jobStatus)

        log.debug("Sending ${jobName} back to the client")
        JSONObject result = new JSONObject()
        result.put("jobName", jobName)
        result.put("jobStatus", jobStatus)

        return result;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def updateJobInputs(final String jobName, final Map params) {
        assert jobName
        assert params

        def job = AsyncJob.findByJobName(jobName)
        assert "${jobName} job is not found.", job

        job.jobInputsJson = new JSONObject(params).toString()
        job.save(flush: true)
    }

    /**
     * Method called that will cancel a running job
     */
    def canceljob(jobName, group = null) {
        def jobStatus = "Cancelled"
        def result = null
        log.debug("Attempting to delete ${jobName} from the Quartz scheduler")
        result = quartzScheduler.deleteJob(jobName, group)
        log.debug("Deletion attempt successful? ${result}")

        updateStatus(jobName, jobStatus)

        JSONObject jsonResult = new JSONObject()
        jsonResult.put("jobName", jobName)
        return jsonResult
    }

    /**
     * Repeatedly called by datasetExplorer.js to get the job status and results
     */
    def checkJobStatus(jobName, jobType = null) {
        JSONObject result = new JSONObject()
        if (StringUtils.isEmpty(jobType)) {
            def jobNameArray = jobName.split("-")
            jobType = jobNameArray[1]
        }
        def jobStatus = jobResultsService[jobName]["Status"]
        def statusIndex = null
        if (jobResultsService[jobName]["StatusList"] != null) {
            statusIndex = jobResultsService[jobName]["StatusList"].indexOf(jobStatus)
        }
        def jobException = jobResultsService[jobName]["Exception"]
        def viewerURL = jobResultsService[jobName]["ViewerURL"]
        def altViewerURL = jobResultsService[jobName]["AltViewerURL"]
        def jobResults = jobResultsService[jobName]["Results"]
        def errorType = ""
        if (viewerURL != null) {
            def jobResultType = jobResultsService[jobName]["resultType"]
            if (jobResultType != null) result.put("resultType", jobResultType)
            log.debug("${viewerURL} is being sent to the client")
            result.put("jobViewerURL", viewerURL)
            if (altViewerURL != null) {
                log.debug("${altViewerURL} for Comparative Marker Selection")
                result.put("jobAltViewerURL", altViewerURL)
            }
            jobStatus = "Completed"
        } else if (jobResults != null) {
            result.put("jobResults", jobResults)
            result.put("resultType", jobType)
            jobStatus = "Completed"
        } else if (jobException != null) {
            log.warn("An exception was thrown, passing this back to the user")
            log.warn(jobException)
            result.put("jobException", jobException)
            jobStatus = "Error"
            errorType = "data"
        }
        if (statusIndex != null) {
            result.put('statusIndexExists', true)
            result.put("statusIndex", statusIndex)
        } else {
            result.put('statusIndexExists', false)
        }

        updateStatus(jobName, jobStatus, viewerURL, altViewerURL, jobResults)

        //log.debug("Returning status: ${jobStatus} for ${jobName}")
        result.put("jobStatus", jobStatus)
        result.put("errorType", errorType)
        result.put("jobName", jobName)

        return result
    }

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
            def asyncJob = AsyncJob.get(Long.parseLong(jobID))

            // TimeDuration td = TimeCategory.minus(new Date(), asyncJob.lastRunOn)
            //log.debug("Job has been running for ${td}}")
            //asyncJob.runTime = td
            asyncJob.jobStatus = status
            if (viewerURL && viewerURL != '') asyncJob.viewerURL = viewerURL
            if (altViewerURL && altViewerURL != '' && asyncJob.altViewerURL != null) asyncJob.altViewerURL = altViewerURL
            if (results && results != '') asyncJob.results = results
            jobResultsService[jobName]["ViewerURL"] = viewerURL
            //We need to flush so that the value doesn't overwrite cancelled when the controller finishes.
            asyncJob.save(flush: true)
        }

        return retValue
    }

    boolean isUserAllowedToExportResults(final User user, final String jobName) {
        assert user
        assert jobName

        def job = AsyncJob.findByJobName(jobName)
        assert "${jobName} is not found.", job

        job.jobInputsJson
        def jobInputsJsonObj = new JsonSlurper().parseText(job.jobInputsJson)

        List<Long> resultInstanceIds = []
        int subsetNumber = 1
        while (jobInputsJsonObj['result_instance_id' + subsetNumber]) {
            resultInstanceIds << (jobInputsJsonObj['result_instance_id' + subsetNumber] as Long)
            subsetNumber += 1
        }
        dataExportService.isUserAllowedToExport(user, resultInstanceIds)
    }

}
