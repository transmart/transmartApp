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

	<script type="text/javascript">
		Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

		// set ajax to 90*1000 milliseconds
		Ext.Ajax.timeout = 180000;
		// this overrides the above
		Ext.Updater.defaults.timeout = 180000;

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
			        items:[

			         {
			                title:"Entrez Gene",
			                id:'entrezGene',
			                timeout:180000,
			            	defaultSrc: "http://www.ncbi.nlm.nih.gov/sites/entrez?Db=gene&Cmd=DetailsSearch&Term=${geneId}[GeneID]&doptcmdl=DocSum"
				     },
					{
		                title:"Entrez Global",
		                id:'entrezGlobal',
		                defaultSrc: "http://www.ncbi.nlm.nih.gov/gquery/gquery.fcgi?term=${symbol}"
					}, /*
				<sec:ifNotGranted roles="ROLE_PUBLIC_USER">
					{
		        		title:"Pictor",
		                id:'pictor',
		                defaultSrc:"http://servername/cgi-bin/chip/pathways.cgi?symbol=${symbol}"
		            },
		            {
		        		title:"Hydra",
		                id:'hydra',
		                defaultSrc:"http://servername/hydra/viewer/index.cfm?hydrageneid=${hydraGeneID}"
		            },
					{
		        		title:"GeneCards",
		                id:'genecard',
		                defaultSrc:"http://www.genecards.org/cgi-bin/carddisp.pl?gene=${symbol}"
		            },
		            {
		        		title:"TargetCV",
		                id:'targetcv',
		                defaultSrc:"http://servername/TargetCV/reports/${symbol}.htm"
		            },
			</sec:ifNotGranted> */
					{
		                title:"Google Scholar",
		                id:'google',
		                defaultSrc: "http://scholar.google.com/scholar?hl=en&lr=&q=${symbol}+gene&btnG=Search&as_allsubj=some&as_subj=bio&as_subj=chm&as_subj=med"
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