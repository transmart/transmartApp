package com.recomdata.security

import grails.plugin.springsecurity.userdetails.GrailsUser
import grails.util.Holders
import org.springframework.security.core.GrantedAuthority

class AuthUserDetails extends GrailsUser {

    private static final long serialVersionUID = 1L

    final String userRealName

    AuthUserDetails(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired,
                    boolean accountNonLocked, Collection<GrantedAuthority> authorities, long id, String userRealName) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, id)

        this.userRealName = userRealName
    }

    // This is a convenience function that is here to keep compatibility with AuthUser.isAdmin()
    // This should be removed as all call the the former are removed.
    public boolean isAdmin() {
        def i2b2HelperService = Holders.applicationContext.getBean("i2b2HelperService")
        i2b2HelperService.isAdmin(this)
    }
}
