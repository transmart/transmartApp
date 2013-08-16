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

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.0 Strict//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <link rel="shortcut icon" href="${resource(dir:'images',file:'searchtool.ico')}">
		<link rel="icon" href="${resource(dir:'images',file:'searchtool.ico')}">
		<link rel="stylesheet" href="${resource(dir:'js', file:'ext/resources/css/ext-all.css')}">
		<link rel="stylesheet" href="${resource(dir:'js', file:'ext/resources/css/xtheme-gray.css')}">
		<link rel="stylesheet" href="${resource(dir:'css', file:'main.css')}">
		
	<!--[if IE 7]>
		<style type="text/css">
			 div#gfilterresult,div#ptfilterresult, div#jubfilterresult, div#dqfilterresult {
				width: 99%;
			}
		</style>
	<![endif]-->
	

		<script type="text/javascript" src="${resource(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'ext/ext-all.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'ext/miframe.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'searchcombobox.js')}"></script>
	    <script type="text/javascript" src="${resource(dir:'js', file:'picklist.js')}"></script>
	    <script type="text/javascript" src="${resource(dir:'js', file:'utilitiesMenu.js')}"></script>
		<script type="text/javascript" charset="utf-8">
			Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

			// set ajax to 90*1000 milliseconds
			Ext.Ajax.timeout = 180000;

			// qtip on
			Ext.QuickTips.init();

			Ext.onReady(function(){			
	            var helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
	            var contact = '${grailsApplication.config.com.recomdata.searchtool.contactUs}';
	            var appTitle = '${grailsApplication.config.com.recomdata.searchtool.appTitle}';
	            var buildVer = 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>';
	             
				var viewport = new Ext.Viewport({
					layout: "border",
					items:[new Ext.Panel({                          
						   region: "center",
						   tbar: createUtilitiesMenu(helpURL, contact, appTitle,'${request.getContextPath()}', buildVer, 'utilities-div'), 
						   contentEl: "header-div"
					    })
			        ]
				});
				viewport.doLayout();

				var picklist = new Ext.app.PickList({
					id: "categories",
					cls: "categories-gray",
					storeUrl: "${createLink([controller:'search',action:'loadCategories'])}",
					renderTo: "search-categories",
					label: "Category:&nbsp;",
					disabledClass: "picklist-disabled",
					onSelect: function(record) {
				        var combo = Ext.getCmp("search-combobox");
				        combo.focus();
				        if ((record.id != "all") || (record.id == "all" && combo.getRawValue().length > 0)) {
							combo.doQuery(combo.getRawValue(), true);
				        }
					}
				});

				var combo = new Ext.app.SearchComboBox({
					id: "search-combobox",
					renderTo: "search-text",
					searchUrl: "${createLink([action:'loadSearch',controller:'search'])}",
					submitUrl: "${createLink([action:'newSearch',controller:'search'])}",
					submitFn: function(param, text) {
						var combo = Ext.getCmp("search-combobox");
						combo.setDisabled(true);
						combo.setRawValue("Searching for " + text + "...");
						var searchbtn = document.getElementById("search-button");
						searchbtn.disabled = true;
						var picklist = Ext.getCmp("categories");
						picklist.setDisabled(true);
						var linkbuttons = document.getElementById("linkbuttons-div");
						linkbuttons.innerHTML = '<span style="color:#a0a0a0;font-size:11px;text-decoration:underline;">browse<br />saved filters</span>';
						var idfield = document.getElementById("id-field");
						idfield.value = param;
						setTimeout("postSubmit();", 100);
						document.form.submit();
					},
					value: "",
					width: 470,
			        onSelect: function(record) {
						this.collapse();
						if (record != null) {
							this.submitFn(record.data.id, record.data.keyword);
						}
					},
			        listeners: {
						"beforequery": {
							fn: function(queryEvent) {
					            var picklist = Ext.getCmp("categories");
					            if (picklist != null) {
						            var rec = picklist.getSelectedRecord();
									if (rec != null) {
										queryEvent.query = rec.id + ":" + queryEvent.query;
									}
								}
							},
							scope: this
						}
			        }
				});
				combo.focus();
			});

			function searchOnClick() {
				var combo = Ext.getCmp("search-combobox");
				var param = combo.getSelectedParam();
				if (param != null) {
					combo.submitFn(param, param);
				}
			}

			function postSubmit() {
				var searchcombo = document.getElementById("search-combobox");
				searchcombo.className += " searchcombobox-disabled";
				searchcombo.style.width = "442px";						
			}
		</script>
		<title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
		<!-- ************************************** -->
        <!-- This implements the Help functionality -->
        <script type="text/javascript" src="${resource(dir:'js', file:'help/D2H_ctxt.js')}"></script>
        <script language="javascript">
        	helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
        </script>
		<!-- ************************************** -->
	</head>
	<body>
		<div id="header-div">
			<g:render template="/layouts/commonheader" model="['app':'search']" />
			<g:render template="/layouts/searchheader" model="['app':'search']" />
		</div>
	</body>
</html>
