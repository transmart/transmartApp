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

import org.json.JSONObject
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import groovy.json.JsonOutput
import grails.converters.JSON 

class DataExportController {

    def index = {}

    def exportService
    def springSecurityService

    //We need to gather a JSON Object to represent the different data types.
    def getMetaData() {
		def resultInstanceId1 = params.long( "result_instance_id1" )
		def resultInstanceId2 = params.long( "result_instance_id2" )
		
        def clinicalData = exportService.getClinicalMetaData( resultInstanceId1, resultInstanceId2 )
		def metadata = convertIntoMetaDataMap( clinicalData, exportService.getHighDimMetaData( resultInstanceId1, resultInstanceId2 ) )
		
        render metadata as JSON
    }
	
	/**
	 * Converts information about clinical data and high dimensional data into a map
	 * that can be handled by the frontend javascript
	 * @param clinicalData
	 * @param highDimensionalData
	 * @see dataTab.js
	 * @see ExportService.getClinicalMetaData()
	 * @see ExportService.getHighDimMetaData()
	 * @return 	Map with root key "exportMetaData", which in turn contains a list of 
	 * 				datatypes to export. Each item in the list is a map that has keys,
	 * 				as below:
	 * 					subsetId1
	 * 					subsetId2
	 * 					subsetName1
	 * 					subsetName2
	 * 					
	 * 					dataTypeId
	 * 					dataTypeName
	 * 					isHighDimensional
	 * 					metadataExists
	 * 
	 *  				subset1
	 *  				subset2
	 */
	protected Map convertIntoMetaDataMap( clinicalData, highDimensionalData ) {
		def clinicalOutput = [
			subsetId1: "subset1",
			subsetId2: "subset2",
			subsetName1: "Subset 1",
			subsetName2: "Subset 2",
			
			dataTypeId: "CLINICAL",
			dataTypeName: "Clinical & Low Dimensional Biomarker Data",
			isHighDimensional: false,
			metadataExists: true,
			
			subset1: [
				[
					fileType: ".TXT",
					dataFormat: "Data",
					fileDataCount: clinicalData.subset1
				]
			],
			subset2:[
				[
					fileType: ".TXT",
					dataFormat: "Data",
					fileDataCount: clinicalData.subset2
				]
			],
		] 
		
		// Return a map, suited for the frontend to handle
		[
			exportMetaData: [ clinicalOutput ] + convertHighDimMetaData( highDimensionalData )
		]
	}
	
	/**
	 * Converts information about high dimensional data into a map
	 * that can be handled by the frontend javascript
	 * @param highDimensionalData	A list with datatypes that can be exported 
	 * @return 	A list of datatypes to export. Each item in the list is a map that has keys,
	 * 				as below:
	 * 					subsetId1
	 * 					subsetId2
	 * 					subsetName1
	 * 					subsetName2
	 *
	 * 					dataTypeId
	 * 					dataTypeName
	 * 					isHighDimensional
	 * 					metadataExists
	 *
	 *  				subset1
	 *  				subset2
	 * @see dataTab.js
	 * @see ExportService.getHighDimMetaData()
	 */
	protected def convertHighDimMetaData( highDimensionalData ) {
		// TODO: Support multiple export formats per datatype (e.g. raw data and processed data)
		// See ExportService.getMetaData @ 2e2d53d0cba6f6573bf7636de372b96f25312276 for information
		// on how it was specified previously, as well as on the types of data that were allowed
		// for different datatypes
		highDimensionalData.collect { highDimRow ->
			[
				subsetId1: "subset1",
				subsetId2: "subset2",
				subsetName1: "Subset 1",
				subsetName2: "Subset 2",
				
				dataTypeId: highDimRow.datatype.dataTypeName,
				dataTypeName: highDimRow.datatype.dataTypeDescription,
				isHighDimensional: true,
				metadataExists: true,
				
				subset1: [
					[
						fileType: "TXT",
						dataTypeHasCounts: true,
						dataFormat: "Data",
						fileDataCount: highDimRow.subset1 ? highDimRow.subset1.size() : 0,
						platforms: getPlatformsForSubjectSampleMappingList( highDimRow.subset1 )
					]
				],
				subset2:[
					[
						fileType: "TXT",
						dataTypeHasCounts: true,
						dataFormat: "Data",
						fileDataCount: highDimRow.subset2 ? highDimRow.subset2.size() : 0,
						platforms: getPlatformsForSubjectSampleMappingList( highDimRow.subset2 )
					]
				],
			]
		}
	}
	
	/**
	 * Returns a list of unique platforms for a given set of subject sample mappings 
	 * @param ssmList
	 * @return	A list of unique platforms, each being a map with the keys
	 * 				gplId
	 * 				gplTitle
	 * 				fileDataCount
	 */
	def getPlatformsForSubjectSampleMappingList( Collection<DeSubjectSampleMapping> ssmList ) {
		if( !ssmList ) 
			return []
			
		return ssmList*.platform.unique().collect { platform ->
			[
				gplId: platform.id,
				gplTitle: platform.title,
				fileDataCount: ssmList.findAll { ssm -> ssm.platform.id == platform.id }.size()
			]
		}
	}

    def downloadFileExists() {
        def InputStream inputStream = exportService.downloadFile(params);
        response.setContentType("text/json")
        JSONObject result = new JSONObject()

        if (null != inputStream) {
            result.put("fileStatus", true)
            inputStream.close()
        } else {
            result.put("fileStatus", false)
            result.put("message", "Download failed as file could not be found on the server")
        }
        response.outputStream << result.toString()
    }

    def downloadFile() {
        def InputStream inputStream = exportService.downloadFile(params);

        def fileName = params.jobname + ".zip"
        response.setContentType "application/octet-stream"
        response.setHeader "Content-disposition", "attachment;filename=${fileName}"
        response.outputStream << inputStream
        response.outputStream.flush()
        inputStream.close();
        return true;
    }

    /**
     * Method that will create the new asynchronous job name
     * Current methodology is username-jobtype-ID from sequence generator
     */
    def createnewjob() {
        def result = exportService.createExportDataAsyncJob(params, springSecurityService.getPrincipal().username)

        response.setContentType("text/json")
        response.outputStream << result.toString()
    }

    /**
     * Method that will run a data export and is called asynchronously from the datasetexplorer -> Data Export tab
     */
    def runDataExport() {
		// TODO: Implement a proper way to retrieve the correct JSON 
		// from the frontend directly, instead of converting it here
		
		// Convert raw input data into proper format for exportData
		def newExport = [:]
		def selections = params.list( "selectedSubsetDataTypeFiles" )
		[ "subset1", "subset2" ].each { subset ->
			newExport[ subset ] = [ 
				clinical: [:],
				highdim: [:] 
			]
				
			selections.findAll { it.startsWith( subset ) }.each { selection ->
				if( selection.endsWith( "_CLINICAL_TXT" ) ) {
					newExport[ subset ].clinical.columnFilter = []
				} else {
					// Handle different HD data types
					def datatype = selection.replace( subset + "_", "" ).replace( "_TXT", "" )
						newExport[ subset ].highdim[ datatype ] = []
				}
			}
		}
        params['newExport'] = JsonOutput.toJson( newExport ) 

        def jsonResult = exportService.exportData(params, springSecurityService.getPrincipal().username)

        response.setContentType("text/json")
        response.outputStream << jsonResult.toString() 
    }
}


