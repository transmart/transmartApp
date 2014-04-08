package com.recomdata.security

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import org.apache.log4j.Logger
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper

class CustomUserDetailsContextMapper implements UserDetailsContextMapper {

    def dataSource
	def conf = SpringSecurityUtils.securityConfig
	static Logger log = Logger.getLogger(CustomUserDetailsContextMapper.class)
	static final List NO_ROLES = [new SimpleGrantedAuthority(SpringSecurityUtils.NO_ROLE)]

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx,
                                          String username,
                                          Collection<? extends GrantedAuthority> authority) {

		log.info "Attempting to find user for username: $username"
		log.debug "Use withTransaction to avoid lazy loading initialization error when accessing the authorities collection"
		Class<?> User = Holders.grailsApplication.getDomainClass(conf.userLookup.userDomainClassName).clazz

        User.withTransaction { status ->
			def user = User.findByUsername(username)
			if(!user){
				user = User.findByUsername(username.toUpperCase())
			}
			if(!user){
				user = User.findByUsername(username.toLowerCase())
			}
            if(!user){
				log.warn "User not found: $username"
				throw new UsernameNotFoundException('User not found', username)
            }
			def authorities = user.authorities.collect {new GrantedAuthorityImpl(it.authority)}

			// def userDetails = new AuthUserDetails(username, user.password, user.enabled, false, false, false, authorities, user.id)
			 return new AuthUserDetails(user.username, user.passwd, user.enabled,
				 !user.accountExpired, !user.passwordExpired, !user.accountLocked,
				 authorities ?: NO_ROLES, user.id, user.userRealName)
        }
    }

    @Override
    public void mapUserToContext(UserDetails arg0, DirContextAdapter arg1) {
    }
}
