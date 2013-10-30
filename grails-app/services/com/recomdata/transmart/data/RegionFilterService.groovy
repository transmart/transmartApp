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

class RegionFilterService {

    boolean transactional = true
	
	def dataSource
	def grailsApplication
	def config = ConfigurationHolder.config
	
	def selectSql = """
		select min(pos) low_pos, max(pos) high_pos, min(chrom) chrom
		from de_rc_snp_info
		where 
	"""
	// gene_name in (?)
	// rs_id in (?)
	def deleteSql = """
		delete from qt_patient_set_collection x
		where result_instance_id = ? and :existsClause (
		select 1
		from qt_patient_set_collection psc, de_subject_sample_mapping ssm, de_variant_subject_summary vss, de_rc_snp_info rsi
		where
		  psc.patient_num=x.patient_num
		  and psc.patient_num = ssm.patient_id
		  and vss.subject_id = ssm.subject_id
		  and rsi.rs_id = vss.rs_id 
		  and rsi.hg_version = ? 
	"""
	//	  and rsi.gene_name(+) in (?)
	//    and rsi.rs_id(+) in (?)
	//    and rsi.chrom(+) = ? and rsi.pos(+) between ? and ?
	def updateSql = """
		update qt_query_result_instance
		set set_size = (select count(1) from qt_patient_set_collection where result_instance_id = ?)
		where result_instance_id = ?
	"""
	
    def filterPatientset(resultInstanceId, region) {

		log.info("resultInstanceId = " + resultInstanceId + ", region = " + region)
		def con = null
		def selectStmt = null
		def selectRs = null
		def deleteStmt = null
		def updateStmt = null
		
		con = dataSource.getConnection()

		try {
			String sql
			// Handle filtering by chromosome and/or basepairs
			if (!"null".equals(region?.position) || !"0".equals(region?.basepairs)) {
				Long lowPos
				Long highPos
				Long chromosome

				// if gene mode, then get range
				if ("gene".equals(region?.mode)) {
					// if rsid list, then query using rsids
					if ("".equals(region?.geneid)) {
						sql = selectSql + "rs_id in ("
					// otherwise, use gene names
					} else {
						sql = selectSql + "gene_name in ("
					}
					sql += getParam(region?.genename) + ")"
					log.info("select sql = " + sql)
					selectStmt = con.prepareStatement(sql)
					setParam(selectStmt, 1, region?.genename)
					selectRs = selectStmt.executeQuery()
					if (selectRs.next()) {
						lowPos = selectRs.getLong("low_pos")
						highPos = selectRs.getLong("high_pos")
						chromosome = selectRs.getLong("chrom")
					}
				// otherwise use specified position
				} else {
					lowPos = Long.parseLong(region?.position)
					highPos = lowPos
					chromosome = Long.parseLong(region?.chromosome)
				}
				
				// adjust position range based on basepair values and range selection
				Long basepairs = new Long(0)
				if (!"0".equals(region?.basepairs)) {
					basepairs = Long.parseLong(region?.basepairs);
					if ("minus".equals(region?.range) || "both".equals(region?.range)) {
						lowPos = lowPos - basepairs
					} else if ("high".equals(region?.range) || "both".equals(region?.range)) {
						highPos = highPos + basepairs
					}
				}
				
				sql = deleteSql + "and rsi.chrom = ? and rsi.pos between ? and ?)"

				
				if ("mutant".equals(region?.inclusionCriteria)) {
					sql = sql.replaceFirst(":existsClause", "not exists")
				} else {
					sql = sql.replaceFirst(":existsClause", "exists")
				}
				log.info("delete sql = " + sql)
				deleteStmt = con.prepareStatement(sql)
				deleteStmt.setLong(1, Long.parseLong(resultInstanceId))
				//deleteStmt.setLong(2, Long.parseLong(resultInstanceId))
				deleteStmt.setLong(2, Long.parseLong(region?.version))
				deleteStmt.setString(3, chromosome.toString())
				deleteStmt.setLong(4, lowPos)
				deleteStmt.setLong(5, highPos)
			// Otherwise handle rsid or genename lists
			} else {
				// if rsid list, then query using rsids
				if ("".equals(region?.geneid)) {
					sql = deleteSql + "and rsi.rs_id in ("
				// otherwise, use gene names
				} else {
					sql = deleteSql + "and rsi.gene_name in ("
				}
				sql += getParam(region?.genename) + ") )"
				
				if ("mutant".equals(region?.inclusionCriteria)) {
					sql = sql.replaceFirst(":existsClause", "not exists")
				} else {
					sql = sql.replaceFirst(":existsClause", "exists")
				}
				log.info("delete sql = " + sql)
				deleteStmt = con.prepareStatement(sql)
				deleteStmt.setLong(1, Long.parseLong(resultInstanceId))
				//deleteStmt.setLong(2, Long.parseLong(resultInstanceId))
				deleteStmt.setLong(2, Long.parseLong(region?.version))
				setParam(deleteStmt, 3, region?.genename)
			}
			
			def result = deleteStmt.executeUpdate()
			log.debug("filtered " + result + " subjects from result instance")
			
			// if subjects were filtered, then update the set size
			if (result > 0) {
				updateStmt = con.prepareStatement(updateSql)
				updateStmt.setLong(1, Long.parseLong(resultInstanceId))
				updateStmt.setLong(2, Long.parseLong(resultInstanceId))
				updateStmt.executeUpdate()
			}
			
		} finally {
			selectRs?.close()
			selectStmt?.close()
			deleteStmt?.close()
			updateStmt?.close()
			con?.close()
		}

    }
	
	private String getParam(String paramValues) {
		String[] values = paramValues.split(",")
		StringBuilder s = new StringBuilder()
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				s.append(",")
			}
			s.append("?")
		}
		return s.toString()
	}
	
	private int setParam(stmt, pos, paramValues) {
		String[] values = paramValues.split(",")
		for (int i = 0; i < values.length; i++) {
			stmt.setString(pos + i, values[i])
		}
		return pos + values.length
	}

}
