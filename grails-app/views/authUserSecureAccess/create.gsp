

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="admin" />
        <title>Grant New Access Control</title>
    </head>
    <body>
        <div class="body">
            <h1>Grant New Access Control</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${authUserSecureAccessInstance}">
            <div class="errors">
                <g:renderErrors bean="${authUserSecureAccessInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="authUser">Auth User:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:authUserSecureAccessInstance,field:'authUser','errors')}">
                                    <g:select optionKey="id"  from="${AuthUser.listOrderByUsername()}" name="authUser.id" value="${authUserSecureAccessInstance?.authUser?.id}" noSelection="['null':'']"
                                    onchange="${remoteFunction(action:'listAccessLevel',
                                                                 update:'accessLevelList',
                                                                 params:'\'id=\'+this.value')}"
                                    ></g:select>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="accessLevel">Access Level:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:authUserSecureAccessInstance,field:'accessLevel','errors')}">
                                    <g:select id="accessLevelList" optionKey="id"  optionValue="accessLevelName" from="${SecureAccessLevel.listOrderByAccessLevelValue()}" name="accessLevel.id" value="${authUserSecureAccessInstance?.accessLevel?.id}" ></g:select>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="secureObject">Secure Object:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:authUserSecureAccessInstance,field:'secureObject','errors')}">
                                    <g:select optionKey="id" optionValue="displayName" from="${SecureObject.listOrderByDisplayName()}" name="secureObject.id" value="${authUserSecureAccessInstance?.secureObject?.id}" ></g:select>
                                </td>
                            </tr>

                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
