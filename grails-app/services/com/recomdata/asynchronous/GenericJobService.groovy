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

import org.quartz.Job
import org.quartz.JobExecutionContext;


import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;

import com.recomdata.transmart.data.export.util.FTPUtil;
import com.recomdata.transmart.data.export.util.ZipUtil
import com.sun.org.apache.xerces.internal.impl.xpath.regex.RegularExpression.Context;

import org.quartz.Job;
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext;

import org.apache.commons.lang.StringUtils;

import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.*;


/**
 * This class will encompass the job scheduled by Quartz. When the execute method is called we will travel down a list of predefined methods to prep data
 * 
 * @author MMcDuffie
 *
 */
class GenericJobService implements Job {
	
	def springSecurityService 
	def i2b2HelperService 
	def i2b2ExportHelperService 
	def snpDataService
	def dataExportService
	def jobResultsService
	def asyncJobService
	def grailsApplication
	
	String tempFolderDirectory
	
	String jobTmpParentDir
	String jobTmpDirectory
	//This is where all the R scripts get run, intermediate files are created, images are initially saved, etc.
	String jobTmpWorkingDirectory
	String finalOutputFile
	
	def jobDataMap
	def jobName
	
	File jobInfoFile
	
	// TODO -- NEED TO BE REVIEWED (f.guitton@imperial.ac.uk)
	private void init ()
	{
		//Put an entry in our log.
		log.info("${jobName} has been triggered to run ")
		
		//Get the data map which shows the attributes for our job.
		
		//Write our attributes to a log file.
		if (log.isDebugEnabled())	{
			jobDataMap.getKeys().each {_key ->
				log.debug("\t${_key} -> ${jobDataMap[_key]}")
			}
		}
		
		grailsApplication = jobDataMap.get("SGA")
		jobResultsService = jobDataMap.get("SJRS")
		asyncJobService = jobDataMap.get("SAJS")
		dataExportService = jobDataMap.get("SDES")
		
	}
	// --
	
	public void execute (JobExecutionContext jobExecutionContext)
	{
		//We use the job detail class to get information about the job.
		def jobDetail = jobExecutionContext.getJobDetail()
		
		//Gather the jobs info.
		jobName = jobDetail.getName()
		jobDataMap = jobDetail.getJobDataMap()
		
		//Initialize
		init();
		
		tempFolderDirectory = grailsApplication.config.com.recomdata.plugins.tempFolderDirectory
		
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
		
		//JobResult[] jresult
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
			jobResultsService[jobName]["Exception"] = dnfe.getMessage()
			return
		/*}catch(WebServiceException wse)	{
			log.error("WebServiceException thrown executing job: " + wse.getMessage(), wse)
			jobResultsService[jobName]["Exception"] = "There was an error running your job. Please contact an administrator."
			return*/
		} catch(RserveException rse)	{
			log.error("RserveException thrown executing job: " + rse.getMessage(), rse)
			jobResultsService[jobName]["Exception"] = "There was an error running the R script for your job. Please contact an administrator."
			return
		} catch(Exception e)	{
			log.error("Exception thrown executing job: " + e.getMessage(), e)
			def errorMsg = null
			if (e instanceof UndeclaredThrowableException) {
				errorMsg = ((UndeclaredThrowableException)e)?.getUndeclaredThrowable().message
			} else {
				errorMsg = e?.message
			}
			if (!errorMsg?.trim()) {
				errorMsg = "There was an error running your job \'${jobName}\'. Please contact an administrator."
			}
			jobResultsService[jobName]["Exception"] = errorMsg
			return
		} finally {
			if (jobResultsService[jobName]["Exception"] != null) {
				asyncJobService.updateStatus(jobName, "Error", null, null, jobResultsService[jobName]["Exception"])
			}
		}
		
