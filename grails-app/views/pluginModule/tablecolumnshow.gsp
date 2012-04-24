
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
