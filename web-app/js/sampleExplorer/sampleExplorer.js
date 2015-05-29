/*
 This function will make a quick call to the server to check
 a session variable that if set indicates that it is OK
 to export the datasets since the user ran the one of the
 heatmap options and loaded the gene expression data
 */
function exportDataSets()
{
    Ext.get("exportdsform").dom.submit();
}


//When a main category is selected we need to reload both panels.
function toggleMainCategorySelection(searchTerm,searchCategory)
{
    initializeGlobalGridColumnList();

    GLOBAL.SearchJSON[searchCategory] = [];

    //Push the term into the array for its category.
    GLOBAL.SearchJSON[searchCategory].push(searchTerm);

    var westPanelURL = pageInfo.basePath+'/' + GLOBAL.explorerType + '/showWestPanelSearch';
    var centerPanelURL = pageInfo.basePath+'/' + GLOBAL.explorerType + '/showDataSetResults';

    //Reload the west panel with new content.
    Ext.getCmp('westPanel').load(
        {
            url : westPanelURL,
            jsonData : {"SearchJSON" : GLOBAL.SearchJSON}
        }
    );

    //Reload the east panel with new content. The grid panel is loaded in the callback function.
    Ext.getCmp('queryPanel').load(
        {
            url : centerPanelURL,
            jsonData : {"SearchJSON" : GLOBAL.SearchJSON},
            callback : loadDataSetGrid
        }
    );

    //Enable the Advanced Workflow button.
    if(GLOBAL.EnableGP=='true'){
        Ext.getCmp('advancedbutton').setVisible(true);
        Ext.getCmp('analysisJobsPanel').setVisible(true);
    }

    Ext.getCmp('clearsearchbutton').setVisible(true);
    //Ext.getCmp('addsubset').setVisible(true);

    //Change the help menu for this page.
    Ext.getCmp("sampleExplorerHelpButton").setHandler(function(){D2H_ShowHelp("1439",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );})
}

/**
 * This fires off when we check a checkbox on the west panel which displays the filters.
 * @param newSearchTerm
 * @param checkedStatus
 * @param searchCategory
 */
function updateFilterList(newSearchTerm,checkedStatus,searchCategory)
{
    //Initialize the array for this category if it doesn't already exists.
    if(!GLOBAL.SearchJSON[searchCategory]) GLOBAL.SearchJSON[searchCategory] = [];

    //Only add the term if it isn't already on the list and the checkedStatus is true.
    if(checkedStatus==true)
    {
        //Push the term into the array for its category.
        GLOBAL.SearchJSON[searchCategory].push(newSearchTerm);
    }

    //If the term is in the list and it's not checked, remove it from the list.
    if(checkedStatus==false)
    {
        GLOBAL.SearchJSON[searchCategory] = removeFromArray(newSearchTerm,GLOBAL.SearchJSON[searchCategory]);
    }

    //All we need to do is reload the data store. It will take into account the updated JSON Filter data.
    GLOBAL.resultGridPanel.getStore().reload();
    GLOBAL.resultGridPanel.setTitle(createValuesString(GLOBAL.SearchJSON));

    //We need to refresh the west panel with the new search criteria.
    var westPanelURL = pageInfo.basePath+'/' + GLOBAL.explorerType + '/showWestPanelSearch';

    //Reload the west panel with new content.
    Ext.getCmp('westPanel').load(
        {
            url : westPanelURL,
            jsonData : {"SearchJSON" : GLOBAL.SearchJSON}
        }
    );
}

/*
 * This function will create a GridPanel with the results from our filtering.
 */
