
/**
 * Instantiate the first panels on the page
 * @returns {void}
 */
function prepareQueryPanels() {

    var table = jQuery("#queryTable tr:last-of-type td")
    window.queryPanelNumber = 0

    for (var i = 0; i < table.size(); i++) {
        // We need to inflate the subset to use legacy functions
        appendQueryPanelInto(i + 1)
    }

    jQuery("#queryPanel").click(function () {
        resetSelected()
    })
}

/**
 * Adding supplementary panel on the page.
 * No attention taken to efficiency as this is no consuming function
 * @param subset
 * @returns {void}
 */
function appendQueryPanelInto(subset) {

    var clone = jQuery("#panelModel").clone().removeAttr('id').each(function () {
        jQuery(this).html(this.innerHTML.replace(/\$n/g, window.queryPanelNumber++)).find("input[id^='panelInclude']").attr("checked", "checked")
    }).attr('subset', subset)

    // Subsets are inflated by 1
    jQuery(jQuery("#queryTable tr:last-of-type td")[subset - 1]).append(clone)
    setupQueryPanelClone(clone)
    removeUselessPanels()
}

/**
 * Adding supplementary item within a destination (subset or panelBoxList).
 * @param destination : subset {numeric} or selector {string} or DOM reference {object} to add the item to
 * @param concept : flatten concept object {object}
 * @param invert : whether the set membership should be updated {boolean}
 * @returns {void}
 */
function appendItemFromConceptInto(destination, concept, invert) {

    var _panel = null

    if (typeof destination == "number")
        _panel = jQuery(jQuery("#queryTable tr:last-of-type td")[destination - 1]).find('div[id^=panelBoxList]').last()
    else
        _panel = jQuery(destination)

    var _invert = typeof invert !== 'undefined' ? invert : false;
    if (_invert)
        _panel.parent().find('label[for^=panelExclude]').click()

    _panel.parent().find('.panelBoxListPlaceholder').hide()
    _panel.append(getPanelItemFromConcept(concept))

    appendQueryPanelInto(_panel.closest('.panelModel').attr('subset'))
}

/**
 * This will hook our object to ExtJS
 * @param clone : jQuery instance of a clonned panel
 * @returns {void}
 */
function setupQueryPanelClone(clone) {

    clone.find(".panelRadio").buttonset()
    clone.find(".panelDate").button().click(function() {
        if (jQuery(this).attr('checked') == 'checked')
            jQuery(this).closest(".panelBox").addClass("withDates")
        else
            jQuery(this).closest(".panelBox").removeClass("withDates")

        adjustPanelSize()
    })
    clone.find("input[id^='panelBoxDate']").datepicker({ dateFormat: 'yy-mm-dd' })
    clone.find("button[id^='panelClear']").click(function() {
        clearQueryPanel(clone)
    })

    clone.find(".panelBoxList").click(function () {
        resetSelected()
    })

    var exto = Ext.get(clone.find("div[id^='panelBoxList']").attr("id"))
    var dtrg = new Ext.dd.DropTarget(exto, { ddGroup : 'makeQuery' })

    dtrg.notifyEnter = function (source, e, data) {
        jQuery("#" + e.target.id).parent().find(".holder").addClass('overed')
    }

    dtrg.notifyOut = function (source, e, data) {
        jQuery(".panelBoxListPlaceholder .holder").removeClass('overed')
    }

    dtrg.notifyDrop = function (source, e, data) {

        // This is legacy ExtJS code
        var _concept = null;

        // Modifiers that are dropped need to be handled differently from regular concepts.
        // Modifiers are identified by the applied_path field.
        if(data.node.attributes.applied_path != null && data.node.attributes.applied_path != "@")
        {
            prepareDroppedModifier(data.node, this.el);
        }
        else
        {
            selectConcept(createPanelItemNew(this.el, convertNodeToConcept(data.node)))
            if (data.node.attributes.oktousevalues == "Y") {
                STATE.Dragging = true;
                STATE.Target = this.el;
                showSetValueDialog();
            }
        }
        // Mask the placeholder and add a new panel
        clone.find(".panelBoxListPlaceholder").hide()
        appendQueryPanelInto(clone.attr('subset'))

        return true;
    }
}

