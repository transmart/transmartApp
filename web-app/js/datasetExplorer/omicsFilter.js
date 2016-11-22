/**
 * Author: Denny Verbeeck (dverbeec@its.jnj.com)
 */

var omics_filter_info;                  // Global variable to hold information on the highdimension filter
var omicsFilterValues;                  // List of values for the current selector, used to calculate subject count
var omicsSliderLowHandleRatio = 0;      // Ratio where low handle bar is
var omicsSliderHighHandleRatio = 1;     // Ratio where high handle bar is (used when changing projection)
var omicsSliderSteps = 500;             // Amount of steps between low and high values on the slider
var omicsAutoCompleteList = [];         // List of results returned by the autocomplete search (for input validation)
var omicsFilterRepopulateWindow;        // If this is set before omicsFilterWindowReceived() is called, the filter window
                                        // will be repopulated with the values here. This is a map that should contain
                                        // the following keys: [conceptid, omicsproperty, omicsselector, omicsoperator,
                                        // omicsvalue, omicsprojection, omicsvaluetype]

/**
 * Function to be called when a high dimensional concept is dropped. This will look up the high dimensional
 * data-type, and display the appropriate dialog.
 * @param node    The dropped high dimensional concept node
 * @param filter  If true, a filter dialog will be shown (for cohort selection); if false, a selection dialog
 *                will be shown (when dropped in summary statistics or grid view)
 */
