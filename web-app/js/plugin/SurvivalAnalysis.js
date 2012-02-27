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
function submitSurvivalJob(form) {
	var timeVariableEle = Ext.get("divTimeVariable");
	var categoryVariableEle = Ext.get("divCategoryVariable");
	var censoringVariableEle = Ext.get("divCensoringVariable");

	var timeVariableConceptCode = ""
	var categoryVariableConceptCode = ""
	var censoringVariableConceptCode = ""

	if (timeVariableEle.dom.childNodes[0])
		timeVariableConceptCode = getQuerySummaryItem(timeVariableEle.dom.childNodes[0]);

	// If the category variable element has children, we need to parse them and
	// concatenate their values.
	if (categoryVariableEle.dom.childNodes[0]) {
		// Loop through the category variables and add them to a comma seperated
		// list.
		for (nodeIndex = 0; nodeIndex < categoryVariableEle.dom.childNodes.length; nodeIndex++) {
			// If we already have a value, add the seperator.
			if (categoryVariableConceptCode != '')
				categoryVariableConceptCode += '|'

				// Add the concept path to the string.
			categoryVariableConceptCode += getQuerySummaryItem(
					categoryVariableEle.dom.childNodes[nodeIndex]).trim()
		}
	}

	if (censoringVariableEle.dom.childNodes[0])
		censoringVariableConceptCode = getQuerySummaryItem(censoringVariableEle.dom.childNodes[0]);

	var formParams = {
		timeVariable : timeVariableConceptCode,
		categoryVariable : categoryVariableConceptCode,
		censoringVariable : censoringVariableConceptCode
	};

	//These default to FALSE
	formParams["binning"] = "FALSE";
	formParams["manualBinning"] = "FALSE";
	
	// Gather the data from the optional binning items, if we had selected to
	// enable binning.
	if (GLOBAL.Binning) {
		// Get the number of bins the user entered.
		var numberOfBins = Ext.get("txtNumberOfBins").getValue()

		// Get the value from the dropdown that specifies the type of
		// binning.
		var binningType = Ext.get("selBinDistribution").getValue()

		// Add these items to our form parameters.
		formParams["binning"] = "TRUE";
		formParams["numberOfBins"] = numberOfBins;
		formParams["binDistribution"] = binningType;

		// If we are using Manual Binning we need to add the parameters
		// here.
		if (GLOBAL.ManualBinning) {

			// Get a bar separated list of bins and their ranges.
			var binRanges = ""

			// Loop over each row in the HTML table.
			var variableType = Ext.get('variableType').getValue();
			if (variableType == "Continuous") {
				for (i = 1; i <= GLOBAL.NumberOfBins; i++) {
					binRanges += "bin" + i + ","
					binRanges += Ext.get('txtBin' + i + 'RangeLow').getValue()
							+ ","
					binRanges += Ext.get('txtBin' + i + 'RangeHigh').getValue()
							+ "|"
				}
			} else {
				for (i = 1; i <= GLOBAL.NumberOfBins; i++) {
					binRanges += "bin" + i + ","
					var bin = Ext.get('divCategoricalBin' + i);
					for (x = 0; x < bin.dom.childNodes.length; x++) {
						binRanges+=bin.dom.childNodes[x].getAttribute('conceptid') + ","
					}
					binRanges=binRanges.substring(0, binRanges.length - 1);
					binRanges=binRanges+"|";
				}
			}
			formParams["manualBinning"] = "TRUE";
			formParams["binRanges"] = binRanges.substring(0,binRanges.length - 1);
			formParams["variableType"] = Ext.get('variableType').getValue();
		}

		
	}

	submitJob(formParams);
}

function loadSurvivalAnalysisView() {
	registerSurvivalDragAndDrop();
}

function registerSurvivalDragAndDrop() {
	// Set up drag and drop for Dependent and Independent variables on the data
	// association tab.
	// Get the Dependent DIV.
	var timeDiv = Ext.get("divTimeVariable");
	// Get the Independent DIV
	var categoryDiv = Ext.get("divCategoryVariable");
	// Get the Censoring DIV
	var censoringDiv = Ext.get("divCensoringVariable");

	// Add the drop targets and handler function.
	dtgD = new Ext.dd.DropTarget(timeDiv, {
		ddGroup : 'makeQuery'
	});
	dtgD.notifyDrop = dropOntoVariableSelection;

	dtgI = new Ext.dd.DropTarget(categoryDiv, {
		ddGroup : 'makeQuery'
	});
	dtgI.notifyDrop = dropOntoVariableSelection;

	dtgI = new Ext.dd.DropTarget(censoringDiv, {
		ddGroup : 'makeQuery'
	});
	dtgI.notifyDrop = dropOntoVariableSelection;

}

function clearGroupSurvival(divName) {
	// Clear the drag and drop div.
	var qc = Ext.get(divName);

	for ( var i = qc.dom.childNodes.length - 1; i >= 0; i--) {
		var child = qc.dom.childNodes[i];
		qc.dom.removeChild(child);
	}

}

function toggleBinning() {
	// Change the Binning flag.
	GLOBAL.Binning = !GLOBAL.Binning;

	// Toggle the div with the binning options.
	Ext.get('divBinning').toggle();

	// Change the toggle button text.
	if (GLOBAL.Binning) {
		document.getElementById('BinningToggle').value = "Disable"
	} else {
		document.getElementById('BinningToggle').value = "Enable"
	}
}

