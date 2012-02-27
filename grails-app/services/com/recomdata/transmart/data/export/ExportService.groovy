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

import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.json.JSONArray
import org.json.JSONObject
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;

import com.recomdata.transmart.data.export.ExportDataProcessor
import com.recomdata.transmart.domain.i2b2.AsyncJob;
//import com.recomdata.transmart.domain.searchapp.AccessLog
import com.recomdata.transmart.validate.RequestValidator;
import com.recomdata.asynchronous.GenericJobService;

class ExportService {

    static transactional = true
	def i2b2HelperService
	def i2b2ExportHelperService
	def dataCountService
	def jobResultsService
	def jobStatusService
	def quartzScheduler
	def config = ConfigurationHolder.config

	def getMetaData(params) {
		def dataTypesMap = config.com.recomdata.transmart.data.export.dataTypesMap
		
		//The result instance id's are stored queries which we can use to get information from the i2b2 schema.
		def rID1 = RequestValidator.nullCheck(params.result_instance_id1)
		def rID2 = RequestValidator.nullCheck(params.result_instance_id2)
		def subsetLen = (rID1 && rID2) ? 2 : (rID1 || rID2) ? 1 : 0
		log.debug('rID1 :: ' + rID1 + ' :: rID2 :: ' + rID2)
		
		//Get the subject ID's from our result instance IDs.
		def subjectIds1 = i2b2HelperService.getSubjects(rID1)
		def subjectIds2 = i2b2HelperService.getSubjects(rID2)
		log.debug('subjectIds1 :: ' + subjectIds1 + ' :: subjectIds2 :: ' + subjectIds2)
		
		//Retrieve the counts for each subset. We get back a map that looks like ['RBM':2,'MRNA':30]
		def subset1CountMap = dataCountService.getDataCounts(subjectIds1)
		def subset2CountMap = dataCountService.getDataCounts(subjectIds2)
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
				def file = [:]
				file['fileType'] = '.TXT'
				file['dataFormat'] = ((key == 'CLINICAL') ? '' : (file['fileType'] == '.TXT') ? 'Processed ' : 'Raw ') + 'Data'
				file['fileDataCount'] = finalMap["subset${i}"][key]
				if (null != finalMap["subset${i}"][key] && finalMap["subset${i}"][key] > 0)
					dataTypeHasCounts = true;
				files.put(file);
				
				//Uncomment the following code-snippet once the .CEL file location has been identified
				if (key == 'MRNA' || key == 'SNP') {
					file = [:]
					file['dataFormat'] = 'Raw Data'
					file['fileType'] = '.CEL'
					file['fileDataCount'] = finalMap["subset${i}"][key+'_CEL']
					files.put(file);
				}
				dataType['metadataExists'] = true
				dataType['subsetId'+i] = "subset"+i
				dataType['subsetName'+i] = "Subset "+i
				dataType['subset'+i] = files
			}
			if (dataTypeHasCounts)
			rows.put(dataType)
		}
		/*JSONObject result = new JSONObject()
		JSONArray rows = new JSONArray();
		def i = 1;
		while (i < 3) {
			def row = [:]
			row['subset'] = 'Subset ' + i;
			
			JSONArray dataTypes = new JSONArray();
			dataTypesMap.each { key, value ->
				def dataType = [:]
				dataType['dataTypeId'] = key
				dataType['dataTypeName'] = value
				
				JSONArray files = new JSONArray();
				def file = [:]
				file['fileType'] = '.TXT'
				file['dataFormat'] = ((key == 'CLINICAL') ? '' : (file['fileType'] == '.TXT') ? 'Intensity ' : 'Raw ') + 'Data' 
				file['fileDataCount'] = finalMap["subset${i}"][key]
				files.put(file);*/
				
				//Uncomment the following code-snippet once the .CEL file location has been identified 
				/*file = [:]
				file['dataFormat'] = 'Raw Data'
				file['fileType'] = '.CEL'
				file['fileDataCount'] = i * 87
				files.put(file);*/
				
				/*dataType['files'] = files
				dataTypes.put(dataType)
			}
			
			row['dataTypes'] = dataTypes
			rows.put(row)
			++i
		}*/
		
		//Use this block when we move to ExtJS 3.3.1, we are currently using version 2.2
		/*def j = 1
		while (j < 3) {
			def i = 1
			while (i < 4) {
				def row = [:]
				row['subset'] = 'Subset ' + j
				row['dataTypeId'] = (i == 1) ? 'ClinicalData' : (i==2) ? 'GeneExprData' : 'SNPData'
				row['dataTypeName'] = (i == 1) ? 'Clinical & Low Dimensional Biomarker Data' : (i==2) ? 'Gene Expression Data' : 'SNP Data'
				def k = 1
				while (k < 3) {
					row['fileType'+k] = (k == 1) ? '.TXT' : '.CEL'
					row[row['dataTypeId'] + '_' + row['fileType'+k] + '_fileType'] = (k == 1) ? '.TXT' : '.CEL'
					row[row['dataTypeId'] + '_' + row['fileType'+k] + '_dataFormat'] = (k == 1) ? 'Intensity Data' : 'Raw Data'
					row[row['dataTypeId'] + '_' + row['fileType'+k] + '_fileDataCount'] = (k == 1) ? 87*k : 89*k
					++k
				}
				row[row['dataTypeId'] + '_fileCount'] = k - 1
				++i
				rows.put(row)
			}
			++j
		}*/
		result.put("success", true)
		result.put('exportMetaData', rows)
		
		return result
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
		jobStatusService.updateStatus(jobName, jobStatus, null, querySummary, null)
		
		log.debug("Sending ${newJob.jobName} back to the client")
		JSONObject result = new JSONObject()
		result.put("jobName", jobName)
		result.put("jobStatus", jobStatus)
		
		return result
	}
	
	def private createExportDataJob(userName, params, statusList) {
		//Put together a hashmap with an entry for each file type we need to output.
		def fileTypeMap = [:]
		
		def jobDataTypes = ["STUDY"]; // default is always study metadata
		
		//We need a sub hash for each subset.
		def resultInstanceIdHashMap = [:]
		
		resultInstanceIdHashMap["subset1"] = params.result_instance_id1
		resultInstanceIdHashMap["subset2"] = params.result_instance_id2
		
		//Loop through the values for each selected checkbox.
		def checkboxList = params.selectedSubsetDataTypeFiles
		
	//	println("checkboxList:"+checkboxList);
		
				//If only one was checked, we need to add that one to an array list.
		if(checkboxList instanceof String)
		{
			def tempArray = []
			tempArray.add(checkboxList)
			checkboxList = tempArray
		}
		
		//Split the list on commas first, each box is seperated by ",".
		checkboxList.each
		{
			checkboxItem ->
			
			//Split the item by "_" to get the different attributes.
			String[] checkboxItemArray = checkboxItem.split("_")
			
			//The first item is the subset name.
			String currentSubset = checkboxItemArray[0].trim().replace(" ","")
			
			if(checkboxItemArray.size()>1) {
				//Second item is the data type.
				String currentDataType = checkboxItemArray[1].trim()
				if (checkboxItemArray.size()>2) {
					jobDataTypes.push(currentDataType+checkboxItemArray[2].trim())
				} else {
					jobDataTypes.push(currentDataType)
				}
				//For this data type we add the subset hashmap.
				fileTypeMap[currentDataType] = resultInstanceIdHashMap
			}
		}
				
		def jdm = new JobDataMap()
		jdm.put("analysis", params.analysis)
		jdm.put("userName", userName)
		jdm.put("jobName", params.jobName)
		jdm.put("result_instance_ids",resultInstanceIdHashMap);
		jdm.put("datatypes", jobDataTypes);
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

		if (jobStatusService.updateStatus(params.jobName, statusList[2]))	{
			return
		}
		def trigger = new SimpleTrigger("triggerNow", params.analysis)
		quartzScheduler.scheduleJob(jobDetail, trigger)
	}
	
    def exportData(params, userName) {
		def statusList = ["Started", "Validating Cohort Information",
		 "Triggering Data-Export Job","Gathering Data","Running Conversions","Running Analysis","Rendering Output"]
		
		jobResultsService[params.jobName]["StatusList"] = statusList
		jobStatusService.updateStatus(params.jobName, statusList[0])
		 
		def al = new AccessLog(username:userName, event:"${params.analysis}, Job: ${params.jobName}",
		eventmessage:"", accesstime:new java.util.Date())
		al.save()
		 
		//TODO get the required input parameters for the job and validate them
		def rID1 = RequestValidator.nullCheck(params.result_instance_id1)
		def rID2 = RequestValidator.nullCheck(params.result_instance_id2)
		log.debug('rID1 :: ' + rID1 + ' :: rID2 :: ' + rID2)
		jobStatusService.updateStatus(params.jobName, statusList[1])
		
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
		def config = ConfigurationHolder.config
		def maxJobs = config.com.recomdata.transmart.data.export.max.export.jobs.loaded
		
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
		
		return exportDataProcessor.getExportJobFile(job.viewerURL)
	}
}
