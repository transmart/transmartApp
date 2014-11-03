<g:if test="${!layout}">
    <i>No columns have been set up for the subfolder list</i>
</g:if>

<div class="list">
    <table class="default">
        <thead>
        <tr>
            <g:each in="${subFolderLayout}" var="layoutRow">
                <th>${layoutRow.displayName}</th>
            </g:each>
        </tr>
        </thead>
        <tbody>
        <g:each in="${subFolders}" status="i" var="subFolder">
            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <td>
                <g:link action="folderDetail" id="${subFolder.id}">
                    <img src="${resource(dir: 'images/skin', file: 'information.png')}" alt="Detail link" border="0"/>
                </g:link>
            </td>
            <g:each in="${subFolderLayout}" status="j" var="layoutRow">
                <td valign="top" class="columnvalue">
                    <g:if test="${layoutRow.dataType == 'date'}">
                        <g:formatDate bean="${folderInstance}" field="${layoutRow.column}" format="yyyy-MM-dd"/>
                    </g:if>
                    <g:else><%-- In all other cases, display as string --%>
                        ${fieldValue(bean: subFolder, field: layoutRow.column)}
                    </g:else>
                </td>
            </g:each>
        </g:each>
        </tbody>
    </table>
</div>
