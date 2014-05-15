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

import grails.util.Holders

import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.export.HighDimExporter

class ExportMetadataService {

    static transactional = true
	def dataCountService
    def highDimensionResourceService
    def highDimExporterRegistry

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

    def getMetaData(Long resultInstanceId1, Long resultInstanceId2) {
        def metadata = convertIntoMetaDataMap( 
            getClinicalMetaData( resultInstanceId1, resultInstanceId2 ), 
            getHighDimMetaData( resultInstanceId1, resultInstanceId2 ) 
        )

        metadata.exportMetaData.addAll(
            getLegacyHighDimensionMetaData(resultInstanceId1, resultInstanceId2)
        )
        
        metadata
    }
    

    def getClinicalMetaData(Long resultInstanceId1, Long resultInstanceId2 ) {
        //The result instance id's are stored queries which we can use to get information from the i2b2 schema.
        log.debug('rID1 :: ' + resultInstanceId1 + ' :: rID2 :: ' + resultInstanceId1)

        //Retrieve the counts for each subset.
        [
            subset1: resultInstanceId1 ? dataCountService.getClinicalDataCount( resultInstanceId1 ) : 0,    
            subset2: resultInstanceId2 ? dataCountService.getClinicalDataCount( resultInstanceId2 ) : 0,    
        ]
    }
    
    def getHighDimMetaData(Long resultInstanceId1, Long resultInstanceId2) {
        def (datatypes1, datatypes2) = [[:], [:]]

        if (resultInstanceId1) {
            def dataTypeConstraint = highDimensionResourceService.createAssayConstraint(
                    AssayConstraint.PATIENT_SET_CONSTRAINT,
                    result_instance_id: resultInstanceId1)

            datatypes1 = highDimensionResourceService.getSubResourcesAssayMultiMap([dataTypeConstraint])
        }

        if (resultInstanceId2) {
            def dataTypeConstraint = highDimensionResourceService.createAssayConstraint(
                    AssayConstraint.PATIENT_SET_CONSTRAINT,
                    result_instance_id: resultInstanceId2)

            datatypes2 = highDimensionResourceService.getSubResourcesAssayMultiMap([dataTypeConstraint])
        }
        
        // Determine the unique set of datatypes, for both subsets
        def uniqueDatatypes = ( datatypes1.keySet() + datatypes2.keySet() ).unique()
        
        // Combine the two subsets, into a map based on datatypes
        def hdMetaData = uniqueDatatypes.collect { datatype ->
            [
                datatype: datatype,
                subset1: datatypes1[ datatype ],
                subset2: datatypes2[ datatype ]
            ]
        }

        hdMetaData
    }

    /*
     * This method was taken from the ExportService before high dimensional datatypes were exported through core-api.
     * SNP data is not yet implemented there. FIXME: implement SNP in core-db and remove this method
     */
    def getLegacyHighDimensionMetaData(Long resultInstanceId1, Long resultInstanceId2) {
        def dataTypesMap = Holders.config.com.recomdata.transmart.data.export.dataTypesMap

        //The result instance id's are stored queries which we can use to get information from the i2b2 schema.
        def rIDs = [resultInstanceId1, resultInstanceId2].toArray( new Long[0] )

        def subsetLen = (resultInstanceId1 && resultInstanceId2) ? 2 : (resultInstanceId1 || resultInstanceId2) ? 1 : 0
        log.debug('rID1 :: ' + resultInstanceId1 + ' :: rID2 :: ' + resultInstanceId2)

        //Retrieve the counts for each subset. We get back a map that looks like ['RBM':2,'MRNA':30]
        def subset1CountMap = dataCountService.getDataCounts(resultInstanceId1, rIDs)
        def subset2CountMap = dataCountService.getDataCounts(resultInstanceId2, rIDs)
        log.debug('subset1CountMap :: ' + subset1CountMap + ' :: subset2CountMap :: ' + subset2CountMap)

        //This is the map we render to JSON.
        def finalMap = [:]

        //Add our counts to the map.
        finalMap['subset1'] = subset1CountMap
        finalMap['subset2'] = subset2CountMap
        //render '{"subset1": [{"PLINK": "102","RBM":"28"}],"subset2": [{"PLINK": "1","RBM":"2"}]}'
        def result = [:]
        result.put('noOfSubsets', subsetLen)

        def rows = []
        dataTypesMap.each { key, value ->
            if (key != 'SNP') return
            def dataType = [:]
            def dataTypeHasCounts = false
            dataType['dataTypeId'] = key
            dataType['dataTypeName'] = value
            //TODO replace 2 with subsetLen
            for (i in 1..2) {
                def files = []
                if (key == 'SNP') {
                    files.add(createJSONFileObject('.PED, .MAP & .CNV', 'Processed Data',
                            finalMap["subset${i}"][key],
                            null, null))
                    files.add(createJSONFileObject('.CEL', 'Raw Data', finalMap["subset${i}"][key + '_CEL'], null,
                            null))
                }
                if ((null != finalMap["subset${i}"][key] && finalMap["subset${i}"][key] > 0))
                    dataTypeHasCounts = true;

                dataType['metadataExists'] = true
                dataType['subsetId' + i] = "subset" + i
                dataType['subsetName' + i] = "Subset " + i
                dataType['subset' + i] = files
                dataType.isHighDimensional = true
            }
            if (dataTypeHasCounts) rows.add(dataType)
        }

        return rows
    }    
    
