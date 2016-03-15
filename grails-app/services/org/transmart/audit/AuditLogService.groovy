package org.transmart.audit

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.transform.CompileStatic
import groovy.util.logging.Log4j
import javax.servlet.http.HttpServletRequest
import org.apache.log4j.Level
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.transmartproject.core.users.User

/**
 * Logs to level TRACE in a specific `metric' format.
 * Configuration of output done in (out-of-tree) Config.groovy.
 */
@CompileStatic
@Log4j
class AuditLogService {

    GrailsApplication grailsApplication

    // NB: gson uses SimpleDateFormat internally, which is quite slow. For heavy use that might need to be optimized
    // by e.g. importing joda time, or caching and recomputing time once per second the way log4j's
    // AbsoluteTimeDateFormat does.
    @Lazy
    static final private Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss.SSSX")
            .serializeNulls()
            .setPrettyPrinting()
            .create()

    @Lazy
    volatile boolean enabled = {
        log.effectiveLevel.toInt() <= Level.TRACE.toInt()
    }()

    def report(Map<String, Object> params = [:], String event, HttpServletRequest request) {
        User user = (User) params.user

        if (!enabled) return

        Map msg = [:]

        msg.program = "Transmart"
        msg.programVersion = grailsApplication?.metadata?."app.version" ?: ''
        // Written twice to ensure order, although nothing should depend on that
        msg.user = user.username
        msg.event = event

        msg.putAll(params)

        Authentication auth = SecurityContextHolder.context.authentication
        if (auth instanceof OAuth2Authentication) {
            OAuth2Authentication oauthAuth = (OAuth2Authentication) auth
            msg.clientId = oauthAuth.getOAuth2Request().clientId
        }

        // Override user and other fields
        msg.user = user.username
        msg.event = event
        msg.userAgent = request.getHeader 'user-agent'
        msg.timestamp = new Date()

        log.trace(msg)
    }
}
