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
<%@ page import="org.transmart.searchapp.AuthUser" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Group Membership</title>
    <style>
    p {
        width: 440px;
    }

    .ext-ie .x-form-text {
        position: static !important;
    }
    </style>
    <script type="text/javascript"	src="${resource(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
    <script type="text/javascript"	src="${resource(dir:'js', file:'ext/ext-all.js')}"></script>
</head>

<body>
<div class="body">
    <h1>Manage Group Membership</h1>
    <g:if test="${flash.message}">
        <div class="message">${flash.message}</div>
    </g:if>
    <div id="divuser" style="width:100%; font-size: 11px; font-familiy: 'tahoma, arial, helvetica, sans-serif'">
        <br/> please select a user then select groups<br/>
        <br/><b>Search User</b><br>
        <input type="text" size="80" id="searchUsers" autocomplete="off"/>
    </div>

    <script type="text/javascript">
        var pageInfo = {
            basePath: "${request.getContextPath()}"
        }
        createUserSearchBox2('${request.getContextPath()}/userGroup/ajaxGetUserSearchBoxData', 440);

        function searchgroup() {
            var pid = document.getElementById('currentprincipalid').value;
            if (pid == null || pid == '') {
                alert("Please select a user first");
                return false;
            }

            ${remoteFunction(action:'searchGroupsWithoutUser',update:[success:'groups', failure:''], params:'$(\'searchtext\').serialize()+\'&id=\'+pid')};
            return false;
        }
    </script>
    <table>
        <tr><td></td><td></td><td><input name="searchtext" id="searchtext">
            <button class="" onclick="searchgroup();">Search Groups</button></td></tr>
        <tr><td><b>Member of these groups</b></td><td></td><td><b>Available groups</b></td></tr>
        <tr id="groups">
            <g:render template="addremoveg" model="['groupswithoutuser': groupswithoutuser]"/>
        </tr>
    </table>
    <input type="hidden" id="currentprincipalid"/>
</div>
</body>
</html>
