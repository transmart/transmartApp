package com.thomsonreuters.lsps.transmart

import grails.converters.*

class MetacoreEnrichmentController {
	
	def metacoreEnrichmentService
	
	def index = {
		render (view:"index", model:[])
	}
	
	def runAnalysis = {
		// todo
		def metacoreParams = [
			baseUrl: grailsApplication.config.com.thomsonreuters.transmart.metacoreURL,
			login: grailsApplication.config.com.thomsonreuters.transmart.metacoreDefaultLogin,
			password: grailsApplication.config.com.thomsonreuters.transmart.metacoreDefaultPassword
		]
		
		// only cohort1 (first list in the first parameter is used in enrichment for now
		render metacoreEnrichmentService.getEnrichmentByMaps([[1,2,3,4,5,6,7,8,9,10]], metacoreParams) as JSON
	}
	
}
