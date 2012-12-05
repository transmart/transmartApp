////////////////////////////////////////////////////////////////////
// Globals
var activeCategories = new Array();   // array of category objects that are in active filter; order is order that arrays appear on screen
var activeKeywords = new Array();     // array of keyword objects that are in active filter;  order is order that they appear on screen within their category
var uniqueIdSequence = 0;             // sequence to uniquely identify keywords in a keyword object

// Store the nodes that were selected before a new node was selected, so that we can compare to the nodes that are selected after.  Selecting
//  one node in the tree can cause lots of changes in other parts of the tree (copies of this node change, children/parents change, 
//  parents of parents, children of parents of parent, etc.)
var nodesBeforeSelect = new Array();

// By default, allow the onSelect event to trigger for the tree nodes;  However, we don't want select events that are triggered from inside the onSelect
// event to cause the onSelectEvent code to keep triggering itself.  So change this to false before any call to select() within the onSelect (the event
// will still fire but is stopped immediately); and set this flag back to true at the end of the event so it can be triggered again.  
var allowOnSelectEvent = true;

// store probe Ids for each analysis that has been loaded
var analysisProbeIds = new Array();

//store probe Ids for each analysis that has been loaded on XT for selected analyses
var analysisProbeIdsSA = new Array();

var openAnalyses = new Array(); //store the IDs of the analyses that are open

var openTrials = new Array(); //store the trials that are currently expanded

//cross trial analysis selected analyses
var selectedAnalyses = [];

//cross trial analysis selected keywords
var xtSelectedKeywords = [];

var uniqueGeneChartId = 0;   // sequence to give unique identifiers to the tooltips used in the gene chart
var gcTooltips = new Array;  // gene chart tooltips


//create an ajaxmanager named rwgAJAXManager
//this will handle all ajax calls on this page and prevent too many 
//requests from hitting the server at once
var rwgAJAXManager = jQuery.manageAjax.create('rwgAJAXManager', {
	queue: true, 			//(true|false|'clear') the queue-type specifies the queue-behaviour.
	maxRequests: 5, 		//(number (1)) limits the number of simultaneous request in the queue. queue-option must be true or 'clear'.
	cacheResponse: false 	//(true|false): caches the response data of successful response
});

var cohortBGColors = new Array(
		/* Pastel */
		"#FFFFD9", //light yellow
		"#80B1D3", //light blue
		"#B3DE69", //moss green
		"#D9D9D9", //grey
		"#BC80BD", //lavender
		"#91d4c5"  //teal		
);


function removeSelectedAnalysis(analysisID){
	
	jQuery("#li_SelectedAnalysis_"+analysisID).fadeOut('fast');
	

	//remove from selecatedAnalyses array
	removeXTAnalysisFromArray(analysisID);
	
	var currentCount = selectedAnalyses.length;
	
	var newLabel = "(" +currentCount + ")";
	jQuery("#analysisCountLabel").html(newLabel);

	jQuery("input[name=chbx_Analysis_"+analysisID+"]").attr('checked', false);
	
	displayxtAnalysesList();
	
}

//When the user adds or removes an analyses from the Selected Analyses list,
//we need to check if there are summary stats or heatmaps already display
//If so, give option to refresh or clear the view
function refreshCrossTrialMsg(){
	
	//if there are already selected keywords, then these need
	//to be refreshed or removed
//	if(xtSelectedKeywords.length>0){
		
		//Display msg to user with option to refresh or clear
		jQuery("#xtMsgBox").fadeIn();
		
		//mask the tabs
		jQuery('#xtMenuBar').mask();
		
//	}
	
}


//remove the the XT Analysis from array selectedAnalyses
function removeXTAnalysisFromArray(analysisID){
	
	for (var i =0; i < selectedAnalyses.length; i++)
		   if (selectedAnalyses[i].id === analysisID) {
			   selectedAnalyses.splice(i,1);
		      break;
		   }

	//update the cookie
	jQuery.cookie('selectedAnalyses', JSON.stringify(selectedAnalyses));
	
	refreshCrossTrialMsg()
	
	return;
	
}

function addXTAnalysisToArray(analysisID, analysisTitle, studyID){
	
	//add item to selectedAnalyses array
	selectedAnalyses.push({'id':analysisID, 'title':analysisTitle, 'studyID':studyID});
	
	//update the cookie
	jQuery.cookie('selectedAnalyses', JSON.stringify(selectedAnalyses));
	
	refreshCrossTrialMsg()
	
	return;
}


function updateAnalysisCount(checkedState, analysisID, analysisTitle, studyID)	{	

	var currentCount = selectedAnalyses.length;
	
	if (checkedState)	{
		currentCount++;
		
		//Add analysis to array
		addXTAnalysisToArray(analysisID, analysisTitle, studyID);
		
	} else	{
		currentCount--;
		
		//remove from selecatedAnalyses array
		removeXTAnalysisFromArray(analysisID);
	}
	
	var newLabel = "(" +currentCount + ")";

	jQuery("#analysisCountLabel").html(newLabel);
	
	displayxtAnalysesList();
	
	setSaveXTFilterLink();
	setClearXTLink();
	
	return;
}

//this function used in the toolbar to display the selected list
function getSelectedAnalysesList(){
	
	jQuery("#selectedAnalysesExpanded").toggle();
	
	var html = "";
	
	if(selectedAnalyses.length==0){
		
		html = "<div style='text-align:center; padding:4px;'><p>No analyses are selected</p></div>"
		
	}else{
	
		html += "<a href='#' onclick='clearAllSelectedAnalyses()'>Clear All</a><br />";
		html +="<ul id='selectedAnalysesList'>";
		
		selectedAnalyses.sort(dynamicSort("studyID"));
		
		jQuery(selectedAnalyses).each(function(index, value){
	
			html = html + "<li id='li_SelectedAnalysis_"+selectedAnalyses[index].id +"'>"
			html = html + "<input type='checkbox' onchange=removeSelectedAnalysis('"+selectedAnalyses[index].id +"') name='chbx_SelectedAnalysis_" + selectedAnalyses[index].id +"' checked='	checked'>";
			html = html + "<span class='result-trial-name'>"+ selectedAnalyses[index].studyID +'</span>: ' +selectedAnalyses[index].title.replace(/_/g, ', ') +'</li>';
			
		});
		
		html = html + '</ul>';
	}
	
	jQuery('#selectedAnalysesExpanded').html(html);
	
}



function clearAllSelectedAnalyses(){
	
	for (var i =0; i < selectedAnalyses.length; i++){
		jQuery("input[name=chbx_Analysis_"+selectedAnalyses[i].id+"]").attr('checked', false);		
	}
	
	jQuery('#selectedAnalysesExpanded').html("");
	jQuery("#analysisCountLabel").html("(0)");
	
	selectedAnalyses =[];
	
	//update cookie
	jQuery.cookie('selectedAnalyses', JSON.stringify(selectedAnalyses));
	
	//Hide menu
	jQuery("#selectedAnalysesExpanded").hide();
	
	//update the display list
	displayxtAnalysesList();
	
	//also clear the search terms
	clearAllXTSearchTerms();	
	
	
	//hide the elements on the main page
	  jQuery('#xtAnalysisList').hide();
	  jQuery('#xtMenuBar').hide();
	  jQuery('#xtSearch-ac').prop('disabled', true);
	  
	  jQuery('#xtNoAnalysesMsg').show();

	return;
	
}

//set the checkbox of the analysis to checked if it exists in the selectedAnalysis array
function setAnalysisCheckboxState(analysisID){
	
	//check the selectedAnalyses array for the current analysisID
	var selected = selectedAnalyses.filter(function (analysis){
			return analysis.id == analysisID;
		});
	
	//if the analysis was found, check the checkbox
	if(selected.length>0){
		jQuery("input[name=chbx_Analysis_"+analysisID+"]").attr('checked', true);
	}

	
}

//Used for sorting arrays of objects 
function dynamicSort(property) {
    return function (a,b) {
        return (a[property] < b[property]) ? -1 : (a[property] > b[property]) ? 1 : 0;
    }
}

function showDetailDialog(dataURL, dialogTitle, dialogHeight)	{
	var height = 'auto';
	if (typeof dialogHeight == 'number')	{
		height = dialogHeight;
	}	
	//dialogTitle += ' --Esc key to close--'; -- removed this to provide more space in title bar
	var dialogDetail = document.getElementById(dialogTitle);
	if (dialogDetail == null)	{
		jQuery('<div id="' + dialogTitle + '"></div>')
			.load(dataURL)
			.dialog({
				autoOpen: false,
				title: dialogTitle,
				height: height,
				width: 550,
				position: ['center', 'center']
			})
			.dialog('open');
	} else	{
		jQuery(dialogDetail).dialog('isOpen') ? jQuery(dialogDetail).dialog('close') : jQuery(dialogDetail).dialog('open');		
	}
	return false;
}

// Open and close the analysis for a given trial
function toggleDetailDiv(trialNumber, dataURL, trialID)	{	
	var imgExpand = "#imgExpand_"  + trialNumber;
	var trialDetail = "#" + trialNumber + "_detail";
	
	// If data attribute is undefined then this is the first time opening the div, load the analysis... 
	if (typeof jQuery(trialDetail).attr('data') == 'undefined')	{
		
		openTrials.push(trialNumber); //add the trial to the openTrials array
		
		//display loading message
		jQuery('#TrialDet_'+ trialID +'_anchor').mask("Loading...");
		
		var src = jQuery(imgExpand).attr('src').replace('down_arrow_small2.png', 'up_arrow_small2.png');	
		jQuery(imgExpand).attr('src',src);
		jQuery.ajax({	
			url:dataURL,			
			success: function(response) {
				jQuery(trialDetail).addClass("gtb1");
				jQuery(trialDetail).html(response);			    
				jQuery(trialDetail).attr('data', true);							// Add an attribute that we will use as a flag so we don't need to load the data multiple times
				jQuery('#TrialDet_'+ trialID +'_anchor').unmask();
			},
			error: function(xhr) {
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
			}
		});
	} else	{
		var src = jQuery(imgExpand).attr('src').replace('up_arrow_small2.png', 'down_arrow_small2.png');
		if (jQuery(trialDetail).attr('data') == "true")	{
			jQuery(trialDetail).attr('data',false);
			removeByValue(openTrials,trialNumber);//remove the trial to the openTrials array
		} else	{
			src = jQuery(imgExpand).attr('src').replace('down_arrow_small2.png', 'up_arrow_small2.png');
			jQuery(trialDetail).attr('data',true);
			openTrials.push(trialNumber); //add the trial to the openTrials array
		}	
		jQuery(imgExpand).attr('src',src);
		jQuery(trialDetail).toggle();		
	}
	return false;
}

// Method to add the toggle button to show/hide the search filters
function addToggleButton()	{
	jQuery("#toggle-btn").button({
		text: false
		}).click(function() {
			toggleFilters();
			jQuery("#main").css('left') == "0px" ? switchImage('toggle-icon-left', 'toggle-icon-right') : switchImage('toggle-icon-right', 'toggle-icon-left');
		}
	).addClass('toggle-icon-left');
	return false;
}

// Add and remove the right/left image for the toggle button
function switchImage(imgToRemove, imgToAdd)	{
	jQuery("#toggle-btn").removeClass(imgToRemove);
	jQuery("#toggle-btn").addClass(imgToAdd);
}

// Method to show/hide the search/filters 
function toggleFilters()	{
	if (jQuery("#main").css('left') == "0px"){	
		
		jQuery("#search-categories").attr('style', 'visibility:visible; display:inline');
		jQuery("#search-ac").attr('style', 'visibility:visible; display:inline');
		jQuery("#search-div").attr('style', 'visibility:visible; display:inline');
		jQuery("#active-search-div").attr('style', 'visibility:visible; display:inline');
		jQuery("#title-search-div").attr('style', 'visibility:visible; display:inline');
		jQuery("#title-filter").attr('style', 'visibility:visible; display:inline');
		jQuery("#side-scroll").attr('style', 'visibility:visible; display:inline');

		jQuery("#toggle-btn").css('left', 278);
		jQuery("#toggle-btn").css('height;', 20);
		jQuery("#toggle-btn").css('height', 20);
		jQuery("#main").css('padding-left', 0);	
		jQuery("#main").css('left', 300);
		jQuery("#menu_bar").css('left', 301);
	} else	{
		jQuery("#search-categories").attr('style', 'visibility:hidden; display:none');
		jQuery("#search-ac").attr('style', 'visibility:hidden; display:none');
		jQuery("#search-div").attr('style', 'visibility:hidden; display:none');
		jQuery("#active-search-div").attr('style', 'visibility:hidden; display:none');
		jQuery("#title-search-div").attr('style', 'visibility:hidden; display:none');
		jQuery("#title-filter").attr('style', 'visibility:hidden; display:none');
		jQuery("#side-scroll").attr('style', 'visibility:hidden; display:none');
		
		jQuery("#toggle-btn").css('height', '100%');	
		jQuery("#main").css('padding-left', 20);	
		jQuery("#main").css('left', 0);	
		jQuery("#toggle-btn").css('left', 0);	
		jQuery("#menu_bar").css('left', 0);	

	}	   
}

// Method to add the categories for the select box
function addSelectCategories()	{
	jQuery("#search-categories").append(jQuery("<option></option>").attr("value", "ALL").text("All"));
	jQuery.getJSON(getCategoriesURL, function(json) {
		for (var i=0; i<json.length; i++)	{
			var category = json[i].category;
			var catText = convertCategory(category);
			jQuery("#search-categories").append(jQuery("<option></option>").attr("value", category).text(catText));
		}
    });
}

// Helper method to only capitalize the first letter of each word
function convertCategory(valueToConvert)	{
	var convertedValue = valueToConvert.toLowerCase();
	return convertedValue.slice(0,1).toUpperCase() + convertedValue.slice(1);
}

// Method to add the autocomplete for the search keywords
function addSearchAutoComplete()	{
	jQuery("#search-ac").autocomplete({
		source: sourceURL,
		minLength:0,
		select: function(event, ui) {  
			searchParam={id:ui.item.id,categoryDisplay:ui.item.category,keyword:ui.item.label,categoryId:ui.item.categoryId, categorySOLR:ui.item.categoryId.toString().replace(/ /g,'_')};
			addSearchTerm(searchParam);
			return false;
		}
	}).data("autocomplete")._renderItem = function( ul, item ) {
		return jQuery('<li></li>')		
		  .data("item.autocomplete", item )
		  .append('<a><span class="category-' + item.category.toLowerCase() + '">' + item.category + '&gt;</span>&nbsp;<b>' + item.label + '</b>&nbsp;' + item.synonyms + '</a>')
		  .appendTo(ul);
	};	
		
	// Add an onchange event to the select so we can set the category in the URL for the autocomplete
	var categorySelect = document.getElementById("search-categories"); 
	categorySelect.onchange=function()	{
		jQuery('#search-ac').autocomplete('option', 'source', sourceURL + "?category=" + this.options[this.selectedIndex].value);
	};
		
	// Capture the enter key on the slider and fire off the search event on the autocomplete
	jQuery("#search-categories").keypress(function(event)	{
		if (event.which == 13)	{
			jQuery("#search-ac").autocomplete('search');
		}
	});	
	return false;
}



function showIEWarningMsg(){
	
	if (jQuery.browser.msie && jQuery.browser.version.substr(0,1)<9) {

		var msg = "<div id='IEwarningBox'>Your browser is not supported. Please use the latest version of Chrome. <br /><br />";
			msg = msg + "<a href='#' id='IEwarningOverlayLink'>More info</a> | <a href='#' onclick=\"javascript:jQuery('#IEwarningBox').slideUp('fast');\">Close</a> </div>";
	
		var overlayMsg = "<div style='padding:20px'><p>TranSMART Faceted Search uses certain web technologies that are not fully supported by older browsers. ";
			overlayMsg = overlayMsg + "Google Chrome is the preferred browser within J&J to use with tranSMART. </p><br /><p>To request Chrome, send an SRM request " ;
			overlayMsg = overlayMsg + "for software package #C01026EE. If you have questions, please email us at <a href='mailto:tranSMART@its.jnj.com'>tranSMART@jnj.com</a>.</p>";
			overlayMsg = overlayMsg + "<br /><p><a href='#' onclick=\"jQuery('#IEwarningOverlayLink').colorbox.close()\">Close</a></p></div>";
				
		jQuery('#results-div').before(msg);
		
		jQuery("#IEwarningOverlayLink").colorbox({html:overlayMsg, width:"50%", height:"300px", opacity:"0.75"});
		
		
	}
	
}


	
// Method to load the search results in the search results panel and facet counts into tree
// This occurs whenever a user add/removes a search term
function showSearchResults(initialLoad)	{

	// clear stored probe Ids for each analysis
	analysisProbeIds = new Array();  
	
	// clear stored analysis results
	jQuery('body').removeData();	
	
	// call method which retrieves facet counts and search results
	showFacetResults(initialLoad);
	
	//all trials/analyses will be closed when doing a new search, so clear this array
	openAnalyses = [];
	openTrials = [];
	
}

