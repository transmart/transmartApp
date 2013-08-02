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
		<title><g:layoutTitle default="Gene Signature" /></title>
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
		<script type="text/javascript" charset="utf-8">
			Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

			// set ajax to 90*1000 milliseconds
			Ext.Ajax.timeout = 180000;

            Ext.onReady(function()
	        {
            	   Ext.QuickTips.init()
            	   
            	   var helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
                   var contact = '${grailsApplication.config.com.recomdata.searchtool.contactUs}';
                   var appTitle = '${grailsApplication.config.com.recomdata.searchtool.appTitle}';
                   var buildVer = 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>';
                   
                   var viewport = new Ext.Viewport({
                    layout: "border",
                    items:[new Ext.Panel({                          
                       region: "center",  
                       tbar: createUtilitiesMenu(helpURL, contact, appTitle,'${request.getContextPath()}', buildVer, 'gs-utilities-div'),  
                       autoScroll:true,                    
                       contentEl: "page"
                    })]
                  });
                  viewport.doLayout();	                
	        });
		</script>
		<g:layoutHead />
	</head>
	<body>
		<div id="page">
			<div id="header"><g:render template="/layouts/commonheader"	model="['app':'genesignature']" /></div>
			<div id="app"><g:layoutBody /></div>
		</div>
	</body>
</html>
