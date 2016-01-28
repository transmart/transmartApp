package org.transmartproject.audit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import javax.servlet.http.HttpServletRequest
import org.apache.log4j.Level
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

    @Lazy
    static final Gson gson = {new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
            .serializeNulls()
            .setPrettyPrinting()
            .create()}()

    def report(Map<String, Object> params = [:], String task, HttpServletRequest request) {
        String action = (String) params.action
        User user = (User) params.user

        if (log.effectiveLevel.toInt() > Level.TRACE.toInt()) return

        Map msg = [:]

        msg.program = "Transmart"
        msg.programVersion = grailsApplication?.metadata?."app.version" ?: ''
        // Written twice to ensure order, although nothing should depend on that
        msg.user = user.username
        msg.task = task
        msg.action = action

        msg.putAll(params)

        // Override user and other fields
        msg.user = user.username
        msg.task = task
        msg.action = action
        msg.userAgent = request.getHeader 'user-agent'
        msg.timestamp = new Date()

        log.trace(gson.toJson(msg))
    }
}
