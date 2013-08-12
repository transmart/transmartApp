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
package com.recomdata.transmart.data.export;

import java.util.List;

import de.DeVariantDataSet;
import de.DeVariantSubjectDetail;
import de.DeVariantSubjectIdx;
import com.recomdata.transmart.data.export.util.FileWriterUtil;

class VcfDataService {
	boolean transactional = false
	
	def snpRefDataService
	def dataSource
	def VCF_V4_1_HEADER_LINE ="#CHROM	POS	ID	REF	ALT	QUAL	FILTER	INFO	FORMAT"
	
	def boolean getDataAsFile(
		String outputDir,
		String jobName,
		List studyList,
	String resultInstanceId1,
	//String resultInstanceId2,
	String selectedSNPs,
	String selectedGenes,
	String selectedChromosomes,
	String subjectPrefix){

		def rsList = []
		def chrList =[]
		def geneNameList = [];
		// create query to find gene to snps
		if(selectedGenes!=null&& selectedGenes.trim().length()>0){
			// ToDo- parse gene list
			//def geneList = parseGeneList(selectedGenes)
		//	def geneList = parseGeneList(["BRCA1"])
			List<Long> geneSearchIdList = new ArrayList<Long>();
			
			if (selectedGenes != null && selectedGenes.length() != 0) {
				
				geneNameList = parseGeneList(selectedGenes, null, geneSearchIdList);
				//println(selectedGenes)
				//println(geneNameList)
				
			rsList.addAll(snpRefDataService.findRsIdByGeneNames(geneNameList))
			}

		}

		// create query to find snps
		if(selectedSNPs!=null && selectedSNPs.trim().length()>0){
			rsList.addAll(parseRsList(selectedSNPs))
		}

		if(selectedChromosomes!=null && "ALL"!=selectedChromosomes){
			chrList.addAll(parseChrList(selectedChromosomes));
		}

		
		// locate dataset
		
		def datasets = findDataset(resultInstanceId1, null);
		

		// locate and retrieve variant
				
		def variants = retrieveVariantDetail(rsList, chrList, datasets)
		
		def dsSubjectIdxMap = [:]
		def dsSubHeaderColMap=[:]
		// create subset 1 subject id query
		
		def s1 = [:]
		if(resultInstanceId1!=null)
			s1 = findSubjectIdx(resultInstanceId1)
	
		// create subset 2 subject id query

		def s2 =[:]
	//	if(resultInstanceId2!=null) {
	//		s2= findSubjectIdx(resultInstanceId2)
	//	}
			// create vcf sample and header list by dataset id
			// each dataset has a vcf file
			s1.each { k,v ->
				if(dsSubjectIdxMap.get(k)==null){
					dsSubjectIdxMap.put(k, [])
					dsSubHeaderColMap.put(k,[])
				}
				v.each{key, value->
				dsSubjectIdxMap.get(k).add(value)
				// construct key
				dsSubHeaderColMap.get(k).add(subjectPrefix+"_"+key)
				}
			}
			// s2
			s2.each { k,v ->
				if(dsSubjectIdxMap.get(k)==null){
					dsSubjectIdxMap.put(k, [])
					dsSubHeaderColMap.put(k,[])
				}
				v.each{key, value->
				dsSubjectIdxMap.get(k).add(value)
				// construct key
				dsSubHeaderColMap.get(k).add(subjectPrefix+key)
				}
			}
			
			// construct VCFdata file
		def vsnpset =	constructVCFfile(variants, datasets, dsSubjectIdxMap, dsSubHeaderColMap, outputDir, jobName,subjectPrefix)
		println(vsnpset)
		constructVCFParamFile(outputDir, geneNameList, new ArrayList(vsnpset), chrList,jobName)
		

		return true;
	}
	
	def constructVCFParamFile(
		outputDir,
		geneNameList,
		rsList,
		chrList,
		jobName){
		// create a filter file so that we can get the right params
		def pfile = null;
		def pfilewriter = null;
		try{
			pfile= new File(outputDir+File.separator+jobName+"_vcf.params")
			//println("writing file:"+pfile.getAbsolutePath())
		
			 pfilewriter = pfile.newWriter();
		// if there is a gene use the first gene 
		if(geneNameList!=null && !geneNameList.isEmpty()){
			pfilewriter.writeLine("Gene="+geneNameList[0])
			
		}
		if(chrList!=null && !chrList.isEmpty()){
			pfilewriter.writeLine("Chr="+"chr"+chrList[0])
		}
		
		if(rsList!=null && !rsList.isEmpty()){
			
			//println(rsList)
			def nrsid = rsList[0].trim().toLowerCase();
			if(!nrsid.startsWith("rs")){
			nrsid = "rs"+nrsid;
			}
		//	println("rsid:"+nrsid)
			def result = DeVariantSubjectDetail.executeQuery("SELECT d.chromosome, d.position FROM DeVariantSubjectDetail d WHERE d.rsID =?", nrsid)
			def chr = null;
			def pos = null;
			if(result!=null){
				chr = result[0][0];
				pos = result[0][1];
			
			def spos = pos -50;
			def epos = pos+50;
			
			pfilewriter.writeLine("SNP="+"chr"+chr+":"+spos+"-"+epos)
			}
		}
			
			
		}catch(Exception e ){
		log.error(e.getMessage(), e)
			
		}finally{
		if(pfilewriter!=null){
		pfilewriter.flush()
		pfilewriter.close();
		}
		}
	}

