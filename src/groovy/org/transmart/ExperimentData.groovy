package org.transmart
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
  

import com.recomdata.export.GenePatternFiles

import grails.converters.JSON
import i2b2.SampleInfo;

import java.sql.ResultSet
import java.sql.Statement
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.web.json.JSONArray
import org.json.JSONObject
import org.springframework.context.ApplicationContext

import com.recomdata.export.GenePatternFiles;
import groovy.util.logging.*

public class ExperimentData 
{

	ApplicationContext ctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getAttribute(org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.APPLICATION_CONTEXT)
	def dataSource = ctx.getBean('dataSource')
	def sessionFactory = ctx.getBean('sessionFactory');
	def sampleInfoService = ctx.getBean('sampleInfoService');
	
	//Logger log = Logger.getLogger();
	
	String pathwayName = "";
	String subjectIds1 = "";
	String subjectIds2 = "";
	String distinctSubjectIds1 = "";
	String distinctSubjectIds2 = "";
	String concepts1 = "";
	String concepts2 = "";
	String timepoints1 = "";
	String timepoints2 = "";
	String samples1 = "";
	String samples2 = "";
	String rbmPanels1 = "";
	String rbmPanels2 = "";
	String dataType = "";
	
	String analysisType = "";
	String whiteString = "";
	String experimentDataQuery = "";
	String trialNames = "";
	String columnList = "";
	
	//This is a JSON Object representing the selected subsets.
	//{"SampleIdList":{"1":["Sample1"],"2":[],"3":[]}}
	def sampleIdList;
	
	GenePatternFiles gpf;
		
	boolean fixlast;
	boolean rawdata;
	
	def numColumnsClosure ={ meta ->  numCols= meta.columnCount }
		
	Integer startingOutputColumn = 0;
	
	Map<Long, SampleInfo> sampleInfoMap = new HashMap<Long, SampleInfo>();
	
	//This is passed to the create header method of the GCT files.
	String[] subjectNameArray
	
	def  getHeatMapData() throws Exception 
	{
		//Get a distinct list of the patients we have data on. Queries "de_subject_sample_mapping".
		distinctSubjectIds1 = filterSubjectIdByBiomarkerData(subjectIds1);
		distinctSubjectIds2 = filterSubjectIdByBiomarkerData(subjectIds2);
		
		//Check to see if we actually had data in the table.
		if (distinctSubjectIds1.equals("") || distinctSubjectIds2.equals(""))	
		{
			throw new Exception("No heatmap data for the given subjects.");
		}
		
		if (dataType == null)	
		{
			throw new Exception("Please choose a platform for analysis.");
		}
		
		String intensityType ="LOG2";
		
		if(rawdata){intensityType ="RAW";}
		
		if(dataType.toUpperCase()=="MRNA_AFFYMETRIX")
		{
			experimentDataQuery = createMRNAHeatmapBaseQuery(pathwayName,distinctSubjectIds1,distinctSubjectIds2,concepts1,concepts2,timepoints1,timepoints2,samples1,samples2,intensityType);
		}
		else if(dataType.toUpperCase()=="RBM")
		{
			experimentDataQuery = createRBMHeatmapQuery(pathwayName, distinctSubjectIds1, distinctSubjectIds2, concepts1, concepts2, timepoints1, timepoints2, rbmPanels1, rbmPanels2);
		}
		else if(dataType.toUpperCase()=="PROTEIN")
		{
			experimentDataQuery = createProteinHeatmapQuery(pathwayName, distinctSubjectIds1, distinctSubjectIds2, concepts1, concepts2, timepoints1, timepoints2);
		}

		//log.debug("mRNA heatmap query: " + query);
		
	}
	
	public void getHeatMapDataSample()
	{
		
		String intensityType ="LOG2";
		
		if(rawdata){intensityType ="RAW";}
		
		//This method will fill in some properties.
		gatherSampleData()
		dataType = "MRNA_AFFYMETRIX"
		
		//Build the query to retrieve the sample data.
		if(dataType.toUpperCase()=="MRNA_AFFYMETRIX")
		{
			experimentDataQuery = buildQuerySampleMRNA(intensityType)
		}
		else if(dataType.toUpperCase()=="RBM")
		{
			experimentDataQuery = createRBMHeatmapQuery(pathwayName, distinctSubjectIds1, distinctSubjectIds2, concepts1, concepts2, timepoints1, timepoints2, rbmPanels1, rbmPanels2);
		}
		else if(dataType.toUpperCase()=="PROTEIN")
		{
			experimentDataQuery = createProteinHeatmapQuery(pathwayName, distinctSubjectIds1, distinctSubjectIds2, concepts1, concepts2, timepoints1, timepoints2);
		}
	}
	
	
	/**
	 * This will build the query as a string that will retrieve our sample data.
	 */
	private void gatherSampleData()
	{
		//First, create a string that has all the sample ID's. We will use this to get the distinct Trials.
		//While doing this we will also construct a map that has Subset:[List of SampleInfo objects].
		//This is the list of all Sample IDs.
		String sampleIdAllListStr = "";
		
		//This is our map which separates our SampleInfo objects into subsets.
		def sampleInfoSubsetMap = [:]
		
		//Create a list that has S#_SAMPLE_CD (Sample_CD is from de_subject_sample_mapping).
		List<String> sampleNameListWithPrefix = new ArrayList<String>();
	
		//Loop over the JSON object and grab the ID's.
		sampleIdList.each
		{
			subsetItem ->
			
			def subsetSampleList = subsetItem.value

			//Each subset has a list of IDs.
			subsetSampleList.each 
			{
				if(sampleIdAllListStr != "") sampleIdAllListStr += ","
				
				//Add the sample ID to the complete list.
				sampleIdAllListStr += it
			}

		}
		
		//Get the trial names based on sample_id.
		trialNames  = getTrialNameBySampleID(sampleIdAllListStr);
		if(trialNames == "") throw new Exception("Could not find trial names for the given subjects!")
		
		//We need to build a map of the sample info objects so that we can easily access the sample info objects by key without going to the DB.
		List<SampleInfo> sampleInfoList = sampleInfoService.getSampleInfoListInOrder(sampleIdAllListStr);
		
		for (SampleInfo sampleInfo : sampleInfoList)
		{
			sampleInfoMap.put(sampleInfo.id, sampleInfo);
		}

		//After we build the map we can build the sample name list.
		
		sampleIdList.each
		{
			subsetItem ->
			
			def subsetSampleList = subsetItem.value
			
			//We pass in the the list of sample ids for each subset.
			getSampleNameListFromIds(subsetSampleList, "S" + subsetItem.key + "_", sampleNameListWithPrefix);
		}
		
		//Build the array for the header for our gct file.
		subjectNameArray = new String[sampleNameListWithPrefix.size()];
		
		for (int i = 0; i < sampleNameListWithPrefix.size(); i ++) 
		{
			subjectNameArray[i] = sampleNameListWithPrefix.get(i);
		}
	}
	
