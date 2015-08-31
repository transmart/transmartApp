/**
 * Created by dverbeec on 09/06/2015.
 */

var omics_filter_info;                  // Global variable to hold information on the omics filter
var omicsFilterValues;                  // List of values for the current selector, used to calculate subject count
var omicsSliderLowHandleRatio = 0;      // Ratio where low handle bar is
var omicsSliderHighHandleRatio = 1;     // Ratio where high handle bar is (used when changing projection)
var omicsSliderSteps = 500;             // Amount of steps between low and high values on the slider

/**
 * Function to be called when a high dimensional concept is dropped. This will look up the high dimensional
 * data-type, and display the appropriate dialog.
 * @param node    The dropped high dimensional concept node
 * @param filter  If true, a filter dialog will be shown (for cohort selection); if false, a selection dialog
 *                will be shown (when dropped in summary statistics or grid view)
 */
function highDimensionalConceptDropped(node, filter) {
    Ext.Ajax.request({
        url: pageInfo.basePath + "/omicsFilter/filterInfo",
        method: 'GET',
        timeout: '60000',
        params: Ext.urlEncode({
            concept_key: node.id,
            filter: filter
        }),
        success: omicsFilterInfoReceived,
        failure: function (result, request) {
            alert("Could not retrieve platform for " + node.id + ": " + result.statusText);
        }
    });
}

function omicsFilterInfoReceived(result, request) {
    omics_filter_info = JSON.parse(result.responseText);
    omics_filter_info.filter = (omics_filter_info.filter == "true");
    Ext.Ajax.request({
        url: pageInfo.basePath + "/omicsFilter/filterDialog",
        method: 'GET',
        timeout: '60000',
        params: Ext.urlEncode({
            gpl_id: omics_filter_info.platform.id,
            filter: omics_filter_info.filter
        }),
        success: omicsFilterWindowReceived,
        failure: function (result, request) {
            alert("Could not retrieve filter info for " + node.id + ": " + result.statusText);
        }
    });
}

function omicsFilterWindowReceived(result, request) {
    var tabpanel = jQuery("#resultsTabPanel");
    var top = tabpanel.offset().top + tabpanel.height() / 2 - setvaluewin.height / 1.5;
    var left = tabpanel.offset().left + tabpanel.width() / 2 - setvaluewin.width / 2;

    omicsfilterwin.setPosition(left, top);
    omicsfilterwin.show(viewport);
    omicsfilterwin.setHeight(140); //set height back to old closed

    jQuery("#omics-filter-main").html(result.responseText);

    omicsfilterwin.setTitle(omics_filter_info.platform.markerType);
    if (omics_filter_info.filter_type == "numeric") {
        addOmicsFilterAutocomplete();
        if (omics_filter_info.filter) {
            addOmicsRangeSlider();
            addOmicsFilterMinMaxInputHandlers();
        }
        jQuery("[id^=omics-slider-row]").css({'display': 'none'});
        jQuery("#omics-filter-selector").focus();
    }
}

