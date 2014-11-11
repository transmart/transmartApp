// store probe Ids for each analysis that has been loaded
var analysisProbeIds = new Array();

var openAnalyses = new Array(); //store the IDs of the analyses that are open


//create an ajaxmanager named rwgAJAXManager
//this will handle all ajax calls on this page and prevent too many 
//requests from hitting the server at once
var rwgAJAXManager = jQuery.manageAjax.create('rwgAJAXManager', {
	queue: true, 			//(true|false|'clear') the queue-type specifies the queue-behaviour.
	maxRequests: 5, 		//(number (1)) limits the number of simultaneous request in the queue. queue-option must be true or 'clear'.
	cacheResponse: false 	//(true|false): caches the response data of successful response
});

var cohortBGColors = new Array(
		/*
		"#F5A9E1",  // light pink
		"#00FFFF",  // light blue
		"#FE9A2E",  // light orange
		"#BDBDBD",  // light grey
		"#2EFE2E",  // light green
		"#FF00FF",   // pink
		"#F3F781"  // light yellow
	*/
		
		/* Pastel */
		"#FFFFD9", //light yellow
		"#80B1D3", //light blue
		"#B3DE69", //moss green
		"#D9D9D9", //grey
		"#BC80BD", //lavender
		"#91d4c5"  //teal

		
		/*Light yellow to green, sequential 
		"#FFFFE5",
		"#F7FCB9",
		"#D9F0A3",
		"#ADDD8E",
		"#78C679",
		"#41AB5D",
		"#238443",
		"#006837"
		*/
		
);


////////////////////////////////////////////////////////////////////
// Not in the July 2012 Release
////////////////////////////////////////////////////////////////////
/*function updateAnalysisCount(checkedState)	{	
	var currentCount = jQuery("#analysisCount").val();
	if (checkedState)	{
		currentCount++;
	} else	{
		currentCount--;
	}
	jQuery("#analysisCount").val(currentCount);
	var newLabel = currentCount + " Analyses Selected";
	if (currentCount == 0)	{
		newLabel = "No Analysis Selected";
	} else if (currentCount == 1)	{
		newLabel = "1 Analysis Selected";
	}
	jQuery("#analysisCountLabel").html(newLabel);
	return false;
}
*/
////////////////////////////////////////////////////////////////////
function showDetailDialog(folderId)	{

	jQuery('#welcome-viewer').empty();

	jQuery('#metadata-viewer').empty().addClass('ajaxloading');
	jQuery('#metadata-viewer').load(folderDetailsURL + '?id=' + folderId, {}, function() {
		jQuery('#metadata-viewer').removeClass('ajaxloading');
	});
	return false;
}

//when a new object is created, show its details, highlight it and check for its parent to add expand/collapse image
function updateForNewFolder(folderId)	{
	jQuery('#metadata-viewer').load(folderDetailsURL + '?id=' + folderId, {}, function() {
		var parentId=jQuery('#parentId').val();
		
		//update parent folder
		var imgExpand = "#imgExpand_"  + parentId;
		var src = jQuery(imgExpand).attr('src').replace('folderplus.png', 'ajax-loader-flat.gif').replace('folderminus.png', 'ajax-loader-flat.gif').replace('folderleaf.png', 'ajax-loader-flat.gif');
		jQuery(imgExpand).attr('src',src);
		
		jQuery.ajax({
			url:folderContentsURL,
			data: {id: parentId, auto: false},
			success: function(response) {
				jQuery('#' + parentId + '_detail').html(response).addClass('gtb1').addClass('analysesopen').attr('data', true);
				
				//check if the object has children
				if(jQuery('#' + parentId + '_detail .search-results-table .folderheader').size() > 0){
					jQuery(imgExpand).attr('src', jQuery(imgExpand).attr('src').replace('ajax-loader-flat.gif', 'folderminus.png'));
				}else{
					jQuery(imgExpand).attr('src', jQuery(imgExpand).attr('src').replace('ajax-loader-flat.gif', 'folderleaf.png'));
				}
				jQuery('.result-folder-name').removeClass('selected');
				jQuery('#result-folder-name-' + folderId).addClass('selected');
			},
			error: function(xhr) {
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
			}
		});
		
		var imgExpand = "#imgExpand_"  + parentId;
		var src = jQuery(imgExpand).attr('src').replace('folderplus.png', 'folderminus.png').replace('ajax-loader-flat.gif', 'folderminus.png').replace('folderleaf.png', 'folderminus.png');
		jQuery(imgExpand).attr('src',src);
		var action="toggleDetailDiv('"+parentId+"', folderContentsURL + '?id="+parentId+"&auto=false');";
		jQuery('#toggleDetail_'+parentId).attr('onclick', action);
		openFolderAndShowChild(parentId, folderId);
		jQuery('.result-folder-name').removeClass('selected');
		jQuery('#result-folder-name-' + folderId).addClass('selected');
	});
	
	return false;
}

function openFolderAndShowChild(parent, child) {
	toggleDetailDiv(parent, folderContentsURL + '?id=' + parent + '&auto=false', true, child);
	showDetailDialog(child);
}


function showOverlay(overlayDiv, dataURL)	
{
	jQuery(overlayDiv).empty()
	jQuery(overlayDiv).load(dataURL)	
	jQuery('#sidebar-accordion').accordion("option", "active", 1);	
	return false;
}

// Open and close the analysis for a given trial
function toggleDetailDiv(trialNumber, dataURL, forceOpen, highlightFolder, manual)	{	
	if(manual==undefined){ manual=false;}
	var imgExpand = "#imgExpand_"  + trialNumber;
	var trialDetail = "#" + trialNumber + "_detail";
	
	// If data attribute is undefined then this is the first time opening the div, load the analysis... 
	if (typeof jQuery(trialDetail).attr('data') == 'undefined')	{
		//add node
		if(manual){
			jQuery.ajax({
				url:addNodeRwgURL+"?node=FOL:"+trialNumber
			});
		}
		var src = jQuery(imgExpand).attr('src').replace('folderplus.png', 'ajax-loader-flat.gif');	
		jQuery(imgExpand).attr('src',src);
		jQuery.ajax({
			url:dataURL,			
			success: function(response) {
				jQuery(imgExpand).attr('src', jQuery(imgExpand).attr('src').replace('ajax-loader-flat.gif', 'folderminus.png'));
				jQuery(trialDetail).addClass("gtb1");
				jQuery(trialDetail).html(response);
				jQuery(trialDetail).addClass("analysesopen");
				jQuery(trialDetail).attr('data', true);// Add an attribute that we will use as a flag so we don't need to load the data multiple times
				
				//If we have a folder to highlight, transfer the highlight to it
				if (highlightFolder) {
			    	jQuery('.result-folder-name').removeClass('selected');
					jQuery('#result-folder-name-' + highlightFolder).addClass('selected');
				}
			},
			error: function(xhr) {
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
			}
		});
	} else	{
		var src = jQuery(imgExpand).attr('src').replace('folderminus.png', 'folderplus.png');
		if (jQuery(trialDetail).attr('data') == "true" && !forceOpen)	{
			//remove node
			if(manual){
				jQuery.ajax({
					url:removeNodeRwgURL+"?node=FOL:"+trialNumber
				});
			}
			
			jQuery(trialDetail).attr('data',false);
			jQuery(trialDetail).removeClass("analysesopen");
		} else	{
			if(manual){
				jQuery.ajax({
					url:addNodeRwgURL+"?node=FOL:"+trialNumber
				});
			}
			src = jQuery(imgExpand).attr('src').replace('folderplus.png', 'folderminus.png');
			jQuery(trialDetail).attr('data',true);
			jQuery(trialDetail).addClass("analysesopen");
			
			if (highlightFolder) {
				jQuery('.result-folder-name').removeClass('selected');
				jQuery('#result-folder-name-' + highlightFolder).addClass('selected');
			}
		}	
		jQuery(imgExpand).attr('src',src);
		if (!forceOpen) {
			jQuery(trialDetail).toggle();
		}
		else {
			jQuery(trialDetail).show();
		}
	}
	return false;
}

// Method to add the toggle button to show/hide the search filters
function addToggleButton()	{
	jQuery("#toggle-btn").button({
		text: false
		}).click(function() {
			toggleFilters();
			jQuery("#main").css('left') == "0px" ? switchImage('toggle-icon-left', 'toggle-icon-right') : switchImage('toggle-icon-right', 'toggle-icon-left');
		}
	).addClass('toggle-icon-left');
	return false;
}

// Add and remove the right/left image for the toggle button
function switchImage(imgToRemove, imgToAdd)	{
	jQuery("#toggle-btn").removeClass(imgToRemove);
	jQuery("#toggle-btn").addClass(imgToAdd);
}

// Method to show/hide the search/filters 
function toggleFilters()	{
	if (jQuery("#main").css('left') == "0px"){		
		jQuery("#search-categories").attr('style', 'visibility:visible; display:inline');
		jQuery("#search-ac").attr('style', 'visibility:visible; display:inline');
		jQuery("#search-div").attr('style', 'visibility:visible; display:inline');
		jQuery("#active-search-div").attr('style', 'visibility:visible; display:inline');
		jQuery("#title-search-div").attr('style', 'visibility:visible; display:inline');
		jQuery("#title-filter").attr('style', 'visibility:visible; display:inline');
		jQuery("#side-scroll").attr('style', 'visibility:visible; display:inline');
		jQuery("#main").css('left', 300);
		jQuery("#toggle-btn").css('left', 278);
		jQuery("#toggle-btn").css('height;', 20);
		jQuery("#main").css('padding-left', 0);	
		jQuery("#menu_bar").css('margin-left', -1);
		jQuery("#toggle-btn").css('height', 20);	
	} else	{
		jQuery("#search-categories").attr('style', 'visibility:hidden; display:none');
		jQuery("#search-ac").attr('style', 'visibility:hidden; display:none');
		jQuery("#search-div").attr('style', 'visibility:hidden; display:none');
		jQuery("#active-search-div").attr('style', 'visibility:hidden; display:none');
		jQuery("#title-search-div").attr('style', 'visibility:hidden; display:none');
		jQuery("#title-filter").attr('style', 'visibility:hidden; display:none');
		jQuery("#side-scroll").attr('style', 'visibility:hidden; display:none');
		jQuery("#main").css('left', 0);	
		jQuery("#toggle-btn").css('left', 0);	
		jQuery("#toggle-btn").css('height', '100%');	
		jQuery("#main").css('padding-left', 20);	
		jQuery("#menu_bar").css('margin-left', -21);	
	}	   
}

