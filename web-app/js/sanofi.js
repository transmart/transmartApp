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
function addSearchTerm(searchTerm)	{
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
	var tree = jQuery("#filter-div").dynatree("getTree");

	tree.visit(  function selectNode(node) {
		             if (node.data.key == key)  {
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

// ---

jQuery(document).ready(function() {
    
    addSelectCategories();
    addSearchAutoComplete();

});

