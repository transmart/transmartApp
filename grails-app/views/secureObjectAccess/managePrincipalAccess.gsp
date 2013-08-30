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
<%@ page import="org.transmart.searchapp.SecureAccessLevel"%>
<%@ page import="org.transmart.searchapp.SecureObject"%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin" />
    <title>Access Control by Study</title>
</head>
<body>
<div class="body">
    <h1>Access Control by Study</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${secureObjectInstance}">
        <div class="errors">
            <g:renderErrors bean="${secureObjectInstance}" as="list" />
        </div>
    </g:hasErrors>

    <div class="dialog">
        <g:form method="post" name="secobjaccessform" action="manageAccessBySecObj" >
            <table>
                <tbody>
                <tr class="prop">
                <td valign="top" class="name" >
                <label for="secureobjectid">Study:</label>
                <g:select optionKey="id" optionValue="displayName" from="${SecureObject.listOrderByDisplayName()}" name="secureobjectid" value="${secureObjectInstance?.id}" onchange="document.secobjaccessform.submit();"></g:select>
                </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name" >
                        <label for="accesslevelid">Access Level:</label>
                        <g:select optionKey="id" optionValue="accessLevelName" from="${SecureAccessLevel.list()}" name="accesslevelid" value="${accesslevelid}" onchange="document.secobjaccessform.submit();"></g:select>
                    </td>
                </tr>
                </tbody>
            </table>

            <tr><td>&nbsp;</td><td>&nbsp;</td><td><input name="searchtext" id="searchtext">
                <input type="submit" value="Search User/Groups"/></td></tr>
        </g:form>
        <table><tbody>
        <tr><td><b>User/Group Assigned Access</b></td><td></td><td><b>User/Group Without Access</b></td></tr>
        <tr id="groups">
            <g:render template="addremovePrincipal" model="['userwithoutaccess' :userwithoutaccess, 'secureObjectAccessList':secureObjectAccessList]" />
        </tr>
        </tbody>
        </table>
    </div>
</div>
</body>
</html>
