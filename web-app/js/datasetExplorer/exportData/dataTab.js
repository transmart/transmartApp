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
function getDatadata()
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
}

function exportMetadataStoreLoaded()
{
	var foo = exportMetadataStore;
	
	var selectedCohortData = [];
	selectedCohortData['dataTypeId'] = '';
	selectedCohortData['dataTypeName'] = 'Selected Cohort';
	selectedCohortData['subset1'] = getQuerySummary(1);
	selectedCohortData['subset2'] = getQuerySummary(2);
	
	var columns = prepareColumnModel(exportMetadataStore, selectedCohortData);
	var newStore = prepareNewStore(exportMetadataStore, columns, selectedCohortData);
	var dataExportToolbar = new Ext.Toolbar(
		{
			id : 'dataExportToolbar',
			title : 'dataExportToolbar',
			items : ['->', // aligns the items to the right
                    new Ext.Toolbar.Button({
                        id:'advancedExport',
                        tooltip:'Advanced options for data export',
                        text: "Advanced Options",
                        handler: function(event, toolEl, panel){
                            showAdvancedOptions();
                        }
                    }),
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
        region : 'north',
		title: "Instructions: Select the check boxes to indicate the data types and file formats that are desired for export.  Then, click on the \"Export Data\" button at the bottom of the screen to initiate an asynchronous data download job.  To download your data navigate to the \"Export Jobs\" tab.",
		viewConfig:	{
			forceFit : true,
			emptyText : "No rows to display"
		}, 
		sm : new Ext.grid.RowSelectionModel({singleSelect : true}),
		layout : "fit",
		//width : 600,
        autoWidth : true,
        autoHeight : true,
        tbar:dataExportToolbar,
        buttons: [{
     	   id : "dataTypesToExportRunButton",
           tooltip : 'Trigger the export job',
    	   text : "Export Data",
    	   handler : function() {
    		   createDataExportJob();
    	   }
        }],
        buttonAlign:"center"
	});

    advancedDataExportPanel = new Ext.Panel({
        id: "advancedDataExportPanel",
        layout : "fit",
        region : 'south',
        autoScroll : true,
        autoHeight : true,
        autoWidth : true,
        hidden : true,
        autoLoad:
        {
            url : pageInfo.basePath+'/dataExport/advancedExport',
            method:'POST',
            callback: registerAdvancedExportDragAndDrop,
            evalScripts:true
        }
    });

    analysisDataExportPanel.add(dataTypesGridPanel);
    analysisDataExportPanel.add(advancedDataExportPanel);
	analysisDataExportPanel.doLayout();
	analysisDataExportPanel.body.unmask();
}

function showAdvancedOptions(){
    advancedDataExportPanel.show();
}

function registerAdvancedExportDragAndDrop(){
    //Get the export concepts div
    var exportableConceptsDiv = Ext.get("divExportableConcepts");
    var dtgExport = new Ext.dd.DropTarget(exportableConceptsDiv,{ddGroup : 'makeQuery'});
    dtgExport.notifyDrop = dropOntoAdvancedExportBox;
}

function dropOntoAdvancedExportBox(source, e, data){

    if(data.node.leaf==false && !data.node.isLoaded())
    {
        data.node.reload(function(){
            generateJSTree(data);
        });
    }
    else
    {
        generateJSTree(data);
    }

    return true;
}

function generateJSTree(data){

    var concepts = createConceptsFromNodeData(data);

    var jsTreeData = convertConceptsToJSTreeData(concepts);

    var currentTreeJson = jQuery("#divExportableConcepts").jstree("get_json");
    if(currentTreeJson[0] && currentTreeJson[0].attr){
        jsTreeData[1] = currentTreeJson;
    }

    drawJsTree(jsTreeData);
}

function createConceptsFromNodeData(data)
{
    var concepts= new Array();
    //Node must be folder so use children leafs
    if(data.node.leaf==false){

        var concept = convertNodeToConcept(data.node);
        concept.isLeaf = false;
        concepts.push(concept);

        //Adding this condition for certain nodes like Dosage and Response, where children of Dosage & Response are intentionally hidden
        if (data.node.childrenRendered && data.node.firstChild == null) {
            concepts.push(convertNodeToConcept(data.node));
        }

    }
    else{
        //Add the item to the input.
        var concept = convertNodeToConcept(data.node);
        concept.isLeaf = true;
        concepts.push(concept);
    }

    return concepts;
}

function drawJsTree(jsTreeData){
    jQuery("#divExportableConcepts").jstree({
        "json_data" : {
            "data" :jsTreeData,
            "ajax" : {
                url : pageInfo.basePath+'/dataExport/childConcepts',
                data : function(n){
                    return {parentConcept: n.data("dimcode")};
                }
            }
        },
        "types" : {
            "valid_children" : [ "T","N","D" ],
            "types" : {
                "T" : {
                    "icon" : {
                        "image" : "../images/alpha.gif"
                    }
                },
                "N" : {
                    "icon" : {
                        "image" : "../images/numeric.gif"
                    }
                },
                "D" : {
                    "icon" : {
                        "image" : "../images/calendar.gif"
                    }
                }
            }
        },
        "themes" : {
            "theme" : "classic",
            "dots" : true,
            "icons" : true
        },
        "contextmenu" : {
            items: customContextMenu
        },
        "plugins": ["themes", "json_data", "types", "contextmenu", "crrm"]
    });
}

function customContextMenu(node) {
    // The default set of all items
    var items = {
        deleteItem: { // The "delete" menu item
            label: "Delete",
            action: function (node) {this.remove(node);}
        }
    };

    return items;
}

function convertConceptsToJSTreeData(concepts){
    var data=new Array();

    for(var i = 0; i<concepts.length; i++){
        var concept = concepts[i];

        var type = '';

        if(concept.isLeaf){//Draw a leaf node with the appropriate type icons.
            if(concept.oktousevalues=='Y'){
                type = 'N';
            }else{
                type= 'T';
            }

            //Date concepts get a special type.
            if(concept.nodeType == "dleaficon")
            {
                type = 'D';
            }

            data.push({
                "data": concept.name,
                "metadata": {dimcode:concept.dimcode},
                "attr" : {rel:type}
            });
        }else{//Draw a exandible folder icon.
            data.push({
                "state": "closed",
                "data": concept.name,
                "metadata": {dimcode:concept.dimcode},
                "children" : [],
                "attr" : {rel:type}
            });
        }

    }
    return data;
}

function clearExportableConcepts(){
    //Clear the drag and drop div.
    var qc = Ext.get('divExportableConcepts');

    for(var i=qc.dom.childNodes.length-1;i>=0;i--)
    {
        var child=qc.dom.childNodes[i];
        qc.dom.removeChild(child);
    }

}

function hideAdvancedExport(){
    clearExportableConcepts();
    advancedDataExportPanel.hide();
}

function readAdvancedExportOptions(){
    var treeJson = jQuery("#divExportableConcepts").jstree("get_json");

    var selectedConcepts = readTreeNode(treeJson);

    return selectedConcepts;
}

function readTreeNode(treeNode){
    var nodeConcepts = "";
    for(var i = 0; i<treeNode.length; i++){
        var currentNode = treeNode[i];
        if(currentNode.children){//If a node has children, only add the children nodes.
            nodeConcepts = nodeConcepts + readTreeNode(currentNode.children)
        }else{//If a node does not have children add the node itself.
            if(currentNode.metadata.iconCls=='T' || currentNode.metadata.iconCls=='N'){
                nodeConcepts = nodeConcepts + "|" + currentNode.metadata.dimcode +"DELIMITER"+"LA";
            }else{
                nodeConcepts = nodeConcepts + "|" + currentNode.metadata.dimcode +"DELIMITER"+"FA";
            }
        }
    }
    return nodeConcepts;
}

function prepareColumnModel(store, selectedCohortData) {
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
			if (selectedCohortData['subset2'] != null && selectedCohortData['subset2'].length > 1) {
				this_column = [];
				this_column['name'] = row.data.subsetId2;
				this_column['header'] = row.data.subsetName2;
				this_column['sortable'] = false;
				columns.push(this_column);
			}
			subsetsAdded = true;
		}
	});
	
	return columns;
}

