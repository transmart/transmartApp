<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->

<html>
<head>
	<title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>

	<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/ext-all.css')}" />
	<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/xtheme-gray.css')}" />
	<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />

	<script type="text/javascript" src="${resource(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'ext/ext-all.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js', file:'ext/miframe.js')}"></script>
    <script type="text/javascript" src="${resource(dir:'js', file:'application.js')}"></script>

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
		                title:"Summary",
		                id:'summary',
		                defaultSrc: {url: "${createLink(controller:'details',action:'compoundSummary',id:id)}",nocache:true, discardUrl:true,method:'POST'}
					},
					{
		                title:"National Library of Medicine",
		                id:'nlm',
		                defaultSrc: "http://druginfo.nlm.nih.gov/drugportal/ProxyServlet?&APPLICATION_NAME=drugportal&actionHandle=searchChemIdLite&objectHandle=Search&nextPage=jsp/drugportal/ChemidDataview.jsp?SearchCategory=&QF1=Name&QV1=${symbol}"
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
