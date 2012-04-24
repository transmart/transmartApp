
<%@ page import="com.recomdata.transmart.plugin.PluginModule" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>PluginModule List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New PluginModule</g:link></span>
        </div>
        <div class="body">
            <h1>PluginModule List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="name" title="Name" />
                        
                   	        <g:sortableColumn property="active" title="Active" />
                   	        
                   	        <g:sortableColumn property="category" title="Category" />
                        
                   	        <g:sortableColumn property="hasForm" title="Has Form" />
                        
                   	        <g:sortableColumn property="formLink" title="Form Link" />
                        
                   	        <g:sortableColumn property="formPage" title="Form Page" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${pluginModuleInstanceList}" status="i" var="pluginModuleInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${pluginModuleInstance.id}" style="font-size:12px">${fieldValue(bean:pluginModuleInstance, field:'name')}</g:link></td>
                        
                            <td align="center"><g:checkBox name="" value="${pluginModuleInstance?.active}" disabled="true"></g:checkBox></td>
                            
                            <td>${fieldValue(bean:pluginModuleInstance, field:'category')}</td>
                        
                            <td align="center"><g:checkBox name="" value="${pluginModuleInstance?.hasForm}" disabled="true"></g:checkBox></td>
                        
                            <td>${fieldValue(bean:pluginModuleInstance, field:'formLink')}</td>
                        
                            <td>${fieldValue(bean:pluginModuleInstance, field:'formPage')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${pluginModuleInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
