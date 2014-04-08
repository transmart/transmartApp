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

import com.recomdata.export.IgvFiles
import org.transmartproject.core.exceptions.UnexpectedResultException;

class IgvDataService {
	
	def createJNLPasString(webRootDir, sessionFileURL){
		// Create a new file instance
		def f = new File (webRootDir + "/files/" + "igv.jnlp")
		def ftext = f.text;
	
		StringBuilder s = new StringBuilder();
		
		// session file url
		s.append("\t<argument>")
		s.append(sessionFileURL)
		s.append("</argument>\n");
		
		s.append("</application-desc>")
		
		ftext= ftext.replaceAll("</application-desc>",s.toString())
		
		//println("jnlp file:"+ftext)
		return ftext;
		
	}
	
	def createSessionURL(IgvFiles igvFiles, String userName, String locus){
	
			
		File sessionFile = igvFiles.getSessionFile();
		sessionFile << "<?xml version='1.0' encoding='UTF-8' standalone='no'?>\n<Session genome='hg19'"
		if(locus!=null){
		sessionFile <<" locus='"+locus+"' "
		}
		sessionFile<<" version='4'>\n<Resources>\n";
		List<File> fileList = igvFiles.getDataFileList();
		
	/* sessionFile<<"<Resource path='http://localhost:8080/transmartApp/data/test.vcf'/>";
	 session files need to be public available so that igv can check the idx file as well
	 currently it's in the data folder under tomcat
	 need to change to a conf location so that apache can manage the doc root
	*/
	
		for (File file : fileList) {
			
			String fileUrl = igvFiles.getFileUrl(file);
			sessionFile << "<Resource path='" + fileUrl + "'/>\n";
			if(isVCFfile(file)){
			createVCFIndexFile(file);
			}
		}
		
		sessionFile << "</Resources>\n</Session>";
		return igvFiles.getFileUrl(sessionFile);

	}
	
	def isVCFfile(File file){
		println(file.getName())
		return file.getName().toLowerCase().endsWith("vcf");
			
	}
	
	
	def createVCFIndexFile(File vcfFile){
		String[]argv = ["index", vcfFile.absolutePath]

        Class igvToolsClass
		try{
            igvToolsClass = Class.forName 'org.broad.igv.tools.IgvTools'
        } catch (e) {
            log.error 'Could not load IgvTools. The igvtools jar is not ' +
                    'bundled anymore. You will have to add it as a ' +
                    'dependency to the project'
            throw e
        }

        igvToolsClass.newInstance().run argv

        File idxFile = File(vcfFile.absolutePath + ".idx")
		
        idxFile.exists() || {
            throw new UnexpectedResultException('Could not create index ' +
                    "file for ${vcfFile.absolutePath}")
        }()
		
        idxFile
	}
	
}
