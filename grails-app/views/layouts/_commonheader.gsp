<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<g:if test="${debug}">
    <div id="search-explain" class="overlay">
        <b>Search Explainer</b>
        <span style="font-family:'Lucida Console', monospace" id="searchlog">&nbsp;</span>
    </div>
</g:if>

<table class="menuDetail" width="100%" style="height: 28px; border-collapse: collapse">
    <tr>
        <th class="menuBar" style="width: 20px">&nbsp;</th>
        <th class="menuBar" style="width: 150px"><g:if test="${'rwg' == app || 'datasetExplorer' == app}"><select
                id="search-categories"></select></g:if></th>
        <th class="menuBar" style="width: 190px"><g:if test="${'rwg' == app || 'datasetExplorer' == app}"><input
                id="search-ac"/></g:if></th>
        <th class="menuBar" style="width: 110px">
            <g:if test="${'rwg' == app}">
                <div id="cartbutton" class="greybutton">
                    <%-- <g:remoteLink controller="export" action="selection" update="${overlayExportDiv}"
                                 params="[eleId:overlayExportDiv]"
                                 before="initLoadingDialog('${overlayExportDiv}')" onComplete="centerDialog('${overlayExportDiv}')">--%>
                    <img src="${resource(dir: 'images', file: 'cart.png')}"/> Export Cart
                <%-- </g:remoteLink>--%>
                    <div id="cartcount">${exportCount ?: 0}</div>
                </div>
            </g:if>
        </th>
        <g:set var="extensionsRegistry" bean="transmartExtensionsRegistry"/>
        <th class="menuBar" style="text-align: left;">
            <!-- menu links -->
            <table class="menuDetail" id="menuLinks" style="width: 1px;"
                   align="right"><!-- Use minimum possible width -->
                <tr>
                    <th width="150">&nbsp;</th>
                    <g:set var="tabs" value="${extensionsRegistry.getTabs([
                            [id: 'search', title: 'Search', controller: 'search', display: grailsApplication.config.ui.tabs.search.show],
                            [id: 'rwg', title: 'Browse', controller: 'RWG', display: !grailsApplication.config.ui.tabs.browse.hide],
                            [id: 'datasetExplorer', title: 'Analyze', controller: 'datasetExplorer'],
                            [id: 'sampleexplorer', title: 'Sample Explorer', controller: 'sampleExplorer', display: !grailsApplication.config.ui.tabs.sampleExplorer.hide],
                            [id: 'genesignature', title: 'Gene&nbsp;Signature/Lists', controller: 'geneSignature', display: !grailsApplication.config.ui.tabs.geneSignature.hide],
                            [id: 'gwas', title: 'GWAS', controller: 'GWAS', display: !grailsApplication.config.ui.tabs.gwas.hide],
                            [id: 'uploaddata', title: 'Upload Data', controller: 'uploadData', display: !grailsApplication.config.ui.tabs.uploadData.hide],
                            [id: 'accesslog', title: 'Admin', controller: 'accessLog', display: SpringSecurityUtils.ifAnyGranted('ROLE_ADMIN')]

                    ])}"/>
                    <g:each in="${tabs}" var="tab">
                        <g:if test="${tab.get('display', true)}">
                            <g:render template="/layouts/headertab" model="[id: tab.id, title: tab.title, controller: tab.controller, app: app]"/>
                        </g:if>
                    </g:each>

                    <tmpl:/layouts/utilitiesMenu/>
                </tr>
            </table>
        </th>

    </tr>
</table>

<link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'sanofi.css')}">

<script type="text/javascript" src="${resource(dir: 'js/jQuery', file: 'jquery.idletimeout.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js/jQuery', file: 'jquery.idletimer.js')}"></script>
<script type="text/javascript" src="${resource(dir: 'js', file: 'sessiontimeout.js')}"></script>

<!-- Session timeout dialog -->
<div id="timeout-div" title="Your session is about to expire!">
    <p>You will be logged off in <span id="timeout-countdown"></span> seconds.</p>

    <p>Do you want to continue your session?</p>
</div>
<r:require module="session_timeout_nodep"/>
<r:script>
    jQuery(document).ready(function() {
	    addTimeoutDialog({
	        sessionTimeout : ${grails.util.Holders.config.com.recomdata.sessionTimeout},
            heartbeatURL : "${createLink([controller: 'userLanding', action: 'checkHeartBeat'])}",
	        heartbeatLaps : ${grails.util.Holders.config.com.recomdata.heartbeatLaps},
            logoutURL : "${createLink([controller: 'login', action: 'forceAuth'])}"
	    });
   });
</r:script>
<!-- The below script fragment provided by JIRA to report bugs at jira.transmartfoundation.org -->
<script type="text/javascript" src="https://jira.transmartfoundation.org/s/8c444fcd9d47fdf56ca2f75ec1e9fd15-T/en_GBh7pwdp/70120/0cff1430a886a90ec539aa112db8aee1/2.0.8/_/download/batch/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector/com.atlassian.jira.collector.plugin.jira-issue-collector-plugin:issuecollector.js?locale=en-GB&collectorId=8d56c6a7"></script>

