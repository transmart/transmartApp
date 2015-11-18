String.prototype.trim = function() {
    return this.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
};

Ext.layout.BorderLayout.Region.prototype.getCollapsedEl = Ext.layout.BorderLayout.Region.prototype.getCollapsedEl.createSequence(function () {
    if ((this.position === 'north' || this.position === 'south') && !this.collapsedEl.titleEl) {
        this.collapsedEl.titleEl = this.collapsedEl.createChild({
            style: 'color:#15428b;font:11px/15px tahoma,arial,verdana,sans-serif;padding:2px 5px;', 
            cn: this.panel.title
        });
    }
});

var runner = new Ext.util.TaskRunner();
var wfsWindow = null;

function dataSelectionCheckboxChanged(ctl) {
    if (getSelected(ctl)[0] !== undefined) {
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
        Ext.Ajax.request({
            url: pageInfo.basePath+"/dataAssociation/loadScripts",
            method: 'GET',
            timeout: '600000',
            params: Ext.urlEncode({}),
            success: function (result, request) {
                var exp = jQuery.parseJSON(result.responseText);
                if (exp.success && exp.files.length > 0) {
                    loadScripts(exp.files);
                }
            },
            failure: function (result, request) {
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
    for (var i = 0, iLength = scripts.length; i < iLength; i++) {

        var file = scripts[i];

        if (file.type === 'script') { // if javascript
            $j.getScript(file.path);
        } else if (file.type === 'css') { // if css
            $j('head').append($j('<link rel="stylesheet" type="text/css" />').attr('href', file.path));
        } else {
            console.error("Unknown file type.");
        }
    }
}

Ext.Panel.prototype.setBody = function (html) {
    var el = this.getEl();
    var domel = el.dom.lastChild.firstChild;
    domel.innerHTML = html;
};

Ext.Panel.prototype.getBody = function (html) {
    var el = this.getEl();
    var domel = el.dom.lastChild.firstChild;
    return domel.innerHTML;
};

Ext.onReady(function () {

    Ext.QuickTips.init();

    //set ajax to 600*1000 milliseconds
    Ext.Ajax.timeout = 1800000;

    // this overrides the above
    Ext.Updater.defaults.timeout = 1800000;

    // create the main regions of the screen
    westPanel = new Ext.Panel({
        id: 'westPanel',
        region: 'west',
        width: 320,
        minwidth: 280,
        split: true,
        border: true,
        layout: 'border'
    });
    
    var tb = new Ext.Toolbar({
        id: 'maintoolbar',
        title: 'maintoolbar',
        items: [
            new Ext.Toolbar.Button({
                id: 'changetool',
                text: 'Switch to subset view',
                iconCls: 'nextbutton',
                disabled: false,
                handler: function () {
                    window.location.href = "i2b2client.jsp";
                }
            })
        ]
    });

    expmenu = new Ext.menu.Menu({
        id: 'exportMenu',
        minWidth: 250,
        items: [
            {
                text: 'Summary Statistics',
                handler: function() {
                    if (typeof(grid) !== undefined && grid !== null) {
                        exportGrid();
                    } else {
                        alert("Nothing to export");
                    }
                }
            },
            '-',
            {
                text: 'Gene Expression/RBM Datasets',
                handler: function() {
                    exportDataSets();
                }
            }
        ]
    });

    advmenu = new Ext.menu.Menu({
        id: 'advancedMenu',
        minWidth: 250,
        items: [
            {
                text: 'Heatmap',
                // when checked has a boolean value, it is assumed to be a CheckItem
                handler: function () {
                    GLOBAL.HeatmapType = 'Compare';
                    validateHeatmap();
                    advancedWorkflowContextHelpId = "1085";
                },
                disabled: GLOBAL.GPURL === ""
            },
            {
                text: 'Hierarchical Clustering',
                // when checked has a boolean value, it is assumed to be a CheckItem
                handler: function () {
                    GLOBAL.HeatmapType = 'Cluster';
                    validateHeatmap();
                    advancedWorkflowContextHelpId = "1085";
                },
                disabled: GLOBAL.GPURL === ""
            },
            {
                text: 'K-Means Clustering',
                // when checked has a boolean value, it is assumed to be a CheckItem
                handler: function () {
                    GLOBAL.HeatmapType = 'KMeans';
                    validateHeatmap();
                    advancedWorkflowContextHelpId = "1085";
                },
                disabled: GLOBAL.GPURL === ""
            },
            {
                text: 'Comparative Marker Selection (Heatmap)',
                // when checked has a boolean value, it is assumed to be a CheckItem
                handler: function () {
                    GLOBAL.HeatmapType = 'Select';
                    validateHeatmap();
                    advancedWorkflowContextHelpId = "1085";
                },
                disabled: GLOBAL.GPURL === ""
            },
            '-',                            
            {
                text: 'Principal Component Analysis',
                // when checked has a boolean value, it is assumed to be a CheckItem
                handler: function () {
                    GLOBAL.HeatmapType = 'PCA';
                    validateHeatmap();
                    advancedWorkflowContextHelpId = "1172";
                },
                disabled: GLOBAL.GPURL === ""
            },
            '-',
            {
                text: 'Survival Analysis',
                handler: function () {
                    if (isSubsetEmpty(1) || isSubsetEmpty(2)) {
                        alert('Survival Analysis needs time point data from both subsets.');
                        return;
                    } else {
                        showSurvivalAnalysis();
                    }
                },
                disabled: GLOBAL.GPURL === ""
            },
            '-',
            {
                text: 'Haploview',
                handler: function() {
                    if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
                        alert('Empty subsets found, need a valid subset to analyze!');
                        return;
                    }
                    if ((GLOBAL.CurrentSubsetIDs[1] === null && !isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] === null && !isSubsetEmpty(2))) {
                        runAllQueries(function() {
                            showHaploviewGeneSelection();
                        });
                    } else {
                        showHaploviewGeneSelection();
                    }
                    return;
                }
            },
            {
                text: 'SNPViewer',
                handler: function() {
                    if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
                        alert('Both dataset is empty. Please choose a valid dataset.');
                        return;
                    }
                    if ((GLOBAL.CurrentSubsetIDs[1] === null && !isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] === null && !isSubsetEmpty(2))) {
                        runAllQueries(function() {
                            showSNPViewerSelection();
                        });
                    } else {
                        showSNPViewerSelection();
                    }
                    return;
                },
                disabled: GLOBAL.GPURL === ""
            },
            {
                text: 'Integrative Genome Viewer',
                handler: function() {
                    if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
                        alert('Both dataset is empty. Please choose a valid dataset.');
                        return;
                    }
                    if ((GLOBAL.CurrentSubsetIDs[1] === null && !isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] === null && !isSubsetEmpty(2))) {
                        runAllQueries(function()    {
                            showIgvSelection();
                        });
                    } else {
                        showIgvSelection();
                    }
                    return;
                },
                disabled: GLOBAL.GPURL === ""
            },
            {
                text: 'PLINK',
                disabled: true,
                handler: function() {
                    if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
                        alert('Both dataset is empty. Please choose a valid dataset.');
                        return;
                    
                    }
                    if ((GLOBAL.CurrentSubsetIDs[1] === null && !isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] === null && !isSubsetEmpty(2))) {
                        runAllQueries(function()    {
                            showPlinkSelection();
                        });
                    } else {
                        showPlinkSelection();
                    }
                    return;
                }
            },
            {
                text: 'Genome-Wide Association Study',
                handler: function() {
                    if (isSubsetEmpty(1) || isSubsetEmpty(2)) {
                        alert('Genome-Wide Association Study needs control datasets (normal patients) in subset 1, and case datasets (disease patients) in subset 2.');
                        return;
                    }
                    if ((GLOBAL.CurrentSubsetIDs[1] === null && !isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] === null && !isSubsetEmpty(2))) {
                        runAllQueries(function()    {
                            showGwasSelection();
                        });
                    } else  {
                        showGwasSelection();
                    }
                    return;
                }
            }
        ]
    });

    var tb2 = new Ext.Toolbar({
        id: 'maintoolbar',
        title: 'maintoolbar',
        items: [
            new Ext.Toolbar.Button({
                id: 'dataExplorerHelpButton',
                iconCls: 'contextHelpBtn',
                qtip: 'Click for Dataset Explorer Help',
                disabled: false,
                handler: function () {
                    D2H_ShowHelp("1258",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP);
                }
            })
        ]
    });

    centerMainPanel = new Ext.Panel({
        id: 'centerMainPanel',
        region: 'center',
        // tbar: tb,
        layout: 'border'
    });

    centerPanel = new Ext.Panel({
        id: 'centerPanel',
        region: 'center',
        width: 500,
        minwidth: 150,
        split: true,
        border: true,
        layout: 'fit'
    });

    // **************
    // Comparison tab
    // **************

    queryPanel = new Ext.Panel({
        id: 'queryPanel',
        title: 'Comparison',
        region: 'north',
        height: 340,
        autoScroll: true,
        split: true,                    
        autoLoad: {
            url: pageInfo.basePath+'/datasetExplorer/queryPanelsLayout',
            scripts: true,
            nocache: true,
            discardUrl: true,
            method: 'POST'
        },
        collapsible: true,
        titleCollapse: false,
        animCollapse: false,
        listeners: {
            activate: function() {
                GLOBAL.Analysis="Advanced";
            }
        }
    });

    resultsPanel = new Ext.Panel({
        id: 'resultsPanel',
        title: 'Results',
        region: 'center',
        split: true,
        height: 90
    });

    resultsTabPanel = new Ext.TabPanel({
        id: 'resultsTabPanel',
        title: 'Analysis/Results',
        region: 'center',
        defaults: {
            hideMode: 'display'
        },
        collapsible: false,
        //height: 300,
        deferredRender: false,
        activeTab: 0,
        tools: [
            {
                id: 'help',
                qtip:'Click for Generate Summary Statistics help',
                handler: function(event, toolEl, panel) {
                    D2H_ShowHelp("1074",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP);
                },
                hidden:true
            }
        ]
    });

    GalaxyPanel = new Ext.Panel({
        id: 'GalaxyPanel',
        title: 'Galaxy Export',
        region: 'center',
        split: true,
        height: 90,
        layout: 'fit',
        listeners: {
            activate: function(p) {
                getJobsDataForGalaxy(p);
            }
        },
        collapsible: true
    });

    // **************
    // Grid view tab
    // **************

    analysisGridPanel = new Ext.Panel({
        id: 'analysisGridPanel',
        title: 'Grid View',
        region: 'center',
        split: true,
        height: 90,
        layout: 'fit',
        listeners: {
            activate: function (p) {
                if (isSubsetQueriesChanged(p.subsetQueries) || !Ext.get('analysisGridPanel')) {
                    runAllQueries(getSummaryGridData, p);
                    activateTab();
                    onWindowResize();
                } else {
                    getSummaryGridData();
                }
            },
            deactivate: function() {
                resultsTabPanel.tools.help.dom.style.display = "none";
            },
            'afterLayout': {
                fn: function (el) {
                    onWindowResize();
                }
            }
        }
    });

    // ******************
    // Summary Statistics
    // ******************

    analysisPanel = new Ext.Panel ({
        id: 'analysisPanel',
        title: 'Summary Statistics',
        region: 'center',
        fitToFrame: true,
        listeners: {
            activate: function (p) {
                if (isSubsetQueriesChanged(p.subsetQueries) || !Ext.get('analysis_title')) {
                    p.body.mask("Loading...", 'x-mask-loading');
                    runAllQueries(getSummaryStatistics, p);
                    activateTab();
                    onWindowResize();
                }
            },
            deactivate: function() {
                resultsTabPanel.tools.help.dom.style.display = "none";
            },
            'afterLayout': {
                fn: function (el) {
                    onWindowResize();
                }
            }
        },
        autoScroll: true,
        html: '<div style="text-align:center;font:12pt arial;width:100%;height:100%;">' +
              '<table style="width:100%;height:100%;"><tr><td align="center" valign="center">Drag concepts ' +
              'to this panel to view a breakdown of the subset by that concept</td></tr></table></div>',
        split: true,
        closable: false,
        height: 90,
        tbar: [
            '->', // Fill
            {
                id: 'printanalysisbutton',
                text: 'Print',
                iconCls: 'printbutton',
                handler: function() {
                    var text = getAnalysisPanelContent();
                    printPreview(text);
                }
            }
        ]
    });

    // ************
    // Data Exports
    // ************

    analysisDataExportPanel = new Ext.Panel({
        id: 'analysisDataExportPanel',
        title: 'Data Export',
        region: 'center',
        split: true,
        height: 90,
        layout: 'fit',
        listeners: {
            activate: function(p) {
                if (isSubsetQueriesChanged(p.subsetQueries) || !Ext.get('dataTypesGridPanel')) {
                    p.body.mask("Loading...", 'x-mask-loading');
                    runAllQueries(getDatadata, p);
                    return;
                }
            },
            'afterLayout': {
                fn: function (el) {
                    onWindowResize();
                }
            }
        },
        collapsible: true                       
    });
    
    // ******************
    // Advanced Workflow
    // ******************

    dataAssociationPanel = new Ext.Panel({
        id: 'dataAssociationPanel',
        title: 'Advanced Workflow',
        region: 'center',
        split: true,
        height: 90,
        layout: 'fit',
        tbar: new Ext.Toolbar({
            id: 'advancedWorkflowToolbar',
            title: 'Advanced Workflow actions',
            items: []
        }),
        autoScroll: true,
        autoLoad: {
            url: pageInfo.basePath+'/dataAssociation/defaultPage',
            method: 'POST',
            callback: setDataAssociationAvailableFlag,
            evalScripts:true
        },
            listeners: {
            activate: function (p) {
                /**
                 * routines when activating advanced workflow tab
                 * @private
                 */
                var _activateAdvancedWorkflow = function () {
                    activateTab();
                    GLOBAL.Analysis="dataAssociation";
                    renderCohortSummary();
                    onWindowResize();
                };

                if (isSubsetQueriesChanged(p.subsetQueries)) {
                    runAllQueries(_activateAdvancedWorkflow, p);
                }

                _activateAdvancedWorkflow();
            },
            'afterLayout': {
                fn: function (el) {
                    onWindowResize();
                }
            }
        },
        collapsible: true
    });

    // ******************
    // Export Jobs
    // ******************

    analysisExportJobsPanel = new Ext.Panel({
        id: 'analysisExportJobsPanel',
        title: 'Export Jobs',
        region: 'center',
        split: true,
        height: 90,
        layout: 'fit',
        listeners: {
            activate: function(p) {
                p.body.mask("Loading...", 'x-mask-loading');
                getExportJobs(p);
            },
            deactivate: function() {
            }
        },
        collapsible: true                       
    });

    /**
     * panel to display list of jobs belong to a user
     * @type {Ext.Panel}
     */
    analysisJobsPanel = new Ext.Panel({
        id: 'analysisJobsPanel',
        title: 'Analysis Jobs',
        region: 'center',
        split: true,
        height: 90,
        layout: 'fit',
        listeners: {
            activate: function(p) {
                getJobsData(p);
            }
        },
        collapsible: true
    });

    workspacePanel = new Ext.Panel({
        id: 'workspacePanel',
        title: 'Workspace',
        region: 'center',
        split: true,
        height: 90,
        layout: 'fit',
        autoScroll: false,
        listeners: {
            activate: function(p) {
                renderWorkspace(p);
            },
            deactivate: function() {

            }
        },
        collapsible: true
    });

    sampleExplorerPanel = new Ext.Panel({
        id: "sampleExplorer",
        title:"Sample Details",
        layout: "fit",
        listeners: {
            activate: function(p) {
                p.body.mask("Loading...", 'x-mask-loading');
                generatePatientSampleCohort(launchSampleBrowseWithCohort);
            }
        }
    });

    resultsTabPanel.add(queryPanel);
    resultsTabPanel.add(analysisPanel);
    resultsTabPanel.add(analysisGridPanel);
    resultsTabPanel.add(dataAssociationPanel);
    resultsTabPanel.add(analysisDataExportPanel);
    resultsTabPanel.add(analysisExportJobsPanel);
    resultsTabPanel.add(analysisJobsPanel);
    resultsTabPanel.add(workspacePanel);

    if (GLOBAL.sampleExplorerEnabled) {
        resultsTabPanel.add(sampleExplorerPanel);
    }

    function loadResources(resources, bootstrap) {
        var scripts = [];
        for (var i = 0, iLength = resources.length; i < iLength; i++) {
            var aFile = resources[i];
            if (aFile.type === 'script') {
                scripts.push(aFile.path);
            } else if (aFile.type === 'stylesheet') {
                dynamicLoad.loadCSS(aFile.path);
            }
        }
        if (scripts.length > 0) {
            dynamicLoad.loadScriptsSequential(scripts, bootstrap);
        } else {
            bootstrap();
        }
    }

    function loadResourcesByUrl(url, bootstrap) {
        return jQuery.post(url, function(data) {
            if (data.success) {
                loadResources(data.files, bootstrap);
            }
        }, "json").fail(function() {
            console.error("Cannot load resources for " + url);
        });
    }

    function loadPlugin(pluginName, scriptsUrl, bootstrap) {
        var def = jQuery.Deferred();
        jQuery.post(pageInfo.basePath + "/pluginDetector/checkPlugin", {pluginName: pluginName}, function(data) {
            if (data === 'true') {
                loadResourcesByUrl(pageInfo.basePath + scriptsUrl, function() {
                    bootstrap();
                    def.resolve();
                }).fail(def.reject);
            } else {
                def.reject();
            }
        }).fail(def.reject);

        return def;
    }

    // DALLIANCE
    // =======
    loadPlugin('dalliance-plugin', "/Dalliance/loadScripts", function () {
        loadDalliance(resultsTabPanel);
    }).always(function () {
        // Keep loading order to prevent tabs shuffling
        if (GLOBAL.metacoreAnalyticsEnabled) {
            loadPlugin('transmart-metacore-plugin', "/MetacoreEnrichment/loadScripts", function () {
                loadMetaCoreEnrichment(resultsTabPanel);
            });
        }
    });

    loadPlugin('smartR', "/SmartR/loadScripts", function () {
       resultsTabPanel.add(smartRPanel); 
    });
  
    if (GLOBAL.galaxyEnabled === 'true') {
       resultsTabPanel.add(GalaxyPanel);
    }

    southCenterPanel = new Ext.Panel({
        id: 'southCenterPanel',
        region: 'center',
        layout: 'border',
        split: true,
        tbar: tb2
    });

    exportPanel = new Ext.Panel({
        id: 'exportPanel',
        title: 'Compare/Export',
        region: 'east',
        html: '<div style="text-align:center;font:12pt arial;width:100%;height:100%;"><table style="width:100%;height:100%;"><tr><td align="center" valign="center">Drag subsets to this panel to compare and export them</td></tr></table></div>',
        split: true,
        width: 300,
        height: 90,
        buttons: [
            {
                text: 'Compare',
                handler: function () {
                    var subsets = exportPanel.body.dom.childNodes;
                    if (subsets.length !== 2) {
                        alert("Must have two subsets!");
                    } else { 
                        showCompareStepPathwaySelection(); 
                    }
                }
            },
            {
                text: 'Export',
                iconCls: 'exportbutton',
                handler: function () {
                    showExportStepSplitTimeSeries();
                }
            },
            {
                text: 'Clear',
                iconCls: 'clearbutton',
                handler: function () {
                    clearExportPanel();
                }
            }

        ]
    });

    var treetitle = "Previous Queries";

    if (GLOBAL.Config === 'jj') {
        treetitle = "Subsets";
    }

    var Tree = Ext.tree;
    prevTree = new Tree.TreePanel({
        id: 'previousQueriesTree',
        title: treetitle,
        animate: false,
        autoScroll: true,
        enableDrag: true,
        ddGroup: 'makeQuery',
        containerScroll: true,
        enableDrop: false,
        region: 'south',
        rootVisible: false,
        expanded: true,
        split: true,
        height: 300
    });

    prevTreeRoot = new Tree.TreeNode({
        text: 'root',
        draggable: false,
        id: 'prevroot',
        qtip: 'root'
    });

    prevTree.setRootNode(prevTreeRoot);

    /**********new prototype*********/

    centerPanel.add(resultsTabPanel);
    /********************************/

    westPanel.add(createOntPanel());
    centerMainPanel.add(westPanel);
    centerMainPanel.add(centerPanel);

    viewport = new Ext.Viewport({
        layout: 'border',
        items: [centerMainPanel],
        listeners: {
            'afterLayout': {
                fn: function (el) {
                    onWindowResize();
                }
            }
        }
    });

    Ext.get(document.body).addListener('contextmenu', contextMenuPressed);

    // preload the setvalue dialog
    setvaluePanel = new Ext.Panel({
        id: 'setvaluePanel',
        region: 'north',
        height: 120,
        width: 490,
        split: false,
        autoLoad: {
            url: pageInfo.basePath+'/panels/setValueDialog.html',
            scripts: true,
            nocache: true,
            discardUrl: true,
            method: 'POST'
        }
    });

    setvaluechartsPanel1 = new Ext.Panel({
        id: 'setvaluechartsPanel1',
        region: 'center',
        width: 245,
        height: 180,
        split: false
    });

    setvaluechartsPanel2 = new Ext.Panel({
        id: 'setvaluechartsPanel2',
        region: 'east',
        width: 245,
        height: 180,
        split: false
    });

    // preload the setvalue dialog
    if (!this.setvaluewin) {
        setvaluewin = new Ext.Window({
            id: 'setValueWindow',
            title: 'Set Value',
            layout: 'border',
            width: 500,
            height: 240,
            closable: false,
            plain: true,
            modal: true,
            border: false,
            items: [setvaluePanel , setvaluechartsPanel1, setvaluechartsPanel2],
            buttons: [
                {
                    text: 'Show Histogram',
                    handler: function () {
                        showConceptDistributionHistogram();
                    }
                },
                {
                    text: 'Show Histogram for subset',
                    handler: function () {
                        var subset;
                        if (selectedConcept.parentNode.id === "hiddenDragDiv") {
                            subset = getSubsetFromPanel(STATE.Target);
                        } else {
                            subset = getSubsetFromPanel(selectedConcept.parentNode);
                        }

                        if (!isSubsetEmpty(subset)) {
                            runQuery(subset, showConceptDistributionHistogramForSubset);
                        } else { 
                            alert('Subset is empty!');
                        }
                    }
                },
                {
                    text: 'OK',
                    handler: function () {
                       var mode = getSelected(document.getElementsByName("setValueMethod"))[0].value;
                       var highvalue = document.getElementById("setValueHighValue").value;
                       var lowvalue = document.getElementById("setValueLowValue").value;
                       var units = document.getElementById("setValueUnits").value;
                       var operator = document.getElementById("setValueOperator").value;
                       var highlowselect = document.getElementById("setValueHighLowSelect").value;

                       // make sure that there is a value set
                       if (mode === "numeric" && operator === "BETWEEN" && (highvalue === "" || lowvalue === "")) {
                           alert('You must specify a low and a high value.');
                       } else if (mode === "numeric" && lowvalue === "") {
                           alert('You must specify a value.');
                       } else {
                           setvaluewin.hide();
                           setValueDialogComplete(mode, operator, highlowselect, highvalue, lowvalue, units);
                       }
                    }
                },
                {
                    text: 'Cancel',
                    handler: function () {
                        setvaluewin.hide();
                    }
                }
            ],
            resizable: false,
            tools: [
                {
                    id:'help',
                    qtip:'Click for context sensitive help',
                    handler: function(event, toolEl, panel) {
                        D2H_ShowHelp("1239", helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP);
                    }
                }
            ]
        });

        setvaluewin.show();
        setvaluewin.hide();
    }

    showLoginDialog();
    var h = queryPanel.header;
});

