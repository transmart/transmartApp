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

<table class="detail" style="width: 515px;">
	<tbody>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Title:</td>
			<td valign="top" class="value">${fieldValue(bean:analysis, field:'shortDescription')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Analysis Description:</td>
			<td valign="top" class="value">${fieldValue(bean:analysis, field:'longDescription')}</td>
		</tr>
		<g:if test='${"comparison".equals(analysis.analysisMethodCode)}'>
			<tr class="prop">
				<td valign="top" class="name" style="text-align: right">p-Value	Cut Off:</td>
				<td valign="top" class="value">${fieldValue(bean:analysis, field:'pValueCutoff')}</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name" style="text-align: right">Fold Change Cut Off:</td>
				<td valign="top" class="value">${fieldValue(bean:analysis, field:'foldChangeCutoff')}</td>
			</tr>
		</g:if>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">QA Criteria:</td>
			<td valign="top" class="value">${fieldValue(bean:analysis, field:'qaCriteria')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Analysis Platform:</td>
			<td valign="top" class="value">${fieldValue(bean:analysis, field:'analysisPlatform.platformName')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Method:</td>
			<td valign="top" class="value">${fieldValue(bean:analysis, field:'analysisMethodCode')}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right">Data type:</td>
			<td valign="top" class="value">${fieldValue(bean:analysis, field:'assayDataType')}</td>
		</tr>
	</tbody>
</table>