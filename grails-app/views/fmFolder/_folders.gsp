<%! import com.recomdata.util.* %>
<%
    def ontologyService = grailsApplication.classLoader.loadClass('transmartapp.OntologyService').newInstance()
    def fmFolderService = grailsApplication.classLoader.loadClass('fm.FmFolderService').newInstance()
%>

<g:set var="ontologyService" bean="ontologyService"/>
<g:set var="fmFolderService" bean="fmFolderService"/>
<g:set var="ts" value="${Calendar.instance.time.time}"/>
<g:set var="restrictedAccessMessage" value="Access to this node has been restricted. Please contact your administrator for access." />
<script type="text/javascript">
    var resultNumber = '${resultNumber}';
</script>

<div class="search-results-table">
    <g:each in="${folders}" status="ti" var="folder">
        <g:set var="folderAccessLevel" value="${folderContentsAccessLevelMap[folder]}"/>
        <g:if test="${!auto || folder.folderLevel > 1 || !folderSearchString || folderSearchString?.indexOf(folder.folderFullName) > -1 || (folder.folderLevel == 1 && fmFolderService.searchMatchesParentProgram(folderSearchString, folder.folderFullName))}">
            <table id="folder-header-${folder.id}" class="folderheader"
                   name="${folder.uniqueId}">

                <tr>
                    <td class="foldertitle">
                        <g:set var="folderIconType"
                               value="${folder.folderType.toLowerCase()}"/>
                        <g:if test="${folder.folderType.equalsIgnoreCase(FolderType.STUDY.name()) && ontologyService.checkSubjectLevelData(fmFolderService.getAssociatedAccession(folder))}">
                            <g:set var="folderIconType" value="studywsubject"/>
                        </g:if>
                        <span>
                            <g:if test="${folder.hasChildren()}">
                                <g:set var="toggleJsFunc" value="${folderAccessLevel == 'LOCKED' ? "alert('$restrictedAccessMessage')"
                                : "toggleDetailDiv('${folder.id}', folderContentsURL + '?id=${folder.id}&auto=false', false, false, true);"}" />

                                <a id="toggleDetail_${folder.id}" href="#"
                                   onclick="${toggleJsFunc}">
                                    <img alt="expand/collapse"
                                         id="imgExpand_${folder.id}"
                                         src="${resource(dir: 'images', file: 'folderplus.png')}"/>
                                    <span class="foldericon ${folderIconType}"></span>
                                </a>
                            </g:if>
                            <g:else>
                                <a id="toggleDetail_${folder.id}" href="#">
                                    <img alt="expand/collapse"
                                         id="imgExpand_${folder.id}"
                                         src="${resource(dir: 'images', file: 'folderleaf.png')}"/>
                                    <span class="foldericon ${folderIconType}"></span>
                                </a>
                            </g:else>
                        </span>
                        <g:set var="showDetailDialogJsFunc" value="${folderAccessLevel == 'LOCKED' ? "alert('$restrictedAccessMessage')" : "showDetailDialog(${folder.id});"}" />
                        <a href="#" onclick="${showDetailDialogJsFunc}">

                            <g:set var="highclass" value=""/>
                            <g:if test="${folderSearchString && folderSearchString.indexOf(folder.folderFullName + ',') > -1}">
                                <g:set var="highclass" value="searchResult"/>
                            </g:if>
                            <span id="result-folder-name-${folder.id}"
                                  class="result-folder-name ${highclass} accLev_${folderAccessLevel}"
                                  title="${folder.folderName}">${folder.folderName}</span>
                        </a>
                    </td>
                </tr>

                <g:if test="${folderAccessLevel != 'LOCKED'}">
                    <g:if test="${folderSearchString?.indexOf(folder.folderFullName) > -1}">
                    <%-- Auto-expand this folder as long as it isn't a unique leaf. --%>
                        <g:if test="${(uniqueLeavesString?.indexOf(folder.folderFullName + ',') == -1 && !nodesToClose.grep(folder.uniqueId))}">
                            <script>toggleDetailDiv('${folder.id}', folderContentsURL + '?id=${folder.id}&auto=true');</script>
                        </g:if>
                        <g:elseif test="${nodesToExpand.grep(folder.uniqueId)}">
                            <script>toggleDetailDiv('${folder.id}', folderContentsURL + '?id=${folder.id}&auto=true', true, false);</script>
                        </g:elseif>
                    </g:if>
                    <g:elseif test="${nodesToExpand.grep(folder.uniqueId)}">
                        <script>toggleDetailDiv('${folder.id}', folderContentsURL + '?id=${folder.id}&auto=true', true, false);</script>
                    </g:elseif>

                    <g:if test="${displayMetadata == (folder.uniqueId)}">
                        <script>showDetailDialog('${folder.id}');</script>
                    </g:if>
                </g:if>

                <g:set var="files" value="${folder.fmFiles}"/>

                <g:if test="${files?.size() > 0}">
                    <tr>
                        <td class="foldertitle">
                            <span class="result-document-count"><i>Documents (<span
                                    class="document-count">${files.size()}</span>)
                            </i></span>
                        </td>
                    </tr>
                </g:if>
            </table>
        </g:if>
        <div id="${folder.id}_detail" name="${folder.id}"
             class="detailexpand"></div>
    </g:each>
</div>
