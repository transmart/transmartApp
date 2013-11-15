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
  

/* SubsetTool.js
Jeremy M. Isikoff
Recombinant */
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
			createMainExplorerWindow();
			
			Ext.getCmp('westPanel').body.setStyle('border','2px solid #D0D0D0');
			Ext.getCmp('centerPanel').body.setStyle('border','2px solid #D0D0D0');
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
}


//When a main category is selected we need to reload both panels.
function toggleMainCategorySelection(searchTerm,searchCategory)
{
	//Reinitialize our JSON and the array for this category.
	GLOBAL.SearchJSON = {};
	GLOBAL.SearchJSON[searchCategory] = [];
	
	//Add the list of columns.
	GLOBAL.SearchJSON["GridColumnList"] = [];
	//Split the default column list into an array.
	var columnSplitArray = GLOBAL.columnList.split(',');
	
	//Loop over the default column array and add it to the JSON object.
	for(var i=0; i<columnSplitArray.length; i++)
	{
		GLOBAL.SearchJSON["GridColumnList"].push(columnSplitArray[i]);
	}
	
	//Push the term into the array for its category.
	GLOBAL.SearchJSON[searchCategory].push(searchTerm);
	
	var westPanelURL = pageInfo.basePath+'/sampleExplorer/showWestPanelSearch';
	var centerPanelURL = pageInfo.basePath+'/sampleExplorer/showDataSetResults';
	
	//Reload the west panel with new content.
	Ext.getCmp('westPanel').load(
			{
				url : westPanelURL,
				jsonData : {"SearchJSON" : GLOBAL.SearchJSON}
			}
		  );

	//Reload the east panel with new content. The gridpanel is loaded in the callback function.
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
	Ext.getCmp('addsubset').setVisible(true);

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
	var westPanelURL = pageInfo.basePath+'/sampleExplorer/showWestPanelSearch';
	
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
	var dataStoreURL = pageInfo.basePath+'/sampleExplorer/getDataSetResults?mainTerm=' + createValuesString(GLOBAL.SearchJSON);
	
	//We need to post our JSON filter to the URL to get our results.
	var dataStoreProxy = new Ext.data.HttpProxy({
		url: dataStoreURL,
		method: 'POST',
		jsonData : {"SearchJSON" : GLOBAL.SearchJSON}
		});
	
	//This reader is used to put the JSON into the data store.
	var dataStoreReader = new Ext.data.JsonReader({
		root: 'results',
		fields: [
		{name: 'Pathology', mapping: 'Pathology'},
		{name: 'Tissue', mapping: 'Tissue'},
		{name: 'DataSet', mapping: 'DataSet'},
		{name: 'DataType', mapping: 'DataType'},
		{name: 'Source_Organism', mapping: 'Source_Organism'},
		{name: 'Sample_Treatment', mapping: 'Sample_Treatment'},
		{name: 'Subject_Treatment', mapping: 'Subject_Treatment'},
		{name: 'BioBank', mapping: 'BioBank'},
		{name: 'Timepoint', mapping: 'Timepoint'},
		{name: 'count', mapping: 'count'}
		]
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
        columns: [
            {header: 'DataSet',			renderer: renderDSELink, sortable: true, dataIndex: 'DataSet', width: 10},
            {header: 'DataType',		sortable: true, dataIndex: 'DataType', width: 15},
            {header: 'Pathology',		sortable: true, dataIndex: 'Pathology', width: 15},
            {header: 'Tissue',			sortable: true, dataIndex: 'Tissue', width: 12},
            {header: 'Source Organism',	sortable: true, dataIndex: 'Source_Organism', width: 10},
            {header: 'Sample Treatment',		sortable: true, dataIndex: 'Sample_Treatment', width: 14},
            {header: 'Subject Treatment',		sortable: true, dataIndex: 'Subject_Treatment', width: 14},
            {header: 'Timepoint',		sortable: true, dataIndex: 'Timepoint', hidden:true},
            {header: 'Samples',			sortable: true, dataIndex: 'count', width: 7,renderer: renderSampleLink}
        ]
    });

	var listingGroupedStore = new Ext.data.GroupingStore({
		id : 'dataGridDataStoreGroup',
		autoLoad: true,
		remoteSort: false,
		proxy: dataStoreProxy,
		reader: dataStoreReader,
		sortInfo:{field: 'count', direction: "ASC"},
        groupField:'DataSet'
    })	

	//This draws the actual grid panel to the DIV on the screen.
	GLOBAL.resultGridPanel = new Ext.grid.GridPanel({
		id: 'resultsGridPanel',
	    title: createValuesString(GLOBAL.SearchJSON),
	    renderTo: "divDataSetResults",
	    layout : 'fit',
		width : 900,
		height : 300,
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
		
	addTabbedSubset();
	addTabbedSubset();
	addTabbedSubset();
	GLOBAL.tabs.setActiveTab(1);
	
	//This one line of code is the product of 8 hours of fidgeting. In order for the tabs to display properly I had to initialize an empty items collection. This leaves a weird empty tab that needs to be removed.
	//I might just be missing some kind of rendering event for the tab panel but for now we'll just initialize and remove the extra.
	GLOBAL.tabs.remove(0);
}

function renderDSELink(val)
{
	return val + '&nbsp;<a href="' + pageInfo.basePath + '/datasetExplorer/index?DataSetName=' + val + '" target="_blank" title="Click to view ' + val + ' in Dataset Explorer!"><img src="/transmart/images/linkext7.gif"></a>';
}

//This renderer draws a checkbox.
function renderIncludeCheckBox(val)
{
	return '<input type="checkbox" onClick="modifyDataList();"></input>';
}

//When we remove or add a column from the grid panel we need to re-query Solr to get the results with or without this column.
function dataGridColumnRemoved(cm,columnIndex,hidden)
{
	//We need to find out what column is being acted on.
	var columnNameFromHeader = Ext.getCmp("resultsGridPanel").getColumnModel().getColumnHeader(columnIndex);
	
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
		GLOBAL.SearchJSON["GridColumnList"].push(columnNameFromHeader)
	}
	
	//Reload the title to the grid.
	GLOBAL.resultGridPanel.getStore().reload();
	GLOBAL.resultGridPanel.setTitle(createValuesString(GLOBAL.SearchJSON));
}

//This renderer creates a link that calls the biobank results function with the id from the datastore corresponding to the row we clicked on.
function renderSampleLink(value,id,row) {
	
	var returnString = "";
	
	if(row.data.BioBank == 'Yes')
	{
		returnString += '<a href="#" onClick="showBioBankResults(\'' + row.id + '\')">' + value + '</a>&nbsp;&nbsp;&nbsp;<img src="/transmart/images/test_tube.png">';
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
		  width : 900,
		  height : 300
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
	
	//Split the default column list into an array.
	var columnSplitArray = GLOBAL.columnList.split(',');

	//Loop over the default column array and add it to the JSON object.
	for(var i=0; i<columnSplitArray.length; i++)
	{
		GLOBAL["SearchJSON" + panelNumber]["GridColumnList"].push(columnSplitArray[i]);
	}	
		
	//Create the DIV which holds the GridPanel.
	myEl = new Ext.Element(document.createElement('div'));
	Ext.get('subsetpanel' + panelNumber).appendChild(myEl)
	
	//This is the URL that will return the data we want in JSON format.
	var dataStoreURL = pageInfo.basePath+'/sampleExplorer/getDataSetResults';	
	
	//This reader is used to put the JSON into the data store.
	var dataStoreReader = new Ext.data.JsonReader({
		root: 'results',
		fields: [
		{name: 'Pathology', mapping: 'Pathology'},
		{name: 'Tissue', mapping: 'Tissue'},
		{name: 'DataSet', mapping: 'DataSet'},
		{name: 'DataType', mapping: 'DataType'},
		{name: 'Source_Organism', mapping: 'Source_Organism'},
		{name: 'Sample_Treatment', mapping: 'Sample_Treatment'},
		{name: 'Subject_Treatment', mapping: 'Subject_Treatment'},
		{name: 'BioBank', mapping: 'BioBank'},
		{name: 'count', mapping: 'count'}
		]
		});	
	
	//This dictates how the columns will show up in our grid.
	var dataSetColModel = new Ext.grid.ColumnModel({
        defaults: {
            sortable: true
        },
        columns: [
            {header: 'DataSet',				sortable: true, dataIndex: 'DataSet', width: 10},
            {header: 'DataType',			sortable: true, dataIndex: 'DataType', width: 15},
            {header: 'Pathology',			sortable: true, dataIndex: 'Pathology', width: 15},
            {header: 'Tissue',				sortable: true, dataIndex: 'Tissue', width: 12},
            {header: 'Source Organism',		sortable: true, dataIndex: 'Source_Organism', width: 10},
            {header: 'Sample Treatment',	sortable: true, dataIndex: 'Sample_Treatment', width: 14},
            {header: 'Subject Treatment',	sortable: true, dataIndex: 'Subject_Treatment', width: 14},
            {header: 'Samples',				sortable: true, dataIndex: 'count', width: 7}
        ]
    });	
	
	//We need to post our JSON filter to the URL to get our results.
	var listingSubsetStoreProxy = new Ext.data.HttpProxy({
		id : 'subsetDataGridDataStore' + panelNumber,
		url: dataStoreURL,
		method: 'POST',
		jsonData : {"SearchJSON" : GLOBAL["SearchJSON" + panelNumber]},
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
		fields: [
		{name: 'Pathology', mapping: 'Pathology'},
		{name: 'Tissue', mapping: 'Tissue'},
		{name: 'DataSet', mapping: 'DataSet'},
		{name: 'DataType', mapping: 'DataType'},
		{name: 'Source_Organism', mapping: 'Source_Organism'},
		{name: 'Sample_Treatment', mapping: 'Sample_Treatment'},
		{name: 'Subject_Treatment', mapping: 'Subject_Treatment'},
		{name: 'BioBank', mapping: 'BioBank'},
		{name: 'count', mapping: 'count'}
		]
		});	
	
	//Build the results grid
    var subsetGrid = new Ext.grid.GridPanel({
        id: 'subsetGrid' + panelNumber,
        renderTo:myEl,
        store: listingSubsetStore,
        colModel: dataSetColModel,
        layout : 'fit',
		width : 900,
		height : 300,
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
        				addRecordToJSONSearch(record,GLOBAL["SearchJSON" + panelNumber])
        		    }
        		      
        		    // Loop through the selections
        		    Ext.each(dd.dragData.selections ,addRow);
        		     
        		    //Refresh the grid.
        		    subsetGrid.getStore().reload();
        		}
    		}
    	);	
}


