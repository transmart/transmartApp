
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
 * This will hook our object to ExtJS
 * @param clone : jQuery instance of a clonned panel
 * @returns {void}
 */
function setupQueryPanelClone(clone) {

    clone.find(".panelRadio").buttonset();
    clone.find("button[id^=panelClear]").click(function() {
        clearQueryPanel(clone)
    })

    var exto = Ext.get(clone.find("div[id^='panelBoxList']").attr("id"))
    var dtrg = new Ext.dd.DropTarget(exto,
        {
            ddGroup : 'makeQuery'
        }
    )

    dtrg.notifyEnter = function (source, e, data) {
        jQuery("#" + e.target.id).parent().find(".holder").addClass('overed')
    }

    dtrg.notifyOut = function (source, e, data) {
        jQuery(".panelBoxListPlaceholder .holder").removeClass('overed')
    }

    dtrg.notifyDrop = function (source, e, data) {

        if (source.tree.id == "previousQueriesTree") {
            getPreviousQueryFromID(data.node.attributes.id);
            return true;
        } else {

            // This is legacy ExtJS code
            var _concept = null;

            // Modifiers that are dropped need to be handled differently from regular concepts.
            // Modifiers are identified by the applied_path field.
            if(data.node.attributes.applied_path != null && data.node.attributes.applied_path != "@")
            {
                _concept = createPanelItemNew(Ext.get("hiddenDragDiv"), convertNodeToConcept(data.node));
                selectConcept(_concept);
                STATE.Dragging = true;
                STATE.Target = this.el;
                prepareDroppedModifier(data.node, this.el);
            }

            if (data.node.attributes.oktousevalues != "Y")
                _concept = createPanelItemNew(this.el, convertNodeToConcept(data.node))
            else
                _concept = createPanelItemNew(Ext.get("hiddenDragDiv"), convertNodeToConcept(data.node))

            if (data.node.attributes.oktousevalues == "Y") {
                STATE.Dragging = true;
                STATE.Target = this.el;
                showSetValueDialog();
            }

            // Mask the placeholder and add a new panel
            clone.find(".panelBoxListPlaceholder").hide()
            appendQueryPanelInto(clone.attr('subset'))

            return true;
        }
    }
}

/**
 * Empty all panels from their content
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
    var _name = arguments.length > 1 ? arguments[1] : GLOBAL.Username+"'s Query at "+ new Date().toUTCString();

    _query.attr('xmlns:ns4', 'http://www.i2b2.org/xsd/cell/crc/psm/1.1/')
    _query
        .append(jQuery("<query_name />").html(_name))
        .append(jQuery("<specificity_scale />").html("0"))
        .append(getQueryPanels(subset))

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

    _number = _number.substring(_number.indexOf('_') + 1)
    _panel
        .append(jQuery("<panel_number />").html(_number))
        .append(jQuery("<panel_date_from />").html(null))
        .append(jQuery("<panel_date_to />").html(null))
        .append(jQuery("<invert />").html(_invert))
        .append(jQuery("<total_item_occurrences />").html(_occurrence))
        .append(getQueryPanelItems(panel))

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
    var _constrain = jQuery('<constrain_by_value />')
    var _value = ''

    switch (item.attr('setvaluemode')) {
        case 'numeric' :

            _value = item.attr('setvaluelowvalue')
            if (item.attr('setvalueoperator') == "BETWEEN")
                _value += ' AND ' + item.attr('setvaluehighvalue')

            _constrain
                .append(jQuery("value_operator").html(item.attr('setvalueoperator')))
                .append(jQuery("value_constraint").html(_value))
                .append(jQuery("value_unit_of_measure").html(item.attr('setvalueunits')))
                .append(jQuery("value_type").html("NUMBER"))

            break;
        case 'highlow' :

            _constrain
                .append(jQuery("value_operator").html("EQ"))
                .append(jQuery("value_constraint").html(item.attr('setvaluehighlowselect').substring(0, 1).toUpperCase()))
                .append(jQuery("value_unit_of_measure").html(item.attr('setvalueunits')))
                .append(jQuery("value_type").html("FLAG"))

            break;
    }

    _item
        .append(jQuery("<hlevel />").html(item.attr('conceptlevel')))
        .append(jQuery("<item_name />").html(item.attr('conceptname').legacyI2B2Escape()))
        .append(jQuery("<item_key />").html(item.attr('conceptid').legacyI2B2Escape()))
        .append(jQuery("<tooltip />").html(item.attr('concepttooltip').legacyI2B2Escape()))
        .append(jQuery("<hlevel />").html(item.attr('conceptlevel')))
        .append(jQuery("<class />").html("ENC"))
        .append(_constrain)

    return _item
}

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