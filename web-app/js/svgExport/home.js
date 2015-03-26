function launchHomePage() {
    jQuery('#home-div').load('getHomePage');
    showHomePage();
    hideResultsPage();
}

function showHomePage() {
    jQuery('#home-div').show();      
}

function hideHomePage() {
    jQuery('#home-div').hide();      
}

function showResultsPage() {
    jQuery('#results-div').show();
}

function hideResultsPage() {
    jQuery('#results-div').hide();
}

function getPieChartData(catid, ddid) {
    rwgAJAXManager.add({
        url:getPieChartDataURL,                                 
        data: {catid: catid, ddid: ddid},
        timeout:60000,
        success: function(response) {
            jQuery('#cat_'+catid+'_div').html('<pre>'+response+'</pre>');
        }
    });
}