//This function will generate a window with filtering options for the faceted search. Each window will have a browse with possibly other options.
function generateBrowseWindow(nodeClicked)
{
	var URLtoUse = "";
	var filteringFunction;
	
	var dialogHeight = 350;
	var dialogWidth = 800;
	
	//Grab the URL from a JS variable. Different popups need different URLS. We declare these on the RWG Index page.
    var windowProperties = popupWindowPropertiesMap[nodeClicked];

    if (windowProperties == null) {
        alert("Failed to find applicable popup for " + nodeClicked + "! Please contact an administrator.");
        return false;
    }

    if (windowProperties['dialogHeight'] != null) { dialogHeight = windowProperties['dialogHeight']}
    if (windowProperties['dialogWidth'] != null) { dialogWidth = windowProperties['dialogWidth']}
	
	//Load from the URL into a dialog window to capture the user input. We pass in a function that handles what happens after the user presses "Select".
	jQuery('#divBrowsePopups').dialog("destroy");
	jQuery('#divBrowsePopups').dialog(
		{
			modal: false,
			open: function()
			{
				jQuery(this).empty().addClass('ajaxloading');
				jQuery(this).load(windowProperties['URLToUse'], function() {
					jQuery(this).removeClass('ajaxloading');
				});
			},
			height: dialogHeight,
			width: dialogWidth,
			title: nodeClicked,
			show: 'fade',
			hide: 'fade',
			resizable: false,
			buttons: {"Select" : windowProperties['filteringFunction']}
		})
}

//After the user clicks select on the popup we need to add the search terms to the filter.
function applyPopupFiltersStudy()
{
	//Loop through all the selected items.
	jQuery("#multiselectbox :selected").each(function(i, selected){
	
		//Add each item to the search parameters object.
		var searchParam={id:selected.value,
		        display:'Study',
		        keyword:selected.text,
		        category:'STUDY_ID'};
		
		addSearchTerm(searchParam, true);
		
	})
	
	//This destroys our popup window.
	jQuery(this).dialog("destroy");
	updateSearch();
}

function applyPopupFiltersAnalyses()
{
	//Loop through all the selected items.
	jQuery("#multiselectbox :selected").each(function(i, selected){
	
		//Add each item to the search parameters object.
		var searchParam={id:selected.value,
		        display:'Analyses',
		        keyword:selected.text,
		        category:'ANALYSIS_ID'};
		
		addSearchTerm(searchParam, true);
		
	})
	
	//This destroys our popup window.
	//Special for analyses - remove the large list at this stage
	jQuery(this).children().detach().remove();
	jQuery(this).dialog("destroy");
	updateSearch();
}

function applyPopupFiltersDataTypes()
{
	//Loop through all the selected items.
	jQuery("#multiselectbox :selected").each(function(i, selected){
	
		//Add each item to the search parameters object.
		var searchParam={id:selected.value,
		        display:'Data Types',
		        keyword:selected.text,
		        category:'DATA_TYPE'};
		
		addSearchTerm(searchParam, true);
		
	})
	
	//This destroys our popup window.
	jQuery(this).dialog("destroy");
	updateSearch();
}
