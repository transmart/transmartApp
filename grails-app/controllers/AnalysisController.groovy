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
  

import grails.converters.*

import java.io.File
import java.text.*
import java.util.List

import javax.xml.parsers.*
import javax.xml.xpath.*

import org.genepattern.client.*
import org.genepattern.webservice.*
import org.json.*
import org.transmart.CohortInformation;
import org.transmart.HeatmapValidator;
import org.transmart.searchapp.AccessLog;
import org.w3c.dom.*
import org.xml.sax.*

import org.transmart.searchapp.SearchKeyword

import com.recomdata.export.GenePatternFiles
import com.recomdata.export.IgvFiles
import com.recomdata.export.SnpViewerFiles
import com.recomdata.export.PlinkFiles
import com.recomdata.genepattern.JobStatus
import com.recomdata.genepattern.WorkflowStatus
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib

/**
 * $Id: AnalysisController.groovy 11302 2011-12-23 02:59:44Z mkapoor $
 * @author $Author: mkapoor $
 * @version $Revision: 11302 $
 *
 */
class AnalysisController {
	
	def index = { };
	def i2b2HelperService;
	def genePatternService;
	def dataSource;
	def springSecurityService;
	def solrService;
	def plinkService;
	def analysisService;
	def snpService;
	def igvService;
	
		
	def heatmapvalidate={
		def platform="";
		log.debug("Received heatmap validation request");
		String resultInstanceID1 = request.getParameter("result_instance_id1");
		if (resultInstanceID1 != null && resultInstanceID1.length()== 0) {
			resultInstanceID1 = null;
		}
		String resultInstanceID2 = request.getParameter("result_instance_id2");
		if (resultInstanceID2 != null && resultInstanceID2.length()== 0) {
			resultInstanceID2 = null;
		}
		
		String analysis = request.getParameter("analysis");
		log.debug("analysis type: " + analysis);
		
		log.debug("\tresultInstanceID1: " + resultInstanceID1);
		log.debug("\tresultInstanceID2: " + resultInstanceID2);
		def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-Before Heatmap", eventmessage:"RID1:"+resultInstanceID1+" RID2:"+resultInstanceID2, accesstime:new java.util.Date())
		al.save()
		String pathwayName = request.getParameter("pathway_name");
		
		List<String> subjectIds1;
		List<String> subjectIds2;
		List<String> concepts1;
		List<String> concepts2;
		def hv1 = new HeatmapValidator();
		def hv2 = new HeatmapValidator();
		def ci1 = new CohortInformation();
		def ci2 = new CohortInformation();
		
		def markerType = ""
		
		if (resultInstanceID1 != null) {
			subjectIds1 = i2b2HelperService.getSubjectsAsList(resultInstanceID1);
			concepts1=i2b2HelperService.getConceptsAsList(resultInstanceID1);
			i2b2HelperService.fillHeatmapValidator(subjectIds1, concepts1, hv1);
			i2b2HelperService.fillCohortInformation(subjectIds1, concepts1, ci1, CohortInformation.TRIALS_TYPE);
			i2b2HelperService.fillDefaultGplInHeatMapValidator(hv1, ci1, concepts1)
			i2b2HelperService.fillDefaultRbmpanelInHeatMapValidator(hv1, ci1, concepts1)
			markerType=i2b2HelperService.getMarkerTypeFromConceptCd(concepts1[0])
		}
		
		if (resultInstanceID2 != null) {
			subjectIds2 = i2b2HelperService.getSubjectsAsList(resultInstanceID2);
			concepts2=i2b2HelperService.getConceptsAsList(resultInstanceID2);
			i2b2HelperService.fillHeatmapValidator(subjectIds2, concepts2, hv2);
			i2b2HelperService.fillCohortInformation(subjectIds2, concepts2, ci2, CohortInformation.TRIALS_TYPE);
			i2b2HelperService.fillDefaultGplInHeatMapValidator(hv2, ci2, concepts2)
			i2b2HelperService.fillDefaultRbmpanelInHeatMapValidator(hv2, ci2, concepts2)
		}
		
		def result=[defaultPlatforms: [
				hv1.getFirstPlatform(),
				hv2.getFirstPlatform()
			], 
			defaultPlatformLabels:[
				hv1.getFirstPlatformLabel(),
				hv2.getFirstPlatformLabel()
			],
			trials:[
				ci1.getAllTrials(),
				ci2.getAllTrials()
			], 
			defaultTimepoints:[
				hv1.getAllTimepoints(),
				hv2.getAllTimepoints()
			],
			defaultTimepointLabels:[
				hv1.getAllTimepointLabels(),
				hv2.getAllTimepointLabels()
			],
			defaultSamples:[
				hv1.getAllSamples(),
				hv2.getAllSamples()
			],
			defaultSampleLabels:[
				hv1.getAllSampleLabels(),
				hv2.getAllSampleLabels()
			],
			defaultGpls:[
				hv1.getAll('gpls'),
				hv2.getAll('gpls')
			],
			defaultGplLabels:[
				hv1.getAll('gplLabels'),
				hv2.getAll('gplLabels')
			],
			defaultTissues:[
				hv1.getAll('tissues'),
				hv2.getAll('tissues')
			],
			defaultTissueLabels:[
				hv1.getAll('tissueLabels'),
				hv2.getAll('tissueLabels')
			],
			defaultRbmpanels:[
				hv1.getAll('rbmpanels'),
				hv2.getAll('rbmpanels')
			],
			defaultRbmpanelLabels:[
				hv1.getAll('rbmpanelsLabels'),
				hv2.getAll('rbmpanelsLabels')
			],
			markerType:markerType];
		//log.debug(result as JSON);
		render result as JSON;
	}
	
