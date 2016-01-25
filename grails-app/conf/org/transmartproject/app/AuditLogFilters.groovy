package org.transmartproject.app

import org.transmartproject.core.users.User

class AuditLogFilters {

    def accessLogService
    def auditLogService
    User currentUserBean

    def filters = {
        dataExport(controller: 'dataExport', action:'runDataExport') {
            after = { model ->
                def ip = request.getHeader('X-FORWARDED-FOR') ?: request.remoteAddr
                accessLogService.report(currentUserBean, 'Data Export',
                    eventMessage: "User (IP: ${ip}) requested export of data. Http request parameters: ${params}",
                    requestURL: "${request.forwardURI}${request.queryString ? '?' + request.queryString : ''}")
            }
        }
        chart(controller: 'chart', action: '*') {
            before = { model ->
                def result_instance_id1 = params.result_instance_id1
                def result_instance_id2 = params.result_instance_id2
                def concept_key = params.concept_key
                def task = "Summary Statistics (${actionName})"
                def action = "${concept_key}|${result_instance_id1}|${result_instance_id2}"
                auditLogService.report(currentUserBean, task, action)
            }
        }
        chart(controller: 'RWG', action: 'getFacetResults') {
            before = { model ->
                def task = "Clinical Data Active Filter"
                def action = searchString
                auditLogService.report(currentUserBean, task, action)
            }
        }
        oauth(controller: 'oauth', action: '*') {
            before = { model ->
                auditLogService.report(currentUserBean, "OAuth authentication", actionName)
            }
        }
    }

}