function showIEWarningMsg(){
	
	if (jQuery.browser.msie && jQuery.browser.version.substr(0,1)<9) {

		var msg = "<div id='IEwarningBox'>Your browser is not supported. Please use the latest version of Chrome. <br /><br />";
			msg = msg + "<a href='#' id='IEwarningOverlayLink'>More info</a> | <a href='#' onclick=\"javascript:jQuery('#IEwarningBox').slideUp('fast');\">Close</a> </div>";
	
		var overlayMsg = "<div style='padding:20px'><p>TranSMART Faceted Search uses certain web technologies that are not fully supported by older browsers. ";
			overlayMsg = overlayMsg + "Google Chrome is the preferred browser within J&J to use with tranSMART. </p><br /><p>To request Chrome, send an SRM request " ;
			overlayMsg = overlayMsg + "for software package #C01026EE. If you have questions, please email us at <a href='mailto:tranSMART@its.jnj.com'>tranSMART@jnj.com</a>.</p>";
			overlayMsg = overlayMsg + "<br /><p><a href='#' onclick=\"jQuery('#IEwarningOverlayLink').colorbox.close()\">Close</a></p></div>";
				
		jQuery('#results-div').before(msg);
		
		jQuery("#IEwarningOverlayLink").colorbox({html:overlayMsg, width:"50%", height:"300px", opacity:"0.75"});
		
		
	}
	
}

//Add the search term to the array that the user has added to filter tree.
function addFilterTreeSearchTerm(searchTerm)	{
	var category = searchTerm.display == undefined ? "TEXT" : searchTerm.display;
	var text = searchTerm.keyword == undefined ? searchTerm : searchTerm.keyword;
	var id = searchTerm.id == undefined ? -1 : searchTerm.id;
	var key = category + ":" + text + ":" + id;
	if (currentSearchTerms.indexOf(key) < 0)	{
		currentSearchTerms.push(key);
		if (currentCategories.indexOf(category) < 0)	{
			currentCategories.push(category);
		}
	}

}



//export the current analysis data to a csv file
function exportLinePlotData(analysisId, exportType)
{
	
	jQuery('#lineplotExportOpts_'+analysisId).hide(); //hide the menu box
	
	var url='';
	
	switch(exportType)
	{
	case 'data':
	//Export the data for the heatmap
	    
	    var probesList = jQuery('body').data("activeLineplot:" + analysisId); //get the stored probe
	    
		url=getHeatmapDataForExportURL+'?id='+analysisId +'&probesList=' +probesList	
		
		downloadURL(url);
		
	break;

	case 'image':
		
		
		//data should be the same for both lineplot and boxplot
		var data = jQuery('body').data("LineplotData:" + analysisId);

		//redraw the plot with the legend so that it appears in the exported image
		drawLinePlot('lineplotAnalysis_'+analysisId, data, analysisId, true);

		var svgID=  "#lineplotAnalysis_"+analysisId;
		
		exportCanvas(svgID);		
		
		drawLinePlot('lineplotAnalysis_'+analysisId, data, analysisId, false);
		
		break;
	default:
		console.log('Error - invalid Export option');
	}
	
}


//export the current analysis data to a csv file
function exportBoxPlotData(analysisId, exportType)
{
	var url='';
	
	switch(exportType)
	{
	case 'data':
	//Export the data for the heatmap
	    
	    var probesList = jQuery('body').data("activeBoxplot:" + analysisId); //get the stored probe
	    
	    var page = jQuery('body').data("currentPage:" + analysisId); //current page is stored
	    
	    jQuery('#boxplotExportOpts_'+analysisId).hide(); //hide the menu box
	    		
		url=getHeatmapDataForExportURL+'?id='+analysisId +'&probesList=' +probesList	
		
		downloadURL(url);
		
	break;

	case 'image':
		
		jQuery('#boxplotExportOpts_'+analysisId).hide(); //hide the menu box
		
		var data = jQuery('body').data("BoxplotData:" + analysisId);
		
		drawBoxPlot('boxplotAnalysis_'+analysisId, data, analysisId, true);
		
		var svgID=  "#boxplotAnalysis_"+analysisId;
		
		exportCanvas(svgID);		

		drawBoxPlot('boxplotAnalysis_'+analysisId, data, analysisId, false);
		
		break;
	default:
		console.log('Error - invalid Export option');
	}
	
}


function exportCanvas(svgID){
	
	var svgData = jQuery(svgID).html();
	
	//Fix for Firefox: remove "<a xlink...>" and "</a>"
	//These tags are only generated in FF, and canvg does not like them
	if(svgData.indexOf("<a xlink")>-1){
		svgData = svgData.replace(/(<a xlink(.*?)>|<\/a>)/g, "");
	}
	
	canvg('canvas', svgData, { ignoreMouse: true, ignoreAnimation: true }) ;
	
	var imageData =  document.getElementById('canvas').toDataURL();
	imageData = imageData.substr(imageData.indexOf(',') + 1).toString(); //remove header info
	
    var dataInput = document.createElement("input") ; 
    dataInput.setAttribute("name", 'imgData') ;
    dataInput.setAttribute("value", imageData);
	
    var myForm = document.createElement("form"); //create form to post to server
    myForm.method = 'post';
    myForm.action = exportAsImage;
    myForm.appendChild(dataInput);
     
    document.getElementById('hiddenItems').appendChild(myForm) ;
    myForm.submit() ;
    
}





//export the current analysis data to a csv file
function exportHeatmapData(analysisId, exportType)
{
	
	
	var url='';
	
    jQuery('#heatmapExportOpts_'+analysisId).hide(); //hide the menu box

    jQuery("#analysis_holder_" + analysisId).mask();
	
	switch(exportType)
	{
	case 'currentPage':
	//Export all probes on the current page
		    
		
		//TODO: Those code is duplicated and should be moved to a seperate function
	    // make sure we are getting number of probes per page for current element
	    var probesPerPageElement = document.getElementById("probesPerPage_" + analysisId);
		var numberOfProbesPerPage = probesPerPageElement.options[probesPerPageElement.selectedIndex].value;
	    
	    var probesList = "";
	    var maxProbeLength = 0;
	    var analysisIndex = getAnalysisIndex(analysisId);
	    
	    var page = jQuery('body').data("currentPage:" + analysisId); //current page is stored
	    
	    
	    // index of probes list is the rankings starting at 1
	    // check that index is less than the maximum for the probe list, and less than max per the current page
		for (var i =0; i < analysisProbeIds[analysisIndex].probeIds.length; i++)  {
			if (i > 0)  {
				probesList = probesList + "|";
			}
			probesList = probesList + analysisProbeIds[analysisIndex].probeIds[i];
			
		}
		
		
		url=getHeatmapDataForExportURL+'?id='+analysisId +'&probesList=' +probesList	
		
	break;
	/* Removed option	
	case 'allPages':
		url=getHeatmapDataForExportURL+'?id='+analysisId +'&probesList=' +'allPages';
		break;
	*/
	case 'allProbes':
	//Export all probes (ignore search on gene/pathway)
		
		url=getHeatmapDataForExportURL+'?id='+analysisId +'&probesList=' +'allProbes';
		break;
	
	case 'image':
		
		var svgID=  "#analysisDiv_"+analysisId;
		var divID = "analysisDiv_" + analysisId;
		
		//redraw the heatmap with the legend
		drawHeatmap(divID, jQuery('body').data(analysisId), analysisId, true);
		
		exportCanvas(svgID);	
		
		//redraw the heatmap without the legend
		drawHeatmap(divID, jQuery('body').data(analysisId), analysisId, false);
		
		break;
		
	default:
		console.log('Error - invalid Export option');
	}
	

	downloadURL(url);
	

    jQuery("#analysis_holder_" + analysisId).unmask();

}

//this function is used to download a file without opening a new browser window
var downloadURL = function(url)
{
    var iframe;
    iframe = document.getElementById("hiddenDownloader");
    if (iframe === null)
    {
        iframe = document.createElement('iframe');  
        iframe.id = "hiddenDownloader";
        iframe.style.visibility = 'hidden';
        document.body.appendChild(iframe);
    }
    iframe.src = url;   

    
}

function analysisMenuEvent(id){
	
	var analysisID = id.substring(id.indexOf('_')+1,id.length);
	var btnID = id.substring(0,id.indexOf('_'));
	
	switch(btnID){
	
	case 'btnLineplotExport':
		jQuery('#lineplotExportOpts_'+analysisID).toggle();
		jQuery('#lineplotControls_'+analysisID).hide();
		
		break;
	
	case 'btnLineplotControls':
		jQuery('#lineplotControls_'+analysisID).toggle();
		jQuery('#lineplotExportOpts_'+analysisID).hide();
		break;
	
	case 'btnBoxplotExport':
		jQuery('#boxplotExportOpts_'+analysisID).toggle();
		jQuery('#boxplotControls_'+analysisID).hide();
		break;
	
	case 'btnBoxplotControls':
		jQuery('#boxplotControls_'+analysisID).toggle();
		jQuery('#boxplotExportOpts_'+analysisID).hide();
		break;
	
	case 'btnHeatmapExport':
		jQuery('#heatmapExportOpts_'+analysisID).toggle();
		jQuery('#heatmapControls_'+analysisID).hide();
		
		break;
	
	case 'btnHeatmapControls':
		jQuery('#heatmapControls_'+analysisID).toggle();
		jQuery('#heatmapExportOpts_'+analysisID).hide();
		break;
	
	case 'btnResultsExport':
		jQuery('#resultsExportOpts_'+analysisID).toggle();
		break;		
		
	default:
		
		console.log("Invalid option: " +id);	
	}
	
}

//Remove the search term that the user has de-selected from filter tree.
function removeFilterTreeSearchTerm(termID)	{
	var idx = currentSearchTerms.indexOf(termID);
	if (idx > -1)	{
		currentSearchTerms.splice(idx, 1);

		// check if there are any remaining terms for this category; remove category from list if none
		var fields = termID.split(":");
		var category = fields[0];
		clearCategoryIfNoTerms(category);
	}
	
}

function updateHeatmap(analysisID){
	
	var divID = "analysisDiv_" + analysisID;
	drawHeatmap(divID, jQuery('body').data(analysisID), analysisID);
	
}