function onWindowResize() {
    //Assorted hackery for accounting for the presence of the toolbar
    var windowHeight = jQuery(window).height();

    jQuery('#centerMainPanel').css('top', jQuery('#header-div').height());

    var boxHeight = jQuery('#box-search').height();
    jQuery('#navigateTermsPanel .x-panel-body').height(windowHeight - boxHeight - 90);

    jQuery('#analysisPanel .x-panel-body').height(jQuery(window).height() - 65);

    if (jQuery('#dataTypesGridPanel .x-panel-body').size() > 0) {
        var exportPanelTop = jQuery('#dataTypesGridPanel .x-panel-body').offset().top;
        jQuery('#dataTypesGridPanel .x-panel-body').height(jQuery(window).height() - exportPanelTop - 40);
    }

    if (jQuery('#resultsTabPanel .x-tab-panel-body').size() > 0) {
        var panelTop = jQuery('#resultsTabPanel .x-tab-panel-body').offset().top;
        jQuery('#resultsTabPanel .x-tab-panel-body').height(jQuery(window).height() - panelTop);
    }

    jQuery('#subsets_wrapper').find('div.dataTables_scrollBody').css("height", calcWorkspaceDataTableHeight() + "px");
    jQuery('#reports_wrapper').find('div.dataTables_scrollBody').css("height", calcWorkspaceDataTableHeight() + "px");
}

