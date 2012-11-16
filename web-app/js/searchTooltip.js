var xOffset = 20;
var yOffset = 20;		

this.registerSearchTooltipEvents = function(){
	// create the method for the hover event for tooltips on the favorites for faceted searches
	jQuery("a.searchTooltip").hoverIntent(
		{
			over:function(e){
	
				// extract the search id from the element id
				var elementId = e.currentTarget.id;
				var idPos = elementId.indexOf('_') + 1;
				var id = elementId.substr(idPos);
		
				// retrieve the html for the tooltip
				var html = getSearchTooltip(id, e);
				
			},
			out: function(){
				jQuery("#searchTooltip").remove();
			},
			interval:500
		});
	
};

function showSearchTooltip(html, e)  {

	// create the div tag which will hold tooltip
	jQuery("body").append("<div id='searchTooltip'></div>");
	
	jQuery("#searchTooltip")
		.css("z-index", 10000)
		.html(html)
		.css("top",(e.pageY - xOffset) + "px")
		.css("left",(e.pageX + yOffset) + "px")
		.fadeIn(200)
		;

	jQuery("#searchTooltip").mousemove(function(){
		jQuery("#searchTooltip").remove();
	}
	);
	

	
}


//retrieve the saved favorites with the given id and generate the HTML to show in the tooltip 
function getSearchTooltip(id, e)  {

	rwgAJAXManager.add({
		url:loadSearchURL,
		data: {id: id},   
		timeout:60000,
		success: function(response) {
			
			if (response['success'])  {
				var searchTerms = response['searchTerms'] 
				var analyses = response['analyses'] 
				var keywordCount = response['keywordCount'] 
				var analysisCount = response['analysisCount'] 
				var termsNotFound = response['termsNotFound'] 
				
				// create 2 arrays that act the same as the global arrays used to populate the search terms section
				var categories = new Array();
				var keywords = new Array();
				
				for (var i=0; i<keywordCount; i++)  {

					addKeyword(searchTerms[i], categories, keywords);

				}
				
				// now we have 2 arrays which are setup to simulate the global arrays; pass these into the function to generate the HTML for the tooltip 
				var html =  '<h3 class="searchTooltipTitle" >Search Keyword(s):</h3>' + showSearchTemplate(categories, keywords);
				
				if (analysisCount>0)  {
					html += '<br /></br><h3 class="searchTooltipTitle" >Analyses:</h3>';
					for (var j=0; j<analysisCount; j++)  {
						var analysisIndex = j + 1;
						html += "<div class='xtSelectedAnalysesListLegendItem'>";						
						html += "<table>";						
						html += "<tr><td>";						
						html += "<span class='analysisNum'>" +  analysisIndex + "</span>";
						html += "</td><td>";
						html += "<span class='result-trial-name'>" + analyses[j]["studyId"] + "</span>";
						html += ": " + analyses[j]["title"];
						html += "</td></tr></table>"
						html += "</div>"
						
					}
				}
				
				// now show the tooltip
				jQuery(document).ready(function () {
					showSearchTooltip(html, e);
				});
				
			}
			else  {
				alert(response['message']);  // show message from server  
			}
 		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});

}
