package org.transmart.security

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.authentication.encoding.BCryptPasswordEncoder
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import grails.test.mixin.services.ServiceUnitTestMixin
import grails.util.Holders
import org.gmock.WithGMock
import org.hamcrest.Description
import org.hamcrest.DiagnosingMatcher
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.oauth2.Client
import org.transmartproject.db.test.RuleBasedIntegrationTestMixin
import org.transmartproject.security.OAuth2SyncService

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

@TestMixin(RuleBasedIntegrationTestMixin)
class OAuth2SyncServiceTests {

    @Autowired
    SpringSecurityService springSecurityService

    def passwordEncoder

    def OAuth2SyncService

    def config = Holders.config

    @Test
    void testClientGetsAdded() {
        def clientConfig = [
                clientId: 'test-client',
                clientSecret: 'test-client-secret',
                authorities: ['ROLE_CLIENT', 'ROLE_ADMIN'],
                scopes: ['read'],
                authorizedGrantTypes: ['authorization_code', 'refresh_token'],
                redirectUris: ['http://localhost:8080/oauth/verify']
        ]
        config.grails.plugin.springsecurity.oauthProvider.clients = [clientConfig]

        OAuth2SyncService.syncOAuth2Clients()

        assertThat Client.list(), allOf(
                hasSize(1),
                contains(
                        allOf(
                                hasProperty('clientId', is(clientConfig['clientId'])),
                                hasProperty('clientSecret',
                                        Matchers.is(PasswordMatcher.hashMatchingPassword(passwordEncoder, clientConfig['clientSecret']))),
                                hasProperty('authorities', contains(*clientConfig['authorities'])),
                                hasProperty('scopes', contains(*clientConfig['scopes'])),
                                hasProperty('authorizedGrantTypes', contains(*clientConfig['authorizedGrantTypes'])),
                                hasProperty('redirectUris', contains(*clientConfig['redirectUris'])),
                        )
                )
        )
    }

    @Test
    void testClientGetsRemoved() {
        new Client(clientId: 'foo-bar').save(failOnError: true, flush: true)

        config.grails.plugin.springsecurity.oauthProvider.clients = []

        assertThat Client.count(), is(1)

        OAuth2SyncService.syncOAuth2Clients()

        assertThat Client.count(), is(0)
    }

    @Test
    void testClientGetsUpdated() {
        new Client(clientId: 'foo-bar', scopes: ['read'] as Set).save(failOnError: true, flush: true)

        def newClientData = [
                clientId: 'foo-bar',
                scopes: ['newscope'],
        ]
        config.grails.plugin.springsecurity.oauthProvider.clients = [newClientData]

        OAuth2SyncService.syncOAuth2Clients()

        assertThat Client.list(), allOf(
                hasSize(1),
                contains(
                        allOf(
                                hasProperty('clientId', is('foo-bar')),
                                hasProperty('scopes', contains(*newClientData['scopes'])),
                        )))
    }
}

class PasswordMatcher extends DiagnosingMatcher<String> {

    def passwordEncoder
    def password

    static Matcher<String> hashMatchingPassword(passwordEncoder, password) {
        new PasswordMatcher(passwordEncoder: passwordEncoder,
                            password: password)
    }

    @Override
    protected boolean matches(Object o, Description description) {
        passwordEncoder.isPasswordValid(o, password, null)
    }

    @Override
    void describeTo(Description description) {
        description
                .appendText('hash matching ')
                .appendValue(password)
    }
}
