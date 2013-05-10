/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

var exportMetadataStore;

function getImperialHeatmapData()
{
    Ext.Ajax.request({
        url : pageInfo.basePath + "/imperialHeatmap/getData",
        method : 'POST',
        params : {
            'result_instance_id1': GLOBAL.CurrentSubsetIDs[1],
            'result_instance_id2': GLOBAL.CurrentSubsetIDs[2]
        },
        success : function(result, request) {
            analysisHeatmapPanel.body.unmask();
            completeImperialHeatmapData(result.responseText);
        },
        failure : function(result, request) {

        },
        timeout : '600000'
    })
}

function completeImperialHeatmapData(data)
{
    bonjour = eval('(' + data + ')');
    min = max = stt = true;
    table = document.createElement('table');
    for(j = 0; j <  bonjour['microarray'].length; j++)
        for(i = 0; i < bonjour['microarray'][j].length; i++)
            if (i && j && (bonjour['microarray'][0][i].substr(0, 1) == "S"))
            {
                if (stt && !(stt = false))
                    min = max = parseFloat(bonjour['microarray'][j][i]);
                if (parseFloat(bonjour['microarray'][j][i]) < min)
                    min = parseFloat(bonjour['microarray'][j][i]);
                if (parseFloat(bonjour['microarray'][j][i]) > max)
                    max = parseFloat(bonjour['microarray'][j][i]);
            }

    var rainbowL = new Rainbow();
    var rainbowR = new Rainbow();
    rainbowL.setSpectrum('163BA1', 'D3D5F2');
    rainbowL.setNumberRange(min, (min + max) / 2);
    rainbowR.setSpectrum('F2D3D3', 'A11616');
    rainbowR.setNumberRange((min + max) / 2, max);

    for(j = 0; j <  bonjour['microarray'].length; j++)
    {
        line = document.createElement('tr');
        for(i = 0; i < bonjour['microarray'][j].length; i++)
        {
            cell = document.createElement('td');
            cell.appendChild(document.createTextNode(bonjour['microarray'][j][i]));
            cell.style.padding="2px";
            if (i && j && (bonjour['microarray'][0][i].substr(0, 1) == "S"))
            {
                if ((tmp = parseFloat(bonjour['microarray'][j][i])) >= (min + max) / 2)
                    cell.style.backgroundColor="#" + rainbowR.colourAt(parseFloat(bonjour['microarray'][j][i]));
                else
                    cell.style.backgroundColor="#" + rainbowL.colourAt(parseFloat(bonjour['microarray'][j][i]));
            }
            else if (j % 2)
                cell.style.backgroundColor="#dddddd";
            line.appendChild(cell)
        }
        table.appendChild(line);
    }

    table.style.width="100%";
    table.style.padding="5px";
    table.style.fontSize="10px";
    table.style.fontFamily="tahoma,arial,helvetica";
    table.cellSpacing="0";

    var tmp = document.createElement("div");
    tmp.appendChild(table);
    analysisHeatmapPanel.setBody(tmp.innerHTML);
    analysisHeatmapPanel.body.setStyle('overflow', 'auto');
}