/**
 * Empty all panels from their content
 * @returns {void}
 */
function clearQueryPanels()
{
    jQuery(".panelModel").each(function (){
        clearQueryPanel(jQuery(this))
    })
}

/**
 * Empty a panels from its content
 * @returns {void}
 */
function clearQueryPanel(clone)
{
    clone.find("div[id^='panelBoxList']").html('')
    invalidateSubset(clone.attr('subset'))
    removeUselessPanels()
}

/**
 * Remove all un-necessary panels on the view
 * @returns {void}
 */
function removeUselessPanels()
{
    jQuery("#queryTable tr:last-of-type td").each(function() {
        jQuery("div[id^='panelBoxList']:not(:last)", this).each(function() {
            if (jQuery(this).html().trim() == '')
                jQuery(this).closest(".panelModel").remove()
        })
    })

    adjustPanelSize()
}

/**
 * Adjust the height of panels to fit items
 * @returns {void}
 */
function adjustPanelSize()
{
    jQuery(".panelBox").each(function() {

        var _itemNumber = jQuery(".panelBoxListItem", jQuery(this)).size()
        var _totalHeight = (_itemNumber + 0.5) * jQuery(".panelBoxListItem:first").outerHeight()

        _totalHeight += parseInt(jQuery(".panelBoxList", jQuery(this)).css('padding-top'))
        jQuery(this).css('height', _totalHeight)
    })

}

/**
 * Get I2B2 XML representation and re-inject it into panels
 * @param subsets
 */
function refillQueryPanels(subsets) {

    // Legacy ExtJS call (The way this is handle can cause problems
    queryPanel.el.mask('Rebuilding query ...', 'x-mask-loading');

    clearQueryPanels()

    // Looping through subsets
    jQuery.each(subsets, function (subset, query) {

        jQuery(query).find("panel").each(function () {

            //This is dangerous, but time efficient
            var _panel = jQuery(this)
            var _panelDOM = jQuery(".panelModel", jQuery("#queryTable tr:last-of-type td")[parseInt(subset) - 1]).last()

            _panelDOM.find(".panelBoxListPlaceholder").hide()

            var _inversion = _panel.find("invert").text() == "1"

            if (_inversion)
                _panelDOM.find("input[id^=panelExclude]").attr("checked", "checked")
            _panelDOM.find(".panelRadio").buttonset("refresh")

            _panel.find("item").each(function () {
                _panelDOM.find(".panelBoxList").append(getPanelItemFromConcept(getConceptFromQueryItem(this)))
            })

            appendQueryPanelInto(subset)
        })

        queryPanel.el.unmask()
        adjustPanelSize()
    })
}

/**
 * Create a concept object from and XML query item
 * This is full of legacy crap
 * @param item
 * @returns {object}
 */
