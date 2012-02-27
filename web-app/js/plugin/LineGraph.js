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
function submitLineGraphJob(form){
	var dependentVariableConceptCode = readConceptVariables("divDependentVariable");
	var independentVariableConceptCode = readConceptVariables("divIndependentVariable");
	var groupByVariableConceptCode = readConceptVariables("divGroupByVariable");
	var variablesConceptCode = dependentVariableConceptCode+"|"+groupByVariableConceptCode;
	
	var formParams = {variablesConceptPaths:variablesConceptCode, 
			dependentVariable:dependentVariableConceptCode,
			independentVariable:independentVariableConceptCode,
			groupByVariable:groupByVariableConceptCode,
			graphType:form.graphType.value};

	//Make sure user entered a group and a concept.
	if(groupByVariableConceptCode == '')
	{
		Ext.Msg.alert('Missing input!', 'Please drag at least one concept into the Group Concepts variable box.');
		return;
	}
	
	if(dependentVariableConceptCode == '')
	{
		Ext.Msg.alert('Missing input!', 'Please drag at least one concept into the Time/Measurement variable box.');
		return;
	}	

	submitJob(formParams);
}

function loadLineGraphView(){
	registerLineGraphDragAndDrop();
}

function clearGroupLine(divName)
{
	//Clear the drag and drop div.
	var qc = Ext.get(divName);
	
	for(var i=qc.dom.childNodes.length-1;i>=0;i--)
	{
		var child=qc.dom.childNodes[i];
		qc.dom.removeChild(child);
	}	

}
function registerLineGraphDragAndDrop()
{
	//Set up drag and drop for Dependent and Independent variables on the data association tab.

	//Get the Dependent DIV.
	var dependentDiv = Ext.get("divDependentVariable");
	dtgD = new Ext.dd.DropTarget(dependentDiv,{ddGroup : 'makeQuery'});
	dtgD.notifyDrop =  dropNumericOntoCategorySelection;
	
	//Get the group by div
	var groupByDiv = Ext.get("divGroupByVariable");
	dtgG = new Ext.dd.DropTarget(groupByDiv, {ddGroup: 'makeQuery'});
	dtgG.notifyDrop = dropOntoCategorySelection;
}