		//Marking the status as complete makes the 
		updateStatus(jobName, "Completed")
	}
	
	private boolean isStudySelected(int studyCnt, List checkboxList) {
		boolean studySelected = false
		for (checkbox in checkboxList) {
			if (StringUtils.contains(checkbox, "subset"+studyCnt)) { 
				studySelected = true
				break
			}
		}
		return studySelected
	}
	
	//This method assumes we have 
	private void getData() throws Exception
	{
		jobDataMap.put('jobTmpDirectory', jobTmpDirectory)
		
		dataExportService.exportData(jobDataMap)
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
				
					String zipFileLoc = (new File(jobTmpDirectory))?.getParent() + File.separator;
					finalOutputFile = ZipUtil.zipFolder(jobTmpDirectory, zipFileLoc + jobDataMap.get("jobName") + ".zip")
					try {
						File outputFile = new File(zipFileLoc+finalOutputFile);
						if (outputFile.isFile()) {
							String ftpServer = grailsApplication.config.com.recomdata.transmart.data.export.ftp.server
							String ftpServerPort = grailsApplication.config.com.recomdata.transmart.data.export.ftp.serverport
							String ftpServerUserName = grailsApplication.config.com.recomdata.transmart.data.export.ftp.username
							String ftpServerPassword = grailsApplication.config.com.recomdata.transmart.data.export.ftp.password
							String ftpServerRemotePath = grailsApplication.config.com.recomdata.transmart.data.export.ftp.remote.path
							String remoteFilePath = FTPUtil.uploadFile(true, outputFile, ftpServer, ftpServerPort, ftpServerUserName, ftpServerPassword, ftpServerRemotePath);
							if (StringUtils.isNotEmpty(remoteFilePath)) {
								//Since File has been uploaded to the FTP server, we can delete the 
								//ZIP file and the folder which has been zipped
								
								//Delete the output Folder
								String outputFolder = null;
								int index = outputFile.name.lastIndexOf('.');
								if (index > 0 && index <= outputFile.name.length() - 2 ) {
									outputFolder = outputFile.name.substring(0, index);
								}
								File outputDir = new File(zipFileLoc+outputFolder)
								if (outputDir.isDirectory()) {
									outputDir.deleteDir()
								}
								
								//Delete the ZIP file 
								outputFile.delete();
							}
						}
					} catch (Exception e) {
						println("Failed to FTP PUT the ZIP file");
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
					jobResultsService[jobName]['resultType'] = "DataExport"
					jobResultsService[jobName]["ViewerURL"] = finalOutputFile
					asyncJobService.updateStatus(jobName, "Rendering Output", finalOutputFile, null, null)
					break;
				case "GSP":
					//Gather the jobs name.
					def jobName = jobDetail.getName()
				
					//Add the link to the output URL to the jobs object. We get the base URL from the job parameters.
					jobResultsService[jobName]["ViewerURL"] = currentStep.value + "?jobName=" + jobName
					break;
			}
		}

	}
	
	private void runRCommandList(stepList)
	{
		
		//We need to get the study ID for this study so we can know the path to the clinical output file.
		def studies = jobDataMap.get("studyAccessions")
		
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
			reformattedCommand = reformattedCommand.replace("||TEMPFOLDERDIRECTORY||", jobTmpDirectory + "subset1_" + studies[0] + File.separator.replace("\\","\\\\"))
			
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
			//x = c.eval(reformattedCommand);
			
			REXP r = c.parseAndEval("try("+reformattedCommand+",silent=TRUE)");
			
			if (r.inherits("try-error")) 
			{
				//Grab the error R gave us.
				String rError = r.asString()
				
				//This is the error we will eventually throw.
				RserveException newError = null
				
				//If it is a friendly error, use that, otherwise throw the default message.
				if(rError ==~ /.*\|\|FRIENDLY\|\|.*/)
				{
					rError = rError.replaceFirst(/.*\|\|FRIENDLY\|\|/,"")
					newError = new RserveException(c,rError);
				}
				else
				{
					log.error("RserveException thrown executing job: " + rError)
					newError = new RserveException(c,"There was an error running the R script for your job. Please contact an administrator.");
				}
				
				throw newError;
				
			}
			
		}
	
	}
	
	
	/**
	* Helper to update the status of the job and log it
	*
	* @param jobName - the unique job name
	* @param status - the new status
	* @return
	*/
   def updateStatus(jobName, status) {
	   jobResultsService[jobName]["Status"] = status
	   log.debug(status)
	   asyncJobService.updateStatus(jobName, status)
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
