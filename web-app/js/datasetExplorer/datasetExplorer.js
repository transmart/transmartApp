
String.prototype.trim = function() {
	return this.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
}
Ext.layout.BorderLayout.Region.prototype.getCollapsedEl = Ext.layout.BorderLayout.Region.prototype.getCollapsedEl.createSequence(function()
		{
	if ((this.position == 'north' || this.position == 'south') && ! this.collapsedEl.titleEl)
	{
		this.collapsedEl.titleEl = this.collapsedEl.createChild(
				{
					style : 'color:#15428b;font:11px/15px tahoma,arial,verdana,sans-serif;padding:2px 5px;', cn : this.panel.title
				}
		);
	}
		}
);

var runner = new Ext.util.TaskRunner();

var wfsWindow = null;

//Grid state
var isGridLoadDone = false;
var isGridLoadRunning = false;
var gridPanelHeaderTips;

var searchPage = "datasetExplorer";

function dataSelectionCheckboxChanged(ctl)
{
	if(getSelected(ctl)[0] != undefined)
	{
		// outputSelected(ctl);
		Ext.getCmp("exportStepDataSelectionNextButton").enable();
	}
}

function setDataAssociationAvailableFlag(el, success, response, options) {

	if (!success) {
		var dataAssociationPanel = Ext.getCmp('dataAssociationPanel');
		var resultsTabPanel = Ext.getCmp('resultsTabPanel');
		resultsTabPanel.remove(dataAssociationPanel);
		resultsTabPanel.doLayout();
	} else {
		Ext.Ajax.request(
		{
			url : pageInfo.basePath+"/dataAssociation/loadScripts",
				method : 'GET',
				timeout: '600000',
				params :  Ext.urlEncode({}),
				success : function(result, request)
				{
					var exp = result.responseText.evalJSON();
					if (exp.success && exp.files.length > 0)	{
						/*for (var i = 0; i < exp.files.length; i++) {
							var file = exp.files[i]
							if (file.type == 'script') {

							}
						}*/
						loadScripts(exp.files);
					}
				},
				failure : function(result, request)
				{
					alert("Unable to process the export: " + result.responseText);
				}
		});
	}
}

/**
 * Load js and css dynamically
 * @param scripts
 */
function loadScripts(scripts) {
    // loop through script array
    for (var i = 0; i < scripts.length; i++) {

        var file = scripts[i];

        if (file.type == 'script') { // if javascript
            $j.getScript(file.path);
        } else if (file.type == 'css') { // if css
            $j('head').append($j('<link rel="stylesheet" type="text/css" />').attr('href', file.path));
        } else {
            console.error("Unknown file type.");
        }
    }
}

Ext.Panel.prototype.setBody = function(html)
{
	var el = this.getEl();
	var domel = el.dom.lastChild.firstChild;
	domel.innerHTML = html;
}

Ext.Panel.prototype.getBody = function(html)
{
	var el = this.getEl();
	var domel = el.dom.lastChild.firstChild;
	return domel.innerHTML;
}

