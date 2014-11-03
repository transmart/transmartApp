<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Folder List</title>
</head>

<body>
<div class="body">
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
        <br/>
    </g:if>

    <g:form controller="fmFolder">
        <table style="background-color: #E6E6E6;" class="default">
            <caption class="note">Instructions: click the 'New' button to add a Folder
                <span class="button" style="float:right;">
                    <g:actionSubmit action="create" value="New Folder"/>
                </span>
            </caption>
        </table>
    </g:form>

    <div>
        <table width="100%">
            <tr><td><h1>Folders (${fmFolderInstanceTotal})</h1></td><td align="right"></td></tr>
        </table>
    </div>

    <div class="list">
        <table class="default">
            <thead>
            <tr>

                <th>Detail</th>
                <g:sortableColumn property="folderName"
                                  title="${message(code: 'fmFolderInstance.folderName.label', default: 'Name')}"/>
                <g:sortableColumn property="folderFullName"
                                  title="${message(code: 'fmFolderInstance.folderFullName.label', default: 'Full Name')}"/>
                <g:sortableColumn property="folderLevel"
                                  title="${message(code: 'fmFolderInstance.folderLevel.label', default: 'Folder Level')}"/>
                <g:sortableColumn property="folderType"
                                  title="${message(code: 'fmFolderInstance.folderType.label', default: 'Folder Type')}"/>
                <g:sortableColumn property="activeInd"
                                  title="${message(code: 'fmFolderInstance.activeInd.label', default: 'Active')}"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${fmFolderInstanceList}" status="i" var="fmFolderInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td>
                        <g:link action="show" id="${fmFolderInstance.id}"><img
                                src="${resource(dir: 'images/skin', file: 'information.png', plugin: 'rdc-core')}"
                                alt="Detail link" border="0"/></g:link>
                    </td>
                    <td>${fmFolderInstance.folderName}</td>
                    <td>${fmFolderInstance.folderFullName}</td>
                    <td>${fmFolderInstance.folderLevel}</td>
                    <td>${fmFolderInstance.folderType}</td>
                    <td><g:checkBox name="activeInd" value="${fmFolderInstance?.activeInd}" disabled='true'/></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate action="list" total="${fmFolderInstanceTotal}"/>
    </div>
</div>
</body>
</html>
