/* SubsetTool.js
 Jeremy M. Isikoff
 Recombinant */
String.prototype.trim = function() {
    return this.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
};

Ext.layout.BorderLayout.Region.prototype.getCollapsedEl = Ext.layout.BorderLayout.Region.prototype.getCollapsedEl.createSequence(function()
    {
        if ((this.position == 'north' || this.position == 'south') && ! this.collapsedEl.titleEl)
        {
            this.collapsedEl.titleEl = this.collapsedEl.createChild(
                {
                    style : 'color:#15428b;font:11px/15px tahoma,arial,verdana,sans-serif;padding:2px 5px;', cn : this.panel.title
                }
            );
        }
    }
);

var runner = new Ext.util.TaskRunner();


Ext.Panel.prototype.setBody = function(html)
{
    var el = this.getEl();
    var domel = el.dom.lastChild.firstChild;
    domel.innerHTML = html;
};

Ext.Panel.prototype.getBody = function(html)
{
    var el = this.getEl();
    var domel = el.dom.lastChild.firstChild;
    return domel.innerHTML;
};

Ext.onReady(function()
    {
        if(sampleRequestType == "search")
        {
            createMainExplorerWindow(pageInfo.basePath+'/' + GLOBAL.explorerType + '/showMainSearchPage', null, pageInfo.basePath+'/' + GLOBAL.explorerType + '/showTopLevelListPage', null, true);
        }
        else
        {
            initializeGlobalGridColumnList();

            createMainExplorerWindow(
                    pageInfo.basePath+'/' + GLOBAL.explorerType + '/showWestPanelSearch',
                {"SearchJSON" : GLOBAL.SearchJSON},
                null,
                null,
                false);

            GLOBAL.SearchJSON["result_instance_id"] = currentResultInstanceId;

            Ext.getCmp('queryPanel').load(
                {
                    url : pageInfo.basePath+'/' + GLOBAL.explorerType + '/showDataSetResults',
                    jsonData : {"SearchJSON" : GLOBAL.SearchJSON, "showCohortInformation":"TRUE", "result_instance_id":currentResultInstanceId},
                    callback : loadDataSetGrid
                }
            );

            Ext.getCmp('clearsearchbutton').setVisible(true);
            //Ext.getCmp('addsubset').setVisible(true);

        }

        Ext.getCmp('westPanel').body.setStyle('border','2px solid #D0D0D0');
        Ext.getCmp('centerPanel').body.setStyle('border','2px solid #D0D0D0');
    }
);


/*
 * Iterate through an array and remove an item if it is present.
 */
function removeFromArray(string, array)
{
    for(i in array)
    {
        if(array[i] == string)
        {
            array.splice(i, 1);
        }
    }

    return array;
}

//This will create a string of the selected items from a JSON object.
function createValuesString(JSONObject)
{
    //This will be the string we return.
    var valueString = '';

    //Iterate over the categories in the JSON object.
    for(var key in JSONObject)
    {
        //If the categories array actually has items in it, we need to add them to a string.
        if(JSONObject[key].length > 0 && (key != "GridColumnList") && (key != "result_instance_id"))
        {
            //WE need a comma if the string isn't empty.
            if(valueString != '') valueString += ', ';

            //Replace the underscores in the category with a space.
            var newKey = key.replace('_',' ');

            //Add the values to our string.
            valueString += newKey + ": (" + JSONObject[key] + ")";
        }
        else if(key == "result_instance_id")
        {
            valueString += "Dataset Explorer Patient Selection"
        }
    }

    return valueString;
}

//We may have something like .. [DataType:Gene Expression, count:236, Pathology:Liver, Cancer of, Tissue:Liver [A03.620], DataSet:PRECOS]
//But we need the values to be lists, not just strings.
//This function will loop over the keys and make the values arrays, lists, whatever they are called.
function convertJsonValuesToLists(JsonToConvert)
{
    var newJsonData = {};

    for (jsonItem in JsonToConvert)
    {
        var tempValue = JsonToConvert[jsonItem];

        newJsonData[jsonItem] = [];

        newJsonData[jsonItem].push(tempValue);
    }

    return newJsonData;
}

