<html>
<head>
	<title>Cortellis API: ${res["@name"]}</title>
	<link rel="stylesheet" href="${resource(dir:'css', file:'main.css')}">
	<link rel="stylesheet" href="${resource(dir:'css', file:'cortellis.css')}">
</head>
<body>
<h1>Cortellis returned ${res["@totalResults"]} relevant target records:</h1>
<table>
	<thead>
		<tr><th>Name</th><th>Description</th></tr>
	</thead>
	<tbody>
		<g:set var="counter" value="${1}" />
		<g:each in="${res.Targets.Target}" var="target">
			<tr class="${counter % 2 == 0 ? 'even' : 'odd'}">
				<td width="30%"><a href="${createLink(action:'showTarget', id:target['@id'])}">${target["@name"]}</a></td>
				<td>${target.Description}</td>
			</tr>
			<g:set var="counter" value="${counter + 1}" />
		</g:each>		
	</tbody>
</table>
</body>
</html>