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

<g:form controller="experimentAnalysis" action="filterResult">
<g:set var="expAnalysisFilter" value="${session.searchFilter.expAnalysisFilter}" />

<table class="jubfilter" style="width: 650px">
	<tr>
		<th colspan=2 style="align: right">
			<span class="button"><g:actionSubmit class="search" action="filterResult" value="Filter Results" />&nbsp;</span>
		</th>
	</tr>
	<tr>
		<td colspan=2 style="border-right: 0px solid #ccc">
		<table class="jubfiltersection">
			<tr>
				<td style="width: 200px; white-space: nowrap;">Platform Species:</td>
				<td><g:select from="${platformOrganisms}" name="species" value="${expAnalysisFilter.species}" noSelection="['':'-- Any --']" /></td>
			</tr>
			<tr>
				<td style="width: 200px; white-space: nowrap;">Disease:</td>
				<td><g:select from="${diseases}" name="bioDiseaseId" optionKey="id"
								optionValue="preferredName" value="${expAnalysisFilter.bioDiseaseId}" noSelection="['':'-- Any --']" /></td>
			</tr>
			<tr>
				<td style="width: 200px; white-space: nowrap;">Compound:</td>
				<td><g:select from="${compounds}" name="bioCompoundId" optionKey="bioDataId" optionValue="keyword"
								value="${expAnalysisFilter.bioCompoundId}" noSelection="['':'-- Any --']" /></td>
			</tr>
			<tr>
				<td style="width: 200px; white-space: nowrap;">Experiment Design:</td>
				<td><g:select from="${expDesigns}" name="expDesign" value="${expAnalysisFilter.expDesign}" noSelection="['':'-- Any --']" /></td>
			</tr>
			<tr>
				<td style="width: 200px; white-space: nowrap;">Data Fold Change Cut Off:</td>
				<td style="font-weight:normal"><g:textField name="foldChange" value="${expAnalysisFilter.foldChange}" /> (Minimum Fold Change Ratio +/-1.0)</td>
			</tr>
			<tr>
				<td style="width: 200px; white-space: nowrap;">Data p Value Less Than:</td>
				<td style="font-weight:normal"><g:textField name="pValue" value="${expAnalysisFilter.pValue}" /> (Maximum P-Value 0.1)</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
</g:form>