// update a node's count (not including children)
function updateNodeIndividualFacetCount(node, count) {
	// only add facet counts if not a category 
	if (!node.data.isCategory)   {
		// if count is passed in as -1, reset the facet count to the initial facet count
		if (count > -1)  {
	        node.data.facetCount = count;
	    }
	    else  {
	    	node.data.facetCount = node.data.initialFacetCount
	    }
	    node.data.title = node.data.termName + " (" + node.data.facetCount + ")";	
	}
	else  {
	    node.data.facetCount = -1;
	    node.data.title = node.data.termName;	
	}
	
}


//Method to clear the facet results in the search tree
function clearFacetResults()	{
	
	var tree = jQuery("#filter-div").dynatree("getTree");
	
	// clear counts from tree
	tree.visit(  function(node) {
		           if (!node.data.isCategory)  {
		        	   updateNodeIndividualFacetCount(node, -1);   		        	    
		           }
		           
	             }
                 , false
               );
		
	 // redraw entire tree after counts updated
	 tree.redraw();
}


//Method to load the facet results in the search tree and populate search results panel
function showFacetResults(initialLoad)	{
	
	var savedKeywords;

    // save the search terms into a copy of the array -- we'll use this to check at the end to make sure the filters haven't changed since we originally 
	//  submitted Ajax request to get results
	if (activeKeywords.length == 0)  {
			savedKeywords = new Array();
	}
	else {
		// JNJ-2456, copy the original array to create the saved array 
		savedKeywords = activeKeywords.slice(0);
	}

	// Generate list of categories/terms to send to facet search
	// create a string to send into the facet search, in form Cat1:Term1,Term2&Cat2:Term3,Term4,Term5&...

	var facetSearch = new Array();   // will be an array of strings "Cat1:Term1|Term2", "Cat2:Term3", ...   
	var categories = new Array();    // will be an array of categories "Cat1","Cat2"
	var terms = new Array();         // will be an array of strings "Term1|Term2", "Term3"

	// first, loop through each term and add categories and terms to respective arrays 		
    for (var i=0; i<savedKeywords.length; i++)	{
		var keywordId = savedKeywords[i].keywordId; 
		var categoryId = savedKeywords[i].categorySOLR; 
		
		var categoryIndex = categories.indexOf(categoryId);

		// if category not in array yet, add category and term to their respective array, else just append term to proper spot in its array
		if (categoryIndex == -1)  {
		    categories.push(categoryId);
		    terms.push(keywordId);
		}
		else  {
		    terms[categoryIndex] = terms[categoryIndex] + "|" + keywordId; 			
		}
	}
    
	var tree = jQuery("#filter-div").dynatree("getTree");

	// create an array of the categories that come from the tree
	var treeCategories = new Array();
	tree.visit(  function(node) {
        if (node.data.isCategory)  {
     	   var cat = node.data.categorySOLR;     	   
     	   treeCategories.push(cat);        	    
        }
      }
      , false
    );

    // now construct the facetSearch array by concatenating the values from the cats and terms array
    for (var i=0; i<categories.length; i++)	{
    	var queryType = "";
    	
    	// determine if category is part of the tree; differentiate these types of query categories
    	// from others
    	if (treeCategories.indexOf(categories[i])>-1) {
    		queryType = "fq";
    	}
    	else  {
    		queryType = "q";
    	}
    	facetSearch.push(queryType + "=" + categories[i] + ":" + terms[i]);
    }

    // now add all tree categories that arene't being searched on to the string
    for (var i=0; i<treeCategories.length; i++)  {
    	if (categories.indexOf(treeCategories[i])==-1)  {
    		queryType = "ff";
        	facetSearch.push(queryType + "=" + treeCategories[i]);
    	}
    }    
    
    //display loading message. Note: because the contents of the 'results-div' is replaced,
    //there is no need to 'unmask' the loading message
	jQuery("#results-div").mask("Loading..."); 
    
    // add study id to list of fields to facet (so we can get count for show search results)
    facetSearch.push("ff=STUDY_ID");
    
    var queryString = facetSearch.join("&");
    
   	queryString = queryString + "&showSignificantResults=" + document.getElementById('cbShowSignificantResults').checked
    
	jQuery.ajax({
			url:facetResultsURL,
			data:queryString,
			success: function(response) {
				    // make sure the active keywords haven't changed since we issued query -- if they have, don't update tree with results or results panel 
				    if (compareKeywordArrays(savedKeywords, activeKeywords) == false)  { 
				    	return false;
				    }
				
					var facetCounts = response['facetCounts'];
					var html = response['html'];
					var errorMsg = response['errorMsg'];
					
					if (errorMsg != '')  {
						alert(errorMsg);
					}
					
					// set html for results panel
					jQuery('#results-div').html(html);
					
					if(!showHomePageFirst)
				    {
						hideHomePage();
						showResultsPage();
				    }
					else
				    {
						hideResultsPage();
						showHomePage();
						showHomePageFirst=false;
				    }

					if (!initialLoad)  {
						// assign counts that were returned in json object to the tree
						tree.visit(  function(node) {
							           if (!node.data.isCategory && node.data.id)  {
							        	   var id = node.data.id.toString();
							        	   var cat = node.data.categorySOLR;
	
							        	   var catArray = facetCounts[cat];
							        	   var count = catArray[id];
							        	   
							        	   // no count returned for this node means it isn't in solr index because no records exist
							        	   if (!count)  {
							        		   count = 0;
							        	   }
							        	   
							        	   updateNodeIndividualFacetCount(node, count);   
							           }
						             }
					                 , false
					               );
											
						 // redraw entire tree after counts updated
						 tree.redraw();
					 }
				//}
			},
			error: function(xhr) {
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
			}
		});
   	

}

function getUniqueId()  {
	uniqueIdSequence++;
	return uniqueIdSequence
}

// add a new keyword and its category to arrays passed in (global arrays activeKeywords and activeCategories if no arrays passed in) 
function addKeyword(searchTerm, categories, keywords)  {

	// we can assume if categories not supplied, that keywords not supplied either, so use global arrays for both
	if (categories == null)  {
		categories = activeCategories;
		keywords = activeKeywords; 
	}
	
	var categoryDisplay = searchTerm.categoryDisplay;	
	var categoryId = searchTerm.categoryId;
	var categorySOLR = searchTerm.categorySOLR;
	var isGeneCategory = false;
	
	if (checkGeneCategory(categoryId))   {
		isGeneCategory = true;
	}
	
    var keyword = searchTerm.keyword;
	var keywordId = searchTerm.id.toString();

	var uniqueKeywordId = getUniqueId();   // this will be used to uniquely identify the keyword/category combination
	var removeAnchorId =  'removeKeyword_' + uniqueKeywordId;  // the html element identifier for the anchor tag used for removing keywords from active filter  	

	// add keyword object to global array if not on there already
	if (getKeyword(keywordId, keywords) == null)  {
		var keywordObject = {keywordId:keywordId, keyword:keyword, categoryId:categoryId, uniqueKeywordId:uniqueKeywordId, 
				             removeAnchorId:removeAnchorId, categorySOLR:categorySOLR};
		keywords.push(keywordObject);	
		
		// add category object to global array if not on there already
		if (getCategory(categoryId, categories) == null)  {
			var categoryObject = {categoryId:categoryId, categoryDisplay:categoryDisplay, isGeneCategory:isGeneCategory};
			categories.push(categoryObject);		
		}
	}
	
	
}

// Add the search term to the array and show it in the panel.
function addSearchTerm(searchTerm)	{
	
	var categoryId = searchTerm.categoryId;
	var keywordId = searchTerm.id;
	
	addKeyword(searchTerm, activeCategories, activeKeywords);
	
	
	// clear the search text box
	jQuery("#search-ac").val("");
	
	// create flag to track if tree was updated
	var treeUpdated = false
	
	// find all nodes in tree with this key, and select them
	var tree = jQuery("#filter-div").dynatree("getTree");

	tree.visit(  function selectNode(node) {
		             if ( node.data.id == keywordId ) {
		            	 node.select(true);
		            	 node.makeVisible();
		            	 treeUpdated = true;
		             }
	             }
			   , false);

	// only refresh results if the tree was not updated (the onSelect also fires these event, so don't want to do 2x)
	if (!treeUpdated) {
      showSearchTemplate();
	  showSearchResults();
	}
}

// Remove the search term that the user has clicked.
function removeSearchTerm(ctrl)	{
	
	var keywordIndex = getKeywordByRemoveAnchorId(ctrl.id, activeKeywords); 
	var keyword = activeKeywords[keywordIndex];
	
	var catId = keyword.categoryId;
	var keywordId = keyword.keywordId;
	
	// remove the keyword from global array
	activeKeywords.splice(keywordIndex, 1);
	
	// remove the category if there are no terms left in it
	clearCategoryIfNoTerms(catId);
	
	if(activeKeywords.length == 0){
		//disable Save link
		setSaveFilterLink('disable');
	}

	// Call back to the server to clear the search filter (session scope)
	jQuery.ajax({
		type:"POST",
		url:newSearchURL
	});

	// create flag to track if tree was updated
	var treeUpdated = false

	// find all nodes in tree with this key and deSelect
	var tree = jQuery("#filter-div").dynatree("getTree");

	tree.visit(  function deselectNode(node) {
                    if (node.data.id == keywordId)  {
       	                node.select(false);
  	            	    treeUpdated = true;
                    }
                 }
                 , false);
	
	// only refresh results if the tree was not updated (the onSelect also fires these event, so don't want to do 2x)
	if (!treeUpdated) {
      showSearchTemplate();
	  showSearchResults();
	}
}

//export the current analysis data to a csv file
function exportLinePlotData(analysisId, exportType)
{
	
	jQuery('#lineplotExportOpts_'+analysisId).hide(); //hide the menu box
	
	var url='';
	
	switch(exportType)
	{
	case 'data':
	//Export the data for the heatmap
	    
	    var probesList = jQuery('body').data("activeLineplot:" + analysisId); //get the stored probe
	    
		url=getHeatmapDataForExportURL+'?id='+analysisId +'&probesList=' +probesList	
		
		downloadURL(url);
		
	break;

	case 'image':
		
		
		//data should be the same for both lineplot and boxplot
		var data = jQuery('body').data("LineplotData:" + analysisId);

		//redraw the plot with the legend so that it appears in the exported image
		drawLinePlotD3('lineplotAnalysis_'+analysisId, data, analysisId, true, false, null);

		var svgID=  "#lineplotAnalysis_"+analysisId;
		
		exportCanvas(svgID);		
		
		drawLinePlotD3('lineplotAnalysis_'+analysisId, data, analysisId, false, false, null);
		
		break;
	default:
		console.log('Error - invalid Export option');
	}
	
}


//export the current analysis data to a csv file
function exportBoxPlotData(analysisId, exportType)
{
	var url='';
	
	switch(exportType)
	{
	case 'data':
	//Export the data for the heatmap
	    
	    var probesList = jQuery('body').data("activeBoxplot:" + analysisId); //get the stored probe
	    
	    var page = jQuery('body').data("currentPage:" + analysisId); //current page is stored
	    
	    jQuery('#boxplotExportOpts_'+analysisId).hide(); //hide the menu box
	    		
		url=getHeatmapDataForExportURL+'?id='+analysisId +'&probesList=' +probesList	
		
		downloadURL(url);
		
	break;

	case 'image':
		
		jQuery('#boxplotExportOpts_'+analysisId).hide(); //hide the menu box
		
		var data = jQuery('body').data("BoxplotData:" + analysisId);
		
		drawBoxPlotD3('boxplotAnalysis_'+analysisId, data, analysisId, true, false, null);
		
		var svgID=  "#boxplotAnalysis_"+analysisId;
		
		exportCanvas(svgID);		

		drawBoxPlotD3('boxplotAnalysis_'+analysisId, data, analysisId, false, false, null);
		
		break;
	default:
		console.log('Error - invalid Export option');
	}
	
}


function exportCanvas(svgID){
	
	var svgData = jQuery(svgID).html();
	
	//Fix for Firefox: remove "<a xlink...>" and "</a>"
	//These tags are only generated in FF, and canvg does not like them
	if(svgData.indexOf("<a xlink")>-1){
		svgData = svgData.replace(/(<a xlink(.*?)>|<\/a>)/g, "");
	}
	
	if(svgData.indexOf("<title>")>-1){
		svgData = svgData.replace(/(<title(.*?)<\/title>)/g, "");
	}

	canvg('canvas', svgData, { ignoreMouse: true, ignoreAnimation: true }) ;
	
	var imageData =  document.getElementById('canvas').toDataURL();
	imageData = imageData.substr(imageData.indexOf(',') + 1).toString(); //remove header info
	
    var dataInput = document.createElement("input") ; 
    dataInput.setAttribute("name", 'imgData') ;
    dataInput.setAttribute("value", imageData);
	
    var myForm = document.createElement("form"); //create form to post to server
    myForm.method = 'post';
    myForm.action = exportAsImage;
    myForm.appendChild(dataInput);
     
    document.getElementById('hiddenItems').appendChild(myForm) ;
    myForm.submit() ;
    
}


//export the current analysis data to a csv file
function exportHeatmapData(analysisId, exportType)
{
	
	
	var url='';
	
    jQuery('#heatmapExportOpts_'+analysisId).hide(); //hide the menu box

    jQuery("#analysis_holder_" + analysisId).mask("Loading...");
	
	switch(exportType)
	{
	case 'currentPage':
	//Export all probes on the current page
		    
		
		//TODO: Those code is duplicated and should be moved to a seperate function
	    // make sure we are getting number of probes per page for current element
	    var probesPerPageElement = document.getElementById("probesPerPage_" + analysisId);
		var numberOfProbesPerPage = probesPerPageElement.options[probesPerPageElement.selectedIndex].value;
	    
	    var probesList = "";
	    var maxProbeLength = 0;
	    var analysisIndex = getAnalysisIndex(analysisId);
	    
	    var page = jQuery('body').data("currentPage:" + analysisId); //current page is stored
	    
	    
	    // index of probes list is the rankings starting at 1
	    // check that index is less than the maximum for the probe list, and less than max per the current page
		for (var i =0; i < analysisProbeIds[analysisIndex].probeIds.length; i++)  {
			if (i > 0)  {
				probesList = probesList + "|";
			}
			probesList = probesList + analysisProbeIds[analysisIndex].probeIds[i];
			
		}
		
		
		url=getHeatmapDataForExportURL+'?id='+analysisId +'&probesList=' +probesList	
		
	break;
	/* Removed option	
	case 'allPages':
		url=getHeatmapDataForExportURL+'?id='+analysisId +'&probesList=' +'allPages';
		break;
	*/
	case 'allProbes':
	//Export all probes (ignore search on gene/pathway)
		
		url=getHeatmapDataForExportURL+'?id='+analysisId +'&probesList=' +'allProbes';
		break;
	
	case 'image':
		
		var svgID=  "#analysisDiv_"+analysisId;
		var divID = "analysisDiv_" + analysisId;
		
		//redraw the heatmap with the legend
		drawHeatmapD3(divID, jQuery('body').data(analysisId), analysisId, true);
		
		exportCanvas(svgID);	
		
		//redraw the heatmap without the legend
		drawHeatmapD3(divID, jQuery('body').data(analysisId), analysisId, false);
		
		break;
		
	default:
		console.log('Error - invalid Export option');
	}
	

	downloadURL(url);
	

    jQuery("#analysis_holder_" + analysisId).unmask();

}

