package org.transmart.biomart

class BioAnalysisAttribute {
	String studyID
	Long bioAssayAnalysisID
	Long termID
	String sourceCode
	
	static mapping = {
		table 'BIO_ANALYSIS_ATTRIBUTE'			   
		version false
		id column:'BIO_ANALYSIS_ATTRIBUTE_ID'
		id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		columns {
		   studyID column:'STUDY_ID'
		   bioAssayAnalysisID column:'BIO_ASSAY_ANALYSIS_ID'
		   termID column:'TERM_ID'
		   sourceCode column:'SOURCE_CD'
		}
	}
	
	static constraints = {
		studyID(nullable:true, maxSize:255)
		termID(nullable:true)
		sourceCode(nullable:true, maxSize:255)		
	}
}
