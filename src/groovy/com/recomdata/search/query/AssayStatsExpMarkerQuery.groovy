package com.recomdata.search.query

import org.transmart.GlobalFilter


/**
 * @author $Author: mmcduffie $
 * $Id $
 * $Revision: 9178 $
 *
 */
public class AssayStatsExpMarkerQuery extends AssayAnalysisDataQuery {

    /**
     * default criteria builder for biomarkers
     */

    def buildGlobalFilterBioMarkerCriteria(GlobalFilter gfilter,
                                           boolean expandBioMarkers) {
        def biomarkerFilters = gfilter.getBioMarkerFilters()

        if (!biomarkerFilters.isEmpty()) {
            def markerAlias = mainTableAlias + ".marker"
            if (expandBioMarkers) {
                addCondition(createExpandBioMarkerCondition(markerAlias, gfilter));
                //	addCondition(markerAlias+".id IN ("+createExpandBioMarkerSubQuery(biomarkerFilters.getKeywordDataIdString())+") ")

            } else {
                addCondition(markerAlias + ".id IN (" + biomarkerFilters.getKeywordDataIdString() + ") ")
            }
        }
    }


}
