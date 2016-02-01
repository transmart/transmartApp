<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Show Role</title>
</head>

<body>
<div class="body">
    <h1>Show Role</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name">ID:</td>
                <td valign="top" class="value">${authority.id}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">Role Name:</td>
                <td valign="top" class="value">${authority.authority}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">Description:</td>
                <td valign="top" class="value">${authority.description}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">People:</td>
                <td valign="top" class="value">&nbsp;</td>

            </tr>
            <tr class="prop">
                <td colspan="2">

                    <table>
                        <thead>
                        <tr>
                            <g:sortableColumn property="id" title="WWID"/>
                            <g:sortableColumn property="username" title="Login Name"/>
                            <g:sortableColumn property="userRealName" title="Full Name"/>
                            <g:sortableColumn property="enabled" title="Enabled"/>
                            <g:sortableColumn property="description" title="Description"/>
                            <th>&nbsp;</th>
                        </tr>
                        </thead>
                        <tbody>
                        <g:each in="${sortedPeople}" status="i" var="person">
                            <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                                <td>${person.id}</td>
                                <td>${person.username?.encodeAsHTML()}</td>
                                <td>${person.userRealName?.encodeAsHTML()}</td>
                                <td>${person.enabled?.encodeAsHTML()}</td>
                                <td>${person.description?.encodeAsHTML()}</td>
                                <td class="actionButtons">
                                    <span class="actionButton">
                                        <g:link controller="authUser" action="show" id="${person.id}">Detail</g:link>
                                    </span>
                                </td>
                            </tr>
                        </g:each>
                        </tbody>
                    </table>

                </td>
            </tr>

            </tbody>
        </table>
    </div>

    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${authority?.id}"/>
            <span class="button"><g:actionSubmit class="edit" value="Edit"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');"
                                                 value="Delete"/></span>
        </g:form>
    </div>

</div>

</body>
</html>
