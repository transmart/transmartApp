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


<%@ page import="com.recomdata.transmart.plugin.Plugin" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Plugin List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New Plugin</g:link></span>
        </div>
        <div class="body">
            <h1>Plugin List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="name" title="Name" />
                        
                   	        <g:sortableColumn property="pluginName" title="Plugin Name" />
                        
                   	        <g:sortableColumn property="hasModules" title="Has Modules" />
                        
                   	        <g:sortableColumn property="hasForm" title="Has Form" />
                   	        
                   	        <g:sortableColumn property="active" title="Active" />
                        
                   	        <g:sortableColumn property="defaultLink" title="Default Link" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${pluginInstanceList}" status="i" var="pluginInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${pluginInstance.id}">${fieldValue(bean:pluginInstance, field:'id')}</g:link></td>
                        
                            <td>${fieldValue(bean:pluginInstance, field:'name')}</td>
                        
                            <td>${fieldValue(bean:pluginInstance, field:'pluginName')}</td>
                        
                            <td><g:checkBox name="hasModules" value="${pluginInstance?.hasModules}" disabled="true"></g:checkBox></td>
                        
                            <td><g:checkBox name="hasForm" value="${pluginInstance?.hasForm}" disabled="true"></g:checkBox></td>
                            
                            <td><g:checkBox name="active" value="${pluginInstance?.active}" disabled="true"></g:checkBox></td>
                        
                            <td>${fieldValue(bean:pluginInstance, field:'defaultLink')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${pluginInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
