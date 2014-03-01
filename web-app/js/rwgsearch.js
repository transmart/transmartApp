////////////////////////////////////////////////////////////////////
// Globals
// Store the current search terms in an array in format ("category display|category:term") where category display is the display term i.e. Gene, Disease, etc.
var currentCategories = new Array();
var currentSearchOperators = new Array(); //AND or OR - keep in line with currentCategories
var currentSearchTerms = new Array();

var searchOpDelimiter = ";";

// Store the nodes that were selected before a new node was selected, so that we can compare to the nodes that are selected after.  Selecting
//  one node in the tree can cause lots of changes in other parts of the tree (copies of this node change, children/parents change, 
//  parents of parents, children of parents of parent, etc.)
var nodesBeforeSelect = new Array();

// By default, allow the onSelect event to trigger for the tree nodes;  However, we don't want select events that are triggered from inside the onSelect
// event to cause the onSelectEvent code to keep triggering itself.  So change this to false before any call to select() within the onSelect (the event
// will still fire but is stopped immediately); and set this flag back to true at the end of the event so it can be triggered again.  
var allowOnSelectEvent = true;

// Method to add the categories for the select box
function addSelectCategories()	{
	
	if (sessionSearchCategory == "") { sessionSearchCategory = "ALL"; }
	
	jQuery("#search-categories").append(jQuery("<option></option>").attr("value", "ALL").text("All").attr('id', 'allCategory'));
	
	jQuery("#search-categories").change(function() {
		jQuery('#search-ac').autocomplete('option', 'source', sourceURL + "?category=" + this.options[this.selectedIndex].value);
		jQuery.ajax({
			url:updateSearchCategoryURL,
			data: {id: jQuery("#search-categories").val()}
		});
	});
	
	jQuery.getJSON(getCategoriesURL, function(json) {
		for (var i=0; i<json.length; i++)	{
			var category = json[i].category;
			var catText = convertCategory(category);
			jQuery("#search-categories").append(jQuery("<option></option>").attr("value", category).text(catText));
		}
		
		jQuery("#search-categories").html(jQuery("option", jQuery("#search-categories")).sort(function(a, b) { 
	        return a.text == b.text ? 0 : a.text < b.text ? -1 : 1 
	    }))
		
		jQuery("#allCategory").after(jQuery("<option></option>").attr("value", "text").text("Free Text"));
		
		jQuery("#search-categories").val(sessionSearchCategory);
		jQuery('#search-ac').autocomplete('option', 'source', sourceURL + "?category=" + jQuery('#search-categories').val());

    });
	
}

function addFilterCategories() {
	jQuery.getJSON(getFilterCategoriesURL, function(json) {
		for (var i=0; i<json.length; i++)	{
			var category = json[i].category;
			var choices = json[i].choices;
			var titleDiv = jQuery("<div></div>").addClass("filtertitle").attr("name", category.category).text(category.displayName);
			var contentDiv = jQuery("<div></div>").addClass("filtercontent").attr("name", category.category).attr("style", "display: none");
			for (var j=0; j < choices.length; j++) {
				var choice = choices[j];
				
				var newItem = jQuery("<div></div>").addClass("filteritem").attr("name", category.category).attr("id", choice.uid).text(choice.name);
				
				//If this has been selected, highlight it
				var idString = '[id="' + category.displayName + "|" + category.category + searchOpDelimiter+ choice.name + searchOpDelimiter + choice.uid + '"]';
				idString = idString.replace(/,/g, "%44").replace(/&/g, "%26"); //Replace commas and ampersands
				var element = jQuery(idString);
				if (element.size() > 0) {
					newItem.addClass("selected");
				}

				contentDiv.append(newItem);
			}
			jQuery("#filter-browser").append(titleDiv);
			jQuery("#filter-browser").append(contentDiv);
		}
		
		jQuery("#filter-browser").removeClass("ajaxloading");
    });
}

//Method to add the autocomplete for the search keywords
function addSearchAutoComplete()	{
	jQuery("#search-ac").autocomplete({
		position:{my:"left top",at:"left bottom",collision:"none"},
		source: sourceURL,
		minLength:1,
		select: function(event, ui) {  
			searchParam={id:ui.item.id,display:ui.item.category,keyword:ui.item.label,category:ui.item.categoryId};
			addSearchTerm(searchParam);
			
			//If category is ALL, add this as free text as well
			var category = jQuery("#search-categories").val();
//			if (category == 'ALL') {
//				searchParam={id:ui.item.label,display:'Free Text',keyword:ui.item.label,category:'text'};
//				addSearchTerm(searchParam);
//			}
			return false;
		}
	}).data("autocomplete")._renderItem = function( ul, item ) {
		var resulta = '<a><span class="category-' + item.category.toLowerCase() + '">' + item.category + '&gt;</span>&nbsp;<b>' + item.label + '</b>&nbsp;';
		if (item.synonyms != null) {
			resulta += (item.synonyms + '</a>');
		}
		else {
			resulta += '</a>';
		}
		
		return jQuery('<li></li>')		
		  .data("item.autocomplete", item )
		  .append(resulta)
		  .appendTo(ul);
	};	
		
	// Capture the enter key on the slider and fire off the search event on the autocomplete
	jQuery("#search-categories").keypress(function(event)	{
		if (event.which == 13)	{
			jQuery("#search-ac").autocomplete('search');
		}
	});
	
	jQuery('#search-ac').keypress(function(event) {
		var category = jQuery("#search-categories").val();
		var categoryText = jQuery('#search-categories option:selected').text();
		if (event.which == 13 && (category == 'DATANODE' || category == 'text' || category == 'ALL')) {
			var val = jQuery('#search-ac').val();
			if (category == 'ALL') {category = 'text'; categoryText = 'Free Text';}
			searchParam={id:val,display:categoryText,keyword:val,category:category};
			addSearchTerm(searchParam);
			return false;
			jQuery('#search-ac').empty();
		}
	});
	return false;
}

