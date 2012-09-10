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
<%@ page import="org.transmart.searchapp.AuthUserSecureAccess"%>
<%@ page import="org.transmart.searchapp.SecureObjectAccess"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="admin" />
		<title>User</title>
	</head>

	<body>
		<div class="body">
			<h1>User</h1>
			<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
			</g:if>
			<div class="dialog">
				<table>
				<tbody>

					<tr class="prop">
						<td valign="top" class="name">WWID:</td>
						<td valign="top" class="value">${person.id}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">Login Name:</td>
						<td valign="top" class="value">${person.username?.encodeAsHTML()}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">Full Name:</td>
						<td valign="top" class="value">${person.userRealName?.encodeAsHTML()}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">Enabled:</td>
						<td valign="top" class="value">${person.enabled}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">Description:</td>
						<td valign="top" class="value">${person.description?.encodeAsHTML()}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">Email:</td>
						<td valign="top" class="value">${person.email?.encodeAsHTML()}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">Show Email:</td>
						<td valign="top" class="value">${person.emailShow}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">Roles:</td>
						<td valign="top" class="value">
							<ul>
							<g:each in="${roleNames}" var='name'>
								<li>${name}</li>
							</g:each>
							</ul>
						</td>
					</tr>
							<tr class="prop">
						<td valign="top" class="name">Groups:</td>
						<td valign="top" class="value">
							<ul>
							<g:each in="${person.groups}" var='group'>
								<li><g:link controller="userGroup" action="show" id="${group.id}">${group.name}</g:link></li>
							</g:each>
							</ul>
						</td>
					</tr>
						<tr class="prop">
						<td valign="top" class="name">Studies Assigned:</td>
						<td valign="top" class="value">
							<ul>
							<g:each in="${SecureObjectAccess.findAllByPrincipal(person,[sort:accessLevel])}" var='soa'>
								<li>${soa.getObjectAccessName()}</li>
							</g:each>
							</ul>
						</td>
					</tr>
					</tr>
						<tr class="prop">
						<td valign="top" class="name">Studies with Access(via groups):</td>
						<td valign="top" class="value">
							<ul>
							<g:each in="${AuthUserSecureAccess.findAllByAuthUser(person,[sort:accessLevel])}" var='soa'>
								<li><g:link controller="secureObject" action="show" id="${soa.secureObject.id}"> ${soa.getObjectAccessName()}</g:link></li>
							</g:each>
							</ul>
						</td>
					</tr>
				</tbody>
				</table>
			</div>

			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" value="${person.id}" />
					<span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
					<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
				</g:form>
			</div>

		</div>
	</body>
</html>