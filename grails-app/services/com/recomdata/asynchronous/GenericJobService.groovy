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
package com.recomdata.asynchronous

import com.recomdata.transmart.data.export.exception.DataNotFoundException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.recomdata.gex.GexDao
import com.recomdata.i2b2.I2b2DAO;
//import com.recomdata.snp.SnpDao;
import com.recomdata.transmart.data.export.util.SftpClient;
import com.recomdata.transmart.data.export.util.ZipUtil
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import org.genepattern.webservice.JobResult
import org.genepattern.webservice.WebServiceException

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH


import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.*;

import com.recomdata.dataexport.dao.StudyDao;

/**
 * This class will encompass the job scheduled by Quartz. When the execute method is called we will travel down a list of predefined methods to prep data
 * @author MMcDuffie
 *
 */
class GenericJobService implements Job {

	def ctx = AH.application.mainContext
	def springSecurityService = ctx.springSecurityService
	def jobResultsService = ctx.jobResultsService
	
	def static final String SFTP_REMOTE_DIR_PATH = CH.config.com.recomdata.transmart.data.export.sftp.remote.dir.path;
	def static final String SFTP_SERVER = CH.config.com.recomdata.transmart.data.export.sftp.server;
	def static final String SFTP_SERVER_PORT = CH.config.com.recomdata.transmart.data.export.sftp.serverport;
	def static final String SFTP_USER_NAME = CH.config.com.recomdata.transmart.data.export.sftp.username;
	def static final String SFTP_PASSPHRASE = CH.config.com.recomdata.transmart.data.export.sftp.passphrase;
	def static final String SFTP_PRKEY_FILE = CH.config.com.recomdata.transmart.data.export.sftp.private.keyfile;
	
	def static final String tempFolderDirectory = CH.config.com.recomdata.plugins.tempFolderDirectory
	
	String jobTmpParentDir
	String jobTmpDirectory
	//This is where all the R scripts get run, intermediate files are created, images are initially saved, etc.
	String jobTmpWorkingDirectory
	String finalOutputFile
	
	def jobDataMap
	
	File jobInfoFile
	
	public void execute (JobExecutionContext jobExecutionContext)
	{
		//We use the job detail class to get information about the job.
		def jobDetail = jobExecutionContext.getJobDetail()
		
		//Gather the jobs name.
		def jobName = jobDetail.getName()
		
		//Put an entry in our log.
		log.info("${jobName} has been triggered to run ")
		
		//Get the data map which shows the attributes for our job.
		jobDataMap = jobDetail.getJobDataMap()
		
		//Write our attributes to a log file.
		if (log.isDebugEnabled())	{
			jobDataMap.getKeys().each {_key ->
				log.debug("\t${_key} -> ${jobDataMap[_key]}")
			}
		}

		//Initialize the jobTmpDirectory which will be used during bundling in ZipUtil
		jobTmpDirectory = tempFolderDirectory + File.separator + "${jobName}" + File.separator
		jobTmpDirectory = jobTmpDirectory.replace("\\","\\\\")
		jobTmpWorkingDirectory = jobTmpDirectory + "workingDirectory"
		
		//Try to make the working directory.
		File jtd = new File(jobTmpWorkingDirectory)
		jtd.mkdirs();
		
		//Create a file that will have all the job parameters for debugging purposes.
		jobInfoFile = new File(jobTmpWorkingDirectory + File.separator + 'jobInfo.txt')
		
		//Write our parameters to the file.
		jobInfoFile.write("Parameters" + System.getProperty("line.separator"))
		jobDataMap.getKeys().each {_key ->
			jobInfoFile.append("\t${_key} -> ${jobDataMap[_key]}" + System.getProperty("line.separator"))
		}
		
		JobResult[] jresult
		String sResult
		try	{
			//TODO: Possibly abstract this our so the Quartz job doesn't have all this nonsense.
			
			updateStatus(jobName, "Gathering Data")
			if (isJobCancelled(jobName)) return
			getData()
			
			updateStatus(jobName, "Running Conversions")
			if (isJobCancelled(jobName)) return
			runConversions()
			
			updateStatus(jobName, "Running Analysis")
			if (isJobCancelled(jobName)) return
			runAnalysis()
			
			updateStatus(jobName, "Rendering Output")
			if (isJobCancelled(jobName)) return
			renderOutput(jobDetail)
			
		} catch(DataNotFoundException dnfe){
			log.error("DAO exception thrown executing job: " + dnfe.getMessage(), dnfe)
			jobResultsService[jobName]["Exception"] = "There was an error gathering your data. Please contact an administrator."
			return
		}catch(WebServiceException wse)	{
			log.error("WebServiceException thrown executing job: " + wse.getMessage(), wse)
			jobResultsService[jobName]["Exception"] = "There was an error running your job. Please contact an administrator."
			return
		} catch(RserveException rse)	{
			log.error("RserveException thrown executing job: " + rse.getMessage(), rse)
			jobResultsService[jobName]["Exception"] = "There was an error running the R script for your job. Please contact an administrator."
			return
		} catch(Exception e)	{
			log.error("Exception thrown executing job: " + e.getMessage(), e)
			jobResultsService[jobName]["Exception"] = "There was an error running your job. Please contact an administrator."
			return
		}
		
		//Marking the status as complete makes the 
		updateStatus(jobName, "Completed")
	}
	
