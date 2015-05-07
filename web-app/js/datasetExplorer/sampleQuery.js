function generatePatientSampleCohort(callback) {
    //Verify subsets are entered.
    if(areAllSubsetsEmpty()) return false;

    //Determine subset count.
    determineNumberOfSubsets();

    //Iterate through all subsets calling the ones that need to be run.
    for (i = 1; i <= GLOBAL.NumOfSubsets; i = i + 1) {
        if( ! isSubsetEmpty(i) && GLOBAL.CurrentSubsetIDs[i] == null) {
            runQuery(i, callback);
        }
    }
}

//Verify the user actually selected subsets.
function areAllSubsetsEmpty() {
    if(isSubsetEmpty(1) && isSubsetEmpty(2)) {
        Ext.Msg.alert('Subsets are empty', 'All subsets are empty. Please select subsets.');
        return true;
    }
}

//Determine the number of subsets we need and store it in a global js variable.
function determineNumberOfSubsets() {
    var subsetstorun = 0;

    for (i = 1; i <= GLOBAL.NumOfSubsets; i = i + 1) {
        if( ! isSubsetEmpty(i) && GLOBAL.CurrentSubsetIDs[i] == null) {
            subsetstorun ++ ;
        }
    }

    STATE.QueryRequestCounter = subsetstorun;
}