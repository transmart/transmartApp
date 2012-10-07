package com.thomsonreuters.lsps.transmart

import com.recomdata.transmart.data.association.RModulesService;

import grails.converters.*
import com.recomdata.transmart.data.export.util.FileWriterUtil
import java.util.UUID

class MetacoreEnrichmentController {
	
	def metacoreEnrichmentService
	def springSecurityService
	def RModulesService
	def dataExportService
	
	def defaultMetacoreParams = [
			baseUrl: grailsApplication.config.com.thomsonreuters.transmart.metacoreURL,
			login: grailsApplication.config.com.thomsonreuters.transmart.metacoreDefaultLogin,
			password: grailsApplication.config.com.thomsonreuters.transmart.metacoreDefaultPassword
	]
	
	def index = {
		render (view:"index", model:[])
	}
	
	def prepareData = {
		params['jobName'] = "${springSecurityService.getPrincipal().username}-metacoreEnrichment-${UUID.randomUUID() as String}"
		
		def jobData = RModulesService.prepareDataForExport(springSecurityService.getPrincipal().username, params);
		jobData['subsetSelectedFilesMap'] = [
			subset1: ['CLINICAL.TXT', 'MRNA_DETAILED.TXT'], 
			subset2: ['CLINICAL.TXT', 'MRNA_DETAILED.TXT']
		]
		
		jobData['pivotData'] = false
		jobData['jobTmpDirectory'] = grailsApplication.config.com.recomdata.plugins.tempFolderDirectory + '/' + params['jobName']
		
		FileWriterUtil f = new FileWriterUtil();
		File baseDir = new File(grailsApplication.config.com.recomdata.plugins.tempFolderDirectory)
		f.createDir(baseDir, params['jobName'])
		
		def res = dataExportService.exportData(jobData);
		// for now, just first subset
		def mrnaFilename = "${jobData['jobTmpDirectory']}/${res[0]}_${jobData.studyAccessions[0]}/mRNA/Processed_Data/mRNA.trans"
		def retVal = ['mrnaFilename': mrnaFilename]
		
		render retVal as JSON
	}
	
	def runAnalysis = {
		// TODO: read metacore params
		def metacoreParams = defaultMetacoreParams
		
		// only cohort1 (first list in the first parameter is used in enrichment for now
		def fname = params['mrnaFilename']
		def f = new File(fname)
		
		def i = 0
		def geneList = [] as Set
		
		f.eachLine {
			line ->
			if (i > 0) {
				def values = line.split("\t")
				if (values[11]) geneList << values[11]
			}
			
			i++
		}
		
		log.info "Running enrichment for ${geneList.size()} genes"
		
		def cohortData = [
			IdType: 'LOCUSLINK',
			Data: [geneList as List]	
		]
		
		render metacoreEnrichmentService.getEnrichmentByMaps(cohortData, metacoreParams) as JSON
	}
	
	def runAnalysisForMarkerSelection = {
		// TODO: read metacore params
		def metacoreParams = defaultMetacoreParams
		
		def cohortData = [
			IdType: 'AFFYMETRIX',
			Data: [params.IdList as List]
		]
		
		log.info "Running enrichment for ${params.IdList.size()} genes"
		
		render metacoreEnrichmentService.getEnrichmentByMaps(cohortData, metacoreParams) as JSON
	}
	
}
