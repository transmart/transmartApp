package com.recomdata.transmart.domain.i2b2;

class ObservationFact {

	String modifierCd
	String patientNum
	String encounterNum
	String sourcesystemCd
	String conceptCd

	ConceptVisit conceptVisit
	
	static mapping = {
		table 'OBSERVATION_FACT'
		version false
		
		modifierCd 		column: 'MODIFIER_CD', 		type: 'string'
		patientNum 		column: 'PATIENT_NUM', 		type: 'string'
		sourcesystemCd 	column: 'SOURCESYSTEM_CD',	type: 'string'
		conceptVisit	column: 'CONCEPT_CD',		type: 'string', insertable: false, updateable: false
		conceptCd		column: 'CONCEPT_CD', 		type: 'string'
	}


}