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
package com.recomdata.i2b2

import com.recomdata.transmart.data.export.util.FileWriterUtil;
import com.sun.rowset.CachedRowSetImpl;

import java.sql.ResultSet
import java.sql.Statement

import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;
import org.springframework.context.ApplicationContext;
import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.commons.ApplicationHolder;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;


/**
 * This class will provide access to the i2b2
 */

public class I2b2DAO
{
	ApplicationContext ctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getAttribute(org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.APPLICATION_CONTEXT)
	def dataSource = ctx.getBean('dataSource')
	def i2b2HelperService = ctx.getBean('i2b2HelperService')
	def springSecurityService = ctx.getBean('springSecurityService')
	
	//This is the SQL query we use to get our data.
	private StringBuilder sqlQuery = new StringBuilder();
	
	//This is the list of parameters passed to the SQL statement.
	ArrayList parameterList = new ArrayList();
	
	def config = ConfigurationHolder.config
	
	boolean dataFound = false
	
	/**
	 * This method will gather data from the i2b2 database and write it to a file. The file will contain PATIENT_NUM,CONCEPT_PATH, The concept name and a subset.
	 * @param fileName Name of the data file.
	 * @param result_instance_ids A hashmap of the form ["subset1":result_instance_id]
	 * @param conceptCodeList An array of strings representing the concept codes to filter on.
	 * @return
	 */
	public void getData(String study, File studyDir, String fileName, String jobName, HashMap result_instance_ids, String[] conceptCodeList, List retrievalTypes, boolean parPivotData) 

	{
		//Log the action of data access.
		//def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"i2b2DAO - getData", eventmessage:"RID:"+result_instance_ids.toString()+" Concept:"+conceptCodeList.toString(), accesstime:new java.util.Date())
		//al.save()
		
		boolean retrievalTypeMRNAExists = retrievalTypeExists('MRNA', retrievalTypes)
		boolean retrievalTypeSNPExists = retrievalTypeExists('SNP', retrievalTypes)

		//Loop through each subset and add SQL code.
		result_instance_ids.each
		{
			resultName ->
			
			if (resultName.value) {
				//If the string isn't empty, we need a Union because we already have a subset in the query.
				if(sqlQuery.toString() != "")
				{
					sqlQuery <<= " UNION "
				}
				
				//Construct the SQL Query.
				sqlQuery <<= "SELECT ofa.PATIENT_NUM, cd.CONCEPT_PATH, cd.CONCEPT_CD, "
				sqlQuery <<= "case ofa.VALTYPE_CD " 
				sqlQuery <<= " WHEN 'T' THEN TVAL_CHAR " 
				sqlQuery <<= " WHEN 'N' THEN CAST(NVAL_NUM AS varchar2(30)) " 
				sqlQuery <<= "END VALUE, ? SUBSET "
				if (retrievalTypeMRNAExists) {
					sqlQuery <<= ", ssm.assay_id, ssm.sample_type, ssm.timepoint, ssm.tissue_type "
				}
				sqlQuery <<= "FROM qt_patient_set_collection qt "
				sqlQuery <<= "INNER JOIN OBSERVATION_FACT ofa ON qt.PATIENT_NUM = ofa.PATIENT_NUM "
				sqlQuery <<= "INNER JOIN CONCEPT_DIMENSION cd ON cd.CONCEPT_CD = ofa.CONCEPT_CD "
				if (retrievalTypeMRNAExists) {
					sqlQuery <<= "INNER JOIN DE_SUBJECT_SAMPLE_MAPPING ssm ON ssm.PATIENT_ID = ofa.PATIENT_NUM  "
					//AND ssm.CONCEPT_CODE = cd.CONCEPT_CD)
				}
				/*if (retrievalTypeSNPExists) {
					sqlQuery <<= "INNER JOIN DE_SUBJECT_SNP_DATASET ssd ON (ssd.PATIENT_NUM = ofa.PATIENT_NUM "
					sqlQuery <<= "AND (ssd.CONCEPT_CD = ssm.CONCEPT_CODE OR ssd.CONCEPT_CD IS NULL)) "
				}*/
				sqlQuery <<= "WHERE qt.RESULT_INSTANCE_ID = ? AND ofa.MODIFIER_CD = ? "
				
				//Add the name of the subset to the parameter list.
				parameterList.add(resultName.key)
				//Add the value of the result instance ID to the parameter list.
				parameterList.add(resultName.value)
				//Add study to the parameter list.
				parameterList.add(study)
				
				//If we have a list of concepts, add them to the query.
				if(conceptCodeList.size() > 0)
				{
					sqlQuery <<= " AND cd.CONCEPT_CD IN (" + quoteCSV(conceptCodeList.join(",")) + ") "
					
					//Add the concept list to the parameter list.
					//parameterList.add(conceptCodeList.join(","))
				}
			}
		}
		
		//sqlQuery <<= "ORDER BY patient_num, assay_id"
		
		//Only pivot the data if the parameter specifies it.

		if(parPivotData)

		{

			pivotData(writeData(studyDir, fileName, jobName, retrievalTypes), retrievalTypeMRNAExists, retrievalTypeSNPExists)

		}

		else

		{

			writeData(studyDir, fileName, jobName, retrievalTypes)

		}

	}
	
