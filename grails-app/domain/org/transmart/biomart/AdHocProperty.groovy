package org.transmart.biomart

class AdHocProperty {

	Long id
	Long objectId
	String key
	String value
	
    static mapping = {
		table 'BIO_AD_HOC_PROPERTY'
		version false
		id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		
		columns {
			id column: 'AD_HOC_PROPERTY_ID'
			objectId column: 'BIO_DATA_ID'
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
