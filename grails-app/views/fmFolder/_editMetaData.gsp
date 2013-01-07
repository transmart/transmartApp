<g:overlayPopup title="Edit Metadata" divContainerId="${params.eleId}">
<%! import annotation.* %> 
<%! import bio.* %>  
<%! import com.recomdata.util.* %> 
  
<g:form action="save">
<g:hiddenField name="id" value="${folder?.id}" />
    
<div style="width:800px">  
<g:if test="${metaDataTagItems && metaDataTagItems.size()>0}">
    <table class="detail" height="300px">
        <tbody>
        <g:each in="${metaDataTagItems}" status="i" var="amTagItem">
          <g:if test="${amTagItem.editable}">
            <tr>
                <td valign="top" align="right" class="name">${amTagItem.displayName}
                <g:if test="${amTagItem.required == true}"><g:requiredIndicator/></g:if>:
                </td>
                <td valign="top" align="left" class="value">
                <g:if test="${amTagItem.tagItemType == 'FIXED'  && amTagItem.tagItemAttr!=null?bioDataObject?.hasProperty(amTagItem.tagItemAttr):false}" >
					<g:if test="${amTagItem.editable == false}">
						${fieldValue(bean:bioDataObject,field:amTagItem.tagItemAttr)}
                	</g:if>
                	<g:else>
	                	<g:if test="${fieldValue(bean:bioDataObject,field:amTagItem.tagItemAttr).length()<100}">
							<g:textField size="100" name="${amTagItem.tagItemAttr}"  value="${fieldValue(bean:bioDataObject,field:amTagItem.tagItemAttr)}"/>
		                </g:if>
	    	            <g:else>
	            	         <g:textArea size="100" cols="74" rows="10" name="${amTagItem.tagItemAttr}" value="${fieldValue(bean:bioDataObject,field:amTagItem.tagItemAttr)}" />          
	        	        </g:else>
	        	    </g:else>    
                </g:if>
                <g:elseif test="${amTagItem.tagItemType == 'PICKLIST'}">
                	<g:set var="tagValues" value="${AmTagDisplayValue.findAll('from AmTagDisplayValue a where a.subjectUid=? and a.amTagItem.id=?',[folder.objectUid,amTagItem.id])}"/>
					<g:if test="${amTagItem.editable == false}">
						${tagValues!=null&&tagValues.size()>0?tagValues[0].displayValue:''}
                	</g:if>
                	<g:else>
    				<g:select from="${ConceptCode.findAll('from ConceptCode where codeTypeName=? order by codeName',[amTagItem.codeTypeName])}"	
                	name="amTagItem_${amTagItem.id}" value="${tagValues!=null&&tagValues.size()>0?tagValues[0].objectId:''}"  optionKey="id" optionValue="codeName"  noSelection="['':'-Select One-']" />	
					</g:else>
                </g:elseif>
                <g:elseif test="${amTagItem.tagItemType == 'CUSTOM'}">
                	<g:set var="tagValues" value="${AmTagDisplayValue.findAll('from AmTagDisplayValue a where a.subjectUid=? and a.amTagItem.id=?',[folder.objectUid,amTagItem.id])}"/>
	           		<g:if test="${amTagItem.editable == false}">
						not editable CUSTOM
                	</g:if>
                	<g:else>
	                	<g:if test="${(tagValues!=null&&tagValues.size()>0?tagValues[0].displayValue:'').length()<100}">
							<g:textField size="100" name="${amTagItem.id}"  value="${tagValues!=null&&tagValues.size()>0?tagValues[0].displayValue:''}"/>
		                </g:if>
	    	            <g:else>
	            	         <g:textArea size="100" cols="74" rows="10" name="${amTagItem.id}" value="${tagValues!=null&&tagValues.size()>0?tagValues[0].displayValue:''}" />          
	        	        </g:else>
                	</g:else>
	           </g:elseif>
                
                <g:else>
                </g:else>
                </td>
            </tr>
         </g:if>
        </g:each>
    </tbody>    
</table>
</g:if> 
</div>
<div></div>
<div class="buttons">
    <span class="button"><g:actionSubmit class="save" action="update" id="save" value="${message(code: 'default.button.update.label', default: 'Save')}" /></span>
    <span class="button"><g:actionSubmit class="list" action="list"  id="cancel" value="Cancel" onclick="return confirm('Are you sure?')"/></span>              
</div>
</g:form>
</g:overlayPopup>
