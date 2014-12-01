/**
 * $Id: LoginController.groovy 10098 2011-10-19 18:39:32Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 10098 $
 */
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.transmart.searchapp.AccessLog

/**
 * Login Controller
 */
class LoginController {

    /**
     * Dependency injection for the authenticationTrustResolver.
     */
    def authenticationTrustResolver

    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService
    def userDetailsService

    /**
     * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
     */
    def index = {
        if (springSecurityService.isLoggedIn()) {
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        } else {
            redirect action: "auth", params: params
        }
    }
    def forceAuth = {
        session.invalidate();
        render view: 'auth', model: [postUrl: request.contextPath + SpringSecurityUtils.securityConfig.apf.filterProcessesUrl]
    }

    /**
     * Show the login page.
     */
    def auth = {
        nocache response

        def guestAutoLogin = grailsApplication.config.com.recomdata.guestAutoLogin;
        boolean guestLoginEnabled = (guestAutoLogin == 'true' || guestAutoLogin.is(true))
        log.info("enabled guest login")
        //log.info("requet:"+request.getQueryString())
        boolean forcedFormLogin = request.getQueryString() != null
        log.info("User is forcing the form login? : " + forcedFormLogin)

        // if enabled guest and not forced login
        if (guestLoginEnabled && !forcedFormLogin) {
            log.info("proceeding with auto guest login")
            def guestuser = grailsApplication.config.com.recomdata.guestUserName;

            UserDetails ud = userDetailsService.loadUserByUsername(guestuser)
            if (ud != null) {
                log.debug("We have found user: ${ud.username}")
                springSecurityService.reauthenticate(ud.username)
                redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl

            } else {
                log.info("can not find the user:" + guestuser);
            }
        }

        /*if (springSecurityService.isLoggedIn()) {
			redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
		} else	{
            render view: 'auth', model: [postUrl: request.contextPath + SpringSecurityUtils.securityConfig.apf.filterProcessesUrl]
        }*/
        render view: 'auth', model: [postUrl: request.contextPath + SpringSecurityUtils.securityConfig.apf.filterProcessesUrl]
    }

    /**
     * Show denied page.
     */
    def denied = {
        if (springSecurityService.isLoggedIn() &&
                authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
            // have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
            redirect action: "full", params: params
        }
    }

    /**
     * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
     */
    def full = {
        render view: 'auth', params: params,
                model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
                        postUrl  : request.contextPath + SpringSecurityUtils.securityConfig.apf.filterProcessesUrl]
    }

    /**
     * Callback after a failed login. Redirects to the auth page with a warning message.
     */
    def authfail = {
        def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
        String msg = ''
        def exception = session[AbstractAuthenticationProcessingFilter.SPRING_SECURITY_LAST_EXCEPTION_KEY]
        if (exception) {
            if (exception instanceof AccountExpiredException) {
                msg = SpringSecurityUtils.securityConfig.errors.login.expired
                new AccessLog(username: username, event: "Account Expired",
                        eventmessage: msg,
                        accesstime: new Date()).save()
            } else if (exception instanceof CredentialsExpiredException) {
                msg = SpringSecurityUtils.securityConfig.errors.login.passwordExpired
                new AccessLog(username: username, event: "Password Expired",
                        eventmessage: msg,
                        accesstime: new Date()).save()
            } else if (exception instanceof DisabledException) {
                msg = SpringSecurityUtils.securityConfig.errors.login.disabled
                new AccessLog(username: username, event: "Login Disabled",
                        eventmessage: msg,
                        accesstime: new Date()).save()
            } else if (exception instanceof LockedException) {
                msg = SpringSecurityUtils.securityConfig.errors.login.locked
                new AccessLog(username: username, event: "Login Locked",
                        eventmessage: msg,
                        accesstime: new Date()).save()
            } else {
                msg = SpringSecurityUtils.securityConfig.errors.login.fail
                new AccessLog(username: username, event: "Login Failed",
                        eventmessage: msg,
                        accesstime: new Date()).save()
            }
        }
        flash.message = msg
        redirect action: "auth", params: params
    }

    /** cache controls */
    private void nocache(response) {
        response.setHeader('Cache-Control', 'no-cache') // HTTP 1.1
        response.addDateHeader('Expires', 0)
        response.setDateHeader('max-age', 0)
        response.setIntHeader('Expires', -1) //prevents caching at the proxy server
        response.addHeader('cache-Control', 'private') //IE5.x only
    }
}
