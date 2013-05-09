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
  

function getJobsData(tab)
{
	jobsstore = new Ext.data.JsonStore(
			{
   				url : pageInfo.basePath+'/asyncJob/getjobs',
   				root : 'jobs',
   				totalProperty : 'totalCount',
   				fields : ['name', 'status', 'runTime', 'startDate', 'viewerURL', 'altViewerURL']
			}
	);
	jobsstore.on('load', jobsstoreLoaded);
	var myparams = Ext.urlEncode({disableCaching: true});
	jobsstore.load(
			{
				params : myparams
			}
	);
}

function jobsstoreLoaded()
{
	var foo = jobsstore;
	var ojobs = Ext.getCmp('ajobsgrid');
	if(ojobs!=null)
	{
		analysisJobsPanel.remove(ojobs);
	}
    var jobs = new Ext.grid.GridPanel({    		
		store: jobsstore,
		id:'ajobsgrid',
		columns: [
		          {name:'name', header: "Name", width: 120, sortable: true, dataIndex: 'name', css: 'text-decoration: underline; color: blue;'},
		          {name:'status', header: "Status", width: 120, sortable: true, dataIndex: 'status'},
		          {name:'runTime', header: "Run Time", width: 120, sortable: true, dataIndex: 'runTime'},
		          {name:'startDate', header: "Started On", width: 120, sortable: true, dataIndex: 'startDate'},
		          {name:'viewerURL', header: "Viewer URL", width: 120, sortable: false, dataIndex: 'viewerURL', hidden: true},
		          {name:'altViewerURL', header: "Alt Viewer URL", width: 120, sortable: false, dataIndex: 'altViewerURL', hidden: true}
		],
		listeners : {cellclick : function (grid, rowIndex, columnIndex, e) 
			{
				var colHeader = grid.getColumnModel().getColumnHeader(columnIndex);
				if (colHeader == "Name") {
					var viewerURL = grid.getStore().getAt(rowIndex).get('viewerURL');
					var altViewerURL = grid.getStore().getAt(rowIndex).get('altViewerURL');
					if (altViewerURL == null)	{
						altViewerURL = "";
					}
					var status = grid.getStore().getAt(rowIndex).get('status');
					var jobName = grid.getStore().getAt(rowIndex).get('name');
					var jobNameArray = jobName.split("-");
					var jobType = jobNameArray[1];
					
					if (status == "Completed")	{
						// First, we check all of the general heatmaps that store a URL
						// Second, we check for special cases where the results are stored in JOB_RESULTS field
						if (viewerURL != null)	{
							runVisualizerFromSpan(viewerURL, altViewerURL);
						} else	{
							Ext.Ajax.request({						
								url: pageInfo.basePath+"/asyncJob/getjobresults",
								method: 'POST',
								success: function(result, request){
									var jobResultsInfo = Ext.util.JSON.decode(result.responseText);					 
									var jobResults = jobResultsInfo.jobResults;
									if (jobType == "Survival")	{
										showSurvivalAnalysisWindow(jobResults);
									} else	{
										showHaploViewWindow(jobResults);
									}
								},
								failure: function(result, request){
									Ext.Msg.alert('Please, Contact a tranSMART Administrator', 'Unable to show the results.');
								},
								timeout: '1800000',
								params: {jobname: jobName}
							});							
						}							
					} else if (status == "Error")	{
						Ext.Msg.alert("Job Failure", "Unfortunately, an error occurred generating this heatmap");	
					} else if (status == "Cancelled")	{
						Ext.Msg.alert("Job Cancelled", "The job has been cancelled");
					} else	{
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
    analysisJobsPanel.add(jobs);
    analysisJobsPanel.doLayout();
}

//Called to show the new job status window
function showJobStatusWindow(result, messages)	{
	var jobInfo = Ext.util.JSON.decode(result.responseText);
	var jobName = jobInfo.jobName;
	var sb = Ext.getCmp('asyncjob-statusbar');
	sb.setVisible(false); 	// Initially, hide the status bar unless the user selects to run in the background
	
	var jobWindow = new Ext.Window({
		id: 'showJobStatus',
		title: 'Job Status',
		layout:'fit',
		width:350,
		height:400,
		closable: false,
		plain: true,
		modal: true,
		border:false,
		resizable: false,
		buttons: [
		          {
		        	  text: 'Cancel Job',
		        	  handler: function()	{		        		  
		        		  cancelJob(jobName);
		        		  sb.setVisible(true);
		        		  if (messages && messages.cancelMsg) {
			        		  Ext.Msg.alert("Job Cancelled", messages.cancelMsg);
			        		  Ext.MessageBox.hide.defer(5000,this);
		        		  }
		        		  jobWindow.close();
		        	  }
		          },
		          {
		        	  text: 'Run in Background',
		        	  handler: function()	{
		        		  sb.setVisible(true);
		        		  if (messages && messages.backgroundMsg) {
			        		  Ext.Msg.alert("Job processing in background", messages.backgroundMsg);
			        		  Ext.MessageBox.hide.defer(5000,this);
		        		  }
		        		  jobWindow.close();
		        	  }
		          }],
	   autoLoad: {
		   url: pageInfo.basePath+'/asyncJob/showJobStatus',
		   scripts: true,
		   nocache:true,
		   discardUrl:true,
		   method:'POST',
		   params: {jobName: jobName}
	   }
	});
	jobWindow.show(viewport);
}

// Used to cancel a given job, this can be called from the Job Status window or the progress toolbar
function cancelJob(jobName)	{
	Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/asyncJob/canceljob",
				method : 'POST',
				timeout : '300000',
				params: {jobName: jobName}
			}
	);
}

// Called to check the heatmap job status 
function checkJobStatus(jobName)	{	
	var sb = Ext.getCmp('asyncjob-statusbar');	
	var cancBtn = Ext.getCmp('cancjob-button');
	var jWindow = Ext.getCmp('showJobStatus');	
	var secCount = 0;
	var pollInterval = 3000;   // 4 second
	var singletonflag= 0; // make sure we don't invoke heatmap more than once on job completion
	
	// Add the handler for the cancel button in the statusbar
	cancBtn.setVisible(true);
	cancBtn.setHandler(function()	{
		Ext.TaskMgr.stop(checkTask);
		sb.setStatus({
		    text: 'Job cancelled',
		    clear: true
		});
		cancBtn.setVisible(false);
		cancelJob(jobName);		
	});
	
	var updateJobStatus = function(){
		
		secCount=secCount+3; // 3 seconds
		Ext.Ajax.request(
			{
				url : pageInfo.basePath+"/asyncJob/checkJobStatus",
				method : 'POST',
				success : function(result, request)
				{
					var jobStatusInfo = Ext.util.JSON.decode(result.responseText);					 
					var status = jobStatusInfo.jobStatus;
					var viewerURL = jobStatusInfo.jobViewerURL;
					var altViewerURL = jobStatusInfo.jobAltViewerURL;
					var exception = jobStatusInfo.jobException;
					var newHTML = jobStatusInfo.jobStatusHTML;
					var resultType = jobStatusInfo.resultType;
					var jobResults = jobStatusInfo.jobResults;
					
					if (newHTML != null)	{
						if (jWindow != null && jWindow.isVisible())	{
							jWindow.body.update(newHTML);
						}
					}
					
					if(status =='Error')	{
						if (jWindow != null && jWindow.isVisible())	{
							jWindow.close();							
						}
						Ext.Msg.alert('Please, Contact a tranSMART Administrator', 'Unable to complete: ' + exception);
						Ext.TaskMgr.stop(checkTask);
					
						sb.setStatus({
						    text: "Status: Error",
						    clear: true
						});								
						cancBtn.setVisible(false);
					}
					else if(status =='Completed')	{
						singletonflag++;
						
						if (jWindow != null && jWindow.isVisible())	{
							jWindow.close();
						}
						
						Ext.TaskMgr.stop(checkTask);
						
						sb.setStatus({
						    text: "Status: " + status,
						    clear: true
						});
						cancBtn.setVisible(false);
						if(singletonflag==1){
						if (resultType == null)	{
							// resultType is only used for special cases (surivival, haploview, snp, igv, plink)
							// Here lies the Venkat rule for standard heatmaps
							// < 120 seconds, just popup the heatmap.
							// >= 120 seconds then popup a message to indicate that the heatmap is finished.
							if (secCount < 120) {  
								runVisualizerFromSpan(viewerURL, altViewerURL);	
							} else	{
								Ext.Msg.buttonText.yes = 'View Now';
								Ext.Msg.buttonText.no = 'View Later';
								Ext.Msg.show({									
									title:'Results Are Ready For Viewing',
									msg: 'Would you like to view results now or later through the Jobs tab?',
									buttons: Ext.Msg.YESNO,	
									fn: function(btn) {
										if (btn == 'yes')	{
											runVisualizerFromSpan(viewerURL, altViewerURL);
										}										
									},
									icon: Ext.MessageBox.QUESTION
								});
							}
						} else if (resultType == "Survival")	{
							showSurvivalAnalysisWindow(jobResults);
						} else if (resultType == "GWAS")	{
							showGwasWindow(jobResults);
						} else if (resultType == "Haplo")	{
							showHaploViewWindow(jobResults);
						}		
						}
					} else if(status == 'Cancelled')	{
						if (jWindow != null && jWindow.isVisible())	{
							jWindow.close();
						}
						Ext.TaskMgr.stop(checkTask);
						sb.setStatus({
						    text: 'Job cancelled',
						    clear: true
						});
						cancBtn.setVisible(false);						
					} else	{
						var secLabel = " seconds"
						if (secCount == 1)	{
							secLabel = " second"
						}
						sb.showBusy("Status: " + jobStatusInfo.jobStatus + ", running for " + String(secCount) + secLabel);						
					} 
				},
				failure : function(result, request)
				{
					Ext.Msg.alert('Failed', 'Could not complete the job, please contact an administrator');
					Ext.TaskMgr.stop(checkTask);
					sb.setStatus({
						text: "Status: Failed",
					    clear: true
					});							
					cancBtn.setVisible(false);
				},
				timeout : '300000',
				params: {jobName: jobName
				}
			}
		);
  	}

	var checkTask =	{
			run: updateJobStatus,
	  	    interval: pollInterval	
	}	
	Ext.TaskMgr.start(checkTask);
}

