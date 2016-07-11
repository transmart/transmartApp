<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>User Group List</title>
</head>

<body>

<div class="body">
    <h1>User Group List</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="id" title="Id"/>

                <g:sortableColumn property="name" title="Name"/>

                <g:sortableColumn property="description" title="Description"/>

                <g:sortableColumn property="enabled" title="Enabled"/>


                <g:sortableColumn property="groupCategory" title="Group Category"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${userGroupInstanceList}" status="i" var="userGroupInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show"
                                id="${userGroupInstance.id}">${fieldValue(bean: userGroupInstance, field: 'id')}</g:link></td>

                    <td>${fieldValue(bean: userGroupInstance, field: 'name')}</td>

                    <td>${fieldValue(bean: userGroupInstance, field: 'description')}</td>

                    <td>${fieldValue(bean: userGroupInstance, field: 'enabled')}</td>


                    <td>${fieldValue(bean: userGroupInstance, field: 'groupCategory')}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${org.transmart.searchapp.UserGroup.count()}"/>
    </div>
</div>
</body>
</html>
