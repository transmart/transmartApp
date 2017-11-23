<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="main"/>
    <title>Feedback List</title>
    <link rel="stylesheet" href="${resource(dir: 'css', file: 'feedback.css')}"/>
</head>

<body>
<div id="header-div" class="header-div">
<g:render template="/layouts/commonheader" model="['app': 'feedback']"/>
</div>
<div class="nav">
    <span class="menuButton"><g:link class="create" action="create">New Feedback</g:link></span>
</div>

<div class="body" style="clear:both; width:99%;margin-left:5px;margin-top:20px">
    <h1>Feedback List</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="list">
        <table width="100%" style="width:100%;">
            <thead>
            <tr>
                <th><g:message code="feedback.feedbackText" default="Feedback"/></th>
                <th><g:message code="feedback.searchUserId" default="User"/></th>
                <th><g:message code="feedback.createDate" default="Created"/></th>
                <th><g:message code="feedback.appVersion" default="Version"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${feedbackList}" status="i" var="feedback">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td style="width:65%"><g:link action="show"
                                                  id="${feedback.id}">${fieldValue(bean: feedback, field: 'feedbackText')}</g:link></td>
                    <td style="width:15%">${fieldValue(bean: feedback, field: 'searchUserId')}</td>
                    <td style="width:10%"><g:formatDate format="yyyy-MM-dd" date="${feedback.createDate}"/></td>
                    <td style="width:10%">${fieldValue(bean: feedback, field: 'appVersion')}</td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate total="${org.transmart.searchapp.Feedback.count()}"/>
    </div>
</div>
</body>
</html>
