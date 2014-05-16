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
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 *
 ******************************************************************/


package com.recomdata.transmart.data.export

import com.recomdata.asynchronous.GenericJobExecutor
import com.recomdata.transmart.domain.i2b2.AsyncJob
import com.recomdata.transmart.validate.RequestValidator
import grails.util.Holders
import org.apache.commons.lang.StringUtils
import org.json.JSONArray
import org.json.JSONObject
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.SimpleTrigger
import org.transmart.authorization.CurrentUserBeanProxyFactory
import org.transmart.searchapp.AccessLog

import javax.annotation.Resource

class ExportService {

    static transactional = true
    def i2b2HelperService
    def i2b2ExportHelperService
    def jobResultsService
    def asyncJobService
    def quartzScheduler
    def grailsApplication
    def dataExportService

    @Resource(name = CurrentUserBeanProxyFactory.BEAN_BAME)
    def currentUser

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

        result
    }

    /**
     * Converts the list of selected checkboxes, into a map
     *
     * Each selected checkbox has the format
     *      <subset_id>_<datatype>_<exportformat>_<platform>
     * where exportformat is a string, prepended with a dot
     * (for compatibility reasons). That dot is removed from the
     * string in this method
     *
     * @param selectedCheckboxList List with selected checkboxes
     * @return
     */
    protected Map getHighDimDataTypesAndFormats(selectedCheckboxList) {
        Map formats = [:]

        selectedCheckboxList.each {
            String[] parts = it.split("_")

            // The third part is the export format. However,
            // for compatibility reasons the format is prepended
            // with a dot. That is not necessary anymore
            def format = parts[2][1..-1]

            if (!formats.containsKey(parts[0])) {
                formats[parts[0]] = [:]
            }

            if (!formats[parts[0]].containsKey(parts[1])) {
                formats[parts[0]][parts[1]] = [:]
            }

            if (!formats[parts[0]][parts[1]].containsKey(format)) {
                formats[parts[0]][parts[1]][format] = []
            }

            formats[parts[0]][parts[1]][format] << parts[3]
        }

        formats
    }

    def private Map getSubsetSelectedFilesMap(selectedCheckboxList) {
        def subsetSelectedFilesMap = [:]

        //If only one was checked, we need to add that one to an array list.
        if (selectedCheckboxList instanceof String) {
            selectedCheckboxList = [selectedCheckboxList]
        }
        if (selectedCheckboxList == null) {
            selectedCheckboxList = []
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
                currentSubset = checkboxItemArray[0].trim().replace(" ", "")
                if (null == subsetSelectedFilesMap.get(currentSubset)) {
                    subsetSelectedFilesMap.put(currentSubset, ["STUDY"])
                }
            }

            if (checkboxItemArray.size() > 1) {
                //Second item is the data type.
                String currentDataType = checkboxItemArray[1].trim()
                if (checkboxItemArray.size() > 3) {
                    def jobDataType = currentDataType + checkboxItemArray[2].trim()
                    if (!subsetSelectedFilesMap.get(currentSubset)?.contains(jobDataType)) {
                        subsetSelectedFilesMap.get(currentSubset).push(jobDataType)
                    }
                } else if (checkboxItemArray.size() > 2) {
                    subsetSelectedFilesMap.get(currentSubset)?.push(currentDataType + checkboxItemArray[2].trim())
                } else {
                    subsetSelectedFilesMap.get(currentSubset)?.push(currentDataType)
                }
            }
        }

        subsetSelectedFilesMap
    }

    def getsubsetSelectedPlatformsByFiles(checkboxList) {
        def subsetSelectedPlatformsByFiles = [:]
        //Split the list on commas first, each box is seperated by ",".
        checkboxList.each { checkboxItem ->
            //Split the item by "_" to get the different attributes.
            // Attributes are: <subset_id>_<datatype>_<exportformat>_<platform>
            // e.g. subset1_mrna_TSV_GPL570
            String[] checkboxItemArray = StringUtils.split(checkboxItem, "_")

            //The first item is the subset name.
            def currentSubset = checkboxItemArray[0].trim().replace(" ", "")

            //Fourth item is the selected (gpl) platform
            if (checkboxItemArray.size() > 3) {
                def fileName = checkboxItemArray[1].trim() + checkboxItemArray[2].trim()
                def platform = checkboxItemArray[3].trim()
                if (subsetSelectedPlatformsByFiles.containsKey(currentSubset)) {
                    if (subsetSelectedPlatformsByFiles.get(currentSubset).containsKey(fileName)) {
                        def platformFilesList = subsetSelectedPlatformsByFiles.get(currentSubset).get(fileName)
                        platformFilesList.push(platform)
                    } else {
                        subsetSelectedPlatformsByFiles.get(currentSubset).put(fileName, [platform])
                    }
                } else {
                    def platformsMap = new HashMap()
                    platformsMap.put(fileName, [platform])
                    subsetSelectedPlatformsByFiles.put(currentSubset, platformsMap)
                }
            }
        }

        subsetSelectedPlatformsByFiles
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

        if(checkboxList instanceof String) {
            def tempArray = []
            if (checkboxList && !checkboxList?.trim().equals("")) tempArray.add(checkboxList)
            checkboxList = tempArray
        }

        def jdm = new JobDataMap()
        jdm.put("analysis", params.analysis)
        jdm.put("userName", userName)
        jdm.put("jobName", params.jobName)
        jdm.put("result_instance_ids", resultInstanceIdHashMap);
        jdm.selection = params.selection
        jdm.highDimDataTypes = getHighDimDataTypesAndFormats( checkboxList )
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

        jdm.put("userInContext", currentUser.targetSource.target)

        def jobDetail = new JobDetail(params.jobName, params.analysis, GenericJobExecutor.class)
        jobDetail.setJobDataMap(jdm);

        // TODO -- NEED TO BE REVIEWED (f.guitton@imperial.ac.uk)

        jobDetail.getJobDataMap().put("SGA", grailsApplication);
        jobDetail.getJobDataMap().put("SAJS", asyncJobService);
        jobDetail.getJobDataMap().put("SJRS", jobResultsService);
        jobDetail.getJobDataMap().put("SDES", dataExportService);

        // --

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
        createExportDataJob(userName, params, statusList)
    }

    def getExportJobs(userName) {
        JSONObject result = new JSONObject()
        JSONArray rows = new JSONArray()
        def maxJobs = Holders.config.com.recomdata.transmart.data.export.max.export.jobs.loaded

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

        result
    }

    def downloadFile(params) {
        def jobName = params.jobname
        def job = AsyncJob.findByJobName(jobName)
        def exportDataProcessor = new ExportDataProcessor()

        // If com.recomdata.transmart.data.export.ftp configuration not set, ExportDataProcessor receives incorrect
        // method signature for getExportJobFileStream, so here we have used Strings instead of defs to initialize
        // the correct parameter signature
        String tempDir = grailsApplication.config.com.recomdata.plugins.tempFolderDirectory
        def ftp = grailsApplication.config.com.recomdata.transmart.data.export.ftp

        InputStream is = exportDataProcessor.getExportJobFileStream(job.viewerURL, tempDir,
                ftp.server ?: '', ftp.serverport ?: '21', ftp.username ?: '',
                ftp.password ?: '', ftp.remote.path ?: '')
        is
    }
}