	private String writeData(File studyDir, String fileName, String jobName, List retrievalTypes)
	{
		//TODO Get the dataTypeName from the list of DataTypeNames either from DB or from config file
		def dataTypeName = "Clinical";
		//TODO set this to either "Raw_Files/Findings" or NULL for processed_files
		def dataTypeFolder = null;
		//Build the query to get the clinical data.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		def char separator = '\t';
		FileWriterUtil writerUtil = new FileWriterUtil(studyDir, fileName, jobName, dataTypeName, dataTypeFolder, separator);
		
		writerUtil.writeLine(getColumnNames(retrievalTypes) as String[])
		sql.eachRow(sqlQuery.toString(), parameterList, { row -> 
			dataFound = true
			def strPart1 = (row.CONCEPT_PATH != '') ? row.CONCEPT_PATH.substring(row.CONCEPT_PATH.indexOf('\\', 1), row.CONCEPT_PATH.length()) : ''
			def shortenedConceptPath = (strPart1 != '') ? strPart1.substring(strPart1.indexOf('\\', 1), strPart1.length()) : ''
			
			def values = []
			values.add(row.PATIENT_NUM.toString())
			def sampleValue = row.VALUE.toString()
			values.add(row.SUBSET.toString())
			values.add(row.CONCEPT_CD.toString())
			
			if (retrievalTypeExists("MRNA", retrievalTypes)) {
				def sampleType = (row.SAMPLE_TYPE != null) ? row.SAMPLE_TYPE.toString() : ''
				def timepoint = (row.TIMEPOINT  != null) ? row.TIMEPOINT.toString() : ''
				def tissueType = (row.TISSUE_TYPE  != null) ? row.TISSUE_TYPE.toString() : ''
				//Remove Sample information from CONCEPT_PATH
				def conceptPathWithoutSampleInfo = ''
				if (shortenedConceptPath != '' &&  sampleType != '') {
					if (shortenedConceptPath.indexOf(sampleType) > 0) {
						conceptPathWithoutSampleInfo = shortenedConceptPath.substring(0, shortenedConceptPath.indexOf(sampleType))
					} else if (shortenedConceptPath.indexOf(sampleValue) > 0) {
						conceptPathWithoutSampleInfo = shortenedConceptPath.substring(0, shortenedConceptPath.indexOf(sampleValue))
					}
				}
				values.add((conceptPathWithoutSampleInfo != '') ? conceptPathWithoutSampleInfo : shortenedConceptPath)
				
				if (sampleValue == 'E' || sampleValue == 'normal') {
					sampleValue = sampleType + ((timepoint != '') ? '/' + timepoint : '') + ((tissueType != '') ? '/' + tissueType : '')
				}
				values.add(sampleValue)
				values.add(row.CONCEPT_PATH)
				values.add(row.ASSAY_ID.toString())
				//values.add(row.SAMPLE_TYPE.toString())
				//values.add(row.TIMEPOINT.toString())
			} else {
				values.add(shortenedConceptPath)
				values.add(sampleValue)
				values.add(row.CONCEPT_PATH)
			}
			
			writerUtil.writeLine(values as String[])
		})
		def filePath = writerUtil.outputFile.getAbsolutePath()
		writerUtil.finishWriting()
		
		sql.close()
		
		return filePath
	}
	
	private void pivotData(String inputFileLoc, boolean mRNAExists, boolean snpExists) {
		if (inputFileLoc != "") {
			File inputFile = new File(inputFileLoc)
			if (null != inputFile) {
				String rOutputDirectory = inputFile.getParent() 
				RConnection c = new RConnection()
				
				//Set the working directory to be our temporary location.
				String workingDirectoryCommand = "setwd('${rOutputDirectory}')".replace("\\","\\\\")
				//Run the R command to set the working directory to our temp directory.
				REXP x = c.eval(workingDirectoryCommand)
				
				String pluginScriptDirectory = config.com.recomdata.plugins.pluginScriptDirectory
				String compilePivotDataCommand = ''
				if (mRNAExists) {
					compilePivotDataCommand = "source('${pluginScriptDirectory}/PivotData/PivotClinicalDataWithAssays2.R')"
				} else {
					compilePivotDataCommand = "source('${pluginScriptDirectory}/PivotData/PivotClinicalData.R')"
				}
				REXP comp = c.eval(compilePivotDataCommand)
				//Prepare command to call the PivotClinicalData.R script
				String pivotDataCommand = "PivotClinicalData.pivot('$inputFile.name')"
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
	
	def private getColumnNames(List retrievalTypes) {
		def columnNames = []
		columnNames.add("PATIENT ID")
		columnNames.add("SUBSET")
		columnNames.add("CONCEPT CODE")
		columnNames.add("CONCEPT PATH")
		columnNames.add("VALUE")
		columnNames.add("CONCEPT_PATH_FULL")
		if (retrievalTypeExists("MRNA", retrievalTypes)) {
			columnNames.add("ASSAY ID")
			//columnNames.add("SAMPLE TYPE")
			//columnNames.add("TIMEPOINT")
		}
		return columnNames
	}
	
	def public boolean wasDataFound(){
		return dataFound
	}
	
}