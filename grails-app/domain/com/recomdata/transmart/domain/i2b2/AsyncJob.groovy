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
  

package com.recomdata.transmart.domain.i2b2
import java.util.Date;

class AsyncJob {
	String jobName
	String jobStatus
	String runTime
	Date   lastRunOn
	String viewerURL
	String altViewerURL
	String results
	String jobType
	
	static mapping = {
		table 'I2B2DEMODATA.ASYNC_JOB'
		version false
		jobName column:'JOB_NAME'
		jobStatus column:'JOB_STATUS'
		runTime column:'RUN_TIME'
		lastRunOn column:'LAST_RUN_ON'
		viewerURL column:'VIEWER_URL'
		altViewerURL column:'ALT_VIEWER_URL'
		results column:'JOB_RESULTS'
		jobType column:'JOB_TYPE'
	}
	
	static constraints = {
		jobName(nullable:true)
		jobStatus(nullable:true)
		runTime(nullable:true)
		viewerURL(nullable:true)
		altViewerURL(nullable:true)		
		results(nullable:true)
		jobType(nullable:true)
	}
}
