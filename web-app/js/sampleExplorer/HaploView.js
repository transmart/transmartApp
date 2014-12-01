//////////////////////////////////////////////////////////////////
//This file holds the javascript functions required to get user input for the Haploview, and actually submit the job.
//
//
//
////////////////////////////////////////////////////////////////

//Show the window which lists the possible genes to select for a Halpoview. This is specific to sample explorer.
function showHaploviewGeneSelection()
{
	//Create EXTjs Window. We autoload a template that has a multiselect for the available genes. We pass the subsets with Sample ID's so we can decide which genes are applicable.
	var win = new Ext.Window({
		id: 'showHaploviewGeneSelection',
		title: 'Haploview-Gene Selection',
		layout:'fit',
		width:250,
		height:250,
		closable: false,
		plain: true,
		modal: true,
		border:false,
		buttons: [
		          {
		        	  id: 'haploviewGeneSelectionOKButton',
		        	  text: 'OK',
		        	  handler: function()
		        	  {
		        		  //If we can't find the gene input box, exit.
		        		  if(Ext.get('haploviewgenes')==null)
		        		  {
		        			  win.close();
		        			  return;
		        		  }

		        		  //Get the gene input box.
		        		  var ob=Ext.get('haploviewgenes').dom;

		        		  //Create an array that will hold the genes.
		        		  var selected = new Array();
		        		  for (var i = 0; i < ob.options.length; i++)
		        			  if (ob.options[i].selected) selected.push(ob.options[i].value);

		        		  //Set a global variable that will hold our gene list.
		        		  GLOBAL.CurrentGenes=selected.join(',');

		        		  //Create Haploview Job.
		        		  getHaploview();

		        		  win.close();
		        	  }
		          }
		          ,{
		        	  text: 'Cancel',
		        	  handler: function(){
		        		  win.close();}
		          }],
		          resizable: false,
		          autoLoad:
		          {
		        	  url: pageInfo.basePath+'/analysis/getGenesForHaploviewFromSampleId',
		        	  scripts: true,
		        	  nocache:true,
		        	  discardUrl:true,
		        	  method:'POST',
		        	  params: {SearchJSON:buildSubsetJSON()}
		          },
		          tools:[{
		        	  id:'help',
		        	  qtip:'Click for context sensitive help',
		        	  handler: function(event, toolEl, panel){
		        		  D2H_ShowHelp("1174",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
		        	  }
		          }]
	});

	win.show(viewport);
}
  
//Called to initiate HaploView Job
function getHaploview()
{
	Ext.Ajax.request({						
		url: pageInfo.basePath+"/asyncJob/createnewjob",
		method: 'POST',
		success: function(result, request)
		{
			RunHaploViewer(result, GLOBAL.DefaultCohortInfo.SampleIdList, GLOBAL.CurrentGenes);
		},
		failure: function(result, request)
		{
			Ext.Msg.alert('Status', 'Unable to create the heatmap job.');
		},
		timeout: '1800000',
		params: {jobType:  "Haplo"}
	});	
}

//This runs the Haploview job.
function RunHaploViewer(result, SampleIdList, genes)
{
	//Get the job information returned from the web service call that created the job.
	var jobNameInfo = Ext.util.JSON.decode(result.responseText);					 
	var jobName = jobNameInfo.jobName;

	genePatternReplacement();
	/*//Show the window that displays the steps in the workflow.
	showJobStatusWindow(result);	
	
	//Log into GenePattern
	document.getElementById("gplogin").src = pageInfo.basePath + '/analysis/gplogin';

	//Run the HaploView
	Ext.Ajax.request
			({						
				url: pageInfo.basePath+"/genePattern/runhaploviewersample",
				method: 'POST',
				timeout: '1800000',
				params: {sampleIdList: SampleIdList,
					genes: genes,
					jobName: jobName
				}
			});
	
	//Start the status polling.
	checkJobStatus(jobName);*/
}  