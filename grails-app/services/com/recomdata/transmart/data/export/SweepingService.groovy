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

import com.recomdata.transmart.domain.i2b2.AsyncJob;

class SweepingService {

    boolean transactional = true
	
	def grailsApplication

    def sweep() {
		log.error("Triggering file sweep")
		def fileAge = grailsApplication.config.com.recomdata.export.jobs.sweep.fileAge;
		def now = new Date()
		def c = AsyncJob.createCriteria()
		def jobList = c.list{
			eq("jobType", "DataExport")
			eq("jobStatus", "Completed")
			lt('lastRunOn',now-fileAge)
			//between('lastRunOn',now-fileAge, now)
		}
		
		def tempDir = grailsApplication.config.com.recomdata.plugins.tempFolderDirectory
		def ftpServer = grailsApplication.config.com.recomdata.transmart.data.export.ftp.server
		def ftpServerPort = grailsApplication.config.com.recomdata.transmart.data.export.ftp.serverport
		def ftpServerUserName = grailsApplication.config.com.recomdata.transmart.data.export.ftp.username
		def ftpServerPassword = grailsApplication.config.com.recomdata.transmart.data.export.ftp.password
		def ftpServerRemotePath = grailsApplication.config.com.recomdata.transmart.data.export.ftp.remote.path

		def deleteDataFilesProcessor = new DeleteDataFilesProcessor()
		jobList.each{ job->
			if(deleteDataFilesProcessor.deleteDataFile(job.viewerURL, job.jobName, tempDir, ftpServer, ftpServerPort, ftpServerUserName, ftpServerPassword, ftpServerRemotePath)){
				job.delete()
			}
		}
    }
}