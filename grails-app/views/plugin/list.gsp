
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
