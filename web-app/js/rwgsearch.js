////////////////////////////////////////////////////////////////////
// Globals
// Store the current search terms in an array in format ("category display|category:term") where category display is the display term i.e. Gene, Disease, etc.
var currentCategories = new Array();
var currentSearchTerms = new Array(); 

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
	jQuery("#search-categories").append(jQuery("<option></option>").attr("value", "ALL").text("All"));
	jQuery.getJSON(getCategoriesURL, function(json) {
		for (var i=0; i<json.length; i++)	{
			var category = json[i].category;
			var catText = convertCategory(category);
			jQuery("#search-categories").append(jQuery("<option></option>").attr("value", category).text(catText));
		}
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

//Helper method to only capitalize the first letter of each word
function convertCategory(valueToConvert)	{
	var convertedValue = valueToConvert.toLowerCase();
	return convertedValue.slice(0,1).toUpperCase() + convertedValue.slice(1);
}

//Add the search term to the array and show it in the panel.
function addSearchTerm(searchTerm, noUpdate)	{
	var category = searchTerm.display == undefined ? "TEXT" : searchTerm.display;
	
	category = category + "|" + (searchTerm.category == undefined ? "TEXT" : searchTerm.category);
	
	var text = (searchTerm.text == undefined ? (searchTerm.keyword == undefined ? searchTerm : searchTerm.keyword) : searchTerm.text);
	var id = searchTerm.id == undefined ? -1 : searchTerm.id;
	var key = category + ":" + text + ":" + id;
	if (currentSearchTerms.indexOf(key) < 0)	{
		currentSearchTerms.push(key);
		if (currentCategories.indexOf(category) < 0)	{
			currentCategories.push(category);
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
	showSearchTemplate();
	if (!treeUpdated && !noUpdate) {
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

	// iterate through categories array and move all the "gene" categories together at the top 
	var newCategories = new Array();
	
	var geneCategoriesProcessed = false;
	for (var i=0; i<currentCategories.length; i++)	{
		var catFields = currentCategories[i].split("|");
		var catId = catFields[1];
		
		// when we find a "gene" category, add it and the rest of the "gene" categories to the new array
		if (isGeneCategory(catId)) {
			// first check if we've processed "gene" categories yet
			if (!geneCategoriesProcessed)  {
				
				// add first gene category to new array
				newCategories.push(currentCategories[i]);

				// look for other "gene" categories, starting at the next index value, and add each to array
				for (var j=i+1; j<currentCategories.length; j++)	{
					var catFields2 = currentCategories[j].split("|");
					var catId2 = catFields2[1];
					if (isGeneCategory(catId2)) {
						newCategories.push(currentCategories[j]);
					}				
				}
				// set flag so we don't try to process again
				geneCategoriesProcessed = true;
			}
		}
		else  {    // not a gene catageory, add to new list
			newCategories.push(currentCategories[i]);
		}
	}
	
	// replace old array with new array
    currentCategories = newCategories;
	
	for (var i=0; i<currentCategories.length; i++)	{
		for (var j=0; j<currentSearchTerms.length; j++)	{
			var fields = currentSearchTerms[j].split(":");
			if (currentCategories[i] == fields[0]){
				var tagID = currentSearchTerms[j].split(' ').join('%20');			// URL encode the spaces
				var tagID = currentSearchTerms[j].split(',').join('%44');			// And the commas
				
				if (firstItem)	{
					var catFields = fields[0].split("|");
					var catDisplay = catFields[0];
					var catId = catFields[1];

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
						
		                // if previous category is a "gene" category, don't show AND
		                if (!suppressAnd)  {
							searchHTML = searchHTML + "<span class='category_join'>AND<span class='h_line'></span></span>";  			// Need to add a new row and a horizontal line
					    }
		                else  {
							searchHTML = searchHTML + "<br/>";  				                	
		                }
					}
					searchHTML = searchHTML +"<span class='category_label'>" +catDisplay + "&nbsp;></span>&nbsp;<span class=term>"+ fields[1] + startATag + tagID + endATag + imgTag +"</span>";
					firstItem = false;
				} else	{
					searchHTML = searchHTML + "<span class='spacer'>| </span><span class=term>"+ fields[1] + startATag + tagID + endATag + imgTag +"</span> ";
				}				
			} else	{
				continue;												// Do the categories by row and in order
			}
		}
		firstItem = true;
	}
	document.getElementById('active-search-div').innerHTML = searchHTML;
	getSearchKeywordList();
}

//Method to load the search results in the search results panel and facet counts into tree
//This occurs whenever a user add/removes a search term
function showSearchResults(tabToShow)	{

	// clear stored probe Ids for each analysis
	analysisProbeIds = new Array();  
	
	// clear stored analysis results
	jQuery('body').removeData();
	
	jQuery('#results-div').empty();
	
	// work out which tab is open and needs updating, if we don't have a specific one
	if (tabToShow == null) {
		if (jQuery('#analysisViewTab.ui-state-active').size() > 0) {
			tabToShow = 'analysis'
		}
		else {
			tabToShow = 'table'
		}
	}
	
	// call method which retrieves facet counts and search results
	showFacetResults(tabToShow);
	
	//all analyses will be closed when doing a new search, so clear this array
	openAnalyses = [];

}

//Method to load the facet results in the search tree and populate search results panel
function showFacetResults(tabToShow)	{
	
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

	// first, loop through each term and add categories and terms to respective arrays 		
    for (var i=0; i<savedSearchTermsArray.length; i++)	{
		var fields = savedSearchTermsArray[i].split(":");
		// search terms are in format <Category Display>|<Category>:<Search term display>:<Search term id>
		var termId = fields[2]; 
		var categoryFields = fields[0].split("|");
		var category = categoryFields[1].replace(" ", "_");   // replace any spaces with underscores (these will then match the SOLR field names) 
		
		var categoryIndex = categories.indexOf(category);

		// if category not in array yet, add category and term to their respective array, else just append term to proper spot in its array
		if (categoryIndex == -1)  {
		    categories.push(category);
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
    	facetSearch.push(queryType + "=" + categories[i] + ":" + terms[i]);
    }

    // now add all tree categories that arene't being searched on to the string
//    for (var i=0; i<treeCategories.length; i++)  {
//    	if (categories.indexOf(treeCategories[i])==-1)  {
//    		queryType = "ff";
//        	facetSearch.push(queryType + "=" + treeCategories[i]);
//    	}
//    }
    
	jQuery("#results-div").empty();
    
    // add study id to list of fields to facet (so we can get count for show search results)
    //facetSearch.push("ff=STUDY_ID");
    
    var queryString = facetSearch.join("&");
    
    //Show significant results is disabled
   	//queryString = queryString + "&showSignificantResults=" + document.getElementById('cbShowSignificantResults').checked
    
	jQuery.ajax({
		url:facetResultsURL,
		data: queryString + "&searchTerms=" + savedSearchTerms,
		success: function(response) {
			

				//var facetCounts = response['facetCounts'];
				//var html = response['html'];
				
				// set html for results panel
				//document.getElementById('results-div').innerHTML = html;
				
				jQuery('#results-div').html(response);

				// assign counts that were returned in json object to the tree
//				tree.visit(  function(node) {
//					           if (!node.data.isCategory && node.data.id)  {
//					        	   var id = node.data.id.toString();
//					        	   var catFields = node.data.categoryName.split("|")
//					        	   var cat = catFields[1].replace(" ","_");
//					        	   //var catArray = response[cat];
//					        	   var catArray = facetCounts[cat];
//					        	   var count = catArray[id];
//					        	   
//					        	   // no count returned for this node means it isn't in solr index because no records exist
//					        	   if (!count)  {
//					        		   count = 0;
//					        	   }
//					        	   
//					        	   updateNodeIndividualFacetCount(node, count);   
//					           }
//				             }
//			                 , false
//			               );
//									
				 // redraw entire tree after counts updated
				// tree.redraw();
			//}

		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});

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
		var fields = currentSearchTerms[j].split(":");		
	    var keyword = fields[2];			
		keywords.push(keyword);
	}
	
	return keywords;
}

//Remove the search term that the user has clicked.
function removeSearchTerm(ctrl)	{
	var currentSearchTermID = ctrl.id.replace(/\%20/g, " ").replace(/\%44/g, ",");
	var idx = currentSearchTerms.indexOf(currentSearchTermID);
	if (idx > -1)	{
		currentSearchTerms.splice(idx, 1);
		
		// check if there are any remaining terms for this category; remove category from list if none
		var fields = currentSearchTermID.split(":");
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
	
	//remove all pending jobs from the ajax queue
	//rwgAJAXManager.clear(true); (this was causing problems, so removing for now)
	
	
	openAnalyses = []; //all analyses will be closed, so clear this array
	
	
	jQuery("#search-ac").val("");
	
	currentSearchTerms = new Array();
	currentCategories = new Array();
	
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
		var fields2 = currentSearchTerms[j].split(":");
		var category2 = fields2[0];
		
		if (category == category2)  {
			found = true; 
			break;
		}
	}
	
	if (!found)  {
		currentCategories.splice(currentCategories.indexOf(category), 1);
	}
}

function unselectFilterItem(id) {
	jQuery('#' + id).removeClass('selected');
}

// ---

jQuery(document).ready(function() {
	
	jQuery('#sidebartoggle').click(function() {
		toggleSidebar();
    });
    
	//Filter browser
	jQuery('#filter-browser').dialog({
		autoOpen: false,
		width:200,
		height:400,
		position: [300, 30],
		resizable:true,
		show: 'fade',
		hide: 'fade',
		title: 'Filter Browser'
    });
	
	jQuery('.filtertitle').click(function () {
		jQuery('.filtercontent[name="' + jQuery(this).attr('name') + '"]').toggle('fast');
	});
	
	jQuery('.filteritem').click(function () {
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
			var idString = '[id="' + category + "|" + name + ":" + value + ":" + id + '"]';
			var element = jQuery(idString);
			removeSearchTerm(element[0]);
		}
	});
	
    addSelectCategories();
    addSearchAutoComplete();
    
//    jQuery("#filter-div").dynatree({
//    	initAjax: {  url: treeURL,
//    		data: { mode: "all" } 
//    	},
//    	checkbox: true,
//    	persist: false,
//    	selectMode: 3,
//    	minExpandLevel: 1,
//    	fx:{ height: "toggle", duration: 180 },
//    	autoCollapse: true,
//        onQuerySelect: function(flag, node) {   // event that is triggered prior to select actually happening on node
//        	
//        	if (!allowOnSelectEvent)  {
//        		return true;
//        	} 
//        	
//        	// before selecting node, save a copy of which nodes were selected
//        	// (note that this only gets done when select is called outside of the onSelect event since we're using the global allowOnSelectEvent flag above) 
//        	nodesBeforeSelect = node.tree.getSelectedNodes(false);
//
//        },
//        onSelect: function(flag, node) {
//        	// don't allow this event to be triggered by itself; return immediately if called as a result of the event itself
//        	if (!allowOnSelectEvent)  {
//        		return true;
//        	} 
//        	else  {
//        		allowOnSelectEvent = false;
//        	}
//        	
//        	// before re-synchronizing tree, make sure any nodes that have same key as this one have been properly
//        	// selected and deselected
//            
//        	var tree = node.tree;        	
//            var selectNode = node;   // store the node that was selected so we can reference unambiguously in tree.visit function below 
//
//        	// node is now selected, and any other changes to the tree have already happened (i.e. changes to children, parents,
//            //   cousins, second cousins, ...) so retrieve a copy of which nodes are now selected
//        	var nodesAfterSelect = node.tree.getSelectedNodes(false);
//           
//            // retrieve a list of those that are partially selected (e.g. no check box but a child or grandchild .. may be);
//            var nodesPartiallySelected = new Array();
//        	jQuery(".dynatree-partsel").each(
//        			function(){
//        		                  var node = jQuery.ui.dynatree.getNode(this);
//        		                  
//        		                  //  Selected nodes may also appear here - 
//        		                  //   make sure only those that are not selected are actually included
//        		                  //    in this list; 
//        		                  //  And don't add category nodes either
//        		                  if (!node.isSelected() && !node.data.isCategory)  {
//        		                      nodesPartiallySelected.push(node);
//        		                  }
//        		              }
//        			);
//        	
//        	
//        	
//        	// find nodes that are in After but were not in Before (i.e. Added)
//        	var nodesAdded = subtractNodes(nodesAfterSelect, nodesBeforeSelect);
//
//        	for (var i = 0; i < nodesAdded.length; i++) {
//        		var n = nodesAdded[i];
//        		// process node if it's not a category
//        		if (!n.data.isCategory)  {
//            		// loop through every node in tree and find copies, make sure all copies are selected        		
//    	            n.tree.visit(  function (node) {
//      	                              if ((n.data.key == node.data.key) && (n.data.uniqueTreeId != node.data.uniqueTreeId)) {
//    	            	            	  node.select(true);
//    	            	              } 
//    	            	           } 
//    	                         , false
//    	            		     );
//                }
//        		
//        		
//        	}
//
//        	// find nodes that are in Before but were not in After (i.e. Removed)
//        	var nodesRemoved = subtractNodes(nodesBeforeSelect, nodesAfterSelect);
//        	
//        	// We need to remove partially selected nodes from removed list, since we don't want to call the select(false) method on these;
//            //   if we did, then we would trigger all children to then be deselected in copies which isn't right;  instead the state of this
//        	//   node will be controlled by actions on the children 
//        	var nodesFullyRemoved = subtractNodes(nodesRemoved, nodesPartiallySelected);
//        	
//        	for (var i = 0; i < nodesFullyRemoved.length; i++) {
//        		var n = nodesFullyRemoved[i];         		
//
//        		// process node if it's not a category
//        		if (!n.data.isCategory)  {
//            		// loop through every node in tree and find copies, make sure all copies are DEselected
//    	            n.tree.visit(  function (node) {
//    	            	              if ((n.data.key == node.data.key) && (n.data.uniqueTreeId != node.data.uniqueTreeId)) {
//    	            	            	  node.select(false);
//    	            	              } 
//    	            	           } 
//    	                         , false
//    	            		     );
//                }
//        		
//        	}
//        	
//        	// reset flag to true now that we're past part that might trigger the event again	      
//    		allowOnSelectEvent = true;
//
//        	// Resynchronize entire tree when something changes
//        	// We need to do this because a select may affect other nodes than the one selected,
//        	//  but that doesn't trigger the onSelect event
//        	// Following call executes the syncNode function on all nodes in tree, except for root
//        	node.tree.visit(syncNode, false); 
//        	showSearchTemplate();
//        	showSearchResults();        	
//        },
//        onClick: function(node, event) {
//        	// if the user clicked outside the node, but in the tree, don't select/unselect the node
//        	// or if the node has a zero count and is not selected, don't allow it to be selected (but allow it to be expanded)       	
//            if( (node.getEventTargetType(event) == null) ||             	 
//           		(node.data.facetCount == 0 && !node.isSelected() && !(node.getEventTargetType(event) == 'expander'))
//              )
//            {
//                return false;// Prevent default processing
//            }
//            
//            //New code to generate popup because the categories don't have children.
//            generateBrowseWindow(node.data.title)
//            
//            return true;
//        },
//        onActivate: function(node){
//	    	if(!node.data.isCategory){
//	    		if(!node.isSelected()){
//	    			node.select(true);
//	    		}
//	    		else{
//	    			node.select(false);
//	    		}
//	    	
//	    	}
//	    	
//	    	node.deactivate();
//    	},
//    	onCustomRender: function(node) {
//    		// if not a category and count is zero, apply the custom class to node
//    		if (!node.data.isCategory && node.data.facetCount == 0)  {
//    			node.data.addClass = "zero-selected";
//    		}
//    		else
//    	    {
//    			node.data.addClass = null;
//    	    }
//    	},
//    	classNames: {connector: "dynatree-no-connector"}
//    });
    
    //Trigger a search immediately
    loadSearchFromSession();
	showSearchResults(); //reload the full search results for the analysis/study view


});

function loadSearchFromSession() {
	var sessionFilters = sessionSearch.split(",,,");
	
	for (var i = 0; i < sessionFilters.length; i++) {
		var item = sessionFilters[i];
		if (item != null && item != "") {
			var itemData = item.split("|");
			var itemSearchData = itemData[1].split(":");
			var searchParam = {id: itemSearchData[2], display: itemData[0], category: itemSearchData[0], keyword: itemSearchData[1]};
			addSearchTerm(searchParam, true);
		}
	}
}