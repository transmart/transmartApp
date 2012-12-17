<g:set var="ts" value="${Calendar.instance.time.time}" />

<div class="search-results-table">
	<g:each in="${folders}" status="ti" var="folder">        
		<table class="folderheader" name="${folder.objectUid}">
			<tr>
				<td class="foldertitle">
					<span>
						<a id="toggleDetail_${folder.id}" href="#" onclick="toggleDetailDiv('${folder.id}', '${createLink(controller:'fmFolder',action:'getFolderContents',params:[id:folder.id])}');">
							<img alt="expand/collapse" id="imgExpand_${folder.id}" src="${resource(dir:'images',file:'folderplus.png')}" />
							 <!-- <img alt="" src="${resource(dir:'images',file:'folder.png')}" />-->
						    <span class="foldericon ${folder.folderType.toLowerCase()}"></span>   
						</a>
					</span>
					<g:if test="${'study'==folder.folderType.toLowerCase()}">
					<a href="#" onclick="showDetailDialog('${createLink(controller:'fmFolder',action:'folderDetail',id:folder.id)}');">
						<span class="result-folder-name"> ${folder.folderName}</span>						
					</a>
					</g:if>
					<g:else>
                    <a href="#" onclick="showDetailDialog('${createLink(controller:'fmFolder',action:'folderDetail',id:folder.id)}');">
                        <span class="result-folder-name"> ${folder.folderName}</span>   
                        </a>                    
					</g:else>
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
	<%-- TODO This is unfinished. Don't display files here? Put them in a table in the metadata viewer --%>
	<g:if test="${files.size()> 0}">
        <table class="folderheader" style="margin-left: 20px;">
            <tr>
                <td class="foldertitle">
                            <span style="padding: 0px 16px 0px 0px"></span>
                            <span class="fileicon txt "></span>
                    <span class="result-folder-name"><i>Documents (${files.size()})</i></span>                   
                </td>
            </tr>
          </table>
	</g:if>
</div>