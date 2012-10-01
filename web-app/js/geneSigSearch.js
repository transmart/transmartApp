var geneListsTable;
var selectedGeneLists;
function initDataTables(){
	geneListsTable = jQuery("#mySignatures").dataTable({
	  	 "sPaginationType": "full_numbers",
	    "aoColumnDefs":[
			{"bSortable":false, "aTargets":[0]},
			{"bSearchable":false, "aTargets":[0,7,8,9,10,11,12]} 
		],
	});
}

function initManipulateDiv(){
	jQuery( "#manipulateDiv" ).dialog({
		autoOpen: false,
		show: "blind",
		hide: "explode"
	});
}

function handleActionItem(actionItem) {
	var id = selectedGeneLists[0].id;
	var action = actionItem.value;
	var url 
	if(action=="") return false;
	
	if(action=="concat" || action=="union" || action=="intersect" ||action=="unique"){
/*		jQuery("#manipulateDiv").load(
	            "manipulate", 
	            {}, // omit this param object to issue a GET request instead a POST request, otherwise you may provide post parameters within the object
	            function (responseText, textStatus, XMLHttpRequest) {
	                // remove the loading class
	                dialog.removeClass('loading');
	            }
	    ).dialog('open'); */
        var url = "manipulate";
        var windowName = "popUp";//$(this).attr("name");
        var windowSize = "width=520,height=600";

        window.open(url, windowName, windowSize);
		return;
	}
	
	// clone existing object and bring into edit wizard
	if(action=="clone") {
		url = "cloneWizard/"+id+"";
	}
	
	// set delete flag
	if(action=="delete") {
		var del=confirm("Are you sure you want to delete?")

		if(del) {
			url="delete/"+id;
			window.location.href=url;
		} else {
			return false;
		}
	}

	// edit wizard
	if(action=="edit") {
		url = "editWizard/"+id+"";
	}				

	if(action=="showEditItems") {
		url = "showEditItems/"+id+"";
	}
	
	// export to Excel 
	if(action=="export") {
		url = "downloadExcel/"+id+"";
	}

	// public action
	if(action=="public") {
		url = "makePublic/"+id;
	}

	// send to url
	window.location.href=url;
}

/**
 * Clear out the action selection list and recreate it based on the number of genelists selected.
 * If only one gene list is selected
 * 		Add options based upon the deleted, public and owned flag
 * If multiple gene lists are selected
 * 		Add gene lists manipulation options 
 */
function populateActionSelection(){
	selectedGeneLists = geneListsTable.$('.geneList:checked');
	
	//grab the action dropdown list.
	var actionList=jQuery("#geneListAction");
	
	//Only one gene list selected.
	if(selectedGeneLists.length==1){
		var selectedGeneListId=selectedGeneLists[0].id;
		
		//grabbing the public, owned and deleted flags for the selected gene list.
		var isPublic=((jQuery("#"+selectedGeneListId+"Public").text())=="Yes");
		var isUserOwned=((jQuery("#"+selectedGeneListId+"Owned").text())=="Yes");
		
		//clear out options before re-adding them
		actionList.html('');
		
		//Add default options
		actionList.append(jQuery("<option>").val("").text("-- Select Action --"));
		actionList.append(jQuery("<option>").val("clone").text("Clone"));

		//Delete only if owned by user and not allready deleted
		if(isUserOwned){
			actionList.append(jQuery("<option>").val("delete").text("Delete"));
		}
		//Edit if user owned
		if(isUserOwned){
			actionList.append(jQuery("<option>").val("edit").text("Edit"));
			actionList.append(jQuery("<option>").val("showEditItems").text("Edit Items"));
		}
		//Default option
		actionList.append(jQuery("<option>").val("export").text("Excel Download"));
		//Make public if user owned and not allready public
		if(!isPublic && isUserOwned){
			actionList.append(jQuery("<option>").val("public").text("Make Public"));
		}
	}else if(selectedGeneLists.length>1){
		//clear out options before re-adding them
		actionList.html('');
		
		actionList.append(jQuery("<option>").val("").text("-- Select Action --"));
		actionList.append(jQuery("<option>").val("union").text("Union"));
		actionList.append(jQuery("<option>").val("concat").text("Concatinate"));
		actionList.append(jQuery("<option>").val("intersect").text("Intersect"));
		actionList.append(jQuery("<option>").val("unique").text("Make Unique"));
	}

}