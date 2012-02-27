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
function getDatadata(tab)
{
	jobsstore = new Ext.data.JsonStore(
			{
   				url : pageInfo.basePath+'/data/getDataJobs',
   				root : 'jobs',
   				totalProperty : 'totalCount',
   				fields : ['name', 'status', 'runTime', 'startDate', 'viewerURL', 'altViewerURL']
			}
	);
	jobsstore.on('load', jobsDatastoreLoaded);
	var myparams = Ext.urlEncode({});
	jobsstore.load(
			{
				params : myparams
			}
	);
}

function jobsDatastoreLoaded()
{
	var foo = jobsstore;

	if(window.jobs)
	{
		analysisJobsPanel.remove(jobs);
	}
    var jobs = new Ext.grid.GridPanel({    		
		store: jobsstore,
		columns: [
		          {name:'name', header: "Name", width: 120, sortable: true, dataIndex: 'name'},
		          {name:'status', header: "Status", width: 120, sortable: true, dataIndex: 'status'},
		          {name:'data', header: "Data", width: 120, sortable: true, dataIndex: 'data'},
		          {name:'results', header: "Results", width: 120, sortable: true, dataIndex: 'results'}
		],
		viewConfig:	{
			forceFit : true
		},
		sm : new Ext.grid.RowSelectionModel({singleSelect : true}),
		layout : 'fit',
		width : 600,
		buttons: [{
			text:'Refresh',
			handler: function()	{
      		  jobsstore.reload();
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
    analysisDataPanel.add(jobs);
    analysisDataPanel.doLayout();
}