//this function is used to download a file without opening a new browser window
var downloadURL = function(url)
{
    var iframe;
    iframe = document.getElementById("hiddenDownloader");
    if (iframe === null)
    {
        iframe = document.createElement('iframe');  
        iframe.id = "hiddenDownloader";
        iframe.style.visibility = 'hidden';
        document.body.appendChild(iframe);
    }
    iframe.src = url;   

    
}

function analysisMenuEvent(id){
	
	var analysisID = id.substring(id.indexOf('_')+1,id.length);
	var btnID = id.substring(0,id.indexOf('_'));
	
	switch(btnID){
	
	case 'btnLineplotExport':
		jQuery('#lineplotExportOpts_'+analysisID).toggle();
		jQuery('#lineplotControls_'+analysisID).hide();
		
		break;
	
	case 'btnLineplotControls':
		jQuery('#lineplotControls_'+analysisID).toggle();
		jQuery('#lineplotExportOpts_'+analysisID).hide();
		break;
	
	case 'btnBoxplotExport':
		jQuery('#boxplotExportOpts_'+analysisID).toggle();
		jQuery('#boxplotControls_'+analysisID).hide();
		break;
	
	case 'btnBoxplotControls':
		jQuery('#boxplotControls_'+analysisID).toggle();
		jQuery('#boxplotExportOpts_'+analysisID).hide();
		break;
	
	case 'btnHeatmapExport':
		jQuery('#heatmapExportOpts_'+analysisID).toggle();
		jQuery('#heatmapControls_'+analysisID).hide();
		
		break;
	
	case 'btnHeatmapControls':
		jQuery('#heatmapControls_'+analysisID).toggle();
		jQuery('#heatmapExportOpts_'+analysisID).hide();
		break;
	
	default:
		
		console.log("Invalid option: " +id);	
	}
	
}




// Remove the category from current categories list if there are no terms left that belong to it
function clearCategoryIfNoTerms(categoryId)  {
	
	var found = false;
	for (var j=0; j<activeKeywords.length; j++)	{
		var categoryId2 = activeKeywords[j].categoryId;
		
		if (categoryId == categoryId2)  {
			found = true; 
			break;
		}
	}
	
	if (!found)  {
		var categoryIndex = getCategoryIndex(categoryId, activeCategories);		
		activeCategories.splice(categoryIndex, 1);
	}
}


//Remove the search term that the user has de-selected from filter tree.
function removeFilterTreeSearchTerm(keywordId)	{
	
	var i = getKeywordIndex(keywordId, activeKeywords);
	
	if (i != null)	{
		var catId = activeKeywords[i].categoryId;

		activeKeywords.splice(i, 1);

		// check if there are any remaining terms for this category; remove category from list if none
		clearCategoryIfNoTerms(catId);
		
		if(activeKeywords.length == 0){
			//disable Save link
			setSaveFilterLink('disable');
		}

	}
	
}

function updateHeatmap(analysisID){
	
	var divID = "analysisDiv_" + analysisID;
	drawHeatmapD3(divID, jQuery('body').data(analysisID), analysisID, false);
	
}


//set the heatmap controls
function setHeatmapControls(analysisID){
	
	//sets the slider used to resize the heatmap
	var sliderID="#heatmapSlider_" +analysisID;
	jQuery(sliderID).width('75');
	jQuery(sliderID).slider({
		min:8,
		max:25,
		value:15,
		step: 1,
	 	stop: function(event, ui) {  
	 		updateHeatmap(analysisID);	 		
 	}
	});
	
	
	//sets the slider used to resize the heatmap
	var colorSliderID="#heatmapColorSlider_" +analysisID;
	jQuery(colorSliderID).width('75');
	jQuery(colorSliderID).slider({
		range: true,
		min:-100,
		max:100,
		values: [ -100, 100 ],
	 	slide: function(event, ui) {  

	 		//prevent the min from being greater than 0, and the max less than 0
/*	 		if(ui.values[1] < 0.01 || ui.values[0] > -0.01){
                return false;// do not allow change
            }
*/	 		
	 	},
	 	stop: function(event, ui) {  
	 		updateHeatmap(analysisID);	 		
 	}
	});
	
	var heatmapControlsDiv = "#heatmapControls_" +analysisID;
	jQuery('.heatmapControls_holder').mouseenter(function(){
	//    clearTimeout(jQuery(this).data('timeoutId'));
	    jQuery('body').data('heatmapControlsID', '');
	     
	 //   jQuery(this).find(".tooltip").fadeIn("slow");
	}).mouseleave(function(){
	 //	    var timeoutId = setTimeout(function(){
	 //   	jQuery(heatmapControlsDiv).fadeOut("fast");
	//    }, 800);
	    //set the timeoutId, allowing us to clear this trigger if the mouse comes back over
	//    jQuery(this).data('timeoutId', timeoutId); 
	    jQuery('body').data('heatmapControlsID', analysisID);
	});
	
	 

	
	
}

function setVisTabs(analysisID){
	var tabID = "#visTabs_" + analysisID;
	jQuery(tabID).tabs();	
	jQuery(tabID).bind( "tabsshow", function(event, ui) {
	    if (ui.panel.id == "boxplot_" + analysisID) {
	    	showBoxOrLinePlotVisualization(ui.panel, analysisID, true);
	    } else if (ui.panel.id == "lineplot_" + analysisID)	{
	    	showBoxOrLinePlotVisualization(ui.panel, analysisID, false);
	    }
	});
}



////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Box or Line Plot Visualization Methods
// Show, Load Data and Draw
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function showBoxOrLinePlotVisualization(panel, analysisID, isBoxplot)	{
	var analysisIndex = getAnalysisIndex(analysisID);
	var probeIds = analysisProbeIds[analysisIndex].probeIds;
	var selectList = analysisProbeIds[analysisIndex].selectList;

	var typeString = '';
	if (isBoxplot) {
		typeString = 'Box';
	}  
	else  {		
		typeString = 'Line';
	}
	
	// retrieve the active probe for the current analysis 
	var activeProbe = getActiveProbe(analysisID);
	
	// if the currently displayed plot is not the active probe then redraw
	var redraw = false;
	
	var currentProbe = jQuery('body').data("active" + typeString + "plot:" + analysisID);
	
	if (currentProbe != activeProbe)  {
		redraw = true;
	}

	// if we're not showing a plot for the active probe, then reload
	if(redraw) { 
		jQuery("#analysis_holder_" + analysisID).mask("Loading...");
	
		if (isBoxplot)  {			
			loadBoxPlotData(analysisID, activeProbe);

		}
		else  {
			loadLinePlotData(analysisID, activeProbe);			
		}
	}
	
}


//Method to add the probes for the Line plot
function setLineplotProbes(analysisID, probeID)	{
	setProbesDropdown(analysisID, probeID, "#probeSelectionLineplot_" + analysisID);	
}


function getActiveProbe(analysisId){
	
	// retreive the active probe for the current analysis, first retrieve from global data
	var probeId = jQuery('body').data("activeAnalysisProbe:" + analysisId);
	
	// if not defined yet, set to the first one for the current page showing for the analysis
	if (probeId == undefined)  {
		var analysisIndex = getAnalysisIndex(analysisId);
		probeId = analysisProbeIds[analysisIndex].probeIds[0];		
	}
	 
	return probeId;
	
}

function setActiveProbe(analysisId, probeId){
	//store the currently active probe for the analysis; i.e the last one drawn for the box or line plot
	jQuery('body').data("activeAnalysisProbe:" + analysisId, probeId); 
	
}




function getGeneforDisplay(analysisID, probeID){

	var analysisIndex = getAnalysisIndex(analysisID);
	var probeIds = analysisProbeIds[analysisIndex].probeIds ;

	var maxProbeIndex = analysisProbeIds[analysisIndex].maxProbeIndex;
	 var probeDisplay = "";
    for (var i=0; i<maxProbeIndex; i++)  {
    	if (probeIds[i] == probeID) {
    		probeDisplay = analysisProbeIds[analysisIndex].selectList[i];
    		return probeDisplay;
    	}
    }
	 return false;
}


