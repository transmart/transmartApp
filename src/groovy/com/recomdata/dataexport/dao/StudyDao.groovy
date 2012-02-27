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
package com.recomdata.dataexport.dao

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.RConnection
import org.springframework.context.ApplicationContext

import bio.ClinicalTrial
import bio.Experiment
import bio.Taxonomy
import bio.Compound
import bio.Disease

import com.recomdata.transmart.data.export.util.FileWriterUtil
import org.apache.commons.logging.LogFactory;


/**
 * This class will provide access to the study metadata
 */

public class StudyDao
{
	ApplicationContext ctx = org.codehaus.groovy.grails.web.context.ServletContextHolder.getServletContext().getAttribute(org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes.APPLICATION_CONTEXT)
	def dataSource = ctx.getBean('dataSource')
	def springSecurityService = ctx.getBean('springSecurityService')
	def dataTypeName = "Study";
	def dataTypeFolder = null;
	def char separator = '\t';
	private static final log = LogFactory.getLog('grails.app.' +StudyDao.class.name)
		
		
	//This is the list of parameters passed to the SQL statement.
	ArrayList parameterList = new ArrayList();
	
	def config = ConfigurationHolder.config
	
	/**
	 * This method will gather study data and write it to a file.
	 *  The file will contain basic study metadata
	 * @param fileName
	 * @param jobName
	 * @param studyAccessions
	 */
	public void getData(File studyDir, String fileName, String jobName, List<String> studyAccessions) 
	{
		//Log the action of data access.
		//def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"i2b2DAO - getData", eventmessage:"RID:"+result_instance_ids.toString()+" Concept:"+conceptCodeList.toString(), accesstime:new java.util.Date())
		//al.save()
		//TODO Get the dataTypeName from the list of DataTypeNames either from DB or from config file
		def dataTypeName = null
		//TODO set this to either "Raw_Files/Findings" or NULL for processed_files
		def dataTypeFolder = null
		def char separator = '\t'
		log.info("loading study metadata for "+studyAccessions)
		
		FileWriterUtil writerUtil = new FileWriterUtil(studyDir, fileName, jobName, dataTypeName, dataTypeFolder, separator)
		writerUtil.writeLine(getStudyColumns() )
		// try to find it by Clinical Trial 
		
		studyAccessions.each {
				studyUid -> 
				def isTrial = true;
				// work around to fix the lazy loading issue - we don't have full transaction support there
				 def exp = ClinicalTrial.findByTrialNumber(studyUid);
					//def exp =	ClinicalTrial.executeQuery("SELECT DISTINCT ct FROM ${ClinicalTrial.name} ct LEFT JOIN FETCH ct.organisms LEFT JOIN FETCH ct.compounds LEFT JOIN FETCH ct.diseases WHERE ct.trialNumber=?",studyUid);
				//	def exp =	ClinicalTrial.find(" FROM ClinicalTrial as ct WHERE ct.trialNumber = :uid",[uid:studyUid]);
				//	def exp =	ClinicalTrial.executeQuery("SELECT DISTINCT ct.trialNumber, ct.organisms, ct.compounds, ct.diseases FROM ${ClinicalTrial.name} ct LEFT JOIN FETCH ct.organisms LEFT JOIN FETCH ct.compounds LEFT JOIN FETCH ct.diseases WHERE ct.trialNumber = :uid",[uid:studyUid]);
				
				if(exp==null){
						exp = Experiment.findByAccession(studyUid);
					//	exp = Experiment.executeQuery("SELECT DISTINCT ct FROM bio.Experiment ct LEFT JOIN FETCH ct.organisms LEFT JOIN FETCH ct.compounds LEFT JOIN FETCH ct.diseases");
						

						isTrial = false;
				}
				def organisms = Taxonomy.findAll(new Taxonomy(experiments:[exp]))
				def compounds = Compound.findAll(new Compound(experiments:[exp]))
			
			//	exp.compounds;
			//	exp.organisms;
			//	exp.diseases;
				
				if(exp!=null){
					writerUtil.writeLine( getStudyData(exp, organisms, compounds));
				}
		}
	
	
	//def filePath = writerUtil.outputFile.getAbsolutePath()
	writerUtil.finishWriting()
	
//	return filePath		

	}
	
	
	
