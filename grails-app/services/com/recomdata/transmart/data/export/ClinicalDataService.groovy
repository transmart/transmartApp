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

import java.io.File
import java.util.ArrayList
import java.util.List
import java.util.Map

import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection

import com.recomdata.dataexport.util.ExportUtil
import com.recomdata.transmart.data.export.util.FileWriterUtil

class ClinicalDataService {

    boolean transactional = true

    def dataSource
	def i2b2HelperService
	def springSecurityService
	def utilService
	//Logger log = Logger.getLogger(getClass()) // log4j

	//This is the SQL query we use to get our data.
	//private StringBuilder sqlQuery = new StringBuilder();

	//This is the list of parameters passed to the SQL statement.
	//def parameterList = null;

	def config = ConfigurationHolder.config

	boolean dataFound = false
	
	/**
	 * This method will gather data from the i2b2 database and write it to a file. The file will contain PATIENT_NUM,CONCEPT_PATH, The concept name and a subset.
	 * @param fileName Name of the data file.
	 * @param result_instance_ids A hashmap of the form ["subset1":result_instance_id]
	 * @param conceptCodeList An array of strings representing the concept codes to filter on.
	 * @return
	 */
	public boolean getData(List studyList, File studyDir, String fileName, String jobName, String resultInstanceId, 
		String[] conceptCodeList, List retrievalTypes, boolean parPivotData, boolean parFilterHighLevelConcepts, 
		Map snpFilesMap, String subset, Map filesDoneMap, List platformsList) 	{
		
		def sqlQuery = new StringBuilder();
		def parameterList = null;
		
		boolean retrievalTypeMRNAExists = retrievalTypeExists('MRNA', retrievalTypes)
		boolean retrievalTypeSNPExists = retrievalTypeExists('SNP', retrievalTypes)

		if (null != resultInstanceId) {
			//If the string isn't empty, we need a Union because we already have a subset in the query.
			//Construct the SQL Query.
			sqlQuery <<= "SELECT ofa.PATIENT_NUM, cd.CONCEPT_PATH, cd.CONCEPT_CD, cd.NAME_CHAR, "
			sqlQuery <<= "case ofa.VALTYPE_CD "
			sqlQuery <<= " WHEN 'T' THEN TVAL_CHAR "
			sqlQuery <<= " WHEN 'N' THEN CAST(NVAL_NUM AS varchar2(30)) "
			sqlQuery <<= "END VALUE, ? SUBSET , pd.sourcesystem_cd "
			if (retrievalTypeMRNAExists && null != filesDoneMap['MRNA.TXT'] && filesDoneMap['MRNA.TXT']) {
				sqlQuery <<= ", ssm.assay_id, ssm.sample_type, ssm.timepoint, ssm.tissue_type "
			}
			sqlQuery <<= "FROM qt_patient_set_collection qt "
			sqlQuery <<= "INNER JOIN OBSERVATION_FACT ofa ON qt.PATIENT_NUM = ofa.PATIENT_NUM "
			sqlQuery <<= "INNER JOIN CONCEPT_DIMENSION cd ON cd.CONCEPT_CD = ofa.CONCEPT_CD "
			sqlQuery <<= "INNER JOIN PATIENT_DIMENSION pd on ofa.patient_num = pd.patient_num "
			if (retrievalTypeMRNAExists && null != filesDoneMap['MRNA.TXT'] && filesDoneMap['MRNA.TXT']) {
				sqlQuery <<= "LEFT JOIN DE_SUBJECT_SAMPLE_MAPPING ssm ON ssm.PATIENT_ID = ofa.PATIENT_NUM  "
			}
			sqlQuery <<= "WHERE qt.RESULT_INSTANCE_ID = ? AND ofa.MODIFIER_CD = ?"

			if (!retrievalTypeMRNAExists && parFilterHighLevelConcepts) {
				sqlQuery <<= " AND cd.concept_cd NOT IN (SELECT DISTINCT NVL(sample_type_cd,'-1') as gene_expr_concept"
				sqlQuery <<= " FROM de_subject_sample_mapping WHERE trial_name = ?"
				sqlQuery <<= " UNION SELECT DISTINCT NVL(tissue_type_cd,'-1') as gene_expr_concept "
				sqlQuery <<= " FROM de_subject_sample_mapping WHERE trial_name = ?"
				sqlQuery <<= " UNION SELECT DISTINCT NVL(platform_cd,'-1') as gene_expr_concept "
				sqlQuery <<= " FROM de_subject_sample_mapping WHERE trial_name = ?)"
			}
			
			if (retrievalTypeMRNAExists && null != filesDoneMap && filesDoneMap['MRNA.TXT'] && !platformsList?.isEmpty()) {
				sqlQuery <<= " AND ssm.GPL_ID IN (" << utilService.toListString(platformsList) << ") "
			}
			//If we have a list of concepts, add them to the query.
			if(conceptCodeList.size() > 0) sqlQuery <<= " AND cd.CONCEPT_CD IN (" + quoteCSV(conceptCodeList.join(",")) + ") "
		}
		
		studyList.each { study ->
			parameterList = new ArrayList();
			//Add the name of the subset to the parameter list.
			parameterList.add(subset)
			//Add the value of the result instance ID to the parameter list.
			parameterList.add(resultInstanceId)
			//Add study to the parameter list.
			parameterList.add(study)
			if (!retrievalTypeMRNAExists && parFilterHighLevelConcepts) {
				parameterList.add(study)
				parameterList.add(study)
				parameterList.add(study)
			}
			def filename = (studyList?.size() > 1) ? study+'_'+fileName : fileName
			log.debug("Retrieving Clinical data : " + sqlQuery)
			log.debug("Retrieving Clinical data : " + parameterList)
	
			//Only pivot the data if the parameter specifies it.
			if(parPivotData)
			{
				boolean mRNAExists =  retrievalTypeMRNAExists && null != filesDoneMap['MRNA.TXT'] && filesDoneMap['MRNA.TXT']
				boolean snpExists =  retrievalTypeSNPExists && null != filesDoneMap['SNP.PED, .MAP & .CNV'] && filesDoneMap['SNP.PED, .MAP & .CNV']
				pivotData((studyList?.size() > 1), study, 
					writeData(sqlQuery, parameterList, studyDir, filename, jobName, retrievalTypes, snpFilesMap), 
					mRNAExists, snpExists)
			}
			else
			{
				writeData(sqlQuery, parameterList, studyDir, filename, jobName, retrievalTypes)
			}
		}
		
		return dataFound
	}

