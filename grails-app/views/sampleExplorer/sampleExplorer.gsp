<%@ page language="java" import="java.util.*" %>
<!DOCTYPE html>
<html>
<head>
    <!-- Force Internet Explorer 8 to override compatibility mode -->
    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

    <title>Sample Explorer :: ${grailsApplication.config.com.recomdata.appTitle}</title>

    <!-- Include Ext and app-specific scripts: -->
    <script type="text/javascript" src="${resource(dir: 'js/jQuery', file: 'jquery.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/jQuery', file: 'jquery-ui-1.9.1.custom.min.js')}"></script>

    <script type="text/javascript">
        var $j = jQuery.noConflict();
    </script>

    <script type="text/javascript" src="${resource(dir: 'js', file: 'browserDetect.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ext/adapter/ext/ext-base.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'myJobs.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'sampleExplorer/sampleExplorerMenu.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'sampleExplorer/sampleExplorer.js')}"></script>
    <script type="text/javascript"
            src="${resource(dir: 'js', file: 'sampleExplorer/sampleExplorer.utilities.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'utilitiesMenu.js')}"></script>
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'js/ext/resources/css', file: 'ext-all.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'sampleExplorer.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'chartservlet.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'js/ext/resources/css', file: 'xtheme-gray.css')}">
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'main.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'datasetExplorer.css')}">
    <!--
    <script type="text/javascript" src="${resource(dir: 'plugins/prototype-1.0/js/prototype', file: 'prototype.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'searchcombobox.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'picklist.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'advancedWorkflowFunctions.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer', file: 'i2b2common.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'sampleExplorer/HaploView.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'sampleExplorer/SNP.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'sampleExplorer/IGV.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'sampleExplorer/GWAS.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/ext-ux', file: 'miframe.js')}"></script>
    -->

    <r:layoutResources/>
    <%-- XXX: Use template --%>

</head>

<body>

<script type="text/javascript">
    Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

    //set ajax to 600*1000 milliseconds
    Ext.Ajax.timeout = 1800000;

    // this overrides the above
    Ext.Updater.defaults.timeout = 1800000;

    var pageInfo = {
        basePath: "${request.getContextPath()}"
    }

    var sampleRequestType = "${sampleRequestType}" || ""
    var currentResultInstanceId = "${result_instance_id}" || ""

    /******************************************************************************/
    //Global Variables
    GLOBAL = {
        Version: '1.0',
        SearchJSON: {},
        resultDataSet: {},
        resultGridPanel: '',
        columnMap: ${columnData},
        CurrentTimepoints: new Array(),
        CurrentSamples: new Array(),
        CurrentPlatforms: new Array(),
        CurrentGpls: new Array(),
        CurrentTissues: new Array(),
        CurrentRbmpanels: new Array(),
        Explorer: "SAMPLE",
        resulttype: 'applet',
        subsetTabs: 1,
        HelpURL: '${grailsApplication.config.com.recomdata.adminHelpURL}',
        ContactUs: '${grailsApplication.config.com.recomdata.contactUs}',
        basePath: pageInfo.basePath,
        AppTitle: '${grailsApplication.config.com.recomdata.appTitle}',
        resultsGridHeight: jQuery(window).height() - 120,
        //resultsGridHeight : ${grailsApplication.config.sampleExplorer.resultsGridHeight},
        resultsGridWidth: '100%',
        BuildVersion: 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>',
        explorerType: 'sampleExplorer'
    };
    // initialize browser version variables; see http://www.quirksmode.org/js/detect.html
    BrowserDetect.init();
    if (BrowserDetect.browser == "Explorer") {

        if (BrowserDetect.version < 7) {
            GLOBAL.resulttype = 'image';
        }
    }



</script>

<div id="header-div" class="header-div">
    <g:render template="/layouts/commonheader" model="['app': 'sampleexplorer', 'utilitiesMenu': 'true']"/>
</div>

<div id="main"></div>

<h3 id="test">Loading....</h3>
<g:form name="exportdsform" controller="export" action="exportDataset"/>
<g:form name="exportgridform" controller="chart" action="exportGrid"/>

<g:if test="${'true' == grailsApplication.config.com.recomdata.datasetExplorer.enableGenePattern}">
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
<script type="text/javascript" src="${resource(dir: 'js', file: 'help/D2H_ctxt.js')}"></script>
<script language="javascript">
    helpURL = '${grailsApplication.config.com.recomdata.adminHelpURL}';
</script>
<!-- ************************************** -->

<r:layoutResources/><%-- XXX: Use template --%>
</body>
</html>