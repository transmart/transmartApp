/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

function createMainExplorerWindow() 
{
	centerMainPanel = new Ext.Panel({
		id : 'centerMainPanel',
		region : 'center',
		layout : 'border'
	});
	
	centerMainPanel.add(createWestPanel());
	centerMainPanel.add(createCenterPanel());

	viewport = new Ext.Viewport({
		layout : 'border',
		items : [centerMainPanel, createNorthPanel() ]
	});
}

//Create the west most panel. 
function createWestPanel()
{
	westPanel = new Ext.Panel({
		id : 'westPanel',
		region : 'west',
		autoLoad:
        {
        	url: pageInfo.basePath+'/sampleExplorer/showMainSearchPage',
           	method:'POST'
        },			
		width : 230,
		minwidth : 200,
		split : false,
		border : true,
		layout : 'border'
	});

	ontTabPanel = new Ext.FormPanel(
			{
				id : 'ontPanel',
				region : 'center',
				defaults :
				{
				hideMode : 'offsets'
				}
			,
			collapsible : false,
			height : 600,
			width : 230,
			items : [{}],
			deferredRender : false,
			split : true
			}
	);
	
	westPanel.add(ontTabPanel);

	return westPanel;
}

//Create the north most panel.
function createNorthPanel()
{
    northPanel = new Ext.Panel({
		id : 'northPanel',
		region : 'north',
		tbar : createUtilitiesMenu(GLOBAL.HelpURL, GLOBAL.ContactUs, GLOBAL.AppTitle,GLOBAL.basePath, GLOBAL.BuildVersion, 'utilities-div'),
		split : false,
		border : true,
		contentEl : "header-div"
	});	

	return northPanel;
}

/*
 * This creates the center panel including toolbar, main category selection, result data grids. 
 */
