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
<%@ page import="org.transmart.searchapp.Principal"%>
<%@ page import="org.transmart.searchapp.SecureAccessLevel"%>
<%@ page import="org.transmart.searchapp.SecureObject"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create SecureObjectAccess</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">SecureObjectAccess List</g:link></span>
        </div>
        <div class="body">
            <h1>Create SecureObjectAccess</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${secureObjectAccessInstance}">
            <div class="errors">
                <g:renderErrors bean="${secureObjectAccessInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="principal">Principal:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:secureObjectAccess,field:'principal','errors')}">
                                    <g:select optionKey="id" from="${Principal.list()}" name="principal.id" value="${secureObjectAccess?.principal?.id}" noSelection="['null':'']"></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="accessLevel">Access Level:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:secureObjectAccess,field:'accessLevel','errors')}">
                                    <g:select optionKey="id" from="${SecureAccessLevel.list()}" name="accessLevel.id" value="${secureObjectAccess?.accessLevel?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="secureObject">Study:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:secureObjectAccess,field:'secureObject','errors')}">
                                    <g:select optionKey="id" from="${SecureObject.list()}" name="secureObject.id" value="${secureObjectAccess?.secureObject?.id}" ></g:select>
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
