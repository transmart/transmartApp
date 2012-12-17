<g:set var="ts" value="${Calendar.instance.time.time}" />

<div class="search-results-table">
    <g:each in="${folders}" status="ti" var="folder">        
            <table class="folderheader" name="${folder.id}">
            <tr>
				<td class="foldertitle">
					<span>
						<a id="toggleDetail_${folder.id}" href="#" onclick="toggleDetailDiv('${folder.id}', '${createLink(controller:'fmFolder',action:'getFolderContents',params:[id:folder.id])}');">
							<img style="margin-bottom: 2px;" alt="expand/collapse" id="imgExpand_${folder.id}" src="${resource(dir:'images',file:'folderplus.png')}" />
							<span class="foldericon ${folder.folderType.toLowerCase()}"></span>   
						</a>
					</span>
					<a href="#" onclick="showDetailDialog('${createLink(controller:'fmFolder',action:'folderDetail',id:folder.id)}');">
						<span class="result-folder-name" title="${folder.folderName}"> ${folder.folderName}</span>
					</a>
				</td>
            </tr>
            </table>
            <div id="${folder.id}_detail" name="${folder.id}" class="detailexpand"></div>
    </g:each>
</div>