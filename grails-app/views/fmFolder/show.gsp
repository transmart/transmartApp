<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Folder Details</title>
</head>

<body>
<div class="body">

    <g:if test="${flash.message}"><div class="message">${flash.message}</div></g:if>
    <g:form controller="fmFolder">

        <table style="background-color: #E6E6E6;" class="default">
            <caption class="note">Instructions: click the 'New' button to add a Folder
                <span class="button" style="float:right;">
                    <g:hiddenField name="id" value="${fmFolderInstance?.id}"/>

                    <g:actionSubmit action="create" value="New Folder"/>
                </span>
            </caption>

        </table>
    </g:form>
    <div>
        <table width="100%">
            <tr><td><h1>Folder</h1></td><td align="right"></td></tr>
        </table>
    </div>

    <div class="dialog">
        <table class="detail" style="width:600px;">
            <tbody>
            <g:hiddenField name="id" value="${fmFolderInstance?.id}"/>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="fmFolderInstance.folderName.label"
                                                         default="Folder Name"/> :</td>
                <td valign="top" class="value" colspan=2>${fieldValue(bean: fmFolderInstance, field: "folderName")}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="fmFolderInstance.folderFullName.label"
                                                         default="Folder Full Name"/> :</td>
                <td valign="top" class="value"
                    colspan=2>${fieldValue(bean: fmFolderInstance, field: "folderFullName")}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="fmFolderInstance.folderlevel.label"
                                                         default="Folder Level"/> :</td>
                <td valign="top" class="value"
                    colspan=2>${fieldValue(bean: fmFolderInstance, field: "folderLevel")}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="fmFolderInstance.folderType.label"
                                                         default="Folder Type"/> :</td>
                <td valign="top" class="value" colspan=2>${fieldValue(bean: fmFolderInstance, field: "folderType")}</td>
            </tr>

            <tr class="prop">
                <td valign="top" class="name"><g:message code="fmFolderInstance.activeInd.label"
                                                         default="Active"/> :</td>
                <td valign="top" class="value" colspan=2><g:checkBox name="activeInd"
                                                                     value="${fmFolderInstance?.activeInd}"
                                                                     disabled='true'/>
            </tr>

            </tbody>
        </table>
    </div>

    <div>
        <richui:treeView xml="${data}"/>
    </div>

    <div class="buttons">
        <g:form>
            <g:hiddenField name="id" value="${fmFolderInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="edit"
                                                 value="${message(code: 'default.button.edit.label', default: 'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="list" action="list" value="Close"/></span>
            <span class="button"><g:actionSubmit class="delete" action="delete"
                                                 value="${message(code: 'default.button.delete.label', default: 'Delete')}"
                                                 onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