	protected String[] getStudyColumns(){
		
		/*def headers1=["Title" , "Trial Number", "Owner", "Description", "Study Phase", "Study Type", "Study Design", "Blinding procedure",
			"Duration of study (weeks)", "Completion date", "Inclusion Criteria", "Exclusion Criteria", "Dosing Regimen",
			"Type of Control", "Gender restriction mfb", "Group assignment", "Primary endpoints", "Secondary endpoints",
			"Route of administration", "Secondary ids", "Subjects", "Max age", "Min age", "Number of patients", "Number of sites", "Compounds", "Diseases", "Organisms"];	
		*/
		def headers1 = ["Title",
			"Date",
			 "Owner",
			  "Institution",
			   "Country", 
			   "Description",
			   "Access Type", 
			   "Phase", 
			   "Objective", 
			   "BioMarker Type", 
			   "Compound", 
			   "Design Factor", 
			   "Number of Patients",
			   "Organism",
			    "Target/Pathways"
				 ]
		return headers1;
	}
	
	protected String[] getStudyData(Experiment exp, organisms, compounds){
		def data =[];
		if(exp instanceof ClinicalTrial){
			data.add(exp.title)
			data.add(exp.completionDate)
			data.add(exp.studyOwner)
			data.add(exp.institution)
			data.add(exp.country)
			data.add(exp.description)
			data.add(exp.accessType)
			data.add(exp.studyPhase)
			data.add(exp.design)
			data.add(exp.bioMarkerType)
			data.add(getCompoundNames(compounds))
			data.add(exp.overallDesign)
			data.add(exp.numberOfPatients)
			data.add(getOrganismNames(organisms))
			data.add(exp.target)
		}else{
			data.add(exp.title)
			data.add(exp.completionDate)
			data.add(exp.primaryInvestigator)
			data.add(exp.institution)
			data.add(exp.country)
			data.add(exp.description)
			data.add(exp.accessType)
			data.add("")
			data.add(exp.design)
			data.add(exp.bioMarkerType)
			data.add(getCompoundNames(compounds))
			data.add(exp.overallDesign)
			data.add("")
			data.add(getOrganismNames(organisms))
			data.add(exp.target)
		}
		
		return data as String[]
	}
	
	
	def getCompoundNames(compounds)	{
		StringBuilder compoundNames = new StringBuilder()
		compounds.each{
			if (it.getName() != null)	{
				if (compoundNames.length() > 0)	{
					compoundNames.append("; ")
				}
				compoundNames.append(it.getName())
			}
		}
		return compoundNames.toString()
	}
	
	def getOrganismNames(organisms)	{
		StringBuilder taxNames = new StringBuilder()
		organisms.each{
			if (it.label != null)	{
				if (taxNames.length() > 0)	{
					taxNames.append("; ")
				}
				taxNames.append(it.label)
			}
		}
		return taxNames.toString()
	}
	
	def getStudies(resultInstanceIds) {
		def resultInstancesStr = ''
		resultInstanceIds.each { key, value ->
			if (value && value.toString().trim() != '') {
				resultInstancesStr <<= value
				resultInstancesStr <<= ', '
			}
		}
		def values = resultInstancesStr[0..-3]
		StringBuilder studiesSql = new StringBuilder()
		studiesSql.append("SELECT DISTINCT ofa.modifier_cd FROM qt_patient_set_collection qt ")
		studiesSql.append("INNER JOIN OBSERVATION_FACT ofa ON qt.PATIENT_NUM = ofa.PATIENT_NUM WHERE ")
		studiesSql.append("qt.result_instance_id in (").append(values).append(") AND ofa.modifier_cd <> 'Across Trials'")
		
		//Build query to get the studies
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		def studies = []
		sql.eachRow(studiesSql.toString()) { row ->
			 studies.add(row.MODIFIER_CD)
		}
		
		return studies
	}
	
}