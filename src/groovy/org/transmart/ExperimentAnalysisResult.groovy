package org.transmart


import com.recomdata.tea.TEABaseResult

/**
 * @author $Author: mmcduffie $
 * $Id: ExperimentAnalysisResult.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @version $Reversion$
 *
 */
public class ExperimentAnalysisResult extends TEABaseResult {

    def experiment
    Long expCount

    // current page rendering in session
    def pagedAnalysisList
}