function loadDataSetGrid()
{
    //This is the URL that will return the data we want in JSON format.
    var dataStoreURL = pageInfo.basePath+'/' + GLOBAL.explorerType + '/getDataSetResults?mainTerm=' + createValuesString(GLOBAL.SearchJSON);

    //We need to post our JSON filter to the URL to get our results.
    var dataStoreProxy = new Ext.data.HttpProxy({
        url: dataStoreURL,
        method: 'POST',
        jsonData : {"SearchJSON" : GLOBAL.SearchJSON}
    });

    //This reader is used to put the JSON into the data store.
    var dataStoreReader = new Ext.data.JsonReader({
        root: 'results',
        fields: getFieldsListForReader()
    });

    //This dictates how the columns will show up in our grid.
    var dataSetColModel = new Ext.grid.ColumnModel({
        id : 'dataSetColModel',
        defaults: {
            sortable: true
        },
        listeners: {
            'hiddenchange' : dataGridColumnRemoved
        },
        columns: getFieldListForColModel()
    });

    var listingGroupedStore = new Ext.data.GroupingStore({
        id : 'dataGridDataStoreGroup',
        autoLoad: true,
        remoteSort: false,
        proxy: dataStoreProxy,
        reader: dataStoreReader,
        sortInfo:{field: 'count', direction: "ASC"}
    });


    //Fix this. Out of patience for now.
    if(GLOBAL.explorerType == "sampleExplorer")
    {
        //This draws the actual grid panel to the DIV on the screen.
        GLOBAL.resultGridPanel = new Ext.grid.GridPanel({
            id: 'resultsGridPanel',
            title: createValuesString(GLOBAL.SearchJSON),
            renderTo: "divDataSetResults",
            layout : 'fit',
            width : GLOBAL.resultsGridWidth,
            height : GLOBAL.resultsGridHeight,
            enableDragDrop : true,
            ddGroup : 'resultsGridPanel-dd',
            ddText : 'Place this row.',
            style: 'margin: 30px auto',
            store: listingGroupedStore,
            colModel: dataSetColModel,
            stripeRows: true,
            view: new Ext.grid.GroupingView({
                forceFit:true,
                startCollapsed: true,
                groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})',
                hideGroupedColumn: false
            }),
            viewConfig: {
                forceFit: true
            },
            tbar:['->',
                {
                    text:'Sample Contact Information',
                    tooltip:'Sample Contact Information',
                    handler : function()
                    {
                        gatherSampleContactInformation();
                    }
                },
                {
                    text:'Collapse All',
                    tooltip:'Collapse All',
                    handler : function()
                    {
                        GLOBAL.resultGridPanel.view.collapseAllGroups();
                    }
                },
                {
                    text:'Expand All',
                    tooltip:'Expand All',
                    handler : function()
                    {
                        GLOBAL.resultGridPanel.view.expandAllGroups();
                    }
                }]
        });
    }
    else
    {
        //This draws the actual grid panel to the DIV on the screen.
        GLOBAL.resultGridPanel = new Ext.grid.GridPanel({
            id: 'resultsGridPanel',
            title: createValuesString(GLOBAL.SearchJSON),
            renderTo: "divDataSetResults",
            layout : 'fit',
            width : GLOBAL.resultsGridWidth,
            height : GLOBAL.resultsGridHeight,
            enableDragDrop : true,
            ddGroup : 'resultsGridPanel-dd',
            ddText : 'Place this row.',
            style: 'margin: 30px auto',
            store: listingGroupedStore,
            colModel: dataSetColModel,
            stripeRows: true,
            view: new Ext.grid.GroupingView({
                forceFit:true,
                startCollapsed: true,
                groupTextTpl: '{text} ({[values.rs.length]} {[values.rs.length > 1 ? "Items" : "Item"]})',
                hideGroupedColumn: false
            }),
            viewConfig: {
                forceFit: true
            },
            tbar:['->',
                {
                    text:'Collapse All',
                    tooltip:'Collapse All',
                    handler : function()
                    {
                        GLOBAL.resultGridPanel.view.collapseAllGroups();
                    }
                },
                {
                    text:'Expand All',
                    tooltip:'Expand All',
                    handler : function()
                    {
                        GLOBAL.resultGridPanel.view.expandAllGroups();
                    }
                }]
        });

    }


    /*
     //*************************************
     GLOBAL.tabs = new Ext.TabPanel({
     renderTo: 'site_content',
     activeTab : 0,
     deferredRender: false,
     autoTabs : false,
     layoutOnTabChange: true,
     items : [{}]
     });
     //*************************************
     */
    //addTabbedSubset();
    //addTabbedSubset();
    //addTabbedSubset();
    //GLOBAL.tabs.setActiveTab(1);

    //This one line of code is the product of 8 hours of fidgeting. In order for the tabs to display properly I had to initialize an empty items collection. This leaves a weird empty tab that needs to be removed.
    //I might just be missing some kind of rendering event for the tab panel but for now we'll just initialize and remove the extra.
    //GLOBAL.tabs.remove(0);
}

