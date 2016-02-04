package org.transmartfoundation.status

class SolrStatus {

	boolean connected
	boolean rwgAvailable
	int rwgNumberOfRecords
	boolean browseAvailable
	int browseNumberOfRecords
	boolean sampleAvailable
	int sampleNumberOfRecords
	Date lastProbe

	String toString () {
		return "SolrStatus - probe at: " + lastProbe
	}
}
