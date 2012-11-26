<g:set var="ts" value="${Calendar.instance.time.time}" />

<div class="search-results-table">
    <g:each in="${folders}" status="ti" var="folder">        
            <table class="folderheader" name="${folder.id}">
            <tr>
				<td class="foldertitle">
					<span>
						<a id="toggleDetail_${folder.id}" href="#" onclick="toggleDetailDiv('${folder.id}', '${createLink(controller:'fmFolder',action:'getFolderContents',params:[id:folder.id])}');">
							<img style="margin-bottom: 2px;" alt="expand/collapse" id="imgExpand_${folder.id}" src="${resource(dir:'images',file:'folderplus.png')}" />
							<img alt="" src="${resource(dir:'images',file:'folder.png')}" />
						</a>
					</span>
					<a href="#" onclick="showDetailDialog('${createLink(controller:'experimentAnalysis',action:'expDetail',id:folder.objectUid)}');">
						<span class="result-folder-name"> ${folder.folderName}</span>
					</a>
				</td>
	            <%--
	            <td class="foldericons">
	            	<div class="foldericonwrapper" style="display: none;">
	            		<span class="foldericon view">View metadata</span>
						<span class="foldericon add">Add to export</span>
					</div>
				</td>
				--%>
            </tr>
            </table>
            <div id="${folder.id}_detail" name="${folder.id}" class="detailexpand"></div>
    </g:each>
</div>