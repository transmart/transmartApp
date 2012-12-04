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
        <title>Folder List</title>
    </head>
    
    <body>
        <div class="body">
		<g:if test="${flash.message}">
			<div class="message">${flash.message}</div>
			<br/>
		</g:if>
		
		<g:form controller="FmFolder"> 									
			<table style="background-color: #E6E6E6;" class="default">
				<caption class="note">Instructions: click the 'New' button to add a Folder
					<span class="button" style="float:right;">			              
						<g:actionSubmit action="create" value="New Folder" />
					</span>											
				</caption>					
				</table>
		</g:form>
		
		<div>
		<table width="100%">
		<tr><td><h1>Folders (${fmFolderInstanceTotal})</h1></td><td align="right"></td></tr>
		</table>
		</div>	
		<div class="list">
			<table class="default">
			<thead>
				<tr>

					<th>Detail</th>
					<g:sortableColumn property="folderName" title="${message(code: 'fmFolderInstance.folderName.label', default: 'Name')}" />
					<g:sortableColumn property="folderFullName" title="${message(code: 'fmFolderInstance.folderFullName.label', default: 'Full Name')}" />
					<g:sortableColumn property="folderLevel" title="${message(code: 'fmFolderInstance.folderLevel.label', default: 'Folder Level')}" />
					<g:sortableColumn property="folderType" title="${message(code: 'fmFolderInstance.folderType.label', default: 'Folder Type')}" />
					<g:sortableColumn property="activeInd" title="${message(code: 'fmFolderInstance.activeInd.label', default: 'Active')}" />
					
				</tr>
			</thead>
			<tbody>
		<g:each in="${fmFolderInstanceList}" status="i" var="fmFolderInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                            <td>
                            <g:link action="show" id="${fmFolderInstance.id}"><img src="${resource(dir:'images/skin',file:'information.png',plugin:'rdc-core')}" alt="Detail link" border="0" /></g:link>
                            </td>
                            <td>${fmFolderInstance.folderName}</td>
                            <td>${fmFolderInstance.folderFullName}</td>
                            <td>${fmFolderInstance.folderLevel}</td>
                            <td>${fmFolderInstance.folderType}</td>
                           <td>${fmFolderInstance.activeInd}</td>
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate action="list" total="${fmFolderInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
