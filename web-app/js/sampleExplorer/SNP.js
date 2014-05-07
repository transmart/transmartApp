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


