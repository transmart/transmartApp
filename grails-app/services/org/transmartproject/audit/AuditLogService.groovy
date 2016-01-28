package org.transmartproject.audit

import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import javax.servlet.http.HttpServletRequest
import org.apache.commons.lang.time.FastDateFormat
import org.apache.log4j.MDC
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.transmartproject.core.users.User

/**
 * Logs to level TRACE in a specific `metric' format.
 * Configuration of output done in (out-of-tree) Config.groovy.
 */
@CompileStatic
@Log4j
class AuditLogService {

    GrailsApplication grailsApplication

    static final FastDateFormat isoFormat = FastDateFormat.getInstance "yyyy-MM-dd'T'HH:mm:ssZ", TimeZone.default

    def report(Map<String, Object> params = [:], String task, HttpServletRequest request) {
        String action = params.action
        User user = (User) params.user
        if (MDC.get("AuditLogService") == null) {
            MDC.put "AuditLogService", [:]
        }
        Map mdc = (Map) MDC.get("AuditLogService")

        mdc.put 'appVersion', grailsApplication?.metadata?."app.version" ?: ''
        mdc.put 'userAgent', request.getHeader('user-agent')

        params.each { k, v -> mdc.put k, v }

        mdc.put 'user', user.username
        mdc.put 'task', task
        mdc.put 'action', action
        mdc.put 'timestamp', isoFormat.format(new Date())

        try {
            log.trace "${task}: ${action}"
        } finally {
            MDC.remove("AuditLogService")
        }
    }
}
