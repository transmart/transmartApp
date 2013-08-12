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

import com.recomdata.transmart.data.export.util.FileWriterUtil;

class AdditionalDataService {

    boolean transactional = true
	
	def i2b2HelperService
	
	def dataSource
	
	def geneExpressionDataService

    def findAdditionalDataFiles(String resultInstanceId,  studyList) {
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		def filesList = []
		
		String studies = geneExpressionDataService.convertList(studyList, false, 1000)
		
		try {
			def additionalFilesQuery = """
				select * from bio_content b where exists (
				  select distinct s.sample_cd from de_subject_sample_mapping s
				  where s.trial_name in ${studies} and patient_id in (
					SELECT DISTINCT sc.patient_num FROM qt_patient_set_collection sc, patient_dimension pd
					WHERE sc.result_instance_id = CAST(? AS numeric) AND sc.patient_num = pd.patient_num
				  ) and s.sample_cd is not null and b.file_name like s.sample_cd||'%'
				)
				"""
			log.debug(additionalFilesQuery)
			log.debug('ResultInstanceId :: '+resultInstanceId)
			def sample, mapKey, mapValue = null
			filesList = sql.rows(additionalFilesQuery, [study, resultInstanceId])
		} catch (Exception e) {
			log.error("Problem finding Files for Additional Data :: "+e.getMessage())
		} finally {
			sql?.close()
		}
		
		return filesList
    }
	
	def downloadFiles(String resultInstanceId, studyList, File studyDir, String jobName) {
		def filesList = findAdditionalDataFiles(resultInstanceId, studyList)
		if (filesList?.size > 0) {
			def char separator = '\t';
			for (file in filesList) {
				def fileURL = (new StringBuffer(file.CEL_LOCATION).append(file.FILE_NAME).append(file.CEL_FILE_SUFFIX)).toString()
				FileWriterUtil writerUtil = new FileWriterUtil(studyDir, file.FILE_NAME, jobName, "Additional_Files", null, separator)
				writerUtil.writeFile(fileURL, writerUtil.outputFile)
			}
		} else {
			log.debug('No Additional data files found to download')
		}
	}
}
