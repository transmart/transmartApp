/*************************************************************************
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************/

var analysisConcept = null;

function renderCohortSummary() {
	var summary = "";
	for (var subset = 1; subset <= GLOBAL.NumOfSubsets; subset++) {
		var subset_query = getQuerySummary(subset);
		if (subset_query != "") {
			summary += "Subset " + subset + ": ";
			summary += "<br>";
			summary += subset_query;
			summary += "<br><br>"
		}

	}
	var summary_html = "";
	if (summary == "") {
		summary_html = "<br><span class=\"cohortwarning\">Warning! You have not selected a study and the analyses will not work. Please go back to the 'Comparison' tab and make a cohort selection.</span><br>";
	} else {
		summary_html = "<br>" + summary;
	}

	var cohort_element = document.getElementById("cohortSummary");
	if (cohort_element != null) {
		cohort_element.innerHTML = summary_html;
	}
}

function checkPreviousAnalysis() {
	//If the user clicks submit but they've run a analysis recently check with them to make sure they want to clear the results.
	if (GLOBAL.AnalysisRun) {
		return confirm('When you navigate to a new analysis the current analysis results will be cleared! If you would like your results to be saved click the "Save to PDF" button. Are you sure you wish to navigate away?');
	}

	return true;
}

//This function fires when an item is dropped onto one of the independent/dependent variable DIVs in the data association tool.
function dropOntoVariableSelection(source, e, data) {
	data.node.attributes.oktousevalues = "N";
	var concept = createPanelItemNew(this.el, convertNodeToConcept(data.node));
	return true;
}

//This function fires when an item is dropped onto one of the
//independent/dependent variable DIVs in the data association tool.
//Used to ensure only a numeric value is dropped. For all values use dropOntoCategorySelection function
function dropNumericOntoCategorySelection(source, e, data) {
	var targetdiv = this.el;
	if (data.node.leaf == false && !data.node.isLoaded()) {
		data.node.reload(function () {
			dropNumericOntoCategorySelection2(source, e, data, targetdiv);
		});
	} else {
		dropNumericOntoCategorySelection2(source, e, data, targetdiv);
	}
	return true;
}

function dropNumericOntoCategorySelection2(source, e, data, targetdiv) {
	//Node must be folder so use children leafs
	if (data.node.leaf == false) {
		//Keep track of whether all the nodes are numeric or not.
		var allNodesNumeric = true;

		//Keep track of whether the folder has any leaves.
		var foundLeafNode = false;

		//Loop through child nodes to add them to input.
		for (var i = 0; i < data.node.childNodes.length; i++) {
			//Grab the child node.
			var child = data.node.childNodes[i];

			//This tells us whether it is a numeric or character node.
			var val = child.attributes.oktousevalues;

			//If we are a numeric leaf node, add it to the tree.
			if (val === 'Y' && child.leaf == true) {
				//Reset the alpha/numeric flag so we don't get the popup for entering a value.
				child.attributes.oktousevalues = "N";

				//Set the flag indicating we had a leaf node.
				foundLeafNode = true;

				//Add the item to the input.
				var concept = createPanelItemNew(targetdiv, convertNodeToConcept(child));

				//Set back to original value
				child.attributes.oktousevalues = val;
			}
			else if (val === 'N' && child.leaf == true) {
				//Set the flag indicating we had a leaf node.
				foundLeafNode = true;

				//If we find a non-numeric node, set our flag.
				allNodesNumeric = false
			}

		}

		//If no leaf nodes found, alert the user.
		if (!foundLeafNode) {
			Ext.Msg.alert('No Nodes in Folder', 'When dragging in a folder you must select a folder that has leaf nodes directly under it.');
		}

		//If we found a non numeric node, alert the user.
		if (!allNodesNumeric && foundLeafNode) {
			Ext.Msg.alert('Numeric Input Required', 'Please select numeric concepts only for this input. Numeric concepts are labeled with a "123" in the tree.');
		}
	}
	else {
		//If we dragged a numeric leaf, add it to the input. Otherwise alert the user.
		if (data.node.attributes.oktousevalues === 'Y') {
			//This tells us whether it is a numeric or character node.
			var val = data.node.attributes.oktousevalues;

			//Reset the alpha/numeric flag so we don't get the popup for entering a value.
			data.node.attributes.oktousevalues = "N";

			//Add the item to the input.
			var concept = createPanelItemNew(targetdiv, convertNodeToConcept(data.node));

			//Set back to original value
			data.node.attributes.oktousevalues = val;
		}
		else {
			Ext.Msg.alert('Numeric Input Required', 'Please select numeric concepts only for this input. Numeric concepts are labeled with a "123" in the tree.');
		}
	}
	return true;
}


