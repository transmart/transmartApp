<g:overlayPopup title="Edit Metadata" divContainerId="${params.eleId}">
    
<g:form action="save">
<g:hiddenField name="id" value="${folder?.id}" />
    
<div style="width:800px">  
     <div style="height:10px;"></div>
  
    <table class="detail" height="300px">
        <tbody>
      <g:each in="${layout}" status="i" var="layoutRow">
            <tr>
                <td valign="top" align="right" class="name">${layoutRow.displayName}</td>
                <td valign="top" align="left" class="value">
                <g:if test="${fieldValue(bean:folder,field:layoutRow.column).length()<100}">
                  <g:textField size="100" name="${layoutRow.column}" value="${fieldValue(bean:folder,field:layoutRow.column)}" />
                </g:if>
                <g:else>
                     <g:textArea size="100" cols="74" rows="10" name="${layoutRow.column}" value="${fieldValue(bean:folder,field:layoutRow.column)}" />          
                </g:else>
                </td>                
            </tr>
        </g:each>  
        </tbody>
    </table>
</div>
<div></div>
<div class="buttons">
    <span class="button"><g:actionSubmit class="save" action="update" id="save" value="${message(code: 'default.button.update.label', default: 'Save')}" /></span>
    <span class="button"><g:actionSubmit class="list" action="list"  id="cancel" value="Cancel" onclick="return confirm('Are you sure?')"/></span>              
</div>
</g:form>
</g:overlayPopup>
