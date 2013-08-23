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



<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="admin" />
        <title>Create Study</title>
    </head>
    <body>
        <div class="body">
            <h1>Create Study</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${secureObjectInstance}">
            <div class="errors">
                <g:renderErrors bean="${secureObjectInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>

                        <tr class="prop">
                            <td valign="top" class="name">
                                <label for="displayName">Study Name:</label>
                            </td>
                            <td valign="top" class="value ${hasErrors(bean:secureObjectInstance,field:'displayName','errors')}">
                                <input type="text" id="displayName" name="displayName" value="${fieldValue(bean:secureObjectInstance,field:'displayName')}"/>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label for="dataType">Data Type:</label>
                            </td>
                            <td valign="top" class="value ${hasErrors(bean:secureObjectInstance,field:'dataType','errors')}">
                                <textarea rows="5" cols="40" id="dataType" name="dataType">${fieldValue(bean:secureObjectInstance, field:'dataType')}</textarea>
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label for="bioDataId">Bio Data Id:</label>
                            </td>
                            <td valign="top" class="value ${hasErrors(bean:secureObjectInstance,field:'bioDataId','errors')}">
                                <input type="text" id="bioDataId" name="bioDataId" value="${fieldValue(bean:secureObjectInstance,field:'bioDataId')}" />
                            </td>
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">
                                <label for="bioDataUniqueId">Bio Data Unique Id:</label>
                            </td>
                            <td valign="top" class="value ${hasErrors(bean:secureObjectInstance,field:'bioDataUniqueId','errors')}">
                                <input type="text" id="bioDataUniqueId" name="bioDataUniqueId" value="${fieldValue(bean:secureObjectInstance,field:'bioDataUniqueId')}"/>
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