	private String writeData(StringBuilder sqlQuery, List parameterList, File studyDir, String fileName, String jobName, List retrievalTypes, Map snpFilesMap = null)
	{
		//TODO Get the dataTypeName from the list of DataTypeNames either from DB or from config file
		def dataTypeName = "Clinical";
		//TODO set this to either "Raw_Files/Findings" or NULL for processed_files
		def dataTypeFolder = null;
		//Build the query to get the clinical data.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		def char separator = '\t';
		def filePath = null
		FileWriterUtil writerUtil = null
		
		try {
			dataFound = false
			
			
			log.debug('Clinical Data Query :: ' + sqlQuery.toString())
			def rows = sql.rows(sqlQuery.toString(), parameterList)
			if (rows.size() > 0) {
				log.debug('Writing Clinical File')
				writerUtil = new FileWriterUtil(studyDir, fileName, jobName, dataTypeName, dataTypeFolder, separator);
				writerUtil.writeLine(getColumnNames(retrievalTypes, snpFilesMap) as String[])
			
				rows.each { row ->
					dataFound = true
					def values = []
					//values.add(row.PATIENT_NUM?.toString())
					values.add(utilService.getActualPatientId(row.SOURCESYSTEM_CD?.toString()))
					values.add(row.SUBSET?.toString())
					values.add(row.CONCEPT_CD?.toString())
		
					//Add Concept Path
					def removalArr = [row.VALUE]
					if (retrievalTypeExists("MRNA", retrievalTypes)) {
						removalArr.add(row.TISSUE_TYPE?.toString())
						removalArr.add(row.TIMEPOINT?.toString())
						removalArr.add(row.SAMPLE_TYPE?.toString())
					}
					if (removalArr.size() == 1 && row.VALUE?.toString().equalsIgnoreCase('E')) {
						removalArr.add(row.NAME_CHAR?.toString())
					}
					values.add(ExportUtil.getShortConceptPath(row.CONCEPT_PATH, removalArr))
		
					if (retrievalTypeExists("MRNA", retrievalTypes)) {
						values.add(ExportUtil.getSampleValue(row.VALUE, row.SAMPLE_TYPE, row.TIMEPOINT, row.TISSUE_TYPE))
					} else {
						if (row.VALUE?.toString().equalsIgnoreCase('E')) {
							values.add(row.NAME_CHAR?.toString())
						} else {
							values.add(row.VALUE?.toString())
						}
					}
		
					//Actual Concept Path is required for Data Association
					values.add(row.CONCEPT_PATH)
					if (retrievalTypeExists("MRNA", retrievalTypes)) {
						values.add(row.ASSAY_ID?.toString())
					}
		
					if (retrievalTypeExists("SNP", retrievalTypes)) {
						def pedFile = snpFilesMap?.get("PEDFiles")?.get(row.PATIENT_NUM?.toString()+'_'+row.CONCEPT_CD?.toString())
						if (null != snpFilesMap?.get("PEDFiles")) {
							if (StringUtils.isNotEmpty(pedFile)) {
								values.add(pedFile?.toString())
							} else {
								values.add("")
							}
						}
						def mapFile = snpFilesMap?.get("MAPFiles")?.get(row.PATIENT_NUM?.toString()+'_'+row.CONCEPT_CD?.toString())
						if (null != snpFilesMap?.get("MAPFiles")) {
							if (StringUtils.isNotEmpty(mapFile)) {
								values.add(mapFile?.toString())
							} else {
								values.add("")
							}
						}
					}
		
					writerUtil.writeLine(values as String[])
				}
			}
			filePath = writerUtil?.outputFile?.getAbsolutePath()
		} catch (Exception e) {
			log.info(e.getMessage())
		} finally {
			writerUtil?.finishWriting()	
			sql?.close()
		}

		return filePath
	}

