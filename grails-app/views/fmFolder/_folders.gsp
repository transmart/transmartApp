<g:set var="ts" value="${Calendar.instance.time.time}" />

<div class="search-results-table">
	<g:each in="${folders}" status="ti" var="folder">        
		<g:if test="${!auto || folder.folderLevel > 1 || !folderSearchString || folderSearchString?.indexOf(folder.folderFullName) > -1}">
			<table class="folderheader" name="${folder.uniqueId}">
				
				<tr>
					<td class="foldertitle">
						<span>
							<g:if test="${folder.hasChildren()}">
								<a id="toggleDetail_${folder.id}" href="#" onclick="toggleDetailDiv('${folder.id}', folderContentsURL + '?id=${folder.id}&auto=false');">
									<img alt="expand/collapse" id="imgExpand_${folder.id}" src="${resource(dir:'images',file:'folderplus.png')}" />
								    <span class="foldericon ${folder.folderType.toLowerCase()}"></span>
								</a>
							</g:if>
							<g:else>
								<a id="toggleDetail_${folder.id}" href="#">
									<img alt="expand/collapse" id="imgExpand_${folder.id}" src="${resource(dir:'images',file:'folderleaf.png')}" />
								    <span class="foldericon ${folder.folderType.toLowerCase()}"></span>
								</a>
							</g:else>
						</span>
						<a href="#" onclick="showDetailDialog('${createLink(controller:'fmFolder',action:'folderDetail',id:folder.id)}');">
						
						<g:set var="highclass" value=""/>
						<g:if test="${folderSearchString && folderSearchString.indexOf(folder.folderFullName + ',') > -1}">
							<g:set var="highclass" value="searchResult"/>
						</g:if>
							<span class="result-folder-name ${highclass}" title="${folder.folderName}"> ${folder.folderName}</span>						
						</a>
					</td>
				</tr>
				
				<g:if test="${folderSearchString?.indexOf(folder.folderFullName) > -1}">
					<%-- Auto-expand this folder as long as it isn't a unique leaf. --%>
					<g:if test="${(uniqueLeavesString?.indexOf(folder.folderFullName + ',') == -1)}">
						<script>toggleDetailDiv('${folder.id}', folderContentsURL + '?id=${folder.id}&auto=true');</script>
					</g:if>
				</g:if>
				
				<g:set var="files" value="${folder.fmFiles}" />
				
				<g:if test="${files?.size() > 0}">
		            <tr>
		                <td class="foldertitle">
		                    <span class="result-document-count"><i>Documents (${files.size()})</i></span>                   
		                </td>
		            </tr>
	            </g:if>
			</table>
		</g:if>
		<div id="${folder.id}_detail" name="${folder.id}" class="detailexpand"></div>
	</g:each>
</div>