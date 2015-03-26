/**
 * This file contains functions for handling the UI elements related to the concept code reporting system.
 */

/**
 * When the user clicks to save the report, prompt for a name/description/public flag and submit to the controller.
 */
function saveCodeReport() 
{
	jQuery( "#saveReportDialog" ).dialog({title: 'Save Report', modal:true});
	jQuery( "#saveReportDialog" ).dialog("open");
}

/**
 * When the user clicks to save a report, send off the ajax call to save the record.
 * @param newReport
 * @param reportName
 * @param reportDescription
 * @param reportPublic
 * @param parConceptList
 */
function saveReport(newReport,reportName,reportDescription,reportPublic,parConceptList,studiesList)
{
	//Validate the user input.
	if(reportName == "")
	{
		Ext.Msg.alert('Missing Input','Please enter a report name.');
		return;
	}
	
	//Verify the user isn't mixing studies.
	var uniqueStudies = studiesList.unique();
	
	if(uniqueStudies.size() > 1)
	{
		Ext.Msg.alert('Mixed Studies','Please only select concepts from the same study.');
		return;
	}	
	
	//Verify the user selected at least one study.
	if(uniqueStudies.size() < 1)
	{
		Ext.Msg.alert('Missing Parameters','Please drag in a list of parameters to the results/analysis screen before saving a report.');
		return;
	}		
	
	//Pull the unique study.
	var uniqueStudy = uniqueStudies.unique()[0].split(":")[1];
	
	jQuery.ajax({
		  url: pageInfo.basePath + '/report/saveReport',
		  success:function(data){
			  jQuery( "#saveReportDialog" ).dialog("close");
			  resultsTabPanel.setActiveTab('workspacePanel');
			  },
		  failure:function(data){alert("Report failed to save.");},
		  data: {	name:reportName,
			  		description:reportDescription,
			  		publicflag:reportPublic,
			  		conceptList:parConceptList,
			  		study:uniqueStudy}
		});
}
/**
 * Determine if the Report is a SummaryStatistics or Adv. Workflow
 * @param moduleName
 */ 
function runReportOrAnalysis(reportId, reportStudy, moduleName){
	//Verify a subset was selected.
	if(isSubsetEmpty(1) && isSubsetEmpty(2))
    {
        Ext.Msg.alert('Subsets are empty', 'All subsets are empty. Please select subsets.');
        return;
    }	
	// If its a Summary Statistics report
	if(moduleName  && moduleName == "Summary Statistics"){
		generateReportFromId(reportId, reportStudy);
	}else {  //otherwise its a RModule
		loadAdvWorkflowAnalysis(reportId,reportStudy,moduleName);
	}
}

/**
 * If a subset is loaded, pull the codes for that report from the database and generate the statistics for each.
 * @param reportId
 */
function generateReportFromId(reportId, reportStudy)
{
	//Verify a subset was selected.
	if(isSubsetEmpty(1) && isSubsetEmpty(2))
    {
        Ext.Msg.alert('Subsets are empty', 'All subsets are empty. Please select subsets.');
        return;
    }	
	
	//Before running the report clear out global report codes and study arrays
	GLOBAL.currentReportCodes=[];
	GLOBAL.currentReportStudy=[];
	
	//Move the user to the analysis tab.
	resultsTabPanel.setActiveTab('analysisPanel');
	
	resultsTabPanel.body.mask("Generating Summary Statistics", 'x-mask-loading');
	
	//If the code hasn't been run to build the cohorts, do so here.
    if((GLOBAL.CurrentSubsetIDs[1] == null && ! isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] == null && ! isSubsetEmpty(2)))
    {
        runAllQueries(function(){generateReportFromId(reportId, reportStudy);});
        return;
    }	

    //Get the basic summary statistics. When that is done processing run "pullReportCodes" which will run an analysis per code.
	getSummaryStatistics(pullReportCodes, [reportId, reportStudy]);
	
}

function pullReportCodes(reportParams)
{
	resultsTabPanel.body.mask("Generating Report From Saved Codes", 'x-mask-loading');
	
	//Get the JSON list of codes for this report.
	jQuery.ajax({
		  url: pageInfo.basePath + '/report/retrieveReportCodes',
		  success:function(returnedData){drawReports(returnedData, reportParams[1]);},
		  failure:function(returnedData){resultsTabPanel.body.unmask();alert("There was an error retrieving your report.");},
		  data: {reportid:reportParams[0]}
		});
}

/**
 * Function used to iterate through codes and build the analysis for each.
 * @param returnedData
 */
function drawReports(returnedData, reportsStudy)
{
	for(var i=0;i<returnedData.length;i++)
	{
		buildAnalysisFromCode(returnedData[i],i==returnedData.length-1, reportsStudy);
	}
}


function buildAnalysisFromCode(nodeCode, lastCode, reportsStudy)
{
    Ext.Ajax.request(
            {
                url : pageInfo.basePath+"/chart/analysis",
                method : 'POST',
                timeout: '600000',
                params :  Ext.urlEncode(
                        {
                            charttype : "analysis",
                            concept_key : nodeCode,
                            result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
                            result_instance_id2 : GLOBAL.CurrentSubsetIDs[2]
                        }
                ), // or a URL encoded string
                success : function(result, request)
                {
                	GLOBAL.currentReportCodes.push(nodeCode);
                	GLOBAL.currentReportStudy.push(reportsStudy);
                	buildAnalysisComplete(result);
                	if(lastCode) resultsTabPanel.body.unmask();
                }
	            ,
	            failure : function(result, request)
	            {
	                buildAnalysisComplete(result);
	                resultsTabPanel.body.unmask();
	            }
            }
    );
}