/*
This function will make a quick call to the server to check
a session variable that if set indicates that it is OK
to export the datasets since the user ran the one of the
heatmap options and loaded the gene expression data
*/
function exportDataSets() {
    Ext.get("exportdsform").dom.submit();
}

function hasMultipleTimeSeries() {
    return true;
}

function createOntPanel() {
    // make tab panel, search panel, ontTree and combine them
    ontTabPanel = new Ext.Panel({
        id: 'ontPanel',
        region: 'center',
        defaults: {
            hideMode: 'offsets'
        },
        collapsible: false,
        height: 300,
        width: 250,
        deferredRender: false,
        split: true
    });

    ontSearchByCodePanel = new Ext.Panel({
        id: 'searchByCodePanel',
        title: 'Search by Codes',
        region: 'center'
    });

    searchByNamePanel = new Ext.Panel({
        title: 'Search by Names',
        id: 'searchByNamePanel',
        region: 'center',
        height: 500,
        width: 250,
        border: true,
        bodyStyle: 'background:lightgrey;',
        layout: 'border',
        split: true
    });

    // make the ontSearchByNamePanel
    shtml = '<table style="font:10pt arial;"><tr><td><select id="searchByNameSelect"><option value="left">Starting with</option><option value="right">Ending with</option>' +
        '<option value="contains" selected>Containing</option><option value="exact">Exact</option></select>&nbsp;&nbsp;</td><td><input id="searchByNameInput" onkeypress="if(enterWasPressed(event)){searchByName();}" type="text" size="15">&nbsp;</td>' +
        '<td><button onclick="searchByName()">Find</button></td></tr><tr><td colspan="2">Select Ontology:<select id="searchByNameSelectOntology"></select></td></tr></table>';

    searchByNameForm = new Ext.Panel({
        id: 'searchByNameForm',
        region: 'north',
        bodyStyle: 'background:#eee;padding: 10px;',
        html: shtml,
        height: 70,
        border: true,
        split: false
    });

    // shorthand
    var Tree = Ext.tree;

    searchByNameTree = new Tree.TreePanel({
        id: 'searchByNameTree',
        animate: false,
        autoScroll: true,
        loader: new Ext.ux.OntologyTreeLoader({
            dataUrl: 'none'
        }),
        enableDrag: true,
        ddGroup: 'makeQuery',
        containerScroll: true,
        enableDrop: false,
        region: 'center',
        rootVisible: false,
        expanded: true,
        split: true,
        border: true,
        height: 400
    });

    searchByNameTreeRoot = new Tree.TreeNode({
        text: 'root',
        draggable: false,
        id: 'root',
        qtip: 'root'
    });
    // add a tree sorter in folder mode
    new Tree.TreeSorter(searchByNameTree, {
        folderSort: true
    });

    searchByNameTree.setRootNode(searchByNameTreeRoot);
    searchByNamePanel.add(searchByNameForm);
    searchByNamePanel.add(searchByNameTree);

    //******************************************************************************
    // FILTER PANEL
    //******************************************************************************
    var showFn = function(node, e) {
        Ext.tree.TreePanel.superclass.onShow.call(this);
    };

    // shorthand
    Tree = Ext.tree;

    ontFilterTree = new Tree.TreePanel({
        id: 'ontFilterTree',
        animate: false,
        autoScroll: true,
        loader: new Ext.ux.OntologyTreeLoader({
            dataUrl: 'none'
        }),
        enableDrag: true,
        ddGroup: 'makeQuery',
        containerScroll: true,
        enableDrop: false,
        region: 'center',
        rootVisible: false,
        expanded: true,
        border: true,
        height: 400
    });

    ontFilterTreeRoot = new Tree.TreeNode({
        text: 'root',
        draggable: false,
        id: 'root',
        qtip: 'root'
    });

    // add a tree sorter in folder mode
    new Tree.TreeSorter(ontFilterTree, {
        folderSort: true
    });

    ontFilterTree.setRootNode(ontFilterTreeRoot);

    setupOntTree('navigateTermsPanel', 'Navigate Terms');

    return ontTabPanel;
}

function closeBrowser() {
    window.open('http://www.i2b2.org', '_self', '');
    window.close();
}

function showLoginDialog() {

    loginwin = new Ext.Window({
        id: 'loginWindow',
        title: 'Login',
        layout: 'fit',
        width: 350,
        height: 140,
        closable: false,
        plain: true,
        modal: true,
        border: false,
        resizable: false
    });

    var txtboxdomain = new Ext.form.TextField({
        fieldLabel: 'Domain',
        id: 'txtFieldDomain',
        name: 'domain'
    });

    var txtboxusername = new Ext.form.TextField({
        fieldLabel: 'Username',
        name: 'username'
    });

    txtboxpassword = new Ext.form.TextField({
        fieldLabel: 'Password',
        name: 'password',
        inputType: 'password'
    });

    loginform = new Ext.FormPanel({
        id: 'loginForm',
        labelWidth: 75,
        frame: true,
        region: 'center',
        width: 350,
        height: 130,
        defaults: {
            width: 230
        },
        defaultType: 'textfield',
        items: [txtboxusername, txtboxpassword],
        buttons: [
            {
                text: 'Login',
                handler: function () {
                    loginform.el.mask('Logging in...', 'x-mask-loading');
                    login(txtboxdomain.getValue(), txtboxusername.getValue(), txtboxpassword.getValue());
                }
            },
            {
                text: 'Cancel',
                handler: closeBrowser
            }
        ]
    });

    if (GLOBAL.AutoLogin) {
        login(GLOBAL.Domain, GLOBAL.Username, GLOBAL.Password);
    } else {
        loginwin.add(loginform);
        loginwin.show(viewport);

        txtboxpassword.getEl().addListener('keypress', function (e) {
            if (enterWasPressed(e)) {
                loginform.el.mask('Logging in...', 'x-mask-loading');
                login(txtboxdomain.getValue(), txtboxusername.getValue(), txtboxpassword.getValue());
            }
        });
    }
}

function login(domain, username, password) {
    GLOBAL.Domain = domain;
    GLOBAL.Username = username;
    GLOBAL.Password = password;
    loginComplete();
}

function loginComplete() {
    if (loginform.isVisible()) {
        loginform.el.unmask();
        loginwin.hide();
    }

    projectDialogComplete();

    // Login GenePattern server. The login process should be completed by the time a user starts GenePattern tasks.
    genePatternLogin();
}

