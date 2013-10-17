package com.recomdata.security

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.ldap.core.DirContextOperations
import org.springframework.ldap.core.support.BaseLdapPathContextSource
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.ldap.authentication.BindAuthenticator
import org.springframework.util.Assert
import org.springframework.util.StringUtils

/**
 * User: Florian Guitton
 * Date: 15/10/13
 * Time: 16:45
 */
class LdapAuthUserAuthenticator extends BindAuthenticator {

    private static final Log logger = LogFactory.getLog(LdapAuthUserAuthenticator.class);

    public LdapAuthUserAuthenticator(BaseLdapPathContextSource contextSource) {
        super(contextSource);
    }

    public DirContextOperations authenticate(Authentication authentication) {
        DirContextOperations user = null;
        Assert.isInstanceOf(UsernamePasswordAuthenticationToken.class, authentication,
                "Can only process UsernamePasswordAuthenticationToken objects");

        String username = authentication.getName();
        String password = (String)authentication.getCredentials();

        if (!StringUtils.hasLength(password)) {
            logger.debug("Rejecting empty password for user " + username);
            throw new BadCredentialsException(messages.getMessage("BindAuthenticator.emptyPassword",
                    "Empty Password"));
        }

        logger.debug("Starting authentication for user " + username);
        // If DN patterns are configured, try authenticating with them directly
        for (String dn : getUserDns(username)) {
            user = bindWithDn(dn, username, password);

            if (user != null) {
                break;
            }
        }

        // Otherwise use the configured search object to find the user and authenticate with the returned DN.
        if (user == null && getUserSearch() != null) {
            DirContextOperations userFromSearch = getUserSearch().searchForUser(username);
            user = bindWithDn(userFromSearch.getDn().toString(), username, password);
        }

        if (user == null) {
            throw new BadCredentialsException(
                    messages.getMessage("LdapAuthUserAuthenticator.badCredentials", "Bad credentials"));
        }

        // The only reason to extend/implement this class id to be able to get passwords compatible with current tranSMART architecture password
        user .setAttributeValue("userPasswordRaw", password);

        return user;
    }

}
