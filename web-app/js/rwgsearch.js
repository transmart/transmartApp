////////////////////////////////////////////////////////////////////
// Globals
// Store the current search terms in an array in format ("category display|category:term") where category display is the display term i.e. Gene, Disease, etc.
var currentCategories = new Array();
var currentSearchOperators = new Array(); //AND or OR - keep in line with currentCategories
var currentSearchTerms = new Array(); 

// Store the nodes that were selected before a new node was selected, so that we can compare to the nodes that are selected after.  Selecting
//  one node in the tree can cause lots of changes in other parts of the tree (copies of this node change, children/parents change, 
//  parents of parents, children of parents of parent, etc.)
var nodesBeforeSelect = new Array();

// By default, allow the onSelect event to trigger for the tree nodes;  However, we don't want select events that are triggered from inside the onSelect
// event to cause the onSelectEvent code to keep triggering itself.  So change this to false before any call to select() within the onSelect (the event
// will still fire but is stopped immediately); and set this flag back to true at the end of the event so it can be triggered again.  
var allowOnSelectEvent = true;
var uploader;

var $j = jQuery.noConflict();

// Method to add the categories for the select box
function addSelectCategories()	{
	
	if (sessionSearchCategory == "") { sessionSearchCategory = "ALL"; }
	
	$j("#search-categories").append($j("<option></option>").attr("value", "ALL").text("All").attr('id', 'allCategory'));
	
	$j("#search-categories").change(function() {
		$j('#search-ac').autocomplete('option', 'source', sourceURL + "?category=" + this.options[this.selectedIndex].value);
		$j.ajax({
			url:updateSearchCategoryURL,
			data: {id: $j("#search-categories").val()}
		});
	});
	
	$j.getJSON(getCategoriesURL, function(json) {
		for (var i=0; i<json.length; i++)	{
			var category = json[i].category;
			var catText = convertCategory(category);
			$j("#search-categories").append($j("<option></option>").attr("value", category).text(catText));
		}
		
		$j("#search-categories").html($j("option", $j("#search-categories")).sort(function(a, b) {
	        return a.text == b.text ? 0 : a.text < b.text ? -1 : 1;
	    }));
		
		$j("#allCategory").after($j("<option></option>").attr("value", "text").text("Free Text"));
		
		$j("#search-categories").val(sessionSearchCategory);
		$j('#search-ac').autocomplete('option', 'source', sourceURL + "?category=" + $j('#search-categories').val());

    });
	
}

function addFilterCategories() {
	$j.getJSON(getFilterCategoriesURL, function(json) {
		for (var i=0; i<json.length; i++)	{
			var category = json[i].category;
			var choices = json[i].choices;
			var titleDiv = $j("<div></div>").addClass("filtertitle").attr("name", category.category).text(category.displayName);
			var contentDiv = $j("<div></div>").addClass("filtercontent").attr("name", category.category).attr("style", "display: none");
			for (var j=0; j < choices.length; j++) {
				var choice = choices[j];
				
				var newItem = $j("<div></div>").addClass("filteritem").attr("name", category.category).attr("id", choice.uid).text(choice.name);
				
				//If this has been selected, highlight it
				var idString = '[id="' + category.displayName + "|" + category.category + ";" + choice.name + ";" + choice.uid + '"]';
				idString = idString.replace(/,/g, "%44").replace(/&/g, "%26"); //Replace commas and ampersands
				var element = $j(idString);
				if (element.size() > 0) {
					newItem.addClass("selected");
				}

				contentDiv.append(newItem);
			}
			$j("#filter-browser").append(titleDiv);
			$j("#filter-browser").append(contentDiv);
		}
		
		$j("#filter-browser").removeClass("ajaxloading");
    });
}

//Method to add the autocomplete for the search keywords
function addSearchAutoComplete()	{
	$j("#search-ac").autocomplete({
		position:{my:"left top",at:"left bottom",collision:"none"},
		source: sourceURL,
		minLength:1,
		select: function(event, ui) {  
		    if (ui.item != null && ui.item != "") {
			searchParam={id:ui.item.id,display:ui.item.category,keyword:ui.item.label,category:ui.item.categoryId};
			addSearchTerm(searchParam);
		    }

			//If category is ALL, add this as free text as well
			var category = $j("#search-categories").val();
		    return false;
		}
	}).data("ui-autocomplete")._renderItem = function( ul, item ) {
		var resulta = '<a><span class="category-' + item.category.toLowerCase() + '">' + item.category + '&gt;</span>&nbsp;<b>' + item.label + '</b>&nbsp;';
		if (item.synonyms != null) {
			resulta += (item.synonyms + '</a>');
		}
		else {
			resulta += '</a>';
		}
		
		return $j('<li></li>')
		  .data("item.autocomplete", item )
		  .append(resulta)
		  .appendTo(ul);
	};	
		
	// Capture the enter key on the slider and fire off the search event on the autocomplete
	$j("#search-categories").keypress(function(event)	{
		if (event.which == 13)	{
			$j("#search-ac").autocomplete('search');
		}
	});
	
	$j('#search-ac').keypress(function(event) {
		var category = $j("#search-categories").val();
		var categoryText = $j('#search-categories option:selected').text();
		if (event.which == 13 && (category == 'DATANODE' || category == 'text' || category == 'ALL')) {
			var val = $j('#search-ac').val();
			if (category == 'ALL') {category = 'text'; categoryText = 'Free Text';}
			searchParam={id:val,display:categoryText,keyword:val,category:category};
			addSearchTerm(searchParam);
			$j('#search-ac').empty();
			return false;
		}
	});
	return false;
}

