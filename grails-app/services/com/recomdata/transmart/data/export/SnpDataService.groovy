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
import java.sql.Clob
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap
import java.util.List
import java.util.Map

import org.apache.commons.lang.StringUtils
import org.apache.commons.logging.LogFactory
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection

import org.transmart.searchapp.SearchKeyword;

import com.recomdata.snp.SnpDataObject;
import com.recomdata.transmart.data.export.util.FileWriterUtil

class SnpDataService {

    boolean transactional = false

	def dataSource
	def i2b2HelperService
	def snpService
	def springSecurityService
	def plinkService
	def fileDownloadService
	def utilService
	def grailsApplication
	def config = ConfigurationHolder.config
	
	def Map getData(studyDir, fileName, jobName, resultInstanceId) {
		def snpFilesMap = [:]
		snpFilesMap.put("PEDFiles", writePEDFiles(studyDir, fileName, jobName, resultInstanceId))

		snpFilesMap.put("MAPFiles", writeMAPFiles(studyDir, fileName, jobName, resultInstanceId))
		return snpFilesMap
	}
	
	def private getPatientId(subjectId) {
		def groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		def patientIdQuery = "SELECT SOURCESYSTEM_CD FROM PATIENT_DIMENSION WHERE PATIENT_NUM = ?"
		def firstRow = sql.firstRow(patientIdQuery, [subjectId])
		def patientId = utilService.getActualPatientId(firstRow?.SOURCESYSTEM_CD)
		
		return patientId
	}
	
	def class PatientData {
		def patientId, omicPatientId, subjectId
	} 
	
	def public getPatientData(resultInstanceId) {
		def groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		def query = """
						SELECT DISTINCT patient_id, omic_patient_id, subject_id
						FROM de_subject_sample_mapping 
						WHERE platform = 'SNP' and patient_id in (
							SELECT DISTINCT patient_num 
							FROM qt_patient_set_collection 
							WHERE result_instance_id = CAST(? AS numeric))
					"""
		def patientDataMap = [:]
		sql.eachRow(query, [resultInstanceId]) { row ->
			PatientData patientData = new PatientData()
			patientData.patientId = row.PATIENT_ID
			patientData.omicPatientId = row.OMIC_PATIENT_ID
			patientData.subjectId = row.SUBJECT_ID
			patientDataMap.put(row.PATIENT_ID, patientData)
		}
		
		return patientDataMap
	}

