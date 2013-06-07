<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
  
 
-->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/> 
        <meta name="layout" content="genesigmain" />
        <title>Gene Signature Search</title>
        <link rel="stylesheet" type="text/css" href="${resource(dir:'css/jquery/cupertino', file:'jquery-ui-1.8.18.custom.css')}">
        <link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'jquery.dataTables.css')}">
        <link rel="stylesheet" href="${resource(dir:'css', file:'colorbox.css')}"/>
        <script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery.min.js')}"></script>
	    <script>jQuery.noConflict();</script> 
		<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery.colorbox-min.js')}"></script>	
		<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-ui.min.js')}"></script>		
		<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.idletimeout.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.idletimer.js')}"></script>
       	<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.dataTables.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'sessiontimeout.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'geneSigSearch.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'utilitiesMenu.js')}"></script>
       	<script type="text/javascript" src="${resource(dir:'js/d3', file:'d3.v2.js')}"></script>
       	<script type="text/javascript" src="${resource(dir:'js', file:'manipulateGeneSig.js')}"></script>
       	<script type="text/javascript">JSCLASS_PATH = '../js/JS.Class/min'</script>
       	<script type="text/javascript" src="${resource(dir:'js/JS.Class/min', file:'loader-browser.js')}"></script>
		
		<!-- override main.css -->
		<style type="text/css">		
			.detail td a {
			    background: '';
			    padding-left: 10px;
			    vertical-align: top;			    
			 }		

			.detail td a:hover {
			    white-space: normal;		    
			 }		
		</style>

		<script language="javascript" type="text/javascript">

			function handleActionItem(actionItem, id) {
				var action = actionItem.value;
				var url 
				if(action=="") return false;
				
				// clone existing object and bring into edit wizard
				if(action=="clone") {
					url = "${createLink(action: 'cloneWizard')}/"+id+"";
				}
				
				// set delete flag
				if(action=="delete") {
					var del=confirm("Are you sure you want to delete?")

					if(del) {
						url="${createLink(action: 'delete')}/"+id;
						window.location.href=url;
					} else {
						return false;
					}
				}

				// edit wizard
				if(action=="edit") {
					url = "${createLink(action: 'editWizard')}/"+id+"";
				}				

				if(action=="showEditItems") {
					url = "${createLink(action: 'showEditItems')}/"+id+"";
				}
				
				// export to Excel 
				if(action=="export") {
					url = "${createLink(action: 'downloadExcel')}/"+id+"";
				}

				// get GMT file 
				if(action=="gmt") {
					url = "${createLink(action: 'downloadGMT')}/"+id+"";
				}

				// public action
				if(action=="public") {
					url = "${createLink(action: 'makePublic')}/"+id;
				}

				// send to url
				window.location.href=url;
			}
		
		</script>
	  <script type="text/javascript" src="${resource(dir:'js', file:'help/D2H_ctxt.js')}"></script>
        <script language="javascript">
        	helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
        </script>
		
		<script language="javascript" type="text/javascript">
			jQuery(document).ready(function() {
				var logoutURL = "${createLink([controller:'logout'])}";
			    var heartbeatURL = "${createLink([controller:'userLanding', action:'checkHeartBeat'])}";
		        addTimeoutDialog(heartbeatURL, logoutURL);
				initDataTables();
			});
			
		</script>
	
    </head>
    <body>
    <div class="body">   
  		<g:form frm="GenSignatureFrm" method="post">
  		<g:hiddenField name="id" value="" />
  		<g:hiddenField name="adminFlag" value="${adminFlag?'true':'false'}"/>
		 
	    <!--  show message -->
    	<g:if test="${flash.message}"><div class="message">${flash.message}</div><br></g:if>
 		
		<p style="text-align: right;"><span class="button"><g:actionSubmit class="edit" action="createWizard" value="New Signature"/></span></p>
    	<h1>Gene Signature List &nbsp;&nbsp;<a HREF="JavaScript:D2H_ShowHelp('1259',helpURL,'wndExternal',CTXT_DISPLAY_FULLHELP )">
				<img src="${resource(dir:'images',file:'help/helpicon_white.jpg')}" alt="Help" border=0 width=18pt style="vertical-align:middle;margin-left:5pt;"/>
			</a></h1>
	
    	<!-- show my signatures -->   	
       	<table id="mySignatures"  class="detail" style="width: 100%">
		<g:tableHeaderToggle label="My Signatures (${myItems.size()})" divPrefix="my_signatures" status="open" colSpan="${12}"/>
       	
    	<tbody id="my_signatures_detail" style="display: block;">
             <tr>
             	<th style="background:white;"></th>               
        	 	<th>Name</th>          	   	                
        	    <th>Author</th>
        	    <th>Date Created</th>
        	    <th>Date Modified</th>
        	    <th>Species</th>
        	    <th>Tech Platform</th>
				<th>Tissue Type</th>           	                   	        
        	    <th>Visibility</th>
        	    <th>Owner</th>
        	    <th>Gene List</th>
        	    <th># Genes</th>
        	    <th># Up-Regulated</th>
        	    <th># Down-Regulated</th>              	        
        	    <th>&nbsp;</th>
        	</tr>
        </thead>
		<tbody id="my_signatures_detail">
	       	<g:each var="gs" in="${myItems}" status="idx">      		 	       		
				<g:render template="/geneSignature/summary_record" model="[gs:gs, idx: idx]" />
			</g:each> 		
		</tbody>
       	</table>
       	
       	<!--  public signatures -->
       	<br>
      	<table id="publicSignatures"  class="detail" style="width: 100%">      	
		<g:tableHeaderToggle label="${adminFlag ? ('Other Signatures ('+pubItems.size()+')') : ('Public Signatures ('+pubItems.size()+')')}" divPrefix="pub_signatures" colSpan="${12}" />
       	
    	<tbody id="pub_signatures_detail" style="display: none;">
             <tr>
             	<th style="background:white;"></th>
        	 	<th>Name</th>          	   	                
        	    <th>Author</th>
        	    <th>Date Created</th>
        	    <th>Date Modified</th>
        	    <th>Species</th>
        	    <th>Tech Platform</th>
				<th>Tissue Type</th>           	                   	        
        	    <th>Visibility</th>
        	    <th>Owner</th>
        	    <th>Gene List</th>
        	    <th># Genes</th>
        	    <th># Up-Regulated</th>
        	    <th># Down-Regulated</th>
        	    <th>&nbsp;</th>
        	</tr>

	       	<g:each var="gs" in="${pubItems}" status="idx">      		 	       		
				<g:render template="/geneSignature/summary_record" model="[gs:gs, idx: idx]" /> 	       		
			</g:each>

		</tbody>
       	</table>
       	
       	</g:form>
    </div>
    <!-- Session timeout dialog -->
    <div id="timeout-div" title="Your session is about to expire!">
    	<p>You will be logged off in <span id="timeout-countdown"></span> seconds.</p>
        <p>Do you want to continue your session?</p>
    </div>    
    <div style="display:none;">
    	<div id="manipulateDiv">
    		<br>
    		<span id="actionLabel" style="border-width: 1px; border-style: solid; font-weight:bold;"></span>
	    	<div id="svg">
	        </div>
	        <br>
	        <div style="width:480; height:240">
	        	<div style="border-width: 1px; border-style: solid;">
	        		<table>
	        			<!--<tr><th colspan="2">Legend</th></tr>-->
	        			<tr><td>Gene Sig 1</td><td id="geneSig1Name"></td></tr>
	        			<tr><td>Gene Sig 2</td><td id="geneSig2Name"></td></tr>
	        			<tr id="geneSig3LegendRow"><td>Gene Sig 3</td><td id="geneSig3Name"></td></tr>
	        		</table>
	        	</div>
	        	<br>
	   	        <%--
	   	        <div style="text-align: right; float:left;">
		       		<g:submitButton class="edit" onClick="exportSVGImage();" value="Export" name="Export"/>
		        </div>
		        --%>
		        <div style="text-align: right; float:left;">
		        	<g:submitButton class="edit" onClick="resetVisualization()" value="Reset" name="Reset"/>
		        </div>
		        <br>
		        <div>
			        <g:form name="newList">
			        	<g:textArea id="manipulationResults" name="manipulationResults" value="" style="height: 180; width:480; border:1px double black"/>
			        	<br>
			        	<g:actionSubmit class="edit" action="createWizard" value="Save" name="Save"/>
			        </g:form>
		        </div>
	        </div>
    	</div>
    </div>
	</body>
</html>
