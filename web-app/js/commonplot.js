var cohortWidth = 140;
var chartMargin = 55;
var titleMargin = 5;
var uniqueDivID = 0;
var titleFontSize = 16;
var titleFontSizeCTA = 10; 
var spaceBetweenPlots = 10;   // spacing between plots for CTA

function getScale(isCTA)  {
	return isCTA ? 0.75 : 1.0;
}

// retrieve the number of cohorts for a given analysis
function getCohortCount(jsonData)  {
	
	var cohortCount = 0;

	for (var key in jsonData)  {
		if (jsonData[key] && jsonData[key]['order'])  {   // if the object in the array doesn't have an order, not a cohort
			cohortCount++;
		}
	}

	return cohortCount;
}

function getTextFlowHeight(text, w, font, fontSize)  {

	jQuery("#testTextHeightDiv").empty();
	
	var titleSVG = d3.select("#testTextHeightDiv")
		.append("svg")
		.append("text")
		.attr("id", "testTextHeight")
		.attr("x", 0)
		.attr("y", 0 )
		.attr("text-anchor", "middle")
		.style("font", font);
	
	var textNode = document.getElementById("testTextHeight");
	
	// textFlow(myText,textToAppend,maxWidth,x,ddy,justified);
	var dy = textFlow(text,
			textNode,
			w,
			0,
			fontSize,
			false);
	
	jQuery("#testTextHeightDiv").empty();

	return dy;
}

// fimd the index of the analysis with the given key in the selected analysis array
function getSelectedAnalysisIndex(selectedAnalyses, analysisKey)  {
	for (var i=0; i<selectedAnalyses.length; i++)  {
		if (selectedAnalyses[i].id == analysisKey)  {
			return i;
		}
	}
	
	return false;
}