	def public getDataByPatientByProbes(studyDir, resultInstanceId, jobName)
	{
		def dataTypeName = 'SNP'
		def dataTypeFolder = "Processed_data"
		char separator = '\t'
		def flushCount = 0
		def flushInterval = 5000
		def groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

		def subjectIds = i2b2HelperService.getSubjectsAsList(resultInstanceId)
		/**
		 * Input params required for the R script are parentDir and subjectIdsStr
		 */
		def parentDir = null
		def subjectIdsStr = i2b2HelperService.getSubjects(resultInstanceId)
		
		def patientDataMap = getPatientData(resultInstanceId)
		//Create objects we use to form JDBC connection.
		def Connection conn = null;
		def PreparedStatement pStmt = null;
		def ResultSet rs = null;
		
		//Grab the connection from the grails object.
		conn = dataSource.getConnection()
		
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
		
		log.debug("Starting the long query to get cnv file information")

		subjectIds.each { subjectId ->
			def PatientData patientData = patientDataMap.get(subjectId)
			
			/**
			 * Prepare the query to extract the records for this subject 
			 */
			def query = """
							SELECT dscg.gsm_num, dscg.patient_num, dscg.snp_calls, dscg.snp_name, dscn.copy_number
							FROM de_snp_calls_by_gsm dscg
							LEFT JOIN (SELECT patient_num, snp_name, copy_number 
							      FROM de_snp_copy_number 
							      WHERE patient_num = ?) dscn ON dscg.patient_num = dscn.patient_num AND dscg.snp_name = dscn.snp_name
							WHERE dscg.patient_num = ?
						"""
			
			def filename = subjectId + '.CNV'
			def FileWriterUtil writerUtil = new FileWriterUtil(studyDir, filename, jobName, dataTypeName, dataTypeFolder, separator)
			def output = writerUtil.outputFile.newWriter(true)

			output << 'SAMPLE\tPATIENT ID\tPROBE ID\tCOPY NUMBER\n'
			
			//Prepare the SQL statement.
			pStmt = conn.prepareStatement(query)
			pStmt.setBigDecimal(1, patientData?.omicPatientId)
			pStmt.setBigDecimal(2, patientData?.omicPatientId)
			pStmt.setFetchSize(fetchSize)
			
			rs = pStmt.executeQuery()
			while(rs?.next()) {
				output.write(StringUtils.isNotEmpty(rs?.getString("GSM_NUM")) ? rs?.getString("GSM_NUM") : "")
				output.write(separator)
				output.write(StringUtils.isNotEmpty(patientData?.subjectId) ? patientData?.subjectId : "")
				output.write(separator)
				output.write(StringUtils.isNotEmpty(rs?.getString("SNP_NAME")) ? rs?.getString("SNP_NAME") : "")
				output.write(separator)
				output.write(StringUtils.isNotEmpty(rs?.getString("COPY_NUMBER")) ? rs?.getString("COPY_NUMBER") : "")
				output.newLine()
				
				flushCount++;
				if(flushCount>=flushInterval) {
					output.flush();
					flushCount = 0;
				}
			}
			
			parentDir = writerUtil.outputFile.parent
			output?.flush()
			output?.close()
		}
		
		log.debug("Finished the long query to get cnv file information")
		log.debug("Starting the query to get platform")
		
		String platformQuery = """
		SELECT dgi.title FROM de_subject_snp_dataset ssd
		INNER JOIN de_gpl_info dgi on dgi.platform=ssd.platform_name
		INNER JOIN de_subject_sample_mapping dssm on ssd.patient_num=dssm.omic_patient_id
		WHERE dssm.patient_id IN (SELECT DISTINCT patient_num
							FROM qt_patient_set_collection 
							WHERE result_instance_id = CAST(? AS numeric))"""
		
		def firstRow = sql.firstRow(platformQuery, [resultInstanceId])
		def platformName = firstRow.title
		if (StringUtils.isEmpty(platformName)) platformName = 'Output'
		
		log.debug("Finished the query to get platform")
		
		/**
		 * R script invocation starts here
		 */
		log.debug("Invoking R for transformations")
		RConnection c = new RConnection()
		//Set the working directory to be our temporary location.
		String workingDirectoryCommand = "setwd('${parentDir}')".replace("\\","\\\\")
		//Run the R command to set the working directory to our temp directory.
		REXP x = c.eval(workingDirectoryCommand)

		String rScriptDirectory = config.com.recomdata.transmart.data.export.rScriptDirectory
		String compilePivotDataCommand = "source('${rScriptDirectory}/PivotData/PivotSNPCNVData.R')"
		REXP comp = c.eval(compilePivotDataCommand)
		//Prepare command to call the PivotSNPCNVData.R script
		parentDir = parentDir.replace("\\","\\\\")
		String pivotDataCommand = "PivotSNPCNVData.pivot('$subjectIdsStr', ',', '$parentDir', '$platformName')"
		//Run the R command to pivot the data in the clinical.i2b2trans file.
		REXP pivot = c.eval(pivotDataCommand)
		log.debug("Finished R transformations")
	}
	
