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

import org.apache.commons.lang.StringUtils;

class DataCountService {
	
	def dataSource
    boolean transactional = true

	//For the given list of Subjects get counts of what kind of data we have for those cohorts.
	//We want to return a map that looks like {"PLINK": "102","RBM":"28"}
    def getDataCounts(subjectIds) 
	{
		//This is the map we build for each subset that contains the data type and count for that data type.
		def resultMap = [:]
		
		//For each query we build a string and add the subject IDs. Build the Queries here.
		
		StringBuilder mrnaQuery = new StringBuilder()
		StringBuilder mrnaCelQuery = new StringBuilder()
		StringBuilder clinicalQuery = new StringBuilder()
		StringBuilder rbmQuery = new StringBuilder()
		StringBuilder snpQuery = new StringBuilder()
		
		if (StringUtils.isNotEmpty((String)subjectIds)) {
			//Build the query we use to get MRNA Data. patient_id should be unique to a given study for each patient. 
			//We count the distinct ID's with MRNA data.
			mrnaQuery.append("SELECT count(distinct s.patient_id) FROM de_subject_sample_mapping s WHERE s.patient_id IN (")
			.append(subjectIds).append(") AND s.platform='MRNA_AFFYMETRIX' AND s.assay_id IS NOT NULL");
			
			//Build the query we use to get MRNA "CEL" Data. patient_id should be unique to a given study for each patient.
			//We count the distinct ID's with MRNA data.
			mrnaCelQuery.append("SELECT count(distinct s.patient_id) FROM de_subject_sample_mapping s ")
			.append("INNER JOIN bio_content b on s.trial_name = b.study_name ")
			.append("WHERE s.patient_id IN (").append(subjectIds).append(") AND s.platform='MRNA_AFFYMETRIX' AND s.assay_id IS NOT NULL ")
			.append("AND b.cel_location IS NOT NULL AND s.sample_cd IS NOT NULL")
			
			//Build the query we use to get the clinical data. patient_num should be unique across all studies.
			clinicalQuery.append("SELECT count(distinct obsf.patient_num) FROM observation_fact obsf WHERE obsf.patient_num IN (")
			.append(subjectIds).append(")");
			
			//Build the query we use to get the RBM data. patient_id should be unique across all studies.
			rbmQuery.append("SELECT count(distinct rbm.patient_id) FROM DE_SUBJECT_RBM_DATA rbm WHERE rbm.patient_id IN (")
			.append(subjectIds).append(")");
	
			//Build the query we use to get the RBM data. patient_num should be unique across all studies.
			snpQuery.append("SELECT count(distinct snp.patient_num) FROM de_subject_snp_dataset snp WHERE snp.patient_num IN (")
			.append(subjectIds).append(")");
		}
		
		//Get the count of MRNA Data for the given list of subject IDs.
		resultMap['MRNA'] = StringUtils.isNotEmpty(mrnaQuery.toString()) ? getCountFromDB(mrnaQuery.toString()) : 0
		
		//Get the count of MRNA "CEL" Data for the given list of subject IDs.
		resultMap['MRNA_CEL'] = StringUtils.isNotEmpty(mrnaCelQuery.toString()) ? getCountFromDB(mrnaCelQuery.toString()) : 0
		
		//Get the count of Clinical Data for the given list of subject IDs.
		resultMap['CLINICAL'] = StringUtils.isNotEmpty(clinicalQuery.toString()) ? getCountFromDB(clinicalQuery.toString()) : 0
		
		//Get the count of RBM Data for the given list of subject IDs.
		resultMap['RBM'] = StringUtils.isNotEmpty(rbmQuery.toString()) ? getCountFromDB(rbmQuery.toString()) : 0
		
		//Get the count of RBM Data for the given list of subject IDs.
		resultMap['SNP'] = StringUtils.isNotEmpty(snpQuery.toString()) ? getCountFromDB(snpQuery.toString()) : 0
		
		return resultMap
    }
	
	
	def getCountFromDB(String commandString)
	{
		//We use a groovy object to handle the DB connections.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		//We will store the count results here.
		def patientSampleCount = 0;
		
		//Iterate over results (Should only be 1 row) and grab the count).
		sql.eachRow(commandString,{row->patientSampleCount = row[0];});
		
		return patientSampleCount;
	}

}
