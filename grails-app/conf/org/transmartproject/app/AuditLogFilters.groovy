package org.transmartproject.app

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.*
import org.transmartproject.core.users.User
import org.transmartproject.core.log.AccessLogEntryResource

class AuditLogFilters {

    AccessLogEntryResource accessLogService
    def auditLogService
    def studyIdService
    User currentUserBean

    /**
     * Get export types from the <var>selectedSubsetDataTypeFiles</var>
     * field in the parameters.
     * Returns a list containing 'LDD' if the parameter contains data type 'CLINICAL'
     * and 'HDD' if any other data type is in the parameter.
     *
     * @param params
     * @return a list containing 'LDD', 'HDD', or both.
     */
    private List<String> getExportTypes(params) {
        def param = params.selectedSubsetDataTypeFiles
        def dataFiles = param instanceof String ?
                [JSON.parse(param)] :
                param.collect { JSON.parse(it) }
        Set<String> dataTypes = dataFiles*.dataTypeId as Set
        List<String> exportTypes = []
        if ('CLINICAL' in dataTypes) {
            exportTypes += 'LDD'
        }
        if ((dataTypes - 'CLINICAL').size() > 0) {
            exportTypes += 'HDD'
        }
        exportTypes
    }

    def filters = {
        dataExport(controller: 'dataExport', action:'runDataExport') {
            after = { model ->
                def ip = request.getHeader('X-FORWARDED-FOR') ?: request.remoteAddr
                def fullUrl = "${request.forwardURI}${request.queryString ? '?' + request.queryString : ''}"
                def result_instance_id1 = params.result_instance_id1 ?: ''
                def result_instance_id2 = params.result_instance_id2 ?: ''
                def studies = studyIdService.getStudyIdsForQueries([result_instance_id1, result_instance_id2])
                def exportTypes = getExportTypes(params).join('+')

                accessLogService.report(currentUserBean, 'Data Export',
                    eventMessage: "User (IP: ${ip}) requested export of data. Http request parameters: ${params}",
                    requestURL: fullUrl)
                auditLogService.report("Clinical Data Exported - ${exportTypes}", request,
                    study: studies,
                    user: currentUserBean
                )
            }
        }
        dataExport(controller: 'dataExport', action:'downloadFile') {
            after = { model ->
                def ip = request.getHeader('X-FORWARDED-FOR') ?: request.remoteAddr
                accessLogService.report(currentUserBean, 'Data Export',
                        eventMessage: "User (IP: ${ip}) downloaded an exported file.",
                        requestURL: "${request.forwardURI}${request.queryString ? '?' + request.queryString : ''}")
            }
        }
        chart(controller: 'chart', action: '*', actionExclude:'clearGrid|displayChart|childConceptPatientCounts') {
            before = { model ->
                if (!auditLogService.enabled) return
                def result_instance_id1 = params.result_instance_id1 ?: ''
                def result_instance_id2 = params.result_instance_id2 ?: ''
                def studies = ''
                if (params.concept_key) {
                    studies = studyIdService.getStudyIdForConceptKey(params.concept_key) ?: ''
    }
                if (studies.empty) {
                    studies = studyIdService.getStudyIdsForQueries([result_instance_id1, result_instance_id2])
                }
                def task = "Summary Statistics"
                if (actionName == "childConceptPatientCounts") {
                    task = "Clinical Data Access"
                }
                auditLogService.report(task, request,
                        study: studies,
                        user: currentUserBean,
                        subset1: result_instance_id1,
                        subset2: result_instance_id2
                )
            }
        }
        concepts(controller: 'concepts', action: 'getChildren') {
            before = { model ->
                if (!auditLogService.enabled) return
                def studies = null
                if (params.concept_key) {
                    studies = studyIdService.getStudyIdForConceptKey(params.concept_key, studyConceptOnly: true)
                }
                if (studies == null) return
                def task = "Clinical Data Access"
                auditLogService.report(task, request,
                        study: studies,
                        user: currentUserBean
                )
            }
        }
        rwg(controller: 'RWG', action: 'getFacetResults') {
            before = { model ->
                auditLogService.report("Clinical Data Active Filter", request,
                        query: params.searchTerms,
                        user: currentUserBean,
                )
            }
        }
        userlanding(controller: 'userLanding', action: '*', actionExclude:'checkHeartBeat') {
            before = { model ->
                auditLogService.report("User Access", request,
                        user: currentUserBean,
                )
            }
        }
        oauth(controller: 'oauth', action: '*') {
            before = { model ->
                auditLogService.report("OAuth authentication", request,
                        user: currentUserBean,
                )
            }
        }
    }

}
