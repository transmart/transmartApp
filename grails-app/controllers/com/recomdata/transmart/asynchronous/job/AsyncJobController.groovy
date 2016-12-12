package com.recomdata.transmart.asynchronous.job

import com.recomdata.genepattern.JobStatus
import com.recomdata.genepattern.WorkflowStatus
import grails.converters.JSON
import org.json.JSONObject;
import org.transmartproject.core.users.User
import org.transmartproject.core.log.AccessLogEntryResource

class AsyncJobController {
    def quartzScheduler
    def springSecurityService
    def i2b2HelperService
    def jobResultsService
    def dataSource
    def asyncJobService

    AccessLogEntryResource accessLogService
    def auditLogService
    def studyIdService
    User currentUserBean

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

        def studies = getStudyIds(params)
        def workflow = getWorkflow(params)

        def result = asyncJobService.createnewjob(params)

        auditLogService.report("Run advanced workflow", request,
                               user: currentUserBean,
                               study: studies,
                               jobname: result.jobName,
                               workflow: workflow
                              )

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

    def getWorkflow = {
        if (params.jobType != null)
            return params.jobType

        if (params.analysisConstraints?.job_type != null)
            return params.analysisConstraints.job_type

        return "unknownWorkflow"
    }

    def getStudyIds = {
        String concept_key
        String concept_table

        Set<String> studyIds = []

        if (params.analysisConstraints != null) {
            def analysisConstraints = params.analysisConstraints

            def jsonAnalysisConstraints = JSON.parse(params.analysisConstraints)
            if (jsonAnalysisConstraints.assayConstraints?.patient_set != null)
                studyIds += studyIdService.getStudyIdsForQueries(jsonAnalysisConstraints.assayConstraints.patient_set);
        }

        // for concept paths we have to make sure they start with \\top node
        // note the string is escaped so we are adding a double backslash at the start
        if (params.independentVariable != null) {
            concept_key = params.independentVariable.split("\\|")[0]
            concept_table = concept_key.split("\\\\")[1]
            studyIds += studyIdService.getStudyIdForConceptKey('\\\\' + concept_table + concept_key)
        }

        if (params.dependentVariable != null) {
            concept_key = params.dependentVariable.split("\\|")[0]
            concept_table = concept_key.split("\\\\")[1]
            studyIds += studyIdService.getStudyIdForConceptKey('\\\\' + concept_table + concept_key)
        }

        if (params.variablesConceptPaths != null) {
            concept_key = params.variablesConceptPaths.split("\\|")[0]
            concept_table = concept_key.split("\\\\")[1]
            studyIds += studyIdService.getStudyIdForConceptKey('\\\\' + concept_table + concept_key)
        }

        List<String> studyIdList = studyIds as List
        studyIdList.sort()
        studyIdList.join(',')
    }
}
