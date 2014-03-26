package org.transmart.marshallers

import grails.converters.JSON
import groovy.json.JsonSlurper
import org.hamcrest.MatcherAssert
import org.junit.Test
import org.transmartproject.core.ontology.OntologyTerm

import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.equalTo

class EnumMarshallerTests {

    @Test
    void testMarshalling() {
        def values = [
                OntologyTerm.VisualAttributes.HIDDEN,
                OntologyTerm.VisualAttributes.MODIFIER_CONTAINER,
        ]

        def out = new JsonSlurper().parseText((values as JSON).toString())
        MatcherAssert.assertThat out, contains(
                equalTo('HIDDEN'),
                equalTo('MODIFIER_CONTAINER')
        )
    }

}
