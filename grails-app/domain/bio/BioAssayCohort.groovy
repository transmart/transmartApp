package bio
class BioAssayCohort {
		Long id
		String studyId
		String cohortId
		String disease
		String sampleType
		String treatment
		String organism
		String pathology
		String cohortTitle
		String shortDesc
		String longDesc
		

 static mapping = {
	 table 'BIO_ASSAY_COHORT'
	 version false
	 cache usage:'read-only'
	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
	 columns {
		id column:'BIO_ASSAY_COHORT_ID'
		studyId column:'STUDY_ID'
		cohortId column:'COHORT_ID'
		disease column:'DISEASE'
		sampleType column:'SAMPLE_TYPE'
		treatment column:'TREATMENT'
		organism column:'ORGANISM'
		pathology column:'PATHOLOGY'
		cohortTitle column:'COHORT_TITLE'
		shortDesc column:'SHORT_DESC'
		longDesc column:'LONG_DESC'
	 }
	}

 static constraints = {
	}
}