//Helper method to only capitalize the first letter of each word
function convertCategory(valueToConvert)	{
	var convertedValue = valueToConvert.toLowerCase();
	if (convertedValue == "genesig") {
		return "Gene Signature";
	}if (convertedValue == "genelist") {
		return "Gene List";
	}
	if (convertedValue == "species") {
		return "Organism";
	}
	return convertedValue.slice(0,1).toUpperCase() + convertedValue.slice(1);
}

//Add the search term to the array and show it in the panel.
function addSearchTerm(searchTerm, noUpdate, openInAnalyze,datasetExplorerPath)	{
	var category = searchTerm.display == undefined ? "TEXT" : searchTerm.display;
	
	category = category + "|" + (searchTerm.category == undefined ? "TEXT" : searchTerm.category);
	
	var text = (searchTerm.text == undefined ? (searchTerm.keyword == undefined ? searchTerm : searchTerm.keyword) : searchTerm.text);
	var id = searchTerm.id == undefined ? -1 : searchTerm.id;
	var key = category + ";" + text + ";" + id;
	if (currentSearchTerms.indexOf(key) < 0)	{
		currentSearchTerms.push(key);
		if (currentCategories.indexOf(category) < 0)	{
			currentCategories.push(category);
			currentSearchOperators.push("or");
		}
	} 
	
	// clear the search text box
	$j("#search-ac").val("");

	// only refresh results if the tree was not updated (the onSelect also fires these event, so don't want to do 2x)
	
	if (!noUpdate) {
		if(!openInAnalyze){
			$j.ajax({
				url:resetNodesRwgURL
			});
			showSearchTemplate();
		}
	  showSearchResults(openInAnalyze, datasetExplorerPath);
	}
}

