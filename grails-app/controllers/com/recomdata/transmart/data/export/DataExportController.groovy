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

import org.json.JSONObject;

import com.recomdata.transmart.domain.i2b2.AsyncJob;

class DataExportController {

    def index = { }
	
	def exportService
	def springSecurityService
	
	//We need to gather a JSON Object to represent the different data types.
	def getMetaData =
	{
		response.setContentType("text/json")
		render exportService.getMetaData(params)
	}
	
	def downloadFileExists = {
		def InputStream inputStream = exportService.downloadFile(params);
		response.setContentType("text/json")
		JSONObject result = new JSONObject()
		
		if(null != inputStream){
			result.put("fileStatus", true)
		} else {
		   	result.put("fileStatus", false)
			result.put("message", "Download failed as file could not be found on the server")
	    }
		response.outputStream << result.toString()
	}
	
	def downloadFile = {
		def InputStream inputStream = exportService.downloadFile(params);
		
		def fileName = params.jobname + ".zip"
		response.setContentType "application/octet-stream"
		response.setHeader "Content-disposition", "attachment;filename=${fileName}"
		response.outputStream << inputStream
		response.outputStream.flush()
		inputStream.close();
		return true;
	}
	
	/**
	* Method that will create the new asynchronous job name
	* Current methodology is username-jobtype-ID from sequence generator
	*/
   def createnewjob = {
	   def result = exportService.createExportDataAsyncJob(params, springSecurityService.getPrincipal().username)
	   
	   response.setContentType("text/json")
	   response.outputStream << result.toString()
   }
	
	
	/**
	* Method that will run a data export and is called asynchronously from the datasetexplorer -> Data Export tab
	*/
    def runDataExport = 	{
		def jsonResult = exportService.exportData(params, springSecurityService.getPrincipal().username)
		
		response.setContentType("text/json")
		response.outputStream << jsonResult.toString()
    }
}


