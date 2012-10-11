// Draw the box plot using 3D
function drawBoxPlotD3(divId, boxPlotJSON, analysisID, forExport)	{
	// boxPlotJSON should be a map of cohortID:[desc:cohort description, order:display order for the cohort, data:sorted log2 intensities]

	jQuery("#" + divId).empty();
	
	var cohortArray = new Array();   // array of cohort ids
	var cohortLabels = new Array();   // array of cohort labels on x axis
	var cohortDesc = new Array();    // array of cohort descriptions
	var cohortDisplayStyles = new Array();    // array of cohort display styles (i.e. number from 0..4)

	var gene_id = parseInt(boxPlotJSON['gene_id']);   // gene_id will be null if this is a protein since first char is alpha for proteins
	
	// loop through and get the cohort ids and description into arrays in the order they should be displayed
	for (var key in boxPlotJSON)  {
		if (boxPlotJSON[key]['order'])  {   // if the object in the array doesn't have an order, not a cohort
			// the "order" of the json objects starts with 1, so subtract 1 so it doesn't leave gap at start of array
			var arrayIndex = boxPlotJSON[key]['order'] - 1;
			cohortArray[arrayIndex] = key;
			cohortDesc[arrayIndex] = boxPlotJSON[key]['desc'];
			cohortDisplayStyles[arrayIndex] = boxPlotJSON[key]['order'] % cohortBGColors.length;
			cohortLabels[arrayIndex] = key + "(n=" +  boxPlotJSON[key]['sampleCount'] + ")"
		}
	}

	// highlight cohort descriptions expects and array starting at index 1
	var cohortDescExport = highlightCohortDescriptions([''].concat(cohortDesc), true);
	
	// Map the all four quartiles to the key (e.g. C1)
	var statMapping = cohortArray.map(function(i)	{
		var data = boxPlotJSON[i]['data'];
		var cohortDisplayStyle = boxPlotJSON[i]['order'] % cohortBGColors.length;
		var cohortColor = cohortBGColors[cohortDisplayStyle];
		var desc = boxPlotJSON[i]['desc'].replace(/_/g, ', ');
		var descExport = cohortDescExport[boxPlotJSON[i]['order']].replace(/_/g, ', ');
		var sampleCount = boxPlotJSON[i]['sampleCount'];
		
		return {
			id:i,
			cohortDisplayStyle:cohortDisplayStyle,
			cohortColor:cohortColor,
			desc:desc,
			descExport:descExport,
			sampleCount:sampleCount,
			min:data[getRank(5, data.length)-1],
			max:data[getRank(95, data.length)-1],			
			median:data[getRank(50, data.length)-1],
			lq:data[getRank(25, data.length)-1],
			uq:data[getRank(75, data.length)-1]
		};		
	});
	
	
	//if the user is setting the range manually:
	if(jQuery('#boxplotRangeRadio_Manual_'+analysisID).is(':checked')){
		
		var yMin = parseFloat(jQuery('#boxplotRangeMin_'+analysisID).val());
		var yMax = parseFloat(jQuery('#boxplotRangeMax_'+analysisID).val());

		
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
		jQuery('#boxplotRangeMin_'+analysisID).val(roundNumber(yMin,2));
		jQuery('#boxplotRangeMax_'+analysisID).val(roundNumber(yMax,2));
		
	}
	
	var title = getGeneforDisplay(analysisID, getActiveProbe(analysisID));
	
	var margin = 55;

	var wChart = cohortArray.length * 140;//generate the width dynamically using the cohort count	
	var  hChart = 350;
	
	var hTitle = 40;

	var wTotal = wChart + margin;
	var hTotal = hChart + hTitle; 

	// if exporting, draw a legend; if not exporting legend is drawn outside of svg
	var hLegend = 0;
	if(forExport){
		hLegend = 30 * (cohortArray.length);
	}

	hTotal = hTotal + hLegend;
	
	var numCohorts = cohortArray.length;
			
	var svg = d3.select("#" + divId)
		.append("svg")
		.attr("width", wTotal)
		.attr("height", hTotal);
	
	var bp = svg
		.append("g")
		.attr("transform", "translate(0," + hLegend + ")")
		.attr("class", "boxplot")
		;

	var yTitle = 2;
	if (forExport)  {
		yTitle = 20;
	}
	
    var titleText = bp.append("a")
	    	.attr("xlink:href", function(d) {return "javascript:showGeneInfo('"+gene_id +"');"})
	        .append("text")
	        .attr("x", margin + wChart/2)
	        .attr("y", yTitle )
	        .attr("dy", ".71em")
	        .attr("class", "title")
	        .attr("text-anchor", "middle")
	        .text(title)
	        ;
	    
	if (gene_id && !forExport)  {			
	    	//Add tooltip for title 
			titleText.append('svg:title').text("View gene information");
	}
		

	var chart = bp.append("g")
		.attr("transform", "translate(" + margin + "," +  hTitle + ")")
		.attr("class", "boxplot");

	// x axis labels
	var x=d3.scale.ordinal()
	  .domain(cohortLabels)	  
	  .rangeBands([0, wChart])
	  ;

	var y = d3.scale.linear().domain([yMin, yMax]).range([hChart - margin, 0]);
		
    var xAxis = d3.svg.axis()
		.scale(x)
		.orient("bottom");
		  					
	 var yAxis = d3.svg.axis()
		.scale(y)
		.tickSize(-wChart)
		.orient("left");
		
	// add an additional offset for the x axis for export 
	var yOffsetForExport = 0;
	if (forExport)  {
		yOffsetForExport = 10;
	}

	  // Add the x-axis.
	  var xAxisTag = chart.append("g")
	      .attr("class", "xaxis")
	      .attr("transform", "translate(0," + (hChart - margin + yOffsetForExport) + ")")
	      .call(xAxis)
	      ;

	  // add tooltip for x axis labels
	  if (!forExport)  {
		  xAxisTag.selectAll("text")
		    .append('svg:title')
		    .data(statMapping)
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
        .attr("transform", "translate(" + 10 + "," + (hChart/2) + ") rotate(-90)")  // note: space between translate/rotate clauses - if not there, export has a problems
        .text("log2 intensity");
			 
	  var wBand = x.rangeBand();
	  var wBox = wBand * .4; 
		
	  
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
 	  	

 	 if (forExport)  {
		drawExportLegend(svg, 10, 0, statMapping);
 	 }

	 applyBoxplotStyles(svg);

	 // need to add a blank entry at the beginning of the arrays for use by drawCohortLegend and highlightCohortDescriptions
	 cohortArray = [''].concat(cohortArray);
	 cohortDesc = [''].concat(cohortDesc);
	 cohortDisplayStyles = [''].concat(cohortDisplayStyles);

	if (!forExport)  {		
		jQuery("#boxplotLegend_" + analysisID).html(drawCohortLegend(numCohorts, cohortArray, cohortDesc, cohortDisplayStyles));
	}
		
}

function applyBoxplotStyles(svg)  {
	// note: export doesn't recognize classes/styles in CSS file, need to apply directly to objects
	svg.selectAll(".boxplot")
		.style("font", "14px sans-serif")
		;
	
	svg.selectAll(".boxplot .title")
		.style("fill", "#065B96")
		.style("font", "bold 16px sans-serif")
		;

	svg.selectAll(".boxplot .plotLines")
		.style("stroke-width", "1px")
		.style("stroke", "#000")
		.style("shape-rendering", "crispEdges")
		;
	
	svg.selectAll(".boxplot .yaxis path, .boxplot .yaxis line")
		.style("fill", "none")
		.style("stroke", "#ccc")
		.style("stroke-width", "0px")
		.style("shape-rendering", "crispEdges")
		;

	svg.selectAll(".boxplot .yaxis line.tick")
		.style("fill", "none")
		.style("stroke", "#CCCCCC")
		.style("stroke-width", "1px")
		.style("shape-rendering", "crispEdges")
		;
	
	
	svg.selectAll(".boxplot .xaxis path, .boxplot .xaxis line")
		.style("fill", "none")
		.style("stroke-width", "0px")
		;		
	
	svg.selectAll(".boxplot rect")
		.style("stroke-width", "1px")
		.style("stroke", "#000")
		.style("shape-rendering", "crispEdges")
		;
	
	svg.selectAll(".boxplot .hoverText")
		.style("font", "10px sans-serif")
		.style("stroke", "none")
		.style("display", "none")
		;
}