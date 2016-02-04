package org.transmartfoundation.status

import java.util.Date
import grails.transaction.Transactional
import org.apache.solr.common.SolrDocumentList
import org.apache.solr.common.params.SolrParams
import org.apache.solr.common.util.NamedList
import org.apache.solr.client.solrj.SolrClient
import org.apache.solr.client.solrj.SolrServerException
import org.apache.solr.client.solrj.impl.HttpSolrClient
import org.apache.solr.client.solrj.response.QueryResponse

class SolrStatusService {

    def getStatus() {
		
		// https://lucene.apache.org/solr/5_4_1/solr-solrj/index.html

		def urlString = "http://localhost:8983/solr/"
		def solrQuery = '*:*'

		SolrClient solr = new HttpSolrClient(urlString)
		
		NamedList nl = new NamedList()
		nl.addAll(['q':solrQuery])
		SolrParams p = SolrParams.toSolrParams(nl);
		
		boolean reachedServer = serverDoesRespond(solr)
	
		def nDocs = getDocumentCountForQuery(solr,'rwg',p)
		def rwgAvailable = (nDocs > -1)
		def rwgNumberOfRecords = (nDocs > -1)?nDocs:0
		
		nDocs = getDocumentCountForQuery(solr,'browse',p)
		def browseAvailable = (nDocs > -1)
		def browseNumberOfRecords = (nDocs > -1)?nDocs:0

		nDocs = getDocumentCountForQuery(solr,'sample',p)
		def sampleAvailable = (nDocs > -1)
		def sampleNumberOfRecords = (nDocs > -1)?nDocs:0

		def canConnect = reachedServer
			
		solr.close();
		
		def settings = [
			'connected'             : canConnect,
			'rwgAvailable'          : rwgAvailable,
			'rwgNumberOfRecords'    : rwgNumberOfRecords,
			'browseAvailable'       : browseAvailable,
			'browseNumberOfRecords' : browseNumberOfRecords,
			'sampleAvailable'       : sampleAvailable,
			'sampleNumberOfRecords' : sampleNumberOfRecords,
			'lastProbe'             : new Date()
		]
		
		SolrStatus status = new SolrStatus(settings)
		return status
    }

	def serverDoesRespond(solr) {
		try {
			solr.ping();
			return true
		} catch (SolrServerException ex) {
			// server not responding
			return false
		} catch (Exception ex2) {
			// server responding but does not handle ping
			return true
		}
	}
	
	def getDocumentCountForQuery(client,coreName,sp) {
		def n = -1
		try {
			QueryResponse queryResponse = client.query(coreName, sp);
			n = queryResponse.results.numFound
		} catch (all) {
			// print "$coreName not available"
		}
		return n
	}
}

