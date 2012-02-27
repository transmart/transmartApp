<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="admin" />
		<title>Edit Role</title>
	</head>
	
	<body>
		<div class="body">
			<h1>Edit Role</h1>
			<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${authority}">
			<div class="errors">
				<g:renderErrors bean="${authority}" as="list" />
			</div>
			</g:hasErrors>
			<div class="prop">
				<span class="name">ID:</span>
				<span class="value">${authority.id}</span>
			</div>
			<g:form>
				<input type="hidden" name="id" value="${authority.id}" />
				<input type="hidden" name="version" value="${authority.version}" />
				<div class="dialog">
				<table>
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><label for="authority">Role Name:</label></td>
						<td valign="top" class="value ${hasErrors(bean:authority,field:'authority','errors')}">
							<input type="text" id="authority" name="authority" value="${authority.authority?.encodeAsHTML()}"/>
						</td>
					</tr>
	
					<tr class="prop">
						<td valign="top" class="name"><label for="description">Description:</label></td>
						<td valign="top" class="value ${hasErrors(bean:authority,field:'description','errors')}">
							<input type="text" id="description" name="description" value="${authority.description?.encodeAsHTML()}"/>
						</td>
					</tr>
	
					<tr class="prop">
						<td valign="top" class="name"><label for="people">People:</label></td>
						<td valign="top" class="value ${hasErrors(bean:authority,field:'people','errors')}">
							<ul>
							<g:each var="p" in="${authority.people?}">
								<li>${p}</li>
							</g:each>
							</ul>
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
