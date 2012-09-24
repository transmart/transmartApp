//This function will generate a window with filtering options for the faceted search. Each window will have a browse with possibly other options.
function generateBrowseWindow(nodeClicked)
{
	var URLtoUse = "";
	var filteringFunction;
	
	//Grab the URL from a JS variable. Different popups need different URLS. We declare these on the RWG Index page.
	switch(nodeClicked)
	{
		case "Study":
			URLtoUse = studyBrowseWindow
			filteringFunction = applyPopupFiltersStudy
		  break;
		case "Analyses":
			URLtoUse = analysisBrowseWindow
			filteringFunction = applyPopupFiltersAnalyses
		  break;
		case "Region of Interest":
			URLtoUse = regionBrowseWindow
			filteringFunction = function(){}
			break;
		case "Data Type":
			URLtoUse = analysisBrowseWindow
			filteringFunction = function(){}
			break;
		default:
			alert("Failed to find applicable popup! Please contact an administrator.");
			return false;
	}
	
	//Load from the URL into a dialog window to capture the user input. We pass in a function that handles what happens after the user presses "Select".
	jQuery('#divBrowsePopups').dialog(
			{
				modal: false,
				open: function()
				{
					jQuery(this).load(URLtoUse)
				},
				height: 300,
				width: 500,
				title: nodeClicked,
				show: 'fade',
				hide: 'fade',
				buttons: {"Select" : filteringFunction}
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
		        category:'STUDY'};
		
		addSearchTerm(searchParam);
		
	})
	
	//This destroys our popup window.
	jQuery(this).dialog("destroy")
}

//After the user clicks select on the popup we need to add the search terms to the filter.
function applyPopupFiltersAnalyses()
{
	//Loop through all the selected items.
	jQuery("#multiselectbox :selected").each(function(i, selected){
	
		//Add each item to the search parameters object.
		var searchParam={id:selected.value,
		        display:'Analyses',
		        keyword:selected.text,
		        category:'ANALYSES'};
		
		addSearchTerm(searchParam);
		
	})
	
	//This destroys our popup window.
	jQuery(this).dialog("destroy")
}