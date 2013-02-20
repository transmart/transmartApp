
<%! import annotation.* %> 
<%! import bio.* %>  
<%! import com.recomdata.util.* %> 
  
<g:form name="editMetadataForm">
<g:hiddenField name="id" value="${folder?.id}" />
<g:set var="objectUid" value="${folder?.uniqueId}"/>

<div id="displayMetadataErrors">
</div>

<table class="detail" height="300px">
	<tbody>
		<g:render template="metaData" model="[templateType: templateType,  measurements:measurements, technologies:technologies, vendors:vendors, platforms:platforms, title:title, bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]"/>
	</tbody>
</table>    

<br/>
<div align="center">
    <span id="savemetadatabutton" class="greybutton">Save</span>
    <span id="cancelmetadatabutton" class="greybutton buttonicon close">Cancel</span>
</div>
</g:form>