//set the heatmap controls
function setHeatmapControls(analysisID){
	
	//sets the slider used to resize the heatmap
	var sliderID="#heatmapSlider_" +analysisID;
	jQuery(sliderID).width('75');
	jQuery(sliderID).slider({
		min:8,
		max:25,
		value:15,
		step: 1,
	 	stop: function(event, ui) {  
	 		updateHeatmap(analysisID);	 		
 	}
	});
	
	
	//sets the slider used to resize the heatmap
	var colorSliderID="#heatmapColorSlider_" +analysisID;
	jQuery(colorSliderID).width('75');
	jQuery(colorSliderID).slider({
		range: true,
		min:-100,
		max:100,
		values: [ -100, 100 ],
	 	slide: function(event, ui) {  

	 		//prevent the min from being greater than 0, and the max less than 0
/*	 		if(ui.values[1] < 0.01 || ui.values[0] > -0.01){
                return false;// do not allow change
            }
*/	 		
	 	},
	 	stop: function(event, ui) {  
	 		updateHeatmap(analysisID);	 		
 	}
	});
	
	var heatmapControlsDiv = "#heatmapControls_" +analysisID;
	jQuery('.heatmapControls_holder').mouseenter(function(){
	//    clearTimeout(jQuery(this).data('timeoutId'));
	    jQuery('body').data('heatmapControlsID', '');
	     
	 //   jQuery(this).find(".tooltip").fadeIn("slow");
	}).mouseleave(function(){
	 //	    var timeoutId = setTimeout(function(){
	 //   	jQuery(heatmapControlsDiv).fadeOut("fast");
	//    }, 800);
	    //set the timeoutId, allowing us to clear this trigger if the mouse comes back over
	//    jQuery(this).data('timeoutId', timeoutId); 
	    jQuery('body').data('heatmapControlsID', analysisID);
	});
	
	 

	
	
}