	public String buildQuerySampleMRNA(intensityType)
	{
		//This is the list of columns in our select statement.
		String columns = null;
		 
		//We need to get the list of columns. The list of columns is based on what we name them in the subquery creation methods below. Which is subject ID.
		columns = listHeatmapColumnsFromMap("probeset", "S", true) + ", star"
		//log.debug("SELECT: " + columns)

		//This will be our counter which we use to build the prefix.
		int subsetCounter = 1;
		
		//For each of the susbsets we need to build a query which we union together with the other subsets.
		def subsetQueries = [];
		
		//We need to pass the ordered list of AssayIds to this function. The Assay ID is stored in the map of sampleInfo objects.
		
		sampleIdList.each
		{
			subsetItem ->
			
			def subsetSampleList = subsetItem.value

			//Don't add a subset if there are no items in the subset.
			if(subsetSampleList.size() > 0)
			{	
				String currentAssayIds = buildAssayIdsFromSampleIds(subsetSampleList)
				String currentSubsetQuery = createMRNAHeatmapPathwayQuery("S" + subsetItem.key + "_",currentAssayIds, intensityType)
				
				subsetQueries.add(currentSubsetQuery)
			}
		}
		
		//We have to use the log2_intensity to make the analysiscontroller happy..
		String intensityColumn = "LOG2_INTENSITY";

		//I think this needs to be "SAMPLE_ID" IN.
		String subjects = listHeatmapColumnsFromMap("","S",false) + ", '*' as star";
		
		//For the final query we need to UNION together all the subsets.
		String r;
		r = "SELECT " + columns + " FROM ("

		subsetCounter = 1;
				
		//TODO:CHECK FOR EMPTY
		subsetQueries.each
		{
			subsetQuery -> 
			
			if(subsetCounter > 1) r += " UNION "
			
			r += subsetQuery
			
			subsetCounter +=1
		}
		
		r += ") PIVOT (avg("+intensityColumn+") for subject_id IN (" + subjects + ")) ";
		
		r = r+" ORDER BY PROBESET, GENE_SYMBOL";
		
		return r.toString();
	}
	
	public String buildAssayIdsFromSampleIds(sampleIds)
	{
		String currentAssayIds = ""
		
		sampleIds.each
		{
			SampleInfo sampleInfo = sampleInfoMap.get(it);
			String currentAssayId = sampleInfo.assayId;
			if(currentAssayIds != "") currentAssayIds += ","
			currentAssayIds += currentAssayId
		}
		
		return currentAssayIds
	}
	
	public String subsetQuerySample()
	{
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		//Get the list of trial names based on
		String trialNames  = getTrialName(ids);
		String assayIds    = getAssayIds(ids, sampleTypes, timepoint);
		
		if (assayIds.equals(''))
			throw new Exception("No heatmap data for the specified parameters.");
		
		String genes;
		if (pathwayName != null && pathwayName.length() > 0)	{
			genes = getGenes(pathwayName);
		}
		
		String intensityCol = "zscore";
		
		if("RAW"==intensityType)
		{
			intensityCol = "RAW_INTENSITY";
			
			//check if we have sufficient raw data to run gp query
			def goodPct
			String rawCountQuery="select DISTINCT /*+ parallel(de_subject_microarray_data,4) */ /*+ parallel(de_mrna_annotation,4) */ count(distinct a.raw_intensity)/count(*) as pct_good " +
					"FROM de_subject_microarray_data a, de_mrna_annotation b " +
					"WHERE a.probeset_id = b.probeset_id AND a.trial_name IN ("+trialNames+") " +
					"AND a.assay_id IN ("+assayIds+")";
					
			sql.eachRow (rawCountQuery,, {row-> goodPct=row[0]})
			
			if(goodPct==0) throw new Exception("No raw data for Comparative Marker Selection.");
		}
		
		// added hint here...
		StringBuilder s = new StringBuilder();
		s.append("select DISTINCT /*+ parallel(de_subject_microarray_data,4) */ /*+ parallel(de_mrna_annotation,4) */  b.PROBE_ID || ':' || b.GENE_SYMBOL as PROBESET, b.GENE_SYMBOL, "+"a."+intensityCol+" as LOG2_INTENSITY ");
		s.append(" , '").append(prefix).append("' || a.patient_ID as subject_id ");
		s.append(" FROM de_subject_microarray_data a, de_mrna_annotation b ");
		s.append(" WHERE a.probeset_id = b.probeset_id AND a.trial_name IN (").append(trialNames).append(") ");
		s.append(" AND a.assay_id IN (").append(assayIds).append(")");
		
		if (pathwayName != null && pathwayName.length() > 0)	{
			s.append(" AND b.gene_id IN (").append(genes).append(")");
			}
			//log.debug(s.toString());
		return s.toString();
	}
	
