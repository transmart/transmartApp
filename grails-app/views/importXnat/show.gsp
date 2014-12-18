<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="admin" />
		<title>Show Import XNAT Configuration</title>
	</head>
	
	<body>
		<div class="body">
			<h1>Show Import XNAT Configuration</h1>
			<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
			</g:if>
			<div class="dialog">
				<table>
				<tbody>
	
					<tr class="prop">
						<td valign="top" class="name">ID:</td>
						<td valign="top" class="value">${importXnatConfiguration.id}</td>
					</tr>
	
					<tr class="prop">
						<td valign="top" class="name">Name:</td>
						<td valign="top" class="value">${importXnatConfiguration.name}</td>
					</tr>
	
					<tr class="prop">
						<td valign="top" class="name">Description:</td>
						<td valign="top" class="value">${importXnatConfiguration.description}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">XNAT Web Address (url/ip):</td>
						<td valign="top" class="value">${importXnatConfiguration.url}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">Username:</td>
						<td valign="top" class="value">${importXnatConfiguration.username}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">XNAT Projectname (id):</td>
						<td valign="top" class="value">${importXnatConfiguration.project}</td>
					</tr>

					<tr class="prop">
						<td valign="top" class="name">TranSMART Nodename (under Public Series):</td>
						<td valign="top" class="value">${importXnatConfiguration.node}</td>
					</tr>
	
				</tbody>
				</table>
			</div>
	
			<div class="buttons">
				<g:form>
					<input type="hidden" name="id" value="${importXnatConfiguration.id}" />
					<span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
					<span class="button"><input type="submit" name="_action_Create_coupling" value="Edit coupling" class="edit" /></span>
					<span class="button"><input type="submit" name="_action_Import_wizard" value="Import data" class="save" /></span>
					<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
				</g:form>
			</div>
	
		</div>
	</body>
</html>