function updateManualBinning() {
	// Change the ManualBinning flag.
	GLOBAL.ManualBinning = document.getElementById('chkManualBin').checked;

	// Get the type of the variable we are dealing with.
	variableType = Ext.get('variableType').getValue();

	// Hide both DIVs.
	var divContinuous = Ext.get('divManualBinContinuous');
	var divCategorical = Ext.get('divManualBinCategorical');
	divContinuous.setVisibilityMode(Ext.Element.DISPLAY);
	divCategorical.setVisibilityMode(Ext.Element.DISPLAY);
	divContinuous.hide();
	divCategorical.hide();
	// Show the div with the binning options relevant to our variable type.
	if (document.getElementById('chkManualBin').checked) {
		if (variableType == "Continuous") {
			divContinuous.show();
			divCategorical.hide();
		} else {
			divContinuous.hide();
			divCategorical.show();
			setupCategoricalItemsList();
		}
	}
}

/**
 * When we change the number of bins in the "Number of Bins" input, we have to
 * change the number of bins on the screen.
 */
function manageBins(newNumberOfBins) {

	// This is the row template for a continousBinningRow.
	var tpl = new Ext.Template(
			'<tr id="binningContinousRow{0}">',
			'<td>Bin {0}</td><td><input type="text" id="txtBin{0}RangeLow" /> - <input type="text" id="txtBin{0}RangeHigh" /></td>',
			'</tr>');
	var tplcat = new Ext.Template(
			'<tr id="binningCategoricalRow{0}">',
			'<td><b>Bin {0}</b><div id="divCategoricalBin{0}" class="queryGroupIncludeSmall"></div></td>',
			'</tr>');

	// This is the table we add continuous variables to.
	continuousBinningTable = Ext.get('tblBinContinuous');
	categoricalBinningTable = Ext.get('tblBinCategorical');
	// Clear all old rows out of the table.

	// For each bin, we add a row to the binning table.
	for (i = 1; i <= newNumberOfBins; i++) {
		// If the object isn't already on the screen, add it.
		if (!(Ext.get("binningContinousRow" + i))) {
			tpl.append(continuousBinningTable, [ i ]);
		} else {
			Ext.get("binningContinousRow" + i).show()
		}

		// If the object isn't already on the screen, add it-Categorical
		if (!(Ext.get("binningCategoricalRow" + i))) {
			tplcat.append(categoricalBinningTable, [ i ]);
			// Add the drop targets and handler function.
			var bin = Ext.get("divCategoricalBin" + i);
			var dragZone = new Ext.dd.DragZone(bin, {
				ddGroup : 'makeBin'
			});
			var dropZone = new Ext.dd.DropZone(bin, {
				ddGroup : 'makeBin'
			});
						
			// dropZone.notifyEnter = test;
			dropZone.notifyDrop = dropOntoBin; // dont forget to make each
			// dropped
			// node a drag target
		} else {
			Ext.get("binningCategoricalRow" + i).show()
		}
	}

	// If the new number of bins is less than the old, hide the old bins.
	if (newNumberOfBins < GLOBAL.NumberOfBins) {
		// For each bin, we add a row to the binning table.
		for (i = parseInt(newNumberOfBins) + 1; i <= GLOBAL.NumberOfBins; i++) {
			// If the object isn't already on the screen, add it.
			if (Ext.get("binningContinousRow" + i)) {
				Ext.get("binningContinousRow" + i).hide();
			}
			// If the object isn't already on the screen, add it.
			if (Ext.get("binningCategoricalRow" + i)) {
				Ext.get("binningCategoricalRow" + i).hide();
			}
		}
	}

	// Set the global variable to reflect the new bin count.
	GLOBAL.NumberOfBins = newNumberOfBins;
	updateManualBinning();
}

function dropOntoBin(source, e, data) {
	this.el.appendChild(data.ddel);
	// Ext.dd.Registry.register(data.ddel, {el : data.ddel});
	return true;
}

function setupCategoricalItemsList() {
	// copy from the category div at top of page first and add drag handlers
	var categoricalSourceDiv = Ext.get("divCategoryVariable");
	var categoricalTargetDiv = Ext.get("divCategoricalItems");

	// clear it out first
	while (categoricalTargetDiv.dom.hasChildNodes())
		categoricalTargetDiv.dom
				.removeChild(categoricalTargetDiv.dom.firstChild);
	for ( var i = 0, n = categoricalSourceDiv.dom.childNodes.length; i < n; ++i) {
		// clone and append
		var newnode = categoricalSourceDiv.dom.childNodes[i].cloneNode(true);
		categoricalTargetDiv.dom.appendChild(newnode);
		// add drag handler
		Ext.dd.Registry.register(newnode, {
			el : newnode
		});
	}
	var dragZone = new Ext.dd.DragZone(categoricalTargetDiv.dom.parentNode, {
		ddGroup : 'makeBin'
	});

	var dropZone = new Ext.dd.DropZone(categoricalTargetDiv, {
		ddGroup : 'makeBin'
	});
	dropZone.notifyDrop = dropOntoBin;
}