function getConceptFromQueryItem(item) {

    var _item = jQuery(item)
    var _concept = {}

    _concept["conceptid"] = _item.find("item_key").text()
    _concept["conceptname"] = createShortNameFromPath(_concept["conceptid"])
    _concept["concepttooltip"] = _concept["conceptid"].substr(1, _concept["conceptid"].length)
    _concept["conceptlevel"] = _item.find("hlevel").text()

    // Default values for other attributes
    _concept["concepttablename"] = ""
    _concept["conceptdimcode"] = ""
    _concept["conceptcomment"] = ""
    _concept["normalunits"] = "ratio"
    _concept["setvaluemode"] = ""
    _concept["setvalueoperator"] = ""
    _concept["setvaluelowvalue"] = ""
    _concept["setvaluehighvalue"] = ""
    _concept["setvaluehighlowselect"] = ""
    _concept["setvalueunits"] = ""
    _concept["oktousevalues"] = ""
    _concept["setnodetype"] = ""
    _concept["visualattributes"] = ""
    _concept["applied_path"] = "@"
    _concept["modifiedNodePath"] = ""
    _concept["modifiedNodeId"] = ""
    _concept["modifiedNodeLevel"] = ""

    _item.find("constrain_by_modifier").each(function () {
        _concept["ismodifier"] = true
        _concept["conceptname"] = _item.find("modifier_name").text()
        _concept["applied_path"] = _item.find("applied_path").text()
        _concept["conceptid"] = _item.find("modifier_key").text()
    })

    _item.find("constrain_by_value").each(function () {
        switch (_item.find("value_type").text().toLowerCase()) {
            case 'number' :
                _concept["setvaluemode"] = "numeric"
                _concept["setvalueunits"] = _item.find("value_type").text()

                var _constrain = _item.find("value_constraint").text().split(" AND ")

                if (_constrain.length > 1) {
                    _concept["setvalueoperator"] = "BETWEEN"
                    _concept["setvaluelowvalue"] = _constrain[0]
                    _concept["setvaluehighvalue"] = _constrain[1]
                } else {
                    _concept["setvalueoperator"] = _item.find("value_operator").text()
                    _concept["setvaluelowvalue"] = _constrain[0]
                }

                break;
            case 'flag' :
                _concept["setvalueoperator"] = _item.find("value_operator").text()
                _concept["setvaluemode"] = "highlow"
                _concept["setvalueunits"] = _item.find("value_type").text()
                _concept["setvaluehighlowselect"] = _item.find("value_constraint").text()

                break;
            case 'text' :

                var _operator = _item.find("value_operator")

                // TODO This should be replaced by a regular expression
                _operator = _operator.substr(_operator.indexOf('['), _operator.indexOf(']' - 1))
                _concept["setvalueoperator"] = _operator

                if (_item.find("modifier_key").text() == "IN")
                    _concept["setvaluemode"] = "list"
                else
                    _concept["setvaluemode"] = "text"

                break;
        }
    })

    jQuery.each(_concept, function(key, value) {
        if (_concept[key] == undefined) _concept[key] = ""
    })

    return _concept
}

/**
 * Create a DOM panel item from a concept object
 * @param concept
 * @returns {object}
 */
function getPanelItemFromConcept(concept) {

    var _item = jQuery("<div />")
    var _valueText = getSetValueText(
        concept["setvaluemode"],
        concept["setvalueoperator"],
        concept["setvaluehighlowselect"],
        concept["setvaluehighvalue"],
        concept["setvaluelowvalue"],
        concept["setvalueunits"]).trim()

    // We try to infer the type when possible to match the icon
    jQuery.get(pageInfo.basePath + "/concepts/getResource", { concept_key: concept["conceptid"] }, function() {}, 'json')
        .success(function (data) {
            jQuery("span:first", _item).addClass(getClassForNodeResource(data))
        })

    _item
        .append(jQuery("<span />").addClass("x-tree-node-icon"))
        .append(jQuery("<span />").addClass("concept-text").html(concept.conceptname + _valueText))

    jQuery.each(concept, function(key, value) {
        _item.attr(key, value)
    })

    _item.addClass("panelBoxListItem")
    _item.addClass("x-tree-node-collapsed")

    // Some legacy hooks here
    Ext.get(_item[0]).addListener('click',conceptClick);
    Ext.get(_item[0]).addListener('contextmenu',conceptRightClick);

    return _item
}

/**
 * Computes the right visual class for the given node
 * @param node
 * @returns {string}
 */
function getClassForNodeResource(node) {

    var _visualAttributes = node.visualAttributes
    var _OKToUseValue = false
    var _iconClass = ""

    if (node.metadata)
        _OKToUseValue = node.metadata.okToUseValues

    if (_OKToUseValue)
        _iconClass = "valueicon";

    jQuery.each(_visualAttributes, function (index, value) {
        if (!_OKToUseValue && (value.toUpperCase() == "LEAF" || value.toUpperCase() == "MULTIPLE"))
            _iconClass = "alphaicon";
        if (value.toUpperCase() == "HIGH_DIMENSIONAL")
            _iconClass = "hleaficon";
        if (value.toUpperCase() == "EDITABLE")
            _iconClass = "eleaficon";
        if (value.toUpperCase() == "PROGRAM")
            _iconClass = "programicon";
        if (value.toUpperCase() == "STUDY")
            _iconClass = "studyicon";
        if(value.toUpperCase() == "MODIFIER_LEAF")
            _iconClass = "modifiericon";
    })

    return _iconClass
}

