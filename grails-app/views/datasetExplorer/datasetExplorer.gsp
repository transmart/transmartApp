<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->

<%@ page language="java" import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<title>Dataset Explorer</title>

    <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE8">

    <LINK REL="SHORTCUT ICON"
	HREF="${resource(dir:'images', file:'i2b2_hive.ico')}">
<LINK REL="ICON"
	HREF="${resource(dir:'images', file:'i2b2_hive.ico')}">

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
            src="${resource(dir:'js', file:'ext/ext-all.js')}"></script>

    <script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-1.7.1.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-ui-1.8.17.custom.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery.tablesorter.min.js')}"></script>
  
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
	
	<script type="text/javascript" src="${resource(dir:'js', file:'browserDetect.js')}"></script>
	

 
	<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'datasetExplorer.css')}">
	<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'metacore.css')}">
    <script type="text/javascript" src="${resource(dir:'js/datasetExplorer', file: 'yui-combo-build-min.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js/datasetExplorer', file:'datasetExplorer.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'advancedWorkflowFunctions.js')}"></script>
	
	<script type="text/javascript" src="${resource(dir:'js/datasetExplorer', file:'highDimensionData.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'utilitiesMenu.js')}"></script>
	
	<script type="text/javascript" src="${resource(dir:'js/raphael', file:'raphael-min.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js/metacore', file:'metacoreEnrichment.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js/metacore', file:'metacoreEnrichmentDisplay.js')}"></script>
		
	<style>
		.ui-progressbar-value { background-image: url(images/pbar-ani.gif); }
	</style> 
</head>

<body>

<script type="text/javascript">
	var $j = jQuery.noConflict();
	Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

	//set ajax to 600*1000 milliseconds
	Ext.Ajax.timeout = 1800000;

	// this overrides the above
	Ext.Updater.defaults.timeout = 1800000;

	var pageInfo = {
		basePath :"${request.getContextPath()}"
	}
	
    var helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
	 
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
	  PMUrl: '${grailsApplication.config.com.recomdata.datasetExplorer.pmServiceURL}',
	  PMTransport: 'rest',
	  PMproxy:${grailsApplication.config.com.recomdata.datasetExplorer.pmServiceProxy},
	  CRCUrl: '',
	  ONTUrl: '',
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
	  HelpURL: '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}',
	  ContactUs: '${grailsApplication.config.com.recomdata.searchtool.contactUs}',
	  AppTitle: '${grailsApplication.config.com.recomdata.searchtool.appTitle}',
      BuildVersion: 'Build Version: <g:meta name="app.version"/> <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>',
	  AnalysisRun: false,
	  Analysis: 'Advanced',
	  HighDimDataType: '',
	  SNPType: '',
	  basePath: pageInfo.basePath,
	  hideAcrossTrialsPanel:'${grailsApplication.config.com.recomdata.datasetExplorer.hideAcrossTrialsPanel}',
	  metacoreAnalyticsEnabled: '${grailsApplication.config.com.thomsonreuters.transmart.metacoreAnalyticsEnable}',
	  metacoreUrl: '${grailsApplication.config.com.thomsonreuters.transmart.metacoreURL}'
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
	<g:if test="${'true'==grailsApplication.config.com.recomdata.datasetExplorer.enableGenePattern}">
	<g:set var="gplogout" value="${grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL}/gp/logout"/>
	</g:if>
	<g:else>
	<g:set var="gplogout" value=""/>	
	</g:else>
	<IFRAME src="${gplogout}" width="1" height="1" scrolling="no" frameborder="0" id="gplogin"></IFRAME>
	<IFRAME src="${gplogout}" width="1" height="1" scrolling="no" frameborder="0" id="altgplogin"></IFRAME>
		
	<span id="visualizerSpan0"></span> <!-- place applet tag here -->
	<span id="visualizerSpan1"></span> <!-- place applet tag here -->
<!-- ************************************** -->
	<!-- This implements the Help functionality -->
	<script type="text/javascript" src="${resource(dir:'js', file:'help/D2H_ctxt.js')}"></script>
	<script language="javascript">
		helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
	</script>
<!-- ************************************** --> 
</body>
</html>
