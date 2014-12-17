<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="admin" />
		<title>Edit Import XNAT Configuration</title>
	</head>
	<body>
		<div class="body">
			<h1>Edit Import XNAT Configuration</h1>
			<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${importXnatConfiguration}">
			<div class="errors">
			<g:renderErrors bean="${importXnatConfiguration}" as="list" />
			</div>
			</g:hasErrors>
	
			<div class="prop">
				<span class="name">ID:</span>
				<span class="value">${importXnatConfiguration.id}</span>
			</div>
	
			<g:form>
				<input type="hidden" name="id" value="${importXnatConfiguration.id}" />
				<input type="hidden" name="version" value="${importXnatConfiguration.version}" />
				<div class="dialog">
					<table>
					<tbody>
	
						<tr class="prop">
							<td valign="top" class="name"><label for="name">Name:</label></td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'name','errors')}">
								<input type="text" id="name" name="name" value="${importXnatConfiguration.name}"/>
							</td>
						</tr>
	
						<tr class="prop">
							<td valign="top" class="name"><label for="description">Description:</label></td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'description','errors')}">
								<input type="text" name='description'  value="${importXnatConfiguration.description}"/>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="url">XNAT Web Address (url/ip):</label></td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'url','errors')}">
								<input type="text" name='url'  value="${importXnatConfiguration.url}"/>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="username">Username:</label></td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'username','errors')}">
								<input type="text" name='username'  value="${importXnatConfiguration.username}"/>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="password">Password:</label></td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'password','errors')}">
								<input type="password" name='password'  value="${importXnatConfiguration.password}"/>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="project">XNAT Projectname (id):</label></td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'project','errors')}">
								<input type="text" name='project'  value="${importXnatConfiguration.project}"/>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name"><label for="node">Node:</label></td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'node','errors')}">
								<input type="text" name='node'  value="${importXnatConfiguration.node}"/>
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
