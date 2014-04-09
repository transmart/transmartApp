package com.recomdata.transmart.domain.i2b2

class ConceptVisit {

    String id
    String visitName

    static hasMany = [observations: ObservationFactAcrossTrial]

    static mapping = {
        table name: 'DE_CONCEPT_VISIT', schema: 'DEAPP'
        version false

        id column: 'CONCEPT_CD', type: 'string'
        visitName column: 'VISIT_NAME',type:'string'
    }


}  
