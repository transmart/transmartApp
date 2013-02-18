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

	<!-- 
		<script type="text/javascript" src="${createLinkTo(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
		<script type="text/javascript" src="${createLinkTo(dir:'js', file:'ext/ext-all.js')}"></script>
	 -->
	<script type="text/javascript" language="javascript"> 

	  function extractCookie(result, request) {
			 alert("result is " + result);
			 parent.gpCookie = result;
	  }

	  function submitFormDelayed() {
		setTimeout("submitForm()", 50);
	  }

	  function submitForm() {
		document.forms["loginForm"].submit();
 
 		//var gpurl = "${grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL}/gp/pages/login.jsf";

		//alert("gp url: " + gpurl);
 
 		//Ext.Ajax.request({
 		//	url: gpurl,
 		//	params: { loginForm: 'loginForm',
 		//			  'javax.faces.ViewState': 'j_id1',
 		//			  username: 'biomart',
 		//			  'loginForm:signIn': 'Sign in' },
 		//	method: "POST",
 		//	success: extractCookie,
 		//	failure: extractCookie
 		//});
 
	  }

	</script>
	
	</head>
	<body onload="submitForm();">

		<!-- load an image to force browser to get a session cookie... -->
 		  <img src="${grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL}/gp/images/GP-logo.gif" alt="GenePattern" height="48" style="border: 0;" width="229" />
		<form id="loginForm" name="loginForm" method="get" action="${grailsApplication.config.com.recomdata.datasetExplorer.genePatternURL}/gp/pages/login.jsf" enctype="application/x-www-form-urlencoded">
			<input type="hidden" name="loginForm" value="loginForm" />
			<input type="hidden" name="javax.faces.ViewState" id="javax.faces.ViewState" value="j_id1" />
			<input type="hidden" id="username" name="username" type="text" value="${userName}"/>
			<input type="hidden" id="loginForm:signIn" type="text" name="loginForm:signIn" value="Sign in" />
			<input id="loginForm:signIn" type="submit" name="loginForm:signIn" value="Sign in" />
			<br />
		</form>
				
	</body>
	
	
	
</html>