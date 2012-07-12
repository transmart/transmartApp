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
<%@ page import="com.recomdata.transmart.plugin.PluginModuleCategory" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create PluginModule</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">PluginModule List</g:link></span>
        </div>
        <div class="body">
            <h1>Create PluginModule</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${pluginModuleInstance}">
            <div class="errors">
                <g:renderErrors bean="${pluginModuleInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="name">Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:pluginModuleInstance,field:'name','errors')}">
                                    <input type="text" id="name" name="name" value="${fieldValue(bean:pluginModuleInstance,field:'name')}"/>
                                </td>
                            </tr> 

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="moduleName">Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:pluginModuleInstance,field:'moduleName','errors')}">
                                    <input type="text" id="moduleName" name="moduleName" value="${fieldValue(bean:pluginModuleInstance,field:'moduleName')}"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="active">Active:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:pluginModuleInstance,field:'active','errors')}">
                                    <g:checkBox name="active" value="${pluginModuleInstance?.active}" ></g:checkBox>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="hasForm">Has Form:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:pluginModuleInstance,field:'hasForm','errors')}">
                                    <g:checkBox name="hasForm" value="${pluginModuleInstance?.hasForm}" ></g:checkBox>
                                </td>
                            </tr> 
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="category">Category:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:pluginModuleInstance,field:'category','errors')}">
                                    <g:select name="category" from="${PluginModuleCategory.values()}" 
                                    value="${fieldValue(bean:pluginModuleInstance,field:'category')}" 
                                    optionKey="key" optionValue="value"></g:select>
                                </td>
                            </tr> 
                            
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="formLink">Form Link:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:pluginModuleInstance,field:'formLink','errors')}">
                                    <input type="text" id="formLink" name="formLink" value="${fieldValue(bean:pluginModuleInstance,field:'formLink')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="formPage">Form Page:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:pluginModuleInstance,field:'formPage','errors')}">
                                    <input type="text" id="formPage" name="formPage" value="${fieldValue(bean:pluginModuleInstance,field:'formPage')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="params">Params:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:pluginModuleInstance,field:'params','errors')}">
                                    <textarea id="paramsStr" name="paramsStr">${fieldValue(bean:pluginModuleInstance,field:'params')}</textarea>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="plugin">Plugin:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:pluginModuleInstance,field:'plugin','errors')}">
                                    <g:select optionKey="id" optionValue="name" from="${com.recomdata.transmart.plugin.Plugin.list()}" name="plugin.id" value="${pluginModuleInstance?.plugin?.id}" ></g:select>
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