Ext.onReady(function()
		{
	Ext.QuickTips.init();

	//set ajax to 600*1000 milliseconds
	Ext.Ajax.timeout = 1800000;

	// this overrides the above
	Ext.Updater.defaults.timeout = 1800000;


	// create the main regions of the screen
	westPanel = new Ext.Panel(
			{
				id : 'westPanel',
				region : 'west',
				width : 330,
				minwidth : 200,
				split : true,
				border : true,
				layout : 'border'
			}
	);

	/* eastPanel = new Ext.Panel({
   id : 'eastPanel',
   region : 'east',
   width : 330,
   minwidth : 200,
   split : true,
   border : true,
   layout : 'border'}); */

	if(GLOBAL.Config != "jj")
	{
		northPanel = new Ext.Panel(
				{
					id : 'northPanel',
					html : '<div style="padding:5px;background:#eee;font:14pt arial"><table><tr><td><img src="/images/i2b2_hive_32.gif" /></td><td><span style="font:arial 14pt;"><b> i2b2 Web Client</b></span></td></tr></table></div>',
					region : 'north',
					height : 45,
					split : false,
					border : true
				}
		);
	}else
	{
		northPanel = new Ext.Panel(
				{
					id : 'northPanel',
					region : 'north',
					height : 30,
					split : false,
					border : true,
					contentEl: "header-div"
				}
		);
	}

	qphtml = "<div style='margin: 10px'>Query Criteria<br /><select size='8' id='queryCriteriaSelect1' style='width:400px; height:250px;'></select><br />\
		< button onclick = 'resetQuery()' > Reset < / button > < br / > < div id = 'queryCriteriaDiv1' style = 'font:11pt;width:200px; height:250px; white-space:nowrap;overflow:auto;border:1px solid black' > < / div > < / div > "

		var tb = new Ext.Toolbar(
				{
					id : 'maintoolbar',
					title : 'maintoolbar',
					items : [new Ext.Toolbar.Button(
							{
								id : 'changetool',
								text : 'Switch to subset view',
								iconCls : 'nextbutton',
								disabled : false,
								handler : function()
								{
								window.location.href = "i2b2client.jsp"
								}
							}
					)]
				}
		);

		expmenu = new Ext.menu.Menu(
			{
				id : 'exportMenu',
				minWidth: 250,
				items : [{
					text : 'Summary Statistics',
					handler : function()	{
						if((typeof(grid)!='undefined') && (grid!=null))	{
							exportGrid();
						} else {
							alert("Nothing to export");
						}
					}
				}
                /*
				,
				'-'
				,
				{
					text : 'Gene Expression/RBM Datasets',
					handler : function()	{
						exportDataSets();
					}
				}
				*/
				]
			}
		);

		advmenu = new Ext.menu.Menu(
				{
					id : 'advancedMenu',
					minWidth: 250,
					items : [
					         {
					        	 text : 'Heatmap',
					        	 disabled : true,
					        	 // when checked has a boolean value, it is assumed to be a CheckItem
					        	 handler : function()
					        	 {
					        	 	GLOBAL.HeatmapType = 'Compare';
					        	 	validateHeatmap();
					        	 	advancedWorkflowContextHelpId="1085";
					        	 },
					        	 disabled : GLOBAL.GPURL == "" 
					         }
					         ,
					         {
					        	 text : 'Hierarchical Clustering',
					        	 disabled : true,
					        	 // when checked has a boolean value, it is assumed to be a CheckItem
					        	 handler : function()
					        	 {
					        	 	GLOBAL.HeatmapType = 'Cluster';
					        	 	validateHeatmap();
					        	 	advancedWorkflowContextHelpId="1085";
					        	 },
					        	 disabled : GLOBAL.GPURL == ""
					         }
					         ,
					         {
					        	 text : 'K-Means Clustering',
					        	 disabled : true,
					        	 // when checked has a boolean value, it is assumed to be a CheckItem
					        	 handler : function()
					        	 {
					        	 	GLOBAL.HeatmapType = 'KMeans';
					        	 	validateHeatmap();
					        	 	advancedWorkflowContextHelpId="1085";
					        	 },
					        	 disabled : GLOBAL.GPURL == ""
					         }
					         ,
					         {
					        	 text : 'Comparative Marker Selection (Heatmap)',
					        	 disabled : true,
					        	 // when checked has a boolean value, it is assumed to be a CheckItem
					        	 handler : function()
					        	 {
					        	 	GLOBAL.HeatmapType = 'Select';
					        	 	validateHeatmap();
					        	 	advancedWorkflowContextHelpId="1085";
					        	 },
					        	 disabled : GLOBAL.GPURL == ""
					         }
					         ,
				        	 '-' 
					         ,					         
					         {
					        	 text : 'Principal Component Analysis',
					        	 disabled : true,
					        	 // when checked has a boolean value, it is assumed to be a CheckItem
					        	 handler : function()
					        	 {
					        	 	GLOBAL.HeatmapType = 'PCA';
					        	 	validateHeatmap();
					        	 	advancedWorkflowContextHelpId="1172";
					        	 },
					        	 disabled : GLOBAL.GPURL == ""
					         }
					         ,
				        	 '-'
				        	 ,
					         {
					        	 text : 'Survival Analysis',
					        	 handler : function()
					        	 {
					        	 	if(isSubsetEmpty(1) || isSubsetEmpty(2))
					        	 	{
					        			alert('Survival Analysis needs time point data from both subsets.');
					        		 	return;
					        	 	}
					        	 	else {
					        	 		showSurvivalAnalysis();
					        	 	}
					        	 },
					        	 disabled : GLOBAL.GPURL == ""
					         }
					         ,
				        	 '-'
				        	 ,
					         {
					        	 text : 'Haploview',
					        	 handler : function()	{
					        	 	if(isSubsetEmpty(1) && isSubsetEmpty(2))
					        	 	{
					        			alert('Empty subsets found, need a valid subset to analyze!');
					        		 	return;
					        	 	}
					        	 	if((GLOBAL.CurrentSubsetIDs[1] == null && ! isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] == null && ! isSubsetEmpty(2)))
					        	 	{
					        			runAllQueries(function()	{
					        				showHaploviewGeneSelection();
					        			});
					        	 	} else	{
					        	 		showHaploviewGeneSelection()
					        	 	}
					        	 	return;
					        	}
					        }
					        ,
					        {
					        	 text : 'SNPViewer',
					        	 disabled : true,
					        	 handler : function()	{
					        	 	if(isSubsetEmpty(1) && isSubsetEmpty(2))
					        	 	{
					        			alert('Both dataset is empty. Please choose a valid dataset.');
					        		 	return;
					        	 	}
					        	 	if((GLOBAL.CurrentSubsetIDs[1] == null && ! isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] == null && ! isSubsetEmpty(2)))
					        	 	{
					        			runAllQueries(function()	{
					        				showSNPViewerSelection();
					        			});
					        	 	} else	{
					        	 		showSNPViewerSelection();
					        	 	}
					        	 	return;
					        	},
					        	disabled : GLOBAL.GPURL == ""
					        }
					        ,
					        {
					        	 text : 'Integrative Genome Viewer',
					        	 disabled : true,
					        	 handler : function()	{
					        	 	if(isSubsetEmpty(1) && isSubsetEmpty(2))
					        	 	{
					        			alert('Both dataset is empty. Please choose a valid dataset.');
					        		 	return;
					        	 	}
					        	 	if((GLOBAL.CurrentSubsetIDs[1] == null && ! isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] == null && ! isSubsetEmpty(2)))
					        	 	{
					        			runAllQueries(function()	{
					        				showIgvSelection();
					        			});
					        	 	} else	{
					        	 		showIgvSelection();
					        	 	}
					        	 	return;
					        	},
					        	disabled : GLOBAL.GPURL == ""
					        }
					        ,
					        {
					        	 text : 'PLINK',
					        	 disabled : true,
					        	 handler : function()	{
					        	 	if(isSubsetEmpty(1) && isSubsetEmpty(2))
					        	 	{
					        			alert('Both dataset is empty. Please choose a valid dataset.');
					        		 	return;
					        	 	}
					        	 	if((GLOBAL.CurrentSubsetIDs[1] == null && ! isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] == null && ! isSubsetEmpty(2)))
					        	 	{
					        			runAllQueries(function()	{
					        				showPlinkSelection();
					        			});
					        	 	} else	{
					        	 		showPlinkSelection();
					        	 	}
					        	 	return;
					        	}
					        },
					        {
					        	 text : 'Genome-Wide Association Study',
					        	 handler : function()	{
					        	 	if(isSubsetEmpty(1) || isSubsetEmpty(2))
					        	 	{
					        			alert('Genome-Wide Association Study needs control datasets (normal patients) in subset 1, and case datasets (disease patients) in subset 2.');
					        		 	return;
					        	 	}
					        	 	if((GLOBAL.CurrentSubsetIDs[1] == null && ! isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] == null && ! isSubsetEmpty(2)))
					        	 	{
					        			runAllQueries(function()	{
					        				showGwasSelection();
					        			});
					        	 	} else	{
					        	 		showGwasSelection();
					        	 	}
					        	 	return;
					        	}
					        }					   ]
				}
		);


		var tb2 = new Ext.Toolbar(
				{
					id : 'maintoolbar',
					title : 'maintoolbar',
					items : [new Ext.Toolbar.Button(
							{
								id : 'generatesubsetsbutton',
								text : 'Generate Summary Statistics',
								iconCls : 'runbutton',
								disabled : false,
								handler : function()
								{
								// alert('generate');
								GLOBAL.CurrentSubsetIDs[1] = null;
								GLOBAL.CurrentSubsetIDs[2] = null;
								runAllQueries(getSummaryStatistics);

								}
							}
					),

					new Ext.Toolbar.Separator(),
					new Ext.Toolbar.Button(
							{
								id : 'showquerysummarybutton',
								text : 'Summary',
								iconCls : 'summarybutton',
								disabled : false,
								handler : function()
								{
								// alert('clear');
								showQuerySummaryWindow();
								}
							}
					),
					
					new Ext.Toolbar.Separator(),
					new Ext.Toolbar.Button(
							{
								id : 'advancedbutton',
								text : 'Advanced',
								iconCls : 'comparebutton',
								hidden : GLOBAL.EnableGP!='true',
								menu : advmenu,
								handler : function()
								{
								// alert('compare');
								// showCompareStepPathwaySelection();
								}
							}
					),
					
					new Ext.Toolbar.Separator(),
					new Ext.Toolbar.Button(
							{
								id : 'clearsubsetsbutton',
								text : 'Clear',
								iconCls : 'clearbutton',
								disabled : false,
								handler : function()
								{
								if(confirm("Are you sure you want to clear your current analysis?"))
									{
										clearAnalysisPanel();
										resetQuery();
										clearDataAssociation();									
										resetExportTabs();
									}
								// clearGrid(); blah
								}
							}
					),
					new Ext.Toolbar.Separator(),
					new Ext.Toolbar.Button(
							{
								id : 'savecomparsionbutton',
								text : 'Save',
								iconCls : 'savebutton',
								disabled : false,
								handler : function()
								{
								if(isSubsetEmpty(1) && isSubsetEmpty(2))
								{
									alert('Empty subsets found, need at least 1 valid subset to save a comparsion');
									return;
								}
								if((GLOBAL.CurrentSubsetIDs[1] == null && ! isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] == null && ! isSubsetEmpty(2)))
								{
									runAllQueries(function()
											{
										saveComparison();});
								}
								else
								{
									saveComparison();
								}
								return;
								}
							}
					),
                    new Ext.Toolbar.Separator(),
                    new Ext.Toolbar.Button(
                            {
                                id : 'savesubsetsbutton',
                                text : 'Save Subsets',
                                iconCls : 'savebutton',
                                disabled : false,
                                handler : function()
                                {
                                    if(isSubsetEmpty(1) && isSubsetEmpty(2))
                                    {
                                        alert('Empty subsets found, need at least 1 valid subset to save a comparsion');
                                        return;
                                    }
                                    else
                                    {
                                        showSaveSubsetsDialog();
                                    }
                                }
                            }
                    ),
					'->',
					new Ext.Toolbar.Separator(),
					new Ext.Toolbar.Button({
						id : 'exportbutton',
						text : 'Export',
						iconCls : 'exportbutton',
						disabled : false,
						menu : expmenu,
						handler : function(){
						// alert('export');
						// showExportStepSplitTimeSeries();
						// if((typeof(grid)!='undefined') && (grid!=null)){exportGrid();}
						// else {alert("Nothing to export");}
					}}),
					new Ext.Toolbar.Separator(),
					new Ext.Toolbar.Button(
							{
								id : 'printanalysisbutton',
								text : 'Print',
								iconCls : 'printbutton',
								disabled : false,
								handler : function()
								{
								// alert('print');
								// analysisPanel.iframe.print();
								var text = getAnalysisPanelContent();
								printPreview(text);
								}
							}
					),
					new Ext.Toolbar.Separator(),
					new Ext.Toolbar.Button(
							{
								id : 'dataExplorerHelpButton',
								iconCls : 'contextHelpBtn',
								qtip: 'Click for Dataset Explorer Help',
								disabled : false,
								handler : function()
								{
								    D2H_ShowHelp("1258",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
								}
							}
					)
					]
				}
		);




		centerMainPanel = new Ext.Panel(
				{
					id : 'centerMainPanel',
					region : 'center',
					// tbar : tb,
					layout : 'border'
				}
		);

		centerPanel = new Ext.Panel(
				{
					id : 'centerPanel',
					region : 'center',
					minwidth : 600,
					split : true,
					border : true,
					layout : 'fit',
					tbar:tb2
				}
		);

		queryPanel = new Ext.Panel(
				{
					id : 'queryPanel',
					title : 'Comparison',
					region : 'north',
					height : 340,
					autoScroll : true,
					split : true,					
					autoLoad :
					{
						url : pageInfo.basePath+'/panels/subsetPanel.html',
						scripts : true,
						nocache : true,
						discardUrl : true,
						method : 'POST'
					},
					collapsible : true,
					titleCollapse : false,
					animCollapse : false,
			        listeners :
					{
						activate : function() {
							GLOBAL.Analysis="Advanced";
						}
					},
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

		resultsPanel = new Ext.Panel(
				{
					id : 'resultsPanel',
					title : 'Results',
					region : 'center',
					split : true,
					height : 90
            }
        );
        //jira- DEMOTM-115 Bringing Save report/save subset functionality to transmart Demo branch
        var savesubsetsTbar = new Ext.Toolbar(
            {
                id: 'savesubsetsTbar',
                title: 'Save Subsets',
                items: [new Ext.Toolbar.Button(
                    {
                        id: 'savesubsetsButton',
                        text: 'Save Subsets',
                        iconCls: 'savebutton',
                        disabled: false,
                        handler: function () {
                            showSaveSubsetsDialog();

                        }
                    }
                ),
                    new Ext.Toolbar.Button(
                        {
                            id: 'clearButton',
                            text: 'Clear',
                            iconCls: 'clearbutton2',
                            disabled: false,
                            handler: function () {
                                clearQuery();

                            }
                        }
                    )
                ]
            }
        );

		resultsTabPanel = new Ext.TabPanel(
				{
					id : 'resultsTabPanel',
					title : 'Analysis/Results',
					region : 'center',
                tbar: savesubsetsTbar,

					defaults :
					{
					hideMode : 'display'
					}
				,
				collapsible : false,
				//height : 300,
				//width : 300,
				deferredRender : false,
				//split : true,
				//tbar : tb2,
				activeTab : 0,
		        tools:[{
					id:'help help-resana-panel',
					qtip:'Click for Generate Summary Statistics help',
				    handler: function(event, toolEl, panel){
				    	D2H_ShowHelp("1074",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
				    },
				hidden:true
		        }]
				}
		);


            analysisGridPanel = new Ext.Panel(
                {
                    id : 'analysisGridPanel',
                    title : 'Grid View',
                    region : 'center',
                    split : true,
                    height : 90,
                    layout : 'fit',
                    listeners :
                    {
                        activate: function(p)
                        {
                            //If we haven't loaded the subsets yet, do so and load the grid.
                            if (GLOBAL.CurrentSubsetIDs[1] == null && GLOBAL.CurrentSubsetIDs[2] == null)
                            {
                                analysisGridPanel.body.mask('Getting subsets...', 'x-mask-loading');

                                //Run the query and get the grid data.
                                runAllQueries(getSummaryGridData, p);
                            }
                            else if(!GLOBAL.isGridViewLoaded)
                            {
                                analysisGridPanel.body.mask('Getting subsets...', 'x-mask-loading');

                                //The grid isn't loaded but subsets have been run already, just load the grid.
                                getSummaryGridData();
                            }
                            else
                            {
                                //The grid is already loaded, just try to resize it.
                                trySizeGridTable ();
                            }
                        }
                    }
                }
        );

        analysisPanel = new Ext.Panel(
            {
                id: 'analysisPanel',
                title: 'Results/Analysis',
                region: 'center',
                fitToFrame: true,
                listeners: {
                    activate: function () {
                        if (GLOBAL.AnalysisHasBeenRun == false) {
                            GLOBAL.CurrentSubsetIDs[1] = null;
                            GLOBAL.CurrentSubsetIDs[2] = null;
                            resultsTabPanel.body.mask("Performing queries...", 'x-mask-loading');
                            runAllQueries(getSummaryStatistics, resultsTabPanel);
                            activateTab();
                        }
                    },
                    deactivate: function () {
                        // resultsTabPanel.tools.help.dom.style.display="none";
                    },
                    'afterLayout': {
                    }
                },
                autoScroll: true,
                // tbar : tb2,
                html: '<div style="text-align:center;font:12pt arial;width:100%;height:100%;"><table style="width:100%;height:100%;"><tr><td align="center" valign="center">Drag concepts to this panel to view a breakdown of the subset by that concept</td></tr></table></div>',
                split: true,
                closable: false,
                height: 90
            }
        );
		/*
		 * Commented out the Jobs panel to hide as it isn't used without Gene Pattern
		 * 
		 * analysisJobsPanel = new Ext.Panel(
				{
					id : 'analysisJobsPanel',
					title : 'Jobs',
					region : 'center',
					split : true,
					height : 90,
					layout : 'fit',
					//autoLoad : getJobsData,
					listeners :
					{
						activate : function(p) {
							getJobsData(p)
						},
						deactivate: function(){
							//resultsTabPanel.tools['help help-resana-panel'].dom.style.display="none";
						}
					},
					collapsible : true						
				}
		);*/
		analysisDataExportPanel = new Ext.Panel(
				{
					id : 'analysisDataExportPanel',
					title : 'Data Export',
					region : 'center',
					split : true,
					height : 90,
					layout : 'fit',
					//autoLoad : getDatadata,
					listeners :
					{
						activate : function(p) {
							GLOBAL.CurrentSubsetIDs[1] = null;
							GLOBAL.CurrentSubsetIDs[2] = null;
							p.body.mask("Loading...", 'x-mask-loading');
							runAllQueries(getDatadata, p);
			        	 	return;
						},
						deactivate: function(){
							//resultsTabPanel.tools['help help-resana-panel'].dom.style.display="none";
                        if (Ext.get('dataTypesGridPanel')) {
                            this.remove('dataTypesGridPanel');
                        }
                        if (Ext.get('advancedDataExportPanel')) {
                            this.remove('advancedDataExportPanel');
                        }
						}
					},
					collapsible : true						
				}
		);
		
		dataAssociationPanel = new Ext.Panel(
				{
					id : 'dataAssociationPanel',
					title : 'Advanced Workflow',
					region : 'center',
					split : true,
					height : 90,
					layout : 'fit',
					tbar : new Ext.Toolbar({
						id : 'advancedWorkflowToolbar',
						title : 'Advanced Workflow actions',
						items : []
						}),
					autoScroll : true,
					autoLoad:
			        {
			        	url : pageInfo.basePath+'/dataAssociation/defaultPage',
			           	method:'POST',
			           	callback: setDataAssociationAvailableFlag,
			           	evalScripts:true
			        },
			        /*buttons: [{
						text:'Run Job',
						handler: function()	{
							var analysis = Ext.get('analysis');
							if (analysis != undefined) {
								var selectedAnalysis = analysis.dom.value;
								if (selectedAnalysis != '') {
									selectedAnalysis = selectedAnalysis.charAt(0).toUpperCase()+selectedAnalysis.substring(1);
									eval("submit"+selectedAnalysis+"Job(this.form)");
								} else {
									Ext.Msg.alert('Analysis required!!!', 'Please select an Analysis from the \'Analysis\' menu.')
								}
							}
						}      	
			        }],
			        buttonAlign:'center',*/
			        listeners :
					{
			        	activate : function() {
							GLOBAL.Analysis="dataAssociation";
							renderCohortSummary();
							//Ext.getCmp('dataAssociationBodyPanel').focus()
						}
					},
					collapsible : true
				}
		);
		
		analysisExportJobsPanel = new Ext.Panel(
				{
					id : 'analysisExportJobsPanel',
					title : 'Export Jobs',
					region : 'center',
					split : true,
					height : 90,
					layout : 'fit',
					//autoLoad : getExportJobs,
					listeners :
					{
						activate : function(p) {
							getExportJobs(p)
						},
						deactivate: function(){
							//resultsTabPanel.tools['help help-resana-panel'].dom.style.display="none";
						}
					},
					collapsible : true						
				}
		);

		/**
		 * panel to display list of jobs belong to a user
		 * @type {Ext.Panel}
		 */
		analysisJobsPanel = new Ext.Panel(
			{
				id : 'analysisJobsPanel',
				title : 'Analysis Jobs',
				region : 'center',
				split : true,
				height : 90,
				layout : 'fit',
				listeners :
				{
					activate : function(p) {
						getJobsData(p)
					}
				},
				collapsible : true
			}
		);

        //jira DEMOTM-138 Workspaces tab - subset and report grid CRUD
        var workspacePanel = new Ext.Panel(
                {
                    id : 'workspacePanel',
                    title : 'Workspace',
                    region : 'center',
                    split : true,
                    height : 90,
                    layout : 'fit',
                    autoScroll : true,
                    listeners :
                    {
                        activate : function(p) {
                            renderWorkspace(p)
                        },
                        deactivate: function(){

                        }
                    },
                    collapsible : true
                }
        );

		metacoreEnrichmentPanel = new Ext.Panel(
				{
					id : 'metacoreEnrichmentPanel',
					title : 'MetaCore Enrichment Analysis',
					region : 'center',
					split : true,
					height : 90,
					layout : 'fit',
					//autoLoad : getExportJobs,
					tbar : new Ext.Toolbar({
						id : 'metacoreEnrichmentWorkflowToolbar',
						title : 'Analysis menu',
						items : []
						}),
					autoScroll : true,
					autoLoad:
			        {
			        	url : pageInfo.basePath+'/metacoreEnrichment/index',
			           	method:'GET',
			           	// callback: setDataAssociationAvailableFlag,
			           	evalScripts:true
			        },
					listeners :
					{
						activate : function(p) {
							renderCohortSummaryMetaCoreEnrichment("cohortSummaryMetaCoreEnrichment");
							initMetaCoreTab();
						},
						deactivate: function(){
							//resultsTabPanel.tools['help help-resana-panel'].dom.style.display="none";
						}
					},
					collapsible : true
				}
		);
		
		resultsTabPanel.add(queryPanel);
		resultsTabPanel.add(dataAssociationPanel);
		resultsTabPanel.add(analysisPanel);
		resultsTabPanel.add(analysisGridPanel);
		//Commented out the Jobs panel to hide as it isn't used without Gene Pattern
		//resultsTabPanel.add(analysisJobsPanel);
		resultsTabPanel.add(analysisDataExportPanel);
		resultsTabPanel.add(analysisExportJobsPanel);
		resultsTabPanel.add(analysisJobsPanel);
        //jira DEMOTM-138 Workspaces tab - subset and report grid CRUD
        resultsTabPanel.add(workspacePanel);

        // add genome browser panel if the plugin is installed

        // JBROWSE
        // =======
        Ext.Ajax.request ({
            url: pageInfo.basePath+"/pluginDetector/checkPlugin",
            method: 'POST',
            success: function (result) {
                if (result.responseText === 'true') {

                    // load script
                    Ext.Ajax.request ({
                        url: pageInfo.basePath+"/JBrowse/loadScripts",
                        method: 'POST',
                        success: function (result) {
                            var exp = result.responseText.evalJSON();
                            if (exp.success && exp.files.length > 0)	{

                                var filesArr = [];

                                for (var i = 0; i < exp.files.length; i++) {
                                    var file = exp.files[i];
                                    filesArr.push(file.path);
                                }

                                dynamicLoad.loadScriptsSequential(filesArr, startDalliance);

                            }
                        }
                    });

                }
            },
            params: {
                pluginName: 'jbrowse-plugin'
            }
        });

        // DALLIANCE
        // =======
        Ext.Ajax.request ({
            url: pageInfo.basePath+"/pluginDetector/checkPlugin",
            method: 'POST',
            success: function (result) {

                var _this = this;

                if (result.responseText === 'true') {

                    // load script
                    Ext.Ajax.request ({
                        url: pageInfo.basePath+"/Dalliance/loadScripts",
                        method: 'POST',
                        success: function (result) {

                            var resultJSON = JSON.parse(result.responseText);
                            var filesArr = [];

                            if (resultJSON.success && resultJSON.files.length > 0)	{
                                for (var i = 0; i < resultJSON.files.length; i++) {

                                    var aFile = resultJSON.files[i];
                                    filesArr.push(aFile.path);
                                }
                                dynamicLoad.loadScriptsSequential(filesArr, startDalliance);
                            }
                        },
                        failure: function() {
                            console.error("Cannot load plugin scripts for " + _this.pluginName);
                        }
                    });


                }
            },
            params: {
                pluginName: 'dalliance-plugin'
            }
        });

        function startDalliance() {
            loadDalliance(resultsTabPanel);
        }

        function startJbrowse() {
            loadJBrowse(resultsPanel);
        }

		if (GLOBAL.metacoreAnalyticsEnabled) {
            // At the moment the metacoreanalysis is in Early alpha so we won't display it.
            if(false){
			resultsTabPanel.add(metacoreEnrichmentPanel);
            }
        }
		
		southCenterPanel = new Ext.Panel(
				{
					id : 'southCenterPanel',
					region : 'center',
					layout : 'border',
					split : true,
					tbar : tb2
				}
		);

		exportPanel = new Ext.Panel(
				{
					id : 'exportPanel',
					title : 'Compare/Export',
					region : 'east',
					html : '<div style="text-align:center;font:12pt arial;width:100%;height:100%;"><table style="width:100%;height:100%;"><tr><td align="center" valign="center">Drag subsets to this panel to compare and export them</td></tr></table></div>',
					split : true,
					width : 300,
					height : 90,
					buttons : [
					           {
					        	   text : 'Compare',
					        	   handler : function()
					        	   {
					        	   var subsets = exportPanel.body.dom.childNodes;
					        	   if (subsets.length != 2)
					        	   {
					        		   alert("Must have two subsets!");
					        	   }
					        	   else showCompareStepPathwaySelection();
					        	   }
					           }
					           ,
					           {
					        	   text : 'Export',
					        	   iconCls : 'exportbutton',
					        	   handler : function()
					        	   {
					        	   showExportStepSplitTimeSeries();
					        	   }
					           }
					           ,
					           {
					        	   text : 'Clear',
					        	   iconCls : 'clearbutton',
					        	   handler : function()
					        	   {
					        	   clearExportPanel();
					        	   }
					           }

					           ]
				}
		);

		var treetitle = "Previous Queries";
		if(GLOBAL.Config == 'jj')
			treetitle = "Subsets";

		var Tree = Ext.tree;
		prevTree = new Tree.TreePanel(
				{
					id : 'previousQueriesTree',
					title : treetitle,
					animate : false,
					autoScroll : true,
					// loader : new Ext.ux.OntologyTreeLoader({dataUrl : 'none'}),
					enableDrag : true,
					ddGroup : 'makeQuery',
					containerScroll : true,
					enableDrop : false,
					region : 'south',
					rootVisible : false,
					expanded : true,
					split : true,
					height : 300
				}
		);

		prevTreeRoot = new Tree.TreeNode(
				{
					text : 'root',
					draggable : false,
					id : 'prevroot',
					qtip : 'root'
				}
		);



		prevTree.setRootNode(prevTreeRoot);

		// start filling in each region with the content panels
		// if(GLOBAL.Config == "jj")
		// {
		// southCenterPanel.add(exportPanel);
		// }
		// southCenterPanel.add(resultsPanel);
		// southCenterPanel.add(analysisPanel);
		// southCenterPanel.add(southCenterCenterPanel);


		/*southCenterPanel.add(resultsTabPanel);
         centerPanel.add(queryPanel);*/

		/**********new prototype*********/

		centerPanel.add(resultsTabPanel);
		/********************************/

		//centerPanel.add(southCenterPanel);


		// centerPanel.add(analysisPanel);
		// centerPanel.add(resultsTabPanel);
		
		westPanel.add(createOntPanel());
		//setTimeout("loadOntPanel()", 3000);
		// westPanel.add(prevTree);
		// eastPanel.add(exportPanel);
		centerMainPanel.add(westPanel);
		centerMainPanel.add(centerPanel);

		viewport = new Ext.Viewport(
				{
					layout : 'border',
					items : [centerMainPanel, northPanel]
				}
		);

		Ext.get(document.body).addListener('contextmenu', contextMenuPressed);
		// prevTree.dragZone.addToGroup("export");


		// preload the setvalue dialog
		setvaluePanel = new Ext.Panel(
				{
					id : 'setvaluePanel',
					region : 'north',
					height : 140,
					width : 490,
					split : false,
					autoLoad :
					{
					url : pageInfo.basePath+'/panels/setValueDialog.html',
					scripts : true,
					nocache : true,
					discardUrl : true,
					method : 'POST'
					}
				}
		);

		setvaluechartsPanel1 = new Ext.Panel(
				{
					id : 'setvaluechartsPanel1',
					region : 'center',
					width : 245,
					height : 180,
					split : false
				}
		);

		setvaluechartsPanel2 = new Ext.Panel(
				{
					id : 'setvaluechartsPanel2',
					region : 'east',
					width : 245,
					height : 180,
					split : false
				}
		);

		// preload the setvalue dialog
		if( ! this.setvaluewin)
		{
			setvaluewin = new Ext.Window(
					{
						id : 'setValueWindow',
						title : 'Set Value',
						layout : 'border',
						width : 500,
						height : 240,
						closable : false,
						plain : true,
						modal : true,
						border : false,
						items : [setvaluePanel , setvaluechartsPanel1, setvaluechartsPanel2],
						buttons : [
						           {
						        	   text : 'Show Histogram',
						        	   handler : function()
						        	   {
						        	   showConceptDistributionHistogram();
						        	   }
						           }
						           ,
						           {
						        	   text : 'Show Histogram for subset',
						        	   handler : function()
						        	   {
						        	   var subset;
						        	   if(selectedConcept.parentNode.id == "hiddenDragDiv")
						        	   {
						        		   subset = getSubsetFromPanel(STATE.Target);
						        	   }
						        	   else
						        	   {
						        		   subset = getSubsetFromPanel(selectedConcept.parentNode)
						        	   }

                                if(!isSubsetEmpty(subset))
                                {

                                    //AccrossTrial Changes 
                                    if(GLOBAL.codeType == "Modifier")
                                    {
                                        runQueryAcrossTrial(subset, showConceptDistributionHistogramForSubset);
                                    }
                                    else
                                    {
                                        runQuery(subset, showConceptDistributionHistogramForSubset);
                                    }
                                    //AccrossTrial Changes
                                }
                                else alert('Subset is empty!');
                            }
                        }
                        ,
                        {
                            text : 'OK',
                            handler : function()
                            {
                                var mode = getSelected(document.getElementsByName("setValueMethod"))[0].value;
                                var highvalue = document.getElementById("setValueHighValue").value;
                                var lowvalue = document.getElementById("setValueLowValue").value;
                                var units = document.getElementById("setValueUnits").value;
                                var operator = document.getElementById("setValueOperator").value;
                                var highlowselect = document.getElementById("setValueHighLowSelect").value;

						        	   // make sure that there is a value set
						        	   if (mode=="numeric" && operator == "BETWEEN" && (highvalue == "" || lowvalue== "")){
						        		   alert('You must specify a low and a high value.');
						        	   } else if (mode=="numeric" && lowvalue == "") {
						        		   alert('You must specify a value.');
						        	   } else {
						        		   setvaluewin.hide();
						        		   setValueDialogComplete(mode, operator, highlowselect, highvalue, lowvalue, units);
						        	   }
						        	   }
						           }
						           ,
						           {
						        	   text : 'Cancel',
						        	   handler : function()
						        	   {
						        	   setvaluewin.hide();
						        	   }
						           }
						           ],
						           resizable : false,
							        tools:[{
										id:'help',
										qtip:'Click for context sensitive help',
									    handler: function(event, toolEl, panel){
									    	D2H_ShowHelp("1239", helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
									    }
							        }]

					}
			);
			setvaluewin.show();
			setvaluewin.hide();
		}


		showLoginDialog();
		var h=queryPanel.header;
		//alert(h);
		}


);

/*
This function will make a quick call to the server to check
a session variable that if set indicates that it is OK
to export the datasets since the user ran the one of the
heatmap options and loaded the gene expression data
*/
function exportDataSets()
{
	Ext.get("exportdsform").dom.submit();

/*	Ext.Ajax.request(
	{
		url : pageInfo.basePath+"/export/check",
			method : 'POST',
			timeout: '600000',
			params :  Ext.urlEncode({}),
			success : function(result, request)
			{
				var exp = result.responseText.evalJSON();
				if (exp.ready)	{
					Ext.get("exportdsform").dom.submit();
				} else	{
					alert("Nothing to export");
				}
			},
			failure : function(result, request)
			{
				alert("Unable to process the export: " + result.responseText);
			}
	}); */
 }

function hasMultipleTimeSeries()
{
	return true;
}

function createOntPanel()
{

    // make tab panel, search panel, ontTree and combine them
    ontTabPanel = new Ext.TabPanel(
        {
            id : 'ontPanel',
            region : 'center',
            defaults :
            {
                hideMode : 'offsets'
            }
            ,
            collapsible : false,
            height : 300,
            width : 316,
            deferredRender : false,
            split : true,
            listeners: {
                tabchange: function(tp,newTab) {
                    if(newTab.id == "acrossTrialTreePanel")
                    {
                        GLOBAL.codeType = "Modifier"
                    }
                    else
                    {
                        GLOBAL.codeType = "Concept"
                    }

                }
            }
        }
    );

	/* ontSearchTermsPanel = new Ext.TabPanel({
   id : 'searchTermsPanel',
   title : 'Find Terms',
   region : 'center',
   deferredRender : false,
   border : true}); */



	ontSearchByCodePanel = new Ext.Panel(
			{
				id : 'searchByCodePanel',
				title : 'Search by Codes',
				region : 'center'
			}
	);


	searchByNamePanel = new Ext.Panel(
			{
				title : 'Search by Names',
				id : 'searchByNamePanel',
				region : 'center',
				height : 500,
				width : 300,
				border : true,
				bodyStyle : 'background:lightgrey;',
				layout : 'border',
				split : true
			}
	);

	// make the ontSerchByNamePanel
	shtml='<table style="font:10pt arial;"><tr><td><select id="searchByNameSelect"><option value="left">Starting with</option><option value="right">Ending with</option>\
		<option value="contains" selected>Containing</option><option value="exact">Exact</option></select>&nbsp;&nbsp;</td><td><input id="searchByNameInput" onkeypress="if(enterWasPressed(event)){searchByName();}" type="text" size="15" />&nbsp;</td>\
		<td><button onclick="searchByName()">Find</button></td></tr><tr><td colspan="2">Select Ontology:<select id="searchByNameSelectOntology"></select></td></tr></table>';

		searchByNameForm = new Ext.Panel(
				{
					// title : 'Search by Form',
					id : 'searchByNameForm',
					region : 'north',
					bodyStyle : 'background:#eee;padding: 10px;',
					html : shtml,
					height : 70,
					border : true,
					split : false
				}
		);

		// shorthand
		var Tree = Ext.tree;

		searchByNameTree = new Tree.TreePanel(
				{
					id : 'searchByNameTree',
					// title : 'Search Results',
					animate : false,
					autoScroll : true,
					loader : new Ext.ux.OntologyTreeLoader(
							{
								dataUrl : 'none'
							}
					),
					enableDrag : true,
					// bodyStyle : 'padding 10px;',
					ddGroup : 'makeQuery',
					containerScroll : true,
					enableDrop : false,
					region : 'center',
					rootVisible : false,
					expanded : true,
					split : true,
					border : true,
					height : 400
				}
		);

		searchByNameTreeRoot = new Tree.TreeNode(
				{
					text : 'root',
					draggable : false,
					id : 'root',
					qtip : 'root'
				}
		);
		// add a tree sorter in folder mode
		new Tree.TreeSorter(searchByNameTree,
				{
			folderSort : true
				}
		);

		searchByNameTree.setRootNode(searchByNameTreeRoot);
		searchByNamePanel.add(searchByNameForm);
		searchByNamePanel.add(searchByNameTree);
//		******************************************************************************
//		FILTER PANEL
//		******************************************************************************
		var showFn = function(node, e){
			Ext.tree.TreePanel.superclass.onShow.call(this);
			//Ext.get('advancedbutton').dom.style.display='';
		}
		ontFilterPanel = new Ext.Panel(
				{
					title : 'Search Terms',
					id : 'ontFilterPanel',
					region : 'center',
					height : 500,
					width : 300,
					border : true,
					bodyStyle : 'background:lightgrey;',
					onShow : showFn,
					layout : 'border'
						//layout: 'table',
						//layoutConfig:{columns:1},
						//split : true
				}
		);

		ontFilterForm = new Ext.Panel(
				{
					title : 'Search',
					id : 'ontFilterForm',
					region : 'north',
					bodyStyle : 'background:#eee;padding: 10px;',
					//html : shtml,
					height : 130,
					border : true,
					split : false,
					//autoScroll: true,
					autoLoad :
					{
					url : pageInfo.basePath+'/ontology/showOntTagFilter',
					scripts : true,
					nocache : true,
					discardUrl : true,
					method : 'POST',
					callback : ontFilterLoaded
					},
			        tools:[{
						id:'help',
						qtip:'Click for context sensitive help',
					    handler: function(event, toolEl, panel){
					    	D2H_ShowHelp("1065",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
					    }
			        }]
				// collapsible: true
				}
		);

		// shorthand
		var Tree = Ext.tree;

		ontFilterTree = new Tree.TreePanel(
				{
					id : 'ontFilterTree',
					// title : 'Search Results',
					animate : false,
					autoScroll : true,
					loader : new Ext.ux.OntologyTreeLoader(
							{
								dataUrl : 'none'
							}
					),
					enableDrag : true,
					// bodyStyle : 'padding 10px;',
					ddGroup : 'makeQuery',
					containerScroll : true,
					enableDrop : false,
					region : 'center',
					rootVisible : false,
					expanded : true,
					//split : true,
					border : true,
					height : 400
				}
		);

		ontFilterTreeRoot = new Tree.TreeNode(
				{
					text : 'root',
					draggable : false,
					id : 'root',
					qtip : 'root'
				}
		);
		// add a tree sorter in folder mode
		new Tree.TreeSorter(ontFilterTree,
				{
			folderSort : true
				}
		);

		ontFilterTree.setRootNode(ontFilterTreeRoot);
		ontFilterPanel.add(ontFilterForm);
		ontFilterPanel.add(ontFilterTree);
		// ontTabPanel.add(ontSearchByCodePanel);

    setupOntTree('navigateTermsPanel', 'Navigate By Study');

		return ontTabPanel;
}


function setupOntTree(id_in, title_in) {

    var Tree = Ext.tree;

    var showFn = function (node, e) {
        Ext.tree.TreePanel.superclass.onShow.call(this);
    }

    var ontTree = new Tree.TreePanel(
        {
            id: id_in,
            title: title_in,
            animate: false,
            autoScroll: true,
            loader: new Ext.ux.OntologyTreeLoader(
                {
                    dataUrl: 'none'
                }
            ),
            enableDrag: true,
            ddGroup: 'makeQuery',
            containerScroll: true,
            enableDrop: false,
            region: 'center',
            rootVisible: false,
            expanded: true,
            onShow: showFn
        }
    );

    ontTree.on('startdrag', function (panel, node, event) {
            Ext.ux.ManagedIFrame.Manager.showShims()

        }
    );

    ontTree.on('enddrag', function (panel, node, event) {
            Ext.ux.ManagedIFrame.Manager.hideShims()

        }
    );

    var treeRoot = new Tree.TreeNode(
        {
            text: 'root',
            draggable: false,
            id: 'treeRoot',
            qtip: 'root'
        }
    );

    // add a tree sorter in folder mode
    new Tree.TreeSorter(ontTree,
{
            folderSort: true
        }
    );


    ontTree.setRootNode(treeRoot);
    ontTabPanel.add(ontTree);
    ontTabPanel.doLayout();

    onWindowResize();

}

function onWindowResize() {
    //Assorted hackery for accounting for the presence of the toolbar
    var windowHeight = jQuery(window).height();

    jQuery('#centerMainPanel').css('top', jQuery('#header-div').height());

    //jQuery('#navigateTermsPanel .x-panel-body').height(windowHeight - boxHeight - 110);

    jQuery('#analysisPanel .x-panel-body').height(jQuery(window).height() - 65);

    if (jQuery('#dataTypesGridPanel .x-panel-body').size() > 0) {
        var exportPanelTop = jQuery('#dataTypesGridPanel .x-panel-body').offset()['top'];
        jQuery('#dataTypesGridPanel .x-panel-body').height(jQuery(window).height() - exportPanelTop - 40);
    }
    if (jQuery('#dataAssociationPanel .x-panel-body').size() > 0) {
        var panelTop = jQuery('#dataAssociationPanel .x-panel-body').offset()['top'];
        jQuery('#dataAssociationPanel .x-panel-body').height(jQuery(window).height() - panelTop);
    }
    if (jQuery('#resultsTabPanel .x-tab-panel-body').size() > 0) {
        var panelTop = jQuery('#resultsTabPanel .x-tab-panel-body').offset()['top'];
        jQuery('#resultsTabPanel .x-tab-panel-body').height(jQuery(window).height() - panelTop);
    }
    if (jQuery('#dataAssociationBody').size() > 0) {
        var panelTop = jQuery('#dataAssociationBody').offset()['top'];
        jQuery('#dataAssociationBody').height(jQuery(window).height() - 50);
    }
    jQuery('#noAnalyzeResults').prependTo(jQuery('#navigateTermsPanel .x-panel-body'));
}

function closeBrowser() {
	window.open('http://www.i2b2.org', '_self', '');
	window.close();
}
function showLoginDialog()
{

	loginwin = new Ext.Window(
			{
				id : 'loginWindow',
				title : 'Login',
				layout : 'fit',
				width : 350,
				height : 140,
				closable : false,
				plain : true,
				modal : true,
				border : false,
				resizable : false
			}
	);

	var txtboxdomain = new Ext.form.TextField(
			{
				fieldLabel : 'Domain',
				id : 'txtFieldDomain',
				name : 'domain'
			}
	);

	var txtboxusername = new Ext.form.TextField(
			{
				fieldLabel : 'Username',
				name : 'username'
			}
	);

	txtboxpassword = new Ext.form.TextField(
			{
				fieldLabel : 'Password',
				name : 'password',
				inputType : 'password'
			}
	);

	loginform = new Ext.FormPanel(
			{
				id : 'loginForm',
				labelWidth : 75,
				frame : true,
				region : 'center',
				width : 350,
				height : 130,
				defaults :
				{
				width : 230
				}
			,
			defaultType : 'textfield',
			items : [txtboxusername, txtboxpassword],
			buttons : [
			           {
			        	   text : 'Login',
			        	   handler : function()
			        	   {
			        	   loginform.el.mask('Logging in...', 'x-mask-loading');
			        	   login(txtboxdomain.getValue(), txtboxusername.getValue(), txtboxpassword.getValue());
			        	   }
			           }
			           ,
			           {
			        	   text : 'Cancel',
			        	   handler : closeBrowser
			           }
			           ]
			}
	);


	if(GLOBAL.AutoLogin)
	{
		login(GLOBAL.Domain, GLOBAL.Username, GLOBAL.Password);
	}
	else
	{
		loginwin.add(loginform);
		loginwin.show(viewport);

		txtboxpassword.getEl().addListener('keypress', function(e)
				{
			if(enterWasPressed(e))
			{
				loginform.el.mask('Logging in...', 'x-mask-loading');
				login(txtboxdomain.getValue(), txtboxusername.getValue(), txtboxpassword.getValue());
			}
				}
		);
	}
}


function login(domain, username, password)
{
	GLOBAL.Domain = domain;
	GLOBAL.Username = username;
	GLOBAL.Password = password;
	getServices();
}

function loginComplete()
{
    if(loginform.isVisible())
    {
        loginform.el.unmask();
        loginwin.hide();
    }

    projectDialogComplete();
	
	// Login GenePattern server. The login process should be completed by the time a user starts GenePattern tasks.
	genePatternLogin();
}

// TODO Check for unused function !
function showProjectDialog(projects)
{

	// create the array
	Ext.projects = [];

	/* Ext.projects = [
      ['AL', 'Alabama'],
      ['AK', 'Alaska'],
      ['AZ', 'Arizona'],
      ['AR', 'Arkansas'],
      ['CA', 'California'],
      ['CO', 'Colorado'],
      ['CN', 'Connecticut'],
      ['DE', 'Delaware'],
      ['DC', 'District of Columbia'] ]; */

	// populate the array
	for(c = 0; c < projects.length; c ++ )
	{
		var p = projects[c].getAttribute("id");
		var a = [];
		a[0] = p;
		a[1] = p;
		Ext.projects[c] = a;
	}


	projectwin = new Ext.Window(
			{
				id : 'projectWindow',
				title : 'Projects',
				layout : 'fit',
				width : 350,
				height : 140,
				closable : false,
				plain : true,
				modal : true,
				border : false,
				resizable : false
			}
	);

	// simple array store
	var store = new Ext.data.SimpleStore(
			{
				fields : ['id', 'projects'],
				data : Ext.projects
			}
	);



	var drdprojects = new Ext.form.ComboBox(
			{
				id : 'drdproject',
				name : 'drdproject',
				title : 'Projects',
				store : store,
				fieldLabel : 'Projects',
				displayField : 'projects',
				typeAhead : true,
				mode : 'local',
				triggerAction : 'all',
				emptyText : 'Select a project...',
				selectOnFocus : true
			}
	);




	projectform = new Ext.FormPanel(
			{
				id : 'projectForm',
				labelWidth : 75,
				frame : true,
				region : 'center',
				width : 350,
				height : 130,
				defaults :
				{
				width : 230
				}
			,
			defaultType : 'textfield',
			items : [drdprojects],
			buttons : [
			           {
			        	   text : 'Select',
			        	   handler : function()
			        	   {
			        	   projectwin.hide();
			        	   projectDialogComplete(drdprojects.getValue());
			        	   }
			           }
			           ,
			           {
			        	   text : 'Cancel',
			        	   handler : closeBrowser
			           }
			           ]
			}
	);

	projectwin.add(projectform);
	projectwin.show(viewport);
}



function projectDialogComplete()
{
	getCategories();

	if(GLOBAL.RestoreComparison)
	{
		getPreviousQueryFromID(1, GLOBAL.RestoreQID1);
		getPreviousQueryFromID(2, GLOBAL.RestoreQID2);
	}
	if((!GLOBAL.Tokens.indexOf("EXPORT")>-1) && (!GLOBAL.IsAdmin))
	{
		Ext.getCmp("exportbutton").disable();
	}
}

function getPreviousQueriesComplete(response) {
    // alert(response.responseText);
    // shorthand
    var Tree = Ext.tree;
    // add a tree sorter in folder mode
    // new Tree.TreeSorter(ontTree, {folderSort : true});

    if (GLOBAL.Debug) {
        alert(response.responseText);
    }
    // clear the tree
    for (c = prevTreeRoot.childNodes.length - 1; c >= 0;
         c--) {
        prevTreeRoot.childNodes[c].remove();
    }
    // prevTree.render();

    var querymasters = response.responseXML.selectNodes('//query_master');
    for (var c = 0; c < querymasters.length; c++) {
        var querymasterid = querymasters[c].selectSingleNode('query_master_id').firstChild.nodeValue;
        var name = querymasters[c].selectSingleNode('name').firstChild.nodeValue;
        var userid = querymasters[c].selectSingleNode('user_id').firstChild.nodeValue;
        var groupid = querymasters[c].selectSingleNode('group_id').firstChild.nodeValue;
        var createdate = querymasters[c].selectSingleNode('create_date').firstChild.nodeValue;
        // set the root node
        var prevNode = new Tree.TreeNode(
            {
                text: name,
                draggable: true,
                id: querymasterid,
                qtip: name,
                userid: userid,
                groupid: groupid,
                createdate: createdate,
                leaf: true
            }
        );
        prevNode.addListener('contextmenu', previousQueriesRightClick);
        prevTreeRoot.appendChild(prevNode);
    }
}


function getCategoriesComplete(ontresponse) {
    ontTabPanel.add(ontFilterPanel);
    // TODO This needs to be investigated further
    if (ontFilterTree.dragZone)
        ontFilterTree.dragZone.addToGroup("analysis");
    getSubCategories('navigateTermsPanel', 'Navigate By Study', ontresponse);
	if(GLOBAL.hideAcrossTrialsPanel != 'true') {
        getSubCategories('crossTrialsPanel', 'Across Trials', ontresponse);
    }
    drawAcrossTrialTree();
    setActiveTab();
}


function getCategoriesComplete_OLD(ontresponse) {

    ontTabPanel.add(ontFilterPanel);
    ontFilterTree.dragZone.addToGroup("analysis");
    getSubCategories('navigateTermsPanel', 'Navigate Terms', ontresponse);
    if(GLOBAL.hideAcrossTrialsPanel != 'true') {
        getSubCategories('crossTrialsPanel', 'Across Trials', ontresponse);
    }

    drawHardcodedJQueryTree();
    setActiveTab();
}

function setActiveTab(){
	//var activeTab='ontFilterPanel';
	var activeTab='navigateTermsPanel';
    if ((GLOBAL.PathToExpand!==''&&GLOBAL.PathToExpand.indexOf('Across Trials')>-1)&&(GLOBAL.hideAcrossTrialsPanel!='true'))
        activeTab='crossTrialsPanel';
	ontTabPanel.setActiveTab(activeTab);
}

/*If includeExcludeFlag is
 * -"include": Across Trials is the only concept included
 * -"exclude": Across Trials concept is the only concept excluded 
 */
function createTree(includeExcludeFlag, ontresponse, treePanel){
	// shorthand
	var Tree = Ext.tree;

    var treeRoot = new Tree.TreeNode(
        {
            text      : 'root',
            draggable : false,
            id        : 'root',
            qtip      : 'root'
        }
    );
    treePanel.setRootNode(treeRoot);

	for (var c = 0; c < ontresponse.length; c ++ )
	{
		var key = ontresponse[c].key;
		var name = ontresponse[c].name;
		var tooltip = ontresponse[c].tooltip;
		var dimcode = ontresponse[c].dimcode;
		
		if(includeExcludeFlag==="include" && name!=="Across Trials") continue;
		if(includeExcludeFlag==="exclude" && name==="Across Trials") continue;
		// set the root node
		var autoExpand=false;
		if(GLOBAL.PathToExpand.indexOf(key)>-1) autoExpand=true;
		var ontRoot = new Tree.AsyncTreeNode(
				{
					text : name,
					draggable : false,
					id : key,
					qtip : tooltip,
					expanded : autoExpand
				}
		);
		
		treeRoot.appendChild(ontRoot);
		/*****************************************/
		var fullname=key.substr(key.indexOf("\\",2), key.length);
		var access=GLOBAL.InitialSecurity[fullname];

		if((access!=undefined && access!='Locked') || GLOBAL.IsAdmin) //if im an admin or there is an access level other than locked leave node unlocked
		{
			//leave node unlocked must have some read access
		}
		else
		{
			//default node to locked
			//child.setText(child.text+" <b>Locked</b>");
			ontRoot.attributes.access='locked';
			ontRoot.disable();
			ontRoot.on('beforeload', function(node){alert("Access to this node has been restricted. Please contact your administrator for access."); return false});
		}
	}
	return treeRoot;
}

/*
 * the id_in drives which off these tabs is created
 * 
 */
function getSubCategories(id_in, title_in, ontresponse)
{
	// shorthand
	var Tree = Ext.tree;

	var treeRoot;
	
	var showFn;
	
    var ontTree = new Tree.TreePanel(
        {
            id : id_in,
            title : title_in,
            animate : false,
            autoScroll : true,
            loader : new Ext.ux.OntologyTreeLoader(
                {
                    dataUrl : 'none'
                }
            ),
            enableDrag : true,
            ddGroup : 'makeQuery',
            containerScroll : true,
            enableDrop : false,
            region : 'center',
            rootVisible : false,
            expanded : true,
            onShow : function() { showFn.apply(this, arguments); },
            listeners :
            {
                activate: function(p){

                    sizeNavigateTreePanel("navigateTermsPanel");

                    $j(window).resize(function() {
                        ontTabPanel.doLayout();
                        sizeNavigateTreePanel("navigateTermsPanel");
                    });

                }
            }
        }
    );
	
	if (id_in==='crossTrialsPanel'){
		showFn = function(node, e){
			Ext.tree.TreePanel.superclass.onShow.call(this);
			//Ext.get('advancedbutton').dom.style.display='none';
		}
		treeRoot = createTree('include', ontresponse, ontTree);
	}else{
		showFn = function(node, e){
			Ext.tree.TreePanel.superclass.onShow.call(this);
			//Ext.get('advancedbutton').dom.style.display='';
		}
		treeRoot = createTree('exclude', ontresponse, ontTree);
	}
	
    var toolbar = new Ext.Toolbar([
		{
			id:'contextHelp-button',
			handler: function(event, toolEl, panel){
			   	D2H_ShowHelp((id_in=="navigateTermsPanel")?"1066":"1091",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
			},
		    iconCls: "contextHelpBtn"  
		}
    ]);
	
	ontTree.on('startdrag', function(panel, node, event)
			{
		Ext.ux.ManagedIFrame.Manager.showShims()

			}
	);

	ontTree.on('enddrag', function(panel, node, event)
			{
		Ext.ux.ManagedIFrame.Manager.hideShims()

			}
	);


	// add a tree sorter in folder mode
	new Tree.TreeSorter(ontTree,
			{
		folderSort : true
			}
	);
	ontTree.setRootNode(treeRoot);
	ontTree.add(toolbar);
	ontTabPanel.add(ontTree);
	/*if(GLOBAL.IsAdmin)
   {
   	ontTabPanel.add(searchByNamePanel);
   	ontTabPanel.setActiveTab('searchByNamePanel');
   }*/

	if(GLOBAL.Debug)
	{
		alert(ontresponse.responseText);
	}

    // ontTabPanel.add(ontSearchTermsPanel);
    ontTabPanel.doLayout();
    ontTree.dragZone.addToGroup("analysis");
    sizeNavigateTreePanel("navigateTermsPanel");
    /*if(GLOBAL.IsAdmin)
     {
     searchByNameTree.dragZone.addToGroup("analysis");
     }*/

}

//Setup the navigate tree for scrolling. We need to wrap the UL element in a DIV and setup scrolling and height
function sizeNavigateTreePanel(panelId)
{

    var parentPanel = $j("#" + panelId);
    var rootEl = $j("#" + panelId + " ul").first();

    var parentHeight = parentPanel.innerHeight();

    var otherHeight = 0;

    if (rootEl.parent().attr("id") != "nbtTreeWrapper") {
        rootEl.wrap('<div id="nbtTreeWrapper" />');
    }

    var wrapper = $j("#nbtTreeWrapper");

    wrapper.siblings().filter(":visible").each( function() {
        otherHeight += $j(this).outerHeight();
    });

    wrapper.css("overflow", "auto").css("height", parentHeight - otherHeight );

}

function ontLoadNode(node)
{
	// shorthand
	var Tree = Ext.tree;
	var child = new Tree.AsyncTreeNode(
			{
				text : 'fake second node',
				draggable : true,
				id : 'key',
				qtip : 'tooltip'
			}
	);
	child.addListener('beforeload', ontLoadNode, this);
	node.appendChild(child);
}

function setupDragAndDrop()
{
	/* Set up the drag and drop for the query panel */
	// var dts = new Array();
	for(var s = 1; s <= GLOBAL.NumOfSubsets; s ++ )
	{
		for(var i = 1; i <= GLOBAL.NumOfQueryCriteriaGroups;
		i ++ )
		{
			var qcd = Ext.get("queryCriteriaDiv" + s.toString() + '_' + i.toString());
			dts = new Ext.dd.DropTarget(qcd,
					{
				ddGroup : 'makeQuery'
					}
			);

            //Enable jQuery dragging into the DIV.
            jQuery("#queryCriteriaDiv" + s.toString() + '_' + i.toString()).addClass("jstree-drop");
			dts.notifyDrop = function(source, e, data)
			{
                if (data.node.attributes.comment)
                    GLOBAL.currentSubsetsStudy=data.node.attributes.comment.substr(6);
                //Analysis will change as a result of this drop, mark this
                GLOBAL.AnalysisHasBeenRun = false;
				if(source.tree.id == "previousQueriesTree")
				{
					getPreviousQueryFromID(data.node.attributes.id);
					return true;
				}
				else
				{
					/* var subset = this.id.substr(16, 1);
               var panelnumber = this.id.substr(18);
               var level = data.node.attributes.level;
               var name = data.node.text;
               var key = data.node.id;
               var tooltip = data.node.attributes.qtip;
               var tablename = data.node.attributes.tablename;
               var dimcode = data.node.attributes.dimcode;
               var comment = data.node.attributes.comment;
               var normalunits = data.node.attributes.normalunits;
               var oktousevalues = data.node.attributes.oktousevalues;
               var concept = createPanelItem(subset, panelnumber, level, name, key, tooltip, tablename, dimcode, comment, normalunits, oktousevalues);
					 */

					var x=e.xy[0];
					var y=e.xy[1];
					//alert(document.elementFromPoint(x,y).id);
					//alert(this.dragElId);
					//if(document.elementFromPoint(x,y).id!=this.dragElId){return;} //hack to fix layers over each other
					var concept = null;
					if(data.node.attributes.oktousevalues != "Y")
					{
						concept = createPanelItemNew(this.el, convertNodeToConcept(data.node));
					}
					else
					{
						concept = createPanelItemNew(Ext.get("hiddenDragDiv"), convertNodeToConcept(data.node));
					}
					// new hack to show setvalue box
					selectConcept(concept);
					if(data.node.attributes.oktousevalues == "Y")
					{
						STATE.Dragging = true;
						STATE.Target = this.el;
						showSetValueDialog();
					}
					/*new code to show next row*/
					var panelnumber = Number(this.id.substr(18));
					showCriteriaGroup(panelnumber+1);
					return true;
				}
			}
		}
	}
	/* Set up Drag and Drop for the export Panel
   var qcd = Ext.get(exportPanel.body);
   dts = new Ext.dd.DropTarget(qcd,
   {
   ddGroup : 'export'
   });

   dts.notifyDrop = function(source, e, data)
   {
   createExportItem(data.node.text, data.node.id);
   return true;
   } */

	/* Set up Drag and Drop for the analysis Panel */
	var qcd = Ext.get(analysisPanel.body);

	dts = new Ext.dd.DropTarget(qcd,
			{
		ddGroup : 'analysis'
			}
	);
    //Add a css class which lets us drop across trial nodes into the results/analysis screen.
    jQuery("#" + analysisPanel.body.id).addClass("jstree-drop");
    jQuery("#" + analysisPanel.body.id).addClass("excludeValuePopup");
    jQuery("#" + analysisPanel.body.id).addClass("results_analysis");


	dts.notifyDrop = function(source, e, data)
	{
		// createAnalysisItem(data.node.text, data.node.id);
		// alert("build analsyis graph now!");
        buildAnalysis(convertNodeToConcept(data.node), 'stats');
		return true;
	}

	/* set up drag and drop for grid */
	var mcd = Ext.get(analysisGridPanel.body);
	dtg = new Ext.dd.DropTarget(mcd,
			{
		ddGroup : 'analysis'
			}
	);
    jQuery("#" + analysisGridPanel.body.id).addClass("jstree-drop");
    jQuery("#" + analysisGridPanel.body.id).addClass("excludeValuePopup");
    jQuery("#" + analysisGridPanel.body.id).addClass("results_grid");

    dtg.notifyDrop = function(source, e, data)
	{
		// createAnalysisItem(data.node.text, data.node.id);
		// alert("build analsyis graph now!");
        buildAnalysis(convertNodeToConcept(data.node),'grid');
		return true;
	}
}

function getPreviousQueryFromIDComplete(subset, result) {
    if (document.getElementById("queryCriteriaDiv" + subset + "_1") == null) {
        setTimeout(function(){
            getPreviousQueryFromIDComplete(subset, result);
        }, 100);
        return ;
    }
    if (result.status != 200) {
        queryPanel.el.unmask();
        return;
    }

    var doc = result.responseXML;

    //resetQuery();  //if i do this now it wipes out the other subset i just loaded need to make it subset specific

    var panels = doc.selectNodes("//panel");

    panel:
    for (var p = 0; p < panels.length; p++) {
        var panelnumber = p + 1;

        showCriteriaGroup(panelnumber); //in case its hidden;
        var panel = document.getElementById("queryCriteriaDiv" + subset + "_" + panelnumber);
        var invert = panels[p].selectSingleNode("invert").firstChild.nodeValue;
        if (invert == "1") {
            excludeGroup(null, subset, panelnumber);
        } //set the invert for the panel

        var items = panels[p].selectNodes("item")
        for (var it = 0; it < items.length; it++) {
            var item = items[it];

            var key = item.selectSingleNode("item_key").firstChild.nodeValue;

            if (key == "\\\\Public Studies\\Public Studies\\SECURITY\\")
                continue panel;

            /*need all this information for reconstruction but not all is available*/
            var valuetype = getValue(item.selectSingleNode("constrain_by_value/value_type"), "");
            var mode;

            if (valuetype == "FLAG") {
                mode = "highlow";
            }
            else if (valuetype == "NUMBER") {
                mode = "numeric";
            }
            else {
                mode == "novalue";
            }

            var valuenode = item.selectSingleNode("contrain_by_value");
            var oktousevalues;
            if (valuenode != null && typeof(valuenode) != undefined) {
                oktousevalues = "Y";
            }

            var operator = getValue(item.selectSingleNode("constrain_by_value/value_operator"), "");
            var numvalue = getValue(item.selectSingleNode("constrain_by_value/value_constraint"), "");
            var lowvalue;
            var highvalue;
            if (operator == "BETWEEN") {
                lowvalue = numvalue.substring(0, numvalue.indexOf("and"));
                highvalue = numvalue.substring(numvalue.indexOf("and") + 3);
            }
            else {
                lowvalue = numvalue;
            }
            var highlowselect = "";
            if (mode == "highlow") {
                highlowselect = numvalue;
            }

            var value = new Value(mode, operator, highlowselect, lowvalue, highvalue, '');
            /* the panel (probably) only needs the concept key and the
             * constraint, hence we not need to fill the rest of the parameters,
             * which is good because we don't have that information...
             */
            var myConcept = new Concept('', key, -1, '', '', '', '', '', oktousevalues, value);
            createPanelItemNew(panel, myConcept);
        }
    }

    queryPanel.el.unmask();
}

function createExportItem(name, setid)
{
	if(GLOBAL.exportFirst == undefined) // clear out the body
	{
		exportPanel.body.update("");
		GLOBAL.exportFirst = false;
	}
	var panel = exportPanel.body.dom;
	var li = document.createElement('div');
	// was li
	// convert all object attributes to element attributes so i can get them later (must be a way to keep them in object ? )
	// li.setAttribute('conceptid', key);
	// li.setAttribute('conceptlevel', level);
	// li.setAttribute('concepttooltip', tooltip);
	// li.setAttribute('concepttablename', tablename);
	li.setAttribute('setid', setid);
	li.setAttribute('setname', name);
	li.className = "conceptUnselected";
	li.style.font = "10pt arial";
	var text = document.createTextNode(name);
	// tooltip
	li.appendChild(text);
	panel.appendChild(li);
	Ext.get(li).addListener('click', conceptClick);
	Ext.get(li).addListener('contextmenu', conceptRightClick);
}



function ontologyRightClick(eventNode, event)
{
	if ( ! this.contextMenuOntology)
	{
		this.contextMenuOntology = new Ext.menu.Menu(
				{
					id : 'contextMenuOntology',
					items : [
					         {
					        	 text : 'Show Definition', handler : function()
					        	 {
					        	 showConceptInfoDialog(eventNode.attributes.id, eventNode.attributes.text, eventNode.attributes.comment);
					        	 }
					         },
					         {
					        	 text : 'Show Node', 
					        	 handler : function()
					        	 {
					        	 	showNode(eventNode.attributes.id);
					        	 }
					         }
					         ]
				}
		);
	}
	var xy = event.getXY();
	this.contextMenuOntology.showAt(xy);
	return false;
}

function previousQueriesRightClick(eventNode, event)
{
	if ( ! this.contextMenuPreviousQueries)
	{
		this.contextMenuPreviousQueries = new Ext.menu.Menu(
				{
					id : 'contextMenuPreviousQueries',
					items : [
					         {
					        	 text : 'Rename', handler : function()
					        	 {
					        	 alert('rename!');
					        	 }
					         }
					         ,
					         {
					        	 text : 'Delete', handler : function()
					        	 {
					        	 alert('delete!');
					        	 }
					         }
					         ,
					         {
					        	 text : 'Query Summary', handler : function()
					        	 {
					        	 showQuerySummaryWindow(eventNode);
					        	 }
					         }
					         ]
				}
		);
	}
	var xy = event.getXY();
	this.contextMenuPreviousQueries.showAt(xy);
	return false;
}

function showNode(key){
	GLOBAL.PathToExpand=key;
	setActiveTab();
	var rootNode = ontTabPanel.getActiveTab().getRootNode();
	//rootNode.collapseChildNodes(true);
	drillDown(rootNode);
}

function drillDown(rootNode){
	for (var i=0; i<rootNode.childNodes.length; i++){
		if(GLOBAL.PathToExpand.indexOf(rootNode.childNodes[i].id)>-1){
			rootNode.childNodes[i].expand();
			rootNode.childNodes[i].ensureVisible();
			drillDown(rootNode.childNodes[i]);
		}
	}
}

function showConceptInfoDialog(conceptKey, conceptid, conceptcomment)
{

	if( ! this.conceptinfowin)
	{
		var link = '<a href="javascript:;"  onclick="return popitup(\'http://www.google.com/search?q='+conceptid+'\')">Search for more information...</a>'
		conceptinfowin = new Ext.Window(
				{
					id : 'showConceptInfoWindow',
					title : 'Show Concept Definition-' + conceptid,
					layout : 'fit',
					width : 600,
					height : 500,
					closable : false,
					plain : true,
					modal : true,
					border : false,
					autoScroll: true,
					buttons : [
					           /* {
            text : 'Search For More Information',
            handler : function()
            {
               popitup('http://www.google.com/search?q=' + conceptid);
            }
         }
         ,*/
					           {
					        	   text : 'Close',
					        	   handler : function()
					        	   {
					        	   conceptinfowin.hide();
					        	   }
					           }
					           ],
					           resizable : false
				}
		);
	}
	//var conceptKeySplits = conceptKey.split('\\');
	//var conceptType = (conceptKeySplits[1]=='')?conceptKeySplits[2]:conceptKeySplits[1];

	conceptinfowin.show(viewport);
	conceptinfowin.header.update("Show Concept Definition-" + conceptid);
	Ext.get(conceptinfowin.body.id).update(conceptcomment);
	//var begin=conceptcomment.indexOf("trial:");
	//if(begin==0)
	//{
		conceptinfowin.load({
			//url: pageInfo.basePath+"/trial/trialDetailByTrialNumber",
			url: pageInfo.basePath+"/ontology/showConceptDefinition",
			//params: {id: conceptcomment.substring(6,conceptcomment.length), conceptType: conceptType}, // or a URL encoded string
			params: {conceptKey:conceptKey}, // or a URL encoded string		
			//callback: yourFunction,
			//scope: yourObject, // optional scope for the callback
			discardUrl: true,
			nocache: true,
			text: "Loading...",
			timeout: 30000,
			scripts: false
		});
	//}

}

function showQuerySummaryWindow(source)
{
	// var query = getCRCQueryRequest();
	if( ! this.querysummarywin)
	{

		querysummarywin = new Ext.Window(
				{
					id : 'showQuerySummaryWindow',
					title : 'Query Summary',
					layout : 'fit',
					width : 600,
					height : 500,
					closable : false,
					plain : true,
					modal : true,
					border : false,
					buttons : [
					           {
					        	   text : 'Done',
					        	   handler : function()
					        	   {
					        	   querysummarywin.hide();
					        	   }
					           }
					           ],
					           resizable : true
				}
		);

		querySummaryPanel = new Ext.Panel(
				{
					id : 'querySummaryPanel',
					region : 'center'
				}
		);
		querysummarywin.add(querySummaryPanel);
	}
	querysummarywin.show(viewport);
	var fakehtml = "<div style='padding:10px;font:12pt arial;width:100%;height:100%;'>\
		< b > Criteria 1 < / b > < br > \
		Trials\\CT0145T03 < br > \
		< b > AND < br > \
		Criteria 2 < / b > < br > \
		Sex\\Female < br > \
		< b > OR < / b > < br > \
		TRIALS\\CT0145T03\\RBM\\Adjusted Values\\IL - 13 - & gt;\
		.75 < br > "



		// querySummaryPanel.setBody("<div style='height:500px;width500px;overflow:auto;'>" + Ext.util.Format.htmlEncode(query) + "</div>");
		// querySummaryPanel.setBody(fakehtml);
		var q1 = getQuerySummary(1);
		var q2 = getQuerySummary(2);
		querySummaryPanel.body.update('<table border="1" height="100%" width="100%"><tr><td width="50%" valign="top"><div style="padding:10px;"><h2>Subset 1 Criteria</h2>' + q1 + '</div></td><td valign="top"><div style="padding:10px;"><h2>Subset 2 Criteria</h2>' + q2 + '</div></td></tr></table>');
}


function showConceptSearchPopUp(conceptid)
{
	popitup('http://www.google.com/search?q=' + conceptid)
}
function popitup(url)
{
	newwindow = window.open(url, 'name', 'height=500,width=500,toolbar=yes,scrollbars=yes, resizable=yes,');
	if (window.focus)
	{
		newwindow.focus()
	}
	return false;
}






function showExportStepSplitTimeSeries()
{

	if( ! this.exportStepSplitTimeSeries)
	{
		exportStepSplitTimeSeries = new Ext.Window(
				{
					id : 'exportStepSplitTimeSeriesWindow',
					title : 'Export-Split Time Series',
					layout : 'fit',
					width : 400,
					height : 200,
					closable : false,
					plain : true,
					modal : true,
					border : false,
					buttons : [
					           {
					        	   id : 'exportStepSplitTimeSeriesNextButton',
					        	   text : 'Next>',
					        	   disabled : true,
					        	   handler : function()
					        	   {
					        	   exportStepSplitTimeSeries.hide();
					        	   showExportStepDataSelection();
					        	   }
					           }
					           ,
					           {
					        	   text : 'Cancel',
					        	   handler : function()
					        	   {
					        	   exportStepSplitTimeSeries.hide();
					        	   }
					           }
					           ],
					           resizable : false ,
					           autoLoad :
					           {
					url : pageInfo.basePath+'/panels/exportStepSplitTimeSeries.html',
					scripts : true,
					nocache : true,
					discardUrl : true,
					method : 'POST'
					           }
				}
		);
	}

	exportStepSplitTimeSeries.show(viewport);
}

function showExportStepDataSelection()
{
	if( ! this.exportStepDataSelection)
	{
		exportStepDataSelection = new Ext.Window(
				{
					id : 'exportStepDataSelectionWindow',
					title : 'Export-Data Selection',
					layout : 'fit',
					width : 400,
					height : 400,
					closable : false,
					plain : true,
					modal : true,
					border : false,
					buttons : [
					           {
					        	   id : 'exportStepDataSelectionAdvancedButton',
					        	   text : 'Advanced',
					        	   handler : function()
					        	   {
					        	   showExportDataSelectionAdvanced();
					        	   }
					           }
					           ,
					           {
					        	   id : 'exportStepDataSelectionNextButton',
					        	   text : 'Get Data',
					        	   disabled : true,
					        	   handler : function()
					        	   {
					        	   getExportData();
					        	   }
					           }
					           ,
					           {
					        	   text : 'Cancel',
					        	   handler : function()
					        	   {
					        	   exportStepDataSelection.hide();
					        	   }
					           }
					           ],
					           resizable : false,
					           autoLoad :
					           {
					url : pageInfo.basePath+'/panels/exportStepDataSelection.html',
					scripts : true,
					nocache : true,
					discardUrl : true,
					method : 'POST'
					           }
				}
		);
	}
	exportStepDataSelection.show(viewport);

}
function getExportData()
{

	exportStepDataSelection.getEl().mask("Getting Data...");
	setTimeout('exportDataFinished()', 2000)

}
function showExportStepProgress()
{
	if( ! this.exportStepProgress)
	{
		exportStepProgress = new Ext.Window(
				{
					id : 'exportStepProgress',
					title : 'Export-Download File',
					layout : 'fit',
					html : '<br><div style="font:12pt arial;width:100%;height:100%;text-align:center;vertical-align:middle"><a href="export/export.xls">Download File</a></div>',
					width : 400,
					height : 200,
					closable : false,
					plain : true,
					modal : true,
					border : false,
					buttons : [
					           {
					        	   text : 'Done',
					        	   handler : function()
					        	   {
					        	   exportStepProgress.hide();
					        	   }
					           }
					           ],
					           resizable : false
				}
		);
	}
	exportStepProgress.show(viewport);

}
function exportDataFinished()
{
	exportStepDataSelection.getEl().unmask();
	exportStepDataSelection.hide();
	showExportStepProgress();
}

function runAllQueries(callback, panel)
{
	// analysisPanel.body.update("<table border='1' width='100%' height='100%'><tr><td width='50%'><div id='analysisPanelSubset1'></div></td><td><div id='analysisPanelSubset2'></div></td></tr>");
	var subset = 1;
	if(isSubsetEmpty(1) && isSubsetEmpty(2))
	{
		if (null != panel) { 
			panel.body.unmask()
		}
		Ext.Msg.alert('Subsets are empty', 'All subsets are empty. Please select subsets.');
		return;
	}

	// setup the number of subsets that need running
	var subsetstorun = 0;
	for (i = 1; i <= GLOBAL.NumOfSubsets; i = i + 1)
	{
		if( ! isSubsetEmpty(i) && GLOBAL.CurrentSubsetIDs[i] == null)
		{
			subsetstorun ++ ;
		}
	}
	STATE.QueryRequestCounter = subsetstorun;
	/* set the number of requests before callback is fired for runquery complete */


    //Accross Trial Changes
    if(ontTabPanel.getActiveTab().title == "Across Trial")
    {
        for (i = 1; i <= GLOBAL.NumOfSubsets; i = i + 1)
        {
            if( ! isSubsetEmpty(i) && GLOBAL.CurrentSubsetIDs[i] == null)
            {
                runQueryAcrossTrial(i, callback);
            }
        }
    }
    else
    {

        //For the navigate by study tab we use i2b2 to get the data.
        // iterate through all subsets calling the ones that need to be run
        for (i = 1; i <= GLOBAL.NumOfSubsets; i = i + 1)
        {
            if( ! isSubsetEmpty(i) && GLOBAL.CurrentSubsetIDs[i] == null)
            {
                runQuery(i, callback);
            }
        }
    }//Accross Trial Changes
}

function runQuery(subset, callback) {
    if (Ext.get('analysisPanelSubset1') == null) {
        // analysisPanel.body.update("<table border='1' width='100%' height='100%'><tr><td width='50%'><div id='analysisPanelSubset1'></div></td><td><div id='analysisPanelSubset2'></div></td></tr>");
    }

    var query = getCRCQueryRequest(subset);
    // first subset
    queryPanel.el.mask('Getting subset ' + subset + '...', 'x-mask-loading');

    Ext.Ajax.request(
        {
            url: pageInfo.basePath + "/queryTool/runQueryFromDefinition",
            method: 'POST',
            xmlData: query,
            success: function (result, request) {
                runQueryComplete(result, subset, callback);
            },
            failure: function (result, request) {
                runQueryComplete(result, subset, callback);
            },
            timeout: '600000'
        }
    );

    if (GLOBAL.Debug) {
        resultsPanel.setBody("<div style='height:400px;width500px;overflow:auto;'>" + Ext.util.Format.htmlEncode(query) + "</div>");
    }
}

function runQueryComplete(result, subset, callback) {
    var jsonRes = JSON.parse(result.responseText);
    var error;

    if (result.status != 200) {
        error = jsonRes.message;
    } else if (jsonRes.errorMessage !== null) {
        error = jsonRes.errorMessage;
    }

    queryPanel.el.unmask();

    if (error) {
        Ext.Msg.show(
            {
                title: 'Error generating patient set',
                msg: error,
                buttons: Ext.Msg.OK,
                fn: function () {
                    Ext.Msg.hide();
                },
                icon: Ext.MessageBox.ERROR
            }
        );
    }

    // Current code requires us to set CurrentSubsetIDs regardless of error status...
    GLOBAL.CurrentSubsetIDs[subset] = jsonRes.id ? jsonRes.id : -1;

    if (subset === null) { // if single subset

        callback(GLOBAL.CurrentSubsetIDs[subset]);

    } else {

        // getPDO_fromInputList is not implemented in core-db
        //if (GLOBAL.Debug) {
        //    alert(getCRCpdoRequest(patientsetid, 1, jsonRes.setSize));
        //}

        /* removed the pdo request call 12 / 17 / 2008 added the callback logic here instead */
        // runQueryPDO(patientsetid, 1, jsonRes.setSize, subset, callback );

        if (STATE.QueryRequestCounter > 0) { // I'm in a chain of requests so decrement
            STATE.QueryRequestCounter = --STATE.QueryRequestCounter;
        }
        if (STATE.QueryRequestCounter == 0) {
            callback();
        }
        /* I'm the last request outstanding in this chain*/
    }
}

function runQueryPDO(patientsetid, minpatient, maxpatient, subset, callback) {
    var query = getCRCpdoRequest(patientsetid, minpatient, maxpatient, subset)
    // resultsPanel.setBody("<div style='height:400px;width500px;overflow:auto;'>" + Ext.util.Format.htmlEncode(query) + "</div>");
    queryPanel.el.mask('Getting patient set ' + subset + '...', 'x-mask-loading');
    Ext.Ajax.request(
        {
            url: pageInfo.basePath + "/proxy?url=" + GLOBAL.CRCUrl + "pdorequest",
            method: 'POST',
            // scope : this,
            xmlData: query,
            success: function (result, request) {
                runQueryPDOComplete(result, subset, callback);
            },
            failure: function (result, request) {
                runQueryPDOComplete(result, subset, callback);
            },
            timeout: '600000'
        }
    );

}

function runQueryPDOComplete(result, subset, callback) {
    if (GLOBAL.Debug) {
        alert(result.responseText)
    }
    ;
    queryPanel.el.unmask();
    var doc = result.responseXML;
    doc.setProperty("SelectionLanguage", "XPath");
    doc.setProperty("SelectionNamespaces", "xmlns:ns2='http://www.i2b2.org/xsd/hive/pdo/1.1/'");
    var patientset = result.responseXML.selectSingleNode("//ns2:patient_set");
    if (patientset == undefined) {
        patientset = result.responseXML.selectSingleNode("//patient_set");
    }
    if (patientset == null) {
        return
    }
    ;
    createStatistics(patientset, subset);
    if (STATE.QueryRequestCounter > 0) // I'm in a chain of requests so decrement
    {
        STATE.QueryRequestCounter = --STATE.QueryRequestCounter;
    }
    if (STATE.QueryRequestCounter == 0) {
        callback();
    }
    /* I'm the last request outstanding in this chain*/
    if (GLOBAL.Debug) {
        resultsPanel.setBody(resultsPanel.getBody() + "<div style='height:200px;width500px;overflow:auto;'>" + Ext.util.Format.htmlEncode(result.responseText) + "</div>");
    }
}

// takes a patientset node
function createStatistics(patientset, subset)
{
	var totalpatients = 0;
	var totalmale = 0;
	var totalfemale = 0;
	var total0to9 = 0;
	var total10to17 = 0;
	var total18to34 = 0;
	var total35to44 = 0;
	var total45to54 = 0;
	var total55to64 = 0;
	var total65to74 = 0;
	var total75to84 = 0;
	var totalgreaterthan84 = 0;
	var totalunrecorded = 0;
	var patients = patientset.selectNodes('patient');
	for(var p = 0; p < patients.length; p ++ ) // iterate every patient
	{
		var patient = patients[p];
		var params = patient.selectNodes('param');
		for(var n = 0; n < params.length; n ++ )
		{
			var param = params[n];
			var paramname = param.getAttribute("name");
			var paramvalue;
			if(param.firstChild)
			{
				paramvalue = param.firstChild.nodeValue;
			}
			else
			{
				paramvalue = null;
			}

			// do something with this param
			// if its a sex add it to the sex variables
			if(paramname == "sex_cd")
			{
				if(paramvalue == "M")
					totalmale ++ ;
				if(paramvalue == "F")
					totalfemale ++ ;
			}
			// do something with it if its an age
			if(paramname == "age_in_years_num")
			{
				if(paramvalue >= 0 && paramvalue <= 9)
				{
					total0to9 ++ ;
				}
				if(paramvalue >= 10 && paramvalue <= 17)
				{
					total10to17 ++ ;
				}
				if(paramvalue >= 18 && paramvalue <= 34)
				{
					total18to34 ++ ;
				}
				if(paramvalue >= 35 && paramvalue <= 44)
				{
					total35to44 ++ ;
				}
				if(paramvalue >= 45 && paramvalue <= 54)
				{
					total45to54 ++ ;
				}
				if(paramvalue >= 55 && paramvalue <= 64)
				{
					total55to64 ++ ;
				}
				if(paramvalue >= 65 && paramvalue <= 74)
				{
					total65to74 ++ ;
				}
				if(paramvalue >= 75 && paramvalue <= 84)
				{
					total75to84 ++ ;
				}
				if(paramvalue > 84)
				{
					totalgreaterthan84 ++ ;
				}
			}
		}
		// close param loop
	}
	// close patient loop

	// make sex table
	var statisticshtml = "<table><tr><td><table border='1' class='demoTable' style='border:1px solid black;margin:5px;'>\
		< tr align = 'center' > < td colspan = '2' > < b > Sex distribution < / b > < / td > < / tr > \
		< tr align = 'center' > < th > Males < / th > < th > Females < / th > < / tr > \
		< tr align = 'center' > < td > "+totalmale+" < / td > < td > "+totalfemale+" < / td > < / tr > < / table > < / td > ";
		// make age table
		statisticshtml = statisticshtml + "<td><table border='1' class='demoTable' style='border:1px solid black;margin:5px'><tr align='center'><td colspan='9'><b>Age distribution</b></td></tr>\
		< tr align = 'center' > < th > 0 - 9 < / th > < th > 10 - 17 < / th > < th > 18 - 34 < / th > < th > 35 - 44 < / th > < th > 45 - 54 < / th > < th > 55 - 64 < / th > < th > 65 - 74 < / th > < th > 75 - 84 < / th > < th > & gt;\
		84 < / th > < / tr > \
		< tr align = 'center' > < td > "+total0to9+" < / td > < td > "+total10to17+" < / td > < td > "+total18to34+" < / td > < td > "+total35to44+" < / td > < td > "+total45to54+" < / td > < td > "+total55to64+" < / td > < td > "+total65to74+" < / td > < td > "+total75to84+" < / td > < td > "+totalgreaterthan84+" < / td > < / tr > \
		< / table > < / td > < / tr > < / table > < br / > ";
		// analysisPanel.body.insertHtml("beforeEnd", statisticshtml);
		Ext.get("analysisPanelSubset" + subset).insertHtml("beforeEnd", statisticshtml);
		// analysisPanel.body.update(statisticshtml);
}
function getNodeForAnalysis(node)
{
	// if im a value leaf return me
	if(node.attributes.oktousevalues == "Y" && node.attributes.leaf == true)
	{
		return node;
	}
	// if im a concept leaf then recurse with my parent node
	else if(node.attributes.oktousevalues != "Y" && node.attributes.leaf == true)
	{
		return getNodeForAnalysis(node.parentNode);
	}
	else
	{
		return node
	}
	;
	// must be a concept folder so return me
}


function buildAnalysis(conceptin, dropType)
{

    if(dropType == 'stats')
    {
        analysisPanel.body.mask("Running analysis...", 'x-mask-loading');
    }

    if(dropType == 'grid')
    {
        analysisGridPanel.body.mask("Loading...", 'x-mask-loading');
    }

    var concept = conceptin;


    if(isSubsetEmpty(1) && isSubsetEmpty(2))
    {
        analysisPanel.body.unmask();
        analysisGridPanel.body.unmask();

        alert('Empty subsets found, need a valid subset to analyze!');
        return;
    }


    if((GLOBAL.CurrentSubsetIDs[1] == null && ! isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] == null && ! isSubsetEmpty(2)))
    {
        runAllQueries(function()
            {
                buildAnalysis(concept, dropType);
            }
        );
        analysisPanel.body.unmask();
        return;
    }

    if(dropType == 'grid')
    {
        window.dragToGrid = true;
        getAnalysisGridData(concept.key);
    }

    if(dropType == 'stats')
    {
        Ext.Ajax.request(
            {
                url : pageInfo.basePath+"/chart/analysis",
                method : 'POST',
                timeout: '600000',
                params :  Ext.urlEncode(
                    {
                        charttype : "analysis",
                        concept_key : concept.key,
                        //modifier_key: concept.modifierkey,
                        //concept: Ext.util.JSON.encode(concept),
                        result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
                        result_instance_id2 : GLOBAL.CurrentSubsetIDs[2]
                    }
                ), // or a URL encoded string
                success : function(result, request)
                {

                    //Add the code we just dragged in to the report list.
                    GLOBAL.currentReportCodes.push(concept.key)
                    GLOBAL.currentReportStudy.push(concept.comment)

                    //node.comment
                    buildAnalysisComplete(result);

                }
                ,
                failure : function(result, request)
                {
                    buildAnalysisComplete(result);
                }
            }
        );
    }

	resultsTabPanel.body.mask("Running analysis...", 'x-mask-loading');
	// analysisPanel.setTitle("Results/Analysis - Analysis of concept " + getShortNameFromKey(node.attributes.id));
	getAnalysisGridData(node.attributes.id);
}

function buildAnalysisComplete(result)
{
	// analysisPanel.body.unmask();
	var txt = result.responseText;
	updateAnalysisPanel(txt, true);
}

function updateAnalysisPanel(html, insert)
{
    var saveButtonHtml="<div id='reportSaveButton'><button style='float:right' type='button' onclick='saveCodeReport()'>Save Report</button></div>";
    var printButtonHtml="<div id='printButton'><button style='float:right' type='button' onclick='print()'>Print or Save Results</button></div><br>";
	/* if(insert)
   {
   var frame = analysisPanel.getFrame();
   var body = analysisPanel.getFrameBody();
   var extBody = Ext.get(body);
   extBody.insertHtml('afterBegin', txt, false);
   extBody.scrollTo('top', 0, false);
   lastAnalysisResult = txt + lastAnalysisResult;
   }
   else
   {
   analysisPanel.getFrame().update(html, false, null);
   } */
	if(insert)
	{
		var body = analysisPanel.body;
		body.insertHtml('afterBegin', html, false);
        jQuery("#reportSaveButton").remove();
        jQuery("#printButton").remove();

        body.insertHtml('afterBegin', printButtonHtml, false);
        body.insertHtml('afterBegin', saveButtonHtml, false);
		body.scrollTo('top', 0, false);
	}
	else
	{
		analysisPanel.body.update(html, false, null);
        analysisPanel.body.insertHtml('afterBegin', printButtonHtml, false);
	}
    analysisPanel.body.unmask();
}

function print(){
    var text = getAnalysisPanelContent();

    //Using string manipulation to hide the redundant buttons when printing reports screen.
    //There should be a better way to do this by cloning the dom and manipulating it using jQuery.
    if(text.indexOf("id=printButton")>-1){
        text = text.replace("id=reportSaveButton","id=\"reportSaveButton\" style=\"display:none\"");
        text = text.replace("id=printButton","id=\"printButton\" style=\"display:none\"");
    }else{
        text = text.replace("id=\"reportSaveButton\"","id=\"reportSaveButton\" style=\"display:none\"");
        text = text.replace("id=\"printButton\"","id=\"printButton\" style=\"display:none\"");
    }


    printPreview(text);
}

function searchByNameComplete(response)
{
	// shorthand
	var length;
	var Tree = Ext.tree;
	searchByNameTree.el.unmask();
	var allkeys="";
	var concepts = response.responseXML.selectNodes('//concept');
	if(concepts != undefined)
	{
		if(concepts.length < GLOBAL.MaxSearchResults)
		{
			length = concepts.length;
		}
		else
		{
			length = GLOBAL.MaxSearchResults;
		}
		for(var c = 0; c < length; c ++ )
		{
			searchByNameTreeRoot.appendChild(getTreeNodeFromXMLNode(concepts[c]));
			var key=concepts[c].selectSingleNode('key').firstChild.nodeValue;
			if(allkeys!="")
			{
				allkeys=allkeys+",";
			}
			allkeys=allkeys+key;
		}
	}
	Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/ontology/sectest",
				method : 'POST',
				success : function(result, request)
				{
				//alert(result);
				}
			,
			failure : function(result, request)
			{
				//alert(result);
			}
			,
			timeout : '300000',
			params : Ext.urlEncode(
					{
						keys: allkeys
					}
			) // or a URL encoded string
			}
	);
}


function enterWasPressed(e)
{
	var pK;
	if(e.which)
	{
		pK = e.which;
	}
	if(pK == undefined && window.event)
	{
		pK = window.event.keyCode;
	}
	if(pK == undefined && e.getCharCode)
	{
		pK = e.getCharCode();
	}
	if ( pK == 13)
	{
		return true;
	}
	else return false;
}

function contextMenuPressed(e)
{
	var x = e;
	e.stopEvent();
	return false;
}

function getSelected(opt)
{
	var selected = new Array();
	var index = 0;
	for (var intLoop = 0; intLoop < opt.length;
	intLoop ++ )
	{
		if ((opt[intLoop].selected) ||
				(opt[intLoop].checked))
		{
			index = selected.length;
			selected[index] = new Object;
			selected[index].value = opt[intLoop].value;
			selected[index].index = intLoop;
		}
	}
	return selected;
}

function outputSelected(opt)
{
	var sel = getSelected(opt);
	var strSel = "";
	for (var intLoop = 0; intLoop < sel.length;
	intLoop ++ )
	{
		strSel += sel[intLoop].value + "\n";
	}
	alert("Selected Items:\n" + strSel);
}

/** 
 * Function to run the survival analysis asynchronously
 */
function showSurvivalAnalysis() {	
	if((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] == null) || (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] == null))
	{
		runAllQueries(showSurvivalAnalysis);
		return;
	}
	
	Ext.Ajax.request({						
		url: pageInfo.basePath+"/asyncJob/createnewjob",
		method: 'POST',
		success: function(result, request){
			RunSurvivalAnalysis(result, GLOBAL.CurrentSubsetIDs[1], GLOBAL.CurrentSubsetIDs[2],
					getQuerySummary(1), getQuerySummary(2));
		},
		failure: function(result, request){
			Ext.Msg.alert('Status', 'Unable to create the heatmap job.');
		},
		timeout: '1800000',
		params: {jobType:  "Survival"}
	});
}