function createCenterPanel()
{
	//Draw our toolbar. Some items are hidden till we select an initial category.
	var tb2 = new Ext.Toolbar(
			{
				id : 'maintoolbar',
				title : 'maintoolbar',
				items : [
						
						new Ext.Toolbar.Button({
							id : 'advancedbutton',
							text : 'Advanced Workflow',
							iconCls : 'comparebutton',
							hidden :true,
							menu : createAdvancedSubMenu(),
							handler : function() {

							}
						}),
						new Ext.Toolbar.Button({
							id : 'clearsearchbutton',
							text : 'Clear Search',
							iconCls : 'clearbutton',
							hidden : true,
							handler : clearSearch
						}),
						new Ext.Toolbar.Button({
							id : 'addsubset',
							text : 'Add Subset',
							iconCls : 'nextbutton',
							hidden : true,
							handler : addTabbedSubset
						}),								
						'->',
						new Ext.Toolbar.Button(
								{
									id : 'sampleExplorerHelpButton',
									iconCls : 'contextHelpBtn',
									qtip: 'Click for Sample Explorer Help',
									disabled : false,
									handler : function()
									{
									    D2H_ShowHelp("1438",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
									}
								}
						)						
						
						]
			});	
	
	//This is the table panel that holds our "Comparison/Jobs" Tabs.
	resultsTabPanel = new Ext.TabPanel(
			{
				id : 'resultsTabPanel',
				title : 'Analysis/Results',
				region : 'center',
				defaults :
				{
					hideMode : 'display'
				}
			,
			collapsible : false,
			deferredRender : false,
			activeTab : 0
			}
	);
	
	//This is the URL for our main "Comparison" tab.
	var centerPanelURL = pageInfo.basePath+'/sampleExplorer/showTopLevelListPage';
	
	//This is our main "Comparison" tab.
	queryPanel = new Ext.Panel(
			{
				id : 'queryPanel',
				title : 'Comparison',
				region : 'north',
				height : 800,
				autoScroll : true,
				split : true,					
		        autoLoad:
		        {
		        	url: centerPanelURL,
		           	method:'POST',
		           	callback: createSearchBox
		        },	
				collapsible : true,
				titleCollapse : false,
				animCollapse : false,
				bbar: new Ext.StatusBar({
					// Status bar to show the progress of generating heatmap and other advanced workflows
			        id: 'asyncjob-statusbar',
			        defaultText: 'Ready',
			        defaultIconCls: 'default-icon',
			        text: 'Ready',
			        statusAlign: 'right',
			        iconCls: 'ready-icon',
			        items: [{
			        	xtype: 'button',
			        	id: 'cancjob-button',
			        	text: 'Cancel',
			        	hidden: true
			        }]				        
			    })
			}
	);
	
	//This is our Jobs tab.
	analysisJobsPanel = new Ext.Panel(
			{
				id : 'analysisJobsPanel',
				title : 'Jobs',
				region : 'center',
				split : true,
				height : 500,
				layout : 'fit',
				hidden: GLOBAL.EnableGP!='true',
				autoLoad : getJobsData,
				collapsible : true						
			}
	);			
	
	resultsTabPanel.add(queryPanel);
	
	if(GLOBAL.EnableGP=='true'){
	resultsTabPanel.add(analysisJobsPanel);	
	}
	centerPanel = new Ext.Panel({
		id : 'centerPanel',
		region : 'center',
		width : 500,
		border : true,
		tbar : tb2	
	});

	centerPanel.add(resultsTabPanel);
	
	return centerPanel;
}

function createSearchBox()
{

	var combo = new Ext.app.SearchComboBox({
		id: "search-combobox",
		renderTo: "search-text",
		searchUrl: pageInfo.basePath+'/sampleExplorer/loadSearch',
		submitFn: function(param, text) {
			//When we pick an item from the list, load the menus.
			//The param is Category|Item.
			var splitArray = param.split("|");
			
			toggleMainCategorySelection(splitArray[1],splitArray[0])
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
	
	var picklist = new Ext.app.PickList({
		id: "categories",
		cls: "categories-gray",
		storeUrl: pageInfo.basePath+'/sampleExplorer/loadCategories',
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
	
}

function createExportSubMenu()
{
	expmenu = new Ext.menu.Menu({
		id : 'exportMenu',
		minWidth : 250,
		items : [ {
			text : 'Summary Statistics',
			handler : function() {
				if ((typeof (grid) != 'undefined') && (grid != null)) {
					exportGrid();
				} else {
					alert("Nothing to export");
				}
			}
		}, '-', {
			text : 'Gene Expression/RBM Datasets',
			handler : function() {
				exportDataSets();
			}
		} ]
	});	
	
	return expmenu;
}

function createAdvancedSubMenu()
{
	advmenu = new Ext.menu.Menu(
			{
				id : 'advancedMenu',
				minWidth : 250,
				items : [
						{
							text : 'Heatmap',
							// when checked has a boolean value, it is assumed to be a CheckItem
							handler : function() {
								GLOBAL.HeatmapType = 'Compare';
								//We need to do some work before we can validate. Call our sample explorer heatmap code.
								validateHeatMapsSample(showGeneSelection);
								advancedWorkflowContextHelpId = "1085";
							}
						},
				         {
				        	 text : 'Hierarchical Clustering',
				        	 // when checked has a boolean value, it is assumed to be a CheckItem
				        	 handler : function()
				        	 {
				        	 	GLOBAL.HeatmapType = 'Cluster';
				        	 	validateHeatMapsSample(showGeneSelection);
				        	 	advancedWorkflowContextHelpId="1085";
				        	 }
				         }
				         ,
				         {
				        	 text : 'K-Means Clustering',
				        	 // when checked has a boolean value, it is assumed to be a CheckItem
				        	 handler : function()
				        	 {
				        	 	GLOBAL.HeatmapType = 'KMeans';
				        	 	validateHeatMapsSample(showGeneSelection);
				        	 	advancedWorkflowContextHelpId="1085";
				        	 }
				         }
				         ,

				         {
				        	 text : 'Comparative Marker Selection (Heatmap)',
				        	 // when checked has a boolean value, it is assumed to be a CheckItem
				        	 handler : function()
				        	 {
				        	 	GLOBAL.HeatmapType = 'Select';
				        	 	validateHeatMapsSample(showGeneSelection);
				        	 	advancedWorkflowContextHelpId="1085";
				        	 }
				         }
				         ,
				         '-'
				         ,
				         {
				        	 text : 'Principal Component Analysis',
				        	 // when checked has a boolean value, it is assumed to be a CheckItem
				        	 handler : function()
				        	 {
				        	 	GLOBAL.HeatmapType = 'PCA';
				        	 	validateHeatMapsSample(showGeneSelection);
				        	 	advancedWorkflowContextHelpId="1172";
				        	 }
				         }
				         ,
				         /*
				         '-'
				         ,
				         {
				        	 text : 'Haploview',
				        	 handler : function()	{
				        		 
				        		 //Get the Sample ID List from the subsets.
				        		 validateHeatMapsSample(showHaploviewGeneSelection);

				        		 return;
				        	}
				        }
				        ,
				        */
				        {
				        	 text : 'SNPViewer',
				        	 handler : function()	
				        	 {
				        		GLOBAL.HeatmapType = '';
				        		validateHeatMapsSample(showSNPViewerSelection);
				        	 	return;
				        	 }
				        }
				        ,
				        {
				        	 text : 'Integrative Genome Viewer',
				        	 handler : function()	
				        	 {
				        		GLOBAL.HeatmapType = '';
					        	validateHeatMapsSample(showIgvSelection);
				        	 	return;
				        	 }
				        },
				        {
				        	 text : 'Genome-Wide Association Study',
				        	 handler : function()	{
				        		GLOBAL.HeatmapType = '';				        		
					        	validateHeatMapsSample(showGwasSelection);
				        	 	return;			
				        	 }
				        }	
				]
			});

	return advmenu;

}


