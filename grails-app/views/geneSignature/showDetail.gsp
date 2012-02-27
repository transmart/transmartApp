<html>
	<head>
		<title>Gene Signature</title>
		<link rel="shortcut icon" href="${resource(dir:'images',file:'searchtool.ico')}">
		<link rel="icon" href="${resource(dir:'images',file:'searchtool.ico')}">
		<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
		<link rel="stylesheet"	href="${resource(dir:'js',file:'ext/resources/css/ext-all.css')}" />
		<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/xtheme-gray.css')}" />
		<link rel="stylesheet"	href="${resource(dir:'css',file:'genesignature.css')}" />
		<g:javascript library="prototype" />
		<script type="text/javascript"	src="${resource(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
		<script type="text/javascript"	src="${resource(dir:'js', file:'ext/ext-all.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'maintabpanel.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'toggle.js')}"></script>
		<script type="text/javascript" charset="utf-8">
			Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

			// set ajax to 90*1000 milliseconds
			Ext.Ajax.timeout = 180000;

			// qtip on
			Ext.QuickTips.init();
		</script>
	</head>
	<body>
		<div id="page">
	<g:render template="gene_sig_detail" model=['gs':gs]/>
		</div>
	</body>
</html>