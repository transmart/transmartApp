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

//**************Start of functions to set global subset ids**************
/**
 * 1. If the global subset ids are null call runAllQueries to populate them.
 * 2. Calls heatmapvalidate to get all relevant data for the high dimensional selection.
 * @param divId
 */
function gatherHighDimensionalData(divId){
	if(!variableDivEmpty(divId)
			&& ((GLOBAL.CurrentSubsetIDs[1]	== null) ||	(multipleSubsets() && GLOBAL.CurrentSubsetIDs[2]== null))){
		runAllQueriesForSubsetId(function(){gatherHighDimensionalData(divId);}, divId);
		return;
	}
	if(variableDivEmpty(divId)){
		Ext.Msg.alert("No cohort selected!", "Please select a cohort first.");
		return;
	}
	//genePatternReplacement();
	//Send a request to generate the heatmapdata that we use to populate the dropdowns in the popup.
	Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/analysis/heatmapvalidate",
				method : 'POST',
				timeout: '1800000',
				params :  Ext.urlEncode(
						{
							result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
							result_instance_id2 : GLOBAL.CurrentSubsetIDs[2],
							analysis            : GLOBAL.HeatmapType
						}
				),
				success : function(result, request)
				{
					determineHighDimVariableType(result);
					readCohortData(result,divId);
				},
				failure : function(result, request)
				{
					determineHighDimVariableType(result);
					readCohortData(result,divId);
				}
			}
	);
}

function gatherHighDimensionalDataSingleSubset(divId, currentSubsetId){
	if((!variableDivEmpty(divId) && currentSubsetId== null)){
		runQueryForSubsetidSingleSubset(function(sId){gatherHighDimensionalDataSingleSubset(divId, sId);}, divId);
		return;
	}
	if(variableDivEmpty(divId)){
		Ext.Msg.alert("No cohort selected!", "Please select a cohort first.");
		return;
	}
	//genePatternReplacement();
	//Send a request to generate the heatmapdata that we use to populate the dropdowns in the popup.
	Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/analysis/heatmapvalidate",
				method : 'POST',
				timeout: '1800000',
				params :  Ext.urlEncode(
						{
							result_instance_id1 : currentSubsetId,
							result_instance_id2 : '',
							analysis            : GLOBAL.HeatmapType
						}
				),
				success : function(result, request)
				{
					determineHighDimVariableType(result);
					readCohortData(result,divId);
				},
				failure : function(result, request)
				{
					determineHighDimVariableType(result);
					readCohortData(result,divId);
				}
			}
	);
}

/**
 * Determine if we are dealing with genotype or copy number
 */
function determineHighDimVariableType(result){
	var mobj=result.responseText.evalJSON();
	GLOBAL.HighDimDataType=mobj.markerType;
}

/**
 * read the result from heatmapvalidate call.
 * @param result
 * @param completedFunction
 */
function readCohortData(result, divId)
{
	//Get the JSON string we got from the server into a real JSON object.
	var mobj=result.responseText.evalJSON();
	
	//If we failed to retrieve any test from the heatmap server call, we alert the user here. Otherwise, show the popup.
	if(mobj.NoData && mobj.NoData == "true")
	{
		Ext.Msg.alert("No Data!","No data found for the samples you selected!");
		return;
	}
	
	//If we got data, store the JSON data globally.
	GLOBAL.DefaultCohortInfo=mobj;
	
	GLOBAL.CurrentAnalysisDivId=divId;
	
	//Reset the pathway information.
	GLOBAL.CurrentPathway = '';
	GLOBAL.CurrentPathwayName = '';	
	
	if(GLOBAL.DefaultCohortInfo.defaultPlatforms[0]=='' || GLOBAL.DefaultCohortInfo.defaultPlatforms[0]==null){
		Ext.Msg.alert("No High Dimensional Data!","Please select a high dimensional data node.");
		return;
	}

	//render the pathway selection popup
	showCompareStepPathwaySelection();
}

/**
 * Check to see if a a selection has been made for the variable
 * @param divId
 * @returns {Boolean}
 */
function variableDivEmpty(divId){
	var queryDiv=Ext.get(divId);
	if(queryDiv.dom.childNodes.length>0){
		return false;
	}	
	return true;
}

