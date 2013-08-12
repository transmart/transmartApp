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
  

package com.recomdata.gex

import i2b2.SampleInfo

import java.io.File;
import java.util.HashMap;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;
import org.springframework.context.ApplicationContext;

import com.recomdata.transmart.data.export.util.FileWriterUtil;
import com.sun.rowset.CachedRowSetImpl;
/**
 * This class has been replaced with GeneExpressionDataService
 * @author SMunikuntla
 *
 */
@Deprecated
public class GexDao {
	ApplicationContext ctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getAttribute(org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.APPLICATION_CONTEXT)
	def dataSource = ctx.getBean('dataSource')
	def i2b2HelperService = ctx.getBean('i2b2HelperService')
	def springSecurityService = ctx.getBean('springSecurityService')
	def grailsApplication = ctx.getBean('grailsApplication')
	
	//private static org.apache.log4j.Logger log = Logger.getLogger(GexDao.class);
	private static final log = LogFactory.getLog('grails.app.' +GexDao.class.name)
	
	//def SearchKeyword = ctx.getBean('SearchKeyword')
	def config = ConfigurationHolder.config

	//This is the SQL query we use to get our data.
	private String sqlQuery = ""

	private Map sampleCdsMap = [:]

	public  getData(String study, File studyDir, String fileName, String jobName, String resultInstanceId, boolean pivot, String pathway, String timepoint, String sampleTypes)
	{
		try {
			pathway = derivePathwayName(pathway)
			
			//Create a query for the Subset.
			if (null != resultInstanceId) {
				//Get the subjects for this result instance id.
				//def subjectIds = i2b2HelperService.getSubjects(resultInstanceId)
	
				//Get the concepts for this result instance id.
				def concepts = i2b2HelperService.getConcepts(resultInstanceId)
	
				//Add the subquery to the main query.
				sqlQuery = createMRNAHeatmapPathwayQuery(study, resultInstanceId, pathway, timepoint, sampleTypes)
				
				println("mRNAData : " + sqlQuery)
				
			}
			def outFile = writeData(resultInstanceId, studyDir, fileName, jobName)
			if (null != outFile && pivot) {
				pivotData(outFile)
			}
			
		} catch (Exception e) {
			log.info(e.getMessage());
		}

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
	def String createMRNAHeatmapPathwayQuery(String study, String resultInstanceId, String pathwayName, String timepoint, String sampleTypes) throws Exception
	{

		//Get the list of trial names based on patient IDs.
		//def trialNames  = getTrialName(ids);

		//Get the list of assay Ids based on resultInstanceId, sample types, and timepoints.
		//String assayIds = getAssayIds(resultInstanceId, sampleTypes, timepoint);

		//If we didn't find any assay Id's, abandon all hope.
		//if (assayIds.equals(''))
		//	throw new Exception("No heatmap data for the specified parameters.");

		//If a pathway was specified add the gene filter.
		String genes;
		if (pathwayName != null && pathwayName.length() > 0)
		{
			genes = getGenes(pathwayName);
		}

		//Build the string to get the sample data.
		StringBuilder s = new StringBuilder()
		s.append(""" 
				SELECT DISTINCT /*+ parallel(de_subject_microarray_data,4) */ /*+ parallel(de_mrna_annotation,4) */
				a.PATIENT_ID, a.RAW_INTENSITY, a.ZSCORE, a.LOG_INTENSITY, a.assay_id, 
				ssm.sample_type, ssm.timepoint, ssm.tissue_type, ssm.sample_cd, ssm.trial_name,
				b.probe_id, b.probeset_id, b.GENE_SYMBOL, b.GENE_ID,sk.SEARCH_KEYWORD_ID
				FROM de_subject_microarray_data a
				INNER JOIN de_mrna_annotation b ON a.probeset_id = b.probeset_id
				INNER JOIN bio_marker bm ON bm.PRIMARY_EXTERNAL_ID = to_char(b.GENE_ID)
				INNER JOIN bio_marker_correl_mv sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id
				INNER JOIN search_keyword sk ON sk.bio_data_id = sbm.bio_marker_id
				INNER JOIN de_subject_sample_mapping ssm ON (ssm.trial_name = A.trial_name AND ssm.assay_id = A.assay_id)
				INNER JOIN (SELECT DISTINCT sc.patient_num FROM qt_patient_set_collection sc, patient_dimension pd
		 		WHERE sc.result_instance_id = CAST(? AS numeric) AND pd.sourcesystem_cd NOT LIKE '%:S:%'
		 		AND sc.patient_num = pd.patient_num) sub ON ssm.patient_id = sub.patient_num
				""");
		//TODO replace study within query as parameter to query
		s.append(" WHERE a.trial_name = '").append(study).append("' ")
		//.append(" AND a.assay_id IN (").append(assayIds).append(")")

		if (pathwayName != null && pathwayName.length() > 0)	{
			s.append(" AND b.gene_id IN (").append(genes).append(")");
		}
		
		s.append(" ORDER BY probe_id, patient_id")
		
		//log.debug(s.toString());
		return s.toString();
	}
	
	def String createDownloadCELFilesQuery(String study, String subjectIds, String timepoint, String sampleTypes) throws Exception
	{
		StringBuilder s = new StringBuilder()
		//Get the list of assay Ids based on patient ids, sample types, and timepoints.
		String assayIds = getAssayIds(subjectIds, sampleTypes, timepoint);
		//If we didn't find any assay Id's, abandon all hope.
		if (StringUtils.isNotEmpty(assayIds)) {
			//Build the string to get the sample data.
			s.append("""
					SELECT DISTINCT /*+ parallel(de_subject_microarray_data,4) */ 
					a.PATIENT_ID, ssm.sample_type, ssm.timepoint, ssm.tissue_type, ssm.sample_cd, ssm.trial_name
					FROM de_subject_microarray_data a
					INNER JOIN de_subject_sample_mapping ssm ON (ssm.trial_name = A.trial_name AND ssm.assay_id = A.assay_id)
					""");
			//TODO replace study within query as parameter to query
			s.append(" WHERE a.trial_name = '").append(study).append("' ")
			.append(" AND a.assay_id IN (").append(assayIds).append(")")
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
	def String getAssayIds(String resultInstanceId, String sampleTypes, String timepoint) {

		//Sql command used to retrieve Assay IDs.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

		//SQL Query string.
		StringBuilder assayS = new StringBuilder("select distinct s.assay_id  from de_subject_sample_mapping s");
		assayS.append(", (SELECT DISTINCT sc.patient_num FROM qt_patient_set_collection sc, patient_dimension pd")
		.append(" WHERE sc.result_instance_id = CAST(? AS numeric) AND pd.sourcesystem_cd NOT LIKE '%:S:%'")
		.append(" AND sc.patient_num = pd.patient_num) A where s.patient_id = A.patient_num");

		//If we have a sample type, append it to the query.
		if(sampleTypes!=null && sampleTypes.length()>0)
		{
			assayS.append(" AND s.sample_type_cd IN ").append(convertStringToken(sampleTypes));
		}

		//If we have timepoints, append it to the query.
		if(timepoint!=null && timepoint.trim().length()>0){
			assayS.append(" AND s.timepoint_cd IN ").append(convertStringToken(timepoint));
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
		}
		);

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
	 * Get the genes in a pathway based on the data in the search database.
	 * @param pathwayName
	 * @return
	 */
	def String getGenes (String pathwayName) {

		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

		//Determine if a gene signature or list was used based on the string passed in.
		StringBuilder pathwayS = new StringBuilder();

		if(pathwayName.startsWith("GENESIG")||pathwayName.startsWith("GENELIST"))
		{
			pathwayS.append(" select  distinct bm.primary_external_id as gene_id from ")
					.append("search_keyword sk, ")
					.append(" search_bio_mkr_correl_fast_mv sbm,")
					.append(" bio_marker bm")
					.append(" where sk.bio_data_id = sbm.domain_object_id")
					.append(" and sbm.asso_bio_marker_id = bm.bio_marker_id")
					.append(" and sk.unique_id IN ");
		}
		else {
			pathwayS.append(" select  distinct bm.primary_external_id as gene_id from ")
					.append("search_keyword sk, ")
					.append(" bio_marker_correl_mv sbm,")
					.append(" bio_marker bm")
					.append(" where sk.bio_data_id = sbm.bio_marker_id")
					.append(" and sbm.asso_bio_marker_id = bm.bio_marker_id")
					.append(" and sk.SEARCH_KEYWORD_ID IN ");
		}
		
		//pathwayS.append(pathwayName.replaceAll("'","''")).append("'");

		//Construct an in list in case the user had multiple genes separated by ",".
		pathwayS.append(convertStringToken(pathwayName));
		
		log.debug("query to get genes from pathway: " + pathwayS.toString());

		//Add genes to an array.
		def genesArray =[];
		sql.eachRow(pathwayS.toString(), {row->
			if(row.gene_id!=null){
				genesArray.add(row.gene_id);
			}
		}
		);

		//Convert the genes array to a string.
		String genes = convertList(genesArray, false, 1000);
		return genes;
	}



	/**
	 *  Compose a list of columns based on an id and a prefix.
	 *
	 * @param biomarker    probeset (mRNA), component (Protein) and antigen_name (RBM)
	 * @param idMap
	 * @param prefix		usually use "S"
	 * @return
	 */
	def String listHeatmapColumnsFromMap(String biomarker, String prefix, boolean roundColumn)
	{
		//This has no use.........yet!
		/*
		 //This is the start of our string.
		 StringBuilder s = new StringBuilder();
		 if(biomarker != "") s.append(" " + biomarker + ", gene_symbol, ")
		 //Loop over the JSON object and add the ID's with the .
		 sampleIdList.each
		 {
		 subsetItem ->
		 def subsetItems = subsetItem.value
		 //Each subset has a list of IDs.
		 subsetItems.each
		 {
		 SampleInfo sampleInfo = sampleInfoMap.get(it)
		 if(roundColumn)
		 {
		 s.append ("round(" + prefix + subsetItem.key + "_" + sampleInfo.patientId + ", 4) as " + prefix + subsetItem.key + "_" + sampleInfo.patientId + ",")
		 }
		 else
		 {
		 s.append ("'" + prefix + subsetItem.key + "_" + sampleInfo.patientId + "' as " + prefix + subsetItem.key + "_" + sampleInfo.patientId + ",")
		 }
		 }
		 }
		 return s.substring(0, s.length()-1);
		 */
	}

	/**
	 *
	 */
	def convertStringToken(String t) {
		String[] ts = t.split(",");
		StringBuilder s = new StringBuilder("(");
		for(int i=0; i<ts.length;i++){
			if(i>0)
				s.append(",");
			s.append("'");
			s.append(ts[i]);
			s.append("'");
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

	private String writeData(String resultInstanceId, File studyDir, String fileName, String jobName)
	{
		def filePath = null
		//TODO Get the dataTypeName from the list of DataTypeNames either from DB or from config file
		def dataTypeName = "mRNA";
		//TODO set this to either "Raw_Files/Findings" or NULL for processed_files
		def dataTypeFolder = "Processed_Data";
		//Build the query to get the gene expression data.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		
		def char separator = '\t';
		log.info("started file writing")
		def output;
		def outFile;
		
		FileWriterUtil writerUtil = new FileWriterUtil(studyDir, fileName, jobName, dataTypeName, dataTypeFolder, separator);
		outFile = writerUtil.outputFile
		output = outFile.newWriter(true)
		
		output << "PATIENT ID\tSAMPLE\tASSAY ID\tVALUE\tZSCORE\tLOG2ED\tPROBE ID\tPROBESET ID\tGENE_ID\tSEARCH_KEYWORD_ID\tGENE_SYMBOL\n"
	
		//writerUtil.writeLine(["PATIENT ID", "SAMPLE", "ASSAY ID", "VALUE", "LOG2ED", "PROBE ID", "PROBESET ID"] as String[])
		//def listToWrite = []
		def sample, log2, value, rawIntensity, zscore, lineToWrite = null
		try {
			sql.eachRow(sqlQuery, [resultInstanceId], { row ->
				
				sample = (new StringBuilder()).append(StringUtils.isNotEmpty(row.SAMPLE_TYPE) ? row.SAMPLE_TYPE.toString() : '')
				.append(StringUtils.isNotEmpty(row.TIMEPOINT) ? (new StringBuilder('_')).append(row.TIMEPOINT).toString() : '')
				.append(StringUtils.isNotEmpty(row.TISSUE_TYPE) ? (new StringBuilder('_')).append(row.TISSUE_TYPE).toString() : '')
				
				rawIntensity = (null != row.RAW_INTENSITY) ? Double.valueOf(row.RAW_INTENSITY) : null
				zscore = (null != row.ZSCORE) ? Double.valueOf(row.ZSCORE) : ''
				
				if (rawIntensity != null) {
					value = Math.log(rawIntensity)/Math.log(2)
					log2 = 1
				} else {
					value = zscore
					log2 = 0
				}
				
				lineToWrite = new StringBuilder()
				.append(StringUtils.isNotEmpty(row.PATIENT_ID?.toString()) ? row.PATIENT_ID?.toString() : '').append('\t')
				.append(sample).append('\t')
				.append(StringUtils.isNotEmpty(row.ASSAY_ID?.toString()) ? row.ASSAY_ID?.toString() : '').append('\t')
				.append(value).append('\t').append(zscore).append('\t').append(log2).append('\t')
				.append(StringUtils.isNotEmpty(row.PROBE_ID?.toString()) ? row.PROBE_ID?.toString() : '').append('\t')
				.append(StringUtils.isNotEmpty(row.PROBESET_ID?.toString()) ? row.PROBESET_ID?.toString() : '').append('\t')
				.append(StringUtils.isNotEmpty(row.GENE_ID?.toString()) ? row.GENE_ID?.toString() : '').append('\t')
				.append(StringUtils.isNotEmpty(row.SEARCH_KEYWORD_ID?.toString()) ? row.SEARCH_KEYWORD_ID?.toString() : '').append('\t')
				.append(StringUtils.isNotEmpty(row.GENE_SYMBOL?.toString()) ? row.GENE_SYMBOL?.toString() : '').append('\n')
				
				output << lineToWrite.toString()
				
				//Recycle (cleanup) of objects
				sample = null
				log2 = null
				value = null
				rawIntensity = null
				zscore = null
				lineToWrite = null
			})
		} catch(Exception e) {
			log.info(e.getMessage())
			log.info(e.printStackTrace())
		} finally {
			output?.close()
			filePath = outFile?.getAbsolutePath()
			//writerUtil.finishWriting();
			log.info("completed file writing")
			sql?.close();
		}
		return filePath
	}
	
	private void pivotData(String inputFileLoc) {
		log.info('Pivot File started')
		if (inputFileLoc != "") {
			File inputFile = new File(inputFileLoc)
			if (inputFile) {
				String rOutputDirectory = inputFile.getParent()
				RConnection c = new RConnection()
				
				//Set the working directory to be our temporary location.
				String workingDirectoryCommand = "setwd('${rOutputDirectory}')".replace("\\","\\\\")
				//Run the R command to set the working directory to our temp directory.
				REXP x = c.eval(workingDirectoryCommand)
				
				String pluginScriptDirectory = config.com.recomdata.plugins.pluginScriptDirectory
				String compilePivotDataCommand = "source('${pluginScriptDirectory}/PivotData/PivotGeneExprData.R')"
				REXP comp = c.eval(compilePivotDataCommand)
				//Prepare command to call the PivotClinicalData.R script
				String pivotDataCommand = "PivotGeneExprData.pivot('$inputFile.name')"
				//Run the R command to pivot the data in the clinical.i2b2trans file.
				REXP pivot = c.eval(pivotDataCommand)
			}
		}
	}

	private String derivePathwayName( pathway_name)	{
		//log.debug("Derived pathway name as ${pathway_name}")
		
			//log.debug("Pathway name has been set to ${pathway_name}")
		
			if (pathway_name == null || pathway_name.length() == 0 || pathway_name == "null" ) 
			{
				//log.debug("Resetting pathway name to null")
				pathway_name = null
			}
			
			//grailsApplication.config.com.recomdata.search.genepathway=='native'
			boolean nativeSearch = true
			
			//log.debug("nativeSearch: ${nativeSearch}")
			
			if(!nativeSearch && pathway_name != null)	
			{
				//pathway_name = SearchKeyword.get(Long.valueOf(pathway_name)).uniqueId;
				//log.debug("pathway_name has been set to a keyword ID: ${pathway_name}")
			}

		return pathway_name
	}

	private String getCELLocation(String studyName) {
		//Build the query to get the clinical data.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		sqlQuery = "SELECT cel_location FROM bio_content WHERE study_name = ?"
		def row = sql.firstRow(sqlQuery, [studyName])
		
		return row?.CEL_LOCATION
	}
	
	def downloadCELFiles(String resultInstanceId, String study, File studyDir, String jobName, String pathway, String timepoint, String sampleTypes) {
		groovy.sql.Sql sql = null
		try {
			//Get the subjects for this result instance id.
			def subjectIds = i2b2HelperService.getSubjects(resultInstanceId)

			//Get the concepts for this result instance id.
			def concepts = i2b2HelperService.getConcepts(resultInstanceId)

			//Add the subquery to the main query.
			sqlQuery = createDownloadCELFilesQuery(study, subjectIds, timepoint, sampleTypes)
			sql = new groovy.sql.Sql(dataSource)
			def sample, mapKey, mapValue = null
			sql.eachRow(sqlQuery, { row ->
				
				sample = (new StringBuilder()).append(StringUtils.isNotEmpty(row.SAMPLE_TYPE) ? row.SAMPLE_TYPE.toString() : '')
				.append(StringUtils.isNotEmpty(row.TIMEPOINT) ? (new StringBuilder('_')).append(row.TIMEPOINT).toString() : '')
				.append(StringUtils.isNotEmpty(row.TISSUE_TYPE) ? (new StringBuilder('_')).append(row.TISSUE_TYPE).toString() : '')
				
				mapKey = ((new StringBuilder(row.TRIAL_NAME?.toString()))
					.append((StringUtils.isNotEmpty(row.SAMPLE_CD?.toString()) ? (new StringBuilder('/')).append(row.SAMPLE_CD?.toString()).toString() : '')))?.toString()
				mapValue = ((new StringBuilder(row.PATIENT_ID?.toString()))
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
				sampleCdsMap.each { key, value ->
					def keyList = key.toString().tokenize("/")
					def studyName = (keyList.size() == 2) ? keyList.get(0) : null
					if (studyName) {
						def celLocation = getCELLocation(studyName)
						if (celLocation && celLocation != '') {
							def CELFileURL = celLocation + '/' + keyList.get(1) + '.CEL.gz'
							downloadCELFile(studyDir, jobName, value+'.CEL.gz', CELFileURL)
						}
					}
				}
			}
		} catch (Exception e) {
			log.info(e.getMessage())
		} finally {
			sql?.close()
		}
	}
	
	private File downloadCELFile(File studyDir, String jobName, String fileName, String CELFileURL) {
		def char separator = '\t';
		FileWriterUtil writerUtil = new FileWriterUtil(studyDir, fileName, jobName, "mRNA", "Raw_data", separator);
		//def file = writerUtil.outputFile
		
		//Uses Java NIO (New I/O) 
		writerUtil.writeFile(CELFileURL, writerUtil.outputFile)
		/*use (FileBinaryCategory)
		{
		  file << CELFileURL.toURL()
		}*/
	}

}

class FileBinaryCategory
{
  def static leftShift(File a_file, URL a_url)
  {
	def InputStream input = null
	def BufferedOutputStream output = null

	try
	{
	  input = a_url.openStream()
	  output = a_file.newOutputStream()

	  output << input
	}
	finally
	{
	   input?.close()
	   output?.close()
	}
  }
}


