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