function projectDialogComplete() {
    jQuery('#box-search').prependTo(jQuery('#westPanel')).show();
    jQuery('#noAnalyzeResults').prependTo(jQuery('#navigateTermsPanel .x-panel-body'));

    //Now that the ont tree has been set up, call the initial search
    showSearchResults();

    if (GLOBAL.RestoreComparison) {
        refillQueryPanels ({
            1: GLOBAL.RestoreQID1,
            2: GLOBAL.RestoreQID2
        })
    }
    if (GLOBAL.Tokens.indexOf("EXPORT") === -1 && !GLOBAL.IsAdmin) {
        //Ext.getCmp("exportbutton").disable();
    }
}

function getCategoriesComplete(ontresponse) {
    getSubCategories(ontresponse);
}

function setupOntTree(id_in, title_in) {

    var Tree = Ext.tree;

    var showFn = function (node, e) {
        Ext.tree.TreePanel.superclass.onShow.call(this);
    };

    var ontTree = new Tree.TreePanel({
        id: id_in,
        title: title_in,
        animate: false,
        autoScroll: true,
        loader: new Ext.ux.OntologyTreeLoader({
            dataUrl: 'none'
        }),
        enableDrag: true,
        ddGroup: 'makeQuery',
        listeners: {
            startdrag: function(e) {
                jQuery("#queryPanel .panelBoxListPlaceholder .holder").addClass('highlight')
            },
            enddrag: function(e) {
                jQuery("#queryPanel .panelBoxListPlaceholder .holder").removeClass('highlight')
            }
        },
        containerScroll: true,
        enableDrop: false,
        region: 'center',
        rootVisible: false,
        expanded: true,
        onShow: showFn
    });

    ontTree.on('startdrag', function (panel, node, event) {
        Ext.ux.ManagedIFrame.Manager.showShims();
    });

    ontTree.on('enddrag', function (panel, node, event) {
        Ext.ux.ManagedIFrame.Manager.hideShims();
    });

    new Tree.TreeSorter(ontTree, {
        folderSort : true,
        sortType: function(node) {
            if (node.attributes.tablename === "MODIFIER_DIMENSION" ) {
                return "A" + node.text
            } else {
                return "B" + node.text
            }
        }
    });
    
    ontTree.on('beforecollapsenode', function (node, deep, anim) {
        Ext.Ajax.request({
            url: removeNodeDseURL + "?node=" + node.id,
            method: 'POST',
            success: function (result, request) {
            },
            failure: function (result, request) {
                console.error(result);
            },
            timeout: '600000'
        });
    });

    var firstExpandProgram = [];

    ontTree.on('beforeexpandnode', function (node, deep, anim) {
        var expand = true;
        if (GLOBAL.PathToExpand !== undefined && GLOBAL.PathToExpand.indexOf(node.id) > -1 && node.parentNode.id === "treeRoot" && !contains(dseClosedNodes, node.id)) {
            if (firstExpandProgram.indexOf(node.id) === -1) {
                firstExpandProgram.push(node.id);
                expand = false;
            }
        }

        if (expand) {
            Ext.Ajax.request({
                url: addNodeDseURL + "?node=" + node.id,
                method: 'POST',
                success: function (result, request) {
                },
                failure: function (result, request) {
                    console.log(result);
                },
                timeout: '600000'
            });
        }
    });

    var treeRoot = new Tree.TreeNode({
        text: 'root',
        draggable: false,
        id: 'treeRoot',
        qtip: 'root'
    });

    // add a tree sorter in folder mode
    new Tree.TreeSorter(ontTree, {
        folderSort: true
    });

    ontTree.setRootNode(treeRoot);
    ontTabPanel.add(ontTree);
    ontTabPanel.doLayout();
    onWindowResize();
}

function createTree(ontresponse) {
    // shorthand
    var Tree = Ext.tree;
    var ontRoots = [];

    if (GLOBAL.DefaultPathToExpand !== "") {
        GLOBAL.PathToExpand += GLOBAL.DefaultPathToExpand + ",";
    }

    var treeRoot = new Tree.TreeNode({
        text: 'root',
        draggable: false,
        id: 'treeRoot',
        qtip: 'root'
    });

    for (var c = 0, cLength = ontresponse.length; c < cLength; c++) {
        var level = ontresponse[c].level;
        var key = ontresponse[c].key;
        var name = ontresponse[c].name;
        var tooltip = ontresponse[c].tooltip;
        var dimcode = ontresponse[c].dimcode;
        var visualAttributes = ontresponse[c].visualAttributes;
        var fullname = key.substr(key.indexOf("\\", 2), key.length);
        var access = GLOBAL.InitialSecurity[fullname];

        // set the root node
        var autoExpand = false;
        var lockedNode = true;

        if ((access !== undefined && access !== 'Locked') || GLOBAL.IsAdmin) {
            lockedNode = false;
        }
        if (lockedNode && key.indexOf('\\\\xtrials\\') === 0) {
            // across trial nodes should never be locked
            lockedNode = false;
        }

        if (GLOBAL.PathToExpand.indexOf(key) > -1 && GLOBAL.UniqueLeaves.indexOf(key + ",") === -1 && !lockedNode) {
            autoExpand = true;
        }

        //For search results - if the node level is 1 (study) or below and it doesn't appear in the search results, filter it out.
        if (level <= '1' && GLOBAL.PathToExpand !== '' && GLOBAL.PathToExpand.indexOf(key) === -1) {
            continue;
        }

        var iconCls = "";

        if (visualAttributes.indexOf('PROGRAM') !== -1) {
            iconCls="programicon";
        }

        var tcls = "";

        if (lockedNode) {
            tcls += ' locked';
        }

        var isSearchResult = (GLOBAL.PathToExpand.indexOf(key + ",") > -1);
        if (isSearchResult) {
            tcls += ' searchResultNode';
        }

        var expand = ((contains(dseOpenedNodes, key)) || autoExpand) && (!contains(dseClosedNodes, key));

        var ontRoot = new Tree.AsyncTreeNode({
            text: name,
            draggable: false,
            id: key,
            qtip: tooltip,
            expanded: expand,
            iconCls: iconCls,
            cls: tcls
        });
        
        if (lockedNode) {
            ontRoot.attributes.access = 'locked';
            ontRoot.on('beforeload', function (node) {
                return false;
            });
        }

        ontRoots.push(ontRoot);

        /*****************************************/
        }

    return ontRoots;
}

/*
 * the id_in drives which off these tabs is created
 * 
 */
function getSubCategories(ontresponse) {
    // shorthand
    var Tree = Ext.tree;
    var showFn;
    var ontRoots = createTree(ontresponse);
    var toolbar = new Ext.Toolbar([
        {
            id:'contextHelp-button',
            handler: function(event, toolEl, panel) {
                D2H_ShowHelp((id_in === "navigateTermsPanel") ? "1066": "1091",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP);
            },
            iconCls: "contextHelpBtn"  
        }
    ]);
    
    var treeRoot = Ext.getCmp('navigateTermsPanel').getRootNode();
    for (c = treeRoot.childNodes.length - 1; c >= 0; c--) {
        treeRoot.childNodes[c].remove();
    }

    jQuery('#noAnalyzeResults').hide();

    for (var c = 0, cLength = ontRoots.length; c < cLength; c++) {
        var newnode = ontRoots[c];
        treeRoot.appendChild(newnode);
    }

    if (ontRoots.length === 0) { //This shouldn't happen!
        jQuery('#noAnalyzeResults').show();
    }

    if (GLOBAL.Debug) {
        alert(ontresponse.responseText);
    }

    onWindowResize();
}

function setupDragAndDrop() {

    // Drag and drop for panels is now setup in querypanel.js

    /* Set up Drag and Drop for the analysis Panel */
    var qcd = Ext.get(analysisPanel.body);

    dts = new Ext.dd.DropTarget(qcd, {
        ddGroup: 'makeQuery'
    });

    dts.notifyDrop = function (source, e, data) {
        buildAnalysis(data.node);
        return true;
    };

    /* set up drag and drop for grid */
    var mcd = Ext.get(analysisGridPanel.body);
    dtg = new Ext.dd.DropTarget(mcd, {
        ddGroup: 'makeQuery'
    });

    dtg.notifyDrop = function (source, e, data) {
        buildAnalysis(data.node);
        return true;
    };
}

function getValue(node, defaultvalue) {
    var result = defaultvalue;
    if (node.size() > 0) {
        result = node.first().html();
    }
    return result;
}

function ontologyRightClick(eventNode, event) {
    if (!this.contextMenuOntology) {
        this.contextMenuOntology = new Ext.menu.Menu({
            id: 'contextMenuOntology',
            items: [
                {
                    text: 'Show Definition', handler: function () {
                        showConceptInfoDialog(eventNode.attributes.id, eventNode.attributes.text, eventNode.attributes.comment);
                    }
                }
            ]
        });
    }
    var xy = event.getXY();
    this.contextMenuOntology.showAt(xy);
    return false;
}

function showConceptInfoDialog(conceptKey, conceptid, conceptcomment) {

    if (!this.conceptinfowin) {
        var link = '<a href="javascript:;"  onclick="return popitup(\'http://www.google.com/search?q='+conceptid+'\')">Search for more information...</a>';
        conceptinfowin = new Ext.Window({
            id: 'showConceptInfoWindow',
            title: 'Show Concept Definition-' + conceptid,
            layout: 'fit',
            width: 600,
            height: 500,
            closable: false,
            plain: true,
            modal: true,
            border: false,
            autoScroll: true,
            buttons: [
                {
                    text: 'Close',
                    handler: function () {
                        conceptinfowin.hide();
                    }
                }
            ],
            resizable: false
        });
    }

    conceptinfowin.show(viewport);
    conceptinfowin.header.update("Show Concept Definition-" + conceptid);
    Ext.get(conceptinfowin.body.id).update(conceptcomment);

    conceptinfowin.load({
        url: pageInfo.basePath+"/ontology/showConceptDefinition",
        params: {conceptKey: conceptKey}, // or a URL encoded string     
        discardUrl: true,
        nocache: true,
        text: "Loading...",
        timeout: 30000,
        scripts: false
    });
}

function showExportStepSplitTimeSeries() {

    if (!this.exportStepSplitTimeSeries) {
        exportStepSplitTimeSeries = new Ext.Window({
            id: 'exportStepSplitTimeSeriesWindow',
            title: 'Export-Split Time Series',
            layout: 'fit',
            width: 400,
            height: 200,
            closable: false,
            plain: true,
            modal: true,
            border: false,
            buttons: [
                {
                    id: 'exportStepSplitTimeSeriesNextButton',
                    text: 'Next>',
                    disabled: true,
                    handler: function () {
                        exportStepSplitTimeSeries.hide();
                        showExportStepDataSelection();
                    }
                },
                {
                    text: 'Cancel',
                    handler: function () {
                        exportStepSplitTimeSeries.hide();
                    }
                }
            ],
            resizable: false ,
            autoLoad: {
                url: pageInfo.basePath+'/panels/exportStepSplitTimeSeries.html',
                scripts: true,
                nocache: true,
                discardUrl: true,
                method: 'POST'
            }
        });
    }

    exportStepSplitTimeSeries.show(viewport);
}

