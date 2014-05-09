
package com.recomdata.security

import grails.plugin.springsecurity.userdetails.GrailsUser
import org.springframework.security.core.GrantedAuthority

class AuthUserDetails extends GrailsUser {
	final String userRealName

	AuthUserDetails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired,
		boolean accountNonLocked, Collection<GrantedAuthority> authorities, long id, String userRealName) {
			super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, id)
			
			this.userRealName = userRealName}
}
