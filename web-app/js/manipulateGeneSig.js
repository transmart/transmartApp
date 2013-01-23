/**
 * Entry function into the manipulate view scripts.
 */
var visualize = function(geneLists, action, labels){
	
	var resultArray
	JS.require('JS.Set', function(){
		//-------Set operations on the incoming lists.
		//Process gene lists and return a sets object with genelist id as the key and the set of genes as the value.
		var sets = processGeneLists(geneLists);
		//Perform the specified set operation and return result set in an array.
		resultArray = operate(sets, action);
		//-------Set operations done 
		
		//Set the result as default value in the results text box.
		populateResults(resultArray.toString(), action);
		
		//------Visualize the incoming lists.
		mapSetIndexToGeneListId(geneLists);
		//Calculate which genes are in the 7 possible regions on the venn diagram.
		var region = calculateRegionMembers(sets);
		//Figure out the titles (tooltips) for each of the 7 possible regions on the venn diagram.
		var regionTitles = calculateRegionTitles(region);
		//Map each region to the appropriate color
		initializeColorMapping(region);
		//-------Set Visualization done
		
		//Render the venn diagram.
		draw(region, regionTitles, labels, action);
	});
}

//**********Visualization Set Operations Functions**********

/**
 * We deal with three sets.
 * Each set has an index (1 to 3)
 * This function creates a global variable to hold the mapping between set index and
 * the gene list id it represents. 
 */
function mapSetIndexToGeneListId(geneLists){
	//Global variable: array setIndex
	setIndex = new Array();
	var count = 1;
	for(geneListId in geneLists){
		setIndex[count++] = geneListId;
	}
}

/**
 * Returns the region object which contains mapping between region id and the number of elements in that region
 * @param sets
 * @returns {region}
 */
function calculateRegionMembers(sets){
	var region = new Object();
	//Region 1
	region['1']=calculateOuterRegionsMembers(sets, 1, 2, 3);
	
	//Region 2
	region['2']=calculateOuterRegionsMembers(sets, 2, 3, 1);
	
	//Region 3
	region['3']=calculateOuterRegionsMembers(sets, 3, 1, 2);
	
	//Region 123
	region['123']=calculateInnerRegionMembers(sets);
	var innerRegionSet = new JS.Set(region['123']);
	
	//Region 12
	region['12']=calculateMiddleRegionsMembers(sets, 1, 2, innerRegionSet);
	
	//Region 13
	region['13']=calculateMiddleRegionsMembers(sets, 1, 3, innerRegionSet);
	
	//Region 23
	region['23']=calculateMiddleRegionsMembers(sets, 2, 3, innerRegionSet);
	
	return region;
}

/**
 * Goes through all 7 regions and picks the first 5 (if applicable) elements to show as title (tooltip on mouse hover)
 */
function calculateRegionTitles(region){
	var regionTitles = new Object();
	
	['1', '2', '3', '12', '13', '23', '123'].forEach(function(regionId){
		regionTitles[regionId]=region[regionId].slice(0,5);
		if(region[regionId].length>5){
			regionTitles[regionId]+="..."
		}
	});
	
	return regionTitles;
}

function calculateOuterRegionsMembers(sets, primaryIdx, firstSubIdx, secondSubIdx){
	var primarySet = sets[setIndex[primaryIdx]];
	//Return a blank array if the primary set is undefined .Happens when we are dealing with just 2 sets.
	if(!primarySet){
		return [];
	}
	var firstSubSet = sets[setIndex[firstSubIdx]];
	var secondSubSet = sets[setIndex[secondSubIdx]];
	
	//Intersect primary and first sub
	var subset = new Object();
	subset[setIndex[primaryIdx]]=primarySet;
	subset[setIndex[firstSubIdx]]=firstSubSet;
	var aSet = operate(subset, "intersection");
	aSet = new JS.Set(aSet);
	
	//Intersect primary and second sub
	subset = new Object();
	subset[setIndex[primaryIdx]]=primarySet;
	subset[setIndex[secondSubIdx]]=secondSubSet;
	var bSet = operate(subset, "intersection");
	bSet = new JS.Set(bSet);
	
	//Union the two results
	var cSet = new Object();
	cSet.a=aSet;
	cSet.b=bSet;
	var dSet = operate(cSet, "union");
	dSet = new JS.Set(dSet);
	
	//Remove the union from primary
	var eSet = sets[setIndex[primaryIdx]].difference(dSet);
	
	return eSet.toArray();
}