function runAllQueriesForSubsetId(callback, divId)
{
	// analysisPanel.body.update("<table border='1' width='100%' height='100%'><tr><td width='50%'><div id='analysisPanelSubset1'></div></td><td><div id='analysisPanelSubset2'></div></td></tr>");
	var subset = 1;
	if(isSubsetEmpty(1) && isSubsetEmpty(2))
	{
		if (null != panel) { 
			panel.body.unmask()
		}
		Ext.Msg.alert('Subsets are empty', 'All subsets are empty. Please select subsets.');
		return;
	}

	// setup the number of subsets that need running
	var subsetstorun = 0;
	for (i = 1; i <= GLOBAL.NumOfSubsets; i = i + 1)
	{
		if( ! isSubsetEmpty(i) && GLOBAL.CurrentSubsetIDs[i] == null)
		{
			subsetstorun ++ ;
		}
	}
	STATE.QueryRequestCounter = subsetstorun;
	/* set the number of requests before callback is fired for runquery complete */

	// iterate through all subsets calling the ones that need to be run
	for (i = 1; i <= GLOBAL.NumOfSubsets; i = i + 1)
	{
		if( ! isSubsetEmpty(i) && GLOBAL.CurrentSubsetIDs[i] == null)
		{
			runQueryForSubsetId(i, callback, divId);
		}
	}
}

function runQueryForSubsetId(subset, callback, divId)
{
	if(Ext.get('analysisPanelSubset1') == null)
	{
		// analysisPanel.body.update("<table border='1' width='100%' height='100%'><tr><td width='50%'><div id='analysisPanelSubset1'></div></td><td><div id='analysisPanelSubset2'></div></td></tr>");
	}
	/* if(isSubsetEmpty(subset))
   {
   callback();
   return;
   } */
	var query = getCRCRequest(subset, "", divId);
	// first subset
	queryPanel.el.mask('Getting subset ' + subset + '...', 'x-mask-loading');
	Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/proxy?url=" + GLOBAL.CRCUrl + "request",
				method : 'POST',
				xmlData : query,
				// callback : callback,
				success : function(result, request)
				{
				runQueryComplete(result, subset, callback);
				}
			,
			failure : function(result, request)
			{
				runQueryComplete(result, subset, callback);
			}
			,
			timeout : '600000'
			}
	);

	if(GLOBAL.Debug)
	{
		resultsPanel.setBody("<div style='height:400px;width500px;overflow:auto;'>" + Ext.util.Format.htmlEncode(query) + "</div>");
	}
}

function runQueryForSubsetidSingleSubset(callback, divId){
	var query = getCRCRequestSingleSubset(divId);
	Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/proxy?url=" + GLOBAL.CRCUrl + "request",
				method : 'POST',
				xmlData : query,
				// callback : callback,
				success : function(result, request)
				{
				runQueryCompleteForSubsetId(result, callback);
				}
			,
			failure : function(result, request)
			{
				runQueryCompleteForSubsetId(result, callback);
			}
			,
			timeout : '600000'
			}
	);

	if(GLOBAL.Debug)
	{
		resultsPanel.setBody("<div style='height:400px;width500px;overflow:auto;'>" + Ext.util.Format.htmlEncode(query) + "</div>");
	}
}

function runQueryCompleteForSubsetId(result, callback)
{
	var numOfPatientsFound = result.responseXML.selectSingleNode("//set_size").firstChild.nodeValue;
	var patientsetid = result.responseXML.selectSingleNode("//result_instance_id").firstChild.nodeValue;
	var currentSubsetId = patientsetid;
	callback(currentSubsetId);

}

