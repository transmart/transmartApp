package org.transmart

import org.springframework.security.core.Authentication
import org.springframework.security.kerberos.authentication.KerberosServiceRequestToken
import org.springframework.security.kerberos.web.authentication.ResponseHeaderSettingKerberosAuthenticationSuccessHandler
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Handler used to avoid ClassCastException (due to https://github.com/grails-plugins/grails-spring-security-kerberos/issues/3 ) if both kerberosServiceAuthenticationProvider and ldap provider are active
 */
class SavedRequestAwareAndResponseHeaderSettingKerberosAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    ResponseHeaderSettingKerberosAuthenticationSuccessHandler kerberosAuthenticationSuccessHandler = new ResponseHeaderSettingKerberosAuthenticationSuccessHandler()

    @Override
    void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        if (authentication instanceof KerberosServiceRequestToken) {
            kerberosAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication)
        } else {
            super.onAuthenticationSuccess(request, response, authentication)
        }
    }
}
