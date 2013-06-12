
<%! import annotation.* %> 
<%! import bio.* %>  
<%! import com.recomdata.util.* %> 

<%
    def metaDataService = grailsApplication.classLoader.loadClass('annotation.MetaDataService').newInstance()
%>

<link rel="stylesheet" href="${resource(dir:'css', file:'uploadData.css')}"></link>
<script type="text/javascript">$j = jQuery.noConflict();</script>
<script type="text/javascript" src="${resource(dir:'js', file:'uploadData.js')}"></script>


<div>  
<g:if test="${metaDataTagItems && metaDataTagItems.size()>0}">
        <g:each in="${metaDataTagItems}" status="i" var="amTagItem">
            <tr>
           <td valign="top" align="right" class="name">${amTagItem.displayName}&nbsp;<g:if test="${amTagItem.required == true}"><g:requiredIndicator/></g:if>:
                </td>
                <td valign="top" align="left" class="value">
               
                <!-- FIXED -->
                <g:if test="${amTagItem.tagItemType == 'FIXED'}">
                 	  <g:if test="${amTagItem.tagItemAttr!=null?bioDataObject?.hasProperty(amTagItem.tagItemAttr):false}" >
						<g:if test="${amTagItem.tagItemSubtype == 'PICKLIST'}">
	           			<g:select from="${ConceptCode.findAll('from ConceptCode where codeTypeName=? order by codeName',[amTagItem.codeTypeName])}"	
		                	name="${amTagItem.tagItemAttr}"  value="${fieldValue(bean:bioDataObject,field:amTagItem.tagItemAttr)}"   optionKey="uniqueId" optionValue="codeName"  noSelection="['':'-Select One-']" />	
						</g:if>
	                	<g:elseif test="${amTagItem.tagItemSubtype == 'MULTIPICKLIST'}">
	                		<g:set var="metaDataService" bean="metaDataService"/>
	                		<g:set var="fieldValue" value="${fieldValue(bean:bioDataObject,field:amTagItem.tagItemAttr)}"/>
                    		<g:set var="displayValues" value="${metaDataService.getViewValues(fieldValue)}"/>
                    		<tmpl:extTagSearchField fieldName="${amTagItem.tagItemAttr}" codeTypeName="${amTagItem.codeTypeName}" searchAction="extSearch" searchController="metaData" values="${displayValues}"/>
						
						</g:elseif>
	                	<g:elseif test="${amTagItem.tagItemSubtype == 'FREETEXT'}">
		                	<g:if test="${fieldValue(bean:bioDataObject,field:amTagItem.tagItemAttr).length()<100}">
								<g:textField size="100" name="${amTagItem.tagItemAttr}"  value='${bioDataObject."${amTagItem.tagItemAttr}"?:""}'/>
			                </g:if>
		    	            <g:else>
		            	         <g:textArea style="width: 100%" rows="10" name="${amTagItem.tagItemAttr}" value='${bioDataObject."${amTagItem.tagItemAttr}"?:""}' />          
		        	        </g:else>
	        	        </g:elseif>
	                	<g:elseif test="${amTagItem.tagItemSubtype == 'FREETEXTAREA'}">
	            	         <g:textArea style="width: 100%" rows="10" name="${amTagItem.tagItemAttr}" value='${bioDataObject."${amTagItem.tagItemAttr}"?:""}' />          
	        	        </g:elseif>
	        	        <g:else>
	        	        ERROR -- Unrecognized tag item subtype
	        	        </g:else>
	        	        </g:if>
                </g:if>
				<g:elseif test="${amTagItem.tagItemType == 'CUSTOM'}">
                	<g:set var="tagValues" value="${AmTagDisplayValue.findAll('from AmTagDisplayValue a where a.subjectUid=? and a.amTagItem.id=?',[folder.getUniqueId(),amTagItem.id])}"/>
	           		<g:if test="${amTagItem.editable == false}">
						not editable CUSTOM
                	</g:if>
                	<g:else>
                	  	<g:set var="tagValues" value="${AmTagDisplayValue.findAll('from AmTagDisplayValue a where a.subjectUid=? and a.amTagItem.id=?',[folder.getUniqueId(),amTagItem.id])}"/>
	                	<g:if test="${amTagItem.tagItemSubtype == 'FREETEXT'}">
		                	<g:if test="${(tagValues!=null&&tagValues.size()>0?tagValues[0].displayValue:'')?.length()<100}">
								<g:textField size="100" name="amTagItem_${amTagItem.id}"  value="${tagValues!=null&&tagValues.size()>0?tagValues[0].displayValue:''}"/>
			                </g:if>
		    	            <g:else>
		            	         <g:textArea size="100" cols="74" rows="10" name="amTagItem_${amTagItem.id}" value="${tagValues!=null&&tagValues.size()>0?tagValues[0].displayValue:''}" />          
		        	        </g:else>
						</g:if>
	                	<g:elseif test="${amTagItem.tagItemSubtype == 'FREETEXTAREA'}">
		               	     <g:textArea size="100" cols="74" rows="10" name="amTagItem_${amTagItem.id}" value="${tagValues!=null&&tagValues.size()>0?tagValues[0].displayValue:''}" />          
		        	   </g:elseif>
	                	<g:elseif test="${amTagItem.tagItemSubtype == 'PICKLIST'}">
		                	<g:select from="${ConceptCode.findAll('from ConceptCode where codeTypeName=? order by codeName',[amTagItem.codeTypeName])}"	
			                	name="amTagItem_${amTagItem.id}" value="${tagValues!=null&&tagValues.size()>0?tagValues[0].objectUid:''}"  optionKey="uniqueId" optionValue="codeName"  noSelection="['':'-Select One-']" />	
						</g:elseif>
	                	<g:elseif test="${amTagItem.tagItemSubtype == 'MULTIPICKLIST'}">
	                	    <tmpl:extTagSearchField fieldName="amTagItem_${amTagItem.id}" codeTypeName="${amTagItem.codeTypeName}" searchAction="extSearch" searchController="metaData" values="${tagValues}"/>
						</g:elseif>
                	</g:else>
	           </g:elseif>
                <g:else>
      			<g:set var="tagValues" value="${AmTagDisplayValue.findAll('from AmTagDisplayValue a where a.subjectUid=? and a.amTagItem.id=?',[folder.getUniqueId(),amTagItem.id])}"/>
            	<g:if test="${amTagItem.tagItemSubtype == 'COMPOUNDPICKLIST'}">
            		<g:render template="${amTagItem.guiHandler}" model="${[measurements:measurements, technologies:technologies, vendors:vendors, platforms:platforms, fieldName:'amTagItem_' + amTagItem.id,searchAction:amTagItem.guiHandler + 'Search',searchController:'metaData', values:tagValues]}" />
				</g:if>
            	<g:else>
            		<g:render template="extBusinessObjSearch" model="${[fieldName:'amTagItem_' + amTagItem.id,searchAction:amTagItem.guiHandler + 'Search',searchController:'metaData', values:tagValues]}" />
				</g:else>
				</g:else>
                </td>
            </tr>
        </g:each>
</g:if> 
</div>

