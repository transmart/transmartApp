
<%@ page import="org.springframework.web.util.JavaScriptUtils; org.transmart.searchapp.SecureObjectAccess" %>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Manage Study Access</title>
    <script type="text/javascript" src="${resource(dir: 'js', file: "jquery/jquery-${org.codehaus.groovy.grails.plugins.jquery.JQueryConfig.SHIPPED_VERSION}.js", plugin: 'jquery')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery/jquery.migrate.js')}"></script>
    <script type="text/javascript" src="${resource(dir: 'js', file: 'jquery/jquery-ui-1.9.1.custom.min.js')}"></script>

    <script type="text/javascript" charset="utf-8">

        var $j = window.$j = jQuery.noConflict();

    </script>
    <style>
    p {
        width: 430px;
    }

    .ext-ie .x-form-text {
        position: static !important;
    }
    </style>

</head>

<body>
<div class="body">
    <h1>Manage Study Access for User/Group</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div id="divuser"
         style="width:100%; font:11px tahoma, arial, helvetica, sans-serif"><br><b>Search User/Group</b><br>
        <input type="text" size="80" id="searchUsers" autocomplete="off"/></div>

    <r:script>
var pageInfo = {
    basePath: '${JavaScriptUtils.javaScriptEscape(request.getContextPath())}'
}
createUserSearchBox(pageInfo.basePath +
        '/userGroup/ajaxGetUsersAndGroupsSearchBoxData',
        440,
        '${JavaScriptUtils.javaScriptEscape(principalInstance?.name)}');

  function searchtrial(){
    var pid = jQuery('#currentprincipalid').val();
    if (!pid) {
	alert("Please select a user/group first");
	return false;
	}
    var form = $(this).closest('form');
        ${remoteFunction(controller: 'secureObjectAccess',
                action: 'listAccessForPrincipal',
                update: [success: 'permissions', failure: ''],
                params: "form.serialize()")};
	return false;
  }
    </r:script>

    <g:form name="accessform" action="manageAccess">
        <table>
            <tr>
                <td>
                    <label for="accesslevelid"><b>Access Level</b></label>
                    <g:select optionKey="id"
                              optionValue="accessLevelName"
                              from="${accessLevelList}"
                              name="accesslevelid"
                              value="${accesslevelid}"
                              onchange="document.accessform.submit();"/>
                    <input type="hidden" name="currentprincipalid" id="currentprincipalid"
                           value="${principalInstance?.id}"/>
                </td>
                <td>&nbsp;</td>
                <td>
                    <input name="searchtext" id="searchtext"><button class=""
                                                                     onclick="return searchtrial.call(this);">Search Study</button>
                </td>
            </tr>
            <tr>
                <td>Has Access for these studies</td>
                <td></td>
                <td>Available studies:</td>
            </tr>
            <tr id="permissions">
                <g:render template="addremoveAccess"
                          model="[secureObjectInstance  : secureObjectInstance,
                                  secureObjectAccessList: secureObjectAccessList,
                                  objectswithoutaccess  : objectswithoutaccess]"/>
            </tr>
        </table>
    </g:form>
</div>
</td>
</tr>
</tbody>

</body>
</html>