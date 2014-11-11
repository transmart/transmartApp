package org.transmart.support

import grails.converters.JSON
import groovy.json.JsonSlurper
import org.junit.Test
import org.transmart.marshallers.MarshallerRegistrarService
import org.transmart.marshallers.OntologyTermMarshaller
import org.transmartproject.db.ontology.ConceptTestData
import org.transmartproject.db.ontology.I2b2

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.transmartproject.db.ontology.ConceptTestData.addI2b2

@Mixin(ConceptTestData)
class MarshallerRegistrarServiceTests {

    def grailsApplication

    @Test
    void testRegistersOntologyTermMarshaller() {
        // The registrar is in the application context
        assertThat grailsApplication.mainContext.
                getBean(MarshallerRegistrarService), is(notNullValue())

        // The registrar has registered the OntologyTermMarshaller
        assertThat grailsApplication.mainContext.
                getBean(OntologyTermMarshaller), is(notNullValue())

        addI2b2(level: 1, fullName: "\\my full name\\", name: 'my name')
        def term = I2b2.find { eq('fullName', "\\my full name\\") }
        term.setTableCode('foo_bar')
        term.metaClass.extra = 'extra'

        // The marshaller is being used
        def termOut = new JsonSlurper().parseText((term as JSON).toString())
        assertThat termOut, allOf(
                hasEntry('fullName', '\\my full name\\'),
                not(hasEntry('extra', 'extra')))
    }

}
