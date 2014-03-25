package org.transmart.ontology

import grails.converters.JSON

class ConceptsController {

    def conceptsResourceService

    def getCategories() {
        render conceptsResourceService.allCategories as JSON
    }

    def getChildren() {
        def parentConceptKey = params.get('concept_key')
        def parent = conceptsResourceService.getByKey(parentConceptKey)
        render parent.children as JSON
    }

}
