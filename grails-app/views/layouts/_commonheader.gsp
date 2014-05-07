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
<div id="navlist">
    <ul>
        <g:if test="${'search'==app}"><li class="active">Search</li></g:if>
        <g:else><li><a href="${createLink([controller:'search'])}">Search</a></li></g:else>

        <g:if test="${'rwg'==app}"><li class="active">Faceted Search</li></g:if>
        <g:else><li><a href="${createLink([controller:'RWG'])}">Faceted Search</a></li></g:else>    

        <g:if test="${'datasetExplorer'==app}"><li class="active">Dataset Explorer</li></g:if>
        <g:else><li><a href="${createLink([controller:'secure'])}">Dataset Explorer</a></li></g:else>    
        
		   			
        <g:if test="${'genesignature'==app}"><li class="active">Gene Signature/Lists</li></g:if>
        <g:else><li><a href="${createLink([controller:'geneSignature'])}">Gene Signature/Lists</a></li></g:else>
             
             
             <g:if test="${'gwas'==app}"><li class="active">GWAS</li></g:if>
        <g:else><li><a href="${createLink([controller:'GWAS'])}">GWAS</a></li></g:else>    
        	
		   			<g:if test="${'uploaddata'==app}"><th class="active">Upload Data</th></g:if>
		   			<g:else><th class="menuLink"><g:link controller="uploadData">Upload Data</g:link></th></g:else>
		   			
		      
            
		      		<sec:ifAnyGranted roles="ROLE_ADMIN">
            <g:if test="${'accesslog'==app}"><li class="active">Admin</li></g:if>
            <g:else><li><a href="${createLink([controller:'accessLog'])}">Admin</a></li></g:else>
		       		</sec:ifAnyGranted>
    </ul>
</div>
<div id = "utilities-holder">
    <div id="utilities-div">
        <a href="#" id="utilities-div-link"><span style="padding-right:2px"><sec:loggedInUserInfo field="userRealName"/></span> <span><img style="margin-top: 6px; vertical-align: top;" alt="" src="${resource(dir:'images',file:'tiny_down_arrow_white.png')}" /></span></a>
    </div>
    <div id="utilities-dropdown" style="display:none;">
        <ol>
            <li><a href="javascript:popupWindow('${grailsApplication.config.com.recomdata.adminHelpURL}','transmart_help');">Help</a></li>
            <li><a href="mailto:tranSMART@its.jnj.com">Contact Us</a></li>
            <li><a href="${createLink([controller:'login', action: 'forceAuth'])}">Login</a></li>
            <li><a href="${createLink([controller:'logout'])}">Logout</a></li>
        </ol>
    </div>   
</div>
<!-- Session timeout dialog -->
<div id="timeout-div" title="Your session is about to expire!">
    <p>You will be logged off in <span id="timeout-countdown"></span> seconds.</p>
    <p>Do you want to continue your session?</p>
</div>
<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery.idletimeout.js')}"></script>
<script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery.idletimer.js')}"></script>
<script type="text/javascript" src="${resource(dir:'js', file:'sessiontimeout.js')}"></script>
<script type="text/javascript" charset="utf-8">   
    var mouse_inside_options_div = false;
    jQuery(document).ready(function() {
        jQuery("#utilities-div-link").click(function(){
            jQuery("#utilities-dropdown").toggle();
        });
    
        //used to hide the options div when the mouse is clicked outside of it
        jQuery('#utilities-holder').hover(function(){ 
	        mouse_inside_options_div=true; 
	    }, function(){ 
	        mouse_inside_options_div=false; 
	    });
	    jQuery(document).bind('click', function(e) { 
	        if(!mouse_inside_options_div ){
	            jQuery('#utilities-dropdown').hide();
	        }
	    });
	
	    //Layout hacks for IE
	    if (jQuery.browser.msie && jQuery.browser.version.substr(0,1)<9) {
	        //the datasetExplorer page uses a different layout, so it does not need the hack
	        //note: this is only in IE
	        <g:if test="${'datasetExplorer'!=app && 'sampleexplorer'!=app}">
	            jQuery("#header-div").css({
	                "position":"absolute"
	            });
	        </g:if>
	    }
	    var logoutURL = "${createLink([controller:'logout'])}";
	    var heartbeatURL = "${createLink([controller:'userLanding', action:'checkHeartBeat'])}";
	    addTimeoutDialog(heartbeatURL, logoutURL);
   });
   function popupWindow(mylink, windowname)
   {
       jQuery('#utilities-dropdown').hide();
      var w = window.open(mylink, windowname,'width=800,height=600,scrollbars=yes,resizable=yes');
      w.focus();
   }
</script>
