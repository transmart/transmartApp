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
        <th class="menuBar" style="text-align: left;">
            <!-- menu links -->
            <table class="menuDetail" id="menuLinks" style="width: 1px;"
                   align="right"><!-- Use minimum possible width -->
                <tr>
                    <th width="150">&nbsp;</th>
                    <%--See Config.groovy--%>
                    <g:if test="${grailsApplication.config.ui.tabs.search.show}">
                        <g:if test="${'search'==app}"><th class="menuVisited">Search</th></g:if>
                        <g:else><th class="menuLink"><g:link controller="search">Search</g:link></th></g:else>
                    </g:if>

                    <g:if test="${!grailsApplication.config.ui.tabs.browse.hide}">
                        <g:if test="${'rwg' == app}"><th class="menuVisited">Browse</th></g:if>
                        <g:else><th class="menuLink"><g:link controller="RWG">Browse</g:link></th></g:else>
                    </g:if>
                    <%--Analyze tab is always visible--%>
                    <g:if test="${'datasetExplorer' == app}"><th class="menuVisited">Analyze</th></g:if>
                    <g:else><th class="menuLink"><g:link controller="datasetExplorer">Analyze</g:link></th></g:else>

                    <g:if test="${!grailsApplication.config.ui.tabs.sampleExplorer.hide}">
                        <g:if test="${'sampleexplorer' == app}"><th class="menuVisited">Sample Explorer</th></g:if>
                        <g:else><th class="menuLink"><g:link controller="sampleExplorer">Sample Explorer</g:link></th></g:else>
                    </g:if>

                    <g:if test="${!grailsApplication.config.ui.tabs.geneSignature.hide}">
                        <g:if test="${'genesignature' == app}"><th class="menuVisited">Gene&nbsp;Signature/Lists</th></g:if>
                        <g:else><th class="menuLink"><g:link controller="geneSignature">Gene&nbsp;Signature/Lists</g:link></th></g:else>
                    </g:if>

                    <g:if test="${!grailsApplication.config.ui.tabs.gwas.hide}">
                        <g:if test="${'gwas' == app}"><th class="menuVisited">GWAS</th></g:if>
                        <g:else><th class="menuLink"><g:link controller="GWAS">GWAS</g:link></th></g:else>
                    </g:if>

                    <g:if test="${grailsApplication.config.ui.tabs.uploadData.show}">
                        <g:if test="${'uploaddata' == app}"><th class="menuVisited">Upload GWAS</th></g:if>
                        <g:else><th class="menuLink"><g:link controller="uploadData">Upload GWAS</g:link></th></g:else>
                    </g:if>

                    <sec:ifAnyGranted roles="ROLE_ADMIN">
                        <g:if test="${'accesslog' == app}"><th class="menuVisited">Admin</th></g:if>
                        <g:else><th class="menuLink"><g:link controller="accessLog">Admin</g:link></th></g:else>
                    </sec:ifAnyGranted>

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

