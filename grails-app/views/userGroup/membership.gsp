
<%@ page import="AuthUser" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="admin" />
        <title>Group Membership</title>
                   <style>
   p { width:440px; }
        .ext-ie .x-form-text {position:static !important;}
  </style>
    </head>
    <body>
        <div class="body">
            <h1>Manage Group Membership</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
  <div id="divuser" style="width:100%; font:11px tahoma, arial, helvetica, sans-serif">
  <br> please select a user then select groups</br>
  <br><b>Search User<b></b><br>
  <input type="text"  size="80" id="searchUsers" autocomplete="off" /></div>
  <script type="text/javascript">
  createUserSearchBox2('${request.getContextPath()}/userGroup/ajaxGetUserSearchBoxData', 440);

  function searchgroup(){
	  var pid = document.getElementById('currentprincipalid').value;
	  if(pid==null||pid ==''){
	alert("Please select a user first");
	return false;
	}

	  ${remoteFunction(action:'searchGroupsWithoutUser',update:[success:'groups', failure:''], params:'$(\'searchtext\').serialize()+\'&id=\'+pid')};
	   return false;
	  }
  </script>
          <table>
                				<tr><td></td><td></td><td><input name="searchtext" id="searchtext"></input>
                				<button class="" onclick="searchgroup();">Search Groups</button></td>
                     			<tr><td><b>Member of these groups</b></td><td></td><td><b>Available groups</b></td></tr>
                     			<tr id="groups">
                                    <g:render template="addremoveg" model="['groupswithoutuser' :groupswithoutuser]" />
                                    </tr>
                                     </table>
                                	</div>
                                </td>
                            </tr>
                        </tbody>
   <input type="hidden" id="currentprincipalid">
    </body>
</html>
