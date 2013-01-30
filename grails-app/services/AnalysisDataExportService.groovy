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
  

import com.recomdata.util.ExcelSheet;
import com.recomdata.util.ExcelGenerator;
import javax.servlet.http.HttpServletResponse;

import org.transmart.SearchResult;
import org.transmart.biomart.BioAssayAnalysis;
import org.transmart.biomart.BioAssayAnalysisData;

import org.transmart.biomart.BioMarker
import org.transmart.biomart.Experiment

/**
 * $Id: AnalysisDataExportService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */

class AnalysisDataExportService {

	def renderAnalysisInExcel(org.transmart.biomart.BioAssayAnalysis analysis){
		def ExcelSheet sheet = null;
		def method = analysis.analysisMethodCode
		if("correlation".equalsIgnoreCase(method)){
			sheet = renderCorrelationAnalysisExcel(analysis);
		} else if("spearman correlation".equalsIgnoreCase(method)) {
			sheet = renderSpearmanAnalysisExcel(analysis);
		} else {
			// todo -- need to handle more methods....
			sheet = renderComparisonAnalysisExcel(analysis);
		}

		def gen = new ExcelGenerator()
		return gen.generateExcel([sheet]);
	}

	def renderComparisonAnalysisExcel(org.transmart.biomart.BioAssayAnalysis analysis){

		def allprobesameexpr = BioAssayAnalysisData.executeQuery("SELECT distinct g FROM BioAssayAnalysisData g JOIN  g.featureGroup.markers markers WHERE markers.bioMarkerType='GENE' AND g.analysis.id ="+analysis.id)
		def headers =[]
		def values=[]
		def dataType = analysis.assayDataType;
		if(isGeneExpression(dataType)){
			headers =["Analysis", "ProbeSet", "Fold Change Ratio","p-Value","adjusted p-value", "TEA p-Value", "Gene"]
		} else if(isRBM(dataType)){
			headers =["Analysis", "Antigen", "Fold Change Ratio","p-Value", "adjusted p-Value", "TEA p-Value", "Gene"]
		}

		for(data in allprobesameexpr){
		   	//println("?")
		   	for(marker in data.featureGroup.markers){
		   	values.add([data.analysis.name, data.featureGroupName, data.foldChangeRatio, data.rawPvalue, data.adjustedPvalue, data.teaNormalizedPValue, marker.name])
		   	}
		}
		return new ExcelSheet("sheet1", headers, values);
	}

	def renderCorrelationAnalysisExcel(org.transmart.biomart.BioAssayAnalysis analysis){

		def allprobesameexpr = BioAssayAnalysisData.executeQuery("SELECT distinct g FROM BioAssayAnalysisData g JOIN g.featureGroup.markers markers WHERE markers.bioMarkerType='GENE' AND g.analysis.id ="+analysis.id)
		def headers=[]
		def values=[]
		def dataType = analysis.assayDataType;

		if(isGeneExpression(dataType)){
			headers =["Analysis", "ProbeSet", "r-Value", "Gene"]
		} else if(isRBM(dataType)){
			headers =["Analysis", "Antigen", "r-Value", "Gene"]
		}

		for(data in allprobesameexpr){
			for(marker in data.featureGroup.markers){
			   	values.add([data.analysis.name, data.featureGroupName, , data.rValue, marker.name])
			}
		}
		return new ExcelSheet("sheet1", headers, values);
	}

	def renderSpearmanAnalysisExcel(org.transmart.biomart.BioAssayAnalysis analysis){

		println(">> Spearman analysis query:")
		//def allprobesameexpr = BioAssayAnalysisData.executeQuery("SELECT distinct g FROM BioAssayAnalysisData g JOIN FETCH g.markers markers WHERE markers.bioMarkerType='GENE' AND g.analysis.id ="+analysis.id)
		def allprobesameexpr = BioAssayAnalysisData.executeQuery("SELECT distinct g FROM BioAssayAnalysisData g WHERE g.analysis.id ="+analysis.id+" order by g.featureGroupName")
		def headers=[]
		def values=[]
		def dataType = analysis.assayDataType;

		// build excel sheet based on data type
		if(isGeneExpression(dataType)){
			headers =["Analysis", "ProbeSet", "rho-value", "Gene"]

			// build excel data
			for(data in allprobesameexpr){
				for(marker in data.featureGroup.markers){
					//println(">>For antigen '" + data.featureGroupName + "' adding marker: id: " + marker + ", name: " + marker.name)
			    	values.add([data.analysis.name, data.featureGroupName, , data.rhoValue, marker.name])
				}
			}
		} else if(isRBM(dataType)){
			// don't grab associated markers due to data annotation
			headers =["Analysis", "Antigen", "rho-value"]
			for(data in allprobesameexpr) values.add([data.analysis.name, data.featureGroupName, data.rhoValue])

			//def mc = [ compare: {a,b-> a.equals(b)? 0: a.size()>b.size()? -1: 1 } ] as Comparator
			//values.sort{it}
		}
		return new ExcelSheet("sheet1", headers, values);
	}

