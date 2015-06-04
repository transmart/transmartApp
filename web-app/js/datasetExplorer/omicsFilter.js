/**
 * Created by dverbeec on 09/06/2015.
 */

/**
 * Function to be called when a high dimensional concept is dropped. This will look up the high dimensional
 * data-type, and display the appropriate dialog.
 * @param node    The dropped high dimensional concept node
 * @param filter  If true, a filter dialog will be shown (for cohort selection); if false, a selection dialog
 *                will be shown (when dropped in summary statistics or grid view)
 */
function highDimensionalConceptDropped(node, filter) {
    Ext.Ajax.request({
        url: pageInfo.basePath + "/omicsPlatformSearch/getPlatform",
        method: 'GET',
        timeout: '60000',
        params: Ext.urlEncode({
            concept_key: node.id
        }),
        success: filter ? showOmicsFilterDialog : showOmicsSelectionDialog,
        failure: function (result, request) {
            alert("Could not retrieve platform for " + node.id + ": " + result.statusText);
        }
    });
}

function showOmicsFilterDialog(result, request) {
    if (result.responseText == 'null') {
        alert("Could not retrieve platform: " + result.statusText);
    }
    else {
        var platform = JSON.parse(result.responseText);
        if (platform.markerType == 'Gene Expression') {
            showGeneExprFilterDialog(platform);
        }
        else
            alert("Support for " + platform.markerType + " is not yet implemented!");
    }
}

function showOmicsSelectionDialog(result, request) {
    if (result == null) {
        alert("Could not retrieve platform: " + result.statusText);
    }
    else {
        var platform = JSON.parse(result.responseText);
        if (platform.markerType == 'Gene Expression') {
            showGeneSelectionDialog(platform);
        }
        else
            alert("Support for " + platform.markerType + " is not yet implemented!");
    }
}

function applyGeneExprFilterDialog(validation) {

    var params = {
        gene_symbol: jQuery("#gene-searchbox").val(),
        threshold1: jQuery("#expression-amount-min").val(),
        threshold2: jQuery("#expression-amount-max").val(),
        operator: "BETWEEN",
        projection_type: jQuery("input[name=gene-expression-projection]:checked").val()
    }
    if (validation && params.gene_symbol == "") {
        alert("You must choose a gene.");
        return;
    }

    geneExprFilterValues = [];
    geneExprSliderLowHandleRatio = 0;
    geneExprSliderHighHandleRatio = 1;

    // make sure that there is a value set
    if (validation && (!jQuery.isNumeric(params.threshold1) || !jQuery.isNumeric(params.threshold2))) {
        alert('You must specify a numeric value.');
    } else {

        if (validation)
            geneExprFilterDialogComplete(params);

        if (STATE.Dragging) {
            jQuery('#' + selectedConcept.id).remove()
            removeUselessPanels()
        }

        geneexprfilterwin.hide();
    }
}

function showGeneExprFilterDialog(platform)
{
    jQuery("#gene-searchbox").val("");
    jQuery("#gene-expr-filter-gplid").text(platform.id);
    jQuery("#gene-searchbox").autocomplete("option", "source", "/transmart/omicsPlatformSearch/searchAutoComplete?gplid=" + encodeURIComponent(platform.id));
    geneexprfilterwin.setHeight(140); //set height back to old closed
    jQuery("[id^=gene-expression-slider-row]").css({'display': 'none'});

    document.getElementById("gene-expression-filter-histogram").innerHTML = ""
    var top = jQuery("#resultsTabPanel").offset().top + jQuery("#resultsTabPanel").height() / 2 - setvaluewin.height / 1.5;
    var left = jQuery("#resultsTabPanel").offset().left + jQuery("#resultsTabPanel").width() / 2 - setvaluewin.width / 2;

    geneexprfilterwin.setPosition(left, top);
    geneexprfilterwin.show(viewport);
    jQuery("#gene-searchbox").focus();
}

function geneExprFilterDialogComplete(params)
{
    var conceptnode = selectedConcept;
    setGeneExprFilterValue(conceptnode, params);
    if(STATE.Dragging==true){
        STATE.Dragging=false;
        moveSelectedConceptFromHoldingToTarget();
    }
}

