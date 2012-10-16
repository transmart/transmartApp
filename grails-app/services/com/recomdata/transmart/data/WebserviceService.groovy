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

class WebserviceService {
	
    boolean transactional = true

	def dataSource
	def grailsApplication
	def config = ConfigurationHolder.config
	
	def final geneLimitsSqlQueryByKeyword = """
	
	SELECT max(snpinfo.pos) as high, min(snpinfo.pos) as low, min(snpinfo.chrom) as chrom FROM SEARCHAPP.SEARCH_KEYWORD
	INNER JOIN bio_marker bm ON bm.BIO_MARKER_ID = SEARCH_KEYWORD.BIO_DATA_ID
	INNER JOIN deapp.de_snp_gene_map gmap ON gmap.entrez_gene_id = bm.PRIMARY_EXTERNAL_ID
	INNER JOIN DEAPP.DE_RC_SNP_INFO snpinfo ON gmap.snp_name = snpinfo.rs_id
	WHERE KEYWORD=? 
	"""
	
	def final geneLimitsSqlQueryById = """
	
	SELECT BIO_MARKER_ID, max(snpinfo.pos) as high, min(snpinfo.pos) as low, min(snpinfo.chrom) as chrom, min(strand) as strand from bio_marker bm
	INNER JOIN deapp.de_snp_gene_map gmap ON gmap.entrez_gene_id = bm.PRIMARY_EXTERNAL_ID
	INNER JOIN DEAPP.DE_RC_SNP_INFO snpinfo ON gmap.snp_name = snpinfo.rs_id
	WHERE BIO_MARKER_ID = ?
	GROUP BY BIO_MARKER_ID
	"""
	
	def final genePositionSqlQuery = """
		SELECT DISTINCT BIO_MARKER_ID, ENTREZ_GENE_ID, BIO_MARKER_NAME, BIO_MARKER_DESCRIPTION FROM deapp.de_snp_gene_map gmap
		INNER JOIN DEAPP.DE_RC_SNP_INFO snpinfo ON gmap.snp_name = snpinfo.rs_id
		INNER JOIN BIO_MARKER bm ON bm.primary_external_id = to_char(gmap.entrez_gene_id)
		WHERE chrom = ? AND pos >= ? AND pos <= ?
	"""
	
	def final modelInfoSqlQuery = """
		SELECT baa.bio_assay_analysis_id as id, ext.model_name as modelName, baa.analysis_name as analysisName, be.title as studyName
		FROM bio_assay_analysis baa
		LEFT JOIN bio_assay_analysis_ext ext ON baa.bio_assay_analysis_id = ext.bio_assay_analysis_id
		LEFT JOIN bio_experiment be ON baa.etl_id = be.accession
		WHERE baa.bio_assay_data_type = ?
	"""
	
	def computeGeneBounds(String geneSymbol, String geneSourceId) {
		//Complete the query - if we have a geneSymbol, use that, otherwise use ID
		def query = geneLimitsSqlQueryByKeyword;
			
		//Create objects we use to form JDBC connection.
		def con, stmt, rs = null;
		
		//Grab the connection from the grails object.
		con = dataSource.getConnection()
		
		//Prepare the SQL statement.
		stmt = con.prepareStatement(query);
		stmt.setString(1, geneSymbol)
		
		rs = stmt.executeQuery();

		try{
			if(rs.next()){
				def high = rs.getLong("HIGH");
				def low = rs.getLong("LOW");
				def chrom = rs.getLong("CHROM")
				return [low, high, chrom]
			}
		}finally{
			rs?.close();
			stmt?.close();
			con?.close();
		}
	}
	
	def getGeneByPosition(String chromosome, Long start, Long stop) {
		//Complete the query - if we have a geneSymbol, use that, otherwise use ID
		def query = genePositionSqlQuery;
		def geneQuery = geneLimitsSqlQueryById;
			
		//Create objects we use to form JDBC connection.
		def con, stmt, rs = null;
		def geneStmt, geneRs = null;
		
		//Grab the connection from the grails object.
		con = dataSource.getConnection()
		
		//Prepare the SQL statement.
		stmt = con.prepareStatement(query);
		stmt.setString(1, chromosome)
		stmt.setLong(2, start)
		stmt.setLong(3, stop)
		rs = stmt.executeQuery();

		def results = []
		
		geneStmt = con.prepareStatement(geneQuery)
		
		try {
			while(rs.next()) {
				
				def bioMarkerId = rs.getLong("BIO_MARKER_ID")
				
				geneStmt.setLong(1, bioMarkerId)
				geneRs = geneStmt.executeQuery();
				try {
					if(geneRs.next()) {
						results.push([
							bioMarkerId,
							"GRCh37",
							rs.getString("BIO_MARKER_NAME"),
							rs.getString("BIO_MARKER_DESCRIPTION"),
							geneRs.getString("CHROM"),
							geneRs.getLong("LOW"),
							geneRs.getLong("HIGH"),
							geneRs.getString("STRAND"),
							0,
							rs.getLong("ENTREZ_GENE_ID")
						])
					}
				}
				finally {
					geneRs?.close();
				}
			}
			
			return results
		}
		finally {
			rs?.close();
			geneRs?.close();
			stmt?.close();
			geneStmt?.close();
			con?.close();
		}

	}
  
