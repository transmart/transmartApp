<!DOCTYPE html>
<html>
    <head>
        <!-- Force Internet Explorer 8 to override compatibility mode -->
        <meta http-equiv="X-UA-Compatible" content="IE=edge" >        
        
        <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
        <title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
        
        <!-- jQuery CSS for cupertino theme -->
        <link rel="stylesheet" href="${resource(dir:'css/jquery/ui', file:'jquery-ui-1.9.1.custom.css')}"></link>        
        <link rel="stylesheet" href="${resource(dir:'css/jquery/skin', file:'ui.dynatree.css')}"></link>        
        
        <!-- Our CSS -->
        <link rel="stylesheet" href="${resource(dir:'css', file:'jquery.loadmask.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'main.css')}"></link>        
        <link rel="stylesheet" href="${resource(dir:'css', file:'rwg.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'colorbox.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'jquery/simpleModal.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'jquery/multiselect/ui.multiselect.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'jquery/multiselect/common.css')}"></link>
        <link rel="stylesheet" href="${resource(dir:'css', file:'jquery/jqueryDatatable.css')}"></link>
                                
        <!-- jQuery JS libraries -->
        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.min.js')}"></script>   
	    <script>jQuery.noConflict();</script> 
        
        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery-ui-1.9.1.custom.min.js')}"></script>
        
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
  		  
  		        
  		<!--Datatable styling and scripts-->
        <script type="text/javascript" src="${resource(dir:'js/', file:'jquery.dataTables.min.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'ColVis.min.js')}"></script> 
  		        
  		<!--  SVG Export -->
  		<%--<script type="text/javascript" src="${resource(dir:'js', file:'svgExport/rgbcolor.js')}"></script>  --%>
  		  
	
        <g:javascript library="prototype" /> 
        <script type="text/javascript">
            var $j = jQuery.noConflict();
        </script>
        
        <!-- Our JS -->        
        <script type="text/javascript" src="${resource(dir:'js', file:'rwg.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'rwgsearch.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'maintabpanel.js')}"></script>
        
        <!-- Protovis Visualization library and IE plugin (for lack of SVG support in IE8 -->
        <%-- <script type="text/javascript" src="${resource(dir:'js/protovis', file:'protovis-r3.2.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js/protovis', file:'protovis-msie.min.js')}"></script> --%>

		<tmpl:/RWG/urls />
		<script type="text/javascript" charset="utf-8">
	        var mouse_inside_options_div = false;

	        jQuery(document).ready(function() {
		        
		        addToggleButton();

		        jQuery('#meta-').accordion(); 
			        
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

	        	<%--jQuery('#topTabs').tabs();	
	        	jQuery('#topTabs').bind( "tabsshow", function(event, ui) {
		        	var id = ui.panel.id;
	        	    if (ui.panel.id == "study-view-div") {
	        	    	
	        	    } else if (ui.panel.id == "subject-view-div")	{
						
	        	    }
	        	});--%>
	        	jQuery('#studyTabs').tabs();

	        	jQuery('#sidebartoggle').click(function() {
					toggleSidebar();
		        });

	        	
	        //    var resize= $("#sidebar");
	      //        var containerWidth = $("#main").width();
	                        
	/*                $("#sidebar").resizable({
	                      handles: 'e',
	                      maxWidth: 450,
	                      minWidth: 120,
	                });

                    resize: function(event, ui){
                        var currentWidth = ui.size.width;
                        
                        // this accounts for padding in the panels + 
                        // borders, you could calculate this using jQuery
                        var padding = 12; 
                        
                        // this accounts for some lag in the ui.size value, if you take this away 
                        // you'll get some instable behaviour
                        $(this).width(currentWidth);
                        
                        // set the content panel width
                        $("#content").width(containerWidth - currentWidth - padding);            
                    }
	*/        	
	    	    jQuery('body').on('mouseenter', '.folderheader', function() {
					jQuery(this).find('.foldericonwrapper').fadeIn(150);
		    	});

	    	    jQuery('body').on('mouseleave', '.folderheader', function() {
					jQuery(this).find('.foldericonwrapper').fadeOut(150);
		    	});

	    	    jQuery('body').on('click', '.foldericon.add', function() {
					var count = jQuery('#cartcount').text();
					count++;
					jQuery('#cartcount').text(count);
		    	});

	    	    jQuery('body').on('click', '.foldericon.view', function() {
		    	    var id = jQuery(this).closest(".folderheader").attr('name');
	    	    	showDetailDialog(experimentDataUrl + '?id=' + id);
		    	});

	    	    jQuery('body').on('click', '.foldericon.viewfile', function() {
		    	    var id = jQuery(this).closest(".folderheader").attr('name');
	    	    	showDetailDialog(fileDataUrl + '?id=' + id);
		    	});

	    /*	    jQuery('#cartbutton').click(function() {
					jQuery('#exportViewLink').click();
		    	});
	      */  	

	        	jQuery('#sidebar-accordion').accordion({heightStyle: "fill", icons: { 'header': 'suppressicon', 'headerSelected': 'suppressicon' }});
	        	resizeAccordion();
	        	
	        	jQuery('#filter-browser').dialog({
	        		autoOpen: false,
	        		width:200,
	        		height:400,
	        		resizable:true,
	        		show: 'fade',
	        		hide: 'fade',
	        		title: 'Filter Browser'
		        });
                 
	        	jQuery('#sidebar').resizable({
                    handles: 'e',
                    maxWidth: 800,
                    minWidth: 120,
                    stop: function(event, ui){
                        var currentWidth = ui.size.width;
                        
                        // this accounts for padding in the panels + 
                        // borders, you could calculate this using jQuery
                        var padding = 12; 
                        
                        // this accounts for some lag in the ui.size value, if you take this away 
                        // you'll get some unstable behaviour
                    //    $(this).width(currentWidth);
                        
                        jQuery('#box-search').width(currentWidth -20)
                        jQuery('#sidebar-accordion').width(currentWidth -20)
                        // jQuery('#results-div').width(currentWidth -20)
                        // set the content panel width
                        jQuery('#main').width(jQuery('body').width() - currentWidth - padding);            
                    }
              });

	        	var xpos = jQuery('#menuLinks').offset()['right'];
	        	
	        	 jQuery('#cartbutton').css({
	        		    "position":"absolute", 
	        		    "top": "3px",
	        		    "left": xpos - 20 + "px",
	        		});
	        		
	        });

	        jQuery(window).resize(function() {
				resizeAccordion();
			});

	        <%-- TODO Accordion is no longer needed, can simplify a lot of this by just making it a normal set of divs 
	        
	        Now that the left nav is resizable this needs to be updated - wvet
	        
	        --%>
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
	        	jQuery('#results-div').height(targetHeight);

	        	jQuery('#datasetExplorer').height(windowHeight - 70);
	        	jQuery('#welcome').height(windowHeight - 90);
			}

			function toggleSidebar() {
				var sidebarIsVisible = (jQuery('#sidebar:visible').size() > 0);
				if (sidebarIsVisible) {
					jQuery('#sidebar').fadeOut(resizeAccordion);
					var bgimg = jQuery('#sidebartoggle').css('background-image').replace('-left', '-right');
					jQuery('#sidebartoggle').css('background-image', bgimg);
				}
				else {
					jQuery('#sidebar').fadeIn();
					resizeAccordion(); //Not a callback here - resize as soon as it starts appearing.
					var bgimg = jQuery('#sidebartoggle').css('background-image').replace('-right', '-left');
					jQuery('#sidebartoggle').css('background-image', bgimg);
				}
			}

			function showTab(tab) {
				if (tab == 'browse') {
//					jQuery('#metadata-viewer').show();
                    jQuery('#folder-viewer').show();
					jQuery('#subject-view-div').hide();
				}
				else {
//					jQuery('#metadata-viewer').hide();
                    jQuery('#folder-viewer').hide();
					jQuery('#subject-view-div').show();
				}
			}


    function dataTableWrapper (containerId, tableId, title)
            {

                var data;
                var gridPanelHeaderTips;
                
                function setupWrapper()
                {
                    var gridContainer =  $j('#' + containerId);
                    gridContainer.html('<table id=\'' + tableId + '\'></table></div>');
                }

                function overrideSort() {

                    $j.fn.dataTableExt.oSort['numeric-pre']  = function(a) {
                        
                        var floatA = parseFloat(a);
                        var returnValue;
                        
                        if (isNaN(floatA))
                            returnValue = Number.MAX_VALUE * -1;    //Emptys will go to top for -1, bottom for +1   
                            else
                                returnValue = floatA;
                        
                            return returnValue;
                        };

                };

                this.loadData = function(dataIn) {


                    setupWrapper();
                    
                    data = dataIn;
                    setupGridData(data);

                    gridPanelHeaderTips = data.headerToolTips.slice(0);

                    //Add the callback for when the grid is redrawn
                    data.fnDrawCallback = function( oSettings ) {

                        //Add the tooltips to the header. This must happen every redraw because the datatables code destroys the html
                        $j(".dataTables_scrollHeadInner > table > thead > tr > th").each( function (index) {
                            
                            var titleAttr = $j(this).attr("title");
                            
                            if (titleAttr == null && gridPanelHeaderTips != null)
                            {
                                $j(this).attr("title", gridPanelHeaderTips[index]);            
                            }
                            
                        });

                    };

                    $j('#' + tableId).dataTable(data);

                    $j(window).bind('resize', function () {
                        $j('#' + tableId).dataTable().fnAdjustColumnSizing()
                      } );
                    
                     $j("#" + containerId + " div.gridTitle").html(title);                  

                }; 

                function setupGridData(data)
                {
                    data.bAutoWidth = true;
                    data.bScrollAutoCss = true;
//                    data.sScrollY = 400;
                    data.sScrollX = "100%";
                    data.bDestroy = true;
                    data.bProcessing = true;
                    data.bLengthChange = false;
                    data.bScrollCollapse = false;
                    data.iDisplayLength = 100;
                    data.sDom = "<\"top\"<\"gridTitle\">p>rt<\"clear\">";    //This controls the grid layout and included functionality
                }
            }
		
