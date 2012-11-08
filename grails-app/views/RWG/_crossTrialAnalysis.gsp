
<script type="text/javascript">		
	jQuery(function ($) {

		displayxtAnalysesList();

    	jQuery( "#xtSelectedAnalysesList" ).sortable();
    	jQuery( "#xtSelectedAnalysesList" ).disableSelection();

    	jQuery('#xtSummaryChartArea').sortable();
    	

    //	displaySelectedAnalysisTopGenes();
    	addXTSearchAutoComplete();

		
	});
</script>

<div id="xtHolder">
	<h1>Cross Trial Analysis</h1>

	<div id ="xtSearch">
		Search for gene, pathway, or gene signature: <input id="xtSearch-ac"/></input> 
	</div>
	
	<div id="xtMenuBar">
		<ul id="xtMenu">
			<li>Summary Table</li>
			<li>Gene Charts</li>
			<li>Heatmap</li>
		</ul>
	</div>
	
	<div id="xtMsgBox" style="display:none">
		<p style="text-align:center; margin-bottom:8px">The analysis selection has changed</p>
		<p style="text-align:center"><a href="#" class=btn>Redraw charts</a>
		<a href="#" class="btn">Clear all</a></p>
	</div>
	<div id="xtSummary"><!-- Summary Tab Content -->
	
	<div id="xtSummaryChartArea"></div>
			
	<h2>Selected Analyses</h2>
	
	<div id="xtSummary_AnalysesList"></div>

	<div id="xtTopGenes"></div>
	
	<!-- XT Summary Tab  -->
	<div id="xtBioMarkerSummaryTab"></div>
			

</div>

<!-- Boxplot Tab Content -->

<div id ="xtHeatmapTab">
	<div id="xtHeatmap"></div>
	<a href="#" onclick="javascript:exportHeatmapCTAImage();">Export Heatmap Image</a>
</div>

<!-- Boxplot Content -->
<div id ="xtBoxplotHolder" style="display:none">
	<div id="xtBoxplot"></div>
	<a href="#" onclick="javascript:exportBoxPlotCTAImage();">Export Box Plot as an Image</a>
	<div id="xtHeatmapPaginator" class="pagination" style="text-align:left">
</div>    

<div id="exportCTAHeatmap">
    <a href="#" onclick="javascript:exportHeatmapCTAImage();">Export Heatmap Image</a>
</div>    


	
</div>

  </div>