// create an object containing all the data needed for drawing a box or line plot and do some configuration (e.g. clear the div, set the range text boxes)
function setupPlotData(isBoxplot, allJsonData, forExport, analysisID, divId, isCTA, selectedAnalyses) {

	jQuery("#" + divId).empty();
	var allPlotData = {};  // contains a structure for each analysis
	var analysisIndex = 0;

	var orderedAnalysisKeys = new Array;

	// first determine the titles because resulting height affects all of the other data
	var allTitles = {}   // contains a structure for each title 
	var maxHTitle = 0;
    for (var analysisKey in allJsonData)  {
    	
        var probeName = allJsonData[analysisKey]['probeName'];		

    	var title;
    	var titleTooltip;
    	if (isCTA)  {
    		analysisIndex =  getSelectedAnalysisIndex(selectedAnalyses, analysisKey);	
    		title = selectedAnalyses[analysisIndex].title.replace(/_/g, ', ');
    		if (probeName)  {
    			title += " (" + probeName + ")";	
    		}
    	    titleTooltip = "";
    	}  else  {
    		var probeID = getActiveProbe(analysisID);
	 	    title = getGeneforDisplay(analysisID, probeID); 
	 	    titleTooltip = "View gene information";
    	}
    	
    	cohortCount =  getCohortCount(allJsonData[analysisKey]);
    	
    	var w = (cohortCount * cohortWidth + chartMargin) * getScale(isCTA); 
    		
		// do a test draw of the title so we can determine its size (don't scale title -- if scale is significantly different adjust font sizes to accomodate)
		var hTitle = getTextFlowHeight(title, w - titleMargin*2, getTitleFont(isCTA), getTitleFontSize(isCTA)) + getTitleFontSize(isCTA) * 2;
		
		if (hTitle > maxHTitle)  {
			maxHTitle = hTitle;
		}
    	
    	allTitles[analysisKey] =  {title:title, titleTooltip:titleTooltip, hTitle:hTitle} ;
    	
    }
    
	analysisIndex = 0;
	var maxHLegend = 0;
    for (var analysisKey in allJsonData)  {
    	
    	if (isCTA)  {
    		analysisIndex =  getSelectedAnalysisIndex(selectedAnalyses, analysisKey);	
    	}
    	
    	orderedAnalysisKeys[analysisIndex] = analysisKey;
    	
    	var title =allTitles[analysisKey].title;
    	var titleTooltip =allTitles[analysisKey].titleTooltip;
    		
    	jsonData =  allJsonData[analysisKey];

		var cohortArray = new Array();   // array of cohort ids
		var cohortLabels = new Array();   // array of cohort labels on x axis
		var cohortDisplayIds = new Array();   // array of cohort ids for legend
		var cohortDesc = new Array();    // array of cohort descriptions
		var cohortDisplayStyles = new Array();    // array of cohort display styles (i.e. number from 0..4)
	
		var gene_id = parseInt(jsonData['gene_id']);   // gene_id will be null if this is a protein since first char is alpha for proteins
		// loop through and get the cohort ids and description into arrays in the order they should be displayed
		for (var key in jsonData)  {
			if (jsonData[key] && jsonData[key]['order'])  {   // if the object in the array doesn't have an order, not a cohort
				// the "order" of the json objects starts with 1, so subtract 1 so it doesn't leave gap at start of array
				var arrayIndex = jsonData[key]['order'] - 1;
				cohortArray[arrayIndex] = key;
				cohortDesc[arrayIndex] = jsonData[key]['desc'];
				cohortDisplayStyles[arrayIndex] = jsonData[key]['order'] % cohortBGColors.length;
				
				var lbl;
				if (isCTA)  {  // cohort labels for CTA are 1A, 1B, ..., 1n, 2A,...2n, ...)
					var charCodeA = "A".charCodeAt(0);
					lbl = (analysisIndex + 1) + String.fromCharCode(charCodeA + arrayIndex);
				}
				else  {
					lbl = key;
				}
					
				cohortDisplayIds[arrayIndex] = lbl;
				cohortLabels[arrayIndex] = lbl + "(n=" +  jsonData[key]['sampleCount'] + ")";
				
				
			}
		}
	
		// highlight cohort descriptions expects and array starting at index 1
		var cohortDescExport = highlightCohortDescriptions([''].concat(cohortDesc), true);

		var statMapping = cohortArray.map(function(i)	{
			var data = jsonData[i]['data'];
			
			var statObject = new Object;
			
			// common properties for both line and box plots
			statObject.id = i;
			statObject.cohortDisplayStyle = jsonData[i]['order'] % cohortBGColors.length;
			statObject.cohortColor = cohortBGColors[statObject.cohortDisplayStyle];
			statObject.desc = jsonData[i]['desc'].replace(/_/g, ', ');
			statObject.descExport = cohortDescExport[jsonData[i]['order']].replace(/_/g, ', ');   // descExport is used for export and for the CTA plots
			statObject.sampleCount = jsonData[i]['sampleCount'];
			statObject.cohortDisplayId = cohortDisplayIds[jsonData[i]['order'] - 1];
			
			// Map the all four quartiles to the key (e.g. C1)  
			if (isBoxplot)  {	
				if (data)  {
					statObject.anyData = true,
					statObject.min = data[getRank(5, data.length)-1],
					statObject.max = data[getRank(95, data.length)-1],			
					statObject.median = data[getRank(50, data.length)-1],
					statObject.lq = data[getRank(25, data.length)-1],
					statObject.uq = data[getRank(75, data.length)-1]					
				}
				else  {					
					statObject.anyData = false,
					statObject.min = 0,
					statObject.max = 10,			
					statObject.median = 0,
					statObject.lq = 0,
					statObject.uq = 0					
				}
			}
			else  {
				if (data)  {
					statObject.anyData = true,
					statObject.mean = data['mean'];
					statObject.stdError = data['stdError'];
					statObject.min = statObject.mean - statObject.stdError;
					statObject.max = statObject.mean + statObject.stdError;
				}
				else {
					statObject.anyData = false,
					statObject.mean = 0;
					statObject.stdError = 0;
					statObject.min = 0;
					statObject.max = 10;					
				}
	
				var meanFormatted = parseFloat(statObject.mean);
				statObject.meanFormatted = meanFormatted.toFixed(4);
				
				var stdErrorFormatted = parseFloat(statObject.stdError);
				statObject.stdErrorFormatted = stdErrorFormatted.toFixed(4);
				
			}
			
			return statObject;		
		});
		
		var plotType;
		plotType = isBoxplot?'box':'line';
		
		//if the user is setting the range manually (and not cross trial analysis)
		if(!isCTA && jQuery('#' + plotType + 'plotRangeRadio_Manual_'+analysisID).is(':checked')){
			
			var yMin = parseFloat(jQuery('#' + plotType + 'plotRangeMin_'+analysisID).val());
			var yMax = parseFloat(jQuery('#' + plotType + 'plotRangeMax_'+analysisID).val());
	
			
		}else{
			//auto set range otherwise
			var yMin = statMapping[0].min;
			var yMax = statMapping[0].max;
			for (var idx=1; idx < statMapping.length; idx++)	{	
				yMin = statMapping[idx].min < yMin ? statMapping[idx].min : yMin;
				yMax = statMapping[idx].max > yMax ? statMapping[idx].max : yMax;
			}
			
			// Put in a rough switch so things can scale on the y axis somewhat dynamically
			if (yMax-yMin < 2)	{
				// round down to next 0.1
				yMin = Math.floor((yMin-0.2) * 10) / 10 ;
				
				// round up to next 0.1
				// and add another 0.01 to ensure that the highest tenths line gets included
				yMax = Math.ceil((yMax+0.2) * 10) / 10 + 0.01;
			} else	{
				yMin = Math.floor(yMin);
				yMax = Math.ceil(yMax);
			}
			
			//set the manual value textboxes with the current yMin and yMax
			if (!isCTA)  {
				jQuery('#' + plotType + 'plotRangeMin_'+analysisID).val(roundNumber(yMin,2));
				jQuery('#' + plotType + 'plotRangeMax_'+analysisID).val(roundNumber(yMax,2));
			}
			
		}

		var scale = getScale(isCTA);
		
		var margin = chartMargin * scale;
	
		var wChart = cohortArray.length * cohortWidth * scale;//generate the width dynamically using the cohort count	
		var hChart = 350 * scale;
		
    	var hTitle = maxHTitle;    // the space needed for title area 
    	var hTitleText = allTitles[analysisKey].hTitle;   // the actual height of the text of the title
		var titleYOffset = (maxHTitle - hTitleText)/2;  // used for centering the title vertically  

		var wTotal = wChart + margin;
		var hTotal = hChart + hTitle; 

		var hCohortDescExport = new Array;
		
		// if exporting (or for CTA), draw a legend; if not exporting legend is drawn outside of svg
		var hLegend = 0;
		if(forExport || isCTA){			
			hLegend = getLegendInfo(cohortDescExport, statMapping, wTotal, hCohortDescExport, isCTA);
		}
	
		hTotal = hTotal + hLegend;
		
		var numCohorts = cohortArray.length;
		
		var dataObject = {
				cohortArray:cohortArray, cohortLabels:cohortLabels, cohortDesc:cohortDesc, cohortDisplayStyles:cohortDisplayStyles,
				gene_id: gene_id, probeName:probeName, cohortDescExport:cohortDescExport, hCohortDescExport:hCohortDescExport, statMapping:statMapping,
				title:title, titleTooltip:titleTooltip, margin:margin, wChart:wChart, hChart:hChart, hTitle:hTitle, hTitleText:hTitleText, titleYOffset:titleYOffset,
				wTotal:wTotal, hTotal:hTotal, hLegend:hLegend, numCohorts:numCohorts, yMin:yMin, yMax:yMax,
				analysisIndex:analysisIndex
		};
		
 	    allPlotData[analysisKey] = dataObject;
 	    
		if (hLegend > maxHLegend) {
			maxHLegend = hLegend;
		}

    }

    allPlotData.orderedAnalysisKeys = orderedAnalysisKeys;
    allPlotData.maxHTitle = maxHTitle;
    allPlotData.maxHLegend = maxHLegend;
    
	return allPlotData;

}

