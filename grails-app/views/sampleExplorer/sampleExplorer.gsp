<%@ page language="java" import="java.util.*" %>
<!DOCTYPE HTML>
<html>
<head>
    <!-- Force Internet Explorer 8 to override compatibility mode -->
    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

    <title>Sample Explorer :: ${grailsApplication.config.com.recomdata.appTitle}</title>

    <link href="${resource(dir: 'images', file: 'searchtool.ico')}" rel="shortcut icon" />
    <link href="${resource(dir: 'images', file: 'searchtool.ico')}" rel="icon" />

    <g:javascript library="jquery" />
    <r:require module="sampleTab" />
    <r:layoutResources/>

    <r:script type="text/javascript" charset="utf-8">

        var $j = window.$j = jQuery.noConflict();

    </r:script>

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
        CurrentTimepoints: [],
        CurrentSamples: [],
        CurrentPlatforms: [],
        CurrentGpls: [],
        CurrentTissues: [],
        CurrentRbmpanels: [],
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

<r:layoutResources/>
</body>
</html>