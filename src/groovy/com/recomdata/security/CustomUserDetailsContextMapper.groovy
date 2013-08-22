package com.recomdata.security

import java.util.List;

import groovy.sql.Sql

import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.GrantedAuthority
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.authentication.DisabledException
import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.apache.log4j.Logger

class CustomUserDetailsContextMapper implements UserDetailsContextMapper {

    def dataSource
	def application = ApplicationHolder.application
	def conf = SpringSecurityUtils.securityConfig
	static Logger log = Logger.getLogger(CustomUserDetailsContextMapper.class)
	static final List NO_ROLES = [new GrantedAuthorityImpl(SpringSecurityUtils.NO_ROLE)]
	
    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<GrantedAuthority> authority) {

		log.info "Attempting to find user for username: $username"		
		log.debug "Use withTransaction to avoid lazy loading initialization error when accessing the authorities collection"
		Class<?> User = application.getDomainClass(conf.userLookup.userDomainClassName).clazz

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

        /*if ( !user.enabled )
			log.warn "User disabled: $username"
            throw new DisabledException("User is disabled", username)*/

        //return userDetails
    }

    @Override
    public void mapUserToContext(UserDetails arg0, DirContextAdapter arg1) {
    }
}