function getCRCRequest(subset, queryname, divId){
	if(queryname=="" || queryname==undefined){
		var d=new Date();
		queryname=GLOBAL.Username+"'s Query at "+ d.toString();
		}
	var query=getCRCRequestHeader()+ '<user group="'+GLOBAL.ProjectID+'" login="'+GLOBAL.Username+'">'+GLOBAL.Username+'</user>\
	            <patient_set_limit>0</patient_set_limit>\
	            <estimated_time>0</estimated_time>\
	            <request_type>CRC_QRY_runQueryInstance_fromQueryDefinition</request_type>\
	        </ns4:psmheader>\
	        <ns4:request xsi:type="ns4:query_definition_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\
					<query_definition>\
	                <query_name>'+queryname+'</query_name>\
	                <specificity_scale>0</specificity_scale>';
	
	var qcd=Ext.get(divId);
	
	if(qcd.dom.childNodes.length>0)
	{
		query=query+getCRCRequestPanel(qcd.dom, 1);
	}
	
	for(var i=1;i<=GLOBAL.NumOfQueryCriteriaGroups;i++)
	{
		var qcd=Ext.get("queryCriteriaDiv"+subset+'_'+i.toString());
		if(qcd.dom.childNodes.length>0)
		{
		query=query+getCRCRequestPanel(qcd.dom, i);
		}
	}
	
	query=query+getSecurityPanel()+"</query_definition>"+getCRCRequestFooter();
	//query=query+"</query_definition>"+getCRCRequestFooter();
	return query;
}

function getCRCRequestSingleSubset(divId, queryname){
	if(queryname=="" || queryname==undefined){
		var d=new Date();
		queryname=GLOBAL.Username+"'s Query at "+ d.toString();
		}
	var query=getCRCRequestHeader()+ '<user group="'+GLOBAL.ProjectID+'" login="'+GLOBAL.Username+'">'+GLOBAL.Username+'</user>\
	            <patient_set_limit>0</patient_set_limit>\
	            <estimated_time>0</estimated_time>\
	            <request_type>CRC_QRY_runQueryInstance_fromQueryDefinition</request_type>\
	        </ns4:psmheader>\
	        <ns4:request xsi:type="ns4:query_definition_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\
					<query_definition>\
	                <query_name>'+queryname+'</query_name>\
	                <specificity_scale>0</specificity_scale>';
	
	var qcd=Ext.get(divId);
	
	if(qcd.dom.childNodes.length>0)
	{
		query=query+getCRCRequestPanel(qcd.dom, 1);
	}
	
	query=query+getSecurityPanel()+"</query_definition>"+getCRCRequestFooter();
	//query=query+"</query_definition>"+getCRCRequestFooter();
	return query;
}

//**************End of functions to set global subset ids**************

function applyToForm(){
	var divId = GLOBAL.CurrentAnalysisDivId;
	var probesAgg = document.getElementById("probesAggregation");
	var snpType = document.getElementById("SNPType");
	
	var subsetCount = multipleSubsets()?2:1;
	for(var idx = 1; idx<=subsetCount; idx++){
		applySubsetToForm(divId, idx);
	}
	
	//Pull other values from javascript object.
	window[divId+'pathway']				= GLOBAL.CurrentPathway;
	window[divId+'pathwayName'] 		= GLOBAL.CurrentPathwayName;
	window[divId+'probesAggregation']	= probesAgg.checked;
	window[divId+'SNPType']				= snpType.value;
	window[divId+'markerType']			= GLOBAL.HighDimDataType;
	
	//Pull the actual values for these items so we can filter on them in a SQL query later. When we click on the items in the dropdowns these "Value" fields get a list of all items in both subsets.
	window[divId+'samplesValues']		= GLOBAL.CurrentSamples;
	window[divId+'tissuesValues']		= GLOBAL.CurrentTissues;
	window[divId+'timepointsValues']	= GLOBAL.CurrentTimepoints;
	window[divId+'gplValues']			= GLOBAL.CurrentGpls.toArray();	
	
	displayHighDimSelectionSummary(subsetCount, divId, probesAgg, snpType);

	compareStepPathwaySelection.hide();
}

function applySubsetToForm(divId, idx){
	//Pull the text value for the selections from the popup form.
	window[divId+'samples'+idx]				= Ext.get('sample'+idx).dom.value;
	window[divId+'platforms'+idx]			= Ext.get('platforms'+idx).dom.value;
	window[divId+'gpls'+idx]				= Ext.get('gpl'+idx).dom.value;
	window[divId+'tissues'+idx]				= Ext.get('tissue'+idx).dom.value;
	window[divId+'timepoints'+idx]			= Ext.get('timepoint'+idx).dom.value;

	//Pull the actual GPL values.
	window[divId+'gplsValue'+idx]			= GLOBAL.CurrentGpls[idx-1];	
	
	//Pull other values from javascript object.
	window[divId+'rbmPanels'+idx]			= GLOBAL.CurrentRbmpanels[idx-1]; 
}

