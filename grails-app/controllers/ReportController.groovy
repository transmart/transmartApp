import com.recomdata.transmart.domain.searchapp.Report
import com.recomdata.transmart.domain.searchapp.ReportItem
import grails.converters.JSON

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
 * Class for controlling Reports.
 * @author MMcDuffie
 *
 */
class ReportController {
	
	def springSecurityService
	
	def saveReport = 
	{
		
		if(params.name && params.conceptList)
		{
			//Initialize new report object.
			def report = new Report()
			
			//Set the report properties from form.
			report.name = params.name
			report.description = params.description
			report.study = params.study
			
			if(params.publicflag == "true")
			{
				report.publicFlag = "Y"
			}
			else
			{
				report.publicFlag = "N"
			}
			
			report.creatingUser = springSecurityService.getPrincipal().username
			
			//Create the report and get its id.
			report.save(flush:true, failOnError:true)
			
			//Loop through the codes and add them to the report.
			params.conceptList.split("\\|").each()
			{
				report.addToReportItems(new ReportItem(reportId:report.id,code:it))
			}
			
			//Create the report and get its id.
			report.save(flush:true, failOnError:true)
			
		}
		else
		{
			throw new Exception("Invalid parameters supplied!")
		}
		
		render "sucess";
	}
	
	def listReports =
	{
		//Get all the reports for this user, and any public reports.
		def reports = Report.findAllByCreatingUserOrPublicFlag(springSecurityService.getPrincipal().username,"Y")
		
		//Pass in the reports, and the username. The username is used to determine whether or not to show the delete link.
		render(template:'/report/list',model:[reports: reports, currentUser: springSecurityService.getPrincipal().username])
	}
	
	/**
	 * This will get all the codes for a report.
	 */
	def retrieveReportCodes =
	{
		//Get the report object.
		def retrievedReport = Report.get(params.reportid)
		
		//Pull the ReportItem objects for this report.
		def retrievedReportItems = retrievedReport.reportItems
		
		//Push the actual code for the ReportItems into an array to be passed back as JSON.
		def codeArray = []
		
		retrievedReportItems.each()
		{
			codeArray.push(it.code)
		}
		
		render codeArray as JSON
	}
	
	def deleteReport =
	{
		def retrievedReport = Report.get(params.reportId)
		
		//Before deleting a report, verify the deleting user is the user who created the report.
		if(springSecurityService.getPrincipal().username != retrievedReport.creatingUser)
		{
			throw new Exception("You do not have permission to delete this report.")
		}
		
		retrievedReport.delete(flush:true)
		
		render "success"
	}
	
	def updateName = {
		def reportId = params["reportId"]
		def name= params["name"]
		def report = Report.get(reportId)
		report.name = name
		report.save(flush:true)
		
		render 'success'
	}
	
	def togglePublicFlag = {
		def reportId = params["reportId"]
		def report = Report.get(reportId)
		if(report.publicFlag=='Y'){
			report.publicFlag='N'
		}else{
			report.publicFlag='Y'
		}
		report.save(flush:true)
		
		render report.publicFlag=='Y'
	}
	
	
}
