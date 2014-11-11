package com.recomdata.security

import grails.plugin.springsecurity.SpringSecurityUtils
import groovy.sql.Sql
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.authentication.DisabledException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper
import org.springframework.util.Assert
import org.transmart.searchapp.AccessLog
import org.transmart.searchapp.AuthUser

/**
 * User: Florian Guitton
 * Date: 15/10/13
 * Time: 11:23
 */
public class LdapAuthUserDetailsMapper implements UserDetailsContextMapper {

    def dataSource
    def springSecurityService
    def databasePortabilityService
    def conf = SpringSecurityUtils.securityConfig

    private final Log logger = LogFactory.getLog(LdapAuthUserDetailsMapper.class);
    private String passwordAttributeName = "userPassword";
    private String rolePrefix = "ROLE_";
    private String[] roleAttributes = null;
    private boolean convertToUpperCase = true;
    private List<GrantedAuthority> mutableAuthorities = new ArrayList<GrantedAuthority>();

    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {

        username = username.toLowerCase()
        logger.debug("Mapping user details from context and databse with username: " + username);

        def roles

        String fullName = ctx.originalAttrs.attrs['cn'].values[0]
        String email = ctx.originalAttrs.attrs['mail'].values[0]
        String password = "";

        Object passwordValue = ctx.getObjectAttribute("userPasswordRaw");

        if (passwordValue != null) {
            password = mapPassword(passwordValue);
        }

        for (int i = 0; (roleAttributes != null) && (i < roleAttributes.length); i++) {
            String[] rolesForAttribute = ctx.getStringAttributes(roleAttributes[i]);

            if (rolesForAttribute == null) {
                logger.debug("Couldn't read role attribute '" + roleAttributes[i] + "' for user " + dn);
                continue;
            }

            for (int j = 0; j < rolesForAttribute.length; j++) {
                GrantedAuthority authority = createAuthority(rolesForAttribute[j]);

                if (authority != null) {
                    mutableAuthorities.add(authority);
                }
            }
        }

        for (GrantedAuthority authority : authorities) {
            mutableAuthorities.add(authority);
        }

        AuthUser.withTransaction { status ->
            def user = AuthUser.findByUsername(username)
            def create = false
            def message

            if (!user) {
                create = true
                def sql = new Sql(dataSource)
                def seqSQL = databasePortabilityService.getNextSequenceValueSql('searchapp', 'hibernate_sequence')
                def result = sql.firstRow(seqSQL)
                user = new AuthUser(id: result.nextval, username: username, passwd: springSecurityService.encodePassword(password), name: fullName, userRealName: fullName, email: email, emailShow: true, enabled: true)
            } else {
                user.passwd = springSecurityService.encodePassword(password)
                user.userRealName = fullName
                user.email = email
            }
            if (!user.save(flush: true)) {
                logger.error("Can't save User: ${username}:")
                user.errors.allErrors.each { logger.error(it) }
                return null;
            }

            if (create) {
                new AccessLog(
                        username: "LDAP",
                        event: "User Created",
                        eventmessage: "User '${user.username}' for ${user.userRealName} created",
                        accesstime: new Date()).save()
            }

            roles = user.getAuthorities()

            if (!user.enabled)
                throw new DisabledException("User is disabled", username)

            def authority

            if (conf.ldap.context.allowInternaRoles) {
                authority = roles.collect { new GrantedAuthorityImpl(it.authority) }
                authority.addAll(authorities)
            } else
                authority = authorities

            return new AuthUserDetails(user.username, user.passwd, user.enabled, !user.accountExpired, !user.passwordExpired, !user.accountLocked, authority ?: AuthUserDetailsService.NO_ROLES, user.id, "LDAP '" + user.userRealName + "'")
        }
    }

    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new UnsupportedOperationException("LdapAuthUserDetailsMapper only supports reading from a context. Please" +
                "use a subclass if mapUserToContext() is required.");
    }

    protected String mapPassword(Object passwordValue) {
        if (!(passwordValue instanceof String)) {
            passwordValue = new String((byte[]) passwordValue);
        }
        return (String) passwordValue;
    }

    protected GrantedAuthority createAuthority(Object role) {
        if (role instanceof String) {
            if (convertToUpperCase) {
                role = ((String) role).toUpperCase();
            }
            return new GrantedAuthorityImpl(rolePrefix + role);
        }
        return null;
    }

    public void setConvertToUpperCase(boolean convertToUpperCase) {
        this.convertToUpperCase = convertToUpperCase;
    }

    public void setPasswordAttributeName(String passwordAttributeName) {
        this.passwordAttributeName = passwordAttributeName;
    }

    public void setRoleAttributes(String[] roleAttributes) {
        Assert.notNull(roleAttributes, "roleAttributes array cannot be null");
        this.roleAttributes = roleAttributes;
    }

    public void setRolePrefix(String rolePrefix) {
        this.rolePrefix = rolePrefix;
    }
}