	/**
	 * construct multiple VCF files - each dataset has a file
	 * @param variants
	 * @param datasetIds
	 * @param dsSubjectIdxMap
	 * @param dsSubHeaderColMap
	 * @param outputDir
	 * @param jobName
	 * @return
	 */
	def constructVCFfile(variants,
		datasetIds, 
		dsSubjectIdxMap, 
		dsSubHeaderColMap, 
		outputDir, 
		jobName,
		subjectPrefix){
		
		Set vsnpSet = new HashSet();
		// create writers, each dataset get a writer
		def dsWriterMap =[:]
		// each dataset has a new files
		datasetIds.each { 
			def nfile =new File(outputDir+File.separator+jobName+"_"+it+"_"+subjectPrefix+".vcf");
			log.debug("writing data file for dataset:"+it+" at :"+nfile.getAbsolutePath())
			
			def nwriter = nfile.newWriter();
				dsWriterMap.put(it, nwriter)
			// write metadata and header
			def dsMetadata = DeVariantDataSet.get(it)
			// metadata
			
			nwriter.write(dsMetadata.metadata);
			// header
		
				StringBuilder s = new StringBuilder(VCF_V4_1_HEADER_LINE)
		//		println("header:"+dsSubHeaderColMap.get(it))
				def dheader = dsSubHeaderColMap.get(it)
				dheader.each {
					s.append("\t")
					s.append(it)
				}
			nwriter.writeLine(s.toString())
			nwriter.flush()
			
				
		}
		// loop through variant data and use the dataset writer to output data
		
		println("variant length:"+variants.size())
		variants.each {
			
			StringBuilder variant = new StringBuilder()
		
			variant.append(it.chromosome).append("\t");
			variant.append(it.position).append("\t");
			variant.append(it.rsID).append("\t");
			variant.append(it.ref).append("\t");
			variant.append(it.alt).append("\t");
			variant.append(it.quality).append("\t");
			variant.append(it.filter).append("\t");
			variant.append(it.info).append("\t");
			variant.append(it.format);
			
			vsnpSet.add(it.rsID);
			
			String value = it.variant
			
			// using a split to get the value in array by tab
			String[] valueArray = value.split("\\t");
			def total = valueArray.length
			//println("totalvalue:"+total)
		//	println(valueArray)
			def indexList = dsSubjectIdxMap.get(it.dataset);
			// 1 based index list
			
			
			indexList.each {
				int idx = it-1;
				if(idx<=total){			
				//println(it)
				variant.append("\t").append(valueArray[idx])
			//	println(valueArray[idx])
				}
				else{
					throw new Exception("variant size :"+total+" do not match variant index:"+it)
				}
			}
			//variant.append(value)
			dsWriterMap.get(it.dataset).writeLine(variant.toString())
			
					
		}
		dsWriterMap.values().each {
			try{
			it.flush()
			it.close()
			}catch(Exception e){
				log.error(e.getMessage(), e)
			}
			
		}
		return vsnpSet;
		
	}

	/**
	 * retrieve variant by rsid, chr and dataset. dataset is required	 
	 * @param rsList
	 * @param chrList
	 * @param dataset
	 * @return
	 */
	def retrieveVariantDetail(rsList, chrList, datasetList){
		String query = "FROM DeVariantSubjectDetail dvd WHERE dvd.dataset IN (:ds) "
		def vmap = [:]
		vmap.put('ds',datasetList)

	//	println("Rs:"+rsList);
	//	println("CHR:"+chrList);
		
		if(!rsList.isEmpty())
		{
			query +=" AND dvd.rsID IN (:rsids) " // this could be an issue for more than 1000 rs ids
			vmap.put('rsids', rsList)
		}
		
		if (!chrList.isEmpty()){

			query+= " AND dvd.chromosome IN (:chrNums) "
			vmap.put('chrNums', chrList)
		}
		query +=" ORDER BY dvd.chromosome, dvd.position"
		
		return DeVariantSubjectDetail.findAll(query, vmap)

	}

	
	/**
	 * parse rsids
	 * @param rsIds
	 * @return
	 */
	def parseRsList(String rsIds){
		//def rsList = []
		String[] rsidArray = rsIds.split(",");
		def rsList = []
		for(int i = 0; i<rsidArray.length; i++){
			rsList.add(rsidArray[i].trim())
		}
		
		return rsList;
	}