//Helper method to only capitalize the first letter of each word
function convertCategory(valueToConvert)	{
	var convertedValue = valueToConvert.toLowerCase();
	if (convertedValue == "genesig") {
		return "Gene List";
	}
	if (convertedValue == "species") {
		return "Organism";
	}
	return convertedValue.slice(0,1).toUpperCase() + convertedValue.slice(1);
}

//Add the search term to the array and show it in the panel.
function addSearchTerm(searchTerm, noUpdate, loadingFromSession)	{
	goWelcome();
	
	var category = searchTerm.display == undefined ? "TEXT" : searchTerm.display;
	
	category = category + "|" + (searchTerm.category == undefined ? "TEXT" : searchTerm.category);
	
	var text = (searchTerm.text == undefined ? (searchTerm.keyword == undefined ? searchTerm : searchTerm.keyword) : searchTerm.text);
	var id = searchTerm.id == undefined ? -1 : searchTerm.id;
	var key = category + searchOpDelimiter + text + searchOpDelimiter + id;
	if (currentSearchTerms.indexOf(key) < 0)	{
		currentSearchTerms.push(key);
		if (currentCategories.indexOf(category) < 0)	{
			currentCategories.push(category);
			currentSearchOperators.push("or");
		}
	} 
	
	// clear the search text box
	jQuery("#search-ac").val("");
	
	// create flag to track if tree was updated
	var treeUpdated = false
	
	// find all nodes in tree with this key, and select them
//	var tree = jQuery("#filter-div").dynatree("getTree");
//
//	tree.visit(  function selectNode(node) {
//		             if (node.data.key == key)  {
//		            	 node.select(true);
//		            	 node.makeVisible();
//		            	 treeUpdated = true;
//		             }
//	             }
//			   , false);

	// only refresh results if the tree was not updated (the onSelect also fires these event, so don't want to do 2x)
	
	if (!treeUpdated && !noUpdate) {
      showSearchTemplate();
	  showSearchResults();
	}
}

