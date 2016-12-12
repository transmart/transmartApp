/**
This file contains the javascript that provides the client side functionality to enable "My workspace"
in Dataset Explorer
*/
var subsetsTable;
var reportsTable;

var workspaceQueryDisplayDialog;
var workspaceLinkDisplayDialog;
var workspaceReportCodesDisplayDialog;

function showSaveSubsetsDialog(){
    if(isSubsetEmpty(1) && isSubsetEmpty(2))
    {
        alert('Empty subsets found, need at least 1 valid subset to save a comparsion');
        return;
    }
	var validator = $j("#saveSubsetForm").validate({
			rules: {
				txtSubsetDescription: "required"
			},
			submitHandler: function(form) {
			     saveSubsets($j('#txtSubsetDescription').val(),$j('#chkSubsetPublic').is(':checked'));
			},
			messages: {
				txtSubsetDescription: "Required"
			}
	});
	$j( "#saveSubsetsDialog" ).dialog({title: 'Save Subsets', modal:true}).dialog("open").find( "input[type=submit]" ).button();
}

function saveSubsets(subsetDescription, isSubsetPublic){
	if((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] == null) || (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] == null)){
		runAllQueries(
						function(){
							saveSubsets(subsetDescription, isSubsetPublic);
						}
					);
		return;
	}
	
	$j.ajax({
		  url: pageInfo.basePath + '/subset/save',
		  success:function(data){
			  						$j( "#saveSubsetsDialog" ).dialog("close");
			  						resultsTabPanel.setActiveTab('workspacePanel');
			  					},
		  failure:function(data){alert("Subsets failed to save.");},
		  data: {	description:subsetDescription,
			  		isSubsetPublic:isSubsetPublic,
                    result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
                    result_instance_id2 : GLOBAL.CurrentSubsetIDs[2],
                    study:GLOBAL.currentSubsetsStudy
		  		}
		});
}

function renderWorkspace(p){
	$j.ajaxSetup ({
	    // Disable caching of AJAX responses
	    cache: false
	});
	$j("#" + Ext.getCmp("workspacePanel").body.id).load(pageInfo.basePath + '/workspace/listWorkspaceItems?_=' + (new Date()).getTime(),
			{selectedSubsetId: GLOBAL.selectedWorkspaceSubsetId},
			initWorkspaceDataTables);
}

var calcWorkspaceDataTableHeight = function() {

    // Commenting this as we don't know WTH the report manager is suppose to work.
    // The only demo provided to us, lamely failed.
    // TODO Please invert as you know what is going on ... that's if you ever know.
    // Disgusting ... but no choice I suppose.
    return ($j('#workspacePanel').height() - $j('#subsets_wrapper .dataTables_scrollHead').height() - $j('#subsets_wrapper > div:first-child').height() - $j('#subsets_wrapper > div:last-child').height() - $j('.workspaceheader').height());
//  return ($j('#workspacePanel').height() - $j('#subsets_wrapper .dataTables_scrollHead').height() - $j('#subsets_wrapper > div:first-child').height() - $j('#subsets_wrapper > div:last-child').height() - $j('.workspaceheader').height()) / 2;

};

function initWorkspaceDataTables(){
	subsetsTable = $j("#subsets").dataTable({
	  	 "sPaginationType": "full_numbers",
	  	 "aoColumnDefs":[
			{"bSortable":false, "aTargets":[2,3,4,5,7,8]},
			{"bSearchable":false, "aTargets":[2,3,4,5,7,8]}
		],
		"fnDrawCallback": function( oSettings ) {
				$j(".editSubsetDescriptionBox").hide();
		    },
		"bDestroy": true,
        "sScrollY": 0,
		"sScrollX": "100%",
		"bProcessing": true,
        "bJQueryUI": true,
        "bScrollAutoCss": true
	});

    // Commenting this as we don't know WTH the report manager is suppose to work.
    // The only demo provided to us, lamely failed.
    // TODO Please invert as you know what is going on ... that's if you ever know.
    $j("#reports").hide();
    /*
    reportsTable = $j("#reports").dataTable({
	  	"sPaginationType": "full_numbers",
	  	"aoColumnDefs":[
			{"bSortable":false, "aTargets":[2,3,5,6]},
			{"bSearchable":false, "aTargets":[2,3,5,6]}
		],
		"fnDrawCallback": function( oSettings ) {
			$j(".editReportDescriptionBox").hide();
		    },
		"bDestroy": true,
        "sScrollY": 0,
		"sScrollX": "100%",
		"bProcessing": true,
        "bJQueryUI": true,
        "bScrollAutoCss": true
	});
	*/

    onWindowResize();

	workspaceQueryDisplayDialog = $j("#workspaceQueryDisplayDialog").dialog({
		autoOpen:false,
		open: function(event, ui){
			var displayData = $j(this).data("displayData");
			$j(this).html(displayData);
		},
		width: 800
	}).css("font-size", "10px");
	
	workspaceLinkDisplayDialog = $j("#workspaceLinkDisplayDialog").dialog({
		autoOpen:false,
		open: function(event, ui){
			var displayData = $j(workspaceLinkDisplayDialog).data("displayData");
			$j(workspaceLinkDisplayDialog).html('<input type="text" title="Subset Link" id="subsetsLink" style="width:100%;" readonly="readonly"/>');
			$j("#subsetsLink").val(displayData);
		},
		close: function(event, ui){
			$j(workspaceLinkDisplayDialog).html("");
		},
		width: 600,
		modal: true
	}).css("font-size", "10px");
		
	workspaceReportCodesDisplayDialog = $j("#workspaceReportCodesDisplayDialog").dialog({
		autoOpen:false,
		open: function(event, ui){
			var displayData = $j(this).data("displayData");
			$j(this).html(displayData);
		},
		width: 600
	}).css("font-size", "10px");

    $j( "#subsets_length" ).before( "<div id=\"subset_manager_name\"><label>Subset Manager</label></div>" );
    $j( "#reports_length" ).before( "<div id=\"report_manager_name\"><label>Report Manager</label></div>" );
}