	def getCohortInformation={
		String infoType = request.getParameter("INFO_TYPE")
		String platform = request.getParameter("PLATFORM")
		String rbmpanels = request.getParameter("RBMPANEL")
		String gpls = request.getParameter("GPL")
		String trial = request.getParameter("TRIAL")
		String tissues = request.getParameter("TISSUE")
		String samples = request.getParameter("SAMPLES");
		def ci = new CohortInformation();
		ci.platforms.add(platform);
		if ((rbmpanels!=null)&&(rbmpanels.length()>0))
			ci.rbmpanels.addAll(Arrays.asList(rbmpanels.split(',')));
		if(trial!=null)
			ci.trials.addAll(Arrays.asList(trial.split(',')));
		if((samples!=null) && (samples.length()>0))
			ci.samples.addAll(Arrays.asList(samples.split(',')));
		if((tissues!=null) && (tissues.length()>0))
			ci.tissues.addAll(Arrays.asList(tissues.split(',')));
		if((gpls!=null) && (gpls.length()>0))
			ci.gpls.addAll(Arrays.asList(gpls.split(',')));
		
		def result=null;
        if((infoType!=null) && (infoType.length()>0)){
            i2b2HelperService.fillCohortInformation(null, null, ci, Integer.parseInt(infoType));
            switch(Integer.parseInt(infoType)){
                case CohortInformation.GPL_TYPE:
                    result = [rows:ci.gpls]
                    break;
                case CohortInformation.TISSUE_TYPE:
                    result = [rows:ci.tissues]
                    break;
                case CohortInformation.TIMEPOINTS_TYPE:
                    result = [rows:ci.timepoints]
                    break;
                case CohortInformation.SAMPLES_TYPE:
                    result = [rows:ci.samples]
                    break;
                case CohortInformation.PLATFORMS_TYPE:
                    result = [rows:ci.platforms]
                    break;
                case CohortInformation.RBM_PANEL_TYPE:
                    result = [rows:ci.rbmpanels]
                    break;
                default:
                    result = [rows:{""}]
            }
        }
        if (result!=null)
            render(text:params.callback + "(" + (result as JSON) + ")", contentType:"application/javascript")
        else
            render(text:"({})")
	}
	
