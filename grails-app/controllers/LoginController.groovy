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
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.WebAttributes
import org.transmart.searchapp.AccessLog

/**
 * Login Controller
 */
class LoginController {

    /**
     * Dependency injection for the authenticationTrustResolver.
     */
    def authenticationTrustResolver
    def bruteForceLoginLockService

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
        log.info("enable guest login: " + guestLoginEnabled)
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
        String msg = ''
        def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
        String username = null
        if (exception instanceof AuthenticationException) {
            username = exception.authentication.name
        }
        if (exception) {
            if (exception instanceof AccountExpiredException) {
                msg = g.message(code: "springSecurity.errors.login.expired")
                new AccessLog(username: username, event: "Account Expired",
                        eventmessage: msg,
                        accesstime: new Date()).save()
            } else if (exception instanceof CredentialsExpiredException) {
                msg = g.message(code: "springSecurity.errors.login.passwordExpired")
                new AccessLog(username: username, event: "Password Expired",
                        eventmessage: msg,
                        accesstime: new Date()).save()
            } else if (exception instanceof DisabledException) {
                msg = g.message(code: "springSecurity.errors.login.disabled")
                new AccessLog(username: username, event: "Login Disabled",
                        eventmessage: msg,
                        accesstime: new Date()).save()
            } else if (exception instanceof LockedException
                    //Extra condition to escape confusion with last login attempt that would be ignored anyway
                    // because user would be locked at that time.
                    //That's confusion caused by the fact that spring event listener for failed attempt is triggered
                    // after user status (e.g. locked) is red by spring security.
                    || username && bruteForceLoginLockService.remainedAttempts(username) <= 0) {
                msg = g.message(code: "springSecurity.errors.login.locked",
                        args: [ bruteForceLoginLockService.lockTimeInMinutes ])
                new AccessLog(username: username, event: "Login Locked",
                        eventmessage: msg,
                        accesstime: new Date()).save()
            } else {
                msg = g.message(code: "springSecurity.errors.login.fail")
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