//Main method to show the current array of search terms
function showSearchTemplate()	{
    var searchHTML = '';
    var startATag = '&nbsp;<a id=\"';
    var endATag = '\" class="term-remove" href="#" onclick="removeSearchTerm(this);">';
    var imgTag = '<img alt="remove" src="' + crossImageURL + '"/></a>&nbsp;'
    var firstItem = true;
    var needsToggle = false;
    var geneTerms = 0;

    var globalLogicOperator = "AND";
    if (jQuery('#globaloperator').hasClass("or")) { globalLogicOperator = "OR" }

    // iterate through categories array and move all the "gene" categories together at the top
    var newCategories = new Array();
    var newSearchOperators = new Array();

    var geneCategoriesProcessed = false;
    var geneCategories = 0;

    for (var i=0; i<currentCategories.length; i++)	{
        var catFields = currentCategories[i].split("|");
        var catId = catFields[1];

        // when we find a "gene" category, add it and the rest of the "gene" categories to the new array
        if (isGeneCategory(catId)) {
            geneCategories++;
            // first check if we've processed "gene" categories yet
            if (!geneCategoriesProcessed)  {

                // add first gene category to new array
                newCategories.push(currentCategories[i]);
                newSearchOperators.push(currentSearchOperators[i]);

                // look for other "gene" categories, starting at the next index value, and add each to array
                for (var j=i+1; j<currentCategories.length; j++)	{
                    var catFields2 = currentCategories[j].split("|");
                    var catId2 = catFields2[1];
                    if (isGeneCategory(catId2)) {
                        newCategories.push(currentCategories[j]);
                        newSearchOperators.push(currentSearchOperators[j]);
                    }
                }
                // set flag so we don't try to process again
                geneCategoriesProcessed = true;
            }
        }
        else  {    // not a gene catageory, add to new list
            newCategories.push(currentCategories[i]);
            newSearchOperators.push(currentSearchOperators[i]);
        }
    }

    // replace old array with new array
    currentCategories = newCategories;
    currentSearchOperators = newSearchOperators;

    for (var i=0; i<currentCategories.length; i++)	{
        for (var j=0; j<currentSearchTerms.length; j++)	{
            var fields = currentSearchTerms[j].split(searchOpDelimiter);
            if (currentCategories[i] == fields[0]){
                var tagID = currentSearchTerms[j].replace(/,/g, "%44").replace(/&/g, "%26");	// URL encode a few things

                var catFields = fields[0].split("|");
                var catDisplay = catFields[0];
                var catId = catFields[1];

                if (isGeneCategory(catId)) {
                    geneTerms++;
                }

                if (firstItem)	{

                    if (i>0)	{

                        var suppressAnd = false;
                        // if this is a "gene" category, check the previous category and see if it is also one
                        if (isGeneCategory(catId))  {
                            var catFieldsPrevious = currentCategories[i-1].split("|");
                            var catIdPrevious = catFieldsPrevious[1];
                            if (isGeneCategory(catIdPrevious))  {
                                suppressAnd = true;
                            }
                        }

                        // if previous category is a "gene" category, don't show operator
                        if (!suppressAnd)  {
                            searchHTML = searchHTML + "<span class='category_join'>" + globalLogicOperator + "<span class='h_line'></span></span>";  			// Need to add a new row and a horizontal line
                        }
                        else  {
                            searchHTML = searchHTML + "<br/>";
                        }
                    }
                    searchHTML = searchHTML +"<span class='category_label'>" +catDisplay + "&nbsp;></span>&nbsp;<span class=term>"+ fields[1] + startATag + tagID + endATag + imgTag +"</span>";
                    firstItem = false;
                }
                else {
                    searchHTML = searchHTML + "<span class='spacer'>" + currentSearchOperators[i] + " </span><span class=term>"+ fields[1] + startATag + tagID + endATag + imgTag +"</span> ";
                    needsToggle = true;
                }
            }
            else {
                continue; // Do the categories by row and in order
            }
        }
        //Show the and/or toggle, if this is a non-gene category or any gene category but the last.
        if ((!isGeneCategory(catId) && needsToggle) || i == geneCategories-1 && geneTerms > 1)  {
            searchHTML = searchHTML + "<div name='" + i + "' class='andor " + currentSearchOperators[i] + "'>&nbsp;</div>";
        }
        firstItem = true;
        needsToggle = false;
    }
    document.getElementById('active-search-div').innerHTML = searchHTML;
    getSearchKeywordList();
}


//Method to load the search results in the search results panel and facet counts into tree
//This occurs whenever a user add/removes a search term
function showSearchResults()	{

	// clear stored probe Ids for each analysis
	analysisProbeIds = new Array();  
	
	// clear stored analysis results
	jQuery('body').removeData();
	
	jQuery('#results-div').empty();
	
	// call method which retrieves facet counts and search results
	showFacetResults();
	
	//all analyses will be closed when doing a new search, so clear this array
	openAnalyses = [];

}