function renderDSELink(val)
{
    if(val)
    {
        return val + '&nbsp;<a href="' + pageInfo.basePath + '/datasetExplorer/index?DataSetName=' + val + '" target="_blank" title="Click to view ' + val + ' in Dataset Explorer!"><img src="' + pageInfo.basePath + '/images/linkext7.gif"></a>';
    }
}

//This renderer draws a checkbox.
function renderIncludeCheckBox(val)
{
    return '<input type="checkbox" onClick="modifyDataList();">';
}

//When we remove or add a column from the grid panel we need to re-query Solr to get the results with or without this column.
function dataGridColumnRemoved(cm,columnIndex,hidden)
{
    //We need to find out what column is being acted on.
    var columnNameFromHeader = Ext.getCmp("resultsGridPanel").getColumnModel().getDataIndex(columnIndex);

    //Replace any spaces with an "_".
    columnNameFromHeader = columnNameFromHeader.replace(' ','_');

    //If the column is now hidden, remove it from list, otherwise add it back if it isn't there.
    if(hidden)
    {
        //Remove this column from our SearchJSON object.
        removeFromArray(columnNameFromHeader,GLOBAL.SearchJSON["GridColumnList"]);
    }
    else
    {
        GLOBAL.SearchJSON["GridColumnList"].push(columnNameFromHeader);
    }

    //Reload the title to the grid.
    GLOBAL.resultGridPanel.getStore().reload();
    GLOBAL.resultGridPanel.setTitle(createValuesString(GLOBAL.SearchJSON));
}

//This renderer creates a link that calls the biobank results function with the id from the datastore corresponding to the row we clicked on.
function renderSampleLink(value,id,row) {

    var returnString = "";

    if(true)
    {
        returnString += value + '&nbsp;&nbsp;&nbsp;<a href="#" onClick="showBioBankResults(\'' + row.id + '\')"><img src="' + pageInfo.basePath + '/images/test_tube.png"></a>&nbsp;&nbsp;&nbsp;';
    }
    else
    {
        returnString += value;
    }

    return returnString;
}

function addTabbedSubset()
{
    panelNumber = GLOBAL.subsetTabs;

    var newTab = GLOBAL.tabs.add({
        id: 'subsettab' + panelNumber,
        title: 'Subset ' + panelNumber,
        items: ({html:'<div id="subsetpanel' + panelNumber + '"></div>'}),
        width : GLOBAL.resultsGridWidth,
        height : GLOBAL.resultsGridHeight,
    });

    GLOBAL.tabs.add(newTab);
    GLOBAL.tabs.setActiveTab(newTab);
    newTab.doLayout();
    addSubset(panelNumber);

    GLOBAL.subsetTabs = GLOBAL.subsetTabs + 1;
}

