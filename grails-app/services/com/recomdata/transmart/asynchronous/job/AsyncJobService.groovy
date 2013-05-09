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
  

package com.recomdata.transmart.asynchronous.job

import org.apache.commons.lang.StringUtils;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.json.JSONArray;
import org.json.JSONObject;

import groovy.time.*;

import com.recomdata.transmart.domain.i2b2.AsyncJob;

class AsyncJobService {

    boolean transactional = true
	
	def quartzScheduler
	def springSecurityService
	def jobResultsService
	def config = ConfigurationHolder.config
	
	/**
	* Method that will get the list of jobs to show in the jobs tab
	*/
	def getjobs(jobType = null) {
		JSONObject result = new JSONObject()
		JSONArray rows = new JSONArray()
		
		def userName = springSecurityService.getPrincipal().username
		def jobResults = null
		def c = AsyncJob.createCriteria()
		if (StringUtils.isNotEmpty(jobType)) {
			jobResults = c {
				like("jobName", "${userName}%")
				eq("jobType", "${jobType}")
				ge("lastRunOn", new Date()-7)
				order("id", "desc")
			}
		} else {
			jobResults = c {
				like("jobName", "${userName}%")
				or {
					ne("jobType", "DataExport")
					isNull("jobType")
				}
				ge("lastRunOn", new Date()-7)
				order("id", "desc")
			}
		}
		def m = [:]
		for (jobResult in jobResults)	{
			m = [:]
			m["name"] = jobResult.jobName
			m["status"] = jobResult.jobStatus
			m["runTime"] = jobResult.runTime
			m["startDate"] = jobResult.lastRunOn
			m["viewerURL"] = jobResult.viewerURL
			m["altViewerURL"] = jobResult.altViewerURL
			rows.put(m)
		}
		
		result.put("success", true)
		result.put("totalCount", jobResults.size())
		result.put("jobs", rows)
		
		return result
	}
	
	/**
	* Called to retrieve the job results (HTML) stored in the JOB_RESULTS field for Haploview and Survival Analysis
	*/
   def getjobresults(jobName) {
	   JSONObject result = new JSONObject()
	   def jobResults = AsyncJob.findByJobName("${jobName}").results
	   result.put("jobResults", jobResults)
	   return result
   }
	
	/**
	* Method that will create the new asynchronous job name
	* Current methodology is username-jobtype-ID from sequence generator
	*/
	def createnewjob(jobName = null, jobType = null) {
		def userName = springSecurityService.getPrincipal().username
		def jobStatus = "Started"
		
		def newJob = new AsyncJob(lastRunOn:new Date())
		newJob.save()
		
		if (StringUtils.isEmpty(jobName)) {
			def jobNameBuf = new StringBuffer(userName)
			jobNameBuf.append('-')
			if (StringUtils.isNotEmpty(jobType)) jobNameBuf.append(jobType)
			jobNameBuf.append('-').append(newJob.id)
			jobName = jobNameBuf.toString()
		}
		newJob.jobName = jobName 
		newJob.jobType = jobType
		newJob.jobStatus = jobStatus
		newJob.save()
		
		jobResultsService[jobName] = [:]
		updateStatus(jobName, jobStatus)
		
		log.debug("Sending ${jobName} back to the client")
		JSONObject result = new JSONObject()
		result.put("jobName", jobName)
		result.put("jobStatus", jobStatus)
		
		return result;
	}
	
	/**
	* Method called that will cancel a running job
	*/
   def canceljob(jobName, group = null) {
	   def jobStatus = "Cancelled"
	   def result = null
	   log.debug("Attempting to delete ${jobName} from the Quartz scheduler")
	   result = quartzScheduler.deleteJob(jobName, group)
	   log.debug("Deletion attempt successful? ${result}")
	   
	   updateStatus(jobName, jobStatus)
					   
	   JSONObject jsonResult = new JSONObject()
	   jsonResult.put("jobName", jobName)
	   return jsonResult
   }
   
