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
        <title>Show AccessLog</title>
    </head>
    <body>
          <div class="body">
            <h1>Show AccessLog</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>



                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="accessLogInstance.id" default="Id"/>:</td>

                            <td valign="top" class="value">${fieldValue(bean:accessLogInstance, field:'id')}</td>

                        </tr>


                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="accessLogInstance.username" default="User"/>:</td>

                            <td valign="top" class="value">${fieldValue(bean:accessLogInstance, field:'username')}</td>

                        </tr>


                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="accessLogInstance.event" default="Event"/>:</td>

                            <td valign="top" class="value">${fieldValue(bean:accessLogInstance, field:'event')}</td>

                        </tr>


                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="accessLogInstance.eventmessage" default="Event Message"/>:</td>

                            <td valign="top" class="value">${fieldValue(bean:accessLogInstance, field:'eventmessage')}</td>

                        </tr>



                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="accessLogInstance.accesstime" default="Access Time"/>:</td>

                            <td valign="top" class="value">${fieldValue(bean:accessLogInstance, field:'accesstime')}</td>

                        </tr>

                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${accessLogInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
