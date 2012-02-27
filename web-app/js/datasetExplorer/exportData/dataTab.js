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
var exportMetadataStore;
function getDatadata(tab)
{
	exportMetadataStore = new Ext.data.JsonStore({
		url : pageInfo.basePath+'/dataExport/getMetaData',
		root : 'exportMetaData',
		fields : ['subsetId1', 'subsetName1', 'subset1', 'subsetId2', 'subsetName2', 'subset2', 'dataTypeId', 'dataTypeName', 'metadataExists'],
		autoLoad : false
	});
	
	exportMetadataStore.load({
		params : {result_instance_id1: GLOBAL.CurrentSubsetIDs[1], 
		          result_instance_id2: GLOBAL.CurrentSubsetIDs[2]}
	});
	
	exportMetadataStore.on('load', exportMetadataStoreLoaded);
	exportMetadataStore.on('loadexception', exportMetadataStoreLoadEx);
	//exportMetadataStoreLoaded();
}

function exportMetadataStoreLoadEx() {
	console.log('Exception while loading export meta data store');
}

function exportMetadataStoreLoaded()
{
	var foo = exportMetadataStore;
	var columns = prepareColumnModel(exportMetadataStore);
	var newStore = prepareNewStore(exportMetadataStore, columns);
	var dataExportToolbar = new Ext.Toolbar(
		{
			id : 'dataExportToolbar',
			title : 'dataExportToolbar',
			items : ['->', // aligns the items to the right
					new Ext.Toolbar.Button(
						{
				            id:'help',
				            tooltip:'Click for Data Export help',
				            iconCls: "contextHelpBtn",
				            handler: function(event, toolEl, panel){
						    	D2H_ShowHelp("1456",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
				            }
						}
					)]
		}
	);
    var dataTypesGridPanel = new Ext.grid.GridPanel({    	
    	id: "dataTypesGridPanel",
		store: newStore,
		columns: columns,
		title: "Instructions: Select the files that you would like to download by selecting the check-boxes and then click on Export Data button at the bottom to initiate the asynchronous job to download the data",
		viewConfig:	{
			forceFit : true,
			emptyText : 'No rows to display'
		}, 
		sm : new Ext.grid.RowSelectionModel({singleSelect : true}),
		layout : 'fit',
		width : 600,
        tbar:dataExportToolbar,
        buttons: [{
     	   id : 'dataTypesToExportRunButton',
    	   text : 'Export Data',
    	   handler : function() {
    		   createDataExportJob();
    	   }
       }],
        buttonAlign:'center',
	});
	
    analysisDataExportPanel.add(dataTypesGridPanel);
	analysisDataExportPanel.doLayout();
	analysisDataExportPanel.body.unmask();
}

function prepareColumnModel(store) {
	var columns = [];
	var columnModelPrepared = false;
	
	var this_column = [];
	this_column['name'] = 'dataTypeName';
	this_column['header'] = '';
	this_column['sortable'] = false;
	columns.push(this_column);
	
	var subsetsAdded = false;
	store.each(function (row) {
		if (!subsetsAdded) {
			var this_column = [];
			this_column['name'] = row.data.subsetId1;
			this_column['header'] = row.data.subsetName1;
			this_column['sortable'] = false;
			columns.push(this_column);
			
			this_column = [];
			this_column['name'] = row.data.subsetId2;
			this_column['header'] = row.data.subsetName2;
			this_column['sortable'] = false;
			columns.push(this_column);
			
			subsetsAdded = true;
		}
	});
	
	return columns;
}

function prepareOutString(files, subset, dataTypeId, metadataExists) {
	var outStr = '';
	files.each(function (file) {
		var dataCount = file.fileDataCount;
		if (dataCount > 1) {
			outStr += file.dataFormat + ' is available for ' + file.fileDataCount + ' patients';
			outStr += '<br/> Export (' + file.fileType + ')&nbsp;&nbsp;';
			outStr += '<input type="checkbox" name="SubsetDataTypeFileType" value="' + subset + '_';
			outStr += dataTypeId + '_' + file.fileType + '"/><br/><br/>';
			
			if (metadataExists)
				outStr += 'Metadata will be downloaded in a separate file';
		}
	});
	
	return outStr;
}

function prepareNewStore(store, columns) {
	var dataTypes;
	var data = [];
	
	var selectedCohortData = [];
	selectedCohortData['dataTypeId'] = '';
	selectedCohortData['dataTypeName'] = 'Selected Cohort';
	selectedCohortData['subset1'] = getQuerySummary(1);
	selectedCohortData['subset2'] = getQuerySummary(2);
	data.push(selectedCohortData);
	
	store.each(function (row) {
		var this_data = [];
		this_data['dataTypeId'] = row.data.dataTypeId;
		this_data['dataTypeName'] = row.data.dataTypeName;
		var outStr = prepareOutString(row.data.subset1, row.data.subsetId1, row.data.dataTypeId, row.data.metadataExists);
		this_data[row.data.subsetId1]= outStr;
		outStr = prepareOutString(row.data.subset2, row.data.subsetId2, row.data.dataTypeId, row.data.metadataExists);
		this_data[row.data.subsetId2]= outStr;
		
		data.push(this_data);
	});
	
	var myStore = new Ext.data.JsonStore({
		autoDestroy:true,
		root:'subsets',
		fields:columns,
		data:{subsets:[]}
	});
	
	myStore.loadData({subsets:data}, false);
	
	return myStore;
}

function createDataExportJob() {
	Ext.Ajax.request({						
		url: pageInfo.basePath+"/dataExport/createnewjob",
		method: 'POST',
		success: function(result, request){
			//Handle data export process
			runDataExportJob(result);
		},
		failure: function(result, request){
			Ext.Msg.alert('Status', 'Unable to create data export job.');
		},
		timeout: '1800000',
		params: {
					querySummary1 : getQuerySummary(1),
					querySummary2 : getQuerySummary(2),
					analysis:  "DataExport"
				}
	});
}

function runDataExportJob(result) {
	var jobNameInfo = Ext.util.JSON.decode(result.responseText);					 
	var jobName = jobNameInfo.jobName;

	showJobStatusWindow(result);
	var subsetDataTypeFiles = document.getElementsByName('SubsetDataTypeFileType');
	var selectedSubsetDataTypeFiles = [];
	for (var i = 0; i < subsetDataTypeFiles.length; i++) {
		if (subsetDataTypeFiles[i].checked) selectedSubsetDataTypeFiles.push(subsetDataTypeFiles[i].value);
	}
	Ext.Ajax.request(
		{						
			url: pageInfo.basePath+"/dataExport/runDataExport",
			method: 'POST',
			timeout: '1800000',
			params: Ext.urlEncode(
					{
						result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
						result_instance_id2 : GLOBAL.CurrentSubsetIDs[2],
						analysis            : 'DataExport',
						jobName             : jobName,
						selectedSubsetDataTypeFiles : selectedSubsetDataTypeFiles
					}
			) // or a URL encoded string
	});
	checkJobStatus(jobName);
}