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

<div style="margin:10px;padding:10px;">
<h3 class="rdc-h3">
<g:if test="${folderInstance?.hasProperty('title')}">
${folderInstance?.title}
</g:if>
<g:else>
${folderInstance?.folderName}
</g:else>
</h3>
<g:if test="${folderInstance?.hasProperty('description')}">
<div style="line-height:14px;font-family:arial,​tahoma,​helvetica,​sans-serif; font-size: 12px;">
 <g:if test="${folderInstance?.description.length() > 325000}">
                       ${(folderInstance?.description).substring(0,324000)}&nbsp;&nbsp;
                       <a href=# >...See more</a>
                       </g:if>
                       <g:else>
                        ${folderInstance?.description}
                        </g:else></div>
<div style="height:20px;"></div>
</g:if>

<g:if test="${metaDataTagItems && metaDataTagItems.size()>0}">
<table class="details-table">
        <thead>
            <tr>                
                <th>&nbsp;</th>
                <th align="right"><g:remoteLink controller="fmFolder" action="editMetaData" update="${overlayDiv}" 
                        params="[eleId:overlayDiv, experimentId:folderInstance?.id]" 
                        before="initLoadingDialog('${overlayDiv}')" onComplete="centerDialog('${overlayDiv}')">
                  <img align="right" src="${resource(dir:'images', file:'pencil.png')}"/></g:remoteLink>
                </th>
            </tr>
        </thead>
    <tbody>
        <g:each in="${metaDataTagItems}" status="i" var="amTagItem">
          <g:if test="${amTagItem.viewInGrid}">
            <tr class='details-row'> <!-- class="${(i % 2) == 0 ? 'odd' : 'even'}"> -->
            <!-- TODO: If active -->
                <td valign="top" align="right" class="columnname" width="20%">${amTagItem.displayName}</td>
                <td valign="top" align="left" class="columnvalue" width="60%">
                <g:if test="${amTagItem.tagItemType == 'FIXED'  && amTagItem.tagItemAttr!=null?folderInstance?.hasProperty(amTagItem.tagItemAttr):false}" >
                      ${fieldValue(bean:folderInstance,field:amTagItem.tagItemAttr)}
                </g:if>
                 <g:elseif test="${amTagItem.tagItemType == 'CUSTOM'}">
                    TODO::custom                    
                 </g:elseif>
                 <g:elseif test="${amTagItem.tagItemType == 'PICKLIST'}">
                    TODO::picklist
                 </g:elseif>
                 <g:elseif test="${amTagItem.tagItemType == 'BIODATA'}">
                    TODO::biodata
                 </g:elseif>
                 <g:else>
                 unknown tag item type
                 </g:else>
                </td>
            </tr>
         </g:if>
        </g:each>
    </tbody>    
</table>
<span></span>
</g:if> 
                    
                   
<div style="height:20px;"></div>
<g:if test="${folderInstance?.hasProperty('fmFiles') && null!=folderInstance?.fmFiles && folderInstance?.fmFiles.size()>0}">   
<div style="align:center;" ><h4 class="rdc-h4" align="center" >Associated Files</h4></div>
<table class="list-table">
            <thead>
                <tr>                
                    <th>File Name</th>
                    <th>Create Date</th>
                    <th>Update Date</th>
                    <th>&nbsp;</th>
                </tr>
            </thead>
		    <tfoot>
		    	<tr>
		    		<td colspan="3">&nbsp;</td>
	    		   <td>
		               <div style="padding: 4px 0px;">
		                    <span class="foldericon addall link">Export all</span>
		               </div>
	               </td>
		    	</tr>
		    </tfoot>
    <tbody>
        <g:each in="${folderInstance?.fmFiles}" status="i" var="fmFile">
            <tr class="file-row">
                <td style="padding: 3px;"><span class="fileicon ${fmFile.fileType}"></span>&nbsp;${fmFile.displayName}</td>
               <td >
               <g:formatDate format="yyyy-MM-dd" date="${fmFile.createDate}" />
               </td> 
               <td >
               <g:formatDate format="yyyy-MM-dd" date="${fmFile.updateDate}" />
               </td> 
               <td>
	               <div>
	                    <span class="foldericon add link" name="${fmFile.id}">Add to export</span>
	               </div>
               </td>
                
            </tr>
        </g:each>
    </tbody>
</table>
</g:if>

<!--  overlay div  -->

<g:overlayDiv divId="${overlayDiv}" />
</div>
 
 <!-- background-color:#9CA4E4;  -->   
    
 