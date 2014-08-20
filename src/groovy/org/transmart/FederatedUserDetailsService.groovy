package org.transmart

import com.recomdata.security.AuthUserDetailsService
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.saml.SAMLCredential
import org.springframework.security.saml.userdetails.SAMLUserDetailsService
import org.springframework.transaction.TransactionStatus
import org.transmart.searchapp.AuthUser
import org.opensaml.xml.XMLObject

// not in grails-app/services to avoid being automatically instantiated
public class FederatedUserDetailsService implements SAMLUserDetailsService {

    @Autowired
    @Qualifier('grailsApplication')
    def grailsApplication

    @Autowired
    SessionFactory sessionFactory

    static Log log = LogFactory.getLog(FederatedUserDetailsService.class)

    @Autowired
    AuthUserDetailsService userDetailsService

	@Override
	public Object loadUserBySAML(SAMLCredential credential)
			throws UsernameNotFoundException {

        String federatedId = credential.nameID.value
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

    private void tryCreateUser(credential, federatedId, nf) {
        if (grailsApplication.config.org.transmart.security.saml.createInexistentUsers != 'true') {
            log.warn("Will not try to create user with federated id " +
                    "'$federatedId', such option is deactivated")
            throw nf
        }

        def getAttr = {
	    XMLObject attrValue = credential.getAttributeByName(it).getAttributeValues().getAt(0)
	    if(attrValue.metaClass.hasProperty(attrValue, "textContent")) {
		attrValue.getTextContent()
	    } else {
		credential.getAttributeByName(it)?.attributeValues?.getAt(0)?.value
	    }
        }
        def attributes = grailsApplication.config.org.transmart.security.saml.attribute

        AuthUser.withTransaction { TransactionStatus status ->
            String username = null,
                   realName = null,
                   email    = null

            if (attributes.username) {
                username = getAttr(attributes.username)
            }
            if (attributes.firstName && attributes.lastName) {
                realName = getAttr(attributes.firstName) ?: ''
                if (realName) {
                    realName += ' '
                }
                if (getAttr(attributes.lastName)) {
                    realName += getAttr(attributes.lastName)
                }
            }
            if (attributes.email) {
                email = getAttr(attributes.email)
            }

            AuthUser newUser = AuthUser.createFederatedUser(federatedId,
                    username, realName, email, sessionFactory.currentSession);
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