function Rainbow()
{
    var gradients = null;
    var minNum = 0;
    var maxNum = 100;
    var colours = ['ff0000', 'ffff00', '00ff00', '0000ff'];
    setColours(colours);

    function setColours (spectrum)
    {
        if (spectrum.length < 2) {
            throw new Error('Rainbow must have two or more colours.');
        } else {
            var increment = (maxNum - minNum)/(spectrum.length - 1);
            var firstGradient = new ColourGradient();
            firstGradient.setGradient(spectrum[0], spectrum[1]);
            firstGradient.setNumberRange(minNum, minNum + increment);
            gradients = [ firstGradient ];

            for (var i = 1; i < spectrum.length - 1; i++) {
                var colourGradient = new ColourGradient();
                colourGradient.setGradient(spectrum[i], spectrum[i + 1]);
                colourGradient.setNumberRange(minNum + increment * i, minNum + increment * (i + 1));
                gradients[i] = colourGradient;
            }

            colours = spectrum;
            return this;
        }
    }

    this.setColors = this.setColours;

    this.setSpectrum = function ()
    {
        setColours(arguments);
        return this;
    }

    this.setSpectrumByArray = function (array)
    {
        setColours(array);
        return this;
    }

    this.colourAt = function (number)
    {
        if (isNaN(number)) {
            throw new TypeError(number + ' is not a number');
        } else if (gradients.length === 1) {
            return gradients[0].colourAt(number);
        } else {
            var segment = (maxNum - minNum)/(gradients.length);
            var index = Math.min(Math.floor((Math.max(number, minNum) - minNum)/segment), gradients.length - 1);
            return gradients[index].colourAt(number);
        }
    }

    this.colorAt = this.colourAt;

    this.setNumberRange = function (minNumber, maxNumber)
    {
        if (maxNumber > minNumber) {
            minNum = minNumber;
            maxNum = maxNumber;
            setColours(colours);
        } else {
            throw new RangeError('maxNumber (' + maxNumber + ') is not greater than minNumber (' + minNumber + ')');
        }
        return this;
    }
}
function ColourGradient()
{
    var startColour = 'ff0000';
    var endColour = '0000ff';
    var minNum = 0;
    var maxNum = 100;

    this.setGradient = function (colourStart, colourEnd)
    {
        startColour = getHexColour(colourStart);
        endColour = getHexColour(colourEnd);
    }

    this.setNumberRange = function (minNumber, maxNumber)
    {
        if (maxNumber > minNumber) {
            minNum = minNumber;
            maxNum = maxNumber;
        } else {
            throw new RangeError('maxNumber (' + maxNumber + ') is not greater than minNumber (' + minNumber + ')');
        }
    }

    this.colourAt = function (number)
    {
        return calcHex(number, startColour.substring(0,2), endColour.substring(0,2))
            + calcHex(number, startColour.substring(2,4), endColour.substring(2,4))
            + calcHex(number, startColour.substring(4,6), endColour.substring(4,6));
    }

    function calcHex(number, channelStart_Base16, channelEnd_Base16)
    {
        var num = number;
        if (num < minNum) {
            num = minNum;
        }
        if (num > maxNum) {
            num = maxNum;
        }
        var numRange = maxNum - minNum;
        var cStart_Base10 = parseInt(channelStart_Base16, 16);
        var cEnd_Base10 = parseInt(channelEnd_Base16, 16);
        var cPerUnit = (cEnd_Base10 - cStart_Base10)/numRange;
        var c_Base10 = Math.round(cPerUnit * (num - minNum) + cStart_Base10);
        return formatHex(c_Base10.toString(16));
    }

    formatHex = function (hex)
    {
        if (hex.length === 1) {
            return '0' + hex;
        } else {
            return hex;
        }
    }

    function isHexColour(string)
    {
        var regex = /^#?[0-9a-fA-F]{6}$/i;
        return regex.test(string);
    }

    function getHexColour(string)
    {
        if (isHexColour(string)) {
            return string.substring(string.length - 6, string.length);
        } else {
            var colourNames =
                [
                    ['red', 'ff0000'],
                    ['lime', '00ff00'],
                    ['blue', '0000ff'],
                    ['yellow', 'ffff00'],
                    ['orange', 'ff8000'],
                    ['aqua', '00ffff'],
                    ['fuchsia', 'ff00ff'],
                    ['white', 'ffffff'],
                    ['black', '000000'],
                    ['gray', '808080'],
                    ['grey', '808080'],
                    ['silver', 'c0c0c0'],
                    ['maroon', '800000'],
                    ['olive', '808000'],
                    ['green', '008000'],
                    ['teal', '008080'],
                    ['navy', '000080'],
                    ['purple', '800080']
                ];
            for (var i = 0; i < colourNames.length; i++) {
                if (string.toLowerCase() === colourNames[i][0]) {
                    return colourNames[i][1];
                }
            }
            throw new Error(string + ' is not a valid colour.');
        }
    }
}

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
		title: "Instructions: Select the check boxes to indicate the data types and file formats that are desired for export.  Then, click on the \"Export Data\" button at the bottom of the screen to initiate an asynchronous data download job.  To download your data navigate to the \"Export Jobs\" tab.",
		viewConfig:	{
			forceFit : true,
			emptyText : "No rows to display"
		}, 
		sm : new Ext.grid.RowSelectionModel({singleSelect : true}),
		layout : "fit",
		width : 600,
        tbar:dataExportToolbar,
        buttons: [{
     	   id : "dataTypesToExportRunButton",
    	   text : "Export Data",
    	   handler : function() {
    		   createDataExportJob();
    	   }
        }],
        buttonAlign:"center"
	});
	
    analysisDataExportPanel.add(dataTypesGridPanel);
	analysisDataExportPanel.doLayout();
	analysisDataExportPanel.body.unmask();
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