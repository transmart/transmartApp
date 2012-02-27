

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Create SecureObjectAccess</title>         
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${resource(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">SecureObjectAccess List</g:link></span>
        </div>
        <div class="body">
            <h1>Create SecureObjectAccess</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${secureObjectAccessInstance}">
            <div class="errors">
                <g:renderErrors bean="${secureObjectAccessInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form action="save" method="post" >
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="principal">Principal:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:secureObjectAccess,field:'principal','errors')}">
                                    <g:select optionKey="id" from="${Principal.list()}" name="principal.id" value="${secureObjectAccess?.principal?.id}" noSelection="['null':'']"></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="accessLevel">Access Level:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:secureObjectAccess,field:'accessLevel','errors')}">
                                    <g:select optionKey="id" from="${SecureAccessLevel.list()}" name="accessLevel.id" value="${secureObjectAccess?.accessLevel?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="secureObject">Secure Object:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:secureObjectAccess,field:'secureObject','errors')}">
                                    <g:select optionKey="id" from="${SecureObject.list()}" name="secureObject.id" value="${secureObjectAccess?.secureObject?.id}" ></g:select>
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><input class="save" type="submit" value="Create" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
