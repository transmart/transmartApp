package org.transmartproject.app

import org.transmartproject.core.users.User

class AuditLogFilters {

    def accessLogService
    User currentUserBean

    def filters = {
        dataExport(controller: 'dataExport', action:'runDataExport') {
            after = { model ->
                accessLogService.report(currentUserBean, 'Data Export',
                    eventMessage: "User (IP: ${request.remoteAddr}) requested export of data. Http request parameters: ${params}",
                    requestURL: "${request.forwardURI}${request.queryString ? '?' + request.queryString : ''}")
            }
        }
    }

}
