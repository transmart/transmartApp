<%--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
  
 
--%>

<%@ page language="java" import="java.util.*"%>
<!DOCTYPE html>
<html>
<head>

<title>Dataset Explorer</title>

    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8">

    <link rel="shortcut icon" href="${resource(dir:'images', file:'i2b2_hive.ico')}">
    <link rel="icon" href="${resource(dir:'images', file:'i2b2_hive.ico')}">

<%-- We do not have a central template, so this only works in the database explorer for now --%>
<g:if test="${['true', true]*.equals(grailsApplication.config.com.recomdata.debug.jsCallbacks).any()}">
    <g:javascript src="long-stack-traces.js"/>
</g:if>

<!-- Include Ext and app-specific scripts: -->
<script type="text/javascript"
	src="${resource(dir:'js/sarissa', file:'sarissa.js')}"></script>
<script type="text/javascript"
	src="${resource(dir:'js/sarissa', file: 'sarissa_ieemu_xpath.js')}"></script>
<script type="text/javascript"
	src="${resource(dir:'js/javeline', file: 'javeline_xpath.js')}"></script>
<script type="text/javascript" src="${resource(dir:'js', file:'prototype.js')}"></script>
<script type="text/javascript"
	src="${resource(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
    <script type="text/javascript"
            src="${resource(dir:'js', file:'ext/ext-all-debug.js')}"></script>
	
<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery.min.js')}"></script>
<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-ui.min.js')}"></script>
<script type="text/javascript"
        src="${resource(dir: 'js/jQuery/multiselect', file: 'jquery.multiselect.min.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js/jQuery', file: 'jquery.tablesorter.min.js')}"></script>
<script type="text/javascript"
        src="${resource(dir: 'js/jQuery/validate', file: 'jquery.validate.min.js')}"></script>
<script type="text/javascript"
        src="${resource(dir: 'js/jQuery/validate', file: 'additional-methods.min.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js/jQuery', file: 'jquery.dataTables.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js/jQuery/custom', file: 'fnGetColumnData.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js/jQuery/flot', file: 'jquery.flot.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js/jsTree', file: 'jquery.jstree.js')}"></script>
<script type="text/javascript"
        src="${resource(dir: 'js/datasetExplorer', file: 'workflowValidationFunctions.js')}"></script>
  
<script type="text/javascript" src="${resource(dir:'js', file:'ajax_queue.js')}"></script> 

<script type="text/javascript"
	src="${resource(dir:'js/ext-ux', file:'miframe.js')}"></script>
<script type="text/javascript" src="${resource(dir:'js/datasetExplorer', file:'i2b2common.js')}"></script>
<script type="text/javascript"
	src="${resource(dir:'js/datasetExplorer', file: 'requests.js')}"></script>
<script type="text/javascript"
	src="${resource(dir:'js/datasetExplorer', file: 'ext-i2b2.js')}"></script>
<script type="text/javascript"
	src="${resource(dir:'js/datasetExplorer', file: 'workflowStatus.js')}"></script>
<script type="text/javascript" src="${resource(dir:'js', file:'myJobs.js')}"></script>

<script type="text/javascript"
	src="${resource(dir:'js/datasetExplorer/exportData', file: 'dataTab.js')}"></script>
<script type="text/javascript"
	src="${resource(dir:'js/datasetExplorer/exportData', file: 'exportJobsTab.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js/jsTree', file:'jquery.jstree.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js/datasetExplorer', file: 'acrossTrial.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js/datasetExplorer', file: 'jsTreeFunctions.js')}"></script>

<!-- <script type="text/javascript" src="http://www.google.com/jsapi"></script>
<script type="text/javascript">
	google.load("visualization", "1", {});
