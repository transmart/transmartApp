package org.transmart.marshallers

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.core.querytool.QueryStatus

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

@TestMixin(GrailsUnitTestMixin)
class QueryResultMarshallerTests {

    QueryResultMarshaller testee

    @Before
    void before() {
        testee = new QueryResultMarshaller()
    }

    @Test
    void basicTest() {
        def value = [
                getId          : { -1L },
                getResultTypeId: { 1L },
                getSetSize     : { 77L },
                getStatus      : { QueryStatus.FINISHED },
                getErrorMessage: { 'error message' },
                getUsername    : { 'bogus_user_name' }
        ] as QueryResult

        def out = testee.convert(value)
        assertThat out, allOf(
                hasEntry('errorMessage', 'error message'),
                hasEntry('id', -1L),
                hasEntry('setSize', 77L),
                hasEntry('status', QueryStatus.FINISHED),
                not(hasEntry(equalTo('statusTypeId'), anything())),
                not(hasEntry(equalTo('description'), anything())),
                not(hasEntry(equalTo('username'), anything())),
        )
    }
}
