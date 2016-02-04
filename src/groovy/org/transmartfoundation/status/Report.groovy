package org.transmartfoundation.status

class Report {
	
	Report(String source) {
		this.source = source
	}
	
	String source
    SolrStatus solrStatus
	RserveStatus rserveStatus
	GwavaStatus gwavaStatus
	RestInterfaceStatus restInterfaceStatus

}
