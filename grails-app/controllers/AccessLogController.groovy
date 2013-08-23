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
  

import java.text.*;

import org.transmart.AccessLogFilter;
import org.transmart.searchapp.AccessLog;

import com.recomdata.util.ExcelSheet;
import com.recomdata.util.ExcelGenerator;

class AccessLogController {

	def searchService

	def index = { redirect(action:list,params:params) }

	// the delete, save and update actions only accept POST requests
	static allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def list = {
		def startdatestr;
		def enddatestr;
		SimpleDateFormat df1 = new SimpleDateFormat( "MM/dd/yyyy" );
		Calendar c=Calendar.getInstance();
		def filter = session.accesslogFilter
		if(filter==null){
			filter = new AccessLogFilter()
			session.accesslogFilter = filter
		}

		if(params.startdate==null && filter.startdate==null)
		{
	//		startdatestr="01/01/2009"
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(new Date());
			calendar.add(Calendar.DATE, -7);
			startdatestr= df1.format(calendar.getTime());
		}
		else if(params.startdate!=null)
		{
			startdatestr=params.startdate
		}
		else
		{
			startdatestr=df1.format(filter.startdate)
		}
		filter.startdate=df1.parse(startdatestr);
		def start = filter.startdate
		log.info filter.startdate

		if(params.enddate==null && filter.enddate==null)
		{
			enddatestr=df1.format(new Date())
		}
		else if(params.enddate!=null)
		{
			enddatestr=params.enddate
		}
		else
		{
			enddatestr=df1.format(filter.enddate)
		}
		filter.enddate=df1.parse(enddatestr);
		log.info filter.enddate
		c.setTime(filter.enddate)
		c.add(Calendar.DATE, 1)
		def end =c.getTime()

		def pageMap=searchService.createPagingParamMap(params, grailsApplication.config.com.recomdata.admin.paginate.max,0 )
		pageMap['sort']='accesstime'
		pageMap['order']='desc'
		def result3 = AccessLog.createCriteria().list(
		max: pageMap['max'],
		offset: pageMap['offset'],
		sort: pageMap['sort'],
		order: pageMap['order']) {
			between "accesstime", start, end
		}
		def totalcount=result3.totalCount
		render(view:'list',model:[accessLogInstanceList:result3, startdate: df1.format(filter.startdate), enddate: df1.format(filter.enddate), totalcount:totalcount])
	}

	def export =	{
		def startdatestr;
		def enddatestr;
		SimpleDateFormat df1 = new SimpleDateFormat( "MM/dd/yyyy" );
		Calendar c=Calendar.getInstance();
		def filter = session.accesslogFilter
		if(filter==null){
			filter = new AccessLogFilter()
			session.accesslogFilter = filter
		}

		if(params.startdate==null && filter.startdate==null)
		{
			startdatestr="01/01/2009"
		}
		else if(params.startdate!=null)
		{
			startdatestr=params.startdate
		}
		else
		{
			startdatestr=df1.format(filter.startdate)
		}
		filter.startdate=df1.parse(startdatestr);
		def start = filter.startdate
		log.info filter.startdate

		if(params.enddate==null && filter.enddate==null)
		{
			enddatestr=df1.format(new Date())
		}
		else if(params.enddate!=null)
		{
			enddatestr=params.enddate
		}
		else
		{
			enddatestr=df1.format(filter.enddate)
		}
		filter.enddate=df1.parse(enddatestr);
		log.info filter.enddate
		c.setTime(filter.enddate)
		c.add(Calendar.DATE, 1)
		def end =c.getTime()


		def pageMap=searchService.createPagingParamMap(params, grailsApplication.config.com.recomdata.search.paginate.max,0 )
		pageMap['sort']='accesstime'
		pageMap['order']='desc'
		def results = AccessLog.createCriteria().list(
		sort: pageMap['sort'],
		order: pageMap['order']) {
			between "accesstime", start, end
		}
		log.info "TESTcount:"+results.totalCount
		def totalcount=results.totalCount
		def headers=["Access Time", "User", "Event", "Event Message" ]
		def values=[]
		results.each{
			values.add([it.accesstime, it.username, it.event, it.eventmessage ])
		}

		def sheet=new ExcelSheet("sheet1", headers, values);
		def gen = new ExcelGenerator()
		response.setHeader("Content-Type", "application/vnd.ms-excel; charset=utf-8")
		response.setHeader("Content-Disposition", "attachment; filename=\"access_logs.xls\"")
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0")
		response.setHeader("Pragma", "public");
		response.setHeader("Expires", "0");
		response.outputStream<<gen.generateExcel([sheet]);
	}

	def show = {
		def accessLogInstance = AccessLog.get( params.id )

		if(!accessLogInstance) {
			flash.message = "AccessLog not found with id ${params.id}"
			redirect(action:list)
		}
		else { return [ accessLogInstance : accessLogInstance ] }
	}

	def delete = {
		def accessLogInstance = AccessLog.get( params.id )
		if(accessLogInstance) {
			accessLogInstance.delete()
			flash.message = "AccessLog ${params.id} deleted"
			redirect(action:list)
		}
		else {
			flash.message = "AccessLog not found with id ${params.id}"
			redirect(action:list)
		}
	}

	def edit = {
		def accessLogInstance = AccessLog.get( params.id )

		if(!accessLogInstance) {
			flash.message = "AccessLog not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			return [ accessLogInstance : accessLogInstance ]
		}
	}

	def update = {
		def accessLogInstance = AccessLog.get( params.id )
		if(accessLogInstance) {
			accessLogInstance.properties = params
			if(!accessLogInstance.hasErrors() && accessLogInstance.save()) {
				flash.message = "AccessLog ${params.id} updated"
				redirect(action:show,id:accessLogInstance.id)
			}
			else {
				render(view:'edit',model:[accessLogInstance:accessLogInstance])
			}
		}
		else {
			flash.message = "AccessLog not found with id ${params.id}"
			redirect(action:edit,id:params.id)
		}
	}

	def create = {
		def accessLogInstance = new AccessLog()
		accessLogInstance.properties = params
		return ['accessLogInstance':accessLogInstance]
	}

	def save = {
		def accessLogInstance = new AccessLog(params)
		if(!accessLogInstance.hasErrors() && accessLogInstance.save()) {
			flash.message = "AccessLog ${accessLogInstance.id} created"
			redirect(action:show,id:accessLogInstance.id)
		}
		else {
			render(view:'create',model:[accessLogInstance:accessLogInstance])
		}
	}
}