</script>
 <script type="text/javascript"
	src="${resource(dir:'js', file:'bioheatmap.js')}"></script>-->
	
	<!-- Include Ext stylesheets here: -->
	<link rel="stylesheet" type="text/css" href="${resource(dir:'js/ext/resources/css', file:'ext-all.css')}">
	<link rel="stylesheet" type="text/css" href="${resource(dir:'js/ext/resources/css', file:'xtheme-gray.css')}">
	<!-- Include JQuery stylesheets here: -->
	<link rel="stylesheet" type="text/css" href="${resource(dir:'css/jQueryUI/smoothness', file:'jquery-ui-1.8.17.custom.css')}">

    <link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'jquery.dataTables.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'jquery.dataTables_themeroller.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'demo_page.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'demo_table.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'demo_table_jui.css')}">


	<script type="text/javascript" src="${resource(dir:'js', file:'browserDetect.js')}"></script>
	
    <script type="text/javascript" src="${resource(dir:'js/utils', file:'json2.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js/utils', file:'dynamicLoad.js')}"></script>

 
	<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'datasetExplorer.css')}">
	<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'metacore.css')}">

    <script type="text/javascript" src="${resource(dir:'js/datasetExplorer', file: 'yui-combo-build-min.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js/datasetExplorer', file:'datasetExplorer.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js', file:'ColVis.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'advancedWorkflowFunctions.js')}"></script>

    <!-- Adding these validation functions to get the Forest Plot to work. These might be able to be blended into the javascript object that controls the advanced workflow validation. -->
    <script type="text/javascript" src="${resource(dir:'js/datasetExplorer', file:'workflowValidationFunctions.js')}"></script>
	
	<script type="text/javascript" src="${resource(dir:'js/datasetExplorer', file:'highDimensionData.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'utilitiesMenu.js')}"></script>
	
	<script type="text/javascript" src="${resource(dir:'js/raphael', file:'raphael-min.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js/metacore', file:'metacoreEnrichment.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js/metacore', file:'metacoreEnrichmentDisplay.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js/cortellisAnalytics', file:'cortellisAnalytics.js')}"></script>

    <style type = "text/css">

    .bottom {
        position: fixed;
        bottom: 0;
        width: 100%;
    }

    </style>

    <script type="text/javascript">

        var calcDataTableHeight = function() {
            return (jQuery(window).height() - 240) + "px";
        };

        var $window = $(window);

        $window.resize(function() {

           // var myVar = jQuery("#gridViewWrapper").find('.dataTables_scrollBody');
           // alert(myVar);
            jQuery('div.dataTables_scrollBody').css("height", calcDataTableHeight());
        });

    </script>

</head>

<body>

<script type="text/javascript">
	var $j = jQuery.noConflict();
	Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

	//set ajax to 600*1000 milliseconds
	Ext.Ajax.timeout = 1800000;

	// this overrides the above
	Ext.Updater.defaults.timeout = 1800000;

    var basicGridUrl = "${createLink(controller:'chart', action:'basicGrid')}"
    var analysisGridUrl = "${createLink(controller:'chart', action:'analysisGrid')}"

	var pageInfo = {
		basePath :"${request.getContextPath()}"
	}
	
    var helpURL = '${grailsApplication.config.com.recomdata.adminHelpURL}';
	 
	/******************************************************************************/
	//Global Variables
	GLOBAL = {
	  Version : '1.0',
	  Domain: '${i2b2Domain}',
	  ProjectID: '${i2b2ProjectID}',
	  Username: '${i2b2Username}',
	  Password: '${i2b2Password}',
	  AutoLogin: true,
	  Debug: false,
	  NumOfSubsets: 2,
	  NumOfQueryCriteriaGroups:20,
	  NumOfQueryCriteriaGroupsAtStart:3,
	  MaxSearchResults: 100,
	  ONTUrl: '',
      usePMHost: '${grailsApplication.config.com.recomdata.datasetExplorer.usePMHost}',
	  Config:'jj',
	  CurrentQueryName:'',
	  CurrentComparisonName:' ',
	  CurrentSubsetIDs: new Array(),
	  CurrentPathway: '',
	  CurrentPathwayName: '',
	  CurrentGenes: '',
	  CurrentChroms: '',
	  CurrentDataType: '',
	  GPURL: '${grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL}',
	  EnableGP:'${grailsApplication.config.com.recomdata.datasetExplorer.enableGenePattern}',
	  HeatmapType: 'Compare',
	  IsAdmin: ${admin},
	  Tokens: "${tokens}",
	  InitialSecurity: ${initialaccess},
	  RestoreComparison: ${restorecomparison},
	  RestoreQID1: "${qid1}",
	  RestoreQID2: "${qid2}",
	  resulttype: 'applet',
	  searchType: "${grailsApplication.config.com.recomdata.search.genepathway}",
	  DefaultCohortInfo: '',
	  CurrentTimepoints: new Array(),
	  CurrentSamples: new Array(),
	  CurrentPlatforms: new Array(),
	  CurrentGpls: new Array(),
	  CurrentTissues: new Array(),
	  CurrentRbmpanels: new Array(),
	  PathToExpand: "${pathToExpand}",
	  preloadStudy: "${params.DataSetName}",
	  Binning: false,
	  ManualBinning: false,
	  NumberOfBins: 4,
	  HelpURL: '${grailsApplication.config.com.recomdata.adminHelpURL}',
	  ContactUs: '${grailsApplication.config.com.recomdata.contactUs}',
	  AppTitle: '${grailsApplication.config.com.recomdata.appTitle}',
      BuildVersion: 'Build Version: <g:meta name="app.version"/> <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>',
	  AnalysisRun: false,
	  Analysis: 'Advanced',
	  HighDimDataType: '',
	  SNPType: '',
	  basePath: pageInfo.basePath,
	  hideAcrossTrialsPanel:'${grailsApplication.config.com.recomdata.datasetExplorer.hideAcrossTrialsPanel}',
	  metacoreAnalyticsEnabled: '${grailsApplication.config.com.thomsonreuters.transmart.metacoreAnalyticsEnable}',
	  metacoreUrl: '${grailsApplication.config.com.thomsonreuters.transmart.metacoreURL}',
      codeType: 'Concept',
      AnalysisHasBeenRun: false,
      ResultSetRegionParams: {},
      currentReportCodes: [],
      currentReportStudy: [],
      currentSubsetsStudy: '',
      isGridViewLoaded: false
	};
	// initialize browser version variables; see http://www.quirksmode.org/js/detect.html
	BrowserDetect.init();
	if (BrowserDetect.browser == "Explorer"){

	    if(BrowserDetect.version < 7) {
			GLOBAL.resulttype = 'image';
		}
	}