//Load the line plot data
function loadLinePlotData(analysisID, probeID)	{
	
	if (probeID === undefined)	{
		// We are called from the user switching probes, throw up the mask and get the probeID
		jQuery("#analysis_holder_" + analysisID).mask("Loading..."); 
		probeID = jQuery("#probeSelectionLineplot_" + analysisID).find('option:selected').attr('id');
		
	}
	
	// retrieve the corresponding display value for the probe Id 
    var analysisIndex = getAnalysisIndex(analysisID);
    var probeIds = analysisProbeIds[analysisIndex].probeIds ;
    var maxProbeIndex = analysisProbeIds[analysisIndex].maxProbeIndex; 

	
	rwgAJAXManager.add({
		url:getLinePlotDataURL,									
		data: {id: analysisID, probeID: probeID},
		timeout:60000,
		success: function(response) {
			
			//store the response
			jQuery('body').data("LineplotData:" + analysisID, response); //store the response
			
			setActiveProbe(analysisID, probeID);
			jQuery('#analysis_holder_' +analysisID).unmask(); //hide the loading msg, unblock the div 
			drawLinePlotD3('lineplotAnalysis_'+analysisID, response, analysisID, false, false, null);
			jQuery('#lineplotAnalysis_'+analysisID).show();
			jQuery('#lineplot_'+analysisID).show();

	//		jQuery('#lineplotLegend_'+analysisID).prepend("<p class='legend_probe'>Line plot for "+probeDisplay +"</p>"); //add the probe ID to the legend
			
			jQuery('#lineplotLegend_'+analysisID).show();

			jQuery('body').data("LineplotData:" + analysisID, response); //store the response
			
			jQuery('body').data("activeLineplot:" + analysisID, probeID); //store the analysis ID and probe ID of this lineplot;
																		 //used to determine if the lineplot has already been drawn
			setLineplotProbes(analysisID, probeID);
			jQuery("#analysis_holder_" + analysisID).unmask(); 
			
			
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Box Plot Visualization Methods
// Show, Load Data and Draw
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function openBoxPlotFromHeatmap(analysisID, probe){
	  var divID = "analysisDiv_" + analysisID;

	  jQuery('#visTabs_' +analysisID).tabs('select', 'boxplot_'+analysisID); // switch to boxplot tab
		
	  var currentBoxplot = jQuery('body').data("activeBoxplot:" + analysisID);
	  
	  if(currentBoxplot != probe){
		  jQuery("#analysis_holder_" + analysisID).mask("Loading...");	
		  loadBoxPlotData(analysisID, probe);		  
	  }
}

//Method set the probes in a select list for current page
function setProbesDropdown(analysisID, selectedProbeID, divId)	{
	var analysisIndex = getAnalysisIndex(analysisID);
	var probeIds = analysisProbeIds[analysisIndex].probeIds;
	var selectList = analysisProbeIds[analysisIndex].selectList;

	jQuery(divId).empty();
	for (var i=0; i<probeIds.length; i++) {
		
		if (probeIds[i] == selectedProbeID)	{
			jQuery(divId).append(jQuery("<option id></option>").attr("selected", "selected").attr("id", probeIds[i]).attr("value", selectList[i]).text(selectList[i]));
		} else	{
			jQuery(divId).append(jQuery("<option></option>").attr("id", probeIds[i]).attr("value", selectList[i]).text(selectList[i]));
		}
	}	
}


//Method to add the probes for the box plot
function setBoxplotProbes(analysisID, selectedProbeID)	{
	setProbesDropdown(analysisID, selectedProbeID, "#probeSelection_" + analysisID);
}

// Load the box plot data
function loadBoxPlotData(analysisID, probeID)	{	
	jQuery('#boxplotEmpty_' +analysisID).hide(); //hide the message that tells the user to select a probe first
	
	if (probeID === undefined)	{
		// We are called from the user switching probes, throw up the mask and get the probeID
		jQuery("#analysis_holder_" + analysisID).mask("Loading..."); 
		probeID = jQuery("#probeSelection_" + analysisID).find('option:selected').attr('id');
		
	}
	
	// retrieve the corresponding display value for the probe Id 
	
	/*
    var analysisIndex = getAnalysisIndex(analysisID);
    var probeDisplay = ""
    var probeIds = analysisProbeIds[analysisIndex].probeIds ;
    var maxProbeIndex = analysisProbeIds[analysisIndex].maxProbeIndex; 
    for (var i=0; i<maxProbeIndex; i++)  {
    	if (probeIds[i] == probeID) {
    		probeDisplay = analysisProbeIds[analysisIndex].selectList[i];
    		break;
    	}
    }
        
        */
	rwgAJAXManager.add({
		url:getBoxPlotDataURL,
		data: {id: analysisID, probeID: probeID},
		timeout:60000,
		success: function(response) {
			setActiveProbe(analysisID, probeID);
			drawBoxPlotD3('boxplotAnalysis_'+analysisID, response, analysisID, false, false, null);
			jQuery('#boxplotLegend_'+analysisID).show();
			jQuery('#boxplotAnalysis_'+analysisID).show();	
			
			jQuery('body').data("BoxplotData:" + analysisID, response); //store the response
			
			jQuery('body').data("activeBoxplot:" + analysisID, probeID); //store the analysis ID and probe ID of this boxplot;
																		 //used to determine if the boxplot has already been drawn
		
			setBoxplotProbes(analysisID, probeID);
			jQuery("#analysis_holder_" + analysisID).unmask(); 
			
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}

function changeRangeRadioBtn(graph, analysisID){
	
	var radioID = '#' +graph +'RangeRadio_Manual_' +analysisID;
	var rangeMinID = '#' +graph +'RangeMin_' +analysisID;
	var rangeMaxID = '#' +graph +'RangeMax_' +analysisID;
	
	if(jQuery(radioID).is(':checked')){
		
		jQuery(rangeMinID).removeAttr('disabled');
		jQuery(rangeMaxID).removeAttr('disabled');
		
	}else{
		
		jQuery(rangeMinID).attr('disabled',true);
		jQuery(rangeMaxID).attr('disabled',true);		
		

		
		if(graph == 'boxplot'){
			
			//redrew the boxplot to set it back to default range
			updateBoxPlot(analysisID)
			
		}else if(graph == 'lineplot') {
			
			updateLineplot(analysisID);
			
		}
	}
}


function updateLineplot(analysisID){
	
	//data should be the same for both lineplot and boxplot
	var data = jQuery('body').data("LineplotData:" + analysisID);
	
	if(data == ''){
		console.log("Error: Could not find data");
	}else
		{
			drawLinePlotD3('lineplotAnalysis_'+analysisID, data, analysisID, false, false, null);
		}
}

//collapse all of the open analyses
function collapseAllAnalyses(){
		
	while (openAnalyses.length>0){
		//each time showVisualization is called, the current analysis is removed from openAnalyses
		showVisualization(openAnalyses[0], false);
	}
	
	//close all expanded trials
	while (openTrials.length>0){
		toggleDetailDiv(openTrials[0], null, null); //only the trial ID must be passed in
	}
}

function updateBoxPlot(analysisID){
	
	var data = jQuery('body').data("BoxplotData:" + analysisID);
	
	if(data == ''){
		console.log("Error: Could not find data");
	}else
		{
			drawBoxPlotD3('boxplotAnalysis_'+analysisID, data, analysisID, false, false, null);
		}
}

// Helper function to provide the rank for the percentile calculation in the box plot
function getRank(P, N)	{
	return Math.round(P/100 * N + 0.5);			// Use P/100 * N + 0.5 as denoted here: http://en.wikipedia.org/wiki/Percentile
}


// Show the heatmap visualization 
function showVisualization(analysisID, changedPaging)	{		
	var analysisHeaderDiv = "#TrialDetail_" + analysisID + "_anchor"
	var divID = "#analysisDiv_" + analysisID;
	var divID2 = "analysisDiv_" + analysisID;
	var loadingDiv = "#analysis_holder_"+ analysisID;
	var imgExpand = "#imgExpand_"  + analysisID;
	var div = document.getElementById(divID);	
	var hmFlagDiv = divID+"_state";
	var hmFlag = jQuery(hmFlagDiv).val();
	// Check the value of the hidden field that is capturing the following "click" states
	// 0: No heatmap loaded, hidden
	// 1: Heatmap loaded, visible
	// 2: Heatmap loaded, hidden

	// if the paging has changed, need to reload page
	if (hmFlag != "1" || changedPaging)	{				
		var src = jQuery(imgExpand).attr('src').replace('down_arrow_small2.png', 'up_arrow_small2.png');
		jQuery(imgExpand).attr('src',src);
		jQuery(analysisHeaderDiv).addClass("active-analysis");
		

		
		if(!changedPaging){
			jQuery(loadingDiv).toggle();
			openAnalyses.push(analysisID); //store this as an open analysis
		}

		if (hmFlag == "0" || changedPaging)	{
			// don't re-set heap map controls if re-loading because of a change in probes per page
			if (!changedPaging)  {
				setVisTabs(analysisID);
				setHeatmapControls(analysisID);
			
				// sync the local probes per page setting with the global one if not re-loading because of changed paging
				probesPerPageElementGlobal = document.getElementById("probesPerPage");
				probesPerPageElementHeatmap = document.getElementById("probesPerPage_"+analysisID);
				probesPerPageElementHeatmap.selectedIndex = probesPerPageElementGlobal.selectedIndex;
		    }

			jQuery(loadingDiv).mask("Loading...");
			loadHeatmapPaginator(divID2, analysisID, 1);			
		}		
		jQuery(hmFlagDiv).val("1");
	} else	{
		var src = jQuery(imgExpand).attr('src').replace('up_arrow_small2.png', 'down_arrow_small2.png');
		jQuery(imgExpand).attr('src',src);
		jQuery(loadingDiv).toggle('blind', {}, 'fast');
		jQuery(analysisHeaderDiv).removeClass("active-analysis");	
		jQuery(hmFlagDiv).val("2");
		
		//remove the analysis from the array, while leaving all others
		//openAnalyses = openAnalyses.splice( jQuery.inArray(analysisID, openAnalyses), 1 );
		removeByValue(openAnalyses,analysisID);
		
	} 	
	return false;
}

// Make a call to the server to load the heatmap data
// keyword query string is optional, is provided for use by the heatmap shown on cta for a specific analysis
function loadHeatmapData(divID, analysisID, probesPage, probesPerPage,  isSA, keywordsQueryString)	{
	
	rwgAJAXManager.add({
		url:getHeatmapDataURL,
		data: {id: analysisID, probesPage: probesPage, probesPerPage:probesPerPage, isSA:isSA, keywordsQueryString:keywordsQueryString},
		timeout:60000,
		success: function(response) {
			if (!isSA)  {
				jQuery('body').data(analysisID, response); //store the result set in case the heatmap is updated 
				jQuery('#analysis_holder_' +analysisID).unmask(); //hide the loading msg, unblock the div				
				drawHeatmapD3(divID, response, analysisID, false);	
				jQuery('#'+divID).show();   // why needed, not needed with old heat map?
				jQuery('#heatmapLegend_'+analysisID).show();
		        var analysisIndex = getAnalysisIndex(analysisID);
		        var probesList = analysisProbeIds[analysisIndex].probeIds;
		        var maxProbeIndex = analysisProbeIds[analysisIndex].maxProbeIndex;
				
				if(maxProbeIndex == 1){ //only one probe returned
					loadBoxPlotData(analysisID, probesList[0]);	//preload boxplot
				}	        
			}
			else  {
				jQuery('#analysis_holderSA_' +analysisID).unmask(); //hide the loading msg, unblock the div				
				drawHeatmapD3(divID, response, analysisID, false, isSA, keywordsQueryString);	
				jQuery('#'+divID).show();   // why needed, not needed with old heat map?
			}


	        
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}

//displays pop-up of gene with tabs to internal and external sources
function showGeneInfo(geneID)
{
	var w=window.open('./../details/gene/?rwg=y&altId='+geneID , 'detailsWindow', 'width=900,height=800'); 
	w.focus(); 
}


//Show diff between each cohort
//returnOnlyDiff: if true, return only the different terms
function highlightCohortDescriptions(cohortDesc, returnOnlyDiff){
	
	var arySplit = new Array();
	var aryDif = new Array();
	var aryDescNew = new Array();
	
	//1. Split each cohort description into an array of terms
	for (var i=1; i<cohortDesc.length; i++){
		arySplit[i]= cohortDesc[i].split('_');
	}
	
	//2. Loop through the array and compare each term to the term in the same position of the next description
	//	 mark which ones are same and different in aryDif
	for (var i=1; i<arySplit.length-1; i++){
		
			for(var x=0; x < arySplit[i].length; x++){
				
					if(trim(arySplit[i][x]).toUpperCase() == trim(arySplit[i+1][x]).toUpperCase()){
						
							if(aryDif[x] != false){
								aryDif[x] = true;
							}
							else{
								aryDif[x] = false;
								}
						}
					else{
						aryDif[x] = false;
					}
				}
		}
	
	//3. Rebuild array, inserting syntax to denote which terms are different
	for (var i=1; i<arySplit.length; i++){
		
		aryDescNew[i]=''; //initilize the first value
		
		for(var x=0; x < arySplit[i].length; x++){

			
				if(aryDif[x] == true){ //the terms are the same
					if(!returnOnlyDiff){
						aryDescNew[i] = aryDescNew[i] + arySplit[i][x];	
					}
				}
				else{	//the terms are different
					if(!returnOnlyDiff){ 
						aryDescNew[i] = aryDescNew[i] +"<span class='highlight'>" +arySplit[i][x] +"</span>";
					}
					else if(returnOnlyDiff){
						aryDescNew[i] = aryDescNew[i] +arySplit[i][x] + ', ';
					}
				}
				
				//check if this is the last term; if not, add an underscore between terms
				if(x+1 < arySplit[i].length && !returnOnlyDiff){
					aryDescNew[i] = aryDescNew[i]+'_';
				}
			}
		
			if(returnOnlyDiff){//remove trailing space and comma
				aryDescNew[i] = aryDescNew[i].slice(0,-2);		
			}
	}
	
	return aryDescNew;

}
//remove whitespace
function trim(stringToTrim) {
	return stringToTrim.replace(/^\s+|\s+$/g,"");
}

//convert a string to Title Case
function toTitleCase(str)
{
    return str.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
}

function goToByScroll(id){
	jQuery('#main').animate({scrollTop: jQuery("#"+id).offset().top},'slow');
}

function checkGeneCategory(catId)  {
	if ((catId == 'GENE') || (catId == 'PATHWAY') || (catId == 'GENELIST') || (catId == 'GENESIG')) {
		return true;
	}
	else  {
		return false;
	}
}

/* Find the width of a text element */
String.prototype.visualLength = function(fontFamily) 
{ 
    var ruler = document.getElementById("ruler"); 
    ruler.style.font = fontFamily; 
    ruler.innerHTML = this; 
    return ruler.offsetWidth; 
} 



function roundNumber(num, dec) {
	var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
	return result;
}

//Main method to show the current array of search terms (if no args passed in, being used to populate search terms div; otherwise we're retrieving html for tooltip
function showSearchTemplate(categories, keywords)	{
	
	// if categories and terms not passed in, use the global arrays (this is for popuataing div); else, this is for tooltip, and we'll pass back generated
	//   HTML
	var tooltip;
	if (!categories)  {
		tooltip = false;
		categories = activeCategories;
		keywords = activeKeywords;
	}
	else {
		tooltip = true;
	}
	
	var searchHTML = '';
	
	var firstItem = true;

	// iterate through categories array and move all the "gene" categories together
	var newCategories = new Array();
	
	var geneCategoriesProcessed = false;
	for (var i=0; i<categories.length; i++)	{
		
		// when we find a "gene" category, add it and the rest of the "gene" categories to the new array
		if (categories[i].isGeneCategory) {
			// first check if we've processed "gene" categories yet
			if (!geneCategoriesProcessed)  {
				
				// add first gene category to new array
				newCategories.push(categories[i]);

				// look for other "gene" categories, starting at the next index value, and add each to array
				for (var j=i+1; j<categories.length; j++)	{
					if (categories[j].isGeneCategory) {
						newCategories.push(categories[j]);
					}				
				}
				// set flag so we don't try to process again
				geneCategoriesProcessed = true;
			}
		}
		else  {    // not a gene catageory, add to new list
			newCategories.push(categories[i]);
		}
	}
	
	// replace old array with new array
	categories = newCategories;
	
	for (var i=0; i<categories.length; i++)	{
		for (var j=0; j<keywords.length; j++)	{
			
			if (categories[i].categoryId == keywords[j].categoryId)  {
											
				if (firstItem)	{
					var catDisplay = categories[i].categoryDisplay;

					if (i>0)	{	
						
						var suppressAnd = false;
						// if this is a "gene" category, check the previous category and see if it is also one
		                if (categories[i].isGeneCategory)  {
		                	if (categories[i - 1].isGeneCategory)  {
		                		suppressAnd = true;	
		                	}
		                } 
						
		                // if previous category is a "gene" category, don't show AND
		                if (!suppressAnd)  {
							searchHTML = searchHTML + "<span class='category_join'>AND<span class='h_line'></span></span>";  			// Need to add a new row and a horizontal line
					    }
		                else  {
							searchHTML = searchHTML + "<br/>";  				                	
		                }
					}
					searchHTML = searchHTML +"<span class='category_label'>" + catDisplay + "&nbsp;></span>&nbsp;";
					firstItem = false;
				} else	{
					searchHTML = searchHTML + "<span class='spacer'>| </span>";
				}				

				var aTag = '';
				var imgTag = ''

				// don't include a or img tags if for tooltip
				if (!tooltip)  {
					aTag = '&nbsp;<a id="' + keywords[j].removeAnchorId + '" class="term-remove" href="#" onclick="removeSearchTerm(this);">' 
 
					imgTag = '<img alt="remove" src="./../images/small_cross.png"/></a>&nbsp;'
				}
					
				searchHTML = searchHTML + "<span class=term>"+ keywords[j].keyword + aTag + imgTag + "</span>";
			} else	{
				continue;												// Do the categories by row and in order
			}
		}
		firstItem = true;
	}
	
	if (!tooltip)  {
		// populate div if not tooltip
		
		//changing the internal html causes problems for the resizable feature
		//so, destry the resizeable code first, then reapply after updating the html
		jQuery('#active-search-div').resizable("destroy");
		
		jQuery('#active-search-div').html(searchHTML);
		
		setActiveFiltersResizable();
		    	
		
		if(keywords.length > 0){
			//enable Save link
			setSaveFilterLink('enable');
		}
	}
	else  {		
		// html for tooltip - just return the html
		return searchHTML;
	}
}

//Allows resizing the "Active Filters" div
function setActiveFiltersResizable(){
	
	jQuery('#active-search-div').resizable({
    	maxWidth:280,
		minWidth:280,
		handles: "s",
        resize: function (event,ui){
				jQuery("#title-filter").css({ top: 127 + ui.size.height +'px' });
				jQuery("#side-scroll").css({ top: 155 + ui.size.height +'px' });
            },
         stop:function (event,ui){
				
				jQuery("#title-filter").css({ top: 127 + ui.size.height +'px' });
				jQuery("#side-scroll").css({ top: 155 + ui.size.height +'px' });
          }
    });
	
	//fix some issues with the size to prevent scroll bars
	jQuery('.ui-resizable-e').css({right:0});
	jQuery('.ui-resizable-s').css({bottom:0});
	
}


// retrieve the current list of search keyword ids
function getSearchKeywordList()   {

	var keywords = new Array();
	
	for (var j=0; j<activeKeywords.length; j++)	{
		keywords.push(activeKeywords[j].keywordId);
	}
	
	return keywords;
}

//retrieve the current list of search keyword ids for the cross trial analysis
function getXTSearchKeywordList()   {

	var keywords = new Array();
	
	for (var j=0; j<xtSelectedKeywords.length; j++)	{
		keywords.push(xtSelectedKeywords[j].id);
	}
	
	return keywords;
}

//retrieve the current list of analyssis ids for the cross trial analysis
function getXTAnalysisIdList()   {

	var analysisIds = new Array();
	
	for (var j=0; j<selectedAnalyses.length; j++)	{
		analysisIds.push(selectedAnalyses[j].id);
	}
	
	return analysisIds;
}


// focus the first visible child element of the passed in element that is an input type
function focusFirstInput(parent)  {		
	var child = parent.find('input[type=text],textarea,select').filter(':visible:first'); 
	if (child.length == 1)  {
	    child.focus();	
	}
}

function modalEffectsOpen(dialog)  {
	
    dialog.overlay.fadeIn(150, function () {
		    dialog.container.slideDown(150,   function () {dialog.data.fadeIn(150, 
		    		                                       function() {focusFirstInput(dialog.container);}  );
			  								              }
                          );
                                            });
}

function modalEffectsClose(dialog)  {
			dialog.data.fadeOut(150, function () {  
				dialog.container.slideUp(150, function () {
					dialog.overlay.fadeOut(150, function () {
						jQuery.modal.close(); 
		      });
		    });
		  });

		  jQuery("#searchTooltip").remove();
			
}


//enable or disable the save filter link
function setSaveFilterLink(state){
	
	if(state == 'enable'){		
		jQuery('#save-modal').removeClass('title-link-inactive');
		jQuery('#save-modal').addClass('title-link-active');
		jQuery('#save-modal').unbind('click').click(function(){openSaveSearchDialog(false);});   
		
		}
	else if (state == 'disable'){

		jQuery('#save-modal').addClass('title-link-inactive');
		jQuery('#save-modal').removeClass('title-link-active');
		jQuery('#save-modal').off('click');
	}
}

//enable or disable the save xt filter link
function setSaveXTFilterLink(){
	if ((xtSelectedKeywords.length > 0) && (selectedAnalyses.length > 0)) {  
		jQuery('#save-modal-xt').removeClass('title-link-inactive');
		jQuery('#save-modal-xt').addClass('title-link-active');
		jQuery('#save-modal-xt').unbind('click').click(function(){openSaveSearchDialog(true);});
		
		}
	else {
		jQuery('#save-modal-xt').addClass('title-link-inactive');
		jQuery('#save-modal-xt').removeClass('title-link-active');
		jQuery('#save-modal-xt').off('click');

	}
}

//enable or disable the clear xt filter link
function setClearXTLink(){
	if ((xtSelectedKeywords.length > 0) || (selectedAnalyses.length > 0)) {  
		jQuery('#clear-xt').removeClass('title-link-inactive');
		jQuery('#clear-xt').addClass('title-link-active');
		jQuery('#clear-xt').unbind('click').click(function(){openSaveSearchDialog(true);});
		}
	else {
		jQuery('#clear-xt').addClass('title-link-inactive');
		jQuery('#clear-xt').removeClass('title-link-active');
		jQuery('#clear-xt').off('click');
	}
}

function openSaveSearchDialog(isXT)  {

	var keywords;
	var analysisIds;
	var title;
	var clickFunction;
	if (!isXT)  {
		keywords = getSearchKeywordList();
		title = 'Save Faceted Search'
		clickFunction = function() {
			saveSearch('FACETED_SEARCH');};
	}
	else {
		keywords = getXTSearchKeywordList();
		analysisIds = getXTAnalysisIdList();
		title = 'Save Cross Trial Analyses';
		
		if (analysisIds.length == 0)  {
			alert('No analyses to save!')
			return false;
		}

		clickFunction = function() {
			saveSearch('XT');};
		
	}

	if (keywords.length == 0)  {
		alert('No keywords to save!')
		return false;
	}

	jQuery('#saveModalTitle').html(title);
	
	// make sure we unbind any click events first, or else they keep get adding and we end up calling
	//   the save search function multiple times
	jQuery("#saveSearchLink").unbind('click').click(clickFunction);
	
	jQuery('#save-modal-content').modal({onOpen: modalEffectsOpen, opacity: [70], position: ["25%"], onClose: modalEffectsClose,
		onShow: function (dialog) {
        dialog.container.css("height", "auto");
		    }	

	});

}

// save a faceted search to the database
function saveSearch(searchType)  {
	
	var keywords;
	var analysisIds;
	var analysisIdsString;
	
	if (searchType == 'XT')  {
		
		keywords = getXTSearchKeywordList();
		analysisIds = getXTAnalysisIdList();

		if (!analysisIds)  {
			alert('Analysis Ids must be provided for saving a Cross Trial Search!');
			return;
		}
	    analysisIdsString = analysisIds.join("|")
	    
	}
	else  {
		keywords = getSearchKeywordList();		
	}
		
	var nameField = 'searchName'; 
	var name = jQuery("#" + nameField).val();
	
	if  (!name) {
		jQuery("#modal-status-message").show().html("Please provide a Name to save this filter.");
		return false;
	}

	//  had no luck trying to use JSON libraries for creating/parsing JSON string so just save keywords as pipe delimited string 
	if (keywords.length>0)  {
		var keywordsString = keywords.join("|");
		
    	jQuery("#save-modal-content").mask("Saving...");
		
		rwgAJAXManager.add({
			url:saveSearchURL,
			data: {keywords: keywordsString, name: name, searchType:searchType, analysisIds: analysisIdsString},
			timeout:60000,
			success: function(response) {
		    	jQuery("#save-modal-content").unmask();

		    	
		    	  if (response['success'])  {
		    		    jQuery('#save-modal-content-main').fadeOut(200, function(){
		    		    	jQuery("#modal-status-message").fadeIn(250).html(response['message']);
					    	jQuery('#modal-status-message').delay(1200).fadeOut(800, function() {
				            	jQuery.modal.close();
				    		  });	
		    		    });
		    		    
		    		    refreshHomeFavorites(searchType);
		    	  	}else{
	    		    	jQuery("#modal-status-message").fadeIn(200).html(response['message']);
		    	  	}
	            
			},
			error: function(xhr) {
		    	jQuery("#save-modal-content").unmask();
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
			}
		});
	}
	else  {
		alert("No search keywords to save!")
	}
	
}

//update a faceted search in the database 
function updateSearch(id, searchType)  {

	var name = jQuery("#searchName_" + id).val();
	
	if  (!name) {
		
		jQuery("#modal-status-message_"+id).show('highlight').html('Pleae enter a name to save the filter.');
    
		return false;
	}

	jQuery("#load-modal-content").mask("Saving...");
	rwgAJAXManager.add({
		url:updateSearchURL,
		data: {id:id, name: name},
		timeout:60000,
		success: function(response) {
			
        	jQuery("#load-modal-content").unmask();
            // close the dialog and update static field if success flag was true
            if (response['success'])  {
            	
            	jQuery("#labelSearchName_" + id).text(name);

            	refreshHomeFavorites(searchType);
            	
            	hideEditSearchDiv(id);	            	
            }else
            	{
        			jQuery("#modal-status-message_"+id).show('highlight').html(response['message']);
            	}
            
		},
		error: function(xhr) {
        	jQuery("#load-modal-content").unmask();
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
	
}


//delete a faceted search from the database
function deleteSearch(id, searchType)  {
	
	var name = jQuery("#labelSearchName_" + id).text();
	
	if (!confirm('Are you sure you want to delete search "' + name + '"?'))  {
		return false;
	}
	
	jQuery("#load-modal-content").mask("Deleting...");
	rwgAJAXManager.add({
		url:deleteSearchURL,
		data: {id: id},
		timeout:60000,
		success: function(response) {
        	jQuery("#load-modal-content").unmask();
            
            
            if (response['success'])  {
            	jQuery("#filter_favorites_"+id).remove();
            	
            	refreshHomeFavorites(searchType);
            }
            else {
            	alert(response['message']);
        	}
        
		},
		error: function(xhr) {
        	jQuery("#load-modal-content").unmask();
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
	
}

//show the edit search div for the given search id
function showEditSearchDiv(id)  {

	// first hide any other edit divs that might be showing and show the static version
   	jQuery("#favoritesTable div.editSearchDiv").hide();	            	
   	jQuery("#favoritesTable div.staticSearchDiv").show();	            	
	
   	
   	// now hide this specific static div, and show its edit div
   	jQuery("#staticSearchDiv_" + id).hide(200);   	   	
   	jQuery("#editSearchDiv_" + id).show(200, function() {  		
		   		focusFirstInput(jQuery(this));
		   	 } 
   	);	            	
}

//hide the edit search div for the given search id
function hideEditSearchDiv(id)  {
				
   	jQuery("#editSearchDiv_" + id).hide(200);	            	
   	jQuery("#staticSearchDiv_" + id).show(200);	            	
}



function openLoadSearchDialog(isXT)  {
	
//	var html = "";
	var keywords;
	var analysisIds;
	var title;
	var clickFunction;
	var searchType;
	if (!isXT)  {
		title = 'Load Favorite';
		searchType = 'FACETED_SEARCH';
	}
	else {
		title = 'Load Cross Trial Analyses';
		searchType = 'XT';
	}

	jQuery('#loadSearchModalTitle').html(title);
	
    jQuery('#load-modal-content').modal({onOpen: modalEffectsOpen, position: ["5%"], onClose: modalEffectsClose });
    
    jQuery('#simplemodal-container').mask("Loading...");
	
	rwgAJAXManager.add({
		url:renderFavoritesTemplateURL,									
		data: {searchType:searchType}, 
		timeout:60000,
		success: function(response) {
		
		    jQuery('#load-modal-content').html(response);
		    jQuery('#simplemodal-container').unmask();
			
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
	
	return false;

}

function refreshHomeFavorites(searchType)  {
	
	var searchType;
	var div;
	if (searchType == 'XT')  {
		div = 'savedCrossTrialAnalysis';
	}
	else {
		div = 'homefavorites';
	}
    
    jQuery('#' + div).mask("Loading...");
	
	rwgAJAXManager.add({
		url:renderHomeFavoritesTemplateURL,									
		data: {searchType:searchType}, 
		timeout:60000,
		success: function(response) {
		
		    jQuery('#' + div).html(response);
		    jQuery('#' + div).unmask();
			
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
	
	return false;

}

// load in the saved favorites (type=FACETED_SEARCH or XT) with the given id
function loadSearch(searchType, id)  {

	if (searchType=='XT')  {
		jQuery("#cross-trial-div").mask("Loading...");
	}
	
	rwgAJAXManager.add({
		url:loadSearchURL,
		data: {id: id, searchType:searchType},   
		timeout:60000,
		success: function(response) {
        	jQuery.modal.close();	            	
			
			if (response['success'])  {
				if (searchType=='FACETED_SEARCH') {
					// clear global arrays
					activeCategories = new Array();
					activeKeywords = new Array();
									
					var tree = jQuery("#filter-div").dynatree("getTree");
	
					// clear the selected items from tree
					// Make sure the onSelect event doesn't fire for the nodes
					// Otherwise, the main search query is going to fire after each item is deselected, as well as facet query
					allowOnSelectEvent = false;
					tree.visit(function clearNode(node) {
														 updateNodeIndividualFacetCount(node, -1);
						                                 node.select(false);
					                                    }, 
					                                    false
					           )
					allowOnSelectEvent = true;
	
					var searchTerms = response['searchTerms'] 
					var count = response['keywordCount'] 
					var termsNotFound = response['termsNotFound'] 
					
					for (i=0; i<count; i++)  {
						
						var searchParam={id:searchTerms[i].id,
								         categoryDisplay:searchTerms[i].categoryDisplay,
								         keyword:searchTerms[i].keyword,
								         categoryId:searchTerms[i].categoryId,
								         categorySOLR:searchTerms[i].categorySOLR
								         };
						
						// make sure we call addKeyword and NOT addSearchTerm (if  we call the latter then we requery SOLR every time
						//    we add one of the saved terms back in)
						addKeyword(searchParam, activeCategories, activeKeywords);
						
						// select the keyword in the tree
						allowOnSelectEvent = false;    // onSelect event will cause a SOLR query call; we don't want this for each term, only at end
						tree.visit(  function selectNode(node) {
				             if ( node.data.id == searchTerms[i].id ) {
				            	 node.select(true);
				             }
			             }
					   , false);
						allowOnSelectEvent = true;
					}

					showSearchTemplate();
					showSearchResults(); //reload the full search results
		
	            	if (termsNotFound > 0)  {
	            		alert(termsNotFound + ' terms could not be loaded from the saved search.  Results may not be as expected.')
	            	}
				}
				else {
					var searchTerms = response['searchTerms'] 
					var count = response['keywordCount'] 
					var termsNotFound = response['termsNotFound'] 
					var analyses = response['analyses'] 
					var analysisCount = response['analysisCount'] 
					var analysesNotFound = response['analysesNotFound'] 

					xtSelectedKeywords = [];					
					for (var kw in searchTerms)  {
						addXTSelectedKeyword(searchTerms[kw].id, searchTerms[kw].keyword, searchTerms[kw].categoryId);

					}
					
					selectedAnalyses = [];
					for (var a in analyses)  {
						selectedAnalyses.push({'id':analyses[a].id, 'title':analyses[a].title, 'studyID':analyses[a].studyId});
					}
					
					jQuery.cookie('selectedAnalyses', JSON.stringify(selectedAnalyses));

					var newLabel = "(" + selectedAnalyses.length + ")";
					jQuery("#analysisCountLabel").html(newLabel);					
					
					updateCrossTrialGeneCharts();
					
					showCrossTrialAnalysis();
					
	            	if (termsNotFound > 0)  {
	            		alert(termsNotFound + ' terms could not be loaded from the saved XT analysis.  Results may not be as expected.')
	            	}
	            	if (analysesNotFound > 0)  {
	            		alert(analysesNotFound + ' analyses could not be loaded from the saved XT analysis.  Results may not be as expected.')
	            	}
				}
				jQuery("#searchTooltip").remove();
				 
			}
			else  {
				alert(response['message']);  // show message from server  
			}
			
			jQuery("#cross-trial-div").unmask();
			
 		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});

}




// Clear the tree, results along with emptying the two arrays that store categories and search terms.
function clearSearch()	{
	
	//remove all pending jobs from the ajax queue
	rwgAJAXManager.clear(true);
	
	openAnalyses = []; //all analyses will be closed, so clear this array
	
	
	//disable Save link
	setSaveFilterLink('disable');
	
	jQuery("#search-ac").val("");
	
	activeKeywords = new Array();
	activeCategories = new Array();
	
	// Change the category picker back to ALL and set autocomplete to not have a category (ALL by default)
	document.getElementById("search-categories").selectedIndex = 0;
	jQuery('#search-ac').autocomplete('option', 'source', sourceURL);
		
	var tree = jQuery("#filter-div").dynatree("getTree");
	
	// Make sure the onSelect event doesn't fire for the nodes
	// Otherwise, the main search query is going to fire after each item is deselected, as well as facet query
	allowOnSelectEvent = false;
	tree.visit(function clearNode(node) {
										 updateNodeIndividualFacetCount(node, -1);
		                                 node.select(false);
	                                    }, 
	                                    false
	           )
	allowOnSelectEvent = true;

	showSearchTemplate();
	showSearchResults(); //reload the full search results
	
}

// this function removes or adds to the filter search term array based on whether or not a node in the tree is selected
function syncNode(node)  {
	var param = new Object();  
	var isCategory = node.data.isCategory;
	
	// don't sync category nodes (they can't be selected)
	if (isCategory) {
		return true
	}

	var categoryId = node.data.categoryId;
	var categoryDisplay = node.data.categoryDisplay;
	
	var outerNode = node;
	var inSearchTerms = false;
	
	// find all nodes that are copies of this node
	// if this or any of the copies of this node should be in search terms, then mark as being in search terms
	//  (this will prevent a later node in tree from removing from terms if an earlier node indicates it should be 
	//   in terms; this logic could be up for debate  - e.g. maybe it should never be in terms if any of the copies
	//         say it shouldn't be - but making consistent for now, can reverse logic easily if needed )
	node.tree.visit(
			          function checkCopies (node) {
			        	  if (outerNode.data.id == node.data.id)  {
			        		  // found a key that matches (i.e. is the original one or a copy)
        	        		  // a node will be in search terms if it is selected and it's parent is not
			        		  // or if it's selected and it's parent is a category
			        		  if (node.isSelected() && (!node.parent.isSelected() || node.parent.data.isCategory))  {
			        			  inSearchTerms = true;  
			        		  }
			        	  }
			          },
			          false
			       )
		
	param.categoryId = categoryId;       // category id        	
	param.categoryDisplay = categoryDisplay;     // category display        	
	param.categorySOLR = node.data.categorySOLR;       // category for SOLR field        	
	param.keyword = node.data.termName;  // term name
	param.id = node.data.id;             //keyword id
	if (inSearchTerms)  {
	    addKeyword(param, activeCategories, activeKeywords);
	}
	else {
		removeFilterTreeSearchTerm(node.data.id);
	}
	
}

// subtract node 2 from node 1;  return an array containing list nodes that are in node 1 but not node 2
function subtractNodes(nodes1, nodes2)  {

	var resultNodes = new Array();

	for (var i = 0; i < nodes1.length; i++) {  // loop thru nodes1
		  var n1 = nodes1[i];                
		  
		  var found = false;
		  
          for (var j = 0; j < nodes2.length; j++) {  // loop thru nodes2
  		      var n2 = nodes2[j];
  		      
  		      if (n2.data.uniqueTreeId == n1.data.uniqueTreeId)  {  // use uniqueTreeId to determine matches
  		    	  found = true;
  		    	  break;   // no need to continue with loop since we found match
  		      } 
          }
          
          // if we didn't find the node in nodes2, add to result array
          if (!found)  {
        	  resultNodes.push(n1);
          }        		  
	}
	return resultNodes;
}

jQuery.ui.dynatree.nodedatadefaults["icon"] = false;


jQuery(function(){
    jQuery("#filter-div").dynatree({
    	initAjax: {  url: treeURL,
    		data: { mode: "all" } 
    	},
    	checkbox: true,
    	persist: false,
    	selectMode: 3,
    	minExpandLevel: 1,
    	fx:{ height: "toggle", duration: 180 },
    	autoCollapse: true,
        onQuerySelect: function(flag, node) {   // event that is triggered prior to select actually happening on node
        	
        	if (!allowOnSelectEvent)  {
        		return true;
        	} 
        	
        	// before selecting node, save a copy of which nodes were selected
        	// (note that this only gets done when select is called outside of the onSelect event since we're using the global allowOnSelectEvent flag above) 
        	nodesBeforeSelect = node.tree.getSelectedNodes(false);

        },
        onSelect: function(flag, node) {

        	// don't allow this event to be triggered by itself; return immediately if called as a result of the event itself
        	if (!allowOnSelectEvent)  {
        		return true;
        	} 
        	else  {
        		allowOnSelectEvent = false;
        	}
        	
        	// before re-synchronizing tree, make sure any nodes that have same key as this one have been properly
        	// selected and deselected
            
        	var tree = node.tree;        	
            var selectNode = node;   // store the node that was selected so we can reference unambiguously in tree.visit function below 

        	// node is now selected, and any other changes to the tree have already happened (i.e. changes to children, parents,
            //   cousins, second cousins, ...) so retrieve a copy of which nodes are now selected
        	var nodesAfterSelect = node.tree.getSelectedNodes(false);
           
            // retrieve a list of those that are partially selected (e.g. no check box but a child or grandchild .. may be);
            var nodesPartiallySelected = new Array();
        	jQuery(".dynatree-partsel").each(
        			function(){
        		                  var node = jQuery.ui.dynatree.getNode(this);
        		                  
        		                  //  Selected nodes may also appear here - 
        		                  //   make sure only those that are not selected are actually included
        		                  //    in this list; 
        		                  //  And don't add category nodes either
        		                  if (!node.isSelected() && !node.data.isCategory)  {
        		                      nodesPartiallySelected.push(node);
        		                  }
        		              }
        			);
        	
        	
        	
        	// find nodes that are in After but were not in Before (i.e. Added)
        	var nodesAdded = subtractNodes(nodesAfterSelect, nodesBeforeSelect);

        	for (var i = 0; i < nodesAdded.length; i++) {
        		var n = nodesAdded[i];
        		// process node if it's not a category
        		if (!n.data.isCategory)  {
            		// loop through every node in tree and find copies, make sure all copies are selected        		
    	            n.tree.visit(  function (node) {
      	                              if ((n.data.id == node.data.id) && (n.data.uniqueTreeId != node.data.uniqueTreeId)) {
    	            	            	  node.select(true);
    	            	              } 
    	            	           } 
    	                         , false
    	            		     );
                }
        		
        		
        	}

        	// find nodes that are in Before but were not in After (i.e. Removed)
        	var nodesRemoved = subtractNodes(nodesBeforeSelect, nodesAfterSelect);
        	
        	// We need to remove partially selected nodes from removed list, since we don't want to call the select(false) method on these;
            //   if we did, then we would trigger all children to then be deselected in copies which isn't right;  instead the state of this
        	//   node will be controlled by actions on the children 
        	var nodesFullyRemoved = subtractNodes(nodesRemoved, nodesPartiallySelected);
        	
        	for (var i = 0; i < nodesFullyRemoved.length; i++) {
        		var n = nodesFullyRemoved[i];         		

        		// process node if it's not a category
        		if (!n.data.isCategory)  {
            		// loop through every node in tree and find copies, make sure all copies are DEselected
    	            n.tree.visit(  function (node) {
    	            	              if ((n.data.id == node.data.id) && (n.data.uniqueTreeId != node.data.uniqueTreeId)) {
    	            	            	  node.select(false);
    	            	              } 
    	            	           } 
    	                         , false
    	            		     );
                }
        		
        	}
        	
        	// reset flag to true now that we're past part that might trigger the event again	      
    		allowOnSelectEvent = true;

        	// Resynchronize entire tree when something changes
        	// We need to do this because a select may affect other nodes than the one selected,
        	//  but that doesn't trigger the onSelect event
        	// Following call executes the syncNode function on all nodes in tree, except for root
        	node.tree.visit(syncNode, false); 
        	showSearchTemplate();
        	showSearchResults();        	
        },
        onClick: function(node, event) {
        	// if the user clicked outside the node, but in the tree, don't select/unselect the node
        	// or if the node has a zero count and is not selected, don't allow it to be selected (but allow it to be expanded)       	
            if( (node.getEventTargetType(event) == null) ||             	 
           		(node.data.facetCount == 0 && !node.isSelected() && !(node.getEventTargetType(event) == 'expander'))
              )
            {
                return false;// Prevent default processing
            }
            return true;
        },
        onActivate: function(node){
	    	if(!node.data.isCategory){
	    		if(!node.isSelected()){
	    			node.select(true);
	    		}
	    		else{
	    			node.select(false);
	    		}
	    	
	    	}
	    	
	    	node.deactivate();
    	},
    	onCustomRender: function(node) {
    		// if not a category and count is zero, apply the custom class to node
    		if (!node.data.isCategory && node.data.facetCount == 0)  {
    			node.data.addClass = "zero-selected";
    		}
    		else
    	    {
    			node.data.addClass = null;
    	    }
    		
    		// if the string doesn't already have a break tag, add one after the 30th character
    		if (node.data.title.length > 30 && node.data.title.indexOf('<br />') == -1)  {
    			// find the first space character starting at 30th character
    			var spacePos = node.data.title.indexOf(' ', 30);
    		    		
    			if (spacePos > -1)  {
        			node.data.title = node.data.title.substr(0, spacePos) + '<br />' + node.data.title.substr(spacePos + 1);      				
    			} 
    		} 
    	}
    });
});



// find the analysis in the array with the given id
function getAnalysisIndex(id)  {
	for (var i = 0; i < analysisProbeIds.length; i++)  {
		if (analysisProbeIds[i].analysisId == id)  {
			return i;
		}
	}
	
    return -1;  // analysis not found		
}

//find the analysis in the sa array with the given id
function getAnalysisIndexSA(id)  {
	for (var i = 0; i < analysisProbeIdsSA.length; i++)  {
		if (analysisProbeIdsSA[i].analysisId == id)  {
			return i;
		}
	}
	
    return -1;  // analysis not found		
}

// compare the contents of one array of keyword objects with another; if same, return true
function compareKeywordArrays(arr1, arr2)  {
	if (arr1.length != arr2.length)  {
		// lengths don't match
		return false;
	}
	
	for (var i = 0; i < arr1.length; i++)  {
		if (arr1[i].keywordId != arr2[i].keywordId)  {
			// one of the keywords doesn't match
			return false;
		}
	}
	
    return true;  		
}

//find the keyword in the array with the given keyword id and return its index
function getKeywordIndex(keywordId, keywords)  {
	for (var i = 0; i < keywords.length; i++)  {
		if (keywords[i].keywordId == keywordId)  {
			return i;
		}
	}
	
    return null;  // keyword not found		
}

//find the keyword in the array with the given keyword id and return the object
function getKeyword(keywordId, keywords)  {
	var i = getKeywordIndex(keywordId, keywords);
	
	if (i != null)  {
		return keywords[i];
	}
	else  {
		return null;  // keyword not found
	}
}

//find the keyword in the array with the given anchor id return its index in array
function getKeywordByRemoveAnchorId(anchorId, keywords)  {
	for (var i = 0; i < keywords.length; i++)  {
		if (keywords[i].removeAnchorId == anchorId )  {
			return i;
		}
	}
	
    return null;  // keyword not found		
}

//find the category in the array with the given category id and return the object
function getCategory(categoryId, categories)  {

	var i = getCategoryIndex(categoryId, categories);
	
	if (i != null)  {
		return categories[i];
	}
	else  {
		return null;  // category not found
	}
}

//find the category in the array with the given category id and return its index
function getCategoryIndex(categoryId, categories)  {
	for (var i = 0; i < categories.length; i++)  {
		if (categories[i].categoryId == categoryId)  {
			return i;
		}
	}
	
    return null;  // category not found		
}

//remove an element from an array by value, keeping all others in place
function removeByValue(arr, val) {
	for(var i=0; i<arr.length; i++) {
		if(arr[i] == val) {
			arr.splice(i, 1);
			break;
		}
	}
}



function getHeatmapPaginator(divID, analysisId, analysisIndex, maxProbeIndex, isSA, keywordQueryString) {
    if (isSA)  {
    	numberOfProbesPerPage = 20;
    }
    else  {    	
    	probesPerPageElement = document.getElementById("probesPerPage_" + analysisId);
    	numberOfProbesPerPage = probesPerPageElement.options[probesPerPageElement.selectedIndex].value;
    }
	
	// get number of extra probes on last page (will be 0 if last page is full)
	var numberProbesLastPage = maxProbeIndex % numberOfProbesPerPage;
	
	// find number of full pages
	var numberOfFullPages = (maxProbeIndex - numberProbesLastPage) / numberOfProbesPerPage;
	
	// find number of pages - equal to number of full pages if none left over after full pages
	var numberOfPages = numberOfFullPages;        	        	
	if (numberProbesLastPage > 0)  {
		numberOfPages = numberOfPages + 1;
	}
	
	var saPrefix ='';
	if (isSA)  {
		saPrefix ='sa';
	}
	
	//if there is only 1 page, just hide the paging control since it's not needed
	if(numberOfPages==1){
		jQuery("#" + saPrefix + "pagination_" + analysisId).hide();
	}
	else  {
		jQuery("#" + saPrefix + "pagination_" + analysisId).show();
	}
	        	        	
	// the probeIds list and selectList are initially null; will be populated when we load the heat map data
	var analysisObject = {analysisId:analysisId, probeIds:null, selectList:null, maxProbeIndex:maxProbeIndex};
		
	// either replace current object, or add new one if not in array yet
	if (analysisIndex == -1)  {
		if (isSA)  {
	        analysisProbeIdsSA.push(analysisObject);
		}
		else  {			
	        analysisProbeIds.push(analysisObject);
		}
    } else
    {	
		if (isSA)  {
	        analysisProbeIdsSA[analysisIndex] = analysisObject;
		}
		else  {			
	        analysisProbeIds[analysisIndex] = analysisObject;
		}
    }

	jQuery("#" + saPrefix + "pagination_" + analysisId).paging(numberOfPages, { 
	    perpage:1, 
        format:"[<(qq -) ncnnn (- pp)>]",
        onSelect: function (page) { 
        	
        	if (isSA)  {
        		jQuery("#analysis_holderSA_" + analysisId).mask("Loading...");
        	}
        	else  {        		
            	jQuery("#analysis_holder_" + analysisId).mask("Loading...");
        	}

            if (isSA)  {
            	numberOfProbesPerPage = 20;
            }
            else  {
                // make sure we are getting number of probes per page for current element
                var probesPerPageElement = document.getElementById("probesPerPage_" + analysisId);
            	var numberOfProbesPerPage = probesPerPageElement.options[probesPerPageElement.selectedIndex].value;            	
            }
            
        	loadHeatmapData(divID, analysisId, page, numberOfProbesPerPage, isSA, keywordQueryString);

        	if (!isSA)  {
        		jQuery('body').data("currentPage:" + analysisId, page);	
        	}        	
                                    
        }, 
        onFormat: formatPaginator
    }); 	
	
}


//Open and close the SA heatmap for a given analysis
function toggleHeatmapSA(analysisId, page)	{	
	
	var expanded = jQuery("#selectedAnalysis_" + analysisId).data("expanded");

	var imgExpand = "#saimgExpand_"  + analysisId;

	// when not expanded, has a down arrow
	// when expanded has an up arrow
	// replace with the other on toggle
    if (expanded)  {
    	// unexpand
    	jQuery(imgExpand).attr('src', './../images/down_arrow_small2.png');
    	jQuery("#analysis_holderSA_" + analysisId).slideUp(200);
    	jQuery("#selectedAnalysis_" + analysisId).data("expanded", false);
    	
    	jQuery("#selectedAnalysis_" + analysisId).removeClass("SA-item-expanded");
    }
    else  {
    	// expand
    	jQuery(imgExpand).attr('src', './../images/up_arrow_small2.png');
    	jQuery("#analysis_holderSA_" + analysisId).slideDown(200);
    	jQuery("#selectedAnalysis_" + analysisId).data("expanded", true);
    	
    	jQuery("#selectedAnalysis_" + analysisId).addClass("SA-item-expanded");
    	
    	var hmLoaded = jQuery("#selectedAnalysis_" + analysisId).data("hmLoaded");
    	
    	// if we don't have a heatmap laoded yet, then load one
    	if (!hmLoaded)  {
    		loadHeatmapSA(analysisId, page);
    		jQuery("#selectedAnalysis_" + analysisId).data("hmLoaded", true);
    	}
    	
    }
	return false;
}


// load the Selected Analyses heatmap (the one that shows underneath the analysis on the CTA page)
function loadHeatmapSA(analysisId, page) {
	// generate a keyword list that can be consumed by the server; needs to be in form:
	// GENELIST:1|2|3&GENESIG:4|5&PATHWAY:6|7&GENE:8|9&PROTEIN:10|11

	// retrieve the current state of the checkbox
	var cbState =  jQuery("input[name=showGenes_" + analysisId + "]:checked").val();
	
	// retrieve the old value of the checkbox
	var oldState = jQuery("#selectedAnalysis_" + analysisId).data("cbState");

	var hmLoaded = jQuery("#selectedAnalysis_" + analysisId).data("hmLoaded");
	
	// nothing changed and a heatmap is loaded, don't load anything
    if (oldState == cbState && hmLoaded)  {
    	return false;
    }
    else {
		
		var queryString;
		var divID = 'heatmapSA_' + analysisId;
		
    	if (cbState == 'ALLSIG')  {
    		// load with no keyword filters
    		queryString = '';
    	}
    	else  {
			var params = new Array
			var geneList = new Array
			var geneSig = new Array
			var pathway = new Array
			var gene = new Array
			var protein = new Array
			
			// loop through each xt keyword and add to the appropriate array based on its category
			for (var i=0; i<xtSelectedKeywords.length; i++)  {
				var kw = xtSelectedKeywords[i];
				switch (kw.categoryId)
				{ 
					case 'GENELIST':  geneList.push(kw.id);  break;
					case 'GENESIG':   geneSig.push(kw.id); break;
					case 'PATHWAY':   pathway.push(kw.id); break;
					case 'GENE':      gene.push(kw.id); break;
					case 'PROTEIN':   protein.push(kw.id); break;
				    default: alert('Invalid category: ' + kw.categoryId); return false;
				}				
			}
			
			// if a category has items, join it's items with a pipe and add to params array
			if (geneList.length>0)  {params.push('GENELIST:' + geneList.join('|'))}
			if (geneSig.length>0)  	{params.push('GENESIG:' + geneSig.join('|'))}
			if (pathway.length>0)  	{params.push('PATHWAY:' + pathway.join('|'))}
			if (gene.length>0)  	{params.push('GENE:' + gene.join('|'))}
			if (protein.length>0)  	{params.push('PROTEIN:' + protein.join('|'))}
			
			queryString = params.join('&');
    	}
    	
    	// save the new state of checkbox
    	jQuery("#selectedAnalysis_" + analysisId).data("cbState", cbState);
 
    	jQuery("#analysis_holderSA_" + analysisId).mask('Loading...');
		loadHeatmapPaginator(divID, analysisId, page, true, queryString);
    }
}


// keywords only necessary for cta version of this heatmap (session variables generated during facet results are used for standard heatmap)
function loadHeatmapPaginator(divID, analysisId, page, isSA, keywordsQueryString) {

	var analysisIndex;
	
	if (isSA)  {		
		analysisIndex = getAnalysisIndexSA(analysisId);
	}
	else  {		
		analysisIndex = getAnalysisIndex(analysisId);
	}
		
	rwgAJAXManager.add({
		url:getHeatmapNumberProbesURL,		
		data: {id: analysisId, page:page, isSA:isSA, keywordsQueryString:keywordsQueryString},
		success: function(response) {								
			var maxProbeIndex = response['maxProbeIndex']
			
			var errorMsg = response['errorMsg'];
			
			if (errorMsg != '')  {
				alert(errorMsg);
			}
			
			if (maxProbeIndex == 0)  {
				jQuery("#" + divID).html('No Data');
		    	jQuery("#analysis_holderSA_" + analysisId).unmask();

			}
			else {
				getHeatmapPaginator(divID, analysisId, analysisIndex, maxProbeIndex, isSA, keywordsQueryString);
			}
	
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}


function showCrossTrialAnalysis()
{
	
	menuIconSwap("imgCTA");
	
	//reset scroll position
	jQuery("#main").scrollTop(0);
	
	//hide the unused menu options
	jQuery("#toolbar-collapse_all").hide();
	jQuery("#toolbar-options").hide();
	
	  hideHomePage();
      hideResultsPage();
	  jQuery('#cross-trial-div').show();
	  
	  //if no analyses are selected, disable everything
	  if(selectedAnalyses.length ==0){
		  
		  jQuery('#xtAnalysisList').hide();
		  jQuery('#xtMenuBar').hide();
		  jQuery('#xtSearch-ac').prop('disabled', true);
		  
		  jQuery('#xtNoAnalysesMsg').show();
	  
	  }else{//otherwise, show everything
		  
		  jQuery('#xtAnalysisList').show();
		  jQuery('#xtMenuBar').show();
		  jQuery('#xtSearch-ac').prop('disabled', false);
		  
		  jQuery('#xtNoAnalysesMsg').hide();
	  }
	  
}


function loadCrossTrialAnalysisInitial(){
    rwgAJAXManager.add({
		url:crossTrialAnalysisURL,				
		timeout:60000,
		success: function(response) {
		  
		  jQuery('#cross-trial-div').html(response);
		  
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}


function launchHomePage(currentsubcategoryid, currentcharttype, showAll)
{
      
      rwgAJAXManager.add({
  		url:homeURL,						
  		data: {currentsubcategoryid: currentsubcategoryid, currentcharttype:currentcharttype, showAll:showAll},
  		timeout:60000,
  		success: function(response) {
  			//jQuery(div).empty();
  		  jQuery('#home-div').html(response);
  		  showHomePage();
  	      hideResultsPage();
  	      hideCrossTrialAnalysis();
  		}
  	});
      
    
}
function showHomePage()
{
	menuIconSwap("imgHome");
	
	//reset scroll position
	jQuery("#main").scrollTop(0);
	
	//hide the unused menu options
	jQuery("#toolbar-collapse_all").hide();
	jQuery("#toolbar-options").hide();
	
      jQuery('#home-div').show();
      hideCrossTrialAnalysis();
      hideResultsPage();
}
function showResultsPage()
{
	//replace image
	menuIconSwap("imgResults");
	
	//reset scroll position
	jQuery("#main").scrollTop(0);
	
	//show the extra menu options
	jQuery("#toolbar-collapse_all").show();
	jQuery("#toolbar-options").show();
	jQuery('#results-div').show();
	 hideCrossTrialAnalysis();
	 hideHomePage();
}
function hideResultsPage()
{
	jQuery('#results-div').hide();
}
function hideHomePage()
{
      jQuery('#home-div').hide();      
}
function hideCrossTrialAnalysis(){
	jQuery('#cross-trial-div').hide();
}

//swap out the black and white icon with the color icon
//reset all other icons to b/w
function menuIconSwap(current){
	//current options: imgHome imgResults imgCTA
	
	//reset all icons to b/w version
	var src = jQuery("#imgHome").attr('src').replace('menu_icon_home.png', 'menu_icon_home_bw.png');	
	jQuery("#imgHome").attr('src',src);
	
	var src = jQuery("#imgResults").attr('src').replace('menu_icon_search.png', 'menu_icon_search_bw.png');	
	jQuery("#imgResults").attr('src',src);
	
	var src = jQuery("#imgCTA").attr('src').replace('menu_icon_crosstrial.png', 'menu_icon_crosstrial_bw.png');	
	jQuery("#imgCTA").attr('src',src);
	
	//set the current one to the color version
	switch(current)
	{
	case "imgHome":
		var src = jQuery("#imgHome").attr('src').replace('menu_icon_home_bw.png', 'menu_icon_home.png');	
		jQuery("#imgHome").attr('src',src);
		break;
	case "imgResults":
		var src = jQuery("#imgResults").attr('src').replace('menu_icon_search_bw.png', 'menu_icon_search.png');	
		jQuery("#imgResults").attr('src',src);
		break;
	  break;
	case "imgCTA":
		var src = jQuery("#imgCTA").attr('src').replace('menu_icon_crosstrial_bw.png', 'menu_icon_crosstrial.png');	
		jQuery("#imgCTA").attr('src',src);
		break;
	}
	
	
}

function getPieChartData(divid, catid, ddid, drillback, charttype, parentcolor, ddstack)
{
	rwgAJAXManager.add({
		url:getPieChartDataURL,									
		data: {catid: catid, ddid: ddid, drillback: drillback, charttype: charttype},
		timeout:60000,
		success: function(response) {
			jQuery("#"+divid).empty();
			drawPieChart(divid, catid, response.ddid, response.data, charttype, parentcolor, ddstack);
		}
	});
}


function displaySelectedAnalysisTopGenes(){
	
	for (var i =0; i < selectedAnalyses.length; i++){
		
		getTopGenes(selectedAnalyses[i].id);
		
	}
}




function getCrossTrialSummaryTableStats()
{
	 jQuery('#xtSummaryTable').html('');
	
	 jQuery('#xtSummaryTable').mask('Loading...');
	
	if (selectedAnalyses.length == 0)  {
		return;
	}

	var analysisList = '';
	
	//Convert the selected analysis array into a list
	for (var i =0; i < selectedAnalyses.length; i++){

		analysisList += selectedAnalyses[i].id;
		
		if (selectedAnalyses.length-1 > i){
			analysisList += ',';
		}
		
	}

	rwgAJAXManager.add({
		url:getCrossTrialSummaryTableStatsURL,
		data: {analysisList: analysisList},
		timeout:60000,
		success: function(data) {
			
			//alert(response[key]['bio_marker_id']);
			 var tbl_body = "<div><table style='width:500px' id='CTAsummaryTable' class='CTAtable'>";
			 tbl_body+="<tr><th>Analysis ID</th><th>Genes Up Regulated</th><th>Genes Down Regulated</th><th>Total Genes</th></tr>";
			
			 jQuery.each(data, function() {
			        var tbl_row = "";
			        jQuery.each(this, function(k , v) {
			            tbl_row += "<td>"+v+"</td>";
			        })
			        tbl_body += "<tr>"+tbl_row+"</tr>";                 
			    })
			    tbl_body += '</table></div>';
			 
			    jQuery('#xtSummaryTable').html(tbl_body);
			    
			    //alternate colors
			    jQuery('#CTAsummaryTable').find('tr:even').css({'background-color':'#efefef'})
	              .end().find('tr:odd').css({'background-color':'#fff'});
			    
			    jQuery('#xtSummaryTable').unmask();
		   
		}
	});
	
}


function getTopGenes(analysisID)
{

	rwgAJAXManager.add({
		url:getTopGenesURL,
		data: {analysisID: analysisID},
		timeout:60000,
		success: function(data) {
			
			//alert(response[key]['bio_marker_id']);
			 var tbl_body = "<div><table style='width:230px'>";
			 jQuery.each(data, function() {
			        var tbl_row = "";
			        jQuery.each(this, function(k , v) {
			            tbl_row += "<td>"+v+"</td>";
			        })
			        tbl_body += "<tr>"+tbl_row+"</tr>";                 
			    })
			    tbl_body += '</table></div>';
			    jQuery('#xtTopGenes').after(tbl_body);
		   
		}
	});
	
}


function updateCrossTrialGeneCharts(){
	
	//update the table
	getCrossTrialSummaryTableStats()
	
	jQuery('#xtMsgBox').fadeOut(200);
	
	//unmaks the tabs
	jQuery('#xtMenuBar').unmask()
	
	//clear gene charts
	jQuery('#xtSummaryChartArea').html('');
	
	//cleart the heatmaps
	jQuery('#xtHeatmapTab').html('');
	
	jQuery(xtSelectedKeywords).each(function (index, value){
		
		var categoryId = xtSelectedKeywords[index].categoryId;
		var keywordId = xtSelectedKeywords[index].id;
		var searchTerm = xtSelectedKeywords[index].termName;

		switch (categoryId)  {
			case "GENE": 
			case "PROTEIN":
				getCrossTrialGeneSummary(keywordId);
				break;
			case "GENELIST": 
			case "GENESIG": 
			case "PATHWAY":
				loadHeatmapCTAPaginator(categoryId, keywordId, 1, searchTerm);
				break;
			default:  
				alert("Invalid category!");
		}
				
	});
	setSaveXTFilterLink();
	setClearXTLink();
	//update the display list
	displayxtAnalysesList();

}

function closeCTAheatmap(divID, geneID){
	
	jQuery('#' +divID).fadeOut(200, function() { 
		jQuery('#' +divID).remove(); 
		
	});
	
	//loop through the array, find the gene ID to be removed, and remove it from the array
	for (var i =0; i < xtSelectedKeywords.length; i++){
	   if (xtSelectedKeywords[i].id == geneID) {
		   xtSelectedKeywords.splice(i,1);
	   }
	}
	
	setSaveXTFilterLink();
	setClearXTLink();	


	if(xtSelectedKeywords.length==0){
		jQuery('#xtNoHeatmapsMsg').fadeIn(200);
	}
		
}



function closeXTGeneChart(divID, geneID){
	
	jQuery('#' +divID).fadeOut(200, function() { 
		jQuery('#' +divID).remove(); 
		
	});
	
	//loop through the array, find the gene ID to be removed, and remove it from the array
	for (var i =0; i < xtSelectedKeywords.length; i++){
	   if (xtSelectedKeywords[i].id == geneID) {
		   xtSelectedKeywords.splice(i,1);
	      break;
	   }
	}

	setSaveXTFilterLink();
	setClearXTLink();		
	
	if(xtSelectedKeywords.length==0){
		jQuery('#xtNoGenesMsg').fadeIn(200);
	}
	
	
}

function clearAllXTSearchTerms(){
	
	//Clear the html div of existing charts
	jQuery('#xtSummaryChartArea').html('');
	
	//Clear the heatmaps
	jQuery('#xtHeatmapTab').html('');
	
	//reset the array
	xtSelectedKeywords = [];
	
	//remove the message box
	jQuery('#xtMsgBox').fadeOut(200);
	
	//remove the mask
	jQuery('#xtMenuBar').unmask()
	
	//show the empty gene msg box
	jQuery('#xtNoGenesMsg').show();
	
	setClearXTLink();
	setSaveXTFilterLink();

	
}


//used on the cross trial analysis page to display the summary
function displayxtAnalysesList(){
	
	
	var html = "<div id='xtSelectedAnalysesListLegend'>";
	
	selectedAnalyses.sort(dynamicSort("studyID"));
	
	jQuery(selectedAnalyses).each(function(index, value){
		
		var num = parseInt(index) + 1;

		html = html + "<div class='xtSelectedAnalysesListLegendItem' onclick='toggleHeatmapSA(" + selectedAnalyses[index].id + ", 1);' id='selectedAnalysis_"+selectedAnalyses[index].id +"'>";
		html = html + "<table><tr><td class='analysisNum'>" +num  +"</td><td style='padding-left:4px'><span style='padding-left:0' class='result-trial-name'>"+ selectedAnalyses[index].studyID +'</span>: ' +selectedAnalyses[index].title.replace(/_/g, ', ');
		html = html + "</td><td style='text-align:right'><img alt='expand/collapse' id='saimgExpand_" + selectedAnalyses[index].id + "' src='./../images/down_arrow_small2.png' style='padding-left:10px; padding-right:10px;'/></td></tr>";
		html = html + '</table></div>';
		html = html + "<div id='analysis_holderSA_" + selectedAnalyses[index].id + "' class='xtSAHeatmapHolder'>"; 
		html = html + "<div class='legend' id='saheatmapLegend_" + selectedAnalyses[index].id + "'></div>";
		html = html + "<div class='heatmap-SA-Holder' id='heatmapSA_" + selectedAnalyses[index].id + "'></div>";
		html = html + "<div class='pagination' id='sapagination_" + selectedAnalyses[index].id + "'></div>";
		
		var allSigChecked = '';
		var selectedChecked = '';
		var selectedDisabled = '';
		if (xtSelectedKeywords.length > 0)  {
			selectedChecked = 'checked';
		}
		else  {
			allSigChecked = 'checked';
			selectedDisabled = 'disabled'
		}
		
		var onclick = "loadHeatmapSA(" +  selectedAnalyses[index].id + ", 1);" 
		html = html + "<input onclick='" + onclick + "' type='radio' name='showGenes_" + selectedAnalyses[index].id + "' value='ALLSIG' " + allSigChecked + " /> Show All Significant Genes<br>"
		html = html + "<input onclick='" + onclick + "' type='radio' name='showGenes_" + selectedAnalyses[index].id + "' value='SELECTED' " + selectedChecked + " " + selectedDisabled + " /> Show Selected Genes<br>"
		
		html = html + "</div>";
	});
	
	html = html + '</div>';
	
	jQuery('#xtSummary_AnalysesList').html(html);

	// store the state of each div 
	jQuery(selectedAnalyses).each(function(index, value){
		jQuery("#selectedAnalysis_" + selectedAnalyses[index].id).data("expanded", false);
		jQuery("#selectedAnalysis_" + selectedAnalyses[index].id).data("hmLoaded", false);
		
		// retrieve the current value of check box
		var cbState =  jQuery("input[name=showGenes_" + selectedAnalyses[index].id + "]:checked").val();
		
		// save the state of cb
		jQuery("#selectedAnalysis_" + selectedAnalyses[index].id).data("cbState", cbState);
	});
	
	
}




function createCrossTrialSummaryChart(data, pdata, keyword_id, placeholder){
	
	//use jsfiddle to test: http://jsfiddle.net/HZ9dg/6/
	
	var keywordObject = xtSelectedKeywords.filter(function(el){return el.id == keyword_id});
	
	var geneName = keywordObject[0].termName;
	var geneID = keywordObject[0].id;
	var divID = "xtSummaryChart_" +geneID;
	
	//html for button to close the graph
	var closeHTML = "<a href='#' class='xtChartClostbtn' id='" +divID  +"_CloseBtn' onclick=\"closeXTGeneChart('" +divID +"'," +geneID +")\">x</a>";
	
	var openBoxplotLinkHTML = "<a href='#' class='xtBoxplotbtn' id='" +divID  +"_BoxplotBtn' onclick=\"openXtBoxplot('"+keyword_id +"', '" +geneName +"')\">view boxplot</a>";

    var margin = {top: 30, right: 40, bottom: 10, left: 50},
        bar_width = 30,
        width = ((data.length) * (bar_width + 10)),
        height = 150- margin.top - margin.bottom;

    var y0 = Math.max(Math.max(-d3.min(data), d3.max(data)),3);
    var y2max = Math.max(d3.max(pdata),3);

    //ensure the y0 and y2max both have valid values (this is to correct in cases where the dataset is all null)
    if(!y0>0){ y0=3;}
    if(!y2max>0){ y2max=3;}
    
    var y = d3.scale.linear()
        .domain([-y0, y0])
        .range([height,0])
        .nice();

    var y2 = d3.scale.linear()
        .domain([0, y2max])
        .range([height/2,0])
        .nice();


    var yAxis = d3.svg.axis()
        .scale(y)
        .ticks(5)
        .orient("left");

    var yAxis2 = d3.svg.axis()
        .scale(y2)
        .ticks(4)	
        .orient("right");
    
    
    //remove the div if it already exists:
    jQuery('#'+divID).remove();
    
    //create div to hold svg chart
    jQuery("#xtSummaryChartArea").prepend("<div id='"+divID +"' class='xtSummaryChart'></div>");
    
    
    //only show the close btn on hover
    jQuery('#'+divID).hover(
    		  function () {
    			 jQuery('#' +divID  +'_CloseBtn').show();
    			 jQuery('#' +divID  +'_BoxplotBtn').show();
    		  },
    		  function () {
    			 jQuery('#' +divID  +'_CloseBtn').hide();
    			 jQuery('#' +divID  +'_BoxplotBtn').hide();
    		  }
    		);
    
    var svg = d3.select('#'+divID).append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .attr("class", 'xtSVGSummaryChart')
      .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

    svg.selectAll(".bar")
        .data(data)
      .enter().append("rect")
        .attr("class", function(d) { return d < 0 ? "bar negative" : "bar positive"; })
                        //5 is the left padding, 10 is the padding between each bar
        .attr("x", function(d,i) { return 5 + i * (bar_width+10); })
        .attr("y", function(d,i) { return y(Math.max(0, d)); })
        .attr("height", function(d) { return Math.abs(y(0) - y(0-d));})
        .attr("width", bar_width);


    svg.selectAll(".bar2")
        .data(pdata)
        .enter().append("rect")
       // .attr("class", function(d) { return d < 0 ? "bar negative" : "bar positive"; })
        .attr("x", function(d,i) { return bar_width/2 +2 + i * (bar_width+10); })
        .attr("y", function(d,i) { return y2(Math.max(0, d)); })
        .attr("height", function(d) { return Math.abs(y2(0) - y2(0-d)); })
        .attr("width", 4);

    //line to show the p-value < 0.05 cut-off
    svg.append("svg:line")
    .attr("x1", 10)
    .attr("y1", function() { return y2(1.3); })
    .attr("x2", width)
    .attr("y2", function() { return y2(1.3); })
    .attr("class", 'pvalue-cutoff-line');

svg.selectAll("text")
       .data(data)
       .enter()
       .append("text")
       .text(function(d,i) {return i+1;})
       .attr("x", function(d,i) { return bar_width/2 +2 + i * (bar_width+10); })
      // .attr("y", function(d,i) { return y(Math.max(0, d)); })
      // .attr("y", function(d) { return d < 0 ? height/2-5 : height/2+10; })
       .attr("y", function(d) { return height +10 })
       .attr("font-family", "sans-serif")
       .attr("font-size", "10px")
       .attr("fill", "black")
       .attr("class", "geneChartTooltip")
	   .attr("id", function(d, i) {
		    	var id = "gcTitle" + uniqueGeneChartId++;   // id here will match id in tooltip array
				var tooltip = 
		    				   "<table>" +
		    				   "<tr><td width='100px'><b>Index</b></td><td>" + (i + 1) + "</td></tr>" +
		    				   "<tr><td><b>Study</b></td><td>" + selectedAnalyses[i].studyID + "</td></tr>" +
		    				   "<tr><td ><b>Analysis</b></td><td>" + selectedAnalyses[i].title + "</td></tr>" 
		    				   "</table>";
		    	
		    	gcTooltips[id] = tooltip;
		    	return id;
	  })		
    ;

	svg.append("line")
		.attr("x1", 50)
		.attr("y1", function() { return  0})
		.attr("x2", 100)
		.attr("y2", function() { return  2 });


        // add Title
        svg.append("svg:text")
          .attr("x", 8)
          .attr("y", -5)
          .attr("class","xtBarPlotGeneTitle")
          .text(geneName);

        // y1 legend
        svg.append("text")
            .attr("class", "y-label")
            .attr("text-anchor", "end")
            .attr("y", -30)
            .attr("x",-height/4)
            .attr("transform", "rotate(-90)")
            .text("fold change");
        // y2 legend
        svg.append("text")
            .attr("class", "y-label")
            .attr("text-anchor", "end")
            .attr("y", width+35)
            .attr("x", 0)
            .attr("transform", "rotate(-90)")
            .text("-log10(p-value)");


    svg.append("g")
        .attr("class", "y axis")
        .call(yAxis);

    svg.append("g")
        .attr("class", "y axis")
        .attr("transform", "translate(" +(width) +",0)")
        .call(yAxis2);

    svg.append("g")
        .attr("class", "x axis")
      .append("line")
        .attr("y1", y(0))
        .attr("y2", y(0))
        .attr("x1", 0)
        .attr("x2", width);
    
    
    //add some buttons
    jQuery('#'+divID).append(closeHTML +"<span style='display:block'>"+openBoxplotLinkHTML+"</span>");
    
    //if placeholder is true, then we are drawing the graph with no data simply to use as a placeholder
    if(placeholder){
    	jQuery('#'+divID).mask("Loading...");//add loading screen
    }else
	{
    	jQuery('#'+divID).unmask();//remove loading screen
	}
    
    registerGeneChartTooltipEvents();
   
}

this.registerGeneChartTooltipEvents = function(){
	// create the method for the hover event for tooltips on the favorites for faceted searches
	jQuery(".geneChartTooltip").hoverIntent(
		{
			over:function(e){
				//var elementId = e.currentTarget.id;
				showGeneChartTooltip(e);
			},
			out: function(){
				jQuery("#geneChartTooltip").remove();
			},
			interval:200
		});
	
};

function showGeneChartTooltip(e)  {

	var xOffset = 20;
	var yOffset = 20;		
	
	// create the div tag which will hold tooltip
	jQuery("body").append("<div id='geneChartTooltip'></div>");
	
	var tooltip = gcTooltips[e.currentTarget.id];
	
	jQuery("#geneChartTooltip")
		.css("z-index", 10000)
		.html(tooltip)
		.css("left",(e.pageX + yOffset) + "px")
		.css("top",(e.pageY - xOffset) + "px")
		.fadeIn(200)
		;
	
}


/*
jQuery.ui.dialog.prototype._makeDraggable = function() { 
    this.uiDialog.draggable({
        containment: false
    });
};*/

function getCrossTrialGeneSummary(search_keyword_id)
{
	
	var foldchangeDataset = [];
	var pvalueDataset =[];
	var analysisList = '';
	
	//hide the "empty" gene message
	
	jQuery('#xtNoGenesMsg').hide();
	
	
	//Convert the selected analysis array into a list
	for (var i =0; i < selectedAnalyses.length; i++){
		
		//add place holder data so that when the chart is drawn without data, it is the correct size
		foldchangeDataset.push('');
		pvalueDataset.push('');
		
		analysisList += selectedAnalyses[i].id;
		
		if (selectedAnalyses.length-1 > i){
			analysisList += ',';
		}
		
	}
	
	//do this first, without data, to create loading place holder while getting data
	createCrossTrialSummaryChart(foldchangeDataset,pvalueDataset, search_keyword_id, true);

	rwgAJAXManager.add({
		url:getCrossTrialBioMarkerSummaryURL,
		data: {analysisList: analysisList, search_keyword:search_keyword_id },
		timeout:60000,
		success: function(data) {
			
			//store the response
			jQuery('body').data("xtBioMarkerSummaryData:" + search_keyword_id, data); 
			
			
			//prepare the data into arrays before creating chart
			jQuery(selectedAnalyses).each(function(index, value){
				
				var result = data.filter(function(el){return el.bio_assay_analysis_id == selectedAnalyses[index].id});
				var fold_change_ratio;
				var preferred_pvalue;
				
				if(result[0] == null){
					fold_change_ratio = '';
					foldchangeDataset[index] = fold_change_ratio;
				}
				 else {
					 fold_change_ratio = result[0].fold_change_ratio;
					 foldchangeDataset[index] = fold_change_ratio;
				 }
				
				if(result[0] == null){
					preferred_pvalue = '';
					pvalueDataset[index]=preferred_pvalue;
				}
				 else {
					 preferred_pvalue = result[0].preferred_pvalue;
					 if(preferred_pvalue<.00001){preferred_pvalue=0.00001};
					 pvalueDataset[index]= -1 * (Math.log(preferred_pvalue) / Math.log(10)); //calculate the -log10(p-value)
				 }
				
			});
			

			//Draw chart
			createCrossTrialSummaryChart(foldchangeDataset,pvalueDataset, search_keyword_id, false);
			
		   
		}
	});
	
}

function addXTSelectedKeyword(keywordId, searchTerm, categoryId) {	
	// first check if the keyword id is already on array
	var found = false;
	for (var i =0; i < xtSelectedKeywords.length; i++)  {
	   if (xtSelectedKeywords[i].id == keywordId) {
			  found = true;
		      break;
	   }
	}	 
	if (!found)  {		
		xtSelectedKeywords.push({id: keywordId, termName: searchTerm, categoryId: categoryId });
	}
	
}

function addXTSearchAutoComplete()	{
	jQuery("#xtSearch-ac").autocomplete({
		source: searchAutoCompleteCTAURL,
		minLength:0,
		select: function(event, ui) {  

			//TODO: Determine if the result is a single gene or a pathway/gene signature/etc.
			//will display different results depending on what was selected
						
			
			var keywordId = ui.item.id;
			var searchTerm = ui.item.label;
			var categoryId = ui.item.categoryId;
						
			switch (categoryId)  {
				case "GENE": 
				case "PROTEIN":
					addXTSelectedKeyword(keywordId, searchTerm, categoryId);
					getCrossTrialGeneSummary(keywordId);
					
					jQuery('#xtMenuBar').tabs('select', 'xtGeneChartTab'); // switch to chart tab

					
					break;
				case "GENELIST": 
				case "GENESIG": 
				case "PATHWAY":
					loadHeatmapCTAPaginator(categoryId, keywordId, 1, searchTerm);
					jQuery('#xtMenuBar').tabs('select', 'xtHeatmapTab'); // switch to heatmap tab
					addXTSelectedKeyword(keywordId, searchTerm, categoryId);
					
					break;
				default:  
					alert("Invalid category!");
			}
			setSaveXTFilterLink();
			setClearXTLink();
			
			displayxtAnalysesList();
			
			// clear the search text box
			jQuery("#xtSearch-ac").val("");
			
			
			return false;
		}
	}).data("autocomplete")._renderItem = function( ul, item ) {
		return jQuery('<li></li>')		
		  .data("item.autocomplete", item )
		  .append('<a><span class="category-' + item.category.toLowerCase() + '">' + item.category + '&gt;</span>&nbsp;<b>' + item.label + '</b>&nbsp;' + item.synonyms + '</a>')
		  .appendTo(ul);
	};	
		
	return false;
}

function openGeneFromCTAheatmap(keywordId, termName, categoryId){
	addXTSelectedKeyword(keywordId, termName, categoryId);
	getCrossTrialGeneSummary(keywordId);
	jQuery('#xtMenuBar').tabs('select', 'xtGeneChartTab'); // switch to chart tab
}



function drawPieChart(divid, data)
{
	

}


//Load the heatmap data for cross trial analysis 
//analysisIds: pipe delimited list of analysis ids
//category: GENELIST, GENESIG, or PATHWAY
//searchKeywordId: the search keyword if for the gene list, gene sig, or pathway
//startRank: first index on the page to be retrieved
//endRank: last index on the page to be retrieved
//keyword: the text of the search keyword (to be used in title of heatmap)
function loadHeatmapCTA(analysisIds, category, searchKeywordId, startRank, endRank, keyword)	{	
	

	var heatmapDiv = "xtHeatmap_" +searchKeywordId;
	var heatmapHolderDivID = "xtHeatmapHolder_" +searchKeywordId;
	

	
	rwgAJAXManager.add({
		url:getHeatmapCTARowsURL,
		data: {analysisIds: analysisIds, category:category, searchKeywordId:searchKeywordId, 
			   startRank:startRank, endRank:endRank},
		timeout:60000,
		success: function(response) {
			
			jQuery('#'+heatmapHolderDivID).unmask(); //hide the loading msg, unblock the div
			
			drawHeatmapCTA(heatmapDiv, response['rows'], selectedAnalyses, keyword);
			
						
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
		
}

function loadHeatmapCTAPaginator(category, searchKeywordId, page, keyword) {
	
	//heatmapHolder div holds both the paginator and the heatmap
	var divID = "xtHeatmapHolder_" +searchKeywordId;
	var divPaginatorID = "xtHeatmapPaginator_"+searchKeywordId;

	
    //remove the div if it already exists:
    jQuery('#'+divID).remove();
    
    //remove the "empty" msg if it exists:
    jQuery("#xtNoHeatmapMsg").remove();
    
    //create div to hold svg chart
    jQuery("#xtHeatmapTab").prepend("<div id='"+divID +"' class='xtHeatmap'></div>");
    
    //insert the paginator div inside the heatmapHolder div
    jQuery("#"+divID).append("<div id='"+divPaginatorID +"' class='pagination'></div>");


	var analysisIds = "";
	// retrieve list of selected analyses, create a pipe delimited list of analysis ids
	for (var i=0; i<selectedAnalyses.length; i++)
	{
		if (analysisIds != "")  {
			analysisIds += "|";
		}
		analysisIds += selectedAnalyses[i].id;
	}
		
	rwgAJAXManager.add({
		url:getHeatmapCTARowCountURL,		
		data: {analysisIds: analysisIds, category: category, searchKeywordId: searchKeywordId, page:page},
		success: function(response) {								
			var numRows = response['totalCount'];
			var facetCounts = response['facetCounts'];
			var html = response['html'];
			var errorMsg = response['errorMsg'];
			
			if (errorMsg != '')  {
				alert(errorMsg);
			}

			getHeatmapPaginatorCTA(divPaginatorID, analysisIds, category, searchKeywordId, numRows, keyword);
	
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
	
	
    //add some buttons
	//html for button to close the graph
	var closeHTML = "<a href='#' class='xtClostbtn' id='" +searchKeywordId  +"_CTAheatmapCloseBtn' onclick=\"closeCTAheatmap('"+divID+"', '" +searchKeywordId +"')\">x</a>";
    jQuery('#'+divID).prepend(closeHTML);
	
}

function getHeatmapPaginatorCTA(divID, analysisIds, category, searchKeywordId, numberRows, keyword) {
	
	var heatmapHolderDivID = "xtHeatmapHolder_" +searchKeywordId;
	
	var heatmapDiv = "xtHeatmap_" +searchKeywordId;
    
    //create div for the heatmap
    jQuery("#"+searchKeywordId+"_CTAheatmapCloseBtn").after("<div id='"+heatmapDiv +"' ></div>");
	
	
	var element = jQuery("#" + divID);
	var numberOfRowsPerPage = 20;
		
	// get number of extra genes on last page (will be 0 if last page is full)
	var numberRowsLastPage = numberRows % numberOfRowsPerPage;
	
	// find number of full pages
	var numberOfFullPages = Math.floor(numberRows / numberOfRowsPerPage);
	
	// find number of pages - equal to number of full pages if none left over after full pages
	var numberOfPages = numberOfFullPages;        	        	
	if (numberRowsLastPage > 0)  {
		numberOfPages = numberOfPages + 1;
	}
	
	//if there is only 1 page, just hide the paging control since it's not needed
	if(numberOfPages==1){
		element.hide();
	}
	else  {
		element.show();
	}

	element.paging(numberOfPages, { 
	    perpage:1, 
      format:"[<(qq -) ncnnn (- pp)>]",
      onSelect: function (page) { 
      	jQuery("#"+heatmapHolderDivID).mask("Loading...");

      	var startRank = (page - 1)*numberOfRowsPerPage + 1
      	var endRank = (page)*numberOfRowsPerPage
      	
      	if (endRank > numberRows)  {
      		endRank = numberRows
      	}
      	
      	if (numberRows == 0)  {
			jQuery('#'+heatmapHolderDivID).unmask(); //hide the loading msg, unblock the div
			
			//html for button to close the graph
			var closeHTML = "<a href='#' class='xtClostbtn' id='" +searchKeywordId  +"_CTAheatmapCloseBtn' onclick=\"closeCTAheatmap('"+heatmapHolderDivID+"', '" +searchKeywordId +"')\">x</a>";
							    
      		drawHeatmapCTA(heatmapDiv, null, selectedAnalyses, keyword);  // draw blank heatmap
      		
		    //add some buttons
		    jQuery('#'+heatmapHolderDivID).prepend(closeHTML);

      	}
      	else  {      		
    		loadHeatmapCTA(analysisIds, category, searchKeywordId, startRank, endRank, keyword);   
      	}

                                  
      }, 
      onFormat: formatPaginator
  }); 	
	
}


function openXtBoxplot(keywordId, geneName){
	
	/* 
	
	jQuery('#xtBoxplot').modal({onOpen: modalEffectsOpen, opacity: [70], onClose: modalEffectsClose,
		onShow: function (dialog) {
	        dialog.container.css("height", "auto");
	        dialog.container.css("width", "80%");
	        dialog.container.css("left", "10%");
	        dialog.container.css("top", "10%");
	    }	
	
	});
	
	*/
	
	//find width of window, multiply by % to get dialog width
	var wWidth = jQuery(window).width() * 0.8;
	
	jQuery('#xtBoxplotHolder').dialog({ width: wWidth, title: geneName });
    

    loadBoxPlotCTA(keywordId);
    
    return;

	
}




//Load the box plot data for cross trial analysis (the keywordId must represent a gene)
function loadBoxPlotCTA(keywordId)	{	
	
	var ids = "";
	// retrieve list of selected analyses, create a pipe delimited list of analysis ids
	for (var i=0; i<selectedAnalyses.length; i++)
	{
		if (ids != "")  {
			ids += "|";
		}
		ids += selectedAnalyses[i].id;
	}
    jQuery('#xtBoxplotWrapper').mask("Loading...");

	rwgAJAXManager.add({
		url:getBoxPlotDataCTAURL,
		data: {ids: ids, keywordId: keywordId},
		timeout:60000,
		success: function(response) {
			
			drawBoxPlotD3('xtBoxplot', response, null, false, true, selectedAnalyses);
		    jQuery('#xtBoxplotWrapper').unmask();

			
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
	
	
}




function formatPaginator(type) {      
    
    switch (type) {      
    case 'block':      
        	if (!this.active)      
        		return '<span class="disabled">' + this.value + '</span>';      
        	else if (this.value != this.page)      
            return '<em><a href="#' + this.value + '">' + this.value + '</a></em>';      
        	return '<span class="current">' + this.value + '</span>';      
    case 'left':      
    case 'right':      

            if (!this.active)      
                    return '';      
            else       
                    return '<em><a href="#' + this.value + '">' + this.value + '</a></em>';      

    case 'next':      

            if (this.active) {      
                    return '<a href="#' + this.value + '" class="next">Next &raquo;</a>';      
            }      
            return '<span class="disabled">Next &raquo;</span>';      

    case 'prev':      

            if (this.active) {      
                    return '<a href="#' + this.value + '" class="prev">&laquo; Previous</a>';      
            }      
            return '<span class="disabled">&laquo; Previous</span>';      

    case 'first':      

            if (this.active) {      
                    return '<a href="#' + this.value + '" class="first">|&lsaquo;</a>';      
            }      
            return '<span class="disabled">|&lsaquo;</span>';      

    case 'last':      

            if (this.active) {      
                    return '<a href="#' + this.value + '" class="prev">&rsaquo;|</a>';      
            }      
            return '<span class="disabled">&rsaquo;|</span>';      

    case 'fill':      
            if (this.active) {      
                    return "...";      
            }      
    }      
}