//This function fires when an item is dropped onto one of the
//independent/dependent variable DIVs in the data association tool.
function dropOntoCategorySelection(source, e, data) {
	var targetdiv = this.el;

	if (data.node.leaf == false && !data.node.isLoaded()) {
		data.node.reload(function () {
			analysisConcept = dropOntoCategorySelection2(source, e, data, targetdiv);
		});
	}
	else {
		analysisConcept = dropOntoCategorySelection2(source, e, data, targetdiv);
	}
	return true;
}

function dropOntoCategorySelection2(source, e, data, targetdiv) {
	//Node must be folder so use children leafs
	if (data.node.leaf == false) {

		//Keep track of whether the folder has any leaves.
		var foundLeafNode = false;

		for (var i = 0; i < data.node.childNodes.length; i++) {
			//Grab the child node.
			var child = data.node.childNodes[i];

			//This tells us whether it is a numeric or character node.
			var val = child.attributes.oktousevalues;

			//Reset the alpha/numeric flag so we don't get the popup for entering a value.
			child.attributes.oktousevalues = "N";

			//If this is a leaf node, add it.
			if (child.leaf == true) {
				//Add the item to the input.
				var concept = createPanelItemNew(targetdiv, convertNodeToConcept(child));

				//Set the flag indicating we had a leaf node.
				foundLeafNode = true;
			}

			//Set back to original value
			child.attributes.oktousevalues = val;
		}
		//Adding this condition for certain nodes like Dosage and Response, where children of Dosage & Response are intentionally hidden 
		if (data.node.childrenRendered && data.node.firstChild == null) {
			foundLeafNode = true;
			var concept = createPanelItemNew(targetdiv, convertNodeToConcept(data.node));
		}

		//If no leaf nodes found, alert the user.
		if (!foundLeafNode) {
			Ext.Msg.alert('No Nodes in Folder', 'When dragging in a folder you must select a folder that has leaf nodes directly under it.');
		}
	} else {
		//This tells us whether it is a numeric or character node.
		var val = data.node.attributes.oktousevalues;

		//Reset the alpha/numeric flag so we don't get the popup for entering a value.
		data.node.attributes.oktousevalues = "N";

		//Add the item to the input.
		var concept = createPanelItemNew(targetdiv, convertNodeToConcept(data.node));

		//Set back to original value
		data.node.attributes.oktousevalues = val;
	}
	return concept;
}


function clearDataAssociation() {
	document.getElementById("dataAssociationBody").innerHTML = "";

	//Whenever we switch views, make the binning toggle false. All the analysis pages default to this state.
	GLOBAL.Binning = false;
	GLOBAL.ManualBinning = false;
	GLOBAL.NumberOfBins = 4;
	GLOBAL.AnalysisRun = false

}

function registerDragNDrop() {
	registerDragNDropOnDiv('divIndependentVariable', dropOntoCategorySelection);
}

function registerDragNDropOnDiv(targetDiv, notifyDropCallback) {
    var div = Ext.get(targetDiv);
    drop_target = new Ext.dd.DropTarget(div, {ddGroup: 'makeQuery'});
    drop_target.notifyDrop = notifyDropCallback;
}

function clearAnalysisData(divName) {
	//Clear the drag and drop div.
	var qc = Ext.get(divName);
	for (var i = qc.dom.childNodes.length - 1; i >= 0; i--) {
		var child = qc.dom.childNodes[i];
		qc.dom.removeChild(child);
	}
	// in highDimensionData.js
	clearHighDimDataSelections(divName);
	clearSummaryDisplay(divName);
}
