<div style="margin: 16px;">
<h3 class="rdc-h3">Export Files</h3>

<g:if test="${files}"><p>These are the files that have been added to the cart. Please select the files to export.</p></g:if>
<g:else>No files have been added to the export cart.</g:else>

<table style="width: 100%; table-layout: fixed" class="exporttable" id="exporttable">
	<g:each in="${files}" var="file">
		<tr name="${file.id}">
			<td>${file.folder}</td>
			<td><g:checkBox name="${file.id}" value="true"/></td>
			<td><span class="fileicon ${file.fileType}"></span>&nbsp;${file.displayName}</td>
			<td><span class="greybutton remove">Remove</span></td>
		</tr>
	</g:each>
</table>
<br/>
<g:if test="${files}"><span class="greybutton export">Export selected files</span></g:if>
</div>