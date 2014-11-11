<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Create SecureObjectPath</title>
</head>

<body>
<div class="body">
    <h1>Create SecureObjectPath</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${secureObjectPathInstance}">
        <div class="errors">
            <g:renderErrors bean="${secureObjectPathInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form action="save" method="post">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="conceptPath">Concept Path:</label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: secureObjectPathInstance, field: 'conceptPath', 'errors')}">
                        <input type="text" id="conceptPath" name="conceptPath"
                               value="${fieldValue(bean: secureObjectPathInstance, field: 'conceptPath')}"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="secureObject">Secure Object:</label>
                    </td>
                    <td valign="top"
                        class="value ${hasErrors(bean: secureObjectPathInstance, field: 'secureObject', 'errors')}">
                        <g:select optionKey="id" optionValue="displayName"
                                  from="${org.transmart.searchapp.SecureObject.list()}" name="secureObject.id"
                                  value="${secureObjectPathInstance?.secureObject?.id}"></g:select>
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
