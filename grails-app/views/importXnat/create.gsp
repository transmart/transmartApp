<html>
	<head>
	    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	    <meta name="layout" content="admin" />
	    <title>Create Import Xnat Configuration</title>         
	</head>
	<body>
		<div class="body">
			<h1>Create Import Xnat Configuration</h1>
			<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${importXnatConfiguration}">
			<div class="errors">
				<g:renderErrors bean="${importXnatConfiguration}" as="list" />
			</div>
			</g:hasErrors>
			<g:form action="save">
				<div class="dialog">
					<table>
					<tbody>
	
						<tr class="prop">
							<td valign="top" class="name">
								<label for="name">Name:</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'name','errors')}">
								<input type="text" id="name" name="name" value="${fieldValue(bean:importXnatConfiguration,field:'name')}"/>
							</td>
						</tr>
	
						<tr class="prop">
							<td valign="top" class="name">
								<label for="description">Description:</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'description','errors')}">
								<textarea rows="5" cols="40" name="description">${fieldValue(bean:importXnatConfiguration, field='description')}</textarea>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name">
								<label for="url">XNAT Web Address (url/ip):</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'url','errors')}">
								<input type="text" id="url" name="url" value="${fieldValue(bean:importXnatConfiguration,field:'url')}"/>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name">
								<label for="username">Username:</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'username','errors')}">
								<input type="text" id="username" name="username" value="${fieldValue(bean:importXnatConfiguration,field:'username')}"/>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name">
								<label for="project">XNAT Projectname (id):</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'project','errors')}">
								<input type="text" id="project" name="project" value="${fieldValue(bean:importXnatConfiguration,field:'project')}"/>
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name">
								<label for="node">TranSMART Nodename (under Public Series):</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:importXnatConfiguration,field:'node','errors')}">
								<input type="text" id="node" name="node" value="${fieldValue(bean:importXnatConfiguration,field:'node')}"/>
							</td>
						</tr>
	
					</tbody>
					</table>
				</div>
	
				<div class="buttons">
					<span class="button"><input class="save" type="submit" value="Create" /></span>
				</div>
			</g:form>
		</div>
	</body>
</html>