function displayHighDimSelectionSummary(subsetCount, divId, probesAgg, snpType){

	var summaryString="";
	for(var idx = 1; idx<=subsetCount; idx++){
		summaryString=createSelectionSummaryString(divId, idx, summaryString);
	}
	
	
	var selectedSearchPathway =Ext.get('searchPathway').dom.value;
	
	var innerHtml = summaryString+ 
	'<br> <b>Pathway:</b> '+selectedSearchPathway+
	'<br> <b>Marker Type:</b> '+GLOBAL.HighDimDataType;
	
	if(GLOBAL.HighDimDataType=="Gene Expression")
	{
		if(isProbesAggregationSupported()){
			innerHtml += '<br><b> Aggregate Probes:</b> '+ probesAgg.checked;
		}
	}else if(GLOBAL.HighDimDataType=="SNP"){
		if(isProbesAggregationSupported()){
			innerHtml += '<br><b> Aggregate Probes:</b> '+ probesAgg.checked;
		}
		innerHtml += '<br><b> SNP Type:</b> '+ snpType.value;
	}
	
	var domObj = document.getElementById("display"+divId);
	domObj.innerHTML=innerHtml;
}

function createSelectionSummaryString(divId, idx, summaryString){
	var selectedPlatform =Ext.get('platforms'+idx).dom.value;
	var selectedGpl =Ext.get('gpl'+idx).dom.value;
	var selectedSample =Ext.get('sample'+idx).dom.value;
	var selectedTissue =Ext.get('tissue'+idx).dom.value;
	var selectedTimepoint =Ext.get('timepoint'+idx).dom.value;
	
	summaryString += '<br> <b>Subset'+idx+'</b>'+
	'<br> <b>Platform:</b> '+ selectedPlatform +
	'<br> <b>GPL Platform:</b> '+selectedGpl+
	'<br> <b>Sample:</b> '+selectedSample+
	'<br> <b>Tissue:</b> '+selectedTissue+
	'<br> <b>Timepoint:</b> '+selectedTimepoint+
	'<br>';
	
	return summaryString
}

function clearHighDimDataSelections(divId){
	var subsetCount = multipleSubsets()?2:1;
	for(var idx = 1; idx<=subsetCount; idx++){
		window[divId+'timepoints'+idx]		="";
		window[divId+'samples'+idx]			="";
		window[divId+'rbmPanels'+idx]		=""; 
		window[divId+'platforms'+idx]		="";
		window[divId+'gpls'+idx]			="";
		window[divId+'tissues'+idx]			="";
		window[divId+'samplesValues'+idx]	="";
		window[divId+'tissuesValues'+idx]	="";
		window[divId+'timepointsValues'+idx]="";
	}
	
	window[divId+'pathway']				="";
	window[divId+'pathwayName'] 		="";
	window[divId+'probesAggregation']	="";
	window[divId+'SNPType']				="";
	window[divId+'markerType']			="";
	
	//invalidate the two global subsets
	GLOBAL.CurrentSubsetIDs[1]			=null;
	GLOBAL.CurrentSubsetIDs[2]			=null;
	
	GLOBAL.CurrentPathway				=null;
	GLOBAL.CurrentPathwayName			=null;
	GLOBAL.HighDimDataType				=null;
	
}

function clearSummaryDisplay(divId){
	var domObj = document.getElementById("display"+divId);
	if(domObj){
		domObj.innerHTML="";
	}
}

function multipleSubsets(){
	var multipleSubsets=false; 
	if(Ext.get('multipleSubsets') && (getQuerySummary(2)!="")){
		multipleSubsets = (Ext.get('multipleSubsets').dom.value=='true');
	}
	return multipleSubsets;
}