	/**
	 * This code will accept a list of Sample IDs to generate a heatmap.
	 */
	def heatMapFromSample = {
		
		String sampleIdList = request.getParameter("idList");
		//[{"SampleID":"PatientID"},{}]
		String patientIdList = getPatientIdsFromSampleIds(sampleIdList);
		println(idList);
		
		render "Done!";
	}
	
def showSNPViewer = {
	JSONObject result=new JSONObject();
	
	def wfstatus = new WorkflowStatus();
	session["workflowstatus"]= wfstatus;
	session["workflowstatus"].setCurrentJobStatus(new JobStatus(name:"Initializing Workflow",status:"C"));
	session["workflowstatus"].setCurrentJobStatus(new JobStatus(name:"Retrieving Data", status:"R"));
	session["workflowstatus"].addNewJob("ConvertLineEndings");
	session["workflowstatus"].addNewJob("SnpViewer");

	
	try{
		log.debug("Received SNPViewer rendering request: " + request);
		
		String resultInstanceID1 = request.getParameter("result_instance_id1");
		log.debug("\tresultInstanceID1: " + resultInstanceID1);
		String resultInstanceID2 = request.getParameter("result_instance_id2");
		log.debug("\tresultInstanceID2: " + resultInstanceID2);
		
		String chroms = request.getParameter("chroms");
		if (chroms == null || chroms.trim().length() == 0) chroms = "ALL";

		String genes = request.getParameter("genes");
		String geneAndIdListStr = request.getParameter("geneAndIdList");
		String snps = request.getParameter("snps");
		
		if (resultInstanceID1 == "undefined" || resultInstanceID1 == "null" || resultInstanceID1 == "") {
			log.debug "\tresultInstanceID1 == undefined or null";
			resultInstanceID1 = null;
		}
		if (resultInstanceID2 == "undefined" || resultInstanceID2 == "null" || resultInstanceID2 == "") {
			log.debug "\tresultInstanceID2 == undefined or null";
			resultInstanceID2 = null;
		}
		
		def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-ShowSNPViewer", eventmessage:"RID1:"+resultInstanceID1+" RID2:"+resultInstanceID2, accesstime:new java.util.Date())
		al.save()
		
		String subjectIds1 = i2b2HelperService.getSubjects(resultInstanceID1);
		String subjectIds2 = i2b2HelperService.getSubjects(resultInstanceID2);
		
		if ((subjectIds1 == null || subjectIds1.length() == 0) && 
		(subjectIds2 == null || subjectIds2.length() == 0)) {
			result.put("error", "No subject was selected");
			response.outputStream << result.toString();
			return;
		}
		
		boolean isByPatient = true;
		List<Long> geneSearchIdList = new ArrayList<Long>();
		List<String> geneNameList = new ArrayList<String>();
		if (genes != null && genes.length() != 0) {
			getGeneSearchIdListFromRequest(genes, geneAndIdListStr, geneSearchIdList, geneNameList);
		}
		List<String> snpNameList = null;
		if (snps != null && snps.length() != 0) {
			snpNameList = new ArrayList<String>();
			String[] snpNameArray = snps.split(",");
			for (String snpName : snpNameArray) {
				snpNameList.add(snpName.trim());
			} 
		}
		if ((geneSearchIdList != null && geneSearchIdList.size() != 0) ||
			(snpNameList != null && snpNameList.size() != 0)) {
			isByPatient = false;
		}
			
		SnpViewerFiles snpFiles = new SnpViewerFiles();
		StringBuffer geneSnpPageBuf = new StringBuffer();
		try {
			if (isByPatient)
				i2b2HelperService.getSNPViewerDataByPatient(subjectIds1, subjectIds2, chroms, snpFiles);
			else {
				i2b2HelperService.getSNPViewerDataByProbe(subjectIds1, subjectIds2, geneSearchIdList, geneNameList, snpNameList, snpFiles, geneSnpPageBuf);
			}
		}
		catch (Exception e) {
			result.put("error", e.getMessage());
			return;
		}
		
		JobResult[] jresult;
		
		try {
			jresult = genePatternService.snpViewer(snpFiles.getDataFile(), snpFiles.getSampleFile());
		}
		catch (WebServiceException e) {
			result.put("error", "WebServiceException: " + e.getMessage());
			return;
		}
		
		String viewerURL;
		String altviewerURL;
		
		try {
			result.put("jobNumber", jresult[1].getJobNumber());
			viewerURL =
			grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL +
			"/gp/jobResults/" +
			jresult[1].getJobNumber() +
			"?openVisualizers=true";
			log.debug("URL for viewer: " + viewerURL)
			result.put("viewerURL", viewerURL);
			
			result.put("snpGeneAnnotationPage", geneSnpPageBuf.toString());
			
			log.debug("result: "+result);
		} catch (JSONException e) {
			log.error("JSON Exception: " + e.getMessage());
			result.put("error", "JSON Exception: " + e.getMessage());
		}
	}finally{
		session["workflowstatus"].result = result;
		session["workflowstatus"].setCompleted();
	}
}

def showSNPViewerSample = {
	JSONObject result=new JSONObject();
	
	//Set the workflow status that gets show in the status popup.
	def wfstatus = new WorkflowStatus();
	session["workflowstatus"]= wfstatus;
	session["workflowstatus"].setCurrentJobStatus(new JobStatus(name:"Initializing Workflow",status:"C"));
	session["workflowstatus"].setCurrentJobStatus(new JobStatus(name:"Retrieving Data", status:"R"));
	session["workflowstatus"].addNewJob("ConvertLineEndings");
	session["workflowstatus"].addNewJob("SnpViewer");

	
	try{
		log.debug("Received SNPViewer rendering request: " + request);
		
		//Get parameters from the form.
		String chroms = request.getParameter("chroms");
		String genes = request.getParameter("genes");
		String geneAndIdListStr = request.getParameter("geneAndIdList");
		String snps = request.getParameter("snps");
		//The JSON we received should be [1:[category:[]]]
		def subsetListJSON = request.JSON.SearchJSON
	
		//We need to get an ID list per subset. Build the subset from the JSON Data.
		def subsetList = solrService.buildSubsetList(subsetListJSON)
		
		//Change the chroms text if the param was empty.
		if (chroms == null || chroms.trim().length() == 0) chroms = "ALL";
		
		//Log the access to this action.
		def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-ShowSNPViewer", eventmessage:"RID1:"+resultInstanceID1+" RID2:"+resultInstanceID2, accesstime:new java.util.Date())
		al.save()
		
		//Gather subjects from Sample IDs.
		//Form a list of lists of longs. We will convert the outside list to an array in a later method.
		List<List <Long>> patientNumList
		
		//We will use this to determine if our queries return ANY patients.
		boolean foundPatients = false;
		
		//For each subset get a list of subjects.
		subsetList.each
		{
			subsetItem ->
			
			def subsetSampleList = subsetItem.value

			//Don't add a subset if there are no items in the subset.
			if(subsetSampleList.size() > 0)
			{
				//Add the list to the list of lists.
				List <Long> tempPatientList = i2b2HelperService.getSubjectsAsListFromSampleLong(subsetSampleList)
				
				//If we found patients, add them to the list and set our boolean to indicate we found some.
				if(tempPatientList.size() > 0) 
				{
					foundPatients = true;
					patientNumList.add(tempPatientList);
				}
				
			}
		}
		
		//If we didn't find any patients, send a message to the output stream.
		if (!foundPatients) 
		{
			result.put("error", "No subject was selected");
			response.outputStream << result.toString();
			return;
		}
		
		//This will decide whether we retrieve SNP data by patient or by probe.
		boolean isByPatient = true;
		
		//Parse the gene list passed to this action to get the actual list of genes.
		List<Long> geneSearchIdList = new ArrayList<Long>();
		List<String> geneNameList = new ArrayList<String>();
		List<String> snpNameList = null;
		
		if (genes != null && genes.length() != 0) {
			getGeneSearchIdListFromRequest(genes, geneAndIdListStr, geneSearchIdList, geneNameList);
		}
		
		//Convert the string of snps into an ArrayList.
		if (snps != null && snps.length() != 0) 
		{
			snpNameList = new ArrayList<String>();
			String[] snpNameArray = snps.split(",");
			for (String snpName : snpNameArray) 
			{
				snpNameList.add(snpName.trim());
			}
		}
		
		//If the gene list is not empty or the snp list is not empty, we don't retrieve by patient, we retrieve by probe.
		if ((geneSearchIdList != null && geneSearchIdList.size() != 0) || (snpNameList != null && snpNameList.size() != 0)) {
			isByPatient = false;
		}
		
		//These are the files we write the SNP data to.
		SnpViewerFiles snpFiles = new SnpViewerFiles();
		
		//A page is used to display the items the user selected? I think? This buffer holds a page, it gets filled in during a helper method call within the SNP service.	
		StringBuffer geneSnpPageBuf = new StringBuffer();
		
		try {
			if (isByPatient)
				snpService.getSNPViewerDataByPatient(patientNumList as List<Long>[], chroms, snpFiles);
			else {
				snpService.getSNPViewerDataByProbe(patientNumList as List<Long>[], geneSearchIdList, geneNameList, snpNameList, snpFiles, geneSnpPageBuf);
			}
		}
		catch (Exception e) {
			result.put("error", e.getMessage());
			return;
		}
		
		JobResult[] jresult;
		
		try {
			jresult = genePatternService.snpViewer(snpFiles.getDataFile(), snpFiles.getSampleFile());
		}
		catch (WebServiceException e) {
			result.put("error", "WebServiceException: " + e.getMessage());
			return;
		}
		
		String viewerURL;
		String altviewerURL;
		
		try {
			result.put("jobNumber", jresult[1].getJobNumber());
			viewerURL =
			grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL +
			"/gp/jobResults/" +
			jresult[1].getJobNumber() +
			"?openVisualizers=true";
			log.debug("URL for viewer: " + viewerURL)
			result.put("viewerURL", viewerURL);
			
			result.put("snpGeneAnnotationPage", geneSnpPageBuf.toString());
			
			log.debug("result: "+result);
		} catch (JSONException e) {
			log.error("JSON Exception: " + e.getMessage());
			result.put("error", "JSON Exception: " + e.getMessage());
		}
	}finally{
		session["workflowstatus"].result = result;
		session["workflowstatus"].setCompleted();
	}
}

def showIgv = {
	JSONObject result=new JSONObject();
	
	def wfstatus = new WorkflowStatus();
	session["workflowstatus"]= wfstatus;
	session["workflowstatus"].setCurrentJobStatus(new JobStatus(name:"Initializing Workflow",status:"C"));
	session["workflowstatus"].setCurrentJobStatus(new JobStatus(name:"Retrieving Data", status:"R"));
	session["workflowstatus"].addNewJob("ConvertLineEndings");
	session["workflowstatus"].addNewJob("IGV");

	
	try{
		log.debug("Received IGV rendering request: " + request);
		
		String resultInstanceID1 = request.getParameter("result_instance_id1");
		log.debug("\tresultInstanceID1: " + resultInstanceID1);
		String resultInstanceID2 = request.getParameter("result_instance_id2");
		log.debug("\tresultInstanceID2: " + resultInstanceID2);
		
		String chroms = request.getParameter("chroms");
		if (chroms == null || chroms.trim().length() == 0) chroms = "ALL";

		String genes = request.getParameter("genes");
		String geneAndIdListStr = request.getParameter("geneAndIdList");
		String snps = request.getParameter("snps");
		
		if (resultInstanceID1 == "undefined" || resultInstanceID1 == "null" || resultInstanceID1 == "") {
			log.debug "\tresultInstanceID1 == undefined or null";
			resultInstanceID1 = null;
		}
		if (resultInstanceID2 == "undefined" || resultInstanceID2 == "null" || resultInstanceID2 == "") {
			log.debug "\tresultInstanceID2 == undefined or null";
			resultInstanceID2 = null;
		}
		
		def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-ShowIgv", eventmessage:"RID1:"+resultInstanceID1+" RID2:"+resultInstanceID2, accesstime:new java.util.Date())
		al.save()
		
		String subjectIds1 = i2b2HelperService.getSubjects(resultInstanceID1);
		String subjectIds2 = i2b2HelperService.getSubjects(resultInstanceID2);
		
		if ((subjectIds1 == null || subjectIds1.length() == 0) &&
		(subjectIds2 == null || subjectIds2.length() == 0)) {
			result.put("error", "No subject was selected");
			return;
		}
		
		def snpDatasetNum_1 = 0, snpDatasetNum_2 = 0;
		if (resultInstanceID1 != null && resultInstanceID1.trim().length() != 0) {
			List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds1);
			if (idList != null) snpDatasetNum_1 = idList.size();
		}
		if (resultInstanceID2 != null && resultInstanceID2.trim().length() != 0) {
			List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds2);
			if (idList != null) snpDatasetNum_2 = idList.size();
		}
		if (snpDatasetNum_1 == 0 && snpDatasetNum_2 == 0) {
			result.put("error", "No SNP dataset was selected");
			return;
		}
	
