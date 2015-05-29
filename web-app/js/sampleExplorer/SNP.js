//////////////////////////////////////////////////////////////////
//This file holds the javascript functions required to get user input for the SNP Analysis, and actually submit the job.
//
//
//
////////////////////////////////////////////////////////////////

//Popup the window with the SNP specific inputs we need from the user.
function showSNPViewerSelection() 
{
	var win = new Ext.Window({
		id: 'showSNPViewerSelection',
		title: 'SNPViewer',
		layout:'fit',
		width:600,
		height:400,
		closable: false,
		plain: true,
		modal: true,
		border:false,
		buttons: [
		          {
		        	  id: 'showSNPViewerSelectionOKButton',
		        	  text: 'OK',
		        	  handler: function()
		        	  {
		        		  //Verify we can get at the input object.
			        	  if(Ext.get('snpViewChroms')==null)
			        	  {
			        		  win.close();
			        		  return;
			        	  }
			        	  
			        	  //Get the input object.
			        	  var ob=Ext.get('snpViewChroms').dom;
			        	  var selected = new Array();
			        	  
			        	  //Get the chromosome list.
			        	  for (var i = 0; i < ob.options.length; i++)
			        		  if (ob.options[i].selected) selected.push(ob.options[i].value);
			        	  GLOBAL.CurrentChroms=selected.join(',');
			        	  
			        	  //Run the SNP Viewer job.
			        	  getSNPViewer();
			        	  win.close();
		        	  }
		          }
		          ,{
		        	  text: 'Cancel',
		        	  handler: function(){
		        	  win.close();}
		          }],
		resizable: false,
		autoLoad: {
			url: pageInfo.basePath+'/analysis/showSNPViewerSelectionSample',
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
		   	D2H_ShowHelp("1360",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
		    }
		}]
	});
	//  }
	win.show(viewport);
}


function getSNPViewer() 
{

	// before Ajax call, log into genepattern:
	//genePatternLogin();
	
	var selectedGenesElt = Ext.get("selectedGenesSNPViewer");
	var selectedGenesEltValue = selectedGenesElt.dom.value;
	var selectedGeneStr = "";
	
	if (selectedGenesEltValue && selectedGenesEltValue.length != 0) 
	{
		selectedGeneStr = selectedGenesEltValue;
	}
	
	var geneAndIdListElt = Ext.get("selectedGenesAndIdSNPViewer");
	var geneAndIdListEltValue = geneAndIdListElt.dom.value;
	var geneAndIdListStr = "";
	
	if (geneAndIdListElt && geneAndIdListEltValue.length != 0) 
	{
		geneAndIdListStr = geneAndIdListEltValue;
	}
	
	var selectedSNPsElt = Ext.get("selectedSNPs");
	var selectedSNPsEltValue = selectedSNPsElt.dom.value;
	var selectedSNPsStr = "";
	
	if (selectedSNPsElt && selectedSNPsEltValue.length != 0) 
	{
		selectedSNPsStr = selectedSNPsEltValue;
	}
	genePatternReplacement();
	/*Ext.Ajax.request(
	{
		url: pageInfo.basePath+"/analysis/showSNPViewerSample",
		method: 'POST',
		success: function(result, request){
			//getSNPViewerComplete(result);
		},
		failure: function(result, request){
			//getSNPViewerComplete(result);
		},
		timeout: '1800000',
		params: { SearchJson: GLOBAL.DefaultCohortInfo.SampleIdList,
			chroms: GLOBAL.CurrentChroms,
			genes: selectedGeneStr,
			geneAndIdList: geneAndIdListStr,
			snps: selectedSNPsStr}
	});
	
	showWorkflowStatusWindow();*/
}