	def getModelInfo(String type) {
		//Complete the query - if we have a geneSymbol, use that, otherwise use ID
		def query = modelInfoSqlQuery;
			
		//Create objects we use to form JDBC connection.
		def con, stmt, rs = null;
		
		//Grab the connection from the grails object.
		con = dataSource.getConnection()
		
		//Prepare the SQL statement.
		stmt = con.prepareStatement(query);
		stmt.setString(1, type)
		
		rs = stmt.executeQuery();

		def results = []
		
		try{
			while(rs.next()){
				def id = rs.getLong("ID");
				def modelName = rs.getString("MODELNAME");
				def analysisName = rs.getString("ANALYSISNAME");
				def studyName = rs.getString("STUDYNAME");

				results.push([id, modelName, analysisName, studyName])
			}
			return results;
		}finally{
			rs?.close();
			stmt?.close();
			con?.close();
		}
	}
	
	def final analysisDataSqlQueryGwas = """
		SELECT gwas.rs_id as rsid, gwas.bio_asy_analysis_gwas_id as resultid, gwas.bio_assay_analysis_id as analysisid, 
    	gwas.p_value as pvalue, gwas.log_p_value as logpvalue, be.title as studyname, baa.analysis_name as analysisname, 
    	baa.bio_assay_data_type AS datatype, info.pos as posstart, info.chrom as chromosome
		FROM biomart.Bio_Assay_Analysis_Gwas gwas
		LEFT JOIN deapp.de_rc_snp_info info ON gwas.rs_id = info.rs_id
		LEFT JOIN biomart.Bio_Assay_Analysis baa ON baa.bio_assay_analysis_id = gwas.bio_assay_analysis_id
		LEFT JOIN biomart.bio_experiment be ON be.accession = baa.etl_id
		WHERE (info.pos BETWEEN ? AND ?)
		AND chrom = ?
    	AND gwas.bio_assay_analysis_id IN (
	"""
	
	def final analysisDataSqlQueryEqtl = """
		SELECT eqtl.rs_id as rsid, eqtl.bio_asy_analysis_data_id as resultid, eqtl.bio_assay_analysis_id as analysisid,
		eqtl.p_value as pvalue, eqtl.log_p_value as logpvalue, be.title as studyname, baa.analysis_name as analysisname,
		baa.bio_assay_data_type AS datatype, info.pos as posstart, info.chrom as chromosome
		FROM biomart.Bio_Assay_Analysis_eqtl eqtl
		LEFT JOIN deapp.de_rc_snp_info info ON eqtl.rs_id = info.rs_id
		LEFT JOIN biomart.Bio_Assay_Analysis baa ON baa.bio_assay_analysis_id = eqtl.bio_assay_analysis_id
		LEFT JOIN biomart.bio_experiment be ON be.accession = baa.etl_id
		WHERE (info.pos BETWEEN ? AND ?)
		AND chrom = ?
		AND eqtl.bio_assay_analysis_id IN (
	"""
	
	def getAnalysisDataBetween(analysisIds, low, high, chrom) {
		//Get all data for the given analysisIds that falls between the limits
		def gwasQuery = analysisDataSqlQueryGwas;
		def eqtlQuery = analysisDataSqlQueryEqtl;
		gwasQuery += analysisIds.join(",") + ")"
		eqtlQuery += analysisIds.join(",") + ")"
			
		def results = []
		
		//Create objects we use to form JDBC connection.
		def con, stmt, rs = null;
		
		//Grab the connection from the grails object.
		con = dataSource.getConnection()
		
		//Prepare the SQL statement.
		stmt = con.prepareStatement(gwasQuery);
		stmt.setLong(1, low)
		stmt.setLong(2, high)
		stmt.setString(3, String.valueOf(chrom))
		
		rs = stmt.executeQuery();
		
		try{
			while(rs.next()){
				results.push([rs.getString("rsid"),
							  rs.getLong("resultid"),
							  rs.getLong("analysisid"),
							  rs.getDouble("pvalue"),
							  rs.getDouble("logpvalue"),
							  rs.getString("studyname"),
							  rs.getString("analysisname"),
							  rs.getString("datatype"),
							  rs.getLong("posstart"),
							  rs.getString("chromosome")])
			}
			return results;
		}finally{
			rs?.close();
			stmt?.close();
		}
		
		//And again for EQTL
		stmt = con.prepareStatement(eqtlQuery);
		stmt.setLong(1, low)
		stmt.setLong(2, high)
		stmt.setString(3, String.valueOf(chrom))
		
		rs = stmt.executeQuery();
		
		try{
			while(rs.next()){
				results.push([rs.getString("rsid"),
							  rs.getLong("resultid"),
							  rs.getLong("analysisid"),
							  rs.getDouble("pvalue"),
							  rs.getDouble("logpvalue"),
							  rs.getString("studyname"),
							  rs.getString("analysisname"),
							  rs.getString("datatype"),
							  rs.getLong("posstart"),
							  rs.getString("chromosome")])
			}
			return results;
		}finally{
			rs?.close();
			stmt?.close();
			con?.close();
		}
	}
}