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
  

/**
* $Id: AuthUserDetailsService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
* @author $Author: mmcduffie $
* @version $Revision: 9178 $
*/
package com.recomdata.security

import java.util.List
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserDetailsService
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException

/**
* Implementation of <code>GrailsUserDetailsService</code> that uses
* domain classes to load users and roles.
*/
class AuthUserDetailsService implements GrailsUserDetailsService {

    boolean transactional = true
	
	static Logger log = Logger.getLogger(AuthUserDetailsService.class)
	
	def application = ApplicationHolder.application
	def conf = SpringSecurityUtils.securityConfig
	
	/** * Some Spring Security classes (e.g. RoleHierarchyVoter) expect at least one role, so * we give a user with no granted roles this one which gets past that restriction but * doesn't grant anything. */ 
	static final List NO_ROLES = [new GrantedAuthorityImpl(SpringSecurityUtils.NO_ROLE)]
	
	UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		log.info "Attempting to find user for username: $username"		
		log.debug "Use withTransaction to avoid lazy loading initialization error when accessing the authorities collection"
		Class<?> User = application.getDomainClass(conf.userLookup.userDomainClassName).clazz
		User.withTransaction { status ->
			def user = User.findWhere((conf.userLookup.usernamePropertyName): username)			
			if (!user) {
				log.warn "User not found: $username"
				throw new UsernameNotFoundException('User not found', username)
			}		
			def authorities = user.authorities.collect {new GrantedAuthorityImpl(it.authority)}
			
			return new AuthUserDetails(user.username, user.passwd, user.enabled,
				!user.accountExpired, !user.passwordExpired, !user.accountLocked,
				authorities ?: NO_ROLES, user.id, user.userRealName)
		}
	}

	UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException {
		return loadUserByUsername(username)
	}
}