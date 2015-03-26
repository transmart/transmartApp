function createMainTabPanel() {

    // create toolbar
    var toolbar = createMainToolbar();

    // create search tabgs
    var tabpanel = createSearchTabs(toolbar);

    return tabpanel;
}

// create search tabs with TEA
function createSearchTabs(toolbar) {

    // create search tabs
    var tabpanel = new Ext.TabPanel({
        id: "tab-panel",
        tbar: toolbar,
        activeTab: pageData.activeTab,
        autoScroll: true,
        //region: "center",
        items: [ 
            {
                id: "tab1",
                iconCls: "clinicalTrialgovTab",
                title: "Clinical Trials (" + pageData.trial.analysisCount + ", " + pageData.trial.count + ")",
                listeners: {activate: activateTab},
                layout: "card",
                activeItem: (pageData.trial.analysisCount>0) ? 0 : 2,
                items: [
                    {
                        id: "trial-tea-results-panel",
                        autoLoad: {
                            url: pageData.trial.teaResultsUrl,
                            nocache: true,
                            discardUrl: true,
                            method: "POST"
                        }
                    },
                    {
                        id: "trial-filter-panel",
                        autoLoad: {
                            url: pageData.trial.filterUrl,
                            nocache: true,
                            discardUrl: true,
                            method: "POST"
                        }
                    },{
                        id: "trial-results-panel",
                        autoLoad: {
                            url: pageData.trial.resultsUrl,
                            nocache: true,
                            discardUrl: true,
                            method: "POST"
                        }
                    }
                ]
            }, 
            {
                id: "tab2",
                iconCls: "expTab",
                title: "mRNA Analysis (" + pageData.pretrial.mRNAAnalysisCount + ", " + pageData.pretrial.count + ")",
                listeners: {activate: activateTab},
                layout: "card",
                layoutConfig: { deferredRender: true },
                activeItem: (pageData.pretrial.mRNAAnalysisCount>0) ? 0 : 2,
                items: [
                    {
                        id: "pretrial-tea-results-panel",
                        autoLoad: {
                            url: pageData.pretrial.teaResultsUrl,
                            nocache: true,
                            discardUrl: true,
                            method: "POST",
                            timeout: 300000
                        }
                    },
                    {
                        id: "pretrial-filter-panel",
                        autoLoad: {
                            url: pageData.pretrial.filterUrl,
                            nocache: true,
                            discardUrl: true,
                            method: "POST"
                        }
                    },{
                        id: "pretrial-results-panel",
                        autoLoad: {
                            url: pageData.pretrial.resultsUrl,
                            nocache: true,
                            discardUrl: true,
                            method: "POST"
                        }
                    }
                ]
             } ,             
             {
                id: "tab3",
                iconCls: "profTab",
                title: "mRNA Profiles (" + pageData.profile.count + ")",
                listeners: {activate: activateTab},
                layout: "card",
                activeItem: 0,
                items: [
                    {
                        id: "profile-results-panel",
                        autoLoad: {
                            url: pageData.profile.resultsUrl,
                            nocache: true,
                            discardUrl: true,
                            method: "POST"
                        }
                    }
                ]
            },
            {
                id: "tab4",
                iconCls: "jubTab",
                title: "Literature (" + pageData.jubilant.count + ")",
                listeners: {activate: activateTab},
                layout: "card",
                //layoutConfig: { deferredRender: true },
                activeItem: 0,
                items: [
                    {
                        id: "jubilant-results-panel",
                        autoLoad: {
                            url: pageData.jubilant.resultsUrl,
                            nocache: true,
                            discardUrl: true,
                            method: "POST"
                        }
                    }, {
                        id: "jubilant-filter-panel",
                        autoLoad: {
                            url: pageData.jubilant.filterUrl,
                            nocache: true,
                            discardUrl: true,
                            method: "POST"
                        }
                    }, {
                        id: "jubilant-summary-panel"
                    }
                ]
            },
            {
                id:"tab5",
                iconCls: "docTab",
                   title: "Documents (" + pageData.doc.count + ")",
                listeners: {
                    activate: activateTab
                },
                layout: "card",
                layoutConfig: { deferredRender: true },
                activeItem: 0,
                items: [
                    {
                        id: "documents-results-panel",
                        autoLoad: {
                            url: pageData.doc.resultsUrl,
                            nocache: true,
                            discardUrl: true,
                            method: "POST"
                        }
                    }, {
                        id: "documents-filter-panel",
                        autoLoad: {
                            url: pageData.doc.filterUrl,
                            nocache: true,
                            discardUrl: true,
                            method: "POST"
                        }
                    }
                ]
            },
            {
                id: "tab6",
                iconCls: "pictorTab",
                title: "Pictor",
                listeners: {activate: activateTab},
                xtype: "iframepanel",
                closable: false,
                loadMask: true,
                defaultSrc: pageData.pictor.resultsUrl
            },
            {
                id: "tab7",
                iconCls: "resnetTab",
                title: "ResNet",
                listeners: {activate: activateTab},
                xtype: "iframepanel",
                closable: false,
                loadMask: true,
                defaultSrc: pageData.resnet.resultsUrl,
                tabTip: pageData.resnet.credentials
            } 
            , 
            {
                id: "tab8",
                iconCls: "genegoTab",
                title: "GeneGo",
                listeners: {activate: activateTab},
                xtype: "iframepanel",
                closable: false,
                loadMask: true,
                defaultSrc: pageData.genego.resultsUrl,
                tabTip: pageData.genego.credentials
            }
            
        ]
    });
    return tabpanel;
}

