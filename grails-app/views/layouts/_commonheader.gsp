<%--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
--%>

<div id="logocutout">
	<g:if test="${'rwg' == app}">
        <g:img file="logo.png"/>
	</g:if>
	<g:else>
		<g:link controller="RWG" action="index">
            <g:img file="logo.png"/>
        </g:link>
	</g:else>

</div>
        
<g:if test="${debug}">
	<div id="search-explain" class="overlay">
		<b>Search Explainer</b>
		<tt id="searchlog">&nbsp;</tt>
	</div>
</g:if>
		   			
<table class="menuDetail" width="100%" style="height: 28px; border-collapse: collapse">
	<tr>
		<th class="menuBar" style="width: 160px">
			&nbsp;
		</th>
		<th class="menuBar" style="width: 150px"><g:if test="${'rwg' == app || 'datasetExplorer' == app}"><select id="search-categories"></select></g:if></th>
		<th class="menuBar" style="width: 160px"><g:if test="${'rwg' == app || 'datasetExplorer' == app}"><input id="search-ac"/></input></g:if></th>
		<th class="menuBar" style="width: 110px">
			<g:if test="${'rwg' == app}">
				<div id="cartbutton" class="greybutton">
		   			<%-- <g:remoteLink controller="export" action="selection" update="${overlayExportDiv}" 
		                            params="[eleId:overlayExportDiv]" 
		                            before="initLoadingDialog('${overlayExportDiv}')" onComplete="centerDialog('${overlayExportDiv}')">--%>
					<img src="${resource(dir:'images', file:'cart.png')}"/> Export Cart
					<%-- </g:remoteLink>--%>
					<div id="cartcount">${exportCount ?: 0}</div>
				</div>
			</g:if>
		</th>
		<th class="menuBar" style="text-align: left;">
			<!-- menu links -->
			<table class="menuDetail" id="menuLinks" style="width: 1px;" align="right"> <!-- Use minimum possible width -->
		    	<tr>
		    		<th width="150">&nbsp;</th>
	   				<%--<g:if test="${'search'==app}"><th class="menuVisited">Search</th></g:if>
		   			<g:else><th class="menuLink"><g:link controller="search">Search</g:link></th></g:else>--%>

			       	<g:if test="${'rwg'==app}"><th class="menuVisited">Browse</th></g:if>
	       			<g:else><th class="menuLink"><g:link controller="RWG">Browse</g:link></th></g:else>

			       	<g:if test="${'datasetExplorer'==app}"><th class="menuVisited">Analyze</th></g:if>
	       			<g:else><th class="menuLink"><g:link controller="datasetExplorer">Analyze</g:link></th></g:else>

	       			<g:if test="${grailsApplication.config.com.recomdata.hideSampleExplorer!='true'}">
	   				<g:if test="${'sampleexplorer'==app}"><th class="menuVisited">Sample Explorer</th></g:if>
		   			<g:else><th class="menuLink"><g:link controller="sampleExplorer">Sample Explorer</g:link></th></g:else>	   
		   			</g:if>

	   				<g:if test="${'genesignature'==app}"><th class="menuVisited">Gene&nbsp;Signature/Lists</th></g:if>
		   			<g:else><th class="menuLink"><g:link controller="geneSignature">Gene&nbsp;Signature/Lists</g:link></th></g:else>

	   				<g:if test="${'gwas'==app}"><th class="menuVisited">GWAS</th></g:if>
		   			<g:else><th class="menuLink"><g:link controller="GWAS">GWAS</g:link></th></g:else>

		      		<sec:ifAnyGranted roles="ROLE_ADMIN">
	   					<g:if test="${'accesslog'==app}"><th class="menuVisited">Admin</th></g:if>
		   				<g:else><th class="menuLink"><g:link controller="accessLog">Admin</g:link></th></g:else>
		       		</sec:ifAnyGranted>
		       		
					<tmpl:/layouts/utilitiesMenu />
		       	</tr>
		 	</table>
		</th>

	</tr>
</table>

<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'sanofi.css')}">

<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery.idletimeout.js')}"></script>
<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery.idletimer.js')}"></script>
<script type="text/javascript" src="${resource(dir:'js', file:'sessiontimeout.js')}"></script>

<!-- Session timeout dialog -->
<div id="timeout-div" title="Your session is about to expire!">
    <p>You will be logged off in <span id="timeout-countdown"></span> seconds.</p>
    <p>Do you want to continue your session?</p>
</div>
<r:require module="session_timeout_nodep"/>
<r:script>
    jQuery(document).ready(function() {
		 var logoutURL = "${createLink([controller:'login', action: 'forceAuth'])}";
	    var heartbeatURL = "${createLink([controller:'userLanding', action:'checkHeartBeat'])}";
	    addTimeoutDialog(heartbeatURL, logoutURL);
   });
</r:script>