function genePatternReplacement() {
	Ext.Msg.alert('Work In Progress', 'Gene Pattern replacement')
}

//Once, we get a job created by GPController, we run the survival analysis
function RunSurvivalAnalysis(result, result_instance_id1, result_instance_id2,
		querySummary1, querySummary2)	{
	var jobNameInfo = Ext.util.JSON.decode(result.responseText);					 
	var jobName = jobNameInfo.jobName;

	genePatternReplacement();
	showJobStatusWindow(result);	
	document.getElementById("gplogin").src = pageInfo.basePath + '/analysis/gplogin';   // log into GenePattern
	Ext.Ajax.request(
		{						
			url: pageInfo.basePath+"/genePattern/runsurvivalanalysis",
			method: 'POST',
			timeout: '1800000',
			params: {result_instance_id1: result_instance_id1,
				result_instance_id2:  result_instance_id2,
				querySummary1: querySummary1,
				querySummary2: querySummary2,
				jobName: jobName
			}
	});
	checkJobStatus(jobName);
}

function showSNPViewerSelection() {
	
	if((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] == null) ||
			   (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] == null))
	{
		runAllQueries(showSNPViewerSelection);
		return;
	}

	//genePatternReplacement();
	var win = new Ext.Window({
		id: 'showSNPViewerSelection',
		title: 'SNPViewer',
		layout:'fit',
		width:600,
		height:400,
		closable: false,
		plain: true,
		modal: true,
		border:false,
		buttons: [
		          {
		        	  id: 'showSNPViewerSelectionOKButton',
		        	  text: 'OK',
		        	  handler: function(){
		        	  if(Ext.get('snpViewChroms')==null)
		        	  {
		        		  win.close();
		        		  return;
		        	  }
		        	  var ob=Ext.get('snpViewChroms').dom;
		        	  var selected = new Array();
		        	  for (var i = 0; i < ob.options.length; i++)
		        		  if (ob.options[i].selected)
		        			  selected.push(ob.options[i].value);
		        	  GLOBAL.CurrentChroms=selected.join(',');
		        	  getSNPViewer();
		        	  win.close();}
		          }
		          ,{
		        	  text: 'Cancel',
		        	  handler: function(){
		        	  win.close();}
		          }],
		resizable: false,
		autoLoad: {
			url: pageInfo.basePath+'/analysis/showSNPViewerSelection',
			scripts: true,
			nocache:true,
			discardUrl:true,
			method:'POST',
			params: {result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
				result_instance_id2: GLOBAL.CurrentSubsetIDs[2]}
		},
		tools:[{
			id:'help',
			qtip:'Click for context sensitive help',
		    handler: function(event, toolEl, panel){
		   	D2H_ShowHelp("1360",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
		    }
		}]
	});
	//  }
	win.show(viewport);
}