	def isGeneExpression(String dataType){
		return "Gene Expression".equalsIgnoreCase(dataType);
	}
	def isRBM(String dataType){
		return "RBM".equalsIgnoreCase(dataType)
	}

	/**
	 * Create the Excel objects and pass the resulting byte array back to be fed to the
	 * response output stream
	 *
	 * @param sResult the search result
	 * @param expAnalysisMap map with experiment accession as key and analysis as value
	 *
	 * @return a byte array containing the Excel workbook
	 */
	def createExcelEAStudyView(SearchResult sResult, Map expAnalysisMap)	{
	    def ExcelSheet sheet1 = null;
	    def ExcelSheet sheet2 = null;

	    def headers1=["Accession Number" , "Type", "Title", "Description", "Design", "Status", "Overall Design", "Start Date", "Completion Date", "Primary Investigator", "Compounds", "Diseases", "Organisms"]
	    def headers2=["Accession Number", "TEA Score", "Analysis Title", "Analysis Description", "p-Value Cut Off", "Fold Change Cut Off", "QA Criteria", "Analysis Platform", "Method", "Data type", "Compounds", "Diseases", "Bio Marker", "Description", "Organism", "ProbeSet", "Fold Change", "RValue", "p-Value", "TEA p-Value", "FDR p-Value", "Rho-Value", "Cut Value", "Results Value", "Numeric Value Code", "Numeric Value"]

		def values1=[]
	    def values2=[]
	    def placeh2=["", "", "", "", "", "", "", "", "", "", "", ""]    					// Empty placeholder for analysis metadata
	    def placebm2=["", "", "", "", "", "", "", "", "", "", "", "", "", ""]   		// Empty placeholder for biomarkers

	    def experimentOrganisms = [:]
	    String orgString = ""
        String organism = ""
        int orgIndex = -1

	    log.info("Number of Experiments: " + sResult.result.expAnalysisResults.size())

	    expAnalysisMap.each	{k, v ->
	        values2.add([k.accession] + ["", "", "", "", "", "", "", "", ""] + [k.getCompoundNames()] + [k.getDiseaseNames()])
	        orgString = ""
	        v.each {
	            values2.add([""] + [it.calcDisplayTEAScore()] + it.analysis.getValues() + placebm2)   // First column is for accession number
	            it.assayAnalysisValueList.each {
	                values2.add(placeh2 + it.bioMarker.getValues() + it.analysisData.getValues())
	                organism = it.bioMarker.organism
	                orgIndex = orgString.indexOf(it.bioMarker.organism)
	                if (orgIndex < 0)	{
	                    if (orgString.length() > 0)	{
	                        orgString += ", "
	                    }
	                    orgString += it.bioMarker.organism
	                }
	            }
	        }
	        experimentOrganisms.put(k.accession, orgString)
	    }

		sResult.result.expAnalysisResults.each()	{
		    values1.add(it.experiment.getExpValues() + [experimentOrganisms.get(it.experiment.accession)])
		}

	    sheet1 = new ExcelSheet("Experiments", headers1, values1)
	    sheet2 = new ExcelSheet("Analysis", headers2, values2)

		def gen = new ExcelGenerator()

		return gen.generateExcel([sheet1, sheet2])
	}

