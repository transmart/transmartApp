package org.transmart.authorization

import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.TestMixin
import grails.util.GrailsWebUtil
import grails.util.Holders
import org.junit.Before
import org.junit.Test
import org.springframework.web.context.WebApplicationContext
import org.transmartproject.core.exceptions.AccessDeniedException
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.querytool.*
import org.transmartproject.db.test.RuleBasedIntegrationTestMixin
import org.transmartproject.db.user.AccessLevelTestData

import static groovy.util.GroovyAssert.shouldFail
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.isA
import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess
import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

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

    QueryDefinition getStudy2SampleDefinition() {
        //see ConceptTestData
        new QueryDefinition('test-query', [
                new Panel(
                        items: [
                                new Item(conceptKey: '\\\\i2b2 main\\foo\\study2\\')])])
    }

    @Test
    void testSimpleUnauthorizedQueryDefinition() {
        // fourth user has no access to study 2
        def user = accessLevelTestData.users[3]

        mockCurrentUser user.username, {
            shouldFail AccessDeniedException, {
                queriesResourceAuthorizationDecorator.
                        runQuery(study2SampleDefinition)
            }
        }
    }


    @Test
    void testSimpleUnauthorizedQueryDefinitionUsernameVariant() {
        // fourth user has no access to study 2
        def user = accessLevelTestData.users[3]

        mockCurrentUser user.username, {
            shouldFail AccessDeniedException, {
                queriesResourceAuthorizationDecorator.runQuery(
                        study2SampleDefinition, user.username)
            }
        }
    }

    @Test
    void testSimpleAuthorizedQueryDefinition() {
        // second user is in group test_-201, which has access to study 2
        def user = accessLevelTestData.users[1]

        mockCurrentUser user.username, {
            def result = queriesResourceAuthorizationDecorator.
                    runQuery(study2SampleDefinition)
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
                        study2SampleDefinition, user.username + '_mismatch')
            }
        }
    }

    @Test
    void testRetrievalOfSelfQueryDefinition() {
        // second user is in group test_-201, which has access to study 2
        def user = accessLevelTestData.users[1]

        mockCurrentUser user.username, {
            def queryResult = queriesResourceAuthorizationDecorator.runQuery(
                    study2SampleDefinition, user.username)

        }
    }

    @Test
    void testRetrievalOfAnotherUsersQueryDefinition() {
        /* 2 second user is in group test_-201, which has access to study 2
         * 3 third user has direct access to study 2
         * We fail even though both users have access to study 2 */
        def userCreator = accessLevelTestData.users[1]
        def userAccessor = accessLevelTestData.users[2]

        QueryResult queryResult
        mockCurrentUser userCreator.username, {
            queryResult = queriesResourceAuthorizationDecorator.runQuery(
                    study2SampleDefinition, userCreator.username)

            assertThat queriesResourceAuthorizationDecorator.
                    getQueryResultFromId(queryResult.id), is(queryResult)
        }
     }

    @Test
    void testLegacyStatic() {
        // second user is in group test_-201, which has access to study 2
        def user = accessLevelTestData.users[1]

        mockCurrentUser user.username, {
            QueryResult queryResult = queriesResourceAuthorizationDecorator.
                    runQuery(study2SampleDefinition, user.username)

            checkQueryResultAccess queryResult.id
            checkQueryResultAccess queryResult.id.toString()
            // empty things should be ignored
            checkQueryResultAccess '', queryResult.id
        }
    }

    @Test
    void testLegacyStaticDenied() {
        def userCreator = accessLevelTestData.users[1]
        def userAccessor = accessLevelTestData.users[3]

        QueryResult queryResult

        mockCurrentUser userCreator.username, {
             queryResult = queriesResourceAuthorizationDecorator.
                    runQuery(study2SampleDefinition, userCreator.username)
        }

        mockCurrentUser userAccessor.username, {
            shouldFail AccessDeniedException, {
                checkQueryResultAccess queryResult.id
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