function getSNPViewer() {

	// before Ajax call, log into genepattern:
	genePatternLogin();
	var selectedGenesElt = Ext.get("selectedGenesSNPViewer");
	var selectedGenesEltValue = selectedGenesElt.dom.value;
	var selectedGeneStr = "";
	if (selectedGenesEltValue && selectedGenesEltValue.length != 0) {
		selectedGeneStr = selectedGenesEltValue;
	}
	
	var geneAndIdListElt = Ext.get("selectedGenesAndIdSNPViewer");
	var geneAndIdListEltValue = geneAndIdListElt.dom.value;
	var geneAndIdListStr = "";
	if (geneAndIdListElt && geneAndIdListEltValue.length != 0) {
		geneAndIdListStr = geneAndIdListEltValue;
	}
	
	var selectedSNPsElt = Ext.get("selectedSNPs");
	var selectedSNPsEltValue = selectedSNPsElt.dom.value;
	var selectedSNPsStr = "";
	if (selectedSNPsElt && selectedSNPsEltValue.length != 0) {
		selectedSNPsStr = selectedSNPsEltValue;
	}
	//genePatternReplacement();
	Ext.Ajax.request(
	{
		url: pageInfo.basePath+"/analysis/showSNPViewer",
		method: 'POST',
		success: function(result, request){
			//getSNPViewerComplete(result);
		},
		failure: function(result, request){
			//getSNPViewerComplete(result);
		},
		timeout: '1800000',
		params: { result_instance_id1:  GLOBAL.CurrentSubsetIDs[1],
			result_instance_id2:  GLOBAL.CurrentSubsetIDs[2],
			chroms: GLOBAL.CurrentChroms,
			genes: selectedGeneStr,
			geneAndIdList: geneAndIdListStr,
			snps: selectedSNPsStr}
	});
	
	showWorkflowStatusWindow();
}

