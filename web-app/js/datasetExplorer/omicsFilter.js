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
        switch (platform.marker_type) {
            case 'Gene Expression':
                showGeneExprFilterDialog(platform);
                break;
            case 'RNASEQ_RCNT':
                showRnaseqRcntFilterDialog(platform);
                break;
            default:
                alert("Support for " + platform.marker_type + " is not yet implemented!");
        }
    }
}

function showOmicsSelectionDialog(result, request) {
    if (result == null) {
        alert("Could not retrieve platform: " + result.statusText);
    }
    else {
        var platform = JSON.parse(result.responseText);
        switch (platform.marker_type) {
            case 'Gene Expression':
                showGeneSelectionDialog(platform);
                break;
            case 'RNASEQ_RCNT':
                // showRnaseqRcntSelectionDialog(platform);
                break;
            default:
                alert("Support for " + platform.marker_type + " is not yet implemented!");
        }
    }
}

function applyGeneExprFilterDialog(validation) {

    var params = getGeneExprFilterParams();
    if (validation && params.selector == "") {
        alert("You must choose a gene.");
        return;
    }

    geneExprFilterValues = [];
    geneExprSliderLowHandleRatio = 0;
    geneExprSliderHighHandleRatio = 1;

    // make sure that there is a value set
    if (validation && (!jQuery.isNumeric(jQuery("#expression-amount-min").val()) || !jQuery.isNumeric(jQuery("#expression-amount-max").val()))) {
        alert('You must specify a numeric value.');
    } else {

        if (validation)
            omicsFilterDialogComplete(params);

        if (STATE.Dragging) {
            jQuery('#' + selectedConcept.id).remove()
            removeUselessPanels()
        }

        geneexprfilterwin.hide();
    }
}

function applyRnaseqRcntFilterDialog(validation) {

    var params = getRnaseqRcntFilterParams();
    if (validation && params.selector == "") {
        alert("You must choose a gene.");
        return;
    }

    rnaseqRcntFilterValues = [];
    rnaseqRcntSliderLowHandleRatio = 0;
    rnaseqRcntSliderHighHandleRatio = 1;

    // make sure that there is a value set
    if (validation && (!jQuery.isNumeric(jQuery("#rnaseq-rcnt-amount-min").val()) || !jQuery.isNumeric(jQuery("#rnaseq-rcnt-amount-max").val()))) {
        alert('You must specify a numeric value.');
    } else {

        if (validation)
            omicsFilterDialogComplete(params);

        if (STATE.Dragging) {
            jQuery('#' + selectedConcept.id).remove()
            removeUselessPanels()
        }

        rnaseqrcntfilterwin.hide();
    }
}

function showGeneExprFilterDialog(platform)
{
    jQuery("#gene-searchbox").val("");
    jQuery("#gene-expr-filter-gplid").text(platform.platform);
    jQuery("#gene-searchbox").autocomplete("option", "source", "/transmart/omicsPlatformSearch/searchAutoComplete?gplid=" + encodeURIComponent(platform.platform));
    geneexprfilterwin.setHeight(140); //set height back to old closed
    jQuery("[id^=gene-expression-slider-row]").css({'display': 'none'});

    document.getElementById("gene-expression-filter-histogram").innerHTML = ""
    var top = jQuery("#resultsTabPanel").offset().top + jQuery("#resultsTabPanel").height() / 2 - setvaluewin.height / 1.5;
    var left = jQuery("#resultsTabPanel").offset().left + jQuery("#resultsTabPanel").width() / 2 - setvaluewin.width / 2;

    geneexprfilterwin.setPosition(left, top);
    geneexprfilterwin.show(viewport);
    jQuery("#gene-searchbox").focus();
}

function showRnaseqRcntFilterDialog(platform)
{
    jQuery("#rnaseq-rcnt-searchbox").val("");
    jQuery("#rnaseq-rcnt-filter-gplid").text(platform.platform);
    jQuery("#rnaseq-rcnt-searchbox").autocomplete("option", "source", "/transmart/omicsPlatformSearch/searchAutoComplete?gplid=" + encodeURIComponent(platform.platform));
    rnaseqrcntfilterwin.setHeight(140); //set height back to old closed
    jQuery("[id^=rnaseq-rcnt-slider-row]").css({'display': 'none'});

    document.getElementById("rnaseq-rcnt-filter-histogram").innerHTML = ""
    var top = jQuery("#resultsTabPanel").offset().top + jQuery("#resultsTabPanel").height() / 2 - setvaluewin.height / 1.5;
    var left = jQuery("#resultsTabPanel").offset().left + jQuery("#resultsTabPanel").width() / 2 - setvaluewin.width / 2;

    rnaseqrcntfilterwin.setPosition(left, top);
    rnaseqrcntfilterwin.show(viewport);
    jQuery("#rnaseq-rcnt-searchbox").focus();
}

function omicsFilterDialogComplete(params)
{
    var conceptnode = selectedConcept;
    setOmicsFilterValue(conceptnode, params);
    if(STATE.Dragging==true){
        STATE.Dragging=false;
        moveSelectedConceptFromHoldingToTarget();
    }
}

