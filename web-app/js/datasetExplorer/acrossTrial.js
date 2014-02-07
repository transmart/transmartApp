

//This version of the runAllQueries function will generate the result instance ids for the cohorts built with across trial nodes.
function runQueryAcrossTrial(subset, callback)
{
	var topLevelObject = []
	var panelObject = {}
	var criteriaObjects = []


	//Loop through the number of criteria divs on the page.
	for(var i=1;i<=GLOBAL.NumOfQueryCriteriaGroups;i++)
	{
		panelObject = {}

		//Grab the div object so we can extract the criteria.
		var qcd=Ext.get("queryCriteriaDiv"+subset+'_'+i.toString());

		//If the div have children, extract the criteria.
		if(qcd.dom.childNodes.length>0)
		{
			//Reset the object we used.
			criteriaObjects = []

			//This is the DIV element.
			var qd = qcd.dom;

			//Record whether the query group has the include or exclude flag.
			var invert = jQuery("#" + qcd.dom.id).hasClass("queryGroupExclude")

			invert = invert ? 1 : 0

			//Record whether this panel should have the same event flag enforced.
			var sameEvent = jQuery("#" + qcd.dom.id).hasClass("queryGroupSAMEEVENT")

			sameEvent = sameEvent ? 1 : 0

			panelObject["invert"] = invert
			panelObject["panel_number"] = i
			panelObject["items"] = []
			panelObject["sameEvent"] = sameEvent

			//Loop through the children of the DIV element.
			for(var j=0;j<qd.childNodes.length;j++)
			{
				//Grab the child to extract the values we need.
				var itemel=qd.childNodes[j];

				var criteriaObject = buildCriteriaObject(
						itemel.getAttribute("conceptid"),
						itemel.getAttribute("setvaluemode"),
						itemel.getAttribute("setvaluehighlowselect"),
						itemel.getAttribute("setvalueoperator"),
						itemel.getAttribute("setvaluehighvalue"),
						itemel.getAttribute("setvaluelowvalue"),
						itemel.getAttribute("conceptdimcode"),
						itemel.getAttribute("conceptlevel"),
						itemel.getAttribute("oktousevalues"),
						itemel.getAttribute("inOutCode")
						)

				panelObject["items"].push(criteriaObject)

			}

			//All the items in the DIV get OR'ed together.
			topLevelObject.push(panelObject)

		}


	}

	//We want to pass criteria JSON that looks like this.
	/*
	{
		criteria:
					[
						[
						 	{
						 		criteriaType: "novalue",
						 		concept_cd: "1"
						 	},
						 	{
						 		criteriaType: "novalue",
						 		concept_cd: "2"
						 	}
						 ],
						 [
						 	{
						 		criteriaType: "novalue",
						 		concept_cd: "3"
						 	}
						 ]
					]

	}

	*/

	//This is an ugly workaround but we need to remove the prototype version of .toJSON
	var tempToJSONFunction = Array.prototype.toJSON;
	delete Array.prototype.toJSON


	jQuery.ajax({
		  	type: "POST",
		  	url: pageInfo.basePath+"/crossTrial/generateCohorts",
		  	contentType: 'application/json',
		  	dataType: "json",
		  	data: JSON.stringify({"criteria": topLevelObject}),
		  	success: function(data){runQueryAcrossTrialComplete(data, subset, callback);},
			error: function(){alert("ERROR");}
		})


	//Restore the prototype function.
	Array.prototype.toJSON = tempToJSONFunction;

}

function runQueryAcrossTrialComplete(data, subset, callbackFunction)
{
	GLOBAL.CurrentSubsetIDs[subset] = data.result_instance_id;

    if(STATE.QueryRequestCounter > 0) // I'm in a chain of requests so decrement
    {
        STATE.QueryRequestCounter = -- STATE.QueryRequestCounter;
    }
    if(STATE.QueryRequestCounter == 0)
    {
    	callbackFunction();
    }


}

function buildCriteriaObject(conceptId, criteriaType, highlowselect, valueOperator, valueHigh, valueLow, conceptFullName, conceptLevel, oktousevalues, inOutCode )
{

	var criteriaObject = {	conceptId:conceptId,
							criteriaType: criteriaType,
							highlowselect: highlowselect,
							valueOperator: valueOperator,
							valueHigh: valueHigh,
							valueLow: valueLow,
							conceptFullName: conceptFullName,
							conceptLevel: conceptLevel,
							oktousevalues: oktousevalues,
							inOutCode: inOutCode
						}

	return criteriaObject;
}

