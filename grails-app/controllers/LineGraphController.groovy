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
import com.recomdata.transmart.data.export.util.ZipUtil
import org.apache.commons.io.FileUtils

class LineGraphController {
	
	def jobResultsService

def lineGraphOutput = 
	{
		//Grab the job ID from the query string.
		String jobName = params.jobName
		
		//This is the directory to the jobs folders.
		String tempFolderDirectory = grailsApplication.config.com.recomdata.plugins.tempFolderDirectory
		
		//This is a boolean indicating if we need to move the file before serving it to the user.
		boolean transferImageFile = grailsApplication.config.com.recomdata.plugins.transferImageFile
		
		//This is the URL we use to serve the user the image.
		String imageURL = grailsApplication.config.com.recomdata.plugins.analysisImageURL
		
		//Create the string that represents the directory to the temporary files.
		String tempDirectory = "${tempFolderDirectory}${jobName}" + File.separator + "workingDirectory"
		
		//These are the paths to our files.
		String imageLocation = "${tempDirectory}" + File.separator + "LineGraph.png"
		
		//This is the string that will be the URL we use to render the image to the user.
		String modelImageLink = ""
		
		String zipLocation = ""
		String zipLink = ""
		
		try{
			//If we need to use a different location so that the image is under a web path, use the config here.
			if(transferImageFile)
			{
				String tempImageFolder = grailsApplication.config.com.recomdata.plugins.temporaryImageFolder
				String tempImageJobFolder = "${tempImageFolder}" + File.separator + "${jobName}"
				String tempImageLocation = "${tempImageJobFolder}" + File.separator + "LineGraph.png"
				
				//Determine if the folder for this job exists in the temp image directory.
				if(!(new File(tempImageJobFolder).exists()))
				{
					new File(tempImageJobFolder)
				}
				
				//Move the image to a location where we can actually render it.
				File oldImage = new File(imageLocation);
				File newImage = new File(tempImageLocation);
				FileUtils.copyFile(oldImage,newImage)
			
				zipLocation = "${tempImageJobFolder}" + File.separator + "zippedData.zip"
				
				//Create the zip utility.
				ZipUtil newZipFile = new ZipUtil()
				
				newZipFile.zipFolder(tempDirectory,zipLocation)
					
			}
		}catch(Exception e){
			log.error("Exception when rendering result of Line Graph Analysis: " + e.getMessage(), e)
			jobResultsService[jobName]["Exception"] = e.getMessage()
			render(template: "/plugin/renderingError_out")
			return
		}

		modelImageLink = "${imageURL}${jobName}/LineGraph.png"
		zipLink = "${imageURL}${jobName}/zippedData.zip"
		
		
		render(template: "/plugin/lineGraph_out", model:[imageLocation:modelImageLink,zipLocation:zipLink])
	}
}
