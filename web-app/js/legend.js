var svgLegend = {
		boxWidth : 30,
		boxHeight : 25,
		boxSpacing : 5,
		margin : 5,
		fontSize : 12,
		fontSizeCTA : 10
}

var svgAnalyisLegend = {
		fontSize: 12,
		fontStyle: "sans-serif",
		spacing: 5,
		margin: 5
}		

function getLegendFont(isCTA)  {
	
	if (isCTA)  {
		return svgLegend.fontSizeCTA + "px sans-serif";
	}
	else  {		
		return svgLegend.fontSize + "px sans-serif";
	}
}

function getLegendFontSize(isCTA)  {
	
	if (isCTA)  {
		return svgLegend.fontSizeCTA;
	}
	else  {		
		return svgLegend.fontSize;
	}
}


function drawSVGLegend(svg, cohortLegendOffsetX, cohortLegendOffsetY, statMapping, isCTA, forExport)  {

 	 	var cohortLegendGroup = svg.append("svg:g")
	 	  .attr("class", "cohortLegendGroup")
	 	  .attr("transform", "translate(" + cohortLegendOffsetX + "," + cohortLegendOffsetY + ")")
	 	  ;

	    var boxWidth = svgLegend.boxWidth;
	    
	    var boxHeight = svgLegend.boxHeight;
	    
	    var boxSpacing = svgLegend.boxSpacing;

	    // Cohort legend
		cohortLegendGroup.selectAll(".legendRect")
		    .data(statMapping)
		    .enter()
			.append("rect")
			.attr("x", 0)
			.attr("y", function (d, i) { return d.yDescExport})
			.attr("width", boxWidth)
			.attr("height", boxHeight)
			.attr('class', 'legendRect')
		    .style('fill', function (d) { return d.cohortColor; })
			;

		
		// cohort legend text inside rectangles
		cohortLegendGroup.selectAll(".legendCohortId")
			.data(statMapping)
			.enter()
			.append("text")
			.attr("x", boxWidth/2)
			.attr("y", function (d, i) { return d.yDescExport + boxHeight/2 + 3})
			.attr("dy", ".35em")
	        .attr("text-anchor", "middle")
			.attr('class', 'legendCohortId')
	        .text(function (d, i) { return d.cohortDisplayId ? d.cohortDisplayId : d.id; } )
	        ;	
		
		// cohort legend text descriptions
		labelX = boxWidth + boxSpacing;
		
		var legendFont =  getLegendFont(isCTA);
		var legendFontSize = getLegendFontSize(isCTA);
		
		// loop through each cohort and draw the description
		for (var i=0; i<statMapping.length; i++)  {
			var divId = "cohortLegendDescription" + uniqueDivID++;
			
			var cohortText = cohortLegendGroup.append("text")
		        .attr("id", divId)
				.attr("x", boxWidth + boxSpacing)
				.attr("y", function () { return statMapping[i].yDescExport + legendFontSize;})
				.attr('class', 'legendCohortDesc')
		        .attr("text-anchor", "start")
		        .style("font", legendFont);   // need to apply style here when drawn so that textFlow draws correctly 

			var textNode = document.getElementById(divId);
			var dy = textFlow(statMapping[i].descExport,
				textNode,
				statMapping[i].wDescExportText,
				boxWidth + boxSpacing,
				legendFontSize,
				false);
			
			if (!forExport)  {
				cohortText.append('svg:title').text(statMapping[i].desc);
			}

		
		}
		
		applyLegendStyles(cohortLegendGroup, isCTA);
}


function applyLegendStyles(svg, isCTA)  {
	// note: export doesn't recognize classes/styles in CSS file, need to apply directly to objects
	svg.selectAll(".legendRect")
		.style("stroke", "#000")    // color of borders around rectangles
		.style("stroke-width", 1)    // width of borders around rectangles
	    .style("shape-rendering", "crispEdges")	
		;
	
	svg.selectAll(".legendCohortId")
	    .style("fill", "#000")
	    .style("font-size", "12px")
	    .style("font-family", "sans-serif")
		;

	svg.selectAll(".legendCohortDesc")
	    .style("fill", "#000")
    	.style("font", getLegendFont(isCTA))
		;
	
}

// draw the legend shown on screen 
function drawScreenLegend(numCohorts, cohortArray, cohortDesc, cohortDisplayStyles, type, analysisID, isSA) {

	if ((type == 'boxplot') || (type == 'lineplot'))  {
		//need to add a blank entry at the beginning of the arrays for use by drawCohortLegend and highlightCohortDescriptions
		cohortArray = [''].concat(cohortArray);
		cohortDesc = [''].concat(cohortDesc);
		cohortDisplayStyles = [''].concat(cohortDisplayStyles);
	}
	
	var saPrefix  = '';
	if (isSA)  {
		saPrefix  = 'sa'
	}
	
	jQuery("#" + saPrefix + type + "Legend_" + analysisID).html(drawCohortLegend(numCohorts, cohortArray, cohortDesc, cohortDisplayStyles));
}

