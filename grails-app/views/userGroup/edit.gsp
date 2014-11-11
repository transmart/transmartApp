<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Edit UserGroup</title>
    <g:setProvider library="prototype"/>
</head>

<body>

<div class="body">
<h1>Edit UserGroup</h1>
<g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
</g:if>
<g:hasErrors bean="${userGroupInstance}">
    <div class="errors">
        <g:renderErrors bean="${userGroupInstance}" as="list"/>
    </div>
</g:hasErrors>
<g:form method="post">
    <input type="hidden" name="id" value="${userGroupInstance?.id}"/>
    <input type="hidden" name="version" value="${userGroupInstance?.version}"/>

    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="enabled">Enabled:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: userGroupInstance, field: 'enabled', 'errors')}">
                    <g:checkBox name="enabled" value="${userGroupInstance?.enabled}"></g:checkBox>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="description">Description:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: userGroupInstance, field: 'description', 'errors')}">
                    <textarea rows="5" cols="40"
                              name="description">${fieldValue(bean: userGroupInstance, field: 'description')}</textarea>
                </td>
            </tr>
            <tr class="prop">
                <td valign="top" class="name">
                    <label for="name">Name:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: userGroupInstance, field: 'name', 'errors')}">
                    <input type="text" id="name" name="name"
                           value="${fieldValue(bean: userGroupInstance, field: 'name')}"/>
                </td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name">
                    <label for="reports">Members:</label>
                </td>
                <td valign="top" class="value ${hasErrors(bean: userGroupInstance, field: 'members', 'errors')}">
                    <table>
                        <tr><td></td><td></td><td><input
                                name="searchtext"
                                id="searchtext"><button
                                class=""
                                onclick="${remoteFunction(action:'searchUsersNotInGroup',update:[success:'groupmembers', failure:''], id:userGroupInstance?.id, params:'$(\'searchtext\').serialize()' )};
                                return false;">Search Users</button>
                        </td>
                        <tr><td>Members of group:</td><td></td><td>Available users:</td>
                        </tr>
                        <tr id="groupmembers">
                            <g:render template="addremove"
                                      bean="${userGroupInstance}"/>
                        </tr>
                    </table>
    </div>
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
