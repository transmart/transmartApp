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
import com.recomdata.plugins.PluginDescriptor
import com.recomdata.transmart.domain.i2b2.AsyncJob
import org.json.JSONObject
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.SimpleTrigger
import com.recomdata.asynchronous.GenericJobService;
import org.springframework.web.context.request.RequestContextHolder;

class JobService {

	def jobResultsService
	def jobStatusService
	def quartzScheduler
	def i2b2HelperService
	
	/**
	 * Method that will create the new asynchronous job name
	 * Current methodology is username-jobtype-ID from sequence generator
	 */
	def createnewjob(userName) {
		def analysis = ""
		def jobStatus = "Started"

		def newJob = new AsyncJob(lastRunOn:new Date())
		newJob.save()

		def jobName = userName + "-" + analysis + "-" + newJob.id
		newJob.jobName = jobName
		newJob.jobStatus = jobStatus
		newJob.jobType = analysis
		newJob.jobType = AsyncJob.DATA_ASSOCIATION_JOB_TYPE
		newJob.save()

		jobResultsService[jobName] = [:]
		jobStatusService.updateStatus(jobName, jobStatus)

		//log.debug("Sending ${newJob.jobName} back to the client")
		JSONObject result = new JSONObject()
		result.put("jobName", jobName)
		result.put("jobStatus", jobStatus)

		return result
	}

	/**
	 * Method called that will cancel a running job
	 */
	def canceljob(jobName) {
	  def jobStatus = "Cancelled"
	  def group = "heatmaps"
	  
	  //log.debug("Attempting to delete ${jobName} from the Quartz scheduler")
	  def result = quartzScheduler.deleteJob(jobName, group)
	  //log.debug("Deletion attempt successful? ${result}")
	  
	  jobStatusService.updateStatus(jobName, jobStatus)
					  
	  JSONObject jsonResult = new JSONObject()
	  jsonResult.put("jobName", jobName)
	  
	  return jsonResult
	}

	/**
	 * This method will gather data from the passed in params collection and from the plugin descriptor stored in session to load up the jobs data map.
	 * @param userName
	 * @param params
	 * @return
	 */
	def scheduleJob(userName, params) {
		
		//TODO:Make this whole process generic.
		//The status list for each plugin will be hard coded for now.
		def statusList = [
			"Started",
			"Validating Cohort Information",
			"Triggering Data-Export Job",
			"Gathering Data",
			"Running Conversions",
			"Running Analysis",
			"Rendering Output"
		]
		
		//Set the status list and update the first status.
		jobResultsService[params.jobName]["StatusList"] = statusList
		jobStatusService.updateStatus(params.jobName, statusList[0])

		//Update the status to say we are validating, No validation code yet though.
		jobStatusService.updateStatus(params.jobName, statusList[1])

		//Return if the user cancelled the job.
		if (jobResultsService[params.jobName]["Status"] == "Cancelled")	{return}

		def jdm = new JobDataMap()
		jdm.put("analysis", params.analysis)
		jdm.put("userName", userName)
		jdm.put("jobName", params.jobName)
		
		//Each subset needs a name and a RID. Put this info in a hash.
		def resultInstanceIdHashMap = [:]
		
		if(params.result_instance_id1 != "") resultInstanceIdHashMap["subset1"] = params.result_instance_id1
		if(params.result_instance_id2 != "") resultInstanceIdHashMap["subset2"] = params.result_instance_id2
		
		jdm.put("result_instance_ids",resultInstanceIdHashMap);
		
		//We need to get information from the plugin descriptor in session, Pull it out of session here.
		def analysis = params['analysis']
		PluginDescriptor plugin = ((Map)RequestContextHolder.currentRequestAttributes().getSession().getAttribute(PluginDescriptor.PLUGIN_MAP)).get(analysis)
		
		//Pull the datatype list from the plugin 
		jdm.put("datatypes",plugin.getDataTypes());

		//Grab the conversion steps from the plugin descriptor.
		jdm.put("conversionSteps",plugin.getConverter());
		
		//Grab the analysis steps from the plugin descriptor.
		jdm.put("analysisSteps",plugin.getProcessor());

		//Add the rendering steps from the plugin descriptor.
		jdm.put("renderSteps",plugin.getRenderer());

		//Add the variable map to the jobs data map.
		jdm.put("variableMap", plugin.getVariableMapping());
		
		//Add the pivot flag to the jobs map.
		jdm.put("pivotData", plugin.getPivotData());
		
		//Add each of the parameters from the html form to the job data map.
		params.each{
			currentParam ->
			jdm.put(currentParam.key,currentParam.value);
		}
		
		String variablesConceptPaths = params['variablesConceptPaths'] 
		def conceptPaths = variablesConceptPaths.split("\\|")
		
		List conceptCodesList = new ArrayList()
		conceptPaths.each{conceptPath ->
			conceptCodesList.add(i2b2HelperService.getConceptCodeFromKey("\\\\"+conceptPath.trim()))
			}
		String[] conceptCodesArray = conceptCodesList.toArray()
		
		
		jdm.put("concept_cds", conceptCodesArray );

		def jobDetail = new JobDetail(params.jobName, params.analysis, GenericJobService.class)
		jobDetail.setJobDataMap(jdm)

		if (jobStatusService.updateStatus(params.jobName, statusList[2]))	{
			return
		}
		def trigger = new SimpleTrigger("triggerNow", params.analysis)
		quartzScheduler.scheduleJob(jobDetail, trigger)
	}
}
