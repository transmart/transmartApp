/**
 * Extend Ext.GridPanel to have 'afterrender' event
 * @type {*}
 */
var CustomGridPanel = Ext.extend(Ext.grid.GridPanel, {
    constructor : function (config) {
        CustomGridPanel.superclass.constructor.apply(this, arguments);
        // add 'afterrender' or any other event here
        this.addEvents({
            'afterrender': true
        });
    },
    afterRender: function () {
        Ext.grid.GridPanel.superclass.afterRender.call(this);
        this.view.layout();
        if (this.deferRowRender) {
            this.view.afterRender.defer(10, this.view);
        } else {
            this.view.afterRender();
        }
        this.viewReady = true;
        this.fireEvent('afterrender');
    }
});

/**
 * Function to check if row element already existing in the Grid Panel
 * When it is, convert them to drop zones.
 */
CustomGridPanel.prototype.dropZonesChecker = function () {

    var _this = this;

    // init row element checker task
    var checkTask = {
        run: function () {

            // init rows array
            var rows = [];

            // check if view already have rows represent the number of records
            for (var i = 1; i <= _this.records.length; i++) {
                var recordData = _this.records[i - 1].data;
                var _rowEl = _this.getView().getRow(i);
                rows.push(_rowEl);
                var _dtgI = new Ext.dd.DropTarget(_rowEl, {ddGroup: 'makeQuery'});
                var isWrongNode = function(rd, data) {
                    return rd.subset1.ontologyTermKeys
                        && rd.subset1.ontologyTermKeys.indexOf(data.node.id) < 0
                        && rd.subset2.ontologyTermKeys
                        && rd.subset2.ontologyTermKeys.indexOf(data.node.id) < 0
                            //TODO We could check for folders
                        || (data.node.attributes.visualattributes.indexOf('HIGH_DIMENSIONAL') >= 0 && rd.dataTypeId == 'CLINICAL');
                };
                var getDropHandler = function(target, rd) {
                    return function (source, e, data) {
                        if (isWrongNode(rd, data)) {
                            return false;
                        }
                        //`this` is used inside function `dropOntoVariableSelection` function
                        target.dropOntoVariableSelection = dropOntoVariableSelection
                        jQuery(target.el.dom).find('input[name=download_dt]').prop('checked', true);
                        return target.dropOntoVariableSelection(source, e, data);
                    };
                };
                _dtgI.notifyDrop = getDropHandler(_dtgI, recordData);

                var getEnterHandler = function(target, rd) {
                    return function(dd, e, data) {
                        if(target.overClass){
                            target.el.addClass(target.overClass);
                        }
                        return isWrongNode(rd, data) ? target.dropNotAllowed : target.dropAllowed;
                    };
                };
                _dtgI.notifyEnter = getEnterHandler(_dtgI, recordData);

                var getOverHandler = function(target, rd) {
                    return function(dd, e, data) {
                        return isWrongNode(rd, data) ? target.dropNotAllowed : target.dropAllowed;
                    };
                };
                _dtgI.notifyOver = getOverHandler(_dtgI, recordData);
            }

            // stop runner when it's already found the elements
            if (rows.length > 0) {
                runner.stopAll();
            }

        },

        interval: 500 // repeat every 0.5 seconds
    };

    // Need to have a task runner since there's no other way to retrieve
    // row elements after they're rendered.

    var runner = new Ext.util.TaskRunner();  // define a runner
    runner.start(checkTask); // start the task
};

/**********************************************************************************************************************/

/**
 * Where everything starts. Create Data Export instance, load the data store to display all data in a cohort
 * that can be exported.
 */
function getDatadata() {

    // create new instance of data export
    var dataExport = new DataExport();

    // load export metadata
    dataExport.exportMetaDataStore.load({
        params: {result_instance_id1: GLOBAL.CurrentSubsetIDs[1], result_instance_id2: GLOBAL.CurrentSubsetIDs[2]},
        scope: dataExport,
        callback: dataExport.displayResult
    });
}

/**
 *  Data Export Object
 * @constructor
 * @param errorHandler function taking response status and message
 */