//Method to load the facet results in the search tree and populate search results panel
function showFacetResults()	{
	
	var globalLogicOperator = "AND";
	if (jQuery('#globaloperator').hasClass("or")) { globalLogicOperator = "OR" }
    GLOBAL.PathToExpand = '';
	var savedSearchTermsArray;
	var savedSearchTerms;
	
	if (currentSearchTerms.toString() == '')
		{
			savedSearchTermsArray = new Array();
			savedSearchTerms = '';
		
		}
	else
		{
			savedSearchTerms = currentSearchTerms.join(",,,");
			savedSearchTermsArray = savedSearchTerms.split(",,,");
		}


	
	// Generate list of categories/terms to send to facet search
	// create a string to send into the facet search, in form Cat1:Term1,Term2&Cat2:Term3,Term4,Term5&...

	var facetSearch = new Array();   // will be an array of strings "Cat1:Term1|Term2", "Cat2:Term3", ...   
	var categories = new Array();    // will be an array of categories "Cat1","Cat2"
	var terms = new Array();         // will be an array of strings "Term1|Term2", "Term3"
	var operators = new Array();

	// first, loop through each term and add categories and terms to respective arrays 		
    for (var i=0; i<savedSearchTermsArray.length; i++)	{
		var fields = savedSearchTermsArray[i].split(searchOpDelimiter);
		// search terms are in format <Category Display>|<Category>:<Search term display>:<Search term id>
		var termId = fields[2]; 
		var categoryFields = fields[0].split("|");
		var category = categoryFields[1].replace(" ", "_");   // replace any spaces with underscores (these will then match the SOLR field names) 
		
		var categoryIndex = categories.indexOf(category);

		// if category not in array yet, add category and term to their respective array, else just append term to proper spot in its array
		if (categoryIndex == -1)  {
		    categories.push(category);
		    
		    //Get the operator for this category from the global arrays
		    var operatorIndex = currentCategories.indexOf(fields[0]);
		    var operator = currentSearchOperators[operatorIndex];
		    if (operator == null) { operator = 'or' }
		    operators.push(operator);
		    

		    terms.push(termId);
		}
		else  {
		    terms[categoryIndex] = terms[categoryIndex] + "|" + termId; 			
		}
	}
    
//	var tree = jQuery("#filter-div").dynatree("getTree");
//
//	// create an array of the categories that come from the tree
//	var treeCategories = new Array();
//	tree.visit(  function(node) {
//        if (node.data.isCategory)  {
//     	   var categoryName = node.data.categoryName.split("|");
//     	   var cat = categoryName[1].replace(/ /g, "_");
//     	   
//     	   treeCategories.push(cat);        	    
//        }
//      }
//      , false
//    );

    // now construct the facetSearch array by concatenating the values from the cats and terms array
    for (var i=0; i<categories.length; i++)	{
    	var queryType = "";
    	
    	// determine if category is part of the tree; differentiate these types of query categories
    	// from others
    	//if (treeCategories.indexOf(categories[i])>-1) {
    	//	queryType = "fq";
    	//}
    	//else  {
    		queryType = "q";
    	//}
    	facetSearch.push(queryType + "=" + categories[i] + ":" + encodeURIComponent(terms[i]) + "::" + operators[i]);
    }

    // now add all tree categories that arene't being searched on to the string
//    for (var i=0; i<treeCategories.length; i++)  {
//    	if (categories.indexOf(treeCategories[i])==-1)  {
//    		queryType = "ff";
//        	facetSearch.push(queryType + "=" + treeCategories[i]);
//    	}
//    }
    
	jQuery("#results-div").addClass('ajaxloading').empty();
    
    var queryString = facetSearch.join("&");
    
    //Construct a list of the current categories and operators to save
    var operators = [];
    for (var i=0; i < currentCategories.length; i++) {
    	var category = currentCategories[i];
    	var operator = currentSearchOperators[i];
    	operators.push(category + "," + operator);
    }
    var operatorString = operators.join(searchOpDelimiter);
    
    queryString += "&searchTerms=" + encodeURIComponent(savedSearchTerms) + "&searchOperators=" + operatorString + "&globaloperator=" + globalLogicOperator;
    
    if (searchPage == 'RWG') {
		jQuery.ajax({
			url:facetResultsURL,
			data: queryString + "&page=RWG",
			success: function(response) {
					jQuery('#results-div').removeClass('ajaxloading').html(response);
					checkSearchLog();
					updateAnalysisData(null, false);
					 displayResultsNumber();
			},
			error: function(xhr) {
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
			}
		});
    }
    else {
    	//If there are no search terms, pass responsibility on to getCategories - if not, do our custom search
    	if (savedSearchTermsArray.length == 0) {
    		//Need to silently clear the search map here as well
			jQuery.ajax({
                url:clearSearchFilterURL,
                success: function(response) {
                    GLOBAL.PathToExpand = '';
                    getCategories();
                },
                error: function(xhr) {
                    console.log('Error!  Status = ' + xhr.status + xhr.statusText);
                }
            });

    	}
    	else {
			jQuery.ajax({
				url:facetResultsURL,
				data: queryString + "&page=datasetExplorer",
				success: function(response) {
						searchByTagComplete(response);
						checkSearchLog();
				},
				error: function(xhr) {
					console.log('Error!  Status = ' + xhr.status + xhr.statusText);
				}
			});
    	}
    }

}

function isGeneCategory(catId)  {
	if ((catId == 'GENE') || (catId == 'PATHWAY') || (catId == 'GENELIST') || (catId == 'GENESIG')) {
		return true;
	}
	else  {
		return false;
	}
}

//retrieve the current list of search keyword ids
function getSearchKeywordList()   {

	var keywords = new Array();
	
	for (var j=0; j<currentSearchTerms.length; j++)	{
		var fields = currentSearchTerms[j].split(searchOpDelimiter);
	    var keyword = fields[2];			
		keywords.push(keyword);
	}
	
	return keywords;
}

//Remove the search term that the user has clicked.
function removeSearchTerm(ctrl)	{
	goWelcome();
	var currentSearchTermID = ctrl.id.replace(/\%20/g, " ").replace(/\%44/g, ",").replace(/\%26/g, "&");
	var idx = currentSearchTerms.indexOf(currentSearchTermID);
	if (idx > -1)	{
		currentSearchTerms.splice(idx, 1);
		
		// check if there are any remaining terms for this category; remove category from list if none
		var fields = currentSearchTermID.split(searchOpDelimiter);
		var category = fields[0];
		clearCategoryIfNoTerms(category);

	}
	
	// Call back to the server to clear the search filter (session scope)
	jQuery.ajax({
		type:"POST",
		url:newSearchURL
	});

	// create flag to track if tree was updated
	var treeUpdated = false

	// find all nodes in tree with this key and deSelect
//	var tree = jQuery("#filter-div").dynatree("getTree");
//
//	tree.visit(  function deselectNode(node) {
//                    if (node.data.key == currentSearchTermID)  {
//       	                node.select(false);
//  	            	    treeUpdated = true;
//                    }
//                 }
//                 , false);
//	
	// only refresh results if the tree was not updated (the onSelect also fires these event, so don't want to do 2x)
	if (!treeUpdated) {
      showSearchTemplate();
	  showSearchResults();
	}
	
	//Remove selected status from filter browser for this item
	unselectFilterItem(fields[2]);
	
}