function buildAnalysisCrossTrial(name, modifierCode, level, oktousevalues, dropType, inOutCode)
{
   // alert(modifierCode)   ;
	if(dropType == 'stats')
	{
		analysisPanel.body.mask("Running analysis...", 'x-mask-loading');
	}

	if(dropType == 'grid')
	{
		analysisGridPanel.body.mask("Loading...", 'x-mask-loading');
	}

    if(isSubsetEmpty(1) && isSubsetEmpty(2))
    {
    	analysisPanel.body.unmask();
    	analysisGridPanel.body.unmask();
        alert('Empty subsets found, need a valid subset to analyze!');
        return;
    }

    if((GLOBAL.CurrentSubsetIDs[1] == null && ! isSubsetEmpty(1)) || (GLOBAL.CurrentSubsetIDs[2] == null && ! isSubsetEmpty(2)))
    {
        runAllQueries(function()
                {
        	buildAnalysisCrossTrial(name, modifierCode, level, oktousevalues, dropType, inOutCode);
                }
        );
        analysisPanel.body.unmask();
        return;
    }

	if(dropType == 'grid')
	{
		getAnalysisGridDataModifier(name, modifierCode, level, oktousevalues, inOutCode);
	}


	if(dropType == 'stats')
	{
		Ext.Ajax.request(
	            {
	                url : pageInfo.basePath+"/chart/analysis",
	                method : 'POST',
	                timeout: '600000',
	                params :  Ext.urlEncode(
	                        {
	                            charttype : "analysis",
	                            name : name,
	                            modifierCode : modifierCode,
	                            level : level,
	                            oktousevalues : oktousevalues,
	                            inOutCode : inOutCode,
                                concept_key : modifierCode,
	                            result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
	                            result_instance_id2 : GLOBAL.CurrentSubsetIDs[2]
	                        }
	                ),
	                success : function(result, request)
	                {
	                	//Add the code we just dragged in to the report list.
	                	GLOBAL.currentReportCodes.push(modifierCode)
	                	GLOBAL.currentReportStudy.push(name)

	                	buildAnalysisComplete(result);
	                }
		            ,
		            failure : function(result, request)
		            {
		                buildAnalysisComplete(result);
		            }
	            }
	    );
	}

}


function getAnalysisGridDataModifier(name, modifierCode, level, oktousevalues, inOutCode)
{

	isGridLoadRunning = true;
	isGridLoadDone = false;

	analysisGridPanel.body.mask("Loading...", 'x-mask-loading');

    var subset1 = GLOBAL.CurrentSubsetIDs[1] == null ? "" : GLOBAL.CurrentSubsetIDs[1];
    var subset2 = GLOBAL.CurrentSubsetIDs[2] == null ? "" : GLOBAL.CurrentSubsetIDs[2];
    var dataArray = {
    					name : name,
    					modifierCode : modifierCode,
    					level : level,
    					oktousevalues : oktousevalues,
    					inOutCode : inOutCode,
    					result_instance_id1:subset1,
    					result_instance_id2: subset2,
    					columnsOnly: false };

    setupGridViewWrapper(); //Setup the html for the jquery datatable

    setTimeout(function(){}, 1);

    $j.getJSON(analysisGridUrl + 'Modifier', dataArray, function (data) {

        //Add the configuration settings for the grid
    	setupGridData(data);

        gridPanelHeaderTips = data.headerToolTips.slice(0);

        //Add the callback for when the grid is redrawn
        data.fnDrawCallback = function( oSettings ) {

            $j(".dataTables_scrollHeadInner > table > thead > tr > th").each( function (index) {

                var titleAttr = $j(this).attr("title");

                if (titleAttr == null && gridPanelHeaderTips != null)
                {
                    $j(this).attr("title", gridPanelHeaderTips[index]);
                }

            });

            analysisGridPanel.body.unmask();

        };

        modifyDataTable();

        setTimeout(function(){}, 1);

	    $j('#gridViewTable').dataTable(data);

        $j(window).bind('resize', function () {
        	$j('#gridViewTable').dataTable().fnAdjustColumnSizing()
          } );


    });

    analysisGridPanel.doLayout();
    resultsTabPanel.body.unmask();
}

