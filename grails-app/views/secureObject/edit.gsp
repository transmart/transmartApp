

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="admin" />
        <title>Edit SecureObject</title>
    </head>
    <body>
        <div class="body">
            <h1>Edit SecureObject</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${secureObjectInstance}">
            <div class="errors">
                <g:renderErrors bean="${secureObjectInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${secureObjectInstance?.id}" />
                <input type="hidden" name="version" value="${secureObjectInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="bioDataId">Bio Data Id:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:secureObjectInstance,field:'bioDataId','errors')}">
                                    <input type="text" id="bioDataId" name="bioDataId" value="${fieldValue(bean:secureObjectInstance,field:'bioDataId')}" />
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dataType">Data Type:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:secureObjectInstance,field:'dataType','errors')}">
                                    <textarea rows="5" cols="40" name="dataType">${fieldValue(bean:secureObjectInstance, field:'dataType')}</textarea>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="bioDataUniqueId">Bio Data Unique Id:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:secureObjectInstance,field:'bioDataUniqueId','errors')}">
                                    <input type="text" id="bioDataUniqueId" name="bioDataUniqueId" value="${fieldValue(bean:secureObjectInstance,field:'bioDataUniqueId')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="conceptPaths">Concept Paths:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:secureObjectInstance,field:'conceptPaths','errors')}">
                                    
<ul>
<g:each var="c" in="${secureObjectInstance?.conceptPaths?}">
    <li><g:link controller="secureObjectPath" action="show" id="${c.id}">${c?.encodeAsHTML()}</g:link></li>
</g:each>
</ul>
<g:link controller="secureObjectPath" params="['secureObject.id':secureObjectInstance?.id]" action="create">Add SecureObjectPath</g:link>

                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="displayName">Display Name:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:secureObjectInstance,field:'displayName','errors')}">
                                    <input type="text" id="displayName" name="displayName" value="${fieldValue(bean:secureObjectInstance,field:'displayName')}"/>
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
