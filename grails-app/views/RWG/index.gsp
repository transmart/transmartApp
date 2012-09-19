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
  		        
  		<!--  SVG Export -->
  		<script type="text/javascript" src="${resource(dir:'js', file:'svgExport/rgbcolor.js')}"></script>  
  		  
	
        <g:javascript library="prototype" /> 
        
        <!-- Our JS -->        
        <script type="text/javascript" src="${resource(dir:'js', file:'rwg.js')}"></script>
        
        <!-- Protovis Visualization library and IE plugin (for lack of SVG support in IE8 -->
        <script type="text/javascript" src="${resource(dir:'js/protovis', file:'protovis-r3.2.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js/protovis', file:'protovis-msie.min.js')}"></script> 

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
	        var getLinePlotDataURL = "${createLink([action:'getLinePlotData'])}";	        
	        var saveSearchURL = "${createLink([action:'saveFacetedSearch'])}";
	        var loadSearchURL = "${createLink([action:'loadFacetedSearch'])}";
	        var deleteSearchURL = "${createLink([action:'deleteFacetedSearch'])}";
	        var exportAsImage = "${createLink([action:'exportAsImage'])}";

			//These are the URLS for the different browse windows.
			var studyBrowseWindow = "${createLink([controller:'experiment',action:'browseExperimentsMultiSelect'])}";
			var getGWASDataURL = "${createLink([controller:'search',action:'getGwasResults'])}";

	       	
	        var mouse_inside_options_div = false;

	        jQuery(document).ready(function() {
		        
		        addSelectCategories();
		        addSearchAutoComplete();
		        addToggleButton();

		        jQuery("#xtButton").colorbox({opacity:.75, inline:true, width:"95%", height:"95%"});
      

		    	showSearchResults(); //reload the full search results	    

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
 
	        });	
            
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
            <g:render template="/layouts/commonheader" model="['app':'rwg']" />
        </div>
		 
		<div id="main">
			<div id="menu_bar">
			    <!-- 
				<div class='toolbar-item' id="xtButton" href="#xtHolder">Cross Trial Analysis</div>
				<div id='analysisCountDiv' class='toolbar-item'>
					<label id="analysisCountLabel">No Analysis Selected</label>
					<input type="hidden" id="analysisCount" value="0" />
				</div>
				<div class='toolbar-item'>Expand All</div>

				 -->
				<div class='toolbar-item' onclick='collapseAllAnalyses();'>Collapse All</div>			
	  			<div id="searchResultOptions_holder">
					<div id="searchResultOptions_btn" class='toolbar-item'>
						 Options <img alt="" style='vertical-align:middle;' src="${resource(dir:'images',file:'tiny_down_arrow.png')}" />
					</div>
					<div id="searchResultOptions" class='auto-hide' style="display:none;">
						<ul>
							<li>
								<select id="probesPerPage" onchange="showSearchResults(); ">
								    <option value="10">10</option>
								    <option value="20" selected="selected">20</option>
								    <option value="50">50</option>
								    <option value="100">100</option>
								    <option value="150">150</option>
								    <option value="200">200</option>
								    <option value="250">250</option>
								</select>  Probes/Page
							</li>
							<li>
								<input type="checkBox" id="cbShowSignificantResults" checked="true" onclick="showSearchResults(); ">Show only significant results</input>		
							</li>
						</ul>
					</div>
				</div>		
		</div>
		
        <div id="results-div"></div>

         	
		</div>
              
        <div id="search-div">         
            <select id="search-categories"></select>                          
            <input id="search-ac"/></input>                                                          
        </div>
        
        <div id="title-search-div" class="ui-widget-header">
	         <h2 style="float:left" class="title">Active Filters</h2>
			 <h2 style="float:right; padding-right:5px;" class="title">
			 	<div id='save-modal'>
			 		<a href="#" class="basic">Save</a>
				 </div>
			 	<a href="#" onclick="loadSearch(); return false;">Load</a>
			 	<a href="#" onclick="clearSearch(); return false;">Clear</a>
			 </h2> 
		</div>

		<!-- Save search modal content -->
		<div id="save-modal-content" style="display:none;">
			<h3>Save Faceted Search</h3><br/>
			Enter Name <input type="text" id="searchName" size="50"/><br/><br/>
			Enter Description <textarea id="searchDescription" rows="5" cols="70" ></textarea><br/>
			<br/>
			<a href="#" onclick="saveSearch(); return false;">Save</a>&nbsp;   
			<a href="#" onclick="jQuery.modal.close();return false;">Cancel</a>   
                     
			
		</div>
				
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
		
		<!--  This is the DIV we stuff the browse windows into. -->
		<div id="divBrowsePopups" style="display: none;">
			TESTSTUFFSTUFF
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
		
		       <!--  Used to measure the width of a text element (in svg plots) -->
		       <span id="ruler" style="visibility: hidden; white-space: nowrap;"></span> 
		
    </body>
</html>