    /**
     * Converts information about clinical data and high dimensional data into a map
     * that can be handled by the frontend javascript
     * @param clinicalData
     * @param highDimensionalData
     * @see dataTab.js
     * @see ExportService.getClinicalMetaData()
     * @see ExportService.getHighDimMetaData()
     * @return  Map with root key "exportMetaData", which in turn contains a list of 
     *              datatypes to export. Each item in the list is a map that has keys,
     *              as below:
     *                  subsetId1
     *                  subsetId2
     *                  subsetName1
     *                  subsetName2
     *                  
     *                  dataTypeId
     *                  dataTypeName
     *                  isHighDimensional
     *                  metadataExists
     * 
     *                  subset1
     *                  subset2
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
     * @param highDimensionalData   A list with datatypes that can be exported 
     * @return  A list of datatypes to export. Each item in the list is a map that has keys,
     *              as below:
     *                  subsetId1
     *                  subsetId2
     *                  subsetName1
     *                  subsetName2
     *
     *                  dataTypeId
     *                  dataTypeName
     *                  isHighDimensional
     *                  metadataExists
     *
     *                  subset1
     *                  subset2
     * @see dataTab.js
     * @see ExportService.getHighDimMetaData()
     */
    protected def convertHighDimMetaData( highDimensionalData ) {
        // TODO: Support multiple export formats per datatype (e.g. raw data and processed data)
        // See ExportService.getMetaData @ 2e2d53d0cba6f6573bf7636de372b96f25312276 for information
        // on how it was specified previously, as well as on the types of data that were allowed
        // for different datatypes
        highDimensionalData.collect { highDimRow ->
            // Determine the types of files that can be exported for this 
            // datatype
            Set<HighDimExporter> exporters = highDimExporterRegistry.getExportersForDataType( 
                    highDimRow.datatype.dataTypeName );
            
            // Determine the data platforms that are present for a given subset
            def platforms = [
                "subset1": getPlatformsForSubjectSampleMappingList( highDimRow.subset1 ),
                "subset2": getPlatformsForSubjectSampleMappingList( highDimRow.subset2 )
            ]
                
            [
                subsetId1: "subset1",
                subsetId2: "subset2",
                subsetName1: "Subset 1",
                subsetName2: "Subset 2",
                
                dataTypeId: highDimRow.datatype.dataTypeName,
                dataTypeName: highDimRow.datatype.dataTypeDescription,
                isHighDimensional: true,
                metadataExists: true,
                
                subset1: exporters.collect {
                    [
                        fileType: "." + it.format,
                        dataTypeHasCounts: true,
                        dataFormat: it.description,
                        fileDataCount: highDimRow.subset1 ? highDimRow.subset1.size() : 0,
                        platforms: platforms.subset1
                    ]
                },
                subset2: exporters.collect {
                    [
                        fileType: "." + it.format,
                        dataTypeHasCounts: true,
                        dataFormat: it.description,
                        fileDataCount: highDimRow.subset2 ? highDimRow.subset2.size() : 0,
                        platforms: platforms.subset2
                    ]
                }
            ]
        }
    }
    
    /**
     * Returns a list of unique platforms for a given set of subject sample mappings 
     * @param assayList
     * @return  A list of unique platforms, each being a map with the keys
     *              gplId
     *              gplTitle
     *              fileDataCount
     */
    private getPlatformsForSubjectSampleMappingList( Collection<Assay> assayList ) {
        if( !assayList ) 
            return []
            
        return assayList*.platform.unique().collect { platform ->
            [
                gplId: platform.id,
                gplTitle: platform.title,
                fileDataCount: assayList.findAll { assay -> assay.platform.id == platform.id }.size()
            ]
        }
    }    

}