		boolean isByPatient = true;
		List<Long> geneSearchIdList = new ArrayList<Long>();
		List<String> geneNameList = new ArrayList<String>();
		if (genes != null && genes.length() != 0) {
			getGeneSearchIdListFromRequest(genes, geneAndIdListStr, geneSearchIdList, geneNameList);
		}
		List<String> snpNameList = null;
		if (snps != null && snps.length() != 0) {
			snpNameList = new ArrayList<String>();
			String[] snpNameArray = snps.split(",");
			for (String snpName : snpNameArray) {
				snpNameList.add(snpName.trim());
			}
		}
		if ((geneSearchIdList != null && geneSearchIdList.size() != 0) ||
			(snpNameList != null && snpNameList.size() != 0)) {
			isByPatient = false;
		}
		
		String newIGVLink = new ApplicationTagLib().createLink(controller:'analysis', action:'getGenePatternFile', absolute:true)
		
		IgvFiles igvFiles = new IgvFiles(getGenePatternFileDirName(),newIGVLink)
		StringBuffer geneSnpPageBuf = new StringBuffer();
		try {
			if (isByPatient)
				igvService.getIgvDataByPatient(subjectIds1, subjectIds2, chroms, igvFiles);
			else {
				igvService.getIgvDataByProbe(subjectIds1, subjectIds2, geneSearchIdList, geneNameList, snpNameList, igvFiles, geneSnpPageBuf);
			}
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			result.put("error", e.getMessage());
			return;
		}
		
		JobResult[] jresult;
		