	def private Map writeMAPFiles(studyDir, fileName, jobName, resultInstanceId) {
		def groovy.sql.Sql sql = null
		def FileWriterUtil writerUtil = null
		def output = null
		def buffer = new byte[1000]
		try {
			//def subjectIds = i2b2HelperService.getSubjects(resultInstanceId)
			
			def platform = plinkService.getStudyInfoByResultInstanceId(resultInstanceId)[0];
			if (StringUtils.isNotEmpty(platform)) {
				String query = """ SELECT probe_def FROM de_snp_probe_sorted_def
				WHERE chrom != 'ALL' and probe_def is not null and platform_name=?""";
				
				sql = new groovy.sql.Sql(dataSource);
	
				def snpMapDataRows = []
				sql.eachRow(query?.toString(), [platform]) { row ->
					snpMapDataRows.add(['PROBE_DEF':(java.sql.Clob) row.PROBE_DEF])
				}
				//Since the file write takes a lot of time we close the connection once we have all the data for this patient
				sql?.close()
				if (snpMapDataRows?.size() > 0) {
					def dataTypeName = 'SNP'
					def dataTypeFolder = "Processed_data"
					char separator = '\t'
					def snpFileName = 'SNPData.MAP'
					
					writerUtil = new FileWriterUtil(studyDir, snpFileName, jobName, dataTypeName, dataTypeFolder, separator);
					snpMapDataRows.each { row ->
						java.sql.Clob clob = (java.sql.Clob) row.PROBE_DEF;
						// change probe_def format from "SNP  chr  position" to "chr  SNP position"
						clob.getAsciiStream().getText().eachLine {
							def items = it.split()
							if (null != items && items?.length == 3) writerUtil?.writeLine([items[1], items[0], items[2]] as String[])
						}
					}
				}
			}
		} catch (Exception e) {
			log.info("Potential issue while exporting map file")
			log.info(e.getMessage())
		}
		finally {
			sql?.close()
			writerUtil?.finishWriting()
		}
	}

	def private Map writePEDFiles(studyDir, fileName, jobName, resultInstanceId) {
		def subjectIds = i2b2HelperService.getSubjectsAsList(resultInstanceId)
		def patientConceptCdPEDFileMap = [:]
		def groovy.sql.Sql sql = null
		def FileWriterUtil writerUtil = null
		def buffer = new byte[1000]
		def snpDataRows = null

		subjectIds.each { subjectId ->
			def snpDataBySampleQry = """SELECT t1.PATIENT_NUM, t1.CHROM, t1.PED_BY_PATIENT_CHR, 
												case t2.PATIENT_GENDER
													 when 'M' then 1
													 when 'F' then 2
													 else 0
												 end as PATIENT_GENDER, t2.CONCEPT_CD, t2.SUBJECT_ID 
											FROM DE_SNP_DATA_BY_PATIENT t1 
											INNER JOIN(SELECT DISTINCT PATIENT_NUM, TRIAL_NAME, PATIENT_GENDER, CONCEPT_CD, SUBJECT_ID FROM DE_SUBJECT_SNP_DATASET) t2 on t1.patient_num=t2.patient_num
                      						INNER JOIN DE_SUBJECT_SAMPLE_MAPPING t3 on t1.patient_num=t3.omic_patient_id
                      						WHERE t1.TRIAL_NAME=t2.TRIAL_NAME
                        						and t1.CHROM != 'ALL'
                        						and t1.PED_BY_PATIENT_CHR is not null 
                        						and t3.patient_id = ?
                        						and t3.platform='SNP'
											ORDER BY t1.PATIENT_NUM, t2.CONCEPT_CD, t2.SUBJECT_ID
									"""
			try {
				sql = new groovy.sql.Sql(dataSource);

				def dataTypeName = "SNP"
				snpDataRows = []
				sql.eachRow(snpDataBySampleQry?.toString(), [subjectId]) { row ->
					snpDataRows.add(['FAMILY_ID':row.SUBJECT_ID?.toString(),
								'PATIENT_NUM':row.PATIENT_NUM?.toString(),
								'CHROM':row.CHROM?.toString(),
								'PED_BY_PATIENT_CHR':(Clob) row.PED_BY_PATIENT_CHR,
								'PATIENT_GENDER':row.PATIENT_GENDER?.toString(),
								'CONCEPT_CD':row.CONCEPT_CD?.toString()])
				}
				//Since the file write takes a lot of time we close the connection once we have all the data for this patient
				sql?.close()
				
				if (snpDataRows?.size() > 0) {
					String familyId = null
					String patientNum = null
					//String chromosome = null
					Clob pedByPatientChrClob = null
					String patientGender = null
					String conceptCd = null
	
					def patientId = getPatientId(subjectId)
					def dataTypeFolder = "Processed_data"
					char separator = '\t'
					def snpFileName = 'SNPData_'+patientId+'.PED'
					writerUtil = new FileWriterUtil(studyDir, snpFileName, jobName, dataTypeName, dataTypeFolder, separator);
					writerUtil?.writeLine([
						'FAMILY ID',
						'PATIENT ID',
						'GENDER',
						'MATERNAL ID',
						'PATERNAL ID',
						'CHROMOSOME DATA']
					as String[])
					
					snpDataRows.each { row ->
						familyId = row.FAMILY_ID?.toString()
						patientNum = row.PATIENT_NUM?.toString()
						//chromosome = row.CHROM?.toString()
						pedByPatientChrClob = (Clob) row.PED_BY_PATIENT_CHR
						patientGender = row.PATIENT_GENDER?.toString()
						conceptCd = row.CONCEPT_CD?.toString()
	
						//store the map between patient_conceptcd and the file created for it
						if (null == patientConceptCdPEDFileMap.get(patientNum+'_'+conceptCd)) {
							patientConceptCdPEDFileMap.put(patientNum+'_'+conceptCd, writerUtil?.outputFile?.name)
						}
	
						def strVal = writerUtil?.getClobAsString(pedByPatientChrClob)
						writerUtil?.writeLine([
							familyId,
							patientId,
							patientGender,
							'0',
							'0',
							strVal]
						as String[])
	
						//Clean-up the objects and re-use them in the next iteration
						familyId = null
						patientNum = null
						//chromosome = null
						pedByPatientChrClob = null
						patientGender = null
						conceptCd = null
					}
				}
			} finally {
				//Close existing file and flush out the contents
				writerUtil?.finishWriting()
				snpDataRows = null
			}
		}

		return patientConceptCdPEDFileMap
	}

