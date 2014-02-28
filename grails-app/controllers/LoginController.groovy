/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 * 
 *
 ******************************************************************/

import grails.converters.JSON
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

import javax.servlet.http.HttpServletResponse

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
            redirect action: 'auth', params: params
        }
    }

    def forceAuth = {
        session.invalidate();
        def config = SpringSecurityUtils.securityConfig
        render view: 'auth', model: [postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
    }

    /**
     * Show the login page.
     */
    def auth = {

        def config = SpringSecurityUtils.securityConfig
        def guestUser= grailsApplication.config.com.recomdata.guestUserName
        def autoLogin = ('true' == grailsApplication.config.com.recomdata.guestAutoLogin)
        def forceLogin = request.getQueryString() != null

        nocache response
        log.info("Enabled guest login?: " + autoLogin);
        log.info("User is forcing the form login?: " + forceLogin);

        if (autoLogin && !forceLogin) {

            log.info("Proceeding with auto guest login")
            def user = userDetailsService.findByUsername(guestUser)
            if (user != null) {
                springSecurityService.reauthenticate(user.username)
            } else {
                log.error("Cannot find the guest user");
            }
        }

        if (springSecurityService.isLoggedIn()) {
            redirect uri: config.successHandler.defaultTargetUrl
            return
        }

        String view = 'auth'
        String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
        render view: view, model: [postUrl: postUrl,
                rememberMeParameter: config.rememberMe.parameter]
    }

    /**
     * The redirect action for Ajax requests.
     */
    def authAjax = {
        response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
        response.sendError HttpServletResponse.SC_UNAUTHORIZED
    }

    /**
     * Show denied page.
     */
    def denied = {
        if (springSecurityService.isLoggedIn() &&
                authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
            // have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
            redirect action: 'full', params: params
        }
    }

    /**
     * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
     */
    def full = {
        def config = SpringSecurityUtils.securityConfig
        render view: 'auth', params: params,
                model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
                        postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
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
                msg = g.message(code: "springSecurity.errors.login.expired")
                new AccessLog(username: username, event:"Account Expired", eventmessage: msg, accesstime:new Date()).save()
            } else if (exception instanceof CredentialsExpiredException) {
                msg = g.message(code: "springSecurity.errors.login.passwordExpired")
                new AccessLog(username: username, event:"Password Expired", eventmessage: msg, accesstime:new Date()).save()
            } else if (exception instanceof DisabledException) {
                msg = g.message(code: "springSecurity.errors.login.disabled")
                new AccessLog(username: username, event:"Login Disabled", eventmessage: msg, accesstime:new Date()).save()
            } else if (exception instanceof LockedException) {
                msg = g.message(code: "springSecurity.errors.login.locked")
                new AccessLog(username: username, event:"Login Locked", eventmessage: msg, accesstime:new Date()).save()
            } else {
                msg = g.message(code: "springSecurity.errors.login.fail")
                new AccessLog(username: username, event:"Login Failed", eventmessage: msg, accesstime:new Date()).save()
            }
        }

        if (springSecurityService.isAjax(request)) {
            render([error: msg] as JSON)
        } else {
            flash.message = msg
            redirect action: 'auth', params: params
        }
    }

    /** cache controls */
    private void nocache(response) {
        response.setHeader('Cache-Control', 'no-cache')
        response.addDateHeader('Expires', 0)
        response.setDateHeader('max-age', 0)
        response.setIntHeader ('Expires', -1)
        response.addHeader('cache-Control', 'private')
        response.addHeader('Pragma', 'no-cache')
    }
}
