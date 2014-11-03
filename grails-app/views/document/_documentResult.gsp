${createFileLink(document: document)}
</td>
</tr>
<tr>
    <td width="90%">
        <b>Repository:</b>&nbsp;${document.getRepository()}
    &nbsp;|&nbsp;<b>Path:</b>&nbsp;
    <g:if test="${document.getFilePath().lastIndexOf('/') > -1}">
        ${document.getFilePath().substring(0, document.getFilePath().lastIndexOf("/"))}
    </g:if>
    <g:else>
        -
    </g:else>
    </td>
    <td width="10%">
        <b>Score:</b>&nbsp;<g:formatNumber number="${document.getScore()}" format="0.00000"/>
    <td>
</tr>
<tr>
    <td colspan="2">
        ${document.getFullText()}
    </td>
</tr>
</table>
</td>
</tr>
<tr>
    <td>
        <div id="${rifdivid}">
        </div>
    </td>
</tr>
</g:each>
</table>
</g:else>
</div>