function calculateMiddleRegionsMembers(sets, firstSetIdx, secondSetIdx, innerSet){
	var firstSet = sets[setIndex[firstSetIdx]];
	var secondSet = sets[setIndex[secondSetIdx]];
	
	//Return a blank array when either of the sets are undefined. Happens when we are dealing with just 2 sets.
	if(!firstSet||!secondSet){
		return [];
	}
	
	//Intersect first and second set
	var subset = new Object();
	subset[setIndex[firstSetIdx]]=firstSet;
	subset[setIndex[secondSetIdx]]=secondSet;
	var aSet = operate(subset, "intersection");
	aSet = new JS.Set(aSet);
	
	var cSet = aSet.difference(innerSet);
	return cSet.toArray();
}

function calculateInnerRegionMembers(sets){
	if(setIndex[3]){
		return operate(sets, "intersection");
	}else{
		return [];
	}
}


/**
 * Uses JS.Set to create set objects 
 * @param geneLists
 */
function processGeneLists(geneLists){
	var sets=new Object();
	//Read genes array from json object
	for(geneListId in geneLists){
		var set = new JS.Set(geneLists[geneListId]);
		sets[geneListId]=set;
	}

	return sets;
}

/**
 * Reads in an object with all sets and performs the specified action
 * No limit on number of elements in a set.
 * @param sets
 * @param action
 */
function operate(sets, action){
	var resultArray;
	var resultSet;
	var count = 0;
	
	if(action=='union' || action == 'intersection' || action == 'difference'){
		for(i in sets){
			if(count===0){
				resultSet = sets[i]
			}else{
				var evalResult = "resultSet = resultSet."+action+"(sets[i]);";
				eval(evalResult);
			}
			count++;
		}
		resultArray = resultSet.toArray();
	}else if(action == 'concat'){
		resultArray = new Array();
		for(i in sets){
			resultSet = sets[i];
			resultArray = resultArray.concat(resultSet.toArray());
		}
	}else if(action == 'unique'){
		var concatArray = operate(sets, 'concat');
		//keep doing this.
	}

	console.log(resultArray);
	return resultArray;
}

//**********Visualization Venn Diagram Rendering functions**********

/**
 * Initialize the global array that will hold a list of active region ids
 * Draw the venn diagram.
 */
function draw(region, regionTitles, labels, action){
	populateActiveRegionIds(action, region);
	
	//Clear out div before drawing SVG again.
	jQuery('#svg').empty();
	
	//Draw the venn diagram
	var w = 480, h = 290;

	var svg = d3.select("#svg").append("svg:svg")
    	.attr("width", w)
    	.attr("height", h);
	
	var defs = svg.append("svg:defs");

	defs.append("svg:clipPath")
    .attr("id", "circle1")
    .append("svg:circle")
    .attr("cx", 200)
    .attr("cy", 110)
    .attr("r", 90);

	defs.append("svg:clipPath")
    .attr("id", "circle2")
    .append("svg:circle")
    .attr("cx", 300)
    .attr("cy", 110)
    .attr("r", 90);

	if (setIndex[3])  {		
		defs.append("svg:clipPath")
	    .attr("id", "circle3")
	    .append("svg:circle")
	    .attr("cx", 250)
	    .attr("cy", 180)
	    .attr("r", 90);
	}

	svg.append("svg:rect")
    .attr("clip-path", "url(#circle1)")
    .attr("width", w)
    .attr("height", h)
    .attr("id", "1")
    .style("fill", determineRegionColor("1", activeRegionIds))
    .on("click", function(){mouseclick(region, this)})
    .append("svg:title")
    .text(regionTitles["1"]);

	svg.append("svg:rect")
	.attr("clip-path", "url(#circle2)")
    .attr("width", w)
    .attr("height", h)
    .attr("id", "2")
    .style("fill", determineRegionColor("2", activeRegionIds))
    .on("click", function(){mouseclick(region, this)})
    .append("svg:title")
    .text(regionTitles["2"]);

	if (setIndex[3])  {		
		svg.append("svg:rect")
	    .attr("clip-path", "url(#circle3)")
	    .attr("width", w)
	    .attr("height", h)
	    .attr("id", "3")
	    .style("fill", determineRegionColor("3", activeRegionIds))
	    .on("click", function(){mouseclick(region, this)})
	    .append("svg:title")
	    .text(regionTitles["3"]);
	}

	svg.append("svg:g")
    .attr("clip-path", "url(#circle1)")
    .append("svg:rect")
    .attr("clip-path", "url(#circle2)")
    .attr("width", w)
    .attr("height", h)
    .attr("id", "12")
    .style("fill", determineRegionColor("12", activeRegionIds))
    .on("click", function(){mouseclick(region, this)})
    .append("svg:title")
    .text(regionTitles["12"]);

	if (setIndex[3])  {		
		svg.append("svg:g")
	    .attr("clip-path", "url(#circle2)")
	    .append("svg:rect")
	    .attr("clip-path", "url(#circle3)")
	    .attr("width", w)
	    .attr("height", h)
	    .attr("id", "23")
	    .style("fill", determineRegionColor("23", activeRegionIds))
	    .on("click", function(){mouseclick(region, this)})
	    .append("svg:title")
	    .text(regionTitles["23"]);
	}

	if (setIndex[3])  {			
		svg.append("svg:g")
	    .attr("clip-path", "url(#circle3)")
	    .append("svg:rect")
	    .attr("clip-path", "url(#circle1)")
	    .attr("width", w)
	    .attr("height", h)
	    .attr("id", "13")
	    .style("fill", determineRegionColor("13", activeRegionIds))
	    .on("click", function(){mouseclick(region, this)})
	    .append("svg:title")
	    .text(regionTitles["13"]);
	}
	
	if (setIndex[3])  {		
		svg.append("svg:g")
	    .attr("clip-path", "url(#circle3)")
	    .append("svg:g")
	    .attr("clip-path", "url(#circle2)")
	    .append("svg:rect")
	    .attr("clip-path", "url(#circle1)")
	    .attr("width", w)
	    .attr("height", h)
		.attr("id", "123")
	    .style("fill", determineRegionColor("123", activeRegionIds))
	    .on("click", function(){mouseclick(region, this)})
	    .append("svg:title")
	    .text(regionTitles["123"]);
	}	
	displayCount(region, svg);
	
	displayLabels(svg);
	
	displayLegend(labels);
}

