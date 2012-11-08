
<script type="text/javascript">		
	jQuery(function ($) {

		displayxtAnalysesList();

    	jQuery( "#xtSelectedAnalysesList" ).sortable();
    	jQuery( "#xtSelectedAnalysesList" ).disableSelection();

    	jQuery('#xtSummaryChartArea').sortable();
    	

    	displaySelectedAnalysisTopGenes();
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
	</div>
	
	<div id="xtMsgBox" style="display:none">
		<p style="text-align:center; margin-bottom:8px">The analysis selection has changed</p>
		<p style="text-align:center"><a href="#" onclick="updateCrossTrialGeneCharts();" class=btn>Redraw charts</a>
		<a href="#" onclick="clearAllXTSearchTerms();" class="btn">Clear all</a></p>
	</div>
	
	
	<div id="xtMenuBar" class="analysis-tabs">
		<ul>
			<li><a href="#xtSummary">Summary Table</a></li>
			<li><a href="#xtSummaryChartArea">Gene Charts</a></li>
			<li><a href="#xtHeatmapTab">Heatmap</a></li>
		</ul>
		
		<!-- Summary Table -->
		<div id="xtSummary">
		
			<div id="xtTopGenes"></div>
		
		</div>		
		
		<!--  Gene Charts Tab -->
		<div id="xtSummaryChartArea"></div>
		
		<!-- Heatmap Tab Content -->
		<div id ="xtHeatmapTab">
			<div id="xtHeatmap"></div>
			<a href="#" onclick="javascript:exportHeatmapCTAImage();">Export Heatmap Image</a>
			<div id="xtHeatmapPaginator" class="pagination" style="text-align:left"></div>
		</div>
		
	</div>
	
	
			
	<h2>Selected Analyses</h2>
	
	<div id="xtSummary_AnalysesList"></div>
	

</div>


	
<!-- Boxplot Content - dialog -->
<div id ="xtBoxplotHolder" style="display:none">
	<div id="xtBoxplot"></div>
	<a href="#" onclick="javascript:exportBoxPlotCTAImage();">Export Box Plot as an Image</a>
</div>    

