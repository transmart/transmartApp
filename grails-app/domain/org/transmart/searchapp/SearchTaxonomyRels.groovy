package org.transmart.searchapp

class SearchTaxonomyRels {
	Long id
	SearchTaxonomy child
	SearchTaxonomy parent
	
	static mapping = {
		table 'SEARCH_TAXONOMY_RELS'
		version false
		id generator:'sequence', params:[sequence:'SEQ_SEARCH_TAXONOMY_RELS_ID']
		columns {
		   id column:'SEARCH_TAXONOMY_RELS_ID'
		   child column:'CHILD_ID'
		   parent column:'PARENT_ID'
		}
	}
	
	static constraints = {
		parent(nullable:true)
		child(nullable:false)
	}
   
}