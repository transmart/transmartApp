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
import java.sql.ResultSetMetaData
import java.util.List;
import java.util.Map

import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.math.NumberUtils;
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection

import org.transmart.searchapp.SearchKeyword

import com.recomdata.transmart.data.export.util.FileWriterUtil

class GeneExpressionDataService {
		
	private String valueDelimiter ="\t";
	private int flushInterval = 5000;
    boolean transactional = true

	def dataSource
	def i2b2HelperService
	def springSecurityService
	def grailsApplication
	def fileDownloadService
	def utilService
	
	def config = ConfigurationHolder.config

	public boolean getData(List studyList, 
							File studyDir, 
							String fileName, 
							String jobName, 
							String resultInstanceId,
							boolean pivot, 
							List gplIds,
							String pathway, 
							String timepoint, 
							String sampleTypes,
							String tissueTypes,
							Boolean splitAttributeColumn)
	{
		
		//This tells us whether we need to include the pathway information or not.
		Boolean includePathwayInfo = false
		
		//This tells us whether we found data when we call the "Write Data" method.
		boolean dataFound = false
		
		try 
		{
			//Grab the pathway name based on the pathway id.
			pathway = derivePathwayName(pathway)
			
			//Set a flag based on the presence of the pathway.			
			if (pathway != null && pathway.length() > 0) includePathwayInfo = true
			
			studyList.each { study ->
				def sqlQuery, sampleQuery = null;
				
				//Create a query for the Subset.
				if (null != resultInstanceId)
				{
					//Get the concepts for this result instance id.
					def concepts = i2b2HelperService.getConcepts(resultInstanceId)
		
					//Add the subquery to the main query.
					 sqlQuery = createMRNAHeatmapPathwayQuery(study, resultInstanceId, gplIds, pathway, timepoint, sampleTypes, tissueTypes)
					 sampleQuery = createStudySampleAssayQuery(study,resultInstanceId, gplIds, timepoint, sampleTypes, tissueTypes )
					
				}
				def filename = (studyList?.size() > 1) ? study+'_'+fileName : fileName
				//The writeData method will return a map that tells us if data was found, and the name of the file that was written.			
				def writeDataStatusMap = writeData(resultInstanceId, sqlQuery, sampleQuery, studyDir, filename, jobName, includePathwayInfo, splitAttributeColumn, gplIds)
				
				def outFile = writeDataStatusMap["outFile"]
				dataFound = writeDataStatusMap["dataFound"]
				if (null != outFile && dataFound && pivot) {
					pivotData((studyList?.size() > 1), study, outFile)
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}

		return dataFound
	}
	
	def String createStudySampleAssayQuery(String study, String resultInstanceId,List gplIds, String timepoint, String sampleTypes, String tissueTypes){
		StringBuilder sQuery = new StringBuilder();
		sQuery.append("""SELECT DISTINCT
			ssm.assay_id,
			ssm.sample_type,
			ssm.timepoint,
			ssm.tissue_type,
			ssm.sample_cd,
			ssm.trial_name,
			ssm.GPL_ID
		FROM
		de_subject_sample_mapping ssm
		INNER JOIN qt_patient_set_collection sc ON sc.result_instance_id = ? AND ssm.patient_id = sc.patient_num

		""");
		sQuery.append(" WHERE ssm.trial_name = '").append(study).append("' ")
		//If we have a sample type, append it to the query.
		if(sampleTypes!=null && sampleTypes.length()>0)
		{
			sQuery.append(" AND ssm.sample_type_cd IN ").append(convertStringToken(sampleTypes));
		}

		//If we have timepoints, append it to the query.
		if(timepoint!=null && timepoint.trim().length()>0){
			sQuery.append(" AND ssm.timepoint_cd IN ").append(convertStringToken(timepoint));
		}

		//If we have tissues, append it to the query.
		if(tissueTypes!=null && tissueTypes.trim().length()>0){
			sQuery.append(" AND ssm.tissue_type_cd IN ").append(convertStringToken(tissueTypes));
		}
		
		//If we have gplid, append it to the query.
		if(!gplIds?.isEmpty()){
			sQuery.append(" AND ssm.GPL_ID IN (").append(utilService.toListString(gplIds)).append(")");
		}
		
		return sQuery.toString()
	}


	/**
	* This creates a query to gather sample information for a given list of subject ids.
	* @param subsetName The subset name is hardcoded as a column into the result set so we know which subset the sample should be a part of.
	* @param ids This is a CSV list of subject IDs that we are retrieving data for.
	* @param pathwayName This is optionally the name of a pathway that we use to filter which genes we gather data for.
	* @param timepoint This is optionally a list of timepoints to gather data for.
	* @param sampleTypes When de_subject_sample_mapping is queried, we use the sample type to determine which assay ID's to retrieve data for.
	* @return
	* @throws Exception
	*/
   def String createMRNAHeatmapPathwayQuery(String study, String resultInstanceId, List gplIds, String pathwayName, String timepoint, String sampleTypes,String tissueTypes) throws Exception
   {

	   //This is the base SQL Statement for getting the mRNA data.
	   StringBuilder sSelect = new StringBuilder()
	   StringBuilder sTables = new StringBuilder()

	   //create select table - we don't get sample mapping in this query - minimize network traffic from db...
			   
		   sSelect.append("""SELECT
			   a.PATIENT_ID,
			   a.RAW_INTENSITY,
			   a.ZSCORE,
			   a.LOG_INTENSITY,
			   a.assay_id,
			   b.probe_id,
			   b.probeset_id, 
		   	   b.GENE_SYMBOL, 
		   	   b.GENE_ID,
		   	   pd.sourcesystem_cd,
		   	   ssm.gpl_id
		   """);
	   
	   sTables.append("""
	   FROM de_subject_microarray_data a
			   INNER JOIN de_subject_sample_mapping ssm ON ssm.assay_id = A.assay_id 
			   INNER JOIN de_mrna_annotation b ON a.probeset_id = b.probeset_id and ssm.gpl_id = b.gpl_id
			   INNER JOIN qt_patient_set_collection sc ON sc.result_instance_id = ? AND ssm.PATIENT_ID = sc.patient_num
	   		   INNER JOIN PATIENT_DIMENSION pd on ssm.patient_id = pd.patient_num
	   """)
	   
	   //If a list of genes was entered, look up the gene ids and add them to the query. If a gene signature or list was supplied then we modify the query to join on the tables that link the list to the gene ids.
	   String genes;
	   if (pathwayName != null && pathwayName.length() > 0 && !(pathwayName.startsWith("GENESIG") || pathwayName?.startsWith("GENELIST")))
	   {
		   // insert distinct
		   sSelect.insert(6, " DISTINCT ");

		   String keywordTokens = convertStringToken(pathwayName);
		   
		   sSelect.append(", sk.SEARCH_KEYWORD_ID ")
		   
		   //Include the tables we join on to get the unique_id.
		   sTables.append("""
			   INNER JOIN bio_marker bm ON bm.PRIMARY_EXTERNAL_ID = to_char(b.GENE_ID)
			   INNER JOIN bio_marker_correl_mv sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id
			   INNER JOIN search_keyword sk ON sk.bio_data_id = sbm.bio_marker_id
		   """)
		   
		   sTables.append(" WHERE SSM.trial_name = '").append(study).append("' ")
		   sTables.append(" AND sk.unique_id IN ").append(keywordTokens).append (" ");

	   }
	   else if(pathwayName?.startsWith("GENESIG") || pathwayName?.startsWith("GENELIST"))
	   {
		   //If we are querying by a pathway, we need to include that id in the final output.
		   sSelect.append(", sk.SEARCH_KEYWORD_ID ")
		   
		   //Include the tables we join on to filter by the pathway.
		   sTables.append("""
		   INNER JOIN bio_marker bm ON bm.PRIMARY_EXTERNAL_ID = to_char(b.GENE_ID)
		   INNER JOIN SEARCHAPP.SEARCH_BIO_MKR_CORREL_VIEW sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id
		   INNER JOIN search_keyword sk ON sk.bio_data_id = sbm.domain_object_id
		   """)

		   //Include the normal filter.
		   sTables.append(" WHERE SSM.trial_name = '").append(study).append("' ")
		   sTables.append(" AND sk.unique_id IN ").append(convertStringToken(pathwayName)).append(" ");
	   }
	   else
	   {
		   sTables.append(" WHERE SSM.trial_name = '").append(study).append("' ")
	   }

	   //If we have a sample type, append it to the query.
	   if(sampleTypes!=null && sampleTypes.length()>0)
	   {
		   sTables.append(" AND ssm.sample_type_cd IN ").append(convertStringToken(sampleTypes));
	   }

	   //If we have timepoints, append it to the query.
	   if(timepoint!=null && timepoint.trim().length()>0){
		   sTables.append(" AND ssm.timepoint_cd IN ").append(convertStringToken(timepoint));
	   }

	   //If we have tissues, append it to the query.
	   if(tissueTypes!=null && tissueTypes.trim().length()>0){
		   sTables.append(" AND ssm.tissue_type_cd IN ").append(convertStringToken(tissueTypes));
	   }
	   
	   //If we have gplid, append it to the query.
	   if(!gplIds?.isEmpty()){
		   sTables.append(" AND ssm.GPL_ID IN (").append(utilService.toListString(gplIds)).append(")");
	   }
	   
	   sTables.append(" ORDER BY probe_id, patient_id, gpl_id")
	   
	   sSelect.append(sTables.toString())
	   
	   return sSelect.toString();
   }
	
	def String createDownloadCELFilesQuery(String resultInstanceId, studyList, String subjectIds, String timepoint, String sampleTypes, String tissueTypes) throws Exception
	{
		StringBuilder s = new StringBuilder()
		
		//Get the list of assay Ids based on patient ids, sample types, and timepoints.
		String assayIds = getAssayIds(resultInstanceId, sampleTypes, timepoint, tissueTypes);
		String studies=convertList(studyList, false, 1000)
		//If we didn't find any assay Id's, abandon all hope.
		if (StringUtils.isNotEmpty(assayIds)) {
			//Build the string to get the sample data.
			s.append("""
						SELECT DISTINCT a.PATIENT_ID, a.sample_type, a.timepoint, a.tissue_type, a.sample_cd, a.trial_name, pd.sourcesystem_cd
						FROM de_subject_sample_mapping a 
						INNER JOIN PATIENT_DIMENSION pd on a.patient_id = pd.patient_num
					""");
			
			s.append(" WHERE a.trial_name in (").append(studies).append(") ")
			.append(" AND a.assay_id IN (").append(assayIds).append(")")
			.append("AND a.platform like 'MRNA%'")
			s.append(" ORDER BY patient_id")
		}
		
		return s.toString();
	}

	/**
	 * This method retrieves the Assay Ids for the given paramters from the de_subject_sample_mapping table.
	 * @param resultInstanceId Result Instance Id.
	 * @param sampleTypes Sample type
	 * @param timepoint List of timepoints.
	 * @return
	 */
	def String getAssayIds(String resultInstanceId, String sampleTypes, String timepoint, String tissueTypes) {

		//Sql command used to retrieve Assay IDs.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

		//SQL Query string.
		StringBuilder assayS = new StringBuilder()
		
		assayS.append("""	SELECT DISTINCT s.assay_id 
							FROM 	de_subject_sample_mapping s,
									qt_patient_set_collection qt 
							WHERE qt.patient_num = s.patient_id AND qt.result_instance_id = ? """);


		//If we have a sample type, append it to the query.
		if(sampleTypes!=null && sampleTypes.length()>0)
		{
			assayS.append(" AND s.sample_type_cd IN ").append(convertStringToken(sampleTypes));
		}

		//If we have timepoints, append it to the query.
		if(timepoint!=null && timepoint.trim().length()>0){
			assayS.append(" AND s.timepoint_cd IN ").append(convertStringToken(timepoint));
		}

		//If we have tissues, append it to the query.
		if(tissueTypes!=null && tissueTypes.trim().length()>0){
			assayS.append(" AND s.tissue_type_cd IN ").append(convertStringToken(tissueTypes));
		}

		
		//Always add an order by to the query.
		assayS.append (" ORDER BY s.assay_id");

		log.debug("getAssayIds used this query: " + assayS.toString());
		println("getAssayIds used this query: " + assayS.toString());

		//Add each result to an array.
		def assayIdsArray =[];

		sql.eachRow(assayS.toString(), [resultInstanceId], {row->
			if(row.assay_id!=null)
			{
				assayIdsArray.add(row.assay_id)
			}
		});
	
		

		//TODO: Why is there a max here?
		//Make a string of the assay IDs.
		String assayIds = convertList(assayIdsArray, false, 1000);
		return assayIds;
	}


	/**
	 * TODO change the param here, do not pass subjectIds instead pass the resultInstanceId and join the query as in getAssayIds
	 * Appending the patient-ids fails if the count is more than 1000 
	 * Get a list of the trials (studies) that this ID is part of. This is used to filter later on.
	 * @param ids
	 * @return
	 */
	def String getTrialName(String ids) {

		//SQL Object used to retrieve trials.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

		//Query used to get trials.
		StringBuilder trialQ = new StringBuilder("select distinct s.trial_name from de_subject_sample_mapping s ");

		//Append the patient ids.
		trialQ.append(" where s.patient_id in (").append(ids).append(") and s.platform = 'MRNA_AFFYMETRIX'");

		//log.debug("getTrialName used this query: " + trialQ.toString());

		//Add the trial names to a string.
		String trialNames = "";
		sql.eachRow(trialQ.toString(), {row ->

			if(trialNames.length()>0)
			{
				trialNames+=",";
			}

			String tName = row.trial_name;
			trialNames +="'"+tName+"'";
		}
		);
		return trialNames;
	}

	/**
	 *  Compose a list of columns used by Heatmap and then trim average value
	 *
	 * @param biomarker    probeset (mRNA), component (Protein) and antigen_name (RBM)
	 * @param ids1
	 * @param ids2
	 * @param prefix1   	usually use "S1_"
	 * @param prefix2		usually use "S2_"
	 * @return
	 */
	def String listHeatmapColumns(String biomarker, String ids1, String ids2, String prefix1, String prefix2){

		StringBuilder s = new StringBuilder();
		s.append(" " + biomarker + ", gene_symbol ")

		if((ids1!= null) && (ids1.length()>0)){
			def idArray = ids1.split(",")
			for(id in idArray) s.append (", round(" + prefix1 + id + ", 4) as " + prefix1 + id)
		}

		if((ids2 != null) &&(ids2.length()>0)){
			def idArray = ids2.split(",")
			for(id in idArray) s.append (", round(" + prefix2 + id + ", 4) as " + prefix2 + id)
		}

		return s
	}

	/**
	 *
	 */
	def convertStringToken(String t) {
		String[] ts = t.split(",");
		StringBuilder s = new StringBuilder("(");
		for(int i=0; i<ts.length;i++){
			//Make sure we have a non blank token before adding to list.
			if(ts[i])
			{
				if(i>0)
					s.append(",");
				s.append("'");
				s.append(ts[i]);
				s.append("'");
			}
		}
		s.append(")");
		return s.toString();
	}

	/**
	 * convert id list
	 */
	def convertList(idList, boolean isString, int max) {
		StringBuilder s = new StringBuilder();
		int i = 0;
		for(id in idList){
			if(i<max){
				if(s.length()>0){
					s.append(",");
				}
				if(isString){
					s.append("'");
				}
				s.append(id);
				if(isString){
					s.append("'");
				}
			}else{
				break;
			}
			i++;
		}
		return s.toString();
	}

	def writeData(String resultInstanceId, String sqlQuery, String sampleQuery, File studyDir, String fileName, String jobName,includePathwayInfo,splitAttributeColumn, gplIds)
	{
		def filePath = null
		def dataTypeName = "mRNA";
		def dataTypeFolder = "Processed_Data";
		Boolean dataFound = false
		
		//Create objects we use to form JDBC connection.
		def con, stmt, stmt1, rs = null;
		
		//Grab the connection from the grails object.
		con = dataSource.getConnection()
		
		//Grab the configuration that sets the fetch size.
		def rsize = config.com.recomdata.plugins.resultSize;
		Integer fetchSize = 5000;
		if(rsize!=null){
			try{
			fetchSize = Integer.parseInt(rsize);
			}catch(Exception exs){
				log.warn("com.recomdata.plugins.resultSize is not set!");
			
			}
		}
		
		//Prepare the SQL statement.
		stmt = con.prepareStatement(sqlQuery);
		stmt.setString(1, resultInstanceId);
		stmt.setFetchSize(fetchSize)
		
		// sample query
		stmt1 = con.prepareStatement(sampleQuery);
		stmt1.setString(1, resultInstanceId);
		stmt1.setFetchSize(fetchSize);
		
		def char separator = '\t';
		log.info("started file writing")
		def output;
		def outFile;
		
		FileWriterUtil writerUtil = new FileWriterUtil(studyDir, fileName, jobName, dataTypeName, dataTypeFolder, separator);
		outFile = writerUtil.outputFile
		output = outFile.newWriter(true)
		
		output << "PATIENT ID\t"
		
		if(splitAttributeColumn)
			output << "SAMPLE TYPE\tTIMEPOINT\tTISSUE TYPE\tGPL ID\tASSAY ID\tVALUE\tZSCORE\tLOG2ED\tPROBE ID\tPROBESET ID\tGENE_ID\tGENE_SYMBOL"
		else
			output << "SAMPLE\tASSAY ID\tVALUE\tZSCORE\tLOG2ED\tPROBE ID\tPROBESET ID\tGENE_ID\tGENE_SYMBOL"
		
		if(includePathwayInfo)
			output << "\tSEARCH_ID\n"
		else
			output << "\n"
			
		def  sampleType, timepoint,tissueType, rawIntensityRS, zScoreRS, patientID, sourceSystemCode, assayID,GPL_ID, logIntensityRS, probeID, probesetID, gplID = null
		def sample, value, zscore,  lineToWrite = null
		Double rawIntensity = null;
		String geneID = null;
		String geneSymbolId = null;
		String searchKeywordId = null;
		String log2 = null;
		def sttMap = [:]
		
		long elapsetime = System.currentTimeMillis();
		// performance optimization - reduce latency by eliminating sample type data from mRNA result
		//we retrieve the results in 2 queries
		// first one gets the sample, patient and timepoint from subject sample mapping table
		// create the sample type\t timepoint\tTissue type string and put it in a map with assay id as key
		
		// second query goes to the mrna table and gets the intensity data
		// use the assay id to look up the sample\tm\tissue type from the map created in the first query
		// and writes to the writer
		
		log.info("start sample retrieving query");
		log.debug("Sample Query : " + sampleQuery);
		rs = stmt1.executeQuery();
		def sttSampleStr = null;
		
		try{
			while(rs.next()){
				
				sampleType = rs.getString("SAMPLE_TYPE");
				timepoint = rs.getString("TIMEPOINT");
				tissueType = rs.getString("TISSUE_TYPE");		
				assayID = rs.getString("ASSAY_ID");
				GPL_ID = rs.getString("GPL_ID");
				
				if(splitAttributeColumn)
				{
					sttSampleStr = (new StringBuilder()).append(StringUtils.isNotEmpty(sampleType) ? sampleType : '').append(valueDelimiter)
					.append(StringUtils.isNotEmpty(timepoint) ? timepoint : '').append(valueDelimiter)
					.append(StringUtils.isNotEmpty(tissueType) ? tissueType : '').append(valueDelimiter)
					.append(StringUtils.isNotEmpty(GPL_ID) ? GPL_ID : '')
					
				}else{
				sttSampleStr = (new StringBuilder()).append(StringUtils.isNotEmpty(sampleType) ? sampleType : '')
					.append(StringUtils.isNotEmpty(timepoint) ? (new StringBuilder('_')).append(timepoint).toString() : '')
					.append(StringUtils.isNotEmpty(tissueType) ? (new StringBuilder('_')).append(tissueType).toString() : '')
					.append(StringUtils.isNotEmpty(GPL_ID) ? (new StringBuilder('_')).append(GPL_ID).toString() : '')
	
				}
				sttMap.put(assayID, sttSampleStr.toString());
			}
		}finally{
			rs?.close();
			stmt1?.close();
		}
		log.info("finished sample retrieving query");		
		
		//Run the query.
		log.debug("begin data retrieving query: "+sqlQuery)
		rs = stmt.executeQuery();
		log.info("query completed")
		// get column name map
		ResultSetMetaData metaData = rs.getMetaData();
		def nameIndexMap = [:]
		int count = metaData.getColumnCount();
		for (int i = 1; i <= count; i++) {
			nameIndexMap.put(metaData.getColumnName(i), i);
		}
		
		def rawIntensityRSIdx = nameIndexMap.get("RAW_INTENSITY");
		def zScoreRSIdx = nameIndexMap.get("ZSCORE");
		def ptIDIdx = nameIndexMap.get("PATIENT_ID");
		def sourceSystemCodeIdx = nameIndexMap.get("SOURCESYSTEM_CD")
		def assayIDIdx = nameIndexMap.get("ASSAY_ID");
		def probeIDIdx = nameIndexMap.get("PROBE_ID");
		def probesetIDIdx = nameIndexMap.get("PROBESET_ID");
		def logIntensityRSIdx = nameIndexMap.get("LOG_INTENSITY");
		def geneIDIdx = nameIndexMap.get("GENE_ID");
		def geneSymbolIdx = nameIndexMap.get("GENE_SYMBOL");
		def searchKeywordIdIdx = nameIndexMap.get("SEARCH_KEYWORD_ID");
		int flushCount = 0;
		long recCount = 0;

		//A workaround for using only GPL96 values. I don't like the way we have to hard-code GPL96 here. 		
		def platformToUse = 'GPL96'
		def patientProbePlatformValueMap = [:]
		def gplIDIdx = nameIndexMap.get("GPL_ID");
		
		try {
			//Iterate over the record set object.
			while (rs.next())
			{
				//Pull the values we need from the record set object.			
				 rawIntensityRS = rs.getString(rawIntensityRSIdx);
				 zScoreRS = rs.getString(zScoreRSIdx);
				 patientID = rs.getString(ptIDIdx);
				 sourceSystemCode = rs.getString(sourceSystemCodeIdx);
				 assayID = rs.getString(assayIDIdx);
				 probeID = rs.getString(probeIDIdx);
				 probesetID = rs.getString(probesetIDIdx);
				 logIntensityRS = rs.getString(logIntensityRSIdx);	
				 geneID = rs.getString(geneIDIdx);
				 geneSymbolId = rs.getString(geneSymbolIdx);
				 
				dataFound = true
				
				//To use only GPL96 when same probe present in both platforms
				if (gplIds.size() > 1) { // when there are more than one platforms
					gplID = rs.getString(gplIDIdx)
					if (gplID.equals(platformToUse)) { // compared with the hard-coded value GPL96
						patientProbePlatformValueMap.put(patientID+'_'+probeID+'_'+gplID, logIntensityRS)
					} else {
						def probeExistsInGPL96 = patientProbePlatformValueMap.containsKey(patientID+'_'+probeID+'_'+platformToUse)
						if (probeExistsInGPL96) continue;// don't write the record for probe that already exists in GPL96
					}
				}
				
				// patient id
				//writeNotEmptyString(output, patientID);
				writeNotEmptyString(output, utilService.getActualPatientId(sourceSystemCode))
				output.write(valueDelimiter);
				// sample attribute, time point, tissue type
				def sampleAttribute=determineSampleAttribute(sttMap.get(assayID))
				output.write(sampleAttribute);
				output.write(valueDelimiter);
				// assay id				
				writeNotEmptyString(output, assayID);
				output.write(valueDelimiter);
				
				/*
				 * If the data is Global Normalized log_intensity is the value to output.
				 * If the log_intensity is NULL, data-load process has to be corrected to always have a value.
				 */
				if(StringUtils.isNotEmpty(logIntensityRS)){
					output.write(logIntensityRS);
					log2 = "1";
				} /*
				//Don't do the below for Global Normalized data as log_intensity must be present
				else if(StringUtils.isNotEmpty(rawIntensityRS)){ // calculate log 2
					rawIntensity =  Double.valueOf(rawIntensityRS);
					output.write((Math.log(rawIntensity)/Math.log(2)).toString());
					log2 ="1";
				} else if(StringUtils.isNotEmpty(zScoreRS)){ // use zscore
					output.write(zScoreRS);
					log2 ="0";
				}*/
				
				output.write(valueDelimiter);
				writeNotEmptyString(output, zScoreRS);
				output.write(valueDelimiter);
				output.write(log2);
				output.write(valueDelimiter);
				writeNotEmptyString(output, probeID);
				output.write(valueDelimiter);
				writeNotEmptyString(output, probesetID);
				output.write(valueDelimiter);
				writeNotEmptyString(output, geneID);
				output.write(valueDelimiter);
				writeNotEmptyString(output, geneSymbolId);
				
				if(includePathwayInfo) {
					 searchKeywordId = rs.getString(searchKeywordIdIdx);
					 output.write(valueDelimiter);
					 writeNotEmptyString(output, searchKeywordId);
				}
				output.newLine();
				
				flushCount++;
				if(flushCount>=flushInterval) {
					output.flush();
					recCount +=flushCount;
					flushCount = 0;
					//log.info("# record processed:"+recCount);
				}
			}
			if (!dataFound) {
				boolean delFile = outFile?.delete()
				writeNotEmptyString(output, "No data found to add to file.");
				/*log.debug("File deleted :: " + delFile)
				if (!delFile) writeNotEmptyString(output, "Unable to delete this file.");
				filePath = null*/
			}
		} catch(Exception e) {
			log.error(e.getMessage(),e)
		} finally {
			output?.flush();
			output?.close()
			filePath = outFile?.getAbsolutePath()
			if (!dataFound) {
				boolean delFile = outFile?.delete()
				filePath = outFile?.getAbsolutePath()
			}
			log.info("completed file writing")
			stmt?.close();
			con?.close();
		}
		
		// calculate elapse tim
		elapsetime = System.currentTimeMillis()-elapsetime;
		log.info("\n \t total seconds:"+(elapsetime/1000)+"\n\n");
		
		//We need to return a map with two key/values.
		def mapReturnValues = [:]
		
		mapReturnValues["outFile"] = filePath
		mapReturnValues["dataFound"] = dataFound
		
		return mapReturnValues
	}
	
	def determineSampleAttribute(sample){
		def controlSampleList = ['Unknown_GPL96', 'Unknown_GPL97']
		if(controlSampleList.contains(sample)){
			sample="Unknown_GPL96_GPL97"
		}
		return sample
	}
	
	private void pivotData(boolean multipleStudies, String study, String inputFileLoc) {
		//TODO pass the boolean param for deletion of the mRNA.trans file 
		log.info('Pivot File started')
		if (inputFileLoc != "") {
			File inputFile = new File(inputFileLoc)
			if (inputFile) {
				String rOutputDirectory = inputFile.getParent()
				RConnection c = new RConnection()
				
				//Set the working directory to be our temporary location.
				String workingDirectoryCommand = "setwd('${rOutputDirectory}')".replace("\\","\\\\")
				
				log.debug("Attempting following R Command : " + "setwd('${rOutputDirectory}')".replace("\\","\\\\"))
				println("Attempting following R Command : " + "setwd('${rOutputDirectory}')".replace("\\","\\\\"))
				
				//Run the R command to set the working directory to our temp directory.
				REXP x = c.eval(workingDirectoryCommand)
				
				String rScriptDirectory = config.com.recomdata.transmart.data.export.rScriptDirectory
				String compilePivotDataCommand = "source('${rScriptDirectory}/PivotData/PivotGeneExprData.R')".replace("\\","\\\\")
				
				log.debug("Attempting following R Command : " + compilePivotDataCommand.replace("\\","\\\\"))
				println("Attempting following R Command : " + compilePivotDataCommand.replace("\\","\\\\"))
				
				REXP comp = c.eval(compilePivotDataCommand)
				
				//Prepare command to call the PivotGeneExprData.R script
				String pivotDataCommand = "PivotGeneExprData.pivot('$inputFile.name', '$multipleStudies', '$study')".replace("\\","\\\\")
				
				log.debug("Attempting following R Command : " + "PivotGeneExprData.pivot('$inputFile.name', '$multipleStudies', '$study')".replace("\\","\\\\"))
				println("Attempting following R Command : " + "PivotGeneExprData.pivot('$inputFile.name', '$multipleStudies', '$study')".replace("\\","\\\\"))
				
				REXP pivot = c.eval(pivotDataCommand)
			}
		}
	}

	private String derivePathwayName( pathway_name)	
	{
		if (pathway_name == null || pathway_name.length() == 0 || pathway_name == "null" ) 
		{
			pathway_name = null
		}
		
		boolean nativeSearch = grailsApplication.config.com.recomdata.search.genepathway=='native'
		
		if(!nativeSearch && pathway_name != null)	
		{
			//If we have multiple genes they will be comma separated. We need to split the string and find the unique ID for each.
			def pathwayGeneList = pathway_name.split(",")
			
			//For each gene, get the long ID.
			pathway_name = pathwayGeneList.collect{ SearchKeyword.get(Long.valueOf(it)).uniqueId }.join(",")
		}
	
		log.debug("pathway_name has been set to a keyword ID: ${pathway_name}")
		return pathway_name
	}

	private List getCELFiles(String studyName, String sampleCd) {
		//Build the query to get the clinical data.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		sqlQuery = "SELECT * FROM bio_content WHERE study_name = ? and file_name like ?"
		def files = sql.rows(sqlQuery, [studyName, sampleCd+'%'])
		
		return files
	}
	
	def downloadCELFiles(String resultInstanceId, studyList, File studyDir, String jobName, String pathway, String timepoint, String sampleTypes, String tissueTypes) {
		groovy.sql.Sql sql = null

		Map sampleCdsMap = null
		
		try {
			//Get the subjects for this result instance id.
			def subjectIds = i2b2HelperService.getSubjects(resultInstanceId)

			//Get the concepts for this result instance id.
			def concepts = i2b2HelperService.getConcepts(resultInstanceId)

			//Add the subquery to the main query.
			def sqlQuery = createDownloadCELFilesQuery(resultInstanceId, studyList, subjectIds, timepoint, sampleTypes, tissueTypes)
			sql = new groovy.sql.Sql(dataSource)
			def sample, mapKey, mapValue = null
			sql.eachRow(sqlQuery, { row ->
				
				sample = (new StringBuilder()).append(StringUtils.isNotEmpty(row.SAMPLE_TYPE) ? row.SAMPLE_TYPE.toString() : '')
				.append(StringUtils.isNotEmpty(row.TIMEPOINT) ? (new StringBuilder('_')).append(row.TIMEPOINT).toString() : '')
				.append(StringUtils.isNotEmpty(row.TISSUE_TYPE) ? (new StringBuilder('_')).append(row.TISSUE_TYPE).toString() : '')
				
				mapKey = ((new StringBuilder(row.TRIAL_NAME?.toString()))
					.append((StringUtils.isNotEmpty(row.SAMPLE_CD?.toString()) ? (new StringBuilder('/')).append(row.SAMPLE_CD?.toString()).toString() : '')))?.toString()
				//mapValue = ((new StringBuilder(row.PATIENT_ID?.toString()))
				mapValue = ((new StringBuilder(utilService.getActualPatientId(row.sourcesystem_cd?.toString())))
					.append((StringUtils.isNotEmpty(sample.toString()) ? (new StringBuilder('_')).append(sample).toString() : '')))?.toString()
				if (null == sampleCdsMap.get(mapKey)) {
					sampleCdsMap.put(mapKey, mapValue)
				}
				
				//Recycle (cleanup) of objects
				sample = null
				mapKey = null
				mapValue = null
			})
		
			if (sampleCdsMap.size() > 0) {
				File mRNADir = FileWriterUtil.createDir(studyDir, 'mRNA')
				File rawDataDir = FileWriterUtil.createDir(mRNADir, 'Raw_data')
				
				sampleCdsMap.each { key, value ->
					// create dir with name as value
					File valueDir = FileWriterUtil.createDir(rawDataDir, value)
					// write files into that dir 
					// use service to download files by passing the folder and filesURLs
					def keyList = key.toString().tokenize("/")
					def studyName = (keyList.size() == 2) ? keyList.get(0) : null
					def sampleCd = (keyList.size() == 2) ? keyList.get(1) : null
					if (studyName) {
						def celFiles = getCELFiles(studyName, sampleCd)
						def filesList = []
						for (file in celFiles) {
							def fileURL = (new StringBuffer(file.CEL_LOCATION).append(file.FILE_NAME).append(file.CEL_FILE_SUFFIX)).toString()
							filesList.add(fileURL)
						}
						
						fileDownloadService.getFiles(filesList, valueDir.getPath())
						
					}
				}
			}
		} catch (Exception e) {
			log.error(e.getMessage(),e)
		} finally {
			sql?.close()
		}
	}

	def writeNotEmptyString(writer, str){
		if(StringUtils.isNotEmpty(str)){
			writer.write(str);
		}
	}
	
	def validateCommonSubjectsIn2Subsets(Map resultInstanceIdMap) {
		if (resultInstanceIdMap && !resultInstanceIdMap.isEmpty() && resultInstanceIdMap.size() == 2) {
			def sqlQuery = """
							SELECT DISTINCT ssm.patient_id FROM de_subject_sample_mapping ssm 
							INNER JOIN (SELECT DISTINCT patient_num 
							            FROM qt_patient_set_collection
							            WHERE result_instance_id = ?
							            INTERSECT
							            SELECT DISTINCT patient_num 
							            FROM qt_patient_set_collection
							            WHERE result_instance_id = ?) sc ON ssm.patient_id = sc.patient_num
							"""
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
			def queryParams = []
			queryParams.addAll(resultInstanceIdMap.values())
			def rows = sql.rows(sqlQuery, queryParams)
			log.debug("Common subjects found :: "+rows.size())
			if (rows.size() > 0) throw new Exception(" Common Subjects found in both Subsets. ");
		}
	}
	
	def validateGSEAExport(Map resultInstanceIdMap) {
		try {
			validateCommonSubjectsIn2Subsets(resultInstanceIdMap)
		} catch(Exception e) {
			throw new Exception("GSEA Export validation failed."+e.message)
		}
	}
	
	/**
	* GCT and CLS files are not created within Subset(1/2)_<StudyName> folder.
	* Instead they are created within GSEA folder within the jobTmpDirectory.
	*/
   public void getGCTAndCLSData(studyList, File studyDir, String fileName, 
	   String jobName, Map resultInstanceIdMap, boolean pivot, platformsList) {
	   validateGSEAExport(resultInstanceIdMap)
	   getGCTData(studyList, studyDir, fileName, jobName, resultInstanceIdMap, pivot, platformsList);
	   getCLSData(studyList, studyDir, fileName, jobName, resultInstanceIdMap, platformsList);
   }
   
   public void getGCTData(List studyList, File studyDir, String fileName, String jobName, 
	   Map resultInstanceIdMap, boolean pivot, List platformsList)
   {
	   
	   //This will tell us if there are multiple subsets being exported.
	   def moreThanOneSubset = false
	   
	   try {
		   def sqlQuery, sampleQuery = null;
		   
		   //Create a query for the Subset.
		   if (null != resultInstanceIdMap) {
			   
			   def resultInstanceIds = getResultInstanceIdsAsStr(resultInstanceIdMap)
			   
			   //Check to see if there are multiple subsets in this string.
			   moreThanOneSubset = isMultipleResultInstanceIds(resultInstanceIds)
			   
			   sqlQuery = createGCTPathwayQuery(studyList, resultInstanceIds, platformsList)
			   sampleQuery = createGCTStudySampleAssayQuery(studyList,resultInstanceIds, platformsList)
			   
			   /**
			   * Create the GCT.trans File in <currentJobDir>/GSEA folder, studyDir.parentDir == currentJobDir
			   * currentJobDir == <username_jobType_jobId>, ex: admin_DataExport_12345
			   */
			  def gctFilePath = null
			  if (studyDir.isDirectory()) {
				  def currentJobDir = studyDir.parentFile
				  def gseaDir = (new FileWriterUtil()).createDir(currentJobDir, 'GSEA')
						
				  //Create the file
				  gctFilePath = writeGCTData(sqlQuery, sampleQuery, gseaDir, fileName, jobName, platformsList)
			  }
			   
			   if (null != gctFilePath && pivot) {
				   pivotGCTData(gctFilePath)
			   }
		   }
	   } catch (Exception e) {
		   log.error(e.getMessage(), e);
	   }
   }
   
   public void getCLSData(List studyList, File studyDir, String fileName, String jobName, 
	   Map resultInstanceIdMap, List platformsList) {
	   
	   //This will tell us if there are multiple subsets being exported.
	   def moreThanOneSubset = false
	   
	   try {
		   def sqlQuery, sampleQuery = null;
		   //Create a query for the Subset.
		   if (null != resultInstanceIdMap) {
			   def resultInstanceIds = getResultInstanceIdsAsStr(resultInstanceIdMap)
			   
			   //Check to see if there are multiple subsets in this string.
			   moreThanOneSubset = isMultipleResultInstanceIds(resultInstanceIds)
			   
			   sqlQuery = createCLSDataQuery(studyList, resultInstanceIds, platformsList)
			   /**
			   * Create the GCT.trans File in <currentJobDir>/GSEA folder, studyDir.parentDir == currentJobDir
			   * currentJobDir == <username_jobType_jobId>, ex: admin_DataExport_12345
			   */
			  def clsFilePath = null
			  if (studyDir.isDirectory()) {
				  def currentJobDir = studyDir.parentFile
				  
				  def gseaDir = (new FileWriterUtil()).createDir(currentJobDir, 'GSEA')
				  
				  //Create the file
				  clsFilePath = writeCLSData(sqlQuery, gseaDir, fileName, jobName, resultInstanceIdMap)
			  }
		   }
	   } catch (Exception e) {
	   		log.error(e.getMessage(), e);
	   }
   }
	   
   def private String createCLSDataQuery(List studyList, String resultInstanceIds, List platformsList) {
	   def sSelect = new StringBuilder()
	   sSelect.append("""
		   SELECT DISTINCT 	ssm.patient_id,
							ssm.sample_type,
							ssm.timepoint,
							ssm.tissue_type,
	   						sc.result_instance_id
		   FROM de_subject_sample_mapping ssm 
		   INNER JOIN (SELECT DISTINCT patient_num, result_instance_id 
	                  FROM qt_patient_set_collection 
	                  WHERE result_instance_id IN (""")
   		.append(resultInstanceIds).append(")) sc ON ssm.patient_id = sc.patient_num")
		.append(" WHERE ssm.trial_name IN (").append(convertList(studyList, true, 100)).append(")")
		.append(" AND ssm.gpl_id IN (").append(utilService.toListString(platformsList)).append(")")
	   sSelect.append(" ORDER BY sc.result_instance_id desc")
	   
	   return sSelect.toString();
   }
   
   def private String createGCTPathwayQuery(List studyList, String resultInstanceIds, List platformsList) {
	   def sSelect = new StringBuilder()	   
	   sSelect.append("""
	   	SELECT a.PATIENT_ID, a.LOG_INTENSITY, a.RAW_INTENSITY, a.assay_id, b.probe_id, b.probeset_id, pd.sourcesystem_cd, ssm.gpl_id
	   	FROM de_subject_microarray_data a
		   INNER JOIN (SELECT probe_id, probeset_id, min(gene_id) gene_id
                       FROM de_mrna_annotation
                       group by probe_id, probeset_id) b ON a.probeset_id = b.probeset_id
		   INNER JOIN de_subject_sample_mapping ssm ON (
	   						ssm.trial_name = A.trial_name 
	   					AND ssm.assay_id = A.assay_id)
	   	   INNER JOIN patient_dimension pd ON a.patient_id = pd.patient_num 
	   """)
	   sSelect.append("""
	   		INNER JOIN (SELECT DISTINCT patient_num 
					                  FROM qt_patient_set_collection 
					                  WHERE result_instance_id IN (""")
	   		.append(resultInstanceIds).append(")) sc ON ssm.patient_id = sc.patient_num") 
			.append(" WHERE ssm.trial_name IN (").append(utilService.toListString(studyList)).append(")")
			.append(" AND ssm.gpl_id IN (").append(utilService.toListString(platformsList)).append(")")
	   sSelect.append(" ORDER BY probe_id, patient_id, gpl_id")
	   
	   return sSelect.toString();
   }
   
   def private String createGCTStudySampleAssayQuery(List studyList, String resultInstanceIds, List platformsList) {
	   def sQuery = new StringBuilder();
	   sQuery.append("""
	   	SELECT DISTINCT ssm.assay_id, ssm.sample_type, ssm.timepoint, ssm.tissue_type, ssm.sample_cd, ssm.trial_name, ssm.GPL_ID
	   	FROM de_subject_sample_mapping ssm
	   """);
	   sQuery.append("""INNER JOIN (SELECT DISTINCT patient_num 
					                  FROM qt_patient_set_collection 
					                  WHERE result_instance_id IN (""")
	   		.append(resultInstanceIds).append(")) sc ON ssm.patient_id = sc.patient_num") 
	   sQuery.append(" WHERE ssm.trial_name IN ( ").append(utilService.toListString(studyList)).append(") ")
	   .append(" AND ssm.gpl_id IN (").append(utilService.toListString(platformsList)).append(")")
	   
	   return sQuery.toString()
   }
   
   def private getResultInstanceIdsAsStr(Map resultInstanceIdMap) {
	   def str = new StringBuffer()
	   def mapValues = resultInstanceIdMap.values()
	   mapValues.each { val ->
		   if (val && ((String)val)?.trim() != '') str.append(val).append(',')
	   }
	   str.delete(str.length()-1, str.length())
   
	   return str.toString()
   }
   
   /*
    * This method will check to see if we have multiple result instance ids and return a boolean.
    */
   def boolean isMultipleResultInstanceIds(str){
	   def strList = str?.tokenize(',')
	   return (!strList.empty && strList.size() > 1)
   }
   
   private Integer getStmtFetchSize() {
	   //Grab the configuration that sets the fetch size.
	   def rsize = config.com.recomdata.plugins.resultSize;
	   Integer fetchSize = 5000;
	   if(rsize!=null){
		   try{
			   fetchSize = Integer.parseInt(rsize);
		   }catch(Exception exs){
			   log.warn("com.recomdata.plugins.resultSize is not set!");
		   }
	   }
	   
	   return fetchSize
   }
   
   private Map getSamplesMap(sampleQuery, resultInstanceId = null, splitAttributeColumn) {
	   def sttMap = [:]
	   def con, stmt, rs = null
	   def sttSampleStr = null;
	   
	   try{
		   con = dataSource.getConnection()
		   // sample query
		   stmt = con.prepareStatement(sampleQuery);
		   if (resultInstanceId != null) stmt.setString(1, resultInstanceId);
		   stmt.setFetchSize(getStmtFetchSize());
		   
		   def  sampleType, timepoint,tissueType, assayID = null, GPL_ID;
		   log.info("start sample retrieving query");
		   rs = stmt.executeQuery();
		   
		   while(rs.next()){
			   
			   sampleType = rs.getString("SAMPLE_TYPE");
			   timepoint = rs.getString("TIMEPOINT");
			   tissueType = rs.getString("TISSUE_TYPE");
			   assayID = rs.getString("ASSAY_ID");
			   GPL_ID = rs.getString("GPL_ID");
			   
			   if(splitAttributeColumn)
			   {
				   sttSampleStr = (new StringBuilder()).append(StringUtils.isNotEmpty(sampleType) ? sampleType : '').append(valueDelimiter)
				   .append(StringUtils.isNotEmpty(timepoint) ? timepoint : '').append(valueDelimiter)
				   .append(StringUtils.isNotEmpty(tissueType) ? tissueType : '')
				   .append(StringUtils.isNotEmpty(GPL_ID) ? GPL_ID : '')
				   
			   }else{
			   sttSampleStr = (new StringBuilder()).append(StringUtils.isNotEmpty(sampleType) ? sampleType : '')
				   .append(StringUtils.isNotEmpty(timepoint) ? (new StringBuilder('_')).append(timepoint).toString() : '')
				   .append(StringUtils.isNotEmpty(tissueType) ? (new StringBuilder('_')).append(tissueType).toString() : '')
				   .append(StringUtils.isNotEmpty(GPL_ID) ? (new StringBuilder('_')).append(GPL_ID).toString() : '')
   
			   }
			   sttMap.put(assayID, sttSampleStr.toString());
			   
			   sampleType = null; timepoint = null; tissueType = null; assayID = null;
		   }
	   }finally{
		   rs?.close();
		   stmt?.close();
		   con?.close();
	   }
	   log.info("finished sample retrieving query");
	   
	   return sttMap
   }
   
   private String writeCLSData(String sqlQuery, File gseaDir, String fileName, String jobName, Map resultInstanceIdMap) {
	   def outFile, output, filePath = null
	   try {
		   log.info("started writing CLS file")
		   groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		   outFile = new File(gseaDir, 'GSEA.CLS');
		   output = outFile.newWriter(true)
		   def rows = sql.rows(sqlQuery)
		   if (rows?.size() > 0) {
			   output.write(String.valueOf(rows?.size()))
			   output.write(valueDelimiter);
			   output.write(resultInstanceIdMap?.subset1 && resultInstanceIdMap?.subset2 ? '2' : '1');
			   output.write(valueDelimiter);
			   output.write('1');
			   output.newLine();
			   
			   output.write('#')
			   output.write(valueDelimiter);
			   if (resultInstanceIdMap?.subset1) output.write('Subset1')
			   output.write(valueDelimiter);
			   if (resultInstanceIdMap?.subset2) output.write('Subset2')
			   output.newLine();
			   
				rows.each { row ->
					if (row.RESULT_INSTANCE_ID.toString() == resultInstanceIdMap?.subset1) output.write('0')
					else if (row.RESULT_INSTANCE_ID.toString() == resultInstanceIdMap?.subset2) output.write('1')
					output.write(valueDelimiter)
				};
		  		output.newLine();
		   }
	   } catch (Exception e) {
	   		log.error(e.message, e)
	   } finally {
		   output?.flush();
		   output?.close()
		   filePath = outFile?.getAbsolutePath()
		   log.info("completed writing CLS file")
	   }
   }
   
   private String writeGCTData(String sqlQuery, String sampleQuery, File gseaDir, String fileName, String jobName, gplIds)
   {
	   def sttMap = getSamplesMap(sampleQuery,null,false)
	   
	   def con, stmt, rs = null;
	   con = dataSource.getConnection()
	   stmt = con.prepareStatement(sqlQuery);
	   stmt.setFetchSize(getStmtFetchSize())
	   
	   def filePath = null
	   def char separator = '\t';
	   log.info("started file writing")
	   
	   def outFile = new File(gseaDir, 'GCT.trans');
	   def output = outFile.newWriter(true)
	   
	   output << "PATIENT ID\tDescription\tSAMPLE\tASSAY ID\tVALUE\tPROBE ID\tPROBESET ID\n"
		   
	   def  sampleType, timepoint,tissueType, rawIntensityRS, zScoreRS, patientID, sourceSystemCode, assayID, logIntensityRS, probeID, probesetID, geneSymbolRS, gplID = null
	   def sample, value, zscore,  lineToWrite = null
	   Double rawIntensity = null;
	   String geneID = null;
	   String geneSymbolId = null;
	   String searchKeywordId = null;
	   String log2 = null;
   	   
	   long elapsetime = System.currentTimeMillis();
	   log.info("begin data retrieving query: "+sqlQuery)
	   rs = stmt.executeQuery();
	   log.info("query completed")
	   // get column name map
	   ResultSetMetaData metaData = rs.getMetaData();
	   def nameIndexMap = [:]
	   int count = metaData.getColumnCount();
	   for (int i = 1; i <= count; i++) {
		   nameIndexMap.put(metaData.getColumnName(i), i);
	   }
	   
	   def logIntensityRSIdx = nameIndexMap.get("LOG_INTENSITY");
	   def rawIntensityRSIdx = nameIndexMap.get("RAW_INTENSITY");
	   def ptIDIdx = nameIndexMap.get("PATIENT_ID");
	   def sourceSystemCodeIdx = nameIndexMap.get("SOURCESYSTEM_CD");
	   def assayIDIdx = nameIndexMap.get("ASSAY_ID");
	   def probeIDIdx = nameIndexMap.get("PROBE_ID");
	   def probesetIDIdx = nameIndexMap.get("PROBESET_ID");
	   //def geneIDIdx = nameIndexMap.get("GENE_ID");
	   
	   //A workaround for using only GPL96 values. I don't like the way we have to hard-code GPL96 here.
	   def platformToUse = 'GPL96'
	   def patientProbePlatformValueMap = [:]
	   def gplIDIdx = nameIndexMap.get("GPL_ID");
	   
	   int flushCount = 0;
	   long recCount = 0;
	   try {
		   //Iterate over the record set object.
		   while (rs.next())
		   {
			   //Pull the values we need from the record set object.
			   logIntensityRS = rs.getString(logIntensityRSIdx);
			   rawIntensityRS = rs.getString(rawIntensityRSIdx);
			   patientID = rs.getString(ptIDIdx);
			   sourceSystemCode = rs.getString(sourceSystemCodeIdx)
			   assayID = rs.getString(assayIDIdx);
			   probeID = rs.getString(probeIDIdx);
			   probesetID = rs.getString(probesetIDIdx);
			   //geneID = rs.getString(geneIDIdx);
			   
			   //To use only GPL96 when same probe present in both platforms
			   if (gplIds.size() > 1) { // when there are more than one platforms
				   gplID = rs.getString(gplIDIdx)
				   if (gplID.equals(platformToUse)) { // compared with the hard-coded value GPL96
					   patientProbePlatformValueMap.put(patientID+'_'+probeID+'_'+gplID, logIntensityRS)
				   } else {
					   def probeExistsInGPL96 = patientProbePlatformValueMap.containsKey(patientID+'_'+probeID+'_'+platformToUse)
					   if (probeExistsInGPL96) continue;// don't write the record for probe that already exists in GPL96
				   }
			   }
			   
			   // patient id
			   //writeNotEmptyString(output, patientID);
			   writeNotEmptyString(output, utilService.getActualPatientId(sourceSystemCode))
			   output.write(valueDelimiter);
			   // Row description
			   writeNotEmptyString(output, 'NA');
			   output.write(valueDelimiter);
			   
			   // sample attribute, time point, tissue type
			   def sampleAttribute = determineSampleAttribute(sttMap.get(assayID))
			   output.write((sampleAttribute) ? sampleAttribute : writeNotEmptyString(output, ''));
			   output.write(valueDelimiter);
			   
			   // assay id
			   writeNotEmptyString(output, assayID);
			   output.write(valueDelimiter);
			   
			   /*
			   * If the data is Global Normalized log_intensity is the value to output.
			   * If the log_intensity is NULL, data-load process has to be corrected to always have a value.
			   */
			   if(StringUtils.isNotEmpty(logIntensityRS)){
				   output.write(logIntensityRS);
			   }
			   
			   output.write(valueDelimiter);
			   writeNotEmptyString(output, probeID);
			   output.write(valueDelimiter);
			   writeNotEmptyString(output, probesetID);
			   //output.write(valueDelimiter);
			   //writeNotEmptyString(output, geneID);
			   
			   output.newLine();
			   
			   flushCount++;
			   if(flushCount>=flushInterval) {
				   output.flush();
				   recCount +=flushCount;
				   flushCount = 0;
				   //log.info("# record processed:"+recCount);
			   }
		   }
	   } catch(Exception e) {
		   log.error(e.getMessage(),e)
	   } finally {
		   output?.flush();
		   output?.close()
		   filePath = outFile?.getAbsolutePath()
		   log.info("completed file writing")
		   rs?.close()
		   stmt?.close();
		   con?.close();
	   }
	   
	   // calculate elapse tim
	   elapsetime = System.currentTimeMillis()-elapsetime;
	   log.info("\n \t total seconds:"+(elapsetime/1000)+"\n\n");
	   return filePath
   }
   
   private void pivotGCTData(String inputFileLoc) {
	   log.info('Pivot File started')
	   if (inputFileLoc != "") {
		   File inputFile = new File(inputFileLoc)
		   if (inputFile) {
			   String rOutputDirectory = inputFile.getParent()
			   RConnection c = new RConnection()
			   
			   //Set the working directory to be our temporary location.
			   String workingDirectoryCommand = "setwd('${rOutputDirectory}')".replace("\\","\\\\")
			   
			   log.debug("Attempting following R Command : " + "setwd('${rOutputDirectory}')".replace("\\","\\\\"))
			   println("Attempting following R Command : " + "setwd('${rOutputDirectory}')".replace("\\","\\\\"))
			   
			   //Run the R command to set the working directory to our temp directory.
			   REXP x = c.eval(workingDirectoryCommand)
			   
			   String rScriptDirectory = config.com.recomdata.transmart.data.export.rScriptDirectory
			   String compilePivotDataCommand = "source('${rScriptDirectory}/PivotData/PivotGSEAExportGCTData.R')".replace("\\","\\\\")
			   
			   log.debug("Attempting following R Command : " + compilePivotDataCommand)
			   println("Attempting following R Command : " + compilePivotDataCommand)
			   
			   REXP comp = c.eval(compilePivotDataCommand)
			   
			   //Prepare command to call the PivotGSEAExportGCTData.R script
			   String pivotDataCommand = "PivotGSEAExportGCTData.pivot('$inputFile.name')".replace("\\","\\\\")
			   
			   log.debug("Attempting following R Command : " + pivotDataCommand)
			   println("Attempting following R Command : " + pivotDataCommand)
			   
			   REXP pivot = c.eval(pivotDataCommand)
		   }
	   }
   }
   
   def getGplTitle(gplId){
	   String gplTitle=""
	   String commandString = "SELECT TITLE FROM de_gpl_info where PLATFORM=?"
	   groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
	   sql.eachRow(commandString, [gplId], {row->
		   gplTitle=row[0]
	   })
	   return gplTitle
   }
   
  
}