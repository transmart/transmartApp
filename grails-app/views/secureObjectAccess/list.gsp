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
<%@ page import="org.transmart.searchapp.SecureObjectAccess"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>SecureObjectAccess List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New SecureObjectAccess</g:link></span>
        </div>
        <div class="body">
            <h1>SecureObjectAccess List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <th><g:message code="secureObjectAccessInstance.id" default="Id"/></th>
                   	   
                        
                   	        <th><g:message code="secureObjectAccessInstance.principal" default="Principal"/></th>
                   	   
                        
                   	        <th><g:message code="secureObjectAccessInstance.accessLevel" default="Access Level"/></th>
                   	   
                        
                   	        <th><g:message code="secureObjectAccessInstance.secureObject" default="Study"/></th>
                   	   
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${secureObjectAccessInstanceList}" status="i" var="secureObjectAccessInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${secureObjectAccessInstance.id}">${fieldValue(bean:secureObjectAccessInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:secureObjectAccessInstance, field:'id')}</td>
                        
                            <td>${fieldValue(bean:secureObjectAccessInstance, field:'principal')}</td>
                        
                            <td>${fieldValue(bean:secureObjectAccessInstance, field:'accessLevel')}</td>
                        
                            <td>${fieldValue(bean:secureObjectAccessInstance, field:'secureObject')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${SecureObjectAccess.count()}" />
            </div>
        </div>
    </body>
</html>
