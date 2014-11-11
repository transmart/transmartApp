<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>SecureObjectPath List</title>
</head>

<body>
<div class="body">
    <h1>SecureObjectPath List</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="id" title="Id"/>

                <g:sortableColumn property="conceptPath" title="Concept Path"/>

                <th>Secure Object</th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${secureObjectPathInstanceList}" status="i" var="secureObjectPathInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show"
                                id="${secureObjectPathInstance.id}">${fieldValue(bean: secureObjectPathInstance, field: 'id')}</g:link></td>

                    <td>${fieldValue(bean: secureObjectPathInstance, field: 'conceptPath')}</td>

                    <td>${fieldValue(bean: secureObjectPathInstance, field: 'secureObject')}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${secureObjectPathInstanceTotal}"/>
    </div>
</div>
</body>
</html>