var DataExport = function() {

    this.records = null;

    this.exportMetaDataStore = null;

    /**
     * Get export metadata store
     * @returns {Ext.data.JsonStore}
     * @private
     */
    var _getExportMetadataStore = function () {
        var ret = new Ext.data.JsonStore({
            url : pageInfo.basePath+'/dataExport/getMetaData',
            root : 'exportMetaData',
            fields : ['subsetId1', 'subsetName1', 'subset1', 'subsetId2', 'subsetName2', 'subset2', 'dataTypeId',
                'dataTypeName']
        });
        ret.proxy.addListener('loadexception', function(dummy, dummy2, response) {
            if (response.status != 200) {
                var responseText,
                    parsedResponseText;
                responseText = response.responseText;
                try {
                    parsedResponseText = JSON.parse(responseText);
                    if (parsedResponseText.message) {
                        responseText = parsedResponseText.message;
                    }
                } catch (syntaxError) {}
                exportListFetchErrorHandler(response.status, responseText);
            }
        });
        return ret;
    };

    var exportListFetchErrorHandler = function(status, text) {
        Ext.Msg.alert('Status', "Error fetching export metadata.<br/>Status " +
        status + "<br/>Message: " + text);
    };

    // let's create export metadata json store
    this.exportMetaDataStore = _getExportMetadataStore();

};

/**
 * Display data to be exported
 * @param records
 * @param options
 * @param success
 */
DataExport.prototype.displayResult = function (records, options, success) {

    var _this = this;

    _this.records = records;

    /**
     * Get tool bar component
     * @returns {Ext.Toolbar}
     * @private
     */
    var _getToolBar = function () {
        return new Ext.Toolbar(
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
                                D2H_ShowHelp("1312",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
                            }
                        }
                    )]
            }
        );
    };

    /**
     * Get data types grid panel component
     * @param newStore
     * @param columns
     * @param dataExportToolBar
     * @returns {Ext.grid.GridPanel}
     * @private
     */
    var _getDataTypesGridPanel = function (newStore, columns) {

        var _toolBar = _getToolBar();

        var _grid = new CustomGridPanel ({
            id: "dataTypesGridPanel",
            store: newStore,
            columns: columns,
            title: "Instructions: <br>" +
            "1. Select the check boxes for the data types and file formats that are " +
            "desired for export. <br>" +
            "2. Optionally you can filter the data by dragging and dropping some " +
            "criteria onto each data type row.<br> " +
            "3. Click on the \"Export Data\" button at the bottom of the screen to " +
            "initiate an asynchronous data download job.  <br>" +
            "4. To download your data navigate to the \"Export Jobs\" tab. <br>" +
            "Note: <i>Metadata will be downloaded in a separate file.</i>",
            viewConfig:	{
                forceFit : true,
                emptyText : "No rows to display"
            },
            sm : new Ext.grid.RowSelectionModel({singleSelect : true}),
            layout : "fit",
            width : 600,
            tbar : _toolBar,
            buttons: [{
                id : "dataTypesToExportRunButton",
                text : "Export Data",
                handler : function () {
                    _this.createDataExportJob(_grid);
                }
            }],
            buttonAlign:"center"
        });

        return _grid;
    };

    // Display data when success and records contains data
    if (success && (_this.records.length > 0)) {

        var _selectedCohortData = [];

        _selectedCohortData['dataTypeId'] = '';
        _selectedCohortData['dataTypeName'] = 'Selected Cohort';
        _selectedCohortData['subset1'] = getQuerySummary(1);
        _selectedCohortData['subset2'] = getQuerySummary(2);

        var _columns = _this.prepareColumnModel(_this.exportMetaDataStore, _selectedCohortData);
        var _newStore = _this.prepareNewStore(_this.exportMetaDataStore, _columns, _selectedCohortData);

        var _dataTypesGridPanel = _getDataTypesGridPanel(_newStore, _columns);
        _dataTypesGridPanel.records = _this.records;
        _dataTypesGridPanel.on("afterrender", _dataTypesGridPanel.dropZonesChecker);

        // add gridPanel to the main panel
        analysisDataExportPanel.add(_dataTypesGridPanel);
        analysisDataExportPanel.doLayout();

    } else {
        console.error("cannot load export metadata .. ");
    }
    // unmask data export panel
    analysisDataExportPanel.body.unmask();
};

/**
 *
 * @param store
 * @param selectedCohortData
 * @returns {Array}
 */