	public void writeGpFiles()
	{
		if(dataType.toUpperCase()=="MRNA_AFFYMETRIX")
		{
			writeMrnaDataToFiles();
		} 
		else if(dataType.toUpperCase()=="RBM") 
		{
			writeDataToFile();
		} 
		else if(dataType.toUpperCase()=="PROTEIN") 
		{
			writeDataToFile();
		}
	}
	
	//****************************************************************
	//File Creation Methods.
	//****************************************************************
	private void writeMrnaDataToFiles()
	{
		//Write the file that has our group information.
		gpf.writeClsFileManySubsets(sampleIdList);
		
		gpf.openGctFile();
		gpf.openCSVFile();
		
		//Get pretty names for the subjects.
		
		//String[] subjectNameArray = getSubjectNameArray(distinctSubjectIds1, distinctSubjectIds2, "S1_",  "S2_");
		
		// handle *
		if (fixlast) {
			String[] newNameArray = new String[subjectNameArray.length + 1];
			newNameArray[subjectNameArray.length] = "*";
			System.arraycopy(subjectNameArray, 0, newNameArray, 0, subjectNameArray.length);
			subjectNameArray = newNameArray;
		}
		
		Integer rows = 0;
		def numCols =0
		
		//GCT File.
		StringBuilder s  = new StringBuilder("");
		//CSV File.
		StringBuilder cs = new StringBuilder("");
		
		//Temporary variable to hold some values we write to files.
		String sval = null;
		
		//Session for our SQL server.
		def session = sessionFactory.getCurrentSession();
		
		//Objects for Statement and Result set.
		Statement st = null;
		ResultSet rs = null;
		
		//This is our transaction object.
		def trans = null;
		
		try{
			//Start our transaction.
			trans = session.beginTransaction();
			
			//Get our connection as an object.
			def conn = session.connection();
			
			//Create statement object.
			st = conn.createStatement();
			
			//Enable Parallel processing.
			st.execute("alter session enable parallel dml");
			
			//Set the statement fetch size.
			st.setFetchSize(5000);
			
			//Execute our SQL statement.
			rs = st.executeQuery(experimentDataQuery);
			
			//Get the total number of columns in the record set.
			int totalCol = rs.getMetaData().getColumnCount();
			
			//Iterate over the records.
			while(rs.next())
			{
				//Reinitialize the length of the stringbuilder.
				cs.setLength(0);
				
				//Reset the temporary value.
				sval=null;
				
				//Loop over all the columns.
				for(int count = 1;count<totalCol; count++)
				{
					//If we are past the first record, append a record separator.
					if(count>1)
					{
						s.append("\t");
						cs.append(",");
					}
					
					//Get the value for the current column.
					sval = rs.getString(count);
					
					//Add either a value or a white space character if we don't have a value.
					if(sval!=null)
					{
						//Check for "null" string.
						if(sval.equals("null"))
						{
							s.append(whiteString);
							cs.append(whiteString);
						}
						else
						{
							s.append(sval);
							cs.append(sval);
						}
					}
					else
					{
						s.append(whiteString);
						cs.append(whiteString);
					}
				}

				//If we have to fixlast, add the characters here.
				if(fixlast)
				{
					s.append("\t").append("0");
					cs.append(",").append("0");
				}
				
				rows++;
				
				//GCT File gets newline character.
				s.append("\n");
				
				//Write this line to the CSV file.
				gpf.writeToCSVFile(cs.toString())
				
			}
		} 
		finally	
		{
			if (rs != null) rs.close()
			if (st != null)	st.close()
			trans.commit()
		}
		
		//Write the gct header and gct file contents.
		gpf.createGctHeader( rows, subjectNameArray, "\t");
		gpf.writeToGctFile(s.toString());

		gpf.closeGctFile();
		gpf.closeCSVFile();

	}

