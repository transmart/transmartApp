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
        <link rel="stylesheet" href="${resource(dir:'css', file:'jquery/multiselect/ui.multiselect.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'jquery/multiselect/common.css')}"></link>
                                
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
  		<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.simplemodal.min.js')}"></script>  
  		<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.dataTables.js')}"></script>
  		<script type="text/javascript" src="${resource(dir:'js', file:'facetedSearch/facetedSearchBrowse.js')}"></script>
  		<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/ui.multiselect.js')}"></script>  
  		  
  		        
  		<!--  SVG Export -->
  		<%--<script type="text/javascript" src="${resource(dir:'js', file:'svgExport/rgbcolor.js')}"></script>  --%>
  		  
	
        <g:javascript library="prototype" /> 
        
        <!-- Our JS -->        
        <script type="text/javascript" src="${resource(dir:'js', file:'rwg.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'maintabpanel.js')}"></script>
        
        <!-- Protovis Visualization library and IE plugin (for lack of SVG support in IE8 -->
        <%-- <script type="text/javascript" src="${resource(dir:'js/protovis', file:'protovis-r3.2.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js/protovis', file:'protovis-msie.min.js')}"></script> --%>

        <script type="text/javascript" charset="utf-8">
	        var searchResultsURL = "${createLink([action:'loadSearchResults'])}";
	        var facetResultsURL = "${createLink([action:'getFacetResults'])}";
	        var facetTableResultsURL = "${createLink([action:'getFacetResultsForTable'])}";
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
	        var getLinePlotDataURL = "${createLink([action:'getLinePlotData'])}";	        
	        var saveSearchURL = "${createLink([action:'saveFacetedSearch'])}";
	        var loadSearchURL = "${createLink([action:'loadFacetedSearch'])}";
	        var deleteSearchURL = "${createLink([action:'deleteFacetedSearch'])}";
	        var exportAsImage = "${createLink([action:'exportAsImage'])}";

	        var getStudyAnalysesUrl = "${createLink([controller:'RWG',action:'getTrialAnalysis'])}";
        
			//These are the URLS for the different browse windows.
			var studyBrowseWindow = "${createLink([controller:'experiment',action:'browseExperimentsMultiSelect'])}";
			var analysisBrowseWindow = "${createLink([controller:'experimentAnalysis',action:'browseAnalysisMultiSelect'])}";
			var regionBrowseWindow = "${createLink([controller:'RWG',action:'getRegionFilter'])}";
			var dataTypeBrowseWindow = "${createLink([controller:'RWG',action:'browseDataTypesMultiSelect'])}";
			var getTableDataURL = "${createLink([controller:'search',action:'getTableResults'])}";
			var getAnalysisDataURL = "${createLink([controller:'search',action:'getAnalysisResults'])}";
			var getQQPlotURL = "${createLink([controller:'search',action:'getQQPlotImage'])}";

			var webStartURL = "${createLink([controller:'search',action:'webStartPlotter'])}";
			var datasetExplorerURL = "${createLink([controller:'datasetExplorer'])}";

			var crossImageURL = "${resource([dir:'images', file:'small_cross.png'])}";
	       	
	        var mouse_inside_options_div = false;

	        jQuery(document).ready(function() {
		        
		        addSelectCategories();
		        addSearchAutoComplete();
		        addToggleButton();

		        jQuery("#xtButton").colorbox({opacity:.75, inline:true, width:"95%", height:"95%"});
      

		    	showSearchResults('analysis'); //reload the full search results for the analysis/study view

		    	//Disabling this, we aren't using the d3js code that takes advantage of HTML5.
		    	//showIEWarningMsg();


		        jQuery("#searchResultOptions_btn").click(function(){
		        	jQuery("#searchResultOptions").toggle();
		        	});
		        
		        //used to hide the options div when the mouse is clicked outside of it

	            jQuery('#searchResultOptions_holder').hover(function(){ 
	            	mouse_inside_options_div=true; 
	            }, function(){ 
	            	mouse_inside_options_div=false; 
	            });

	            jQuery("body").mouseup(function(){ 
		            //top menu options
	                if(! mouse_inside_options_div ){
		                jQuery('#searchResultOptions').hide();
	                }

	                var analysisID = jQuery('body').data('heatmapControlsID');

	                if(analysisID > 1){
	            		jQuery('#heatmapControls_' +analysisID).hide();
		             }

	            });

	        	jQuery('#topTabs').tabs();	
	        	jQuery('#topTabs').bind( "tabsshow", function(event, ui) {
		        	var id = ui.panel.id;
	        	    if (ui.panel.id == "results-div") {
	        	    	
	        	    } else if (ui.panel.id == "table-results-div")	{
						
	        	    }
	        	});

	        	jQuery('#sidebartoggle').click(function() {
					toggleSidebar();
		        });

	        	jQuery('#table-results-div').append(
	        		jQuery("<iframe></iframe>")
	        			.attr("id", "datasetExplorer")
	        			.attr("name", "datasetExplorer")
	        			.attr("src", datasetExplorerURL)
	    	    );
	        	

	        	jQuery('#sidebar-accordion').accordion({heightStyle: "fill", icons: { 'header': 'suppressicon', 'headerSelected': 'suppressicon' }});
	        	resizeAccordion();
	        });

	        jQuery(window).resize(function() {
				resizeAccordion();
			});

			function resizeAccordion() {
				
				var windowHeight = jQuery(window).height();
				var sidebarIsVisible = (jQuery('#sidebar:visible').size() > 0);
				if (!sidebarIsVisible) {
		        	jQuery('#main').css('width', '100%');
				}
				else {
					jQuery('#main').width(jQuery(window).width()-310);
				}
	        	jQuery('#sidebar').height(jQuery(window).height()-30);
				var ypos = jQuery('#sidebar-accordion').offset()['top'];
	        	
	        	var targetHeight = windowHeight - ypos - 90;
	        	jQuery('#filter-browser').height(targetHeight);
	        	jQuery('#metadata-viewer').height(targetHeight);

	        	jQuery('#datasetExplorer').height(windowHeight - 70);
			}

			function toggleSidebar() {
				var sidebarIsVisible = (jQuery('#sidebar:visible').size() > 0);
				if (sidebarIsVisible) {
					jQuery('#sidebar').fadeOut(resizeAccordion);
					var bgimg = jQuery('#sidebartoggle').css('background-image').replace('-right', '-left');
					jQuery('#sidebartoggle').css('background-image', bgimg);
				}
				else {
					jQuery('#sidebar').fadeIn();
					resizeAccordion(); //Not a callback here - resize as soon as it starts appearing.
					var bgimg = jQuery('#sidebartoggle').css('background-image').replace('-left', '-right');
					jQuery('#sidebartoggle').css('background-image', bgimg);
				}
			}

			
            
        </script>
        
                
        <script type="text/javascript">		
			jQuery(function ($) {
				// Load dialog on click of Save link
				$('#save-modal .basic').click(openSaveSearchDialog);
			});
		</script>
                  
                
    </head>
    <body>
        <div id="header-div">        
            <g:render template="/layouts/commonheader" model="['app':'rwg', 'utilitiesMenu':'true']" />
        </div>
		 
		<div id="main">
			<div id="topTabs" class="analysis-tabs">
		       <ul>
		          <li id="analysisViewTab"><a href="#results-div" onclick="return false;">Study View</a></li>
		          <li id="tableViewTab"><a href="#table-results-div" onclick="return false;">Subject View</a></li>
		       </ul>
		     
	       		<div id="results-div">
	
	         	
				</div>
				
				<div id="table-results-div">
					
				</div> 
			</div>
		</div>
		
		<div id="sidebar">
	       
	        <div id="box-search">
		        <div id="title-search-div" class="ui-widget-header">
			         <h2 style="float:left" class="title">Active Filters</h2>
					 <h2 style="float:right; padding-right:5px;" class="title">
					 	<a href="#" onclick="clearSearch(); return false;">Clear</a>
					 </h2> 
				</div>
				<div id="active-search-div"></div>
			</div>
				

			
			<div id="accordion-container" style="height: 600px">
				<div id="sidebar-accordion">
					<h3>Filter Browser</h3>
			        <div id="filter-browser">
			        	<div id="search-div">
			        		<table><tr>
			        			<td><select id="search-categories"></select></td>
			        			<td><input id="search-ac"/></input></td>
			        		</tr></table>
				                                   
				                                                               
			        	</div>
			        	<div class="filtertitle" name="dataLevel">Data Level</div>
			        	<div class="filtercontent" name="dataLevel" style="display: none;">
			        		<div class="filteritem" name="dataLevel" id="dl1">One</div>
			        		<div class="filteritem" name="dataLevel" id="dl2">Two</div>
			        		<div class="filteritem" name="dataLevel" id="dl3">Three</div>
			        		<div class="filteritem" name="dataLevel" id="dl4">Four</div>
			        		<div class="filteritem" name="dataLevel" id="dl5">Five</div>
			        		<div class="filteritem" name="dataLevel" id="dl6">Six</div>
			        		<div class="filteritem" name="dataLevel" id="dl7">Seven</div>
			        		<div class="filteritem" name="dataLevel" id="dl8">Eight</div>
			        		<div class="filteritem" name="dataLevel" id="dl9">Nine</div>
			        	</div>
			        	<div class="filtertitle" name="otherFilter">Compound</div>
			        	<div class="filtercontent" name="otherFilter" style="display: none;">
			        		<div class="filteritem" name="otherFilter" id="of1">One</div>
			        		<div class="filteritem" name="otherFilter" id="of2">Two</div>
			        		<div class="filteritem" name="otherFilter" id="of3">Three</div>
			        		<div class="filteritem" name="otherFilter" id="of4">Four</div>
			        		<div class="filteritem" name="otherFilter" id="of5">Five</div>
			        		<div class="filteritem" name="otherFilter" id="of6">Six</div>
			        		<div class="filteritem" name="otherFilter" id="of7">Seven</div>
			        		<div class="filteritem" name="otherFilter" id="of8">Eight</div>
			        		<div class="filteritem" name="otherFilter" id="of9">Nine</div>
			        	</div>
			        	<div class="filtertitle" name="assayPlatform">Assay Platform</div>
			        	<div class="filtercontent" name="assayPlatform" style="display: none;">
			        		<div class="filteritem" name="assayPlatform" id="ap1">One</div>
			        		<div class="filteritem" name="assayPlatform" id="ap2">Two</div>
			        		<div class="filteritem" name="assayPlatform" id="ap3">Three</div>
			        		<div class="filteritem" name="assayPlatform" id="ap4">Four</div>
			        		<div class="filteritem" name="assayPlatform" id="ap5">Five</div>
			        		<div class="filteritem" name="assayPlatform" id="ap6">Six</div>
			        		<div class="filteritem" name="assayPlatform" id="ap7">Seven</div>
			        		<div class="filteritem" name="assayPlatform" id="ap8">Eight</div>
			        		<div class="filteritem" name="assayPlatform" id="ap9">Nine</div>
			        	</div>
			        </div>
			        <h3>Metadata Viewer</h3>
			        <div id="metadata-viewer">
			        	Select a folder on the left to view its metadata.
			        </div>
			    </div>
		    </div>
		    
		    <div id="filter-div" style="display: none;"></div>
			
		</div>
		<div id="hiddenItems" style="display:none">
		        <!-- For image export -->
		        <canvas id="canvas" width="1000px" height="600px"></canvas>  

		</div>
	
		<!--  This is the DIV we stuff the browse windows into. -->
		<div id="divBrowsePopups" style="width:800px; display: none;">
			
		</div>
		
		<!--  Another DIV for the manhattan plot options. -->
		<div id="divPlotOptions" style="width:300px; display: none;">
			<table class="columndetail">
				<tr>
					<td class="columnname">SNP Annotation Source</td>
					<td>
						<select id="plotSnpSource" style="width: 220px">
							<option value="19">Human Genome 19</option>
							<option value="18">Human Genome 18</option>
						</select>
					</td>
				</tr>
				<%--<tr>
					<td class="columnname">Gene Annotation Source</td>
					<td>
						<select id="plotGeneSource" style="width: 220px">
							<option id="GRCh37">Human Gene data from NCBI</option>
						</select>
					</td>
				</tr>--%>
				<tr>
					<td class="columnname">P-value cutoff</td>
					<td>
						<input id="plotPvalueCutoff" style="width: 210px">
					</td>
				</tr>
			</table>
		</div>
		
		<!--  Everything for the across trial function goes here and is displayed using colorbox -->
		<div style="display:none">
			<div id="xtHolder">
				<div id="xtTopbar">
					<p>Cross Trial Analysis</p>
					<ul id="xtMenu">
						<li>Summary</li>
						<li>Heatmap</li>
						<li>Boxplot</li>
					</ul>
					<p>close</p>
				</div>
				<div id="xtSummary"><!-- Summary Tab Content -->
							
				
				</div>
				<div id="xtHeatmap"><!-- Heatmap Tab Content -->
				
				
				</div>
				<div id="xtBoxplot"><!-- Boxplot Tab Content -->
				
				
				</div>
			</div>
		</div>
		
		<div id="logocutout">
			<img src="${resource(dir:'images', file:'logo.png')}"/>
		</div>
		<div id="sidebartoggle">&nbsp;</div>
       <!--  Used to measure the width of a text element (in svg plots) -->
       <span id="ruler" style="visibility: hidden; white-space: nowrap;"></span> 
	
    </body>
</html>
