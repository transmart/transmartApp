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
  

package com.recomdata.transmart.externaltool

import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib
import com.recomdata.export.IgvFiles
import com.recomdata.transmart.externaltool.IgvDataService


class IgvController {

	def springSecurityService;
	def igvDataService
	
    def index = {
		
		def webRootDir = servletContext.getRealPath ("/")
		
		// get data first
		//String newIGVLink = new ApplicationTagLib().createLink(controller:'analysis', action:'getGenePatternFile', absolute:true)
		String fileDirName = grailsApplication.config.com.recomdata.analysis.data.file.dir;
		if(fileDirName == null)
		fileDirName = "data";
		String newIGVLink = new ApplicationTagLib().createLink(controller:fileDirName, , absolute:true)

		IgvFiles igvFiles = new IgvFiles(getIgvFileDirName(),newIGVLink)

		// testing data
		def f = new File (webRootDir + "/data/" + "test.vcf")
		igvFiles.addFile(f);
		
		String userName = springSecurityService.getPrincipal().username;
	
		
		// create session file URL
		def sessionfileURL = igvDataService.createSessionURL(igvFiles, userName)
		
		// create JNLP file
		
		def ftext= igvDataService.createJNLPasString(webRootDir, sessionfileURL);
		
		
		
		response.setHeader("Content-Type", "application/x-java-jnlp-file")
		
		//println(ftext)
		response.outputStream<<ftext
		
		//redirect(url:"http://www.broadinstitute.org/igv/projects/current/igv.php?sessionURL=http://www.broadinstitute.org/igvdata/1KG/pilot2Bams/NA12878.SLX.bam&genome=hg18&locus=chr1:64,098,103-64,098,175")
		 }
	
	
	
	protected String getIgvFileDirName() {
		String fileDirName = grailsApplication.config.com.recomdata.analysis.data.file.dir;
		def webRootName = servletContext.getRealPath("/");
		if (webRootName.endsWith(File.separator) == false)
			webRootName += File.separator;
		return webRootName + fileDirName;
	}
}
