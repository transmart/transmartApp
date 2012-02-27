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
function submitScatterPlotJob(form){
	var dependentVariableEle = Ext.get("divDependentVariable");
	var independentVariableEle = Ext.get("divIndependentVariable");
	
	var dependentVariableConceptCode = "";
	var independentVariableConceptCode = "";	
	
	if(dependentVariableEle.dom.childNodes[0]) dependentVariableConceptCode = getQuerySummaryItem(dependentVariableEle.dom.childNodes[0]);
	if(independentVariableEle.dom.childNodes[0]) independentVariableConceptCode = getQuerySummaryItem(independentVariableEle.dom.childNodes[0]); 

	var variablesConceptCode = dependentVariableConceptCode+"|"+independentVariableConceptCode;
	
	//Make sure the user entered some items into the variable selection boxes.
	if(dependentVariableConceptCode == '' || (dependentVariableEle.dom.childNodes.length != 1))
		{
			Ext.Msg.alert('Missing input!', 'Please drag one and only one concept into the dependent variable box.');
			return;
		}
	
	if(independentVariableConceptCode == '' || (independentVariableEle.dom.childNodes.length != 1))
		{
			Ext.Msg.alert('Missing input!', 'Please drag one and only one concept into the independent variable box.');
			return;
		}	
	
	var formParams = {dependentVariable:dependentVariableConceptCode,independentVariable:independentVariableConceptCode,variablesConceptPaths:variablesConceptCode};
	
	submitJob(formParams);
}

function loadScatterPlotView(){
	registerScatterPlotDragAndDrop();
}

function clearGroupScatter(divName)
{
	//Clear the drag and drop div.
	var qc = Ext.get(divName);
	
	for(var i=qc.dom.childNodes.length-1;i>=0;i--)
	{
		var child=qc.dom.childNodes[i];
		qc.dom.removeChild(child);
	}	

}

function registerScatterPlotDragAndDrop()
{
	//Set up drag and drop for Dependent and Independent variables on the data association tab.
	//Get the Dependent DIV.
	var dependentDiv = Ext.get("divDependentVariable");
	//Get the Independent DIV
	var independentDiv = Ext.get("divIndependentVariable");
	
	//Add the drop targets and handler function.
	dtgD = new Ext.dd.DropTarget(dependentDiv,{ddGroup : 'makeQuery'});
	dtgD.notifyDrop =  dropNumericOntoCategorySelection;
	
	dtgI = new Ext.dd.DropTarget(independentDiv,{ddGroup : 'makeQuery'});
	dtgI.notifyDrop =  dropNumericOntoCategorySelection;
	
}