function applyOmicsFilterDialog(validation) {
    if (omics_filter_info.filter) {
        // filter
        var params = getOmicsFilterParams();
        if (validation && params.selector == "") {
            alert("You must choose a gene.");
            return;
        }

        // make sure that there is a value set
        if (validation && (!jQuery.isNumeric(jQuery("#omics-amount-min").val()) || !jQuery.isNumeric(jQuery("#omics-amount-max").val()))) {
            alert('You must specify a numeric value.');
        } else {
            if (validation)
                omicsFilterDialogComplete(params);

            if (STATE.Dragging) {
                jQuery('#' + selectedConcept.id).remove();
                removeUselessPanels();
            }
            omicsFilterValues = [];
            omicsSliderLowHandleRatio = 0;
            omicsSliderHighHandleRatio = 1;

            document.getElementById("omics-filter-main").removeChild(document.getElementById("omics-filter-content"));
            omicsfilterwin.hide();
        }
    }
    else {
        // selection
        var selector = jQuery("#omics-filter-selector").val();
        if (validation && selector == "") {
            alert("You must choose a gene.");
            return;
        }

        if (validation) {
            resultsTabPanel.body.mask("Running analysis...", 'x-mask-loading');
            var omics_params = {omics_selector: selector,
                omics_value_type: omics_filter_info.platform.markerType,
                omics_projection_type: jQuery("input[name=omics-filter-projection]:checked").val()};
            Ext.Ajax.request(
                {
                    url : pageInfo.basePath+"/chart/analysis",
                    method : 'POST',
                    timeout: '600000',
                    params :  Ext.urlEncode(
                        {
                            charttype : "analysis",
                            concept_key : omics_filter_info.concept_key,
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
                        alert("A problem arose while trying to retrieve the results");
                        resultsTabPanel.body.unmask();
                    }
                }
            );

            getAnalysisGridData(omics_filter_info.concept_key, omics_params);
        }

        document.getElementById("omics-filter-main").removeChild(document.getElementById("omics-filter-content"));
        omicsfilterwin.hide();
    }
}

function addOmicsFilterAutocomplete() {
    var searchbox = jQuery("#omics-filter-selector");
    searchbox.css({"max-height": "300px", "overflow-y": "auto", "overflow-x": "hidden", "padding-right": "20px"});
    searchbox.autocomplete({
        position:{my:"left top",at:"left bottom",collision:"none"},
        appendTo:"#omicsFilterWindow",
        source: omics_filter_info.auto_complete_source,
        minLength:1,
        select: function(event, ui) {
            jQuery("#omics-filter-selector").val(ui.item.label);
            if (omics_filter_info.filter) {
                showConceptDistributionHistogramForOmicsFilterComplete(null);
                showConceptDistributionHistogramForOmicsFilter(getOmicsFilterParams());
            }
        },
        focus: function(event, ui) {
            jQuery("#omics-filter-selector").val(ui.item.label);
        },
        close: function(event, ui) {
            if (ui.item) {
                jQuery("#omics-filter-selector").val(ui.item.label);
            }
        }
    }).data("uiAutocomplete")._renderItem = function( ul, item ) {
        var resulta = '<a><span class="category-gene"><b>' + item.label + '</b>';
        if (item.synonyms != null) {
            resulta += (item.synonyms + '</a>');
        }
        else {
            resulta += '</a>';
        }

        return jQuery('<li></li>')
            .data("item.autocomplete", item )
            .append(resulta)
            .appendTo(ul);
    };
}

function addOmicsRangeSlider() {
    var slider = jQuery( "#omics-range" );
    slider.css({width: "130px", margin: "10px"});
    slider.slider({
        range: true,
        min: -2500,
        max: 2500,
        values: [ -2500, 2500 ],
        slide: function( event, ui ) {
            omicsSliderUpdated(ui);
        }
    });
}

function addOmicsFilterMinMaxInputHandlers() {
    var minbox = jQuery("#omics-amount-min");
    var maxbox = jQuery("#rnaseq-rcnt-amount-max");
    minbox.blur(function(event) {
        var value = minbox.val();
        if (!jQuery.isNumeric(value)) {
            minbox.val(omicsFilterValues[0]);
        }
        else if (value < omicsFilterValues[0]) {
            minbox.val(omicsFilterValues[0]);
        }
        else if (value > maxbox.val()) {
            minbox.val(maxbox.val());
        }
        jQuery("#omics-range").slider('values',0,minbox.val());
        jQuery( "#omics-filter-subjectcount").html(omicsFilterValues.filter(function (el, idx, array) {return el >= minbox.val();})
            .filter(function(el, idx, array) {return el <= maxbox.val();})
            .length)
    });

    maxbox.blur(function(event) {
        var value = maxbox.val();
        if (!jQuery.isNumeric(value)) {
            maxbox.val(omicsFilterValues[omicsFilterValues.length - 1]);
        }
        else if (value > omicsFilterValues[omicsFilterValues.length - 1]) {
            maxbox.val(omicsFilterValues[omicsFilterValues.length - 1]);
        }
        else if (value < minbox.val()) {
            maxbox.val(minbox.val());
        }
        jQuery("#omics-range").slider('values',1,maxbox.val());
        jQuery( "#omics-filter-subjectcount").html(omicsFilterValues.filter(function (el, idx, array) {return el >= minbox.val();})
            .filter(function(el, idx, array) {return el <= maxbox.val();})
            .length)
    });

    // let the enter key blur the text fields, thus updating the slider and subject counts

    minbox.keyup(function(e) {
        if (e.which == 13) {
            jQuery(this).blur();
        }
    });

    maxbox.keyup(function(e) {
        if (e.which == 13) {
            jQuery(this).blur();
        }
    });
}

function omicsSliderUpdated(ui) {
    var low, high;
    var slider = jQuery("#omics-range");
    if (ui != null) {
        // called from the slide event, therefore use the ui object to get the value the handle was slid to
        low = ui.values[0];
        high = ui.values[1];
    }
    else {
        // not called from the slide event, get the current value of the handle
        low = slider.slider('values', 0);
        high = slider.slider('values', 1);
    }
    var min = slider.slider('option', 'min');
    var max = slider.slider('option', 'max');
    omicsSliderLowHandleRatio = (low - min) / (max - min);
    omicsSliderHighHandleRatio = (high - min) / (max - min);
    jQuery("#omics-amount-min").val(low.toFixed(3));
    jQuery("#omics-amount-max").val(high.toFixed(3));
    jQuery("#omics-filter-subjectcount").html(omicsFilterValues.filter(function (el, idx, array) {return el >= low;})
        .filter(function(el, idx, array) {return el <= high;})
        .length)
}

function omicsProjectionChanged() {
    if (jQuery("#omics-filter-selector").val() != "" && omics_filter_info.filter) {
        showConceptDistributionHistogramForOmicsFilterComplete(null);
        showConceptDistributionHistogramForOmicsFilter(getOmicsFilterParams());
    }
}

function getOmicsFilterParams() {
    var slider = jQuery("#omics-range");
    return {
        platform: omics_filter_info.platform.id,
        selector: jQuery("#omics-filter-selector").val(),
        value: slider.slider('values',0) + ":" + slider.slider('values',1),
        operator: "BETWEEN",
        projection_type: jQuery("input[name=omics-filter-projection]:checked").val(),
        type: omics_filter_info.platform.markerType
    };
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

    var valuetext=getOmicsFilterValueText(params);
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
        case "Gene Expression":
        case "RNASEQ_RCNT":
        case "PROTEOMICS":
        case "MIRNA_QPCR":
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
        case "CHROMOSOMAL":
            break;
        case "VCF":
            break;
    }
    return "<em>" + result + "</em>";
}

