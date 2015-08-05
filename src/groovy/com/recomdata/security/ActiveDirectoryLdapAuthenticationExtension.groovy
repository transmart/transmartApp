package com.recomdata.security
import grails.util.Holders
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider

import javax.annotation.PostConstruct
/**
 * Date: 06.07.2015
 * Time: 15:05
 */
@Aspect
class ActiveDirectoryLdapAuthenticationExtension {
    boolean lowerCaseName = false
    boolean onlyDomainNames = false
    String domain

    @PostConstruct
    def configure() {
        def ldapConf = Holders.grailsApplication.config.org.transmart.security.ldap
        if (ldapConf.ad.lowerCaseName) {
            lowerCaseName = ldapConf.ad.lowerCaseName
        }
        if (ldapConf.ad.onlyDomainNames) {
            onlyDomainNames = ldapConf.ad.onlyDomainNames
        }
        domain = ldapConf.ad.domain
    }

    protected static Authentication toLowerCaseAuthentication(Authentication auth) {
        if (auth.name == auth.name.toLowerCase()) {
            return auth;
        }
        Authentication res = new UsernamePasswordAuthenticationToken(auth.name.toLowerCase(),
                auth.credentials, auth.authorities)
        res.details = auth.details
        return res
    }

    @Around("execution(* org.springframework.security.ldap.authentication.AbstractLdapAuthenticationProvider+.authenticate(..))")
    def authenticate(ProceedingJoinPoint point) {
        Authentication auth = point.args[0]
        if (point.target instanceof ActiveDirectoryLdapAuthenticationProvider) {
            if (lowerCaseName) {
                auth = toLowerCaseAuthentication(auth)
            }
            if (onlyDomainNames && !auth.name.endsWith("@$domain")) {
                return null;
            }
        }
        return point.proceed(auth)
    }
}
