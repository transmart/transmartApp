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

import java.util.Collection;
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
	def selectGeneNamesSql = """
			select distinct
				bm.bio_marker_name as gene_name
			from
				search_keyword sk, bio_marker bm
			where
				sk.search_keyword_id in (:seachKeywordIds)
			  	and sk.bio_data_id = bm.bio_marker_id
			  	and bm.bio_marker_type = 'GENE'
			union
			select
				bm.bio_marker_name from search_keyword sk, search_bio_mkr_correl_view sbmcv, bio_marker bm
			where
				sk.search_keyword_id in (:seachKeywordIds)
			  	and sk.bio_data_id = sbmcv.domain_object_id
			  	and sbmcv.asso_bio_marker_id = bm.bio_marker_id
			  	and bm.bio_marker_type = 'GENE'
			union
			select
				bm.bio_marker_name from search_keyword sk, search_bio_mkr_correl_view sbmcv, bio_marker bm
			where
				sk.search_keyword_id in (:seachKeywordIds)
			  	and sk.bio_data_id = sbmcv.domain_object_id
			  	and sbmcv.asso_bio_marker_id = bm.bio_marker_id
			  	and bm.bio_marker_type = 'GENE'
		"""		
	def variantDetailColumns = ["id", "alt", "chromosome", "dataset", "filter", "format", "info",
		"position", "quality", "ref", "rsID", "variant"]		
	def selectVariantDetailSql = """
			select
				variant_subject_detail_id as id, alt, chr as chromosome, dataset_id as dataset, filter, format, 
				info, pos as position, qual as quality, ref, rs_id as rsID, variant_value as variant
			from
				de_variant_subject_detail 
			where
		"""
	/**
	 * Creates VCF file based on variant data
	 * @param outputDir
	 * @param jobName
	 * @param studyList
	 * @param resultInstanceId1
	 * @param selectedSNPs
	 * @param selectedGenes
	 * @param selectedGenesAndId
	 * @param selectedChromosomes
	 * @param subjectPrefix
	 * @return true
	 */	
	def boolean getDataAsFile(
			String outputDir,
			String jobName,
			List studyList,
			String resultInstanceId1,
			String selectedSNPs,
			String selectedGenes,
			String selectedGenesAndId,
			String selectedChromosomes,
			String subjectPrefix) {

		def rsList = []
		def chrList =[]
		def geneNameList = [];
		def selectedIds = parseSelectedGenesAndId(selectedGenesAndId)
		
		// Get list of gene names for selected searchKeywordIds
		if (selectedIds != null && selectedIds.trim().length() > 0) {
			geneNameList.addAll(parseSearchKeywordIds(selectedIds))
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
		def variants = retrieveVariantDetail(geneNameList, rsList, chrList, datasets)

		def dsSubjectIdxMap = [:]
		def dsSubHeaderColMap=[:]
		// create subset 1 subject id query

		def s1 = [:]
		if(resultInstanceId1!=null)
			s1 = findSubjectIdx(resultInstanceId1)

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
		jobName) {
		// create a filter file so that we can get the right params
		def pfile = null;
		def pfilewriter = null;
		try{
			pfile= new File(outputDir+File.separator+jobName+"_vcf.params")
			//println("writing file:"+pfile.getAbsolutePath())
		
			 pfilewriter = pfile.newWriter();
			// if there is a gene use the first gene 
			if (geneNameList!=null && !geneNameList.isEmpty()) {
				pfilewriter.writeLine("Gene="+geneNameList[0])			
			}
			if (chrList!=null && !chrList.isEmpty()) {
				pfilewriter.writeLine("Chr="+"chr"+chrList[0])
			}
			
			if (rsList!=null && !rsList.isEmpty()) {
				def nrsid = rsList[0].trim().toLowerCase();
				if (!nrsid.startsWith("rs")) {
					nrsid = "rs"+nrsid;
				}
	
				def result = DeVariantSubjectDetail.executeQuery("SELECT d.chromosome, d.position FROM DeVariantSubjectDetail d WHERE d.rsID =?", nrsid)
				def chr = null;
				def pos = null;
				if (result!=null) {
					chr = result[0][0];
					pos = result[0][1];
				
					def spos = pos -50;
					def epos = pos+50;
				
					pfilewriter.writeLine("SNP="+"chr"+chr+":"+spos+"-"+epos)
				}
			}		
		} catch(Exception e  ){
			log.error(e.getMessage(), e)		
		} finally {
			if (pfilewriter!=null) {
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
			
			def metadata = dsMetadata.metadata
			// If meta data includes header, then trim it
			if (metadata.indexOf("#CHROM") != -1) {
				metadata = metadata.substring(0, dsMetadata.metadata.lastIndexOf("#CHROM"))
			}
			nwriter.write(metadata);
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
			def indexList = dsSubjectIdxMap.get(it.dataset);
			// 1 based index list
						
			indexList.each {
				int idx = it-1
				if (idx <= total) {	
					def subjectValue = valueArray[idx]
					
					// HACK to replace "." characters in subject value fields with "0" so that IGV doesn't gernate errors.
					String[] values = subjectValue.split(":")
					StringBuilder newSubjectValue = new StringBuilder();
					newSubjectValue.append(values[0])
					newSubjectValue.append(":")
					if (values[1].indexOf(".") != -1) {
						newSubjectValue.append(values[1].replaceAll("\\.", "0"))
					} else {
						newSubjectValue.append(values[1])
					}
					newSubjectValue.append(":")
					if (values[2].indexOf(".") != -1) {
						newSubjectValue.append(values[2].replaceAll("\\.", "0")) 
					} else {
						newSubjectValue.append(values[2])
					}
					variant.append("\t").append(newSubjectValue.toString())
				//	println(valueArray[idx])
				}  else {
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
	 * Gets variant detail records for specified parameters.	
	 * @param geneNameList
	 * @param rsIdList
	 * @param chromosomeList
	 * @param datasetIdList
	 * @return
	 */
	def retrieveVariantDetail(geneNameList, rsIdList, chromosomeList, datasetIdList) {
		
		// NOTE: This was converted to a SQL query to be able to handle queries with more than 1000 rsIDs
		
		StringBuilder whereClause = new StringBuilder()
		if (geneNameList.size() > 0) {
			whereClause.append(" and (rs_id in (select rs_id from de_rc_snp_info where gene_name in (")
				.append(listToParams(geneNameList)).append("))")
		}
		if (rsIdList.size() > 0) {
			if (whereClause.length() > 0) {
				whereClause.append(" or ")
			} else {
				whereClause.append("and (")
			}
			whereClause.append("rs_id in (").append(listToParams(rsIdList)).append( ")")
		}
		if (whereClause.length() > 0) {
			whereClause.append(")")
		}
		
		if (chromosomeList.size() > 0) {
			whereClause.append(" and chr in (").append(listToParams(chromosomeList)).append(")")
		}
		
		StringBuilder sql = new StringBuilder()
		sql.append(selectVariantDetailSql)
			.append(" dataset_id in (").append(listToParams(datasetIdList)).append(")")
			.append(whereClause.toString())
			.append(" order by chr, pos")

		def con = null
		def stmt = null
		def rs = null
		def records = []
		try {
			con = dataSource.getConnection()
			stmt = con.prepareStatement(sql.toString())
			rs = stmt.executeQuery()
			while (rs.next()) {
				def record = [:]
				for (column in variantDetailColumns) {
					record[column] = rs.getString(column)
				}
				records.add(record)
			}
	
		} finally {
			rs?.close()
			stmt?.close()
			con?.close()
		}
		
		return records
		
	}

	/**
	 * Splits list into string of comma separated sql string values
	 * @param list
	 * @return
	 */
	def listToParams(list) {
		StringBuilder params = new StringBuilder()
		for (item in list) {
			if (params.length() > 0) {
				params.append(", ")
			}
			params.append("'").append(item).append("'")
		}
		return params.toString()
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
			param.add(resultInstanceId1)
		}
		if(resultInstanceId2!=null){
			param.add(resultInstanceId2)
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
		INNER JOIN qt_patient_set_collection sc ON sc.result_instance_id in (?) AND b.patient_id = sc.patient_num
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
	 * Parses list of gene names and search keyword IDs to get comma separated list of search keyword IDs
	 * @param selectedGenesAndId
	 * @return
	 */
	def parseSelectedGenesAndId(String selectedGenesAndId) {
		
		StringBuilder result = new StringBuilder()
		String[] geneAndIds = selectedGenesAndId.split("\\|\\|\\|")
		
		for (geneAndId in geneAndIds) {
			String[] tokens = geneAndId.split("\\|\\|")
			if (result.length() > 0) {
				result.append(", ")
			}
			result.append(tokens[1])
		}
		
		return result.toString()
		
	}
	
	/**
	 * Gets list of gene names for specified search keyword IDs, splitting out
	 * genes from pathways, gene signatures, and gene lists
	 * @param seachKeywordIds
	 * @param geneNameList
	 * @return
	 */
	def parseSearchKeywordIds(String seachKeywordIds) {
		
		if (seachKeywordIds == null || seachKeywordIds.length() == 0) {
			return null
		}

		def con = null
		def stmt = null
		def rs = null
		def sql = selectGeneNamesSql.replace(":seachKeywordIds", seachKeywordIds)
		def geneNameList = []
		
		try {
			con = dataSource.getConnection()
			stmt = con.prepareStatement(sql)
			rs = stmt.executeQuery()
			while (rs.next()) {
				geneNameList.add(rs.getString("gene_name"))
			}

		} finally {
			rs?.close()
			stmt?.close()
			con?.close()
		}
		
		return geneNameList
		
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