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
		<link rel="stylesheet" type="text/css" href="${resource(dir:'js/ext/resources/css', file:'ext-all.css')}">
	<link rel="stylesheet" type="text/css" href="${resource(dir:'js/ext/resources/css', file:'xtheme-gray.css')}">
	<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'datasetExplorer.css')}">

</head>
<body>
<g:if test="${clinicalTrial == null}">
	<table class="detail">
		<tr>
			<td>Study not found.</td>
		</tr>
		</g:if>
		<g:else>
			<table class="detail" style="width: 515px;">
				<tbody>
					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.trialnumber" default="Trial number" />:</td>
						<td valign="top" class="value">
							${fieldValue(bean:clinicalTrial, field:'trialNumber')} <g:if
								test="${clinicalTrial?.files.size() > 0}">
								<g:set var="fcount" value="${0}" />
								<g:each in="${clinicalTrial.files}" var="file">
									<g:if test="${file.content.type=='Experiment Web Link'}">
										<g:set var="fcount" value="${fcount++}" />
										<g:if test="${fcount > 1}">, </g:if>
										<g:createFileLink content="${file.content}"
											displayLabel="${file.content.repository.repositoryType}" />
									</g:if>
									<g:elseif
										test="${file.content.type=='Dataset Explorer Node Link'&&search==1}">
										<g:link controller="datasetExplorer" action="index"
											params="[path:file.content.location]">Dataset Explorer<img
												src="${resource(dir:'images', file:'internal-link.gif')}" />
										</g:link>
									</g:elseif>
								</g:each>
							</g:if> <g:if test="${searchId!=null}">
					| <g:link controller="search" action="newSearch" id="${searchId}">Search analyzed Data <img
										src="${resource(dir:'images', file:'internal-link.gif')}" />
								</g:link>
							</g:if></td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><g:message code="clinicalTrial.name"
								default="Name" />:</td>
						<td valign="top" class="value">
							${fieldValue(bean:clinicalTrial, field:'title')}
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.completiondate" default="Date" />:</td>
						<g:if
							test="${fieldValue(bean:clinicalTrial, field:'completionDate')?.length() > 10}">
							<td valign="top" class="value">
								${fieldValue(bean:clinicalTrial, field:'completionDate').substring(0,11)}
							</td>
						</g:if>
						<g:else>
							<td valign="top" class="value">&nbsp;</td>
						</g:else>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><g:message code="clinicalTrial.owner"
								default="Owner" />:</td>
						<g:if test="${clinicalTrial.type=='Clinical Trial'}">
							<td valign="top" class="value">
								${fieldValue(bean:clinicalTrial, field:'studyOwner')}
							</td>
						</g:if>
						<g:else>
							<td valign="top" class="value">
								${fieldValue(bean:clinicalTrial, field:'primaryInvestigator')}
							</td>
						</g:else>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.institution" default="Institution" />:</td>
						<td valign="top" class="value">
							${fieldValue(bean:clinicalTrial, field:'institution')}
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><g:message code="clinicalTrial.country"
								default="Country" />:</td>
						<td valign="top" class="value">
							${fieldValue(bean:clinicalTrial, field:'country')}
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.publication" default="Related Publication(s)" />:</td>
						<td valign="top" class="value">
							<g:if
								test="${clinicalTrial?.files.size() > 0}">
								<g:set var="fcount" value="${0}" />
								<g:each in="${clinicalTrial.files}" var="file">
									<g:if test="${file.content.type=='Publication Web Link'}">
										<g:set var="fcount" value="${fcount++}" />
										<g:if test="${fcount > 1}">, </g:if>
										<g:createFileLink content="${file.content}"
											displayLabel="${file.content.repository.repositoryType}" />
									</g:if>								
								</g:each>
								</g:if>
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.description" default="Description" />:</td>
						<td valign="top" class="value">
							${fieldValue(bean:clinicalTrial, field:'description')}
						</td>
					</tr>
						<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.accessType" default="Access Type" />:</td>
						<td valign="top" class="value">
							${fieldValue(bean:clinicalTrial, field:'accessType')}
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.clinicalTrialphase" default="Phase" />:</td>
						<td valign="top" class="value">
							${fieldValue(bean:clinicalTrial, field:'studyPhase')}
						</td>
					</tr>								
					<tr class="prop">
						<td valign="top" class="name"><g:message code="clinicalTrial.design"
								default="Objective" />:</td>
						<td valign="top" class="value">
							${fieldValue(bean:clinicalTrial, field:'design')}
						</td>
					</tr>
						<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.bioMarkerType" default="BioMarker Type" />:</td>
						<td valign="top" class="value">
							${fieldValue(bean:clinicalTrial, field:'bioMarkerType')}
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.compound" default="Compound" />:</td>
						<td valign="top" class="value"><g:each
								in="${clinicalTrial.compounds}" var="compound">
								<g:createFilterDetailsLink id="${compound?.id}"
									label="${compound?.genericName}" type="compound" />
								<br>
							</g:each></td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.studydesign" default="Design Factors" />:</td>
						<td valign="top" class="value">
							${fieldValue(bean:clinicalTrial, field:'overallDesign')}
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.numberofpatients"
								default="Number of Followed Subjects" />:</td>
						<td valign="top" class="value">
							${fieldValue(bean:clinicalTrial, field:'numberOfPatients')}
						</td>
					</tr>
					<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.organism" default="Organism(s)" />:</td>
						<td valign="top" style="text-align: left;" class="value"><g:each
								var="og" in="${clinicalTrial.organisms}">
								${og?.label.encodeAsHTML()}<br>
							</g:each></td>
					</tr>
						<tr class="prop">
						<td valign="top" class="name"><g:message
								code="clinicalTrial.target" default="Target/Pathways" />:</td>
						<td valign="top" class="value">
							${fieldValue(bean:clinicalTrial, field:'target')}
						</td>
					</tr>
				</tbody>
			</table>
		</g:else>
</body>
</html>