function showIgvSelection() {
	
	if((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] == null) ||
			   (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] == null))
	{
		runAllQueries(showIgvSelection);
		return;
	}

	//genePatternReplacement();
	var win = new Ext.Window({
	id: 'showIgvSelection',
		title: 'IGV',
		layout:'fit',
		width:600,
		height:400,
		closable: false,
		plain: true,
		modal: true,
		border:false,
		buttons: [
		          {
		        	  id: 'showIgvSelectionOKButton',
		        	  text: 'OK',
		        	  handler: function(){
		        	  if(Ext.get('igvChroms')==null)
		        	  {
		        		  win.close();
		        		  return;
		        	  }
		        	  var ob=Ext.get('igvChroms').dom;
		        	  var selected = new Array();
		        	  for (var i = 0; i < ob.options.length; i++)
		        		  if (ob.options[i].selected)
		        			  selected.push(ob.options[i].value);
		        	  GLOBAL.CurrentChroms=selected.join(',');
		        	  getIgv();
		        	  win.close();}
		          }
		          ,{
		        	  text: 'Cancel',
		        	  handler: function(){
		        	  win.close();}
		          }],
		resizable: false,
		autoLoad: {
			url: pageInfo.basePath+'/analysis/showIgvSelection',
			scripts: true,
			nocache:true,
			discardUrl:true,
			method:'POST',
			params: {result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
				result_instance_id2: GLOBAL.CurrentSubsetIDs[2]}
		},
		tools:[{
			id:'help',
			qtip:'Click for context sensitive help',
		    handler: function(event, toolEl, panel){
		   	D2H_ShowHelp("1427",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
		    }
		}]
	});
	//  }
	win.show(viewport);
}