function highDimensionalConceptDropped(node, filter) {
    Ext.Ajax.request({
        url: pageInfo.basePath + "/highDimensionFilter/filterInfo",
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
        url: pageInfo.basePath + "/highDimensionFilter/filterDialog",
        method: 'GET',
        timeout: '60000',
        params: Ext.urlEncode({
            gpl_id: omics_filter_info.platform.id,
            filter: omics_filter_info.filter,
            concept_key: omics_filter_info.concept_key
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
    omicsfilterwin.setHeight(158); //set height back to old closed

    jQuery("#highdimension-filter-main").html(result.responseText);

    omicsfilterwin.setTitle(omics_filter_info.platform.markerType);

    if (omics_filter_info.filter_type == "SINGLE_NUMERIC" || omics_filter_info.filter_type == "ACGH") {
        addOmicsFilterAutocomplete();
        if (omics_filter_info.filter) {
            addOmicsRangeSlider();
            addOmicsBinsSlider();
            addOmicsFilterMinMaxInputHandlers();
        }
        jQuery("[id^=highdimension-slider-row]").css({'display': 'none'});
        jQuery("#highdimension-filter-selector").focus();
    }
    else if (omics_filter_info.filter_type == "VCF") {

    }

    repopulateFilterWindow(); // this checks if the user did an 'edit filter', if so it repopulates the values in the window
}

function repopulateFilterWindow() {
    if (omicsFilterRepopulateWindow == null) return;

    jQuery("#highdimension-search-property").val(omicsFilterRepopulateWindow.omicsproperty.nodeValue);
    jQuery("#highdimension-filter-selector").val(omicsFilterRepopulateWindow.omicsselector.nodeValue);
    jQuery("#highdimension-filter-projection").val(omicsFilterRepopulateWindow.omicsprojection.nodeValue);

    showConceptDistributionHistogramForOmicsFilterComplete(null);
    showConceptDistributionHistogramForOmicsFilter(getOmicsFilterParams());
}

function repopulateOmicsFilterRange() {
    if (omicsFilterRepopulateWindow == null) return;

    if (omics_filter_info.filter_type == "SINGLE_NUMERIC" || omics_filter_info.filter_type == "ACGH") {
        var minbox = jQuery("#highdimension-amount-min");
        var maxbox = jQuery("#highdimension-amount-max");
        var values = omicsFilterRepopulateWindow.omicsvalue.nodeValue.split(":");
        minbox.val(values[0]);
        minbox.blur();
        maxbox.val(values[1]);
        maxbox.blur();
    }
}

function applyOmicsFilterDialog(validation) {
    if (omics_filter_info.filter_type == "SINGLE_NUMERIC" || omics_filter_info.filter_type == "ACGH") {
        applySingleNumericOmicsFilter(validation);
    }
    else if (omics_filter_info.filter_type == "VCF") {
        applyVCFOmicsFilter(validation);
    }
}

function addOmicsFilterAutocomplete() {
    var searchbox = jQuery("#highdimension-filter-selector");
    searchbox.autocomplete({
        position:{my:"left top",at:"left bottom",collision:"none"},
        appendTo:"#omicsFilterWindow",
        source: function(request, response) {
            jQuery.ajax({
                url: omics_filter_info.auto_complete_source,
                dataType: "json",
                data: {

                    term : request.term == '' ? '%' : request.term,
                    concept_key : omics_filter_info.concept_key,
                    search_property : jQuery("#highdimension-search-property").find("option:selected").val()
                },
                success: function(data) {
                    response(data);
                }
            });
        },
        minLength: 0,
        delay: 500,
        select: function(event, ui) {
            jQuery("#highdimension-filter-selector").val(ui.item.label);
            if (omics_filter_info.filter) {
                showConceptDistributionHistogramForOmicsFilterComplete(null);
                showConceptDistributionHistogramForOmicsFilter(getOmicsFilterParams());
            }
        },
        focus: function(event, ui) {
            jQuery("#highdimension-filter-selector").val(ui.item.label);
        },
        close: function(event, ui) {
            if (ui.item) {
                jQuery("#highdimension-filter-selector").val(ui.item.label);
            }
        },
        response: function(event, ui) {
            omicsAutoCompleteList = ui.content.map(function(item) {return item.label;});
        }
    }).data("ui-autocomplete")._renderItem = function( ul, item ) {
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
    jQuery(".ui-autocomplete").css({"max-height": "300px", "overflow-y": "auto", "overflow-x": "hidden", "padding-right": "20px"});
    jQuery("#highdimension-search-property").find('input').click(function() {
        searchbox.autocomplete('source', omics_filter_info.auto_complete_source + "&search_property=" + encodeURIComponent(jQuery(this).val()));
    });
}

function addOmicsRangeSlider() {
    var slider = jQuery( "#highdimension-range" );
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

function addOmicsBinsSlider() {
    var slider = jQuery( "#highdimension-bins" );
    slider.css({width: "130px", margin: "10px"});
    slider.slider({
        range: "min",
        min: 5,
        max: 25,
        value: 10,
        slide: function( event, ui ) {
            jQuery("#highdimension-amount-bins").val(ui.value);
        },
        stop: function( event, ui ) {
            showConceptDistributionHistogramForOmicsFilterComplete(null);
            showConceptDistributionHistogramForOmicsFilter(getOmicsFilterParams());
        }
    });
    jQuery("#highdimension-amount-bins").val(slider.slider('value'));
}

function addOmicsFilterMinMaxInputHandlers() {
    var minbox = jQuery("#highdimension-amount-min");
    var maxbox = jQuery("#highdimension-amount-max");
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
        var slider = jQuery("#highdimension-range");
        slider.slider('values',0,minbox.val());
        jQuery("#highdimension-filter-subjectcount").html(omicsFilterValues.filter(function (el, idx, array) {return el >= minbox.val();})
            .filter(function(el, idx, array) {return el <= maxbox.val();})
            .length);
        var min = slider.slider('option', 'min');
        var max = slider.slider('option', 'max');
        omicsSliderLowHandleRatio = (minbox.val() - min) / (max - min);
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
        var slider = jQuery("#highdimension-range");
        slider.slider('values',1,maxbox.val());
        jQuery("#highdimension-filter-subjectcount").html(omicsFilterValues.filter(function (el, idx, array) {return el >= minbox.val();})
            .filter(function(el, idx, array) {return el <= maxbox.val();})
            .length);
        var min = slider.slider('option', 'min');
        var max = slider.slider('option', 'max');
        omicsSliderHighHandleRatio = (maxbox.val() - min) / (max - min);
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
    var slider = jQuery("#highdimension-range");
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
    jQuery("#highdimension-amount-min").val(low.toFixed(3));
    jQuery("#highdimension-amount-max").val(high.toFixed(3));
    jQuery("#highdimension-filter-subjectcount").html(omicsFilterValues.filter(function (el, idx, array) {return el >= parseFloat(low.toFixed(3));})
        .filter(function(el, idx, array) {return el <= parseFloat(high.toFixed(3));})
        .length)
}

function omicsProjectionChanged() {
    if (jQuery("#highdimension-filter-selector").val() != "" && omics_filter_info.filter) {
        showConceptDistributionHistogramForOmicsFilterComplete(null);
        showConceptDistributionHistogramForOmicsFilter(getOmicsFilterParams());
    }
}

function getOmicsFilterParams() {
    if (omics_filter_info.filter_type == "SINGLE_NUMERIC" || omics_filter_info.filter_type == "ACGH") {
        var slider = omics_filter_info.filter ? jQuery("#highdimension-range") : null;
        return {
            platform: omics_filter_info.platform.id,
            property: jQuery("#highdimension-search-property").val(),
            selector: jQuery("#highdimension-filter-selector").val(),
            value: omics_filter_info.filter ? jQuery("#highdimension-amount-min").val() + ":" + jQuery("#highdimension-amount-max").val() : "",
            operator: omics_filter_info.filter ? "BETWEEN" : "",
            projection_type: jQuery("#highdimension-filter-projection").find("option:selected").val(),
            projection_pretty_name: jQuery("#highdimension-filter-projection").find("option:selected").text(),
            type: omics_filter_info.platform.markerType,
            hist_bins: omics_filter_info.filter ? jQuery("#highdimension-amount-bins").val() : ""
        };
    }
    else if (omics_filter_info.filter_type == "VCF") {
        return {
            platform: omics_filter_info.platform.id,
            selector: "DUMMY",
            value: "",
            operator: "BETWEEN",
            projection_type: "COMPLEX",
            type: omics_filter_info.platform.markerType
        }
    }
    else return null;
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
    conceptnode.setAttribute("omicsproperty", params.property);
    conceptnode.setAttribute("omicsselector", params.selector);
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
        case "VCF":
            result = "Dummy VCF filter";
            break;
        default:
            switch (params.operator) {
                case "BETWEEN":
                    var thresholds = params.value.split(":");
                    if (thresholds.length != 2) {
                        result = "";
                    }
                    else {
                        result = thresholds[0] + " <= " + params.projection_pretty_name + " for " + params.selector + " <= " + thresholds[1];
                    }
                    break;
            }
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
                omics_property: filter_params.property,
                omics_selector: filter_params.selector,
                omics_platform: filter_params.platform,
                omics_value_type: filter_params.type,
                omics_projection_type: filter_params.projection_type,
                omics_hist_bins: filter_params.hist_bins})
        });
}

function showConceptDistributionHistogramForOmicsFilterComplete(result)
{
    omicsfilterwin.setHeight(445);
    var slider_rows = jQuery("[id^=highdimension-slider-row]");
    var input_fields = jQuery("[id^=highdimension-amount-m]");
    var sliders = jQuery("[id^=highdimension-slider-row] .ui-slider");
    var projection_select = jQuery("#highdimension-filter-projection");
    var hist_div = jQuery("[id^=highdimension-filter-histogram]");

    if (result == null) {
        //
        input_fields.prop('disabled', true);
        projection_select.prop('disabled', true);
        sliders.slider('option','disabled', true);
        return hist_div.html("<div class='x-mask-loading'><div class='conceptDistributionPlaceholder'/></div>");
    }
    var data = [];
    try {
        // grails JSON converter seems to print NaN and Infinity in JSON output, these are not JSON tokens so
        // we need to quote them
        var resptext = result.responseText.replace(/NaN/g,'"NaN"');
        resptext = resptext.replace(/-Infinity/g, '"-Infinity"');
        resptext = resptext.replace(/ Infinity/g, ' "Infinity"');
        data = JSON.parse(resptext);
    }
    catch (err) {
        hist_div.html('An error occured parsing the server\'s response.');
        projection_select.prop('disabled', false);
        omicsfilterwin.setHeight(175);
        return;
    }
    hist_div.html(data['commons']['conceptHisto']);
    slider_rows.css({'display': 'table-row'});
    input_fields.prop('disabled', false);
    projection_select.prop('disabled', false);
    sliders.slider('option','disabled', false);
    omicsValuesObtained(data['1']['conceptData']);
}

function omicsValuesObtained(values) {
    omicsFilterValues = values;
    if (omicsFilterValues.length > 0) {
        omicsFilterValues.sort(function (a, b) {return a - b}); // sort numerically rather than string-based
        var slider = jQuery("#highdimension-range");
        slider.slider('option',{'min': omicsFilterValues[0],
                                'max': omicsFilterValues[omicsFilterValues.length - 1],
                                'step': (omicsFilterValues[omicsFilterValues.length - 1] - omicsFilterValues[0]) / omicsSliderSteps});
        slider.slider('values', 0, omicsFilterValues[0] + omicsSliderLowHandleRatio * (omicsFilterValues[omicsFilterValues.length - 1] - omicsFilterValues[0]));
        slider.slider('values', 1, omicsFilterValues[0] + omicsSliderHighHandleRatio * (omicsFilterValues[omicsFilterValues.length - 1] - omicsFilterValues[0]));
        omicsSliderUpdated(null);
        repopulateOmicsFilterRange();
    }
    else {
        jQuery("[id^=highdimension-slider-row]").css({'display': 'none'});
        jQuery( "#highdimension-amount-min" ).val(0);
        jQuery( "#highdimension-amount-max" ).val(0);
        document.getElementById("highdimension-filter-histogram").innerHTML = "No data is available for the given gene.";
        omicsfilterwin.setHeight(175);
    }

    // this is the last step in populating or repopulating the filter window, so let's clear the repopulate variables
    omicsFilterRepopulateWindow = null;
}

function omicsValuesFailed(result) {
    jQuery("[id^=highdimension-slider-row]").css({'display': 'none'});
    jQuery( "#highdimension-amount-min" ).val(0);
    jQuery( "#highdimension-amount-max" ).val(0);
    document.getElementById("highdimension-filter-histogram").innerHTML = "An error occured while retrieving the histogram values: " + result.responseText;
}

function applySingleNumericOmicsFilter(validation) {
    var params = getOmicsFilterParams();
    if (omics_filter_info.filter) {
        // filter
        if (validation) {
            if (params.selector == "") {
                alert("You must choose a gene.");
                return;
            }

            var in_list = false;
            for (var i = 0; i < omicsAutoCompleteList.length; i++) {
                if (omicsAutoCompleteList[i].toLowerCase() == params.selector.toLocaleLowerCase()) {
                    in_list = true;
                    params.selector = omicsAutoCompleteList[i];  // case insensitive match, so change the user-supplied value to the 'real' value
                    break;
                }
            }

            if (!in_list) {
                alert("Please select a gene from the list.");
                return;
            }
        }

        // make sure that there is a value set
        if (validation && (!jQuery.isNumeric(jQuery("#highdimension-amount-min").val()) || !jQuery.isNumeric(jQuery("#highdimension-amount-max").val()))) {
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

            document.getElementById("highdimension-filter-main").removeChild(document.getElementById("highdimension-filter-content"));
            omicsfilterwin.hide();
        }
    }
    else {
        // selection
        if (validation && params.selector == "") {
            alert("You must choose a gene.");
            return;
        }

        if (validation) {
            resultsTabPanel.body.mask("Running analysis...", 'x-mask-loading');
            var omics_params = {omics_property: params.property,
                omics_selector: params.selector,
                omics_value_type: omics_filter_info.platform.markerType,
                omics_projection_type: jQuery("#highdimension-filter-projection").find("option:selected").val()};
            Ext.Ajax.request(
                {
                    url : pageInfo.basePath+"/chart/analysis",
                    method : 'POST',
                    timeout: '600000',
                    params :  Ext.urlEncode(
                        {
                            charttype : "analysis",
                            concept_key : omics_filter_info.concept_key,
                            omics_property: omics_params.omics_property,
                            omics_selector : omics_params.omics_selector,
                            omics_value_type: omics_params.omics_value_type,
                            omics_projection_type: omics_params.omics_projection_type,
                            omics_platform: omics_filter_info.platform.id,
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

        document.getElementById("highdimension-filter-main").removeChild(document.getElementById("highdimension-filter-content"));
        omicsfilterwin.hide();
    }
}

function applyVCFOmicsFilter(validation) {
    var params = getOmicsFilterParams();
    if (omics_filter_info.filter) {
        if (validation) // also check if required fields are filled correctly
            omicsFilterDialogComplete(params);

        document.getElementById("highdimension-filter-main").removeChild(document.getElementById("highdimension-filter-content"));
        omicsfilterwin.hide();
    }
    else {
        if (validation)
            omicsFilterDialogComplete(params);

        document.getElementById("highdimension-filter-main").removeChild(document.getElementById("highdimension-filter-content"));
        omicsfilterwin.hide();
    }
}
