package com.thomsonreuters.lsps.transmart

import groovyx.net.http.*

class MetacoreEnrichmentService {

    boolean transactional = true

	// cohortGeneList = [ list1, list2 ], where list is just a list of EntrezGene IDs
	// metacoreParams = [ "baseUrl": url, "login": login, "password": password ]
    def getEnrichmentByMaps(cohortGeneLists, metacoreParams) {
		def res
		// only one list is supported so far
		
		// call API functions
		def site = new HTTPBuilder(metacoreParams["baseUrl"])
		if (System.properties.proxyHost && System.properties.proxyPort)
			site.setProxy(System.properties.proxyHost, System.properties.proxyPort.toInteger(), null)
		
		log.info 'MetaCore - logging in'
		site.get( path: '/api/rpc.cgi',
		  query: [ proc: 'login', login: metacoreParams.login, passwd: metacoreParams.password, output: 'json' ] ) {
		  	resp, json ->
			
			  def authKey = json?.Result[0]?.Key
			  if (authKey) {
				  log.info 'MetaCore - running enrichment'
				  site.post( path: '/api/rpc.cgi',
					body: [ proc: 'getEnrichment', diagram_type: 'maps', limit: 5, lists_origin: 'ids', list_name: 'Cohort 1',
					idtype: 'LOCUSLINK', includeObjectIds: 0, output: 'json', auth_key: authKey, id: cohortGeneLists[0] ]) {
					  resp2, json2 ->
						  
					  if (json2?.Code == 0) 
					  	res = json2
				  }
					
				  log.info 'MetaCore - logging out'
				  site.get ( path: '/api/rpc.cgi', query: [ proc: 'logout', auth_key: authKey ] )
			  }
		}
		  
		  return res
    }
}
