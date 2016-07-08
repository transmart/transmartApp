package com.recomdata.transmart.data.export

import grails.converters.JSON
import org.transmartproject.core.exceptions.AccessDeniedException
import org.transmartproject.core.exceptions.InvalidArgumentsException
import org.transmartproject.core.users.User

import java.util.regex.Matcher
import java.util.regex.Pattern

class DataExportController {

    def exportService
    def exportMetadataService
    def springSecurityService
    User currentUserBean
    def dataExportService

    private static final String ROLE_ADMIN = 'ROLE_ADMIN'

    def index = {}

    //We need to gather a JSON Object to represent the different data types.
    def getMetaData() {
        List<Long> resultInstanceIds = parseResultInstanceIds()
        checkRightsToExport(resultInstanceIds)

        render exportMetadataService.getMetaData(
                resultInstanceIds[0],
                resultInstanceIds[1]) as JSON
    }

    def downloadFileExists() {
        checkJobAccess params.jobname

        def InputStream inputStream = exportService.downloadFile(params);
        def result = [:]

        if (inputStream) {
            result.fileStatus = true
            inputStream.close()
        } else {
            result.fileStatus = false
            result.message = "Download failed as file could not be found on the server"
        }

        render result as JSON
    }

    def downloadFile() {
        checkJobAccess params.jobname

        def InputStream inputStream = exportService.downloadFile(params)

        def fileName = params.jobname + ".zip"
        response.setContentType 'application/zip'
        response.setHeader "Content-disposition", "attachment;filename=${fileName}"
        response.outputStream << inputStream
        response.outputStream.flush()
        inputStream.close();
        return true;
    }

    /**
     * Method that will create the new asynchronous job name
     * Current methodology is username-jobtype-ID from sequence generator
     */
    def createnewjob() {
        def result = exportService.createExportDataAsyncJob(params, springSecurityService.getPrincipal().username)

        response.setContentType("text/json")
        response.outputStream << result.toString()
    }

    /**
     * Method that will run a data export and is called asynchronously from the datasetexplorer -> Data Export tab
     */
    def runDataExport() {
        checkRightsToExport(parseResultInstanceIds())

        def jsonResult = exportService.exportData(params, currentUserBean.username)

        response.setContentType("text/json")
        response.outputStream << jsonResult.toString()
    }

    def isCurrentUserAllowedToExport() {
        boolean isAllowed = dataExportService
                .isUserAllowedToExport(currentUserBean, parseResultInstanceIds())
        render([result: isAllowed] as JSON)
    }

    private List<Long> parseResultInstanceIds() {
        List<Long> result = []
        int subsetNumber = 1
        while (params.containsKey('result_instance_id' + subsetNumber)) {
            result << params.long('result_instance_id' + subsetNumber)
            subsetNumber += 1
        }
        result
    }

    private void checkRightsToExport(List<Long> resultInstanceIds) {
        if (!resultInstanceIds) {
            throw new InvalidArgumentsException("No result instance id provided")
        }

        if (!dataExportService
                .isUserAllowedToExport(currentUserBean,
                    resultInstanceIds)) {
            throw new AccessDeniedException("User ${currentUserBean.username} has no EXPORT permission" +
                    " on one of the result sets: ${resultInstanceIds}")
        }
    }

    private checkJobAccess(String jobName) {
        if (isAdmin()) {
            return
        }

        String loggedInUsername = springSecurityService.principal.username
        String jobUsername = extractUserFromJobName(jobName)

        if (jobUsername != loggedInUsername) {
            log.warn("Denying access to job $jobName because the " +
                    "corresponding username ($jobUsername) does not match " +
                    "that of the current user")
            throw new AccessDeniedException("Job $jobName was not started by " +
                    "this user")
        }
    }

    // copied from Rmodules' AnalysisFilesController
    // Job control and file download access ought to be unified
    private boolean isAdmin() {
        springSecurityService.principal.authorities.any {
            it.authority == ROLE_ADMIN
        }
    }

    static private String extractUserFromJobName(String jobName) {
        Pattern pattern = ~/(.+)-[a-zA-Z]+-\d+/
        Matcher matcher = pattern.matcher(jobName)

        if (!matcher.matches()) {
            throw new IllegalStateException('Invalid job name')
        }

        matcher.group(1)
    }
}


