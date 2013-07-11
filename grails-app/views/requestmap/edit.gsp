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
		<title>Edit Requestmap</title>
	</head>
	<body>
		<div class="body">
			<h1>Edit Requestmap</h1>
			<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${requestmap}">
			<div class="errors">
			<g:renderErrors bean="${requestmap}" as="list" />
			</div>
			</g:hasErrors>
	
			<div class="prop">
				<span class="name">ID:</span>
				<span class="value">${requestmap.id}</span>
			</div>
	
			<g:form>
				<input type="hidden" name="id" value="${requestmap.id}" />
				<input type="hidden" name="version" value="${requestmap.version}" />
				<div class="dialog">
					<table>
					<tbody>
	
						<tr class="prop">
							<td valign="top" class="name"><label for="url">URL Pattern:</label></td>
							<td valign="top" class="value ${hasErrors(bean:requestmap,field:'url','errors')}">
								<input type="text" id="url" name="url" value="${requestmap.url?.encodeAsHTML()}"/>
							</td>
						</tr>
	
						<tr class="prop">
							<td valign="top" class="name"><label for="configAttribute">Roles (comma-delimited):</label></td>
							<td valign="top" class="value ${hasErrors(bean:requestmap,field:'configAttribute','errors')}">
								<input type="text" name='configAttribute'  value="${requestmap.configAttribute}"/>
							</td>
						</tr>
	
					</tbody>
					</table>
				</div>
	
				<div class="buttons">
					<span class="button"><g:actionSubmit class="save" value="Update" /></span>
					<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
				</div>
	
			</g:form>
	
		</div>
	</body>
</html>