   /**
   * Repeatedly called by datasetExplorer.js to get the job status and results
   */
  def checkJobStatus(jobName, jobType = null) {
	  JSONObject result = new JSONObject()
	  if (StringUtils.isEmpty(jobType)) {
		  def jobNameArray = jobName.split("-")
		  jobType = jobNameArray[1]
	  }
	  def jobStatus = jobResultsService[jobName]["Status"]
	  def statusIndex = null
	  if (jobResultsService[jobName]["StatusList"] != null)	{
		  statusIndex = jobResultsService[jobName]["StatusList"].indexOf(jobStatus)
	  }
	  def jobException = jobResultsService[jobName]["Exception"]
	  def viewerURL = jobResultsService[jobName]["ViewerURL"]
	  def altViewerURL = jobResultsService[jobName]["AltViewerURL"]
	  def jobResults = jobResultsService[jobName]["Results"]
	  def errorType = ""
	  if (viewerURL != null)	{
		  def jobResultType = jobResultsService[jobName]["resultType"]
		  if (jobResultType != null) result.put("resultType", jobResultType)
		  log.debug("${viewerURL} is being sent to the client")
		  result.put("jobViewerURL", viewerURL)
		  if (altViewerURL != null)	{
			  log.debug("${altViewerURL} for Comparative Marker Selection")
			  result.put("jobAltViewerURL", altViewerURL)
		  }
		  jobStatus = "Completed"
	  } else if (jobResults != null)	{
		  result.put("jobResults", jobResults)
		  result.put("resultType", jobType)
		  jobStatus = "Completed"
	  } else if (jobException != null)	{
		  log.warn("An exception was thrown, passing this back to the user")
		  log.warn(jobException)
		  result.put("jobException", jobException)
		  jobStatus = "Error"
		  errorType = "data"
	  }
	  if (statusIndex != null)	{
		  result.put('statusIndexExists', true)
		  result.put("statusIndex", statusIndex)
	  } else {
	  	  result.put('statusIndexExists', false)
	  }
	  
	  updateStatus(jobName, jobStatus, viewerURL, altViewerURL, jobResults)
	  
	  //log.debug("Returning status: ${jobStatus} for ${jobName}")
	  result.put("jobStatus", jobStatus)
	  result.put("errorType", errorType)
	  
	  return result
  }
  
  /**
  * Helper to update the status of the job and log it
  *
  * @param jobName - the unique job name
  * @param status - the new status
  * @param viewerURL - optional, store the viewer URL if the job is completed
  * @param altViewerURL - optional, store the alternate viewer URL for CMS heatmaps
  * @param results - optional, store the results from survival analysis, haploview, etc.
  *
  * @return true if the job was cancelled
  */
  def updateStatus(jobName, status, viewerURL = null, altViewerURL = null, results = null)	{
	 def retValue = false   // true if the job was cancelled
	 def jobNameArray = jobName.split("-")
	 def jobID = jobNameArray[2]
	 
	 //log.debug("Checking to see if the user cancelled the job")
	 if (jobResultsService[jobName]["Status"] == "Cancelled")	{
		 log.warn("${jobName} has been cancelled")
		 retValue = true
	 } else	{
		 jobResultsService[jobName]["Status"] = status
	 }

	 //If the job isn't already cancelled, update the job info.
	 if(!retValue)
	 {
		 def asyncJob = AsyncJob.get(jobID)
		 
		 TimeDuration td = TimeCategory.minus(new Date(), asyncJob.lastRunOn)
		 //log.debug("Job has been running for ${td}}")
		 asyncJob.runTime = td
		 asyncJob.jobStatus = status
		 if (viewerURL && viewerURL != '') asyncJob.viewerURL = viewerURL
		 if (altViewerURL && altViewerURL != '' && asyncJob.altViewerURL != null) asyncJob.altViewerURL = altViewerURL
		 if (results && results != '') asyncJob.results = results
		 
		 //We need to flush so that the value doesn't overwrite cancelled when the controller finishes.
		 asyncJob.save(flush:true)
	 }
	 
	 return retValue
  }
}
