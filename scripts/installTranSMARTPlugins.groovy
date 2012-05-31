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
  

import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

/**
 *	Explodes the plugins in the TranSMART plugin folder.
 */
Ant = new AntBuilder();
grailsHome = Ant.project.properties."environment.GRAILS_HOME"
basedir = System.getProperty("base.dir")
pluginFiles = "${basedir}/plugins/ext-2.2.zip"
jsDir = "${basedir}/web-app/js/plugin"
gspDir = "${basedir}/grails-app/views/plugin"
rDir = ""

//This is our plugin zip directory.
def zipDir = new File("${basedir}/plugins/")

//For each of the zips in this directory, instruct ant to unzip the file and move the contents to the directory specified by their file extension.
zipDir.eachFile{ zipFile->

	//If we find a file.
	if(zipFile.isFile())
	{
		//Unzip file to a temp directory.
		Ant.unzip(src: "${zipFile}", dest: "${basedir}/plugins/temp/")

		fileDir = new File("${basedir}/plugins/temp/")

		fileDir.eachFile{ file->
			if(file.isFile())
			{
				if(file.name.contains(".gsp")) Ant.move(file: "${file}", tofile: "${gspDir}/${file.name}")
				if(file.name.contains(".js")) Ant.move(file: "${file}", tofile: "${jsDir}/${file.name}")
				if(file.name.contains(".r")) Ant.move(file: "${rDir}", tofile: "${rDir}/${file.name}")
			}
		}
		
		//TODO: Delete temp directory?

	}
}