	public void writeDataToFile()
	{
		//Open the files we are going to write to.
		gpf.openGctFile();
		gpf.openCSVFile();
		
		//Write cls file
		gpf.writeClsFile(distinctSubjectIds1, distinctSubjectIds2);
		
		StringBuilder s  = new StringBuilder("");
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		def rowsObj = sql.rows(experimentDataQuery, numColumnsClosure)
		
		// create header
		gpf.createGctHeader( rowsObj.size(), subjectNameArray, "\t");
		
		rowsObj.each 
		{	
			row ->
			
			s.setLength(0);
			
			if(dataType.toUpperCase()=="PROTEIN")
			{
				if (row.getAt("component") == null) 
				{
					s.append(row.getAt("GENE_SYMBOL"));
				} 
				else 
				{
					s.append(row.getAt("component"));
				}
				
				s.append("\t" + row.getAt("GENE_SYMBOL"));
			}
			
			for (int count: startingOutputColumn ..< numCols-1) 
			{
				String val = row.getAt(count);
				if (val == "null" || val == null) val = whiteString;
				if(count>0)  s.append("\t");
				s.append(val);
			}
			
			gpf.writeToGctFile(s.toString()	);
			gpf.writeToCSVFile(s.toString().replaceAll("\t",","))
		}
		
		gpf.closeGctFile();
		gpf.closeCSVFile();
	}
	//****************************************************************
	
	def filterSubjectIdByBiomarkerData(ids)
	{
		if(ids==null || ids.length()==0)
			return ids;
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

		StringBuilder fids = new StringBuilder();
		StringBuilder sampleQ = new StringBuilder("SELECT distinct s.patient_id FROM de_subject_sample_mapping s WHERE s.patient_id IN (").append(ids).append(")");
		
		sql.eachRow(sampleQ.toString(),
				{row->
					String st = row.patient_id;
					if(fids.length()>0){
						fids.append(",");
					}
					fids.append(st);
				});
		return fids.toString();
	}

	
	   //****************************************************************
	   //Query Creation Methods.
	   //****************************************************************
	
	def String createMRNAHeatmapBaseQuery(
	String pathwayName,
	String ids1,
	String ids2,
	String concepts1,
	String concepts2,
	String timepoint1,
	String timepoint2,
	String sample1,
	String sample2,
	String intensityType) throws Exception{
		//log.debug("mRNA: called with ids1=" + ids1 + " and ids2=" + ids2);
		
		String columns = null;

		columns = listHeatmapColumns("probeset", ids1, ids2, "S1_", "S2_") + ", star"
		//log.debug("SELECT: " + columns)
		
		String s1;
		String s2;
		
		//Change this so we get the assay ID's before calling the pathway query thing.
		
		//Get the list of trial names based on
		//String trialNames  = getTrialName(ids);
		//String assayIds    = getAssayIds(ids, sampleTypes, timepoint);
		
		if (ids1 != null && ids1.length() > 0 )
		{
			s1 = createMRNAHeatmapPathwayQuery("S1_",ids1, concepts1, pathwayName, timepoint1, sample1, intensityType);
		}
				
		if (ids2 != null && ids2.length() > 0 )
		{
			s2 = createMRNAHeatmapPathwayQuery("S2_",ids2, concepts2, pathwayName, timepoint2, sample2, intensityType);
		}
		
		// we have to use the log2_intensity to make the analysiscontroller happy..
		String intensityColumn = "LOG2_INTENSITY";

		String subjects = getSubjectSelectColumns(ids1, ids2, "S1_", "S2_") + ", '*' as star";
		
		String r;
		if (s1 != null){
			if (s2 != null){
				
				r = "SELECT " + columns + " FROM (" +
						s1 + " UNION " + s2 +
						") PIVOT (avg("+intensityColumn+") for subject_id IN (" + subjects +
						")) ";
				if(!count) r = r+" ORDER BY PROBESET, GENE_SYMBOL";
			}
			else{
				r = "SELECT " + columns + " FROM (" + s1 +
						") PIVOT (avg("+intensityColumn+") for subject_id IN (" + subjects +
						"))";
				if(!count) r = r+" ORDER BY PROBESET, GENE_SYMBOL";
			}
		}else {
			r = "SELECT " + columns + " FROM (" + s2 +
					") PIVOT (avg("+intensityColumn+") for subject_id IN (" + subjects +
					"))";
			if(!count) r = r+" ORDER BY PROBESET, GENE_SYMBOL";
		}
		return r.toString();
	}
	
	def String createMRNAHeatmapPathwayQuery(String prefix, String assayIds, String intensityType) throws Exception
	{
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
				
		//if (assayIds.equals('')) throw new Exception("No heatmap data for the specified parameters.");
		
		String genes;
		if (pathwayName != null && pathwayName.length() > 0)	
		{
			genes = getGenes(pathwayName);
		}
		
		String intensityCol = "zscore";
		
		if("RAW"==intensityType)
		{
			intensityCol = "RAW_INTENSITY";
			
			//check if we have sufficient raw data to run gp query
			def goodPct
			String rawCountQuery="select DISTINCT /*+ parallel(de_subject_microarray_data,4) */ /*+ parallel(de_mrna_annotation,4) */ count(distinct a.raw_intensity)/count(*) as pct_good " +
					"FROM de_subject_microarray_data a, de_mrna_annotation b " +
					"WHERE a.probeset_id = b.probeset_id AND a.trial_name IN ("+trialNames+") " +
					"AND a.assay_id IN ("+assayIds+")";
					
			sql.eachRow (rawCountQuery,, {row-> goodPct=row[0]})
			
			if(goodPct==0) throw new Exception("No raw data for Comparative Marker Selection.");
		}
		
		// added hint here...
		StringBuilder s = new StringBuilder();
		s.append("select DISTINCT /*+ parallel(de_subject_microarray_data,4) */ /*+ parallel(de_mrna_annotation,4) */  b.PROBE_ID || ':' || b.GENE_SYMBOL as PROBESET, b.GENE_SYMBOL, "+"a."+intensityCol+" as LOG2_INTENSITY ");
		s.append(" , '").append(prefix).append("' || a.patient_ID as subject_id ");
		s.append(" FROM de_subject_microarray_data a, de_mrna_annotation b ");
		s.append(" WHERE a.probeset_id = b.probeset_id AND a.trial_name IN (").append(trialNames).append(") ");
		s.append(" AND a.assay_id IN (").append(assayIds).append(")");
		
		if (pathwayName != null && pathwayName.length() > 0)	{
			s.append(" AND b.gene_id IN (").append(genes).append(")");
			}
			//log.debug(s.toString());
			return s.toString();
	}
	
	
	
	
	
