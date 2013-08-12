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

class I2b2ExportHelperService {

    static transactional = false
	def sessionFactory
	def dataSource;


def  findStudyAccessions( result_instance_ids){
		def rids = []
		for(r in result_instance_ids){
			if(r?.trim()?.length()>0 ){
				rids.add('CAST('+ r + ' AS numeric)');
			}
		}
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
			StringBuilder sqltb=new StringBuilder("select DISTINCT b.TRIAL FROM i2b2demodata.QT_PATIENT_SET_COLLECTION a ").
			append("INNER JOIN i2b2demodata.PATIENT_TRIAL b").
		    	append(" ON a.PATIENT_NUM=b.PATIENT_NUM WHERE RESULT_INSTANCE_ID IN(").
				append(rids.join(", ")).append(")");
			def trials =[]
			sql.eachRow(sqltb.toString(), 
				{row ->
					trials.add(row.TRIAL);
					}
				)
		return trials 
    }
}
