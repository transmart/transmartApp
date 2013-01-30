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
		<meta name="layout" content="admin" />
		<title>Show Role</title>
	</head>

	<body>
		<div class="body">
			<h1>Show Role</h1>
			<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
			</g:if>
			<div class="dialog">
				<table>
				<tbody>

					<tr class="prop">
						<td valign="top" class="name">ID:</td>
						<td valign="top" class="value">${authority.id}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">Role Name:</td>
						<td valign="top" class="value">${authority.authority}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">Description:</td>
						<td valign="top" class="value">${authority.description}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">People:</td>
						<td valign="top" class="value">&nbsp;</td>

					</tr>
					<tr class="prop">
					<td colspan="2">

				<table>
				<thead>
					<tr>
						<g:sortableColumn property="id" title="WWID" />
						<g:sortableColumn property="username" title="Login Name" />
						<g:sortableColumn property="userRealName" title="Full Name" />
						<g:sortableColumn property="enabled" title="Enabled" />
						<g:sortableColumn property="description" title="Description" />
						<th>&nbsp;</th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${authority.people}" status="i" var="person">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td>${person.id}</td>
						<td>${person.username?.encodeAsHTML()}</td>
						<td>${person.userRealName?.encodeAsHTML()}</td>
						<td>${person.enabled?.encodeAsHTML()}</td>
						<td>${person.description?.encodeAsHTML()}</td>
						<td class="actionButtons">
							<span class="actionButton">
								<g:link action="show" id="${person.id}">Detail</g:link>
							</span>
						</td>
					</tr>
				</g:each>
				</tbody>
				</table>

					</td>
					</tr>

				</tbody>
				</table>
			</div>

			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" value="${authority?.id}" />
					<span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
					<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
				</g:form>
			</div>

		</div>

	</body>
</html>