	def String createRBMHeatmapQuery(String prefix, String ids, String concepts, String pathwayName, String timepoint, String rbmPanels)
	{
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		StringBuilder s = new StringBuilder();
		String genes;
		
		//log.debug("Pathway: " + pathwayName)
		if (pathwayName != null && pathwayName.length() > 0 && "SHOWALLANALYTES".compareToIgnoreCase(pathwayName) != 0)	{
			genes = getGenes(pathwayName);
			//log.debug("Genes obtained for given pathway: " + genes)
		}
		
		if (timepoint == null || timepoint.length() == 0 ) {
			s.append("SELECT distinct t1.ANTIGEN_NAME, t1.GENE_SYMBOL, t1.zscore as value, '");
			s.append(prefix + "'|| t1.patient_id as subject_id ");
			s.append("FROM DE_SUBJECT_RBM_DATA t1, de_subject_sample_mapping t2 ");
			s.append("WHERE t1.patient_id = t2.patient_id and t1.patient_id IN (" + ids + ")");
		} else {
			s.append("SELECT distinct t1.ANTIGEN_NAME, t1.GENE_SYMBOL, t1.zscore as value, '");
			s.append(prefix + "'|| t1.patient_id as subject_id ");
			s.append("FROM DE_SUBJECT_RBM_DATA t1, de_subject_sample_mapping t2 ");
			s.append("WHERE ")
			s.append("t2.patient_id IN ("+ ids + ") and ");
			s.append("t2.timepoint_cd IN (" + quoteCSV(timepoint) + ") and ");
			s.append("t1.data_uid = t2.data_uid and t1.assay_id=t2.assay_id");
		}
		
		if (rbmPanels != null && rbmPanels.length()>0){
			s.append(" and t2.rbm_panel IN (" + quoteCSV(rbmPanels) + ")");
		}
		
		if (pathwayName != null && pathwayName.length() > 0 && "SHOWALLANALYTES".compareToIgnoreCase(pathwayName) != 0)	{
			s.append(" AND t1.gene_id IN (").append(genes).append(")");
			}
		
		//log.debug(s.toString());
		return s.toString();
	}
	
	
	def String createProteinHeatmapQuery(String prefix, String pathwayName,String ids, String concepts, String timepoint) 
	{
		
		//log.debug("createProteinHeatmapQuery called with concepts = " + concepts);
	
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		String cntQuery = "SELECT COUNT(*) as N FROM DE_SUBJECT_SAMPLE_MAPPING WHERE concept_code IN (" + quoteCSV(concepts) + ")";
	
		//log.debug("createProteinHeatmapQuery created cntQuery = " + cntQuery);
		
		Integer cnt;
		
		//log.debug("createProteinHeatmapQuery defined cnt = " + cntQuery);
	
	
		sql.query(cntQuery) 
		{ 
			ResultSet rs ->
			while (rs.next()) cnt = rs.toRowResult().N;
		}
		
		//log.debug("createProteinHeatmapQuery executed query to get count");
		
		//log.debug("createProteinHeatmapQuery cnt=" + cnt);
	
		StringBuilder s = new StringBuilder();
	
		if (cnt == 0) {
			if (timepoint != null && timepoint.length() > 0 ) {
				s.append("SELECT distinct a.component, a.GENE_SYMBOL, a.zscore, '");
				s.append(prefix + "' || a.patient_ID as subject_id ");
				s.append("FROM DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p, ");
				s.append("DE_subject_sample_mapping b ");
				s.append("WHERE c.pathway_id= p.id and ");
				if (pathwayName != null) {
					s.append(" p.pathway_uid='" + pathwayName + "' and ");
				}
				s.append("a.gene_symbol = c.gene_symbol and ");
				s.append("a.patient_id IN (" + ids + ") and ");
				s.append("b.TIMEPOINT_CD IN (" + quoteCSV(timepoint) + ") and ");
				s.append("a.PATIENT_ID=b.patient_id and a.timepoint=b.timepoint and ");
				s.append("a.assay_id=b.assay_id  ");
			} else {
				s.append("SELECT distinct a.component, a.GENE_SYMBOL, a.zscore, '");
				s.append(prefix + "' || a.patient_ID as subject_id ");
				s.append("FROM DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p ");
				s.append("WHERE c.pathway_id= p.id and ");
				if (pathwayName != null) {
					s.append(" p.pathway_uid='" + pathwayName + "' and ");
				}
				s.append("a.gene_symbol = c.gene_symbol and ");
				s.append("a.patient_id IN (" + ids + ")");
			}
		} else {
			if (timepoint != null && timepoint.length() > 0 ) {
				s.append("select distinct a.component, a.GENE_SYMBOL, a.zscore, '");
				s.append(prefix + "' || a.patient_ID as subject_id ");
				s.append("FROM DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p, ");
				s.append("DE_subject_sample_mapping b ");
				s.append("WHERE c.pathway_id= p.id and ");
				if (pathwayName != null) {
					s.append(" p.pathway_uid='" + pathwayName + "' and ");
				}
				s.append("a.gene_symbol = c.gene_symbol and ");
				s.append("a.PATIENT_ID = b.PATIENT_ID and a.assay_id = b.assay_id and ");
				s.append("b.concept_code IN (" + quoteCSV(concepts) + ") and ");
				s.append("a.patient_id IN (" + ids + ") and ");
				s.append("b.TIMEPOINT_CD IN (" + quoteCSV(timepoint) + ") and ");
				s.append("a.PATIENT_ID=b.patient_id and a.timepoint=b.timepoint ");
			}  else {
				s.append("select distinct a.component, a.GENE_SYMBOL, a.zscore, '");
				s.append(prefix + "' || a.patient_ID as subject_id ");
				s.append("FROM DE_SUBJECT_PROTEIN_DATA a, DE_pathway_gene c, de_pathway p, ");
				s.append("DE_subject_sample_mapping b ");
				s.append("WHERE c.pathway_id= p.id and ");
				if (pathwayName != null) {
					s.append(" p.pathway_uid='" + pathwayName + "' and ");
				}
				s.append("a.gene_symbol = c.gene_symbol and ");
				s.append("a.PATIENT_ID = b.PATIENT_ID and a.assay_id = b.assay_id and ");
				s.append("b.concept_code IN (" + quoteCSV(concepts) + ") and ");
				s.append("a.patient_id IN (" + ids + ")");
			}
		}
	//log.debug("createProteinHeatmapQuery complete:" + s.toString() );
	// log.debug(s.toString());
		return s.toString();
	}
	
