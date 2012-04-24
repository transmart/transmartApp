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


import org.springframework.context.ApplicationContext;
import search.SearchKeyword
import org.apache.commons.lang.StringUtils;

class SnpDataService {
	// need a default rowProcessor 
	ApplicationContext ctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getAttribute(org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.APPLICATION_CONTEXT)
		
	def dataSource
	def grailsApplication = ctx.getBean('grailsApplication')
	def utilService
	
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
		
		//These will be the two parts of the SQL statement. This SQL gets our SNP data by probe. We'll need to extract the actual genotypes/copynumber later.
		StringBuilder sSelect = new StringBuilder()
		StringBuilder sTables = new StringBuilder()
		
		sSelect.append("""
						SELECT  SNP_GENO.SNP_NAME AS SNP,
						DSM.PATIENT_ID,
						bm.BIO_MARKER_NAME AS GENE,
						DSM.sample_type,
						DSM.timepoint,
						DSM.tissue_type,
						SNP_GENO.SNP_CALLS AS GENOTYPE,
						SNP_COPY.COPY_NUMBER AS COPYNUMBER,
						PD.sourcesystem_cd
					""")
		
		//This from statement needs to be in all selects.
		sTables.append(""" 	FROM DE_SUBJECT_SAMPLE_MAPPING DSM 
							INNER JOIN patient_dimension PD ON DSM.patient_id = PD.patient_num 
							INNER JOIN qt_patient_set_collection qt ON qt.result_instance_id = ? AND qt.PATIENT_NUM = DSM.PATIENT_ID
							LEFT JOIN DE_SNP_CALLS_BY_GSM SNP_GENO ON DSM.PATIENT_ID = SNP_GENO.PATIENT_NUM AND DSM.SAMPLE_CD = SNP_GENO.GSM_NUM
							LEFT JOIN DE_SNP_COPY_NUMBER SNP_COPY ON DSM.PATIENT_ID = SNP_COPY.PATIENT_NUM AND SNP_GENO.snp_name = SNP_COPY.snp_name
							INNER JOIN DE_SNP_GENE_MAP D2 ON D2.SNP_NAME = SNP_GENO.SNP_NAME
							INNER JOIN bio_marker bm ON bm.PRIMARY_EXTERNAL_ID = to_char(D2.ENTREZ_GENE_ID)
						""")

		//If a list of genes was entered, look up the gene ids and add them to the query. If a gene signature or list was supplied then we modify the query to join on the tables that link the list to the gene ids.
		if (pathway != null && pathway.length() > 0 && !(pathway.startsWith("GENESIG") || pathway.startsWith("GENELIST")))
		{
			String genes;
			//Get the list of gene ids based on the search ids.
			genes = getGenes(pathway);
			
			sSelect.append(",sk.SEARCH_KEYWORD_ID ")
			
			//Include the tables we join on to get the unique_id.
			sTables.append("""
				INNER JOIN bio_marker_correl_mv sbm ON sbm.asso_bio_marker_id = bm.bio_marker_id
				INNER JOIN search_keyword sk ON sk.bio_data_id = sbm.bio_marker_id
			""")
			
			sTables.append(" WHERE DSM.trial_name = '").append(study).append("' ")
			sTables.append(" AND D2.ENTREZ_GENE_ID IN (").append(genes).append(")");
			
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
			sTables.append(" WHERE DSM.trial_name = '").append(study).append("' ")
			sTables.append(" AND sk.unique_id IN ").append(convertStringToken(pathway)).append(" ");
			
			includePathwayInfo = true
		}
		else
		{
			sTables.append(" WHERE DSM.trial_name = '").append(study).append("' ")
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
		
		println("SNP Query : " + sSelect.toString())
		
		//Create our output file.
		new File(fileLocation).withWriterAppend { out ->

			//Write the header line to the file.
			if(includePathwayInfo)
				out.write("PATIENT.ID\tGENE\tPROBE.ID\tGENOTYPE\tCOPYNUMBER\tSAMPLE.TYPE\tTIMEPOINT\tTISSUE.TYPE\tSEARCH_ID" + System.getProperty("line.separator"))
			else
				out.write("PATIENT.ID\tGENE\tPROBE.ID\tGENOTYPE\tCOPYNUMBER\tSAMPLE\tTIMEPOINT\tTISSUE" + System.getProperty("line.separator"))

			//For each of the probe records we need to extract out the data for a given patient.
			sql.eachRow(sSelect.toString(),[resultInstanceId]) 
			{ 
				row ->
				
				retrievedData = true
				
				//This data object holds onto our values.
				SnpDataObject snpDataObject = new SnpDataObject();
				
				//snpDataObject.patientNum = row.PATIENT_ID
				snpDataObject.patientNum = utilService.getActualPatientId(row.SOURCESYSTEM_CD)
				snpDataObject.probeName = row.SNP
				snpDataObject.geneName = row.GENE
				snpDataObject.sample = row.sample_type
				snpDataObject.timepoint = row.timepoint
				snpDataObject.tissue = row.tissue_type
				
				if(genotype)
				{
					snpDataObject.genotype = row.GENOTYPE
				}
				else
				{
					snpDataObject.genotype = "NA"
				} 
				
				if(copyNumber)
				{
					snpDataObject.copyNumber = row.COPYNUMBER
				}
				else
				{
					snpDataObject.copyNumber = "NA"
				}
				
				if(includePathwayInfo) 
				{
					snpDataObject.searchKeywordId = StringUtils.isNotEmpty(row.SEARCH_KEYWORD_ID?.toString()) ? row.SEARCH_KEYWORD_ID?.toString() : ''
				}
				else
				{
					snpDataObject.searchKeywordId = null
				}
				
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

class SnpDataObject
{
	String patientNum
	String probeName
	String genotype
	String copyNumber
	String geneName
	String searchKeywordId
	String sample
	String timepoint
	String tissue
}


