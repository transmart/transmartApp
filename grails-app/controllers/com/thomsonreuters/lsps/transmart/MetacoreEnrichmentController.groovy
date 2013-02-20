package com.thomsonreuters.lsps.transmart

//import com.recomdata.transmart.data.association.RModulesService;

import grails.converters.*
import com.recomdata.transmart.data.export.util.FileWriterUtil
import java.util.UUID

class MetacoreEnrichmentController {
	
	def metacoreEnrichmentService
	def springSecurityService
	def RModulesService
	def dataExportService
	
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
		def metacoreParams = metacoreEnrichmentService.getMetacoreParams()
		
		// only cohort1 (first list in the first parameter is used in enrichment for now)
		def fname = params['mrnaFilename']
		def threshold = 0
		
		try {
			threshold = Double.parseDouble(params['zThresholdAbs'])
		}
		catch (e) {}
		
		def f = new File(fname)
		
		def i = 0
		def geneList = [] as Set
		
		f.eachLine {
			line ->
			if (i > 0) {
				def values = line.split("\t")
				def z_score = 0
				
				try {
					z_score = Double.parseDouble(values[7])
				}
				catch (e) {}
				
				if (values[11] && Math.abs(z_score) >= threshold) geneList << values[11]
			}
			
			i++
		}
		
		def cohortData = [
			IdType: 'LOCUSLINK',
			Data: [geneList as List]	
		]
		
		log.info "Running enrichment for ${geneList.size()} genes; |z| >= ${threshold}"
		
		render metacoreEnrichmentService.getEnrichmentByMaps(cohortData, metacoreParams) as JSON
	}
	
	def runAnalysisForMarkerSelection = {
		def metacoreParams = metacoreEnrichmentService.getMetacoreParams()
		
		def cohortData = [
			IdType: params.IdType?:'AFFYMETRIX',
			Data: [params.IdList as List]
		]
		
		log.info "Running enrichment for ${params.IdList.size()} genes (IdType passed: ${params.IdType})"
		
		render metacoreEnrichmentService.getEnrichmentByMaps(cohortData, metacoreParams) as JSON
	}
	
	def serverSettingsWindow = {
		def mode = metacoreEnrichmentService.metacoreSettingsMode()
		def sysDef = metacoreEnrichmentService.systemMetacoreSettingsDefined()
		def settings = metacoreEnrichmentService.getMetacoreParams()
		def isConfigured = metacoreEnrichmentService.areSettingsConfigured()
		
		def res = (mode=='user')?settings:[baseUrl: settings?.baseUrl]
		
		render(view:'metacoreSettingsWindow', model: [settingsConfigured: isConfigured, settingsMode: mode, systemSettingsDefined: sysDef, settings: res])
	}
	
	def saveMetacoreSettings = {
		def settings = params.settings
		
		if (params.containsKey('mode')) {
			log.info "MC Settings - Setting mode: ${params.mode}"
			metacoreEnrichmentService.setMetacoreSettingsMode(params.mode)
		}	
		
		if (params.containsKey('baseUrl')) {
			log.info "MC Settings - Setting baseUrl: ${params.baseUrl}"
			metacoreEnrichmentService.setMetacoreBaseUrl(params.baseUrl)
		}	
		
		if (params.containsKey('login')) {
			log.info "MC Settings - Setting login: ${params.login}"
			metacoreEnrichmentService.setMetacoreLogin(params.login)
		}
		
		if (params.containsKey('password')) {
			log.info "MC Settings - Setting new password"
			metacoreEnrichmentService.setMetacorePassword(params.password)
		}
		
		def res = [ 'result': 'success']
		render res as JSON
	}
	
}