function getIgv() {

	// before Ajax call, log into genepattern:
	genePatternLogin();
	var selectedGenesElt = Ext.get("selectedGenesIgv");
	var selectedGenesEltValue = selectedGenesElt.dom.value;
	var selectedGeneStr = "";
	if (selectedGenesEltValue && selectedGenesEltValue.length != 0) {
		selectedGeneStr = selectedGenesEltValue;
	}
	
	var geneAndIdListElt = Ext.get("selectedGenesAndIdIgv");
	var geneAndIdListEltValue = geneAndIdListElt.dom.value;
	var geneAndIdListStr = "";
	if (geneAndIdListElt && geneAndIdListEltValue.length != 0) {
		geneAndIdListStr = geneAndIdListEltValue;
	}
	
	var selectedSNPsElt = Ext.get("selectedSNPsIgv");
	var selectedSNPsEltValue = selectedSNPsElt.dom.value;
	var selectedSNPsStr = "";
	if (selectedSNPsElt && selectedSNPsEltValue.length != 0) {
		selectedSNPsStr = selectedSNPsEltValue;
	}
	
	//genePatternReplacement();
	Ext.Ajax.request(
	{
		url: pageInfo.basePath+"/analysis/showIgv",
		method: 'POST',
		success: function(result, request){
			//getSNPViewerComplete(result);
		},
		failure: function(result, request){
			//getSNPViewerComplete(result);
		},
		timeout: '1800000',
		params: { result_instance_id1:  GLOBAL.CurrentSubsetIDs[1],
			result_instance_id2:  GLOBAL.CurrentSubsetIDs[2],
			chroms: GLOBAL.CurrentChroms,
			genes: selectedGeneStr,
			geneAndIdList: geneAndIdListStr,
			snps: selectedSNPsStr}
	});
	
	showWorkflowStatusWindow();
}


function showPlinkSelection() {
	
	if((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] == null) ||
			   (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] == null))
	{
		runAllQueries(showIgvSelection);
		return;
	}

	//genePatternReplacement();
	var win = new Ext.Window({
		id: 'showPlinkSelection',
		title: 'PLINK',
		layout:'fit',
		width:450,
		height:400,
		closable: false,
		plain: true,
		modal: true,
		border:false,
		buttons: [
		          {
		        	  id: 'showPlinkSelectionOKButton',
		        	  text: 'OK',
		        	  handler: function(){
		        	  if(Ext.get('plinkChroms')==null)
		        	  {
		        		  win.close();
		        		  return;
		        	  }
		        	  var ob=Ext.get('plinkChroms').dom;
		        	  var selected = new Array();
		        	  for (var i = 0; i < ob.options.length; i++)
		        		  if (ob.options[i].selected)
		        			  selected.push(ob.options[i].value);
		        	  GLOBAL.CurrentChroms=selected.join(',');
		        	  getPlink();
		        	  win.close();}
		          }
		          ,{
		        	  text: 'Cancel',
		        	  handler: function(){
		        	  win.close();}
		          }],
		resizable: false,
		autoLoad: {
			url: pageInfo.basePath+'/analysis/showPlinkSelection',
			scripts: true,
			nocache:true,
			discardUrl:true,
			method:'POST',
			params: {result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
				result_instance_id2: GLOBAL.CurrentSubsetIDs[2]}
		},
		tools:[{
			id:'help',
			qtip:'Click for context sensitive help',
		    handler: function(event, toolEl, panel){
		    // 1360 needs to be changed for PLINK
		   	D2H_ShowHelp("1360",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
		    }
		}]
	});
	//  }
	win.show(viewport);
}


function getPlink() {

	// before Ajax call, log into genepattern:
	//genePatternLogin();
	
	/*
	var selectedGenesElt = Ext.get("selectedGenesPlink");
	var selectedGenesEltValue = selectedGenesElt.dom.value;
	var selectedGeneStr = "";
	if (selectedGenesEltValue && selectedGenesEltValue.length != 0) {
		selectedGeneStr = selectedGenesEltValue;
	}
	
	var geneAndIdListElt = Ext.get("selectedGenesAndIdPlink");
	var geneAndIdListEltValue = geneAndIdListElt.dom.value;
	var geneAndIdListStr = "";
	if (geneAndIdListElt && geneAndIdListEltValue.length != 0) {
		geneAndIdListStr = geneAndIdListEltValue;
	}
	*/
	
	/*
	var selectedSNPsElt = Ext.get("selectedSNPsPlink");
	var selectedSNPsEltValue = selectedSNPsElt.dom.value;
	var selectedSNPsStr = "";
	if (selectedSNPsElt && selectedSNPsEltValue.length != 0) {
		selectedSNPsStr = selectedSNPsEltValue;
	}
	*/
	
	//genePatternReplacement();
	/*Ext.Ajax.request(
	{
		url: pageInfo.basePath+"/analysis/showPlink",
		method: 'POST',
		success: function(result, request){
			//getSNPViewerComplete(result);
		},
		failure: function(result, request){
			//getSNPViewerComplete(result);
		},
		timeout: '1800000',
		params: { result_instance_id1:  GLOBAL.CurrentSubsetIDs[1],
			result_instance_id2:  GLOBAL.CurrentSubsetIDs[2],
			chroms: GLOBAL.CurrentChroms //,
			//genes: selectedGeneStr,
			//geneAndIdList: geneAndIdListStr//,
			//snps: selectedSNPsStr
			}
	});
	
	showWorkflowStatusWindow();*/
}

function showGwasSelection() {
	
	if((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] == null) ||
			   (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] == null))
	{
		runAllQueries(showGwasSelection);
		return;
	}

	//genePatternReplacement();
	var win = new Ext.Window({
		id: 'showGwasSelection',
		title: 'Genome-Wide Association Study',
		layout:'fit',
		width:600,
		height:400,
		closable: false,
		plain: true,
		modal: true,
		border:false,
		buttons: [
		          {
		        	  id: 'showGwasSelectionOKButton',
		        	  text: 'OK',
		        	  handler: function(){
		        	  if(Ext.get('gwasChroms')==null)
		        	  {
		        		  win.close();
		        		  return;
		        	  }
		        	  var ob=Ext.get('gwasChroms').dom;
		        	  var selected = new Array();
		        	  for (var i = 0; i < ob.options.length; i++)
		        		  if (ob.options[i].selected)
		        			  selected.push(ob.options[i].value);
		        	  GLOBAL.CurrentChroms=selected.join(',');
		        	  showGwas();
		        	  win.close();}
		          }
		          ,{
		        	  text: 'Cancel',
		        	  handler: function(){
		        	  win.close();}
		          }],
		resizable: false,
		autoLoad: {
			url: pageInfo.basePath+'/genePattern/showGwasSelection',
			scripts: true,
			nocache:true,
			discardUrl:true,
			method:'POST',
			params: {result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
				result_instance_id2: GLOBAL.CurrentSubsetIDs[2]}
		},
		tools:[{
			id:'help',
			qtip:'Click for context sensitive help',
		    handler: function(event, toolEl, panel){
		    // 1360 needs to be changed for PLINK
		   	D2H_ShowHelp("1360",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
		    }
		}]
	});
	//  }
	win.show(viewport);
}

/** 
 * Function to run the GWAS asynchronously
 */
function showGwas() {	
	if((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] == null) || (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] == null))
	{
		runAllQueries(showGwas);
		return;
	}
	
	genePatternReplacement();
	/*Ext.Ajax.request({						
		url: pageInfo.basePath+"/asyncJob/createnewjob",
		method: 'POST',
		success: function(result, request){
			runGwas(result, GLOBAL.CurrentSubsetIDs[1], GLOBAL.CurrentSubsetIDs[2],
					getQuerySummary(1), getQuerySummary(2));
		},
		failure: function(result, request){
			Ext.Msg.alert('Status', 'Unable to create the heatmap job.');
		},
		timeout: '1800000',
		params: {jobType:  "GWAS"}
	});*/
}

// After we get a job created by GPController, we run GWAS
function runGwas(result, result_instance_id1, result_instance_id2,
		querySummary1, querySummary2)	{
	var jobNameInfo = Ext.util.JSON.decode(result.responseText);					 
	var jobName = jobNameInfo.jobName;

	genePatternReplacement();
	/*showJobStatusWindow(result);	

	Ext.Ajax.request(
	{						
		url: pageInfo.basePath+"/genePattern/runGwas",
		method: 'POST',
		timeout: '1800000',
		params: {result_instance_id1: result_instance_id1,
			result_instance_id2:  result_instance_id2,
			querySummary1: querySummary1,
			querySummary2: querySummary2,
			chroms: GLOBAL.CurrentChroms,
			jobName: jobName
		}
	});
	checkJobStatus(jobName);*/
}

function validateheatmapComplete(result)
{
	var mobj=result.responseText.evalJSON();
	GLOBAL.DefaultCohortInfo=mobj;

	//genePatternReplacement();
	showCompareStepPathwaySelection();

}

