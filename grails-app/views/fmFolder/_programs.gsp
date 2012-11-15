<div class="breaker">&nbsp;</div>
<g:set var="ts" value="${Calendar.instance.time.time}" />

<div class="search-results-table">
    <g:each in="${folders}" status="ti" var="folder">        
        <div class="${ (ti % 2) == 0 ? 'result-trial-odd' : 'result-trial-even'}" id="folder_${folder.id}_anchor">
            <table class="folderheader" name="${folder.id}">
            <tr>
				<td class="foldertitle">
					<span>
						<a id="toggleDetail_${folder.id}" href="#" onclick="javascript:toggleDetailDiv('${folder.id}', '${createLink(controller:'fmFolder',action:'getFoldersUnder',params:[id:folder.id])}');">
							<img alt="expand/collapse" id="imgExpand_${folder.id}" src="${resource(dir:'images',file:'down_arrow_small2.png')}" />
							<img alt="" src="${resource(dir:'images',file:'folder-big-closed.png')}" />
						</a>
					</span>
					<a href="#" onclick="showDetailDialog('${createLink(controller:'experimentAnalysis',action:'expDetail',id:folder.objectUid)}');">
						<span class="result-trial-name"> ${folder.folderName}</span>
					</a>
				</td>
	            <td class="foldericons">
	            	<div class="foldericonwrapper" style="display: none;">
	            		<span class="foldericon view">View metadata</span>
						<span class="foldericon add">Add to export</span>
					</div>
				</td>
            </tr>
            </table>
            <div id="${folder.id}_detail" name="${folder.id}" class="detailexpand"></div>
        </div> 
    </g:each>
</div>