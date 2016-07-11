<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="grails.converters.JSON" %>
<!DOCTYPE HTML>
<html>
<head>
    <!-- Force Internet Explorer 8 to override compatibility mode -->
    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

    <title>Dataset Explorer</title>

    <link href="${resource(dir: 'images', file: 'i2b2_hive.ico')}" rel="shortcut icon">
    <link href="${resource(dir: 'images', file: 'i2b2_hive.ico')}" rel="icon">

    <%-- We do not have a central template, so this only works in the database explorer for now --%>
    <g:if test="${['true', true]*.equals(grailsApplication.config.com.recomdata.debug.jsCallbacks).any()}">
        <g:javascript src="long-stack-traces.js"/>
    </g:if>

    <!-- Include Ext and app-specific scripts: -->
    <script type="text/javascript" src="${resource(dir: 'plugins/prototype-1.0/js/prototype', file: 'prototype.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ext/adapter/ext/ext-base.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/jQuery', file: 'jquery.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/jQuery', file: 'jquery-ui-1.9.1.custom.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/jQuery', file: 'jquery.tablesorter.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.cookie.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.dynatree.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.paging.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.loadmask.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.ajaxmanager.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.numeric.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.colorbox-min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.simplemodal.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.dataTables.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'facetedSearch/facetedSearchBrowse.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/ui.multiselect.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/jquery.validate.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jQuery/additional-methods.min.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ajax_queue.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'ext/ext-all.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/ext-ux', file: 'miframe.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer', file: 'i2b2common.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer', file: 'requests.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer', file: 'ext-i2b2.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer', file: 'workflowStatus.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'myJobs.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer', file: 'reports.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer', file: 'workspace.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer/exportData', file: 'dataTab.js')}"></script>
    <script type="text/javascript"
            src="${resource(dir: 'js/datasetExplorer/exportData', file: 'exportJobsTab.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'fixconsole.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'browserDetect.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/utils', file: 'json2.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/utils', file: 'dynamicLoad.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer', file: 'highDimensionData.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'utilitiesMenu.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer', file: 'gridView.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer', file: 'datasetExplorer.js')}"></script>
    <script type="text/javascript"
            src="${resource(dir: 'js/datasetExplorer', file: 'datasetExplorerLaunchers.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer', file: 'sampleQuery.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'rwgsearch.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'advancedWorkflowFunctions.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/datasetExplorer', file: 'highDimensionData.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js/Galaxy', file: 'galaxyExport.js')}"></script>
    <script type="text/javascript"
            src="//yui.yahooapis.com/combo?2.9.0/build/yahoo/yahoo-min.js&2.9.0/build/get/get-min.js"></script>

    <tmpl:/RWG/urls/>

    <!-- Include Ext stylesheets here: -->
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'js/ext/resources/css', file: 'ext-all.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'js/ext/resources/css', file: 'xtheme-gray.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css/jquery/ui', file: 'jquery-ui-1.9.1.custom.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css/jquery/skin', file: 'ui.dynatree.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'datasetExplorer.css')}">
    <link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'folderManagement.css', plugin: 'folder-management')}">
    <script type="text/javascript" src="${resource(dir:'js', file:'folderManagementDE.js', plugin: 'folder-management')}"></script>

    <!-- Adding these validation functions to get the Forest Plot to work. These might be able to be blended into the javascript object that controls the advanced workflow validation. -->
    <script type="text/javascript"
            src="${resource(dir: 'js/datasetExplorer', file: 'workflowValidationFunctions.js')}"></script>

    <r:layoutResources/>
    <%-- XXX: Use template --%>

    <script type="text/javascript">
        /******************************************************************************/
        //Global Variables

        var pageInfo = {
            basePath: "${request.getContextPath()}"
        }

        GLOBAL = {
            Version: '1.0',
            Domain: '${i2b2Domain}',
            ProjectID: '${i2b2ProjectID}',
            Username: '${i2b2Username}',
            Password: '${i2b2Password}',
            AutoLogin: true,
            Debug: false,
            NumOfSubsets: 2,
            NumOfQueryCriteriaGroups: 20,
            NumOfQueryCriteriaGroupsAtStart: 3,
            MaxSearchResults: 100,
            ONTUrl: '',
            usePMHost: '${grailsApplication.config.com.recomdata.datasetExplorer.usePMHost}',
            Config: 'jj',
            CurrentQueryName: '',
            CurrentComparisonName: ' ',
            CurrentSubsetIDs: [],
            CurrentSubsetQueries: ["", "", ""],
            CurrentPathway: '',
            CurrentPathwayName: '',
            CurrentGenes: '',
            CurrentChroms: '',
            CurrentDataType: '',
            GPURL: '${grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL}',
            EnableGP: '${grailsApplication.config.com.recomdata.datasetExplorer.enableGenePattern}',
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
            DefaultPathToExpand: "${pathToExpand}",
            UniqueLeaves: "",
            preloadStudy: "${params.DataSetName}",
            Binning: false,
            ManualBinning: false,
            NumberOfBins: 4,
            HelpURL: '${grailsApplication.config.com.recomdata.adminHelpURL}',
            ContactUs: '${grailsApplication.config.com.recomdata.contactUs}',
            AppTitle: '${grailsApplication.config.com.recomdata.appTitle}',
            BuildVersion: 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>',
            AnalysisRun: false,
            Analysis: 'Advanced',
            HighDimDataType: '',
            SNPType: '',
            basePath: pageInfo.basePath,
            hideAcrossTrialsPanel: ${!!grailsApplication.config.com.recomdata.datasetExplorer.hideAcrossTrialsPanel},
            gridViewEnabled: ${!grailsApplication.config.ui.tabs.datasetExplorer.gridView.hide},
            dataExportEnabled: ${!grailsApplication.config.ui.tabs.datasetExplorer.dataExport.hide},
            dataExportJobsEnabled: ${!grailsApplication.config.ui.tabs.datasetExplorer.dataExportJobs.hide},
            analysisJobsEnabled: ${!!grailsApplication.config.ui.tabs.datasetExplorer.analysisJobs.show},
            workspaceEnabled: ${!grailsApplication.config.ui.tabs.datasetExplorer.workspace.hide},
            sampleExplorerEnabled: ${!!grailsApplication.config.ui.tabs.sampleExplorer.show},
            metacoreAnalyticsEnabled: ${!!grailsApplication.config.com.thomsonreuters.transmart.metacoreAnalyticsEnable},
            metacoreUrl: '${grailsApplication.config.com.thomsonreuters.transmart.metacoreURL}',
            AnalysisHasBeenRun: false,
            ResultSetRegionParams: {},
            currentReportCodes: [],
            currentReportStudy: [],
            currentSubsetsStudy: '',
            isGridViewLoaded: false,
            galaxyEnabled: '${grailsApplication.config.com.galaxy.blend4j.galaxyEnabled}',
            galaxyUrl: "${grailsApplication.config.com.galaxy.blend4j.galaxyURL}",
            analysisTabExtensions: ${grailsApplication.mainContext.getBean('transmartExtensionsRegistry').analysisTabExtensions as JSON}
        };
        // initialize browser version variables; see http://www.quirksmode.org/js/detect.html
        BrowserDetect.init();
        if (BrowserDetect.browser == "Explorer") {

            if (BrowserDetect.version < 7) {
                GLOBAL.resulttype = 'image';
            }
        }
    </script>
