package org.transmartfoundation.status

import grails.util.Holders

import java.util.Date
//import org.apache.http.impl.client.HttpClientBuilder
//import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpClient
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

        def solrHost = Holders.config.com.rwg.solr.host
		def urlString = "http://"+ solrHost + "/solr/"
		def solrQuery = '*:*'

//        CloseableHttpClient httpClient = HttpClientBuilder.create().build()
        DefaultHttpClient httpClient = new DefaultHttpClient()
		SolrClient solr = new HttpSolrClient(urlString,httpClient)
		
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
//        httpClient from DefaultHttpClient does not have .close()
//        httpClient.close();
		
		def settings = [
            'url'                   : urlString,
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
            // print("server not responding")
			return false
		} catch (Exception ex2) {
            // print(ex2)
            // print("server responding but does not handle ping")
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

