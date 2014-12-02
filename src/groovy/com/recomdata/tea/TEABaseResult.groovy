package com.recomdata.tea

/**
 * @author jspencer
 * base class for result classes for trials and experiments
 */
public class TEABaseResult {
    Long analysisCount = 0
    Long inSignificantAnalCount = 0

    // contains all analyses
    List analysisResultList = []

    // subset of above (insignificant TEA analyses)
    List insigAnalResultList = []

    //flag indicates if results should be groupd by experiment
    boolean groupByExp = false;

    // count of biomarkers included in the search
    Long bioMarkerCt = 0

    /**
     * set list of insignificat TEA analyses
     */
    def populateInsignificantTEAAnalysisList() {
        analysisResultList.each { if (!it.bSignificantTEA) insigAnalResultList.add(it) }
        Collections.sort(insigAnalResultList)
        inSignificantAnalCount = insigAnalResultList.size()
    }

}
