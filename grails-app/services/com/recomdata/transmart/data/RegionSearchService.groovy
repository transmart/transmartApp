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
  

package com.recomdata.transmart.data

import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import search.SearchKeyword

import bio.BioAssayAnalysisGwas;

import com.recomdata.transmart.data.export.util.FileWriterUtil

class RegionSearchService {
	
    boolean transactional = true

	def dataSource
	def grailsApplication
	def config = ConfigurationHolder.config
	
	def geneLimitsSqlQuery = """
	
	SELECT max(snpinfo.pos) as high, min(snpinfo.pos) as low, min(snpinfo.chrom) as chrom FROM SEARCHAPP.SEARCH_KEYWORD
	INNER JOIN bio_marker bm ON bm.BIO_MARKER_ID = SEARCH_KEYWORD.BIO_DATA_ID
	INNER JOIN deapp.de_snp_gene_map gmap ON gmap.entrez_gene_id = bm.PRIMARY_EXTERNAL_ID
	INNER JOIN DEAPP.DE_RC_SNP_INFO snpinfo ON gmap.snp_name = snpinfo.rs_id
	WHERE SEARCH_KEYWORD_ID=? AND snpinfo.hg_version = ?
	
	"""
	
	def genesForSnpQuery = """
	
	SELECT DISTINCT(GENE_NAME) as BIO_MARKER_NAME FROM DE_SNP_GENE_MAP
	WHERE SNP_NAME = ?
	
	"""
	
	def snpLimitsSqlQuery = """
	
	SELECT max(snpinfo.pos) as high, min(snpinfo.pos) as low, min(snpinfo.chrom) as chrom FROM SEARCHAPP.SEARCH_KEYWORD sk
	INNER JOIN DEAPP.DE_RC_SNP_INFO snpinfo ON sk.keyword = snpinfo.rs_id
	WHERE SEARCH_KEYWORD_ID=? AND snpinfo.hg_version = ?
	
	"""
	
	def analysisNameSqlQuery = """
	SELECT DATA.bio_assay_analysis_id as id, DATA.analysis_name as name
	FROM BIOMART.bio_assay_analysis DATA WHERE 1=1 
"""
	//Query with mad Oracle pagination
	def gwasSqlQuery = """
		SELECT a.*
		  FROM (SELECT   _analysisSelect_ info.chrom AS chrom,
		                 info.pos AS pos, gmap.gene_name AS rsgene,
		                 DATA.rs_id AS rsid, DATA.p_value AS pvalue,
		                 DATA.log_p_value AS logpvalue, DATA.ext_data AS extdata
		                 ,
		                 ROW_NUMBER () OVER (ORDER BY _orderclause_) AS row_nbr
		                 FROM biomart.bio_assay_analysis_gwas DATA
		                 _analysisJoin_
		                 JOIN deapp.de_rc_snp_info info ON DATA.rs_id = info.rs_id and (_regionlist_)
		                 LEFT JOIN deapp.de_snp_gene_map gmap ON info.snp_info_id = gmap.snp_id
		                 WHERE 1=1
	"""
	def gwasHg19SqlQuery = """
	SELECT a.*
	  FROM (SELECT   _analysisSelect_ info.chrom AS chrom,
					 info.pos AS pos, info.rsgene AS rsgene,
					 DATA.rs_id AS rsid, DATA.p_value AS pvalue,
					 DATA.log_p_value AS logpvalue, DATA.ext_data AS extdata
					 ,
					 ROW_NUMBER () OVER (ORDER BY _orderclause_) AS row_nbr
					 FROM biomart.bio_assay_analysis_gwas DATA
					 _analysisJoin_
					 JOIN deapp.de_snp_info_hg19_mv info ON DATA.rs_id = info.rs_id and ( _regionlist_ )
					 WHERE 1=1
"""
	def eqtlSqlQuery = """
		SELECT a.*
		  FROM (SELECT   _analysisSelect_ info.chrom AS chrom,
		                 info.pos AS pos, gmap.gene_name AS rsgene,
		                 DATA.rs_id AS rsid, DATA.p_value AS pvalue,
		                 DATA.log_p_value AS logpvalue, DATA.ext_data AS extdata, DATA.gene as gene
		                 ,
		                 ROW_NUMBER () OVER (ORDER BY _orderclause_) AS row_nbr
		                 FROM biomart.bio_assay_analysis_eqtl DATA
		                 _analysisJoin_
		                 JOIN deapp.de_rc_snp_info info ON DATA.rs_id = info.rs_id and (_regionlist_)
		                 LEFT JOIN deapp.de_snp_gene_map gmap ON info.snp_info_id = gmap.snp_id
		                 WHERE 1=1
	"""
	