/**
 * display counts for each area
 */
function displayCount(region, svg){
	
	var y12 = 80;   // the y coordinate for the counts for regions 1 and 2 and their intersection
	
	if (!setIndex[3])  {
		// move this down to center if there are only 2 regions
		y12 = 120;  
	}
	
	display(svg, 150, y12, region['1'].length);
	display(svg, 245, y12, region['12'].length);
	display(svg, 340, y12, region['2'].length);
	
	if (setIndex[3]) {		
		display(svg, 190, 160, region['13'].length);//
		display(svg, 245, 160, region['123'].length);
		display(svg, 290, 160, region['23'].length);
		display(svg, 240, 240, region['3'].length);//
	}
}

/**
 * Display labels for each set
 * @param labels
 */
function displayLabels(svg){
	display(svg, 50, 80, "Gene Sig 1");
	display(svg, 390, 80, "Gene Sig 2");
	if (setIndex[3]) {		
		display(svg, 215, 280, "Gene Sig 3");
	}
}

/**
 * Dispalys the legend table
 * @param labels
 */
function displayLegend(labels){
	
	if (setIndex[3]) {
		jQuery("#geneSig3LegendRow").show();	
	}
	else {
		jQuery("#geneSig3LegendRow").hide();			
	}

	jQuery("#geneSig1Name").text("");
	jQuery("#geneSig2Name").text("");
	jQuery("#geneSig3Name").text("");
	
	jQuery("#geneSig1Name").text(labels[setIndex[1]]);
	jQuery("#geneSig2Name").text(labels[setIndex[2]]);
	if (setIndex[3]) {
		jQuery("#geneSig3Name").text(labels[setIndex[3]]);
	}
}

function display(svg, x, y, count){
svg.append("svg:text")
.attr("x",x)
.attr("y",y)
.text(count);
}

/**
 * add label for each set
 */
function addLabel(){
	addCount(svg, 50, 80, "List1");
	addCount(svg, 400, 80, "List3");
	addCount(svg, 225, 290, "List2");
}

/**
 * Handle mouseclick events for different regions in the Venn Diagram.
 * @param d
 * @param i
 */
function mouseclick(region, clickedRegion){
	//Add or remove the region id from the active ids array
	var index = activeRegionIds.indexOf(clickedRegion.id);
	//Add if doesn't allready exist
	if(index<0){
		activeRegionIds.push(clickedRegion.id);
		clickedRegion.style.fill= selectedColorMapping[clickedRegion.id];
	}else{//Remove if it does exist
		activeRegionIds.splice(index,1);
		clickedRegion.style.fill= unselectedColorMapping[clickedRegion.id];
	}
	
	//Clear out the results text area
	populateResults('');
	
	//Recreate the selection and put it in the test area
	var results = '';
	for(var i = 0;i<activeRegionIds.length; i++){
		var regionMembers = region[activeRegionIds[i]];
		if(regionMembers.length>0){
			if(results==''){
				results = results+region[activeRegionIds[i]];
			}else{
				results = results+','+region[activeRegionIds[i]];
			}
		}
	}
	populateResults(results);
	
}

