package org.transmart.marshallers

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.querytool.QueryStatus
import org.transmartproject.db.querytool.QtQueryResultInstance

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
        def value = new QtQueryResultInstance(
                resultTypeId : 1,
                setSize      : 77,
                startDate    : new Date(),
                statusTypeId : 3,
                errorMessage : 'error message',
                description  : 'my description',
                deleteFlag   : 'Y'
        )
        value.id = -1L

        def out = testee.convert(value)
        MatcherAssert.assertThat out, org.hamcrest.Matchers.allOf(
                org.hamcrest.Matchers.hasEntry('errorMessage', 'error message'),
                org.hamcrest.Matchers.hasEntry('id', -1L),
                org.hamcrest.Matchers.hasEntry('setSize', 77L),
                org.hamcrest.Matchers.hasEntry('status', QueryStatus.FINISHED),
                org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasEntry(org.hamcrest.Matchers.equalTo('statusTypeId'), org.hamcrest.Matchers.anything())),
                org.hamcrest.Matchers.not(org.hamcrest.Matchers.hasEntry(org.hamcrest.Matchers.equalTo('description'), org.hamcrest.Matchers.anything())),
        )
    }
}
