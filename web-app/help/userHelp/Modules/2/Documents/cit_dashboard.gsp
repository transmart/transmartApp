<html>
<head>
	<title><g:layoutTitle default="Dashboard" /></title>
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'dashboard.css')}" />
	<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'nav_styles.css')}" />
    <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.ico')}" type="image/x-icon" />   
    <g:javascript src="toggle.js" />
    <g:javascript src="FusionCharts.js" />	
	<g:javascript library="prototype" />		
        
    <g:layoutHead /> 

		<!-- ************************************** -->

                <!-- This implements the Help functionality -->
                <script language="javascript" src="/quality/js/D2H_ctxt.js">
                </script>
                <script language="javascript">
                           	 helpURL = "/quality/DemoOnlineHelpProject/NetHelp/default.htm";
                                document.onhelp = function()
                                {
                                     JavaScript:D2H_ShowHelp(0000,helpURL,"_self",CTXT_DISPLAY_FULLHELP ); 
                                     return false;
                                }
                </script>

		<!-- ************************************** -->




</head>

<body style="width:100%; margin: 0;">

<div id="page">
	<table style="width: 100%;" cellpadding=0 cellspacing=0>
		<tbody>
		<tr>
			<td><div id="header"><g:render template="/layouts/commonheader"	model="['app':'dashboard']" /></div></td>
		</tr>
		
		<tr>
			<td><div id="content" style="padding-left: 10px;"><br/><g:layoutBody /></div></td>
		</tr>
		
		<tr>
			<td><div id="footer" style="width:99%;"><br/><g:render template="/layouts/footer" /></div></td>
		</tr>		
		</tbody>	
	</table>
</div>

</body>
</html>