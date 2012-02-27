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
function renderCohortSummary(){
	var q1 = getQuerySummary(1);
	var innerHtml = ""
	if(""==q1){
		innerHtml = "<font style='color:red;font-weight:bold;'>Warning! You have not selected a study and the analyses will not work. Please go back to the 'Comparison' tab and make a cohort selection.</font>";
	}else{
		innerHtml = q1;
	}
		
	
	document.getElementById("cohortSummary").innerHTML=innerHtml;
}

function checkPreviousAnalysis()
{
	//If the user clicks submit but they've run a analysis recently check with them to make sure they want to clear the results.
	if(GLOBAL.AnalysisRun)
	{
		return confirm('When you navigate to a new analysis the current analysis results will be cleared! If you would like your results to be saved click the "Save to PDF" button. Are you sure you wish to navigate away?');
	}
	
	return true;
}


function loadPluginView(){
	
	//Remove the output screen.
	document.getElementById("analysisOutput").innerHTML = "";

	//Whenever we switch views, make the binning toggle false. All the analysis pages default to this state.
	GLOBAL.Binning = false
	GLOBAL.ManualBinning = false
	GLOBAL.NumberOfBins = 4
	GLOBAL.AnalysisRun = false
	
	var selectedAnalysis = document.getElementById("analysis").value;
	selectedAnalysis = selectedAnalysis.charAt(0).toUpperCase()+selectedAnalysis.substring(1);
	eval("load"+selectedAnalysis+"View()");
	
}

//This function fires when an item is dropped onto one of the independent/dependent variable DIVs in the data association tool.
function dropOntoVariableSelection(source, e, data)
{
	data.node.attributes.oktousevalues = "N"
	var concept = createPanelItemNew(this.el, convertNodeToConcept(data.node));
	return true;
}

//This function fires when an item is dropped onto one of the
//independent/dependent variable DIVs in the data association tool.
//Used to ensure only a numeric value is dropped. For all values use dropOntoCategorySelection function
function dropNumericOntoCategorySelection(source, e, data){
	var targetdiv=this.el;
	if(data.node.leaf==false && !data.node.isLoaded()){
		data.node.reload(function(){dropNumericOntoCategorySelection2(source, e, data, targetdiv);});
		}
	else{
		dropNumericOntoCategorySelection2(source, e, data, targetdiv);
		}
	return true;
}

function dropNumericOntoCategorySelection2(source, e, data, targetdiv)
{
	//Node must be folder so use children leafs
	if(data.node.leaf==false) 
	{
		//Keep track of whether all the nodes are numeric or not.
		var allNodesNumeric = true
		
		//Keep track of whether the folder has any leaves.
		var foundLeafNode = false
		
		//Loop through child nodes to add them to input.
		for ( var i = 0; i<data.node.childNodes.length; i++)
		{
			//Grab the child node.
			var child=data.node.childNodes[i];
			
			//This tells us whether it is a numeric or character node.
			var val=child.attributes.oktousevalues;

			//If we are a numeric leaf node, add it to the tree.
			if(val==='Y' && child.leaf==true)
			{
				//Reset the alpha/numeric flag so we don't get the popup for entering a value.
				child.attributes.oktousevalues = "N"; 

				//Set the flag indicating we had a leaf node.
				foundLeafNode = true;
				
				//Add the item to the input.
				var concept = createPanelItemNew(targetdiv, convertNodeToConcept(child));
				
				//Set back to original value
				child.attributes.oktousevalues=val; 
			}
			else if(val==='N' && child.leaf==true)
			{
				//Set the flag indicating we had a leaf node.
				foundLeafNode = true;				
				
				//If we find a non-numeric node, set our flag.
				allNodesNumeric = false
			}
			
		}

		//If no leaf nodes found, alert the user.
		if(!foundLeafNode)
		{
			Ext.Msg.alert('No Nodes in Folder','When dragging in a folder you must select a folder that has leaf nodes directly under it.');
		}		
		
		//If we found a non numeric node, alert the user.
		if(!allNodesNumeric && foundLeafNode)
		{
			Ext.Msg.alert('Numeric Input Required','Please select numeric concepts only for this input. Numeric concepts are labeled with a "123" in the tree.');
		}
	}
	else 
	{
		//If we dragged a numeric leaf, add it to the input. Otherwise alert the user.
		if(data.node.attributes.oktousevalues==='Y')
		{
			//This tells us whether it is a numeric or character node.
			var val=data.node.attributes.oktousevalues;
			
			//Reset the alpha/numeric flag so we don't get the popup for entering a value.
			data.node.attributes.oktousevalues="N";
			
			//Add the item to the input.
			var concept = createPanelItemNew(targetdiv, convertNodeToConcept(data.node));
			
			//Set back to original value
			data.node.attributes.oktousevalues=val;
		}
		else
		{
			Ext.Msg.alert('Numeric Input Required','Please select numeric concepts only for this input. Numeric concepts are labeled with a "123" in the tree.');
		}		
	}
	return true;
} 


