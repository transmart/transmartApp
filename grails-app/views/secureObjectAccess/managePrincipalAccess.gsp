<%@ page import="org.transmart.searchapp.SecureObject; org.transmart.searchapp.SecureAccessLevel" %>




<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Manage Study Access</title>
    <script type="text/javascript">

    </script>
</head>

<body>
<div class="body">
    <h1>Manage Study Access</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <g:hasErrors bean="${secureObjectInstance}">
        <div class="errors">
            <g:renderErrors bean="${secureObjectInstance}" as="list"/>
        </div>
    </g:hasErrors>

    <div class="dialog">
        <g:form method="post" name="secobjaccessform" action="manageAccessBySecObj">
            <table>
                <tbody>
                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="secureobjectid">Study:</label>
                        <g:select optionKey="id" optionValue="displayName"
                                  from="${SecureObject.listOrderByDisplayName()}"
                                  name="secureobjectid"
                                  value="${secureObjectInstance?.id}"
                                  onchange="document.secobjaccessform.submit();"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td valign="top" class="name">
                        <label for="accesslevelid">Access Level:</label>
                        <g:select optionKey="id" optionValue="accessLevelName"
                                  from="${SecureAccessLevel.list()}"
                                  name="accesslevelid" value="${accesslevelid}"
                                  onchange="document.secobjaccessform.submit();"/>
                    </td>
                </tr>
                </tbody>
            </table>

            <tr><td>&nbsp;</td><td>&nbsp;</td><td><input name="searchtext" id="searchtext">
                <input type="submit" value="Search User/Groups"/></td></tr>
        </g:form>
        <div>
            <table>
                <tbody>
                <tr><td><b>User/Group Assigned Access</b></td><td></td><td><b>User/Group Without Access</b></td></tr>
                <tr id="groups">
                    <g:render template="addremovePrincipal"
                              model="['userwithoutaccess': userwithoutaccess, 'secureObjectAccessList': secureObjectAccessList]"/>
                </tr>
                </tbody>
            </table>
        </div>

    </div>
</body>
</html>
