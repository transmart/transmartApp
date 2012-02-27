
<%@ page import="SecureObjectAccess" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="admin" />
        <title>Manage Study Access</title>
                   <style>
   p { width:430px; }
        .ext-ie .x-form-text {position:static !important;}
  </style>
    </head>
    <body>
        <div class="body">
            <h1>Manage Study Access for User/Group</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
  <div id="divuser" style="width:100%; font:11px tahoma, arial, helvetica, sans-serif"><br><b>Search User/Group</b><br>
  <input type="text"  size="80" id="searchUsers" autocomplete="off" /></div>
  <script type="text/javascript">
  createUserSearchBox('${request.getContextPath()}/userGroup/ajaxGetUsersAndGroupsSearchBoxData', 440,'${principalInstance?.name}');

  function searchtrial(){
	  var pid = document.getElementById('currentprincipalid').value;
	  if(pid==null||pid ==''){
	alert("Please select a user/group first");
	return false;
	}
 ${remoteFunction(controller:'secureObjectAccess', action:'listAccessForPrincipal',update:[success:'permissions', failure:''], params:'$(\'searchtext\').serialize()+\'&id=\'+pid')};
	return false;
  }

  </script>

          <table>
                				<tr><td>
<g:form name="accessform" action="manageAccess">
                                    <label for="accessLevel"><b>Access Level</b></label>
                                    <g:select optionKey="id"  optionValue="accessLevelName" from="${accessLevelList}" name="accesslevelid" value="${accesslevelid}" onchange="document.accessform.submit();"></g:select>
  <input type="hidden" name="currentprincipalid" id="currentprincipalid" value="${principalInstance?.id}"/>
   </g:form>
                                </td><td>&nbsp;</td>
                				<td><input name="searchtext" id="searchtext"></input><button class="" onclick="searchtrial();">Search Study</button></td>
                     			<tr><td>Has Access for these studies</td><td></td><td>Available studies:</td></tr>
                       			<tr id="permissions">
                                    <g:render template="addremoveAccess" model="['secureObjectInstance':secureObjectInstance,'secureObjectAccessList' :secureObjectAccessList,'objectswithoutaccess':objectswithoutaccess" />
                                    </tr>
                                     </table>
                                	</div>
                                </td>
                            </tr>
                        </tbody>

    </body>
</html>