function toggleDataAssociationFields(extEle){

	
	if((GLOBAL.Analysis=='Advanced')||multipleSubsets()){
		//rbm panel, gpl, sample and tissue have display rules based on the selected platform
		//Those rules have been applied by now in i2b2common.js->resetCohortInfoValues().
		//The display property for those fields is not modified.
		document.getElementById("labelsubset1").style.display="";
		document.getElementById("labelsubset2").style.display="";
		document.getElementById("divplatform2").style.display="";
		document.getElementById("divtimepoint2").style.display="";
	}else{
		document.getElementById("labelsubset1").style.display="none";
		document.getElementById("labelsubset2").style.display="none";
		document.getElementById("divplatform2").style.display="none";
		document.getElementById("divrbmpanel2").style.display="none";
		document.getElementById("divgpl2").style.display="none";
		document.getElementById("divsample2").style.display="none";
		document.getElementById("divtissue2").style.display="none";
		document.getElementById("divtimepoint2").style.display="none";
	}
	
	// toggle display of Probes aggregation checkbox
	if (document.getElementById("divProbesAggregation") != null) {
		
		document.getElementById("divProbesAggregation").style.display="none";

		if(isProbesAggregationSupported()){
			document.getElementById("divProbesAggregation").style.display="";
			document.getElementById("divProbesAggregation").checked=false
		}
	}
	
	//toggle display of SNP type dropdown
	if (document.getElementById("divSNPType") != null) {
		if(GLOBAL.Analysis=='Advanced'){
			document.getElementById("divSNPType").style.display="none";
		}else if(GLOBAL.Analysis=="dataAssociation"){
			if(GLOBAL.HighDimDataType=='Gene Expression'){
				document.getElementById("divSNPType").style.display="none";
			}else if (GLOBAL.HighDimDataType=='SNP'){
				document.getElementById("divSNPType").style.display="";
			}else if (GLOBAL.HighDimDataType==''){
			document.getElementById("divSNPType").style.display="none";
			}
		}
	}


    //display the appropriate submit button
    if(GLOBAL.Analysis=="dataAssociation" || GLOBAL.Analysis=='MetaCoreEnrichment'){
        document.getElementById("compareStepPathwaySelectionOKButton").style.display="none";
        document.getElementById("dataAssociationApplyButton").style.display="";
    }else if(GLOBAL.Analysis=='Advanced'){
        document.getElementById("compareStepPathwaySelectionOKButton").style.display="";
        document.getElementById("dataAssociationApplyButton").style.display="none";
    }
}

function isProbesAggregationSupported(){
	var probesAggregationSupported = false;
	//The checkbox is displayd only for the dataAssociation tab.
	if(GLOBAL.Analysis=="dataAssociation"){
		var highDimDataTypeSupported=false;
		if(["Gene Expression", "SNP"].indexOf(GLOBAL.HighDimDataType)>-1){
			highDimDataTypeSupported=true;
		}
		
		var analysisSupported=true;
		var currentAnalysis=(Ext.get("analysis")).dom.value;
		if(["markerSelection", "pca"].indexOf(currentAnalysis)>-1){
			analysisSupported=false;
		}
			
		if(highDimDataTypeSupported && analysisSupported){
			probesAggregationSupported=true; 
		}
	}
	return probesAggregationSupported;
}