/**
 * Get subset query that represent user's cohort selections
 * @param subset
 * @returns {object}
 */
function getSubsetQuery (subset) {

    var _query = ''
    jQuery(".panelModel[subset='" + subset + "']").each(function () {
        if (jQuery(this).find(".panelBoxList").html().trim() != '')
            _query += getQueryPanel(jQuery(this)).html()
    })

    return _query;
}

/**
 * Get I2B2 XML representation of the whole cohort
 * @param panel
 * @returns {object}
 */
function getQuery(subset) {

    var _query = jQuery("<ns4:query_definition />")

    _query.attr('xmlns:ns4', 'http://www.i2b2.org/xsd/cell/crc/psm/1.1/')
    _query
        .append(jQuery("<specificity_scale />").html("0"))
        .append(getQueryPanels(subset))

    if (jQuery('#queryEncounter_' + subset).attr('checked') == 'checked')
        _query.append(jQuery('<query_timing />').html('SAMEVISIT'))
    else
        _query.append(jQuery('<query_timing />').html('ANY'))

    return _query
}

/**
 * Get I2B2 XML representation of all panels
 * @param subset
 * @returns {object}
 */
function getQueryPanels(subset) {

    var _panels = jQuery()

    jQuery(".panelModel[subset='" + subset + "']").each(function () {

        if (jQuery(this).find(".panelBoxList").html().trim() != '')
            _panels = _panels.add(getQueryPanel(jQuery(this)))

    })

    return _panels
}


/**
 * Get I2B2 XML representation of a panel
 * @param panel
 * @returns {object}
 */
function getQueryPanel(panel) {

    var _panel = jQuery("<panel />")
    var _number = panel.find("div[id^='panelBoxList']").attr('id')
    var _invert = panel.find("input[name^=panelRadio]:checked").val()
    var _occurrence = 1

    _number = _number ? _number.substring(_number.indexOf('_') + 1) : new Date().getTime()
    _invert = _invert != undefined ? _invert : 0
    _panel
        .append(jQuery("<panel_number />").html(_number))
        .append(jQuery("<invert />").html(_invert))
        .append(jQuery("<total_item_occurrences />").html(_occurrence))
        .append(getQueryPanelItems(panel))

    if (panel.find(".panelDate").attr('checked') == 'checked') {

        var _from = panel.find("input[id^='panelBoxDateFrom']").val().trim()
        var _to = panel.find("input[id^='panelBoxDateTo']").val().trim()
        var _offset = new Date().toString().match(/([-\+][0-9]+)\s/)[1]

        _offset = 'T00:00:00.000' + _offset.slice(0, 3) + ':' +_offset.slice(3)

        if (_from != '')
            _panel.append(jQuery("<panel_date_from />").html(_from + _offset))
        if (_to != '')
            _panel.append(jQuery("<panel_date_to />").html(_to + _offset))
    }

    if (jQuery('#queryEncounter_' + panel.attr('subset')).attr('checked') == 'checked')
        _panel.append(jQuery('<panel_timing />').html('SAMEVISIT'))
    else
        _panel.append(jQuery('<panel_timing />').html('ANY'))

    return _panel
}

/**
 * Get I2B2 XML representation of all items in the panel
 * @param panel
 * @returns {object}
 */
function getQueryPanelItems(panel) {


    var _items = jQuery()

    jQuery(".panelBoxListItem", panel).each(function () {

        _items = _items.add(getQueryPanelItem(jQuery(this)))

    })

    return _items
}

/**
 * Get I2B2 XML representation of an item
 * @param item
 * @returns {object}
 */
