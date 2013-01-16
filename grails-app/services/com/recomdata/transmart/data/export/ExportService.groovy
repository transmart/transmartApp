/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

package com.recomdata.transmart.data.export

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.json.JSONArray
import org.json.JSONObject
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;

import com.recomdata.transmart.data.export.ExportDataProcessor
import com.recomdata.transmart.domain.i2b2.AsyncJob;
import org.transmart.searchapp.AccessLog
import com.recomdata.transmart.validate.RequestValidator;
import com.recomdata.asynchronous.GenericJobService;

class ExportService {

    static transactional = true
	def geneExpressionDataService
	def i2b2HelperService
	def i2b2ExportHelperService
	def dataCountService
	def jobResultsService
	def asyncJobService
	def quartzScheduler
	def grailsApplication

	def Map createJSONFileObject(fileType, dataFormat, fileDataCount, gplId, gplTitle) {
		def file = [:]
		if(dataFormat!=null){
			file['dataFormat'] = dataFormat
		}
		if(fileType!=null){
			file['fileType'] = fileType
		}
		if(fileDataCount!=null){
			file['fileDataCount'] = fileDataCount
		}
		if(gplId!=null){
			file['gplId']=gplId
		}
		if(gplTitle!=null){
			file['gplTitle']=gplTitle
		}
		return file
	}
	
	def getMetaData(params) {
		def dataTypesMap = grailsApplication.config.com.recomdata.transmart.data.export.dataTypesMap
		
		//The result instance id's are stored queries which we can use to get information from the i2b2 schema.
		def rID1 = RequestValidator.nullCheck(params.result_instance_id1)
		def rID2 = RequestValidator.nullCheck(params.result_instance_id2)
		def rIDs = null
		if (rID1 && rID1?.trim() != '' && rID2 && rID2?.trim() != '') rIDs = rID1 + ',' + rID2
		else if (rID1 && rID1?.trim() != '') rIDs = rID1
		else if (rID2 && rID2?.trim() != '') rIDs = rID2
		
		def subsetLen = (rID1 && rID2) ? 2 : (rID1 || rID2) ? 1 : 0
		log.debug('rID1 :: ' + rID1 + ' :: rID2 :: ' + rID2)
				
		//Retrieve the counts for each subset. We get back a map that looks like ['RBM':2,'MRNA':30]
		def subset1CountMap = dataCountService.getDataCounts(rID1, rIDs)
		def subset2CountMap = dataCountService.getDataCounts(rID2, rIDs)
		log.debug('subset1CountMap :: ' + subset1CountMap + ' :: subset2CountMap :: ' + subset2CountMap)
		
		//This is the map we render to JSON.
		def finalMap = [:]
		
		//Add our counts to the map.
		finalMap['subset1'] = subset1CountMap
		finalMap['subset2'] = subset2CountMap
		//render '{"subset1": [{"PLINK": "102","RBM":"28"}],"subset2": [{"PLINK": "1","RBM":"2"}]}'
		JSONObject result = new JSONObject()
		result.put('noOfSubsets', subsetLen)
		
		JSONArray rows = new JSONArray();
		dataTypesMap.each { key, value ->
			def dataType = [:]
			def dataTypeHasCounts = false
			dataType['dataTypeId'] = key
			dataType['dataTypeName'] = value
			//TODO replace 2 with subsetLen
			for (i in 1..2) {
				JSONArray files = new JSONArray();
				
				if (key == 'CLINICAL') {
					files.put(createJSONFileObject('.TXT', 'Data', finalMap["subset${i}"][key], null, null))
				} else if (key == 'MRNA') {
					def countsMap = createCountsMap('.TXT', 'Processed Data', finalMap, key, i)
					dataTypeHasCounts=dataTypeHasCounts||countsMap.get('dataTypeHasCounts')
					files.put(countsMap)
					files.put(createJSONFileObject('.CEL', 'Raw Data', finalMap["subset${i}"][key+'_CEL'], null, null))
				} else if (key == 'SNP') {
					files.put(createJSONFileObject('.PED, .MAP & .CNV', 'Processed Data', finalMap["subset${i}"][key], null, null))
					files.put(createJSONFileObject('.CEL', 'Raw Data', finalMap["subset${i}"][key+'_CEL'], null, null))
				} else if (key == 'ADDITIONAL') {
					files.put(createJSONFileObject('', 'Additional Data', finalMap["subset${i}"][key], null, null))
				} else if (key == 'GSEA') {
					if (i==1) {
						def countsMap = createCountsMap('.GCT & .CLS', 'Processed Data (for both subsets)',finalMap, key, i)
						dataTypeHasCounts=dataTypeHasCounts||countsMap.get('dataTypeHasCounts')
						files.put(countsMap)
					}
				}
				if (!(['MRNA', 'GSEA'].contains(key)) && (null != finalMap["subset${i}"][key] && finalMap["subset${i}"][key] > 0))
					dataTypeHasCounts = true;
				
				dataType['metadataExists'] = true
				dataType['subsetId'+i] = "subset"+i
				dataType['subsetName'+i] = "Subset "+i
				dataType['subset'+i] = files
			}
			if (dataTypeHasCounts) rows.put(dataType)
		}

		result.put("success", true)
		result.put('exportMetaData', rows)
		
		return result
	}
	
