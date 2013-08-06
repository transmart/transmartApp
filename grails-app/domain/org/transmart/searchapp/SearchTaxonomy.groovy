package org.transmart.searchapp

class SearchTaxonomy {
	
	static transients = ["children", "parents"]
	Long id
	String termName
	String sourceCd
	Date importDate 
	Long searchKeywordId
	List children = []
	List parents = []
	
	static mapping = {
		table 'SEARCH_TAXONOMY'
		version false
		id generator:'sequence', params:[sequence:'SEQ_SEARCH_TAXONOMY_TERM_ID']
		columns {
		   id column:'TERM_ID'
		   searchKeywordId column: 'SEARCH_KEYWORD_ID'
		   termName column:'TERM_NAME'
		   sourceCd column:'SOURCE_CD'
		   importDate column:'IMPORT_DATE'
		}
    }
	
	static constraints = {
		termName(nullable:false,maxSize:900)
		sourceCd(nullable:true,maxSize:900)
		importDate(nullable:true)
	}	
   	
}
