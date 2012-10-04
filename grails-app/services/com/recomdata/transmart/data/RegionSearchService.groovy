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

import com.recomdata.transmart.data.export.util.FileWriterUtil

class RegionSearchService {
	
    boolean transactional = true

	def dataSource
	def grailsApplication
	def config = ConfigurationHolder.config
	
	def sqlQuery = """
	
	SELECT max(snpinfo.chrom_pos) as high, min(snpinfo.chrom_pos) as low FROM SEARCHAPP.SEARCH_KEYWORD
	INNER JOIN bio_marker bm ON bm.BIO_MARKER_ID = SEARCH_KEYWORD.BIO_DATA_ID
	INNER JOIN deapp.de_snp_gene_map gmap ON gmap.entrez_gene_id = bm.PRIMARY_EXTERNAL_ID
	INNER JOIN DEAPP.DE_SNP_INFO snpinfo ON gmap.snp_name = snpinfo.name
	WHERE SEARCH_KEYWORD_ID=?
	
	"""

	def getGeneLimits(Long searchId) {
		//Create objects we use to form JDBC connection.
		def con, stmt, rs = null;
		
		//Grab the connection from the grails object.
		con = dataSource.getConnection()
		
		//Prepare the SQL statement.
		stmt = con.prepareStatement(sqlQuery);
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
  
}