//This function fires when an item is dropped onto one of the
//independent/dependent variable DIVs in the data association tool.
function dropOntoCategorySelection(source, e, data)
{
	var targetdiv=this.el;
	if(data.node.leaf==false && !data.node.isLoaded())
	{
		data.node.reload(function(){dropOntoCategorySelection2(source, e, data, targetdiv);});
	}
	else
	{
		dropOntoCategorySelection2(source, e, data, targetdiv);
	}
	return true;
}

function dropOntoCategorySelection2(source, e, data, targetdiv)
{
	//Node must be folder so use children leafs
	if(data.node.leaf==false) 
	{

		//Keep track of whether the folder has any leaves.
		var foundLeafNode = false
		
		for ( var i = 0; i<data.node.childNodes.length; i++)
		{
			//Grab the child node.
			var child=data.node.childNodes[i];
			
			//This tells us whether it is a numeric or character node.
			var val=child.attributes.oktousevalues;
			
			//Reset the alpha/numeric flag so we don't get the popup for entering a value.
			child.attributes.oktousevalues = "N"; 
			
			//If this is a leaf node, add it.
			if(child.leaf==true)
			{
				//Add the item to the input.
				var concept = createPanelItemNew(targetdiv, convertNodeToConcept(child));
				
				//Set the flag indicating we had a leaf node.
				foundLeafNode = true;
			}
			
			//Set back to original value
			child.attributes.oktousevalues=val;
		}
		
		//If no leaf nodes found, alert the user.
		if(!foundLeafNode)
		{
			Ext.Msg.alert('No Nodes in Folder','When dragging in a folder you must select a folder that has leaf nodes directly under it.');
		}				
	}
	else 
	{
		//This tells us whether it is a numeric or character node.
		var val=data.node.attributes.oktousevalues;
		
		//Reset the alpha/numeric flag so we don't get the popup for entering a value.
		data.node.attributes.oktousevalues="N";
		
		//Add the item to the input.
		var concept = createPanelItemNew(targetdiv, convertNodeToConcept(data.node));
		
		//Set back to original value
		data.node.attributes.oktousevalues=val;
	}
	return true;
} 


function setupSubsetIds(formParams){

				runAllQueries(function(){submitJob(formParams);});

}

function readConceptVariables(divIds){
	var variableConceptCode = ""
	var variableEle = Ext.get(divIds);
	
	//If the variable element has children, we need to parse them and concatenate their values.
	if(variableEle && variableEle.dom.childNodes[0])
	{
		//Loop through the variables and add them to a comma seperated list.
		for(nodeIndex = 0; nodeIndex < variableEle.dom.childNodes.length; nodeIndex++)
		{
			//If we already have a value, add the seperator.
			if(variableConceptCode != '') variableConceptCode += '|' 
			
			//Add the concept path to the string.
				variableConceptCode += getQuerySummaryItem(variableEle.dom.childNodes[nodeIndex]).trim()
		}
	}
	return variableConceptCode;
}

function submitJob(formParams)
{
	//Make sure at least one subset is filled in.
	if(isSubsetEmpty(1) && isSubsetEmpty(2))
	{
		Ext.Msg.alert('Missing input!','Please select a cohort from the \'Comparison\' tab.');
		return;
	}	
	
	waitWindowForAnalysis();
	
	if((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] == null) || (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] == null))
			{
				setupSubsetIds(formParams);
				return;
			}
		
	Ext.Ajax.request({						
		url: pageInfo.basePath+"/job/createnewjob",
		method: 'POST',
		success: function(result, request){
			//Handle data export process
			runJob(result, formParams);
		},
		failure: function(result, request){
			Ext.Msg.alert('Status', 'Unable to create data export job.');
		},
		timeout: '1800000',
		params: formParams
	});

}

function runJob(result, formParams) {
	var jobNameInfo = Ext.util.JSON.decode(result.responseText);					 
	var jobName = jobNameInfo.jobName;

	formParams.result_instance_id1=GLOBAL.CurrentSubsetIDs[1];
	formParams.result_instance_id2=GLOBAL.CurrentSubsetIDs[2];
	formParams.analysis=document.getElementById("analysis").value;
	formParams.jobName=jobName;
	
	Ext.Ajax.request(
		{						
			url: pageInfo.basePath+"/job/scheduleJob",
			method: 'POST',
			timeout: '1800000',
			params: Ext.urlEncode(formParams) // or a URL encoded string
	});
	
	//Start the js code to check the job status so we can display results when we are done.
	checkPluginJobStatus(jobName)
}

