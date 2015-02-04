<%@ page import="org.transmart.searchapp.CustomFilter" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title>Show CustomFilter</title>
</head>

<body>
<div class="nav">
    <span class="menuButton"><a class="home" href="${resource(dir: '')}">Home</a></span>
    <span class="menuButton"><g:link class="list" action="list">CustomFilter List</g:link></span>
    <span class="menuButton"><g:link class="create" action="create">New CustomFilter</g:link></span>
</div>

<div class="body">
    <h1>Show CustomFilter</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>
            <tr class="prop">
                <td valign="top" class="name"><g:message code="customFilterInstance.id" default="Id"/>:</td>

                <td valign="top" class="value">${fieldValue(bean: customFilterInstance, field: 'id')}</td>

            </tr>


            <tr class="prop">
                <td valign="top" class="name"><g:message code="customFilterInstance.name" default="Name"/>:</td>

                <td valign="top" class="value">${fieldValue(bean: customFilterInstance, field: 'name')}</td>

            </tr>


            <tr class="prop">
                <td valign="top" class="name"><g:message code="customFilterInstance.description"
                                                         default="Description"/>:</td>

                <td valign="top" class="value">${fieldValue(bean: customFilterInstance, field: 'description')}</td>

            </tr>


            <tr class="prop">
                <td valign="top" class="name"><g:message code="customFilterInstance.privateFlag"
                                                         default="Private Flag"/>:</td>

                <td valign="top" class="value">${fieldValue(bean: customFilterInstance, field: 'privateFlag')}</td>

            </tr>


            <tr class="prop">
                <td valign="top" class="name"><g:message code="customFilterInstance.items" default="Items"/>:</td>

                <td valign="top" style="text-align:left;" class="value">
                    <ul>
                        <g:each var="i" in="${customFilterInstance.items}">
                            <li><g:link controller="customFilterItem" action="show"
                                        id="${i.id}">${i?.encodeAsHTML()}</g:link></li>
                        </g:each>
                    </ul>
                </td>

            </tr>


            <tr class="prop">
                <td valign="top" class="name"><g:message code="customFilterInstance.searchUserId"
                                                         default="Search User Id"/>:</td>

                <td valign="top" class="value">${fieldValue(bean: customFilterInstance, field: 'searchUserId')}</td>

            </tr>

            </tbody>
        </table>
    </div>

    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${customFilterInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" value="Edit"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');"
                                                 value="Delete"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
