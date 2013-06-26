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

import grails.converters.JSON
import groovy.time.*

import org.apache.commons.lang.StringUtils;
import org.json.*

import com.recomdata.transmart.domain.i2b2.AsyncJob

import com.recomdata.genepattern.WorkflowStatus;
import com.recomdata.genepattern.JobStatus;

class AsyncJobController {
	def quartzScheduler
	def springSecurityService
	def i2b2HelperService
	def jobResultsService
	def dataSource
	def asyncJobService

	static String ASYNC_JOB_WHITE_SPACE_DEFAULT = "0";
	static String ASYNC_JOB_WHITE_SPACE_EMPTY = "";

	/**
	 * Method that will create the get the list of jobs to show in the jobs tab
	 */
	def getjobs = {
		def result = asyncJobService.getjobs(params.jobType)
		
		response.setContentType("text/json")
		response.outputStream << result?.toString()
	}

	/**
	 * Called to retrieve the job results (HTML) stored in the JOB_RESULTS field for Haploview and Survival Analysis
	 */
	def getjobresults = {
		def result = asyncJobService.getjobresults(params.jobName)
		response.setContentType("text/json")
		response.outputStream << result?.toString()
	}

	def createnewjob = {
		def result = asyncJobService.createnewjob(params.jobName, params.jobType)

		response.setContentType("text/json")
		response.outputStream << result?.toString()
	}

	/**
	 * Method called that will cancel a running job
	 */
	def canceljob = {
		def result = asyncJobService.canceljob(params.jobName, params.group)

		response.setContentType("text/json")
		response.outputStream << result?.toString()
	}

	/**
	 * Repeatedly called by datasetExplorer.js to get the job status and results 
	 */
	def checkJobStatus = {
		def result = asyncJobService.checkJobStatus(params.jobName, params.jobType)

		def statusIndexExists = result.get('statusIndexExists')
		if (statusIndexExists) {
			def statusIndex = result.get('statusIndex')
			def statusHtml = g.render(template:"/genePattern/jobStatusList", model:[jobStatuses:jobResultsService[params.jobName]["StatusList"], statusIndex:statusIndex]).toString();
			result.put('jobStatusHTML', statusHtml)
			
			result.remove('statusIndex')
			result.remove('statusIndexExists')
		}
		
		response.setContentType("text/json")
		response.outputStream << result?.toString()
	}
	
	/**
	* Shows the job status window
	*/
   def showJobStatus ={
	   render (view:"/genePattern/workflowStatus")
   }
   
   /**
   * for snp viewer and igv
   */
   
     def showWorkflowStatus ={
	   def wfstatus = session["workflowstatus"];
	   if(wfstatus ==null){
		   wfstatus = new WorkflowStatus();
		   session["workflowstatus"]=wfstatus;
		   session["workflowstatus"].setCurrentJobStatus(new JobStatus(name:"initializing Workflow",status:"R"));
		   }

	   render (view:"/genePattern/workflowStatus");
   }
 
   
   def checkWorkflowStatus ={
   // check session status
	   def wfstatus = session["workflowstatus"];
	   
	   JSONObject result = wfstatus.result;
	   if(result==null){
		   result = new JSONObject();
	   }
	   
	   def statusHtml = g.render(template:"/genePattern/jobStatus", model:[wfstatus:wfstatus]).toString();
	   result.put("statusHTML", statusHtml);
	   println(statusHtml);
	   
	   if(wfstatus.isCompleted()){
		   result.put("wfstatus","completed");
		   wfstatus.rpCount++;
		   result.put("rpCount", wfstatus.rpCount);
	   }else{
	   result.put ("wfstatus","running");
	   }
	   render result.toString();
   }
   
   def cancelJob = {
	   def wfstatus = session["workflowstatus"]
	   wfstatus.setCancelled();
	   render(wfstatus.jobStatusList as JSON)
   }
}
