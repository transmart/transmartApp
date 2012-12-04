<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->

<g:set var="overlayDiv" value="metaData_div" />
<script type="text/javascript">
    $j(document).ready(function() 
    {
        var dt1 = new dataTableWrapper('gridViewWrapper1', 'gridViewTable1');
        dt1.loadData(${jSONForGrid});
  
    });

    $('#gridViewTable1').dataTable( {
        "sDom": '<"toolbar">frtip'
    } );
    $("div.toolbar").html('<b>Custom tool bar! Text/images etc.</b>');

</script>

    

<div style="margin:10px;padding:10px;">
<h3 class="rdc-h3">${folderInstance?.folderName}</h3>
<g:if test="${layout}">
<table class="details-table">
            <thead>
                <tr>                
                    <th>&nbsp;</th>
                    <th align="right"><g:remoteLink controller="fmFolder" action="editMetaData" update="${overlayDiv}" 
                            params="[eleId:overlayDiv, experimentId:folderInstance.id]" 
                            before="initLoadingDialog('${overlayDiv}')" onComplete="centerDialog('${overlayDiv}')">
                      <img align="right" src="${resource(dir:'images', file:'pencil.png')}"/></g:remoteLink>
                      </th>
                </tr>
            </thead>
            
    <tbody>
        <g:each in="${layout}" status="i" var="layoutRow">
            <tr class='details-row'> <!-- class="${(i % 2) == 0 ? 'odd' : 'even'}"> -->
                <td valign="top" align="right" class="columnname" width="20%">${layoutRow.displayName}</td>
                
                                    <td valign="top" align="left" class="columnvalue" width="60%">
                    <g:if test="${layoutRow.dataType == 'date'}">
                    </g:if>
                    <g:else> <%-- In all other cases, display as string --%>
                       <g:if test="${layoutRow.column.length() > 325}">
                       ${(layoutRow.column).substring(0,324)}&nbsp;&nbsp;
                       <a href=# >See more</a>
                       </g:if>
                       <g:else>
                        ${layoutRow.column}
                        </g:else>
                    </g:else>
                </td>
                
            
            </tr>
        </g:each>
    </tbody>    
</table>
<span></span>
</g:if>  
                    
                   
<div style="height:20px;"></div>
<g:if test="${null!=folderInstance.fmFiles && folderInstance.fmFiles.size()>0}">   
<div style="width:900px;align:center;" ><h4 class="rdc-h4" align="center" >Associated Files</h4></div>
<table class="list-table">
            <thead>
                <tr>                
                    <th>File Name</th>
                    <th>File Description</th>
                    <th>Upload Date</th>
                    <th>Export All</th>
                </tr>
            </thead>
            
    <tbody>
        <g:each in="${folderInstance.fmFiles}" status="i" var="fmFile">
            <tr class="file-row">
                <td style="padding: 3px 3px 3px 3px;"><span class="fileicon ${fmFile.fileType}"></span>&nbsp;${fmFile.displayName}</td>
                <td>${fmFile.description}</td>
               <td >
               <g:formatDate format="yyyy-MM-dd" date="${fmFile.uploadDate}" />
               </td> 
               <td>
               <div>
                    <span class="foldericon view">View metadata</span>
                    <span class="foldericon add">Add to export</span>
                          </div>
                </td>
                
            </tr>
        </g:each>
    </tbody>    
</table>
        </g:if>
        <g:else>
       <div>
            <div id='gridViewWrapper1'>
            </div>        
        </div>
        </g:else>

<!--  overlay div  -->

<g:overlayDiv divId="${overlayDiv}" />
</div>
 
 <!-- background-color:#9CA4E4;  -->   
    
 