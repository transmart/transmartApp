package org.transmartproject.app

import org.transmartproject.core.users.User

class AuditLogFilters {

    def accessLogService
    User currentUserBean

    def filters = {
        dataExport(controller: 'dataExport', action:'runDataExport') {
            after = { model ->
                def ip = request.getHeader('X-FORWARDED-FOR') ?: request.remoteAddr
                accessLogService.report(currentUserBean, 'Data Export',
                    eventMessage: "User (IP: ${ip}) requested export of data. Http request parameters: ${params}")
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
    }

}
