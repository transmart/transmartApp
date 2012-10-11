function drawExportLegend(svg, cohortLegendOffsetX, cohortLegendOffsetY, statMapping)  {

 	 	var cohortLegendGroup = svg.append("svg:g")
	 	  .attr("class", "cohortLegendGroup")
	 	  .attr("transform", "translate(" + cohortLegendOffsetX + "," + cohortLegendOffsetY + ")")
	 	  ;

	    var boxWidth = 30;
	    var boxHeight = 25;
	    var boxSpacing = 5;

	    // Cohort legend
		cohortLegendGroup.selectAll(".legendRect")
		    .data(statMapping)
		    .enter()
			.append("rect")
			.attr("x", 0)
			.attr("y", function (d, i) { return i * (boxHeight + boxSpacing)})
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
			.attr("y", function (d, i) { return (i)*(boxHeight + boxSpacing) + boxHeight/2 + 3})
			.attr("dy", ".35em")
	        .attr("text-anchor", "middle")
			.attr('class', 'legendCohortId')
	        .text(function (d, i) { return d.id; } )
	        ;	
		
		// cohort legend text descriptions
		labelX = boxWidth + boxSpacing;
		
		var desc=cohortLegendGroup.selectAll(".legendCohortDesc")
			.data(statMapping)
			.enter()
		    .append("text")
			.attr("x", boxWidth + boxSpacing)
			.attr("y", function (d, i) { return (i)*(boxHeight + boxSpacing) + boxHeight/2 + 3})
			.attr('class', 'legendCohortDesc')
	    	.text(function (d, i) { return d.descExport;});   	
		;
		
		applyLegendStyles(cohortLegendGroup);
}


function applyLegendStyles(svg)  {
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
    	.style("font", "12px  sans-serif")
		;
	
}