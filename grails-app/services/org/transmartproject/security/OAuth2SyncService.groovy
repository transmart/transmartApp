package org.transmartproject.security

import groovy.util.logging.Slf4j
import org.transmart.oauth2.Client

/**
 * Synchronize OAuth2 client configuration.
 */
@Slf4j
class OAuth2SyncService {

    static datasource = 'oauth2'

    def springSecurityService

    def grailsApplication

    void syncOAuth2Clients() {
        def clients = grailsApplication.config.grails.plugin.springsecurity.oauthProvider.clients

        if (clients == false) {
            log.debug('Clients list in config is false; will do no synchronization')
            return
        }

        clients.each { Map m ->
            if (!m['clientId']) {
                log.error("Client data without clientId: $m")
                return
            }

            def client = Client.findByClientId(m['clientId'])
            if (client == null) {
                client = new Client()
            }
            def dirty = false
            m.each { String prop, def value ->
                if (Client.hasProperty(prop)) {
                    log.error("Invalid property $prop in client definition $m")
                    return
                }

                // Convert GStrings to Strings, Lists to Sets
                if (!(value instanceof List)) {
                    value = value.toString()
                } else {
                    value = value*.toString() as Set
                }

                if (prop == 'clientSecret' && springSecurityService.passwordEncoder) {
                    if (springSecurityService.passwordEncoder.isPasswordValid(client."$prop", value, null)) {
                        return
                    }
                } else if (client."$prop" == value) {
                    return
                }

                client."$prop" = value
                dirty = true
            }

            if (dirty) {
                log.info("Updating client ${m['clientId']}")
                client.save(flush: true)
            }
        }

        def allClientIds = clients.collect { Map m -> m['clientId'] }.findAll()

        int n = 0
        n = Client.where {
            ne 'clientId', '__BOGUS' // hack to avoid empty WHERE clause
            if (allClientIds) {
                not {
                    'in' 'clientId', allClientIds
                }
            }
        }.deleteAll()

        if (n != 0) {
            log.warn("Deleted $n OAuth2 clients")
        }
    }
}