	   def String createProteinHeatmapQuery(String pathwayName,String ids1, String ids2,String concepts1, String concepts2,String timepoint1, String timepoint2)
	   {
		   //log.debug("Protein: called with ids1=" + ids1 + " and ids2=" + ids2);
	   
		   String columns = listHeatmapColumns("component", ids1, ids2, "S1_", "S2_") + ", star"
		   
		   //log.debug("Protein SELECT: " + columns)
		   
		   String s1;
		   String s2;
		   
		   if (ids1 != null && ids1.length() > 0 )
		   {
			   s1 = createProteinHeatmapQuery("S1_", pathwayName, ids1, concepts1, timepoint1);
		   }
		   
		   if (ids2 != null && ids2.length() > 0 )
		   {
			   s2 = createProteinHeatmapQuery("S2_", pathwayName, ids2, concepts2, timepoint2);
		   }
		   
		   String subjects = getSubjectSelectColumns(ids1, ids2, "S1_", "S2_") + ", '*' as star";
		   
		   //log.debug("Protein Pivot: " + subjects)
		   
		   String r;
		   if (s1 != null)
			   if (s2 != null)
				   r = "SELECT " + columns + " FROM (" + s1.replace("distinct ", " ") + " UNION " + s2.replace("distinct ", " ") + ") PIVOT (avg(zscore) for subject_id IN (" + subjects + ")) ORDER BY component, GENE_SYMBOL";
			   else
				   r = "SELECT " + columns + " FROM (" + s1 + ") PIVOT (avg(zscore) for subject_id IN (" + subjects + ")) ORDER BY component, GENE_SYMBOL";
		   else
			   r = "SELECT " + columns + " FROM (" + s2 + ") PIVOT (avg(zscore) for subject_id IN (" + subjects + ")) ORDER BY component, GENE_SYMBOL";
		   return r;

	   }
	   
	   
	   
	   def String createRBMHeatmapQuery(String pathwayName, String ids1, String ids2,  String concepts1, String concepts2,	   String timepoint1, String timepoint2, String rbmPanels1, String rbmPanels2)
	   {
		   //log.debug("RBM: called with ids1=" + ids1 + " and ids2=" + ids2);
		   
		   String columns = listHeatmapColumns("antigen_name", ids1, ids2, "S1_", "S2_") + ", star"
		   
		   //log.debug("SELECT: " + columns)
		   
		   String s1;
		   String s2;
		   
		   if (ids1 != null && ids1.length() > 0 )
			   s1 = createRBMHeatmapQuery("S1_", ids1, concepts1, pathwayName, timepoint1, rbmPanels1);
		   
		   if (ids2 != null && ids2.length() > 0 )
			   s2 = createRBMHeatmapQuery("S2_", ids2, concepts2, pathwayName, timepoint2, rbmPanels2);
		
		   String subjects = getSubjectSelectColumns(ids1, ids2, "S1_", "S2_") + ", '*' as star";
		   
		   //log.debug("RBM: " + subjects)
		   
		   String r;
		   if (s1 != null)
			   if (s2 != null)
				   r = "SELECT " + columns + " FROM (" + s1.replace("distinct ", " ") + " UNION " + s2.replace("distinct ", " ") + ") PIVOT (avg(value) for subject_id IN (" + subjects + ")) ORDER BY ANTIGEN_NAME, GENE_SYMBOL";
		   else
			   r = "SELECT " + columns + " FROM (" + s1 + ") PIVOT (avg(value) for subject_id IN (" + subjects + ")) ORDER BY ANTIGEN_NAME, GENE_SYMBOL";
		   else
			   r = "SELECT " + columns + " FROM (" + s2 + ") PIVOT (avg(value) for subject_id IN (" + subjects + ")) ORDER BY ANTIGEN_NAME, GENE_SYMBOL";
		   return r;
	   }
	   
