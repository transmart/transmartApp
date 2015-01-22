//////////////////////////////////////////////////////////////////
//This file holds the javascript functions required to get user input for running IGV, and actually submit the job.
//
//
//
////////////////////////////////////////////////////////////////
function showGwasSelection() {
	genePatternReplacement();
	/*var win = new Ext.Window({
		id: 'showGwasSelection',
		title: 'Genome-Wide Association Study',
		layout:'fit',
		width:600,
		height:400,
		closable: false,
		plain: true,
		modal: true,
		border:false,
		buttons: [
		          {
		        	  id: 'showGwasSelectionOKButton',
		        	  text: 'OK',
		        	  handler: function()
		        	  {
		        		  //
			        	  if(Ext.get('gwasChroms')==null)
			        	  {
			        		  win.close();
			        		  return;
			        	  }
			        	  
			        	  //
			        	  var ob=Ext.get('gwasChroms').dom;
			        	  var selected = new Array();
			        	  
			        	  //
			        	  for (var i = 0; i < ob.options.length; i++)
			        		  if (ob.options[i].selected) selected.push(ob.options[i].value);
			        	  GLOBAL.CurrentChroms=selected.join(',');
			        	  
			        	  //
			        	  showGwas();
			        	  win.close();}
		          }
		          ,{
		        	  text: 'Cancel',
		        	  handler: function(){
		        	  win.close();}
		          }],
		resizable: false,
		autoLoad: {
			url: pageInfo.basePath+'/genePattern/showGwasSelection',
			scripts: true,
			nocache:true,
			discardUrl:true,
			method:'POST',
			params: {SearchJson: GLOBAL.DefaultCohortInfo.SampleIdList}
		},
		tools:[{
			id:'help',
			qtip:'Click for context sensitive help',
		    handler: function(event, toolEl, panel){
		    // 1360 needs to be changed for PLINK
		   	D2H_ShowHelp("1360",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
		    }
		}]
	});
	//  }
	win.show(viewport);*/
}

//TODO-PLUGIN:This should be part of the plugin
/** 
 * Function to run the GWAS asynchronously
 */
function showGwas() {	

	Ext.Ajax.request({						
		url: pageInfo.basePath+"/asyncJob/createnewjob",
		method: 'POST',
		success: function(result, request){
			runGwas(result, 
					GLOBAL.CurrentSubsetIDs[1], 
					GLOBAL.CurrentSubsetIDs[2],
					getSubsetQuerySummary(1),
					getSubsetQuerySummary(2));
		},
		failure: function(result, request){
			Ext.Msg.alert('Status', 'Unable to create the heatmap job.');
		},
		timeout: '1800000',
		params: {jobType:  "GWAS"}
	});
}

// After we get a job created by GPController, we run GWAS
function runGwas(result, result_instance_id1, result_instance_id2,
		querySummary1, querySummary2)	{
	var jobNameInfo = Ext.util.JSON.decode(result.responseText);					 
	var jobName = jobNameInfo.jobName;

	genePatternReplacement();
	/*showJobStatusWindow(result);	

	Ext.Ajax.request(
	{						
		url: pageInfo.basePath+"/genePattern/runGwas",
		method: 'POST',
		timeout: '1800000',
		params: {result_instance_id1: result_instance_id1,
			result_instance_id2:  result_instance_id2,
			querySummary1: querySummary1,
			querySummary2: querySummary2,
			chroms: GLOBAL.CurrentChroms,
			jobName: jobName
		}
	});
	checkJobStatus(jobName);*/
}