	def eqtlHg19SqlQuery = """
	SELECT a.*
	  FROM (SELECT   _analysisSelect_ info.chrom AS chrom,
					 info.pos AS pos, info.rsgene AS rsgene,
					 DATA.rs_id AS rsid, DATA.p_value AS pvalue,
					 DATA.log_p_value AS logpvalue, DATA.ext_data AS extdata, DATA.gene as gene
					 ,
					 ROW_NUMBER () OVER (ORDER BY _orderclause_) AS row_nbr
					 FROM biomart.bio_assay_analysis_eqtl DATA
					 _analysisJoin_
					 JOIN deapp.de_snp_info_hg19_mv info ON DATA.rs_id = info.rs_id and (_regionlist_)
					 WHERE 1=1
"""

	def gwasSqlCountQuery = """
		SELECT COUNT(*) AS TOTAL FROM biomart.Bio_Assay_Analysis_Gwas data 
	     JOIN deapp.de_rc_snp_info info ON DATA.rs_id = info.rs_id and (_regionlist_)
	     LEFT JOIN deapp.de_snp_gene_map gmap ON info.snp_info_id = gmap.snp_id
	     WHERE 1=1
	"""
	
	def gwasHg19SqlCountQuery = """
	SELECT COUNT(*) AS TOTAL FROM biomart.Bio_Assay_Analysis_Gwas data
	 JOIN deapp.de_snp_info_hg19_mv info ON DATA.rs_id = info.rs_id and (_regionlist_)
	 WHERE 1=1
"""
	def eqtlSqlCountQuery = """
		SELECT COUNT(*) AS TOTAL FROM biomart.Bio_Assay_Analysis_Eqtl data
	     JOIN deapp.de_rc_snp_info info ON DATA.rs_id = info.rs_id and (_regionlist_)
	     LEFT JOIN deapp.de_snp_gene_map gmap ON info.snp_info_id = gmap.snp_id
	     WHERE 1=1
    """
	def eqtlHg19SqlCountQuery = """
	SELECT COUNT(*) AS TOTAL FROM biomart.Bio_Assay_Analysis_Eqtl data
	 JOIN deapp.de_snp_info_hg19_mv info ON DATA.rs_id = info.rs_id and (_regionlist_)
	 WHERE 1=1
"""
	def getGeneLimits(Long searchId, String ver) {
		//Create objects we use to form JDBC connection.
		def con, stmt, rs = null;
		
		//Grab the connection from the grails object.
		con = dataSource.getConnection()
		
		//Prepare the SQL statement.
		stmt = con.prepareStatement(geneLimitsSqlQuery);
		stmt.setLong(1, searchId);
		stmt.setString(2, ver);

		rs = stmt.executeQuery();

		try{
			if(rs.next()){
				def high = rs.getLong("HIGH");
				def low = rs.getLong("LOW");
				def chrom = rs.getString("CHROM");
				return [low: low, high:high, chrom: chrom]
			}
		}finally{
			rs?.close();
			stmt?.close();
			con?.close();
		}
	}
	
