<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Config Info - Authentication Providers</title>
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

<h2>Authentication providers &nbsp;&nbsp;
    <a target="_blank" href="${grailsApplication.config.com.recomdata.adminHelpURL ?: "JavaScript:D2H_ShowHelp('1259','${grailsApplication.config.com.recomdata.adminHelpURL}','wndExternal',CTXT_DISPLAY_FULLHELP )"}">
        <img src="${resource(dir:'images',file:'help/helpicon_white.jpg')}"
	     alt="Help" border=0 width=18pt style="vertical-align:middle;margin-left:5pt;"/>
    </a>
</h2>

<table id="configAuthProviders"  class="detail" style="width: 100%">
    <g:tableHeaderToggle
        label="Providers (${providers.size()})"
	divPrefix="config_auth_providers" status="open" colSpan="${2}"/>

    <tbody id="config_config_auth_providers_detail" style="display: block;">
        <tr>
	    <th>Provider</th>
	    <th width="100%">Description</th>
	</tr>

        <g:each in="${providers}" var="provider">
            <tr>
	        <td>${provider.key}</td>
		<td>${provider.value}</td>
	    </tr>
	</g:each>

    </tbody>
</table>

    <p>
	Usernames and passwords are checked against one or more authentication providers.
    </p>
    <p>
	Other configuration parameters are needed to control each of the provdiers. See
	<a href="index">the full configuration information page</a>.
    </p>



</div>
</body>
</html>