//Helper function to draw the legend for the cohorts in the visualization panel
function drawCohortLegend(numCohorts, cohorts, cohortDescriptions, cohortDisplayStyles)	{
	
	cohortDescriptions = highlightCohortDescriptions(cohortDescriptions);
	
	var pCohortAll = "<table class='cohort_table'>"
	var classIndex = null;
	var pCohort = "";
	for(var i=1; i<=numCohorts; i++) {
		pCohort = "<tr><td style='width:40px'><p class='cohort' style='background-color:" + cohortBGColors[cohortDisplayStyles[i]]  + "'>" +cohorts[i] +"</p></td><td><p class='cohortDesc'>"+cohortDescriptions[i].replace(/_/g, ', ')+'</p></td>';
		pCohortAll = pCohortAll +  pCohort;
	}
	return pCohortAll + "</table>	";
}

// set information needed for drawing legend, e.g. height of each cohort, total height; return the height of the legend 
function getLegendInfo(cohortDescExport, statMapping, wTotal, hCohortDescExport, isCTA)  {
	var hLegend = 0;
	
	// retrieve the height that the cohort descriptions will be when drawn in svg with textFlow()
	var legendFont = getLegendFont(isCTA);
	var legendFontSize = getLegendFontSize(isCTA);
	var wLegendText = wTotal - svgLegend.boxWidth - svgLegend.margin*2;
	
	var svgLegendHeight = svgLegend.margin;
	for (var i=1; i<cohortDescExport.length; i++)  {   
		 statMapping[i - 1].yDescExport = svgLegendHeight;
		 statMapping[i - 1].wDescExportText = wLegendText;

		 legendCohortDesc = cohortDescExport[i];
		 var hText = getTextFlowHeight(legendCohortDesc, wLegendText, legendFont, legendFontSize) + legendFontSize;
		 
		 // height of cohort is greater of the box height or the height of the text
		 hCohortDescExport[i] = (hText > svgLegend.boxHeight ? hText :  svgLegend.boxHeight) + svgLegend.boxSpacing;

		 statMapping[i - 1].hDescExport = hCohortDescExport[i];

		 if (i > 1)   {  // add spacer if not on 1st one 
			 svgLegendHeight += svgLegend.boxSpacing;					 
		 }
		 
		 svgLegendHeight += hCohortDescExport[i];

	}
	svgLegendHeight += svgLegend.margin;
						
	hLegend = svgLegendHeight;   // don't scale legend height since size is dependent on size of text inside
	return hLegend;
}


//set information needed for drawing analysis legend, e.g. height of each analysis, total height; return the height of the legend 
function getAnalysisLegendInfo(analyses, wTotal)  {
	var hLegend = 0;
	
	// retrieve the height that the analysis descriptions will be when drawn in svg with textFlow()
	var legendFontStyle = svgAnalyisLegend.fontStyle;
	var legendFontSize = svgAnalyisLegend.fontSize;
	var legendFont = legendFontSize + "px, " + legendFontStyle;
	var wLegendText = wTotal - svgAnalyisLegend.margin*2;
	
	var analysisInfo = new Array;
	
	var svgLegendHeight = svgAnalyisLegend.margin;
	for (var i=0; i<analyses.length; i++)  {
		var info = {}
		 info.y = svgLegendHeight;
		 info.w = wLegendText;
		 info.title = analyses[i].titleDisplay;

		 var hText = getTextFlowHeight(info.title, wLegendText, legendFont, legendFontSize) + legendFontSize;
		 
		 // height of analysis is greater of the font height or the total height of the text
		 info.h = (hText > legendFontSize ? hText :  legendFontSize) + svgAnalyisLegend.spacing;

		 if (i > 1)   {  // add spacer if not on 1st one 
			 svgLegendHeight += svgAnalyisLegend.spacing;					 
		 }
		 
		 svgLegendHeight += info.h;
		 
		 analysisInfo[i] = info

	}
	svgLegendHeight += svgAnalyisLegend.margin;
						
	hLegend = svgLegendHeight;  
	return {hLegend:hLegend, analysisInfo:analysisInfo};
}


function drawCTAAnalysisLegend(svg, analysisLegendOffsetX, analysisLegendOffsetY, analysisInfo)  {

	var analysisLegendGroup = svg.append("svg:g")
	  .attr("id", "ctaHeatmapLegend")
 	  .attr("class", "analysisLegendGroup")
 	  .attr("transform", "translate(" + analysisLegendOffsetX + "," + analysisLegendOffsetY + ")")
 	  ;
	
	var legendFont =  svgAnalyisLegend.fontSize + "px " + svgAnalyisLegend.fontStyle;
	var legendFontSize = svgAnalyisLegend.fontSize;
	
	// loop through each analysis and draw the description
	for (var i=0; i<analysisInfo.length; i++)  {
		var divId = "analysisLegendDescription" + uniqueDivID++;
		
		var analysisText = analysisLegendGroup.append("text")
	        .attr("id", divId)
			.attr("x", svgAnalyisLegend.margin)
			.attr("y", function () { return analysisInfo[i].y + legendFontSize;})
			.attr('class', 'legendAnalysisDesc')
	        .attr("text-anchor", "start")
	        .style("font", legendFont);   // need to apply style here when drawn so that textFlow draws correctly 

		var textNode = document.getElementById(divId);
		var dy = textFlow(analysisInfo[i].title,
			textNode,
			analysisInfo[i].w,
			0,
			legendFontSize,
			false);
			
	}	
}
