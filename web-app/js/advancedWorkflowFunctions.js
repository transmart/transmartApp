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
  

/*
 * This file contains the methods responsible for handling some of the initial advanced workflow functionality.
 
 * GLOBAL.HeatmapType is determined by the button click on the advanced menu.
 * Heatmap = 'Compare'
 * Hierarchical Clustering = 'Cluster'
 * K-Means Clustering = 'KMeans'
 * Comparative Marker Selection (Heatmap) = 'Select'
 * Principal Component Analysis = 'PCA'


*/

//*******************************************************************
//After we have selected to run a heat map from the Advanced workflow menu., these functions run.
//*******************************************************************

//This is fired after an advanced workflow button is clicked that generates a heatmap.
function validateHeatmap()
{
	//Determine if the subsets are actually filled in.
	if(isSubsetEmpty(1) && isSubsetEmpty(2))
	{
		alert('Empty subset found, need at least one subset to plot a heatmap');
		return;
	}
	
	//If we are doing Comparative marker selection we need two subsets.
	if((GLOBAL.HeatmapType=='Select')&&(isSubsetEmpty(1) || isSubsetEmpty(2)))
	{
		alert('Comparative marker selection requires two non-empty subsets');
		return;
	}
	
	//This runs some queries to populate the CurrentSubSetIDs.
	if((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] == null) || (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] == null))
	{
		runAllQueries(validateHeatmap);
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
					validateheatmapComplete(result,"");
				},
				failure : function(result, request)
				{
					validateheatmapComplete(result,"");
				}
			}
	);
}

//*******************************************************************
//After the Run Workflow button is clicked on the advanced workflow popup, these functions run.
//*******************************************************************

//Sample side
function finalAdvancedMenuValidationSample()
{
	//Hide the window that we select our gene/pathway from.
	compareGeneSelection.hide();
	
	Ext.Ajax.request(
			{						
				url: pageInfo.basePath+"/asyncJob/createnewjob",
				method: 'POST',
				success: function(result, request)
				{
					RunHeatMapSample(result, 
							GLOBAL.DefaultCohortInfo.SampleIdList, 
							GLOBAL.CurrentPathway, 
							GLOBAL.DataType, 
							GLOBAL.HeatmapType,
							GLOBAL.resulttype, 
							GLOBAL.nClusters, 
							GLOBAL.CurrentTimepoints[0], 
							GLOBAL.CurrentTimepoints[1], 
							GLOBAL.CurrentSamples[0],
							GLOBAL.CurrentSamples[1], 
							GLOBAL.CurrentRbmpanels[0], 
							GLOBAL.CurrentRbmpanels[1]);
				},
				failure: function(result, request)
				{
					Ext.Msg.alert('Status', 'Unable to create the heatmap job.');
				},
				timeout: '1800000',
				params: {jobType:  GLOBAL.HeatmapType}
			})
}

//DatasetExplorer
function finalAdvancedMenuValidation()
{

	//Get the ID's of our subsets.
    var setid1 = GLOBAL.CurrentSubsetIDs[1];
	var setid2 = GLOBAL.CurrentSubsetIDs[2];
	
	//Don't allow form submission of the Number of Clusters < 1 or > 100.
	var numClustersValid = true;
	
	//Only validate the number of clusters box if this is KMeans clustering.
	if(GLOBAL.HeatmapType == 'KMeans')
	{
		if(GLOBAL.nClusters < 1 || GLOBAL.nClusters > 100)
			{
				numClustersValid = false;
			}
	}
	
	//Compare platforms.
	var platformsMatch=false;
	
	//Determine if platforms match, or if we are missing an ID.
	if ((GLOBAL.CurrentPlatforms[0]==GLOBAL.CurrentPlatforms[1]) || (!setid1) || (!setid2))
	{
		platformsMatch=true;
	}
	
	//Ensure pathway is present when required
	var sp=Ext.get("searchPathway");
	var pathwayEmpty=false;
	
	//If the pathway searchbox is empty, set the global variable for it.
	if(sp.dom.value=='')
	{
		GLOBAL.CurrentPathway='';
	}
	
	//If we are running a clustering job and the pathway is empty.
	if (GLOBAL.HeatmapType == 'Cluster' && GLOBAL.CurrentPathway == '')
	{
		//If the platforms are both Affy, the pathway empty variable is true.
		if(platformsMatch && GLOBAL.CurrentPlatforms[0]=='MRNA_AFFYMETRIX')
		{
			pathwayEmpty=true;
		}
	} 
	
	//If we passed validation, start the workflow.
	if (platformsMatch && !pathwayEmpty && numClustersValid)
	{
		//Determine the datatype from one of the platforms.
		var datatype=(GLOBAL.CurrentPlatforms[0]) ? GLOBAL.CurrentPlatforms[0]:GLOBAL.CurrentPlatforms[1];
		
		//Hide the popup window.
		compareStepPathwaySelection.hide();
		
		//Fire off the ajax call to start the job.
		Ext.Ajax.request({						
			url: pageInfo.basePath+"/asyncJob/createnewjob",
			method: 'POST',
			success: function(result, request)
			{
				RunHeatMap(
						result, 
						setid1, 
						setid2, 
						GLOBAL.CurrentPathway, 
						datatype, 
						GLOBAL.HeatmapType,
						GLOBAL.resulttype, 
						GLOBAL.nClusters, 
						GLOBAL.CurrentTimepoints[0], 
						GLOBAL.CurrentTimepoints[1], 
						GLOBAL.CurrentSamples[0],
						GLOBAL.CurrentSamples[1], 
						GLOBAL.CurrentRbmpanels[0], 
						GLOBAL.CurrentRbmpanels[1]);
			},
			failure: function(result, request){
				Ext.Msg.alert('Status', 'Unable to create the heatmap job.');
			},
			timeout: '1800000',
			params: {jobType:  GLOBAL.HeatmapType}
		});					
				
	}else{
		if(!platformsMatch){
			alert('Platforms do not match');
		}else if(pathwayEmpty){
			alert('Please specify a pathway before continuing');
		}else if(!numClustersValid){
			alert('Number of clusters is invalid!')
		}
	}	

}
//*******************************************************************



