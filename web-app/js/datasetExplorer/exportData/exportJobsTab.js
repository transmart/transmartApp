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
function getExportJobs(tab)
{
	exportjobsstore = new Ext.data.JsonStore({
		url : pageInfo.basePath+'/dataExport/getJobs',
		root : 'exportJobs',
		totalProperty : 'totalCount',
		fields : ['name', 'status', 'runTime', 'startDate', 'viewerURL', 'querySummary']
	});
	exportjobsstore.on('load', exportjobsstoreLoaded);
	var myparams = Ext.urlEncode({});
	exportjobsstore.load({params : myparams});
}

function exportjobsstoreLoaded()
{
	var foo = exportjobsstore;

	if(window.exportJobs)
	{
		analysisExportJobsPanel.remove(exportJobs);
	}
    var jobs = new Ext.grid.GridPanel({    		
		store: exportjobsstore,
		columns: [
		          {name:'name', header: "Name", width: 120, sortable: true, dataIndex: 'name',
		        	  renderer: function(value, metaData, record, rowIndex, colIndex, store) {
		        		  var changedName;
		        		  if (store.getAt(rowIndex).get('status') == 'Completed') {
		        			  changedName = '<a href="'+pageInfo.basePath+'/dataExport/downloadFile?jobname='+value+'">'+value+'</a>';
		        		  } else {
		        			  changedName = value;
		        		  }
		        		  return changedName;
		              }
		          },
		          {name:'querySummary', header: "Query Summary", width: 120, sortable: false, dataIndex: 'querySummary'},
		          {name:'status', header: "Status", width: 60, sortable: true, dataIndex: 'status'},
		          {name:'runTime', header: "Run Time", width: 80, sortable: true, dataIndex: 'runTime', hidden: true},
		          {name:'startDate', header: "Started On", width: 80, sortable: true, dataIndex: 'startDate'},
		          {name:'viewerURL', header: "Viewer URL", width: 120, sortable: false, dataIndex: 'viewerURL'}
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
}