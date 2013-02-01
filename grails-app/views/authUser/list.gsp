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
<%@page import="org.transmart.searchapp.AuthUser"%>

<html>
	<head>
       	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="admin" />
		<title>AuthUser List</title>
		
		<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.dataTables.js')}"></script>
       	<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'jquery.dataTables.css')}">
		
		
        <script type="text/javascript" charset="utf-8">

	        jQuery(document).ready(function() {

	        	 jQuery("#userTable").dataTable({
	    	        	 "iDisplayLength": 50,
	    	        	 "aLengthMenu": [[25, 50,100, -1], [25, 50,100, "All"]],
	    	        	 "sPaginationType": "full_numbers",
	    	        	 "bStateSave": true
	        	 });
	
	        });

        </script>

	</head>

	<body>
		<div class="body">
			<h1>AuthUser List</h1>
			<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
			</g:if>
			<div class="list" style="margin-top:15px">
				<table id = 'userTable'>
				<thead>
					<tr>
						<th>WWID</th>
						<th>User Name</th>
						<th>Full Name</th>
						<th>Enabled</th>
						<th>Description</th>
						<th>&nbsp;</th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${personList}" var="person">
					<tr>
						<td>${person.id}&nbsp&nbsp&nbsp&nbsp</td>
						<td>${person.username?.encodeAsHTML()}&nbsp&nbsp&nbsp&nbsp</td>
						<td>${person.userRealName?.encodeAsHTML()}&nbsp&nbsp&nbsp&nbsp</td>
						<td>${person.enabled?.encodeAsHTML()}</td>
						<td>${person.description?.encodeAsHTML()}&nbsp&nbsp&nbsp&nbsp</td>
						<td class="actionButtons">
							<span class="actionButton">
								<g:link action="show" id="${person.id}">Show</g:link>
							</span>
						</td>
					</tr>
				</g:each>
				</tbody>
				</table>
			</div>

		</div>
	</body>
</html>