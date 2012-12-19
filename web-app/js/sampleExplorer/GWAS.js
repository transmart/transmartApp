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
					getQuerySummary(1), 
					getQuerySummary(2));
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
