<g:set var="ts" value="${Calendar.instance.time.time}" />

<div class="search-results-table">
	<g:each in="${folders}" status="ti" var="folder">        
		<g:if test="${!auto || folder.folderLevel > 1 || !folderSearchString || folderSearchString?.indexOf(folder.folderFullName) > -1}">
			<table class="folderheader" name="${folder.uniqueId}">
				
				<tr>
					<td class="foldertitle">
						<span>
							<a id="toggleDetail_${folder.id}" href="#" onclick="toggleDetailDiv('${folder.id}', folderContentsURL + '?id=${folder.id}&auto=false');">
								<img alt="expand/collapse" id="imgExpand_${folder.id}" src="${resource(dir:'images',file:'folderplus.png')}" />
							    <span class="foldericon ${folder.folderType.toLowerCase()}"></span>   
							</a>
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
					<g:set var="autoExpand" value="true" />
					<%-- If this is a program and ONLY the program is matched, expand the studies under it with auto=false to return all of them without respect to the folder mask --%>
					<g:if test="${folder.folderLevel == 0 && folderSearchString?.indexOf(folder.folderFullName + ',') > -1}"> <%-- Only run this test if the program itself is a match --%>
						<g:set var="indexmatch" value="${folderSearchString?.indexOf(folder.folderFullName + '')}"/>
						<g:set var="indexmatch2" value="${folderSearchString?.indexOf(folder.folderFullName, indexmatch+1)}"/>
						<g:if test="${indexmatch2 == -1}"> <%-- This happens if only one string match has been found - the program is the only result in this branch of the tree --%>
							<g:set var="autoExpand" value="false"/>
						</g:if>
					</g:if>
					<script>toggleDetailDiv('${folder.id}', folderContentsURL + '?id=${folder.id}&auto=${autoExpand}');</script>
				</g:if>
				<g:if test="${files?.size()> 0}">
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