DataExport.prototype.prepareColumnModel = function (store, selectedCohortData) {
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
};

/**
 *
 * @param files
 * @param subset
 * @param dataTypeId
 * @returns {string}
 */
DataExport.prototype.prepareOutString = function (file, subset, dataTypeId) {
    var outStr = '';
    var dataCountExists = false;
    var _this = this;

    if (!dataCountExists) dataCountExists = true;
    outStr += _this.createSelectBoxHtml(file, subset, dataTypeId);

    return outStr;
};

/**
 *
 * @param file
 * @param subset
 * @param dataTypeId
 * @returns {string|*}
 */
DataExport.prototype.createSelectBoxHtml = function (file, subset, dataTypeId) {
    outStr = '';
    outStr +=  '<strong>' + file.patientsNumber + '</strong> patient' + (file.patientsNumber > 1 ? 's' : '');
    //TODO Show simple label if there is just one file format possible
    outStr += '<br/><input type="checkbox" id="' + subset + '_' + dataTypeId + '" name="download_dt" ' + (file.patientsNumber < 1 ? 'disabled' : '') +' />';
    outStr += '&nbsp;<select id="file_type_' + subset + '_' + dataTypeId + '"';
    outStr += ' name="file_type" ' + (file.patientsNumber < 1 || file.exporters.length < 2 ? 'disabled' : '') +'>';
    if(file.exporters) {
        file.exporters.each(function (exporter) {
            outStr += '<option value="{subset: ' + subset + ', dataTypeId: \'' + dataTypeId + '\', fileType: \'' + exporter.format + '\'}">' + exporter.format + '</option>';
        });
    }
    outStr += '</select><br/>';

    return outStr;
};

/**
 *
 * @param store
 * @param columns
 * @param selectedCohortData
 * @returns {Ext.data.JsonStore}
 */
DataExport.prototype.prepareNewStore = function (store, columns, selectedCohortData) {
    var _this = this;
    var data = [];
    data.push(selectedCohortData);

    /**
     * get export data tips
     * @returns {string}
     * @private
     */
    var _get_export_data_tip = function (file) {
        var _str_data_type = 'low dimensional';

        if (file.ontologyTermKeys) {
            _str_data_type = 'high dimensional';
        }

        return " <br><span class='data-export-filter-tip'>(Drag and drop " + _str_data_type
            + " nodes here to filter the exported data.)</span>";
    };

    store.each(function (row) {
        var this_data = [];
        this_data['dataTypeId'] = row.data.dataTypeId;
        this_data['dataTypeName'] = row.data.dataTypeName + _get_export_data_tip (row.data.subset1);

        var outStr = _this.prepareOutString(row.data.subset1, row.data.subsetId1, row.data.dataTypeId);
        this_data[row.data.subsetId1] = outStr;

        if (selectedCohortData['subset2'] != null && selectedCohortData['subset2'].length > 1) {
            // cohort string for subset 2
            this_data['dataTypeName'] = row.data.dataTypeName + _get_export_data_tip (row.data.subset2);
            // outstring for subset 2
            outStr = _this.prepareOutString(row.data.subset2, row.data.subsetId2, row.data.dataTypeId);
            this_data[row.data.subsetId2] = outStr;
        }

        data.push(this_data);
    });

    var myStore = new Ext.data.JsonStore({
        id: 'metadataStore',
        autoDestroy: true,
        root: 'subsets',
        fields: columns,
        data: {subsets: []}
    });

    myStore.loadData({subsets: data}, false);

    return myStore;
};

/**
 * Create data export job
 * @param gridPanel
 */
DataExport.prototype.createDataExportJob = function (gridPanel) {
    var _this = this;
    var subsetDataTypeFiles = jQuery("input[name=download_dt]:checked");
    if (subsetDataTypeFiles.length < 1) {
        alert('Please select the check boxes for the data types to export.');
        return
    }
    Ext.Ajax.request({
        url: pageInfo.basePath + "/dataExport/createnewjob",
        method: 'POST',
        success: function (result, request) {
            //Handle data export process
            _this.runDataExportJob(result, gridPanel);
        },
        failure: function (result, request) {
            Ext.Msg.alert('Status', 'Unable to create data export job.');
        },
        timeout: '1800000',
        params: {
            querySummary1: getQuerySummary(1),
            querySummary2: getQuerySummary(2),
            analysis: "DataExport"
        }
    });
};