</script>
<div id="header-div"><g:render template="/layouts/commonheader" model="['app':'datasetExplorer']" /></div>
<div id="main"></div>
<h3 id="test">Loading....</h3>
<g:form name="exportdsform" controller="export" action="exportDataset"/>
<g:form name="exportgridform" controller="chart" action="exportGrid" />
	<g:if test="${'true'==grailsApplication.config.com.recomdata.datasetExplorer.genePatternEnabled}">
	<IFRAME src="${gplogout}" width="1" height="1" scrolling="no" frameborder="0" id="gplogin"></IFRAME>
	<IFRAME src="${gplogout}" width="1" height="1" scrolling="no" frameborder="0" id="altgplogin"></IFRAME>
	</g:if>
	
    <div id="saveReportDialog" style="display:none;font: 11px arial,tahoma,helvetica,sans-serif;font-weight:normal;">
        <br />
        Report Name : <input id='txtReportName' type='text' title="Report Name" /> <br />
        Make Report Public : <input id='chkReportPublic' type='checkbox' value='Y' title="Make Report Public" /><br /><br />

        <input type="button" onclick="saveReport(true,jQuery('#txtReportName').val(),jQuery('#txtReportDescription').val(),jQuery('#chkReportPublic').is(':checked'),GLOBAL.currentReportCodes.join('|'),GLOBAL.currentReportStudy)" value="Create Report" />
    </div>

    <div id="saveSubsetsDialog" style="display:none;font: 11px arial,tahoma,helvetica,sans-serif;font-weight:normal;">
        <form id="saveSubsetForm">
            <br />
            <em>*</em> Description : <input id='txtSubsetDescription' type='text' name='txtSubsetDescription' title="Subset Description"/>
            <br />
            <em>*</em> Make Subset Public : <input id='chkSubsetPublic' type='checkbox' value='Y' title="Subset Public" />
            <br />
            <br />
            <input class="submit" type="submit" value="Save Subsets"/>
        </form>
    </div>
	<span id="visualizerSpan0"></span> <!-- place applet tag here -->
	<span id="visualizerSpan1"></span> <!-- place applet tag here -->
<!-- ************************************** -->
	<!-- This implements the Help functionality -->
	<script type="text/javascript" src="${resource(dir:'js', file:'help/D2H_ctxt.js')}"></script>
	<script language="javascript">
		helpURL = '${grailsApplication.config.com.recomdata.adminHelpURL}';
	</script>
<!-- ************************************** -->

</body>
</html>