function loadHighDimensionalParameters(formParams)
{
	//These will tell tranSMART what data types we need to retrieve.
	var mrnaData = false
	var snpData = false

	//Gene expression filters.
	var fullGEXSampleType 	= "";
	var fullGEXTissueType 	= "";
	var fullGEXTime 		= "";
	var fullGEXGeneList 	= "";
	var fullGEXGPL 			= "";
	
	//SNP Filters.
	var fullSNPSampleType 	= "";
	var fullSNPTissueType 	= "";
	var fullSNPTime 		= "";	
	var fullSNPGeneList 	= "";
	var fullSNPGPL 			= "";
	
	//Pull the individual filters from the window object.
	var independentGeneList = window['divIndependentVariablepathway'];
	var dependentGeneList 	= window['divDependentVariablepathway'];
	
	var dependentPlatform 	= window['divDependentVariableplatforms1'];
	var independentPlatform = window['divIndependentVariableplatforms1'];
	
	var dependentType 		= window['divDependentVariablemarkerType'];
	var independentType		= window['divIndependentVariablemarkerType'];
	
	var dependentTime		= window['divDependentVariabletimepointsValues'];
	var independentTime		= window['divIndependentVariabletimepointsValues'];
	
	var dependentSample		= window['divDependentVariablesamplesValues'];
	var independentSample	= window['divIndependentVariablesamplesValues'];
	
	var dependentTissue		= window['divDependentVariabletissuesValues'];
	var independentTissue	= window['divIndependentVariabletissuesValues'];
	
	var dependentGPL		= window['divDependentVariablegplValues'];
	var independentGPL		= window['divIndependentVariablegplValues'];
	
	if(dependentGPL) dependentGPL = dependentGPL[0];
	if(independentGPL) independentGPL = independentGPL[0];		
	
	//If we are using High Dimensional data we need to create variables that represent genes from both independent and dependent selections (In the event they are both of a single high dimensional type).
	//Check to see if the user selected GEX in the independent input.
	if(independentType == "Gene Expression")
	{
		//Put the independent filters in the GEX variables.
		fullGEXGeneList 	= String(independentGeneList);
		fullGEXSampleType 	= String(independentSample);
		fullGEXTissueType 	= String(independentTissue);
		fullGEXTime			= String(independentTime);
		fullGEXGPL 			= String(independentGPL);
		
		//This flag will tell us to write the GEX text file.
		mrnaData = true;
		
		//Fix the platform to be something the R script expects.
		independentType = "MRNA";		
	}

	if(dependentType == "Gene Expression")
	{
		//If the gene list already has items, add a comma.
		if(fullGEXGeneList != "") 	fullGEXGeneList 	+= ","
		if(fullGEXSampleType != "") fullGEXSampleType 	+= ","
		if(fullGEXTissueType != "") fullGEXTissueType 	+= ","
		if(fullGEXTime != "") 		fullGEXTime 		+= ","
		if(fullGEXGPL != "") 		fullGEXGPL 			+= ","
				
		//Add the genes in the list to the full list of GEX genes.
		fullGEXGeneList 	+= String(dependentGeneList);
		fullGEXSampleType 	+= String(dependentSample);
		fullGEXTissueType 	+= String(dependentTissue);
		fullGEXTime			+= String(dependentTime);
		fullGEXGPL 			+= String(dependentGPL);
		
		//This flag will tell us to write the GEX text file.		
		mrnaData = true;
		
		//Fix the platform to be something the R script expects.
		dependentType = "MRNA";	
	}
	
	//Check to see if the user selected SNP in the independent input.
	if(independentType == "SNP")
	{
		//The genes entered into the search box were SNP genes.
		fullSNPGeneList 	= String(independentGeneList);
		fullSNPSampleType 	= String(independentSample);
		fullSNPTissueType 	= String(independentTissue);
		fullSNPTime 		= String(independentTime);
		fullSNPGPL 			= String(independentGPL);
		
		//This flag will tell us to write the SNP text file.
		snpData = true;
	}
	
	if(dependentType == "SNP")
	{
		//If the gene list already has items, add a comma.
		if(fullSNPGeneList != "") 	fullSNPGeneList 	+= ","
		if(fullSNPSampleType != "") fullSNPSampleType 	+= ","
		if(fullSNPTissueType != "") fullSNPTissueType 	+= ","
		if(fullSNPTime != "") 		fullSNPTime 		+= ","
		if(fullSNPGPL != "") 		fullSNPGPL 			+= ","
				
		//Add the genes in the list to the full list of SNP genes.
		fullSNPGeneList 	+= String(dependentGeneList)
		fullSNPSampleType 	+= String(dependentSample);
		fullSNPTissueType 	+= String(dependentTissue);
		fullSNPTime 		+= String(dependentTime);	
		fullSNPGPL 			+= dependentGPL;
		
		//This flag will tell us to write the SNP text file.		
		snpData = true;
	}	
	
	if((fullGEXGeneList == "") && (independentType == "MRNA" || dependentType == "MRNA"))
	{
		Ext.Msg.alert("No Genes Selected", "Please specify Genes in the Gene/Pathway Search box.")
		return false;
	}
	
	if((fullSNPGeneList == "") && (independentType == "SNP" || dependentType == "SNP"))
	{
		Ext.Msg.alert("No Genes Selected", "Please specify Genes in the Gene/Pathway Search box.")
		return false;
	}
		
	//If we don't have a platform, fill in Clinical.
	if(dependentPlatform == null || dependentPlatform == "") dependentType = "CLINICAL"
	if(independentPlatform == null || independentPlatform == "") independentType = "CLINICAL"
	
	formParams["divDependentVariabletimepoints"] 			= window['divDependentVariabletimepoints1'];
	formParams["divDependentVariablesamples"] 				= window['divDependentVariablesamples1'];
	formParams["divDependentVariablerbmPanels"]				= window['divDependentVariablerbmPanels1'];
	formParams["divDependentVariableplatforms"]				= dependentPlatform
	formParams["divDependentVariablegpls"]					= window['divDependentVariablegplsValue1'];
	formParams["divDependentVariabletissues"]				= window['divDependentVariabletissues1'];
	formParams["divDependentVariableprobesAggregation"]	 	= window['divDependentVariableprobesAggregation'];
	formParams["divDependentVariableSNPType"]				= window['divDependentVariableSNPType'];
	formParams["divDependentVariableType"]					= dependentType;
	formParams["divDependentVariablePathway"]				= dependentGeneList;
	formParams["divIndependentVariabletimepoints"]			= window['divIndependentVariabletimepoints1'];
	formParams["divIndependentVariablesamples"]				= window['divIndependentVariablesamples1'];
	formParams["divIndependentVariablerbmPanels"]			= window['divIndependentVariablerbmPanels1'];
	formParams["divIndependentVariableplatforms"]			= independentPlatform;
	formParams["divIndependentVariablegpls"]				= window['divIndependentVariablegplsValue1'];
	formParams["divIndependentVariabletissues"]				= window['divIndependentVariabletissues1'];
	formParams["divIndependentVariableprobesAggregation"]	= window['divIndependentVariableprobesAggregation'];
	formParams["divIndependentVariableSNPType"]				= window['divIndependentVariableSNPType'];
	formParams["divIndependentVariableType"]				= independentType;
	formParams["divIndependentVariablePathway"]				= independentGeneList;
	formParams["gexpathway"]								= fullGEXGeneList;
	//formParams["gextime"]									= fullGEXTime;
	//formParams["gextissue"]									= fullGEXTissueType;
	//formParams["gexsample"]									= fullGEXSampleType;
	formParams["snppathway"]								= fullSNPGeneList;
	//formParams["snptime"]									= fullSNPTime;
	//formParams["snptissue"]									= fullSNPTissueType;
	//formParams["snpsample"]									= fullSNPSampleType;
	formParams["divIndependentPathwayName"]					= window['divIndependentVariablepathwayName'];
	formParams["divDependentPathwayName"]					= window['divDependentVariablepathwayName'];
	formParams["mrnaData"]									= mrnaData;
	formParams["snpData"]									= snpData;
	formParams["gexgpl"]									= fullGEXGPL;
	formParams["snpgpl"]									= fullSNPGPL;
	
	return true;
}

