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

import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.authentication.session.ConcurrentSessionControlStrategy
import org.springframework.security.web.session.ConcurrentSessionFilter

beans = {

    println '\nConfiguring tranSMART Beans ...'

    def conf = SpringSecurityUtils.securityConfig
    String[] attributesToReturn = toStringArray(conf.ldap.authenticator.attributesToReturn)
    String[] dnPatterns = toStringArray(conf.ldap.authenticator.dnPatterns)

    ldapCustomAuthenticator(com.recomdata.security.LdapAuthUserAuthenticator, ref("contextSource")) {
        userSearch = ref("ldapUserSearch")
        if (attributesToReturn) {
            userAttributes = attributesToReturn
        }
        if (dnPatterns) {
            userDnPatterns = dnPatterns
        }
    }
    ldapUserDetailsMapper(com.recomdata.security.LdapAuthUserDetailsMapper){
        dataSource = ref('dataSource')
        springSecurityService = ref('springSecurityService')
    }
    ldapAuthProvider(org.springframework.security.ldap.authentication.LdapAuthenticationProvider, ldapCustomAuthenticator, ref("ldapAuthoritiesPopulator")) {
        userDetailsContextMapper = ldapUserDetailsMapper
        hideUserNotFoundExceptions = Boolean.parseBoolean((String)conf.ldap.auth.hideUserNotFoundExceptions)
        useAuthenticationRequestCredentials = Boolean.parseBoolean((String)conf.ldap.auth.useAuthPassword)
    }

    if (grailsApplication.config.org.transmart.security.samlEnabled) {
        importBeans('classpath:/spring/spring-security-saml.xml')
    }

    dataSourcePlaceHolder(com.recomdata.util.DataSourcePlaceHolder) {
		dataSource = ref('dataSource')
	}
	sessionRegistry(SessionRegistryImpl)
	sessionAuthenticationStrategy(ConcurrentSessionControlStrategy, sessionRegistry) {
		maximumSessions = 10
	}
	concurrentSessionFilter(ConcurrentSessionFilter){
		sessionRegistry = sessionRegistry
		expiredUrl = '/login'
	}

    //overrides bean implementing GormUserDetailsService?
	userDetailsService(com.recomdata.security.AuthUserDetailsService)

    println '... finished configuring tranSMART Beans\n'
}

private String[] toStringArray(value) {
    if (value == null) {
        return null
    }
    if (value instanceof String) {
        value = [value]
    }
    value as String[]
}