function getQueryPanelItem(item) {

    var _item = jQuery("<item />")
    var _constrainValue = jQuery('<constrain_by_value />')
    var _value = ''

    switch (item.attr('setvaluemode').toLowerCase()) {
        case 'numeric' :

            _value = item.attr('setvaluelowvalue')
            if (item.attr('setvalueoperator') == "BETWEEN")
                _value += ' AND ' + item.attr('setvaluehighvalue')

            _constrainValue
                .append(jQuery("<value_operator />").html(item.attr('setvalueoperator')))
                .append(jQuery("<value_constraint />").html(_value))
                .append(jQuery("<value_unit_of_measure />").html(item.attr('setvalueunits')))
                .append(jQuery("<value_type />").html("NUMBER"))

            break;
        case 'highlow' :

            _constrainValue
                .append(jQuery("<value_operator />").html("EQ"))
                .append(jQuery("<value_constraint />").html(item.attr('setvaluehighlowselect').substring(0, 1).toUpperCase()))
                .append(jQuery("<value_unit_of_measure />").html(item.attr('setvalueunits')))
                .append(jQuery("<value_type />").html("FLAG"))

            break;
        case 'text' :

            _constrainValue
                .append(jQuery("<value_operator />").html('LIKE[' + item.attr('setvalueoperator') + ']'))
                .append(jQuery("<value_constraint />").html('<![CDATA[' + item.attr('***') + ']]>'))
                .append(jQuery("<value_type />").html("TEXT"))

            break;
        case 'list' :

            _constrainValue
                .append(jQuery("<value_operator />").html("IN"))
                .append(jQuery("<value_constraint />").html(item.attr('***')))
                .append(jQuery("<value_type />").html("TEXT"))

            break;
    }

    _item
        .append(jQuery("<item_name />").html(item.attr('conceptname').legacyI2B2Escape()))
        .append(jQuery("<item_key />").html(item.attr('conceptid').legacyI2B2Escape()))
        .append(jQuery("<tooltip />").html(item.attr('concepttooltip').legacyI2B2Escape()))
        .append(jQuery("<hlevel />").html(item.attr('conceptlevel')))
        .append(jQuery("<class />").html("ENC"))

    if (item.attr('ismodifier')) {

        var _constrainModifier = jQuery('<constrain_by_modifier />')

        _constrainModifier
            .append(jQuery("<modifier_name />").html(item.attr('conceptname')))
            .append(jQuery("<applied_path />").html(item.attr('applied_path')))
            .append(jQuery("<modifier_key />").html(item.attr('conceptid')))

        if (_constrainValue.html().length)
            _constrainModifier.append(_constrainValue)

        _item.append(_constrainModifier)

    } else if (_constrainValue.html().length)
        _item.append(_constrainValue)

    return _item
}
/**
 * This returns a summary of the query as a string
 * @param subset
 * @returns {string}
 */
function getSubsetQuerySummary(subset) {

    var _summary = "";
    var _panels = getQueryPanels(subset)

    _panels.each(function() {

        var _e = jQuery(this)
        var _item = ""

        if (_summary.trim() != '')
            _summary += " AND"

        if (_e.find("invert").html() == '0')
            _summary += " INCLUDE"
        else
            _summary += " DO NOT INCLUDE"

        _e.find("item").each(function() {

            if (_item.trim() != '' && _e.find("invert").html() == '0')
                _item += " OR"
            else if (_item.trim() != '')
                _item += " NOR"

            _item += " " + _e.find("tooltip").html()

            if (_e.find("constrain_by_value").size()) {
                _item += " CONSTRAINED"
            }

            if (_e.find("constrain_by_modifier").size()) {
                _item += " MODIFIED"
            }
        })

        _summary += " (" + _item + " )"
    })

    return _summary.trim()
}

/**
 * This returns a summary of the query as a string
 * This is a legacy function hook for RModule
 * TODO: This should be replaced following evolution of RModule
 * @param subset
 * @returns {string}
 */
function getQuerySummary(subset) { return getSubsetQuerySummary(subset) }

/**
 * This exclusively encode squared brackets as legacy code describe
 * @returns {string}
 */
String.prototype.legacyI2B2Escape = function() {
    return String(this).replace(/[<>]/g, function (s) {

        // Bad practice, but I wanna avoid scope polution
        var __entityMap = {
            "<": "&lt;",
            ">": "&gt;"
        };
        return __entityMap[s];
    });
}