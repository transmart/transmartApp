<html>
	<head>
	    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	    <meta name="layout" content="admin" />
	    <title>Create Import Xnat Coupling</title>         
	</head>
	<body>
		<div class="body">
			<h1>Create Import Xnat Coupling</h1>
			<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${importXnatVariable}">
			<div class="errors">
				<g:renderErrors bean="${importXnatVariable}" as="list" />
			</div>
			</g:hasErrors>
			<g:hasErrors bean="${importXnatConfiguration}">
			<div class="errors">
				<g:renderErrors bean="${importXnatConfiguration}" as="list" />
			</div>
			</g:hasErrors>
			<h2>List of variables</h2>
			<div class="list">
				<table>
				<thead>
					<tr>
						<g:sortableColumn property="id" title="ID" />
						<g:sortableColumn property="name" title="Name" />
						<g:sortableColumn property="datatype" title="DataType" />
						<g:sortableColumn property="url" title="XNAT (REST) URL" />
						<th>&nbsp;</th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${importXnatVariableList}" status="i" var="variable">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td>${variable.id}</td>
						<td>${variable.name}</td>
						<td>${variable.datatype}</td>
						<td>${variable.url}</td>
						<td class="actionButtons">
							<span class="actionButton">
								<g:link action="delete_coupling" id="${variable.id}" params="[configId: importXnatConfiguration.id]" onclick="return confirm('Are you sure?');">Delete</g:link>
							</span>
						</td>
					</tr>
				</g:each>
				</tbody>
				</table>
			</div>
 			<g:form action="downloadXml">
				<g:hiddenField name="id" value="${importXnatConfiguration.id}" />
				<div class="buttons">
					<span class="button"><input type="submit" class="save" value="Download as XML" /></span>
				</div>
			</g:form>

			<hr style="margin: 20 0 20 0px;"/>

			<h2>Add variable</h2>
 			<g:form action="save_coupling">
				<g:hiddenField name="id" value="${importXnatConfiguration.id}" />
				<div class="dialog">
					<table>
					<tbody>
	
						<tr class="prop">
							<td valign="top" class="name">
								<label for="name">Name:</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:importXnatVariable, field:'name','errors')}">
								<input type="text" id="name" name="name" value="${fieldValue(bean:importXnatVariable, field:'name')}"/>
							</td>
						</tr>
	
						<tr class="prop">
							<td valign="top" class="name">
								<label for="datatype">Datatype:</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:importXnatVariable,field:'datatype','errors')}">
								<g:select id="datatype" name="datatype" from="${['Subject', 'Session', 'SubjectVariable', 'SessionVariable']}" value="${fieldValue(bean:importXnatVariable, field='datatype')}" />
							</td>
						</tr>

						<tr class="prop">
							<td valign="top" class="name">
								<label for="url">XNAT (REST) URL:</label>
							</td>
							<td valign="top" class="value ${hasErrors(bean:importXnatVariable,field:'url','errors')}">
								<input type="text" id="url" name="url" value="${fieldValue(bean:importXnatVariable,field:'url')}"/>
							</td>
						</tr>	
						
					</tbody>
					</table>
				</div>
	
				<div class="buttons">
					<span class="button"><input class="save" type="submit" value="Create" /></span>
				</div>
			</g:form>

			<hr style="margin: 20 0 20 0px;"/>

			<h2>Help</h2>
			<br />The screen manages the coupling of TranSMART nodes with the XNAT paths. Below is a short description per column:
			<ul style="list-style-type:disc;padding-left:20px;">
				<li>Name: the TranSMART nodename in its data structure.</li>
				<li>Datatype: the XNAT accession type:
				<ul style="list-style-type:disc;padding-left:20px;">
						<li>Subject: data stored on the patient level and it is part of the datatype.</li>
						<li>Session: data stored on the session (experiment) level and it is part of the datatype.</li>
						<li>SubjectVariable: data stored on the patient level and it is a custom variable.</li>
						<li>SessionVariable: data stored on the session (experiment) level and it is a custom variable.</li>
					</ul>
				</li>
				<li>
					XNAT (REST) URL: the REST api link to the attribute in XNAT. All possible links can be found here: <a target="_blank" href="https://wiki.xnat.org/display/XNAT/XNAT+REST+XML+Path+Shortcuts">XNAT REST XML Path Shortcuts</a><br />
					Note that although an attribute in XNAT can be shown at the session level for convenience, it could be stored at the subject level. By looking at the links above each table in the REST api, it can be determined if it is subject or session data. 
				</li>
			</ul>
		</div>
	</body>
</html>
