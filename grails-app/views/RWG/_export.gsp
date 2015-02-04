<div style="margin: 16px;">
    <div>
        <div style="float: right;">
            <span id="closeexport" class="greybutton buttonicon close">Close</span>
        </div>

        <h3 class="rdc-h3">Export Files</h3>
    </div>

    <g:if test="${files}"><p>These are the files that have been added to the cart. Please select the files to export.</p></g:if>
    <g:else>No files have been added to the export cart.</g:else>

    <table style="width: 100%;" class="exporttable" id="exporttable">
        <g:each in="${files}" var="file">
            <tr>
                <td>${file.folder}</td>
                <td><g:checkBox name="${file.id}" onclick="updateExportCount();" value="true"/></td>
                <td><span class="fileicon ${file.fileType}"></span>&nbsp;${file.displayName}</td>
                <td><span class="greybutton remove">Remove</span></td>
            </tr>
        </g:each>
    </table>
    <br/>
    <g:if test="${files}"><span id="exportbutton"
                                class="greybutton export">Export selected files (${files.size()})</span></g:if>
</div>