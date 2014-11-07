<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Edit SecureObjectPath</title>
</head>

<body>
<div class="body">
    <h1>Edit SecureObjectPath</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${secureObjectPathInstance}">
        <div class="errors">
            <g:renderErrors bean="${secureObjectPathInstance}" as="list"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <input type="hidden" name="id" value="${secureObjectPathInstance?.id}"/>
        <input type="hidden" name="version" value="${secureObjectPathInstance?.version}"/>

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
                        <g:select optionKey="id" from="${org.transmart.searchapp.SecureObject.list()}"
                                  name="secureObject.id"
                                  value="${secureObjectPathInstance?.secureObject?.id}"></g:select>
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
