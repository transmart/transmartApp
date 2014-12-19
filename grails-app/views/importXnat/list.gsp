<%@ page import="org.transmart.searchapp.ImportXnatConfiguration"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta name="layout" content="admin" />
		<title>Import XNAT Configuration List</title>
	</head>	
	<body>
		<div class="body">
			<h1>Import XNAT Configuration List</h1>
			<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
			</g:if>
			<div class="list">
				<table>
				<thead>
					<tr>
						<g:sortableColumn property="id" title="ID" />
						<g:sortableColumn property="name" title="Name" />
						<g:sortableColumn property="description" title="Description" />
						<th>&nbsp;</th>
					</tr>
				</thead>
				<tbody>
				<g:each in="${importXnatConfigurationList}" status="i" var="config">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td>${config.id}</td>
						<td>${config.name}</td>
						<td>${config.description}</td>
						<td class="actionButtons">
							<span class="actionButton">
							<g:link action="show" id="${config.id}">Show</g:link>
							</span>
						</td>
					</tr>
					</g:each>
				</tbody>
				</table>
			</div>
	
			<div class="paginateButtons">
				<g:paginate total="${ImportXnatConfiguration.count()}" />
			</div>
	
		</div>
	</body>
</html>
