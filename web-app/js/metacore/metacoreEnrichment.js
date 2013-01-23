
// --------- Metacore Enrichment Tab ---------

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

function scrollToEnrichmentResults() {
	jQuery('html body').animate({ scrollTop: jQuery('#enrichment').offset().top}, 1000);
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

function runEnrichment(preparedData) {
// run enrichment
	
	jQuery('#metacoreEnrichmentResults').hide();
	jQuery('#enrichment tbody').empty();
	jQuery('#scale').empty();
	
	var spinnerMaskEnrichment = new Ext.LoadMask(Ext.getBody(), {msg:"Running enrichment analysis, please wait..."});
	spinnerMaskEnrichment.show();

	Ext.Ajax.request({
		url : pageInfo.basePath+'/metacoreEnrichment/runAnalysis',
		method: 'POST',
		timeout: '1800000',
		params: Ext.urlEncode(preparedData),
		success : function(response, request) {
			spinnerMaskEnrichment.hide();
			var data = Ext.decode(response.responseText);
			if (data.Code == 0) {
				drawEnrichment(data.Result[0]);
				scrollToEnrichmentResults();
			}
		},
		failure : function(response, request) {
			spinnerMaskEnrichment.hide();
			alert("ERROR: " + response.statusText); // TODO: process error
		}
	});
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
	// converting form parameters to normal values (quick dirty hack)
	for (var p in formParams) { 
		if (p.indexOf('MetaCoreEnrichment') > 0) { 
			var new_p = p.replace('MetaCoreEnrichment', ''); 
			formParams[new_p] = formParams[p]; 
			delete formParams[p]; 
		}
	}
	
	formParams.result_instance_id1=GLOBAL.CurrentSubsetIDs[1];
	formParams.result_instance_id2=GLOBAL.CurrentSubsetIDs[2];
	formParams.analysis='heatmap'; // to fool RModulesService

	var z_threshold_abs = jQuery('#z_threshold_abs').val()
	
	var spinnerMask = new Ext.LoadMask(Ext.getBody(), {msg:"Preparing data, please wait..."});
	spinnerMask.show();
	
	var preparedData;
	
	Ext.Ajax.request({
		url : pageInfo.basePath+'/metacoreEnrichment/prepareData',
		method: 'POST',
		timeout: '1800000',
		params: Ext.urlEncode(formParams),
		success : function(response, request) {
			spinnerMask.hide();
			preparedData = Ext.decode(response.responseText);
			preparedData.zThresholdAbs = z_threshold_abs;
			runEnrichment(preparedData);
		},
		failure : function(response, request) {
			spinnerMask.hide();
			alert("ERROR: " + response.statusText); // TODO: process error
		}
	});
	
}	

// ------------ Marker Selection Analysis -------------

function markerSelectionRunMetacoreEnrichment() {
	var ids = [];
	var idType = "GENESYMBOL";
	var idCol = 0;
	
	// TODO: this is potentially dangerous, need to convert to numbers before adding to list
	var test_id = jQuery(jQuery('#markerSelectionTable tbody tr').children('td')[1]).html();
	if (/_at$/.test(test_id)) {
		idType = "AFFYMETRIX";
		idCol = 1;
	}
	
	jQuery('#markerSelectionTable tbody tr').each(function() { 
		var cell = jQuery(jQuery(this).children('td')[idCol]).html();
		if (! /^\s+$/.test(cell))
			ids.push(cell); 
	});
	
	jQuery('#marker_metacoreEnrichmentResults').hide();
	jQuery('#marker_enrichment tbody').empty();
	jQuery('#marker_scale').empty();
	
	var spinnerMaskEnrichment = new Ext.LoadMask(Ext.getBody(), {msg:"Running enrichment analysis, please wait..."});
	spinnerMaskEnrichment.show();

	Ext.Ajax.request({
		url : pageInfo.basePath+'/metacoreEnrichment/runAnalysisForMarkerSelection',
		method: 'POST',
		timeout: '1800000',
		params: Ext.urlEncode({'IdList': ids, 'IdType': idType}),
		success : function(response, request) {
			spinnerMaskEnrichment.hide();
			var data = Ext.decode(response.responseText);
			if (data.Code == 0) {
				drawEnrichment(data.Result[0], 'marker_');
				scrollToEnrichmentResults();
			}
		},
		failure : function(response, request) {
			spinnerMaskEnrichment.hide();
			alert("ERROR: " + response.statusText); // TODO: process error
		}
	});

}

// ----------- Server settings ---------------

function resetSettingsButton(mode) {
	// if you change class/title here, don't forget to modify the _metacoreSettingsButton template
	if (mode == 'user') {
		jQuery('#metacoreSettingsButton').attr('class', 'metacoreSettingsUser');
		jQuery('#metacoreSettingsButton a').attr('title', 'Using personal account');
	}
	else if (mode == 'system') {
		jQuery('#metacoreSettingsButton').attr('class', 'metacoreSettingsSystem');
		jQuery('#metacoreSettingsButton a').attr('title', 'Using company account')
	}
	else {
		jQuery('#metacoreSettingsButton').attr('class', 'metacoreSettingsDemo');
		jQuery('#metacoreSettingsButton a').attr('title', 'Using free enrichment')
	}
}

function saveMetacoreParams(metacoreSettingsWindow) {
	// TODO: implement!
	var radioSetting = jQuery('#metacoreSettingsForm input[name=accountType]:checked').val();
	var radioSettingOriginal = jQuery('#metacoreSettingsForm input[name=accountType_original]').val();
	
	if (radioSetting == radioSettingOriginal && radioSetting != 'user')
		return "cancel";
	
	var baseUrl = jQuery('#metacoreSettingsForm input[name=baseUrl]').val();
	var baseUrlOriginal = jQuery('#metacoreSettingsForm input[name=baseUrl_original]').val();
	
	var login = jQuery('#metacoreSettingsForm input[name=login]').val();
	var loginOriginal = jQuery('#metacoreSettingsForm input[name=login_original]').val();
	
	var password1 = jQuery('#metacoreSettingsForm input[name=password1]').val();
	var password2 = jQuery('#metacoreSettingsForm input[name=password2]').val();
	var passwordOriginal = jQuery('#metacoreSettingsForm input[name=password_original]').val();
	
	var saveStruct = {}
	
	if (radioSetting == 'system') {
		saveStruct = { mode: 'system' };
	}
	else if (radioSetting == 'user') {
		saveStruct = { mode: 'user' };
		
		if (baseUrl == "") return "Base URL cannot be empty!";
		
		if (baseUrl != baseUrlOriginal) saveStruct['baseUrl'] = baseUrl;
		
		if (login == "") return "Login cannot be empty!";
		
		if (login != loginOriginal) saveStruct['login'] = login;
		if (password1 != passwordOriginal) 
			if (password1 == password2) 
				saveStruct['password'] = password1; 
			else {
				return "Passwords don't match";
			}		
	}
	else if (radioSetting == 'demo') {
		saveStruct = { mode: 'demo' };
	}
	else {
		return "wrong mode";
	}
	
	if (jQuery.isEmptyObject(saveStruct))
		return "cancel";
	
	// alert(JSON.stringify(saveStruct));
	
	// OK, making an AJAX call to save our settings
	
	var spinnerMask = new Ext.LoadMask(Ext.getBody(), {msg:"Saving settings, please wait..."});
	spinnerMask.show();
	
	Ext.Ajax.request({
		url : pageInfo.basePath+'/metacoreEnrichment/saveMetacoreSettings',
		method: 'POST',
		timeout: '1800000',
		params: Ext.urlEncode(saveStruct),
		success : function(response, request) {
			spinnerMask.hide(); 
			var data = Ext.decode(response.responseText);
			res = data.result;
			
			if (res == 'success') {
				metacoreSettingsWindow.destroy();
				resetSettingsButton(radioSetting);
			}
			else 
				Ext.Msg.alert('Error saving settings', res);
		},
		failure : function(response, request) {
			spinnerMask.hide();
			res = response.statusText;
			Ext.Msg.alert('Error saving settings', res);
		}
	});
	
	return "success";
}

function showMetacoreSettingsWindow() {
	// window.open(pageInfo.basePath+'/metacoreEnrichment/serverSettingsWindow', 'MetaCore Server Settings', 'width=400, height=300');
	if (this.metacoreSettingsWindow)
		metacoreSettingsWindow.destroy();
	
	var metacoreSettingsWindow = new Ext.Window({
        id: 'metacoreSettingsWindow',
        title: 'MetaCore Settings',
    	layout:'fit',
        width:450,
        // height:250,
        autoHeight: true,
        closable: false,
        plain: true,
        modal: true,
        border:true,
        //autoScroll: true,
        buttons: [
        		{
                    id: 'metacoreSettingsOKButton',
                    text: 'Save',
                    handler: function(){       
                    	var res = saveMetacoreParams(metacoreSettingsWindow);
                    	
                    	if (res == 'cancel') 
                    		metacoreSettingsWindow.destroy();
                    	else if (res != "success")
                    		Ext.Msg.alert('Errors in form', res);
                    }
                },
        		{
                    text: 'Cancel',
                    handler: function() {
                    	metacoreSettingsWindow.destroy();
                    }
                 }],
        resizable: false,
        autoLoad:
        {
        	url: pageInfo.basePath+'/metacoreEnrichment/serverSettingsWindow',
        	scripts: true,
           	nocache:true, 
           	discardUrl:true,
           	method:'POST'
           	// callback: toggleDataAssociationFields
        }
    });
	
	metacoreSettingsWindow.show(viewport);
}
	
