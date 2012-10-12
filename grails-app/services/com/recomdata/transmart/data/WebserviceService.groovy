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
  
  
}