	def getGenesForSnp(String snp) {
		//Create objects we use to form JDBC connection.
		def con, stmt, rs = null;
		
		//Grab the connection from the grails object.
		con = dataSource.getConnection()
		
		//Prepare the SQL statement.
		stmt = con.prepareStatement(genesForSnpQuery);
		stmt.setString(1, snp);

		rs = stmt.executeQuery();

		def results = []
		try{
			while(rs.next()){
				results.push(rs.getString("BIO_MARKER_NAME"))
			}
		}finally{
			rs?.close();
			stmt?.close();
			con?.close();
		}
		
		return results;
	}
	
	def getSnpLimits(Long searchId, String ver) {
		//Create objects we use to form JDBC connection.
		def con, stmt, rs = null;
		
		//Grab the connection from the grails object.
		con = dataSource.getConnection()
		
		//Prepare the SQL statement.
		stmt = con.prepareStatement(snpLimitsSqlQuery);
		stmt.setLong(1, searchId);
		stmt.setString(2, ver);

		rs = stmt.executeQuery();

		try{
			if(rs.next()){
				def high = rs.getLong("HIGH");
				def low = rs.getLong("LOW");
				def chrom = rs.getString("CHROM");
				return [low: low, high:high, chrom: chrom]
			}
		}finally{
			rs?.close();
			stmt?.close();
			con?.close();
		}
	}
	
