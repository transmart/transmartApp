package com.recomdata.search.query

import org.transmart.GlobalFilter

/**
 * @author $Author: mmcduffie $
 * $Id: LiteratureDataQuery.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * $Version$
 *
 */
public class LiteratureDataQuery extends Query {

    /**
     *  criteria builder for experiment,override default
     */

    def buildGlobalFilterExperimentCriteria(GlobalFilter gfilter) {
        if (!gfilter.getTrialFilters().isEmpty()) {
            addCondition("1 = 0")
        }
    }

    def buildGlobalFilterStudyCriteria(GlobalFilter gfilter) {
        if (!gfilter.getStudyFilters().isEmpty()) {
            addCondition("1 = 0")
        }
    }

}
