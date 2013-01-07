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
        <title>Create Folder</title>
    </head>
    
    <body>
        <div class="body">
		<g:if test="${flash.message}"><div class="message">${flash.message}</div></g:if>

		<div>
		<table width="100%">
		<tr><td><h1>Create Folder</h1></td><td align="right"></td></tr>
		</table>
		</div>	
	
        <g:hasErrors bean="${fmFolderInstance}">
        <div class="errors">
            <g:renderErrors bean="${fmFolderInstance}" as="list" />
        </div>
        </g:hasErrors>
		
		<g:form action="save">
                <g:hiddenField name="id" value="${fmFolderInstance?.id}" />
                <g:hiddenField name="version" value="${fmFolderInstance?.version}" />
			<div class="dialog">
				<table class="detail">
				<tbody>				
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="fmFolderInstance.folderName.label" default="Name" /> <g:requiredIndicator/> :</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: fmFolderInstance, field: 'folderName', 'errors')}">
                                    <g:textField size="50" name="folderName" value="${fmFolderInstance?.folderName}" />
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="name"><g:message code="fmFolderInstance.folderType.label" default="Folder Type" /> <g:requiredIndicator/> :</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: fmFolderInstance, field: 'folderType', 'errors')}">
                                    <g:select name="folderType" id="folderType" from="${com.recomdata.util.FolderType.values()}" value="${fieldValue(bean: fmFolderInstance, field: 'folderType')}" />
                                </td>
                            </tr>
                           <tr class="prop">
                                <td valign="top" class="name">
                                  <label for="description"><g:message code="fmFolderInstance.active.label" default="Active" /> :</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean: fmFolderInstance, field: 'activeInd', 'errors')}">
                                 <g:checkBox name="activeInd" value="${fmFolderInstance?.activeInd}" />
                                </td>
                            </tr>
                           
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" action="save" id="save" value="${message(code: 'default.button.update.label', default: 'Save')}" /></span>
  					<span class="button"><g:actionSubmit class="list" action="list"  id="cancel" value="Cancel" onclick="return confirm('Are you sure?')"/></span>				
                </div>
            </g:form>
        </div>
    </body>
</html>