/**
 * If a subset is loaded, pull the codes for that report from the database and generate the statistics for each.
 * @param reportId
 */
function deleteReportFromId(reportId)
{
	if(confirm("Report will be deleted. Proceed?")){
		jQuery.ajax({
			  url: pageInfo.basePath + '/report/deleteReport',
			  success:function(data){
					var rowEle = document.getElementById("reportRow"+reportId);
					var rowIndex = reportsTable.fnGetPosition(rowEle); 
					reportsTable.fnDeleteRow(rowIndex);
			  },
			  failure:function(data){alert("Report failed to delete.");},
			  error:function(data){alert("Report failed to delete.");},
			  data: {reportId:reportId}
			});
	}
}

function clearSavedReports()
{
	//Clear the items used to save reports.
	if(document.getElementById('txtReportName')){
		document.getElementById('txtReportName').value = "";
	}
	if(document.getElementById('txtReportDescription')){
		document.getElementById('txtReportDescription').value = "";
	}
	if(document.getElementById('chkReportPublic')){
		document.getElementById('chkReportPublic').checked = false;
	}

	GLOBAL.currentReportCodes = [];
	GLOBAL.currentReportStudy = [];
}

function editReportName(reportId){
	jQuery("#reportNameDisplay"+reportId).hide();
	jQuery("#editReportNameBox"+reportId).show();
	jQuery("#editReportNameBox"+reportId).focus();
}

function updateReportName(reportId){
	var name = jQuery("#editReportNameBox"+reportId).val();
	jQuery.get(pageInfo.basePath + '/report/updateName', {reportId:reportId, name:name}, function(data){
		renderWorkspace();
	});
}

function toggleReportPublicFlag(reportId){
	jQuery.get(pageInfo.basePath + '/report/togglePublicFlag', {reportId:reportId}, function(data){
		if(data=='true'){
			jQuery("#reportPublicFlag"+reportId).removeClass("ui-icon-locked");
			jQuery("#reportPublicFlag"+reportId).addClass("ui-icon-unlocked");
		}else if(data=='false'){
			jQuery("#reportPublicFlag"+reportId).removeClass("ui-icon-unlocked");
			jQuery("#reportPublicFlag"+reportId).addClass("ui-icon-locked");
		}
	}).fail(function() { alert("Server Error in toggling public flag"); });
}

function displayReportCodes(event, reportId){
	var posX=event.clientX;
	var posY=event.clientY;
	displayReportCodesFunction = setTimeout(function(){
		//Get the JSON list of codes for this report.
		jQuery.ajax({
			  url: pageInfo.basePath + '/report/retrieveReportCodes',
			  success:function(returnedData){
				  var reportCodes="<b>Variables</b><br>";
				  for(var i=0;i<returnedData.length;i++){
					  	reportCodes=reportCodes+"(";
						reportCodes=reportCodes+returnedData[i];
						reportCodes=reportCodes+")";
						reportCodes=reportCodes+"<br>";
				  }
				jQuery(workspaceReportCodesDisplayDialog).dialog("option", {position:[posX+20, posY+60]});
				jQuery(workspaceReportCodesDisplayDialog).data("displayData",reportCodes).dialog("open");
			  },
			  failure:function(returnedData){alert("There was an error retrieving your report.");},
			  data: {reportid:reportId}
			});
	}, 500);
}

function hideReportCodes(){
	clearTimeout(displayReportCodesFunction);
	jQuery(workspaceReportCodesDisplayDialog).dialog("close");
}

/**
 * If a subset is loaded, pull the codes for that report from the database and load the analysis
 * @param reportId
 * @param reportStudy
 */
function loadAdvWorkflowAnalysis(reportId, reportStudy,moduleName)
{
	//Verify a subset was selected.
	if(isSubsetEmpty(1) && isSubsetEmpty(2))
    {
        Ext.Msg.alert('Subsets are empty', 'All subsets are empty. Please select subsets.');
        return;
    }	
	
	//Before running the report clear out global report codes and study arrays
	GLOBAL.currentAnalysisParams=[];
	GLOBAL.currentReportStudy=[];
	GLOBAL.returnedAnalysisData = [];
	//resultsTabPanel.body.mask("loading Analysis Parameters From Database", 'x-mask-loading');
	
	//Move the user to theAdv. Workfows tab.
	resultsTabPanel.setActiveTab('dataAssociationPanel');
	
	//Get the JSON list of codes for this report.
	jQuery.ajax({
		  url: pageInfo.basePath + '/report/retrieveReportCodes',
		  success:function(returnedData){openAnalysis(moduleName, returnedData, reportStudy);},
		  failure:function(returnedData){resultsTabPanel.body.unmask();alert("There was an error retrieving your analysisParameters.");},
		  data: {reportid:reportId}
		});
	
//	jQuery(document).ready(function() {
//		//alert("It's loaded!");
//		//populateAnalysis(GLOBAL.returnedAnalysisData[1]);
//	})
	
	 jQuery(document).ready(function () { 
		 setTimeout(function () { 
				var selectedAnalysis = document.getElementById("analysis").value;
				selectedAnalysis = selectedAnalysis.charAt(0).toUpperCase()+selectedAnalysis.substring(1);
				
				var funcName = "populate"+selectedAnalysis;
				
				if (typeof funcName == 'string' && eval('typeof ' + funcName) == 'function') 
				{
					eval(funcName +'()');
				}
			 //populateAnalysis(GLOBAL.returnedAnalysisData[1]);
			 
			 }, 3000); });
	
}

function openAnalysis(moduleName, returnedData, reportStudy){
	
	var menuItem = Ext.getCmp(moduleName);
	onItemClick(menuItem);
    clearDataAssociation();
	GLOBAL.returnedAnalysisData[1] = returnedData;

}
