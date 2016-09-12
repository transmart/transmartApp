package com.recomdata.security

import grails.util.Holders
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper
import org.springframework.util.Assert
import org.transmart.searchapp.AccessLog
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.Role

/**
 * User: Florian Guitton
 * Date: 15/10/13
 * Time: 11:23
 */
public class LdapAuthUserDetailsMapper implements UserDetailsContextMapper {
    def springSecurityService
    def bruteForceLoginLockService

    String mappedUsernameProperty = 'username' // also "federatedId" allowed
    boolean inheritPassword = false
    // Pattern for newly created username generation, ignored if mappedUsernameProperty == 'username'
    String newUsernamePattern
    // List of roles assigned to new user
    List<String> defaultAuthorities

    private final Log logger = LogFactory.getLog(LdapAuthUserDetailsMapper.class);
    private String passwordAttributeName = "userPassword";
    private String rolePrefix = "ROLE_";
    private String[] roleAttributes = null;
    private boolean convertToUpperCase = true;

    protected Collection<? extends GrantedAuthority> collectAuthoritiesForRoleAttributes(DirContextOperations ctx) {
        Collection<? extends GrantedAuthority> result = new ArrayList<>()
        if (!roleAttributes) {
            return result;
        }
        for (int i = 0; i < roleAttributes.length; i++) {
            String[] rolesForAttribute = ctx.getStringAttributes(roleAttributes[i]);

            if (rolesForAttribute == null) {
                logger.debug("Couldn't read role attribute '" + roleAttributes[i] + "' for user " + ctx.dn);
                continue;
            }

            for (int j = 0; j < rolesForAttribute.length; j++) {
                GrantedAuthority authority = createAuthority(rolesForAttribute[j]);
                if (authority != null) {
                    result.add(authority);
                }
            }
        }
        return result
    }

    protected Collection<? extends GrantedAuthority> collectDatabaseAuthorities(AuthUser user) {
        user.authorities.collect { new SimpleGrantedAuthority(it.authority) }
    }

    protected AuthUser findOrSaveUser(DirContextOperations ctx, String username) {
        String fullName = ctx.getStringAttribute('cn')
        String email = ctx.getStringAttribute('mail')
        String password = mapPassword(ctx)

        def ldapConfig = Holders.config.transmartproject.ldap

        AuthUser user
        if (ldapConfig.caseInsensitive) {
            user = AuthUser.createCriteria().get { eq(mappedUsernameProperty, username)}
            if (user == null) {
                user = AuthUser.create()
                user.username = username
            }
        } else {
            user = AuthUser.findOrCreateWhere((mappedUsernameProperty): username)
        }
        user.name = fullName
        user.passwd = password
        user.userRealName = fullName
        user.email = email

        def created = !user.id
        def willGenerateUsername = false

        if (created && ldapConfig.doNotCreateUserIfNotExist) {
            logger.warn("Can't create user '${username}' because transmartproject.ldap.doNotCreateUserIfNotExist is set.")
            throw new UsernameNotFoundException("User '${username}' does not exist in transmart DB.")
        } else {
            if (created) {
                user.emailShow = true
                user.enabled = true
                if (mappedUsernameProperty != 'username') {
                    // we will set username later
                    if (!newUsernamePattern) {
                        user.username = username
                    } else if (UsernameUtils.patternHasId(newUsernamePattern)) {
                        willGenerateUsername = true
                        user.username = UsernameUtils.randomName()
                    } else {
                        user.username = UsernameUtils.evaluatePattern(user, newUsernamePattern)
                    }
                }
            }
            user.save(flush: true)

            // generate user name after initial save, because it can use identifier
            if (!user.hasErrors() && willGenerateUsername) {
                user.username = UsernameUtils.evaluatePattern(user, newUsernamePattern)
                user.save(flush: true)
            }

            if (user.hasErrors()) {
                logger.error("Can't save User: ${username}:")
                user.errors.allErrors.each { logger.error(it) }
                return null;
            }

            if (created) {
                def authorities = defaultAuthorities ?: [Role.SPECTATOR_ROLE]
                Role.findAllByAuthorityInList(authorities).each { user.addToAuthorities(it) }

                new AccessLog(
                        username: "LDAP",
                        event: "User Created",
                        eventmessage: "User '${user.username}' for ${user.userRealName} created",
                        accesstime: new Date()).save()
            }

            if (!user.enabled) {
                logger.error("User is disabled: ${username}")
                return null
            }

            return user
        }
    }


    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        username = username.replaceAll(/[^0-9A-Za-z]*/, "").toLowerCase()
        logger.debug("Mapping user details from context and database with username: " + username);

        AuthUser.withTransaction { status ->
            def user = findOrSaveUser(ctx, username)
            if (!user) {
                return null
            }

            Collection<? extends GrantedAuthority> collectedAuthorities = new HashSet<? extends GrantedAuthority>()
            collectedAuthorities.addAll(authorities)
            collectedAuthorities.addAll(collectAuthoritiesForRoleAttributes(ctx))
            collectedAuthorities.addAll(collectDatabaseAuthorities(user))

            return new AuthUserDetails(
                    user.username,
                    user.passwd,
                    user.enabled,
                    true,
                    true,
                    !bruteForceLoginLockService.isLocked(user.username),
                    collectedAuthorities ?: AuthUserDetailsService.NO_ROLES,
                    user.id,
                    "LDAP '${user.userRealName}'")
        }
    }

    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new UnsupportedOperationException("LdapAuthUserDetailsMapper only supports reading from a context. Please" +
                "use a subclass if mapUserToContext() is required.");
    }

    protected String mapPassword(DirContextOperations ctx) {
        if (!inheritPassword) {
            return 'NO_PASSWORD'
        }
        String password = ''
        Object passwordValue = ctx.getObjectAttribute("userPasswordRaw");
        if (passwordValue != null) {
            if (!(passwordValue instanceof String)) {
                passwordValue = new String((byte[]) passwordValue);
            }
            password = (String) passwordValue
        }
        return springSecurityService.encodePassword(password)
    }

    protected GrantedAuthority createAuthority(Object role) {
        if (role instanceof String) {
            if (convertToUpperCase) {
                role = ((String) role).toUpperCase();
            }
            return new SimpleGrantedAuthority(rolePrefix + role);
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