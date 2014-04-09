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
	var validator = jQuery("#saveSubsetForm").validate({
			rules: {
				txtSubsetDescription: "required"
			},
			submitHandler: function(form) {
			     saveSubsets(jQuery('#txtSubsetDescription').val(),jQuery('#chkSubsetPublic').is(':checked'));
			},
			messages: {
				txtSubsetDescription: "Required"
			}
	});
	jQuery( "#saveSubsetsDialog" ).dialog({title: 'Save Subsets', modal:true});
	jQuery( "#saveSubsetsDialog" ).dialog("open");
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
	
	jQuery.ajax({
		  url: pageInfo.basePath + '/subset/save',
		  success:function(data){
			  						jQuery( "#saveSubsetsDialog" ).dialog("close");
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
	jQuery.ajaxSetup ({
	    // Disable caching of AJAX responses
	    cache: false
	});
	jQuery("#" + Ext.getCmp("workspacePanel").body.id).load(pageInfo.basePath + '/workspace/listWorkspaceItems?_=' + (new Date()).getTime(),
			{selectedSubsetId: GLOBAL.selectedWorkspaceSubsetId},
			initWorkspaceDataTables);
}

function initWorkspaceDataTables(){
	subsetsTable = jQuery("#subsets").dataTable({
	  	 "sPaginationType": "full_numbers",
	  	 "aoColumnDefs":[
			{"bSortable":false, "aTargets":[2,3,4,5,7,8]},
			{"bSearchable":false, "aTargets":[2,3,4,5,7,8]}
		],
		"fnDrawCallback": function( oSettings ) {
				jQuery(".editSubsetDescriptionBox").hide();
		    },
		"bDestroy": true,
		"sScrollY": "320px",
		"sScrollX": "100%",
		"bProcessing": true
	});

	reportsTable = jQuery("#reports").dataTable({
	  	"sPaginationType": "full_numbers",
	  	"aoColumnDefs":[
			{"bSortable":false, "aTargets":[2,3,5,6]},
			{"bSearchable":false, "aTargets":[2,3,5,6]}
		],
		"fnDrawCallback": function( oSettings ) {
			jQuery(".editReportDescriptionBox").hide();
		    },
		"bDestroy": true,
		"sScrollY": "320px",
		"sScrollX": "100%",
		"bProcessing": true
	});
	
	workspaceQueryDisplayDialog = jQuery("#workspaceQueryDisplayDialog").dialog({
		autoOpen:false,
		open: function(event, ui){
			var displayData = jQuery(this).data("displayData");
			jQuery(this).html(displayData);
		},
		width: 800
	}).css("font-size", "10px");
	
	workspaceLinkDisplayDialog = jQuery("#workspaceLinkDisplayDialog").dialog({
		autoOpen:false,
		open: function(event, ui){
			var displayData = jQuery(workspaceLinkDisplayDialog).data("displayData");
			jQuery(workspaceLinkDisplayDialog).html('<input type="text" title="Subset Link" id="subsetsLink" style="width:100%;" readonly="readonly"/>');
			jQuery("#subsetsLink").val(displayData);
		},
		close: function(event, ui){
			jQuery(workspaceLinkDisplayDialog).html("");
		},
		width: 600,
		modal: true
	}).css("font-size", "10px");
		
	workspaceReportCodesDisplayDialog = jQuery("#workspaceReportCodesDisplayDialog").dialog({
		autoOpen:false,
		open: function(event, ui){
			var displayData = jQuery(this).data("displayData");
			jQuery(this).html(displayData);
		},
		width: 600
	}).css("font-size", "10px");
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
	var jQueryElement = jQuery("#"+elementId);
	jQueryElement.addClass('ui-state-hover');
}

function handleMouseOut(elementId){
	var jQueryElement = jQuery("#"+elementId);
	jQueryElement.removeClass('ui-state-hover');
}

function displayQuery(event, subsetId){
	var posX=event.clientX;
	var posY=event.clientY;
	displayQueryFunction = setTimeout(function(){
		jQuery.get(pageInfo.basePath + '/subset/query', {subsetId:subsetId}, function(data){
			jQuery(workspaceQueryDisplayDialog).dialog("option", {position:[posX+20, posY+60]});
			jQuery(workspaceQueryDisplayDialog).data("displayData",data).dialog("open");
		});
	}, 500);
}

function hideQuery(){
	clearTimeout(displayQueryFunction);
	jQuery(workspaceQueryDisplayDialog).dialog("close");
}

function applySubsets(subsetId, study){
	var overrideCurrentSubsets = true;
	if(!isSubsetEmpty(1)){
		overrideCurrentSubsets = confirm("This will override the criteria you have selected on the comparison tab.")
	}
	if(overrideCurrentSubsets){
		jQuery.get(pageInfo.basePath + '/subset/getQueryIdsForSubset', {subsetId:subsetId}, function(data){
			resetQuery();
			getPreviousQueryFromID(1, data.queryId1);
			if(data.queryId2!=-1){
				getPreviousQueryFromID(2, data.queryId2);
			}
			//Refresh the global subset study variable.
			GLOBAL.currentSubsetsStudy=study;
            if (study == ""){ ontTabPanel.setActiveTab("acrossTrialTreePanel"); }
            else
            {
                ontTabPanel.setActiveTab("navigateTermsPanel");
            }
			//Refresh the global workspace subset variable.
			GLOBAL.selectedWorkspaceSubsetId=subsetId;
		});
	}
}

function clearWorkspaceSelections(){
	//Refresh the global workspace subset variable.
	GLOBAL.selectedWorkspaceSubsetId='';
	
}

function linkifySubsets(link){
	jQuery(workspaceLinkDisplayDialog).data("displayData",link).dialog("open");
}

function deleteSubset(subsetId){
	if(confirm("Subset will be deleted. Proceed?")){
		jQuery.get(pageInfo.basePath + '/subset/delete', {subsetId:subsetId}, function(data){
			var rowEle = document.getElementById("subsetRow"+subsetId);
			var rowIndex = subsetsTable.fnGetPosition(rowEle); 
			subsetsTable.fnDeleteRow(rowIndex);
		})
		.fail(function(){alert("Server Error while deleting subset");});
	}
}

function togglePublicFlag(subsetId){
	jQuery.get(pageInfo.basePath + '/subset/togglePublicFlag', {subsetId:subsetId}, function(data){
		if(data=='true'){
			jQuery("#publicFlag"+subsetId).removeClass("ui-icon-locked");
			jQuery("#publicFlag"+subsetId).addClass("ui-icon-unlocked");
		}else if(data=='false'){
			jQuery("#publicFlag"+subsetId).removeClass("ui-icon-unlocked");
			jQuery("#publicFlag"+subsetId).addClass("ui-icon-locked");
		}
	}).fail(function() { alert("Server Error in toggling public flag"); })
}

function editDescription(subsetId){
	jQuery("#subsetDescriptionDisplay"+subsetId).hide();
	jQuery("#editDescriptionBox"+subsetId).show();
	jQuery("#editDescriptionBox"+subsetId).focus();
}

function updateDescription(subsetId){
	var description = jQuery("#editDescriptionBox"+subsetId).val();
	jQuery.get(pageInfo.basePath + '/subset/updateDescription', {subsetId:subsetId, description:description}, function(data){
		renderWorkspace();
	})
}