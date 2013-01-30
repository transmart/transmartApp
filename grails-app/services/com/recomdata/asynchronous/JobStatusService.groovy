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

import org.codehaus.groovy.grails.commons.ApplicationHolder;

import com.recomdata.transmart.domain.i2b2.AsyncJob;

import groovy.time.TimeCategory;
import groovy.time.TimeDuration;

class JobStatusService {

    boolean transactional = true

    def jobResultsService
	
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
