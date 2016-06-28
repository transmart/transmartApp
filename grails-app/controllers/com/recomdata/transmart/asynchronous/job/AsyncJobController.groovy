package com.recomdata.transmart.asynchronous.job

import com.recomdata.genepattern.JobStatus
import com.recomdata.genepattern.WorkflowStatus
import grails.converters.JSON
import org.json.JSONObject;

class AsyncJobController {
    def quartzScheduler
    def springSecurityService
    def i2b2HelperService
    def jobResultsService
    def dataSource
    def asyncJobService

    static String ASYNC_JOB_WHITE_SPACE_DEFAULT = "0";
    static String ASYNC_JOB_WHITE_SPACE_EMPTY = "";

    /**
     * Method that will create the get the list of jobs to show in the jobs tab
     */
    def getjobs = {
        def result = asyncJobService.getjobs(params.jobType)

        response.setContentType("text/json")
        response.outputStream << result?.toString()
    }

    /**
     * get job stats by name
     */
    def getjobbyname = {

        println(params.jobName)

        def result = asyncJobService.getjobbyname(params.jobName)

        response.setContentType("text/json")
        response.outputStream << result?.toString()
    }

    /**
     * Called to retrieve the job results (HTML) stored in the JOB_RESULTS field for Haploview and Survival Analysis
     */
    def getjobresults = {
        def result = asyncJobService.getjobresults(params.jobName)
        response.setContentType("text/json")
        response.outputStream << result?.toString()
    }

    def createnewjob = {
        def result = asyncJobService.createnewjob(params)

        response.setContentType("text/json")
        response.outputStream << result?.toString()
    }

    /**
     * Method called that will cancel a running job
     */
    def canceljob = {
        def result = asyncJobService.canceljob(params.jobName, params.group)

        response.setContentType("text/json")
        response.outputStream << result?.toString()
    }

    /**
     * Repeatedly called by datasetExplorer.js to get the job status and results
     */
    def checkJobStatus = {
        def result = asyncJobService.checkJobStatus(params.jobName, params.jobType)

        def statusIndexExists = result.get('statusIndexExists')
        if (statusIndexExists) {
            def statusIndex = result.get('statusIndex')
            def statusHtml = g.render(template: "/genePattern/jobStatusList", model: [jobStatuses: jobResultsService[params.jobName]["StatusList"], statusIndex: statusIndex]).toString();
            result.put('jobStatusHTML', statusHtml)

            result.remove('statusIndex')
            result.remove('statusIndexExists')
        }

        response.setContentType("text/json")
        response.outputStream << result?.toString()
    }

    /**
     * Shows the job status window
     */
    def showJobStatus = {
        render(view: "/genePattern/workflowStatus", model: [:])
    }

    /**
     * for snp viewer and igv
     */

    def showWorkflowStatus = {
        def wfstatus = session["workflowstatus"];
        if (wfstatus == null) {
            wfstatus = new WorkflowStatus();
            session["workflowstatus"] = wfstatus;
            session["workflowstatus"].setCurrentJobStatus(new JobStatus(name: "initializing Workflow", status: "R"));
        }

        render(view: "/genePattern/workflowStatus");
    }


    def checkWorkflowStatus = {
        // check session status
        def wfstatus = session["workflowstatus"];

        JSONObject result = wfstatus.result;
        if (result == null) {
            result = new JSONObject();
        }

        def statusHtml = g.render(template: "/genePattern/jobStatus", model: [wfstatus: wfstatus]).toString();
        result.put("statusHTML", statusHtml);
        println(statusHtml);

        if (wfstatus.isCompleted()) {
            result.put("wfstatus", "completed");
            wfstatus.rpCount++;
            result.put("rpCount", wfstatus.rpCount);
        } else {
            result.put("wfstatus", "running");
        }
        render result.toString();
    }

    def cancelJob = {
        def wfstatus = session["workflowstatus"]
        wfstatus.setCancelled();
        render(wfstatus.jobStatusList as JSON)
    }
}