function prepareOutString(files, subset, dataTypeId, metadataExists) {
	var outStr = '';
	var dataCountExists = false;
	files.each(function (file) {
		var dataCount = file.fileDataCount;
		if (dataCount >= 1) {
			if (!dataCountExists) dataCountExists = true;
			outStr += createSelectBoxHtml(file, subset, dataTypeId)
		} else {
			if (file.platforms){
				file.platforms.each(function (platform){
					if(platform.fileDataCount>0){
						dataCountExists=true;
						outStr += createSelectBoxHtml(file, subset, dataTypeId, platform)
					}
				});
			}else{
				outStr += file.dataFormat + ' is not available. ';
				//outStr += (file.fileDataCount != null) ? file.fileDataCount : '0';
				//outStr += ' patients were found.'
				outStr += '<br/><br/>'
			}
		}
		
	});
	
	if (dataCountExists && metadataExists)
		outStr += 'Metadata will be downloaded in a separate file.';
	return outStr;
}

function createSelectBoxHtml(file, subset, dataTypeId, platform){
	outStr = '';
	if(platform){
		outStr += file.dataFormat + ' is available for </br/>' +platform.gplTitle +": "+ platform.fileDataCount + ' patients';
		outStr += '<br/> Export (' + file.fileType + ')&nbsp;&nbsp;';
		outStr += '<input type="checkbox" name="SubsetDataTypeFileType"';
		outStr += ' value="' + subset + '_' + dataTypeId + '_' + file.fileType + '_' + platform.gplId + '"';
		outStr += ' id="' + subset + '_' + dataTypeId + '_' + file.fileType + '_' + platform.gplId + '"';
		outStr += ' /><br/><br/>';
	}else{
		outStr += file.dataFormat + ' is available for ' + file.fileDataCount + ' patients';
		outStr += '<br/> Export (' + file.fileType + ')&nbsp;&nbsp;';
		outStr += '<input type="checkbox" name="SubsetDataTypeFileType"';
		outStr += ' value="' + subset + '_' + dataTypeId + '_' + file.fileType + '"';
		outStr += ' id="' + subset + '_' + dataTypeId + '_' + file.fileType + '"';
		outStr += ' /><br/><br/>';
	}
	
	return outStr
}

