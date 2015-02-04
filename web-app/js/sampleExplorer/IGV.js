//////////////////////////////////////////////////////////////////
//This file holds the javascript functions required to get user input for running IGV, and actually submit the job.
//
//
//
////////////////////////////////////////////////////////////////

//Popup the window with the IGV specific options.
function showIgvSelection() 
{
	var win = new Ext.Window({
		id: 'showIgvSelection',
		title: 'IGV',
		layout:'fit',
		width:600,
		height:400,
		closable: false,
		plain: true,
		modal: true,
		border:false,
		buttons: [
		          {
		        	  id: 'showIgvSelectionOKButton',
		        	  text: 'OK',
		        	  handler: function()
		        	  {
		        		  //
			        	  if(Ext.get('igvChroms')==null)
			        	  {
			        		  win.close();
			        		  return;
			        	  }
			        	  
			        	  //
			        	  var ob=Ext.get('igvChroms').dom;
			        	  var selected = new Array();
			        	  
			        	  //
			        	  for (var i = 0; i < ob.options.length; i++)
			        		  if (ob.options[i].selected) selected.push(ob.options[i].value);
			        	  GLOBAL.CurrentChroms=selected.join(',');
			        	  
			        	  //
			        	  getIgv();
			        	  win.close();}
		          }
		          ,{
		        	  text: 'Cancel',
		        	  handler: function(){
		        	  win.close();}
		          }],
		resizable: false,
		autoLoad: {
			url: pageInfo.basePath+'/analysis/showIgvSelection',
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
		   	D2H_ShowHelp("1427",helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP );
		    }
		}]
	});
	//  }
	win.show(viewport);
}

function getIgv() 
{

	// before Ajax call, log into genepattern:
	//genePatternLogin();
	
	var selectedGenesElt = Ext.get("selectedGenesIgv");
	var selectedGenesEltValue = selectedGenesElt.dom.value;
	var selectedGeneStr = "";
	
	if (selectedGenesEltValue && selectedGenesEltValue.length != 0) 
	{
		selectedGeneStr = selectedGenesEltValue;
	}
	
	var geneAndIdListElt = Ext.get("selectedGenesAndIdIgv");
	var geneAndIdListEltValue = geneAndIdListElt.dom.value;
	var geneAndIdListStr = "";
	
	if (geneAndIdListElt && geneAndIdListEltValue.length != 0) 
	{
		geneAndIdListStr = geneAndIdListEltValue;
	}
	
	var selectedSNPsElt = Ext.get("selectedSNPsIgv");
	var selectedSNPsEltValue = selectedSNPsElt.dom.value;
	var selectedSNPsStr = "";
	
	if (selectedSNPsElt && selectedSNPsEltValue.length != 0) 
	{
		selectedSNPsStr = selectedSNPsEltValue;
	}
	genePatternReplacement();
	/*Ext.Ajax.request(
	{
		url: pageInfo.basePath+"/analysis/showIgvSample",
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