function setOmicsFilterValue(conceptnode, params)
{
    conceptnode.setAttribute("setvaluemode","omics_value");
    conceptnode.setAttribute("selector", params.selector);
    conceptnode.setAttribute("omicsoperator", params.operator);
    conceptnode.setAttribute("omicsvalue", params.value);
    conceptnode.setAttribute("omicsprojection", params.projection_type);
    conceptnode.setAttribute("omicsvaluetype", params.type);

    var valuetext="";
    valuetext=getOmicsFilterValueText(params);
    conceptnode.setAttribute('conceptsetvaluetext',valuetext);
    var conceptname=conceptnode.getAttribute("conceptname");
    jQuery('#' + conceptnode.id + " .concept-text").html(conceptname + " " + valuetext);
    var subset=getSubsetFromPanel(conceptnode.parentNode);
    invalidateSubset(subset);
}

function getOmicsFilterValueText(params)
{
    var result = "";

    switch (params.type) {
        case "GENE_EXPRESSION":
            switch (params.operator) {
                case "BETWEEN":
                    var thresholds = params.value.split(":");
                    if (thresholds.length != 2) {
                        result = "";
                    }
                    else {
                        result = thresholds[0] + " <= " + params.selector + " <= " + thresholds[1];
                    }
                    break;
            }
            break;
        case "RNASEQ_RCNT":
            switch (params.operator) {
                case "BETWEEN":
                    var thresholds = params.value.split(":");
                    if (thresholds.length != 2) {
                        result = "";
                    }
                    else {
                        result = thresholds[0] + " <= " + params.selector + " <= " + thresholds[1];
                    }
                    break;
            }
            break;
        case "PROTEOMICS":
            break;
        case "CHROMOSOMAL":
            break;
        case "MIRNA_QPCR":
            break;
    }
    return "<em>" + result + "</em>";
}

function showGeneSelectionDialog(platform) {
    jQuery("#gene-selection-searchbox").val("");
    jQuery("#gene-selection-searchbox").autocomplete("option", "source", "/transmart/omicsPlatformSearch/searchAutoComplete?gplid=" + encodeURIComponent(platform.platform));
    jQuery("#gene-selection-concept-key").val(platform.concept_key);

    var top = jQuery("#resultsTabPanel").offset().top + jQuery("#resultsTabPanel").height() / 2 - setvaluewin.height / 1.5;
    var left = jQuery("#resultsTabPanel").offset().left + jQuery("#resultsTabPanel").width() / 2 - setvaluewin.width / 2;

    geneselectionwin.setPosition(left, top);
    geneselectionwin.show(viewport);
}

function showGeneSelectionDialog(platform) {
    jQuery("#gene-selection-searchbox").val("");
    jQuery("#gene-selection-searchbox").autocomplete("option", "source", "/transmart/omicsPlatformSearch/searchAutoComplete?gplid=" + encodeURIComponent(platform.platform));
    jQuery("#gene-selection-concept-key").val(platform.concept_key);

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

function showConceptDistributionHistogramForOmicsFilter(filter_params)
{
    var concept_key = selectedConcept.getAttribute('conceptid');

    Ext.Ajax.request(
        {
            url: pageInfo.basePath+"/chart/conceptDistribution",
            method: 'POST',
            success: function(result, request){showConceptDistributionHistogramForOmicsFilterComplete(result, filter_params);},
            failure: function(result, request){showConceptDistributionHistogramForOmicsFilterComplete(result, filter_params);},
            timeout: '300000',
            params: Ext.urlEncode({
                concept_key: concept_key,
                omics_selector: filter_params.selector,
                omics_platform: filter_params.platform,
                omics_value_type: filter_params.type,
                omics_projection_type: filter_params.projection_type})
        });
}

function showConceptDistributionHistogramForOmicsFilterComplete(result, filter_params)
{
    var concept_key = selectedConcept.getAttribute('conceptid');

    filter_params.window.setHeight(370);

    if (result == null) {
        filter_params.slider_rows.css({'display': 'none'});
        return filter_params.hist_div.html("<div class='x-mask-loading'><div class='conceptDistributionPlaceholder'/></div>");
    }

    Ext.Ajax.request(
        {
            url: pageInfo.basePath + "/chart/conceptDistributionValues",
            method: 'GET',
            success: function(result, request) {
                var values = eval(result.responseText);
                filter_params.valuescallback(values);
            },
            failure: function(result, request) {
                filter_params.valuescallbackfailed(result);
            },
            timeout: '30000',
            params: Ext.urlEncode({
                concept_key: concept_key,
                omics_selector: filter_params.selector,
                omics_platform: filter_params.platform,
                omics_value_type: filter_params.type,
                omics_projection_type: filter_params.projection_type})
        }
    );

    filter_params.hist_div.html(result.responseText);
}
/*
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
                                          // this way, full and subset distributions will be overlayed
                result_instance_id2: GLOBAL.CurrentSubsetIDs[getSubsetFromPanel(selectedDiv)]
            })
        });
}

function showConceptDistributionHistogramForGeneExprFilterForSubsetComplete(result)
{
    geneexprfilterwin.setHeight(370);

    //if (result == null)
    // return Ext.get("geneexprfilterchartsPanel2").update("<div class='x-mask-loading'><div class='conceptDistributionPlaceholder'/></div>");

    // Ext.get("geneexprfilterchartsPanel2").update(result.responseText);
}*/