//Clear the tree, results along with emptying the two arrays that store categories and search terms.
function clearSearch()	{
	goWelcome();
	//remove all pending jobs from the ajax queue
	//rwgAJAXManager.clear(true); (this was causing problems, so removing for now)
	
	
	openAnalyses = []; //all analyses will be closed, so clear this array
	
	
	jQuery("#search-ac").val("");
	
	currentSearchTerms = new Array();
	currentCategories = new Array();
	currentSearchOperators = new Array();
	
	// Change the category picker back to ALL and set autocomplete to not have a category (ALL by default)
	document.getElementById("search-categories").selectedIndex = 0;
	jQuery('#search-ac').autocomplete('option', 'source', sourceURL);
		
	//var tree = jQuery("#filter-div").dynatree("getTree");
	
	// Make sure the onSelect event doesn't fire for the nodes
	// Otherwise, the main search query is going to fire after each item is deselected, as well as facet query
//	allowOnSelectEvent = false;
//	tree.visit(function clearNode(node) {
//										 updateNodeIndividualFacetCount(node, -1);
//		                                 node.select(false);
//	                                    }, 
//	                                    false
//	           )
//	allowOnSelectEvent = true;
	
	showSearchTemplate();
	showSearchResults(); //reload the full search results
	
}

//update a node's count (not including children)
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

//Remove the category from current categories list if there are no terms left that belong to it
function clearCategoryIfNoTerms(category)  {
	
	var found = false;
	for (var j=0; j<currentSearchTerms.length; j++)	{
		var fields2 = currentSearchTerms[j].split(searchOpDelimiter);
		var category2 = fields2[0];
		
		if (category == category2)  {
			found = true; 
			break;
		}
	}
	
	if (!found)  {
		var index = currentCategories.indexOf(category);
		currentCategories.splice(index, 1);
		currentSearchOperators.splice(index, 1);
	}
}

function unselectFilterItem(id) {
	//Longhand as may contain : characters
	jQuery("[id='" + id + "']").removeClass('selected');
}

// ---

