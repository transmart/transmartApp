
<!DOCTYPE html>
<html>
	<head>
		<title><g:layoutTitle default="Gene Signature" /></title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<link rel="shortcut icon" href="${resource(dir:'images',file:'searchtool.ico')}">
		<link rel="icon" href="${resource(dir:'images',file:'searchtool.ico')}">
		<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
		<link rel="stylesheet"	href="${resource(dir:'js',file:'ext/resources/css/ext-all.css')}" />
		<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/xtheme-gray.css')}" />
		<link rel="stylesheet"	href="${resource(dir:'css',file:'genesignature.css')}" />		

		<script type="text/javascript"	src="${resource(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
		<script type="text/javascript"	src="${resource(dir:'js', file:'ext/ext-all.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'maintabpanel.js')}"></script>		
		<script type="text/javascript" src="${resource(dir:'js', file:'toggle.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'utilitiesMenu.js')}"></script>		
		<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.min.js')}"></script>   
        <script>jQuery.noConflict();</script>
        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery-ui.min.js')}"></script>
		<script type="text/javascript" charset="utf-8">
			Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

			// set ajax to 90*1000 milliseconds
			Ext.Ajax.timeout = 180000;
            Ext.onReady(function()
	        {
            	   Ext.QuickTips.init()
            	   
                   var helpURL = '${grailsApplication.config.com.recomdata.adminHelpURL}';
                   var contact = '${grailsApplication.config.com.recomdata.contactUs}';
                   var appTitle = '${grailsApplication.config.com.recomdata.appTitle}';
                   var buildVer = 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>';
                   
            	   Ext.QuickTips.init();            	  	                
	        });
		</script>
		<g:layoutHead />
	</head>
	<body>
        <div id="header-div">
            <g:render template="/layouts/commonheader" model="['app':'genesignature']" />
			<div id="app"><g:layoutBody /></div>
		</div>
	</body>
</html>