function setVisTabs(analysisID){
	var tabID = "#visTabs_" + analysisID;
	jQuery(tabID).tabs();	
	jQuery(tabID).bind( "tabsshow", function(event, ui) {
	    if (ui.panel.id == "boxplot_" + analysisID) {
	    	showBoxOrLinePlotVisualization(ui.panel, analysisID, true);
	    } else if (ui.panel.id == "lineplot_" + analysisID)	{
	    	showBoxOrLinePlotVisualization(ui.panel, analysisID, false);
	    }
	});
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Box or Line Plot Visualization Methods
// Show, Load Data and Draw
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function showBoxOrLinePlotVisualization(panel, analysisID, isBoxplot)	{
	var analysisIndex = getAnalysisIndex(analysisID);
	var probeIds = analysisProbeIds[analysisIndex].probeIds;
	var selectList = analysisProbeIds[analysisIndex].selectList;

	var typeString = '';
	if (isBoxplot) {
		typeString = 'Box';
	}  
	else  {		
		typeString = 'Line';
	}
	
	// retrieve the active probe for the current analysis 
	var activeProbe = getActiveProbe(analysisID);
	
	// if the currently displayed plot is not the active probe then redraw
	var redraw = false;
	
	var currentProbe = jQuery('body').data("active" + typeString + "plot:" + analysisID);
	
	if (currentProbe != activeProbe)  {
		redraw = true;
	}

	// if we're not showing a plot for the active probe, then reload
	if(redraw) { 
		jQuery("#analysis_holder_" + analysisID).mask("Loading...");
	
		if (isBoxplot)  {			
			loadBoxPlotData(analysisID, activeProbe);

		}
		else  {
			loadLinePlotData(analysisID, activeProbe);			
		}
	}
	
}


//Method to add the probes for the Line plot
function setLineplotProbes(analysisID, probeID)	{
	setProbesDropdown(analysisID, probeID, "#probeSelectionLineplot_" + analysisID);	
}


function getActiveProbe(analysisId){
	
	// retreive the active probe for the current analysis, first retrieve from global data
	var probeId = jQuery('body').data("activeAnalysisProbe:" + analysisId);
	
	// if not defined yet, set to the first one for the current page showing for the analysis
	if (probeId == undefined)  {
		var analysisIndex = getAnalysisIndex(analysisId);
		probeId = analysisProbeIds[analysisIndex].probeIds[0];		
	}
	 
	return probeId;
	
}

function setActiveProbe(analysisId, probeId){
	//store the currently active probe for the analysis; i.e the last one drawn for the box or line plot
	jQuery('body').data("activeAnalysisProbe:" + analysisId, probeId); 
	
}




function getGeneforDisplay(analysisID, probeID){

	var analysisIndex = getAnalysisIndex(analysisID);
	var probeIds = analysisProbeIds[analysisIndex].probeIds ;

	var maxProbeIndex = analysisProbeIds[analysisIndex].maxProbeIndex;
	 var probeDisplay = "";
    for (var i=0; i<maxProbeIndex; i++)  {
    	if (probeIds[i] == probeID) {
    		probeDisplay = analysisProbeIds[analysisIndex].selectList[i];
    		return probeDisplay;
    	}
    }
	 return false;
}


//Load the line plot data
function loadLinePlotData(analysisID, probeID)	{
	
	if (probeID === undefined)	{
		// We are called from the user switching probes, throw up the mask and get the probeID
		jQuery("#analysis_holder_" + analysisID).mask(); //hide the loading screen
		probeID = jQuery("#probeSelectionLineplot_" + analysisID).find('option:selected').attr('id');
		
	}
	
	// retrieve the corresponding display value for the probe Id 
    var analysisIndex = getAnalysisIndex(analysisID);
    var probeIds = analysisProbeIds[analysisIndex].probeIds ;
    var maxProbeIndex = analysisProbeIds[analysisIndex].maxProbeIndex; 

	
	rwgAJAXManager.add({
		url:getLinePlotDataURL,									
		data: {id: analysisID, probeID: probeID},
		timeout:60000,
		success: function(response) {
			
			//store the response
			jQuery('body').data("LineplotData:" + analysisID, response); //store the response
			
			setActiveProbe(analysisID, probeID);
			jQuery('#analysis_holder_' +analysisID).unmask(); //hide the loading msg, unblock the div 
			drawLinePlot('lineplotAnalysis_'+analysisID, response, analysisID);
			jQuery('#lineplotAnalysis_'+analysisID).show();
			jQuery('#lineplot_'+analysisID).show();

	//		jQuery('#lineplotLegend_'+analysisID).prepend("<p class='legend_probe'>Line plot for "+probeDisplay +"</p>"); //add the probe ID to the legend
			
			jQuery('#lineplotLegend_'+analysisID).show();

			jQuery('body').data("LineplotData:" + analysisID, response); //store the response
			
			jQuery('body').data("activeLineplot:" + analysisID, probeID); //store the analysis ID and probe ID of this lineplot;
																		 //used to determine if the lineplot has already been drawn
			setLineplotProbes(analysisID, probeID);
			jQuery("#analysis_holder_" + analysisID).unmask(); 
			
			
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}

//Draw the line plot
function drawLinePlot(divId, linePlotJSON, analysisID, forExport)	{
	
	
	var cohortArray = new Array();   // array of cohort ids
	var cohortDesc = new Array();    // array of cohort descriptions
	var cohortDisplayStyles = new Array();    // array of cohort display styles
	
	var gene_id = parseInt(linePlotJSON['gene_id']);   // gene_id will be null if this is a protein since first char is alpha for proteins

	// loop through and get the cohort ids and description into arrays in the order they should be displayed
	for (var key in linePlotJSON)  {
		// the "order" of the json objects starts with 1, so subtract 1 so it doesn't leave gap at start of array
		var arrayIndex = linePlotJSON[key]['order'] - 1;
		cohortArray[arrayIndex] = key;
		cohortDesc[arrayIndex] = linePlotJSON[key]['desc'];
		cohortDisplayStyles[arrayIndex] = linePlotJSON[key]['order'] % cohortBGColors.length;		
		
	}
	
	var statMapping = cohortArray.map(function(i)	{
		var data = linePlotJSON[i]['data'];
		
		// retrieve mean and standard error- round to 4 decimal places
		var mean = data['mean'];
		var stdError = data['stdError'];
		var min = mean - stdError;
		var max = mean + stdError;
		var desc = linePlotJSON[i]['desc'].replace(/_/g, ', ');
		var sampleCount = linePlotJSON[i]['sampleCount'];

		var meanFormatted = parseFloat(mean);
		meanFormatted = meanFormatted.toFixed(4);
		
		var stdErrorFormatted = parseFloat(stdError);
		stdErrorFormatted = stdErrorFormatted.toFixed(4);
		
		var cohortDisplayStyle = linePlotJSON[i]['order'] % cohortBGColors.length; 
		
		return {
			id:i,
			desc:desc,
			sampleCount:sampleCount,
			mean:mean,
			stdError:stdError,			
			meanFormatted:meanFormatted,
			stdErrorFormatted:stdErrorFormatted,			
			min:min,
			max:max,
			cohortDisplayStyle:cohortDisplayStyle
		};		
	});

	
	
	
	//if the user is setting the range manually:
	if(jQuery('#lineplotRangeRadio_Manual_'+analysisID).is(':checked')){
		
		var yMin = parseFloat(jQuery('#lineplotRangeMin_'+analysisID).val());
		var yMax = parseFloat(jQuery('#lineplotRangeMax_'+analysisID).val());

		
	}else{
		
		var yMin = statMapping[0].min;
		var yMax = statMapping[0].max;
		for (var idx=1; idx < statMapping.length; idx++)	{	
			yMin = statMapping[idx].min < yMin ? statMapping[idx].min : yMin;
			yMax = statMapping[idx].max > yMax ? statMapping[idx].max : yMax;
		}
		
		// Put in a rough switch so things can scale on the y axis somewhat dynamically
		if (yMax-yMin < 2)	{
			// round down to next 0.1
			yMin = Math.floor((yMin-0.1) * 10) / 10;

			// round up to next 0.1
			// and add another 0.01 to ensure that the highest tenths line gets included
			yMax = Math.ceil((yMax+0.1) * 10) / 10  + 0.01;
		} else	{
			yMin = Math.floor(yMin);
			yMax = Math.ceil(yMax);
		}		
			
		
		//set the manual value textboxes with the current yMin and yMax
		jQuery('#lineplotRangeMin_'+analysisID).val(roundNumber(yMin,2));
		jQuery('#lineplotRangeMax_'+analysisID).val(roundNumber(yMax,2));
		
	}

		
	var w = cohortArray.length * 150;//generate the width dynamically using the cohort count
	h = 300,
	margin = 55,
	widthErrorBarBottomAndTopLines = 6,
	radiusDot = 3,
	h_legend=0;//used to draw the legend for export

	
	if(forExport){
		h_legend=35+ 30 * (cohortArray.length); //h_legend is the extra space required for the legend 
	}
	
	
	var x = pv.Scale.ordinal(statMapping, function(e){return e.id}).splitBanded(0, w, 1/2);
	var y = pv.Scale.linear(yMin, yMax).range(0, h)			
	
	var numCohorts = cohortArray.length;
	
	// need to add a blank entry at the beginning of the arrays for use by drawCohortLegend
	cohortArray = [''].concat(cohortArray);
	cohortDesc = [''].concat(cohortDesc);
	cohortDisplayStyles = [''].concat(cohortDisplayStyles);
	
	if(forExport){
		cohortDesc=highlightCohortDescriptions(cohortDesc, true);
	}
	
	
	var vis = new pv.Panel().canvas(document.getElementById(divId)) 	
	.width(w)
	.height(h+h_legend)
	.margin(margin);
	
	/* Add the y-axis rules */
	vis.add(pv.Rule)
	.data(y.ticks())
	.strokeStyle("#ccc")
	.bottom(y)
	.anchor("left").add(pv.Label)
	.font("14px sans-serif")
	.text(y.tickFormat);

	vis.add(pv.Label)
	.data(statMapping)
	.left(function(d){return x(d.id)})
	.bottom(-20)
	.textAlign("center")
	.font("14px sans-serif")
	.events("all")
	.title(function(d){return d.desc})	
	.text(function(d){return d.id + "(n=" + d.sampleCount + ")"});
	
	/* Add the log2 label */
	vis.add(pv.Label)
	.left(-40)
	.bottom(h/2)
	.textAlign("center")
	.textAngle(-Math.PI / 2)
	.font("14px sans-serif")
    .text("log2 intensity");
	
	if (gene_id)  {
		/* Add the title with link to gene info*/
		vis.add(pv.Label)
		.font("bold 16px sans-serif")
		.textStyle("#065B96")
	    .left(w/2)
	    .bottom(300)
	    .textAlign("center")
	  
		    /*Add link in title to gene info */
	    .cursor("pointer")
	    .event("mouseover", function(){ self.status = "Gene Information"})
	    .event("mouseout", function(){ self.status = ""})
	    .event("click", function(d) {self.location = "javascript:showGeneInfo('"+gene_id +"');"})
		.events("all")
	    .title("View gene information")
	    .text(getGeneforDisplay(analysisID, getActiveProbe(analysisID)));
	}
	else  {
		/* Add the title without link to gene info*/
		vis.add(pv.Label)
		.font("bold 16px sans-serif")
		.textStyle("#065B96")
	    .left(w/2)
	    .bottom(300)
	    .textAlign("center")
	    .text(getGeneforDisplay(analysisID, getActiveProbe(analysisID)));		
	}
	// create line
    var line = 	vis.add(pv.Line)
    .data(statMapping)
	.strokeStyle("#000000")
    .bottom(function(d){return y(d.mean)})
	.left(function(d){return x(d.id)});
    
    // add dots at each point in line
    line.add(pv.Dot)
      .radius(radiusDot)
	  .strokeStyle("#000000")
      .fillStyle("#000000")
      .title(function(d){return d.meanFormatted + " +/- " + d.stdErrorFormatted});

    // Add error bars
    // vertical line
    line.add(pv.Rule)
      .left(function(d){return x(d.id)})
      .bottom(function(d) {return y(d.mean - Math.abs(d.stdError))})
      .top(function(d) {return y(yMax) - y(d.mean + Math.abs(d.stdError)) + h_legend}); 
    

    // bottom horizontal line
    line.add(pv.Rule)
      .left(function(d){return x(d.id) - widthErrorBarBottomAndTopLines/2} )
      .bottom(function(d) { return y(d.mean - d.stdError)})
      .width(widthErrorBarBottomAndTopLines);
    // top horizontal line
    line.add(pv.Rule)
      .left(function(d){return x(d.id) - widthErrorBarBottomAndTopLines/2} )
      .bottom(function(d) { return y(d.mean + d.stdError)})
      .width(widthErrorBarBottomAndTopLines);
    
	/*add legend if export */
	if(forExport){
			
	    /*		Legend	     */
	    var legend = vis.add(pv.Bar)
	    	.data(statMapping)
	    	.height(25)
	    	.top(function(){return (this.index * 30)-20 })
	    	.antialias(false)
	    	.left(-30)
	    	.strokeStyle("#000")
	    	.lineWidth(1)
	    	.width(30)
	    	.fillStyle(function (d) {return cohortBGColors[d.cohortDisplayStyle]});

	    legend.anchor("center").add(pv.Label)
    	.textStyle("#000")
    	.font("12px  sans-serif")
    	.text(function(d){return d.id} );
	    
	    vis.add(pv.Label)
	    	.data(statMapping)
	    .top(function(){return this.index * 30})
	    .antialias(false)
	    .left(5)
    	.textStyle("#000")
    	.font("12px  sans-serif")
    //	.text(function(d){return d.desc});   	
    	.text(function(){return cohortDesc[this.index+1].replace(/_/g, ', ')});
	}

	

	vis.root.render();
	

	/////////////////////////////////////////////////////////////////////////////////////////////////////////		
	jQuery("#lineplotLegend_" + analysisID).html(drawCohortLegend(numCohorts, cohortArray, cohortDesc, cohortDisplayStyles));
	
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Box Plot Visualization Methods
// Show, Load Data and Draw
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
function openBoxPlotFromHeatmap(analysisID, probe){
	  var divID = "analysisDiv_" + analysisID;

	  jQuery('#visTabs_' +analysisID).tabs('select', 'boxplot_'+analysisID); // switch to boxplot tab
		
	  var currentBoxplot = jQuery('body').data("activeBoxplot:" + analysisID);
	  
	  if(currentBoxplot != probe){
		  jQuery("#analysis_holder_" + analysisID).mask("Loading...");	
		  loadBoxPlotData(analysisID, probe);		  
	  }
}

//Method set the probes in a select list for current page
function setProbesDropdown(analysisID, selectedProbeID, divId)	{
	var analysisIndex = getAnalysisIndex(analysisID);
	var probeIds = analysisProbeIds[analysisIndex].probeIds;
	var selectList = analysisProbeIds[analysisIndex].selectList;

	jQuery(divId).empty();
	for (var i=0; i<probeIds.length; i++) {
		
		if (probeIds[i] == selectedProbeID)	{
			jQuery(divId).append(jQuery("<option id></option>").attr("selected", "selected").attr("id", probeIds[i]).attr("value", selectList[i]).text(selectList[i]));
		} else	{
			jQuery(divId).append(jQuery("<option></option>").attr("id", probeIds[i]).attr("value", selectList[i]).text(selectList[i]));
		}
	}	
}


//Method to add the probes for the box plot
function setBoxplotProbes(analysisID, selectedProbeID)	{
	setProbesDropdown(analysisID, selectedProbeID, "#probeSelection_" + analysisID);
}

// Load the box plot data
function loadBoxPlotData(analysisID, probeID)	{	
	jQuery('#boxplotEmpty_' +analysisID).hide(); //hide the message that tells the user to select a probe first
	
	if (probeID === undefined)	{
		// We are called from the user switching probes, throw up the mask and get the probeID
		jQuery("#analysis_holder_" + analysisID).mask(); //hide the loading screen
		probeID = jQuery("#probeSelection_" + analysisID).find('option:selected').attr('id');
		
	}
	
	// retrieve the corresponding display value for the probe Id 
	
	/*
    var analysisIndex = getAnalysisIndex(analysisID);
    var probeDisplay = ""
    var probeIds = analysisProbeIds[analysisIndex].probeIds ;
    var maxProbeIndex = analysisProbeIds[analysisIndex].maxProbeIndex; 
    for (var i=0; i<maxProbeIndex; i++)  {
    	if (probeIds[i] == probeID) {
    		probeDisplay = analysisProbeIds[analysisIndex].selectList[i];
    		break;
    	}
    }
        
        */
	rwgAJAXManager.add({
		url:getBoxPlotDataURL,
		data: {id: analysisID, probeID: probeID},
		timeout:60000,
		success: function(response) {
			setActiveProbe(analysisID, probeID);
			drawBoxPlot('boxplotAnalysis_'+analysisID, response, analysisID);
			jQuery('#boxplotLegend_'+analysisID).show();
			jQuery('#boxplotAnalysis_'+analysisID).show();	
			
			jQuery('body').data("BoxplotData:" + analysisID, response); //store the response
			
			jQuery('body').data("activeBoxplot:" + analysisID, probeID); //store the analysis ID and probe ID of this boxplot;
																		 //used to determine if the boxplot has already been drawn
		
			setBoxplotProbes(analysisID, probeID);
			jQuery("#analysis_holder_" + analysisID).unmask(); 
			
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}

function changeRangeRadioBtn(graph, analysisID){
	
	var radioID = '#' +graph +'RangeRadio_Manual_' +analysisID;
	var rangeMinID = '#' +graph +'RangeMin_' +analysisID;
	var rangeMaxID = '#' +graph +'RangeMax_' +analysisID;
	
	if(jQuery(radioID).is(':checked')){
		
		jQuery(rangeMinID).removeAttr('disabled');
		jQuery(rangeMaxID).removeAttr('disabled');
		
	}else{
		
		jQuery(rangeMinID).attr('disabled',true);
		jQuery(rangeMaxID).attr('disabled',true);		
		

		
		if(graph == 'boxplot'){
			
			//redrew the boxplot to set it back to default range
			updateBoxPlot(analysisID)
			
		}else if(graph == 'lineplot') {
			
			updateLineplot(analysisID);
			
		}
	}
}


function updateLineplot(analysisID){
	
	//data should be the same for both lineplot and boxplot
	var data = jQuery('body').data("LineplotData:" + analysisID);
	
	if(data == ''){
		console.log("Error: Could not find data");
	}else
		{
			drawLinePlot('lineplotAnalysis_'+analysisID, data, analysisID);
		}
}

//collapse all of the open analyses
function collapseAllAnalyses(){
		
	while (openAnalyses.length>0){
		//each time showVisualization is called, the current analysis is removed from openAnalyses
		showVisualization(openAnalyses[0], false);
	}
}

function collapseAllStudies() {
	collapseAllAnalyses();
	//For each open study, toggleDetailDiv
	var openstudyelements = jQuery(".analysesopen");
	for (var i = 0; i < openstudyelements.length; i++) {
		var studyelement = openstudyelements[i];
		var studyId = jQuery(studyelement).attr('name');
		toggleDetailDiv(studyId, ''); //No URL needed when collapsing
	}
}

function expandAllStudies() {
	//For each closed study, toggleDetailDiv
	var closedstudyelements = jQuery(".detailexpand").not(".analysesopen");
	for (var i = 0; i < closedstudyelements.length; i++) {
		var studyelement = jQuery(closedstudyelements[i]);
		var studyId = studyelement.attr('name');
		var key = new Date().getTime(); //Key to prevent AJAX caching
		
		toggleDetailDiv(studyId, getStudyAnalysesUrl + "?id=" + studyId + "&trialNumber=" + studyId + "&unqKey=" + key);
	}
}

function updateBoxPlot(analysisID){
	
	var data = jQuery('body').data("BoxplotData:" + analysisID);
	
	if(data == ''){
		console.log("Error: Could not find data");
	}else
		{
			drawBoxPlot('boxplotAnalysis_'+analysisID, data, analysisID);
		}
}

// Helper function to provide the rank for the percentile calculation in the box plot
function getRank(P, N)	{
	return Math.round(P/100 * N + 0.5);			// Use P/100 * N + 0.5 as denoted here: http://en.wikipedia.org/wiki/Percentile
}

// Draw the box plot
function drawBoxPlot(divId, boxPlotJSON, analysisID, forExport)	{
	// boxPlotJSON should be a map of cohortID:[desc:cohort description, order:display order for the cohort, data:sorted log2 intensities]
	
	
	var cohortArray = new Array();   // array of cohort ids
	var cohortDesc = new Array();    // array of cohort descriptions
	var cohortDisplayStyles = new Array();    // array of cohort display styles (i.e. number from 0..4)

	var gene_id = parseInt(boxPlotJSON['gene_id']);   // gene_id will be null if this is a protein since first char is alpha for proteins
	
	// loop through and get the cohort ids and description into arrays in the order they should be displayed
	for (var key in boxPlotJSON)  {
		// the "order" of the json objects starts with 1, so subtract 1 so it doesn't leave gap at start of array
		var arrayIndex = boxPlotJSON[key]['order'] - 1;
		cohortArray[arrayIndex] = key;
		cohortDesc[arrayIndex] = boxPlotJSON[key]['desc'];
		cohortDisplayStyles[arrayIndex] = boxPlotJSON[key]['order'] % cohortBGColors.length;		
	}
	
	// Map the all four quartiles to the key (e.g. C1)
	var statMapping = cohortArray.map(function(i)	{
		var data = boxPlotJSON[i]['data'];
		var cohortDisplayStyle = boxPlotJSON[i]['order'] % cohortBGColors.length;		
		var desc = boxPlotJSON[i]['desc'].replace(/_/g, ', ');
		var sampleCount = boxPlotJSON[i]['sampleCount'];
		
		return {
			id:i,
			cohortDisplayStyle:cohortDisplayStyle,
			desc:desc,
			sampleCount:sampleCount,
			min:data[getRank(5, data.length)-1],
			max:data[getRank(95, data.length)-1],			
			median:data[getRank(50, data.length)-1],
			lq:data[getRank(25, data.length)-1],
			uq:data[getRank(75, data.length)-1]
		};		
	});
	
	
	//if the user is setting the range manually:
	if(jQuery('#boxplotRangeRadio_Manual_'+analysisID).is(':checked')){
		
		var yMin = parseFloat(jQuery('#boxplotRangeMin_'+analysisID).val());
		var yMax = parseFloat(jQuery('#boxplotRangeMax_'+analysisID).val());

		
	}else{
		//auto set range otherwise
		var yMin = statMapping[0].min;
		var yMax = statMapping[0].max;
		for (var idx=1; idx < statMapping.length; idx++)	{	
			yMin = statMapping[idx].min < yMin ? statMapping[idx].min : yMin;
			yMax = statMapping[idx].max > yMax ? statMapping[idx].max : yMax;
		}
		
		// Put in a rough switch so things can scale on the y axis somewhat dynamically
		if (yMax-yMin < 2)	{
			// round down to next 0.1
			yMin = Math.floor((yMin-0.2) * 10) / 10 ;
			
			// round up to next 0.1
			// and add another 0.01 to ensure that the highest tenths line gets included
			yMax = Math.ceil((yMax+0.2) * 10) / 10 + 0.01;
		} else	{
			yMin = Math.floor(yMin);
			yMax = Math.ceil(yMax);
		}
		
		//set the manual value textboxes with the current yMin and yMax
		jQuery('#boxplotRangeMin_'+analysisID).val(roundNumber(yMin,2));
		jQuery('#boxplotRangeMax_'+analysisID).val(roundNumber(yMax,2));
		
	}
	
	var title = getGeneforDisplay(analysisID, getActiveProbe(analysisID));
	
	var w = cohortArray.length * 140;//generate the width dynamically using the cohort count	
	var  h = 300,  
		x = pv.Scale.ordinal(statMapping, function(e){return e.id}).splitBanded(0, w, 1/2),
		y = pv.Scale.linear(yMin, yMax).range(0, h-15),
		s = x.range().band / 2;
	
	
	var numCohorts = cohortArray.length;
	
	// need to add a blank entry at the beginning of the arrays for use by drawCohortLegend
	cohortArray = [''].concat(cohortArray);
	cohortDesc = [''].concat(cohortDesc);
	cohortDisplayStyles = [''].concat(cohortDisplayStyles);
	
	if(forExport){
		h=320 + 30 * (cohortArray.length);
		cohortDesc=highlightCohortDescriptions(cohortDesc, true);
	}

		var vis = new pv.Panel().canvas(document.getElementById(divId)) 	
		.width(w)
		.height(h)
		.margin(55);

		if (gene_id)  {
			/* Add the title with link to gene info*/
			vis.add(pv.Label)
			.font("bold 16px sans-serif")
		    .left(w/2)
		    .bottom(300)
		    .textStyle("#065B96")
		    .textAlign("center")
	    	/*Add link in title to gene info */
		    .cursor("pointer")
		    .event("mouseover", function(){ self.status = "Gene Information"})
		    .event("mouseout", function(){ self.status = ""})
		    .event("click", function(d) {self.location = "javascript:showGeneInfo('"+gene_id +"');"})
			.events("all")   
			.title("View gene information")
			.text(title);
		}
		else {
			/* Add the title without link to gene info*/
			vis.add(pv.Label)
			.font("bold 16px sans-serif")
		    .left(w/2)
		    .bottom(300)
		    .textStyle("#065B96")
		    .textAlign("center")
			.text(title);
			
		}
	
		/* Add the y-axis rules */
		vis.add(pv.Rule)
		.data(y.ticks())
		.strokeStyle("#ccc")
		.bottom(y)
		.anchor("left").add(pv.Label)
		.font("14px sans-serif")
		.text(y.tickFormat);	
		
		/* Add the log2 label */
		vis.add(pv.Label)
		.left(-40)
		.bottom(300/2) //300 is the height of the boxplot
		.textAlign("center")
		.textAngle(-Math.PI / 2)
		.font("14px sans-serif")
	    .text("log2 intensity");

		/* Add a panel for each data point */
		var points = vis.add(pv.Panel)
		.def("showValues", false)
		.data(statMapping)
		.left(function(d){return x(d.id)})
		.width(s * 2)
		.events("all");

		/* Add the experiment id label */
		vis.add(pv.Label)
		.data(statMapping)
		.left(function(d){return x(d.id) + s})
		.bottom(-20)
		.textAlign("center")
		.font("14px sans-serif")
		.events("all")
		.title(function(d){return d.desc})
		.text(function(d){return d.id + "(n=" + d.sampleCount + ")"});
		
		/*add legend if export */
		if(forExport){
				
		    /*		Legend	     */
		    var legend = vis.add(pv.Bar)
		    	.data(statMapping)
		    	.height(25)
		    	.top(function(){return (this.index * 30)-20 })
		    	.antialias(false)
		    	.left(-30)
		    	.strokeStyle("#000")
		    	.lineWidth(1)
		    	.width(30)
		    	.fillStyle(function (d) {return cohortBGColors[d.cohortDisplayStyle]});

		    legend.anchor("center").add(pv.Label)
	    	.textStyle("#000")
	    	.font("12px  sans-serif")
	    	.text(function(d){return d.id} );
		    
		    vis.add(pv.Label)
		    	.data(statMapping)
		    .top(function(){return this.index * 30})
		    .antialias(false)
		    .left(5)
	    	.textStyle("#000")
	    	.font("12px  sans-serif")
	    //	.text(function(d){return d.desc});   	
	    	.text(function(){return cohortDesc[this.index+1].replace(/_/g, ', ')});
		}
		
		

		/* Add the range line */
		points.add(pv.Rule)
		.left(s)
		.bottom(function(d){return y(d.min)})
		.height(function(d){return y(d.max) - y(d.min)});

		/* Add the min and max indicators */
		var minLine = points.add(pv.Rule)
			.data(function(d){return [d.min]})
			.bottom(y)
			.left(s / 2)
			.width(s)
			.anchor("bottom").add(pv.Label)
			.visible(function(){return this.parent.showValues()}) 
			.text(function(d){return d.toFixed(2)});
		
		var maxLine = points.add(pv.Rule)
			.data(function(d){return [d.max]})
			.bottom(y)
			.left(s / 2)
			.width(s)
			.anchor("top").add(pv.Label)
			.visible(function(){return this.parent.showValues()}) 
			.text(function(d){return d.toFixed(2)});

		/* Add the upper/lower quartile ranges */
		var quartileBar = points.add(pv.Bar)
			.fillStyle(function (d) {return cohortBGColors[d.cohortDisplayStyle]})
			.bottom(function(d){return y(d.lq)})
			.height(function(d){return y(d.uq) - y(d.lq)})
			.strokeStyle("black")
			.lineWidth(1)
			.event("mouseover", function() {return this.parent.showValues(true)}) 
			.event("mouseout", function() {return this.parent.showValues(false)})
			.antialias(false);
		
		var lqLabel = quartileBar.add(pv.Label)
			.visible(function(){return this.parent.showValues()})
			.text(function(d){return d.lq.toFixed(2)})
			.textAlign("right")
			.textBaseline("top");
		
		var uqLabel = quartileBar.anchor("top").add(pv.Label)		
			.visible(function(){return this.parent.showValues()})
			.left(-15)
			.text(function(d){return d.uq.toFixed(2)})
			.textMargin(-10);
		
		/* Add the median line */
		points.add(pv.Rule)
		.bottom(function(d){ return y(d.median)})
		.anchor("right").add(pv.Label)
		.visible(function(){return this.parent.showValues()})
		.text(function(d){return d.median.toFixed(2)});

		vis.render();

		/////////////////////////////////////////////////////////////////////////////////////////////////////////		
		jQuery("#boxplotLegend_" + analysisID).html(drawCohortLegend(numCohorts, cohortArray, cohortDesc, cohortDisplayStyles));
		
}

// Show the heatmap visualization 
function showVisualization(analysisID, changedPaging)	{	
	
	var analysisHeaderDiv = "#TrialDetail_" + analysisID + "_anchor"
	var divID = "#analysis_results_" + analysisID;
	var divID2 = "analysis_results_" + analysisID;
	var loadingDiv = "#analysis_holder_"+ analysisID;
	var imgExpand = "#imgExpand_"  + analysisID;
	var div = document.getElementById(divID);	
	var hmFlagDiv = divID+"_state";
	var hmFlag = jQuery(hmFlagDiv).val();
	
	// Check the value of the hidden field that is capturing the following "click" states
	// 0: No heatmap loaded, hidden
	// 1: Heatmap loaded, visible
	// 2: Heatmap loaded, hidden
	
	// if the paging has changed, need to reload page
	if (hmFlag != "1")	{				
		var src = jQuery(imgExpand).attr('src').replace('down_arrow_small2.png', 'up_arrow_small2.png');
		jQuery(imgExpand).attr('src',src);
		jQuery(analysisHeaderDiv).addClass("active-analysis");
		jQuery(loadingDiv).toggle();
		openAnalyses.push(analysisID); //store this as an open analysis
		
		if (hmFlag == "0")	{

			setVisTabs(analysisID);
			jQuery(loadingDiv).mask("Loading...");
			loadAnalysisResultsGrid(analysisID, {'max': 10, 'offset':0, 'cutoff': 0, 'search': "", 'sortField': "", "order": "asc"});
		}
		
		jQuery(hmFlagDiv).val("1");
	} else	{
		var src = jQuery(imgExpand).attr('src').replace('up_arrow_small2.png', 'down_arrow_small2.png');
		jQuery(imgExpand).attr('src',src);
		jQuery(loadingDiv).toggle('blind', {}, 'fast');
		jQuery(analysisHeaderDiv).removeClass("active-analysis");	
		jQuery(hmFlagDiv).val("2");
		
		//remove the analysis from the array, while leaving all others
		//openAnalyses = openAnalyses.splice( jQuery.inArray(analysisID, openAnalyses), 1 );
		removeByValue(openAnalyses,analysisID);
		
	} 	
	return false;
}

//This function will kick off the webservice that generates the QQ plot.
function loadQQPlot(analysisID)
{
	jQuery('#qqplot_results_' +analysisID).empty().addClass('ajaxloading');
	jQuery.ajax( {
	    "url": getQQPlotURL,
	    bDestroy: true,
	    bServerSide: true,
	    data: {analysisId: analysisID},
	    "success": function ( json ) {
	    	jQuery('#analysis_holder_' +analysisID).unmask();
	    	jQuery('#qqplot_results_' + analysisID).prepend("<img src='" + json.imageURL + "' />").removeClass('ajaxloading');
	    	jQuery('#qqplot_export_' + analysisID).attr('href', json.imageURL);
	    	},
	    "error": function ( json ) {
	    	jQuery('#qqplot_results_' + analysisID).prepend(json).removeClass('ajaxloading');
	    	jQuery('#analysis_holder_' +analysisID).unmask();
	    },
	    "dataType": "json"
	} );		
}

// This function will load the analysis data into a GRAILS template.
function loadAnalysisResultsGrid(analysisID, paramMap)
{
	paramMap.analysisId = analysisID
	jQuery('#analysis_results_table_' + analysisID + '_wrapper').empty().addClass('ajaxloading');
	jQuery.ajax( {
	    "url": getAnalysisDataURL,
	    bDestroy: true,
	    bServerSide: true,
	    data: paramMap,
	    "success": function (jqXHR) {
	    	jQuery('#analysis_holder_' +analysisID).unmask();
	    	jQuery('#analysis_results_table_' + analysisID + '_wrapper').html(jqXHR).removeClass('ajaxloading');
	    },
	    "error": function (jqXHR, error, e) {
	    	jQuery('#analysis_results_table_' + analysisID + '_wrapper').html(error).removeClass('ajaxloading');
	    	jQuery('#analysis_holder_' +analysisID).unmask();
	    },
	    "dataType": "html"
	} );		
}

// Make a call to the server to load the heatmap data
function loadHeatmapData(divID, analysisID, probesPage, probesPerPage)	{
	
	rwgAJAXManager.add({
		url:getHeatmapDataURL,
		data: {id: analysisID, probesPage: probesPage, probesPerPage:probesPerPage},
		timeout:60000,
		success: function(response) {
			jQuery('body').data(analysisID, response); //store the result set in case the heatmap is updated 
			jQuery('#analysis_holder_' +analysisID).unmask(); //hide the loading msg, unblock the div
			drawHeatmap(divID, response, analysisID);		
			jQuery('#heatmapLegend_'+analysisID).show();


	        var analysisIndex = getAnalysisIndex(analysisID);
	        var probesList = analysisProbeIds[analysisIndex].probeIds;
	        var maxProbeIndex = analysisProbeIds[analysisIndex].maxProbeIndex;
			
			if(maxProbeIndex == 1){ //only one probe returned
				loadBoxPlotData(analysisID, probesList[0]);	//preload boxplot
			}	        
	        
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}

//displays pop-up of gene with tabs to internal and external sources
function showGeneInfo(geneID)
{
	var w=window.open('/transmart/details/gene/?rwg=y&altId='+geneID , 'detailsWindow', 'width=900,height=800'); 
	w.focus(); 
}

// Take the heatmap data in the second parameter and show it in the Protovis panel
function drawHeatmap(divID, heatmapJSON, analysisID, forExport)	{
	
	
	// set up arrays to be used to populating drop down boxes for line/box plots
	// do this first since we need this for determining max probe string length
	var probesList = new Array() 
	var selectList = new Array()
	var hasFoldChange = false; //true if the data contains fold change values
    var hasTPvalue = false;
    var hasPvalue = false;
    var hasNullValues = false; //checks if there are null in the heatmap; null legend should only be displayed if so
	var maxProbeLength = 0;
	for (var i=0; i<heatmapJSON.length; i++)	{
		probesList.push(heatmapJSON[i].PROBE);
		selectList.push(heatmapJSON[i].GENE + " (" + heatmapJSON[i].PROBE + ")");		
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
		//check if any of the heatmapJSON values are undefined. The legend for null values will
		//only display if 'hasNullValues' is true
		//"key.indexOf(':') > 0" <- this is used to only check the chohorts (ex, 'C1:3432'). Other key values are ignored
		for (var key in (heatmapJSON[i])){
				if(heatmapJSON[i][key] == undefined && key.indexOf(':') > 0){
	    			hasNullValues=true;
			}
		}
		
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
	
	//set the header font size depending on the cell size
	var headerfont = "12px  sans-serif";
	if(cellSize < 12){
		headerfont = "8px  sans-serif";
	}else if (cellSize>19){
		headerfont = "16px  sans-serif";
	}
	
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
	
	// The fill variable will have a map of the columns array as the key and a color range as the value 

	fill = pv.dict(columns, function(f) {
		return pv.Scale.linear()	
		.domain(rangeMin,rangeMid,rangeMid,rangeMax) 
	    .range("#4400BE", "#D7D5FF","#ffe2f2", "#D70C00"); 
	});

	
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
	var numCohorts = cohorts.size()-1;   // there is no cohort at index 0

	
	// need to add a blank entry at the beginning of the arrays for use by drawCohortLegend
//	cohortArray = [''].concat(cohortArray);
//	cohortDesc = [''].concat(cohortDesc);
//	cohortDisplayStyles = [''].concat(cohortDisplayStyles);
		
	var height;
	if(forExport){
		height = 4*h +h_header + (heatmapJSON.length * h) + (cohorts.size()-1)*35;
		cohortDescriptions=highlightCohortDescriptions(cohortDescriptions, true);
	}else{
		height = 4*h +h_header + (heatmapJSON.length * h);
	}
	
	var vis = new pv.Panel().canvas(document.getElementById(divID)) 				    					// First panel is the canvas where everything is drawn
    	.width(w_probe + columns.length * w + w_gene + w_fold_change + w_pvalue + w_Tpvalue)				    		// Width of the entire panel									
    	.height(height)			    									// Height of the entire panel 
    	
    vis.add(pv.Panel)
        .data(columns)
    	.left(function()	{
    		return w_probe + this.index * w;
    	})
    	.width(w)
    	.add(pv.Panel)
    	.data(heatmapJSON)
    	.top(function()	{
    		return h + (this.index * h) + h_header;
    	})
    	.height(h)
    	.fillStyle(function(d, f)	{
    		if (d[f] == undefined)	{
    			return "#FFFF00";
    		} else	{
    			return fill[f](d[f]);

    		}
    	})


    	.strokeStyle("#333333")
    	.lineWidth(1)
    	.antialias(false)
    	.title(function(d, f)	{			// title is the tooltip
    		var cohort = f.split(':')[0];    		
    		if (d[f] == undefined){
    			return cohort + ":" + d["PROBE"] + ":" + d["GENE"] + "=" + d[f];
    		} else	{
    			return cohort + ":" + d["PROBE"] + ":" + d["GENE"] + "=" + d[f].toFixed(2);
    		}    		
    	});

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

	var leftPosition = w_probe;
	for(var i=1; i<=numCohorts; i++) {		
		var classIndex;
		
		cohortDisplayStyles[i] = i % cohortBGColors.length;

		var dataColor = cohortBGColors[i % cohortBGColors.length];
		var strokeStyleColor = "#000";
		var textStyleColor = "#000";
		
		/* cohort header */
		var barC = vis.add(pv.Bar)
	    	.data([dataColor])
	    	.height(h)
	    	.top(2)
	    	.antialias(false)
	    	.left(leftPosition)
	    	.strokeStyle(strokeStyleColor)
	    	.title(cohortDescriptions[i].replace(/_/g, ', '))
	    	.lineWidth(1)
	    	.width(cohortWidths[i])
	    	.fillStyle(function(d)	{
	    		          return d;
    	                 }
	    	           );

	    barC.anchor("center").add(pv.Label)
    	.textStyle(textStyleColor)
    	.font(headerfont)
    	.text(cohorts[i] );
	    
	    
	    //only draw the legend if the result is being exported as an image
	    if(forExport){
	    	
		    /*		Legend	     */
		    var legend = vis.add(pv.Bar)
		    	.data([dataColor])
		    	.height(25)
		    	.top(2*h + (heatmapJSON.length * h)+h_header + i*30)
		    	.antialias(false)
		    	.left(0)
		    	.strokeStyle(strokeStyleColor)
		    	.lineWidth(1)
		    	.width(30)
		    	.fillStyle(function(d)	{
		    		          return d;
	    	                 });

		    legend.anchor("center").add(pv.Label)
	    	.textStyle(textStyleColor)
	    	.font(headerfont)
	    	.text(cohorts[i] );
		    
		    vis.add(pv.Label)
		    .top(2*h + (heatmapJSON.length * h)+h_header + i*30 +20)
		    .antialias(false)
		    .left(35)
	    	.textStyle(textStyleColor)
	    	.font("12px  sans-serif")
	    	.text(cohortDescriptions[i].replace(/_/g, ', '));   	
	    	
	    }

	    // determine left position for next cohort
	    leftPosition = leftPosition + cohortWidths[i];
	}	
	
	//only do this if the data contains fold change values
	if(hasFoldChange){
	    // Show the fold change table header
	    vis.add(pv.Label)
	    	.width(w_fold_change)
	    	.top(h_header+11)
			.left(w_probe + columns.length * w + w_gene)
	    	.textAlign("left")
	    	.font("bold 11px sans-serif")
	    	.text("Fold change");
	    
	    
	    // Show the fold change
	    vis.add(pv.Label)
			.data(heatmapJSON)
			.top(function()	{
				return (h + (this.index * h + h / 2) + h_header);
			})
			.width(w_fold_change)
			    	.strokeStyle("#333333")
	    	.lineWidth(1)
	    	.antialias(false)
			.left(w_probe + columns.length * w + w_gene)
			.textAlign("left")
			.textBaseline("middle")
			.text(function(d)	{
				return d.FOLD_CHANGE;
			});
	}
    
	if(hasTPvalue)
	{
	    // Show the tea p value header    
	    vis.add(pv.Label)
			.width(w_Tpvalue)
			.top(h_header+11)
			.left(w_probe + columns.length * w + w_gene + w_fold_change)
			.textAlign("left")
		    .font("bold 11px sans-serif")
			.text("TEA p-value"); 
	    
	    // Show the tea p value
	    vis.add(pv.Label)
			.data(heatmapJSON)
			.top(function()	{
				return (h + (this.index * h + h / 2) + h_header);
			})
			.width(w_pvalue)
			.left(w_probe + columns.length * w + w_gene + w_fold_change)
			.textAlign("left")
			.textBaseline("middle")
			.text(function(d)	{
				if (d.TEA_P_VALUE == 0)  {
					return "< 0.00001"
				}
				else  {
					return d.TEA_P_VALUE;
				}
			});
	}
	
	if(hasPvalue){
		
	    //show the p-value header
	    vis.add(pv.Label)
			.width(w_pvalue)
			.top(h_header+11)
			.left(w_probe + (columns.length * w) + w_gene + w_fold_change +w_Tpvalue)
			.textAlign("left")
		    .font("bold 11px sans-serif")
			.text("p-value"); 
	    
	    // Show the p value
	    vis.add(pv.Label)
			.data(heatmapJSON)
			.top(function()	{
				return (h + (this.index * h + h / 2) + h_header);
			})
			.width(w_pvalue)
			.left(w_probe + (columns.length * w) + w_gene + w_fold_change +w_Tpvalue)
			.textAlign("left")
			.textBaseline("middle")
			
			.text(function(d)	{
				if (d.PREFERRED_PVALUE == 0)  {
					return "< 0.00001"
				}
				else  {
					return d.PREFERRED_PVALUE;
				}
			});
	}
	

    // Show the Gene labels
    vis.add(pv.Label)
    	.data(heatmapJSON)
    	.top(function()	{
    		return (h + (this.index * h + h / 2) + h_header);
		})
		.width(w_gene)
		.left(w_probe + columns.length * w)
		
		//add hyperlink to gene label that opens pop-up
	    .cursor( function(d) {if (parseInt(d.GENE_ID)) {return "pointer"}})
	    .event("mouseover", function(d){ if (parseInt(d.GENE_ID))  {self.status = "Gene Information"}})
	    .event("mouseout", function(d){ self.status = ""})
	    .event("click", function(d) {if (parseInt(d.GENE_ID))  {self.location = "javascript:showGeneInfo('"+d.GENE_ID +"');"}})

	    .textAlign("left")
		.textBaseline("middle")
		.events("all")
		.title(function(d)	{
			return d.GENELIST;
		})
		.text(function(d)	{
			return d.GENE;
		}); 
    
    // Show the Probe labels
    vis.add(pv.Label)
    	.data(heatmapJSON)
    	.top(function()	{
    		return (h + (this.index * h + h / 2) + h_header);
    	})
    	.width(w_probe)

    	//add link to view boxplot of values
        .cursor("pointer")
	
	    .event("mouseout", function(){ self.status = ""})
	    .event("click", function(d) {self.location = "javascript:openBoxPlotFromHeatmap(" +analysisID +", '" +d.PROBE +"');"})

	    .events("all")
    	.textAlign("left")
    	.font("10px Verdana, Tahoma, Arial")
    	.textBaseline("middle")
    	.title("View in boxplot")
    	.text(function(d)	{
    		return d.PROBE;
    	});
    

	vis.add(pv.Bar)
		.data(["#4400BE"])
		.height(15)
		.left(w_probe)
		.width(55)
		.top(2*h + (heatmapJSON.length * h) + h_header)
		.fillStyle(function(d)	{
			return d;
		})
		.anchor("left")
		.add(pv.Label)
		.textStyle("white")
		.text("min: "+Math.round((rangeMin)*100)/100);//Math.round get nearest integer		
	
	vis.add(pv.Bar)
		.data(["#D70C00"])
		.height(15)
		.width(55)
		.left(w_probe + 85)
		.top(2*h + (heatmapJSON.length * h) + h_header)
		.fillStyle(function(d)	{
			return d;
		})
		.anchor("left")
		.add(pv.Label)
		.textStyle("white")
		.text("max: " + Math.round((rangeMax)*100)/100); //Math.round get nearest integer
	
	
	//draw legend for null values only if they exist in the current heatmap
	if(hasNullValues){
		vis.add(pv.Bar)
		.data(["#FFFF00"])
		.height(15)
		.width(55)
		.left(w_probe + 170)
		.top(2*h + (heatmapJSON.length * h) + h_header)
		.fillStyle(function(d)	{
			return d;
		})
		.anchor("left")
		.add(pv.Label)
		.textStyle("black")
		.text("null");	
		
	}
	


	vis.root.render();					// Need root panel for the canvas call to work
	jQuery("#heatmapLegend_" + analysisID).html(drawCohortLegend(numCohorts, cohorts, cohortDescriptions, cohortDisplayStyles));
}

// Helper function to draw the legend for the cohorts in the visualization panel
function drawCohortLegend(numCohorts, cohorts, cohortDescriptions, cohortDisplayStyles)	{
	
	cohortDescriptions = highlightCohortDescriptions(cohortDescriptions);
	
	var pCohortAll = "<table class='cohort_table'>"
	var classIndex = null;
	var pCohort = "";
	for(var i=1; i<=numCohorts; i++) {
		pCohort = "<tr><td style='width:40px'><p class='cohort' style='background-color:" + cohortBGColors[cohortDisplayStyles[i]]  + "'>" +cohorts[i] +"</p></td><td><p class='cohortDesc'>"+cohortDescriptions[i].replace(/_/g, ', ')+'</p></td>';
		pCohortAll = pCohortAll +  pCohort;
	}
	return pCohortAll + "</table>	";
}

//Show diff between each cohort
//returnOnlyDiff: if true, return only the different terms
function highlightCohortDescriptions(cohortDesc, returnOnlyDiff){
	
	var arySplit = new Array();
	var aryDif = new Array();
	var aryDescNew = new Array();
	
	//1. Split each cohort description into an array of terms
	for (var i=1; i<cohortDesc.length; i++){
		arySplit[i]= cohortDesc[i].split('_');
	}
	
	//2. Loop through the array and compare each term to the term in the same position of the next description
	//	 mark which ones are same and different in aryDif
	for (var i=1; i<arySplit.length-1; i++){
		
			for(var x=0; x < arySplit[i].length; x++){
				
					if(trim(arySplit[i][x]).toUpperCase() == trim(arySplit[i+1][x]).toUpperCase()){
						
							if(aryDif[x] != false){
								aryDif[x] = true;
							}
							else{
								aryDif[x] = false;
								}
						}
					else{
						aryDif[x] = false;
					}
				}
		}
	
	//3. Rebuild array, inserting syntax to denote which terms are different
	for (var i=1; i<arySplit.length; i++){
		
		aryDescNew[i]=''; //initilize the first value
		
		for(var x=0; x < arySplit[i].length; x++){

			
				if(aryDif[x] == true){ //the terms are the same
					if(!returnOnlyDiff){
						aryDescNew[i] = aryDescNew[i] + arySplit[i][x];	
					}
				}
				else{	//the terms are different
					if(!returnOnlyDiff){ 
						aryDescNew[i] = aryDescNew[i] +"<span class='highlight'>" +arySplit[i][x] +"</span>";
					}
					else if(returnOnlyDiff){
						aryDescNew[i] = aryDescNew[i] +arySplit[i][x] + ', ';
					}
				}
				
				//check if this is the last term; if not, add an underscore between terms
				if(x+1 < arySplit[i].length && !returnOnlyDiff){
					aryDescNew[i] = aryDescNew[i]+'_';
				}
			}
		
			if(returnOnlyDiff){//remove trailing space and comma
				aryDescNew[i] = aryDescNew[i].slice(0,-2);		
			}
	}
	
	return aryDescNew;

}
//remove whitespace
function trim(stringToTrim) {
	return stringToTrim.replace(/^\s+|\s+$/g,"");
}

//convert a string to Title Case
function toTitleCase(str)
{
    return str.replace(/\w\S*/g, function(txt){return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();});
}

function goToByScroll(id){
	jQuery('#main').animate({scrollTop: jQuery("#"+id).offset().top},'slow');
}

/* Find the width of a text element */
String.prototype.visualLength = function(fontFamily) 
{ 
    var ruler = document.getElementById("ruler"); 
    ruler.style.font = fontFamily; 
    ruler.innerHTML = this; 
    return ruler.offsetWidth; 
}



//Round number to given decimal place
function roundNumber(num, dec) {
	var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
	return result;
}

function openSaveSearchDialog()  {

	var keywords = getSearchKeywordList();

	if (keywords.length>0)  {
		jQuery('#save-modal-content').modal();
	}
	else  {
		alert("No search criteria to save!")
	}
		

	return false;

}

// save a faceted search to the database
function saveSearch(keywords, name, desc)  {

	var name = jQuery("#searchName").val();
	var desc = jQuery("#searchDescription").val();
	var keywords = getSearchKeywordList();

	//  had no luck trying to use JSON libraries for creating/parsing JSON string so just save keywords as pipe delimited string 
	if (keywords.length>0)  {
		var criteriaString = keywords.join("|") 
		rwgAJAXManager.add({
			url:saveSearchURL,
			data: {criteria: criteriaString, name: name, description:desc},
			timeout:60000,
			success: function(response) {
	            alert(response['message']);	
	            
	            // close the dialog if success flag was true
	            if (response['success'])  {
	            	jQuery.modal.close();	            	
	            }
	            
			},
			error: function(xhr) {
				console.log('Error!  Status = ' + xhr.status + xhr.statusText);
			}
		});
	}
	else  {
		alert("No search criteria to save!")
	}
	
}

//delete a faceted search from the database
function deleteSearch()  {
	
	rwgAJAXManager.add({
		url:deleteSearchURL,
		data: {name: "testname23"},
		timeout:60000,
		success: function(response) {
            alert(response['message']);	        
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
	
}


function loadSearch()  {

	
	rwgAJAXManager.add({
		url:loadSearchURL,
		data: {id: 37},   //37
		timeout:60000,
		success: function(response) {
			clearSearch();
			
			if (response['success'])  {
				var searchTerms = response['searchTerms'] 
				var count = response['count'] 
				
				for (i=0; i<count; i++)  {
					// e.g. "Therapeutic Areas|THERAPEUTIC AREAS:Immunology:1004"
					
					var searchParam={id:searchTerms[i].id,
							         display:searchTerms[i].displayDataCategory,
							         keyword:searchTerms[i].keyword,
							         category:searchTerms[i].dataCategory};
					addSearchTerm(searchParam);

				}
					
			}
			else  {
				alert('failed');  // show message from server  
			}
 		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});

}

// this function removes or adds to the filter search term array based on whether or not a node in the tree is selected
function syncNode(node)  {
	var param = new Object();  
	var isCategory = node.data.isCategory;
	
	// don't sync category nodes (they can't be selected)
	if (isCategory) {
		return true
	}

	var categoryName = node.data.categoryName;
	
	var outerNode = node;
	var inSearchTerms = false;
	
	// find all nodes that are copies of this node
	// if this or any of the copies of this node should be in search terms, then mark as being in search terms
	//  (this will prevent a later node in tree from removing from terms if an earlier node indicates it should be 
	//   in terms; this logic could be up for debate  - e.g. maybe it should never be in terms if any of the copies
	//         say it shouldn't be - but making consistent for now, can reverse logic easily if needed )
	node.tree.visit(
			          function checkCopies (node) {
			        	  if (outerNode.data.key == node.data.key)  {
			        		  // found a key that matches (i.e. is the original one or a copy)
        	        		  // a node will be in search terms if it is selected and it's parent is not
			        		  // or if it's selected and it's parent is a category
			        		  if (node.isSelected() && (!node.parent.isSelected() || node.parent.data.isCategory))  {
			        			  inSearchTerms = true;  
			        		  }
			        	  }
			          },
			          false
			       )
		
	param.display = categoryName;     // category        	
	param.keyword = node.data.termName;  // term name
	param.id = node.data.id;
	if (inSearchTerms)  {
	    addFilterTreeSearchTerm(param);
	}
	else {
		// create string that remove fn recognizes as key
		var termID = node.data.key;
		removeFilterTreeSearchTerm(termID);
	}
	
}

// subtract node 2 from node 1;  return an array containing list nodes that are in node 1 but not node 2
function subtractNodes(nodes1, nodes2)  {

	var resultNodes = new Array();

	for (var i = 0; i < nodes1.length; i++) {  // loop thru nodes1
		  var n1 = nodes1[i];                
		  
		  var found = false;
		  
          for (var j = 0; j < nodes2.length; j++) {  // loop thru nodes2
  		      var n2 = nodes2[j];
  		      
  		      if (n2.data.uniqueTreeId == n1.data.uniqueTreeId)  {  // use uniqueTreeId to determine matches
  		    	  found = true;
  		    	  break;   // no need to continue with loop since we found match
  		      } 
          }
          
          // if we didn't find the node in nodes2, add to result array
          if (!found)  {
        	  resultNodes.push(n1);
          }        		  
	}
	return resultNodes;
}

//jQuery.ui.dynatree.nodedatadefaults["icon"] = false;


// find the analysis in the array with the given id
function getAnalysisIndex(id)  {
	for (var i = 0; i < analysisProbeIds.length; i++)  {
		if (analysisProbeIds[i].analysisId == id)  {
			return i;
		}
	}
	
    return -1;  // analysis not found		
}

//remove an element from an array by value, keeping all others in place
function removeByValue(arr, val) {
	for(var i=0; i<arr.length; i++) {
		if(arr[i] == val) {
			arr.splice(i, 1);
			break;
		}
	}
}


function getHeatmapPaginator(divID, analysisId, analysisIndex, maxProbeIndex) {
	probesPerPageElement = document.getElementById("probesPerPage_" + analysisId);
	numberOfProbesPerPage = probesPerPageElement.options[probesPerPageElement.selectedIndex].value;
	
	// get number of extra probes on last page (will be 0 if last page is full)
	var numberProbesLastPage = maxProbeIndex % numberOfProbesPerPage;
	
	// find number of full pages
	var numberOfFullPages = (maxProbeIndex - numberProbesLastPage) / numberOfProbesPerPage;
	
	// find number of pages - equal to number of full pages if none left over after full pages
	var numberOfPages = numberOfFullPages;        	        	
	if (numberProbesLastPage > 0)  {
		numberOfPages = numberOfPages + 1;
	}
	
	//if there is only 1 page, just hide the paging control since it's not needed
	if(numberOfPages==1){
		jQuery("#pagination_" + analysisId).hide();
	}
	else  {
		jQuery("#pagination_" + analysisId).show();
	}
	        	        	
	// the probeIds list and selectList are initially null; will be populated when we load the heat map data
	var analysisObject = {analysisId:analysisId, probeIds:null, selectList:null, maxProbeIndex:maxProbeIndex};
	
	// either replace current object, or add new one if not in array yet
	if (analysisIndex == -1)  {
        analysisProbeIds.push(analysisObject);
    } else
    {	
        analysisProbeIds[analysisIndex] = analysisObject;
    }

	jQuery("#pagination_" + analysisId).paging(numberOfPages, { 
	    perpage:1, 
        format:"[<(qq -) ncnnn (- pp)>]",
        onSelect: function (page) { 
        	

        	jQuery("#analysis_holder_" + analysisId).mask("Loading...");
            var analysisIndex = getAnalysisIndex(analysisId);

            // make sure we are getting number of probes per page for current element
            var probesPerPageElement = document.getElementById("probesPerPage_" + analysisId);
        	var numberOfProbesPerPage = probesPerPageElement.options[probesPerPageElement.selectedIndex].value;
            
        	loadHeatmapData(divID, analysisId, page, numberOfProbesPerPage);

        	jQuery('body').data("currentPage:" + analysisId, page);
                                    
        }, 
        onFormat: function(type) {      
      
                switch (type) {      
                case 'block':      
                    	if (!this.active)      
                    		return '<span class="disabled">' + this.value + '</span>';      
                    	else if (this.value != this.page)      
                        return '<em><a href="#' + this.value + '">' + this.value + '</a></em>';      
                    	return '<span class="current">' + this.value + '</span>';      
                case 'left':      
                case 'right':      
      
                        if (!this.active)      
                                return '';      
                        else       
                                return '<em><a href="#' + this.value + '">' + this.value + '</a></em>';      
      
                case 'next':      
      
                        if (this.active) {      
                                return '<a href="#' + this.value + '" class="next">Next &raquo;</a>';      
                        }      
                        return '<span class="disabled">Next &raquo;</span>';      
      
                case 'prev':      
      
                        if (this.active) {      
                                return '<a href="#' + this.value + '" class="prev">&laquo; Previous</a>';      
                        }      
                        return '<span class="disabled">&laquo; Previous</span>';      
      
                case 'first':      
      
                        if (this.active) {      
                                return '<a href="#' + this.value + '" class="first">|&lsaquo;</a>';      
                        }      
                        return '<span class="disabled">|&lsaquo;</span>';      
      
                case 'last':      
      
                        if (this.active) {      
                                return '<a href="#' + this.value + '" class="prev">&rsaquo;|</a>';      
                        }      
                        return '<span class="disabled">&rsaquo;|</span>';      
      
                case 'fill':      
                        if (this.active) {      
                                return "...";      
                        }      
                }      
        }
    }); 	
	
}

function loadHeatmapPaginator(divID, analysisId, page) {

	var analysisIndex = getAnalysisIndex(analysisId);
		
	rwgAJAXManager.add({
		url:getHeatmapNumberProbesURL,		
		data: {id: analysisId, page:page},
		success: function(response) {
			var maxProbeIndex = response['maxProbeIndex']
			
			getHeatmapPaginator(divID, analysisId, analysisIndex, maxProbeIndex, page);
	
		},
		error: function(xhr) {
			console.log('Error!  Status = ' + xhr.status + xhr.statusText);
		}
	});
}

function updateSelectedAnalyses() {
	var selectedboxes = jQuery(".analysischeckbox:checked");
	if (selectedboxes.length > 0) {
		jQuery('#selectedAnalyses').html("<b>(" + selectedboxes.length + "</b> analyses selected)");
	}
	else {
		jQuery('#selectedAnalyses').html("&nbsp;");
	}
}

function startPlotter() {
	var selectedboxes = jQuery(".analysischeckbox:checked");
	if (selectedboxes.length == 0) {
		alert("No analyses are selected! Please select analyses to plot.");
	}
	else {
		var analysisIds = "";
		analysisIds += jQuery(selectedboxes[0]).attr('name');
		for (var i = 1; i < selectedboxes.length; i++) {
			analysisIds += "," + jQuery(selectedboxes[i]).attr('name');
		}
		
		var snpSource = jQuery('#plotSnpSource').val();
		var geneSource = jQuery('#plotGeneSource').val();
		var pvalueCutoff = jQuery('#plotPvalueCutoff').val();
		
		window.location = webStartURL + "?analysisIds=" + analysisIds + "&snpSource=" + snpSource + "&geneSource=GRCh37&pvalueCutoff=" + pvalueCutoff;
		jQuery('#divPlotOptions').dialog("destroy");
	}
}

function openPlotOptions() {
	var selectedboxes = jQuery(".analysischeckbox:checked");
	if (selectedboxes.length == 0) {
		alert("No analyses are selected! Please select analyses to plot.");
	}
	else {
		jQuery('#divPlotOptions').dialog("destroy");
		jQuery('#divPlotOptions').dialog(
			{
				modal: false,
				height: 250,
				width: 400,
				title: "Manhattan Plot Options",
				show: 'fade',
				hide: 'fade',
				resizable: false,
				buttons: {"Plot" : startPlotter}
			});
	}
}

function updateAnalysisData(analysisId, full) {
	jQuery('#partialanalysiswarning').hide();
	jQuery('#analysisgenefilteredwarning').hide();
	
	//If passed a null analysisId, check to see if an analysis is displayed, then update it.
	if (analysisId == null) {
		var currentId = jQuery('#gridViewWrapperAnalysis').attr('name');
		if (currentId == null || currentId == 0) {
			return false;
		}
		analysisId = currentId;
	}
	
	jQuery('#gridViewWrapperAnalysis').empty().addClass('ajaxloading');
	$j.ajax({
		url:analysisDataURL,
		data: {id: analysisId, full: full},
		success: function(response) {
			jQuery('#gridViewWrapperAnalysis').removeClass('ajaxloading');
	 	 	var dtAnalysis  = new dataTableWrapper('gridViewWrapperAnalysis', 'gridViewTableAnalysis', 'Title', [[2, "asc"]], 25);
	   		dtAnalysis.loadData(response);
	   		if (!full && response.rowCount > 1000) {
		   		jQuery('#loadfullanalysis').text('Load all ' + response.rowCount + ' rows')
	   			jQuery('#partialanalysiswarning').show();
	   		}
	   		if (response.filteredByGenes) {
		   		jQuery('#analysisgenefilteredwarning').show();
		   	}
		},
		error: function(xhr) {
			alert(xhr.message);
		}
	});
}

//Globally prevent AJAX from being cached (mostly by IE)
jQuery.ajaxSetup({
	cache: false
});