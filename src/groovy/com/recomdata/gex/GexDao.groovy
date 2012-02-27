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
import java.util.HashMap;

import org.springframework.context.ApplicationContext;

import com.recomdata.transmart.data.export.util.FileWriterUtil;
import com.sun.rowset.CachedRowSetImpl;

public class GexDao {
	ApplicationContext ctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getAttribute(org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.APPLICATION_CONTEXT)
	def dataSource = ctx.getBean('dataSource')
	def i2b2HelperService = ctx.getBean('i2b2HelperService')
	def springSecurityService = ctx.getBean('springSecurityService')
	def grailsApplication = ctx.getBean('grailsApplication')
	//def SearchKeyword = ctx.getBean('SearchKeyword')

	//This is the SQL query we use to get our data.
	private String sqlQuery = "";


	public  getData(String fileName, String jobName, HashMap result_instance_ids, boolean pivot, String pathway, String timepoint, String sampleTypes)
	{

		pathway = derivePathwayName(pathway)
		
		//We create a query for each subset and union them together.
		result_instance_ids.each
		{ result_instance ->

			if (result_instance.value) {
				//Get the subjects for this result instance id.
				def subjectIds = i2b2HelperService.getSubjects(result_instance.value)
	
				//Get the concepts for this result instance id.
				def concepts = i2b2HelperService.getConcepts(result_instance.value)
	
				//If the string isn't empty, we need a Union because we already have a subset in the query.
				if(sqlQuery != "")
				{
					sqlQuery += " UNION "
				}
	
				//Add the subquery to the main query.
				sqlQuery += createMRNAHeatmapPathwayQuery(result_instance.key,subjectIds,pathway,timepoint,sampleTypes)
			}
		}

		//TODO: Implement this code.
		//To make this robust we offer the user the option to pivot the data using oracle.
		if(pivot)
		{
			//Get the list of columns for the select statement
			//columns = listHeatmapColumns("probeset", ids1, ids2, "S1_", "S2_") + ", star"

			//Add our columns and subqueries to the main query.
			//sqlQuery = "SELECT " + columns + " FROM (" + sqlQuery +	") PIVOT (avg("+intensityColumn+") for subject_id IN (" + subjects + ")) ";
		}

		//Add an order by clause to our query.
		sqlQuery += " ORDER BY GENE_SYMBOL";

		writeData(fileName, jobName);

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
	def String createMRNAHeatmapPathwayQuery(String subsetName, String ids, String pathwayName, String timepoint, String sampleTypes) throws Exception
	{

		//Get the list of trial names based on patient IDs.
		String trialNames  = getTrialName(ids);

		//Get the list of assay Ids based on patient ids, sample types, and timepoints.
		String assayIds    = getAssayIds(ids, sampleTypes, timepoint);

		//If we didn't find any assay Id's, abandon all hope.
		if (assayIds.equals(''))
			throw new Exception("No heatmap data for the specified parameters.");

		//If a pathway was specified add the gene filter.
		String genes;
		if (pathwayName != null && pathwayName.length() > 0)
		{
			genes = getGenes(pathwayName);
		}

		//Build the string to get the sample data.
		StringBuilder s = new StringBuilder();
		s.append(""" 
				SELECT DISTINCT /*+ parallel(de_subject_microarray_data,4) */ /*+ parallel(de_mrna_annotation,4) */
				a.PATIENT_ID,
				a.TIMEPOINT,
				a.PVALUE,
				a.RAW_INTENSITY,
				b.GENE_SYMBOL
				FROM de_subject_microarray_data a
				INNER JOIN de_mrna_annotation b ON a.probeset_id = b.probeset_id
				""");
		s.append(" WHERE a.trial_name IN (").append(trialNames).append(") ");
		s.append(" AND a.assay_id IN (").append(assayIds).append(")");

		if (pathwayName != null && pathwayName.length() > 0)	{
			s.append(" AND b.gene_id IN (").append(genes).append(")");
		}

		//log.debug(s.toString());
		return s.toString();
	}

	/**
	 * This method retrieves the Assay Ids for the given paramters from the de_subject_sample_mapping table.
	 * @param ids List of Patient IDS.
	 * @param sampleTypes Sample type
	 * @param timepoint List of timepoints.
	 * @return
	 */
	def String getAssayIds(String ids, String sampleTypes, String timepoint) {

		//Sql command used to retrieve Assay IDs.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

		//SQL Query string.
		StringBuilder assayS = new StringBuilder("select distinct s.assay_id  from de_subject_sample_mapping s where s.patient_id in (").append(ids).append(")");

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

		//log.debug("getAssayIds used this query: " + assayS.toString());

		//Add each result to an array.
		def assayIdsArray =[];

		sql.eachRow(assayS.toString(), {row->
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
					.append(" and sk.unique_id ='");
		}
		else {
			pathwayS.append(" select  distinct bm.primary_external_id as gene_id from ")
					.append("search_keyword sk, ")
					.append(" bio_marker_correl_mv sbm,")
					.append(" bio_marker bm")
					.append(" where sk.bio_data_id = sbm.bio_marker_id")
					.append(" and sbm.asso_bio_marker_id = bm.bio_marker_id")
					.append(" and sk.unique_id ='");
		}
		pathwayS.append(pathwayName.replaceAll("'","''")).append("'");

		//log.debug("query to get genes from pathway: " + pathwayS.toString());

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

	private void writeData(String fileName, String jobName)
	{
		//TODO Get the dataTypeName from the list of DataTypeNames either from DB or from config file
		def dataTypeName = "mRNA";
		//TODO set this to either "Raw_Files/Findings" or NULL for processed_files
		def dataTypeFolder = null;
		//Build the query to get the clinical data.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		CachedRowSetImpl cached=new CachedRowSetImpl();
		sql.query(sqlQuery) { cached.populate(it); }
		cached.beforeFirst()
		
		def char separator = '\t';
		File outputFile = FileWriterUtil.setupOutputFile(fileName, jobName, dataTypeName, dataTypeFolder);
		FileWriterUtil.write(outputFile, separator, cached, null, null);
		
		sql.close();
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

	

}


