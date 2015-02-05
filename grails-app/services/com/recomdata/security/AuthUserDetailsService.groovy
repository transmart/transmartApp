/**
 * $Id: AuthUserDetailsService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package com.recomdata.security

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.GrailsUserDetailsService
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

import javax.annotation.Resource

/**
 * Implementation of <code>GrailsUserDetailsService</code> that uses
 * domain classes to load users and roles.
 * See also GormUserDetailsService
 */
class AuthUserDetailsService implements GrailsUserDetailsService {

    /* @Resource is required because this bean is declared in resources.groovy
       and therefore the service does not benefit from Grails' conventional
       autoinjection into services */
    @Resource
    def grailsApplication
    @Resource
    def bruteForceLoginLockService

    def conf = SpringSecurityUtils.securityConfig

    /**
     * Some Spring Security classes (e.g. RoleHierarchyVoter) expect at least
     * one role, so we give a user with no granted roles this one which gets
     * past that restriction but doesn't grant anything. */
    static final List NO_ROLES = [new SimpleGrantedAuthority(SpringSecurityUtils.NO_ROLE)]

    @Override
    UserDetails loadUserByUsername(String username,
                                   boolean loadRoles = true) throws UsernameNotFoundException {
        try {
            loadUserByProperty((String) conf.userLookup.usernamePropertyName,
                    username,
                    loadRoles,
                    true /* ignore case */)
        } catch (UsernameNotFoundException unfe) {
            def splitUsername = username.split('@') as List
            if (splitUsername.size() != 2) {
                throw unfe
            }

            loadUserByProperty((String) conf.userLookup.usernamePropertyName,
                    splitUsername[0],
                    loadRoles,
                    true /* ignore case */)
        }
    }

    UserDetails loadUserByProperty(String property,
                                   String value,
                                   boolean loadRoles,
                                   boolean ignoreCase = false)
            throws UsernameNotFoundException {

        log.info "Attempting to find user for $property = $value"

        Class<?> userClass = grailsApplication.getDomainClass(
                conf.userLookup.userDomainClassName).clazz

        def user = userClass.createCriteria().get {
            eq property, value, [ignoreCase: ignoreCase]

            if (loadRoles) {
                createAlias 'authorities', 'a', CriteriaSpecification.LEFT_JOIN
            }
        }
        def authorities = []

        if (!user) {
            log.warn "User not found with $property = $value"
            throw new UsernameNotFoundException("User not found",
                    "$property = $value")
        }

        if (loadRoles) {
            authorities = user.authorities*.authority.collect {
                new SimpleGrantedAuthority(it)
            }
        }

        if (loadRoles && log.isDebugEnabled()) {
            log.debug("Roles for user ${user.username} are: " +
                    authorities.join(', ') ?: '(none)')
        }

        new AuthUserDetails(user.username, user.passwd, user.enabled,
                true, true, !bruteForceLoginLockService.isLocked(user.username),
                authorities ?: NO_ROLES, user.id, user.userRealName)
    }
}