	def getAnalysisData(analysisIds, ranges, Long limit, Long offset, Double cutoff, String sortField, String order, String search, String type, geneNames, doCount) {
		
		def con, stmt, rs = null;
		con = dataSource.getConnection()
		StringBuilder queryCriteria = new StringBuilder();
		def analysisQuery
		def countQuery
		StringBuilder regionList = new StringBuilder();
		def analysisQCriteria = new StringBuilder();
		def analysisNameQuery = analysisNameSqlQuery;
		def hg19only = false;
		
		def analysisNameMap =[:]
		
		if(!ranges){
			hg19only = true;
		}else {
			hg19only = true; // default to true
			for(range in ranges){
				//println(range)
				
				if(range.ver!='19'){
					hg19only = false;
					break;
				}
			}
		}
		
		if (type.equals("gwas")) {
			analysisQuery = gwasSqlQuery
			countQuery = gwasSqlCountQuery
			if(hg19only){ // for hg19, special query 
				analysisQuery = gwasHg19SqlQuery;
				countQuery = gwasHg19SqlCountQuery
			}
		}
		else if (type.equals("eqtl")) {
			analysisQuery = eqtlSqlQuery
			countQuery = eqtlSqlCountQuery
			if(hg19only){
				analysisQuery = eqtlHg19SqlQuery
				countQuery = eqtlHg19SqlCountQuery
			}
		}
		else {
			throw new Exception("Unrecognized data type")
		}
		
		def rangesDone = 0;
		
		//if (!ranges) {
			//If no ranges, force HG19
		//	regionList.append("hg_version='19'") -- we have a special sql for hg19
		//}
		//else {
		if(ranges!=null){
			for (range in ranges) {
				if (rangesDone != 0) {
					regionList.append(" OR ")
				}
				//Chromosome
				if (range.chromosome != null) {
					if (range.low == 0 && range.high == 0) {
						regionList.append("(info.chrom = '${range.chromosome}' ")
					}
					else {
						regionList.append("(info.pos >= ${range.low} AND info.pos <= ${range.high} AND info.chrom = '${range.chromosome}' ")
					}
					
					if(hg19only== false) {
						regionList.append("  AND info.hg_version = '${range.ver}' ")
					}
					regionList.append(")");
				}
				//Gene
				else {
					regionList.append("(info.pos >= ${range.low} AND info.pos <= ${range.high} ")
					if(hg19only== false) {
						regionList.append("  AND info.hg_version = '${range.ver}' ")
					}
					regionList.append(")")
				}
				rangesDone++
			}
		}
		
		def analysisNameIncluded = false
		//Add analysis IDs
		if (analysisIds) {
			analysisQCriteria.append(" AND data.BIO_ASSAY_ANALYSIS_ID IN (" + analysisIds[0]);
			for (int i = 1; i < analysisIds.size(); i++) {
			analysisQCriteria.append(", " + analysisIds[i]);
			}
			analysisQCriteria.append(") ")
			queryCriteria.append(analysisQCriteria.toString())
			
			//Only select the analysis name if we need to distinguish between them!
			if (analysisIds.size() > 1) {
				//analysisQuery = analysisQuery.replace("_analysisSelect_", "baa.analysis_name AS analysis, ")
				analysisQuery = analysisQuery.replace("_analysisSelect_", "DATA.bio_assay_analysis_id AS analysis_id, ")
				//analysisQuery = analysisQuery.replace("_analysisJoin_", " JOIN biomart.bio_assay_analysis baa ON baa.bio_assay_analysis_id = DATA.bio_assay_analysis_id ")
				analysisQuery = analysisQuery.replace("_analysisJoin_", "");
				analysisNameIncluded = true
			}
			else {
				analysisQuery = analysisQuery.replace("_analysisSelect_", "");
				analysisQuery = analysisQuery.replace("_analysisJoin_", "");
			}
		}
		
		//Add gene names
		if (geneNames) {
			// quick fix for hg19 only
			if(hg19only){
				queryCriteria.append(" AND info.rsgene IN (")	
			}else{
			queryCriteria.append(" AND gmap.gene_name IN (");
			}
			queryCriteria.append( "'" + geneNames[0] + "'");
			for (int i = 1; i < geneNames.size(); i++) {
				queryCriteria.append(", " + "'" + geneNames[i] + "'");
			}
			queryCriteria.append(") ")
		}

		
		if (cutoff) {
			queryCriteria.append(" AND p_value <= ?");
		}
		if (search) {
			queryCriteria.append(" AND (data.rs_id LIKE '%${search}%'")
			queryCriteria.append(" OR data.ext_data LIKE '%${search}%'")
			if(hg19only){
				queryCriteria.append(" OR info.rsgene LIKE '%${search}%'")	
			}else{
				queryCriteria.append(" OR gmap.gene_name LIKE '%${search}%'");
			}
			queryCriteria.append(" OR info.pos LIKE '%${search}%'")
			queryCriteria.append(" OR info.chrom LIKE '%${search}%'")
			if (type.equals("eqtl")) {
				queryCriteria.append(" OR data.gene LIKE '%${search}%'")
			}
			queryCriteria.append(") ")
		}

		// handle null regionlist issue
		if(regionList.length()==0){
			regionList.append("1=1")
		}
		
		analysisQuery = analysisQuery.replace("_regionlist_", regionList.toString())
		
		// this is really a hack
		def sortOrder = sortField?.trim();
		//println(sortField)
		if(hg19only){
		sortOrder = sortOrder.replaceAll("gmap.gene_name", "info.rsgene");
		
		}
		//println("after:"+sortOrder)
		analysisQuery = analysisQuery.replace("_orderclause_", sortOrder + " " + order)
		countQuery = countQuery.replace("_regionlist_", regionList.toString())
		
		// analysis name query
		
		if (analysisNameIncluded) {
			try {
				def nameQuery = analysisNameQuery + analysisQCriteria.toString();
				log.debug(nameQuery)
				stmt = con.prepareStatement(nameQuery)
				
				rs = stmt.executeQuery();
				while (rs.next()) {
					analysisNameMap.put(rs.getLong("id"), rs.getString("name"));
				}
			}catch(Exception e){
				log.error(e.getMessage(),e)
			}
			finally {
				rs?.close();
				stmt?.close();
				con?.close();
			}
		}
		
		//println(analysisNameMap)
		// data query
		def finalQuery = analysisQuery + queryCriteria.toString() + "\n) a";
		if (limit > 0) {
			finalQuery += " where a.row_nbr between ${offset+1} and ${offset+limit}"; 
		}
		stmt = con.prepareStatement(finalQuery);
		
		//stmt.setString(1, sortField)
		if (cutoff) {
			stmt.setDouble(1, cutoff);
		}

		log.debug("Executing: " + finalQuery)
	

		def results = []
		try{
			rs = stmt.executeQuery();
			if (analysisNameIncluded) {
				while(rs.next()){
					if ((type.equals("gwas"))) {
						results.push([rs.getString("rsid"), rs.getDouble("pvalue"), rs.getDouble("logpvalue"), rs.getString("extdata"),analysisNameMap.get( rs.getLong("analysis_id")), rs.getString("rsgene"), rs.getString("chrom"), rs.getLong("pos")]);
					}
					else {
						results.push([rs.getString("rsid"), rs.getDouble("pvalue"), rs.getDouble("logpvalue"), rs.getString("extdata"), analysisNameMap.get(rs.getLong("analysis_id")), rs.getString("rsgene"), rs.getString("chrom"), rs.getLong("pos"), rs.getString("gene")]);
					}
				}
			}
			else {
				while(rs.next()){
					if ((type.equals("gwas"))) {
						results.push([rs.getString("rsid"), rs.getDouble("pvalue"), rs.getDouble("logpvalue"), rs.getString("extdata"), "analysis", rs.getString("rsgene"), rs.getString("chrom"), rs.getLong("pos")]);
					}
					else {
						results.push([rs.getString("rsid"), rs.getDouble("pvalue"), rs.getDouble("logpvalue"), rs.getString("extdata"), "analysis", rs.getString("rsgene"), rs.getString("chrom"), rs.getLong("pos"), rs.getString("gene")]);
					}
				}
			}
		}catch(Exception e){
			log.error(e.getMessage(), e);
		}
		finally{
			rs?.close();
			stmt?.close();
		}
		
		//Count - skip if we're not to do this (loading results from cache)
		def total = 0;
		if (doCount) {
			try {
				def finalCountQuery = countQuery + queryCriteria.toString();
				stmt = con.prepareStatement(finalCountQuery)
				if (cutoff) {
					stmt.setDouble(1, cutoff);
				}
				rs = stmt.executeQuery();
				if (rs.next()) {
					total = rs.getLong("TOTAL")
				}
			}
			finally {
				rs?.close();
				stmt?.close();
				con?.close();
			}
		}
		
		return [results: results, total: total];
	}
	
