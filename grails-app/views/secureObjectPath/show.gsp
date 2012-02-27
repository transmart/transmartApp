<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="admin" />
        <title>Show SecureObjectPath</title>
    </head>
    <body>
        <div class="body">
            <h1>Show SecureObjectPath</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:secureObjectPathInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Concept Path:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:secureObjectPathInstance, field:'conceptPath')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Secure Object:</td>
                            
                            <td valign="top" class="value"><g:link controller="secureObject" action="show" id="${secureObjectPathInstance?.secureObject?.id}">${secureObjectPathInstance?.secureObject?.displayName?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${secureObjectPathInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
