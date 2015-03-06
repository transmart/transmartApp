<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>User</title>
</head>

<body>
<div class="body">
    <h1>User</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name">WWID:</td>
                <td valign="top" class="value">${person.id}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">Login Name:</td>
                <td valign="top" class="value">${person.username?.encodeAsHTML()}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">Full Name:</td>
                <td valign="top" class="value">${person.userRealName?.encodeAsHTML()}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">Enabled:</td>
                <td valign="top" class="value">${person.enabled}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">Description:</td>
                <td valign="top" class="value">${person.description?.encodeAsHTML()}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">Email:</td>
                <td valign="top" class="value">${person.email?.encodeAsHTML()}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">Show Email:</td>
                <td valign="top" class="value">${person.emailShow}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">Roles:</td>
                <td valign="top" class="value">
                    <ul>
                        <g:each in="${roleNames}" var='name'>
                            <li>${name}</li>
                        </g:each>
                    </ul>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">Groups:</td>
                <td valign="top" class="value">
                    <ul>
                        <g:each in="${person.groups}" var='group'>
                            <li><g:link controller="userGroup" action="show"
                                        id="${group.id}">${group.name}</g:link></li>
                        </g:each>
                    </ul>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">Studies Assigned:</td>
                <td valign="top" class="value">
                    <ul>
                        <g:each in="${org.transmart.searchapp.SecureObjectAccess.findAllByPrincipal(person, [sort: accessLevel])}"
                                var='soa'>
                            <li>${soa.getObjectAccessName()}</li>
                        </g:each>
                    </ul>
                </td>
            </tr>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">Studies with Access(via groups):</td>
                <td valign="top" class="value">
                    <ul>
                        <g:each in="${org.transmart.searchapp.AuthUserSecureAccess.findAllByAuthUser(person, [sort: accessLevel])}"
                                var='soa'>
                            <li><g:link controller="secureObject" action="show"
                                        id="${soa.secureObject.id}">${soa.getObjectAccessName()}</g:link></li>
                        </g:each>
                    </ul>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">Force user to change the password:</td>
                <td valign="top" class="value">
                    ${person.changePassword ? 'Yes' : 'No'}
                </td>
            </tr>
            </tbody>
        </table>
    </div>

    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${person.id}"/>
            <span class="button"><g:actionSubmit class="edit" value="Edit"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');"
                                                 value="Delete"/></span>
        </g:form>
    </div>

</div>
</body>
</html>