// draw empty plots on a single svg element 
function drawEmptyPlots(allPlotData, forExport, divId, isCTA)  {

	var wTotal = 0;
	var hTotal = 0;
	// determine the starting coordinates for each plot, and figure out the total height and width for drawing them all on a single SVG
	for (var i=0; i<allPlotData.orderedAnalysisKeys.length; i++)  {
		
		var key = allPlotData.orderedAnalysisKeys[i];

		allPlotData[key].xOffset = wTotal;
		allPlotData[key].yOffset = 0;
		
		var w = allPlotData[key].wTotal;
		var h = allPlotData[key].hTotal;
		
		// use plot with max height as basis
		if (h>hTotal)  {
			hTotal = h;			
		}
		
		wTotal  = wTotal + w + spaceBetweenPlots;		
	}
	
	var root = d3.select("#" + divId)
		.append("svg")
		.attr("width", wTotal)
		.attr("height", hTotal);

	// draw each empty plot (and save the return value to the data structure)
	for (var i=0; i<allPlotData.orderedAnalysisKeys.length; i++)  {		
		var key = allPlotData.orderedAnalysisKeys[i];
		
		var ep = drawEmptyPlot(root, allPlotData[key], forExport, isCTA);
		
		allPlotData[key].emptyPlotData = ep;
		
	}	
}