	def quickQueryGwas = """
	
		SELECT analysis, chrom, pos, rsgene, rsid, pvalue, logpvalue, extdata FROM biomart.BIO_ASY_ANALYSIS_GWAS_TOP50
		WHERE analysis = ?
		ORDER BY rnum
	
	"""
	
	def quickQueryEqtl = """
	
		SELECT analysis, chrom, pos, rsgene, rsid, pvalue, logpvalue, extdata, gene FROM biomart.BIO_ASY_ANALYSIS_EQTL_TOP50
		WHERE analysis = ?
		ORDER BY rnum
	
	"""
	
	def getQuickAnalysisDataByName(analysisName, type) {
		
		def con, stmt, rs = null;
		con = dataSource.getConnection()
		StringBuilder queryCriteria = new StringBuilder();
		def quickQuery

		if (type.equals("eqtl")) {
			quickQuery = quickQueryEqtl
		}
		else {
			quickQuery = quickQueryGwas
		}
		
		def results = []
		try {
			stmt = con.prepareStatement(quickQuery)
			stmt.setString(1, analysisName);

			//println("Running shortcut query with name: " + analysisName)
			rs = stmt.executeQuery();
			if (type.equals("eqtl")) {
				while(rs.next()){
					results.push([rs.getString("rsid"), rs.getDouble("pvalue"), rs.getDouble("logpvalue"), rs.getString("extdata"), rs.getString("analysis"), rs.getString("rsgene"), rs.getString("chrom"), rs.getLong("pos"), rs.getString("gene")]);
				}
			}
			else {
				while(rs.next()){
					results.push([rs.getString("rsid"), rs.getDouble("pvalue"), rs.getDouble("logpvalue"), rs.getString("extdata"), "analysis", rs.getString("rsgene"), rs.getString("chrom"), rs.getLong("pos")]);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			rs?.close();
			stmt?.close();
			con?.close();
		}
		
		println("Returning " + results.size())
		return [results: results]
		
	}
  
}