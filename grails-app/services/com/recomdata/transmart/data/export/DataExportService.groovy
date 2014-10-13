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

import com.google.common.base.CharMatcher
import com.recomdata.snp.SnpData
import com.recomdata.transmart.data.export.exception.DataNotFoundException
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.apache.commons.lang.StringUtils
import org.springframework.transaction.annotation.Transactional

class DataExportService {

    boolean transactional = true

    def i2b2ExportHelperService
    def grailsApplication
    def clinicalDataService
    def snpDataService
    def geneExpressionDataService
    def ACGHDataService
    def RNASeqDataService
    def highDimExportService
    def highDimensionResourceService
    def additionalDataService
    def vcfDataService
    def dataSource

    @Transactional(readOnly = true)
    def exportData(jobDataMap) {
        def checkboxList = jobDataMap.get('checkboxList')
        if ((checkboxList.getClass().isArray() && checkboxList?.length == 0) ||
                (checkboxList instanceof List && checkboxList?.isEmpty())) {
            throw new Exception("Please select the data to Export.");
        }
        def jobTmpDirectory = jobDataMap.jobTmpDirectory
        def resultInstanceIdMap = jobDataMap.result_instance_ids
        def subsetSelectedFilesMap = jobDataMap.subsetSelectedFilesMap
        def subsetSelectedPlatformsByFiles = jobDataMap.subsetSelectedPlatformsByFiles
        def highDimDataTypes = jobDataMap.highDimDataTypes

        def mergeSubSet = jobDataMap.mergeSubset
        //Hard-coded subsets to count 2
        def subsets = ['subset1', 'subset2']
        def study = null
        def File studyDir = null
        def filesDoneMap = [:]
        def selection = jobDataMap.selection ?
                new JsonSlurper().parseText(jobDataMap.selection)
                : [:]

        if (StringUtils.isEmpty(jobTmpDirectory)) {
            jobTmpDirectory = grailsApplication.config.com.recomdata.transmart.data.export.jobTmpDirectory
            if (StringUtils.isEmpty(jobTmpDirectory)) {
                throw new Exception('Job temp directory needs to be specified')
            }
        }

        subsets.each { subset ->
            def columnFilter = selection[subset]?.clinical?.selector
            def snpFilesMap = [:]
            def selectedFilesList = subsetSelectedFilesMap.get(subset) ?: []

            // Add all High Dimensional data types available, to the list
            selectedFilesList?.addAll((selection[subset]?.keySet() ?: []) - ['clinical'])

            if (null != selectedFilesList && !selectedFilesList.isEmpty()) {
                //Prepare Study dir
                def List studyList = null
                if (null != resultInstanceIdMap[subset] && !resultInstanceIdMap[subset].isEmpty()) {
                    studyList = i2b2ExportHelperService.findStudyAccessions([resultInstanceIdMap[subset]])
                    if (!studyList.isEmpty()) {
                        study = studyList.get(0)
                        studyDir = new File(jobTmpDirectory, subset + (studyList.size() == 1 ? '_' + study : ''))
                        studyDir.mkdir()
                    }
                }

                //Pull the data pivot parameter out of the data map.
                def pivotDataValueDef = jobDataMap.get("pivotData")
                boolean pivotData = new Boolean(true)
                if (pivotDataValueDef == false) pivotData = new Boolean(false)
                boolean writeClinicalData = 'clinical' in selection[subset]
                if (resultInstanceIdMap[subset]) {
                    // Construct a list of the URL objects we're running, submitted to the pool
                    selectedFilesList.each() { selectedFile ->

                        if (StringUtils.equalsIgnoreCase(selectedFile, "CLINICAL.TXT")) {
                            writeClinicalData = true
                        }

                        def List gplIds = subsetSelectedPlatformsByFiles?.get(subset)?.get(selectedFile)
                        def retVal = null
                        switch (selectedFile) {
                            case "STUDY":
                                break;
                        // New high dimensional data
                        // case "MRNA.TXT":
                            case highDimensionResourceService.knownTypes:
                                //retVal = geneExpressionDataService.getData(studyList, studyDir, "mRNA.trans", jobDataMap.get("jobName"), resultInstanceIdMap[subset], pivotData, gplIds, null, null, null, null, false)

                                // boolean splitAttributeColumn
                                // String (of a number) resultInstanceId
                                // List<String> conceptPaths
                                // String dataType
                                // String studyDir
                                log.info "Exporting " + selectedFile + " using core api"

                                // For now we ignore the information about the platforms to 
                                // export. All data that matches the selected concepts
                                // is exported
                                highDimDataTypes[subset][selectedFile].keySet().each { format ->
                                    log.info "  Using format " + format
                                    retVal = highDimExportService.exportHighDimData(jobName: jobDataMap.jobName,
                                            splitAttributeColumn: false,
                                            resultInstanceId: resultInstanceIdMap[subset],
                                            conceptPaths: selection[subset][selectedFile].selector,
                                            dataType: selectedFile,
                                            format: format,
                                            studyDir: studyDir
                                    )
                                }
                                log.info "Exported " + selectedFile + " using core api"

                                //filesDoneMap is used for building the Clinical Data query
                                filesDoneMap.put('MRNA.TXT', new Boolean(true))
                                break;
                            case "MRNA_DETAILED.TXT":
                                //We need to grab some inputs from the jobs data map.
                                def pathway = jobDataMap.get("gexpathway")
                                def timepoint = jobDataMap.get("gextime")
                                def sampleType = jobDataMap.get("gexsample")
                                def tissueType = jobDataMap.get("gextissue")
                                def gplString = jobDataMap.get("gexgpl")

                                if (tissueType == ",") tissueType = ""
                                if (sampleType == ",") sampleType = ""
                                if (timepoint == ",") timepoint = ""

                                if (gplIds != null) {
                                    gplIds = gplString.tokenize(",")
                                } else {
                                    gplIds = []
                                }

                                //adding String to a List to make it compatible to the type expected
                                //if gexgpl contains multiple gpl(s) as single string we need to convert that to a list

                                retVal = geneExpressionDataService.getData(studyList, studyDir, "mRNA.trans", jobDataMap.get("jobName"), resultInstanceIdMap[subset], pivotData, gplIds, pathway, timepoint, sampleType, tissueType, true)
                                if (jobDataMap.get("analysis") != "DataExport") {
                                    //if geneExpressionDataService was not able to find data throw an exception.
                                    if (!retVal) {
                                        throw new DataNotFoundException("There are no patients that meet the criteria selected therefore no gene expression data was returned.")
                                    }
                                }
                                break;
                            case "ACGH_REGIONS.TXT":
                                if (studyList.size() != 1) {
                                    throw new Exception("Only one study " +
                                            "allowed per analysis; list given" +
                                            " was : " + studyList);
                                }
                                this.ACGHDataService.writeRegions(
                                        studyList[0],
                                        studyDir,
                                        'regions.txt',
                                        jobDataMap.get("jobName"),
                                        resultInstanceIdMap[subset]
                                        /* currently the interface does not allow filtering,
                                           so don't implement it here was well
                                         */
                                )
                                break;
                            case "RNASEQ.TXT":
                                if (studyList.size() != 1) {
                                    throw new Exception("Only one study " +
                                            "allowed per analysis; list given" +
                                            " was : " + studyList);
                                }
                                this.RNASeqDataService.writeRegions(
                                        studyList[0],
                                        studyDir,
                                        'RNASeq.txt',
                                        jobDataMap.get("jobName"),
                                        resultInstanceIdMap[subset]
                                        /* currently the interface does not allow filtering,
                                           so don't implement it here was well
                                         */
                                )
                                break;
                            case "MRNA.CEL":
                                geneExpressionDataService.downloadCELFiles(resultInstanceIdMap[subset], studyList, studyDir, jobDataMap.get("jobName"), null, null, null, null)
                                break;
                            case "GSEA.GCT & .CLS":
                                geneExpressionDataService.getGCTAndCLSData(studyList, studyDir, "mRNA.GCT", jobDataMap.get("jobName"), resultInstanceIdMap, pivotData, gplIds)
                                break;
                            case "SNP.PED, .MAP & .CNV":
                                retVal = snpDataService.getData(studyDir, "snp.trans", jobDataMap.get("jobName"), resultInstanceIdMap[subset])
                                snpDataService.getDataByPatientByProbes(studyDir, resultInstanceIdMap[subset], jobDataMap.get("jobName"))
                                break;
                            case "SNP.CEL":
                                snpDataService.downloadCELFiles(studyList, studyDir, resultInstanceIdMap[subset], jobDataMap.get("jobName"))
                                break;
                            case "SNP.TXT":
                                //In this case we need to get a file with Patient ID, Probe ID, Gene, Genotype, Copy Number
                                //We need to grab some inputs from the jobs data map.
                                def pathway = jobDataMap.get("snppathway")
                                def sampleType = jobDataMap.get("snpsample")
                                def timepoint = jobDataMap.get("snptime")
                                def tissueType = jobDataMap.get("snptissue")

                                //This object will be our row processor which handles the writing to the SNP text file.
                                SnpData snpData = new SnpData()
                                //Construct the path that we create the SNP file on.
                                def SNPFolderLocation = jobTmpDirectory + File.separator + "subset1_${study}" + File.separator + "SNP" + File.separator
                                //Make sure the directory we want to write the file to is created.
                                def snpDir = new File(SNPFolderLocation)
                                snpDir.mkdir()
                                //This is the exact path of the file to write.
                                def fileLocation = jobTmpDirectory + File.separator + "subset1_${study}" + File.separator + "SNP" + File.separator + "snp.trans"
                                //Call our service which writes the SNP data to a file.
                                Boolean gotData = snpDataService.getSnpDataByResultInstanceAndGene(resultInstanceIdMap[subset], study, pathway, sampleType, timepoint, tissueType, snpData, fileLocation, true, true)
                                if (jobDataMap.get("analysis") != "DataExport") {
                                    //if SNPDataService was not able to find data throw an exception.
                                    if (!gotData) {
                                        throw new DataNotFoundException("There are no patients that meet the criteria selected therefore no SNP data was returned.")
                                    }
                                }
                                break;
                            case "ADDITIONAL":
                                additionalDataService.downloadFiles(resultInstanceIdMap[subset], studyList, studyDir, jobDataMap.get("jobName"))
                                break;
                            case "IGV.VCF":

                                def selectedGenes = jobDataMap.get("selectedGenes")
                                def chromosomes = jobDataMap.get("chroms")
                                def selectedSNPs = jobDataMap.get("selectedSNPs")

                                log.trace("VCF Parameters")
                                log.trace("selectedGenes:" + selectedGenes)
                                log.trace("chromosomes:" + chromosomes)
                                log.trace("selectedSNPs:" + selectedSNPs)

                                //def IGVFolderLocation = jobTmpDirectory + File.separator + "subset1_${study}" + File.separator + "VCF" + File.separator

                                //
                                //	def outputDir = "/users/jliu/tmp"
                                def outputDir = grailsApplication.config.com.recomdata.analysis.data.file.dir;
                                def webRootName = jobDataMap.get("appRealPath");
                                if (webRootName.endsWith(File.separator) == false)
                                    webRootName += File.separator;
                                outputDir = webRootName + outputDir;
                                def prefix = "S1"
                                if ('subset2' == subset)
                                    prefix = "S2"
                                vcfDataService.getDataAsFile(outputDir, jobDataMap.get("jobName"), null, resultInstanceIdMap[subset], selectedSNPs, selectedGenes, chromosomes, prefix);
                                break;
                        }
                    }
                }

                if (writeClinicalData) {
                    //Grab the item from the data map that tells us whether we need the concept contexts.
                    Boolean includeConceptContext = jobDataMap.get("includeContexts", false);

                    //This is a list of concept codes that we use to filter the result instance id results.
                    String[] conceptCodeList = jobDataMap.get("concept_cds");

                    //This is list of concept codes that are parents to some child concepts. We need to expand these out in the service call.
                    List parentConceptCodeList = new ArrayList()

                    if (jobDataMap.get("parentNodeList", null) != null) {
                        //This variable tells us which variable actually holds the parent concept code.
                        String conceptVariable = jobDataMap.get("parentNodeList")

                        //Get the actual concept value from the map.
                        parentConceptCodeList.add(jobDataMap.get(conceptVariable))
                    } else {
                        parentConceptCodeList = []
                    }

                    //Make this blank instead of null if we don't find it.
                    if (conceptCodeList == null) conceptCodeList = []

                    //Set the flag that tells us whether or not to exclude the high level concepts. Should this logic even be in the DAO?
                    boolean filterHighLevelConcepts = false

                    if (jobDataMap.get("analysis") == "DataExport") filterHighLevelConcepts = true
                    def platformsList = subsetSelectedPlatformsByFiles?.get(subset)?.get("MRNA.TXT")
                    //Reason for moving here: We'll get the map of SNP files from SnpDao to be output into Clinical file
                    def retVal = clinicalDataService.getData(studyList, studyDir, "clinical.i2b2trans", jobDataMap.get("jobName"),
                            resultInstanceIdMap[subset], conceptCodeList, selectedFilesList, pivotData, filterHighLevelConcepts,
                            snpFilesMap, subset, filesDoneMap, platformsList, parentConceptCodeList as String[], includeConceptContext)

                    if (jobDataMap.get("analysis") != "DataExport") {
                        //if i2b2Dao was not able to find data for any of the studies associated with the result instance ids, throw an exception.
                        if (!retVal) {
                            throw new DataNotFoundException("There are no patients that meet the criteria selected therefore no clinical data was returned.")
                        }
                    }

                    if (jobDataMap.analysis == 'DataExport') {

                        def resultInstanceId = resultInstanceIdMap[subset]

                        def mapOfSampleCdsBySource = buildMapOfSampleCdsBySource(resultInstanceId)

                        def sampleCodesTable = new Sql(dataSource).rows("""
                                        SELECT DISTINCT
                                            p.SOURCESYSTEM_CD
                                        FROM
                                            patient_dimension p
                                        WHERE
                                            p.PATIENT_NUM IN (
                                                SELECT
                                                    DISTINCT patient_num
                                                FROM
                                                    qt_patient_set_collection
                                                WHERE
                                                    result_instance_id = ? )
                                    """, resultInstanceId)
                        for (row in sampleCodesTable) {
                            row.SAMPLE_CDS = mapOfSampleCdsBySource[row.SOURCESYSTEM_CD]
                        }
                        sampleCodesTable = sampleCodesTable.collectEntries {
                            [it.SOURCESYSTEM_CD.split(':')[-1].trim(), it.SAMPLE_CDS]
                        }
                        // add the header to the mapping table
                        sampleCodesTable['PATIENT ID'] = 'SAMPLE CODES'
                        // example: columnFilter = [/\Subjects\Ethnicity/, /\Endpoints\Diagnosis/]
                        studyList.each { studyName ->
                            String directory
                            String fileWritten = "clinical_i2b2trans.txt"
                            if (studyList.size() > 1) {
                                // yes, the output of the previous stage has a " _" in the name, with a space in it.
                                fileWritten = studyName + ' _' + fileWritten

                            }
                            directory = clinicalDataFileName(studyDir.path)

                            def reader = new File(directory, fileWritten)
                            def writer = new File(directory, "newclinical")
                            def writerstream = writer.newOutputStream()

                            def filter = null

                            reader.eachLine {
                                def line = Arrays.asList(it.split('\t'))
                                if (filter == null) {
                                    if (columnFilter) {
                                        filter = []
                                        for (String columnName : columnFilter) {
                                            columnName = CharMatcher.is('\\' as char).trimTrailingFrom(columnName)
                                            String parentColumnName = columnName.replaceFirst(/\\[^\\]+$/, '')
                                            def index = line.findIndexOf() {
                                                columnName.endsWith(it) ||
                                                        parentColumnName.endsWith(it)
                                            }
                                            if (index >= 1 && !(index in filter)) filter.add(index)
                                        }
                                    } else {
                                        filter = 1..(line.size() - 1)
                                    }
                                }
                                def patientId = line[0].trim()
                                def joined = ((line[[0]] + [sampleCodesTable[patientId]] + line[filter])
                                        .join('\t') + '\n')
                                writerstream.write(joined.getBytes())
                            }

                            writerstream.close()

                            writer.renameTo(directory + '/' + fileWritten)
                        }
                    }
                }
            }
        }

    }

