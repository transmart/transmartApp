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


<div class="gtb1">
<table class="detail">
	<tbody>
		<tr class="prop">
			<td valign="top" class="name">Analysis:</td>
			<td valign="top" class="value">${fieldValue(bean:geneExprAnalysis, field:'contentID')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">Gene:</td>
			<td valign="top" class="value">${fieldValue(bean:geneExprAnalysis, field:'geneSymbol')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">GenBank Accession:</td>
			<td valign="top" class="value">${fieldValue(bean:geneExprAnalysis, field:'genBankAccession')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">Description:</td>
			<td valign="top" class="value">${fieldValue(bean:geneExprAnalysis, field:'description')}</td>
		</tr>

		<g:if test="${geneExprAnalysis.probeSet!=null }">
			<tr class="prop">
				<td valign="top" class="name">Probe Set:</td>
				<td valign="top" class="value">${fieldValue(bean:geneExprAnalysis, field:'probeSet')}</td>
			</tr>
		</g:if>
		<tr class="prop">
			<td valign="top" class="name">Fold Change:</td>
			<td valign="top" class="value">${fieldValue(bean:geneExprAnalysis, field:'ratio')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">Rho Value:</td>
			<td valign="top" class="value">${fieldValue(bean:geneExprAnalysis, field:'rhovalue')}</td>

		</tr>
	</tbody>
</table>
</div>