		try {
			String userName = springSecurityService.getPrincipal().username;
			jresult = genePatternService.igvViewer(igvFiles, null, null, userName);
		}
		catch (WebServiceException e) {
			log.error(e.getMessage(),e);
			result.put("error", "WebServiceException: " + e.getMessage());
			return;
		}
		
		String viewerURL;
		String altviewerURL;
		
		try {
			result.put("jobNumber", jresult[1].getJobNumber());
			viewerURL =
			grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL +
			"/gp/jobResults/" +
			jresult[1].getJobNumber() +
			"?openVisualizers=true";
			log.debug("URL for viewer: " + viewerURL)
			result.put("viewerURL", viewerURL);
			
			result.put("snpGeneAnnotationPage", geneSnpPageBuf.toString());
			
			log.debug("result: "+result);
		} catch (JSONException e) {
			log.error("JSON Exception: " + e.getMessage());
			result.put("error", "JSON Exception: " + e.getMessage());
		}
	}finally{
		session["workflowstatus"].result = result;
		session["workflowstatus"].setCompleted();
	}
}

def showIgvSample = {
	JSONObject result=new JSONObject();
	
	def wfstatus = new WorkflowStatus();
	session["workflowstatus"]= wfstatus;
	session["workflowstatus"].setCurrentJobStatus(new JobStatus(name:"Initializing Workflow",status:"C"));
	session["workflowstatus"].setCurrentJobStatus(new JobStatus(name:"Retrieving Data", status:"R"));
	session["workflowstatus"].addNewJob("ConvertLineEndings");
	session["workflowstatus"].addNewJob("IGV");

	
	try{
		log.debug("Received IGV rendering request: " + request);

		String genes = request.getParameter("genes");
		String geneAndIdListStr = request.getParameter("geneAndIdList");
		String snps = request.getParameter("snps");

		//The JSON we received should be [1:[category:[]]]
		def subsetListJSON = request.JSON.SearchJSON

		//We need to get an ID list per subset. Build the subset from the JSON Data.
		def subsetList = solrService.buildSubsetList(subsetListJSON)

		String chroms = request.getParameter("chroms");
		if (chroms == null || chroms.trim().length() == 0) chroms = "ALL";
		
		def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-ShowIgv", eventmessage:"RID1:"+resultInstanceID1+" RID2:"+resultInstanceID2, accesstime:new java.util.Date())
		al.save()
		
		//Gather subjects from Sample IDs.
		//Form a list of lists of longs. We will convert the outside list to an array in a later method.
		List<List <Long>> patientNumList
		
		//We will use this to determine if our queries return ANY patients.
		boolean foundPatients = false;
		boolean foundSNPData = false;
		
		//For each subset get a list of subjects.
		subsetList.each
		{
			subsetItem ->
			
			def subsetSampleList = subsetItem.value

			//Don't add a subset if there are no items in the subset.
			if(subsetSampleList.size() > 0)
			{
				//Add the list to the list of lists.
				List <Long> tempPatientList = i2b2HelperService.getSubjectsAsListFromSampleLong(subsetSampleList)
				
				//If we found patients, add them to the list and set our boolean to indicate we found some.
				if(tempPatientList.size() > 0) 
				{
					foundPatients = true;
					patientNumList.add(tempPatientList);
				}
				
				//TODO: This needs to be a string.
				List<Long> idList = i2b2HelperService.getSNPDatasetIdList(tempPatientList);
				if (idList != null && idList.size() > 0) foundSNPData = true;
						
			}
		}

		//If we didn't find SNP data, add an error message to the results.
		if (!foundSNPData) {
			result.put("error", "No SNP dataset was selected");
			return;
		}
	
		//If we didn't find any patients, send a message to the output stream.
		if (!foundPatients)
		{
			result.put("error", "No subject was selected");
			response.outputStream << result.toString();
			return;
		}

		
		boolean isByPatient = true;
		List<Long> geneSearchIdList = new ArrayList<Long>();
		List<String> geneNameList = new ArrayList<String>();
		if (genes != null && genes.length() != 0) {
			getGeneSearchIdListFromRequest(genes, geneAndIdListStr, geneSearchIdList, geneNameList);
		}
		List<String> snpNameList = null;
		if (snps != null && snps.length() != 0) {
			snpNameList = new ArrayList<String>();
			String[] snpNameArray = snps.split(",");
			for (String snpName : snpNameArray) {
				snpNameList.add(snpName.trim());
			}
		}
		if ((geneSearchIdList != null && geneSearchIdList.size() != 0) ||
			(snpNameList != null && snpNameList.size() != 0)) {
			isByPatient = false;
		}
		
		String newIGVLink = new ApplicationTagLib().createLink(controller:'analysis', action:'getGenePatternFile', absolute:true)
			
		IgvFiles igvFiles = new IgvFiles(getGenePatternFileDirName(),newIGVLink)
		StringBuffer geneSnpPageBuf = new StringBuffer();
		try {
			if (isByPatient)
				igvService.getIgvDataByPatientSample(patientNumList as List<Long>[], chroms, igvFiles);
			else {
				igvService.getIgvDataByProbeSample(patientNumList as List<Long>[], geneSearchIdList, geneNameList, snpNameList, igvFiles, geneSnpPageBuf);
			}
		}
		catch (Exception e) {
			result.put("error", e.getMessage());
			return;
		}
		
		JobResult[] jresult;
		
		try {
			String userName = springSecurityService.getPrincipal().username;
			jresult = genePatternService.igvViewer(igvFiles, null, null, userName);
		}
		catch (WebServiceException e) {
			result.put("error", "WebServiceException: " + e.getMessage());
			return;
		}
		
		String viewerURL;
		String altviewerURL;
		
		try {
			result.put("jobNumber", jresult[1].getJobNumber());
			viewerURL =
			grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL +
			"/gp/jobResults/" +
			jresult[1].getJobNumber() +
			"?openVisualizers=true";
			log.debug("URL for viewer: " + viewerURL)
			result.put("viewerURL", viewerURL);
			
			result.put("snpGeneAnnotationPage", geneSnpPageBuf.toString());
			
			log.debug("result: "+result);
		} catch (JSONException e) {
			log.error("JSON Exception: " + e.getMessage());
			result.put("error", "JSON Exception: " + e.getMessage());
		}
	}finally{
		session["workflowstatus"].result = result;
		session["workflowstatus"].setCompleted();
	}
}