	def private String getSubjects(result_instance_ids) {
		def Set subjectsSet = new HashSet()

		//Get all the patient_ids/subjects
		result_instance_ids.each { resultInstance ->
			subjectsSet.addAll(i2b2HelperService.getSubjectsAsList(resultInstance.value))
		}

		def subjectsStr = new StringBuilder();
		subjectsSet.each { subject -> subjectsStr.append(subject).append(',') }
		def subjectIds = (subjectsStr.toString() != '') ? subjectsStr.toString()[0..-2] : '';

		return subjectIds
	}

	def private List getSubjectsAsList(result_instance_ids) {
		def Set subjectsSet = new HashSet()

		//Get all the patient_ids/subjects
		result_instance_ids.each { resultInstance ->
			subjectsSet.addAll(i2b2HelperService.getSubjectsAsList(resultInstance.value))
		}

		return subjectsSet?.toList()
	}

	def private getDataForMAPFile(String fileName, String jobName, HashMap result_instance_ids) {
		def subjectIds = getSubjects(result_instance_ids)
		// 0 -- Platform Name   1 -- Trial Name
		def platform = plinkService.getStudyInfoBySubject(subjectIds)[0];
		//writeData(fileName, jobName, cached)
	}
	
	private void downloadCELFiles(studyList, File studyDir, resultInstanceId, String jobName) {
		String studies=convertList(studyList, false, 1000)
		
		def groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		
		def query = """
			SELECT b.* 
			FROM bio_content b
			WHERE b.study_name in ${studies}
			AND b.file_name IN (SELECT DISTINCT sg.gsm_num FROM de_snp_calls_by_gsm sg WHERE sg.patient_num IN (
			    SELECT DISTINCT patient_num FROM qt_patient_set_collection WHERE result_instance_id = CAST(? AS numeric)
					 AND patient_num IN (SELECT patient_num FROM patient_dimension WHERE sourcesystem_cd NOT LIKE '%:S:%')))
		"""
		
		def celFiles = sql.rows(query, [resultInstanceId])
		def filesList = []
		for (file in celFiles) {
			def fileURL = (new StringBuffer(file.CEL_LOCATION).append(file.FILE_NAME).append(file.CEL_FILE_SUFFIX)).toString()
			filesList.add(fileURL)
		}
		
		File snpDir = FileWriterUtil.createDir(studyDir, 'SNP')
		File rawDataDir = FileWriterUtil.createDir(snpDir, 'Raw_data')
		
		fileDownloadService.getFiles(filesList, rawDataDir.getPath())
	}
	
	//This tells us whether we need to include the pathway information or not.
	private Boolean includePathwayInfo = false
	
