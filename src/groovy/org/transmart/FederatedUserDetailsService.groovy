package org.transmart

import com.recomdata.security.AuthUserDetailsService
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.hibernate.SessionFactory
import org.opensaml.xml.XMLObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.saml.SAMLCredential
import org.springframework.security.saml.userdetails.SAMLUserDetailsService
import org.springframework.transaction.TransactionStatus
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.Role
import org.transmartproject.core.exceptions.UnexpectedResultException

import javax.annotation.Resource

// not in grails-app/services to avoid being automatically instantiated
// we want to instantiate it only if SAML is on
public class FederatedUserDetailsService implements SAMLUserDetailsService {

    @Resource
    def grailsApplication

    @Autowired
    SessionFactory sessionFactory

    static Log log = LogFactory.getLog(FederatedUserDetailsService.class)

    @Autowired
    AuthUserDetailsService userDetailsService

    @Override
    public Object loadUserBySAML(SAMLCredential credential)
            throws UsernameNotFoundException {

        String federatedId = fetchFederatedId(credential)
        try {
            log.debug("Searching for user with federated id '$federatedId'")
            return userDetailsService.loadUserByProperty('federatedId', federatedId, true)
        } catch (UsernameNotFoundException nf) {
            log.info("No user found with federated id '$federatedId")
            tryCreateUser(credential, federatedId, nf)
            log.info("Trying to load user with federated id '$federatedId' again")

            return userDetailsService.loadUserByProperty('federatedId', federatedId, true)
        }
    }

    String fetchFederatedId(SAMLCredential credential) {
        if (attributeConfig.federatedId) {
            getAttr(credential, attributeConfig.federatedId)
        } else {
            credential.nameID.value //better be persistent
        }
    }

    static String getAttr(SAMLCredential credential, String it) {
        def values = credential.getAttribute(it)?.attributeValues
        if (!values) {
            throw new UnexpectedResultException("Could not find values " +
                    "for attribute $it in SAML credential")
        }
        if (values.size() > 1) {
            throw new UnexpectedResultException("Found more than one " +
                    "value for attribute $it: $values")
        }

        XMLObject attrValue = values.getAt(0)
        if (attrValue.hasProperty('value')) {
            attrValue.value
        } else if (values.hasProperty('textContent')) {
            attrValue.textContent
        } else {
            throw new UnexpectedResultException("Unexpected value for " +
                    "attribute $it: $attrValue")
        }
    }

    private def getSamlConfig() {
        grailsApplication.config.org.transmart.security.saml
    }

    private def getAttributeConfig() {
        samlConfig.attribute
    }

    private void tryCreateUser(SAMLCredential credential, federatedId, nf) {
        if (grailsApplication.config.org.transmart.security.saml.createInexistentUsers != 'true') {
            log.warn("Will not try to create user with federated id " +
                    "'$federatedId', such option is deactivated")
            throw nf
        }

        AuthUser.withTransaction { TransactionStatus status ->
            String username = null,
                   realName = null,
                   email = null

            if (attributeConfig.username) {
                username = getAttr(credential, attributeConfig.username)
            }
            if (attributeConfig.firstName && attributeConfig.lastName) {
                realName = getAttr(credential, attributeConfig.firstName) ?: ''
                if (realName) {
                    realName += ' '
                }
                if (getAttr(credential, attributeConfig.lastName)) {
                    realName += getAttr(credential, attributeConfig.lastName)
                }
            }
            if (attributeConfig.email) {
                email = getAttr(credential, attributeConfig.email)
            }

            AuthUser newUser = AuthUser.createFederatedUser(federatedId,
                    username, realName, email, sessionFactory.currentSession);

            if (samlConfig.defaultRoles) {
                // if new user authorities specified then replace default authorities
                newUser.authorities.clear()
                Role.findAllByAuthorityInList(samlConfig.defaultRoles).each { newUser.addToAuthorities(it) }
            }

            def outcome = newUser.save(flush: true)
            if (outcome) {
                log.info("Created new user. {federatedId=$federatedId, " +
                        "username=$username, realName=$realName, email=" +
                        "$email}")
            } else {
                log.error("Failed creating new user with federatedId " +
                        "$federatedId, errors: " + newUser.errors)
                throw nf
            }
        }
    }
}
