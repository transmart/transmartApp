package bio

class ExperimentProperty {

	Long id
	Experiment experiment
	String key
	String value
		
	static belongsTo = [experiment: Experiment]
	
    static mapping = {
		table 'BIO_EXPERIMENT_PROPERTY'
		version false
		id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		
		columns {
			id column: 'BIO_EXP_PROPERTY_ID'
			experiment column: 'BIO_DATA_ID'
			key column: 'PROPERTY_KEY'
			value column: 'PROPERTY_VALUE'
		}
    }
	
	static constraints = {
		key (nullable: false)
		value (nullable: false)
	}
	
	String toString() {
		return value
	}
}
