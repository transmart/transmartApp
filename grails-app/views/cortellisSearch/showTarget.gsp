<html>
<head>
	<title>Cortellis API: ${res["@name"]}</title>
	<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/ext-all.css')}">
	<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/xtheme-gray.css')}">
	<link rel="stylesheet" href="${resource(dir:'css', file:'main.css')}">
	<link rel="stylesheet" href="${resource(dir:'css', file:'cortellis.css')}">
	<style type="text/css">
		.x-tab-panel-body .x-panel-body {
	    	padding: 10px;
		}
	</style>
	<script type="text/javascript" src="${resource(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'ext/ext-all.js')}"></script>
	<script language="JavaScript">
		Ext.onReady(function(){
		    var tabs = new Ext.TabPanel({
		        renderTo: 'infoTabs',
		        //width: 700,
		        activeTab: 0,
				frame: false,
		        defaults :{
		            bodyPadding: 5,
					autoHeight: true
		        },
		        items: [
					<g:if test="${res.Description.size()}">
					{
			            contentEl:'Description', 
			            title: 'Description'
			            //closable: true
		        	},
					</g:if>
					<g:if test="${res.Synonyms.size()}">
					{
			            contentEl:'Synonyms', 
			            title: 'Synonyms'
			            //closable: true
		        	},
					</g:if>
					<g:if test="${res.Localizations.size()}">
					{
						contentEl:'Localizations',
						title: 'Localizations'
						//closable: true
					},
					</g:if>
					<g:if test="${res.Conditions.size()}">
					{
						contentEl:'Conditions',
						title: 'Conditions'
						//closable: true
					},
					</g:if>
					<g:if test="${res.Maps.size()}">
					{
						contentEl: 'Maps',
						title: 'MetaCore Maps'
						//closable: true
					}
					</g:if>
				]
			});
		});
	</script>
</head>
<body>
<h1>${res["@name"]}</h1>

<div id="infoTabs">
	<g:if test="${res.Description.size()}">
	<div id="Description" class="x-hide-display">
		${res.Description}
	</div>
	</g:if>
	<g:if test="${res.Synonyms.size()}">
	<div id="Synonyms" class="x-hide-display">
		<ul>
			<g:each in="${res.Synonyms.Synonym}" var="synonym">
				<li>${synonym["@name"]}</li>
			</g:each>
		</ul>
	</div>
	</g:if>
	<g:if test="${res.Localizations.size()}">
	<div id="Localizations" class="x-hide-display">
		<ul>
			<g:each in="${res.Localizations.Localization}" var="loc">
				<li>${loc["@name"]}</li>
			</g:each>
		</ul>
	</g:if>
	</div>
	<g:if test="${res.Conditions.size()}">
	<div id="Conditions" class="x-hide-display">
		<g:set var="counter" value="${1}" />
		<table>
			<thead>
				<tr><th>Name</th><th>Status</th></tr>
			</thead>
			<tbody>
			<g:each in="${res.Conditions.Condition}" var="cond">
				<tr class="${counter % 2 == 0 ? 'even' : 'odd'}"><td>${cond["@name"]}</td><td>${cond["@status"]}</td></tr>
				<g:set var="counter" value="${counter + 1}" />
			</g:each>
			</tbody>
		</table>
	</div>
	</g:if>
	<g:if test="${res.Maps.size()}">
	<div id="Maps" class="x-hide-display">
		<ol>
			<g:each in="${res.Maps.Map}" var="map">
				<li><a href="${grailsApplication.config.com.thomsonreuters.transmart.metacoreURL}/cgi/imagemap.cgi?id=${map["@id"]}">${map["@name"]}</a></li>
			</g:each>
		<ol>
	</div>
	</g:if>
</div>

</body>
</html>