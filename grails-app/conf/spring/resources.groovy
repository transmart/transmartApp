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
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/


import com.google.common.collect.ImmutableMap
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.spring.DefaultBeanConfiguration
import org.springframework.beans.factory.config.CustomScopeConfigurer
import org.springframework.security.core.session.SessionRegistryImpl
// plugin is not functional at this point
//import org.springframework.security.extensions.kerberos.web.SpnegoAuthenticationProcessingFilter
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.access.AccessDeniedHandlerImpl
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.session.ConcurrentSessionControlStrategy
import org.springframework.security.web.session.ConcurrentSessionFilter
import org.transmart.authorization.CurrentUserBeanFactoryBean
import org.transmart.authorization.CurrentUserBeanProxyFactory
import org.transmart.authorization.QueriesResourceAuthorizationDecorator
import org.transmart.marshallers.MarshallerRegistrarService
import org.transmart.spring.QuartzSpringScope
import org.transmartproject.core.users.User
import org.transmartproject.export.HighDimExporter

def logger = Logger.getLogger('com.recomdata.conf.resources')

beans = {
    xmlns context:"http://www.springframework.org/schema/context"

    /* core-api authorization wrapped beans */
    queriesResourceAuthorizationDecorator(QueriesResourceAuthorizationDecorator) {
            DefaultBeanConfiguration bean ->
        bean.beanDefinition.autowireCandidate = false
    }

    quartzSpringScope(QuartzSpringScope)
    quartzScopeConfigurer(CustomScopeConfigurer) {
        scopes = ImmutableMap.of('quartz', ref('quartzSpringScope'))
    }

    "${CurrentUserBeanProxyFactory.BEAN_BAME}"(CurrentUserBeanProxyFactory)
    "${CurrentUserBeanProxyFactory.SUB_BEAN_REQUEST}"(CurrentUserBeanFactoryBean) { bean ->
        bean.scope = 'request'
    }
    "${CurrentUserBeanProxyFactory.SUB_BEAN_QUARTZ}"(User) { bean ->
        // Spring never actually creates this bean
        bean.scope = 'quartz'
    }

    legacyQueryResultAccessCheckRequestCache(
            QueriesResourceAuthorizationDecorator.LegacyQueryResultAccessCheckRequestCache) { bean ->
        bean.scope = 'request'
    }

    context.'component-scan'('base-package': 'org.transmartproject.export') {
        context.'include-filter'(
                type:       'assignable',
                expression: HighDimExporter.canonicalName)
    }

	sessionRegistry(SessionRegistryImpl)
	sessionAuthenticationStrategy(ConcurrentSessionControlStrategy, sessionRegistry) {
		maximumSessions = 10
	}
	concurrentSessionFilter(ConcurrentSessionFilter){
		sessionRegistry = sessionRegistry
		expiredUrl = '/login'
	}

    redirectStrategy(DefaultRedirectStrategy)
    accessDeniedHandler(AccessDeniedHandlerImpl) {
        errorPage = '/login'
    }
    failureHandler(SimpleUrlAuthenticationFailureHandler) {
        defaultFailureUrl = '/login'
    }

    //overrides bean implementing GormUserDetailsService?
    userDetailsService(com.recomdata.security.AuthUserDetailsService)

    marshallerRegistrarService(MarshallerRegistrarService)

    if (grailsApplication.config.org.transmart.security.spnegoEnabled) {
        // plugin is not functional at this point
//        SpnegoAuthenticationProcessingFilter(SpnegoAuthenticationProcessingFilter) {
//            authenticationManager = ref('authenticationManager')
//            failureHandler = ref('failureHandler')
//        }
        ldapUserDetailsMapper(com.recomdata.security.CustomUserDetailsContextMapper) {
            dataSource = ref("dataSource")
        }
    } else {
        // plugin is not functional at this point
//        SpringSecurityKerberosGrailsPlugin.metaClass.getDoWithSpring = {->
//            logger.info "Skipped Kerberos Grails plugin initialization"
//            return {}
//        }
        SpringSecurityLdapGrailsPlugin.metaClass.getDoWithSpring = {->
            logger.info "Skipped LDAP Grails plugin initialization"
            return {}
        }
    }

    if (!('clientCredentialsAuthenticationProvider' in
            grailsApplication.config.grails.plugin.springsecurity.providerNames)) {
        SpringSecurityOauth2ProviderGrailsPlugin.metaClass.getDoWithSpring = {->
            logger.info "Skipped Oauth2 Grails plugin initialization"
            return {}
        }
    }
}
