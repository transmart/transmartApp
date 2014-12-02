function prepareQueryPanels() {

    var model = null
    var table = jQuery("#queryTable tr:last-of-type td")
    window.queryPanelNumber = 0

    for (var i = 0; i < table.size(); i++) {
        jQuery(table[i]).html(jQuery("#panelModel").clone().removeAttr('id').each(function () {
            jQuery(this).html(this.innerHTML.replace(/\$n/g, window.queryPanelNumber++))
        }).data('subset', i))
    }

    jQuery( ".panelRadio" ).buttonset();
}