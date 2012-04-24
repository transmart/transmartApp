<tr class="${(idx % 2) == 0 ? 'odd' : 'even'}">
	<g:set var="dtlLink" value="${createLink(action:'show', id:gs.id)}" />
	<g:set var="ownerFlag" value="${adminFlag || user.id==gs.createdByAuthUser.id}" />
	<g:set var="ctLkup" value="${ctMap.get(gs.id)}" />
	     			
    <td><a onclick="showDialog('GeneSigDetail_${gs.id}', { title: 'Gene Signature Detail [${gs.name?.replaceAll("'","\\\\'").encodeAsHTML()}]', url: '${dtlLink}'})">
    		<img alt="detail" style="vertical-align:middle;" src="${resource(dir:'images',file:'grid.png')}" >&nbsp;${gs.name?.replaceAll("'","\\\\'").encodeAsHTML()}</a></td>       		
	<td>${gs.createdByAuthUser.userRealName?.encodeAsHTML()}</td>
	<td><g:formatDate format="yyyy-MM-dd" date="${gs.dateCreated}" /></td>
	<td>${gs.speciesConceptCode?.codeName?.encodeAsHTML()}</td>
	<td>${gs.techPlatform?.accession?.encodeAsHTML()}</td>
	<td>${gs.tissueTypeConceptCode?.codeName?.encodeAsHTML()}</td>
	<td style="text-align:center;">${gs.publicFlag ? 'Yes' : 'No'}</td>
	<td style="text-align:center;">${(gs.foldChgMetricConceptCode?.bioConceptCode=='NOT_USED') ? 'Yes' : 'No'}</td>
	<g:if test="${ctLkup!=null}">
	<td style="text-align:center;">${ctLkup?.getAt(1)}</td>
	<td style="text-align:center;">${ctLkup?.getAt(2)}</td>
	<td style="text-align:center;">${ctLkup?.getAt(3)}</td>	
	</g:if>
	<g:else>
<td style="text-align:center;">0</td>
	<td style="text-align:center;">0</td>
	<td style="text-align:center;">0</td>		
	</g:else>
	<td><select name="action_${gs.id}" style="font-size: 10px;" onchange="handleActionItem(this, ${gs.id});">
			<option value="">-- Select Action --</option>							             		                    	
			<option value="clone">Clone</option>	                    								
			<g:if test="${!gs.deletedFlag && ownerFlag}"><option value="delete">Delete</option></g:if>	                    								
			<g:if test="${ownerFlag}"><option value="edit">Edit</option></g:if>	      
			<g:if test="${ownerFlag}"><option value="showEditItems">Edit Items</option></g:if>	                    								              	
			<option value="export">Excel Download</option>
			<option value="gmt">Download .GMT file</option>
			<g:if test="${!gs.publicFlag && ownerFlag}"><option value="public">Make Public</option></g:if>
     	</select>
	</td>
</tr>		     
