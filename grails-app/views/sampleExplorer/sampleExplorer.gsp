<%@ page language="java" import="java.util.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	
	<title>Sample Explorer</title>
	
	<!-- Include Ext and app-specific scripts: -->
	<script type="text/javascript" src="${resource(dir:'js/sarissa', file:'sarissa.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js/sarissa', file: 'sarissa_ieemu_xpath.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js/javeline', file: 'javeline_xpath.js')}"></script>
	<g:javascript library="prototype" />
	<script type="text/javascript" src="${resource(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'browserDetect.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'ext/ext-all.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'searchcombobox.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'picklist.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'sampleExplorerMenu.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'myJobs.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'advancedWorkflowFunctions.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js/datasetExplorer', file:'i2b2common.js')}"></script>
		
	
	<script type="text/javascript" src="${resource(dir:'js', file:'sampleExplorer/sampleExplorer.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'sampleExplorer/HaploView.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'sampleExplorer/SNP.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'sampleExplorer/IGV.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'sampleExplorer/GWAS.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js/ext-ux', file:'miframe.js')}"></script>
	<link rel="stylesheet" type="text/css" href="${resource(dir:'js/ext/resources/css', file:'ext-all.css')}">
	<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'sampleExplorer.css')}">	
	<link rel="stylesheet" type="text/css" href="${resource(dir:'js/ext/resources/css', file:'xtheme-gray.css')}">
	<script type="text/javascript" src="${resource(dir:'js', file:'utilitiesMenu.js')}"></script>
	<link rel="stylesheet" href="${resource(dir:'css', file:'main.css')}"></link>
</head>		
	
	<body>
		
<script type="text/javascript">
	Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

	//set ajax to 600*1000 milliseconds
	Ext.Ajax.timeout = 1800000;

	// this overrides the above
	Ext.Updater.defaults.timeout = 1800000;

	var pageInfo = {
		basePath :"${request.getContextPath()}"
	}

	/******************************************************************************/
	//Global Variables
	GLOBAL = {
	  Version : '1.0',
	  SearchJSON: {},
	  resultDataSet: {},
	  resultGridPanel: '',
	  columnList: '${columnList}',
	  CurrentTimepoints: new Array(),
	  CurrentSamples: new Array(),
	  CurrentPlatforms: new Array(),
	  CurrentGpls: new Array(),
	  CurrentTissues: new Array(),
	  CurrentRbmpanels: new Array(),
	  Explorer: "SAMPLE",
	  resulttype: 'applet',
	  subsetTabs: 1,
      HelpURL: '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}',
      ContactUs: '${grailsApplication.config.com.recomdata.searchtool.contactUs}',
      AppTitle: '${grailsApplication.config.com.recomdata.searchtool.appTitle}',
      BuildVersion: 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>'
	};
	// initialize browser version variables; see http://www.quirksmode.org/js/detect.html
	BrowserDetect.init();
	if (BrowserDetect.browser == "Explorer"){

	    if(BrowserDetect.version < 7) {
			GLOBAL.resulttype = 'image';
		}
	}

	
	
</script>	

<div id="header-div"><g:render template="/layouts/commonheader" model="['app':'sampleexplorer']" /></div>
<div id="main"></div>
<h3 id="test">Loading....</h3>
<g:form name="exportdsform" controller="export" action="exportDataset"/>
<g:form name="exportgridform" controller="chart" action="exportGrid" />

<IFRAME src="${grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL}/gp/logout" width="1" height="1" scrolling="no" frameborder="0" id="gplogin"></IFRAME>
<IFRAME src="${grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL}/gp/logout" width="1" height="1" scrolling="no" frameborder="0" id="altgplogin"></IFRAME>
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