<html>
<head>
	<title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
	<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
</head>
<body>

<div id="summary">

<g:if test="${pathway == null}">

	<table class="snoborder" width="100%">
		<tbody>
			<tr width="100%">
				<td width="100%" style="text-align: center; font-size: 14px; font-weight: bold">
					No summary data available
				</td>
			</tr>
		</tbody>
	</table>
</g:if>
<g:else>
	<p class="Title">
		<span class="Title">
			${pathway.name}
		</span>
	</p>
	<div id="SummaryHeader">
		<span class="SummaryHeader">Summary</span>
	</div>
	<table class="SummaryTable" width="100%">
		${createPropertyTableRow(width:'20%', label:'Name', value:pathway.name)}
		${createPropertyTableRow(width:'20%', label:'Description', value:pathway.description)}
		${createPropertyTableRow(width:'20%', label:'Organism', value:pathway.organism)}
		${createPropertyTableRow(width:'20%', label:'Primary Source', value:pathway.primarySourceCode)}
		${createPropertyTableRow(width:'20%', label:'Primary External ID', value:pathway.primaryExternalId)}
		<tr>
			<td width="20%"><b>Genes:</b></td>
			<td>
				<g:each in="${genes}" status ="i" var="gene">
					<nobr>${gene.keyword}<g:if test="${i < genes.size() - 1}">,</g:if></nobr>
				</g:each>
			</td>
		</tr>
	</table>
	<br />
</g:else>

</div>
</body>
</html>