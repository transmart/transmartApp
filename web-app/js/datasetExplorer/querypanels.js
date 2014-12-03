function prepareQueryPanels() {

    var table = jQuery("#queryTable tr:last-of-type td")
    window.queryPanelNumber = 0

    for (var i = 0; i < table.size(); i++) {
        // We need to inflate the subset to use legacy functions
        appendQueryPanelInto(i + 1)
    }
}

function appendQueryPanelInto(subset) {

    var clone = jQuery("#panelModel").clone().removeAttr('id').each(function () {
        jQuery(this).html(this.innerHTML.replace(/\$n/g, window.queryPanelNumber++)).find("input[id^='panelInclude']").attr("checked", "checked")
    }).attr('subset', subset)

    // Subsets are inflated by 1
    jQuery(jQuery("#queryTable tr:last-of-type td")[subset - 1]).append(clone)
    setupQueryPanelClone(clone)
    removeUselessPanels()
}

// This will hook our object to ExtJS
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

    dtrg.notifyDrop = function (source, e, data) {

        if (source.tree.id == "previousQueriesTree") {
            getPreviousQueryFromID(data.node.attributes.id);
            return true;
            
        } else {

            var x=e.xy[0];
            var y=e.xy[1];
            var concept = null;

            //Modifiers that are dropped need to be handled differently from regular concepts. Modifiers are identified by the applied_path field.
            if(data.node.attributes.applied_path != null && data.node.attributes.applied_path != "@")
            {
                concept = createPanelItemNew(Ext.get("hiddenDragDiv"), convertNodeToConcept(data.node));
                selectConcept(concept);
                STATE.Dragging = true;
                STATE.Target = this.el;
                prepareDroppedModifier(data.node, this.el);
            }

            if (data.node.attributes.oktousevalues != "Y") {
                concept = createPanelItemNew(this.el, convertNodeToConcept(data.node));
            } else {
                concept = createPanelItemNew(Ext.get("hiddenDragDiv"), convertNodeToConcept(data.node));
            }

            if (data.node.attributes.oktousevalues == "Y") {
                STATE.Dragging = true;
                STATE.Target = this.el;
                showSetValueDialog();
            }

            appendQueryPanelInto(clone.attr('subset'))
            return true;
        }
    }
}

function clearQueryPanel(clone)
{
    clone.find("div[id^='panelBoxList']").html('')
    invalidateSubset(clone.attr('subset'))
    removeUselessPanels()
}

function removeUselessPanels()
{
    jQuery("#queryTable tr:last-of-type td").each(function() {
        jQuery("div[id^='panelBoxList']:not(:last)", this).each(function() {
            if (jQuery(this).html().trim() == '')
                jQuery(this).closest(".panelModel").remove()
        })
    })

}