	/**
	 * parse chromosome  
	 */
	def parseChrList(String chrs){
		def chrList =[]
		chrList.add(chrs)
		return chrList;
	}

	/**
	 * find dataset
	 * @param resultInstanceId1
	 * @param resultInstanceId2
	 * @return
	 */
	
	def findDataset(resultInstanceId1, resultInstanceId2){
		
		
		def q= """
		SELECT DISTINCT a.dataset_id from DE_VARIANT_SUBJECT_IDX a
		INNER JOIN de_subject_sample_mapping b on a.SUBJECT_ID = b.SUBJECT_ID 
		INNER JOIN qt_patient_set_collection sc ON sc.result_instance_id in (?) AND b.patient_id = sc.patient_num
""";
		def param = []
		if(resultInstanceId1!=null){
			param.add('CAST(' + resultInstanceId1 + '? AS numeric)')
		}
		if(resultInstanceId2!=null){
			param.add('CAST(' + resultInstanceId2 + '? AS numeric)')
		}
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		def datasetList=[];
	
		sql.eachRow(q, param, {row->
			if(row.dataset_id!=null)
			{
				datasetList.add(row.dataset_id)
			}
		});
		return datasetList
	}

	/**
	 * find subject idx map
	 * @param resultInstanceId
	 * @param prefix
	 * @return
	 */
	def findSubjectIdx(resultInstanceId){
		
		def q  =
"""				SELECT distinct a.DATASET_ID, a.SUBJECT_ID, a.POSITION from DE_VARIANT_SUBJECT_IDX a
		INNER JOIN de_subject_sample_mapping b on a.SUBJECT_ID = b.SUBJECT_ID 
		INNER JOIN qt_patient_set_collection sc ON sc.result_instance_id in (CAST(? AS numeric)) AND b.patient_id = sc.patient_num
 ORDER BY a.POSITION"""
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		def datasetIdxMap =[:]
		
		sql.eachRow(q, [resultInstanceId], {row->
			def sIdx = datasetIdxMap.get(row.DATASET_ID);
			if(sIdx==null){
				sIdx = new LinkedHashMap() 
				datasetIdxMap.put(row.DATASET_ID,sIdx)
			}	
			sIdx.put(row.SUBJECT_ID, row.POSITION)
			//log.debug("sub:"+row.SUBJECT_ID+" pos:"+row.POSITION)
			
		});
		
		return datasetIdxMap;

	}
	
	
	
	/**
	* This function parse the ","-separated gene string like "Gene>MET", and return a list of gene search ID and a list of matching gene names.
	*/
   
   
   def parseGeneList(String genes, String geneAndIdListStr, List<Long> geneSearchIdList) {
	   def geneNameList = []
	    if (genes == null || genes.length() == 0)
		   return null;
	   Map<String, Long> geneIdMap = new HashMap<String, Long>();
	 
	/*   String[] geneAndIdList = geneAndIdListStr.split("\\|\\|\\|");
	   for (String geneAndIdStr : geneAndIdList) {
		   String[] geneIdPair = geneAndIdStr.split("\\|\\|");
		   geneIdMap.put(geneIdPair[0].trim(), new Long(geneIdPair[1].trim()));
	   }*/
	   String[] geneValues = genes.split(",");
	   
	 //  println("parse:"+geneValues)
	   for (String geneStr : geneValues) {
		   geneStr = geneStr.trim();
		   Long geneId = geneIdMap.get(geneStr.trim());
		   geneSearchIdList.add(geneId);
		   if (geneStr.startsWith("Gene>"))
			   geneStr = geneStr.substring("Gene>".length());
		   geneNameList.add(geneStr.trim());
	   }
	   return geneNameList;
   }
   
   def listToIN(List<String> list) {
	   StringBuilder sb=new StringBuilder();
	   // need to make it less than 1000! -- temp solution
	   int i = 0;
	   for(c in list)
	   {
		   //If the only thing submitted was "ALL" we return an empty string just like there was nothinbg in the box.
		   if(c.toString()=="ALL" && list.size()==1)
		   {
			   break;
		   }
		   
		   sb.append("'");
		   sb.append(c.toString().replace("'","''"));
		   sb.append("'");
		   sb.append(",");
		   i++;
		   if(i>=1000){
			   break;
		   }
	   }
	   if(sb.length()>0) {
		   sb.deleteCharAt(sb.length() - 1);//remove last comma
	   }
	   return sb.toString();
   }

}