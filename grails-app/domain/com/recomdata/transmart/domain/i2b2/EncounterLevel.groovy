package com.recomdata.transmart.domain.i2b2

class EncounterLevel {

	String studyId 
	String conceptCode
	String linkType
	
	static hasMany = [observations: ObservationFact]
	
	static mapping = {
		table 'DE_ENCOUNTER_LEVEL'
		version false
		
		studyId column: 'STUDY_ID', type: 'string'
		conceptCode column: 'CONCEPT_CD', type: 'string'
		linkType column: 'LINK_TYPE', type: 'string'
		
		observations column: 'CONCEPT_CD',type:'string'
	}


}