function addSubset(panelNumber)
{
    //Create the new JSON object which represents the search for this subset.
    GLOBAL["SearchJSON" + panelNumber] = {};

    //Create a list of the default columns for use in the subset grids.
    GLOBAL["SearchJSON" + panelNumber]["GridColumnList"] = [];

    initializeGlobalGridColumnList(panelNumber);

    //Create the DIV which holds the GridPanel.
    myEl = new Ext.Element(document.createElement('div'));
    Ext.get('subsetpanel' + panelNumber).appendChild(myEl);

    //This is the URL that will return the data we want in JSON format.
    var dataStoreURL = pageInfo.basePath+'/' + GLOBAL.explorerType + '/getDataSetResults';

    //This reader is used to put the JSON into the data store.
    var dataStoreReader = new Ext.data.JsonReader({
        root: 'results',
        fields: getFieldsListForReader()
    });

    //This dictates how the columns will show up in our grid.
    var dataSetColModel = new Ext.grid.ColumnModel({
        defaults: {
            sortable: true
        },
        columns: getFieldListForColModel()
    });

    //We need to post our JSON filter to the URL to get our results.
    var listingSubsetStoreProxy = new Ext.data.HttpProxy({
        id : 'subsetDataGridDataStore' + panelNumber,
        url: dataStoreURL,
        method: 'POST',
        jsonData : {"SearchJSON" : GLOBAL["SearchJSON" + panelNumber], "PanelNumber":panelNumber},
        disableCaching : true
    });

    //This is the datastore that will hold the data from Solr.
    var listingSubsetStore = new Ext.data.Store({
        id : 'subsetDataGridDataStore' + panelNumber,
        autoLoad: false,
        proxy: listingSubsetStoreProxy,
        reader: dataStoreReader,
        sortInfo:{field: 'count', direction: "ASC"}
    });

    //This reader is used to put the JSON into the data store.
    var dataStoreReader = new Ext.data.JsonReader({
        root: 'results',
        fields: getFieldsListForReader()
    });

    //Build the results grid
    var subsetGrid = new Ext.grid.GridPanel({
        id: 'subsetGrid' + panelNumber,
        renderTo:myEl,
        store: listingSubsetStore,
        colModel: dataSetColModel,
        layout : 'fit',
        width : GLOBAL.resultsGridWidth,
        height : GLOBAL.resultsGridHeight,
        enableDragDrop : true,
        title:'Subset ' + panelNumber,
        ddGroup : 'resultsGridPanel-dd',
        ddText : 'Place this row.',
        viewConfig: {
            forceFit: true
        },
        tbar:['->',
            {
                text:'Clear Subset',
                tooltip:'Clear Subset',
                handler : function()
                {
                    /*
                     //Clear the search JSON.
                     GLOBAL["SearchJSON" + panelNumber] = {};
                     GLOBAL["SearchJSON" + panelNumber]["GridColumnList"] = [];
                     GLOBAL.columnList.split(',').forEach(function(item) {GLOBAL["SearchJSON" + panelNumber]["GridColumnList"].push(item)});
                     */

                    //Refresh the grid.
                    //Ext.getCmp('subsetGrid' + panelNumber).getStore().reload();
                }
            }
        ],
        style: 'margin: 30px auto'
    });

    var ddrowTarget = new Ext.dd.DropTarget(subsetGrid.container,
        {
            id: 'ddRowTargetId' + panelNumber,
            ddGroup: "resultsGridPanel-dd",
            notifyDrop : function(dd, e, data)
            {
                //Adding a record involves updating the search JSON object and refreshing the grid.
                //For each record we update the search JSON here.
                function addRow(record, index, allItems)
                {
                    addRecordToJSONSearch(record,panelNumber);
                }

                // Loop through the selections
                Ext.each(dd.dragData.selections ,addRow);

                //Refresh the grid.
                subsetGrid.getStore().reload();
            }
        }
    );
}


function addRecordToJSONSearch(record,panelNumber)
{
    if(!GLOBAL["SearchJSON" + panelNumber].Records)
    {
        GLOBAL["SearchJSON" + panelNumber].Records = [];
    }

    var newRecord = {};

    //Loops through each category in our record.
    for(var key in record.data)
    {
        //Make sure we have a value to add to the search category.
        if(record.data[key] != "")
        {
            //Initialize the array for this category if it doesn't already exists.
            if(!newRecord[key]) newRecord[key] = [];

            //Push the term into the array for its category if it doesn't already exist.
            if(!newRecord[key].has(record.data[key]))
            {
                newRecord[key].push(record.data[key]);
            }
        }
        else
        {
            newRecord[key] = [];
        }
    }

    GLOBAL["SearchJSON" + panelNumber].Records.push(newRecord);
}

/**
 * This toggles the rows of links that are hidden on or off.
 * @param moreLink
 * @param lessLink
 * @param resultRows
 */
function toggleMoreResults(moreLink,lessLink,resultRows)
{
    //This is the case where the more link is already hidden, we are going less.
    if(moreLink.style.display == 'none')
    {
        moreLink.style.display='';
        lessLink.style.display='none';
        resultRows.style.display='none';
    }
    else
    {
        moreLink.style.display='none';
        lessLink.style.display='';
        resultRows.style.display='';
    }
}

function toggleContactSampleSection(sampleTable)
{
    //This is the case where the more link is already hidden, we are going less.
    if(sampleTable.style.display == 'none')
    {
        sampleTable.style.display='';
    }
    else
    {
        sampleTable.style.display='none';
    }
}