def showPlink = {
	JSONObject result=new JSONObject();
	
	def wfstatus = new WorkflowStatus();
	session["workflowstatus"]= wfstatus;
	session["workflowstatus"].setCurrentJobStatus(new JobStatus(name:"Initializing Workflow",status:"C"));
	session["workflowstatus"].setCurrentJobStatus(new JobStatus(name:"Retrieving Data", status:"R"));
	session["workflowstatus"].addNewJob("ConvertLineEndings");
	session["workflowstatus"].addNewJob("PLINK");
	
	try{
		log.debug("Received PLINK rendering request: " + request);
		
		String resultInstanceID1 = request.getParameter("result_instance_id1");
		log.debug("\tresultInstanceID1: " + resultInstanceID1);
		String resultInstanceID2 = request.getParameter("result_instance_id2");
		log.debug("\tresultInstanceID2: " + resultInstanceID2);
		
		String chroms = request.getParameter("chroms");
		if (chroms == null || chroms.trim().length() == 0) chroms = "ALL";

		/*
		String genes = request.getParameter("genes");
		String geneAndIdListStr = request.getParameter("geneAndIdList");
		String snps = request.getParameter("snps");
		*/
		
		List <String> conceptCodeList1
		List <String> conceptCodeList2
			
		if (resultInstanceID1 == "undefined" || resultInstanceID1 == "null" || resultInstanceID1 == "") {
			log.debug "\tresultInstanceID1 == undefined or null";
			resultInstanceID1 = null;
		}else{
			conceptCodeList1 = i2b2HelperService.getConceptsAsList(resultInstanceID1)
		}
		
		if (resultInstanceID2 == "undefined" || resultInstanceID2 == "null" || resultInstanceID2 == "") {
			log.debug "\tresultInstanceID2 == undefined or null";
			resultInstanceID2 = null;
		}else{
			conceptCodeList2 = i2b2HelperService.getConceptsAsList(resultInstanceID2)
		}
		
		String subjectIds1 = i2b2HelperService.getSubjects(resultInstanceID1);
		String subjectIds2 = i2b2HelperService.getSubjects(resultInstanceID2);
		
		def al = new AccessLog(username:springSecurityService.getPrincipal().username, event:"DatasetExplorer-ShowPlink", eventmessage:"RID1:"+resultInstanceID1+" RID2:"+resultInstanceID2, accesstime:new java.util.Date())
		al.save()		
		
		if ((subjectIds1 == null || subjectIds1.length() == 0) ||
		(subjectIds2 == null || subjectIds2.length() == 0)) {
			result.put("error", "No subject was selected");
			return;
		}
		
		/*
		boolean isByPatient = true;
		List<Long> geneSearchIdList = null;
		if (genes != null && genes.length() != 0) {
			geneSearchIdList = getGeneSearchIdListFromRequest(genes, geneAndIdListStr);
		}
		
		List<String> snpNameList = null;
		if (snps != null && snps.length() != 0) {
			snpNameList = new ArrayList<String>();
			String[] snpNameArray = snps.split(",");
			for (String snpName : snpNameArray) {
				snpNameList.add(snpName.trim());
			}
		}
				
		if ((geneSearchIdList != null && geneSearchIdList.size() != 0) ||
			(snpNameList != null && snpNameList.size() != 0)) {
			isByPatient = false;
		}
		*/
					
		PlinkFiles plinkFiles = new PlinkFiles();
		File mapFile = plinkFiles.getMapFile();	
		File pedFile = plinkFiles.getPedFile();
		
		plinkService.getMapDataByChromosome(subjectIds1, chroms, mapFile);
		
		// set the subset1 as "unaffected" or "control" and the subset2 as "affected" or "case"
		plinkService.getSnpDataBySujectChromosome(subjectIds1, chroms, pedFile, conceptCodeList1, "1");
		plinkService.getSnpDataBySujectChromosome(subjectIds2, chroms, pedFile, conceptCodeList2, "2");
		
		/*JobResult[] jresult;
		
		try {
			String userName = springSecurityService.getPrincipal().username;
			jresult = genePatternService.PLINK(pedFile, mapFile);
		}
		catch (WebServiceException e) {
			result.put("error", "WebServiceException: " + e.getMessage());
			return;
		}*/
		
		String viewerURL;
		String altviewerURL;
		
		try {
			/*result.put("jobNumber", jresult[1].getJobNumber());
			viewerURL = grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL +
						"/gp/jobResults/" +
						jresult[1].getJobNumber() +
						"?openVisualizers=true";*/
			log.debug("URL for viewer: " + viewerURL)
			result.put("viewerURL", viewerURL);
			
			result.put("snpGeneAnnotationPage", geneSnpPageBuf.toString());
			
			log.debug("result: "+result);
		} catch (JSONException e) {
			log.error("JSON Exception: " + e.getMessage());
			result.put("error", "JSON Exception: " + e.getMessage());
		}
		
	}finally{
		session["workflowstatus"].result = result;
		session["workflowstatus"].setCompleted();
	}
	
}

