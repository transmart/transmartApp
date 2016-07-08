package com.recomdata.transmart.data.export

import com.recomdata.asynchronous.GenericJobExecutor
import com.recomdata.transmart.domain.i2b2.AsyncJob
import com.recomdata.transmart.validate.RequestValidator
import grails.converters.JSON
import org.apache.commons.lang.StringUtils
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
    def currentUserBean


    def createExportDataAsyncJob(params, userName) {
        def analysis = params.analysis
        def jobStatus = "Started"

        def newJob = new AsyncJob(lastRunOn: new Date())
        newJob.jobType = analysis
        newJob.jobStatus = jobStatus
        newJob.save()

        def jobName = userName + "-" + analysis + "-" + newJob.id
        newJob.jobName = jobName
        newJob.altViewerURL = 'Test'
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
     * Fixes up the data export request data.
     * Format of individual values of selectedSubsetDataTypeFiles:
     *{*   subset: subset<1|2>,
     *   dataTypeId: <data type>,
     *   fileType: <EXTENSION>
     *}*
     * This method than builds a pointlessly deeply nested map for you!
     *
     * @param selectedCheckboxList List with selected checkboxes
     * @return
     */
    protected Map getHighDimDataTypesAndFormats(selectedCheckboxList) {
        Map formats = [:]

        selectedCheckboxList.collect {
            JSON.parse(it.toString())
        }.each { Map checkbox ->
            def fileType = checkbox.fileType

            if (!formats.containsKey(checkbox.subset)) {
                formats[checkbox.subset] = [:]
            }

            if (!formats[checkbox.subset].containsKey(checkbox.dataTypeId)) {
                formats[checkbox.subset][checkbox.dataTypeId] = [:]
            }

            if (!formats[checkbox.subset][checkbox.dataTypeId].containsKey(fileType)) {
                formats[checkbox.subset][checkbox.dataTypeId][fileType] = []
            }
        }

        formats
    }

    def private Map getSubsetSelectedFilesMap(List selectedCheckboxJsonList) {
        def subsetSelectedFilesMap = [:]

        selectedCheckboxJsonList?.unique()?.each { checkboxItem ->
            String currentSubset = null
            if (checkboxItem.subset) {
                //The first item is the subset name.
                currentSubset = checkboxItem.subset.trim().replace(" ", "")
                if (null == subsetSelectedFilesMap.get(currentSubset)) {
                    subsetSelectedFilesMap.put(currentSubset, [])
                }
            }

            if (checkboxItem.dataTypeId) {
                //Second item is the data type.
                String selectedFile = checkboxItem.dataTypeId.trim()
                if (!(checkboxItem.fileType in ['.TSV', 'TSV'])) {
                    selectedFile += checkboxItem.fileType
                }
                subsetSelectedFilesMap.get(currentSubset)?.push(selectedFile)
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

        if (checkboxList instanceof String) {
            def tempArray = []
            if (checkboxList && !checkboxList?.trim().equals("")) tempArray.add(checkboxList)
            checkboxList = tempArray
        }

        def jdm = new JobDataMap()
        jdm.put("analysis", params.analysis)
        jdm.put("userName", userName)
        jdm.put("jobName", params.jobName)
        jdm.put("result_instance_ids", resultInstanceIdHashMap)
        jdm.selection = params.selection
        jdm.highDimDataTypes = getHighDimDataTypesAndFormats(checkboxList)
        jdm.put("subsetSelectedPlatformsByFiles", getsubsetSelectedPlatformsByFiles(checkboxList))
        jdm.put("checkboxList", checkboxList);
        def checkedFileTypes = [params.selectedSubsetDataTypeFiles].flatten().collect { JSON.parse(it) }
        jdm.put("subsetSelectedFilesMap", getSubsetSelectedFilesMap(checkedFileTypes))
        jdm.put("resulttype", "DataExport")
        jdm.put("studyAccessions", i2b2ExportHelperService.findStudyAccessions(resultInstanceIdHashMap.values().toArray()))

        //Add the pivot flag to the jobs map.
        jdm.put("pivotData", (new Boolean(true)));

        //TODO: This should be a part of something else, config files eventually.
        //This is hardcoded for now but it adds the step of bundling the files to a workflow.
        jdm.put("analysisSteps", ["bundle": ""]);

        //This adds a step to the job to create a file link as the plugin output.
        jdm.put("renderSteps", ["FILELINK": ""]);

        jdm.put("userInContext", currentUserBean.targetSource.target)

        def jobDetail = new JobDetail(params.jobName, params.analysis, GenericJobExecutor.class)
        jobDetail.setJobDataMap(jdm)

        if (asyncJobService.updateStatus(params.jobName, statusList[2])) {
            return
        }
        def trigger = new SimpleTrigger("triggerNow" + Math.random(), params.analysis)
        quartzScheduler.scheduleJob(jobDetail, trigger)
    }

    def exportData(params, userName) {
        def statusList = ["Started", "Validating Cohort Information",
                          "Triggering Data-Export Job", "Gathering Data", "Running Conversions", "Running Analysis", "Rendering Output"]

        jobResultsService[params.jobName]["StatusList"] = statusList
        asyncJobService.updateStatus(params.jobName, statusList[0])

        //TODO get the required input parameters for the job and validate them
        def rID1 = RequestValidator.nullCheck(params.result_instance_id1)
        def rID2 = RequestValidator.nullCheck(params.result_instance_id2)
        log.debug('rID1 :: ' + rID1 + ' :: rID2 :: ' + rID2)
        asyncJobService.updateStatus(params.jobName, statusList[1])

        log.debug("Checking to see if the user cancelled the job prior to running it")
        if (jobResultsService[params.jobName]["Status"] == "Cancelled") {
            log.warn("${params.jobName} has been cancelled")
            return
        }
        createExportDataJob(userName, params, statusList)
    }

    def downloadFile(params) {
        def jobName = params.jobname
        def job = AsyncJob.findByJobName(jobName)
        def exportDataProcessor = new ExportDataProcessor()

        return exportDataProcessor.getExportJobFileStream(job.viewerURL)
    }
}
