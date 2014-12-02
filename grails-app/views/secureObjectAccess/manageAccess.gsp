<!--
  tranSMART - translational medicine data mart

  Copyright 2008-2012 Janssen Research & Development, LLC.

  This product includes software developed at Janssen Research & Development, LLC.

  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.

  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.


-->

<g:setProvider library="prototype"/>
<%@ page import="org.springframework.web.util.JavaScriptUtils; org.transmart.searchapp.SecureObjectAccess" %>

<html>
<head>
	<r:require module="jquery-ui"/>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin" />
    <title>Manage Study Access</title>
    <script type="text/javascript"	src="${resource(dir:'js', file:'jQuery/jquery.min.js')}"></script>
    
    <script type="text/javascript">
        var $j = jQuery.noConflict();
    </script>
    <g:setProvider library="prototype" />
    <g:javascript library="prototype" />

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
                action:     'listAccessForPrincipal',
                update:     [success: 'permissions', failure: ''],
                params:     "form.serialize()")};
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
                              onchange="document.accessform.submit();" />
                    <input type="hidden" name="currentprincipalid" id="currentprincipalid" value="${principalInstance?.id}"/>
                </td>
                <td>&nbsp;</td>
                <td>
                    <input name="searchtext" id="searchtext"><button class="" onclick="return searchtrial.call(this);">Search Study</button>
                </td>
            </tr>
            <tr>
                <td>Has Access for these studies</td>
                <td></td>
                <td>Available studies:</td>
            </tr>
            <tr id="permissions">
                <g:render template="addremoveAccess"
                          model="[secureObjectInstance:   secureObjectInstance,
                                  secureObjectAccessList: secureObjectAccessList,
                                  objectswithoutaccess:   objectswithoutaccess]" />
            </tr>
        </table>
    </g:form>
</div>
</td>
</tr>
</tbody>

</body>
</html>