// draw the basic line or box plot without any lines or boxes -- just title, axes, legend
// return an object the root svg tag, chart svg tag, the width of each band on chart, and the x and y axis domains 
function drawEmptyPlot(root, plotData, forExport, isCTA) {
	var svg = root.append("g")
		.attr("width", plotData.wTotal)
		.attr("height", plotData.hTotal)
		.attr("transform", "translate(" + plotData.xOffset + "," + plotData.yOffset + ")")
		;

	var bpYOffset = isCTA ? 0 : plotData.hLegend;  // legend goes after plot if CTA
	
	var bp = svg
		.append("g")
		.attr("transform", "translate(0," + bpYOffset + ")")
		.attr("class", "plot")
		;
	
	var yTitle = 15;
	if (forExport)  {
		yTitle = 20;
	}
	
	var nextTag;

	// create a link to show gene information if not on CTA
	if (!isCTA && plotData.gene_id)  {
		nextTag = bp.append("a")
    		.attr("xlink:href", function(d) {return "javascript:showGeneInfo('"+plotData.gene_id +"');"});
	}
	else  {
		nextTag = bp;
		
	}
	
	var analysisTitleId = "analysisTitle" + uniqueDivID++ ;
	var titleText = nextTag
	        .append("text")
	        .attr("id", analysisTitleId)
	        .attr("x", plotData.margin + plotData.wChart/2)
	        .attr("y", yTitle + plotData.titleYOffset )
	        .attr("class", "title")
	        .attr("text-anchor", "middle")
	        .style("font", getTitleFont(isCTA))   // need to apply style here when drawn so that textFlow draws correctly 
	        ;
	
	var textNode = document.getElementById(analysisTitleId);
	var dy = textFlow(plotData.title,
			textNode,
			plotData.wTotal - titleMargin*2,
			plotData.wTotal/2 + titleMargin,
			getTitleFontSize(isCTA),
			false);	    
	
	if (( (!isCTA && plotData.gene_id) || (isCTA) ) && !forExport && plotData.titleTooltip!="")  {			
	    	//Add tooltip for title, if:
		    // not for export, tooltip text is non-empty, AND  one of these conditions:
			//     a.  is a cross trial analysis
			//     b.  not a CTA and gene id is defined
			titleText.append('svg:title').text(plotData.titleTooltip);
	}
		
	var chart = bp.append("g")
		.attr("transform", "translate(" + plotData.margin + "," +  plotData.hTitle + ")")
		.attr("class", "plot");
	
	// x axis labels
	var x=d3.scale.ordinal()
	  .domain(plotData.cohortLabels)	  
	  .rangeBands([0, plotData.wChart])
	  ;
	
	var y = d3.scale.linear().domain([plotData.yMin, plotData.yMax]).range([plotData.hChart - plotData.margin, 0]);
		
	var xAxis = d3.svg.axis()
		.scale(x)
		.orient("bottom");
		  					
	 var yAxis = d3.svg.axis()
		.scale(y)
		.tickSize(-plotData.wChart)
		.orient("left");
		
	// add an additional offset for the x axis for export 
	var yOffsetForExport = 0;
	if (forExport)  {
		yOffsetForExport = 10;
	}

  // Add the x-axis.
  var xAxisTag = chart.append("g")
      .attr("class", "xaxis")
      .attr("transform", "translate(0," + (plotData.hChart - plotData.margin + yOffsetForExport) + ")")
      .call(xAxis)
      ;

  // add tooltip for x axis labels
  if (!forExport)  {
	  xAxisTag.selectAll("text")
	    .append('svg:title')
	    .data(plotData.statMapping)
      	.text(function(d, i) {return d.desc;})
      	;		  
  }

  // Add the y-axis.
  chart.append("g")
      .attr("class", "yaxis")
      .attr("transform", "translate(0,0)")
      .call(yAxis);		

  // Y axis label
  bp.append("text")
    .attr("text-anchor", "middle")
    .attr("transform", "translate(" + 10 + "," + (plotData.hTitle + (plotData.hChart)/2 ) + ") rotate(-90)")  // note: space between translate/rotate clauses - if not there, export has a problems
    .text("log2 intensity");
		 
  var wBand = x.rangeBand();

  if (forExport || isCTA)  {
	  var legendYOffset = isCTA ? plotData.hChart + plotData.hTitle : 0;  // legend goes after plot if CTA
	  drawSVGLegend(svg, 10, legendYOffset, plotData.statMapping, isCTA, forExport);
  }


  return {svg:svg, chart:chart, wBand:wBand, x:x, y:y};
}