	def createCountsMap(fileType, dataFormat, finalMap, key, subsetIdx){
		def dataTypeHasCounts = false
		def countsMap = createJSONFileObject(fileType, dataFormat, null, null, null)
		def platforms = new JSONArray()
		finalMap["subset${subsetIdx}"][key].each {gplId, count->
			if(count>0){
				platforms.put(createJSONFileObject(null, null,
													count, gplId, geneExpressionDataService.getGplTitle(gplId)))
			}
			dataTypeHasCounts = (dataTypeHasCounts||(count>0))
		}
		countsMap.put('platforms',platforms)
		countsMap.put('dataTypeHasCounts', dataTypeHasCounts)
		return countsMap
	}
	
	def createExportDataAsyncJob(params, userName) {
		def analysis = params.analysis
		def jobStatus = "Started"
		
		def newJob = new AsyncJob(lastRunOn:new Date())
		newJob.save()
		
		def jobName = userName + "-" + analysis + "-" + newJob.id
		newJob.jobName = jobName
		newJob.jobStatus = jobStatus
		newJob.jobType = analysis
		newJob.altViewerURL = 'Test'
		//params.querySummary1 + ((params.querySummary2 != '') ? ' <br/> ' + params.querySummary2 : '')
		newJob.save()
		
		jobResultsService[jobName] = [:]
		//jobResultsService[jobName]['altViewerURL'] = params.querySummary1 + ((params.querySummary2 != '') ? ' <br/> ' + params.querySummary2 : '')
		def querySummary = 'Subset 1:' + params.querySummary1 + ((params.querySummary2 != '') ? ' <br/> Subset 2:' + params.querySummary2 : '')
		asyncJobService.updateStatus(jobName, jobStatus, null, querySummary, null)
		
		log.debug("Sending ${newJob.jobName} back to the client")
		JSONObject result = new JSONObject()
		result.put("jobName", jobName)
		result.put("jobStatus", jobStatus)
		
		return result
	}
	
