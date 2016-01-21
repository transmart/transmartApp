import com.google.common.collect.ImmutableMap
import com.recomdata.security.ActiveDirectoryLdapAuthenticationExtension
import grails.plugin.springsecurity.SpringSecurityUtils
import com.recomdata.extensions.ExtensionsRegistry
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.spring.DefaultBeanConfiguration
import org.springframework.beans.factory.config.CustomScopeConfigurer
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider
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

// plugin is not functional at this point
//import org.springframework.security.extensions.kerberos.web.SpnegoAuthenticationProcessingFilter
import org.transmartproject.core.users.User
import org.transmartproject.export.HighDimExporter
import org.transmartproject.security.AuthSuccessEventListener
import org.transmartproject.security.BadCredentialsEventListener
import org.transmartproject.security.BruteForceLoginLockService
import org.transmartproject.security.SSLCertificateValidation

def logger = Logger.getLogger('com.recomdata.conf.resources')

beans = {
    xmlns context: "http://www.springframework.org/schema/context"

    if (grailsApplication.config.org.transmart.security.samlEnabled) {
        importBeans('classpath:/spring/spring-security-saml.xml')
        // Provider of default SAML Context. Moved to groovy to allow choose implementation
        if (grailsApplication.config.org.transmart.security.saml.lb.serverName) {
            contextProvider(org.springframework.security.saml.context.SAMLContextProviderLB) {
                scheme = grailsApplication.config.org.transmart.security.saml.lb.scheme
                serverName = grailsApplication.config.org.transmart.security.saml.lb.serverName
                serverPort = grailsApplication.config.org.transmart.security.saml.lb.serverPort
                includeServerPortInRequestURL = grailsApplication.config.org.transmart.security.saml.lb.includeServerPortInRequestURL
                contextPath = grailsApplication.config.org.transmart.security.saml.lb.contextPath
            }
        } else {
            contextProvider(org.springframework.security.saml.context.SAMLContextProviderImpl)
        }
    }

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
                type: 'assignable',
                expression: HighDimExporter.canonicalName)
    }


    // We need to inject the RestBuilder with its bean declaration because its *crappy* constructor
    // would reinitialize the JSON marshaller we use later; rendering the application incompetent
    // It is important this falls first !
    if (!grailsApplication.config.org.transmart.security.sniValidation) {
        logger.info "Disabling server name indication extension"
        System.setProperty("jsse.enableSNIExtension", "false");
    }
    if (!grailsApplication.config.org.transmart.security.sslValidation) {
        logger.info "Disabling hostname and certification verification"
        SSLCertificateValidation.disable()
    }
    restBuilder(grails.plugins.rest.client.RestBuilder)


    sessionRegistry(SessionRegistryImpl)
    sessionAuthenticationStrategy(ConcurrentSessionControlStrategy, sessionRegistry) {
        if (grailsApplication.config.org.transmartproject.maxConcurrentUserSessions) {
                maximumSessions = grailsApplication.config.org.transmartproject.maxConcurrentUserSessions
        } else {
                maximumSessions = 10
        }
    }
    concurrentSessionFilter(ConcurrentSessionFilter) {
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

    def transmartSecurity = grailsApplication.config.org.transmart.security
    if (SpringSecurityUtils.securityConfig.ldap.active) {
        ldapUserDetailsMapper(com.recomdata.security.LdapAuthUserDetailsMapper) {
            springSecurityService = ref('springSecurityService')
            bruteForceLoginLockService = ref('bruteForceLoginLockService')
            // pattern for newly created user, can include <ID> for record id or <FEDERATED_ID> for external user name
            if (transmartSecurity.ldap.newUsernamePattern) {
                newUsernamePattern = transmartSecurity.ldap.newUsernamePattern
            }
            // comma separated list of new user authorities
            if (transmartSecurity.ldap.defaultAuthorities) {
                defaultAuthorities = transmartSecurity.ldap.defaultAuthorities
            }
            // if inheritPassword == false specified user will not be able to login without LDAP
            inheritPassword = transmartSecurity.ldap.inheritPassword
            // can be 'username' or 'federatedId'
            mappedUsernameProperty = transmartSecurity.ldap.mappedUsernameProperty
        }

        if (grailsApplication.config.org.transmart.security.ldap.ad.domain) {
            xmlns aop:"http://www.springframework.org/schema/aop"

            adExtension(ActiveDirectoryLdapAuthenticationExtension)

            aop {
                config("proxy-target-class": true) {
                    aspect(id: 'adExtensionService', ref: 'adExtension')
                }
            }

            ldapAuthProvider(ActiveDirectoryLdapAuthenticationProvider,
                    transmartSecurity.ldap.ad.domain,
                    SpringSecurityUtils.securityConfig.ldap.context.server
            ) {
                userDetailsContextMapper = ref('ldapUserDetailsMapper')
            }
        }
    }

    if (grailsApplication.config.org.transmart.security.spnegoEnabled) {
        // plugin is not functional at this point
//        SpnegoAuthenticationProcessingFilter(SpnegoAuthenticationProcessingFilter) {
//            authenticationManager = ref('authenticationManager')
//            failureHandler = ref('failureHandler')
//        }
    } else {
        // plugin is not functional at this point
//        SpringSecurityKerberosGrailsPlugin.metaClass.getDoWithSpring = {->
//            logger.info "Skipped Kerberos Grails plugin initialization"
//            return {}
//        }
    }

    if (!('clientCredentialsAuthenticationProvider' in
            grailsApplication.config.grails.plugin.springsecurity.providerNames)) {
        SpringSecurityOauth2ProviderGrailsPlugin.metaClass.getDoWithSpring = { ->
            logger.info "Skipped Oauth2 Grails plugin initialization (doWithSpring)"
            return {}
        }
        SpringSecurityOauth2ProviderGrailsPlugin.metaClass.getDoWithApplicationContext = { ->
            logger.info "Skipped Oauth2 Grails plugin initialization (doWithApplicationContext)"
            return {}
        }
    }

    bruteForceLoginLockService(BruteForceLoginLockService) {
        allowedNumberOfAttempts = grailsApplication.config.bruteForceLoginLock.allowedNumberOfAttempts
        lockTimeInMinutes = grailsApplication.config.bruteForceLoginLock.lockTimeInMinutes
    }

    authSuccessEventListener(AuthSuccessEventListener) {
        bruteForceLoginLockService = ref('bruteForceLoginLockService')
    }

    badCredentialsEventListener(BadCredentialsEventListener) {
        bruteForceLoginLockService = ref('bruteForceLoginLockService')
    }

    acghBedExporterRgbColorScheme(org.springframework.beans.factory.config.MapFactoryBean) {
        sourceMap = grailsApplication.config.dataExport.bed.acgh.rgbColorScheme
    }

    transmartExtensionsRegistry(ExtensionsRegistry) {
    }
}
