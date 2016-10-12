package org.transmart.marshallers

import org.transmartproject.core.ontology.OntologyTerm

//import org.transmartproject.core.ontology.BoundModifier
class OntologyTermMarshaller {

    static targetType = OntologyTerm

    def convert(OntologyTerm term) {
        def ret = [
                key               : term.key,
                level             : term.level,
                fullName          : term.fullName,
                name              : term.name,
                tooltip           : term.tooltip,
                visualAttributes  : term.visualAttributes,
                metadata          : term.metadata,

                /* I'm not comfortable having this here; the web frontend
                 * should only be worried about adding and removing terms
                 * from sets, not care about how the panels are built and how
                 * the terms map to patient sets.
                 * Unfortunately, that is not the way the frontend is setup
                 * right now, as right now it needs this data */
                dimensionCode     : term.dimensionCode,
                dimensionTableName: term.dimensionTableName,
        ]

/*        if (term instanceof BoundModifier) {
            ret['applied_path'] = term.appliedPath
            ret['qualified_term_key'] = term.qualifiedTerm.key
        }
*/
        ret
    }

}
