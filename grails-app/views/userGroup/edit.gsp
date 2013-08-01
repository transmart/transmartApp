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


<g:setProvider library="prototype"/>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="admin" />
        <title>Edit UserGroup</title>
    </head>
    <body>

        <div class="body">
            <h1>Edit UserGroup</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${userGroupInstance}">
            <div class="errors">
                <g:renderErrors bean="${userGroupInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${userGroupInstance?.id}" />
                <input type="hidden" name="version" value="${userGroupInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="enabled">Enabled:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userGroupInstance,field:'enabled','errors')}">
                                    <g:checkBox name="enabled" value="${userGroupInstance?.enabled}" ></g:checkBox>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="description">Description:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userGroupInstance,field:'description','errors')}">
                                    <textarea rows="5" cols="40" name="description">${fieldValue(bean:userGroupInstance, field:'description')}</textarea>
                                </td>
                            </tr>
                          <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userGroupInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:userGroupInstance,field:'name')}"/>
                                </td>
                            </tr>

                                <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="reports">Members:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:userGroupInstance,field:'members','errors')}">
                               <table>
                				<tr><td></td><td></td><td><input name="searchtext" id="searchtext"><button class="" onclick="${remoteFunction(action:'searchUsersNotInGroup',update:[success:'groupmembers', failure:''], id:userGroupInstance?.id, params:'$(\'searchtext\').serialize()' )}; return false;">Search Users</button></td>
                     				<tr><td>Members of group:</td><td></td><td>Available users:</td></tr>
                     				<tr id="groupmembers">
                                    <g:render template="addremove" bean="${userGroupInstance}" />
                                    </tr>
                                     </table>
                                	</div>
                                </td>
                            </tr>

                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