	/**
	 * Create the Excel objects and pass the resulting byte array back to be fed to the
	 * response output stream
	 *
	 * @param sResult the search result
	 *
	 * @return a byte array containing the Excel workbook
	 */
	def createExcelEATEAView(SearchResult sResult)	{
	    def ExcelSheet sheet1 = null;
	    def ExcelSheet sheet2 = null;

	    def headers1=["Accession Number" , "Type", "Title", "Description", "Design", "Status", "Overall Design", "Start Date", "Completion Date", "Primary Investigator", "Compounds", "Diseases", "Organisms"]
	    def headers2=["Accession Number", "TEA Score", "Analysis Title", "Analysis Description", "p-Value Cut Off", "Fold Change Cut Off", "QA Criteria", "Analysis Platform", "Method", "Data type", "Compounds", "Diseases", "Bio Marker", "Description", "Organism", "ProbeSet", "Fold Change", "RValue", "p-Value", "TEA p-Value", "FDR p-Value", "Rho-Value", "Cut Value", "Results Value", "Numeric Value Code", "Numeric Value"]

	    def values1=[]
	    def values2=[]
	    def placeh2=["", "", "", "", "", "", "", "", "", "", "", ""]    					// Empty placeholder for analysis metadata
	    def placebm2=["", "", "", "", "", "", "", "", "", "", "", "", "", ""]   			// Empty placeholder for biomarkers

        def expIDs=[] as Set
        String orgString = ""
        String organism = ""
        int orgIndex = -1

   	    def ear = sResult.result.expAnalysisResults[0]
	    log.info("Number of Experiments: " + ear.expCount)

	    ear.analysisResultList.each {
			def experiment = Experiment.get(it.experimentId)
            values2.add([experiment.accession] + [it.calcDisplayTEAScore()] + it.analysis.getValues() + [experiment.getCompoundNames()] + [experiment.getDiseaseNames()] + placebm2)
            orgString = ""
            it.assayAnalysisValueList.each {
                values2.add(placeh2 + it.bioMarker.getValues() + it.analysisData.getValues())
                organism = it.bioMarker.organism
                orgIndex = orgString.indexOf(it.bioMarker.organism)
                if (orgIndex < 0)	{
                    if (orgString.length() > 0)	{
                        orgString += ", "
                    }
                    orgString += it.bioMarker.organism
                }
	        }

            if (!expIDs.contains(experiment.id))	{
                values1.add(experiment.getExpValues() + [orgString])
                expIDs.add(experiment.id)
            }
		}
	    sheet1 = new ExcelSheet("Experiments", headers1, values1)
	    sheet2 = new ExcelSheet("Analysis", headers2, values2)

		def gen = new ExcelGenerator()

		return gen.generateExcel([sheet1, sheet2]);
	}

	/**
	 * Create the Excel objects and pass the resulting byte array back to be fed to the
	 * response output stream
	 *
	 * @param sResult the search result
	 *
	 * @return a byte array containing the Excel workbook
	 */
	def createExcelTrialStudyView(SearchResult sResult, Map trialMap)	{
	    def ExcelSheet sheet1 = null;
	    def ExcelSheet sheet2 = null;

	    def headers1=["Title" , "Trial Number", "Owner", "Description", "Study Phase", "Study Type", "Study Design", "Blinding procedure",
	                  "Duration of study (weeks)", "Completion date", "Inclusion Criteria", "Exclusion Criteria", "Dosing Regimen",
	                  "Type of Control", "Gender restriction mfb", "Group assignment", "Primary endpoints", "Secondary endpoints",
	                  "Route of administration", "Secondary ids", "Subjects", "Max age", "Min age", "Number of patients", "Number of sites", "Compounds", "Diseases", "Organisms"]
	    def headers2=["Trial Number", "TEA Score", "Analysis Title", "Analysis Description", "p-Value Cut Off", "Fold Change Cut Off", "QA Criteria", "Analysis Platform", "Method", "Data type", "Compounds", "Diseases", "Bio Marker", "Description", "Organism", "ProbeSet", "Fold Change", "RValue", "p-Value", "TEA p-Value", "FDR p-Value", "Rho-Value", "Cut Value", "Results Value", "Numeric Value Code", "Numeric Value"]

	    def values1=[]
	    def values2=[]
	    def placeh2=["", "", "", "", "", "", "", "", "", "", "", ""]				// Empty placeholder for analysis metadata
	    def placebm2=["", "", "", "", "", "", "", "", "", "", "", "", "", ""]   // Empty placeholder for biomarkers

	    def trialOrganisms = [:]
	    String orgString = ""
        String organism = ""
        int orgIndex = -1

	    log.info("Number of Trials: " + sResult.result.expAnalysisResults.size())

	    trialMap.each	{k, v ->
	        values2.add([k.trialNumber] + ["", "", "", "", "", "", "", "", ""] + [k.getCompoundNames()] + [k.getDiseaseNames()])
	        orgString = ""
	        log.info("Trial Number: " + k.trialNumber)
	        v.each {
	            values2.add([""] + [it.calcDisplayTEAScore()] + it.analysis.getValues() + placebm2)   // First column is for accession number
	            it.assayAnalysisValueList.each {
	                values2.add(placeh2 + it.bioMarker.getValues() + it.analysisData.getValues())
	                organism = it.bioMarker.organism
	                orgIndex = orgString.indexOf(it.bioMarker.organism)
	                if (orgIndex < 0)	{
	                    if (orgString.length() > 0)	{
	                        orgString += ", "
	                    }
	                    orgString += it.bioMarker.organism
	                }
	            }
	        }
	        trialOrganisms.put(k.trialNumber, orgString)
	    }

	    sResult.result.expAnalysisResults.each {
	        values1.add(it.trial.getValues() + [trialOrganisms.get(it.trial.trialNumber)])
	    }

	    sheet1 = new ExcelSheet("Trials", headers1, values1)
	    sheet2 = new ExcelSheet("Analysis", headers2, values2)

		def gen = new ExcelGenerator()

		return gen.generateExcel([sheet1, sheet2])
	}