    def buildMapOfSampleCdsBySource(resultInstanceId) {
        def map = [:]
        def sampleCodesTable = new Sql(dataSource).rows("""
			SELECT DISTINCT
                p.SOURCESYSTEM_CD,
                s.SAMPLE_CD
            FROM
                patient_dimension p
            LEFT JOIN
                observation_fact s on s.patient_num = p.patient_num
            WHERE
                p.PATIENT_NUM IN (
                    SELECT
                        DISTINCT patient_num
                    FROM
                        qt_patient_set_collection
                    WHERE
                        result_instance_id = ? )
			ORDER BY SOURCESYSTEM_CD, SAMPLE_CD
			""", resultInstanceId
        )
        for (row in sampleCodesTable) {
            def sourceSystemCd = row.SOURCESYSTEM_CD
            if (!sourceSystemCd) continue
            def sampleCd = row.SAMPLE_CD
            if (!sampleCd) continue
            def entry = map[sourceSystemCd]
            if (!entry) {
                entry = sampleCd
            } else {
                entry = entry + "," + sampleCd
            }
            map[sourceSystemCd] = entry
        }
        return map
    }

    static clinicalDataFileName(String studyDir) {

        String dataTypeName = 'Clinical'
        String dataTypeFolder = null

        String dataTypeNameDir = (StringUtils.isNotEmpty(dataTypeName) && null != studyDir) ?
                studyDir + '/' + dataTypeName : null;
        String dataTypeFolderDir = (StringUtils.isNotEmpty(dataTypeFolder) && null != dataTypeNameDir) ?
                dataTypeNameDir + '/' + dataTypeFolder : null;

        if (null != studyDir && null == dataTypeNameDir) {
            studyDir
        } else if (null != studyDir && null != dataTypeNameDir) {
            ((null == dataTypeFolderDir) ? dataTypeNameDir : dataTypeFolderDir)
        }


    }

}