//If we have an array like [{blah:"blah"},{blah:"blah2"}] we want to convert it to [{blah:["blah","blah2"]}]
function arrayOfJsonToJsonData(JsonToConvert)
{
    //This will be the final object we return.
    var finalJsonData = {};

    //Loop through each of the sets of JSON data.
    for (JsonSet in JsonToConvert)
    {
        //For each set we have multiple pairs of key/values that we need to add to the final object.
        for (jsonItem in JsonToConvert[JsonSet].data)
        {
            //Make sure the key exists.
            if(!finalJsonData[jsonItem]) finalJsonData[jsonItem] = [];

            //Add this key/value pair to the final hash.
            finalJsonData[jsonItem].push(JsonToConvert[JsonSet].data[jsonItem]);

        }
    }

    //Now we need to distinct each of the JSON lists.
    for (jsonItem in finalJsonData)
    {
        finalJsonData[jsonItem] = finalJsonData[jsonItem].unique();
    }

    return finalJsonData;
}

//This might be inefficient. 
//Return new array with duplicate values removed
Array.prototype.unique =
    function() {
        var a = [];
        var l = this.length;
        for(var i=0; i<l; i++) {
            for(var j=i+1; j<l; j++) {
                // If this[i] is found later in the array
                if (this[i] === this[j])
                    j = ++i;
            }
            a.push(this[i]);
        }
        return a;
    };

Array.prototype.has=function(v){
    for (i=0; i<this.length; i++){
        if (this[i]==v) return true;
    }
    return false;
};

function initializeGlobalGridColumnList(panelNumber)
{
    var panelNumber = (panelNumber || "");

    //Reinitialize our JSON and the array for this category.
    GLOBAL["SearchJSON" + panelNumber] = {};

    //Add the list of columns.
    GLOBAL["SearchJSON" + panelNumber]["GridColumnList" + panelNumber] = [];

    for(var loopCounter=0; loopCounter < GLOBAL.columnMap.columns.length; loopCounter++){

        //Only if this column is to appear in the grid do we add it here.
        if(GLOBAL.columnMap.columns[loopCounter].showInGrid)
        {
            GLOBAL["SearchJSON" + panelNumber]["GridColumnList" + panelNumber].push(GLOBAL.columnMap.columns[loopCounter].dataIndex);
        }
    }

}

function getFieldsListForReader()
{
    var returnColumnMaps = [];

    for(var loopCounter=0; loopCounter < GLOBAL.columnMap.columns.length; loopCounter++)
    {
        var newColumnMaps = {};

        newColumnMaps.name = GLOBAL.columnMap.columns[loopCounter].dataIndex;

        returnColumnMaps.push(newColumnMaps);
    }

    //We always need a count field.
    returnColumnMaps.push({name:'count'});
    returnColumnMaps.push({name:'STUDY_ID'});

    return returnColumnMaps;
}

function getFieldListForColModel()
{
    var returnColumnMaps = [];

    for(var loopCounter=0; loopCounter < GLOBAL.columnMap.columns.length; loopCounter++)
    {

        var newColumnMaps = {};

        newColumnMaps.header = GLOBAL.columnMap.columns[loopCounter].header;
        newColumnMaps.sortable = true;
        newColumnMaps.dataIndex = GLOBAL.columnMap.columns[loopCounter].dataIndex;
        newColumnMaps.width = GLOBAL.columnMap.columns[loopCounter].width || '5';

        if(!GLOBAL.columnMap.columns[loopCounter].showInGrid) newColumnMaps.hidden = true;
        if(GLOBAL.columnMap.columns[loopCounter].dataIndex == "STUDY_ID") newColumnMaps.renderer = renderDSELink;

        returnColumnMaps.push(newColumnMaps);

    }

    //Hacky, change this!
    if(GLOBAL.explorerType == "sampleExplorer")
    {
        returnColumnMaps.push({header:'Aliquot Count',dataIndex:'count', width:'5', renderer: renderSampleLink});
    }
    else
    {
        returnColumnMaps.push({header:'Term Count',dataIndex:'count', width:'5'});
    }

    return returnColumnMaps;

}