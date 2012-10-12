// Draw the line plot using D3
function drawLinePlotD3(divId, linePlotJSON, analysisID, forExport)	{

	var plotData = setupPlotData(false, linePlotJSON, forExport, analysisID, divId);
	
	// create the plot without any lines (just title, axes, legend)
	var chartObject = drawEmptyPlot(plotData, forExport, analysisID, divId);
	
	var chart = chartObject.chart;
	var x=chartObject.x;	
	var y=chartObject.y;
 	  
/*	  
	  // create group for boxPlots  
 	  var boxPlotsGroup = chart
		.append("g")
        .attr("id", "boxplotId")
        .attr("transform", "translate(" + wBand/2 + ",0)")
		.attr("class", "boxes")
		.on("mouseover", function() {
			   jQuery('#boxplotId text.hoverText').show();
			})
		.on("mouseout", function() {
			   jQuery('#boxplotId text.hoverText').hide();
			})
 	  	;

	  // draw range lines
 	  var rangeLinesGroup = boxPlotsGroup
		.append("g")
		.attr("class", "plotLines");

 	  var rangeLines = rangeLinesGroup
         .selectAll(".line")
    	.data(statMapping).enter().append("line")
    	.attr('x1', function(d) {return x(d.id);})
    	.attr('x2', function(d) {return x(d.id);})
    	.attr('y1', function(d) {return y(d.min);})
    	.attr('y2', function(d) {return y(d.max);})
    	.attr("class", "rangeLines");
    	;
	  
 	 var wMinMaxLines = wBox/2;
	  // draw min lines
 	  var minLinesGroup = boxPlotsGroup
		.append("g")
		.attr("class", "plotLines");

 	  var minLines = minLinesGroup
         .selectAll(".line")
    	.data(statMapping).enter().append("line")
    	.attr('x1', function(d) {return x(d.id) - wMinMaxLines/2;})
    	.attr('x2', function(d) {return x(d.id) + wMinMaxLines/2;})
    	.attr('y1', function(d) {return y(d.min);})
    	.attr('y2', function(d) {return y(d.min);})
    	;

 	  // min line text for hovers
 	 var minLinesText = minLinesGroup
 	 	.selectAll(".text")
 	 	.data(statMapping).enter().append("text")
 	 	.attr('x', function(d) {return x(d.id) })
 	 	.attr('y', function(d) {return y(d.min) + 1 ;})
 	 	.attr("dy", ".71em")
 	 	.attr('text-anchor', "middle")
 	 	.attr('class', 'hoverText')
 	 	.text(function(d) {return d.min.toFixed(2) })
	; 	  
 	  
	  // draw max lines
 	  var maxLinesGroup = boxPlotsGroup
		.append("g")
		.attr("class", "plotLines");

 	  var maxLines = maxLinesGroup
         .selectAll(".line")
    	.data(statMapping).enter().append("line")
    	.attr('x1', function(d) {return x(d.id) - wMinMaxLines/2;})
    	.attr('x2', function(d) {return x(d.id) + wMinMaxLines/2;})
    	.attr('y1', function(d) {return y(d.max);})
    	.attr('y2', function(d) {return y(d.max);})
    	;

 	  // max line text for hovers
  	 var maxLinesText = maxLinesGroup
  	 	.selectAll(".text")
  	 	.data(statMapping).enter().append("text")
  	 	.attr('x', function(d) {return x(d.id) })
  	 	.attr('y', function(d) {return y(d.max) - 1 ;})
  	 	.attr('text-anchor', "middle")
  	 	.attr('class', 'hoverText')
  	 	.text(function(d) {return d.max.toFixed(2) })
 	 ; 	  

  	 // draw boxes (draw after the range lines have been drawn so that range line is behind box
	 var boxes = boxPlotsGroup
	    .selectAll(".rect")
	    .data(statMapping).enter().append("svg:rect")
	    .attr('width',wBox)
	    .attr('height',function(d){
	    	return y(d.lq) - y(d.uq);}   // height goes down from the upper quartile
	    )
	    .attr('x',  function(d){return (x(d.id) -  wBox/2);  } )	    
	    .attr('y', function(d){return y(d.uq)})   // start at upper
	    .style('fill', function (d) {return cohortBGColors[d.cohortDisplayStyle]})
	    ;	 	 
	 
      // draw median lines
	  var medianLinesGroup = boxPlotsGroup
		.append("g")
		.attr("class", "plotLines");

	  var medianLines = medianLinesGroup
        .selectAll(".line")
        .data(statMapping).enter().append("line")
        .attr('x1', function(d) {return x(d.id) - wBox/2;})
        .attr('x2', function(d) {return x(d.id) + wBox/2;})
        .attr('y1', function(d) {return y(d.median);})
        .attr('y2', function(d) {return y(d.median);})
   	;

	  // max line text for hovers
 	 var medianLinesText = medianLinesGroup
 	 	.selectAll(".text")
 	 	.data(statMapping).enter().append("text")
 	 	.attr('x', function(d) {return x(d.id) + wBox/2 + 2; })
 	 	.attr('y', function(d) {return y(d.median);})
 	 	.attr('text-anchor', "start")
 	 	.attr('dy', '.35em')
 	 	.attr('class', 'hoverText')
 	 	.text(function(d) {return d.median.toFixed(2) })
	 ; 	  
	 
 	 
	 // upper quartile text for hovers
 	 var uqText = boxPlotsGroup
 	 	.selectAll(".text")
 	 	.data(statMapping).enter().append("text")
 	 	.attr('x', function(d) {return x(d.id) - wBox/2 - 2; })
 	 	.attr('y', function(d) {return y(d.uq);})
 	 	.attr('text-anchor', "end")
 	 	.attr('class', 'hoverText')
 	 	.text(function(d) {return d.uq.toFixed(2) })
	 ; 	  

	 // lower quartile text for hovers
 	 var lqText = boxPlotsGroup
 	 	.selectAll(".text")
 	 	.data(statMapping).enter().append("text")
 	 	.attr('x', function(d) {return x(d.id) - wBox/2 - 2; })
 	 	.attr('y', function(d) {return y(d.lq);})
 	 	.attr('text-anchor', "end")
 	 	.attr('dy', '.71em')
 	 	.attr('class', 'hoverText')
 	 	.text(function(d) {return d.lq.toFixed(2) })
	 ; 	  
 */	  	

	 applyPlotStyles(chartObject.svg);

	 if (!forExport)  {		
		drawScreenLegend(plotData.numCohorts, plotData.cohortArray, plotData.cohortDesc, plotData.cohortDisplayStyles, "lineplot", analysisID);
  	 }
		
}