	def private Map getSubsetSelectedFilesMap(checkboxList) {
		def subsetSelectedFilesMap = [:]
		def selectedCheckboxList = []
		//If only one was checked, we need to add that one to an array list.
		if(checkboxList instanceof String)
		{
			def tempArray = []
			tempArray.add(checkboxList)
			selectedCheckboxList = tempArray
		} else {
			selectedCheckboxList = checkboxList
		}
		
		//Remove duplicates. duplicates are coming in from the UI, better handle it here
		//The same issue is handled in the UI now so the following code may not be necessary
		def tempArray = [] as Set
		tempArray.addAll(selectedCheckboxList)
		selectedCheckboxList = tempArray.toList()
		
		//Prepare a map like ['subset1'
		selectedCheckboxList.each { checkboxItem ->
			//Split the item by "_" to get the different attributes.
			String[] checkboxItemArray = checkboxItem.split("_")
			String currentSubset = null
			if (checkboxItemArray.size() > 0) {
				//The first item is the subset name.
				currentSubset = checkboxItemArray[0].trim().replace(" ","")
				if (null == subsetSelectedFilesMap.get(currentSubset)) subsetSelectedFilesMap.put(currentSubset, ["STUDY"])
			}
			
			if (checkboxItemArray.size() > 1) {
				//Second item is the data type.
				String currentDataType = checkboxItemArray[1].trim()
				if (checkboxItemArray.size()>3) {
					def jobDataType = currentDataType+checkboxItemArray[2].trim()
					if (!subsetSelectedFilesMap.get(currentSubset)?.contains(jobDataType)) {
						subsetSelectedFilesMap.get(currentSubset).push(jobDataType)
					}
				} else if (checkboxItemArray.size()>2) {
					subsetSelectedFilesMap.get(currentSubset)?.push(currentDataType+checkboxItemArray[2].trim())
				} else {
					subsetSelectedFilesMap.get(currentSubset)?.push(currentDataType)
				}
			}
		}
		
		return subsetSelectedFilesMap
	}
	
	def getsubsetSelectedPlatformsByFiles(checkboxList){
		def subsetSelectedPlatformsByFiles=[:]
		//Split the list on commas first, each box is seperated by ",".
		checkboxList.each {checkboxItem ->
			//Split the item by "_" to get the different attributes.
			String[] checkboxItemArray = StringUtils.split(checkboxItem, "_")
			
			//The first item is the subset name.
			def currentSubset = checkboxItemArray[0].trim().replace(" ","")
			
			//Fourth item is the selected (gpl) platform
			if(checkboxItemArray.size()>3){
				def fileName = checkboxItemArray[1].trim()+checkboxItemArray[2].trim()
				def platform = checkboxItemArray[3].trim()
				if(subsetSelectedPlatformsByFiles.containsKey(currentSubset)){
					if(subsetSelectedPlatformsByFiles.get(currentSubset).containsKey(fileName)){
						def platformFilesList = subsetSelectedPlatformsByFiles.get(currentSubset).get(fileName)
						platformFilesList.push(platform)
					}else{
						subsetSelectedPlatformsByFiles.get(currentSubset).put(fileName,[platform])
					}
				}else{
					def platformsMap = new HashMap()
					platformsMap.put(fileName,[platform])
					subsetSelectedPlatformsByFiles.put(currentSubset,platformsMap)
				}
			}
		}
		return subsetSelectedPlatformsByFiles
	}
	
	def private createExportDataJob(userName, params, statusList) {
		//Put together a hashmap with an entry for each file type we need to output.
		def fileTypeMap = [:]
		
		//def jobDataTypes = ["STUDY"]; // default is always study metadata
		
		def selectedPlatformsByDataType
		
		//We need a sub hash for each subset.
		def resultInstanceIdHashMap = [:]
		
		resultInstanceIdHashMap["subset1"] = params.result_instance_id1
		resultInstanceIdHashMap["subset2"] = params.result_instance_id2
		
		//Loop through the values for each selected checkbox.
		def checkboxList = params.selectedSubsetDataTypeFiles
		
		if(checkboxList instanceof String)
		{
			def tempArray = []
			if (checkboxList && !checkboxList?.trim().equals("")) tempArray.add(checkboxList)
			checkboxList = tempArray
		}
		
		def jdm = new JobDataMap()
		jdm.put("analysis", params.analysis)
		jdm.put("userName", userName)
		jdm.put("jobName", params.jobName)
		jdm.put("result_instance_ids",resultInstanceIdHashMap);
		//jdm.put("datatypes", jobDataTypes);
		jdm.put("subsetSelectedPlatformsByFiles", getsubsetSelectedPlatformsByFiles(checkboxList))
		jdm.put("checkboxList", checkboxList);
		jdm.put("subsetSelectedFilesMap", getSubsetSelectedFilesMap(params.selectedSubsetDataTypeFiles))
		jdm.put("resulttype", "DataExport")
		jdm.put("studyAccessions", i2b2ExportHelperService.findStudyAccessions(resultInstanceIdHashMap.values()) )
		
		//Add the pivot flag to the jobs map.
		jdm.put("pivotData", (new Boolean(true)));
		
		//TODO: This should be a part of something else, config files eventually.
		//This is hardcoded for now but it adds the step of bundling the files to a workflow.
		jdm.put("analysisSteps",["bundle":""]);

		//This adds a step to the job to create a file link as the plugin output.
		jdm.put("renderSteps",["FILELINK":""]);
				
		def jobDetail = new JobDetail(params.jobName, params.analysis, GenericJobService.class)
		jobDetail.setJobDataMap(jdm)

		if (asyncJobService.updateStatus(params.jobName, statusList[2]))	{
			return
		}
		def trigger = new SimpleTrigger("triggerNow"+Math.random(), params.analysis)
		quartzScheduler.scheduleJob(jobDetail, trigger)
	}
	
