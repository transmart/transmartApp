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

        studyId column: 'STUDY_ID'
        conceptCode column: 'CONCEPT_CD'
        linkType column: 'LINK_TYPE'
    }


}