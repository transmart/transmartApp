
<%! import annotation.* %> 
<%! import bio.* %>  
<%! import com.recomdata.util.* %> 
  
<g:form name="createAssayForm">
<g:hiddenField name="id" value="${folder?.id}" />
<g:set var="objectUid" value="${folder?.uniqueId}"/>
<table class="detail" height="300px">
        <tbody>
 <tr>
                <td valign="top" align="right" class="name">Name:</td>
                <td valign="top" align="left" name="folderName">
                <g:textField size="100" name="folderName"  value="${folder?.folderName}"/>
			    </td>            
        </tr>
        <tr>
         <td valign="top" align="right" class="name">Description:</td>
                <td valign="top" align="left">
                <g:textArea size="100" cols="74" rows="10" name="description" value="${folder?.description}" />          
		      	</td>
		      	</tr>
<g:render template="metaData" model="[templateType: templateType, title:title, bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]"/>

  </tbody>
        </table>    
        <br/>
<div align="center">
    <span id="saveassaybutton" class="greybutton">Save</span>
    <span id="cancelassaybutton" class="greybutton buttonicon close">Cancel</span>
</div>
</g:form>
