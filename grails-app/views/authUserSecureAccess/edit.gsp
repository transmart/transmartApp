<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Edit AuthUserSecureAccess</title>
</head>

<body>
<div class="body">
    <h1>Edit AuthUserSecureAccess</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${authUserSecureAccessInstance}">
        <div class="errors">
            <g:renderErrors bean="${authUserSecureAccessInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <input type="hidden" name="id" value="${authUserSecureAccessInstance?.id}"/>
        <input type="hidden" name="version" value="${authUserSecureAccessInstance?.version}"/>

        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="authUser">Auth User:</label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: authUserSecureAccessInstance, field: 'authUser', 'errors')}">
                        <g:select optionKey="id" from="${AuthUser.listOrderByUsername()}" name="authUser.id"
                                  value="${authUserSecureAccessInstance?.authUser?.id}"
                                  noSelection="['null': '']"></g:select>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="accessLevel">Access Level:</label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: authUserSecureAccessInstance, field: 'accessLevel', 'errors')}">
                        <g:select optionKey="id" optionValue="accessLevelName" from="${accessLevelList}"
                                  name="accessLevel.id"
                                  value="${authUserSecureAccessInstance?.accessLevel?.id}"></g:select>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="secureObject">Secure Object:</label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: authUserSecureAccessInstance, field: 'secureObject', 'errors')}">
                        <g:select optionKey="id" optionValue="displayName"
                                  from="${org.transmart.searchapp.SecureObject.listOrderByDisplayName()}"
                                  name="secureObject.id"
                                  value="${authUserSecureAccessInstance?.secureObject?.id}"></g:select>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" value="Update"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');"
                                                 value="Delete"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