//This will clear out the current search and load the landing page again.
function clearSearch()
{
    window.location=pageInfo.basePath+'/' + GLOBAL.explorerType + '/list';
}

/**
 * This function fires off when a user clicks on the count link in order to show the BioBank data.
 * The store id is the id of the row in the extjs data store.
 * @param dataStoreID
 */
function showBioBankResults(dataStoreID)
{
    //If the window is already open, close it.
    if(this.bioBankResults) bioBankResults.close();

    //Pull the JSON from the data store and convert the values to arrays.
    //This does bad things because it doesn't get a copy of the value, it creates a reference.
    var jsonData = GLOBAL.resultGridPanel.store.data.get(dataStoreID).json;

    jsonData["GridColumnList"] = GLOBAL["SearchJSON"]["GridColumnList"];

    var JSONDataToPass = convertJsonValuesToLists(jsonData);

    if(GLOBAL.SearchJSON.result_instance_id) JSONDataToPass["result_instance_id"] = GLOBAL["SearchJSON"]["result_instance_id"];

    //Load the biobank data window.
    bioBankResults = new Ext.Window
    ({
        id: 'bioBankResultsWindow',
        title: 'Detailed Sample Information',
        autoScroll:true,
        closable: true,
        buttons: [],
        resizable: true,
        height: 500,
        width: 500,
        autoLoad:
        {
            url: pageInfo.basePath+'/' + GLOBAL.explorerType + '/bioBank',
            scripts: true,
            nocache:true,
            discardUrl:true,
            method:'POST',
            jsonData : {"SearchJSON" : JSONDataToPass}
        }
    });

    //Show the window we just created.
    bioBankResults.show(viewport);
}

function showNewsUpdateDetail(newsid)
{
    //If the window is already open, close it.
    if(this.newsUpdateDetailsWindow) newsUpdateDetailsWindow.close();

    newsUpdateDetailsWindow = new Ext.Window
    ({
        id: 'newsUpdateDetailsWindow',
        title: 'Recent Update',
        closable: true,
        buttons: [],
        resizable: true,
        autoLoad:
        {
            url: pageInfo.basePath+'/newsUpdate/listDetailed?id=' + newsid,
            scripts: true,
            nocache:true,
            discardUrl:true,
            method:'GET'
        }
    });

    //Show the window we just created.
    newsUpdateDetailsWindow.show(viewport);

}

function gatherSampleContactInformation()
{

    var jsonDataToPost 					= {};
    jsonDataToPost.SearchJSON			= {};
    jsonDataToPost.SearchJSON.Records 	= [];

    for (var itemIterator=0; itemIterator<GLOBAL.resultGridPanel.selModel.selections.items.length; itemIterator++)
    {
        var selectedItem = GLOBAL.resultGridPanel.selModel.selections.items[itemIterator].json;

        jsonDataToPost.SearchJSON.Records.push(convertJsonValuesToLists(selectedItem));
    }

    jsonDataToPost["SearchJSON"]["GridColumnList"] = GLOBAL["SearchJSON"]["GridColumnList"];
    if(GLOBAL.SearchJSON.result_instance_id) jsonDataToPost["SearchJSON"]["result_instance_id"] = GLOBAL["SearchJSON"]["result_instance_id"];

    //If the window is already open, close it.
    if(this.bioBankResults) bioBankResults.close();

    if(jsonDataToPost.SearchJSON.Records.length <= 0) {
        alert("You have not selected any sample. Please select your sample(s) first.");
        return;
    }

    //Load the biobank data window.
    bioBankResults = new Ext.Window
    ({
        id: 'bioBankResultsWindow',
        title: 'Sample Contact Information',
        autoScroll:true,
        closable: true,
        buttons: [],
        resizable: true,
        height: 500,
        width: 500,
        autoLoad:
        {
            url: pageInfo.basePath+'/' + GLOBAL.explorerType + '/sampleContactScreen',
            scripts: true,
            nocache:true,
            discardUrl:true,
            method:'POST',
            jsonData : jsonDataToPost
        }
    });

    //Show the window we just created.
    bioBankResults.show(viewport);

}