/**
 * This function parse the ","-separated gene string like "Gene>MET", and return a list of gene search ID and a list of matching gene names.
 */

public static String geneInputPrefix = "Gene>";

void getGeneSearchIdListFromRequest(String genes, String geneAndIdListStr, List<Long> geneSearchIdList, List<String> geneNameList) {
	if (genes == null || genes.length() == 0 || geneAndIdListStr == null || geneAndIdListStr.length() == 0 ||
		geneSearchIdList == null || geneNameList == null)
		return;
	Map<String, Long> geneIdMap = new HashMap<String, Long>();
	String[] geneAndIdList = geneAndIdListStr.split("\\|\\|\\|");
	for (String geneAndIdStr : geneAndIdList) {
		String[] geneIdPair = geneAndIdStr.split("\\|\\|");
		geneIdMap.put(geneIdPair[0].trim(), new Long(geneIdPair[1].trim()));
	}
	String[] geneValues = genes.split(",");
	for (String geneStr : geneValues) {
		geneStr = geneStr.trim();
		Long geneId = geneIdMap.get(geneStr.trim());
		geneSearchIdList.add(geneId);
		if (geneStr.startsWith(geneInputPrefix))
			geneStr = geneStr.substring(geneInputPrefix.length());
		geneNameList.add(geneStr.trim());
	}
}

def showHaploviewGeneSelector= {
	//log.debug("called test happleview")
	String resultInstanceID1 = request.getParameter("result_instance_id1");
	//log.debug(resultInstanceID1)
	String resultInstanceID2 = request.getParameter("result_instance_id2");
	//log.debug("*"+resultInstanceID2+"*")
	def genes1
	def genes2
	if(resultInstanceID1!=null && resultInstanceID1!='')
	{
		genes1=i2b2HelperService.getGenesForHaploviewFromResultInstanceId(resultInstanceID1);
	}
	if(resultInstanceID2!=null && resultInstanceID2!='') {
		genes2=i2b2HelperService.getGenesForHaploviewFromResultInstanceId(resultInstanceID2);
	}
	def combined=[:]
	genes1.each{gene ->combined.put(gene,"gene")
	}
	genes2.each{gene ->combined.put(gene,"gene")
	}
	def genes=combined.keySet();
	genes.sort()
	render(template:'haploviewGeneSelector',model:[genes: genes])
}

//Use the search JSON to get the list of samples. Find the Genes associated with those samples.
def showHaploviewGeneSelectorSample= {
	
	//We need to first retrieve the list of Sample ID's for the dataset we have selected.
	//Grab the URL of the solr server.
	String solrURL = grailsApplication.config.com.recomdata.solr.baseURL
	//Grab the string for the maximum number of result rows to return.
	String solrMaxRows = grailsApplication.config.com.recomdata.solr.maxRows

	//Get the list of Sample ID's based on the criteria in the JSON object.
	//We need to get an ID list per subset. The JSON we received should be [1:[category:[]]]
	def subsetList = request.JSON.SearchJSON
	
	//Build the subset from the JSON Data.
	def result = solrService.buildSubsetList(solrURL,solrMaxRows,subsetList)
	
	//We use this map to get a list of distinct genes.	
	def genes = analysisService.getGenesForHaploviewFromSampleId(result)
	
	//Sort the list of genes.
	genes=genes.keySet();
	genes.sort()
	
	render(template:'haploviewGeneSelector',model:[genes: genes])
}

def showSNPViewerSelection = {
	String resultInstanceID1 = request.getParameter("result_instance_id1");
	String resultInstanceID2 = request.getParameter("result_instance_id2");
	
	def snpDatasetNum_1 = 0, snpDatasetNum_2 = 0;
	if (resultInstanceID1 != null && resultInstanceID1.trim().length() != 0) {
		String subjectIds1 = i2b2HelperService.getSubjects(resultInstanceID1);
		List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds1);
		if (idList != null) snpDatasetNum_1 = idList.size();
	}
	if (resultInstanceID2 != null && resultInstanceID2.trim().length() != 0) {
		String subjectIds2 = i2b2HelperService.getSubjects(resultInstanceID2);
		List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds2);
		if (idList != null) snpDatasetNum_2 = idList.size();
	}
	
	String warningMsg = null;
	if (snpDatasetNum_1 + snpDatasetNum_2 > 10) {
		warningMsg = "Note: The performance may be slow with more than 10 SNP datasets. Please consider displaying individual chromosomes.";
	}
	def chroms = ['ALL','1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16','17','18','19','20','21','22','X','Y'];
	[chroms: chroms, snpDatasetNum_1: snpDatasetNum_1, snpDatasetNum_2: snpDatasetNum_2, warningMsg: warningMsg, chromDefault: 'ALL'];
}

