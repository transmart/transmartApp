// Take the heatmap data in the second parameter and draw the D3 heatmap
function drawHeatmapD3(divID, heatmapJSON, analysisID, forExport)	{
	jQuery("#" + divID).empty();
    var savedDisplayStyle = jQuery("#" + divID).css('display');
	jQuery("#" + divID).show();  // show first so title height can be calculated correctly

	
	// set up arrays to be used to populating drop down boxes for line/box plots
	// do this first since we need this for determining max probe string length
	var probesList = new Array(); 
	var selectList = new Array();
	var hasFoldChange = false; //true if the data contains fold change values
    var hasTPvalue = false;
    var hasPvalue = false;
    var hasNullValues = false; //checks if there are null in the heatmap; null legend should only be displayed if so
	var maxProbeLength = 0;

    var foldChange = new Array();    
    var tpValues = new Array();    
    var preferredPValues = new Array();    
    var geneLabels = new Array();    
	for (var i=0; i<heatmapJSON.length; i++)	{
		probesList.push(heatmapJSON[i].PROBE);
		selectList.push(heatmapJSON[i].GENE + " (" + heatmapJSON[i].PROBE + ")");
		foldChange.push(heatmapJSON[i].FOLD_CHANGE);

		var tpValue = (heatmapJSON[i].TEA_P_VALUE == 0) ? "< 0.00001" : heatmapJSON[i].TEA_P_VALUE; 
		tpValues.push(tpValue);
		
		var ppValue = (heatmapJSON[i].PREFERRED_PVALUE == 0) ? "< 0.00001" : heatmapJSON[i].PREFERRED_PVALUE; 
		preferredPValues.push(ppValue);

		geneLabels.push({gene:heatmapJSON[i].GENE, genelist:heatmapJSON[i].GENELIST, geneId:heatmapJSON[i].GENE_ID});
		
		if (heatmapJSON[i].PROBE.visualLength("10px Verdana, Tahoma, Arial") > maxProbeLength)  {
			//maxProbeLength = heatmapJSON[i].PROBE.length;
			maxProbeLength = heatmapJSON[i].PROBE.visualLength("10px Verdana, Tahoma, Arial");
		}
		if(heatmapJSON[i].FOLD_CHANGE != null){
			hasFoldChange = true;
			}
		if(heatmapJSON[i].TEA_P_VALUE != null){
			hasTPvalue = true;
			}
		if(heatmapJSON[i].PREFERRED_PVALUE != null){
			hasPvalue = true;
			}
		
		// create an array for the row of data points for the probe 
	    var dataRow = new Array();
        var x = 0;
        
		//check if any of the heatmapJSON values are undefined. The legend for null values will
		//only display if 'hasNullValues' is true
		//"key.indexOf(':') > 0" <- this is used to only check the chohorts (ex, 'C1:3432'). Other key values are ignored
		for (var key in (heatmapJSON[i])){
				if(heatmapJSON[i][key] == undefined && key.indexOf(':') > 0){
	    			hasNullValues=true;
			}
				
			if (key.indexOf(':') > 0)  {
				var val;
				if(heatmapJSON[i][key] == undefined)  {
					val = null;   
				}  
				else  {
					val = heatmapJSON[i][key];
				}  
				
			    // create a data object for the cell 
 		        var dataCell = {val:val, 
 		        		       x:x, 
 		        		       y:i,
 		        		       probe:heatmapJSON[i].PROBE,
 		        		       gene:heatmapJSON[i].GENE,
 		        		       cohort:key.split(":")[0]}
 		                       ;
	
		        dataRow[x] = dataCell;
		        
		        x = x + 1;
			}
		}
		
		heatmapJSON[i].data = dataRow;
		
		
	}
	
    var analysisIndex = getAnalysisIndex(analysisID);

    analysisProbeIds[analysisIndex].probeIds = probesList;
    analysisProbeIds[analysisIndex].selectList = selectList;

    // reset the active probe for the other plots to be the first on this page
    setActiveProbe(analysisID, probesList[0]);
    
	//store the max probe length for this analysis
	jQuery('body').data("maxProbeLength:" + analysisID, maxProbeLength);	    
	
	// First, we need to get the subject IDs that we will use for mapping the color range
	// We also need the two cohort prefixes and when the cohort first subset ends as these will be used for the legend	

	var cellID = "#heatmapSlider_" +analysisID;
	var colorSliderID = "#heatmapColorSlider_" +analysisID;
	
	var cellSize = parseInt(jQuery(cellID).slider( "option", "value" ));
	
	var w_probe = 6 + parseInt(jQuery('body').data("maxProbeLength:" + analysisID));
	
	
	var rangeMax = parseInt(jQuery(colorSliderID).slider( "values", 1 )) /100.0;
	var rangeMin = parseInt(jQuery(colorSliderID).slider( "values", 0 )) / 100.0;
	var rangeMid = (rangeMax + rangeMin)/2;
	
	var columns = new Array();

	
	// create an array for cohorts, their descriptions, and their switch positions
	var cohorts = new Array();
	var cohortDescriptions = new Array();
	var cohortSwitches = new Array();
	var cohortDisplayStyles = new Array();
		
	var idx = 0;
	
	var firstRowData = heatmapJSON[0];
	for (var key in firstRowData)	{
		// We have two types of values in the first row of the array
		// Metadata values: GENE, PROBE, FOLD_CHANGE, TEA_P_VALUE and the N cohort descriptions 		
		// Normalized values: The data given by the key Cohort:Subject 
		// First, we see if the cohort is cohort:subject unless it is Gene
		var keyArray = key.split(':');
		if (keyArray.length == 1)	{
			// OK, so we have metdata, ignore everything except the cohort info
			
			// add key to appropriate array, depending upon what it starts with
			if (key.indexOf('SWITCH_') == 0)  {
				cohortSwitches[key.slice(7)] = firstRowData[key]
			}
			else if (key.indexOf('DESC_') == 0)  {
				cohortDescriptions[key.slice(5)] = firstRowData[key]
			}
			else if (key.indexOf('COHORT_') == 0)  {
				cohorts[key.slice(7)] = firstRowData[key]
			}

				
		} else	{
			// We have data, save the key (e.g. C19:C0525T0300023) as the column metadata
			columns[idx] = key;			
			idx++;
		}
	}
		
/*	This code is for the "local" heatmap shading option, but is incomplete
	
	var x = pv.dict(columns, function(f) { return pv.mean(heatmapJSON, function(d){return d[f]}) }),
    s = pv.dict(columns, function(f) { return pv.deviation(heatmapJSON, function(d){ return d[f] })}),
 fill = pv.dict(columns, function(f) { return pv.Scale.linear()
        .domain(-3 * s[f] + x[f], x[f], x[f], 3 * s[f] + x[f])
        .range("#4400BE", "#D7D5FF","#ffe2f2", "#D70C00")});	
*/	
	
	
	
	// Hardcode the size of the cell and the labels
	var w_sample = 75, w_gene = 75, h_header=6, w_fold_change = 75, w_Tpvalue =75, w_pvalue = 75;			// Label dimensions
	var w = cellSize, h = cellSize;									    							// Cell dimensions
	
	if(!hasFoldChange){
		w_fold_change = 0; //if there is no fold change data, we need no space for it
	}
	
	if(!hasTPvalue){
		w_Tpvalue =0;
	}
	
	if(!hasPvalue){
		w_pvalue=0;
	}
	var numCohorts = cohorts.length-1;   // there is no cohort at index 0
	
	var heatmapHeight = 4*h +h_header + (heatmapJSON.length * h);
	var cohortLegendHeight;
	var cohortLegendOffset = 0;
	var heatmapOffset = 0;

	var mapIndex = 0;
	var cohortDescExport = highlightCohortDescriptions(cohortDescriptions, true);
	
	// setup statMapping object to pass data into legend
	var statMapping = cohorts.slice(1).map(function(i)	{
		var id = i;
		var styleIndex = (mapIndex + 1) % cohortBGColors.length;
		var cohortColor = cohortBGColors[styleIndex];
		var descExport = cohortDescExport[mapIndex+1].replace(/_/g, ', ');
		
		mapIndex++;
		
		return {
			id:i,
			cohortColor:cohortColor,
			descExport:descExport,
		};		
	});
	
	var hCohortDescExport = new Array;		
	
	if(forExport){
    	cohortLegendHeight = getLegendInfo(cohortDescExport, statMapping, wTotal, hCohortDescExport, false);    	
		cohortLegendOffset = heatmapHeight;  // heatmap on top, legend under
	}
	else {
		cohortLegendHeight = 0;  // legend not drawn with svg so height is zero
		heatmapOffset = 0;   // legend on top, heatmap under, but not drawing legend within svg object so offset is 0
	}
	var height = heatmapHeight + cohortLegendHeight;
		
	var wTotal = w_probe + columns.length * w + w_gene + w_fold_change + w_pvalue + w_Tpvalue;
	// create the main svg object
	var svg = d3.select("#" + divID)
		.append("svg")
		.attr("width", wTotal)
		.attr("height", height);   
	
	
	var hm = svg
		.append("g")
		.attr("transform", "translate(" + 0 + "," + heatmapOffset + ")")
		.attr("class", "heatmap");

	var colorScale = d3.scale.linear()
	    .domain([rangeMin, rangeMid, rangeMid, rangeMax])
	    .range(["#4400BE", "#D7D5FF","#ffe2f2", "#D70C00"]);
    
    //generate the heatmap
    var hmRow = hm.selectAll(".heatmap")
      .data(heatmapJSON)
      .enter().append("g");    
    
    var hmRects = hmRow
	    .selectAll(".rect")
	    .data(function(d) {
	      return d["data"];
	    }).enter().append("svg:rect")
	    .attr('width',w)
	    .attr('height',h)
	    .attr('x', function(d) {
	    	return w_probe + d.x * w;
	    })
	    .attr('y', function(d) {
	    	return h + d.y * h + h_header;
	    })
	    .style("stroke", "#333333")    // color of borders around rectangles
	    .style("stroke-width", 1)    // width of borders around rectangles
	    .style("shape-rendering", "crispEdges")
	    .style('fill',function(d) {
	    	
	        if (d.val == null)  {        	
	        	return "#FFFF00";
	        } 	
	        else  {
	        	return colorScale(d.val);
	        }
	      
	    })
	    ;

    if (!forExport)  {
    	hmRects.append('svg:title').text(function(d,i) {
		var val;
		if (d.val == undefined)  {
			val = "null"
		}
		else {
			val = d.val.toFixed(2);
		}
		
		return d.cohort + ":" + d.probe + ":" + d.gene + "=" + val;
		
    	});

    }
    
	// create array of cohort widths
	var cohortWidths = new Array();
	for(var i=1; i<=numCohorts; i++) {
		if (i == numCohorts)  {
		    cohortWidths[i] = w * (columns.length - cohortSwitches[i]);			
		}
		else  {
		    cohortWidths[i] = w * (cohortSwitches[i+1] - cohortSwitches[i]);
		}
	}

	// position of cohort header group
	var xPosition = w_probe;
	var yPosition = 2;
	
	//GROUP FOR Cohort headers
	var cohortHeaderGroup = hm.append("svg:g")
	  .attr("class", "cohortHeader")
	  .attr("transform", "translate(" + xPosition + "," + yPosition + ")")
	  ;
	
	var leftPosition = 0;  // relative position within cohort header group
	
	for(var i=1; i<=numCohorts; i++) {		
		var classIndex;
		
		cohortDisplayStyles[i] = i % cohortBGColors.length;

		var dataColor = cohortBGColors[i % cohortBGColors.length];
		var strokeStyleColor = "#000";
		var textStyleColor = "#000";
		
		// Cohort header
		var barC = cohortHeaderGroup.append("rect")
			.attr("x", leftPosition)
			.attr("y", 0)
			.attr("width", cohortWidths[i])
			.attr("height", h)
			.style("stroke", strokeStyleColor)    // color of borders around rectangles
			.style("stroke-width", 1)    // width of borders around rectangles
		    .style('fill', dataColor  )
		    .style("shape-rendering", "crispEdges")	
			;
		
		// this tooltip needs to be added both to rectangle and the label so it doesn't disappear when you mouse over the label
		var cohortTooltip = cohortDescriptions[i].replace(/_/g, ', ');
		
		// Tooltips for cohort header rectangle
	    if (!forExport)  {
	    	barC.append('svg:title').text(cohortTooltip);
	    }
				
		//set the header font size depending on the cell size
		var headerFontFamily = "sans-serif";
		var headerFontSize = 12;

		if(cellSize < 12){
			var headerFontSize = 8;
		}else if (cellSize>19){
			var headerFontSize = 16;
		}
		
		// calculate coordinates for label in center of rect -- is there an easier way to do this with D3?  
		var labelX = leftPosition + cohortWidths[i]/2;
		var labelY = h/2;
		
		if (forExport)  {
			labelY += 3;
		}
		
		var chgText = cohortHeaderGroup.append("text")
			.attr("x", labelX)
			.attr("y", labelY)
			.attr("dy", ".35em")
 	        .attr("text-anchor", "middle")
		    .style("fill", textStyleColor)
		    .style("font-size", headerFontSize + "px")
		    .style("font-family", headerFontFamily)
 	        .text(cohorts[i] )
 	        ;	
			
	    if (!forExport)  {
	    	chgText.append('svg:title').text(cohortTooltip);   // tooltip for label
	    }
	    
		// determine left position for next cohort
	    leftPosition = leftPosition + cohortWidths[i];

	}

    if (forExport)  {
		drawSVGLegend(svg, 10, cohortLegendOffset, statMapping, false, forExport);
    }

	var yOffset = h + h_header + 4;
	var xOffset;
	//only do this if the data contains fold change values
	if(hasFoldChange)  {
		xOffset = w_probe + columns.length * w + w_gene;
		addDataColumn(hm, xOffset, yOffset, w_fold_change, foldChange, "Fold change", "foldChangeGroup", h);
	}

	
	//only do this if the data contains TP values
	if(hasTPvalue)  {
		xOffset = w_probe + columns.length * w + w_gene + w_fold_change;
		addDataColumn(hm, xOffset, yOffset, w_Tpvalue, tpValues, "TEA p-value", "tpValueGroup", h);		
	}

	
	//only do this if the data contains TP values
	if(hasPvalue)  {
		xOffset = w_probe + (columns.length * w) + w_gene + w_fold_change +w_Tpvalue;
		addDataColumn(hm, xOffset, yOffset, w_pvalue, preferredPValues, "p-value", "ppValueGroup", h);		
	}

	
	// GENE LABELS
	xOffset = w_probe + columns.length * w + 2;
	var geneGroup = hm.append("svg:g")
	  .attr("class", "geneGroup")
	  .attr("transform", "translate(" + xOffset + "," + yOffset + ")")
	  ;
	
    // Show the gene labels
	var geneGroupText = geneGroup.selectAll("a")
		.data(geneLabels)
		.enter().append("a")
		.attr("xlink:href", function(d) {return "javascript:showGeneInfo('"+d.geneId +"');"})
		.append("text")
		.attr("x", 0)
		.attr("y",function(d, i)	{
			return (i * (h) + h / 2); 
		    })
		.attr("width", w_gene)
		.attr("text-anchor", "start")
	    .style("font", "10px Verdana, Tahoma, Arial")
		.text(function(d)	{
			return d.gene;
		} )		
        ;			
	
	// tooltip for gene labels
    if (!forExport)  {
    	geneGroupText.append('svg:title').text(function(d)	{
    		return d.genelist;
    	} );
    }
		
    // PROBE LABELS
	xOffset = 0;
	var probeGroup = hm.append("svg:g")
	  .attr("class", "probeGroup")
	  .attr("transform", "translate(" + xOffset + "," + yOffset + ")")
	  ;
	
    // Show the probe labels
	var probeGroupText = probeGroup.selectAll("a")
		.data(probesList)
		.enter().append("a")
		.attr("xlink:href", function(d) {return "javascript:openBoxPlotFromHeatmap(" +analysisID +", '" + d +"');"})
		.append("text")
		.attr("x", 0)
		.attr("y",function(d, i)	{
			return (i * (h) + h / 2); 
		    })
		.attr("width", w_probe)
		.attr("text-anchor", "start")
	    .style("font", "10px Verdana, Tahoma, Arial")
		.text(function(d)	{
			return d;
		} )		
        ;			
	
	// tooltip for probe labels
    if (!forExport)  {
    	probeGroupText.append('svg:title').text("View in boxplot");
    }

	//GROUP FOR legend (min, max, null)
	var legendGroup = hm.append("svg:g")
	  .attr("class", "legendGroup")
	  .attr("transform", "translate(" + w_probe + "," + (2*h + (heatmapJSON.length * h) + h_header) + ")")
	  ;
	
	var legendBars = new Array();
	legendBars.push({bgColor:"#4400BE", textColor:"white", text:"min: "+Math.round((rangeMin)*100)/100});
	legendBars.push({bgColor:"#D70C00", textColor:"white", text:"max: " + Math.round((rangeMax)*100)/100});
	//draw legend for null values only if they exist in the current heatmap
	if(hasNullValues){
		legendBars.push({bgColor:"#FFFF00", textColor:"black", text:"null"});
	}
		
	var barWidth = 55;
	var barHeight = 15;
	var barSpacing = 30;
	// legend labels
	var barLegends = legendGroup.selectAll("rect")
		 .data(legendBars)
		 .enter().append("rect")
		.attr("x", function(d, i) {return i*(barWidth + barSpacing); })
		.attr("y", 0)
		.attr("width", barWidth)
		.attr("height", barHeight)
	    .style('fill', function(d) {return d.bgColor; })
		;


	var barLegends = legendGroup.selectAll("text")
		.data(legendBars)
		.enter().append("text")
		.attr("x", function(d, i) {return i*(barWidth + barSpacing) + 2; })
		.attr("y", barHeight - 3)
 	    .attr("text-anchor", "start")
		.style("fill", function(d) {return d.textColor; })
		.style("font-size", "10px")
 	    .text(function(d) {return d.text; });

	// not exporting, add the html to the div for the legend
	 if (!forExport)  {		
		 drawScreenLegend(numCohorts, cohorts, cohortDescriptions, cohortDisplayStyles, "heatmap", analysisID);
  	 }
	
 	 jQuery("#" + divID).css('display', savedDisplayStyle);
	
}


// add one of the data columns to the heatmap (pvalue, tpvalue, or fold change)
//  hm - heatmap 
//  xOffset - x Offset for group
//  yOffset - y Offset for group
//  width - width of column
//  data - data array for column
//  header - title of column
//  groupName - group name used as class name for group
//  h - height of each row
function addDataColumn(hm, xOffset, yOffset, width, data, header, groupName, h)  {
		
		//GROUP FOR new text labels
		var group = hm.append("svg:g")
		  .attr("class", groupName)
		  .attr("transform", "translate(" + xOffset + "," + yOffset + ")")
		  ;
		
	    // Show the data value
		group.selectAll("text")
			.data(data)
			.enter().append("text")
			.attr("x", 0)
			.attr("y",function(d, i)	{
				return (i * (h) + h / 2); 
			    })
			.attr("width", width)
			.attr("text-anchor", "start")
		    .style("font", "10px sans-serif")
			.text(function(d)	{
				return d;
			} )
	        ;			

		// Show the table header
		group.append("text")
			.attr("x", 0)
			.attr("y", - 11)
			.attr("width", width)
 	        .attr("text-anchor", "start")
		    .style("font", "bold 11px sans-serif")
	        .text(header)
	        ;	
}
