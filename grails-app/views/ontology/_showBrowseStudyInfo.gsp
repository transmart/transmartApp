<%@ page import="org.transmart.biomart.BioData; org.transmart.biomart.ConceptCode; com.recomdata.util.*; annotation.* " %>
<%-- TODO This is a copy and paste from folderDetail - turn this into a template! --%>

<g:if test="${browseStudyInfo.metaDataTagItems}">
    <br/>
    <h2><g:message code="show.browse.study.header" default="Browse information"/>:</h2>
    <table class="detail" style="width: 515px;">
        <tbody>
        <g:each in="${browseStudyInfo.metaDataTagItems}" status="i" var="amTagItem">
            <g:if test="${amTagItem.viewInGrid}">
                <tr class='prop'>
                    <!-- TODO: If active -->

                    <td valign="top" align="right" class="name" width="20%">${amTagItem.displayName}</td>
                    <td valign="top" align="left" class="value" width="60%">

                    <!-- FIXED -->
                        <g:if test="${amTagItem.tagItemType == 'FIXED' && amTagItem.tagItemAttr != null ? browseStudyInfo.bioDataObject?.hasProperty(amTagItem.tagItemAttr) : false}">
                            <g:set var="fieldValue"
                                   value="${browseStudyInfo.bioDataObject[amTagItem.tagItemAttr]}"/>
                            <g:if test="${amTagItem.tagItemSubtype == 'PICKLIST'}">
                            <%-- Split multiple values by pipe --%>
                                <g:set var="terms" value="${fieldValue.split('\\|')}"/>

                                <g:each in="${terms}" var="term" status="t">
                                    <g:set var="bioDataId"
                                           value="${BioData.find('from BioData where uniqueId=?', [term])?.id}"/>
                                    <g:if test="${t > 0}"><br/></g:if>
                                    <g:if test="${bioDataId}">
                                        ${ConceptCode.find('from ConceptCode where id=?', bioDataId).codeName}
                                    </g:if>
                                    <g:else>
                                        ${term}
                                    </g:else>
                                </g:each>
                            </g:if>
                            <g:elseif test="${amTagItem.tagItemSubtype == 'MULTIPICKLIST'}">
                            <%-- Split multiple values by pipe --%>
                                <g:set var="terms" value="${fieldValue.split('\\|')}"/>

                                <g:each in="${terms}" var="term" status="t">
                                    <g:set var="bioDataId"
                                           value="${BioData.find('from BioData where uniqueId=?', [term])?.id}"/>
                                    <g:if test="${t > 0}"><br/></g:if>
                                    <g:if test="${bioDataId}">
                                        ${ConceptCode.find('from ConceptCode where id=?', bioDataId).codeName}
                                    </g:if>
                                    <g:else>
                                        ${term}
                                    </g:else>
                                </g:each>
                            </g:elseif>
                            <g:else>
                                ${fieldValue}
                            </g:else>
                        </g:if>
                        <g:else>
                            <g:set var="tagValues"
                                   value="${AmTagDisplayValue.findAllDisplayValue(browseStudyInfo.folder.getUniqueId(), amTagItem.id)}"/>
                            <g:if test="${tagValues != null}">
                                <g:each var="tagValue" status="k" in="${tagValues}">
                                    <g:if test="${k > 0 && tagValue.displayValue}">,</g:if>
                                    <g:createTagItemValue tagItem="${amTagItem}" tagValue="${tagValue}"/>
                                </g:each>
                            </g:if>
                        </g:else>

                    </td>
                </tr>
            </g:if>
        </g:each>
        </tbody>
    </table>
    <span></span>
</g:if> 