function addRecordToJSONSearch(record,JSONSearch)
{
	//Loops through each category in our record.
	for(var key in record.data)
	{
    	//Make sure we have a value to add to the search category.
		if(record.data[key] != "")
		{
	    	//Initialize the array for this category if it doesn't already exists.
	    	if(!JSONSearch[key]) JSONSearch[key] = [];	

	    	//Push the term into the array for its category if it doesn't already exist.
	    	if(!JSONSearch[key].has(record.data[key]))
    		{
	    		JSONSearch[key].push(record.data[key]);
    		}
		}
		else
		{
			JSONSearch[key] = [];
		}
	}
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

//This will clear out the current search and load the landing page again.
function clearSearch()
{
	window.location=pageInfo.basePath+'/sampleExplorer/list'
}

//This will create a string of the selected items from a JSON object.
function createValuesString(JSONObject)
{
	//This will be the string we return.
	var valueString = '';
	
	//Iterate over the categories in the JSON object.
    for(var key in JSONObject)
    {
    	//If the categories array actually has items in it, we need to add them to a string.
    	if(JSONObject[key].length > 0) 
		{
    		//WE need a comma if the string isn't empty.
    		if(valueString != '') valueString += ', ';
    		
    		//Replace the underscores in the category with a space.
    		var newKey = key.replace('_',' ');
    		
    		//Add the values to our string.
    		valueString += newKey + ": (" + JSONObject[key] + ")";
		}
    }
		
	return valueString;
}

/*
 * Iterate through an array and remove an item if it is present.
 */
function removeFromArray(string, array)
{
	for(i in array)
	{
		if(array[i] == string)
		{
			array.splice(i, 1);
		}
	}
	
	return array;
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
	var jsonData = GLOBAL.resultGridPanel.store.data.get(dataStoreID).json;
	
	var JSONDataToPass = convertJsonValuesToLists(jsonData);
	
	//Load the biobank data window.
	bioBankResults = new Ext.Window
	({
        id: 'bioBankResultsWindow',
        title: 'BioBank',
        autoScroll:true,
        closable: true,
        buttons: [],
        resizable: true,
        height: 200,
        autoLoad:
        {
            url: pageInfo.basePath+'/sampleExplorer/bioBank',
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

//We may have something like .. [DataType:Gene Expression, count:236, Pathology:Liver, Cancer of, Tissue:Liver [A03.620], DataSet:PRECOS]
//But we need the values to be lists, not just strings.
//This function will loop over the keys and make the values arrays, lists, whatever they are called.
function convertJsonValuesToLists(JsonToConvert)
{
	var newJsonData = {};
	
	for (jsonItem in JsonToConvert)
		{
			var tempValue = JsonToConvert[jsonItem];
			
			newJsonData[jsonItem] = [];
			
			newJsonData[jsonItem].push(tempValue);
		}

	return newJsonData
}

//If we have an array like [{blah:"blah"},{blah:"blah2"}] we want to convert it to [{blah:["blah","blah2"]}]
function arrayOfJsonToJsonData(JsonToConvert)
{
	//This will be the final object we return.
	var finalJsonData = {}
	
	//Loop through each of the sets of JSON data.
	for (JsonSet in JsonToConvert)
	{
		//For each set we have multiple pairs of key/values that we need to add to the final object.
		for (jsonItem in JsonToConvert[JsonSet].data)
		{
			//Make sure the key exists.
			if(!finalJsonData[jsonItem]) finalJsonData[jsonItem] = [];
			
			//Add this key/value pair to the final hash.
			finalJsonData[jsonItem].push(JsonToConvert[JsonSet].data[jsonItem]);
		
		}
	}

	//Now we need to distinct each of the JSON lists.
	for (jsonItem in finalJsonData)
	{
		finalJsonData[jsonItem] = finalJsonData[jsonItem].unique()
	}
	
	return finalJsonData;
}

//This might be inefficient. 
//Return new array with duplicate values removed
Array.prototype.unique =
  function() {
    var a = [];
    var l = this.length;
    for(var i=0; i<l; i++) {
      for(var j=i+1; j<l; j++) {
        // If this[i] is found later in the array
        if (this[i] === this[j])
          j = ++i;
      }
      a.push(this[i]);
    }
    return a;
  };
  
  Array.prototype.has=function(v){
	  for (i=0; i<this.length; i++){
	  if (this[i]==v) return true;
	  }
	  return false;
	  }  
  
/*function genePatternLogin() {
	document.getElementById("gplogin").src = pageInfo.basePath + '/analysis/gplogin';
}*/
  