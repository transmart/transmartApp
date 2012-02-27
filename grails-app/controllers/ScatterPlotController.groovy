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

class ScatterPlotController {

	def scatterPlotOut = 
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
		String imageLocation = "${tempDirectory}" + File.separator + "ScatterPlot.png"
		String linearLocation = "${tempDirectory}" + File.separator + "LinearRegression.txt"
		
		//Create objects for the linear regression output files.
		File linearFile = new File(linearLocation);
		
		//Parse the output files.
		String linearRegressionData = parseLinearRegressionStr(linearFile.getText())
				
		//This is the string that will be the URL we use to render the image to the user.
		String modelImageLink = ""
		
		String zipLocation = ""
		String zipLink = ""
		
		//If we need to use a different location so that the image is under a web path, use the config here.
		if(transferImageFile)
		{
			String tempImageFolder = grailsApplication.config.com.recomdata.plugins.temporaryImageFolder
			String tempImageJobFolder = "${tempImageFolder}" + File.separator + "${jobName}"
			String tempImageLocation = "${tempImageJobFolder}" + File.separator + "ScatterPlot.png"
			
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
			zipLink = "${imageURL}${jobName}/zippedData.zip"
			
			//Create the zip utility.
			ZipUtil newZipFile = new ZipUtil()
	
			newZipFile.zipFolder(tempDirectory,zipLocation)
			
			zipLink = "${imageURL}${jobName}/zippedData.zip"
		}

		modelImageLink = "${imageURL}${jobName}/ScatterPlot.png"
		
		render(template: "/plugin/scatterPlot_out", model:[imageLocation:modelImageLink,linearRegressionData:linearRegressionData,zipLink:zipLink])
	}
	
	public String parseLinearRegressionStr(String inStr) {
		StringBuffer buf = new StringBuffer();
		
		String numSubject
		String intercept
		String slope
		String rSquared
		String adjRSquared
		String pValue
		
		inStr.eachLine {
			if (it.indexOf("n=") >=0)					{numSubject 	= it.substring(it.indexOf("n=") + 2).trim();}
			else if (it.indexOf("intercept=") >=0)		{intercept 		= it.substring(it.indexOf("intercept=") + 10).trim();}
			else if (it.indexOf("slope=") >=0)			{slope 			= it.substring(it.indexOf("slope=") + 6).trim();}
			else if (it.indexOf("nr2=") >=0)			{rSquared 		= it.substring(it.indexOf("nr2=") + 4).trim();}
			else if (it.indexOf("ar2=") >=0)			{adjRSquared 	= it.substring(it.indexOf("ar2=") + 4).trim();}
			else if (it.indexOf("p=") >=0)				{pValue 		= it.substring(it.indexOf("p=") + 2).trim();}
		}
		
		buf.append("<table class='AnalysisResults' width='30%'><tr><th>Number of Subjects</th><td>${numSubject}</td></tr>");
		buf.append("<tr><th>Intercept</th><td>${intercept}</td></tr>");
		buf.append("<tr><th>Slope</th><td>${slope}</td></tr>");
		buf.append("<tr><th>r-squared</th><td>${rSquared}</td></tr>");
		buf.append("<tr><th>adjusted r-squared</th><td>${adjRSquared}</td></tr>");
		buf.append("<tr><th>p-value</th><td>${pValue}</td></tr>");
		buf.append("</table>");
		return buf.toString();
	}
	
}
