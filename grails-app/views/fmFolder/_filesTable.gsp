<table class="details-table" id="file-list-table" name="${folder.id}">
            <thead>
                <tr>                
                    <th class="columnheader">File Name</th>
                    <th class="columnheader">Created on</th>
                    <th class="columnheader">Updated on</th>
                    <th class="columnheader" width="200px">&nbsp;</th>
                </tr>
            </thead>
		    <tfoot>
		    	<tr>
		    		<td colspan="3">&nbsp;</td>
	    		   <td>
		               <div style="padding: 4px 0px;">
		                    <span class="foldericon addall link">Export all</span>
		               </div>
	               </td>
		    	</tr>
		    </tfoot>
    <tbody>
        <g:each in="${folder?.fmFiles.sort{a,b-> a.displayName.compareTo(b.displayName)}}" status="i" var="fmFile">
            <tr class="details-row ${(i % 2) == 0 ? 'odd' : 'even'}">
               <td class="columnname" style="text-align: left;">
                   <span class="fileicon ${fmFile.fileType}"></span>
                   <g:link controller="fmFolder" action="downloadFile" params="[id: fmFile.id]">
                       <g:if test="${hlFileIds?.contains(fmFile.uniqueId)}">
                           <mark><b>${fmFile.displayName}</b></mark>
                       </g:if>
                       <g:else>
                           ${fmFile.displayName}
                       </g:else>
                   </g:link>
               </td>
               <td class="columnvalue">
               <g:formatDate format="yyyy-MM-dd" date="${fmFile.createDate}" />
               </td> 
               <td class="columnvalue">
               <g:formatDate format="yyyy-MM-dd" date="${fmFile.updateDate}" />
               </td> 
               <td class="columnvalue">
	               <div>
	                    <span class="exportaddspan foldericon add link" name="${fmFile.id}">Add to export</span>
	                    <sec:ifAnyGranted roles="ROLE_ADMIN">
	                    	<span class="deletefilespan foldericon delete link" name="${fmFile.id}"> Delete</span>
	                    </sec:ifAnyGranted>
	               </div>
               </td>
            </tr>
        </g:each>
    </tbody>
</table>