/**
 * Maps each region to a specific color.
 * If a region has no members it is mapped to white.
 */
function initializeColorMapping(region){
	selectedColorMapping = new Object();
	
	unselectedColorMapping = new Object();
	
	selectedColorMapping = mapColor(region, selectedColorMapping, "1", "#84A3FF");
	selectedColorMapping = mapColor(region, selectedColorMapping, "2", "#F76060");
	selectedColorMapping = mapColor(region, selectedColorMapping, "3", "#F5F847");
	selectedColorMapping = mapColor(region, selectedColorMapping, "12", "#F43471");
	selectedColorMapping = mapColor(region, selectedColorMapping, "13", "#69BEC3");
	selectedColorMapping = mapColor(region, selectedColorMapping, "23", "#FF7721");
	selectedColorMapping = mapColor(region, selectedColorMapping, "123", "#FF1818");
	
	unselectedColorMapping = mapColor(region, unselectedColorMapping, "1", "#A4BAF7");
	unselectedColorMapping = mapColor(region, unselectedColorMapping, "2", "#F79797");
	unselectedColorMapping = mapColor(region, unselectedColorMapping, "3", "#F1F380");
	unselectedColorMapping = mapColor(region, unselectedColorMapping, "12", "#CE7894");
	unselectedColorMapping = mapColor(region, unselectedColorMapping, "13", "#9DB4B4");
	unselectedColorMapping = mapColor(region, unselectedColorMapping, "23", "#F09154");
	unselectedColorMapping = mapColor(region, unselectedColorMapping, "123", "#CB7575");
}

/**
 * Maps the specified color to the specified region.
 * Adds the mapping to the specified map
 * @param region
 * @param color
 */
function mapColor(region, map, regionId, color){
	if(region[regionId].length>0){
		map[regionId]=color;
	}else{
		map[regionId]="#CCCCCC"
	}
	return map;
}

/**
 * Determines based on action and region id, what the color of the given region is (between selected and unselected).
 * @param regionId
 * @param action
 * @returns
 */
function determineRegionColor(regionId, activeRegionIds){
	if(activeRegionIds.indexOf(regionId)<0){
		return unselectedColorMapping[regionId];		
	}
	
	return selectedColorMapping[regionId];
}

/**
 * Determine the initial list of active regions based on the user selected action
 */
function populateActiveRegionIds(action, region){
	//Global variable: Initialize the global array that will hold a list of active region ids
	activeRegionIds = new Array();
	
	['1', '2', '3', '12', '13', '23', '123'].forEach(function(regionId){
		if(region[regionId].length>0 && (action=='union'|| action=='concat' || (action=='intersection' && (regionId=='12' || regionId=='123')))){
			activeRegionIds.push(regionId);
		}
	});
}

//**********Visualization Form handling functions**********

/**
 * Populates the results text box.
 */
function populateResults(results, action){
	var delimitingChar="\n"
	if(results!=''){
		var geneSigItems = results.split(",");
		results='';
		for(var i =0; i<geneSigItems.length; i++){
			results = results+geneSigItems[i]+delimitingChar;
		}
	}
	jQuery("#manipulationResults").val(results);
	
	if(action){
		if(action=='concat'){
			action = 'concatenation';
		}
		jQuery("#actionLabel").text(action);
	}else{
		jQuery("#actionLabel").text("custom");
	}

}

/**
 * Handle the reset button click event on the manipulate view.
 * Clear out the text area
 * Clear out selections
 * Redraw venn diagram
 */
function resetVisualization(){
	//Clear out text area
	populateResults('');
	//Clear out the global variable that keeps track of currently selected(active) regions 
	activeRegionIds = new Array();
	//Remove any highlighting from the venn diagram 
	['1', '2', '3', '12', '13', '23', '123'].forEach(function(regionId){
		var currentRegion = document.getElementById(regionId);
		currentRegion.style.fill= unselectedColorMapping[regionId];
	});
}

/**
 * Clears out the New Gene List name field
 */
function resetGeneListName(){
	jQuery("#newGeneListName").val("");
}

/**
 * Exports the Venn diagram.
 */
function exportSVGImage(){
	
}
