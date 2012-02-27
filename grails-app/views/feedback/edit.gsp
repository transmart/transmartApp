<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit Feedback</title>
 		<link rel="stylesheet" href="${resource(dir:'css',file:'feedback.css')}" />
    </head>
    <body>
 		<g:render template="/layouts/commonheader" model="['app':'feedback']" />
        <div class="nav">
            <span class="menuButton"><g:link class="list" action="list">Feedback List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New Feedback</g:link></span>
        </div>
        <div class="body">
            <h1>Edit Feedback</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${feedback}">
            <div class="errors">
                <g:renderErrors bean="${feedback}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${feedback?.id}" />
                <div class="dialog">
                    <table>
                        <tbody>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="appuser">User:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:feedback,field:'appUser','errors')}">
                                    <input type="text" id="appUser" name="appUser" value="${fieldValue(bean:feedback,field:'appUser')}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="createDate">Created:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:feedback,field:'createDate','errors')}">
                                    <g:datePicker name="createDate" value="${feedback?.createDate}" precision="day"></g:datePicker>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="appVersion">Version:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:feedback,field:'appVersion','errors')}">
                                    <input type="text" id="appVersion" name="appVersion" value="${fieldValue(bean:feedback,field:'appVersion')}"/>
                                </td>
                            </tr>
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="feedbackText">Feedback:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:feedback,field:'feedbackText','errors')}">
                                    <textarea id="feedbackText" name="feedbackText" rows="10" cols="100">${fieldValue(bean:feedback,field:'feedbackText')}</textarea>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