function setGeneExprFilterValue(conceptnode, params)
{
    conceptnode.setAttribute("setvaluemode","omics_value");
    conceptnode.setAttribute("selector", params.gene_symbol);
    conceptnode.setAttribute("omicsoperator", params.operator);
    conceptnode.setAttribute("omicsvalue", params.threshold1 + ':' + params.threshold2);
    conceptnode.setAttribute("omicsprojection", params.projection_type);
    conceptnode.setAttribute("omicsvaluetype", "GENE_EXPRESSION");

    var valuetext="";
    valuetext=getGeneExprFilterValueText(params);
    conceptnode.setAttribute('conceptsetvaluetext',valuetext);
    var conceptname=conceptnode.getAttribute("conceptname");
    jQuery('#' + conceptnode.id + " .concept-text").html(conceptname + " " + valuetext);
    var subset=getSubsetFromPanel(conceptnode.parentNode);
    invalidateSubset(subset);
}

function getGeneExprFilterValueText(params)
{
    var result = "";
    switch (params.operator) {
        case "BETWEEN":
            result = params.threshold1 + " <= " + params.gene_symbol + " <= " + params.threshold2;
            break;
    }
    return "<em>" + result + "</em>";
}

function showGeneSelectionDialog(platform) {
    jQuery("#gene-selection-searchbox").val("");
    jQuery("#gene-selection-searchbox").autocomplete("option", "source", "/transmart/omicsPlatformSearch/searchAutoComplete?gplid=" + encodeURIComponent(platform.id));
    jQuery("#gene-selection-concept-key").val(platform.concept_key);
    jQuery("[id^=gene-expression-slider-row]").css({'display': 'none'});

    var top = jQuery("#resultsTabPanel").offset().top + jQuery("#resultsTabPanel").height() / 2 - setvaluewin.height / 1.5;
    var left = jQuery("#resultsTabPanel").offset().left + jQuery("#resultsTabPanel").width() / 2 - setvaluewin.width / 2;

    geneselectionwin.setPosition(left, top);
    geneselectionwin.show(viewport);
}

function applyGeneSelectionDialog(validation) {

    var gene_symbol = document.getElementById("gene-selection-searchbox").value;
    var concept_key = document.getElementById("gene-selection-concept-key").value;
    if (validation && gene_symbol == "") {
        alert("You must choose a gene.");
        return;
    }

    if (validation) {
        resultsTabPanel.body.mask("Running analysis...", 'x-mask-loading');
        var omics_params = {omics_selector: gene_symbol,
            omics_value_type: 'GENE_EXPRESSION',
            omics_projection_type: jQuery("input[name=gene-selection-projection]:checked").val()};
        Ext.Ajax.request(
            {
                url : pageInfo.basePath+"/chart/analysis",
                method : 'POST',
                timeout: '600000',
                params :  Ext.urlEncode(
                    {
                        charttype : "analysis",
                        concept_key : concept_key,
                        omics_selector : omics_params.omics_selector,
                        omics_value_type: omics_params.omics_value_type,
                        omics_projection_type: omics_params.omics_projection_type,
                        result_instance_id1 : GLOBAL.CurrentSubsetIDs[1],
                        result_instance_id2 : GLOBAL.CurrentSubsetIDs[2]
                    }
                ), // or a URL encoded string
                success: function (result, request) {
                    buildAnalysisComplete(result);
                    resultsTabPanel.body.unmask();
                },
                failure: function (result, request) {
                    alert("A problem arose while trying to retrieve the results")
                    resultsTabPanel.body.unmask();
                }
            }
        );

        getAnalysisGridData(concept_key, omics_params);
    }

    geneselectionwin.hide();
}

function showConceptDistributionHistogramForGeneExprFilter()
{
    var concept_key = selectedConcept.getAttribute('conceptid');

    Ext.Ajax.request(
        {
            url: pageInfo.basePath+"/chart/conceptDistribution",
            method: 'POST',
            success: function(result, request){showConceptDistributionHistogramForGeneExprFilterComplete(result);},
            failure: function(result, request){showConceptDistributionHistogramForGeneExprFilterComplete(result);},
            timeout: '300000',
            params: Ext.urlEncode({
                concept_key: concept_key,
                omics_selector: document.getElementById("gene-searchbox").value,
                omics_platform: document.getElementById("gene-expr-filter-gplid").innerHTML,
                omics_value_type: 'GENE_EXPRESSION',
                omics_projection_type: jQuery("input[name=gene-expression-projection]:checked").val()})
        });
}

