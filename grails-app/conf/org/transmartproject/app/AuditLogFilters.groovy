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
                auditLogService.report("Summary Statistics", request,
                        action: params.concept_key,
                        user: currentUserBean,
                        study: actionName,
                        subset1: params.result_instance_id1,
                        subset2: params.result_instance_id2,
                )
            }
        }
        chart(controller: 'RWG', action: 'getFacetResults') {
            before = { model ->
                auditLogService.report("Clinical Data Active Filter", request,
                        action: searchString,
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