function showExportStepDataSelection() {
    if (!this.exportStepDataSelection) {
        exportStepDataSelection = new Ext.Window({
            id: 'exportStepDataSelectionWindow',
            title: 'Export-Data Selection',
            layout: 'fit',
            width: 400,
            height: 400,
            closable: false,
            plain: true,
            modal: true,
            border: false,
            buttons: [
                {
                    id: 'exportStepDataSelectionAdvancedButton',
                    text: 'Advanced',
                    handler: function () {
                        showExportDataSelectionAdvanced();
                    }
               },
               {
                    id: 'exportStepDataSelectionNextButton',
                    text: 'Get Data',
                    disabled: true,
                    handler: function () {
                        getExportData();
                    }
               },
               {
                    text: 'Cancel',
                    handler: function () {
                        exportStepDataSelection.hide();
                    }
                }
            ],
            resizable: false,
            autoLoad: {
                url: pageInfo.basePath+'/panels/exportStepDataSelection.html',
                scripts: true,
                nocache: true,
                discardUrl: true,
                method: 'POST'
            }
        });
    }
    exportStepDataSelection.show(viewport);
}

function getExportData() {
    exportStepDataSelection.getEl().mask("Getting Data...");
    setTimeout('exportDataFinished();', 2000);
}

function showExportStepProgress() {
    if (!this.exportStepProgress) {
        exportStepProgress = new Ext.Window({
            id: 'exportStepProgress',
            title: 'Export-Download File',
            layout: 'fit',
            html: '<br><div style="font:12pt arial;width:100%;height:100%;text-align:center;vertical-align:middle"><a href="export/export.xls">Download File</a></div>',
            width: 400,
            height: 200,
            closable: false,
            plain: true,
            modal: true,
            border: false,
            buttons: [
                {
                    text: 'Done',
                    handler: function () {
                        exportStepProgress.hide();
                    }
                }
           ],
           resizable: false
        });
    }

    exportStepProgress.show(viewport);
}

function exportDataFinished() {
    exportStepDataSelection.getEl().unmask();
    exportStepDataSelection.hide();
    showExportStepProgress();
}

function runAllQueries(callback, panel) {

    if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
        if (panel) {
            panel.body.unmask();
        }
        Ext.Msg.alert('Subsets are empty', 'All subsets are empty. Please select subsets.');
    }

    // setup the number of subsets that need running
    var subsetstorun = 0;
    for (var i = 1; i <= GLOBAL.NumOfSubsets; i++) {
        if (!isSubsetEmpty(i)) {
            subsetstorun ++ ;
        }
    }

    /* set the number of requests before callback is fired for runquery complete */
    STATE.QueryRequestCounter = subsetstorun;

    // init panel's subset query array if it's not existing yet
    if (panel) {
        panel.subsetQueries = panel.subsetQueries ? panel.subsetQueries : ["", "", ""];
    }

    // iterate through all subsets calling the ones that need to be run
    for (var j = 1; j <= GLOBAL.NumOfSubsets; j++) {
        if (!isSubsetEmpty(j)) {
            if (panel) {
                panel.subsetQueries[j] = getSubsetQuery(j); // set subset queries to the selected tab
            }
            runQuery(j, callback);
        }
    }
}

/**
 * Check if there're any changes in both subsets
 * @returns {boolean}
 */
function isSubsetQueriesChanged(referenceQueries) {
    var retVal = false;

    for (var i = 1; i <= GLOBAL.NumOfSubsets; i++) {

        // get fresh subset query
        var _newQuery = getSubsetQuery(i);

        if (referenceQueries) {
            // check if reference query is the same as the new query
            // return true if it's changed.
            retVal = referenceQueries[i] !== _newQuery ? true : false;
        }

        if (retVal) {
            break;
        }
    }
    return retVal;
}

function runQuery(subset, callback) {
    if (Ext.get('analysisPanelSubset1') === null) {
        // analysisPanel.body.update("<table border='1' width='100%' height='100%'><tr><td width='50%'><div id='analysisPanelSubset1'></div></td><td><div id='analysisPanelSubset2'></div></td></tr>");
    }

    var query = getQuery(subset)[0].outerHTML

    // first subset
    if (setvaluewin.hidden) {
        queryPanel.el.mask('Getting subset ' + subset + '...', 'x-mask-loading');
    }
        
    Ext.Ajax.request({
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
    });

    if (GLOBAL.Debug) {
        resultsPanel.setBody("<div style='height:400px;width500px;overflow:auto;'>" + Ext.util.Format.htmlEncode(query) + "</div>");
    }
}

function runQueryComplete(result, subset, callback) {
    var jsonRes = jQuery.parseJSON(result.responseText);
    var error;

    if (result.status !== 200) {
        error = jsonRes.message;
    } else if (jsonRes.errorMessage !== null) {
        error = jsonRes.errorMessage;
    }

    queryPanel.el.unmask();

    if (error) {
        Ext.Msg.show({
            title: 'Error generating patient set',
            msg: error,
            buttons: Ext.Msg.OK,
            fn: function () {
                Ext.Msg.hide();
            },
            icon: Ext.MessageBox.ERROR
        });
    }

    // Current code requires us to set CurrentSubsetIDs regardless of error status...
    GLOBAL.CurrentSubsetIDs[subset] = jsonRes.id ? jsonRes.id : -1;

    // Save query to global variable
    GLOBAL.CurrentSubsetQueries[subset] = getSubsetQuery(subset);

    if (subset === null) { // if single subset
        callback(GLOBAL.CurrentSubsetIDs[subset]);
    } else {

        if (STATE.QueryRequestCounter > 0) { // I'm in a chain of requests so decrement
            STATE.QueryRequestCounter = --STATE.QueryRequestCounter;
        }

        if (STATE.QueryRequestCounter === 0) {
            callback();
        }
    }
}

function buildAnalysis(nodein) {
    var node = nodein;
    if (isSubsetEmpty(1) && isSubsetEmpty(2)) {
        alert('Empty subsets found, need a valid subset to analyze!');
        return;
    }

    if ((GLOBAL.CurrentSubsetIDs[1] === null && !isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] === null && !isSubsetEmpty(2))) {
        runAllQueries(function () {
            buildAnalysis(node);
        });

        return;
    }

    resultsTabPanel.body.mask("Running analysis...", 'x-mask-loading');

    Ext.Ajax.request({
        url: pageInfo.basePath + "/chart/analysis",
        method: 'POST',
        timeout: '600000',
        params: Ext.urlEncode({
            charttype: "analysis",
            concept_key: node.attributes.id,
            result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
            result_instance_id2: GLOBAL.CurrentSubsetIDs[2]
        }), // or a URL encoded string
        success: function (result, request) {
            buildAnalysisComplete(result);
            resultsTabPanel.body.unmask();
        },
        failure: function (result, request) {
            alert("A problem arose while trying to retrieve the results")
            resultsTabPanel.body.unmask();
        }
    });
    getAnalysisGridData(node.attributes.id);
}

function buildAnalysisComplete(result) {
    // analysisPanel.body.unmask();
    var txt = result.responseText;
    updateAnalysisPanel(txt, true);
}

function updateAnalysisPanel(html, insert) {
    if (insert) {
        var div = jQuery("#analysisPanel div.analysis");
        var uniq = 'appenedItem_' + new Date().getTime();
        div.append(jQuery(html).attr('id', uniq));
        div.parent().scrollTop(jQuery('#' + uniq).prop('offsetTop'));
    } else {
        analysisPanel.body.update(html, false, null);
    }
}

function searchByNameComplete(response) {
    // shorthand
    var length;
    var Tree = Ext.tree;
    searchByNameTree.el.unmask();
    var allkeys="";
    var concepts = response.responseXML.selectNodes('//concept');
    if (concepts !== undefined) {
        if (concepts.length < GLOBAL.MaxSearchResults) {
            length = concepts.length;
        } else {
            length = GLOBAL.MaxSearchResults;
        }

        for (var c = 0; c < length; c++) {
            searchByNameTreeRoot.appendChild(getTreeNodeFromXMLNode(concepts[c]));
            var key=concepts[c].selectSingleNode('key').firstChild.nodeValue;
            if (allkeys !== "") {
                allkeys = allkeys + ",";
            }

            allkeys = allkeys+key;
        }
    }
    Ext.Ajax.request({
        url: pageInfo.basePath+"/ontology/sectest",
        method: 'POST',
        success: function (result, request) {
        },
        failure: function (result, request) {
        },
        timeout: '300000',
        params: Ext.urlEncode({
            keys: allkeys
        }) // or a URL encoded string
    });
}

function enterWasPressed(e) {
    var pK;
    if (e.which) {
        pK = e.which;
    }
    if (pK === undefined && window.event) {
        pK = window.event.keyCode;
    }
    if (pK === undefined && e.getCharCode) {
        pK = e.getCharCode();
    }
    if (pK === 13) {
        return true;
    } else {
        return false;
    }
}

function contextMenuPressed(e) {
    var x = e;
    e.stopEvent();
    return false;
}

function getSelected(opt) {
    var selected = [];
    var index = 0;
    for (var intLoop = 0, oLength = opt.length; intLoop < oLength; intLoop++) {
        if (opt[intLoop].selected || opt[intLoop].checked) {
            index = selected.length;
            selected[index] = {};
            selected[index].value = opt[intLoop].value;
            selected[index].index = intLoop;
        }
    }

    return selected;
}

function outputSelected(opt) {
    var sel = getSelected(opt);
    var strSel = "";
    for (var intLoop = 0, sLength = sel.length; intLoop < sLength; intLoop++) {
        strSel += sel[intLoop].value + "\n";
    }
    alert("Selected Items:\n" + strSel);
}

/** 
 * Function to run the survival analysis asynchronously
 */
function showSurvivalAnalysis() {   
    if ((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] === null) || (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] === null)) {
        runAllQueries(showSurvivalAnalysis);
        return;
    }
    
    Ext.Ajax.request({                      
        url: pageInfo.basePath+"/asyncJob/createnewjob",
        method: 'POST',
        success: function(result, request) {
            RunSurvivalAnalysis(result, GLOBAL.CurrentSubsetIDs[1], GLOBAL.CurrentSubsetIDs[2], getQuerySummary(1), getQuerySummary(2));
        },
        failure: function(result, request) {
            Ext.Msg.alert('Status', 'Unable to create the heatmap job.');
        },
        timeout: '1800000',
        params: {jobType:  "Survival"}
    });
}

