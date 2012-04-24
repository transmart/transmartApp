
<%@ page import="com.recomdata.transmart.plugin.Plugin" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Plugin</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">Plugin List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Plugin</g:link></span>
        </div>
        <div class="body">
            <h1>Show Plugin</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginInstance.id" default="Id"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginInstance.name" default="Name"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginInstance, field:'name')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginInstance.pluginName" default="Plugin Name"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginInstance, field:'pluginName')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginInstance.hasModules" default="Has Modules"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginInstance, field:'hasModules')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginInstance.hasForm" default="Has Form"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginInstance, field:'hasForm')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginInstance.defaultLink" default="Default Link"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginInstance, field:'defaultLink')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginInstance.formLink" default="Form Link"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginInstance, field:'formLink')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginInstance.formPage" default="Form Page"/>:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:pluginInstance, field:'formPage')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="pluginInstance.modules" default="Modules"/>:</td>
                            
                            <td  valign="top" style="text-align:left;" class="value">
                                <ul>
                                <g:each var="m" in="${pluginInstance.modules}">
                                    <li><g:link controller="pluginModule" action="show" id="${m.id}">${m?.encodeAsHTML()}</g:link></li>
                                </g:each>
                                </ul>
                            </td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${pluginInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
