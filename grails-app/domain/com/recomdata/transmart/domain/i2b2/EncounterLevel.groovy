package com.recomdata.transmart.domain.i2b2

import org.transmartproject.db.i2b2data.ObservationFact

class EncounterLevel {

    String studyId
    String conceptCode
    String linkType

    static hasMany = [observations: ObservationFact]

    static mapping = {
        table 'DEAPP.DE_ENCOUNTER_LEVEL'
        version false

        studyId column: 'STUDY_ID', type: 'string'
        conceptCode column: 'CONCEPT_CD', type: 'string'
        linkType column: 'LINK_TYPE', type: 'string'

        observations column: ['encounterNum', 'conceptCode', 'providerId', 'startDate', 'modifierCd', 'instanceNum'], type:'string'
    }


}