jQuery(document).ready(function() {
	
	jQuery('#sidebartoggle').click(function() {
		toggleSidebar();
    });
	
	
	
	jQuery('#filter-browser').on('click', '.filtertitle', function () {
		jQuery('.filtercontent[name="' + jQuery(this).attr('name') + '"]').toggle('fast');
	});
	
	
	jQuery('#filter-browser').on('click', '.filteritem', function () {
		var selecting = !jQuery(this).hasClass('selected');
		jQuery(this).toggleClass('selected');
		
		var name = jQuery(this).attr('name');
		var id = jQuery(this).attr('id');
		var category = jQuery('.filtertitle[name="' + name + '"]').text();
		var value = jQuery(this).text();
		
		//If selecting this filter, add it to the list of current filters
		if (selecting) {
			var searchParam={id:id,
			        display:category,
			        keyword:value,
			        category:name};
			
			addSearchTerm(searchParam);
		}
		else {
			var idString = '[id="' + category + "|" + name + searchOpDelimiter + value + searchOpDelimiter + id + '"]';
			idString = idString.replace(/,/g, "%44").replace(/&/g, "%26"); //Replace special characters!
			var element = jQuery(idString);
			removeSearchTerm(element[0]);
		}
	});
	
    jQuery('body').on('mouseenter', '.folderheader', function() {
		jQuery(this).find('.foldericonwrapper').fadeIn(150);
	});

    jQuery('body').on('mouseleave', '.folderheader', function() {
		jQuery(this).find('.foldericonwrapper').fadeOut(150);
	});

    jQuery('body').on('click', '.foldericon.add', function() {
		var id = jQuery(this).attr('name');
		jQuery(this).removeClass("foldericon").removeClass("add").removeClass("link").text("Added to cart");
		jQuery('#cartcount').hide();
		
		jQuery.ajax({
			url:exportAddURL,
			data: {id: id},			
			success: function(response) {
				jQuery('#cartcount').show().text(response);
			},
			error: function(xhr) {
				jQuery('#cartcount').show();
			}
		});
	});

    jQuery('body').on('click', '.foldericon.addall', function() {
		var nameelements = jQuery(this).closest('table').find('.foldericon.add');
		var ids = [];
		for (i = 0; i < nameelements.size(); i++) {
			ids.push(jQuery(nameelements[i]).attr('name'));
			jQuery(nameelements[i]).removeClass("foldericon").removeClass("add").removeClass("link").text("Added to cart");
		}
		
		jQuery('#cartcount').hide();
		
		jQuery.ajax({
			url:exportAddURL,
			data: {id: ids.join(",")},			
			success: function(response) {
				jQuery('#cartcount').show().text(response);
			},
			error: function(xhr) {
				jQuery('#cartcount').show();
			}
		});
	});
    
    jQuery('body').on('click', '.foldericon.delete', function() {
		var id = jQuery(this).attr('name');
		
		if (confirm("Are you sure you want to delete this file?")) {
			jQuery.ajax({
				url:deleteFileURL,
				data: {id: id},
				success: function(response) {
					jQuery('#files-table').html(response);
					//Get document count and reduce by 1
					var folderId = jQuery('#file-list-table').attr('name');
					var documentCount = jQuery('#folder-header-' + folderId + ' .document-count');
					if (documentCount.size() > 0) {
						var currentValue = documentCount.text();
						documentCount.text(currentValue - 1);
					}
				},
				error: function(xhr) {
					alert(xhr.message);
				}
			});
		}
	});

    jQuery('body').on('click', '.foldericon.view', function() {
	    var id = jQuery(this).closest(".folderheader").attr('name');
    	showDetailDialog(id);
	});
	
	jQuery('#metadata-viewer').on('click', '.editmetadata', function() {

    	var id = jQuery(this).attr('name');

		jQuery('#editMetadataOverlay').fadeIn();
		jQuery('#editMetadata').empty().addClass('ajaxloading');

		jQuery.ajax({
			url:editMetaDataURL,
			data: {folderId: id},			
			success: function(response) {
				jQuery('#editMetadata').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr.responseText);
				jQuery('#editMetadata').html(response).removeClass('ajaxloading');
			}
		});
	});
	
    jQuery('#box-search').on('click', '.andor', function() {

        if(currentSearchTerms.toString() != ''){
    	
            if (jQuery(this).attr('id') == 'globaloperator') {
                //For global switch, just alter the class - this is picked up later
                if (jQuery(this).hasClass("or")) {
                    jQuery(this).removeClass("or").addClass("and");
                }
                else {
                    jQuery(this).removeClass("and").addClass("or");
                }
                showSearchTemplate();
                showSearchResults();
            }
            else {
                //For individual categories, alter this index of the current search operators, then redisplay
                if (jQuery(this).hasClass("or")) {
                    currentSearchOperators[jQuery(this).attr('name')] = 'and'
                }
                else {
                    currentSearchOperators[jQuery(this).attr('name')] = 'or'
                }
                showSearchTemplate();
                showSearchResults();
            }
	    }
    });


	jQuery('#metadata-viewer').on('click', '.addassay', function() {

    	var id = jQuery(this).attr('name');

		jQuery('#createAssayOverlay').fadeIn();
		jQuery('#createAssay').empty().addClass('ajaxloading');

		jQuery.ajax({
			url:createAssayURL,
			data: {folderId: id},			
			success: function(response) {
				jQuery('#createAssay').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				jQuery('#createAssay').html(response).removeClass('ajaxloading');
			}
		});
	});

	jQuery('#metadata-viewer').on('click', '.addanalysis', function() {

    	var id = jQuery(this).attr('name');

		jQuery('#createAnalysisOverlay').fadeIn();
		jQuery('#createAnalysis').empty().addClass('ajaxloading');

		jQuery.ajax({
			url:createAnalysisURL,
			data: {folderId: id},			
			success: function(response) {
				jQuery('#createAnalysis').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				jQuery('#createAnalysis').html(response).removeClass('ajaxloading');
			}
		});
	});

	jQuery('#metadata-viewer').on('click', '.addfolder', function() {

    	var id = jQuery(this).attr('name');

		jQuery('#createFolderOverlay').fadeIn();
		jQuery('#createFolder').empty().addClass('ajaxloading');

		jQuery.ajax({
			url:createFolderURL + "?",
			data: {folderId: id},			
			success: function(response) {
				jQuery('#createFolder').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				jQuery('#createFolder').html(response).removeClass('ajaxloading');
			}
		});
	});
	
	jQuery('#metadata-viewer').on('click', '.deletefolder', function() {

    	var id = jQuery(this).attr('name');
    	var parent = jQuery(this).data('parent');
    	
    	if (confirm("Are you sure you want to delete this folder and the files and folders beneath it?")) {
			jQuery.ajax({
				url:deleteFolderURL,
				data: {id: id},
				success: function(response) {
					//Update viewer with response ("folder has been deleted" message) and update the parent in the browse tree.
					jQuery('#metadata-viewer').html(response);
					updateFolder(parent);
					showDetailDialog(parent);
					jQuery('.result-folder-name').removeClass('selected');
					jQuery('#result-folder-name-' + parent).addClass('selected');
				},
				error: function(xhr) {
					alert(xhr.message);
				}
			});
    	}
	});

	jQuery('#metadata-viewer').on('click', '.addstudy', function() {

    	var id = jQuery(this).attr('name');

		jQuery('#createStudyOverlay').fadeIn();
		jQuery('#createStudy').empty().addClass('ajaxloading');

		jQuery.ajax({
			url:createStudyURL,
			data: {folderId: id},			
			success: function(response) {
				jQuery('#createStudy').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				jQuery('#createStudy').html(response).removeClass('ajaxloading');
			}
		});
	});

	jQuery('#welcome-viewer').on('click', '.addprogram', function() {
		
	   	var id = jQuery(this).attr('name');

		jQuery('#createProgramOverlay').fadeIn();
		jQuery('#createProgram').empty().addClass('ajaxloading');

		jQuery.ajax({
			url:createProgramURL,
			data: {folderId: id},			
			success: function(response) {
				jQuery('#createProgram').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				jQuery('#createProgram').html(response).removeClass('ajaxloading');
			}
		});
	});

    jQuery('#exportOverlay').on('click', '.greybutton.remove', function() {

    	var row = jQuery(this).closest("tr");
	    var id = row.attr('name');
	   
	    jQuery('#cartcount').hide();
	    
		jQuery.ajax({
			url:exportRemoveURL,
			data: {id: id},			
			success: function(response) {
				row.remove();
				jQuery('#cartcount').show().text(response);
				updateExportCount();
				jQuery('#metadata-viewer').find(".exportaddspan[name='" + id + "']").addClass("foldericon").addClass("add").addClass("link").text('Add to export');
			},
			error: function(xhr) {
				jQuery('#cartcount').show();
			}
		});
	});

    jQuery('#exportOverlay').on('click', '.greybutton.export', function() {

    	var checkboxes = jQuery('#exporttable input:checked');
		var ids = [];
		for (i = 0; i < checkboxes.size(); i++) {
			ids.push(jQuery(checkboxes[i]).attr('name'));
		}

		if (ids.size() == 0) {return false;}

		window.location = exportURL + "?id=" + ids.join(',');
	    

		for(j=0; j<ids.size(); j++){
		    var id = ids[j];
		    var i=0;
		   
		    jQuery('#cartcount').hide();
		    
			jQuery.ajax({
				url:exportRemoveURL,
				data: {id: id},			
				success: function(response) {
					jQuery(checkboxes[i]).closest("tr").remove();
					console.log(jQuery(checkboxes[i]).attr('name'));
					jQuery('#cartcount').show().text(response);
					updateExportCount();
					jQuery('#metadata-viewer').find(".exportaddspan[name='" + ids[i] + "']").addClass("foldericon").addClass("add").addClass("link").text('Add to export');
					i=i+1;
				},
				error: function(xhr) {
					jQuery('#cartcount').show();
				}
			});
		}
	});

	jQuery('body').on('click', '#closeexport', function() {
		jQuery('#exportOverlay').fadeOut();	
    });
    
   jQuery('body').on('click', '#closefilter', function() {
		jQuery('#filter-browser').fadeOut();	
    });
    
   jQuery('body').on('click', '#closeedit', function() {
		jQuery('#editMetadataOverlay').fadeOut();	
    });

   jQuery('body').on('click', '#closeassay', function() {
		jQuery('#createAssayOverlay').fadeOut();	
   });

   jQuery('body').on('click', '#closeanalysis', function() {
		jQuery('#createAnalysisOverlay').fadeOut();	
  });

   jQuery('body').on('click', '#closefolder', function() {
		jQuery('#createFolderOverlay').fadeOut();	
   });

   jQuery('body').on('click', '#closestudy', function() {
		jQuery('#createStudyOverlay').fadeOut();	
   });
   jQuery('body').on('click', '#closeprogram', function() {
		jQuery('#createProgramOverlay').fadeOut();	
  });

    //Close export and filter overlays on click outside
    jQuery('body').on('click', function(e) {

    	if (!jQuery(e.target).closest('#exportOverlay').length
    	    	&& !jQuery(e.target).closest('#cartbutton').length
    	    	&& jQuery(e.target).attr('id') != 'cartbutton') {
    	
	    	if (jQuery('#exportOverlay').is(':visible')) {
    	    	jQuery('#exportOverlay').fadeOut();
	    	}
    	}
    	
    	if (!jQuery(e.target).closest('#filter-browser').length
    			&& !jQuery(e.target).closest('#filterbutton').length
    	    	&& jQuery(e.target).attr('id') != 'filter-browser') {
    	
	    	if (jQuery('#filter-browser').is(':visible')) {
    	    	jQuery('#filter-browser').fadeOut();
	    	}
    	}
	});

	jQuery('#results-div').on('click', '.result-folder-name', function() {
    	jQuery('.result-folder-name').removeClass('selected');
		jQuery(this).addClass('selected');
    });

    jQuery('#logocutout').on('click', function() {
    	jQuery('#metadata-viewer').empty();

    	jQuery('#welcome-viewer').empty().addClass('ajaxloading');
    	jQuery('#welcome-viewer').load(welcomeURL, {}, function() {
    		jQuery('#welcome-viewer').removeClass('ajaxloading');
    	});
	});

    jQuery('#cartbutton').click(function() {
		jQuery.ajax({
			url:exportViewURL,		
			success: function(response) {
				jQuery('#exportOverlay').html(response);
			},
			error: function(xhr) {
			}
		});
		jQuery('#exportOverlay').fadeToggle();
	});
	
	jQuery('#filterbutton').click(function() {
		jQuery('#filter-browser').fadeToggle();
	});
	
    addSelectCategories();
    addSearchAutoComplete();
    
    //Trigger a search immediately if RWG. Dataset Explorer does this on Ext load

    //This will block a search if the user comes from a clicked path
    if (GLOBAL.DefaultPathToExpand == '')
    {
        loadSearchFromSession();
        showSearchResults();
    }

});

