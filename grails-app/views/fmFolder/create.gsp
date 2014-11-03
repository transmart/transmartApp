<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Create Folder</title>
</head>

<body>
<div class="body">
    <g:if test="${flash.message}"><div class="message">${flash.message}</div></g:if>

    <div>
        <table width="100%">
            <tr><td><h1>Create Folder</h1></td><td align="right"></td></tr>
        </table>
    </div>

    <g:hasErrors bean="${fmFolderInstance}">
        <div class="errors">
            <g:renderErrors bean="${fmFolderInstance}" as="list"/>
        </div>
    </g:hasErrors>

    <g:form action="save">
        <g:hiddenField name="id" value="${fmFolderInstance?.id}"/>
        <g:hiddenField name="version" value="${fmFolderInstance?.version}"/>
        <div class="dialog">
            <table class="detail">
                <tbody>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name"><g:message code="fmFolderInstance.folderName.label"
                                                     default="Name"/> <g:requiredIndicator/> :</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: fmFolderInstance, field: 'folderName', 'errors')}">
                        <g:textField size="50" name="folderName" value="${fmFolderInstance?.folderName}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="name"><g:message code="fmFolderInstance.folderType.label"
                                                     default="Folder Type"/> <g:requiredIndicator/> :</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: fmFolderInstance, field: 'folderType', 'errors')}">
                        <g:select name="folderType" id="folderType" from="${com.recomdata.util.FolderType.values()}"
                                  value="${fieldValue(bean: fmFolderInstance, field: 'folderType')}"/>
                    </td>
                </tr>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="description"><g:message code="fmFolderInstance.active.label"
                                                            default="Active"/> :</label>
                    </td>
                    <td valign="top" class="value ${hasErrors(bean: fmFolderInstance, field: 'activeInd', 'errors')}">
                        <g:checkBox name="activeInd" value="${fmFolderInstance?.activeInd}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>

        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="save" id="save"
                                                 value="${message(code: 'default.button.update.label', default: 'Save')}"/></span>
            <span class="button"><g:actionSubmit class="list" action="list" id="cancel" value="Cancel"
                                                 onclick="return confirm('Are you sure?')"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