/**
 * Get export parameters to be sent to the backend
 * @param gridPanel
 * @param selectedFiles
 * @returns {{}}
 */
DataExport.prototype.getExportParams = function (gridPanel, selectedFiles) {

    var params = {}; // init params

    /**
     * Check what subset of a file string
     * @param file
     * @returns {string}
     * @private                              getQuerySummaryItem
     */
    var _checkSubset = function (file) {
        var _subsets = ["subset1", "subset2"];
        var _subsetRegexs = [new RegExp(_subsets[0]), new RegExp(_subsets[1])];
        var _returnVal = "";

        for (var i = 0, maxLength = _subsetRegexs.length; i < maxLength; i++) {
            if (_subsetRegexs[i].test(file)) {
                _returnVal = _subsets[i];
            }
        }
        return _returnVal;
    }; //

    /**
     * Check if a particular data type is selected
     * @param file
     * @param type
     * @returns {boolean}
     * @private
     */
    var _checkDataType = function (file, type) {
        var _typeRegex = new RegExp(type);
        return _typeRegex.test(file);
    };

    /**
     * Get concept paths
     * @param el
     * @private
     */
    var _get_concept_path = function (tr) {
        var  _concept_path_arr = [];
        var _el = Ext.get(tr); // convert tr to element

        for (var i = 1; i < _el.dom.childNodes.length; i++) {
            var _concept_path = (_el.dom.childNodes[i]).getAttribute("conceptid");
            _concept_path_arr.push(_concept_path);
        }

        return _concept_path_arr;

    }; //

    if (gridPanel.records.length > 0) {

        for (var i = 0; i < gridPanel.records.length; i++) {

            // get data type
            var _data_type = gridPanel.records[i].data.dataTypeId;


            // get concept paths
            var _concept_path_arr = _get_concept_path(gridPanel.getView().getRow(i+1));

            // loop through selected files
            for (var j = 0; j < selectedFiles.length; j++) {

                var _sub = _checkSubset(selectedFiles[j]);
                var _type = _checkDataType(selectedFiles[j], _data_type);

                if (_sub) {

                    // create subset node
                    if (!params[_sub]) params[_sub] = {};

                    // create selector node
                    if (_type) {
                        params[_sub][_data_type.toLowerCase()] = {
                            'selector' : _concept_path_arr
                        };
                    }

                }
            }

        }
    }

    return params;
};

/**
 * Run data export job
 * @param result
 * @param gridPanel
 */
DataExport.prototype.runDataExportJob = function (result, gridPanel) {
    var jobNameInfo = Ext.util.JSON.decode(result.responseText);
    var jobName = jobNameInfo.jobName;

    var messages = {
        cancelMsg: "Your Job has been cancelled.",
        backgroundMsg: "Your job has been put into background process. Please check the job status in " +
        "the 'Export Jobs' tab."
    };

    showJobStatusWindow(result, messages);


    var subsetDataTypeFiles = jQuery("input[name=download_dt]:checked");
    var selectedSubsetDataTypeFiles = [];

    for (var i = 0; i < subsetDataTypeFiles.length; i++) {
        var selectedVal = jQuery("select[id=file_type_" + subsetDataTypeFiles[i].id + "] option:selected").val();
        selectedSubsetDataTypeFiles.push(selectedVal);
    }

    if (window.console) {
        console.log("selectedSubsetDataTypeFiles", selectedSubsetDataTypeFiles);
    }

    var _exportParams = this.getExportParams(gridPanel, selectedSubsetDataTypeFiles);

    Ext.Ajax.request(
        {
            url: pageInfo.basePath + "/dataExport/runDataExport",
            method: 'POST',
            timeout: '1800000',
            params: Ext.urlEncode(
                {
                    result_instance_id1: GLOBAL.CurrentSubsetIDs[1],
                    result_instance_id2: GLOBAL.CurrentSubsetIDs[2],
                    analysis: 'DataExport',
                    jobName: jobName,
                    selectedSubsetDataTypeFiles: selectedSubsetDataTypeFiles,
                    selection : JSON.stringify(_exportParams)
                }) // or a URL encoded string
        });

    checkJobStatus(jobName);
};
