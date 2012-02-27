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
function submitCorrelationAnalysisJob(form){
	var variablesConceptCode = readConceptVariables("divVariables");

	var formParams = {	variablesConceptPaths:variablesConceptCode, 
						correlationBy:form.correlationBy.value,
						correlationType:form.correlationType.value
					};

	var variableEle = Ext.get("divVariables");
	
	//If the list of concepts we are running the analysis on is empty, alert the user.
	if(variablesConceptCode == '' || (variableEle.dom.childNodes.length < 2))
	{
		Ext.Msg.alert('Missing input!', 'Please drag at least two concepts into the variables box.');
		return;
	}	
	
	submitJob(formParams);
}

function loadCorrelationAnalysisView(){
	registerCorrelationAnalysisDragAndDrop();
}

function clearGroupCorrelation(divName)
{
	//Clear the drag and drop div.
	var qc = Ext.get(divName);
	
	for(var i=qc.dom.childNodes.length-1;i>=0;i--)
	{
		var child=qc.dom.childNodes[i];
		qc.dom.removeChild(child);
	}	

}

function registerCorrelationAnalysisDragAndDrop()
{
	//Set up drag and drop for Dependent and Independent variables on the data association tab.
	//Get the Dependent DIV.
	var variablesDiv = Ext.get("divVariables");
	
	//Add the drop targets and handler function.
	dtgD = new Ext.dd.DropTarget(variablesDiv,{ddGroup : 'makeQuery'});
	dtgD.notifyDrop =  dropNumericOntoCategorySelection;
}