function showConceptDistributionHistogramForGeneExprFilterComplete(result)
{
    var concept_key = selectedConcept.getAttribute('conceptid');

    geneexprfilterwin.setHeight(370);

    if (result == null) {
        jQuery("[id^=gene-expression-slider-row]").css({'display': 'none'});
        return document.getElementById("gene-expression-filter-histogram").innerHTML = "<div class='x-mask-loading'><div class='conceptDistributionPlaceholder'/></div>";
    }

    Ext.Ajax.request(
        {
            url: pageInfo.basePath + "/chart/conceptDistributionValues",
            method: 'GET',
            success: function(result, request) {
                geneExprFilterValues = eval(result.responseText);
                if (geneExprFilterValues.length > 0) {
                    geneExprFilterValues.sort(function (a, b) {return a - b}); // sort numerically rather then string-based
                    jQuery("[id^=gene-expression-slider-row]").css({'display': 'table-row'});
                    jQuery("#gene-expression-range").slider('option',{'min': geneExprFilterValues[0] * geneExprSliderFactor, 'max': geneExprFilterValues[geneExprFilterValues.length - 1] * geneExprSliderFactor})
                    jQuery("#gene-expression-range").slider('values', 0, (geneExprFilterValues[0] + geneExprSliderLowHandleRatio * (geneExprFilterValues[geneExprFilterValues.length - 1] - geneExprFilterValues[0])) * geneExprSliderFactor);
                    jQuery("#gene-expression-range").slider('values', 1, (geneExprFilterValues[0] + geneExprSliderHighHandleRatio * (geneExprFilterValues[geneExprFilterValues.length - 1] - geneExprFilterValues[0])) * geneExprSliderFactor);
                    geneExpFilterSliderUpdated();
                }
                else {
                    jQuery("[id^=gene-expression-slider-row]").css({'display': 'none'});
                    jQuery( "#expression-amount-min" ).val(0);
                    jQuery( "#expression-amount-max" ).val(0);
                    document.getElementById("gene-expression-filter-histogram").innerHTML = "No data is available for the given gene.";
                    geneexprfilterwin.setHeight(140);
                }
            },
            failure: function(result, request) {
                jQuery("#gene-expression-slider-row").css({'display': 'none'});
                jQuery( "#expression-amount-min" ).val(0);
                jQuery( "#expression-amount-max" ).val(0);
                document.getElementById("gene-expression-filter-histogram").innerHTML = "An error occured while retrieving the histogram values: " + result.responseText;
            },
            timeout: '30000',
            params: Ext.urlEncode({
                concept_key: concept_key,
                omics_selector: document.getElementById("gene-searchbox").value,
                omics_platform: document.getElementById("gene-expr-filter-gplid").innerHTML,
                omics_value_type: 'GENE_EXPRESSION',
                omics_projection_type: jQuery("input[name=gene-expression-projection]:checked").val()
            })
        }
    );

    document.getElementById("gene-expression-filter-histogram").innerHTML = result.responseText;
}

function showConceptDistributionHistogramForGeneExprFilterForSubset()
{
    var concept_key = selectedConcept.getAttribute('conceptid');

    Ext.Ajax.request(
        {
            url: pageInfo.basePath + "/chart/conceptDistributionForSubset",
            method: 'POST',
            success: function (result, request) {
                showConceptDistributionHistogramForGeneExprFilterForSubsetComplete(result);
            },
            failure: function (result, request) {
                showConceptDistributionHistogramForGeneExprFilterForSubsetComplete(result);
            },
            timeout: '300000',
            params: Ext.urlEncode({
                concept_key: concept_key,
                omics_selector: document.getElementById("gene-searchbox").value,
                omics_value_type: 'GENE_EXPRESSION',
                omics_projection_type: 'ZSCORE',
                result_instance_id1: " ", // hack: chartcontroller will see this as non-empty,
                                          // omicsqueryservice will see this as empty and show
                                          // distribution for complete study
                result_instance_id2: GLOBAL.CurrentSubsetIDs[getSubsetFromPanel(selectedDiv)]
            })
        });
}

function showConceptDistributionHistogramForGeneExprFilterForSubsetComplete(result)
{
    geneexprfilterwin.setHeight(370);

    /*if (result == null)
     return Ext.get("geneexprfilterchartsPanel2").update("<div class='x-mask-loading'><div class='conceptDistributionPlaceholder'/></div>");

     Ext.get("geneexprfilterchartsPanel2").update(result.responseText);*/
}