	//This method assumes we have 
	private void getData() throws Exception
	{
		//Get the data based on the job configuration.
		def retrievalTypes = jobDataMap.get("datatypes")
		def dataTypesMap = CH.config.com.recomdata.transmart.data.export.dataTypesMap
		def snpFilesMap = [:]

		//This is a list of concept codes that we use to filter the result instance id results.
		String[] conceptCodeList = jobDataMap.get("concept_cds");
		//Make this blank instead of null if we don't find it.
		if(conceptCodeList == null)	conceptCodeList = []
		log.info("retrievalTypes:"+retrievalTypes);
		println("retrievalTypes:"+retrievalTypes);
		
		//log.info("retrieved study data")
		
		//In order to run the DAOs we need a few variables.
		//This is a hashmap that looks like ["subset1":12333,"subset2":204814]. We use it to pass the subsets to our DAO.
		HashMap result_instance_ids = jobDataMap.get("result_instance_ids")
		def studies = (new StudyDao()).getStudies(result_instance_ids)
				
		def pivotDataValueDef = jobDataMap.get("pivotData")
				
		//Pull the data pivot parameter out of the data map.
		boolean pivotData = true
		
		if(pivotDataValueDef==false) 
		{
			pivotData = false
		}
		
		boolean dataFound = false
		for (study in studies) {
			File studyDir = new File(jobTmpDirectory, study)
			studyDir.mkdir()
			//We assume that we get back a list of data types that we need to call the DAO objects to retrieve.
			for(retrievalType in retrievalTypes)
			{
				String[] dataTypeFileType = StringUtils.split(retrievalType, ".")
				
				String dataType;
				if (dataTypeFileType.size() == 1) {
					dataType= retrievalType
				}
				String fileType;
				if (dataTypeFileType.size() > 1) {
					dataType = dataTypeFileType[0].trim().replace(" ","")
					fileType = dataTypeFileType[1].trim().replace(" ","")
				}
				if(null == dataTypesMap.get(dataType)) 
					return;
				
				//For this current data type we call the DAO.
				switch (dataType)
				{
					case "STUDY":
						StudyDao studyDao = new StudyDao();
						studyDao.getData(studyDir, "experimentalDesign.txt", jobDataMap.get("jobName"), jobDataMap.get("studyAccessions"));
						log.info("retrieved study data")
						break;
					case "MRNA":
						GexDao gexDao = new GexDao()
						gexDao.getData(study, studyDir, "mRNA.trans", jobDataMap.get("jobName"), result_instance_ids, false, null, null, null, fileType)
						break
					case "CLINICAL":
						//Moved the code to create Clinical-data after the process for MRNA and SNP is executed
						break
					case "SNP":
						//SnpDao snpDao = new SnpDao();
						//snpFilesMap = snpDao.getData("snp.trans", jobDataMap.get("jobName"), result_instance_ids)
						break
				}
			}
			
			//Reason for moving here: We'll get the map of SNP files from SnpDao to be output into Clinical file
			I2b2DAO i2b2Dao = new I2b2DAO()
			//Call the object that is going to create our temporary data file.
			i2b2Dao.getData(study, studyDir, "clinical.i2b2trans", jobDataMap.get("jobName"), jobDataMap.get("result_instance_ids"), conceptCodeList, retrievalTypes, pivotData)
			dataFound = i2b2Dao.wasDataFound()
		}
		//if i2b2Dao was not able to find data for any of the studies associated with the result instance ids, throw an exception.
		//Currently if we have data for atleast one study, we are good.
		if(!dataFound){
			throw new DataNotFoundException()
		}

	}
	
	private void runConversions()
	{
		//Get the data based on the job configuration.
		def conversionSteps = jobDataMap.get("conversionSteps")
		
		conversionSteps.each
		{
			currentStep ->

			switch (currentStep.key)
			{
				case "R":
				
					//Call a function to process our R commands.
					runRCommandList(currentStep.value);
			}
			
		}
	}
	
