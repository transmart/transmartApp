<%@ page import="com.recomdata.transmart.plugin.Plugin" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title>Show Plugin</title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><a class="home" href="${createLinkTo(dir: '')}">Home</a></span>
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
                <td valign="top" class="name">Id:</td>

                <td valign="top" class="value">${fieldValue(bean: pluginInstance, field: 'id')}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name">Name:</td>

                <td valign="top" class="value">${fieldValue(bean: pluginInstance, field: 'name')}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name">Plugin Name:</td>

                <td valign="top" class="value">${fieldValue(bean: pluginInstance, field: 'pluginName')}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name">Has Modules:</td>

                <td valign="top" class="value"><g:checkBox name="hasModules" value="${pluginInstance?.hasModules}"
                                                           disabled="true"></g:checkBox></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name">Has Form:</td>

                <td valign="top" class="value"><g:checkBox name="hasForm" value="${pluginInstance?.hasForm}"
                                                           disabled="true"></g:checkBox></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name">Active:</td>

                <td valign="top" class="value"><g:checkBox name="active" value="${pluginInstance?.active}"
                                                           disabled="true"></g:checkBox></td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name">Default Link:</td>

                <td valign="top" class="value">${fieldValue(bean: pluginInstance, field: 'defaultLink')}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name">Form Link:</td>

                <td valign="top" class="value">${fieldValue(bean: pluginInstance, field: 'formLink')}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name">Form Page:</td>

                <td valign="top" class="value">${fieldValue(bean: pluginInstance, field: 'formPage')}</td>

            </tr>

            <tr class="prop">
                <td valign="top" class="name">Modules:</td>

                <td valign="top" style="text-align:left;" class="value">
                    <ul>
                        <g:each var="m" in="${pluginInstance.modules}">
                            <li><g:link controller="pluginModule" action="show" id="${m.id}">${m?.name}</g:link></li>
                        </g:each>
                    </ul>
                </td>

            </tr>

            </tbody>
        </table>
    </div>

    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${pluginInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" value="Edit"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');"
                                                 value="Delete"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