function genePatternReplacement() {
    Ext.Msg.alert('Work In Progress', 'Gene Pattern replacement');
}

//Once, we get a job created by GPController, we run the survival analysis
function RunSurvivalAnalysis(result, result_instance_id1, result_instance_id2, querySummary1, querySummary2) {
    var jobNameInfo = Ext.util.JSON.decode(result.responseText);                     
    var jobName = jobNameInfo.jobName;

    genePatternReplacement();
    showJobStatusWindow(result);    
    document.getElementById("gplogin").src = pageInfo.basePath + '/analysis/gplogin';   // log into GenePattern
    Ext.Ajax.request({                       
        url: pageInfo.basePath+"/genePattern/runsurvivalanalysis",
        method: 'POST',
        timeout: '1800000',
        params: {
            result_instance_id1: result_instance_id1,
            result_instance_id2: result_instance_id2,
            querySummary1: querySummary1,
            querySummary2: querySummary2,
            jobName: jobName
        }
    });

    checkJobStatus(jobName);
}

function showSNPViewerSelection() {
    
    if ((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] === null) || (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] === null)) {
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
                handler: function() {
                    if (Ext.get('snpViewChroms') === null) {
                        win.close();
                        return;
                    }
                    var ob = Ext.get('snpViewChroms').dom;
                    var selected = [];
                    for (var i = 0, iLength = ob.options.length; i < iLength; i++) {
                        if (ob.options[i].selected) {
                            selected.push(ob.options[i].value);
                        }
                    }

                    GLOBAL.CurrentChroms = selected.join(',');
                    getSNPViewer();
                    win.close();
                }
            },
            {
                text: 'Cancel',
                handler: function() {
                    win.close();
                }
            }
        ],
        resizable: false,
        autoLoad: {
            url: pageInfo.basePath+'/analysis/showSNPViewerSelection',
            scripts: true,
            nocache: true,
            discardUrl: true,
            method:'POST',
            params: {
                result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
                result_instance_id2: GLOBAL.CurrentSubsetIDs[2]
            }
        },
        tools: [
            {
                id: 'help',
                qtip: 'Click for context sensitive help',
                handler: function(event, toolEl, panel) {
                    D2H_ShowHelp("1360",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP);
                }
            }
        ]
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
    if (selectedGenesEltValue && selectedGenesEltValue.length !== 0) {
        selectedGeneStr = selectedGenesEltValue;
    }
    
    var geneAndIdListElt = Ext.get("selectedGenesAndIdSNPViewer");
    var geneAndIdListEltValue = geneAndIdListElt.dom.value;
    var geneAndIdListStr = "";
    if (geneAndIdListElt && geneAndIdListEltValue.length !== 0) {
        geneAndIdListStr = geneAndIdListEltValue;
    }
    
    var selectedSNPsElt = Ext.get("selectedSNPs");
    var selectedSNPsEltValue = selectedSNPsElt.dom.value;
    var selectedSNPsStr = "";
    if (selectedSNPsElt && selectedSNPsEltValue.length !== 0) {
        selectedSNPsStr = selectedSNPsEltValue;
    }
    //genePatternReplacement();
    Ext.Ajax.request({
        url: pageInfo.basePath+"/analysis/showSNPViewer",
        method: 'POST',
        success: function(result, request) {
            //getSNPViewerComplete(result);
        },
        failure: function(result, request) {
            //getSNPViewerComplete(result);
        },
        timeout: '1800000',
        params: { 
            result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
            result_instance_id2: GLOBAL.CurrentSubsetIDs[2],
            chroms: GLOBAL.CurrentChroms,
            genes: selectedGeneStr,
            geneAndIdList: geneAndIdListStr,
            snps: selectedSNPsStr
        }
    });
    
    showWorkflowStatusWindow();
}

function showIgvSelection() {
    
    if ((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] === null) || (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] === null)) {
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
                handler: function() {
                    if (Ext.get('igvChroms') === null) {
                        win.close();
                        return;
                    }

                    var ob = Ext.get('igvChroms').dom;
                    var selected = [];
                    for (var i = 0, iLength = ob.options.length; i < iLength; i++) {
                        if (ob.options[i].selected) {
                            selected.push(ob.options[i].value);
                        }
                    }

                    GLOBAL.CurrentChroms = selected.join(',');
                    getIgv();
                    win.close();
                }
            },
            {
                text: 'Cancel',
                handler: function() {
                win.close();
            }
            }
        ],
        resizable: false,
        autoLoad: {
            url: pageInfo.basePath+'/analysis/showIgvSelection',
            scripts: true,
            nocache:true,
            discardUrl:true,
            method:'POST',
            params: {
                result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
                result_instance_id2: GLOBAL.CurrentSubsetIDs[2]
            }
        },
        tools: [
            {
                id: 'help',
                qtip: 'Click for context sensitive help',
                handler: function(event, toolEl, panel) {
                    D2H_ShowHelp("1427",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP);
                }
            }
        ]
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
    if (selectedGenesEltValue && selectedGenesEltValue.length !== 0) {
        selectedGeneStr = selectedGenesEltValue;
    }
    
    var geneAndIdListElt = Ext.get("selectedGenesAndIdIgv");
    var geneAndIdListEltValue = geneAndIdListElt.dom.value;
    var geneAndIdListStr = "";
    if (geneAndIdListElt && geneAndIdListEltValue.length !== 0) {
        geneAndIdListStr = geneAndIdListEltValue;
    }
    
    var selectedSNPsElt = Ext.get("selectedSNPsIgv");
    var selectedSNPsEltValue = selectedSNPsElt.dom.value;
    var selectedSNPsStr = "";
    if (selectedSNPsElt && selectedSNPsEltValue.length !== 0) {
        selectedSNPsStr = selectedSNPsEltValue;
    }
    
    Ext.Ajax.request({
        url: pageInfo.basePath+"/analysis/showIgv",
        method: 'POST',
        success: function(result, request) {
            //getSNPViewerComplete(result);
        },
        failure: function(result, request) {
            //getSNPViewerComplete(result);
        },
        timeout: '1800000',
        params: { 
            result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
            result_instance_id2: GLOBAL.CurrentSubsetIDs[2],
            chroms: GLOBAL.CurrentChroms,
            genes: selectedGeneStr,
            geneAndIdList: geneAndIdListStr,
            snps: selectedSNPsStr
        }
    });
    
    showWorkflowStatusWindow();
}

function showPlinkSelection() {
    
    if ((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] === null) || (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] === null)) {
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
                handler: function() {
                    if (Ext.get('plinkChroms') === null) {
                        win.close();
                        return;
                    }
                    var ob = Ext.get('plinkChroms').dom;
                    var selected = [];
                    for (var i = 0, iLength = ob.options.length; i < iLength; i++) {
                        if (ob.options[i].selected) {
                            selected.push(ob.options[i].value);
                        }
                    }

                    GLOBAL.CurrentChroms=selected.join(',');
                    getPlink();
                    win.close();
                }
            },
            {
                text: 'Cancel',
                handler: function() {
                    win.close();
                }
            }
        ],
        resizable: false,
        autoLoad: {
            url: pageInfo.basePath+'/analysis/showPlinkSelection',
            scripts: true,
            nocache:true,
            discardUrl:true,
            method:'POST',
            params: {
                result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
                result_instance_id2: GLOBAL.CurrentSubsetIDs[2]
            }
        },
        tools: [
            {
                id: 'help',
                qtip: 'Click for context sensitive help',
                handler: function(event, toolEl, panel) {
                    // 1360 needs to be changed for PLINK
                    D2H_ShowHelp("1360",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP);
                }
            }
        ]
    });
    //  }
    win.show(viewport);
}

function getPlink() {

}

function showGwasSelection() {
    
    if ((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] === null) || (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] === null)) {
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
                handler: function() {
                    if (Ext.get('gwasChroms') === null) {
                        win.close();
                        return;
                    }
                    var ob = Ext.get('gwasChroms').dom;
                    var selected = [];
                    for (var i = 0, iLength = ob.options.length; i < iLength; i++) {
                        if (ob.options[i].selected) {
                            selected.push(ob.options[i].value);
                        }
                    }
                    GLOBAL.CurrentChroms=selected.join(',');
                    showGwas();
                    win.close();
                }
            },
            {
                text: 'Cancel',
                handler: function() {
                    win.close();
                }
            }
        ],
        resizable: false,
        autoLoad: {
            url: pageInfo.basePath+'/genePattern/showGwasSelection',
            scripts: true,
            nocache:true,
            discardUrl:true,
            method:'POST',
            params: {
                result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
                result_instance_id2: GLOBAL.CurrentSubsetIDs[2]
            }
        },
        tools: [
            {
                id:'help',
                qtip:'Click for context sensitive help',
                handler: function(event, toolEl, panel) {
                    // 1360 needs to be changed for PLINK
                    D2H_ShowHelp("1360",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP);
                }
            }
        ]
    });
    //  }
    win.show(viewport);
}

/** 
 * Function to run the GWAS asynchronously
 */
function showGwas() {   
    if ((!isSubsetEmpty(1) && GLOBAL.CurrentSubsetIDs[1] === null) || (!isSubsetEmpty(2) && GLOBAL.CurrentSubsetIDs[2] === null)) {
        runAllQueries(showGwas);
        return;
    }
    
    genePatternReplacement();
}

// After we get a job created by GPController, we run GWAS
function runGwas(result, result_instance_id1, result_instance_id2, querySummary1, querySummary2) {
    var jobNameInfo = Ext.util.JSON.decode(result.responseText);                     
    var jobName = jobNameInfo.jobName;

    genePatternReplacement();
}

function validateheatmapComplete(result) {
    var mobj = jQuery.parseJSON(result.responseText);
    GLOBAL.DefaultCohortInfo = mobj;

    showCompareStepPathwaySelection();
}

function compareSubsetsComplete(result, setname1, setname2) {
    viewport.el.unmask();
    if (!this.heatmapDisplay) {
        heatmapDisplay = new Ext.Window({
            id: 'heatmapDisplayWindow',
            title: 'Heatmap Comparison',
            layout: 'fit',
            width: 800,
            height: 600,
            closable: false,
            plain: true,
            modal: true,
            border: false,
            autoScroll: true,
            buttons: [
                {
                    id: 'Done',
                    text: 'OK',
                    handler: function () {
                        heatmapDisplay.hide();
                    }
                }
            ],
            resizable: true,
            html: '<div style="width:100%;height:100%;overflow:auto;"><div id="heatmapContainer"></div><br><div id="heatmapLegend"></div><div>'
        });
    }
    heatmapDisplay.show(viewport);

    var data = jsonToDataTable(result.responseText);

    var container = heatmapDisplay.body.dom;
    heatmap = new org.systemsbiology.visualization.BioHeatMap(document.getElementById('heatmapContainer'));
    
    heatmap.draw(data, {
        cellHeight: 5, 
        cellWidth: 5, 
        fontHeight: 3
    });

    var html = "s1=" + setname1 + "<br>s2=" + setname2;
    Ext.get("heatmapLegend").update(html);
}

