
function getJobsDataForGalaxy(tab)
{
    galaxyjobsstore = new Ext.data.JsonStore({
        url : pageInfo.basePath+'/RetrieveData/getjobs',
        root : 'jobs',
        fields : ['name', 'status', 'lastExportName', 'lastExportTime', 'exportStatus']
    });

    galaxyjobsstore.on('load', galaxyjobsstoreLoaded);
    var myparams = Ext.urlEncode({jobType: 'DataExport',disableCaching: true});
    galaxyjobsstore.load({ params : myparams  });
}

function galaxyjobsstoreLoaded()
{
    var foo = galaxyjobsstore;
    var ojobs = Ext.getCmp('ajobsgrid');
    if(ojobs != null)
    {
        GalaxyPanel.remove(ojobs);
    }
    var jobs = new Ext.grid.GridPanel({
        store: galaxyjobsstore,
        id:'ajobsgrid',
        columns: [
            {name:'name', header: "Name", width: 120, sortable: true, dataIndex: 'name',
                renderer: function(value, metaData, record, rowIndex, colIndex, store) {
                    var changedName;
                    if (store.getAt(rowIndex).get('status') == 'Completed') {
                        changedName = '<a href="#">'+value+'</a>';
                    } else {
                        changedName = value;
                    }
                    return changedName;
                }
            },
            {name:'status', header: "Status", width: 120, sortable: true, dataIndex: 'status'},
            {name:'runTime', header: "Run Time", width: 120, sortable: true, dataIndex: 'runTime', hidden: true},
            {name:'startDate', header: "Started On", width: 120, sortable: true, dataIndex: 'startDate', hidden: true},
            {name:'viewerURL', header: "Viewer URL", width: 120, sortable: false, dataIndex: 'viewerURL', hidden: true},
            {name:'altViewerURL', header: "Alt Viewer URL", width: 120, sortable: false, dataIndex: 'altViewerURL', hidden: true},
            {name:'lastExportName', header: "lastExportName", width: 120, sortable: true, dataIndex: 'lastExportName'},
            {name:'lastExportTime', header: "lastExportTime", width: 120, sortable: true, dataIndex: 'lastExportTime' },
            {name:'exportStatus', header: "exportStatus", width: 120, sortable: true, dataIndex: 'exportStatus' }

        ],
        listeners : {cellclick : function (grid, rowIndex, columnIndex, e){
            var colHeader = grid.getColumnModel().getColumnHeader(columnIndex);
            if (colHeader == "Name") {
                var status = grid.getStore().getAt(rowIndex).get('status');
                if (status == "Error")	{
                Ext.Msg.alert("Job Failure", "Unfortunately, an error occurred on this job.");
                } else if (status == "Cancelled")	{
                Ext.Msg.alert("Job Cancelled", "The job has been cancelled");}
                else if (status == "Completed")	{
                    Ext.Msg.prompt('Name', 'Name of the Library to be exported:', function(btn, text){
                        if (btn == 'ok'){
                            var nameOfTheLibrary = text;
                            var nameOfTheExportJob = grid.getStore().getAt(rowIndex).get('name');
                            Ext.Ajax.request({
                                url: pageInfo.basePath+'/RetrieveData/JobExportToGalaxy',
                                method: 'POST',
                                params: {
                                    "nameOfTheLibrary" : nameOfTheLibrary,
                                    "nameOfTheExportJob" : nameOfTheExportJob
                                },
                                success: function(response) {
                                    if (200 == response.status){
                                        Ext.Msg.show({
                                            title:'Request Sent',
                                            msg: 'The export request has been sent to Galaxy',
                                            buttons: Ext.Msg.OK,
                                            animEl: 'elId'
                                        });
                                    }
                                    else{
                                        Ext.Msg.alert("Job Failure", "Unfortunately, an error occurred on this job. Error="+ response.status.toString());
                                    }
                                }
                            });
                        }
                    });
                 }
                else if (status != "Completed") {
                    Ext.Msg.alert("Job Processing", "The job is still processing, please wait");
                }
            }
        }
        },
        viewConfig:	{
            forceFit : true
        },
        sm : new Ext.grid.RowSelectionModel({singleSelect : true}),
        layout : 'fit',
        width : 600,
        buttons: [{
            text:'Refresh',
            handler: function()	{
                galaxyjobsstore.reload();
            }
        }],
        buttonAlign:'center',
        tbar:['->',{
            id:'help',
            tooltip:'Click for Jobs help',
            iconCls: "contextHelpBtn",
            handler: function(event, toolEl, panel){
                D2H_ShowHelp("1456",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
            }
        }]
    });
    GalaxyPanel.add(jobs);
    GalaxyPanel.doLayout();
}
