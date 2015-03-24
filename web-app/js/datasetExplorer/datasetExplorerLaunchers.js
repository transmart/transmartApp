//This function is used to redirect the user to the sample explorer page with search results loaded for the selected cohort.
function launchSampleBrowseWithCohort() {
    //This function gets called from the DSE when we've already generated the cohort. We need to generate the list of samples and continue on to the sample explorer page.
    jQuery.ajax({
        async: false,
        url: pageInfo.basePath+'/sampleExplorer/generateSampleCohort?result_instance_id='+GLOBAL.CurrentSubsetIDs[1],
        success: function() {window.location=pageInfo.basePath+'/sampleExplorer/showCohortSamples?result_instance_id='+GLOBAL.CurrentSubsetIDs[1];},
        failure: function() {alert("Failed to generate Patient Sample set. Please contact an administrator.");}
    });
}