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
/**
 * 
 */
package com.recomdata.snp

import java.sql.Clob;
import java.util.HashMap;
import javax.sql.rowset.CachedRowSet

import org.springframework.context.ApplicationContext;
import org.codehaus.groovy.grails.web.context.ServletContextHolder as SCH
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes as GA

import com.recomdata.transmart.data.export.util.FileWriterUtil;
import com.sun.rowset.CachedRowSetImpl;


/**
 * @author SMunikuntla
 *
 */
class SnpDao {

	def ApplicationContext ctx = SCH.servletContext.getAttribute(GA.APPLICATION_CONTEXT)
	def dataSource = ctx.getBean('dataSource')
	def i2b2HelperService = ctx.getBean('i2b2HelperService')
	def snpService = ctx.getBean('snpService')
	def springSecurityService = ctx.getBean('springSecurityService')
	def plinkService = ctx.getBean('plinkService')
	
	def getData(String fileName, String jobName, HashMap result_instance_ids) {
		
		getDataForPEDFile(fileName, jobName, result_instance_ids)
		
		getDataForMAPFile(fileName, jobName, result_instance_ids)
		
	}
	
	def private getDataForPEDFile(String fileName, String jobName, HashMap result_instance_ids) {
		def patientConceptCdPEDFileMap = [:]
		
		def subjectIds = getSubjects(result_instance_ids)
		
		if (subjectIds != '') {	
			//def concepts = getConcepts(subjectIds)
			//concepts.each { concept ->
			//}
			
			def snpDataBySampleQry = "SELECT t1.PATIENT_NUM, t1.PED_BY_PATIENT_CHR, t2.PATIENT_GENDER, t2.CONCEPT_CD "
			snpDataBySampleQry <<= "FROM DE_SNP_DATA_BY_PATIENT t1, "
			snpDataBySampleQry <<= "(SELECT DISTINCT PATIENT_NUM, TRIAL_NAME, PATIENT_GENDER, CONCEPT_CD FROM DE_SUBJECT_SNP_DATASET) t2 "
			snpDataBySampleQry <<= "WHERE t1.PATIENT_NUM=t2.PATIENT_NUM and t1.TRIAL_NAME=t2.TRIAL_NAME and "
			snpDataBySampleQry <<= "t1.PED_BY_PATIENT_CHR is not null and t1.PATIENT_NUM IN ( "
			snpDataBySampleQry <<= subjectIds
			snpDataBySampleQry <<= ") ORDER BY t1.PATIENT_NUM, t2.CONCEPT_CD";
			
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
			CachedRowSetImpl cached=new CachedRowSetImpl()
			cached.populate(sql.executeQuery(snpDataBySampleQry))
			cached.beforeFirst()
			
			patientConceptCdPEDFileMap = writePEDData(fileName, jobName, cached)
		}
	}
	
	def private getConcepts(String subjectIds) {
		def conceptsList = []
		if (subjectIds != '') {
			def conceptsQry = 'SELECT DISTINCT CONCEPT_CD FROM DEAPP.DE_SUBJECT_SNP_DATASET WHERE PATIENT_NUM IN ('
			conceptsQry << subjectIds
			conceptsQry << ')'
			
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
			sql.eachRow(conceptsQry) { conceptsList.push(it) }
		}
		return conceptsList
	}
	
	def private String getSubjects(result_instance_ids) {
		def List subjectsSet = new HashSet()
		
		//Get all the patient_ids/subjects
		result_instance_ids.each { resultInstance ->
			subjectsSet.addAll(i2b2HelperService.getSubjectsAsList(resultInstance.value))
		}
		
		def subjectsStr = new StringBuilder();
		subjectsSet.each { subject -> subjectsStr.append(subject).append(',') }
		def subjectIds = (subjectsStr.toString() != '') ? subjectsStr.toString()[0..-2] : '';
		
		return subjectIds
	}
	
	def private getDataForMAPFile(String fileName, String jobName, HashMap result_instance_ids) {
		def subjectIds = getSubjects(result_instance_ids)
		// 0 -- Platform Name   1 -- Trial Name
		def platform = plinkService.getStudyInfoBySubject(subjectIds)[0];
		//writeData(fileName, jobName, cached)
	}
	
	def private Map writePEDData(String fileName, String jobName, CachedRowSet crs, String concept) {
		//TODO Get the dataTypeName from the list of DataTypeNames either from DB or from config file
		def dataTypeName = "SNP"
		def currentConceptCd = ''
		def currentPatientNum = ''
		def CachedRowSet currentCrs = null;
		def patientConceptCdPEDFileMap = [:]
		
		while (crs.next()) {
	        
	        Long patientNum = crs.getLong('PATIENT_NUM')
			Clob pedByPatientChrClob = crs.getClob('PED_BY_PATIENT_CHR')
			
			String patientGender = crs.getString('PATIENT_GENDER')
			String conceptCd = crs.getString('CONCEPT_CD')
			
			if (currentConceptCd != conceptCd && currentPatientNum != patientNum) {
				if (currentCrs && currentCrs.size() > 0) {
					//Write currentCrs to a file
					def dataTypeFolder = currentConceptCd;
					def char separator = '\t';
					File outputFile = FileWriterUtil.setupOutputFile('SNPData_'+currentPatientNum+'_'+currentConceptCd+'.PED', jobName, dataTypeName, dataTypeFolder);
					FileWriterUtil.write(outputFile, separator, currentCrs, null, null);
					
					patientConceptCdPEDFileMap.put(currentPatientNum+'_'+currentConceptCd, currentConceptCd + File.separator + outputFile.getName())
				}
				//Reset everything current
				currentConceptCd = conceptCd
				currentPatientNum = patientNum
				currentCrs = new CachedRowSetImpl()
			}
			
			if (currentCrs) {
				currentCrs.populate(crs);
				currentCrs.setString('PED_BY_PATIENT_CHR', pedByPatientChrClob.getAsciiStream().getText())
			}
	    }
		
		return patientConceptCdPEDFileMap
	}
}
