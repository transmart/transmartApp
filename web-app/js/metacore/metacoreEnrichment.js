// --- enrichment display ----
	
	BAR_MAX = 400;
	BAR_WIDTH = 10;
	TICKS_COUNT = 8;
	TICK_FONT_FAMILY = 'Verdana, Helvetica, sans-serif';
	TICK_FONT_SIZE = 10;
	TICK_FONT_WEIGHT = 100;
	
	function drawScale(min, max) {
		var R = Raphael("scale", 500, 20);
		R.path("M0,0L400,0").attr({ stroke: "black" });
		var tick_step = (max - min) / TICKS_COUNT;
		var tick_width = BAR_MAX / TICKS_COUNT;
		for (var i=0; i<TICKS_COUNT; i++) {
			var tick_value = min + i*tick_step;
			tick_value = tick_value.toFixed(1);
			var tick_offset = i*tick_width;
			R.path("M" + tick_offset + ",0v5").attr({ stroke: "black" });
			R.text(tick_offset, 9, tick_value).attr({ 
				'font-family': TICK_FONT_FAMILY, 'font-size': TICK_FONT_SIZE, stroke: 'white', 'stroke-width': 0,
				'text-anchor': (i==0)?'start':'middle', 'font-weight': TICK_FONT_WEIGHT 
			});
		}
		R.path("M" + BAR_MAX + ",0v5").attr({ stroke: "black" });
		R.text(BAR_MAX, 9, max.toFixed(1)).attr({ 'font-family': TICK_FONT_FAMILY, 'font-size': TICK_FONT_SIZE, stroke: 'black', 'stroke-width': 0, 'text-anchor': 'end', 'font-weight': TICK_FONT_WEIGHT });
		
		R.text(BAR_MAX+5, 9, "-log(pValue)").attr({ 'font-family': TICK_FONT_FAMILY, 'font-size': TICK_FONT_SIZE, stroke: 'black', 'stroke-width': 0, 'text-anchor': 'start', 'font-weight': 'bold' });
	}
	
	function drawEnrichment(data) {
		jQuery('#metacoreEnrichmentResults').css('display', 'block'); // show results div
		
		// determine min/max value
		var min = 0;
		var max = 0;
		
		for (var i=0; i<data.enrichment.process.length; i++) {
			var val = data.enrichment.process[i].exp[0].value;
			max = Math.max(max, val);
			min = Math.min(min, val);
		}
		
		// draw enrichment
		for (var i=0; i<data.enrichment.process.length; i++) {
			var proc = data.enrichment.process[i];
			var cell_id = "cell" + proc.id;
			jQuery('#enrichment  > tbody:last').append(
				'<tr><td>' + (i+1) + '</td><td>'
				+ '<a href="' + GLOBAL.metacoreUrl + data.enrichment.info_url + proc.id + '">' + proc.name + '</a>' 
				+ '</td><td id="' + cell_id 
				+ '"</td><td>'+ proc.val.toExponential(3) 
				+'</td></tr>'
			);
			var R = Raphael(cell_id, 450, 10);
			var len = (proc.exp[0].value - min) / (max - min) * BAR_MAX;
			R.rect(0,0,0,10).attr({ fill: "90-#ffb4bb-#ffb400:20-#ffb400:80-#ffb4bb", stroke: 'none'}).animate({ width: len }, 1000)
			R.text(BAR_MAX+5, 5, proc.exp[0].value.toPrecision(5)).attr({ 'font-family': TICK_FONT_FAMILY, 'font-size': TICK_FONT_SIZE, stroke: 'white', 'stroke-width': 0, 'text-anchor': 'start', 'font-weight': TICK_FONT_WEIGHT });
		}
		
		drawScale(min, max);
	}

// --------- form parameters ---------

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
	
	jQuery.fn.scrollView = function () {
	    return this.each(function () {
	        $('html, body').animate({
	            scrollTop: $(this).offset().top
	        }, 1000);
	    });
	}
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
	
	// TODO: prepare data here, fetch filename
	
	
	// run enrichment, TODO: pass exp. data filename to the controller
	
	var spinnerMask = new Ext.LoadMask(Ext.getBody(), {msg:"Running Enrichment Analysis, Please Wait..."});
	spinnerMask.show();

	Ext.Ajax.request({
		url : pageInfo.basePath+'/metacoreEnrichment/runAnalysis',
		method: 'POST',
		timeout: '1800000',
		success : function(response, request) {
			spinnerMask.hide();
			var data = Ext.decode(response.responseText);
			if (data.Code == 0) {
				drawEnrichment(data.Result[0]);
				jQuery('#metacoreEnrichmentResults').scrollView();
			}
		},
		failure : function(response, request) {
			spinnerMask.hide();
			alert("ERROR: " + response.statusText); // TODO: process error
		}
	});
}	
	