// create toolbar below search tabs
function createMainToolbar() {

    var toolbar = new Ext.Toolbar([
           {
               id: "filters-show-button",
               text: "Show Filters",
               handler: showFilters,
               cls: "x-btn-text-icon",
               iconCls: "filtersBtn"
           }, {
               id: "filters-hide-button",
               text: "Hide Filters",
               handler: showFilters,
               cls: "x-btn-text-icon",
               hidden: true,
               iconCls: "filtersBtn"
           }, {
               id: "summary-show-button",
               text: "Show Summary",
               handler: showSummary,
               cls: "x-btn-text-icon",
               iconCls: "summaryBtn"
           }, {
               id: "summary-hide-button",
               text: "Show Search Results",
               handler: showSummary,
               cls: "x-btn-text-icon",
               hidden: true,
               iconCls: "summaryBtn"
           }, {
               id: "heatmap-button",
               text: "Heatmap",
               handler: showHeatmap,
               cls: "x-btn-text-icon",
               iconCls: "heatmapBtn"
           },
           {
               id: "tea-button",
               text: "Analysis View",
               handler: showTEAView,
               cls: "x-btn-text-icon",
               iconCls: "teaBtn"
           },
           {
               id: "studyview-button",
               text: "Study View",
               handler: showStudyView,
               cls: "x-btn-text-icon",
               iconCls: "studyBtn"
           },
           {
               id: "exportsummary-button",
               text: "Export Results",
               handler: exportSummary,
               cls: "x-btn-text-icon",
               iconCls: "exportSummaryBtn"
           },
           {
               id: "exportresnet-button",
               text: "Export to ResNet",
               handler: exportResNet,
               cls: "x-btn-text-icon",
               iconCls: "exportResNetBtn"               
           },
           {
				id:'contextHelp-button',
			    handler: function(event, toolEl, panel){
			    	D2H_ShowHelp(filterContextHelpId,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
			    },
	            cls: "x-btn-text-icon",
	            iconCls: "contextHelpBtn"  
		   },
		   {
               xtype: "tbfill"
           }
       ]);
    return toolbar;
}

function activateTab(tab) {
    switch (tab.id) {
    case "tab1":
        setButtonVisibility("filters", true);
        setButtonVisibility("summary", false);
        if(pageData.trial.count>0) {
            setButtonVisibility("heatmap", true);
            setButtonVisibility("studyview", true);

            if(pageData.trial.analysisCount>0) {
                setButtonVisibility("tea",true);
            } else {
                setButtonVisibility("tea",false);
            }
        } else {
            setButtonVisibility("heatmap", false);
            setButtonVisibility("tea",false);
            setButtonVisibility("studyview", false);
        }
        setButtonVisibility("exportsummary", true);
        setButtonVisibility("exportresnet", false);
        
        var contextHelpVisibility = false;
        if(pageData.trial.analysisCount>0 || pageData.trial.count>0){
        	contextHelpVisibility = true;
        }
        setButtonVisibility("contextHelp", contextHelpVisibility);
        filterContextHelpId = (pageData.trial.analysisCount>0) ? "1027" : "1028";
        break;
    case "tab2":
        setButtonVisibility("filters", true);

        // experiment views
        if(pageData.pretrial.count>0){
            setButtonVisibility("studyview", true);
        } else {
        	setButtonVisibility("studyview", false);
        }

        // tea analysis view
        if(pageData.pretrial.mRNAAnalysisCount>0){
            setButtonVisibility("tea", true);
        } else {
        	setButtonVisibility("tea", false);
        }
        setButtonVisibility("summary", false);
        setButtonVisibility("heatmap", false);
        setButtonVisibility("exportsummary", true);
        setButtonVisibility("exportresnet", false);
        setButtonVisibility("contextHelp", true);
        
        var contextHelpVisibility = false;
        if(pageData.pretrial.mRNAAnalysisCount>0 || pageData.pretrial.count>0){
        	contextHelpVisibility = true;
        }
        setButtonVisibility("contextHelp", contextHelpVisibility);
        filterContextHelpId = (pageData.pretrial.mRNAAnalysisCount>0) ? "1023" : "1023";
        break;
    case "tab3":
        setButtonVisibility("filters", false);
        setButtonVisibility("summary", false);
        setButtonVisibility("heatmap", false);
        setButtonVisibility("exportsummary", false);
        setButtonVisibility("exportresnet", false);
        setButtonVisibility("studyview", false)
        setButtonVisibility("tea",false);
        setButtonVisibility("contextHelp", true);
        filterContextHelpId="1040";
        break;
    case "tab4":
        setButtonVisibility("filters", true);
        setButtonVisibility("summary", pageData.jubilant.litJubOncAltCount > 0);
        setButtonVisibility("heatmap", false);
        if (pageData.jubilant.count < 1) 	{
        	setButtonVisibility("exportsummary", false);
        } else	{
        	setButtonVisibility("exportsummary", true);
        }
        if (pageData.hideInternal==true || pageData.jubilant.count < 1)  {
        	setButtonVisibility("exportresnet", false);
        } else	{
        	setButtonVisibility("exportresnet", true);
        }
        setButtonVisibility("studyview", false);
        setButtonVisibility("tea",false);
        setButtonVisibility("contextHelp", true);
        filterContextHelpId="1042";
        break;
    case "tab5":
        setButtonVisibility("filters", true);
        setButtonVisibility("summary", false);
        setButtonVisibility("heatmap", false);
        setButtonVisibility("exportsummary", false);
        setButtonVisibility("exportresnet", false);
        setButtonVisibility("studyview", false);
        setButtonVisibility("tea",false);
        setButtonVisibility("contextHelp", true);
        filterContextHelpId="1047";
        break;
    case "tab6":
        setButtonVisibility("filters", false);
        setButtonVisibility("summary", false);
        setButtonVisibility("heatmap", false);
        setButtonVisibility("exportsummary", false);
        setButtonVisibility("exportresnet", false);
        setButtonVisibility("studyview", false);
        setButtonVisibility("tea",false);
        if (pageData.pictor.resultsUrl.length > 1980) {
            window.alert("Note: The length of the URL for the Pictor query has exceeded the maximum supported by Internet Explorer and some genes may have been excluded from the query.");
        }
        setButtonVisibility("contextHelp", false);
        break;

    case "tab7":
        setButtonVisibility("filters", false);
        setButtonVisibility("summary", false);
        setButtonVisibility("heatmap", false);
        setButtonVisibility("exportsummary", false);
        setButtonVisibility("exportresnet", false);
        setButtonVisibility("studyview", false);
        setButtonVisibility("tea",false);
        setButtonVisibility("contextHelp", false);
        break;

    case "tab8":
        setButtonVisibility("filters", false);
        setButtonVisibility("summary", false);
        setButtonVisibility("heatmap", false);
        setButtonVisibility("exportsummary", false);
        setButtonVisibility("exportresnet", false);
        setButtonVisibility("studyview", false);
        setButtonVisibility("tea",false);
        setButtonVisibility("contextHelp", false);
        break;
    }
}

/**
 * @activetab: The tab which is currently active.
 * @item: The item for which help is provided.
 */
function showContextSpecificHelp(activetab, button){
	var contextHelpButton = Ext.getCmp("contextHelp-button");
    switch (activetab.getId()) {
    case "tab1":
		switch (button.id){
		case "filters-show-button":
				filterContextHelpId = "1025";
				contextHelpButton.setVisible(true);
		break;
		case "filters-hide-button":
				contextHelpButton.setVisible(false);
		break;
		case "tea-button":
				filterContextHelpId="1027";
				contextHelpButton.setVisible(true);
		break;
		case "studyview-button":
				filterContextHelpId="1028";
				contextHelpButton.setVisible(true);
		break;
		default:
			contextHelpButton.setVisible(false);
		}

	break;
    case "tab2":
    	switch (button.id){
		case "filters-show-button":
			filterContextHelpId = "1033";
			contextHelpButton.setVisible(true);
		break;
		case "filters-hide-button":
			contextHelpButton.setVisible(false);
		break;
		case "tea-button":
			filterContextHelpId="1034";
			contextHelpButton.setVisible(true);
		break;
		case "studyview-button":
			filterContextHelpId="1035";
			contextHelpButton.setVisible(true);
		break;
		default:
			contextHelpButton.setVisible(false);
    	}
    break;
    case "tab4":
    	switch(button.id){
    	case "filters-show-button":
			filterContextHelpId = "1043";
			contextHelpButton.setVisible(true);
		break;
    	case "filters-hide-button":
    		filterContextHelpId = "1042";
			contextHelpButton.setVisible(true);
		break;
    	case "summary-show-button":
			filterContextHelpId = "1319";
			contextHelpButton.setVisible(true);
		break;
		case "summary-hide-button":
			filterContextHelpId = "1042";
			contextHelpButton.setVisible(true);
		break;
		default:
			contextHelpButton.setVisible(false);
    	}
    break;
    case "tab5":
    	switch(button.id){
    	case "filters-show-button":
			filterContextHelpId = "1049";
			contextHelpButton.setVisible(true);
		break;
    	case "filters-hide-button":
    		filterContextHelpId = "1047";
			contextHelpButton.setVisible(true);
		break;
		default:
			contextHelpButton.setVisible(false);
    	}
    break;
    default:
    	contextHelpButton.setVisible(false);
	}
}

function showFilters(button) {
    var tabpanel = Ext.getCmp("tab-panel");
    var activetab = tabpanel.getActiveTab();
    var layout = activetab.getLayout();
    var activeitem = layout.activeItem;
    if (activeitem.id.indexOf("-results-") > -1 || activeitem.id.indexOf("-summary-") > -1) {
        if (activetab.id == "tab1" && !Ext.getCmp("trialfilter-tree")) {
            showTrialFilterTree(pageData.trialFilterUrl);
        }
        layout.setActiveItem(1);
    } else {
        layout.setActiveItem(0);
    }

    var showFiltersButton = Ext.getCmp("filters-show-button");
    var hideFiltersButton = Ext.getCmp("filters-hide-button");
    if (showFiltersButton != null) {
    	if (activetab.id == "tab1" || activetab.id == "tab2")	{
    	//	var exportSummaryButton = Ext.getCmp( "exportsummary-button");
    	//	exportSummaryButton.setVisible(showFiltersButton.hidden);
    	} else if(activetab.id == "tab4") {
            var showSummaryButton = Ext.getCmp("summary-show-button");
            var hideSummaryButton = Ext.getCmp( "summary-hide-button");
            var exportSummaryButton = Ext.getCmp( "exportsummary-button");
            var exportResnetButton = Ext.getCmp( "exportresnet-button");
            showSummaryButton.setVisible(showFiltersButton.hidden);
            exportSummaryButton.setVisible(showFiltersButton.hidden);
            if (pageData.jubilant.litJubOncIntCount > 0 || pageData.jubilant.litJubAsthmaCount > 0)	{
        		exportResnetButton.setVisible(showFiltersButton.hidden);
        	} else	{
        		exportResnetButton.setVisible(false);
        	}

            hideSummaryButton.setVisible(false);
        }
        showFiltersButton.setVisible(showFiltersButton.hidden);
    }
    if (hideFiltersButton != null) {
        hideFiltersButton.setVisible(hideFiltersButton.hidden);
    }
    showContextSpecificHelp(activetab, button);
}


function showSummary(button) {
    var tabpanel = Ext.getCmp("tab-panel");
    var activetab = tabpanel.getActiveTab();
    var layout = activetab.getLayout();
    var activeitem = layout.activeItem;
    if (activeitem.id.indexOf("-results-") > -1) {
        if (activetab.id == "tab4") {
            var sum;
            if (Ext.getCmp("jubilant-summary-gridpanel") == null) {
                sum = createJubSummary();
            } else {
                sum = Ext.getCmp("jubilant-summary-gridpanel");
            }
//            var datatype = getResultType();
//            var titles = {
//                "JUBILANT_ONCOLOGY_ALTERATION":"Jubilant Oncology Alteration Summary",
//                "JUBILANT_ONCOLOGY_INHIBITOR":"Jubilant Oncology Inhibitor Summary",
//                "JUBILANT_ONCOLOGY_INTERACTION":"Jubilant Oncology Interaction Summary"
//            };
            sum.setTitle("Jubilant Oncology Alteration Summary");
            sum.getStore().load({params: {offset:0, max:20}});
        }
        layout.setActiveItem(2);
    } else {
        layout.setActiveItem(0);
    }

    var showbutton = Ext.getCmp("summary-show-button");
    var hidebutton = Ext.getCmp("summary-hide-button");
    if (showbutton != null) {
        showbutton.setVisible(showbutton.hidden);
    }
    if (hidebutton != null) {
        hidebutton.setVisible(hidebutton.hidden);
    }
    showContextSpecificHelp(activetab, button);
}


function showTEAView(button){
    var tabpanel = Ext.getCmp("tab-panel");
    var activetab = tabpanel.getActiveTab();
    var layout = activetab.getLayout();
    var activeitem = layout.activeItem;
    layout.setActiveItem(0);
    showContextSpecificHelp(activetab, button);
}

function showStudyView(button){
    var tabpanel = Ext.getCmp("tab-panel");
    var activetab = tabpanel.getActiveTab();
    var layout = activetab.getLayout();
    var activeitem = layout.activeItem;
    layout.setActiveItem(2);
    showContextSpecificHelp(activetab, button);
}

function setButtonVisibility(id, visibility) {
    var showbutton = Ext.getCmp(id + "-show-button");
    var hidebutton = Ext.getCmp(id + "-hide-button");
    var button = Ext.getCmp(id + "-button");
    if (showbutton != null) {
        showbutton.setVisible(visibility);
    }
    if (hidebutton != null) {
        hidebutton.setVisible(false);
    }
    if (button != null) {
        button.setVisible(visibility);
    }
}

function showHeatmap(button) {
    var w = window.open(pageData.heatmapUrl, "_trialHeatmap", "width=900,height=800,scrollbars,resizable");
    w.focus();
}

function exportSummary(button) {
	var tabpanel = Ext.getCmp("tab-panel");
    var activetab = tabpanel.getActiveTab();
    var layout = activetab.getLayout();
    var activeitem = layout.activeItem;
    switch (activetab.getId()) {
    case "tab1":
    	if (activeitem.id.indexOf("-tea-") > -1) {
    		window.location = pageData.downloadTrialAnalysisUrl;
    	} else	{
    		window.location = pageData.downloadTrialStudyUrl;
    	}
    	break;
    case "tab2":
    	if (activeitem.id.indexOf("-tea-")	> -1)	{
    		window.location = pageData.downloadEaTEAUrl;
    	} else	{
    		window.location = pageData.downloadEaUrl;
    	}
    	break;
    case "tab4":
    	window.location = pageData.downloadJubSummaryUrl;
    	break;
    }
}

function exportResNet(button) {
    window.location = pageData.downloadResNetUrl;
}

function createJubSummary() {

    var store = new Ext.data.JsonStore({
        root: "rows",
        totalProperty: 'count',
        remoteSort: true,
        listeners: {
            "beforeload" : {
                fn: function(obj, options) {
                    var el = Ext.getDom("resultType");
                    if (el != null) {
                        obj.baseParams.datatype = el.value;
                    }
                    return true;
                },
                scope: this
            }
        },
        fields: [
         {name: 'dataType'},
         {name: 'alterationType'},
         {name: 'totalFrequency'},
         {name: 'totalAffectedCases'},
         {name: 'summary'},
         {name: 'target'},
         {name: 'variant'},
         {name: 'diseaseSite'}
        ],
        proxy: new Ext.data.ScriptTagProxy({ url: pageData.jubSummaryUrl })
    });
    store.setDefaultSort('dataType', 'ASC');

    var pagingBar = new Ext.PagingToolbar({
        pageSize: 20,
        store: store,
        displayInfo: true,
        displayMsg: "Displaying records {0} - {1} of {2}",
        emptyMsg: "No records to display",
        paramNames: {start: 'offset', limit: 'max'}
    });

    // create the Grid
    var grid = new Ext.grid.GridPanel({
        id: "jubilant-summary-gridpanel",
        title: "Jubilant Oncolgoy Summary",
        store: store,
        trackMouseOver: false,
        disableSelection: true,
        columns: [
               {header: "Data Type", width: 75, sortable: true, dataIndex: 'dataType', menuDisabled: true},
            {header: "Alteration Type", width: 150, sortable: true, dataIndex: 'alterationType', menuDisabled: true},
            {header: "Disease Site", width: 200, sortable: true, dataIndex: 'diseaseSite', menuDisabled: true},
            {id: "summary", header: "Summary", width: 300, sortable: true, dataIndex: 'summary', menuDisabled: true},
            {header: "Target", width: 75, sortable: true, dataIndex: 'target', menuDisabled: true},
            {header: "Variant", width: 75, sortable: true, dataIndex: 'variant', menuDisabled: true},
            {header: "Frequency", width: 75, sortable: true, dataIndex: 'totalFrequency', menuDisabled: true},
            {header: "Cases", width: 75, sortable: true, dataIndex: 'totalAffectedCases', menuDisabled: true}
        ],
        autoScroll: true,
        stripeRows: true,
        enableHdMenu: false,
        autoExpandColumn: "summary",
        bbar: pagingBar
    });

    var panel = Ext.getCmp("jubilant-summary-panel");
    panel.add(grid);
    panel.doLayout();
    return grid;
}

function selectJubilantPanel(index) {
    var tabpanel = Ext.getCmp("tab-panel");
    var activetab = tabpanel.getActiveTab();
    var layout = activetab.getLayout();
    layout.setActiveItem(index);
}

function onItemCheck(item, checked){
    
}

function popupWindow(mylink, windowname) {
    if (!window.focus)
    return true;

    var href;
    if (typeof(mylink) == 'string')
        href = mylink;
    else
        href = mylink.href;

    try {
        var w = window.open(href, windowname, 'width=800,height=800,scrollbars=yes');
        w.focus();
    } catch (er) {
        if (Ext.isIE) {
            alert("Unable to open the following url:\n" + href);
        } else {
            alert("Unable to open the following url:\n" + href + "\nThis may be caused by security issues with non Internet Explorer browsers.");
        }
    }
    return false;
}

// Show a dialog window animated from the specified id parameter with
// title and url contained in the value parameter.
function showDialog(id, value) {
    // Attempt to get existing window with id.
    var win = Ext.getCmp(id + '-win');

    if(win==null){
    win = new Ext.Window({
        id: id + '-win',
        animateTarget: id,
        autoScroll: true,
        width: 550,
        height: 350,
        closeAction: 'hide',
        bodyBorder: false,
        plain: true,
        constrain: true,
        title: value.title,
       // contentEl: value.element
        autoLoad: {
            url: value.url,
            nocache: false,
            discardUrl: false,
            method: "POST"
        }
    });
    }

    win.show();
    win.toFront();
    var anchor = id + '_anchor';
    if (document.getElementById(anchor) != null) {
    	win.alignTo(anchor, 'bl-tl?');
    }
}

//Show a dialog window animated from the specified id parameter with
//title and content contained in value parameter.
function showDialogDiv(id, value) {
    // Attempt to get existing window with id.
    var win = Ext.getCmp(id + '-win');

    if(win==null){
 win = new Ext.Window({
     id: id + '-win',
     animateTarget: id,
     autoScroll: true,
     width: 550,
     height: 350,
     closeAction: 'hide',
     bodyBorder: false,
     plain: true,
     constrain: true,
     title: value.title,
     contentEl: value.element
 });
    }

 win.show();
 win.toFront();
 var atitle = id+'_anchor';
// alert(atitle);
//  var anchor = document.getElementById(value.title+'_anchor');
 win.alignTo(atitle,'bl-tl?');
}
function selectOnChange(url, id, name, value) {
    return new Ajax.Updater(id, url, {asynchronous:true, evalScripts:true, parameters:name + '=' + value});
}

function getResultType() {
    var resultType = "JUBILANT_ONCOLOGY_ALTERATION";
    var el = Ext.getDom("resultType");
    if (el != null) {
        resultType = el.value;
    }
    return resultType;
}

function validateDocumentFilters() {
    var form = document.documentfilters;
    if (form != null) {
        if (!form.repository_biomarker.checked &&
        		!form.repository_conferences.checked &&
                !form.repository_dip.checked &&
                !form.repository_jubilant_oncology.checked) {
            alert("Please select at least one Repository.");
            return false;
        }
        if (!form.type_excel.checked &&
                !form.type_html.checked &&
                !form.type_pdf.checked &&
                !form.type_powerpoint.checked &&
                !form.type_text.checked &&
                !form.type_word.checked &&
                !form.type_other.checked) {
            alert("Please select at least one Document Type.");
            return false;
        }
    }
    return true;
}
