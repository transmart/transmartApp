// Draw the line plot using D3
function drawLinePlotD3(divId, linePlotJSON, analysisID, forExport)	{

	var plotData = setupPlotData(false, linePlotJSON, forExport, analysisID, divId);
	
	// create the plot without any lines (just title, axes, legend)
	var chartObject = drawEmptyPlot(plotData, forExport, analysisID, divId);
	
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
	for (key in plotData.statMapping)  {
		linePointsArray.push({index:index, mean:plotData.statMapping[key].mean, 
							  meanFormatted:plotData.statMapping[key].meanFormatted, 
							  stdErrorFormatted:plotData.statMapping[key].stdErrorFormatted});
		index++;
	}
	
	// create an array of the lines 
	var linesArray = new Array();
	
	for (var i=0; i<linePointsArray.length - 1; i++)  {
		linesArray.push({
			x1:x(linePointsArray[i].index),
			x2:x(linePointsArray[i+1].index),
			y1:y(linePointsArray[i].mean),
			y2:y(linePointsArray[i+1].mean)
		});
	}
			
	var lines = linesGroup
       .selectAll(".linePlotLines")
       .data(linesArray).enter().append("line")
       .attr('x1', function(d) {return d.x1;})
       .attr('x2', function(d) {return d.x2 ;})
       .attr('y1', function(d) {return d.y1;})
       .attr('y2', function(d) {return d.y2;})
       .attr("class", "linePlotLines");
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
	
	 applyPlotStyles(chartObject.svg);

	 if (!forExport)  {		
		drawScreenLegend(plotData.numCohorts, plotData.cohortArray, plotData.cohortDesc, plotData.cohortDisplayStyles, "lineplot", analysisID);
  	 }
		
}
