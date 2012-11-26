
<script type="text/javascript">		
	jQuery(function ($) {

		displayxtAnalysesList();

    	jQuery( "#xtSelectedAnalysesList" ).sortable();
    	jQuery( "#xtSelectedAnalysesList" ).disableSelection();

    	jQuery('#xtSummaryChartArea').sortable();

    	jQuery('#xtHeatmapTab').sortable();
    	
    	

    	//displaySelectedAnalysisTopGenes();
    	
    	getCrossTrialSummaryTableStats();
    	
    	addXTSearchAutoComplete();

    		var tabID = "#xtMenuBar";
    		jQuery(tabID).tabs();	
    		jQuery(tabID).bind( "tabsshow", function(event, ui) {
    		   
    		});

	});
</script>

<div id="xtHolder">
	<h1>Cross Trial Analysis</h1>

	<div id ="xtSearch">
		Search for gene, pathway, or gene signature: <input id="xtSearch-ac"/></input> 
		<div style="float:right">
			<a href="#" id="save-modal-xt" class='title-link-inactive'><span>Save Selection</span></a> | <a href="#" id='load-modal-xt' class='title-link-active' onclick="openLoadSearchDialog(true);"}>Load Analysis</a> | <a href="#" id="clear-xt" class='title-link-inactive' onclick="clearAllSelectedAnalyses();">Clear</a>
		</div>
	</div>
	
	<div style="width: 95%;margin: auto;position: absolute;z-index: 700;">
		<div id="xtMsgBox" style="display:none">
			<p style="text-align:center; margin-bottom:8px">The analysis selection has changed</p>
			<p style="text-align:center"><a href="#" onclick="updateCrossTrialGeneCharts();" class=btn>Redraw charts</a>
			<a href="#" onclick="clearAllXTSearchTerms();" class="btn">Clear all</a></p>
		</div>
	</div>
	
	<div id="xtMenuBar" class="analysis-tabs">
		<ul>
			<li><a href="#xtSummary">Summary Table</a></li>
			<li><a href="#xtGeneChartTab">Gene Charts</a></li>
			<li><a href="#xtHeatmapTab">Heatmap</a></li>
		</ul>
		
		<!-- Summary Table -->
		<div id="xtSummary">
		
			<div id="xtTopGenes"></div>
			<div id="xtSummaryTable"></div>
		
		</div>		
		
		<!--  Gene Charts Tab -->
		<div id="xtGeneChartTab">
			<div id="xtSummaryChartArea"></div>
			<div id="xtNoGenesMsg" class="xtInfoBox">
			<p>No genes are selected.</p><br/><p> Use the search box above to add genes for analysis.</p></div>
		</div>
		
		<!-- Heatmap Tab Content -->
		<div id ="xtHeatmapTab">
		<!-- 	<a href="#" onclick="javascript:exportHeatmapCTAImage();">Export Heatmap Image</a> -->
			<div id="xtNoHeatmapMsg" class="xtInfoBox">
			<p>No gene signatures or pathways are selected.</p><br/><p> Use the search box above to view gene lists, gene signatures, or pathways.</p></div>
		</div>
		
	</div>
	
	
	<div id="xtAnalysisList">
		<h2>Selected Analyses</h2>
		<div id="xtSummary_AnalysesList"></div>
	</div>

	<div id="xtNoAnalysesMsg" class="xtInfoBox" style="display:none">
		<p>No analyses are selected.</p><br />
		<p>Select analyses for comparison by using the check boxes next to each analysis in the search results,
		or load a saved comparison.</p>
	</div>


	

</div>


	
<!-- Boxplot Content - dialog -->
<div id ="xtBoxplotHolder" style="display:none">
	<div id="xtBoxplot"></div>
	<a href="#" onclick="javascript:exportBoxPlotCTAImage();">Export Box Plot as an Image</a>
</div>    

