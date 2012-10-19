// Draw the line plot using D3
function drawLinePlotD3(divId, linePlotJSON, analysisID, forExport, isCTA, selectedAnalyses)	{

    // boxPlotJSON should be a map of analysisId:[cohortID:[desc:cohort description, order:display order for the cohort, data:sorted log2 intensities]]	  
	var allPlotData = setupPlotData(false, linePlotJSON, forExport, analysisID, divId, isCTA, selectedAnalyses);

	// create the plot without any lines (just title, axes, legend)
	drawEmptyPlots(allPlotData, forExport, divId, isCTA);
	  
	for (var i=0; i<allPlotData.orderedAnalysisKeys.length; i++)  { 			
	    var analysisKey = allPlotData.orderedAnalysisKeys[i];

	    var plotData = allPlotData[analysisKey];
   	    var chartObject = allPlotData[analysisKey].emptyPlotData;
	
		var chart = chartObject.chart;
		var x=chartObject.x;	
		var y=chartObject.y;
	
	    // draw lines
		var linesGroup = chart
			.append("g")
			.attr("class", "plotLines")
	        .attr("transform", "translate(" + chartObject.wBand/2 + ",0)")
			;
	
		// create an array of the points on the line
		var linePointsArray = new Array();
		var index = 0;
		for (var key in plotData.statMapping)  {
			linePointsArray.push({index:index, mean:plotData.statMapping[key].mean, 
								  meanFormatted:plotData.statMapping[key].meanFormatted, 
								  stdErrorFormatted:plotData.statMapping[key].stdErrorFormatted});
			index++;
		}
		
	    // Specify the function for generating path data             
	    var pathData = d3.svg.line()
	                    .x(function(d){return x(d.index);})
	                    .y(function(d){return y(d.mean);})
	                    .interpolate("linear"); 
	
	    // Create path
	    var path = linesGroup.append("path")
	        .attr("d", pathData(linePointsArray))
			.attr("class", "linePlotPath")
			;
	    
	    // draw error bar
		var errorBarsGroup = linesGroup
			.append("g")
			.attr("class", "errorBar")
			;
	
		// error bar vertical lines
		var errorBarLines = errorBarsGroup
	    	.selectAll(".linePlotErrorBarsLines")
	    	.data(plotData.statMapping).enter().append("line")
	    	.attr('x1', function(d) {return x(d.id);})
	    	.attr('x2', function(d) {return x(d.id);})
	    	.attr('y1', function(d) {return y(d.mean - Math.abs(d.stdError));})
	    	.attr('y2', function(d) {return y(d.mean + Math.abs(d.stdError));})
	    	.attr("class", "linePlotErrorBarsLines");
		;
		
		var widthErrorBarBottomAndTopLines = 6;
	    // error bar top horizontal line
		errorBarsGroup
			.selectAll(".linePlotErrorBarsLinesTop")
			.data(plotData.statMapping).enter().append("line")
			.attr('x1', function(d) {return x(d.id) - widthErrorBarBottomAndTopLines/2;})
			.attr('x2', function(d) {return x(d.id) + widthErrorBarBottomAndTopLines/2;})
			.attr('y1', function(d) {return y(d.mean + Math.abs(d.stdError));})
			.attr('y2', function(d) {return y(d.mean + Math.abs(d.stdError));})
			.attr("class", "linePlotErrorBarsLinesTop");
	
	    // error bar bottom horizontal line
		errorBarsGroup
			.selectAll(".linePlotErrorBarsLinesBottom")
			.data(plotData.statMapping).enter().append("line")
			.attr('x1', function(d) {return x(d.id) - widthErrorBarBottomAndTopLines/2;})
			.attr('x2', function(d) {return x(d.id) + widthErrorBarBottomAndTopLines/2;})
			.attr('y1', function(d) {return y(d.mean - Math.abs(d.stdError));})
			.attr('y2', function(d) {return y(d.mean - Math.abs(d.stdError));})
			.attr("class", "linePlotErrorBarsLinesBottom");
		
	    // draw points (draw last so they are on top and tooltips appear)
		var pointsGroup = linesGroup
			.append("g")
			.attr("class", "plotPoints")
			;
			
		var points = linesGroup
			.selectAll(".linePlotPoints")
			.data(linePointsArray).enter().append("circle")
			.attr('cx', function(d) {return x(d.index);})
			.attr('cy', function(d) {return y(d.mean);})
			.attr("class", "linePlotPoints")
			.attr("r", 3)
		;
		
		// add tooltip for points
		 if (!forExport)  {
			 points
		     .append('svg:title')
		     .text(function(d){
		    	 return d.meanFormatted + " +/- " + d.stdErrorFormatted;
		    	 })
		     	;		  
		 }
		
		 applyPlotStyles(chartObject.svg, isCTA);
	
		 if (!forExport)  {		
			drawScreenLegend(plotData.numCohorts, plotData.cohortArray, plotData.cohortDesc, plotData.cohortDisplayStyles, "lineplot", analysisID);
	  	 }
	}	
}