function runVisualizerFromSpan(viewerURL, altviewerURL) {
	//genePatternLogin();

	//genePatternReplacement();
	Ext.Ajax.request(
	{
		url: viewerURL,
		method: 'GET',
		success: function(result, request){
			//Ext.MessageBox.hide();
			runAppletFromSpan(result, 'visualizerSpan0');
		},
		failure: function(result, request){
			//Ext.MessageBox.hide();
			alert('Failed in getting the content of ' + viewerURL);
		},
		timeout: '1800000'
	});

	if (altviewerURL == undefined || altviewerURL == "") {
		return;
	}
	
	Ext.Ajax.request(
	{
		url: altviewerURL,
		method: 'GET',
		success: function(result, request){
			runAppletFromSpan(result, 'visualizerSpan1');
		},
		failure: function(result, request){
			alert('Failed in getting the content of ' + viewerURL);
		},
		timeout: '1800000'
	});
}

function runAppletFromSpan(result, spanId) {

	var error = result.error;
	if (error != undefined) {
		alert(error);
	}
	else {
		var text = result.responseText;
		var idxAddVis = text.lastIndexOf('addVisualizer');
		var idxStart = text.indexOf('name=', idxAddVis);
		var idxEnd = text.indexOf('\'', idxStart + 1);
		var appletTagPart = text.substring(idxStart, idxEnd);
		var appletTag = '<applet ' + appletTagPart + '</applet>';
		document.getElementById(spanId).innerHTML = appletTag;
	}
}

//TODO-PLUGIN:This should be part of the plugin
function RunHeatMapSample(result, sampleIdList, pathway, datatype, analysis,
		resulttype, nclusters, timepoints1, timepoints2, sample1,
		sample2, rbmPanels1, rbmPanels2)	
{
	var jobNameInfo = Ext.util.JSON.decode(result.responseText);					 
	var jobName = jobNameInfo.jobName;
	
	genePatternReplacement();
	showJobStatusWindow(result);	
	genePatternLogin();

	Ext.Ajax.request(
			{						
				url: pageInfo.basePath+"/genePattern/runheatmapsample",
				method: 'POST',
				timeout: '1800000',
				params: {
					sampleIdList: Ext.encode(sampleIdList),
					pathway_name:  pathway,
					datatype:  datatype,
					analysis:  analysis,
					resulttype: resulttype,
					nclusters: nclusters,
					timepoints1: timepoints1,
					timepoints2: timepoints2,
					sample1: sample1,
					sample2: sample2,
					rbmPanels1: rbmPanels1,
					rbmPanels2: rbmPanels2,
					jobName: jobName,
					json: Ext.encode(sampleIdList)
				}
		});

	checkJobStatus(jobName);	
}

//Show Gene selection.
function showGeneSelection()
{
	if(!this.compareGeneSelection)
	{
		compareGeneSelection = new Ext.Window({
		id: 'compareGeneSelectionWindow',
		title: 'Compare Gene Selection',
		layout:'fit',
		width:450,
		autoHeight: true,
		closable: false,
		plain: true,
		modal: true,
		border:false,
		resizable: false,		
		buttons: [
		          {
		            id: 'compareGeneSelectionOKButton',
		            text: 'Run Workflow',
		            handler: function()
		            	{       
							//If we come from the sample side we handle the code a little different.
							if(GLOBAL.Explorer == "SAMPLE")
							{
								finalAdvancedMenuValidationSample();
								return;
							}
				
							finalAdvancedMenuValidation();
		
		            	}
		          }
		          ,
		          {
		        	  text: 'Cancel',
		        	  handler: function() 
	              		{
		        		  compareGeneSelection.hide();
	              		}
		           }],
		 autoLoad: {
		   url: pageInfo.basePath+'/panels/compareGeneSelection.html',
		   scripts: true,
		   nocache:true, 
		   discardUrl:true,
		   method:'POST'
		 		},
		tools:[{
			id:'help',
		qtip:'Click for context sensitive help',
		handler: function(event, toolEl, panel){
			D2H_ShowHelp(advancedWorkflowContextHelpId, helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
		    }
		}]
		});
	}
	else
	{
		resetCohortInfoValues();
    }
	
	//Show the window we just created.
	compareGeneSelection.show(viewport); 

	// toggle display of "k" selector for k-means clustering
	if (document.getElementById("divnclusters") != null) 
	{
		if (GLOBAL.HeatmapType == 'KMeans') 
		{
			document.getElementById("divnclusters").style.display = "";
		} 
		else 
		{
			document.getElementById("divnclusters").style.display = "none";
		}
	}

	// toggle display of Gene/Pathway selector
	if (document.getElementById("divpathway") != null) 
	{
		if (GLOBAL.HeatmapType == 'Select' || GLOBAL.HeatmapType=='PCA') 
		{
			document.getElementById("divpathway").style.display = "none";
		} 
		else 
		{
			document.getElementById("divpathway").style.display = "";
		}
	}
}