<%! import annotation.* %> 
<%! import org.transmart.biomart.* %>  
<%! import com.recomdata.util.* %> 
  
<g:form name="createAnalysisForm">
<g:hiddenField name="id" value="${folder?.id}" />
<g:hiddenField name="folderType" value="${folder?.folderType}" />
<g:hiddenField name="parentId" value="${folder?.parent.id}" />
<g:set var="objectUid" value="${folder?.uniqueId}"/>

<div id="displayAnalysisErrors">
</div>

<table class="detail" height="300px">
	<tbody>
	<tr>
		<td valign="top" align="right" class="name">Name&nbsp;<g:requiredIndicator/>:</td>
		<td valign="top" align="left" name="folderName"><g:textField size="100" name="folderName" value="${folder?.folderName}"/></td>            
	</tr>
	<tr>
		<td valign="top" align="right" class="name">Description&nbsp;<g:requiredIndicator/>:</td>
		<td valign="top" align="left"><g:textArea size="100" cols="74" rows="10" name="description" value="${folder?.description}"/></td>
	</tr>
	<g:render template="metaData" model="[templateType: templateType,  measurements:measurements, technologies:technologies, vendors:vendors, platforms:platforms, title:title, bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]"/>
	</tbody>
</table>    

<br/>
<div align="center">
    <span id="saveanalysisbutton" class="greybutton">Save</span>
    <span id="cancelanalysisbutton" class="greybutton buttonicon close">Cancel</span>
</div>
</g:form>
<r:layoutResources disposition="defer"/>