function getTitleFont(isCTA)  {
	
	if (isCTA)  {
		return "bold " + titleFontSizeCTA + "px sans-serif";
	}
	else  {		
		return "bold " + titleFontSize + "px sans-serif";
	}
}

function getTitleFontSize(isCTA)  {
	
	if (isCTA)  {
		return titleFontSizeCTA;
	}
	else  {		
		return titleFontSize;
	}
}

function applyPlotStyles(svg, isCTA)  {
	// note: export doesn't recognize classes/styles in CSS file, need to apply directly to objects
	svg.selectAll(".plot")
		.style("font", "14px sans-serif")
		;
	
	svg.selectAll(".plot .title")
		.style("fill", "#065B96")
		.style("font", getTitleFont(isCTA))
		;

	svg.selectAll(".plot .plotLines")
		.style("stroke-width", "1px")
		.style("stroke", "#000")
		.style("shape-rendering", "crispEdges")
		;
	
	svg.selectAll(".plot .yaxis path, .plot .yaxis line")
		.style("fill", "none")
		.style("stroke", "#ccc")
		.style("stroke-width", "0px")
		.style("shape-rendering", "crispEdges")
		;

	svg.selectAll(".plot .yaxis line.tick")
		.style("fill", "none")
		.style("stroke", "#CCCCCC")
		.style("stroke-width", "1px")
		.style("shape-rendering", "crispEdges")
		;
	
	
	svg.selectAll(".plot .xaxis path, .plot .xaxis line")
		.style("fill", "none")
		.style("stroke-width", "0px")
		;		
	
	svg.selectAll(".plot rect")
		.style("stroke-width", "1px")
		.style("stroke", "#000")
		.style("shape-rendering", "crispEdges")
		;
	
	svg.selectAll(".plot .linePlotPoints, .plot .errorBar")
		.style("stroke", "#000000")
		.style("shape-rendering", "crispEdges")
	;

	svg.selectAll(".plot .linePlotPath")
		.style("shape-rendering", "auto")
		.style("fill", "none")
		.style("stroke", "black")
		.style("stroke-width", "1px")
	;

	svg.selectAll(".plot .hoverText")
		.style("font", "10px sans-serif")
		.style("stroke", "none")
		.style("display", "none")
		;
	
	svg.selectAll(".plot .noDataText")
		.style("font", "10px sans-serif")
		.style("stroke", "none")
	;
}

//export the CTA boxplot
function exportBoxPlotCTAImage()
{	
		var svgID=  "#xtBoxplot"
		
		exportCanvas(svgID);					
}