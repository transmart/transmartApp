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

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/> 
        <meta name="layout" content="genesigmain" />
        <title>Gene Signature Search</title>
        <link rel="stylesheet" type="text/css" href="${resource(dir:'css/jquery/cupertino', file:'jquery-ui-1.8.18.custom.css')}">
        <link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'jquery.dataTables.css')}">
        <link rel="stylesheet" href="${resource(dir:'css', file:'colorbox.css')}"></link>
        <script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery.min.js')}"></script>
	    <script>jQuery.noConflict();</script> 
		<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery.colorbox-min.js')}"></script>	
		<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-ui.min.js')}"></script>		
		<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.idletimeout.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.idletimer.js')}"></script>
       	<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.dataTables.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'sessiontimeout.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'geneSigSearch.js')}"></script>
       	<script type="text/javascript" src="${resource(dir:'js/d3', file:'d3.v2.js')}"></script>
       	<script type="text/javascript" src="${resource(dir:'js', file:'manipulateGeneSig.js')}"></script>
		
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
			jQuery(document).ready(function() {
				var logoutURL = "${createLink([controller:'logout'])}";
			    var heartbeatURL = "${createLink([controller:'userLanding', action:'checkHeartBeat'])}";
		        addTimeoutDialog(heartbeatURL, logoutURL);
		        console.log("applying datatables");
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
    	
    	<div style="float:left"><h1>Gene Signature List(${myItems.size()})</h1></div>
    	<div style="text-align: right; float:right;"><span class="button"><g:actionSubmit class="edit" action="createWizard" value="New Signature"/></span></div>
    	<!-- show my signatures -->   	
       	<table id="mySignatures"  class="detail" style="width: 100%">
       	<thead>
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
        	    <th>Ownership</th>
        	    <th>Gene List</th>
        	    <th># Genes</th>
        	    <th># Up-Regulated</th>
        	    <th># Down-Regulated</th>              	        
        	</tr>
        </thead>
		<tbody id="my_signatures_detail">
	       	<g:each var="gs" in="${myItems}" status="idx">      		 	       		
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
	    	<div id="svg">
	        </div>
	        <br>
	        <div style="width:480; height:240">
	   	        <div style="text-align: right; float:left;">
		       		<g:submitButton class="edit" onClick="exportSVGImage();" value="Export" name="Export"/>
		        </div>
		        <div style="text-align: right; float:right;">
		        	<g:submitButton class="edit" onClick="resetResults();" value="Reset" name="Reset"/>
		        </div>
		        <br>
		        <g:form name="newList">
		        	<g:textArea id="manipulationResults" name="results" value="" style="height: 180; width:480; border:1px double black"/>
		        	<br>
		        	<g:textField name="newGeneListName" value="New Genelist Name" onClick="resetGeneListName()"/>
		        	<g:submitButton class="edit" onClick="saveNewList();" value="Save" name="Save"/>
		        </g:form>
	        </div>
    	</div>
    </div>
	</body>
</html>
