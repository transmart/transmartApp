function getExportJobs(tab)
{
	//TODO : point it to /asyncJob/getjobs : somehow the UI in "Export Jobs" tab seems to be not showing the list of jobs
	exportjobsstore = new Ext.data.JsonStore({
		url : pageInfo.basePath+'/asyncJob/getjobs',
		root : 'jobs',
		totalProperty : 'totalCount',
		fields : ['name', 'status', 'runTime', 'startDate', 'viewerURL', 'altViewerURL']
	});
	exportjobsstore.on('load', exportjobsstoreLoaded);
	var myparams = Ext.urlEncode({jobType: 'DataExport', disableCaching: true});
	exportjobsstore.load({params : myparams});
}

function exportjobsstoreLoaded()
{
	var foo = exportjobsstore;

//	if(window.exportJobs)
//	{
//		analysisExportJobsPanel.remove(exportJobs);
//	}
	var ojobs = Ext.getCmp('exportJobsgrid');
	if(ojobs!=null)
	{
		analysisExportJobsPanel.remove(ojobs);
	}
    var jobs = new Ext.grid.GridPanel({    		
		store: exportjobsstore,
		id:'exportJobsgrid',
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
		          {name:'altViewerURL', header: "Query Summary", width: 120, sortable: false, dataIndex: 'altViewerURL'},
		          {name:'status', header: "Status", width: 60, sortable: true, dataIndex: 'status'},
		          {name:'runTime', header: "Run Time", width: 80, sortable: true, dataIndex: 'runTime', hidden: true},
		          {name:'startDate', header: "Started On", width: 80, sortable: true, dataIndex: 'startDate'},
		          {name:'viewerURL', header: "Viewer URL", width: 120, sortable: false, dataIndex: 'viewerURL', hidden: true}
		],
		listeners : {cellclick : function (grid, rowIndex, columnIndex, e) 
			{
				var colHeader = grid.getColumnModel().getColumnHeader(columnIndex);
				if (colHeader == "Name") {
					var status = grid.getStore().getAt(rowIndex).get('status');
					if (status == "Error")	{
						Ext.Msg.alert("Job Failure", "Unfortunately, an error occurred generating this data-export");	
					} else if (status == "Cancelled")	{
						Ext.Msg.alert("Job Cancelled", "The job has been cancelled");
					} else if (status == "Completed") {
						// this implementation is inside button handler on step 5
						Ext.Ajax.request({
								url: pageInfo.basePath+'/dataExport/downloadFileExists',
								method: 'POST',
								params: 'jobname=' + grid.getStore().getAt(rowIndex).get('name'),
								success: function(obj) {
									var resp = Ext.decode(obj.responseText);
									if (!resp.fileStatus) {
										Ext.MessageBox.alert('File not Found!!!', resp.message);
									} else {
										try {
										    Ext.destroy(Ext.get('jobname'));
										} catch(e) {}
										var body = Ext.getBody();
									    var frame = body.createChild({tag:'iframe',cls:'x-hidden',id:'iframe',name:'iframe'});
									    //var params = {'jobname': grid.getStore().getAt(rowIndex).get('name')};
									    var form = body.createChild({tag:'form',cls:'x-hidden',id:'form',
									    	action:pageInfo.basePath+'/dataExport/downloadFile',
									        target:'iframe'
									    });
									    
									    var jobname = new Ext.form.Field({
										    	fieldLabel: '', id:'jobname', name:'jobname', labelSeparator: ' ', boxLabel:'', hidden: true, 
										    	value: grid.getStore().getAt(rowIndex).get('name'), renderTo: form
									    	});
									    form.dom.submit();
									}
								}
							});
					} else if (status != "Completed") {
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
      		  exportjobsstore.reload();
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
    analysisExportJobsPanel.add(jobs);
    analysisExportJobsPanel.doLayout();
    analysisExportJobsPanel.body.unmask();
}