	   /**
		* default log2 heatmap query
		* @param pathwayName
		* @param ids1
		* @param ids2
		* @param concepts1
		* @param concepts2
		* @param timepoint1
		* @param timepoint2
		* @return
		*/
	   def String createMRNAHeatmapQuery(
	   String pathwayName,
	   String ids1,
	   String ids2,
	   String concepts1,
	   String concepts2,
	   String timepoint1,
	   String timepoint2){
		   
		   return createMRNAHeatmapBaseQuery(pathwayName, ids1, ids2, concepts1, concepts2, timepoint1, timepoint2, "LOG2");
	   }
	   //****************************************************************
	   
	   
	   
	   
	   //****************************************************************
	   //Helper functions.
	   //****************************************************************
	   
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
	  def String listHeatmapColumns(String biomarker, String ids1, String ids2, String prefix1, String prefix2)
	  {
		  
		  StringBuilder s = new StringBuilder();
		  s.append(" " + biomarker + ", gene_symbol ")
	  
		  if((ids1!= null) && (ids1.length()>0))
		  {
			  def idArray = ids1.split(",")
			  for(id in idArray) s.append (", round(" + prefix1 + id + ", 4) as " + prefix1 + id)
		  }
	  
		  if((ids2 != null) &&(ids2.length()>0))
		  {
			  def idArray = ids2.split(",")
			  for(id in idArray) s.append (", round(" + prefix2 + id + ", 4) as " + prefix2 + id)
		  }
		  
		  return s
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
	 }
	  
	  
	  def String getSubjectSelectColumns(String ids1, String ids2, String prefix1, String prefix2)
	  {
		  StringBuilder s = new StringBuilder();
		  
		  if (ids1 != null && ids1.length() > 0)
		  {
			  def idArray = ids1.split(",");
			  
			  for(id in idArray) s.append("'" + prefix1 + id + "' as " + prefix1 + id + ",");
		  }
	  
		  if (ids2 != null && ids2.length() > 0)
		  {
			  def idArray = ids2.split(",");
			  for(id in idArray) s.append("'" + prefix2 + id + "' as " + prefix2 + id + ",");
		  }
		  
		  return s.substring(0, s.length()-1);
	  }
	   
