package com.recomdata.transmart.domain.i2b2;

class ConceptVisit {

	String id 
	String visitName
	
	static hasMany = [observations: ObservationFact]
	
	static mapping = {
		table 'DE_CONCEPT_VISIT'
		version false
		
		id column: 'CONCEPT_CD', type: 'string'
		visitName column: 'VISIT_NAME',type:'string'
		
		observations column: 'CONCEPT_CD',type:'string'
	}


}  