function loadSearchFromSession() {
	var sessionFilters = sessionSearch.split(",,,");
	var sessionOperatorStrings = sessionOperators.split(searchOpDelimiter);
	
	//This pre-populates the categories array with the search operators - our saved terms will
	//then have the correct operator automatically applied
	for (var i=0; i < sessionOperatorStrings.length; i++) {
		var operatorPair = sessionOperatorStrings[i].split(",");
		var cat = operatorPair[0];
		var op = operatorPair[1];
		
		if (cat != null && cat != "") {
			currentCategories.push(cat);
			currentSearchOperators.push(op);
		}
	}
	
	
	for (var i = 0; i < sessionFilters.length; i++) {
		var item = sessionFilters[i];
		if (item != null && item != "") {
			var itemData = item.split("|");
			var itemSearchData = itemData[1].split(searchOpDelimiter);
			var searchParam = {id: itemSearchData[2], display: itemData[0], category: itemSearchData[0], keyword: itemSearchData[1]};
			addSearchTerm(searchParam, true, true);
		}
	}
	
	showSearchTemplate();
}

function updateFolder(id) {
	
	var imgExpand = "#imgExpand_"  + id;
	var src = jQuery(imgExpand).attr('src').replace('folderplus.png', 'ajax-loader-flat.gif').replace('folderminus.png', 'ajax-loader-flat.gif');
	jQuery(imgExpand).attr('src',src);
	
	jQuery.ajax({
		url:folderContentsURL,
		data: {id: id, auto: false},
		success: function(response) {
			jQuery('#' + id + '_detail').html(response).addClass('gtb1').addClass('analysesopen').attr('data', true);
			
			//check if the object has children
			if(jQuery('#' + id + '_detail .search-results-table .folderheader').size() > 0){
				jQuery(imgExpand).attr('src', jQuery(imgExpand).attr('src').replace('ajax-loader-flat.gif', 'folderminus.png'));
			}else{
				jQuery(imgExpand).attr('src', jQuery(imgExpand).attr('src').replace('ajax-loader-flat.gif', 'folderleaf.png'));
			}
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}

function checkSearchLog() {
	
	if (jQuery('#searchlog').size() > 0) {
		jQuery.ajax({
			url:searchLogURL,
			success: function(response) {
				var searchLog = jQuery('#searchlog').empty();
				searchLog.append("<br/>" + "------");
				var log = response.log
				for (var i = 0; i < log.length; i++) {
					searchLog.append("<br/>" + log[i]);
				}
			},
			error: function(xhr) {
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
			}
		});
	}
}

//go back to the welcome message
function goWelcome() {
	jQuery('#metadata-viewer').empty();
	jQuery('#welcome-viewer').empty().addClass('ajaxloading');
	jQuery('#welcome-viewer').load(welcomeURL, {}, function() {
		jQuery('#welcome-viewer').removeClass('ajaxloading');
	});
}

//display search results numbers
function displayResultsNumber(){
	if(resultNumber!=""){
		var jsonNumbers = JSON.parse(resultNumber);
		
		jQuery('#welcome-viewer').empty();
		jQuery('#metadata-viewer').empty();
		var htmlResults="<div style='margin: 10px;padding: 10px;'><h3 class='rdc-h3'>Search results by type</h3>";
		htmlResults+="<table class='details-table'>";
		htmlResults+="<thead><tr><th class='columnheader'>Object</th><th class='columnheader'>Number of results</th></tr></thead>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Programs</td><td class='columnvalue'>"+jsonNumbers.PROGRAM+"</td></tr>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Studies</td><td class='columnvalue'>"+jsonNumbers.STUDY+"</td></tr>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Assays</td><td class='columnvalue'>"+jsonNumbers.ASSAY+"</td></tr>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Analyses</td><td class='columnvalue'>"+jsonNumbers.ANALYSIS+"</td></tr>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Folders</td><td class='columnvalue'>"+jsonNumbers.FOLDER+"</td></tr>";
		htmlResults+="</table></div>";
		jQuery('#metadata-viewer').html(htmlResults);
	}
}

//Globally prevent AJAX from being cached (mostly by IE)
jQuery.ajaxSetup({
	cache: false
});