function handleKeyPress(event, entityId, entity){
	if(event.keyCode==13){
		if(entity=='subset'){
			updateDescription(entityId);
		}else if(entity=='report'){
			updateReportName(entityId);
		}
	}
}

function handleMouseEnter(elementId){
	var jQueryElement = $j("#"+elementId);
	jQueryElement.addClass('ui-state-hover');
}

function handleMouseOut(elementId){
	var jQueryElement = $j("#"+elementId);
	jQueryElement.removeClass('ui-state-hover');
}

function displayQuery(event, subsetId){
	var posX=event.clientX;
	var posY=event.clientY;
	displayQueryFunction = setTimeout(function(){
		$j.get(pageInfo.basePath + '/subset/query', {subsetId:subsetId}, function(data){
			$j(workspaceQueryDisplayDialog).dialog("option", {position:[posX+20, posY+60]});
			$j(workspaceQueryDisplayDialog).data("displayData",data).dialog("open");
		});
	}, 500);
}

function hideQuery(){
	clearTimeout(displayQueryFunction);
	$j(workspaceQueryDisplayDialog).dialog("close");
}

function applySubsets(subsetId){
	var overrideCurrentSubsets = true;
	if(!isSubsetEmpty(1) || !isSubsetEmpty(2)){
		overrideCurrentSubsets = confirm("This will override the criteria you have selected on the comparison tab.");
	}
	if(overrideCurrentSubsets){
		$j.get(pageInfo.basePath + '/subset/getQueryForSubset', {subsetId:subsetId}, function(data){

			var _obj = {}
			if (data.query1) {
				_obj[1] = data.query1
			}
			if (data.query2) {
				_obj[2] = data.query2
			}

			resultsTabPanel.setActiveTab("queryPanel");
			refillQueryPanels(_obj)

			// Refresh the global subset study variable.
			// TODO review this, it depends on a variable not anymore retrived
			// TODO see getChildren() in transmart-core

			GLOBAL.selectedWorkspaceSubsetId=subsetId;
		});
	}
}

function clearWorkspaceSelections(){
	//Refresh the global workspace subset variable.
	GLOBAL.selectedWorkspaceSubsetId='';
	
}

function linkifySubsets(link){
	$j(workspaceLinkDisplayDialog).data("displayData",link).dialog("open");
}

function mailComparison(link, comparisonId) {
	var subject = 'Link to Saved comparison' + (comparisonId ? ' ID=' + comparisonId : '');
	var body = 'The following is a link to the saved comparison in tranSMART.';

	var a = document.createElement('a');
	a.href = 'mailto:?subject=' + encodeURIComponent(subject) +
		'&body=' + encodeURIComponent(body) +
		' ' + encodeURIComponent(link);
	a.setAttribute('type', 'hidden');
	document.body.appendChild(a);
	a.click();
	$j(a).remove();
}

function deleteSubset(subsetId){
	if(confirm("Subset will be deleted. Proceed?")){
		$j.get(pageInfo.basePath + '/subset/delete', {subsetId:subsetId}, function(data){
			var rowEle = document.getElementById("subsetRow"+subsetId);
			var rowIndex = subsetsTable.fnGetPosition(rowEle); 
			subsetsTable.fnDeleteRow(rowIndex);
		})
		.fail(function(){alert("Server Error while deleting subset");});
	}
}

function togglePublicFlag(subsetId){
	$j.get(pageInfo.basePath + '/subset/togglePublicFlag', {subsetId:subsetId}, function(data){
		if(data=='true'){
			$j("#publicFlag"+subsetId).removeClass("ui-icon-locked");
			$j("#publicFlag"+subsetId).addClass("ui-icon-unlocked");
		}else if(data=='false'){
			$j("#publicFlag"+subsetId).removeClass("ui-icon-unlocked");
			$j("#publicFlag"+subsetId).addClass("ui-icon-locked");
		}
	}).fail(function() { alert("Server Error in toggling public flag"); });
}

function editDescription(subsetId){
	$j("#subsetDescriptionDisplay"+subsetId).hide();
	$j("#editDescriptionBox"+subsetId).show();
	$j("#editDescriptionBox"+subsetId).focus();
}

function updateDescription(subsetId){
	var description = $j("#editDescriptionBox"+subsetId).val();
	$j.get(pageInfo.basePath + '/subset/updateDescription', {subsetId:subsetId, description:description}, function(data){
		renderWorkspace();
	});
}
