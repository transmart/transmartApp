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
<%! import annotation.* %> 
<%! import com.recomdata.util.* %> 

<g:set var="overlayDiv" value="metaData_div" />

<div style="margin:10px;padding:10px;">

<div>
	<div style="float: right">
		<%-- Add buttons here depending on folder type --%>
		
		<sec:ifAnyGranted roles="ROLE_ADMIN">
			<g:if test="${folder.folderType.equalsIgnoreCase(FolderType.PROGRAM.name())}">
				<span class="greybutton buttonicon addstudy">Add new study</span>
			</g:if>
		</sec:ifAnyGranted>
		
		<g:if test="${folder.folderType.equalsIgnoreCase(FolderType.STUDY.name())}">
			<span class="greybutton buttonicon addassay">Add new assay</span>
			<span class="greybutton buttonicon addfolder">Add new folder</span>
		</g:if>
		
		<g:if test="${folder.folderType.equalsIgnoreCase(FolderType.FOLDER.name()) || folder.folderType.equalsIgnoreCase(FolderType.ASSAY.name()) || folder.folderType.equalsIgnoreCase(FolderType.ANALYSIS.name())}">
			<span class="greybutton buttonicon addfolder">Add new folder</span>
		</g:if>
	</div>
	<h3 class="rdc-h3">
		<g:if test="${bioDataObject?.hasProperty('title')}">
		${bioDataObject?.title}
		</g:if>
		<g:else>
		${bioDataObject?.folderName}
		</g:else>
	</h3>
</div>
<g:if test="${bioDataObject?.hasProperty('description')}">
<div style="line-height:14px;font-family:arial,​tahoma,​helvetica,​sans-serif; font-size: 12px;">
 <g:if test="${bioDataObject?.description.length() > 325000}">
                       ${(bioDataObject?.description).substring(0,324000)}&nbsp;&nbsp;
                       <a href=# >...See more</a>
                       </g:if>
                       <g:else>
                        ${bioDataObject?.description}
                        </g:else></div>
<div style="height:20px;"></div>
</g:if>

<g:if test="${metaDataTagItems && metaDataTagItems.size()>0}">
<table class="details-table">
        <thead>
            <tr>                
                <th>&nbsp;</th>
                <th align="right"><g:remoteLink controller="fmFolder" action="editMetaData" update="${overlayDiv}" 
                        params="[eleId:overlayDiv, folderId:folder?.id]" 
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
                <g:if test="${amTagItem.tagItemType == 'FIXED'  && amTagItem.tagItemAttr!=null?bioDataObject?.hasProperty(amTagItem.tagItemAttr):false}" >
                      ${fieldValue(bean:bioDataObject,field:amTagItem.tagItemAttr)}
                </g:if>
                <g:else>      
	                <g:set var="tagValues" value="${AmTagDisplayValue.findAll('from AmTagDisplayValue a where a.subjectUid=? and a.amTagItem.id=?',[folder.objectUid,amTagItem.id])}"/>
	                <g:if test="${tagValues!=null}">
	             	   <g:set var="counter" value="0"/>
	             		 <g:each var="tagValue" status="k" in="${tagValues}">
						      <g:if test="${counter==1}">, </g:if>${tagValue.displayValue}
						      <g:set var="counter" value="${counter.toLong() + 1}"/>
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
                    
                   
<div style="height:20px;"></div>
<g:if test="${bioDataObject?.hasProperty('fmFiles') && null!=bioDataObject?.fmFiles && bioDataObject?.fmFiles.size()>0}">   
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
        <g:each in="${bioDataObject?.fmFiles}" status="i" var="fmFile">
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
    
 