function waitWindowForAnalysis()
{
	//Mask the panel while the analysis runs.
	Ext.getCmp('dataAssociationPanel').body.mask("Running analysis...", 'x-mask-loading');
}


//Called to check the heatmap job status 
function checkPluginJobStatus(jobName)	
{	
	var secCount = 0;
	var pollInterval = 1000;   // 1 second
	
	var updateJobStatus = function(){
		secCount++;
		Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/genePattern/checkJobStatus",
				method : 'POST',
				success : function(result, request)
				{
					var jobStatusInfo = Ext.util.JSON.decode(result.responseText);					 
					var status = jobStatusInfo.jobStatus;
					var errorType = jobStatusInfo.errorType;
					var viewerURL = jobStatusInfo.jobViewerURL;
					var altViewerURL = jobStatusInfo.jobAltViewerURL;
					var exception = jobStatusInfo.jobException;
					var resultType = jobStatusInfo.resultType;
					var jobResults = jobStatusInfo.jobResults;
					
					if(status =='Error' && errorType!='data')	
					{
						Ext.getCmp('dataAssociationPanel').body.unmask();
						Ext.Msg.alert('Please, Contact a tranSMART Administrator', 'Unable to complete: ' + exception);
						Ext.TaskMgr.stop(checkTask);
					}
					else if(status =='Error' && errorType=='data')	
					{
						Ext.getCmp('dataAssociationPanel').body.unmask();
						Ext.Msg.alert('Error running analysis', exception);
						Ext.TaskMgr.stop(checkTask);
					}
					else if(status =='Completed')	
					{
						Ext.getCmp('dataAssociationPanel').body.unmask();
						Ext.TaskMgr.stop(checkTask);
						
						var fullViewerURL = pageInfo.basePath + viewerURL;
						
						//Set the results DIV to use the URL from the job.
						Ext.get('analysisOutput').load({url : fullViewerURL});
						
						//Set the flag that says we run an analysis so we can warn the user if they navigate away.
						GLOBAL.AnalysisRun = true;
						
					} else if(status == 'Cancelled')	
					{
						Ext.getCmp('dataAssociationPanel').body.unmask();
						Ext.TaskMgr.stop(checkTask);						
					}
				},
				failure : function(result, request)
				{
					Ext.getCmp('dataAssociationPanel').body.unmask();
					Ext.Msg.alert('Failed', 'Could not complete the job, please contact an administrator');
					Ext.TaskMgr.stop(checkTask);
				},
				timeout : '300000',
				params: {jobName: jobName}
			}
		);
  	}

	var checkTask =	{
			run: updateJobStatus,
	  	    interval: pollInterval	
	}	
	Ext.TaskMgr.start(checkTask);
}

function setupCategoricalItemsList(strDivSource,strDivTarget) {
	// copy from the category div at top of page first and add drag handlers
	var categoricalSourceDiv = Ext.get(strDivSource);
	var categoricalTargetDiv = Ext.get(strDivTarget);

	// clear it out first
	while (categoricalTargetDiv.dom.hasChildNodes())
		categoricalTargetDiv.dom
				.removeChild(categoricalTargetDiv.dom.firstChild);
	for ( var i = 0, n = categoricalSourceDiv.dom.childNodes.length; i < n; ++i) {
		// clone and append
		var newnode = categoricalSourceDiv.dom.childNodes[i].cloneNode(true);
		categoricalTargetDiv.dom.appendChild(newnode);
		// add drag handler
		Ext.dd.Registry.register(newnode, {
			el : newnode
		});
	}
	var dragZone = new Ext.dd.DragZone(categoricalTargetDiv.dom.parentNode, {
		ddGroup : 'makeBin',
		isTarget: true,
		ignoreSelf: false
	});

	var dropZone = new Ext.dd.DropTarget(categoricalTargetDiv, {
		ddGroup : 'makeBin',
		isTarget: true,
		ignoreSelf: false,
		onNodeEnter: function(target, dd, e, dragData) {
		    delete this.dropOK;
		    this.dropOK=true;
		    return true;
		    
		},
		onNodeOver: function(target, dd, e, dragData) {
			var ret= this.dropOK ? this.dropAllowed : this.dropNotAllowed;
		    console.log(ret);
		    return ret;
		}
	});
	dropZone.notifyDrop = dropOntoBin;
}

function clearDataAssociation()
{
	//Remove the output screen.
	document.getElementById("analysisOutput").innerHTML = "";
	//Remove the variable selection screen.
	document.getElementById("variableSelection").innerHTML = "";
	
	//Whenever we switch views, make the binning toggle false. All the analysis pages default to this state.
	GLOBAL.Binning = false
	GLOBAL.ManualBinning = false
	GLOBAL.NumberOfBins = 4
	GLOBAL.AnalysisRun = false
	
	//Set the message below the cohort summary that lets the user know they need to select a cohort.
	renderCohortSummary();
	
}
