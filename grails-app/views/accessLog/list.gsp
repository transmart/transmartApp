<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>AccessLog List</title>
</head>

<body>
<div class="body">
    <g:form name="form">
        <table style="width: 700px;"><tr><td>
            Start Date&nbsp;&nbsp;<input id="startdate" name="startdate" type="text" value="${startdate}"></td>
            <td>End Date&nbsp;&nbsp;<input id="enddate" name="enddate" type="text" value="${enddate}"></td>
            <td><g:actionSubmit class="filter" value="Filter"
                                action="list"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<g:actionSubmit class="filter"
                                                                                                   value="Export to Excel"
                                                                                                   action="export"/>
            </td></tr></table>
    </g:form>
    <h1>AccessLog List</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr style="height: 30px;">

                <th style="vertical-align: middle;"><g:message code="accessLogInstance.accesstime"
                                                               default="Access Time"/></th>


                <th style="vertical-align: middle;"><g:message code="accessLogInstance.username" default="User"/></th>


                <th style="vertical-align: middle;"><g:message code="accessLogInstance.event" default="Event"/></th>


                <th style="vertical-align: middle;"><g:message code="accessLogInstance.eventmessage"
                                                               default="Event Message"/></th>

                <th style="vertical-align: middle;"><g:message code="accessLogInstance.requesturl"
                                                               default="Request Url"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${accessLogInstanceList}" status="i" var="accessLogInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}" style="height: 30px;">

                    <td style="width: 180px; vertical-align: top;">${fieldValue(bean: accessLogInstance, field: 'accesstime')}</td>
                    <td style="width: 100px; vertical-align: top;">${fieldValue(bean: accessLogInstance, field: 'username')}</td>
                    <td style="width: 200px; vertical-align: top;">${fieldValue(bean: accessLogInstance, field: 'event')}</td>
                    <td style="vertical-align: top;">${fieldValue(bean: accessLogInstance, field: 'eventmessage')}</td>
                    <td style="vertical-align: top;">
                        <g:if test="${accessLogInstance.requestURL}">
                            <a href="${accessLogInstance.requestURL}" target="_blank">link</a>
                        </g:if>
                    </td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>

    <div class="paginateButtons">
        <g:paginate
                total="${totalcount}"
                maxsteps="${grailsApplication.config.com.recomdata.search.paginate.maxsteps}"
                max="${grailsApplication.config.com.recomdata.search.paginate.max}"/>
    </div>
</div>
<r:script>
    jQuery(function () {
        jQuery("#startdate").datepicker({ dateFormat: 'dd/mm/yy' });
    });
    jQuery(function () {
        jQuery("#enddate").datepicker({ dateFormat: 'dd/mm/yy' });
    });
</r:script>
</body>
</html>
