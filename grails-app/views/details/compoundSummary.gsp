<html>
<head>
	<title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
	<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
</head>
<body>

<div id="summary">

<g:if test="${compound == null}">

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
			${compound.genericName} also known as ${compound.codeName}
		</span>
	</p>
	<div id="SummaryHeader">
		<span class="SummaryHeader">Summary</span>
	</div>
	<table class="SummaryTable" width="100%">
		<tbody>
			${createPropertyTableRow(width:'20%', label:'Generic Name', value:compound.genericName)}
			${createPropertyTableRow(width:'20%', label:'Code Name', value:compound.codeName)}
			${createPropertyTableRow(width:'20%', label:'Brand Name', value:compound.brandName)}
			${createPropertyTableRow(width:'20%', label:'Chemical Name', value:compound.chemicalName)}
			${createPropertyTableRow(width:'20%', label:'Description', value:compound.description)}
			${createPropertyTableRow(width:'20%', label:'Mechanism', value:compound.mechanism)}
			${createPropertyTableRow(width:'20%', label:'Product Category', value:compound.productCategory)}
			${createPropertyTableRow(width:'20%', label:'CAS Registry Number', value:compound.casRegistry)}
			${createPropertyTableRow(width:'20%', label:'CNTO Number', value:compound.cntoNumber)}
			${createPropertyTableRow(width:'20%', label:'Internal Number', value:compound.jnjNumber)}
		</tbody>
	</table>
	<br />
</g:else>

</div>
</body>
</html>