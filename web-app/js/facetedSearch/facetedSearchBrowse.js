//This function will generate a window with filtering options for the faceted search. Each window will have a browse with possibly other options.
function generateBrowseWindow(nodeClicked)
{
	var URLtoUse = "";
	var filteringFunction;
	
	var dialogHeight = 350;
	var dialogWidth = 800;
	
	//Grab the URL from a JS variable. Different popups need different URLS. We declare these on the RWG Index page.
	switch(nodeClicked)
	{
		case "Study":
			URLtoUse = studyBrowseWindow;
			filteringFunction = applyPopupFiltersStudy;
		  break;
		case "Analyses":
			URLtoUse = analysisBrowseWindow;
			filteringFunction = applyPopupFiltersAnalyses;
		  break;
		case "Region of Interest":
			URLtoUse = regionBrowseWindow;
			filteringFunction = applyPopupFiltersRegions;
			dialogHeight = 340;
			dialogWidth = 430;
			break;
		case "Data Type":
			URLtoUse = dataTypeBrowseWindow;
			filteringFunction = applyPopupFiltersDataTypes;
			break;
		default:
			alert("Failed to find applicable popup! Please contact an administrator.");
			return false;
	}
	
	//Load from the URL into a dialog window to capture the user input. We pass in a function that handles what happens after the user presses "Select".
	jQuery('#divBrowsePopups').dialog("destroy");
	jQuery('#divBrowsePopups').dialog(
			{
				modal: false,
				open: function()
				{
					jQuery(this).empty();
					jQuery(this).load(URLtoUse);
				},
				height: dialogHeight,
				width: dialogWidth,
				title: nodeClicked,
				show: 'fade',
				hide: 'fade',
				resizable: false,
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
		        category:'STUDY_ID'};
		
		addSearchTerm(searchParam);
		
	})
	
	//This destroys our popup window.
	jQuery(this).dialog("destroy")
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
		
		addSearchTerm(searchParam);
		
	})
	
	//This destroys our popup window.
	jQuery(this).dialog("destroy")
}

function applyPopupFiltersRegions()
{
	//Pick out the useful fields and generate search terms
	
	var range = null;
	var basePairs = null;
	var version = null;
	var searchString = "";
	
	if (jQuery('[name=\'regionFilter\'][value=\'gene\']:checked').size() > 0) {
		var geneId = jQuery('#filterGeneId').val();
		range = jQuery('#filterGeneRange').val();
		basePairs = jQuery('#filterGeneBasePairs').val();
		use = jQuery('#filterGeneUse').val();
		searchString += "GENE;" + geneId
	}
	else if (jQuery('[name=\'regionFilter\'][value=\'chromosome\']:checked').size() > 0) {
		range = jQuery('#filterChromosomeRange').val();
		basePairs = jQuery('#filterChromosomeBasePairs').val();
		use = jQuery('#filterChromosomeUse').val();
		searchString += "CHROMOSOME;" + jQuery('#filterChromosomeNumber').val() + ";" + jQuery('#filterChromosomeUse').val() + ";" + jQuery('#filterChromosomePosition').val();
	}
	if (basePairs == null || basePairs == "") {
		basePairs = 0;
	}
	searchString += ";" + range + ";" + basePairs + ";" + use;

	var searchParam={id:searchString,
	        display:'Region',
	        keyword:searchString,
	        category:'REGION'};
	
	addSearchTerm(searchParam);
	
	//This destroys our popup window.
	jQuery(this).dialog("destroy")
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
		
		addSearchTerm(searchParam);
		
	})
	
	//This destroys our popup window.
	jQuery(this).dialog("destroy")
}