function prepareNewStore(store, columns, selectedCohortData) {
	//Remove existing check-boxes
	var subsetDataTypeFiles = document.getElementsByName('SubsetDataTypeFileType');
	while (subsetDataTypeFiles.length >= 1) {
		subsetDataTypeFiles[0].parentNode.removeChild(subsetDataTypeFiles[0])
	}
	
	var dataTypes;
	var data = [];
	data.push(selectedCohortData);
	
	store.each(function (row) {
		var this_data = [];
		this_data['dataTypeId'] = row.data.dataTypeId;
		this_data['dataTypeName'] = row.data.dataTypeName;
		var outStr = prepareOutString(row.data.subset1, row.data.subsetId1, row.data.dataTypeId, row.data.metadataExists);
		this_data[row.data.subsetId1]= outStr;
		if (selectedCohortData['subset2'] != null && selectedCohortData['subset2'].length > 1) {
			outStr = prepareOutString(row.data.subset2, row.data.subsetId2, row.data.dataTypeId, row.data.metadataExists);
			this_data[row.data.subsetId2]= outStr;
		}
		
		data.push(this_data);
	});
	
	var myStore = new Ext.data.JsonStore({
		id: 'metadataStore',
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

	var messages = {
		cancelMsg: "Your Job has been cancelled.", 
		backgroundMsg: "Your job has been put into background process. Please check the job status in the 'Export Jobs' tab."
	}
	showJobStatusWindow(result, messages);
	var subsetDataTypeFiles = document.getElementsByName('SubsetDataTypeFileType');
	var selectedSubsetDataTypeFiles = [];
	for (var i = 0; i < subsetDataTypeFiles.length; i++) {
		if (subsetDataTypeFiles[i].checked) selectedSubsetDataTypeFiles.push(subsetDataTypeFiles[i].value);
	}

    //Read dragged concepts from advanced options box
    var draggedConcepts = readAdvancedExportOptions();

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