// analog of registerSOMETHINGDrugAndDrop functions in /plugins/rdc-modules-0.1/js/plugin/*.* files
function registerMetaCoreEnrichmentDragAndDrop()
{
	//Set up drag and drop for Dependent and Independent variables on the data association tab.
	//Get the Independent DIV
	var independentDiv = Ext.get("divIndependentVariableMetaCoreEnrichment");
	dtgI = new Ext.dd.DropTarget(independentDiv,{ddGroup : 'makeQuery'});
	dtgI.notifyDrop = dropOntoCategorySelection;
} 

function initMetaCoreTab()
{
	GLOBAL.Analysis = "MetaCoreEnrichment";
	registerMetaCoreEnrichmentDragAndDrop();
}

// TODO: needs refactoring: copy of renderCohortSummary from /plugins/rdc-modules-0.1/js/dataAssociations.js accepting divId
function renderCohortSummaryMetaCoreEnrichment(cohortSummaryDisplayId) {
	var cohortsSummary = ""
	for ( var i = 1; i <= GLOBAL.NumOfSubsets; i++) {
		var currentQuery = getQuerySummary(i)
		if (currentQuery != "") {
			cohortsSummary += "Subset " + i + ": "
			cohortsSummary += currentQuery
			cohortsSummary += "<br>"
		}
	}
	var innerHtml = ""
	if ("" == cohortsSummary) {
		innerHtml = "<font style='color:red;font-weight:bold;'>Warning! You have not selected a study and the analyses will not work. Please go back to the 'Comparison' tab and make a cohort selection.</font>";
	} else {
		innerHtml = cohortsSummary;
	}
	document.getElementById(cohortSummaryDisplayId).innerHTML = innerHtml;
}

//TODO: needs refactoring: modified copy of submitHeatmapJob from /plugins/rdc-modules-0.1/js/plugin/Heatmap.js
function submitMetaCoreEnrichmentJob(form) {
	var independentVariableConceptCode = "";
	independentVariableConceptCode = readConceptVariables("divIndependentVariableMetaCoreEnrichment");
	var variablesConceptCode = independentVariableConceptCode;
	// ----------------------------------
	// Validation
	// ----------------------------------
	// This is the independent variable.
	var independentVariableEle = Ext.get("divIndependentVariableMetaCoreEnrichment");
	// Get the types of nodes from the input box.
	var independentNodeList = createNodeTypeArrayFromDiv(
			independentVariableEle, "setnodetype")
	// Validate to make sure a concept was dragged in.
	if (independentVariableConceptCode == '') {
		Ext.Msg
				.alert('Missing input',
						'Please drag at least one concept into the Heatmap Variable box.');
		return;
	}
	if ((independentNodeList[0] == 'valueicon' || independentNodeList[0] == 'hleaficon')
			&& (independentVariableConceptCode.indexOf("|") != -1)) {
		Ext.Msg
				.alert(
						'Wrong input',
						'For continuous and high dimensional data, you may only drag one node into the input boxes. The heatmap variable input box has multiple nodes.');
		return;
	}
	// ----------------------------------
	var formParams = {
		independentVariable : independentVariableConceptCode,
		variablesConceptPaths : variablesConceptCode,
		jobType : 'MetaCoreEnrichment'
	};
	// Use a common function to load the High Dimensional Data params.
	loadCommonHighDimFormObjects(formParams, "divIndependentVariableMetaCoreEnrichment")
	// ------------------------------------
	// More Validation
	// ------------------------------------
	// If the user dragged in a high dim node, but didn't enter the High Dim
	// Screen, throw an error.
	if (independentNodeList[0] == 'hleaficon'
			&& formParams["divIndependentVariableMetaCoreEnrichmentType"] == "CLINICAL") {
		Ext.Msg
				.alert(
						'Wrong input',
						'You dragged a High Dimensional Data node into the category variable box but did not select any filters! Please click the "High Dimensional Data" button and select filters. Apply the filters by clicking "Apply Selections".');
		return;
	}
	// For the time being if the user is trying to run anything but GEX, stop
	// them.
	if (formParams["divIndependentVariableMetaCoreEnrichmentType"] != "MRNA") {
		Ext.Msg
				.alert(
						"Invalid selection",
						"MetaCore Enrichment Analysis only supports GEX data at this time. Please drag a Gene Expression node into the Heatmap variable and click the 'High Dimensional Data' button.")
		return false;
	}
	//------------------------------------
	
	submitMetaCoreEnrichment(formParams); // instead of submitJob(formParams);
}

// modified version of submitJob from /plugins/rdc-modules-0.1/js/dataAssociations.js
// it doesn't run a job, it just posts request to controller
function submitMetaCoreEnrichment(formParams) {
	// Make sure at least one subset is filled in.
	if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
		Ext.Msg.alert('Missing input!',
				'Please select a cohort from the \'Comparison\' tab.');
		return;
	}

	Ext.Ajax.request({
		url : pageInfo.basePath+'/metacoreEnrichment/runAnalysis',
		method: 'POST',
		timeout: '1800000',
		success : function(response, request) {
			alert("SUCCESS: " + response.responseText); // TODO: process response
		},
		failure : function(response, request) {
			alert("ERROR: " + response.statusText); // TODO: process error
		}
	});
}