package com.recomdata.transmart.domain.i2b2

import org.transmartproject.db.i2b2data.ObservationFact

class ConceptVisit {

    String id
    String visitName

    static hasMany = [observations: ObservationFact]

    static mapping = {
        table 'DEAPP.DE_CONCEPT_VISIT'
        version false

        id column: 'CONCEPT_CD', type: 'string'
        visitName column: 'VISIT_NAME',type:'string'
    }


}  