function showNameQueryDialog() {
    if (!this.namequerywin) {
        namequerywin = new Ext.Window({
            id: 'namequeryWindow',
            title: 'Name the Query',
            layout: 'fit',
            width: 500,
            height: 150,
            closable: false,
            plain: true,
            modal: true,
            border: false,
            buttons: [
                {
                    text: 'OK',
                    handler: function () {
                        var newvalue = Ext.get("nameQueryDialogInput").getValue();
                        // Ext.get("txtBoxQueryName").dom.value = newvalue;
                        GLOBAL.CurrentQueryName = newvalue;
                        runQuery2();
                        namequerywin.hide();
                    }
                },
                {
                    text: 'Cancel',
                    handler: function () {
                        namequerywin.hide();
                    }
                }
           ],
           resizable: false,
           html: '<br>Query Name:&nbsp<input id="nameQueryDialogInput" type="text" size="50">'
        });
    }
    namequerywin.show(viewport);
    Ext.get("nameQueryDialogInput").dom.value = "";
    // clear out for next run
}

function jsonToDataTable(jsontext) {

    var table = eval("(" + jsontext + ")").table;
    var data = new google.visualization.DataTable();

    // convert to Google.DataTable
    // column
    for (var col = 0, cLength = table.cols.length; col < cLength; col++) {
        data.addColumn('string', table.cols[col].label);
    }
    // row
    for (var row = 0, rLength = table.rows.length; row < rLength; row++) {
        data.addRow();
        for (var column = 0, colLength = table.cols.length; column < colLength; column++) {
            data.setCell(row, column, table.rows[row][column].v);
        }
    }

    return data;
}