</head>

<body>

<script type="text/javascript">
    var sessionSearch = "${rwgSearchFilter}";
    var sessionOperators = "${rwgSearchOperators}";
    var sessionSearchCategory = "${rwgSearchCategory}";
    var searchPage = "datasetExplorer";
    var $j = jQuery.noConflict();
    Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";
    var dseOpenedNodes = "${dseOpenedNodes}";
    var dseClosedNodes = "${dseClosedNodes}";

    //set ajax to 600*1000 milliseconds
    Ext.Ajax.timeout = 1800000;

    // this overrides the above
    Ext.Updater.defaults.timeout = 1800000;

    var helpURL = '${grailsApplication.config.com.recomdata.adminHelpURL}';
</script>

<div id="header-div"><g:render template="/layouts/commonheader" model="['app': 'datasetExplorer']"/></div>

<div id="main"></div>

<h3 id="test">Loading ...</h3>
<tmpl:/RWG/boxSearch hide="true"/>
<tmpl:/RWG/filterBrowser/>
<div id="sidebartoggle">&nbsp;</div>

<div id="noAnalyzeResults" style="display: none;">No subject-level results found.<br/>
<g:if test="${!grailsApplication.config.ui.tabs.browse.hide}">
    <g:link controller="RWG" action="index">Switch to Browse view</g:link>
</g:if>
</div>

<div id="filter-div" style="display: none;"></div>
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

<div id="saveReportDialog" style="display:none;font: 11px arial,tahoma,helvetica,sans-serif;font-weight:normal;">
    <br/>
    Report Name : <input id='txtReportName' type='text' title="Report Name"/> <br/>
    Make Report Public : <input id='chkReportPublic' type='checkbox' value='Y' title="Make Report Public"/><br/><br/>

    <input type="button"
           onclick="saveReport(true, jQuery('#txtReportName').val(), jQuery('#txtReportDescription').val(), jQuery('#chkReportPublic').is(':checked'), GLOBAL.currentReportCodes.join('|'), GLOBAL.currentReportStudy)"
           value="Create Report"/>
</div>

<div id="saveSubsetsDialog" style="display:none;font: 11px arial,tahoma,helvetica,sans-serif;font-weight:normal;">
    <form id="saveSubsetForm">
        <br/>
        <em>*</em> Description : <input id='txtSubsetDescription' type='text' name='txtSubsetDescription'
                                        title="Subset Description"/>
        <br/>
        <em>*</em> Make Subset Public : <input id='chkSubsetPublic' type='checkbox' value='Y' title="Subset Public"/>
        <br/>
        <br/>
        <input class="submit" type="submit" value="Save Subsets"/>
    </form>
</div>
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
