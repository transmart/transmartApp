package org.transmart

import com.recomdata.security.AuthUserDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.saml.SAMLCredential
import org.springframework.security.saml.userdetails.SAMLUserDetailsService

// not in grails-app/services to avoid being automatically instantiated
public class FederatedUserDetailsService implements SAMLUserDetailsService {

    @Autowired
    AuthUserDetailsService userDetailsService

	@Override
	public Object loadUserBySAML(SAMLCredential credential)
			throws UsernameNotFoundException {
        credential.getAttributeByName("urn:oid:0.9.2342.19200300.100.1.1")

        //userDetailsService.loadUserByProperty('federatedId',
        //        credential.nameID.value)
        String federatedId = /*credential.nameID.value*/
            credential.getAttributeByName("urn:oid:0.9.2342.19200300.100.1.1").
                    attributeValues[0].value /* LDAP uid */

        userDetailsService.loadUserByProperty('federatedId', federatedId, true)
	}
}
