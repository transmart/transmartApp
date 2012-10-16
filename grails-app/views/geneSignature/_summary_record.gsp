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

<tr class="${(idx % 2) == 0 ? 'odd' : 'even'}">
	<g:set var="dtlLink" value="${createLink(action:'show', id:gs.id)}" />
	<g:set var="ownerFlag" value="${adminFlag || user.id==gs.createdByAuthUser.id}" />
	<g:set var="ctLkup" value="${ctMap.get(gs.id)}" />
	<td><g:checkBox name="${gs.id}" class="geneList"/></td>
    <td><a onclick="showDialog('GeneSigDetail_${gs.id}', { title: 'Gene Signature Detail [${gs.name?.encodeAsHTML()}]', url: '${dtlLink}'})">
    		<img alt="detail" style="vertical-align:middle;" src="${resource(dir:'images',file:'grid.png')}" />&nbsp;${gs.name?.encodeAsHTML()}</a></td>       		
	<td>${gs.createdByAuthUser.userRealName?.encodeAsHTML()}</td>
	<td><g:formatDate format="yyyy-MM-dd" date="${gs.dateCreated}" /></td>
	<td><g:formatDate format="yyyy-MM-dd" date="${gs.lastUpdated}" /></td>
	<td>${gs.speciesConceptCode?.codeName?.encodeAsHTML()}</td>
	<td title="${gs.techPlatform?.description?.encodeAsHTML()}">${gs.techPlatform?.accession?.encodeAsHTML()}</td>
	<td>${gs.tissueTypeConceptCode?.codeName?.encodeAsHTML()}</td>
	<td id="${gs.id}Public" style="text-align:center;">${gs.publicFlag ? 'Public' : 'Private'}</td>
	<td id="${gs.id}Owned" style="text-align:center;">${(user.id==gs.createdByAuthUser.id)?'Owned':'Unowned'}</td>
	<g:hiddenField name="${gs.id}Deleted" value="${gs.deletedFlag?'Deleted':'Undeleted'}"/>
	<td style="text-align:center;">${(gs.foldChgMetricConceptCode.bioConceptCode=='NOT_USED') ? 'List' : 'Sig'}</td>
	<td style="text-align:center;">
		<g:if test="${ctLkup!=null}">
			${ctLkup[1]}
		</g:if>
	</td>
	<td style="text-align:center;">
		<g:if test="${ctLkup!=null}">
			${ctLkup[2]}
		</g:if>
	</td>
	<td style="text-align:center;">
		<g:if test="${ctLkup!=null}">
			${ctLkup[3]}
		</g:if>
	</td>	
</tr>		     