function compareSubsetsComplete(result, setname1, setname2)
{
	viewport.el.unmask();
	if( ! this.heatmapDisplay)
	{
		heatmapDisplay = new Ext.Window(
				{
					id : 'heatmapDisplayWindow',
					title : 'Heatmap Comparison',
					layout : 'fit',
					width : 800,
					height : 600,
					closable : false,
					plain : true,
					modal : true,
					border : false,
					autoScroll : true,
					buttons : [
					           {
					        	   id : 'Done',
					        	   text : 'OK',
					        	   handler : function()
					        	   {
					        	   heatmapDisplay.hide();
					        	   }
					           }
					           ],
					           resizable : true,
					           html : '<div style="width:100%;height:100%;overflow:auto;"><div id="heatmapContainer"></div><br><div id="heatmapLegend"></div><div>'
				}
		);
	}
	heatmapDisplay.show(viewport);

	var data = jsonToDataTable(result.responseText);

	/* new google.visualization.DataTable();
   data.addColumn('string', 'Gene Name');
   data.addColumn('number', 'chip_XXX_XXX_600');
   data.addColumn('number', 'chip2');
   data.addColumn('number', 'chip3');
   data.addColumn('number', 'chip4');
   data.addColumn('number', 'chip5');
   data.addColumn('number', 'chip6');
   data.addRows(4);
   data.setCell(0, 0, 'ATF3');
   data.setCell(0, 1, 0);
   data.setCell(0, 2, 0.5);
   data.setCell(0, 3, 1);
   data.setCell(0, 4, 1.5);
   data.setCell(0, 5, 2);
   data.setCell(0, 6, 2.5);
   data.setCell(1, 0, 'INS');
   data.setCell(1, 1, 3);
   data.setCell(1, 2, 3.5);
   data.setCell(1, 3, 4);
   data.setCell(1, 4, 4.5);
   data.setCell(1, 5, 5);
   data.setCell(1, 6, 5.5);
   data.setCell(2, 0, 'TAP1');
   data.setCell(2, 1, 0);
   data.setCell(2, 2, null);
   data.setCell(2, 3, - 1);
   data.setCell(2, 4, - 1.5);
   data.setCell(2, 5, - 2);
   data.setCell(2, 6, - 2.5);
   data.setCell(3, 0, 'IL6');
   data.setCell(3, 1, - 3);
   data.setCell(3, 2, - 3.5);
   data.setCell(3, 3, - 4);
   data.setCell(3, 4, - 4.5);
   data.setCell(3, 5, - 5);
   data.setCell(3, 6, - 5.5); */
	var container = heatmapDisplay.body.dom;
	heatmap = new org.systemsbiology.visualization.BioHeatMap(document.getElementById('heatmapContainer'));
	// heatmap = new org.systemsbiology.visualization.BioHeatMap(container);
	heatmap.draw(data,
			{
		cellHeight : 5, cellWidth : 5, fontHeight : 3
			}
	);
	var html = "s1=" + setname1 + "<br>s2=" + setname2;
	Ext.get("heatmapLegend").update(html);
}
function showNameQueryDialog()
{
	if( ! this.namequerywin)
	{
		namequerywin = new Ext.Window(
				{
					id : 'namequeryWindow',
					title : 'Name the Query',
					layout : 'fit',
					width : 500,
					height : 150,
					closable : false,
					plain : true,
					modal : true,
					border : false,
					buttons : [
					           {
					        	   text : 'OK',
					        	   handler : function()
					        	   {
					        	   var newvalue = Ext.get("nameQueryDialogInput").getValue();
					        	   // Ext.get("txtBoxQueryName").dom.value = newvalue;
					        	   GLOBAL.CurrentQueryName = newvalue;
					        	   runQuery2();
					        	   namequerywin.hide();
					        	   }
					           }
					           ,
					           {
					        	   text : 'Cancel',
					        	   handler : function()
					        	   {
					        	   namequerywin.hide();
					        	   }
					           }
					           ],
					           resizable : false,
					           html : '<br>Query Name:&nbsp<input id="nameQueryDialogInput" type="text" size="50">'
				}
		);
	}
	namequerywin.show(viewport);
	Ext.get("nameQueryDialogInput").dom.value = "";
	// clear out for next run
}

function jsonToDataTable(jsontext)
{

	var table = eval("(" + jsontext + ")").table;
	var data = new google.visualization.DataTable();

	// convert to Google.DataTable
	// column
	for (var col = 0; col < table.cols.length;
	col ++ )
	{
		data.addColumn('string', table.cols[col].label);
	}
	// row
	for (var row = 0; row < table.rows.length;
	row ++ )
	{
		data.addRow();
		for (var col = 0; col < table.cols.length;
		col ++ )
		{
			data.setCell(row, col, table.rows[row][col].v);
		}
	}

	// var vis_table = new google.visualization.Table(document.getElementById('table_div'));
	// vis_table.draw(data, {showRowNumber : false});
	return data;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////
// START: Advanced Heatmap Workflow methods
// Called from Run Workflow button in the Heatmap Validation window 
//////////////////////////////////////////////////////////////////////////////////////////////////////
// Once, we get a job created by GPController, we run the heatmap
function RunHeatMap(result, setid1, setid2, pathway, datatype, analysis,
					resulttype, nclusters, timepoints1, timepoints2, sample1,
					sample2, rbmPanels1, rbmPanels2)	{
	var jobNameInfo = Ext.util.JSON.decode(result.responseText);					 
	var jobName = jobNameInfo.jobName;

	//genePatternReplacement();
	showJobStatusWindow(result);	
	genePatternLogin();
	Ext.Ajax.request(
		{						
			url: pageInfo.basePath+"/genePattern/runheatmap",
			method: 'POST',
			timeout: '1800000',
			params: {result_instance_id1:  setid1,
				result_instance_id2:  setid2,
				pathway_name:  pathway,
				datatype:  datatype,
				analysis:  analysis,
				resulttype: resulttype,
				nclusters: nclusters,
				timepoints1: timepoints1,
				timepoints2: timepoints2,
				sample1: sample1,
				sample2: sample2,
				rbmPanels1: rbmPanels1,
				rbmPanels2: rbmPanels2,
				jobName: jobName
			}
	});
	checkJobStatus(jobName);
}
//////////////////////////////////////////////////////////////////////////////////////////////////////
//END: Advanced Heatmap Workflow methods
//////////////////////////////////////////////////////////////////////////////////////////////////////

// This is the new popup window for Survival Analysis. 
function showSurvivalAnalysisWindow(results)	{
	var resultWin = window.open('', 'Survival_Analysis_View_' + (new Date()).getTime(), 
		'width=600,height=800,scrollbars=yes,resizable=yes,location=no,toolbar=no,status=no,menubar=no,directories=no');
	resultWin.document.write(results);
}

//This is the new popup window for GWAS. 
function showGwasWindow(results)	{
	var resultWin = window.open('', 'Gwas_View_' + (new Date()).getTime());
	resultWin.document.write(results);
}

// This is the new popup window for the Haploview
function showHaploViewWindow(results)	{
	var win = new Ext.Window({
		id: 'showHaploView',
		title: 'Haploview',
		layout:'fit',
		width:800,
		height:550,
		closable: true,
		plain: false,
		modal: false,
		border:true,
		maximizable:true,								
		resizable: true,
		html: results
	});
	win.show(viewport);						
}

function clearExportPanel()
{
	// clear the div
	exportPanel.body.update("");
}

/**
 * @return {String} return the value of the radio button that is checked
 * return an empty string if none are checked, or
 * there are no radio buttons
 * @param {} radioObj
 */
function getCheckedValue(radioObj)
{
	if( ! radioObj)
		return "";
	var radioLength = radioObj.length;
	if(radioLength == undefined)
		if(radioObj.checked)
			return radioObj.value;
		else
			return "";
	for(var i = 0; i < radioLength; i ++ )
	{
		if(radioObj[i].checked)
		{
			return radioObj[i].value;
		}
	}
	return "";
}

// set the radio button with the given value as being checked
//do nothing if there are no radio buttons
//if the given value does not exist, all the radio buttons
//are reset to unchecked
function setCheckedValue(radioObj, newValue)
{
	if( ! radioObj)
		return;
	var radioLength = radioObj.length;
	if(radioLength == undefined)
	{
		radioObj.checked = (radioObj.value == newValue.toString());
		return;
	}
	for(var i = 0; i < radioLength; i ++ )
	{
		radioObj[i].checked = false;
		if(radioObj[i].value == newValue.toString())
		{
			radioObj[i].checked = true;
		}
	}
}
function searchByName()
{
	var matchstrategy = document.getElementById('searchByNameSelect').value;
	var matchterm = document.getElementById('searchByNameInput').value;
	var a=matchterm.trim();
	if(a.length<3)
	{
		alert("Please enter a longer search term");
		return;
	}
	var matchontology = document.getElementById('searchByNameSelectOntology').value;
	var query = getONTgetNameInfoRequest(matchstrategy, matchterm, matchontology);
	searchByNameTree.el.mask('Searching...', 'x-mask-loading');
	for(c = searchByNameTreeRoot.childNodes.length - 1;
	c >= 0;
	c -- )
	{
		searchByNameTreeRoot.childNodes[c].remove();
	}
	searchByNameTree.render();
	Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/proxy?url=" + GLOBAL.ONTUrl + "getNameInfo",
				method : 'POST',
				xmlData : query,
				success : function(result, request)
				{
				searchByNameComplete(result);
				}
			,
			failure : function(result, request)
			{
				searchByNameComplete(result);
			}
			,
			timeout : '300000'
			}
	);
}

function getSummaryStatistics(postCompletionFunction,postCompletionFunctionParameter)
{
	Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/chart/basicStatistics",
				method : 'POST',
				success : function(result, request)
				{

				getSummaryStatisticsComplete(result);
                if(postCompletionFunction) postCompletionFunction(postCompletionFunctionParameter)
				}
			,
			failure : function(result, request)
			{
				getSummaryStatisticsComplete(result);
			}
			,
			timeout : '300000',
			params : Ext.urlEncode(
					{
						charttype : "basicstatistics",
						concept_key : "",
						result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
						result_instance_id2 : GLOBAL.CurrentSubsetIDs[2]
					}
			) // or a URL encoded string
			}
	);




	/* analysisPanel.load({
   // url : "analysis.jsp",
   url : "chart",
   params : Ext.urlEncode({charttype : "basicstatistics",
   concept_key : "",
   result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
   result_instance_id2 : GLOBAL.CurrentSubsetIDs[2]}), // or a URL encoded string
   // callback :
   // scope : yourObject, // optional scope for the callback
   discardUrl : false,
   nocache : true,
   text : "Loading...",
   timeout : 30,
   scripts : true
   }); */

	// analysisPanel.setTitle("Results/Analysis - Summary statistics");
	// analysisPanel.body.mask("Running analysis...", 'x-mask-loading');
	resultsTabPanel.body.mask("Running analysis...", 'x-mask-loading');
}


function gridViewCellFormating(value)
{
    if (value == "NULL")
        return '<span class="GVNullValues">' + value + '</span>';
    return value;
}

function buildColumnModel(fields)
{
	var size = fields.size();
	var con = new Array();
	for(var i = 0; i < size; i ++ )
	{
		var c = new Object();
		var f = fields[i];
		c.id = f.name;
		c.dataIndex = f.name;
		c.header = f.header;
		c.tooltip = f.name;
		c.width = f.width;
		c.sortable = f.sortable;
		c.menuDisabled = false;
        c.renderer = gridViewCellFormating;
		con.push(c);
	}

	return new Ext.grid.ColumnModel(con);
}

function getSummaryStatisticsComplete(result, request)
{
	// analysisPanel.body.update(result.responseText, true, null);
	// analysisPanel.getFrame().update(result.responseText, true, null);
	// lastAnalysisResult = result.responseText;
	resultsTabPanel.setActiveTab('analysisPanel');
	updateAnalysisPanel(result.responseText, false);
	getSummaryGridData();

	getExportButtonSecurity();
}


function getExportButtonSecurity()
{
	Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/export/exportSecurityCheck",
				method : 'POST',
				success : function(result, request)
				{
				getExportButtonSecurityComplete(result);
				}
			,
			failure : function(result, request)
			{
				getExportButtonSecurityComplete(result);
			}
			,
			timeout : '300000',
			params : Ext.urlEncode(
					{
						result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
						result_instance_id2 : GLOBAL.CurrentSubsetIDs[2]
					}
			) // or a URL encoded string
			}
	);
}

function getExportButtonSecurityComplete(result)
{
	var mobj=result.responseText.evalJSON();
	var canExport=mobj.canExport;
	if(canExport || GLOBAL.IsAdmin)
	{
		Ext.getCmp("exportbutton").enable();
	}
	else
	{
		Ext.getCmp("exportbutton").disable();
	}
}

function activateTab(tab)
{
    resultsTabPanel.tools['help help-resana-panel'].dom.style.display="";
}

/*function getSummaryGridData()
{
    gridstore = new Ext.data.JsonStore(
        {
            url : pageInfo.basePath+'/chart/basicGrid',
            root : 'rows',
            fields : ['name', 'url']
        }
    );
    gridstore.on('load', storeLoaded);
    var myparams = Ext.urlEncode(
        {
            charttype : "basicgrid",
            concept_key : "",
            result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
            result_instance_id2 : GLOBAL.CurrentSubsetIDs[2]
        }
    );
    // or a URL encoded string *//*

    gridstore.load(
        {
            params : myparams
        }
    );
}*/


function getSummaryGridData()
{

    isGridLoadRunning = true;
    analysisGridPanel.body.mask("Loading...", 'x-mask-loading');

    var subset1 = GLOBAL.CurrentSubsetIDs[1] == null ? "" : GLOBAL.CurrentSubsetIDs[1];
    var subset2 = GLOBAL.CurrentSubsetIDs[2] == null ? "" : GLOBAL.CurrentSubsetIDs[2];
    var dataArray = {columnsOnly: false, result_instance_id1:subset1, result_instance_id2: subset2, codeType : GLOBAL.codeType };

    setupGridViewWrapper();  //Add the datatable html if needed

    setTimeout(function(){}, 1);

    $j.getJSON(basicGridUrl, dataArray, function (data) {

        setupGridData(data);

        gridPanelHeaderTips = data.headerToolTips.slice(0);

        data.fnDrawCallback = function( oSettings ) {

        };

        modifyDataTable();

        setTimeout(function(){}, 1);

        analysisGridPanel.body.mask("Data fetched, processing results...", 'x-mask-loading');

        /* This removes the subject profile link from the grid
         data.aoColumnDefs=[ {
         "aTargets": [ 1 ],
         "mData": function ( source, type, val ) {
         return source[1];
         },
         "mRender": function ( data, type, full ) {
         return '<a onclick="displayProfile('+full[0]+',\''+data+'\',\''+full[3]+'\')" href="#">'+data+'</a>';
         }
         } ];
         */

        window.dt = $j('#gridViewTable').dataTable(data);

        $j(window).bind('resize', function () {
            $j('#gridViewTable').dataTable().fnAdjustColumnSizing();
        } );

        analysisGridPanel.body.unmask();

        ColVis.fnSaveVis(window.dt);

    });

    //resultsTabPanel.setActiveTab('analysisGridPanel');
    analysisGridPanel.doLayout();

    resultsTabPanel.body.unmask();

}


function modifyDataTable() {

    //Override the pre-sorting algorithm for numerics so empty values go to top or bottom
    $j.fn.dataTableExt.oSort['numeric-pre']  = function(a) {

        var floatA = parseFloat(a);
        var returnValue;

        if (isNaN(floatA))
            returnValue = Number.MAX_VALUE * -1;	//Emptys will go to top for -1, bottom for +1
        else
            returnValue = floatA;

        return returnValue;
    };
}

function setupGridViewWrapper()
{
    var gridTable = $j('#gridViewTable');
    var gpBody = analysisGridPanel.body;

    //Remove the wrapper html if it exists
    if (gridTable.length != 0)
    {
        var gridViewWrapper = $j('#gridViewWrapper');
        if (gridViewWrapper.length != 0) {
            gridViewWrapper.remove();
        }
    }

    gpBody.insertHtml('afterBegin', '<div id=\'gridViewWrapper\'><table id=\'gridViewTable\'></table></div><div id=\'subjectProfileWrapper\'></div>');
}


function setupGridData(data)
{
    data.bAutoWidth = true;
    data.bScrollAutoCss = true;
    data.sScrollY = 400;
    data.sScrollX = "100%";
    data.bDestroy = true;
    data.bProcessing = true;
    data.bLengthChange = false;
    data.bScrollCollapse = false;
    data.iDisplayLength = 100;
    data.isDrawingDelayed = true;
    data.fnLoadCallBack = function() {
        isGridLoadDone = true;
        GLOBAL.isGridViewLoaded = true;
        trySizeGridTable();
    };
    data.sDom = "<\"top\"RfCp>rt<\"bottom\"ip><\"clear\">";
}

var trySizeGridTable = function ()
{

    if (!isGridLoadRunning) { return; }

    var activePanel = resultsTabPanel.getActiveTab();

    if (isGridLoadDone && activePanel == analysisGridPanel )
    {
        sizeGridTable();
    }
    else
    {
        setTimeout(function() {trySizeGridTable}, 500);
    }
}


var sizeGridTable = function ()
{

    var gridTable = $j('#gridViewTable');

    //Don't try to size the table if there is no table
    if (gridTable == null)
    {
        return;
    }

    var d = gridTable.dataTable({"bRetrieve": true});
    d.fnAdjustColumnSizing();

    $j(".dataTables_scrollHeadInner > table > thead > tr > th").each( function (index) {

        var titleAttr = $j(this).attr("title");

        if (titleAttr == null && gridPanelHeaderTips != null)
        {
            $j(this).attr("title", gridPanelHeaderTips[index]);
        }

    });

    analysisGridPanel.body.unmask();

    isGridLoadRunning = false;
    isGridLoadDone = false;


}


function storeLoaded()
{
	var blah = gridstore;
	var cm = buildColumnModel(gridstore.reader.meta.fields);
	if(window.grid)
	{
		analysisGridPanel.remove(grid);
	}
	grid = new Ext.grid.GridPanel(
			{
				id : 'grid',
				store : gridstore,
				cm : cm,
				viewConfig :
				{
				// forceFit : true
				}
			,
			sm : new Ext.grid.RowSelectionModel(
					{
						singleSelect : true
					}
			),
			layout : 'fit',
			width : 800
			// frame : true,
			// title : 'Framed with Checkbox Selection and Horizontal Scrolling'
			}
	);
	analysisGridPanel.add(grid);
	analysisGridPanel.doLayout();
	resultsTabPanel.body.unmask();
}

