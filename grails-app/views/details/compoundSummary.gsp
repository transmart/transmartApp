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
			${createPropertyTableRow(width:'20%', label:'Number', value:compound.number)}
		</tbody>
	</table>
	<br />
</g:else>

</div>
</body>
</html>