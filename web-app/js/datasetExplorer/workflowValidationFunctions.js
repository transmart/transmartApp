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

function detectMissingInput(stringToEvaluate, variableBoxName)
{
	
	if(stringToEvaluate == '')
	{
		Ext.Msg.alert('Missing input', 'Please drag at least one concept into the ' + variableBoxName + ' variable box.');
		return false;
	}
	else
	{
		return true;
	}

}

function detectMultipleNodeTypes(listToEvaluate, variableBoxName)
{
	if(listToEvaluate.length > 1)
	{
		Ext.Msg.alert('Wrong input', 'You may only drag nodes of the same type (Continuous,Categorical,High Dimensional) into the input box. The ' + variableBoxName + ' input box has multiple types.');
		return false;		
	}	
	else
	{
		return true;
	}
}

function detectSingleCategoricalNode(nodeList, variableBoxValue, variableBoxName)
{
	if((!nodeList[0] || nodeList[0] == "null") && (variableBoxValue != "") && (!variableBoxValue.match(/\|/g)))
	{
		Ext.Msg.alert('Wrong input', 'The ' + variableBoxName + ' input box must have more than one categorical input to be used.');
		return false;		
	}	
	else
	{
		return true;
	}	
}

function detectMultipleValueNodes(nodeList, variableBoxValue, variableBoxName)
{
	if((nodeList[0] == 'valueicon' || nodeList[0] == 'hleaficon') && (variableBoxValue.indexOf("|") != -1))
	{
		Ext.Msg.alert('Wrong input', 'For continuous and high dimensional data, you may only drag one node into the input boxes. The ' + variableBoxName + ' input box has multiple nodes.');
		return false;		
	}	
	else
	{
		return true;
	}
}

function detectCatBinningWithoutManual(globalBinningFlag, binningTypeInputName, manualBinningCheckboxName, enableBinningInput, variableBoxName)
{
	if(globalBinningFlag && Ext.get(binningTypeInputName).getValue() != 'Continuous' && !document.getElementById(manualBinningCheckboxName).checked && document.getElementById(enableBinningInput).checked)
	{
		Ext.Msg.alert('Wrong input', 'You must enable manual binning when binning a categorical variable. (' + variableBoxName + ' Variable)');
		return false;			
	}		
	else
	{
		return true;
	}

}

function detectCatBinnedAsCont(globalBinningFlag, enableBinningInput, binningTypeInputName, stringToEvaluate, listToEvaluate, snpTypeWindow, markerTypeWindow, variableBoxName)
{
	if(globalBinningFlag && document.getElementById(enableBinningInput).checked && Ext.get(binningTypeInputName).getValue() == 'Continuous' && ((stringToEvaluate != "" && (!listToEvaluate[0] || listToEvaluate[0] == "null")) || (listToEvaluate[0] == 'hleaficon' && window[snpTypeWindow] == "Genotype" && window[markerTypeWindow] == 'SNP')) )
	{
		Ext.Msg.alert('Wrong input', 'There is a categorical input in the ' + variableBoxName + ' variable box, but you are trying to bin it as if it was continuous. Please alter your binning options or the concept in the ' + variableBoxName + ' variable box.');
		return false;		
	}		
	else
	{
		return true;
	}

}

function detectMultipleCategoricalNodesWithoutBinning(nodeList, variableBoxValue, variableBoxName, inputMax, binningEnabled)
{
	if((!nodeList[0] || nodeList[0] == "null") && (variableBoxValue.match(/\|/g)) && (variableBoxValue.match(/\|/g).length + 1 > inputMax) && (!binningEnabled))
	{
		Ext.Msg.alert('Wrong input', 'The ' + variableBoxName + ' input box has too many categorical inputs specified, you may only use ' + inputMax + '.');
		return false;		
	}	
	else
	{
		return true;
	}
}

function detectMultipleCategoricalNodesWithoutBinningAndGroupCheckboxChecked(nodeList, variableBoxValue, variableBoxName, inputMax, binningEnabled, checkboxValue)
{
	if((!nodeList[0] || nodeList[0] == "null") && (variableBoxValue.match(/\|/g)) && (variableBoxValue.match(/\|/g).length + 1 >= inputMax) && (!binningEnabled) && (checkboxValue))
	{
		Ext.Msg.alert('Wrong input', 'When using the grouping checkbox you may only supply one categorical variable to the ' + variableBoxName + ' variable. Please enable binning or remove variables from this box.');
		return false;		
	}	
	else
	{
		return true;
	}
}

function detectMultipleBinsWithGroupCheckboxChecked(binning, numberOfBins, checkboxValue)
{
	if(binning && numberOfBins > 1 && checkboxValue)
	{
		Ext.Msg.alert('Wrong input', 'When using the grouping checkbox you may only supply one bin for the dependent variable.');
		return false;		
	}	
	else
	{
		return true;
	}	
}

function detectOneBinGroupCheckBoxUnchecked(binning, numberOfBins, checkboxValue)
{
	if(binning && numberOfBins == 1 && !checkboxValue)
	{
		Ext.Msg.alert('Wrong input', 'When using binning you must either specify two bins or enable the grouping checkbox.');
		return false;		
	}	
	else
	{
		return true;
	}		
}


function detectOneBinGroup(binning, numberOfBins, variableBoxName)
{
	if(binning && numberOfBins < 2)
	{
		Ext.Msg.alert('Wrong input', 'You cannot specify less than 2 bins for the ' + variableBoxName + ' input box.');
		return false;		
	}	
	else
	{
		return true;
	}		
}





