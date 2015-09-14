<g:set var="overlayDiv" value="metaData_div" />
<script type="text/javascript">
var analysisCount = 1
var assayCount = 3

    $j(document).ready(function() 
    {     
        var dt1 = new dataTableWrapper('gridViewWrapper1', 'gridViewTable1', 'Analysis (' + analysisCount + ")");
        dt1.loadData(${jSONForGrid});

        var dt2 = new dataTableWrapper('gridViewWrapper2', 'gridViewTable2', 'Assays (' + assayCount + ')');
        dt2.loadData(${jSONForGrid1});
        
     });
</script>

<g:if test="${!layout}">
    <i>No columns have been set up for the study view</i>
</g:if>

<div style="margin:10px;padding:10px;">
    <h3 class="rdc-h3">${experimentInstance?.title}</h3>
    <div style="line-height:14px;font-family:arial,​tahoma,​helvetica,​sans-serif; font-size: 12px;">
    <g:if test="${experimentInstance?.description != null && experimentInstance?.description.length() > 325000}">
        ${(experimentInstance?.description).substring(0,324000)}&nbsp;&nbsp;
        <a href=# >...See more</a>
    </g:if>
    <g:elseif test="${experimentInstance?.description != null}">
        ${experimentInstance?.description}
    </g:elseif>
</div>
<div style="height:20px;"></div>

<div style="width:800px; border:2px solid #DDD; border-radius:8px;-moz-border-radius: 8px;">
    <table class="details-table">
        <thead style="border-radius:8px;-moz-border-radius: 8px;">
            <tr style="border-radius:8px;-moz-border-radius: 8px;">   
                <th style="border-radius:8px;-moz-border-radius: 8px;">&nbsp;</th>
                <th align="right"><g:remoteLink controller="fmFolder" action="editMetaData" update="${overlayDiv}" 
                    params="[eleId:overlayDiv, experimentId:experimentInstance.id]" 
                    before="initLoadingDialog('${overlayDiv}')" onComplete="centerDialog('${overlayDiv}')">
                    <img align="right" src="${resource(dir:'images', file:'pencil.png')}"/></g:remoteLink>
                </th>
            </tr>
        </thead>
            
        <tbody>
            <g:each in="${layout}" status="i" var="layoutRow">
                <g:if test="${layoutRow.display}">
                    <tr class='details-row'> <!-- class="${(i % 2) == 0 ? 'odd' : 'even'}"> -->
                        <td valign="top" align="right" class="columnname" width="20%">${layoutRow.displayName}</td>

                        <td valign="top" align="left" class="columnvalue" width="80%">
                            <g:if test="${layoutRow.dataType == 'date'}">
                                <g:fieldDate bean="${experimentInstance}" field="${layoutRow.column}" format="yyyy-MM-dd"/>
                            </g:if>

                            <%-- Special cases --%>
                            <g:elseif test="${layoutRow.dataType == 'special'}">
                                <g:if test="${layoutRow.column == 'accession'}">
                                    ${fieldValue(bean:experimentInstance,field:'accession')}
                                    <g:if test="${experimentInstance?.files.size() > 0}">
                                        <g:set var="fcount" value="${0}" />
                                        <g:each in="${experimentInstance.files}" var="file">
                                            <g:if test="${file.content.type=='Experiment Web Link'}">
                                                <g:set var="fcount" value="${fcount++}" />
                                                <g:if test="${fcount > 1}">, </g:if>
                                                <g:createFileLink content="${file.content}"
                                                                  displayLabel="${file.content.repository.repositoryType}"/>
                                            </g:if>
                                            <g:elseif test="${file.content.type=='Dataset Explorer Node Link'&&search==1}">
                                                <g:link controller="datasetExplorer" action="index" params="[path:file.content.location]">Dataset Explorer<img src="${resource(dir:'images', file:'internal-link.gif')}"/></g:link>
                                            </g:elseif>
                                        </g:each>
                                    </g:if>
                                    <g:if test="${searchId!=null}">
                                        | <g:link controller="search" action="newSearch" id="${searchId}">Search analyzed Data <img src="${resource(dir:'images', file:'internal-link.gif')}"/></g:link>
                                    </g:if>
                                </g:if>
                                <g:elseif test="${layoutRow.column == 'platforms'}">
                                    <g:each var="pf" in="${expPlatforms}">
                                        ${pf?.name.encodeAsHTML()}<br>
                                    </g:each>
                                </g:elseif>
                                <g:elseif test="${layoutRow.column == 'organism'}">
                                    <g:each var="og" in="${expOrganisms}">
                                        ${og?.encodeAsHTML()}<br>
                                    </g:each>
                                </g:elseif>
                            </g:elseif>

                            <g:else> <%-- In all other cases, display as string --%>
                                <g:if test="${fieldValue(bean:experimentInstance,field:layoutRow.column).length() > 325}">
                                    ${(fieldValue(bean:experimentInstance,field:layoutRow.column)).substring(0,324)}&nbsp;&nbsp;
                                    <a href=# >...See more</a>
                                </g:if>
                                <g:else>
                                    ${fieldValue(bean:experimentInstance,field:layoutRow.column)}
                                </g:else>
                            </g:else>
                        </td>
                    </tr>
                </g:if>
            </g:each>
        </tbody>
        <thead>
            <tr>                
                <th>&nbsp;</th>
                <th align="right">
                    <div align="right">Subject1&nbsp;Level&nbsp;Data&nbsp;is&nbsp;available&nbsp;
                        <a href="#subjectFake" onclick="showTab('analyze')"><img src="${resource(dir:'images', file:'application_go.png')}"/></a>
                    </div>
                </th>
            </tr>
        </thead>

    </table>
</div>
<span></span>
                    
       <div style="height:20px;"></div>
       <div style="width:1100px">
            <div id='gridViewWrapper1'>
            </div>        
        </div>

       <div style="height:30px;"></div>
       <div style="width:1100px">
            <div id='gridViewWrapper2'>
            </div>        
        </div>

<!--  overlay div  -->

<g:overlayDiv divId="${overlayDiv}" />
</div>
 
 <!-- background-color:#9CA4E4;  -->