	/**
	 * Create the Excel objects and pass the resulting byte array back to be fed to the
	 * response output stream
	 *
	 * @param sResult the search result
	 *
	 * @return a byte array containing the Excel workbook
	 */
	def createExcelTrialTEAView(SearchResult sResult)	{
	    def ExcelSheet sheet1 = null;
	    def ExcelSheet sheet2 = null;

	    def headers1=["Title" , "Trial Number", "Owner", "Description", "Study Phase", "Study Type", "Study Design", "Blinding procedure",
	                  "Duration of study (weeks)", "Completion date", "Inclusion Criteria", "Exclusion Criteria", "Dosing Regimen",
	                  "Type of Control", "Gender restriction mfb", "Group assignment", "Primary endpoints", "Secondary endpoints",
	                  "Route of administration", "Secondary ids", "Subjects", "Max age", "Min age", "Number of patients", "Number of sites", "Compounds", "Diseases", "Organisms"]
	    def headers2=["Accession Number", "TEA Score", "Analysis Title", "Analysis Description", "p-Value Cut Off", "Fold Change Cut Off", "QA Criteria", "Analysis Platform", "Method", "Data type", "Compounds", "Diseases", "Bio Marker", "Description", "Organism", "ProbeSet", "Fold Change", "RValue", "p-Value", "TEA p-Value", "FDR p-Value", "Rho-Value", "Cut Value", "Results Value", "Numeric Value Code", "Numeric Value"]

	    def values1=[]
	    def values2=[]
	    def placeh2=["", "", "", "", "", "", "", "", "", "", "", ""]   					// Empty placeholder for analysis metadata
	    def placebm2=["", "", "", "", "", "", "", "", "", "", "", "", "", ""]   		// Empty placeholder for biomarkers

        def trialIDs=[] as Set
        String orgString = ""
        String organism = ""
        int orgIndex = -1
	    log.info("Number of Trials: " + sResult.result.expCount)

	    def ear = sResult.result.expAnalysisResults[0]
	    def trialValues = []

	    ear.analysisResultList.each	{
			def experiment = Experiment.get(it.experimentId)
            trialValues = it.experiment.getValues()
	    	log.info("Trial Number: "+trialValues[1])
            values2.add([trialValues[1]] + [it.calcDisplayTEAScore()] + it.analysis.getValues() + [experiment.getCompoundNames()] + [experiment.getDiseaseNames()] + placebm2)
            orgString = ""
            it.assayAnalysisValueList.each {
                values2.add(placeh2 + it.bioMarker.getValues() + it.analysisData.getValues())
                organism = it.bioMarker.organism
                orgIndex = orgString.indexOf(it.bioMarker.organism)
                if (orgIndex < 0)	{
                    if (orgString.length() > 0)	{
                        orgString += ", "
                    }
                    orgString += it.bioMarker.organism
                }
	        }

            if (!trialIDs.contains(experiment.expId))	{
	    	    values1.add(trialValues + [orgString])
	            trialIDs.add(experiment.expId)
	        }
		}

	    sheet1 = new ExcelSheet("Trials", headers1, values1)
	    sheet2 = new ExcelSheet("Analysis", headers2, values2)

		def gen = new ExcelGenerator()

		return gen.generateExcel([sheet1, sheet2]);
	}
}
