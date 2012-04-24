<html>
<head>
	<title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
	<link rel="SHORTCUT ICON" href="${resource(dir:'images',file:'searchtool.ico')}">
	<link rel="ICON" href="${resource(dir:'images',file:'searchtool.ico')}"> 
	<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/ext-all.css')}" />
	<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/xtheme-gray.css')}" />
	<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />

	<g:javascript library="prototype" />
	<script type="text/javascript" src="${resource(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'ext/ext-all.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'ext/miframe.js')}"></script>
	<g:javascript library="application" />

	<style type="text/css">
 		.x-tab-strip span.x-tab-strip-text {
		    font: 12px verdana, arial, helvetica, sans-serif;
			color:#538d4e;
		}
      	.x-tab-strip-active span.x-tab-strip-text {
        	font: 12px verdana, arial, helvetica, sans-serif;
			color:#000000;
			font-weight:bold;
		}
	</style>
	<!-- ************************************** -->
    <!-- This implements the Help functionality -->
    <script type="text/javascript" src="${resource(dir:'js', file:'help/D2H_ctxt.js')}"></script>
    <script language="javascript">
    	helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
    </script>
    <sec:ifAnyGranted roles="ROLE_ADMIN">
			<script language="javascript">
				helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
			</script>
	</sec:ifAnyGranted>
	<!-- ************************************** -->

	<script type="text/javascript">
		Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

		// set ajax to 90*1000 milliseconds
		Ext.Ajax.timeout = 180000;

		Ext.onReady(function() {
			var panel = new Ext.Viewport({
				layout: 'border',
				items: [
				{
			        activeTab:0,
			        xtype: 'tabpanel', // TabPanel
					region: 'center',
			        defaults: { autoScroll: true, closable: false, loadMask: true },
			        defaultType: 'iframepanel', // tabs use ManagedIFrame to display encapsulated web pages.
			        items:[/*
						{
			                title:"Clinical Trial",
			                id:'trial',
			                defaultSrc: {url: '${createLink(controller:'searchHelp',action:'listAllTrials')}',nocache:true, discardUrl:true,method:'POST'}
						}, */
						{
							title:"Compound",
			                id:'compound',
				            defaultSrc: {url: '${createLink(controller:'searchHelp',action:'listAllCompounds')}',nocache:true, discardUrl:true,method:'POST'}
						},
						{
							title:"Disease",
				            id:'disease',
			                defaultSrc: {url: '${createLink(controller:'searchHelp',action:'listAllDiseases')}',nocache:true, discardUrl:true,method:'POST'}
						},
						{
							title:"Pathway",
				            id:'pathway',
				            defaultSrc: {url: '${createLink(controller:'searchHelp',action:'listAllPathways')}',nocache:true, discardUrl:true,method:'POST'}
						},
						{
							title:"Gene Signature/Lists",
				            id:'genesig',
				            defaultSrc: {url: '${createLink(controller:'searchHelp',action:'listAllGeneSignatures')}',nocache:true, discardUrl:true, method:'POST'}
						}					
					],
			        tools:[{
						id:'help',
						qtip:'Click for context sensitive help',
					    handler: function(event, toolEl, panel){
					    	<%topicID="1016" %>
					    	D2H_ShowHelp(<%=topicID%>,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
					    }
			        }]
				}]
			});
		});
	</script>
</head>
<body>
	<!-- Ext.Viewport automatically renders to document body -->
</body>
</html>