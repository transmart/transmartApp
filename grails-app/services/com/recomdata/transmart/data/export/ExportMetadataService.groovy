package com.recomdata.transmart.data.export

import grails.util.Holders
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.export.HighDimExporter

//TODO Remove duplicated code for both subset. Make code more generic for any number of subsets
class ExportMetadataService {

    static transactional = false

    def dataCountService
    def highDimensionResourceService
    def highDimExporterRegistry
    def queriesResourceService

    def Map createJSONFileObject(fileData, patientsNumber) {
        def file = [:]
        if (patientsNumber != null) {
            file['patientsNumber'] = patientsNumber
        }
        file['dataTypeHasCounts'] = true
        file['exporters'] = fileData.collect { [format: it.key, description: it.value] }
        file['ontologyTermKeys'] = ''

        return file
    }

    def getMetaData(Long resultInstanceId1, Long resultInstanceId2) {
        def metadata = convertIntoMetaDataMap(
                getClinicalMetaData(resultInstanceId1, resultInstanceId2),
                getHighDimMetaData(resultInstanceId1, resultInstanceId2)
        )

        metadata.exportMetaData.addAll(
                getLegacyHighDimensionMetaData(resultInstanceId1, resultInstanceId2)
        )

        metadata
    }


    def getClinicalMetaData(Long resultInstanceId1, Long resultInstanceId2) {
        //The result instance id's are stored queries which we can use to get information from the i2b2 schema.
        log.debug('rID1 :: ' + resultInstanceId1 + ' :: rID2 :: ' + resultInstanceId1)

        //Retrieve the counts for each subset.
        [
                subset1: resultInstanceId1 ? dataCountService.getClinicalDataCount(resultInstanceId1) : 0,
                subset2: resultInstanceId2 ? dataCountService.getClinicalDataCount(resultInstanceId2) : 0,
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
        def uniqueDatatypes = (datatypes1.keySet() + datatypes2.keySet())
                .sort { dt1, dt2 -> dt1.dataTypeDescription <=> dt2.dataTypeDescription }

        // Combine the two subsets, into a map based on datatypes
        def hdMetaData = uniqueDatatypes.collect { datatype ->
            def ontologyTermKeys1 = []
            if (resultInstanceId1) {
                def queryResult = queriesResourceService.getQueryResultFromId(resultInstanceId1)
                Set<OntologyTerm> ontologyTerms = datatype.getAllOntologyTermsForDataTypeBy(queryResult)
                ontologyTermKeys1 = ontologyTerms.collect { it.key }
            }

            def ontologyTermKeys2 = []
            if (resultInstanceId2) {
                def queryResult = queriesResourceService.getQueryResultFromId(resultInstanceId2)
                Set<OntologyTerm> ontologyTerms = datatype.getAllOntologyTermsForDataTypeBy(queryResult)
                ontologyTermKeys2 = ontologyTerms.collect { it.key }
            }

            [
                    datatype        : datatype,
                    subset1         : datatypes1[datatype],
                    subset1_hd_terms: ontologyTermKeys1,
                    subset2         : datatypes2[datatype],
                    subset2_hd_terms: ontologyTermKeys2,
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
        def rIDs = [resultInstanceId1, resultInstanceId2].toArray(new Long[0])

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
                if (key == 'SNP') {
                    dataType['subset' + i] = createJSONFileObject([".PED, .MAP & .CNV": "Processed Data", ".CEL": "Raw Data", ".TXT": 'Text'], finalMap["subset${i}"][key])
                }
                if ((null != finalMap["subset${i}"][key] && finalMap["subset${i}"][key] > 0))
                    dataTypeHasCounts = true;

                dataType['subsetId' + i] = "subset" + i
                dataType['subsetName' + i] = "Subset " + i
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
     * @see ExportService.getClinicalMetaData ( )
     * @see ExportService.getHighDimMetaData ( )
     * @return Map with root key "exportMetaData", which in turn contains a list of
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
     *
     *                  subset1
     *                  subset2
     */
    protected Map convertIntoMetaDataMap(clinicalData, highDimensionalData) {
        def clinicalOutput = [
                subsetId1        : "subset1",
                subsetId2        : "subset2",
                subsetName1      : "Subset 1",
                subsetName2      : "Subset 2",

                dataTypeId       : "CLINICAL",
                dataTypeName     : "Clinical & Low Dimensional Biomarker Data",
                isHighDimensional: false,

                subset1          : [
                        exporters     : [[format: 'TSV', description: 'Tab separated file.']],
                        patientsNumber: clinicalData.subset1
                ],
                subset2          : [
                        exporters     : [[format: 'TSV', description: 'Tab separated file.']],
                        patientsNumber: clinicalData.subset2
                ],
        ]

        // Return a map, suited for the frontend to handle
        [
                exportMetaData: [clinicalOutput] + convertHighDimMetaData(highDimensionalData)
        ]
    }

    /**
     * Converts information about high dimensional data into a map
     * that can be handled by the frontend javascript
     * @param highDimensionalData A list with datatypes that can be exported
     * @return A list of datatypes to export. Each item in the list is a map that has keys,
     *              as below:
     *                  subsetId1
     *                  subsetId2
     *                  subsetName1
     *                  subsetName2
     *
     *                  dataTypeId
     *                  dataTypeName
     *                  isHighDimensional
     *
     *                  subset1
     *                  subset2
     * @see dataTab.js
     * @see ExportService.getHighDimMetaData ( )
     */
    protected def convertHighDimMetaData(highDimensionalData) {
        // TODO: Support multiple export formats per datatype (e.g. raw data and processed data)
        // See ExportService.getMetaData @ 2e2d53d0cba6f6573bf7636de372b96f25312276 for information
        // on how it was specified previously, as well as on the types of data that were allowed
        // for different datatypes
        highDimensionalData.collect { highDimRow ->
            // Determine the types of files that can be exported for this 
            // datatype
            def exporters = highDimExporterRegistry
                    .findExporters(dataType: highDimRow.datatype.dataTypeName)
                    .sort { it.format }

            [
                    subsetId1        : "subset1",
                    subsetId2        : "subset2",
                    subsetName1      : "Subset 1",
                    subsetName2      : "Subset 2",

                    dataTypeId       : highDimRow.datatype.dataTypeName,
                    dataTypeName     : highDimRow.datatype.dataTypeDescription,
                    isHighDimensional: true,
                    subset1          : [
                            dataTypeHasCounts: true,
                            exporters        : exporters.collect { [format: it.format, description: it.description] },
                            patientsNumber   : highDimRow.subset1 ?
                                    (highDimRow.subset1*.patientInTrialId).unique().size() : 0,
                            ontologyTermKeys : highDimRow.subset1_hd_terms
                    ],

                    subset2          : [
                            dataTypeHasCounts: true,
                            exporters        : exporters.collect { [format: it.format, description: it.description] },
                            patientsNumber   : highDimRow.subset2 ?
                                    (highDimRow.subset2*.patientInTrialId).unique().size() : 0,
                            ontologyTermKeys : highDimRow.subset2_hd_terms
                    ]
            ]
        }
    }

}
