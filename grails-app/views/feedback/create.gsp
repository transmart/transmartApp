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
        <meta name="layout" content="main" />
        <title>Create Feedback</title>
 		<link rel="stylesheet" href="${resource(dir:'css',file:'feedback.css')}" />
    </head>
    <body>
		<g:render template="/layouts/commonheader" model="['app':'feedback']" />
        <div class="nav">
            <span class="menuButton"><g:link class="list" action="list">Feedback List</g:link></span>
        </div>
        <div class="body">
            <h1>Create Feedback</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${feedback}">
            <div class="errors">
                <g:renderErrors bean="${feedback}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="appuser">User:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:feedback,field:'searchUserId','errors')}">
                                    <input type="text" id="searchUserId" name="searchUserId" value="${fieldValue(bean:feedback,field:'searchUserId')}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="createDate">Created:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:feedback,field:'createDate','errors')}">
                                    <g:datePicker name="createDate" value="${feedback?.createDate}" precision="day"></g:datePicker>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="appVersion">Version:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:feedback,field:'appVersion','errors')}">
                                    <input type="text" id="appVersion" name="appVersion" value="${fieldValue(bean:feedback,field:'appVersion')}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="feedbackText">Feedback:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:feedback,field:'feedbackText','errors')}">
                                    <textarea id="feedbackText" name="feedbackText" rows="10" cols="100">${fieldValue(bean:feedback,field:'feedbackText')}</textarea>
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
