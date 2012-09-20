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

<g:if test="${!layout}">
	<i>No columns have been set up for the study view</i>
</g:if>
<table class="columndetail" style="width: 515px;">
	<tbody>
		<g:each in="${layout}" var="layoutRow">
			<tr class="columnprop">
				<td valign="top" class="columnname">${layoutRow.displayName}</td>
				<td valign="top" class="columnvalue">
					<g:if test="${layoutRow.dataType == 'date'}">
						<g:fieldDate bean="${experimentInstance}" field="${layoutRow.column}" format="yyyy-MM-dd"/>
					</g:if>
					
					<%-- Special cases --%>
					<g:elseif test="${layoutRow.dataType == 'special'}">
						<g:if test="${layoutRow.column == 'accession'}">
							${fieldValue(bean:experimentInstance,field:'accession')}
							<g:if test="${experimentInstance?.files.size() > 0}">
								<g:set var="fcount" value="${0}" />
								<g:each in="${experimentInstance.files}" var="file">
									<g:if test="${file.content.type=='Experiment Web Link'}">
										<g:set var="fcount" value="${fcount++}" />
										<g:if test="${fcount > 1}">, </g:if>
										<g:createFileLink content="${file.content}" displayLabel="${file.content.repository.repositoryType}"/>
									</g:if>
									<g:elseif test="${file.content.type=='Dataset Explorer Node Link'&&search==1}">
									<g:link controller="datasetExplorer" action="index" params="[path:file.content.location]">Dataset Explorer<img src="${resource(dir:'images', file:'internal-link.gif')}"/></g:link>
									</g:elseif>
								</g:each>
							</g:if>
							<g:if test="${searchId!=null}">
								| <g:link controller="search" action="newSearch" id="${searchId}">Search analyzed Data <img src="${resource(dir:'images', file:'internal-link.gif')}"/></g:link>
							</g:if>
						</g:if>
						<g:elseif test="${layoutRow.column == 'platforms'}">
							<g:each var="pf" in="${expPlatforms}">
								${pf?.name.encodeAsHTML()}<br>
							</g:each>
						</g:elseif>
						<g:elseif test="${layoutRow.column == 'organism'}">
							<g:each var="og" in="${expOrganisms}">
								${og?.encodeAsHTML()}<br>
							</g:each>
						</g:elseif>
					</g:elseif>
					
					<g:else> <%-- In all other cases, display as string --%>
						${fieldValue(bean:experimentInstance,field:layoutRow.column)}
					</g:else>
				</td>
			</tr>
		</g:each>
	</tbody>
</table>

