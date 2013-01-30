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
<%@ page import="org.transmart.searchapp.Role"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="admin" />
		<title>Role List</title>
	</head>
	
	<body>
		<div class="body">
			<h1>Role List</h1>
			<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
			</g:if>
			<div class="list">
				<table>
				<thead>
					<tr>
						<g:sortableColumn property="id" title="ID" />
						<g:sortableColumn property="authority" title="Role Name" />
						<g:sortableColumn property="description" title="Description" />
						<th>&nbsp;</th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${authorityList}" status="i" var="authority">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td>${authority.id}</td>
						<td>${authority.authority?.encodeAsHTML()}</td>
						<td>${authority.description?.encodeAsHTML()}</td>
						<td class="actionButtons">
							<span class="actionButton">
								<g:link action="show" id="${authority.id}">Show</g:link>
							</span>
						</td>
					</tr>
				</g:each>
				</tbody>
				</table>
			</div>
	
			<div class="paginateButtons">
				<g:paginate total="${Role.count()}" />
			</div>
		</div>
	</body>
</html>