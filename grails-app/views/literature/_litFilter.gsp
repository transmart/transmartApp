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

<div style="border:0px;overflow:auto;width:100%;height:100%">
<g:form controller="literature">
<table border="0" cellspacing="0" cellpadding="0" class="jubfilter">
	<tr>
		<th colspan="4">
			<g:actionSubmit	class="search" action="filterJubilant" value="Filter Results" />&nbsp;
		</th>
	</tr>
	<tr style="vertical-align:middle">
		<td class="title">Disease:</td>
		<td colspan="3" class="selection">
			<g:select from="${disease}" name="bioDiseaseId" optionKey="id" optionValue="preferredName" value="${session.searchFilter.litFilter.bioDiseaseId}" noSelection="['':'-- Any --']"/>
		</td>
	</tr>
	<tr style="vertical-align:top">
		<td class="title" style="vertical-align:top;">Disease Site:</td>
		<td class="selection" colspan="3">
			<g:select from="${diseaseSite}" name="diseaseSite" value="${session.searchFilter.litFilter.diseaseSite}" noSelection="['':'All']" multiple="multiple" size="5" />
		</td>
	</tr>
	<tr style="vertical-align:middle">
		<td class="title">Gene:</td>
		<td colspan="3" class="selection">
			<g:select from="${component}" name="componentList" value="${session.searchFilter.litFilter.componentList}" noSelection="['':'All']" multiple="multiple" size="5"/>
		</td>
	</tr>
	<tr>
		<td colspan="4" class="titlebar">Jubilant Alteration Filters</td>
	</tr>
	<tr style="vertical-align:middle">
		<td class="title">Mutation Type:</td>
		<td class="selection"><g:select from="${mutationType}" name="mutationType" value="${session.searchFilter.litFilter.mutationType}" noSelection="['':'-- Any --']" /></td>
		<td class="title">Region:</td>
		<td class="selection" width="100%"><g:select from="${mutationSite}" name="mutationSite" value="${session.searchFilter.litFilter.mutationSite}" noSelection="['':'-- Any --']" /></td>
	</tr>
	<tr style="vertical-align:middle">
		<td class="title">Epigenetic&nbsp;Type:&nbsp;</td>
		<td class="selection"><g:select from="${epigeneticType}" name="epigeneticType" value="${session.searchFilter.litFilter.epigeneticType}" noSelection="['':'-- Any --']" /></td>
		<td class="title">Region:</td>
		<td class="selection"><g:select from="${epigeneticRegion}" name="epigeneticRegion" value="${session.searchFilter.litFilter.epigeneticRegion}" noSelection="['':'-- Any --']" /></td>
	</tr>
	<tr style="vertical-align:top">
		<td class="title" style="vertical-align:top;">Alteration&nbsp;Type:&nbsp;</td>
		<td colspan="3" class="selction">
		<g:each in="${session.searchFilter.litFilter.alterationTypes.keySet()}" status ="i" var="alterationType">
			<g:checkBox name="alterationtype_${alterationType.toLowerCase().replace(' ', '_')}"	value="${session.searchFilter.litFilter.alterationTypes.get(alterationType)}" />
			${alterationType}<br />
		</g:each>
		</td>
	</tr>
	<tr style="vertical-align:middle">
		<td class="title">Molecule Type:</td>
		<td colspan="3" class="selection"><g:select from="${moleculeType}" name="moleculeType" value="${session.searchFilter.litFilter.moleculeType}" noSelection="['':'-- Any --']" /></td>
	</tr>
	<tr style="vertical-align:middle">
		<td class="title">Regulation:</td>
		<td colspan="3" class="selection">
			<select class="jubselect" name="regulation" id="regulation">
				<option value="">-- Any --</option>
				<option value="Expression" ${session.searchFilter.litFilter.regulation == 'Expression' ? "selected" : ""}>Expression</option>
				<option value="OverExpression" ${session.searchFilter.litFilter.regulation == 'OverExpression' ? "selected" : ""}>OverExpression</option>
			</select>
		</td>
	</tr>
	<tr style="vertical-align:middle">
		<td class="title">PTM Type:</td>
		<td class="selection"><g:select from="${ptmType}" name="ptmType" value="${session.searchFilter.litFilter.ptmType}" noSelection="['':'-- Any --']" /></td>
		<td class="title">Region:</td>
		<td class="selection"><g:select from="${ptmRegion}" name="ptmRegion" value="${session.searchFilter.litFilter.ptmRegion}" noSelection="['':'-- Any --']" /></td>
	</tr>
	<tr>
		<td colspan="4" class="titlebar" colspan="4">Jubilant Interaction Filters</td>
	</tr>
	<tr style="vertical-align:middle">
		<td class="title">Source:</td>
		<td class="selection" colspan="3"><g:select from="${source}" name="source" value="${session.searchFilter.litFilter.source}" noSelection="['':'-- Any --']" /></td>
	</tr>
	<tr style="vertical-align:middle">
		<td class="title">Target:</td>
		<td class="selection" colspan="3"><g:select from="${target}" name="target" value="${session.searchFilter.litFilter.target}" noSelection="['':'-- Any --']" /></td>
	</tr>
	<tr style="vertical-align:middle">
		<td class="title">Model:</td>
		<td><g:select from="${experimentalModel}" name="experimentalModel" value="${session.searchFilter.litFilter.experimentalModel}" noSelection="['':'-- Any --']" /></td>
		<td class="title">Mechanism:</td>
		<td class="selection"><g:select from="${mechanism}" name="mechanism" value="${session.searchFilter.litFilter.mechanism}" noSelection="['':'-- Any --']" /></td>
	</tr>
	<tr>
		<td colspan="4" class="titlebar">Jubilant Inhibitor Filters</td>
	</tr>
	<tr>
		<td class="title">Study Type:</td>
		<td class="selection"><g:select from="${trialType}" name="trialType" value="${session.searchFilter.litFilter.trialType}" noSelection="['':'-- Any --']" /></td>
		<td class="title">Trial&nbsp;Phase:</td>
		<td class="selection"><g:select from="${trialPhase}" name="trialPhase" value="${session.searchFilter.litFilter.trialPhase}" noSelection="['':'-- Any --']" /></td>
	</tr> 
	<tr style="vertical-align:middle">
		<td class="title">Inhibitor Name:</td>
		<td class="selection" colspan="3"><g:select from="${inhibitorName}" name="inhibitorName" value="${session.searchFilter.litFilter.inhibitorName}" noSelection="['':'-- Any --']" /></td>
	</tr>
	<tr style="vertical-align:middle">
		<td class="title">Model:</td>
		<td class="selection" colspan="3"><g:select from="${trialExperimentalModel}" name="trialExperimentalModel" value="${session.searchFilter.litFilter.trialExperimentalModel}" noSelection="['':'-- Any --']" /></td>
	</tr>
</table>	
</g:form>
</div>