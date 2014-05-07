package org.transmart.authorization

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.TestMixin
import grails.util.GrailsWebUtil
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.exceptions.AccessDeniedException
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.querytool.*
import org.transmartproject.db.test.RuleBasedIntegrationTestMixin
import org.transmartproject.db.user.AccessLevelTestData

import static groovy.util.GroovyAssert.shouldFail
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.isA

/**
 * Created by glopes on 3/31/14.
 */
@TestMixin(RuleBasedIntegrationTestMixin)
class QueriesResourceAuthorizationDecoratorTests {

    AccessLevelTestData accessLevelTestData = new AccessLevelTestData()

    SpringSecurityService springSecurityService

    ConceptsResource conceptsResourceService

    QueriesResource queriesResourceAuthorizationDecorator

    @Before
    void setUp() {
        accessLevelTestData.saveAll()
        assert springSecurityService != null
        assert conceptsResourceService != null
        assert queriesResourceAuthorizationDecorator != null
    }

    @Test
    void testSimpleUnauthorizedQueryDefinition() {
        // fourth user has no access to study 2
        def user = accessLevelTestData.users[3]

        //see ConceptTestData
        QueryDefinition queryDefinition = new QueryDefinition('test-query', [
                new Panel(
                        items: [
                                new Item(conceptKey: '\\\\i2b2 main\\foo\\study2\\')])])

        mockCurrentUser user.username, {
            shouldFail AccessDeniedException, {
                queriesResourceAuthorizationDecorator.runQuery(queryDefinition)
            }
        }
    }


    @Test
    void testSimpleUnauthorizedQueryDefinitionUsernameVariant() {
        // fourth user has no access to study 2
        def user = accessLevelTestData.users[3]

        QueryDefinition queryDefinition = new QueryDefinition('test-query', [
                new Panel(
                        items: [
                                new Item(conceptKey: '\\\\i2b2 main\\foo\\study2\\')])])

        mockCurrentUser user.username, {
            shouldFail AccessDeniedException, {
                queriesResourceAuthorizationDecorator.runQuery(
                        queryDefinition, user.username)
            }
        }
    }

    @Test
    void testSimpleAuthorizedQueryDefinition() {
        // second user is in group test_-201, which has access to study 2
        def user = accessLevelTestData.users[1]

        QueryDefinition queryDefinition = new QueryDefinition('test-query', [
                new Panel(
                        items: [
                                new Item(conceptKey: '\\\\i2b2 main\\foo\\study2\\')])])

        mockCurrentUser user.username, {
            def result = queriesResourceAuthorizationDecorator.runQuery(queryDefinition)
            assertThat result, isA(QueryResult)
        }
    }

    @Test
    void testSimpleUnauthorizedQueryDefinitionUsernameMismatch() {
        // second user is in group test_-201, which has access to study 2
        def user = accessLevelTestData.users[1]

        QueryDefinition queryDefinition = new QueryDefinition('test-query', [
                new Panel(
                        items: [
                                new Item(conceptKey: '\\\\i2b2 main\\foo\\study2\\')])])

        mockCurrentUser user.username, {
            shouldFail AccessDeniedException, {
                queriesResourceAuthorizationDecorator.runQuery(
                        queryDefinition, user.username + '_mismatch')
            }
        }
    }

    void mockCurrentUser(String username, Closure closure) {
        GrailsWebUtil.bindMockWebRequest()

        def proxyMetaClass = ProxyMetaClass.getInstance(SpringSecurityService)
        proxyMetaClass.interceptor = new PropertyAccessInterceptor() {
            Object beforeInvoke(Object object, String methodName, Object[] arguments) {}

            Object afterInvoke(Object object,
                               String methodName,
                               Object[] arguments,
                               Object result) {
                if (methodName == 'isLoggedIn') {
                    return true
                }
                throw new UnsupportedOperationException(
                        "Unexpected method call: $methodName($arguments)")
            }

            boolean doInvoke() {
                false
            }

            Object beforeGet(Object object, String property) {
                if (property == 'principal') {
                    return [username: username]
                }
                throw new UnsupportedOperationException(
                        "Unexpected property requested: $property")
            }

            void beforeSet(Object object, String property, Object newValue) {
                throw new UnsupportedOperationException(
                        "Unexpected property setting requested: $property, " +
                                "value $newValue")
            }
        }

        proxyMetaClass.use(springSecurityService, closure)
    }

}
