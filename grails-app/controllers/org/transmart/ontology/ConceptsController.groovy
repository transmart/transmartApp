package org.transmart.ontology
import grails.converters.JSON
import org.transmart.searchapp.AuthUser
import org.transmartproject.core.exceptions.InvalidArgumentsException
//import org.transmartproject.core.ontology.BoundModifier
class ConceptsController {

    def conceptsResourceService
    def i2b2HelperService
    def springSecurityService

    def getCategories() {
        render conceptsResourceService.allCategories as JSON
    }

    def getChildren() {
        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        def parentConceptKey = params.get('concept_key')
        def parent = conceptsResourceService.getByKey(parentConceptKey)
        def childrenWithTokens = i2b2HelperService.getChildPathsWithTokensFromParentKey(parentConceptKey)
        def childrenWithAuth = i2b2HelperService.getAccess(childrenWithTokens, user)
        def authChildren = []

        parent.children.each { child->
            if (childrenWithAuth[child.fullName] != 'Locked') {
                authChildren.add(child)
            }
        }

        render authChildren as JSON
    }

    def getResource() {
        def concept = params.get('concept_key')
        render conceptsResourceService.getByKey(concept) as JSON
    }

    def getModifierChildren() {
        def modifierKey = params.get('modifier_key')
        def appliedPath = params.get('applied_path')
        def qualifiedTermKey = params.get('qualified_term_key')

        if (!modifierKey || !appliedPath || !qualifiedTermKey) {
            throw new InvalidArgumentsException('Missing arguments')
        }

        /* TODO: method needs to be added to the interface */
 /*       if (conceptsResourceService.respondsTo('getModifier')) {
            BoundModifier modifier =
                    conceptsResourceService.getModifier(
                            modifierKey, appliedPath, qualifiedTermKey)
            render modifier.children as JSON
        } else {
            throw new OperationNotSupportedException()
        }*/
    }

}
