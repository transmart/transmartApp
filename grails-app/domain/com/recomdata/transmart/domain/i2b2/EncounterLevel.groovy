package com.recomdata.transmart.domain.i2b2

class EncounterLevel {

    String studyId
    String conceptCode
    String linkType

    static mapping = {
        table 'DEAPP.DE_ENCOUNTER_LEVEL'
        version false

        studyId column: 'STUDY_ID'
        conceptCode column: 'CONCEPT_ID'
        linkType column: 'LINK_TYPE'
    }


}