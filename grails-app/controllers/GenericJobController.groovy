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
  

import com.recomdata.transmart.domain.i2b2.AsyncJob
import org.json.JSONObject

class GenericJobController {

	def springSecurityService
	def jobResultsService
	def asyncJobService
	
	/**
	 * Method that will create the new asynchronous job name
	 * Current methodology is username-jobtype-ID from sequence generator
	 */
	def createnewjob = {
		def userName = springSecurityService.getPrincipal().username
		def analysis = request.getParameter("analysis")
		def jobStatus = "Started"
		
		def newJob = new AsyncJob(lastRunOn:new Date())
		newJob.save()
		
		def jobName = userName + "-" + analysis + "-" + newJob.id
		newJob.jobName = jobName 
		newJob.jobStatus = jobStatus
		newJob.jobType = analysis
		newJob.save() 
		
		jobResultsService[jobName] = [:]
		asyncJobService.updateStatus(jobName, jobStatus)
		
		log.debug("Sending ${jobName} back to the client")		
		JSONObject result = new JSONObject()
		result.put("jobName", jobName)
		result.put("jobStatus", jobStatus)
		response.setContentType("text/json")
		response.outputStream << result.toString()		
	}
}