	private void runAnalysis()
	{
		//Get the data based on the job configuration.
		def analysisSteps = jobDataMap.get("analysisSteps")
		
		analysisSteps.each
		{
			currentStep ->
			
			switch (currentStep.key)
			{
				case "bundle":
				
					/** Access the ZipUtil in a static way */
					String zipFileLoc = System.getProperty("java.io.tmpdir") + "jobs" + File.separator
					finalOutputFile = ZipUtil.zipFolder(jobTmpDirectory, zipFileLoc + jobDataMap.get("jobName") + ".zip")
					try {
						SftpClient sftpClient = new SftpClient(SFTP_SERVER, SFTP_USER_NAME,
								SFTP_PRKEY_FILE, Integer.parseInt(SFTP_SERVER_PORT),
								SFTP_PASSPHRASE);
						sftpClient.changeDirectory(SFTP_REMOTE_DIR_PATH);
						sftpClient.putFile(finalOutputFile);
			
						sftpClient.close();
					} catch (Exception e) {
						//log.error("Failed to SFTP PUT the ZIP file");
					}
					break
				case "R":
				
					//Call a function to process our R commands.
					runRCommandList(currentStep.value);
					break
			}
			
		}
	}
	
	private void renderOutput(jobDetail)
	{
		//Get the data based on the job configuration.
		def renderSteps = jobDataMap.get("renderSteps")
			
		renderSteps.each
		{
			currentStep ->
		
			switch (currentStep.key)
			{
				case "FILELINK":
				
					//Gather the jobs name.
					def jobName = jobDetail.getName()
					
					//Add the result file link to the job.
					jobResultsService[jobName]["ViewerURL"] = finalOutputFile
					
				case "GSP":
					//Gather the jobs name.
					def jobName = jobDetail.getName()
				
					//Add the link to the output URL to the jobs object. We get the base URL from the job parameters.
					jobResultsService[jobName]["ViewerURL"] = currentStep.value + "?jobName=" + jobName
			}
		}

	}
	
	private void runRCommandList(stepList)
	{
		
		//We need to get the study ID for this study so we can know the path to the clinical output file.
		HashMap result_instance_ids = jobDataMap.get("result_instance_ids")
		def studies = (new StudyDao()).getStudies(result_instance_ids)
		
		//String representing rOutput Directory.
		String rOutputDirectory = jobTmpWorkingDirectory
	
		//Make sure an rOutputFiles folder exists in our job directory.
		new File(rOutputDirectory).mkdir()
	
		//Establish a connection to R Server.
		RConnection c = new RConnection();
		
		log.debug("Attempting following R Command : " + "setwd('${rOutputDirectory}')".replace("\\","\\\\"))
		println("Attempting following R Command : " + "setwd('${rOutputDirectory}')".replace("\\","\\\\"))
		
		//Set the working directory to be our temporary location.
		String workingDirectoryCommand = "setwd('${rOutputDirectory}')".replace("\\","\\\\")
		
		//Run the R command to set the working directory to our temp directory.
		REXP x = c.eval(workingDirectoryCommand);
		
		//For each R step there is a list of commands.
		stepList.each
		{
			currentCommand ->
			
			//Need to escape backslashes for R commands.
			String reformattedCommand = currentCommand.replace("\\","\\\\")
			
			//Replace the working directory flag if it exists in the string.
			reformattedCommand = reformattedCommand.replace("||TEMPFOLDERDIRECTORY||", jobTmpDirectory + studies[0] + File.separator.replace("\\","\\\\"))
			
			//We need to loop through the variable map and do string replacements on the R command.
			jobDataMap.get("variableMap").each
				{
					variableItem -> 
					
					//Try and grab the variable from the Job Data Map. These were fed in from the HTML form.
					def valueFromForm = jobDataMap.get(variableItem.value)
					
					//Clean up the variable if it was found in the form.
					if(valueFromForm)
					{
						valueFromForm = valueFromForm.replace("\\","\\\\").trim()
					}
					else
					{
						valueFromForm = ""
					}
					
					reformattedCommand = reformattedCommand.replace(variableItem.key,valueFromForm)
				}
				
			log.debug("Attempting following R Command : " + reformattedCommand)
			println("Attempting following R Command : " + reformattedCommand)
			
			//Run the R command against our server.
			x = c.eval(reformattedCommand);
		}
	
	}
	
	
	/**
	* Helper to update the status of the job and log it
	*
	* @param jobName - the unique job name
	* @param status - the new status
	* @return
	*/
   def updateStatus(jobName, status)	{
	   jobResultsService[jobName]["Status"] = status
	   log.debug(status)
   }
   
   def boolean isJobCancelled(jobName) {
	   boolean jobCancelled = false
	   //log.debug("Checking to see if the user cancelled the job")
	   if (jobResultsService[jobName]["Status"] == "Cancelled")	{
		   log.warn("${jobName} has been cancelled")
		   jobCancelled = true
	   }
	   return jobCancelled
   }
   
}