function loadHighDimensionalParametersGeneral(formParams, variableNameOfDiv)
{
	//These will tell tranSMART what data types we need to retrieve.
	var mrnaData = false
	var snpData = false

	//Gene expression filters.
	var fullGEXSampleType 	= "";
	var fullGEXTissueType 	= "";
	var fullGEXTime 		= "";
	var fullGEXGeneList 	= "";
	var fullGEXGPL 			= "";
	
	//SNP Filters.
	var fullSNPSampleType 	= "";
	var fullSNPTissueType 	= "";
	var fullSNPTime 		= "";	
	var fullSNPGeneList 	= "";
	var fullSNPGPL 			= "";
	
	//Pull the individual filters from the window object.
	var divInputGeneList 	= window['div' + variableNameOfDiv + 'Variablepathway'];
	var divInputPlatform 	= window['div' + variableNameOfDiv + 'Variableplatforms1'];
	var divInputType		= window['div' + variableNameOfDiv + 'VariablemarkerType'];
	var divInputTime		= window['div' + variableNameOfDiv + 'VariabletimepointsValues'];
	var divInputSample		= window['div' + variableNameOfDiv + 'VariablesamplesValues'];
	var divInputTissue		= window['div' + variableNameOfDiv + 'VariabletissuesValues'];
	var divInputGPL			= window['div' + variableNameOfDiv + 'VariablegplValues'];
	
	if(divInputGPL) divInputGPL = divInputGPL[0];		
	
	//If we are using High Dimensional data we need to create variables that represent genes from both independent and dependent selections (In the event they are both of a single high dimensional type).
	//Check to see if the user selected GEX in the independent input.
	if(divInputType == "Gene Expression")
	{
		//Put the independent filters in the GEX variables.
		fullGEXGeneList 	= String(divInputGeneList);
		fullGEXSampleType 	= String(divInputSample);
		fullGEXTissueType 	= String(divInputTissue);
		fullGEXTime			= String(divInputTime);
		fullGEXGPL 			= String(divInputGPL);
		
		//This flag will tell us to write the GEX text file.
		mrnaData = true;
		
		//Fix the platform to be something the R script expects.
		divInputType = "MRNA";		
	}
	
	//Check to see if the user selected SNP in the independent input.
	if(divInputType == "SNP")
	{
		//The genes entered into the search box were SNP genes.
		fullSNPGeneList 	= String(divInputGeneList);
		fullSNPSampleType 	= String(divInputSample);
		fullSNPTissueType 	= String(divInputTissue);
		fullSNPTime 		= String(divInputTime);
		fullSNPGPL 			= String(divInputGPL);
		
		//This flag will tell us to write the SNP text file.
		snpData = true;
	}

	if(fullGEXGeneList == "" && divInputType == "MRNA")
	{
		Ext.Msg.alert("No Genes Selected", "Please specify Genes in the Gene/Pathway Search box.")
		return false;
	}
	
	if(fullSNPGeneList == "" && divInputType == "SNP")
	{
		Ext.Msg.alert("No Genes Selected", "Please specify Genes in the Gene/Pathway Search box.")
		return false;
	}
		
	//If we don't have a platform, fill in Clinical.
	if(divInputPlatform == null || divInputPlatform == "") divInputType = "CLINICAL"
	
	formParams["div" + variableNameOfDiv + "Variabletimepoints"]		= window['div' + variableNameOfDiv + 'Variabletimepoints1'];
	formParams["div" + variableNameOfDiv + "Variablesamples"]			= window['div' + variableNameOfDiv + 'Variablesamples1'];
	formParams["div" + variableNameOfDiv + "VariablerbmPanels"]			= window['div' + variableNameOfDiv + 'VariablerbmPanels1'];
	formParams["div" + variableNameOfDiv + "Variableplatforms"]			= divInputPlatform;
	formParams["div" + variableNameOfDiv + "Variablegpls"]				= window['div' + variableNameOfDiv + 'VariablegplsValue1'];
	formParams["div" + variableNameOfDiv + "Variabletissues"]			= window['div' + variableNameOfDiv + 'Variabletissues1'];
	formParams["div" + variableNameOfDiv + "VariableprobesAggregation"]	= window['div' + variableNameOfDiv + 'VariableprobesAggregation'];
	formParams["div" + variableNameOfDiv + "VariableSNPType"]			= window['div' + variableNameOfDiv + 'VariableSNPType'];
	formParams["div" + variableNameOfDiv + "VariableType"]				= divInputType;
	formParams["div" + variableNameOfDiv + "VariablePathway"]			= divInputGeneList;
	formParams["div" + variableNameOfDiv + "PathwayName"]				= window['div' + variableNameOfDiv + 'VariablepathwayName'];
	
	//TODO, This logic needs to be changed to be additive, not destructive.
	formParams["gexpathway"]									= fullGEXGeneList;
	formParams["snppathway"]									= fullSNPGeneList;
	
	formParams["mrnaData"]										= mrnaData;
	formParams["snpData"]										= snpData;
	formParams["gexgpl"]										= fullGEXGPL;
	formParams["snpgpl"]										= fullSNPGPL;
	
	return true;
}