function getAnalysisGridData(concept_key)
{

    isGridLoadRunning = true;

    analysisGridPanel.body.mask("Loading...", 'x-mask-loading');

    var subset1 = GLOBAL.CurrentSubsetIDs[1] == null ? "" : GLOBAL.CurrentSubsetIDs[1];
    var subset2 = GLOBAL.CurrentSubsetIDs[2] == null ? "" : GLOBAL.CurrentSubsetIDs[2];
    var dataArray = {concept_key: concept_key, result_instance_id1:subset1, result_instance_id2: subset2, columnsOnly: false };

    setupGridViewWrapper(); //Setup the html for the jquery datatable

    setTimeout(function(){}, 1);

    $j.getJSON(analysisGridUrl, dataArray, function (data) {

        //Add the configuration settings for the grid
        setupGridData(data);

        gridPanelHeaderTips = data.headerToolTips.slice(0);

        //Add the callback for when the grid is redrawn
        data.fnDrawCallback = function( oSettings ) {

            $j(".dataTables_scrollHeadInner > table > thead > tr > th").each( function (index) {

                var titleAttr = $j(this).attr("title");

                if (titleAttr == null && gridPanelHeaderTips != null)
                {
                    $j(this).attr("title", gridPanelHeaderTips[index]);
                }

            });

            analysisGridPanel.body.unmask();
        };
        /* This removes the subject profile link from the grid
         data.aoColumnDefs=[ {
         "aTargets": [ 1 ],
         "mData": function ( source, type, val ) {
         return source[1];
         },
         "mRender": function ( data, type, full ) {
         return '<a onclick="displayProfile('+full[0]+',\''+data+'\',\''+full[3]+'\')" href="#">'+data+'</a>';
         }
         } ];
         */

        modifyDataTable();

        setTimeout(function(){}, 1);

        window.dt = $j('#gridViewTable').dataTable(data);

        if (window.dt.dataTableSettings[0].aoColumns.length !==  window.columnVis.length)
        {
            window.columnVis.push(true);
        }

        ColVis.fnApplyVis(window.dt) ;

        $j(window).bind('resize', function () {
            $j('#gridViewTable').dataTable().fnAdjustColumnSizing()
        } );


    });

    analysisGridPanel.doLayout();
    resultsTabPanel.body.unmask();
}

function getAnalysisPanelContent()
{
	var a = analysisPanel.body;
	return analysisPanel.body.dom.innerHTML;
}

function printPreview(content)
{
	var stylesheet = "<html><head><link rel='stylesheet' type='text/css' href='css/chartservlet.css'></head><body>";
	var generator = window.open('', 'name', 'height=400,width=500, resizable=yes, scrollbars=yes');
	var printbutton = "<input type='button' value=' Print this page 'onclick='window.print();return false;' />";
	generator.document.write(stylesheet + printbutton + content);
	generator.document.close();
	// generator.print();
}

function exportGrid()
{
	viewport.getEl().mask("Getting Data....");
	/* Ext.Ajax.request(
   {
   url : "export",
   method : 'POST',
   success : function(result, request){exportDataComplete(result); },
   failure : function(result, request){exportDataComplete(result); },
   timeout : '300000',
   params : { result_instance_id1 : id1,
   result_instance_id2 : id2
   }
   });  */
	// Ext.get("rid1").dom.value = id1;
	// Ext.get("rid2").dom.value = id2;
	Ext.get("exportgridform").dom.submit();
	setTimeout('viewport.getEl().unmask();', 10000);
}

function watchForSymbol(options)
{
	var stopAt;

	if ( ! options || ! options.symbol || ! Object.isFunction(options.onSuccess))
	{
		throw "Missing required options";
	}
	options.onTimeout = options.onTimeout || Prototype.K;
	options.timeout = options.timeout || 10;
	stopAt = (new Date()).getTime() + (options.timeout * 1000);
	new PeriodicalExecuter(function(pe)
			{
		if (typeof window[options.symbol] != "undefined")
		{
			pe.stop();
			options.onSuccess(options.symbol);
		}
		else if ((new Date()).getTime() > stopAt)
		{
			pe.stop();
			options.onTimeout(options.symbol);
		}
			}
	, 0.25);
}

//Called to run the Haploviewer
function getHaploview()
{
	Ext.Ajax.request({						
		url: pageInfo.basePath+"/asyncJob/createnewjob",
		method: 'POST',
		success: function(result, request){
			RunHaploViewer(result, GLOBAL.CurrentSubsetIDs[1], GLOBAL.CurrentSubsetIDs[2], GLOBAL.CurrentGenes);
		},
		failure: function(result, request){
			Ext.Msg.alert('Status', 'Unable to create the heatmap job.');
		},
		timeout: '1800000',
		params: {jobType:  "Haplo"}
	});	
}

function RunHaploViewer(result, result_instance_id1, result_instance_id2, genes)
{
	var jobNameInfo = Ext.util.JSON.decode(result.responseText);					 
	var jobName = jobNameInfo.jobName;

	//genePatternReplacement();
	showJobStatusWindow(result);	
	document.getElementById("gplogin").src = pageInfo.basePath + '/analysis/gplogin';   // log into GenePattern
	Ext.Ajax.request(
		{						
			url: pageInfo.basePath+"/genePattern/runhaploviewer",
			method: 'POST',
			timeout: '1800000',
			params: {result_instance_id1: result_instance_id1,
				result_instance_id2:  result_instance_id2,
				genes: genes,
				jobName: jobName
			}
	});
	checkJobStatus(jobName);
}

function searchByTagBefore()
{
	//ontFilterPanel.el.mask("Searching...");
	var tagterm=document.getElementById("tagterm");
	var tagtype=document.getElementById("tagtype");
	var searchterm = document.getElementById('ontsearchterm').value;
	var a=searchterm.trim();
	if(a.length>0 && a.length<3)
	{
		alert("Please enter a longer search term.");
		return false;
	}
	if(a.length==0 && tagtype.selectedIndex==0)
	{
		alert("Please select a search term.");
		return false;
	}
	if(a.length==0 && tagtype.selectedIndex!=0)
	{
		if(tagterm.selectedIndex==-1)
		{
			alert("Please select a search term.");
			return false;
		}
	}
	for(c = ontFilterTreeRoot.childNodes.length - 1;
	c >= 0;
	c -- )
	{
		ontFilterTreeRoot.childNodes[c].remove();
	}
	ontFilterTree.render();
	viewport.el.mask("Searching...")
	return true;
}

function searchByTagComplete(response) {
    // shorthand
    var Tree = Ext.tree;
    //ontFilterPanel.el.unmask();
    viewport.el.unmask();
    var robj = response.responseText.evalJSON();
    var rtext = robj.resulttext;
    var concepts = robj.concepts;
    // concept = concepts[4];
    // test = concept.selectSingleNode('name').firstChild.nodeValue;
    // alert(response.responseText);
    var length;
    var leaf = false;
    var draggable = false;
    if (concepts != undefined) {
        if (concepts.length < GLOBAL.MaxSearchResults) {
            length = concepts.length;
        }
        else {
            length = GLOBAL.MaxSearchResults;
        }
        for (var c = 0; c < length; c++) {
            var newnode = getTreeNodeFromJSON(concepts[c])
			ontFilterTreeRoot.appendChild(newnode);
			setTreeNodeSecurity(newnode, concepts[c].access);
		}
		var t=document.getElementById("searchresultstext");
		t.innerHTML=rtext;
	}
}

function showHaploviewGeneSelection() {
	//genePatternReplacement();
	//if(!this.compareStepPathwaySelection)
	//{
	var win = new Ext.Window({
		id: 'showHaploviewGeneSelection',
		title: 'Haploview-Gene Selection',
		layout:'fit',
		width:250,
		height:250,
		closable: false,
		plain: true,
		modal: true,
		border:false,
		//autoScroll: true,
		buttons: [
		          {
		        	  id: 'haploviewGeneSelectionOKButton',
		        	  text: 'OK',
		        	  handler: function(){
		        	  if(Ext.get('haploviewgenes')==null)
		        	  {
		        		  win.close();
		        		  return;
		        	  }
		        	  var ob=Ext.get('haploviewgenes').dom;
		        	  var selected = new Array();
		        	  for (var i = 0; i < ob.options.length; i++)
		        		  if (ob.options[i].selected)
		        			  selected.push(ob.options[i].value);
		        	  //alert(selected.join(','));
		        	  GLOBAL.CurrentGenes=selected.join(',');
		        	  getHaploview();
		        	  win.close();}
		          }
		          ,{
		        	  text: 'Cancel',
		        	  handler: function(){
		        	  win.close();}
		          }],
		          resizable: false,
		          autoLoad:
		          {
		url: pageInfo.basePath+'/analysis/showHaploviewGeneSelector',
		scripts: true,
		nocache:true,
		discardUrl:true,
		method:'POST',
		params: {result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
		result_instance_id2: GLOBAL.CurrentSubsetIDs[2]}
		          },
		tools:[{
			id:'help',
			qtip:'Click for context sensitive help',
		    handler: function(event, toolEl, panel){
		   	D2H_ShowHelp("1174",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
		    }
		}]
	});
	//  }
	win.show(viewport);
}

function genePatternLogin() {
    element = document.getElementById("gplogin");
	if(element)
        element.src = pageInfo.basePath + '/analysis/gplogin';
}

function showWorkflowStatusWindow()
{	
	wfsWindow = new Ext.Window({
		id: 'showWorkflowStatus',
		title: 'Workflow Status',
		layout:'fit',
		width:300,
		height:300,
		closable: false,
		plain: true,
		modal: true,
		border:false,
		//autoScroll: true,
		buttons: [
		         {
		        	  text: 'Cancel Job',
		        	  handler: function(){
		        	  runner.stopAll();
		        	  terminateWorkflow();
		        	  wfsWindow.close();}
		          }],
		          resizable: false,
		          autoLoad:
		          {
					url: pageInfo.basePath+'/asyncJob/showWorkflowStatus',
					scripts: true,
					nocache:true,
					discardUrl:true,
					method:'POST'
		          }
	});
	//  }
	wfsWindow.show(viewport);
	
	var updateStatus = function(){
		Ext.Ajax.request(
				{
					url : pageInfo.basePath+"/asyncJob/checkWorkflowStatus",
					method : 'POST',
					success : function(result, request)
					{
						//alert(result);
						workflowStatusUpdate(result);
					}
				,
				failure : function(result, request)
				{
					//alert(result);
					//saveComparisonComplete(result);
				}
				,
				timeout : '300000'
				}
		);
  	} 
  	
  	var task = {
  	    run: updateStatus,
  	    interval: 4000 //4 second
  	}
 
  	runner.start(task);
  	


}

function terminateWorkflow(){
	Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/asyncJob/cancelJob",
				method : 'POST',
				success : function(result, request)
				{
					
				}
			,
			failure : function(result, request)
			{
				//alert(result);
				//saveComparisonComplete(result);
			}
			,
			timeout : '300000'
			}
	);
}
function workflowStatusUpdate(result){
	var response=eval("(" + result.responseText + ")");	
	var inserthtml = response.statusHTML;
	var divele = Ext.fly("divwfstatus");
	if(divele!=null){
		divele.update(inserthtml);
	}
	var status = response.wfstatus;
	if(status =='completed'){
		runner.stopAll();		
		if(divele!=null){
			divele.update("");
		}		
		if(wfsWindow!=null){
			wfsWindow.close();
			wfsWindow =null;
		}		
		//var rpCount = response.rpCount;
		//if(rpCount<=1){}
		// only show it once
		showWorkflowResult(result);
	} 
}

function showWorkflowResult(result)
{
	var response=eval("(" + result.responseText + ")");
	var jobNumber = response.jobNumber;
	var viewerURL = response.viewerURL;
	var altviewerURL = response.altviewerURL;
	var gctURL = response.gctURL;
	var cdtURL = response.cdtURL;
	var gtrURL = response.gtrURL;
	var atrURL = response.atrURL;
	var error = response.error;
	var snpGeneAnnotationPage = response.snpGeneAnnotationPage;

	//Ext.MessageBox.hide();

	if (error != undefined) {
		alert(error);
	} 
	else {
		if (snpGeneAnnotationPage != undefined && snpGeneAnnotationPage.length != 0) {
			showSnpGeneAnnotationPage(snpGeneAnnotationPage);
		}
		runVisualizerFromSpan(viewerURL, altviewerURL);
	}
}

function showSnpGeneAnnotationPage(snpGeneAnnotationPage) {
	var resultWin = window.open('', 'Snp_Gene_Annotation_' + (new Date()).getTime(), 
		'width=600,height=800,scrollbars=yes,resizable=yes,location=no,toolbar=no,status=no,menubar=no,directories=no');
	resultWin.document.write(snpGeneAnnotationPage);
}

function saveComparison()
{

	Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/comparison/save",
				method : 'POST',
				success : function(result, request)
				{
				saveComparisonComplete(result);
				}
			,
			failure : function(result, request)
			{
				saveComparisonComplete(result);
			}
			,
			timeout : '600000',
			params : Ext.urlEncode(
					{
						result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
						result_instance_id2 : GLOBAL.CurrentSubsetIDs[2],
						genes: GLOBAL.CurrentGenes
					}
			) // or a URL encoded string
			}
	);
}

function saveComparisonComplete(result)
{
	var mobj=result.responseText.evalJSON();
	
	//If the window is already open, close it.
	if(this.saveComparisonWindow) saveComparisonWindow.close();
	
	//Draw the window with the link to the comparison.
	saveComparisonWindow = new Ext.Window
	({
        id: 'saveComparisonWindow',
        title: 'Saved Comparison',
        autoScroll:true,
        closable: true,
        tools: [
                  {
                	  	id : 'saveComparisonWindowHelpButton',
						qtip: 'Click for Saved Camparison Window Help',
						disabled : false,
						handler : function()
						{
						    D2H_ShowHelp("1474",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
						}
                  }
                 ],
        resizable: true,
        width: 200,
        html: mobj.link
	});	
	
	//Show the window we just created.
	saveComparisonWindow.show(viewport);	
}

function ontFilterLoaded(el, success, response, options)
{
	if(GLOBAL.preloadStudy != "")
		{
			Ext.get("ontsearchterm").dom.value = GLOBAL.preloadStudy;
			Ext.get("ontSearchButton").dom.click();
		}
}

function clearQuery() {
    if (confirm("Are you sure you want to clear your current analysis?")) {
        clearAnalysisPanel();
        resetQuery();
        clearDataAssociation();
        GLOBAL.AnalysisHasBeenRun = false;
    }
}

function addHelpIcons() {

    jQuery('#analysisPanel').append(getHelpIconHTML(1074, 20, 25));
    jQuery('#resultsTabPanel .x-tab-panel-header').append(getHelpIconHTML(1061));
    jQuery('#ontPanel .x-tab-panel-header').append(getHelpIconHTML(1064));

    jQuery('#ontFilterPanel').append(getHelpIconHTML(1065, 4, 28));

}

function getHelpIconHTML(id, right, top) {
    if (right == null) {
        right = 4;
    }
    if (top == null) {
        top = 2;
    }
    return ('<div style="z-index: 999; cursor: pointer; height: 18px; width: 18px; position: absolute; right: ' + right + 'px; top: ' + top + 'px" class="contextHelpBtn" onclick="D2H_ShowHelp(\'' + id + '\',helpURL,\'wndExternal\',CTXT_DISPLAY_FULLHELP);">&nbsp;</div>')
}




/*If includeExcludeFlag is
 * -"include": Across Trials is the only concept included
 * -"exclude": Across Trials concept is the only concept excluded
 */
function createTree(includeExcludeFlag, ontresponse) {
    // shorthand
    var Tree = Ext.tree;
    var treelist = [];
    var treeRoot = new Tree.TreeNode(
        {
            text: 'root',
            draggable: false,
            id: 'root',
            qtip: 'root'
        }
    );


    for (var c = 0; c < ontresponse.length; c++) {
        var level = ontresponse[c].level;
        var key = ontresponse[c].key;
        var name = ontresponse[c].name;
        var tooltip = ontresponse[c].tooltip;
        var dimcode = ontresponse[c].dimcode;

        if (includeExcludeFlag === "include" && name !== "Across Trials") continue;
        if (includeExcludeFlag === "exclude" && name === "Across Trials") continue;
        // set the root node
        var autoExpand = false;
        if (GLOBAL.PathToExpand.indexOf(key) > -1) autoExpand = true;
        if(GLOBAL.PathToExpand != '' && GLOBAL.PathToExpand.indexOf(key) == -1) { continue; }
        var ontRoot = new Tree.AsyncTreeNode(
            {
                text: name,
                draggable: false,
                id: key,
                qtip: tooltip,
                expanded: autoExpand
            }
        );

        //treeRoot.appendChild(ontRoot);
        treelist.push(ontRoot)  ;

        /***************************************/
        var fullname = key.substr(key.indexOf("\\", 2), key.length);
        var access = GLOBAL.InitialSecurity[fullname];

        if ((access != undefined && access != 'Locked') || GLOBAL.IsAdmin) //if im an admin or there is an access level other than locked leave node unlocked
        {
            //leave node unlocked must have some read access
        }
        else {
            //default node to locked
            //child.setText(child.text+" <b>Locked</b>");
            ontRoot.attributes.access = 'locked';
            ontRoot.disable();
            ontRoot.on('beforeload', function (node) {
                alert("Access to this node has been restricted. Please contact your administrator for access.");
                return false
            });
        }
    }

    //return treeRoot;
    return treelist;
}

/*
 * the id_in drives which off these tabs is created
 *
 */


function getSubCategories(id_in, title_in, ontresponse)
{
    // shorthand
    var Tree = Ext.tree;
    var ontRoots;
    var panel;

        var showFn;

        if (id_in==='crossTrialsPanel'){
            showFn = function(node, e){
                Ext.tree.TreePanel.superclass.onShow.call(this);
                // Ext.get('advancedbutton').dom.style.display='none';
            }
            ontRoots = createTree('include', ontresponse);
            panel = Ext.getCmp('crossTrialsPanel');
        }
        else {
        showFn = function(node, e){
            Ext.tree.TreePanel.superclass.onShow.call(this);
            // Ext.get('advancedbutton').dom.style.display='';
        }
        ontRoots = createTree('exclude', ontresponse);
        panel = Ext.getCmp('navigateTermsPanel');
    }

    /*var toolbar = new Ext.Toolbar([
     {
     id:'contextHelp-button',
     handler: function(event, toolEl, panel){
     D2H_ShowHelp((id_in=="navigateTermsPanel")?"1066":"1091",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
     },
     iconCls: "contextHelpBtn"
     }
     ]); */

    // TODO This needs further investiguation regarding to the acrossTrial feature
    if (!panel)
        return;

    var treeRoot = panel.getRootNode();
    for(c = treeRoot.childNodes.length - 1; c >= 0; c -- ) {
        treeRoot.childNodes[c].remove();
    }

    jQuery('#noAnalyzeResults').hide();

    for(var c = 0; c < ontRoots.length; c ++ )
    {
        var newnode=ontRoots[c];
        treeRoot.appendChild(newnode);
    }

    if (ontRoots.length == 0) { //This shouldn't happen!
        jQuery('#noAnalyzeResults').show();
    }

    if(GLOBAL.Debug)
    {
        alert(ontresponse.responseText);
    }

    onWindowResize();

    panel.render();
    panel.dragZone.addToGroup("analysis");

    /*if(GLOBAL.IsAdmin)
     {
     searchByNameTree.dragZone.addToGroup("analysis");
     }*/
}
