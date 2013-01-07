<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.    You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.    You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="admin" />
        <title>Folder Details</title>
    </head>
    
    <body>
        <div class="body">
	
		<g:if test="${flash.message}"><div class="message">${flash.message}</div></g:if>
			<g:form controller="fmFolder"> 									

			<table style="background-color: #E6E6E6;" class="default">
				<caption class="note">Instructions: click the 'New' button to add a Folder
					<span class="button" style="float:right;">			         
					    <g:hiddenField name="id" value="${fmFolderInstance?.id}" />
					     
						<g:actionSubmit action="create" value="New Folder" />
					</span>											
				</caption>
					
				</table>
		</g:form>
		<div>
		<table width="100%">
		<tr><td><h1>Folder</h1></td><td align="right"></td></tr>
		</table>
		</div>	
		
		<div class="dialog">
			<table class="detail" style="width:600px;">
			<tbody>
                    <g:hiddenField name="id" value="${fmFolderInstance?.id}" />
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="fmFolderInstance.folderName.label" default="Folder Name" /> :</td>
                            <td valign="top" class="value" colspan=2>${fieldValue(bean: fmFolderInstance, field: "folderName")}</td>
                        </tr>
                               
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="fmFolderInstance.folderFullName.label" default="Folder Full Name" /> :</td>
                            <td valign="top" class="value" colspan=2>${fieldValue(bean: fmFolderInstance, field: "folderFullName")}</td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="fmFolderInstance.folderlevel.label" default="Folder Level" /> :</td>
                            <td valign="top" class="value" colspan=2>${fieldValue(bean: fmFolderInstance, field: "folderLevel")}</td>
                        </tr>

                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="fmFolderInstance.folderType.label" default="Folder Type" /> :</td>
                            <td valign="top" class="value" colspan=2>${fieldValue(bean: fmFolderInstance, field: "folderType")}</td>
                        </tr>
                        
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="fmFolderInstance.activeInd.label" default="Active" /> :</td>
                            <td valign="top" class="value" colspan=2><g:checkBox name="activeInd" value="${fmFolderInstance?.activeInd}" disabled='true' />
                        </tr>
                                                         
                        


               </tbody>
             </table>
            </div>
            <div>
            <richui:treeView xml="${data}"/>
            </div>
            <div class="buttons">
                <g:form>
                    <g:hiddenField name="id" value="${fmFolderInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" action="edit" value="${message(code: 'default.button.edit.label', default: 'Edit')}" /></span>
					<span class="button"><g:actionSubmit class="list" action="list" value="Close"/></span>				
		            <span class="button"><g:actionSubmit class="delete" action="delete" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