//Get the data for the display elements in the SNP selection window.
def showSNPViewerSelectionSample = {
	
	//We need to first retrieve the list of Sample ID's for the dataset we have selected.
	//Grab the URL of the solr server.
	String solrURL = grailsApplication.config.com.recomdata.solr.baseURL
	//Grab the string for the maximum number of result rows to return.
	String solrMaxRows = grailsApplication.config.com.recomdata.solr.maxRows

	//Get the list of Sample ID's based on the criteria in the JSON object.
	//We need to get an ID list per subset. The JSON we received should be [1:[category:[]]]
	def subsetList = request.JSON.SearchJSON
	
	//Build the subset from the JSON Data.
	def result = solrService.buildSubsetList(solrURL,solrMaxRows,subsetList)

	//We need to show the users a count of how many datasets exist for each subset. As we gather the lists, stash the count in a hashmap.
	def datasetCount = [:]
	
	//Keep track of the total number of datasets so we can warn the user if > 10 datasets are available.
	int datasetCounter = 0;
	
	//For each subset we need to get a list of the Dataset Ids.
	result.each
	{
		subsetItem ->
		
		def subsetSampleList = subsetItem.value
		
		//Verify we have samples in this subset.
		if(subsetSampleList.size() > 0)
		{
		
			//Get the list of subjects from the subject sample mapping table based on Sample_ID
			String subjectIds = i2b2HelperServer.getSubjectsAsListFromSample(subsetItem.value)
			
			//Use the list of subjects to get the dataset ID List.
			List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds);
			
			//Make sure we retrieved Data Set Ids.
			if(idList != null)
			{
				//Put the count of dataset items in the hashmap.
				datasetCount[subseyItem.key] = idList.size();
				
				//Add the number of datasets to our total counter.
				datasetCounter += idList.size()
			}
		}
	}
	
	//Warn the user if there are over 10 SNP Datasets selected.
	String warningMsg = null;
	if (datasetCounter > 10) {
		warningMsg = "Note: The performance may be slow with more than 10 SNP datasets. Please consider displaying individual chromosomes.";
	}
	
	def chroms = ['ALL','1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16','17','18','19','20','21','22','X','Y'];
	
	//Render the showSNPViewerSelectionSample template.
	[chroms: chroms, snpDatasets : datasetCount, warningMsg: warningMsg, chromDefault: 'ALL'];
}

def showIgvSelection = {
	String resultInstanceID1 = request.getParameter("result_instance_id1");
	String resultInstanceID2 = request.getParameter("result_instance_id2");
	
	def snpDatasetNum_1 = 0, snpDatasetNum_2 = 0;
	if (resultInstanceID1 != null && resultInstanceID1.trim().length() != 0) {
		String subjectIds1 = i2b2HelperService.getSubjects(resultInstanceID1);
		List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds1);
		if (idList != null) snpDatasetNum_1 = idList.size();
	}
	if (resultInstanceID2 != null && resultInstanceID2.trim().length() != 0) {
		String subjectIds2 = i2b2HelperService.getSubjects(resultInstanceID2);
		List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds2);
		if (idList != null) snpDatasetNum_2 = idList.size();
	}
	
	String warningMsg = null;
	if (snpDatasetNum_1 + snpDatasetNum_2 > 10) {
		warningMsg = "Note: The performance may be slow with more than 10 SNP datasets. Please consider displaying individual chromosomes.";
	}
	def chroms = ['ALL','1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16','17','18','19','20','21','22','X','Y'];
	[chroms: chroms, snpDatasetNum_1: snpDatasetNum_1, snpDatasetNum_2: snpDatasetNum_2, warningMsg: warningMsg, chromDefault: 'ALL'];
}


def showPlinkSelection = {
	String resultInstanceID1 = request.getParameter("result_instance_id1");
	String resultInstanceID2 = request.getParameter("result_instance_id2");
	
	def snpDatasetNum_1 = 0, snpDatasetNum_2 = 0;
	if (resultInstanceID1 != null && resultInstanceID1.trim().length() != 0) {
		String subjectIds1 = i2b2HelperService.getSubjects(resultInstanceID1);
		List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds1);
		if (idList != null) snpDatasetNum_1 = idList.size();
	}
	if (resultInstanceID2 != null && resultInstanceID2.trim().length() != 0) {
		String subjectIds2 = i2b2HelperService.getSubjects(resultInstanceID2);
		List<Long> idList = i2b2HelperService.getSNPDatasetIdList(subjectIds2);
		if (idList != null) snpDatasetNum_2 = idList.size();
	}
	
	String warningMsg = null;
	if (snpDatasetNum_1 + snpDatasetNum_2 > 10) {
		warningMsg = "Note: The performance may be slow with more than 10 SNP datasets. Please consider displaying individual chromosomes.";
	}
	def chroms = ['ALL','1','2','3','4','5','6','7','8','9','10','11','12','13','14','15','16','17','18','19','20','21','22','X','Y'];
	[chroms: chroms, snpDatasetNum_1: snpDatasetNum_1, snpDatasetNum_2: snpDatasetNum_2, warningMsg: warningMsg, chromDefault: 'ALL'];
}



/**
 * Used to obtain the pathway for biomarker comparison when using the heatmap in dataset explorer
 */
def ajaxGetPathwaySearchBoxData = {
	String searchText = request.getParameter("query");
	log.info("Obtaining pathways for " + searchText)
	
	def pathways=[];
	
	groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
	String sqlt = "SELECT * FROM (select * from de_pathway where upper(name) like upper(?)";
	sqlt += " ORDER BY LENGTH(name)) WHERE ROWNUM<=40";
	
	sql.eachRow(sqlt, [searchText + '%'], {row ->
		pathways.add([name:row.name, type:row.type, source:row.source, uid:row.pathway_uid])
	})
	
	def result = [rows:pathways]
    render(text:params.callback + "(" + (result as JSON) + ")", contentType:"application/javascript")
}

def gplogin = {
	def gpEnabled = grailsApplication.config.com.recomdata.datasetExplorer.enableGenePattern;
  if('true'==gpEnabled){
 return [userName : springSecurityService.getPrincipal().username]
  }else{
  render(view:'nogp')

  }
}
	

	protected String getGenePatternFileDirName() {
		String fileDirName = grailsApplication.config.com.recomdata.analysis.genepattern.file.dir;
		String webRootName = servletContext.getRealPath("/");
		if (webRootName.endsWith(File.separator) == false) 
			webRootName += File.separator;
		return webRootName + fileDirName;
	}
}
