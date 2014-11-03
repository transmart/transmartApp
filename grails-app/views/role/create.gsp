<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Create Role</title>
</head>

<body>
<div class="body">

    <h1>Create Role</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${authority}">
        <div class="errors">
            <g:renderErrors bean="${authority}" as="list"/>
        </div>
    </g:hasErrors>

    <g:form action="save">
        <div class="dialog">
            <table>
                <tbody>
                <tr class="prop">
                    <td valign="top" class="name"><label for="authority">Role Name:</label></td>
                    <td valign="top" class="value ${hasErrors(bean: authority, field: 'authority', 'errors')}">
                        <input type="text" id="authority" name="authority"
                               value="${authority?.authority?.encodeAsHTML()}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name"><label for="description">Description:</label></td>
                    <td valign="top" class="value ${hasErrors(bean: authority, field: 'description', 'errors')}">
                        <input type="text" id="description" name="description"
                               value="${authority?.description?.encodeAsHTML()}"/>
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