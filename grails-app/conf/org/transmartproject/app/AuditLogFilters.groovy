package org.transmartproject.app

import org.transmartproject.core.users.User

class AuditLogFilters {

    def accessLogService
    def auditLogService
    def studyIdService
    User currentUserBean

    def filters = {
        dataExport(controller: 'dataExport', action:'runDataExport') {
            after = { model ->
                def ip = request.getHeader('X-FORWARDED-FOR') ?: request.remoteAddr
                def fullUrl = "${request.forwardURI}${request.queryString ? '?' + request.queryString : ''}"
                accessLogService.report(currentUserBean, 'Data Export',
                    eventMessage: "User (IP: ${ip}) requested export of data. Http request parameters: ${params}",
                    requestURL: fullUrl)
                auditLogService.report("Data Export", request,
                    action: params.searchString,
                    user: currentUserBean,
                    url: fullUrl as String,
                )
            }
        }
        chart(controller: 'chart', action: '*', actionExclude:'clearGrid|displayChart') {
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
                def task = "Summary Statistics (${actionName})"
                auditLogService.report(task, request,
                        study: studies,
                        user: currentUserBean,
                        subset1: result_instance_id1,
                        subset2: result_instance_id2,
                )
            }
        }
        chart(controller: 'RWG', action: 'getFacetResults') {
            before = { model ->
                auditLogService.report("Clinical Data Active Filter", request,
                        action: params.searchString,
                        user: currentUserBean,
                )
            }
        }
        userlanding(controller: 'userLanding', action: '*', actionExclude:'checkHeartBeat') {
            before = { model ->
                auditLogService.report("User Access", request,
                        action: actionName,
                        user: currentUserBean,
                )
            }
        }
        oauth(controller: 'oauth', action: '*') {
            before = { model ->
                auditLogService.report("OAuth authentication", request,
                        action: actionName,
                        user: currentUserBean,
                )
            }
        }
    }

}
