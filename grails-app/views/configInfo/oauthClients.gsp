<html>
  <head>	
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Config Info - OAuth Clients</title>
  </head>

  <body>
    <script type="text/javascript" src="${resource(dir:'js', file:'toggle.js')}"></script>
    <!-- override main.css -->
    <style type="text/css">
      .detail td a {
      padding-left: 10px;
      vertical-align: top;
      }

      .detail td a:hover {
      white-space: normal;
      }
    </style>

    <div class="body">

      <h2>OAuth clients &nbsp;&nbsp;
	<a target="_blank" href="${grailsApplication.config.com.recomdata.adminHelpURL ?: "JavaScript:D2H_ShowHelp('1259','${grailsApplication.config.com.recomdata.adminHelpURL}','wndExternal',CTXT_DISPLAY_FULLHELP )"}">
          <img src="${resource(dir:'images',file:'help/helpicon_white.jpg')}"
	       alt="Help" border=0 width=18pt style="vertical-align:middle;margin-left:5pt;"/>
	</a>
      </h2>

      <table id="configOauthClients"  class="detail" style="width: 100%">
	<g:tableHeaderToggle
          label="mapping (${clients.size()})"
	  divPrefix="config_oauth_clients" status="open" colSpan="${7}"/>

	<tbody id="config_oauth_clients_detail" style="display: block;">
          <tr>
	    <th>clientId</th>
	    <th>clientSecret</th>
	    <th>authorities</th>
	    <th>scopes</th>
	    <th>authorizedGrantTypes</th>
	    <th>redirectUris</th>
	    <th width="100%">description</th>
	  </tr>

          <g:each in="${clients}" var="client">
            <tr>
	      <td>${client.clientId}</td>
	      <td>${client.clientSecret}</td>
	      <td>${client.authorities}</td>
	      <td>${client.scopes}</td>
	      <td>${client.authorizedGrantTypes}</td>
	      <td>${client.redirectUris}</td>
	      <td>${client.desc}</td>
	    </tr>
	  </g:each>

	</tbody>
      </table>

      <p>
	OAuth clients provide an interface for external applications to login and interact with a tranSMART server.
      </p>
      <p>
	These include the REST API client (api-client) and the Glowing Bear UI (glowingbear-js)
      </p>

    </div>
  </body>
</html>