//		var panel = createOntPanel()
//		jQuery('#metadata-viewer').empty()
 //           jQuery('#metadata-viewer').add(panel);
        </script>
          
        <script type="text/javascript">		
			jQuery(function ($) {
				// Load dialog on click of Save link
				$('#save-modal .basic').click(openSaveSearchDialog);
			});
		</script>
                  
       <r:layoutResources/>          
    </head>
    <body>
    
        <div id="header-div" class="header-div">        
            <g:render template="/layouts/commonheader" model="['app':'rwg', 'utilitiesMenu':'true']" />
        </div>
        
		<div id="sidebar" style="border-right:3px solid;border-color:#EDEEF6">
		
			<%-- 
				Some code that needs justification here... jQuery Tabs assumes that the tabs will be followed by a
				collection of divs with the intended content. We want these tabs to affect a pane to the right
				instead - so we set up an invisible div for each tab, then call a function to display the tab we
				actually want (and to do any additional setup work).
			 
			<div id="topTabs" class="analysis-tabs">
		       <ul>
		          <li id="studyViewTab"><a href="#studyFake" onclick="showTab('browse')">Browse</a></li>
		          <li id="subjectViewTab"><a href="#subjectFake" onclick="showTab('analyze')">Analyze</a></li>
		       </ul>
		       
				<div id="studyFake" style="height: 0px; padding: 0"></div>
				<div id="subjectFake" style="height: 0px; padding: 0"></div>
				
		    </div>
		    --%>
	       
	        <div id="box-search">
		        <div id="title-search-div" class="ui-widget-header">
			         <h2 style="float:left" class="title">Active Filters</h2>
					 <h2 style="float:right; padding-right:5px;" class="title">
					 	<a href="#" onclick="clearSearch(); return false;">Clear</a>
					 </h2> 
					 <div id="filterbutton" class="greybutton" onclick="jQuery('#filter-browser').dialog('open');">
						<img src="${resource(dir:'images', file:'filter.png')}"/> Filter
					 </div>
				</div>
				<div id="active-search-div" style="position: relative;">
					&nbsp;
				</div>
			</div>
			
			<div id="accordion-container" style="height: 600px">
				<div id="sidebar-accordion">
			        <h3>Program Explorer</h3>
			        <div id="results-div">
			        	Results appear here
			        </div>
			    </div>
		    </div>
		    
		    <div id="filter-div" style="display: none;"></div>
			
		</div>
		 
		<div id="main">		     	
                <div id="folder-viewer">
                <div id="welcome-viewer">
                    <tmpl:welcome />
                </div>
                <div id="metadata-viewer">
				</div>
				<div id="subfolder-viewer">
                </div>
                </div>
				
				<div id="subject-view-div" style="display: none;" >
				
				</div>
				<div id="export-div" style="display: none;">
				
				</div>
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
				<div id="xtSummary"><!-- Summary Tab Content --></div>
				<div id="xtHeatmap"><!-- Heatmap Tab Content --></div>
				<div id="xtBoxplot"><!-- Boxplot Tab Content --></div>
			</div>
		</div>

		<%-- Elements that are in fixed positions on the page --%>
		<div id="sidebartoggle">&nbsp;</div>
		<div id="search-div">
      		<table><tr>
      			<td><select id="search-categories"></select></td>
      			<td><input id="search-ac"/></input></td>
      		</tr></table>                                            
      	</div>
   		<div id="cartbutton" class="greybutton">
   		<g:remoteLink controller="export" action="selection" update="${overlayExportDiv}" 
                            params="[eleId:overlayExportDiv]" 
                            before="initLoadingDialog('${overlayExportDiv}')" onComplete="centerDialog('${overlayExportDiv}')">
			<img src="${resource(dir:'images', file:'cart.png')}"/> Export Cart
			</g:remoteLink>
			<div id="cartcount">0</div>
		</div>
      	
        <div id="filter-browser">			        	
        	<%-- TODO Source all of this from the database... obviously --%>
        	<div class="filtertitle" name="ASSAY_PLATFORM">Assay Platform</div>
        	<div class="filtercontent" name="ASSAY_PLATFORM" style="display: none;">
        		<div class="filteritem" name="ASSAY_PLATFORM" id="ap1">IHC</div>
        		<div class="filteritem" name="ASSAY_PLATFORM" id="ap2">mRNA Profiling</div>
        		<div class="filteritem" name="ASSAY_PLATFORM" id="ap3">SNP Profiling</div>
        		<div class="filteritem" name="ASSAY_PLATFORM" id="ap4">ELISA</div>
        	</div>
        	<div class="filtertitle" name="COMPOUND">Compound</div>
        	<div class="filtercontent" name="COMPOUND" style="display: none;">
        		<div class="filteritem" name="COMPOUND" id="co1">XL147</div>
        		<div class="filteritem" name="COMPOUND" id="co2">BSI-201</div>
        	</div>
        	<div class="filtertitle" name="ACCESS_TYPE">Access Type</div>
        	<div class="filtercontent" name="ACCESS_TYPE" style="display: none;">
        		<div class="filteritem" name="ACCESS_TYPE" id="at1">Proprietary</div>
        		<div class="filteritem" name="ACCESS_TYPE" id="at2">Public</div>
        	</div>
        </div>
        	
       <!--  Used to measure the width of a text element (in svg plots) -->
       <span id="ruler" style="visibility: hidden; white-space: nowrap;"></span> 
	 <r:layoutResources/>
	 <g:overlayDiv divId="${overlayExportDiv}" />
	 </body>
</html>
