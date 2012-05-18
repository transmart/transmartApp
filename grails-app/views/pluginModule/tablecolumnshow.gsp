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


<%@ page import="com.recomdata.transmart.plugin.PluginModule" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show PluginModule</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">PluginModule List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New PluginModule</g:link></span>
        </div>
        <div class="body">
            <h1>Show PluginModule</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginModuleInstance.id" default="Id"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginModuleInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginModuleInstance.name" default="Name"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginModuleInstance, field:'name')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginModuleInstance.active" default="Active"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginModuleInstance, field:'active')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginModuleInstance.hasForm" default="Has Form"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginModuleInstance, field:'hasForm')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginModuleInstance.formLink" default="Form Link"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginModuleInstance, field:'formLink')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginModuleInstance.formPage" default="Form Page"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginModuleInstance, field:'formPage')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginModuleInstance.params" default="Params"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginModuleInstance, field:'params')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginModuleInstance.plugin" default="Plugin"/>:</td>
                            
                            <td valign="top" class="value"><g:link controller="plugin" action="show" id="${pluginModuleInstance?.plugin?.id}">${pluginModuleInstance?.plugin?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${pluginModuleInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
