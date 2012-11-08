<!DOCTYPE html>
<html>
    <head>  
        <!-- Force Internet Explorer 8 to override compatibility mode -->
        <meta http-equiv="X-UA-Compatible" content="IE=edge" >        
        
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
        
        <!-- jQuery CSS for cupertino theme -->
        <link rel="stylesheet" href="${resource(dir:'css/jquery/cupertino', file:'jquery-ui-1.8.18.custom.css')}"></link>        
        <link rel="stylesheet" href="${resource(dir:'css/jquery/skin', file:'ui.dynatree.css')}"></link>        
        
        <!-- Our CSS -->
        <link rel="stylesheet" href="${resource(dir:'css', file:'jquery.loadmask.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'main.css')}"></link>        
        <link rel="stylesheet" href="${resource(dir:'css', file:'rwg.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'colorbox.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'jquery/simpleModal.css')}"></link>

        <link rel="stylesheet" href="${resource(dir:'css', file:'searchTooltip.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'heatmapTooltip.css')}"></link>
                                
        <!-- jQuery JS libraries -->
        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.min.js')}"></script>   
	    <script>jQuery.noConflict();</script> 
        
        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery-ui.min.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.cookie.js')}"></script>   
        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.dynatree.min.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.paging.min.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.loadmask.min.js')}"></script>   
 		<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.ajaxmanager.js')}"></script>  
  		<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.numeric.js')}"></script>
  		<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.colorbox-min.js')}"></script>  
  		<script type="text/javascript" src="${resource(dir:'js', file:'searchTooltip.js')}"></script>  
  		<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.simplemodal.min.js')}"></script>  
        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.hoverIntent.min.js')}"></script>  
  		
  		<!--  SVG Export -->
  		<script type="text/javascript" src="${resource(dir:'js', file:'svgExport/rgbcolor.js')}"></script>  
  		<script type="text/javascript" src="${resource(dir:'js', file:'svgExport/canvg.js')}"></script>  
	        
        <!-- Our JS -->        
        <script type="text/javascript" src="${resource(dir:'js', file:'rwg.js')}"></script>
        
		<!-- d3js Visualization Library -->
		<script type="text/javascript" src="${resource(dir:'js/d3', file:'d3.v2.js')}"></script>
       
       	<!--Pie Chart -->
       	<script type="text/javascript" src="${resource(dir:'js', file:'piechart.js')}"></script>
        <link rel="stylesheet" href="${resource(dir:'css', file:'piechart.css')}"></link>
        
       	<!--Heatmap (d3) -->
       	<script type="text/javascript" src="${resource(dir:'js', file:'heatmap.js')}"></script>
       	<!--Boxplot (d3) -->
       	<script type="text/javascript" src="${resource(dir:'js', file:'boxplot.js')}"></script>
       	<!--Lineplot (d3) -->
       	<script type="text/javascript" src="${resource(dir:'js', file:'lineplot.js')}"></script>
       	<!--legend (d3) -->
       	<script type="text/javascript" src="${resource(dir:'js', file:'legend.js')}"></script>
       	<!--Functions shared by both line and box plots (d3) -->
       	<script type="text/javascript" src="${resource(dir:'js', file:'commonplot.js')}"></script>

        <!--textFlow add in -->
        <script type="text/javascript" src="${resource(dir:'js', file:'textFlow/helper_functions.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'textFlow/textFlow.js')}"></script>

        <script type="text/javascript" charset="utf-8">        
	        var searchResultsURL = "${createLink([action:'loadSearchResults'])}";
	        var facetResultsURL = "${createLink([action:'getFacetResults'])}";
	        var newSearchURL = "${createLink([action:'newSearch'])}";
	        var visualizationURL = "${createLink([action:'newVisualization'])}";
	        var tableURL = "${createLink([action:'newTable'])}";
	        var treeURL = "${createLink([action:'getDynatree'])}";
	        var sourceURL = "${createLink([action:'searchAutoComplete'])}";	      
	        var getCategoriesURL = "${createLink([action:'getSearchCategories'])}";
	        var getHeatmapNumberProbesURL = "${createLink([action:'getHeatmapNumberProbes'])}";
	        var getHeatmapDataURL = "${createLink([action:'getHeatmapData'])}";
	        var getHeatmapDataForExportURL = "${createLink([action:'getHeatmapDataForExport2'])}";
	        var getBoxPlotDataURL = "${createLink([action:'getBoxPlotData'])}";
            var getBoxPlotDataCTAURL = "${createLink([action:'getBoxPlotDataCTA'])}";
	        var getLinePlotDataURL = "${createLink([action:'getLinePlotData'])}";	        
	        var saveSearchURL = "${createLink([action:'saveFacetedSearch'])}";
	        var loadSearchURL = "${createLink([action:'loadFacetedSearch'])}";
	        var updateSearchURL = "${createLink([action:'updateFacetedSearch'])}";
	        var renderFavoritesTemplateURL = "${createLink([action:'renderFavoritesTemplate'])}";
	        var deleteSearchURL = "${createLink([action:'deleteFacetedSearch'])}";
	        var exportAsImage = "${createLink([action:'exportAsImage'])}";	        
	        var homeURL = "${createLink([action:'getHomePage'])}";	     
	        var crossTrialAnalysisURL = "${createLink([action:'getCrossTrialAnalysis'])}";	     
	        var mouse_inside_options_div = false;
	        var mouse_inside_analysis_div = false;
			var getPieChartDataURL = "${createLink([action:'getPieChartData'])}";
			var getTopGenesURL = "${createLink([action:'getTopGenes'])}";
			var getCrossTrialBioMarkerSummaryURL = "${createLink([action:'getCrossTrialBioMarkerSummary'])}";
	        var getHeatmapDataCTAURL = "${createLink([action:'getHeatmapDataCTA'])}";
	        var getHeatmapCTAGenesURL = "${createLink([action:'getHeatmapCTAGenes'])}";
	        			
			var showHomePageFirst=true;
			
	        jQuery(document).ready(function() {

	        	//set cookie
	        	if (jQuery.cookie('selectedAnalyses') == null){
	        		jQuery.cookie('selectedAnalyses', '');
	        	}
		        else{
	        		selectedAnalyses = jQuery.parseJSON(jQuery.cookie("selectedAnalyses"));

	        		if(selectedAnalyses==null){
	        			selectedAnalyses = [];
		        		}

	        		if(selectedAnalyses==null){
	        			jQuery("#analysisCountLabel").html("(0)");
		        	}else{
				        //update the header with the number of Analyses
			        	jQuery("#analysisCountLabel").html("(" +selectedAnalyses.length + ")");
			        }
	        	}

		        
		        addSelectCategories();
		        addSearchAutoComplete();
		        addToggleButton();

		    	showSearchResults(); //reload the full search results	    

		    	showIEWarningMsg();



		    	//Resize code
				setActiveFiltersResizable();


		        //used to hide the options div when the mouse is clicked outside of it
	            jQuery('#searchResultOptions_holder').hover(function(){ 
	            	mouse_inside_options_div=true; 
	            }, function(){ 
	            	mouse_inside_options_div=false; 
	            });

		        //used to hide the options div when the mouse is clicked outside of it
	            jQuery('#selectedAnalyses_holder').hover(function(){ 
	            	mouse_inside_analysis_div=true; 
	            }, function(){ 
	            	mouse_inside_analysis_div=false; 
	            });

	            jQuery("body").mouseup(function(){ 
		            //hide the options menu
	                if(! mouse_inside_options_div ){
		                jQuery('#searchResultOptions').hide();
	                }
	                //hide the selected analyses menu
	                if(! mouse_inside_analysis_div ){
		                jQuery('#selectedAnalysesExpanded').hide();
	                }

	                var analysisID = jQuery('body').data('heatmapControlsID');

	                if(analysisID > 1){
	            		jQuery('#heatmapControls_' +analysisID).hide();
		             }

	            });	

	            launchHomePage();

	        });	
            
        </script>
        
                
        <script type="text/javascript">		
			jQuery(function ($) {
				// Load dialog on click of Load link
				$('#load-modal .basic').click(openLoadSearchDialog);
			});
		</script>

                  
                
    </head>
    <body>
        <div id="header-div">        
            <g:render template="/layouts/commonheader" model="[app:rwg]" />
        </div>
		 
		<div id="main">
			<div id="menu_bar">
			
			<ul>
				<li class='toolbar-item'><a href="#" onclick='showHomePage();'><img style='vertical-align:top;' src="${resource(dir:'images/fc_menu',file:'menu_icon_home_bw.png')}"> Home</a></li>
				<li class='toolbar-item'><a href="#" onclick='showResultsPage();'><img style='vertical-align:top' src="${resource(dir:'images/fc_menu',file:'menu_icon_search_bw.png')}"> Search Results</a></li>
				<li class='toolbar-item'><a href="#" onclick='showCrossTrialAnalysis();'><img style='vertical-align:top' src="${resource(dir:'images/fc_menu',file:'menu_icon_crosstrial_bw.png')}"> Cross Trial Analysis</a>
				<span style="color: #999;"> | </span>
									
					<div id="selectedAnalyses_holder" style='display: inline-block'>
						<div id='selectedAnalyses_btn'>
							<a href="#" onclick='getSelectedAnalysesList();'>
								 <span id="analysisCountLabel">(0)</span>
								 <img alt="" style='vertical-align:middle;' src="${resource(dir:'images',file:'tiny_down_arrow.png')}" />
							</a>
						</div>
							<div id="selectedAnalysesExpanded" class='auto-hide' style="display:none;"></div>
					</div>
				</li>
				<li class='toolbar-item' id='toolbar-collapse_all' style='display:none;'><a href="#" onclick='collapseAllAnalyses();'>Collapse All</a></li>
				<li class='toolbar-item' id='toolbar-options'  style='display:none;'>			
					<div id="searchResultOptions_holder">
						<div id="searchResultOptions_btn">
							<a href="#" onclick='jQuery("#searchResultOptions").toggle();'>
								Options <img alt="" style='vertical-align:middle;' src="${resource(dir:'images',file:'tiny_down_arrow.png')}" />
							</a>
						</div>
						<div id="searchResultOptions" class='auto-hide' style="display:none;">
							<select id="probesPerPage" onchange="showSearchResults(); ">
							    <option value="10">10</option>
							    <option value="20" selected="selected">20</option>
							    <option value="50">50</option>
							    <option value="100">100</option>
							    <option value="150">150</option>
							    <option value="200">200</option>
							    <option value="250">250</option>
							</select>  Probes/Page
							<br /><br />
							<input type="checkBox" id="cbShowSignificantResults" checked="true" onclick="showSearchResults(); ">Show only significant results</input>		
						</div>
					</div>
				</li>		
			</ul>

		</div>
		
		<div id="home-div"></div>
        <div id="results-div"></div>
        <div id="cross-trial-div"></div>

         	
		</div>
       
        <div id="search-div">         
            <select id="search-categories"></select>                          
            <input id="search-ac"/></input>                                                          
        </div>
        
        <div id="title-search-div" class="ui-widget-header">
	         <h2 style="float:left" class="title">Active Filters</h2>
			 <h2 style="float:right; padding-right:5px;" class="title">
			 	<span id='save-modal' class='title-link-inactive'>
			 		Save
				</span>&nbsp;&nbsp;
				
			 	<span id='load-modal' style="z-index:1">
				 	<a href="#"  class="basic">Favorites</a>
				</span>&nbsp;&nbsp;
				
			 	<a href="#" onclick="clearSearch(); return false;">Clear</a>
			 </h2> 
		</div>

        <g:render template="saveFavoritesModal" />
        

		<div id="active-search-div"></div>
		 
		<div id="title-filter" class="ui-widget-header">
			 <h2 style="float:left" class="title">Filter Browser</h2>			 
		</div>
		<div id="side-scroll">
		        <div id="filter-div"></div>
		</div>
		<button id="toggle-btn"></button>
		
		<div id="hiddenItems" style="display:none">
		        <!-- For image export -->
		        <canvas id="canvas" width="1000px" height="600px"></canvas>  

		</div>
		
		<div id="load-modal-content" style="display:none;">
      		<!-- Load load favorites modal content is done via Ajax call-->
		</div>
		

       <!--  Used to measure the width of a text element (in svg plots) -->
       <span id="ruler" style="visibility: hidden; white-space: nowrap;"></span> 
       <div id="testTextHeightDiv" />         
    </body>
    
           		
    
</html>