	private void pivotData(boolean multipleStudies, String study, String inputFileLoc, boolean mRNAExists, boolean snpExists) {
		if (StringUtils.isNotEmpty(inputFileLoc)) {
			File inputFile = new File(inputFileLoc)
			if (null != inputFile) {
				String rOutputDirectory = inputFile.getParent()
				RConnection c = new RConnection()

				//Set the working directory to be our temporary location.
				String workingDirectoryCommand = "setwd('${rOutputDirectory}')".replace("\\","\\\\")
				//Run the R command to set the working directory to our temp directory.
				REXP x = c.eval(workingDirectoryCommand)

				String rScriptDirectory = config.com.recomdata.transmart.data.export.rScriptDirectory
				String compilePivotDataCommand = ''
				if (mRNAExists) {
					compilePivotDataCommand = "source('${rScriptDirectory}/PivotData/PivotClinicalDataWithAssays2.R')"
				} else {
					compilePivotDataCommand = "source('${rScriptDirectory}/PivotData/PivotClinicalData.R')"
				}
				REXP comp = c.eval(compilePivotDataCommand)
				//Prepare command to call the PivotClinicalData.R script
				String pivotDataCommand = "PivotClinicalData.pivot('$inputFile.name', '$snpExists', '$multipleStudies', '$study')"
				//, '"+mRNAExists+"','"+snpExists+"'
				//Run the R command to pivot the data in the clinical.i2b2trans file.
				REXP pivot = c.eval(pivotDataCommand)
			}
		}
	}

	def String quoteCSV(String val)
	{
		String[] inArray;
		StringBuilder s = new StringBuilder();

		if (val != null && val.length() > 0)
		{
			inArray= val.split(",");
			s.append("'" +inArray[0] + "'");

			for (int i=1; i < inArray.length; i++)
			{
				s.append(",'" +inArray[i] + "'");
			}
		}

		return s.toString();
	}

	def boolean retrievalTypeExists(String checkRetrievalType, List retrievalTypes) {
		boolean exists = false
		retrievalTypes.each { retrievalType ->
			String[] dataTypeFileType = StringUtils.split(retrievalType, ".")
			String dataType;
			if (dataTypeFileType.size() == 1) {
				dataType= retrievalType
			}
			String fileType;
			if (dataTypeFileType.size() > 1) {
				dataType = dataTypeFileType[0].trim().replace(" ","")
				fileType = dataTypeFileType[1].trim().replace(" ","")
			}
			if (dataType == checkRetrievalType) {
				exists = true
				return exists
			}
		}

		return exists
	}

	def private getColumnNames(List retrievalTypes, Map snpFilesMap) {
		def columnNames = []
		columnNames.add("PATIENT ID")
		columnNames.add("SUBSET")
		columnNames.add("CONCEPT CODE")
		columnNames.add("CONCEPT PATH")
		columnNames.add("VALUE")
		columnNames.add("CONCEPT_PATH_FULL")
		if (retrievalTypeExists("MRNA", retrievalTypes)) {
			columnNames.add("ASSAY ID")
		}
		if (retrievalTypeExists("SNP", retrievalTypes)) {
			if (null != snpFilesMap?.get('PEDFiles')) columnNames.add("SNP PED File")
			if (null != snpFilesMap?.get('MAPFiles')) columnNames.add("SNP MAP File")
		}
		return columnNames
	}

	def public boolean wasDataFound(){
		return dataFound
	}
}