	   def String getAssayIds(String ids, String sampleTypes, String timepoint) 
	   {
		   groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		   
		   StringBuilder assayS = new StringBuilder("select distinct s.assay_id  from de_subject_sample_mapping s where s.patient_id in (").append(ids).append(")");
		   // check sample type
		   if(sampleTypes!=null && sampleTypes.length()>0)
		   {
			   assayS.append(" AND s.sample_type_cd IN ").append(convertStringToken(sampleTypes));
		   }
		   
		   if(timepoint!=null && timepoint.trim().length()>0)
		   {
			   assayS.append(" AND s.timepoint_cd IN ").append(convertStringToken(timepoint));
		   }
		   
		   assayS.append (" ORDER BY s.assay_id");
		   
		   //log.debug("getAssayIds used this query: " + assayS.toString());
		   
		   def assayIdsArray =[];
		   sql.eachRow(assayS.toString(), 
			   {
				   row->
				   
				   if(row.assay_id!=null)
				   {
					   assayIdsArray.add(row.assay_id)
				   }
			   }
		   );
	   
		   String assayIds = convertList(assayIdsArray, false, 1000);
		   
		   return assayIds;
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
	 
	/**
	*
	*/
	def convertStringToken(String t) 
	{
		String[] ts = t.split(",");
		StringBuilder s = new StringBuilder("(");
		
		for(int i=0; i<ts.length;i++)
		{
			if(i>0)	s.append(",");
			
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
   def convertList(idList, boolean isString, int max) 
   {
	   StringBuilder s = new StringBuilder();
	   
	   int i = 0;
	   
	   for(id in idList)
	   {
		   if(i<max)
		   {
			   if(s.length()>0)
			   {
				   s.append(",");
			   }
			   
			   if(isString)
			   {
				   s.append("'");
			   }
			   
			   s.append(id);
			   
			   if(isString)
			   {
				   s.append("'");
			   }
		   }
		   else
		   {
			   break;
		   }
		   
		   i++;
	   }
	   return s.toString();
   }
	

   
   
   /**
   * Go to the i2b2DemoData.sample_categories table and gather the trial names for the list of sample IDs.
   * @param ids
   * @return
   */
  def String getTrialNameBySampleID(String ids) 
  {
	  
	  //Create a SQL object.
	  groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
	  
	  //Build the query to get the trial names.
	  StringBuilder trialQ = new StringBuilder("select distinct s.trial_name from i2b2DemoData.sample_categories s ");
	  trialQ.append(" where s.SAMPLE_ID in (").append(quoteCSV(ids)).append(")");
	  
	  //Log the trial query.
	  //log.debug("getTrialNameBySampleID used this query: " + trialQ.toString());
	  
	  //This will be the list of trial names.
	  String trialNames = "";
	  
	  //For each of the retrieved SQL records, add the trial name to the list.
	  sql.eachRow(trialQ.toString(),
		  {
			  row ->
	  
			  //If we have multiple trial Names, make them comma delimited.
			  if(trialNames.length()>0) trialNames+=",";
			  
			  //Get the trial name from the SQL record object.
			  String tName = row.trial_name;
			  
			  //These are some hardcoded study names.
			  if (tName.equalsIgnoreCase("BRC Antidepressant Study") )
			  {
				  tName ="BRC:mRNA:ADS";
			  }
			  
			  if (tName.equalsIgnoreCase("BRC Depression Study"))
			  {
				  tName = "BRC:mRNA:DS";
			  }
			  
			  //Add the trial name to our string.
			  trialNames +="'"+tName+"'";
		  }
	  );
	  return trialNames;
  }
   
	  /* It is more meaningful to the scientists to use subject name such as S1_GSE19539_IC022 in the heatmap, and to be consistent with genomic data */
	  String[] getSubjectNameArray(String ids1, String ids2, String prefix1, String prefix2)
	  {
		  List<String> nameList = null;
		  if (ids1 != null && ids1.length() > 0) {
			  List<String> nameList1 = getSubjectNameList(ids1, prefix1);
			  if (nameList1 != null && nameList1.size() != 0)
				  nameList = nameList1;
		  }
		  if (ids2 != null && ids2.length() > 0) {
			  List<String> nameList2 = getSubjectNameList(ids2, prefix2);
			  if (nameList2 != null && nameList2.size() != 0) {
				  if (nameList != null && nameList.size() != 0) {
					  for (String name : nameList2) {
						  nameList.add(name);
					  }
				  }
				  else {
					  nameList = nameList2;
				  }
			  }
		  }
		  String[] ids = null;
		  if (nameList != null && nameList.size() != 0) {
			  ids = new String[nameList.size()];
			  for (int i = 0; i < nameList.size(); i ++) {
				  ids[i] = nameList.get(i);
			  }
		  }
		  return ids;
	  }
  
	  def String getTrialName(String ids)
	  {
		  
		  groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		  
		  StringBuilder trialQ = new StringBuilder("select distinct s.trial_name from de_subject_sample_mapping s ");
		  trialQ.append(" where s.patient_id in (").append(ids).append(") and s.platform = 'MRNA_AFFYMETRIX'");
		  
		  //log.debug("getTrialName used this query: " + trialQ.toString());
		  
		  String trialNames = "";
		  sql.eachRow(trialQ.toString(), {row ->
			  if(trialNames.length()>0){
				  trialNames+=",";
			  }
			  String tName = row.trial_name;
			  if (tName.equalsIgnoreCase("BRC Antidepressant Study") ){
				  tName ="BRC:mRNA:ADS";
			  }
			  if (tName.equalsIgnoreCase("BRC Depression Study")){
				  tName = "BRC:mRNA:DS";
			  }
			  trialNames +="'"+tName+"'";
		  }
		  );
		  return trialNames;
	  }
  
	  List<String> getSubjectNameList(String ids, String prefix)
	  {
		  List<String> nameList = new ArrayList<String>();
		  
		  groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		  
		  String sqlStr = "SELECT sourcesystem_cd, patient_num FROM patient_dimension WHERE patient_num IN (" +
				  ids + ") order by patient_num";
		  
		  sql.eachRow(sqlStr) { row ->
			  String sourceSystemCd = row.sourcesystem_cd;
			  Long patientNum = row.patient_num;
			  if (sourceSystemCd != null && sourceSystemCd.length() != 0) {
				  nameList.add(prefix + sourceSystemCd);
			  }
			  else {
				  nameList.add(prefix + patientNum.toString());
			  }
		  }
		  return nameList;
	  }
  
	  def String getGenes (String pathwayName)
	  {
		  
		  groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		  
		  // build pathway sub query and get gene symbol list
		  // gene sig or gene list
		  StringBuilder pathwayS = new StringBuilder();
		  if(pathwayName.startsWith("GENESIG")||pathwayName.startsWith("GENELIST")){
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
		  
		  def genesArray =[];
		  sql.eachRow(pathwayS.toString(), {row->
			  if(row.gene_id!=null){
				  genesArray.add(row.gene_id);
			  }
		  }
		  );
		  
		  String genes = convertList(genesArray, false, 1000);
		  return genes;
	 }
   

	  void getSampleNameListFromIds(sampleIds, String prefix, List<String> sampleNameList) 
	  {
		  if (sampleIds == null || sampleIds.size() == 0 || sampleInfoMap == null || sampleInfoMap.size() == 0 || sampleNameList == null )
			  return;
		  
		  //String[] sampleIdStrArray = sampleIds.split(",");
		  sampleIds.each
		  {
			  sampleIdStr ->
			  
			  SampleInfo sampleInfo = sampleInfoMap.get(sampleIdStr);
			  String sampleName = sampleInfo.sampleName;
			  if (prefix != null && prefix.length() != 0)
				  sampleName = prefix + sampleName;
			  sampleNameList.add(sampleName);
		  }
	  }
	//****************************************************************
	  
}
