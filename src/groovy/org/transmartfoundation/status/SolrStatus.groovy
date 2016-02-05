package org.transmartfoundation.status

class SolrStatus {

	String url
	boolean connected
	boolean rwgAvailable
	int rwgNumberOfRecords
	boolean browseAvailable
	int browseNumberOfRecords
	boolean sampleAvailable
	int sampleNumberOfRecords
	Date lastProbe

	String toString () {
		return "SolrStatus (URL: " + url + ") - probe at: " + lastProbe
	}
}
