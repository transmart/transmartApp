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
	<title><g:layoutTitle default="Dashboard" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'dashboard.css')}" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'nav_styles.css')}" />
    <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
    <script type="text/javascript" src="${resource(dir:'js', file:'toggle.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js', file:'prototype.js')}"></script>
        
    <g:layoutHead /> 

		<!-- ************************************** -->

                <!-- This implements the Help functionality -->
                <script language="javascript" src="/quality/js/D2H_ctxt.js">
                </script>
                <script language="javascript">
                           	 helpURL = "/quality/DemoOnlineHelpProject/NetHelp/default.htm";
                                document.onhelp = function()
                                {
                                     JavaScript:D2H_ShowHelp(0000,helpURL,"_self",CTXT_DISPLAY_FULLHELP ); 
                                     return false;
                                }
                </script>

		<!-- ************************************** -->




</head>

<body style="width:100%; margin: 0;">

<div id="page">
	<table style="width: 100%;" cellpadding=0 cellspacing=0>
		<tbody>
		<tr>
			<td><div id="header"><g:render template="/layouts/commonheader"	model="['app':'dashboard']" /></div></td>
		</tr>
		
		<tr>
			<td><div id="content" style="padding-left: 10px;"><br/><g:layoutBody /></div></td>
		</tr>
		
		<tr>
			<td><div id="footer" style="width:99%;"><br/><g:render template="/layouts/footer" /></div></td>
		</tr>		
		</tbody>	
	</table>
</div>

</body>
</html>