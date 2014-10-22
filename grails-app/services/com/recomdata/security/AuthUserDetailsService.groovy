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
                !user.accountExpired, !user.passwordExpired, !user.accountLocked,
                authorities ?: NO_ROLES, user.id, user.userRealName)
    }
}
