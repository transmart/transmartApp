var uniqueHeatmapId = 0;
var hmTooltips = new Array;
var ctaHeaderFontFamily = "Verdana, Tahoma, Arial";
var ctaHeaderFontSize = 10;
var ctaGeneLabelFontFamily = "Verdana, Tahoma, Arial";
var ctaGeneLabelFontSize = 10;
var ctaGeneLabelFontWeight = "normal";
var ctaHeatmapHeightWithoutLegend;
var ctaHeatmapHeightWithLegend;

// Take the heatmap data in the second parameter and draw the D3 heatmap
function drawHeatmapD3(divID, heatmapJSON, analysisID, forExport, isSA, keywordQueryString)	{
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
	var maxGeneLength = 0;

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
		
		var geneLength = heatmapJSON[i].GENE.visualLength("10px Verdana, Tahoma, Arial");
		if ( geneLength > maxGeneLength)  {
			maxGeneLength = geneLength;
		}
		
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
	
	var analysisIndex;
	var cellSize;
	var w_probe;		
	var rangeMax;
	var rangeMin;
	var rangeMid;
	if (!isSA)  {		
	    analysisIndex = getAnalysisIndex(analysisID);

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
		cellSize = parseInt(jQuery(cellID).slider( "option", "value" ));
		rangeMax = parseInt(jQuery(colorSliderID).slider( "values", 1 )) /100.0;
		rangeMin = parseInt(jQuery(colorSliderID).slider( "values", 0 )) / 100.0;
	}
	else  {		
	    analysisIndex = getAnalysisIndexSA(analysisID);

	    analysisProbeIdsSA[analysisIndex].probeIds = probesList;
	    analysisProbeIdsSA[analysisIndex].selectList = selectList;
	    cellSize = 15;
		rangeMax = 1;
		rangeMin = -1;
	}
	
	var w_probe = 6 + parseInt(maxProbeLength);
	
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
	var w_sample = 75, w_gene = Math.max(75, maxGeneLength + 5), h_header=6, w_fold_change = 75, w_Tpvalue =75, w_pvalue = 75;			// Label dimensions
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
	
	var probesGroupTextTags;
	
	// only have links if on the non SA heatmap
	if (!isSA)  {
		probesGroupTextTags = probeGroup.selectAll("a")
		.data(probesList)
		.enter()
		.append("a")
		.attr("xlink:href", function(d) {
				return "javascript:openBoxPlotFromHeatmap(" +analysisID +", '" + d +"');"				
		  }
		)
		.append("text")
	}  
	else  {
		probesGroupTextTags = probeGroup.selectAll("text")
		.data(probesList)
		.enter()
		.append("text")		
	}
	
    // Show the probe labels
	var probeGroupText = probesGroupTextTags
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
    if (!forExport && !isSA)  {
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
		 drawScreenLegend(numCohorts, cohorts, cohortDescriptions, cohortDisplayStyles, "heatmap", analysisID, isSA);
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


//Take the heatmap data in the second parameter and draw the D3 heatmap for Cross Trial Analysis
//  (i.e. Genes down left, analysis on top, fold change in box)
// rows is an array containing one entry for each row of data; each row contains data for the label (search keyword) and one entry for each data
//  col: e.g.
//         {"1":{"searchKeywordId":1240513,"keyword":"CAMP","geneId":"820","data":{"0":{"col":0,"row":0},"1":{"probeId":"210244_at","foldChange":-2.3866,"preferredPValue":null,"bioMarkerName":"CAMP","organism":"HOMO SAPIENS","col":1,"row":0}}},
//          "2":{"searchKeywordId":1253597,"keyword":"CCL28","geneId":"56477","data":{"0":{"col":0,"row":1},"1":{"probeId":"224027_at","foldChange":1.675,"preferredPValue":null,"bioMarkerName":"CCL28","organism":"HOMO SAPIENS","col":1,"row":1}}},
//          "3":{"searchKeywordId":1242219,"keyword":"CXCR3","geneId":"2833","data":{"0":{"col":0,"row":2},"1":{"probeId":"207681_at","foldChange":1.747,"preferredPValue":null,"bioMarkerName":"CXCR3","organism":"HOMO SAPIENS","col":1,"row":2}}},
//          "4":{"searchKeywordId":1241668,"keyword":"FCER1A","geneId":"2205","data":{"0":{"col":0,"row":3},"1":{"probeId":"211734_s_at","foldChange":2.186,"preferredPValue":null,"bioMarkerName":"FCER1A","organism":"HOMO SAPIENS","col":1,"row":3}}},
//          "5":....
function drawHeatmapCTA(divID, rows, analyses, keywordTitle)	{
	jQuery("#" + divID).empty();
    var savedDisplayStyle = jQuery("#" + divID).css('display');
    
	
	// convert the rows to an array and the data columnd to an array for consumption by d3 
	var rowsArray = new Array
	var index = 0;
	for (var r in rows)  {
		
		var dataCol = new Array
		for (c in rows[r]['data'])  {
			dataCol[c] = rows[r]['data'][c]
		}
		
		var searchKeywordId = rows[r]['searchKeywordId'];
		var keyword = rows[r]['keyword'];
		var geneId = rows[r]['geneId'];
		
		var row = {searchKeywordId:searchKeywordId, keyword:keyword, geneId:geneId, data:dataCol}
		rowsArray[index] = row
		index++;
	}
	
	//hmTooltips = new Array;
	
	for (var i=0; i<analyses.length; i++)  {
		analyses[i].title = analyses[i].title.replace(/_/g,', ') 		
		analyses[i].titleDisplay = (i + 1) + " - " + analyses[i].studyID + " : " + analyses[i].title; 		
	}
	
	var cellSize = 15;
	var wCell = cellSize, hCell = cellSize;		// Cell dimensions
	
	var hHeader = 20;   // this is the header row for the heatmap
	
	var numAnalyses = analyses.length;
	
	var numGenes = 0;

	var maxGeneWidth = 0;

	var heatmapRows = new Array;
	var genesArray = new Array;
	
	var maxFoldChange = 0;
	var minFoldChange = 0;

	var numRows = rowsArray.length;
	var geneLabels = new Array;
	// iterate thru rows and create the gene labels that will go on the left of the graph 
	//  (for now take the first we come across, do we want to prioritize and use human if available or some other criteria to determine the one shown)
	for (var r=0; r<rowsArray.length; r++)  {
		var row = rowsArray[r];
		
		var geneName; 
		var geneId;
		geneName =  row.keyword; 
		geneId = row.geneId;
		searchKeywordId = row.searchKeywordId;
  	    geneLabels[r] = {geneName:geneName, geneId:geneId, searchKeywordId:searchKeywordId};
		
		var wGene = geneName.visualLength(ctaGeneLabelFontWeight + " " + ctaGeneLabelFontSize + "px " + ctaGeneLabelFontFamily);
	    maxGeneWidth = (wGene > maxGeneWidth) ? wGene : maxGeneWidth;	

	}

	var minWidth = 200;
	var hmWidth = Math.max(maxGeneWidth + numAnalyses*wCell + 6, 100)
	var wTotal;
	wTotal = Math.max(minWidth, hmWidth)

	// determine the width of the title
	var titleFontSize = 11;	
	var titleFont = "bold " + titleFontSize + "px sans-serif"; 
	var hTitle = 25;  // leave at least enough room for at 2 lines of text
	var actualTitleHeight = getTextFlowHeight(keywordTitle, hmWidth, titleFont, titleFontSize);
	hTitle = Math.max(hTitle, actualTitleHeight)

	var heatmapHeight = hTitle + hHeader + (numRows * hCell) + 15;
	var analysisLegendHeight;
	var analysisLegendOffset = heatmapHeight;
	var heatmapOffset = 0;
	
		
	// retrieve info needed for legend
	var legendInfo = getAnalysisLegendInfo(analyses, wTotal);
	var hLegend = legendInfo.hLegend;
	var analysisInfo = legendInfo.analysisInfo;
	
	// save the height with and without legend so we can increase size for export/decrease when export done
	ctaHeatmapHeightWithLegend = heatmapHeight + hLegend;
	ctaHeatmapHeightWithoutLegend = heatmapHeight;

	// draw the heatmap including legend, but truncate so that only heatmap shows
	var height = ctaHeatmapHeightWithoutLegend;

	// create the main svg object
	var svg = d3.select("#" + divID)
		.append("svg")
		.attr("width", wTotal)	
		.attr("height", height);   
	
	
	var hm = svg
		.append("g")
		.attr("transform", "translate(" + 0 + "," + heatmapOffset + ")")
		.attr("class", "heatmap");

	// use same colors between the 2 range max/min values
	var rangeMax = 3.5;
	var rangeMax2 = 10000;
	var rangeMin = -3.5;
	var rangeMin2 = -10000;
	
	var rangeMid = (rangeMax + rangeMin)/2;
	
	var colorScale = d3.scale.linear()
	    .domain([rangeMin2, rangeMin, rangeMid, rangeMid, rangeMax, rangeMax2])
	    .range(["#4400BE", "#4400BE", "#D7D5FF","#ffe2f2", "#D70C00", "#D70C00"]);

	if (numRows == 0)  {
	    var hmNoData = hm.append("text")
		.attr("x", 0)
		.attr("y", hTitle + hHeader + 15)
		.attr("text-anchor", "start")
		.text("No data")		
	}
	
	// the title will be centered over heatmap if possible; othwerwise start it at left and it will extend as far as it needs to
	var titleAnchor = "middle";
	var titleX = hmWidth/2;		
	
	var hmTitleId = "hmTitle" + uniqueDivID++ ;
	
    var hmTitle = hm.append("text")
	    .attr("id", hmTitleId)
		.attr("x", titleX)
		.attr("y", titleFontSize)
		.style("fill", "#111")
	    .style("font", titleFont)
		.attr("text-anchor", titleAnchor);		

    var textNode = document.getElementById(hmTitleId);
	var dy = textFlow(keywordTitle,
			textNode,
			hmWidth,
			hmWidth/2,
			titleFontSize,
			false);	    

	
    //generate the heatmap
    var hmRow = hm.selectAll(".heatmap")
      .data(rowsArray)
      .enter().append("g");    
    
    var hmRects = hmRow
	    .selectAll(".rect")
	    .data(function(d) {
	      return d['data'];
	    }).enter().append("svg:rect")
	    .attr('width', wCell)
	    .attr('height',hCell)
	    .attr("class", "heatmapTooltip")
	    .attr("id", function(d) {
	    	var id = "hmCell" + uniqueHeatmapId++;   // id here will match id in tooltip array
	    	var geneName = d.bioMarkerName;
	    	
	    	var fc;
			if (d.foldChange == undefined)  {
				fc = "null"
			}
			else {
				fc = d.foldChange.toFixed(2);
			}
			
			var pvalue;
			if (d.preferredPValue == undefined)  {
				pvalue = "null"
			}
			else {
				if (d.preferredPValue == 0)   {
					pvalue = "< 0.00001";
				}
				else {					
					pvalue = d.preferredPValue;
				}
			}

			var tooltip = 
	    				   "<table style='td{padding:5px}'>" +
	    				   "<tr><td width='100px'><b>Index</b></td><td>" + (d.x + 1) + "</td></tr>" +
	    				   "<tr><td><b>Study</b></td><td>" + analyses[d.x].studyID + "</td></tr>" +
	    				   "<tr><td ><b>Analysis</b></td><td>" + analyses[d.x].title + "</td></tr>" +
	    				   "<tr><td><b>Probe</b></td><td>" + d.probeId + "</td></tr>" +
	    				   "<tr><td><b>Gene</b></td><td>" + geneName  + "</td></tr>" +
	    				   "<tr><td><b>Organism</b></td><td>" + d.organism + "</td></tr>" +
	    				   "<tr><td><b>Fold Change</b></td><td>" + fc  + "</td></tr>" +
	    				   "<tr><td><b>Preferred pvalue</b></td><td>" + pvalue  + "</td></tr>" +
	    				   "</table>";
	    	
	    	hmTooltips[id] = tooltip;
	    	return id;
	    })
	    .attr('x', function(d) {
	    	return maxGeneWidth + d.x * wCell;
	    })
	    .attr('y', function(d) {
	    	return d.y * hCell + hHeader + hTitle;
	    })
	    .style("stroke", "#333333")    // color of borders around rectangles
	    .style("stroke-width", 1)    // width of borders around rectangles
	    .style("shape-rendering", "crispEdges")
	    .style('fill',function(d) {
	    	
	        if (d.foldChange == null)  {        	
	        	return "#FFFF00";
	        } 	
	        else  {
	        	//color the scale using a combination of the p-value (-log10 transformed) and the fold change
	        	var pvalue=d.preferredPValue;
	        	
	        	if(pvalue == null){pvalue=1}//for the calculation below, use 1 for the pvalue if it is null
	        		else if(pvalue<0.00001){pvalue=0.00001;} //use .00001 as the smallest allowed p-value
	        	
	        	return colorScale(d.foldChange * (-Math.log(pvalue) / Math.LN10));
	        }
	      
	    })
	    ;

	//GROUP FOR Column headers
	var headerGroup = hm.append("svg:g")
	  .attr("class", "columnHeader")
	  .attr("transform", "translate(" + maxGeneWidth + "," + (hHeader + hTitle) + ")")
	  ;
	
    // Show the gene labels
	var headerGroupText = headerGroup.selectAll("text")
		.data(analyses)
		.enter().append("text")
		.attr("x",function(d, i)	{
			return (i * wCell + wCell/2 ); 
		    })
		.attr("y", -2)
		.attr("text-anchor", "middle")
		.attr("cursor","default")
	    .attr("class", "heatmapTooltip")
	    .attr("id", function(d, i) {
	    	var id = "hmHeader" + uniqueHeatmapId++;   // id here will match id in tooltip array
			var tooltip = 
	    				   "<table>" +
	    				   "<tr><td width='100px'><b>Index</b></td><td>" + (i + 1) + "</td></tr>" +
	    				   "<tr><td><b>Study</b></td><td>" + d.studyID + "</td></tr>" +
	    				   "<tr><td ><b>Analysis</b></td><td>" + d.title + "</td></tr>" 
	    				   "</table>";
	    	
	    	hmTooltips[id] = tooltip;
	    	return id;
	    })		
	    .style("font-size", ctaHeaderFontSize + "px")
   	    .style("font-family", ctaHeaderFontFamily)
		.text(function(d, i)	{
			return i + 1;
		} )		
        ;				
	
    // GENE LABELS
	xOffset = 0;
	var geneGroup = hm.append("svg:g")
	  .attr("class", "geneGroup")
	  .attr("transform", "translate(" + 0 + "," + (hTitle + hHeader) + ")")
	  ;
	
    // Show the gene labels
	var geneGroupText = geneGroup.selectAll("a")
		.data(geneLabels)
		.enter().append("a")					//openGeneFromCTAheatmap(keywordId, termName, categoryId)
		.attr("xlink:href", function(d) {return "javascript:openGeneFromCTAheatmap(" + d.searchKeywordId + ", '" +d.geneName +"', 'GENE');"})
		.append("text")
		.attr("x", 0)
		.attr("y",function(d, i)	{
			return (i + 1) * hCell; 
		    })
		.attr("width", maxGeneWidth)
		.attr("text-anchor", "start")
	    .style("font-size", ctaGeneLabelFontSize + "px")
	    .style("font-family", ctaGeneLabelFontFamily)	    
	    .style("font-weight", ctaGeneLabelFontWeight)	    
		.text(function(d)	{
			return d.geneName;
		} )		
        ;			
	
	 registerHeatmapTooltipEvents();
	
	 // draw the legend whether exporting or not -- will be hidden by truncation if not exporting
	 drawCTAAnalysisLegend(svg, 0, analysisLegendOffset, analysisInfo);
	
 	 jQuery("#" + divID).css('display', savedDisplayStyle);
	
}

this.registerHeatmapTooltipEvents = function(){
	// create the method for the hover event for tooltips on the favorites for faceted searches
	jQuery(".heatmapTooltip").hoverIntent(
		{
			over:function(e){
				//var elementId = e.currentTarget.id;
				showHeatmapTooltip(e);
			},
			out: function(){
				jQuery("#heatmapTooltip").remove();
			},
			interval:200
		});
	
};

function showHeatmapTooltip(e)  {

	var xOffset = 20;
	var yOffset = 20;		
	
	// create the div tag which will hold tooltip
	jQuery("body").append("<div id='heatmapTooltip'></div>");
	
	var tooltip = hmTooltips[e.currentTarget.id];
	
	jQuery("#heatmapTooltip")
		.css("z-index", 10000)
		.html(tooltip)
		.css("left",(e.pageX + yOffset) + "px")
		.css("top",(e.pageY - xOffset) + "px")
		.fadeIn(200)
		;
	
}

//export the cta heatmap 
function exportHeatmapCTAImage()
{	
		var svgID=  "#xtHeatmap";
	
		
		d3.select(svgID)
			.selectAll("svg")
			.attr("height", ctaHeatmapHeightWithLegend);   
		
		exportCanvas(svgID);
		
		d3.select(svgID)
			.selectAll("svg")
			.attr("height", ctaHeatmapHeightWithoutLegend);   

}