    def exportData(params, userName) {
		def statusList = ["Started", "Validating Cohort Information",
		 "Triggering Data-Export Job","Gathering Data","Running Conversions","Running Analysis","Rendering Output"]
		
		jobResultsService[params.jobName]["StatusList"] = statusList
		asyncJobService.updateStatus(params.jobName, statusList[0])
		 
		def al = new AccessLog(username:userName, event:"${params.analysis}, Job: ${params.jobName}",
		eventmessage:"", accesstime:new java.util.Date())
		al.save()
		 
		//TODO get the required input parameters for the job and validate them
		def rID1 = RequestValidator.nullCheck(params.result_instance_id1)
		def rID2 = RequestValidator.nullCheck(params.result_instance_id2)
		log.debug('rID1 :: ' + rID1 + ' :: rID2 :: ' + rID2)
		asyncJobService.updateStatus(params.jobName, statusList[1])
		
		log.debug("Checking to see if the user cancelled the job prior to running it")
		if (jobResultsService[params.jobName]["Status"] == "Cancelled")	{
			log.warn("${params.jobName} has been cancelled")
			return
		}
		createExportDataJob(userName, params, statusList);
	}
	
	def getExportJobs(userName) {
		JSONObject result = new JSONObject()
		JSONArray rows = new JSONArray()
		def maxJobs = grailsApplication.config.com.recomdata.transmart.data.export.max.export.jobs.loaded
		
		maxJobs = maxJobs ? maxJobs : 0
		
		//TODO find out why the domain class AsyncJob was not getting imported. Is it because it is in the default package?
		def c = AsyncJob.createCriteria()
		def jobResults = c {
			maxResults(maxJobs)
			like("jobName", "${userName}%")
			eq("jobType", "DataExport")
			//ge("lastRunOn", new Date()-30)
			order("id", "desc")
		}
		def m = [:]
		jobResults.each	{
			m = [:]
			m["name"] = it.jobName
			m["status"] = it.jobStatus
			m["runTime"] = it.runTime
			m["startDate"] = it.lastRunOn
			m["viewerURL"] = it.viewerURL
			m["querySummary"] = it.altViewerURL
			rows.put(m)
		}
		
		result.put("success", true)
		result.put("totalCount", jobResults.size())
		result.put("exportJobs", rows)
		
		return result;
	}
	
	def downloadFile(params) {
		def jobName = params.jobname
		def job = AsyncJob.findByJobName(jobName)
		def exportDataProcessor = new ExportDataProcessor()
		
		def tempDir = grailsApplication.config.com.recomdata.plugins.tempFolderDirectory
		def ftpServer = grailsApplication.config.com.recomdata.transmart.data.export.ftp.server
		def ftpServerPort = grailsApplication.config.com.recomdata.transmart.data.export.ftp.serverport
		def ftpServerUserName = grailsApplication.config.com.recomdata.transmart.data.export.ftp.username
		def ftpServerPassword = grailsApplication.config.com.recomdata.transmart.data.export.ftp.password
		def ftpServerRemotePath = grailsApplication.config.com.recomdata.transmart.data.export.ftp.remote.path

		return exportDataProcessor.getExportJobFileStream(job.viewerURL, tempDir, ftpServer, ftpServerPort, ftpServerUserName, ftpServerPassword, ftpServerRemotePath)
	}
}
