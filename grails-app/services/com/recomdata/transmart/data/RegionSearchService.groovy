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
	
	SELECT max(snpinfo.pos) as high, min(snpinfo.pos) as low FROM SEARCHAPP.SEARCH_KEYWORD
	INNER JOIN bio_marker bm ON bm.BIO_MARKER_ID = SEARCH_KEYWORD.BIO_DATA_ID
	INNER JOIN deapp.de_snp_gene_map gmap ON gmap.entrez_gene_id = bm.PRIMARY_EXTERNAL_ID
	INNER JOIN DEAPP.DE_RC_SNP_INFO snpinfo ON gmap.snp_name = snpinfo.rs_id
	WHERE SEARCH_KEYWORD_ID=?
	
	"""
	
	//Query with mad Oracle pagination
	def gwasSqlQuery = """
		select * from (select a.*, ROWNUM rnum from 
		(SELECT gwas.bio_assay_analysis_id as analysis, gwas.rs_id as rsid, gwas.p_value as pvalue, gwas.log_p_value as logpvalue, gwas.ext_data as extdata
		FROM biomart.Bio_Assay_Analysis_Gwas gwas 
		LEFT JOIN deapp.de_rc_snp_info info ON gwas.rs_id = info.rs_id 
	"""
	
	def eqtlSqlQuery = """
	    select * from (select a.*, ROWNUM rnum from
	    (SELECT eqtl.bio_assay_analysis_id as analysis, eqtl.rs_id as rsid, eqtl.p_value as pvalue, eqtl.log_p_value as logpvalue, eqtl.ext_data as extdata, eqtl.gene as gene
	    FROM biomart.Bio_Assay_Analysis_eqtl eqtl
	    LEFT JOIN deapp.de_rc_snp_info info ON eqtl.rs_id = info.rs_id
	"""
	
	def gwasSqlCountQuery = """
		SELECT COUNT(*) AS TOTAL FROM biomart.Bio_Assay_Analysis_Gwas gwas LEFT JOIN deapp.de_rc_snp_info info ON gwas.rs_id = info.rs_id 
		
	"""
	
	def eqtlSqlCountQuery = """
	    SELECT COUNT(*) AS TOTAL FROM biomart.Bio_Assay_Analysis_Eqtl eqtl LEFT JOIN deapp.de_rc_snp_info info ON eqtl.rs_id = info.rs_id
	
    """
	
	def getGeneLimits(Long searchId) {
		//Create objects we use to form JDBC connection.
		def con, stmt, rs = null;
		
		//Grab the connection from the grails object.
		con = dataSource.getConnection()
		
		//Prepare the SQL statement.
		stmt = con.prepareStatement(geneLimitsSqlQuery);
		stmt.setLong(1, searchId);

		rs = stmt.executeQuery();

		try{
			if(rs.next()){
				def high = rs.getLong("HIGH");
				def low = rs.getLong("LOW");
				return [low: low, high:high]
			}
		}finally{
			rs?.close();
			stmt?.close();
			con?.close();
		}
	}
	
	def getAnalysisData(analysisIds, ranges, Long limit, Long offset, Double cutoff, String sortField, String order, String search, String type) {
		
		def con, stmt, rs = null;
		con = dataSource.getConnection()
		StringBuilder qb = new StringBuilder();
		def analysisQuery
		def countQuery
		
		if (type.equals("gwas")) {
			analysisQuery = gwasSqlQuery
			countQuery = gwasSqlCountQuery
		}
		else if (type.equals("eqtl")) {
			analysisQuery = eqtlSqlQuery
			countQuery = eqtlSqlCountQuery
		}
		else {
			throw new Exception("Unrecognized data type")
		}

		//Add analysis IDs
		if (analysisIds) {
			qb.append("WHERE BIO_ASSAY_ANALYSIS_ID IN (" + analysisIds[0]);
			for (int i = 1; i < analysisIds.size(); i++) {
				qb.append(", " + analysisIds[i]);
			}
			qb.append(") ")
		}
		else {
			//Quick way to avoid WHERE/AND confusion
			qb.append("WHERE 1=1 ");
		}

		
		if (cutoff) {
			qb.append(" AND p_value <= ?");
		}
		if (search) {
			qb.append(" AND (${type}.rs_id LIKE '%${search}%'")
			qb.append(" OR ${type}.ext_data LIKE '%${search}%'")
			if (type.equals("eqtl")) {
				qb.append(" OR ${type}.gene LIKE '%${search}%'")
			}
			qb.append(") ")
		}
		
		def rangesDone = 0;
		if (ranges) {
			for (range in ranges) {
				if (rangesDone == 0) {
					qb.append(" AND (")
				}
				else {
					qb.append(" OR ")
				}
				//Chromosome
				if (range.chromosome != null) {
					qb.append("(info.pos >= ${range.low} AND info.pos <= ${range.high} AND info.chrom = '${range.chromosome}' AND info.hg_version = '${range.ver}')")
				}
				//Gene
				else {
					qb.append("(info.pos >= ${range.low} AND info.pos <= ${range.high} AND info.hg_version = '${range.ver}')")
				}
				
				rangesDone++
			}
			qb.append(")"); //Finish range selection
		}
		def total = 0;

		def finalQuery = analysisQuery + qb.toString() + " ORDER BY ${sortField} ${order}, ${type}.rowid) a where ROWNUM <= ${limit+offset} ) where rnum >= ${offset}";
		stmt = con.prepareStatement(finalQuery);
		if (cutoff) {
			stmt.setDouble(1, cutoff);
		}

		println("Executing: " + finalQuery)
		rs = stmt.executeQuery();

		def results = []
		try{
			while(rs.next()){
				if ((type.equals("gwas"))) {
					results.push([rs.getString("rsid"), rs.getDouble("pvalue"), rs.getDouble("logpvalue"), rs.getString("extdata"), rs.getLong("analysis")]);
				}
				else {
					results.push([rs.getString("rsid"), rs.getDouble("pvalue"), rs.getDouble("logpvalue"), rs.getString("extdata"), rs.getLong("analysis"), rs.getString("gene")]);
				}
			}
		}finally{
			rs?.close();
			stmt?.close();
		}
		
		try {
			stmt = con.prepareStatement(countQuery + qb.toString())
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
		
		return [results: results, total: total];
	}
  
}