function showConceptDistributionHistogramForOmicsFilter(filter_params)
{
    var concept_key = selectedConcept.getAttribute('conceptid');

    Ext.Ajax.request(
        {
            url: pageInfo.basePath+"/chart/conceptDistributionWithValues",
            method: 'POST',
            success: function(result, request){showConceptDistributionHistogramForOmicsFilterComplete(result);},
            failure: function(result, request){omicsValuesFailed(result);},
            timeout: '300000',
            params: Ext.urlEncode({
                concept_key: concept_key,
                omics_selector: filter_params.selector,
                omics_platform: filter_params.platform,
                omics_value_type: filter_params.type,
                omics_projection_type: filter_params.projection_type})
        });
}

function showConceptDistributionHistogramForOmicsFilterComplete(result)
{
    omicsfilterwin.setHeight(370);
    var slider_rows = jQuery("[id^=omics-slider-row]");
    var hist_div = jQuery("[id^=omics-filter-histogram]");

    if (result == null) {
        slider_rows.css({'display': 'none'});
        return hist_div.html("<div class='x-mask-loading'><div class='conceptDistributionPlaceholder'/></div>");
    }
    var data = JSON.parse(result.responseText);
    hist_div.html(data['commons']['conceptHisto']);
    slider_rows.css({'display': 'table-row'});
    omicsValuesObtained(data['1']['conceptData']);
}

function omicsValuesObtained(values) {
    omicsFilterValues = values;
    if (omicsFilterValues.length > 0) {
        omicsFilterValues.sort(function (a, b) {return a - b}); // sort numerically rather than string-based
        var slider = jQuery("#omics-range");
        // make the range slightly bigger to properly include extrema
        slider.slider('option',{'min': omicsFilterValues[0],
                                'max': omicsFilterValues[omicsFilterValues.length - 1],
                                'step': (omicsFilterValues[omicsFilterValues.length - 1] - omicsFilterValues[0]) / omicsSliderSteps});
        slider.slider('values', 0, omicsFilterValues[0] + omicsSliderLowHandleRatio * (omicsFilterValues[omicsFilterValues.length - 1] - omicsFilterValues[0]));
        slider.slider('values', 1, omicsFilterValues[0] + omicsSliderHighHandleRatio * (omicsFilterValues[omicsFilterValues.length - 1] - omicsFilterValues[0]));
        omicsSliderUpdated(null);
    }
    else {
        jQuery("[id^=omics-slider-row]").css({'display': 'none'});
        jQuery( "#omics-amount-min" ).val(0);
        jQuery( "#omics-amount-max" ).val(0);
        document.getElementById("omics-filter-histogram").innerHTML = "No data is available for the given gene.";
        omicsfilterwin.setHeight(140);
    }
}

function omicsValuesFailed(result) {
    jQuery("[id^=omics-slider-row]").css({'display': 'none'});
    jQuery( "#omics-amount-min" ).val(0);
    jQuery( "#omics-amount-max" ).val(0);
    document.getElementById("omics-filter-histogram").innerHTML = "An error occured while retrieving the histogram values: " + result.responseText;
}

