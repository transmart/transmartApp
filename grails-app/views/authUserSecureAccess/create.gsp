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
        <title>Grant New Access Control</title>
    </head>
    <body>
        <div class="body">
            <h1>Grant New Access Control</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${authUserSecureAccessInstance}">
            <div class="errors">
                <g:renderErrors bean="${authUserSecureAccessInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="authUser">Auth User:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:authUserSecureAccessInstance,field:'authUser','errors')}">
                                    <g:select id="authUser" optionKey="id"  from="${AuthUser.listOrderByUsername()}" name="authUser.id" value="${authUserSecureAccessInstance?.authUser?.id}" noSelection="['null':'']"
                                    onchange="${remoteFunction(action:'listAccessLevel',
                                                                 update:'accessLevelList',
                                                                 params:'\'id=\'+this.value')}"
                                    ></g:select>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="accessLevelList">Access Level:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:authUserSecureAccessInstance,field:'accessLevel','errors')}">
                                    <g:select id="accessLevelList" optionKey="id"  optionValue="accessLevelName" from="${SecureAccessLevel.listOrderByAccessLevelValue()}" name="accessLevel.id" value="${authUserSecureAccessInstance?.accessLevel?.id}" ></g:select>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="secureObject">Study:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:authUserSecureAccessInstance,field:'secureObject','errors')}">
                                    <g:select id="secureObject" optionKey="id" optionValue="displayName" from="${SecureObject.listOrderByDisplayName()}" name="secureObject.id" value="${authUserSecureAccessInstance?.secureObject?.id}" ></g:select>
                                </td>
                            </tr>

                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
