package com.recomdata.search.query

import org.transmart.GlobalFilter


/**
 * @author $Author: mmcduffie $
 * $Id: AssayAnalysisDataTeaQuery.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * $Version$
 *
 */
public class AssayAnalysisDataTeaQuery extends Query {

    /**
     * create biomarker table alias
     */
    def String getBioMarkerTable() {
        return mainTableAlias + ".featureGroup.markers ";
    }
    /**
     *  criteria builder for disease,override default
     */
    def buildGlobalFilterDiseaseCriteria(GlobalFilter gfilter) {
        if (!gfilter.getDiseaseFilters().isEmpty()) {
            def dAlias = mainTableAlias + "_dis"
            def dtable = mainTableAlias + ".experiment.diseases " + dAlias;
            addTable("JOIN " + dtable)
            addCondition(dAlias + ".id IN (" + gfilter.getDiseaseFilters().getKeywordDataIdString() + ") ")
        }

    }

    /**
     *  criteria builder for compound,override default
     */

    def buildGlobalFilterCompoundCriteria(GlobalFilter gfilter) {
        if (!gfilter.getCompoundFilters().isEmpty()) {
            def dAlias = mainTableAlias + "_cpd"
            def dtable = mainTableAlias + ".experiment.compounds " + dAlias;
            addTable("JOIN " + dtable)
            addCondition(dAlias + ".id IN (" + gfilter.getCompoundFilters().getKeywordDataIdString() + ") ")
        }

    }

    /**
     *  criteria builder for experiment,override default
     */

    def buildGlobalFilterExperimentCriteria(GlobalFilter gfilter) {
        if (!gfilter.getTrialFilters().isEmpty()) {
            addCondition(mainTableAlias + ".experiment.id IN (" + gfilter.getTrialFilters().getKeywordDataIdString() + ")")
        }
        if (!gfilter.getStudyFilters().isEmpty()) {
            addCondition(mainTableAlias + ".experiment.id IN (" + gfilter.getStudyFilters().getKeywordDataIdString() + ")")
        }
    }


}
