<%@ page import="com.recomdata.transmart.plugin.PluginModule" %>
<%@ page import="com.recomdata.transmart.plugin.PluginModuleCategory" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title>Create PluginModule</title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><a class="home" href="${createLinkTo(dir: '')}">Home</a></span>
    <span class="menuButton"><g:link class="list" action="list">PluginModule List</g:link></span>
</div>

<div class="body">
    <h1>Create PluginModule</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${pluginModuleInstance}">
        <div class="errors">
            <g:renderErrors bean="${pluginModuleInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save" method="post">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name">Name:</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: pluginModuleInstance, field: 'name', 'errors')}">
                        <input type="text" id="name" name="name"
                               value="${fieldValue(bean: pluginModuleInstance, field: 'name')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="moduleName">Name:</label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: pluginModuleInstance, field: 'moduleName', 'errors')}">
                        <input type="text" id="moduleName" name="moduleName"
                               value="${fieldValue(bean: pluginModuleInstance, field: 'moduleName')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="active">Active:</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: pluginModuleInstance, field: 'active', 'errors')}">
                        <g:checkBox name="active" value="${pluginModuleInstance?.active}"></g:checkBox>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="hasForm">Has Form:</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: pluginModuleInstance, field: 'hasForm', 'errors')}">
                        <g:checkBox name="hasForm" value="${pluginModuleInstance?.hasForm}"></g:checkBox>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="category">Category:</label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: pluginModuleInstance, field: 'category', 'errors')}">
                        <g:select name="category" from="${PluginModuleCategory.values()}"
                                  value="${fieldValue(bean: pluginModuleInstance, field: 'category')}"
                                  optionKey="key" optionValue="value"></g:select>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="formLink">Form Link:</label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: pluginModuleInstance, field: 'formLink', 'errors')}">
                        <input type="text" id="formLink" name="formLink"
                               value="${fieldValue(bean: pluginModuleInstance, field: 'formLink')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="formPage">Form Page:</label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: pluginModuleInstance, field: 'formPage', 'errors')}">
                        <input type="text" id="formPage" name="formPage"
                               value="${fieldValue(bean: pluginModuleInstance, field: 'formPage')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="params">Params:</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: pluginModuleInstance, field: 'params', 'errors')}">
                        <textarea id="paramsStr"
                                  name="paramsStr">${fieldValue(bean: pluginModuleInstance, field: 'params')}</textarea>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="plugin">Plugin:</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: pluginModuleInstance, field: 'plugin', 'errors')}">
                        <g:select optionKey="id" optionValue="name"
                                  from="${com.recomdata.transmart.plugin.Plugin.list()}" name="plugin.id"
                                  value="${pluginModuleInstance?.plugin?.id}"></g:select>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><input class="save" type="submit" value="Create"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
