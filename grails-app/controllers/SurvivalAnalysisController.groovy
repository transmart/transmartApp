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
import org.genepattern.webservice.JobResult;
import org.apache.commons.io.FileUtils

class SurvivalAnalysisController {
	
	def index = { };

	def survivalAnalysisOutput = 
	{
		//Grab the job ID from the query string.
		String jobName = params.jobName
		
		//Create the string that represents the directory to the temporary files.
		String tempDirectory = "C:\\Users\\AppData\\Local\\Temp\\jobs\\${jobName}\\rOutputFiles"
		String tempImageFolder = "C:\\SVN\\trunk\\biomart\\web-app\\images\\tempImages"
		String tempImageLink = "/transmart/images/tempImages/SurvivalCurve_SurvivalCurve.png"
		
		//These are the paths to our files.
		String imageLocation = "${tempDirectory}\\SurvivalCurve_SurvivalCurve.png"
		String tempImageLocation = "${tempImageFolder}\\SurvivalCurve_SurvivalCurve.png"
		String coxLocation = "${tempDirectory}\\CoxRegression_result.txt"
		String survivalLocation = "${tempDirectory}\\SurvivalCurve_FitSummary.txt"
		
		
		//Create objects for the survival curve output files.
		File coxFile = new File(coxLocation);
		File survivalFile = new File(survivalLocation);
		
		//Parse the output files.
		String coxData = parseCoxRegressionStr(coxFile.getText())
		String survivalData = parseSurvivalCurveSummary(survivalFile.getText())
		
		//TODO: Errrm, change this.
		//Move the image to a location where we can actually render it.
		File oldImage = new File(imageLocation);
		File newImage = new File(tempImageLocation);
		FileUtils.copyFile(oldImage,newImage)
		
		render(template: "/plugin/survivalAnalysis_out", model:[imageLocation:tempImageLink,coxData:coxData,survivalData:survivalData])
	}
	
	public String parseCoxRegressionStr(String inStr) {
		StringBuffer buf = new StringBuffer();
		String numSubject, coef, hazardRatio, standardError, pVal, lower95, upper95;
		boolean nextLineHazard = false, nextLine95 = false;
		inStr.eachLine {
			if (it.indexOf("n=") >=0) {
				numSubject = it.substring(it.indexOf("n=") + 2).trim();
			}
			else if (it.indexOf("se(coef)") >= 0) {
				nextLineHazard = true;
			}
			else if (it.indexOf("classList") >= 0 && nextLineHazard == true) {
				nextLineHazard = false;
				String[] resultArray = it.split();
				coef = resultArray[1];
				hazardRatio = resultArray[2];
				standardError = resultArray[3];
				pVal = resultArray[5];
			}
			else if (it.indexOf("lower") >= 0) {
				nextLine95 = true
			}
			else if (it.indexOf("classList") >= 0 && nextLine95 == true) {
				nextLine95 = false;
				String[] resultArray = it.split();
				lower95 = resultArray[3];
				upper95 = resultArray[4];
			}
		}
		buf.append("<table border='1'  width='100%'><tr><th>Number of Subjects</th><td>" + numSubject + "</td></tr>");
		buf.append("<tr><th>Hazard Ratio (95% CI)</th><td>" + hazardRatio + " (" + lower95 + " - " + upper95 + ")</td></tr>");
		buf.append("<tr><th>Relative Risk (p Value)</th><td>" + coef + " (" + pVal + ")</td></tr>");
		buf.append("</table>");
		return buf.toString();
	}

	public String parseSurvivalCurveSummary(String inStr) {
		StringBuffer buf = new StringBuffer();
		buf.append("<table border='1' width='100%'><tr><th>Subset</th><th>Number of Subjects</th><th>Number of Events</th><th>Median Value</th><th>Lower Range of 95% Confidence Level</th><th>Upper Range of 95% Confidence Level</th></tr>")
		inStr.eachLine {
			if (it.indexOf("classList=") >=0) 
			{
				String[] strArray = it.split();
				
				//For each class, extract the name.
				
				
				if (strArray[0].indexOf("classList=") >=0)
					buf.append("<tr><td>" + strArray[0].replace("classList=","").replace("_"," ") + "</td>");

				for(int i = 1; i < 6; i++) 
				{
					String value = strArray[i];
					
					if (value.indexOf("Inf") >= 0) 
					{
						value = "infinity";
					}
					buf.append("<td>" + value + "</td>");
				}
				
				buf.append("</tr>");
			}
		}
		buf.append("</table>");
		return buf.toString();
	}
	

}