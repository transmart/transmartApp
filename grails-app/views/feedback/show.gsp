<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show Feedback</title>
 		<link rel="stylesheet" href="${resource(dir:'css',file:'feedback.css')}" />
    </head>
    <body>
		<g:render template="/layouts/commonheader" model="['app':'feedback']" />

        <div class="nav">
            <span class="menuButton"><g:link class="list" action="list">Feedback List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Feedback</g:link></span>
        </div>
        <div class="body">
            <h1>Show Feedback</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
						<g:if test="${feedback.searchUserId!=null&& feedback.searchUserId.toString().length()>0}">
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="feedback.searchUserId" default="User"/>:</td>
                            <td valign="top" class="value">${fieldValue(bean:feedback, field:'searchUserId')}</td>
                        </tr>
						</g:if>
						<g:if test="${feedback.createDate!=null&& feedback.createDate.toString().length()>0}">
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="feedback.createDate" default="Created"/>:</td>
                            <td valign="top" class="value"><g:formatDate format="yyyy-MM-dd" date="${feedback.createDate}" /></td>
                        </tr>
						</g:if>
						<g:if test="${feedback.appVersion!=null&& feedback.appVersion.toString().length()>0}">
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="feedback.appVersion" default="Version"/>:</td>
                            <td valign="top" class="value">${fieldValue(bean:feedback, field:'appVersion')}</td>
                        </tr>
						</g:if>
						<g:if test="${feedback.feedbackText!=null&& feedback.feedbackText.toString().length()>0}">
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="feedback.feedbackText" default="Feedback"/>:</td>
                            <td valign="top" class="value">${fieldValue(bean:feedback, field:'feedbackText')}</td>
                        </tr>
						</g:if>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${feedback?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