//Main method to show the current array of search terms 
function showSearchTemplate()	{
	var searchHTML = '';
	var startATag = '&nbsp;<a id=\"';
	var endATag = '\" class="term-remove" href="#" onclick="removeSearchTerm(this);">';
	var imgTag = '<img alt="remove" src="' + crossImageURL + '"/></a>&nbsp;';
	var firstItem = true;
	var needsToggle = false;
	var geneTerms = 0;
	
	var globalLogicOperator = "AND";
	if ($j('#globaloperator').hasClass("or")) { globalLogicOperator = "OR"; }

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
			var fields = currentSearchTerms[j].split(";");
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
function showSearchResults(openInAnalyze, datasetExplorerPath)	{

	// clear stored probe Ids for each analysis
	analysisProbeIds = new Array();  
	
	// clear stored analysis results
	$j('body').removeData();
	
	$j('#results-div').empty();
	
	// call method which retrieves facet counts and search results
	showFacetResults(openInAnalyze, datasetExplorerPath);
	
	//all analyses will be closed when doing a new search, so clear this array
	openAnalyses = [];

}

//Method to load the facet results in the search tree and populate search results panel
function showFacetResults(openInAnalyze, datasetExplorerPath)	{
	if(openInAnalyze == undefined){
		openInAnalyze = false;
	}
	var globalLogicOperator = "AND";
	if ($j('#globaloperator').hasClass("or")) { globalLogicOperator = "OR" }
	
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
		var fields = savedSearchTermsArray[i].split(";");
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
		    if (operator == null) { operator = 'or'; }
		    operators.push(operator);
		    

		    terms.push(termId);
		}
		else  {
		    terms[categoryIndex] = terms[categoryIndex] + "|" + termId; 			
		}
	}

    // now construct the facetSearch array by concatenating the values from the cats and terms array
    for (var i=0; i<categories.length; i++)	{
    	var queryType = "";

    	queryType = "q";
    	facetSearch.push(queryType + "=" + categories[i] + ":" + encodeURIComponent(terms[i]) + "::" + operators[i]);
    }
    
	$j("#results-div").addClass('ajaxloading').empty();
    
    var queryString = facetSearch.join("&");
    
    //Construct a list of the current categories and operators to save
    var operators = [];
    for (var i=0; i < currentCategories.length; i++) {
    	var category = currentCategories[i];
    	var operator = currentSearchOperators[i];
    	operators.push(category + "," + operator);
    }
    var operatorString = operators.join(";");
    
    queryString += "&searchTerms=" + encodeURIComponent(savedSearchTerms) + "&searchOperators=" + operatorString + "&globaloperator=" + globalLogicOperator;
    
    if(!openInAnalyze){
	    if (searchPage == 'RWG') {
			$j.ajax({
				url:facetResultsURL,
				data: queryString + "&page=RWG",
				success: function(response) {
						$j('#results-div').removeClass('ajaxloading').html(response);
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
				$j.ajax({url:clearSearchFilterURL});
				GLOBAL.PathToExpand = '';
	    		getCategories();
	    	}
	    	else {
	    		$j.ajax({
	    			url:facetResultsURL,
	    			data: queryString + "&page=datasetExplorer",
	    			success: function(response) {
	    			searchByTagComplete(response);
	    			checkSearchLog();
	    			},
	    			error: function(xhr) {
	    			console.log('Error! Status = ' + xhr.status + xhr.statusText);
	    			}
	    			});
	    	}
	    }
	}else{
		$j.ajax({
			url:saveSearchURL,
			data: queryString + "&page=RWG",
			success: function(response) {
				window.location.href = datasetExplorerPath;
			},
			error: function(xhr) {
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
				window.location.href = datasetExplorerPath;
			}
		});
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
		var fields = currentSearchTerms[j].split(";");		
	    var keyword = fields[2];			
		keywords.push(keyword);
	}
	
	return keywords;
}

//Remove the search term that the user has clicked.
function removeSearchTerm(ctrl)	{
	$j.ajax({
		url:resetNodesRwgURL
	});
	var currentSearchTermID = ctrl.id.replace(/\%20/g, " ").replace(/\%44/g, ",").replace(/\%26/g, "&");
	var idx = currentSearchTerms.indexOf(currentSearchTermID);
	if (idx > -1)	{
		currentSearchTerms.splice(idx, 1);
		
		// check if there are any remaining terms for this category; remove category from list if none
		var fields = currentSearchTermID.split(";");
		var category = fields[0];
		clearCategoryIfNoTerms(category);

	}
	
	// Call back to the server to clear the search filter (session scope)
	$j.ajax({
		type:"POST",
		url:newSearchURL
	});

	// create flag to track if tree was updated
	var treeUpdated = false;

	// only refresh results if the tree was not updated (the onSelect also fires these event, so don't want to do 2x)
	if (!treeUpdated) {
	    showSearchTemplate();
	    showSearchResults();
	}
	
        //Remove selected status from filter browser for this item
        if(idx > -1) {
	    unselectFilterItem(fields[2]);
        }
}

//Clear the tree, results along with emptying the two arrays that store categories and search terms.
function clearSearch()	{
	goWelcome();
	$j.ajax({
		url:resetNodesRwgURL
	});
	
	openAnalyses = []; //all analyses will be closed, so clear this array
	
	
	$j("#search-ac").val("");
	
	currentSearchTerms = new Array();
	currentCategories = new Array();
	currentSearchOperators = new Array();
	
	// Change the category picker back to ALL and set autocomplete to not have a category (ALL by default)
	document.getElementById("search-categories").selectedIndex = 0;
	$j('#search-ac').autocomplete('option', 'source', sourceURL);

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
	    	node.data.facetCount = node.data.initialFacetCount;
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
		var fields2 = currentSearchTerms[j].split(";");
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
	$j("[id='" + id + "']").removeClass('selected');
}

// ---

function toggleSidebar() {
    element = $j('#sidebar')[0] || $j('#westPanel')[0];
    element = '#' + element.id;

    var leftPointingArrow = ($j('#sidebartoggle').css('background-image').indexOf("-right") < 0);
    var sidebarIsVisible = ($j(element + ':visible').size() > 0);
    //console.log("toggleSidebar: leftPointingArrow = " + leftPointingArrow + ", sidebarIsVisible = " + sidebarIsVisible);

    // This fixes problems with ExtJS in case of rapid consecutive clicks, double-click. JIRA TRANSREL-18.
    if (leftPointingArrow != sidebarIsVisible) { // it is still fading
    //    console.log("Too fast.")
        return;
    }

    func = null;
    if (typeof resizeAccordion == 'function') func = resizeAccordion;
    else func = function () {
        var panel = Ext.getCmp('westPanel');
        if (panel != undefined) {
            if (panel.hidden) {
                panel.hidden = false;
                panel.setVisible(true);
            }
            else {
                panel.hidden = true;
                panel.setVisible(false);
            }
            viewport.doLayout();
        }
    };
    if (sidebarIsVisible) {
        $j(element).fadeOut(500, func);
        var bgimg = $j('#sidebartoggle').css('background-image').replace('-left', '-right');
        $j('#sidebartoggle').css('background-image', bgimg);
    }
    else {
        $j(element).fadeIn();
        if (func) func(); //Not a callback here - resize as soon as it starts appearing.
        var bgimg = $j('#sidebartoggle').css('background-image').replace('-right', '-left');
        $j('#sidebartoggle').css('background-image', bgimg);
    }
}

$j(document).ready(function() {
	$j('#sidebartoggle').click(function() {
		toggleSidebar();
    });
	
	
	
	$j('#filter-browser').on('click', '.filtertitle', function () {
		$j('.filtercontent[name="' + $j(this).attr('name') + '"]').toggle('fast');
	});
	
	
	$j('#filter-browser').on('click', '.filteritem', function () {
		var selecting = !$j(this).hasClass('selected');
		$j(this).toggleClass('selected');
		
		var name = $j(this).attr('name');
		var id = $j(this).attr('id');
		var category = $j('.filtertitle[name="' + name + '"]').text();
		var value = $j(this).text();
		
		//If selecting this filter, add it to the list of current filters
		if (selecting) {
			var searchParam={id:id,
			        display:category,
			        keyword:value,
			        category:name};
			
			addSearchTerm(searchParam);
		}
		else {
			var idString = '[id="' + category + "|" + name + ";" + value + ";" + id + '"]';
			idString = idString.replace(/,/g, "%44").replace(/&/g, "%26"); //Replace special characters!
			var element = $j(idString);
			removeSearchTerm(element[0]);
		}
	});
	
    $j('body').on('mouseenter', '.folderheader', function() {
		$j(this).find('.foldericonwrapper').fadeIn(150);
	});

    $j('body').on('mouseleave', '.folderheader', function() {
		$j(this).find('.foldericonwrapper').fadeOut(150);
	});

    $j('body').on('click', '.foldericon.addcart', function() {
		var id = $j(this).attr('name');
		$j(this).removeClass("foldericon").removeClass("addcart").removeClass("link").text("Added to cart");
		$j('#cartcount').hide();
		
		$j.ajax({
			url:exportAddURL,
			data: {id: id},			
			success: function(response) {
				$j('#cartcount').show().text(response);
			},
			error: function(xhr) {
				$j('#cartcount').show();
			}
		});
	});

    $j('body').on('click', '.foldericon.addall', function() {
		var nameelements = $j(this).closest('table').find('.foldericon.addcart');
		var ids = [];
		for (i = 0; i < nameelements.size(); i++) {
			ids.push($j(nameelements[i]).attr('name'));
			$j(nameelements[i]).removeClass("foldericon").removeClass("addcart").removeClass("link").text("Added to cart");
		}
		
		$j('#cartcount').hide();
		
		$j.ajax({
			url:exportAddURL,
			data: {id: ids.join(",")},			
			success: function(response) {
				$j('#cartcount').show().text(response);
			},
			error: function(xhr) {
				$j('#cartcount').show();
			}
		});
	});
    
    $j('body').on('click', '.foldericon.deletefile', function() {
		var id = $j(this).attr('name');
		
		if (confirm("Are you sure you want to delete this file?")) {
			$j.ajax({
				url:deleteFileURL,
				data: {id: id},
				success: function(response) {
					$j('#files-table').html(response);
					//Get document count and reduce by 1
					var folderId = $j('#file-list-table').attr('name');
					var documentCount = $j('#folder-header-' + folderId + ' .document-count');
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

    $j('body').on('click', '.foldericon.view', function() {
	    var id = $j(this).closest(".folderheader").attr('name');
    	showDetailDialog(id);
	});
	
	$j('#metadata-viewer').on('click', '.editmetadata', function() {

    	var id = $j(this).attr('name');

		$j('#editMetadataOverlay').fadeIn();
		$j('#editMetadata').empty().addClass('ajaxloading');

		$j.ajax({
			url:editMetaDataURL,
			data: {folderId: id},			
			success: function(response) {
				$j('#editMetadata').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr.responseText);
				$j('#editMetadata').html(response).removeClass('ajaxloading');
			}
		});
	});
	
    $j('#box-search').on('click', '.andor', function() {
    	
    	if ($j(this).attr('id') == 'globaloperator') {
    		//For global switch, just alter the class - this is picked up later
    	    if ($j(this).hasClass("or")) {
    	    	$j(this).removeClass("or").addClass("and");
    	    }
    	    else {
    	    	$j(this).removeClass("and").addClass("or");
    	    }
    	    showSearchTemplate();
    	    showSearchResults();
    	}
    	else {
    		//For individual categories, alter this index of the current search operators, then redisplay
		    if ($j(this).hasClass("or")) {
		    	currentSearchOperators[$j(this).attr('name')] = 'and';
		    }
		    else {
		    	currentSearchOperators[$j(this).attr('name')] = 'or';
		    }
		    showSearchTemplate();
		    showSearchResults();
    	}
	});


	$j('#metadata-viewer').on('click', '.addassay', function() {

    	var id = $j(this).attr('name');

		$j('#createAssayOverlay').fadeIn();
		$j('#createAssay').empty().addClass('ajaxloading');
		$j('#editMetadata').empty();

		$j.ajax({
			url:createAssayURL,
			data: {folderId: id},			
			success: function(response) {
				$j('#createAssay').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				$j('#createAssay').html(response).removeClass('ajaxloading');
			}
		});
	});

	$j('#metadata-viewer').on('click', '.addanalysis', function() {

    	var id = $j(this).attr('name');

		$j('#createAnalysisOverlay').fadeIn();
		$j('#createAnalysis').empty().addClass('ajaxloading');
		$j('#editMetadata').empty();

		$j.ajax({
			url:createAnalysisURL,
			data: {folderId: id},			
			success: function(response) {
				$j('#createAnalysis').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				$j('#createAnalysis').html(response).removeClass('ajaxloading');
			}
		});
	});

	$j('#metadata-viewer').on('click', '.addfolder', function() {

    	var id = $j(this).attr('name');

		$j('#createFolderOverlay').fadeIn();
		$j('#createFolder').empty().addClass('ajaxloading');
		$j('#editMetadata').empty();

		$j.ajax({
			url:createFolderURL + "?",
			data: {folderId: id},			
			success: function(response) {
				$j('#createFolder').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				$j('#createFolder').html(response).removeClass('ajaxloading');
			}
		});
	});
	
	$j('#metadata-viewer').on('click', '.deletefolder', function() {

    	var id = $j(this).attr('name');
    	var parent = $j(this).data('parent');
    	
    	if (confirm("Are you sure you want to delete this folder and the files and folders below it?")) {
			$j.ajax({
				url:deleteFolderURL,
				data: {id: id},
				success: function(response) {
					updateFolder(parent);
					showDetailDialog(parent);
					$j('.result-folder-name').removeClass('selected');
				        if (parent > 0) {
					    $j('#result-folder-name-' + parent).addClass('selected');
				        }
				},
				error: function(xhr) {
					alert(xhr.message);
				}
			});
    	}
	});
	$j('#metadata-viewer').on('click', '.uploadfiles', function() {
	    var id = $j(this).attr('name');
	    $j('#uploadtitle').html("<p>Upload files into folder "+$j('#parentFolderName').val()+"</p>");
	    $j('#parentFolderId').val(id);
	    $j('#uploadFilesOverlay').fadeIn();
	    if ($j('#existingfiles').val()!="yes"){
	      $j.ajax({
	        url:uploadFilesURL + "?",
            data: {folderId: id},
	        success: function(response) {
	          $j('#uploadFiles').html(response).removeClass('ajaxloading');
	          createUploader();
	        },
	        error: function(xhr) {
	          alert(xhr);
	        }
	      });
	    }else{
            setUploaderEndPoint(id);
	    }
	});
	  
	$j('body').on('click', '#closeupload', function() {
	      jQuery('#uploadFilesOverlay').fadeOut();  
	});

	$j('#metadata-viewer').on('click', '.deletestudy', function () {

		var id = $j(this).attr('name');
		var parent = $j('#parentId').val();
		if (confirm("Are you sure you want to delete this study?")) {
			findChildByParent(id, function (hasChildren) {
				if (hasChildren) {
					if (!confirm("This study contains some elements below it. Are you sure?")) {
						return;
					}
				}
				$j.ajax({
					url: deleteStudyURL,
					data: {id: id},
					success: function (response) {
						updateFolder(parent);
						showDetailDialog(parent);
						$j('.result-folder-name').removeClass('selected');
						$j('#result-folder-name-' + parent).addClass('selected');
					},
					error: function (xhr) {
						alert(xhr.message);
					}
				});
			})

		}
	});

	$j('#metadata-viewer').on('click', '.deleteprogram', function () {

		var id = $j(this).attr('name');
		if (confirm("Are you sure you want to delete this program?")) {
			findChildByParent(id, function (hasChildren) {
				if (hasChildren) {
					if (!confirm("This program contains some elements below it. Are you sure?")) {
						return;
					}
				}
				$j.ajax({
					url: deleteProgramURL,
					data: {id: id},
					success: function (response) {
						showSearchResults();
						goWelcome();
					},
					error: function (xhr) {
						alert(xhr.message);
					}
				});
			})

		}
	});

	$j('#metadata-viewer').on('click', '.addstudy', function() {

    	var id = $j(this).attr('name');

		$j('#createStudyOverlay').fadeIn();
		$j('#createStudy').empty().addClass('ajaxloading');
		$j('#editMetadata').empty();

		$j.ajax({
			url:createStudyURL,
			data: {folderId: id},			
			success: function(response) {
				$j('#createStudy').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				$j('#createStudy').html(response).removeClass('ajaxloading');
			}
		});
	});

	$j('#welcome-viewer').on('click', '.addprogram', function() {
		
	   	var id = $j(this).attr('name');

		$j('#createProgramOverlay').fadeIn();
		$j('#createProgram').empty().addClass('ajaxloading');
		$j('#editMetadata').empty();

		$j.ajax({
			url:createProgramURL,
			data: {folderId: id},			
			success: function(response) {
				$j('#createProgram').html(response).removeClass('ajaxloading');
			},
			error: function(xhr) {
				alert(xhr);
				$j('#createProgram').html(response).removeClass('ajaxloading');
			}
		});
	});

    $j('#exportOverlay').on('click', '.greybutton.remove', function() {

    	var row = $j(this).closest("tr");
	    var id = row.attr('name');
	   
	    $j('#cartcount').hide();
	    
		$j.ajax({
			url:exportRemoveURL,
			data: {id: id},			
			success: function(response) {
				row.remove();
				$j('#cartcount').show().text(response);
				updateExportCount();
				$j('#metadata-viewer').find(".exportaddspan[name='" + id + "']").addClass("foldericon").addClass("addcart").addClass("link").text('Add to export');
			},
			error: function(xhr) {
				$j('#cartcount').show();
			}
		});
	});

    $j('#exportOverlay').on('click', '.greybutton.export', function() {

    	var checkboxes = $j('#exporttable input:checked');
		var ids = [];
		for (i = 0; i < checkboxes.size(); i++) {
			ids.push($j(checkboxes[i]).attr('name'));
		}

		if (ids.length == 0) {return false;}

		window.location = exportURL + "?id=" + ids.join(',');
		   
	    $j('#cartcount').hide();
	    
		$j.ajax({
			url:exportRemoveURL,
			data: {id: ids.join(',')},			
			success: function(response) {
				for(j=0; j<ids.length; j++){
                    $j(checkboxes[j]).closest("tr").remove();
                    $j('#cartcount').show().text(response);
					updateExportCount();
					$j('#metadata-viewer').find(".exportaddspan[name='" + ids[j] + "']").addClass("foldericon").addClass("addcart").addClass("link").text('Add to export');
				}
			},
			error: function(xhr) {
				$j('#cartcount').show();
			}
		});
	});

	$j('body').on('click', '#closeexport', function() {
		$j('#exportOverlay').fadeOut();
    });
    
   $j('body').on('click', '#closefilter', function() {
		$j('#filter-browser').fadeOut();
    });
    
   $j('body').on('click', '#closeedit', function() {
		$j('#editMetadataOverlay').fadeOut();
    });

   $j('body').on('click', '#closeassay', function() {
		$j('#createAssayOverlay').fadeOut();
   });

   $j('body').on('click', '#closeanalysis', function() {
		$j('#createAnalysisOverlay').fadeOut();
  });

   $j('body').on('click', '#closefolder', function() {
		$j('#createFolderOverlay').fadeOut();
   });

   $j('body').on('click', '#closestudy', function() {
		$j('#createStudyOverlay').fadeOut();
   });
   $j('body').on('click', '#closeprogram', function() {
		$j('#createProgramOverlay').fadeOut();
  });

    //Close export and filter overlays on click outside
    $j('body').on('click', function(e) {

    	if (!$j(e.target).closest('#exportOverlay').length
    	    	&& !$j(e.target).closest('#cartbutton').length
    	    	&& $j(e.target).attr('id') != 'cartbutton') {
    	
	    	if ($j('#exportOverlay').is(':visible')) {
    	    	$j('#exportOverlay').fadeOut();
	    	}
    	}
    	
    	if (!$j(e.target).closest('#filter-browser').length
    			&& !$j(e.target).closest('#filterbutton').length
    	    	&& $j(e.target).attr('id') != 'filter-browser') {
    	
	    	if ($j('#filter-browser').is(':visible')) {
    	    	$j('#filter-browser').fadeOut();
	    	}
    	}
	});

	$j('#results-div').on('click', '.result-folder-name', function() {
    	$j('.result-folder-name').removeClass('selected');
		$j(this).addClass('selected');
    });

    $j('#logocutout').on('click', function() {
    	$j('#metadata-viewer').empty();

    	$j('#welcome-viewer').empty().addClass('ajaxloading');
    	$j('#welcome-viewer').load(welcomeURL, {}, function() {
    		$j('#welcome-viewer').removeClass('ajaxloading');
    	});
	});

    $j('#cartbutton').click(function() {
		$j.ajax({
			url:exportViewURL,		
			success: function(response) {
				$j('#exportOverlay').html(response);
			},
			error: function(xhr) {
			}
		});
		$j('#exportOverlay').fadeToggle();
	});
	
	$j('#filterbutton').click(function() {
		$j('#filter-browser').fadeToggle();
	});
	
    addSelectCategories();
    addFilterCategories();
    addSearchAutoComplete();
    
    //Trigger a search immediately if RWG. Dataset Explorer does this on Ext load
    loadSearchFromSession();
    if (searchPage == 'RWG') {
		showSearchResults();
	}
});
function incrementDocumentCount(folderId) {
    var documentCount = $j('#folder-header-' + folderId + ' .document-count');
    if (documentCount.size() > 0) {
      var currentValue = documentCount.text();
      documentCount.text(parseInt(currentValue) + 1);
    }else{
      $j('#folder-header-'+folderId).html($j('#folder-header-'+folderId).html()+
          '<tr><td class="foldertitle">'+
      '<span class="result-document-count"><i>Documents (<span class="document-count">1</span>)</i></span></td></tr>');
    }
}

function loadSearchFromSession() {
	var sessionFilters = sessionSearch.split(",,,");
	var sessionOperatorStrings = sessionOperators.split(";");
	
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
		if (item != undefined && item != "") {
			var itemData = item.split("|");
			var itemSearchData = itemData[1].split(";");
			var searchParam = {id: itemSearchData[2], display: itemData[0], category: itemSearchData[0], keyword: itemSearchData[1]};
			addSearchTerm(searchParam, true, true);
		}
	}
	
	showSearchTemplate();
}

function updateFolder(id) {
    console.log('updateFolder '+id);
    // id=0 means no parent to update (deleting PROGRAM at top level)
    if(id > 0) {
	var imgExpand = "#imgExpand_"  + id;
	var src = $j(imgExpand).attr('src').replace('folderplus.png', 'ajax-loader-flat.gif').replace('folderminus.png', 'ajax-loader-flat.gif');
	$j(imgExpand).attr('src',src);
	
	$j.ajax({
		url:folderContentsURL,
		data: {id: id, auto: false},
		success: function(response) {
			$j('#' + id + '_detail').html(response).addClass('gtb1').addClass('analysesopen').attr('data', true);
			
			//check if the object has children
			if($j('#' + id + '_detail .search-results-table .folderheader').size() > 0){
				$j(imgExpand).attr('src', $j(imgExpand).attr('src').replace('ajax-loader-flat.gif', 'folderminus.png'));
			}else{
				$j(imgExpand).attr('src', $j(imgExpand).attr('src').replace('ajax-loader-flat.gif', 'folderleaf.png'));
			}
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
    }
}

function checkSearchLog() {
	
	if ($j('#searchlog').size() > 0) {
		$j.ajax({
			url:searchLogURL,
			success: function(response) {
				var searchLog = $j('#searchlog').empty();
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
	$j('#metadata-viewer').empty();
	$j('#welcome-viewer').empty().addClass('ajaxloading');
	$j('#welcome-viewer').load(welcomeURL, {}, function() {
		$j('#welcome-viewer').removeClass('ajaxloading');
	});
}

//display search results numbers
function displayResultsNumber(){
	if(resultNumber!=""){
		var jsonNumbers = JSON.parse(resultNumber);
		
		$j('#welcome-viewer').empty();
		$j('#metadata-viewer').empty();
		var htmlResults="<div style='margin: 10px;padding: 10px;'><h3 class='rdc-h3'>Search results by type</h3>";
		htmlResults+="<table class='details-table'>";
		htmlResults+="<thead><tr><th class='columnheader'>Object</th><th class='columnheader'>Number of results</th></tr></thead>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Programs</td><td class='columnvalue'>"+jsonNumbers.PROGRAM+"</td></tr>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Studies</td><td class='columnvalue'>"+jsonNumbers.STUDY+"</td></tr>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Assays</td><td class='columnvalue'>"+jsonNumbers.ASSAY+"</td></tr>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Analyses</td><td class='columnvalue'>"+jsonNumbers.ANALYSIS+"</td></tr>";
		htmlResults+="<tr class='details-row odd'><td class='columnname'>Folders</td><td class='columnvalue'>"+jsonNumbers.FOLDER+"</td></tr>";
		htmlResults+="</table></div>";
		$j('#metadata-viewer').html(htmlResults);
	}
}

//Globally prevent AJAX from being cached (mostly by IE)
$j.ajaxSetup({
	cache: false
});
function createUploader() {
    $fub = $j('#fine-uploader-basic');
    uploader = new qq.FineUploaderBasic({
      button: $fub[0],
      multiple: true,
      request: {
        endpoint: uploadActionURL+'?parentId='+$j('#parentFolderId').val()
      },
      callbacks: {
        onSubmit: function(id, fileName) {
            var folderName = $j('#folderName').val();
              
            $j('#uploadtable').append('<tr id="file-' + id + '" class="alert" style="margin: 20px 0 0">'+
                '<td id="parent">'+folderName+'</td>'+
                '<td id="name">'+fileName+'</td>'+
                '<td id="status">Submitting</td>'+
                '<td id="progress"></td></tr>');
        },
        onUpload: function(id, fileName) {
            $j('#file-' + id + " #name").html(fileName);
            $j('#file-' + id + " #status").html('Initializing ');
        },
        onProgress: function(id, fileName, loaded, total) {
          if (loaded < total) {
            progress = Math.round(loaded / total * 100) + '% of ' + Math.round(total / 1024) + ' kB';

            $j('#file-' + id + " #status").html('Uploading ');
            $j('#file-' + id + " #progress").html(progress);
          } else {
              $j('#file-' + id + " #status").html('Saving');
              $j('#file-' + id + " #progress").html('100%');
          }
        },
        onComplete: function(id, fileName, responseJSON) {
          if (responseJSON.success) {
            $j('#file-' + id + " #status").html('File successfully uploaded ');
              $j('#file-' + id + " #progress").html('');

              var folderId=responseJSON.folderId;
              incrementDocumentCount(folderId);
              
              if(folderId == $j('#parentFolderId').val()){
                $j('#metadata-viewer').empty().addClass('ajaxloading');
                $j('#metadata-viewer').load(folderDetailsURL + '?id=' + folderId, {}, function() {
                    $j('#metadata-viewer').removeClass('ajaxloading');
                });
              }
          } else {
              $j('#file-' + id + " #status").html('Error: '+responseJSON.error);
                $j('#file-' + id + " #progress").html('');
          }
          
        }
      }
    });
}
function createUploader() {
    $fub = $j('#fine-uploader-basic');
    uploader = new qq.FineUploaderBasic({
      button: $fub[0],
      multiple: true,
      request: {
        endpoint: uploadActionURL+'?parentId='+$j('#parentFolderId').val()
      },
      callbacks: {
        onSubmit: function(id, fileName) {
            var folderName = $j('#parentFolderName').val();

            $j('#uploadtable').append('<tr id="file-' + id + '" class="alert" style="margin: 20px 0 0">'+
                '<td id="parent">'+folderName+'</td>'+
                '<td id="name">'+fileName+'</td>'+
                '<td id="status">Submitting</td>'+
                '<td id="progress"></td></tr>');
        },
        onUpload: function(id, fileName) {
            $j('#file-' + id + " #name").html(fileName);
            $j('#file-' + id + " #status").html('Initializing ');
        },
        onProgress: function(id, fileName, loaded, total) {
          if (loaded < total) {
            progress = Math.round(loaded / total * 100) + '% of ' + Math.round(total / 1024) + ' kB';

            $j('#file-' + id + " #status").html('Uploading ');
            $j('#file-' + id + " #progress").html(progress);
          } else {
              $j('#file-' + id + " #status").html('Saving');
              $j('#file-' + id + " #progress").html('100%');
          }
        },
        onComplete: function(id, fileName, responseJSON) {
          if (responseJSON.success) {
            $j('#file-' + id + " #status").html('File successfully uploaded ');
              $j('#file-' + id + " #progress").html('');

              var folderId=responseJSON.folderId;
              incrementDocumentCount(folderId);

              if(folderId == $j('#parentFolderId').val()){
                $j('#metadata-viewer').empty().addClass('ajaxloading');
                $j('#metadata-viewer').load(folderDetailsURL + '?id=' + folderId, {}, function() {
                    $j('#metadata-viewer').removeClass('ajaxloading');
                });
              }
          } else {
              $j('#file-' + id + " #status").html('Error: '+responseJSON.error);
                $j('#file-' + id + " #progress").html('');
          }
        }
      }
    });
}

function setUploaderEndPoint(id) {
	uploader.setEndpoint(uploadActionURL+'?parentId='+id);
}

function findChildByParent(parent, callFunc){
	var hasChildren = true
	$j.ajax({
		url: hasChildrenURL,
		data: {id: parent},
		success: function(response) {
			hasChildren = response.result
			callFunc(hasChildren);
		}
	});
}