//////////////////////////////////////////////////////////////////////////////////////////////////////
// START: Advanced Heatmap Workflow methods
// Called from Run Workflow button in the Heatmap Validation window 
//////////////////////////////////////////////////////////////////////////////////////////////////////
// Once, we get a job created by GPController, we run the heatmap
function RunHeatMap(result, setid1, setid2, pathway, datatype, analysis, resulttype, nclusters, timepoints1, timepoints2, sample1, sample2, rbmPanels1, rbmPanels2) {
    var jobNameInfo = Ext.util.JSON.decode(result.responseText);                     
    var jobName = jobNameInfo.jobName;

    //genePatternReplacement();
    showJobStatusWindow(result);    
    genePatternLogin();
    Ext.Ajax.request({                       
        url: pageInfo.basePath+"/genePattern/runheatmap",
        method: 'POST',
        timeout: '1800000',
        params: {
            result_instance_id1:  setid1,
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
function showSurvivalAnalysisWindow(results) {
    var resultWin = window.open('', 'Survival_Analysis_View_' + (new Date()).getTime(), 
        'width=600,height=800,scrollbars=yes,resizable=yes,location=no,toolbar=no,status=no,menubar=no,directories=no');
    resultWin.document.write(results);
}

//This is the new popup window for GWAS. 
function showGwasWindow(results) {
    var resultWin = window.open('', 'Gwas_View_' + (new Date()).getTime());
    resultWin.document.write(results);
}

// This is the new popup window for the Haploview
function showHaploViewWindow(results) {
    var win = new Ext.Window({
        id: 'showHaploView',
        title: 'Haploview',
        layout: 'fit',
        width: 800,
        height: 550,
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

function clearExportPanel() {
    // clear the div
    exportPanel.body.update("");
}

/**
 * @return {String} return the value of the radio button that is checked
 * return an empty string if none are checked, or
 * there are no radio buttons
 * @param {} radioObj
 */
function getCheckedValue(radioObj) {
    if (! radioObj) {
        return "";
    }

    var radioLength = radioObj.length;
    if (radioLength === undefined) {
        if (radioObj.checked) {
            return radioObj.value;
        } else {
            return "";
        }
    }
    for (var i = 0; i < radioLength; i++) {
        if (radioObj[i].checked) {
            return radioObj[i].value;
        }
    }
    return "";
}

// set the radio button with the given value as being checked
//do nothing if there are no radio buttons
//if the given value does not exist, all the radio buttons
//are reset to unchecked
function setCheckedValue(radioObj, newValue) {
    if ( ! radioObj) {
        return;
    }

    var radioLength = radioObj.length;

    if (radioLength === undefined) {
        radioObj.checked = (radioObj.value === newValue.toString());
        return;
    }

    for (var i = 0; i < radioLength; i++) {
        radioObj[i].checked = false;
        if (radioObj[i].value === newValue.toString()) {
            radioObj[i].checked = true;
        }
    }
}

function searchByName() {
    var matchstrategy = document.getElementById('searchByNameSelect').value;
    var matchterm = document.getElementById('searchByNameInput').value;
    var a = matchterm.trim();
    if (a.length < 3) {
        alert("Please enter a longer search term");
        return;
    }
    var matchontology = document.getElementById('searchByNameSelectOntology').value;
    var query = getONTgetNameInfoRequest(matchstrategy, matchterm, matchontology);
    searchByNameTree.el.mask('Searching...', 'x-mask-loading');
    for (c = searchByNameTreeRoot.childNodes.length - 1; c >= 0; c--) {
        searchByNameTreeRoot.childNodes[c].remove();
    }
    searchByNameTree.render();
    Ext.Ajax.request({
        url: pageInfo.basePath+"/proxy?url=" + GLOBAL.ONTUrl + "getNameInfo",
        method: 'POST',
        xmlData: query,
        success: function (result, request) {
            searchByNameComplete(result);
        },
        failure: function (result, request) {
              searchByNameComplete(result);
        },
        timeout: '300000'
    });
}

function getSummaryStatistics() {
    Ext.Ajax.request({
        url: pageInfo.basePath+"/chart/basicStatistics",
        method: 'POST',
        success: function (result, request) {
            getSummaryStatisticsComplete(result);
            analysisPanel.body.unmask();
        },
        failure: function (result, request) {
            //getSummaryStatisticsComplete(result);
            console.error("Cannot get Summary Statistics");
            analysisPanel.body.unmask();
        },
        timeout: '300000',
        params: Ext.urlEncode({
            charttype: "basicstatistics",
            concept_key: "",
            result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
            result_instance_id2: GLOBAL.CurrentSubsetIDs[2]
        }) // or a URL encoded string
    });
}


function getSummaryStatisticsComplete(result, request) {
    resultsTabPanel.setActiveTab('analysisPanel');
    updateAnalysisPanel(result.responseText, false);
}


function getExportButtonSecurity() {
    Ext.Ajax.request({
        url: pageInfo.basePath+"/export/exportSecurityCheck",
        method: 'POST',
        success: function (result, request) {
            getExportButtonSecurityComplete(result);
        },
        failure: function (result, request) {
            getExportButtonSecurityComplete(result);
        },
        timeout: '300000',
        params: Ext.urlEncode({
            result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
            result_instance_id2: GLOBAL.CurrentSubsetIDs[2]
        }) // or a URL encoded string
    });
}

function getExportButtonSecurityComplete(result) {
    var mobj = jQuery.parseJSON(result.responseText);
    var canExport = mobj.canExport;
    if (canExport || GLOBAL.IsAdmin) {
        Ext.getCmp("exportbutton").enable();
    } else {
        Ext.getCmp("exportbutton").disable();
    }
}

function activateTab(tab) {
    resultsTabPanel.tools.help.dom.style.display = "none";
}

function getSummaryGridData() {

    resultsTabPanel.body.mask("Loading ..", 'x-mask-loading');

    if (!(GLOBAL.CurrentSubsetIDs[0]) && !(GLOBAL.CurrentSubsetIDs[1])) {
        Ext.Msg.alert('Subsets are unavailable.',
                'Please select one or two Comparison subsets and run Summary Statistics.');
        resultsTabPanel.body.unmask();
        return;
    }

    gridstore = new Ext.data.JsonStore({
        url: pageInfo.basePath+'/chart/analysisGrid'
    });

    gridstore.on('load', storeLoaded);

    var myparams = Ext.urlEncode({
        concept_key: "",
        result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
        result_instance_id2: GLOBAL.CurrentSubsetIDs[2]
    });

    gridstore.load({
        params: myparams,
        callback: function () {
            resultsTabPanel.body.unmask();
        }
    });
}

function storeLoaded(jsonStore, rows, paramsObject) {

    var cm = buildColumnModel(gridstore.reader.meta.fields);
    var grid = analysisGridPanel.getComponent('gridView');

    if (grid) {
        analysisGridPanel.remove(grid);
    }

    var bbar = new Ext.Toolbar({ height: 25 });

    if (paramsObject && paramsObject.params) {
        jQuery.get(pageInfo.basePath + '/dataExport/isCurrentUserAllowedToExport?' + paramsObject.params, function(data) {
            if (data.result) {
                var exportButton = new Ext.Button ({
                    text: 'Export to Excel',
                    listeners: {
                        click: function () {
                            var a = document.createElement('a');
                            a.href = 'data:application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;base64,'
                                + Base64.encode(grid.getExcelXml());
                            a.download = 'grid_view.xlsx';
                            a.click();
                            jQuery.post(pageInfo.basePath + '/chart/reportGridTableExport', paramsObject.params);
                        }
                    }
                });
                bbar.add(exportButton);
            }
        });
    }

    grid = new GridViewPanel({
        id: 'gridView',
        title: 'Grid View',
        viewConfig: {
            forceFit: true
        },
        bbar: bbar,
        frame:true,
        layout: 'fit',
        cm: cm,
        store: gridstore
    });
    analysisGridPanel.add(grid);
    analysisGridPanel.doLayout();
}

function getAnalysisGridData(concept_key) {
    gridstore = new Ext.data.JsonStore({
        url: pageInfo.basePath+'/chart/analysisGrid'
    });
    gridstore.on('load', storeLoaded);
    var myparams = Ext.urlEncode({
        charttype: "analysisgrid",
        concept_key: concept_key,
        result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
        result_instance_id2: GLOBAL.CurrentSubsetIDs[2]
    });
    // or a URL encoded string */

    gridstore.load({
        params: myparams
    });
}

function getAnalysisPanelContent() {
    var a = analysisPanel.body;
    return analysisPanel.body.dom.innerHTML;
}

function printPreview(content) {
    var stylesheet = "<html><head><link rel='stylesheet' type='text/css' href='" + pageInfo.basePath + "/css/datasetExplorer.css'></head><body>";
    var generator = window.open('', 'name', 'height=700,width=1100, resizable=yes, scrollbars=yes');
    var printbutton = "<input type='button' value=' Print this page 'onclick='window.print();return false;' />";
    generator.document.write(stylesheet + printbutton + content);
    generator.document.close();
}

function exportGrid() {
    viewport.getEl().mask("Getting Data....");
    Ext.get("exportgridform").dom.submit();
    setTimeout('viewport.getEl().unmask();', 10000);
}

function watchForSymbol(options) {
    var stopAt;

    if (!options || !options.symbol || !Object.isFunction(options.onSuccess)) {
        throw "Missing required options";
    }
    options.onTimeout = options.onTimeout || Prototype.K;
    options.timeout = options.timeout || 10;
    stopAt = (new Date()).getTime() + (options.timeout * 1000);
    new PeriodicalExecuter(function (pe) {
        if (typeof window[options.symbol] !== undefined) {
            pe.stop();
            options.onSuccess(options.symbol);
        } else if ((new Date()).getTime() > stopAt) {
            pe.stop();
            options.onTimeout(options.symbol);
        }
    }, 0.25);
}

//Called to run the Haploviewer
function getHaploview() {
    Ext.Ajax.request({                      
        url: pageInfo.basePath+"/asyncJob/createnewjob",
        method: 'POST',
        success: function(result, request) {
            RunHaploViewer(result, GLOBAL.CurrentSubsetIDs[1], GLOBAL.CurrentSubsetIDs[2], GLOBAL.CurrentGenes);
        },
        failure: function(result, request) {
            Ext.Msg.alert('Status', 'Unable to create the heatmap job.');
        },
        timeout: '1800000',
        params: {jobType:  "Haplo"}
    }); 
}

function RunHaploViewer(result, result_instance_id1, result_instance_id2, genes) {
    var jobNameInfo = Ext.util.JSON.decode(result.responseText);                     
    var jobName = jobNameInfo.jobName;

    showJobStatusWindow(result);    
    document.getElementById("gplogin").src = pageInfo.basePath + '/analysis/gplogin';   // log into GenePattern
    Ext.Ajax.request({                       
        url: pageInfo.basePath+"/genePattern/runhaploviewer",
        method: 'POST',
        timeout: '1800000',
        params: {
            result_instance_id1: result_instance_id1,
            result_instance_id2:  result_instance_id2,
            genes: genes,
            jobName: jobName
        }
    });
    checkJobStatus(jobName);
}

function searchByTagBefore() {
    var tagterm=document.getElementById("tagterm");
    var tagtype=document.getElementById("tagtype");
    var searchterm = document.getElementById('ontsearchterm').value;
    var a = searchterm.trim();

    if (a.length > 0 && a.length < 3) {
        alert("Please enter a longer search term.");
        return false;
    }

    if (a.length === 0 && tagtype.selectedIndex === 0) {
        alert("Please select a search term.");
        return false;
    }

    if (a.length === 0 && tagtype.selectedIndex !== 0) {
        if (tagterm.selectedIndex === -1) {
            alert("Please select a search term.");
            return false;
        }
    }
    for (c = treeRoot.childNodes.length - 1; c >= 0; c--) {
        treeRoot.childNodes[c].remove();
    }

    ontTree.render();
    viewport.el.mask("Searching...");
    return true;
}

function searchByTagComplete(response) {
    // shorthand
    var Tree = Ext.tree;
    var treeRoot = Ext.getCmp('navigateTermsPanel').getRootNode();

    viewport.el.unmask();
    var concepts = response.searchResults; //Response is an array of concept paths
    var uniqueLeaves = response.uniqueLeaves;

    var length;
    var leaf = false;
    var draggable = false;

    for (c = treeRoot.childNodes.length - 1; c >= 0; c--) {
        treeRoot.childNodes[c].remove();
    }

    jQuery('#noAnalyzeResults').hide();

    //Clear path to expand and unique leaves
    GLOBAL.PathToExpand = '';
    GLOBAL.UniqueLeaves = '';

    if (GLOBAL.DefaultPathToExpand !== "") {
        GLOBAL.PathToExpand += GLOBAL.DefaultPathToExpand + ",";
    }

    if (concepts !== undefined) {
        if (concepts.length < GLOBAL.MaxSearchResults) {
            length = concepts.length;
        } else {
            length = GLOBAL.MaxSearchResults;
        }
        for (var c = 0; c < length; c++) {
            GLOBAL.PathToExpand += concepts[c] + ",";
        }

        for (var d = 0, dLength = uniqueLeaves.length; d < dLength; d++) {
            GLOBAL.UniqueLeaves += uniqueLeaves[d] + ",";
    }

        if (concepts.length === 0) {
            jQuery('#noAnalyzeResults').show();
            Ext.getCmp('navigateTermsPanel').render();
            onWindowResize();
        } else {
            //Get the categories with the new path to expand
            getCategories();
        }
    }
}

function showHaploviewGeneSelection() {
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
        buttons: [
            {
                id: 'haploviewGeneSelectionOKButton',
                text: 'OK',
                handler: function() {
                    if (Ext.get('haploviewgenes') === null) {
                        win.close();
                        return;
                    }
                    var ob=Ext.get('haploviewgenes').dom;
                    var selected = [];
                    for (var i = 0, iLength = ob.options.length; i < iLength; i++) {
                        if (ob.options[i].selected) {
                            selected.push(ob.options[i].value);
                        }
                    }
                    GLOBAL.CurrentGenes=selected.join(',');
                    getHaploview();
                    win.close();
                }
            },
            {
                text: 'Cancel',
                handler: function() {
                    win.close();
                }
            }
        ],
        resizable: false,
        autoLoad: {
            url: pageInfo.basePath+'/analysis/showHaploviewGeneSelector',
            scripts: true,
            nocache:true,
            discardUrl:true,
            method:'POST',
            params: {
                result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
                result_instance_id2: GLOBAL.CurrentSubsetIDs[2]
            }
        },
        tools: [
            {
                id:'help',
                qtip:'Click for context sensitive help',
                handler: function(event, toolEl, panel) {
                    D2H_ShowHelp("1174",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP);
                }
            }
        ]
    });
    //  }
    win.show(viewport);
}

function genePatternLogin() {
    document.getElementById("gplogin").src = pageInfo.basePath + '/analysis/gplogin';
}

function showWorkflowStatusWindow() {
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
        buttons: [
            {
                text: 'Cancel Job',
                handler: function() {
                    runner.stopAll();
                    terminateWorkflow();
                    wfsWindow.close();
                }
            }
        ],
        resizable: false,
        autoLoad: {
            url: pageInfo.basePath+'/asyncJob/showWorkflowStatus',
            scripts: true,
            nocache: true,
            discardUrl: true,
            method: 'POST'
        }
    });
    //  }
    wfsWindow.show(viewport);
    
    var updateStatus = function() {
        Ext.Ajax.request({
            url: pageInfo.basePath+"/asyncJob/checkWorkflowStatus",
            method: 'POST',
            success: function (result, request) {
                workflowStatusUpdate(result);
            },
            failure: function (result, request) {
            },
            timeout: '300000'
        });
    };
    
    var task = {
        run: updateStatus,
        interval: 4000 //4 second
    };
 
    runner.start(task);
}

function terminateWorkflow() {
    Ext.Ajax.request({
        url: pageInfo.basePath+"/asyncJob/cancelJob",
        method: 'POST',
        success: function (result, request) {
                
        },
        failure: function (result, request) {
        },
        timeout: '300000'
    });
}

function workflowStatusUpdate(result) {
    var response = eval("(" + result.responseText + ")");   
    var inserthtml = response.statusHTML;
    var divele = Ext.fly("divwfstatus");
    if (divele !== null) {
        divele.update(inserthtml);
    }
    var status = response.wfstatus;
    if (status === 'completed') {
        runner.stopAll();       
        if (divele !== null) {
            divele.update("");
        }       
        if (wfsWindow !== null) {
            wfsWindow.close();
            wfsWindow =null;
        }       
        showWorkflowResult(result);
    } 
}

function showWorkflowResult(result) {
    var response = eval("(" + result.responseText + ")");
    var jobNumber = response.jobNumber;
    var viewerURL = response.viewerURL;
    var altviewerURL = response.altviewerURL;
    var gctURL = response.gctURL;
    var cdtURL = response.cdtURL;
    var gtrURL = response.gtrURL;
    var atrURL = response.atrURL;
    var error = response.error;
    var snpGeneAnnotationPage = response.snpGeneAnnotationPage;

    if (error !== undefined) {
        alert(error);
    } else {
        if (snpGeneAnnotationPage !== undefined && snpGeneAnnotationPage.length !== 0) {
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

function saveComparison() {

    Ext.Ajax.request({
        url: pageInfo.basePath+"/comparison/save",
        method: 'POST',
        success: function (result, request) {
            saveComparisonComplete(result);
        },
        failure: function (result, request) {
            saveComparisonComplete(result);
        },
        timeout: '600000',
        params: Ext.urlEncode({
            result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
            result_instance_id2: GLOBAL.CurrentSubsetIDs[2],
            genes: GLOBAL.CurrentGenes
        }) // or a URL encoded string
    });
}

function saveComparisonComplete(result) {
    var mobj = jQuery.parseJSON(result.responseText);
    
    //If the window is already open, close it.
    if (this.saveComparisonWindow) {
        saveComparisonWindow.close();
    }
    
    //Draw the window with the link to the comparison.
    saveComparisonWindow = new Ext.Window({
        id: 'saveComparisonWindow',
        title: 'Saved Comparison',
        autoScroll:true,
        closable: true,
        tools: [
            {
                id: 'sampleExplorerHelpButton',
                qtip: 'Click for Saved Comparison Window Help',
                disabled: false,
                handler: function () {
                    D2H_ShowHelp("1474",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP);
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

function ontFilterLoaded(el, success, response, options) {
    if (GLOBAL.preloadStudy !== "") {
        Ext.get("ontsearchterm").dom.value = GLOBAL.preloadStudy;
        Ext.get("ontSearchButton").dom.click();
    }
}

function clearQuery() {
    if (confirm("Are you sure you want to clear your current selections and analysis ?")) {
        clearAnalysisPanel();
		clearQueryPanels();
        clearDataAssociation();
    }
}

//check that an array a contains an object obj
function contains(a, obj) {
    var l = a.replace("[", "").replace("]", "").split(", ");
    var i = l.length;
    while (i--) {
        if (l[i] === obj) {
            return true;
        }
    }
    return false;
}