	def getSnpDataByResultInstanceAndGene(resultInstanceId,study,pathway,sampleType,timepoint,tissueType,rowProcessor,fileLocation,genotype,copyNumber)
	{
		//This boolean tells us whether we retrieved data or not.
		Boolean retrievedData = false;
		
		//SQL Object to gather data.
		def groovy.sql.Sql sql = null
		sql = new groovy.sql.Sql(dataSource);
		
		//Get the pathway to use the uniqueid.
		pathway = derivePathwayName(pathway)
		
		//String studies = convertList(studyList, true, 1000)
		
		//These will be the two parts of the SQL statement. This SQL gets our SNP data by probe. We'll need to extract the actual genotypes/copynumber later.
		StringBuilder sSelect = new StringBuilder()
		StringBuilder sTables = new StringBuilder()
		
		sSelect.append("""
						SELECT  SNP.SNP_NAME AS SNP,
						DSM.PATIENT_ID, DSM.SUBJECT_ID, 
						bm.BIO_MARKER_NAME AS GENE,
						DSM.sample_type,
						DSM.timepoint,
						DSM.tissue_type,
						SNP.SNP_CALLS AS GENOTYPE,
						SNP.COPY_NUMBER AS COPYNUMBER,
						PD.sourcesystem_cd,
						DSM.GPL_ID
					""")
		
		//This from statement needs to be in all selects.
		sTables.append(""" 	FROM DE_SUBJECT_SAMPLE_MAPPING DSM
							INNER JOIN patient_dimension PD ON DSM.patient_id = PD.patient_num 
							INNER JOIN qt_patient_set_collection qt ON qt.result_instance_id = CAST(? AS numeric) AND qt.PATIENT_NUM = DSM.PATIENT_ID
							INNER JOIN DE_SAMPLE_SNP_DATA SNP ON DSM.SAMPLE_CD = SNP.SAMPLE_ID
							INNER JOIN DE_SNP_GENE_MAP D2 ON D2.SNP_NAME = SNP.SNP_NAME
							INNER JOIN bio_marker bm ON bm.PRIMARY_EXTERNAL_ID = to_char(D2.ENTREZ_GENE_ID)
						""")

		//If a list of genes was entered, look up the gene ids and add them to the query. If a gene signature or list was supplied then we modify the query to join on the tables that link the list to the gene ids.
		if (pathway != null && pathway.length() > 0 && !(pathway.startsWith("GENESIG") || pathway.startsWith("GENELIST")))
		{
			String genes;
			//Get the list of gene ids based on the search ids.
			//genes = getGenes(pathway);
			
			sSelect.append(",sk.SEARCH_KEYWORD_ID ")
			
			//Include the tables we join on to get the unique_id.
			sTables.append("""
				INNER JOIN bio_marker_correl_mv sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id
				INNER JOIN search_keyword sk ON sk.bio_data_id = sbm.bio_marker_id
			""")
			
			sTables.append(" WHERE DSM.trial_name = ?")
			sTables.append(" AND sk.unique_id IN ").append(convertStringToken(pathway)).append(" ");
			
			includePathwayInfo = true
		}
		else if(pathway.startsWith("GENESIG") || pathway.startsWith("GENELIST"))
		{
			//If we are querying by a pathway, we need to include that id in the final output.
			sSelect.append(",sk.SEARCH_KEYWORD_ID ")
			
			//Include the tables we join on to filter by the pathway.
			sTables.append("""
				INNER JOIN search_bio_mkr_correl_fast_mv sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id
				INNER JOIN search_keyword sk ON sk.bio_data_id = sbm.domain_object_id
			""")

			//Include the normal filter.
			sTables.append(" WHERE DSM.trial_name = ?")
			sTables.append(" AND sk.unique_id IN ").append(convertStringToken(pathway)).append(" ");
			
			includePathwayInfo = true
		}
		else
		{
			sTables.append(" WHERE DSM.trial_name = ?")
		}
		
		 //If we have a sample type, append it to the query.
		 if(sampleType!=null && sampleType.length()>0)
		 {
			 sTables.append(" AND DSM.sample_type_cd IN ").append(convertStringToken(sampleType));
		 }
	
		 //If we have timepoints, append it to the query.
		 if(timepoint!=null && timepoint.trim().length()>0){
			 sTables.append(" AND DSM.timepoint_cd IN ").append(convertStringToken(timepoint));
		 }
	
		 //If we have tissues, append it to the query.
		 if(tissueType!=null && tissueType.trim().length()>0){
			 sTables.append(" AND DSM.tissue_type_cd IN ").append(convertStringToken(tissueType));
		 }
		
		sSelect.append(sTables.toString())
		
		log.debug("SNP Query : " + sSelect.toString())
		
		//Create objects we use to form JDBC connection.
		def Connection conn = null;
		def PreparedStatement pStmt = null;
		def ResultSet rs = null;
		
		//Grab the connection from the grails object.
		conn = dataSource.getConnection()
		
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
		pStmt = conn.prepareStatement(sSelect.toString())
		pStmt.setString(1, resultInstanceId)
		pStmt.setString(2, study)
		pStmt.setFetchSize(fetchSize)
		
		rs = pStmt.executeQuery()
		//while(rs?.next()) {
		//Create our output file.
		new File(fileLocation).withWriterAppend { out ->

			//Write the header line to the file.
			if(includePathwayInfo)
				out.write("PATIENT.ID\tGENE\tPROBE.ID\tGENOTYPE\tCOPYNUMBER\tSAMPLE.TYPE\tTIMEPOINT\tTISSUE.TYPE\tGPL.ID\tSEARCH_ID" + System.getProperty("line.separator"))
			else
				out.write("PATIENT.ID\tGENE\tPROBE.ID\tGENOTYPE\tCOPYNUMBER\tSAMPLE\tTIMEPOINT\tTISSUE.TYPE\tGPL.ID" + System.getProperty("line.separator"))

			//For each of the probe records we need to extract out the data for a given patient.
			//sql.eachRow(sSelect.toString(),[resultInstanceId])
			//{
			while(rs?.next()) {
				//row ->
				
				retrievedData = true
				
				//This data object holds onto our values.
				SnpDataObject snpDataObject = new SnpDataObject();
				
				//snpDataObject.patientNum = row.PATIENT_ID
				snpDataObject.patientNum = rs?.getString("SUBJECT_ID") 
				snpDataObject.probeName = rs?.getString("SNP")
				snpDataObject.geneName = rs?.getString("GENE")
				snpDataObject.sample = rs?.getString("sample_type")
				snpDataObject.timepoint = rs?.getString("timepoint")
				snpDataObject.tissue = rs?.getString("tissue_type")
				
				if(genotype)
				{
					snpDataObject.genotype = rs?.getString("GENOTYPE")
				}
				else
				{
					snpDataObject.genotype = "NA"
				}
				
				if(copyNumber)
				{
					snpDataObject.copyNumber = rs?.getString("COPYNUMBER")
				}
				else
				{
					snpDataObject.copyNumber = "NA"
				}
				
				if(includePathwayInfo)
				{
					snpDataObject.searchKeywordId = StringUtils.isNotEmpty(rs?.getString("SEARCH_KEYWORD_ID")?.toString()) ? rs?.getString("SEARCH_KEYWORD_ID")?.toString() : ''
				}
				else
				{
					snpDataObject.searchKeywordId = null
				}
				
				snpDataObject.gplId = rs?.getString("GPL_ID")
				
				//Write record.
				rowProcessor.processDataRow(snpDataObject,out)
			}
		}
		
		return retrievedData;
	}
	
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
	
	/**
	* Get the genes in a pathway based on the data in the search database.
	* @param pathwayName
	* @return
	*/
   def String getGenes (String pathwayName) {

	   groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

	   //Determine if a gene signature or list was used based on the string passed in.
	   StringBuilder pathwayS = new StringBuilder();

	   pathwayS.append(" select  distinct bm.primary_external_id as gene_id from ")
			   .append("search_keyword sk, ")
			   .append(" bio_marker_correl_mv sbm,")
			   .append(" bio_marker bm")
			   .append(" where sk.bio_data_id = sbm.bio_marker_id")
			   .append(" and sbm.asso_bio_marker_id = bm.bio_marker_id")
			   .append(" and sk.unique_id IN ");

	   //Construct an in list in case the user had multiple genes separated by ",".
	   pathwayS.append(convertStringToken(pathwayName));
	   
	   println("query to get genes from pathway: " + pathwayS.toString())
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
}