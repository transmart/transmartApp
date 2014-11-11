package org.transmart

import com.recomdata.tea.TEABaseResult

/**
 * $Id: TrialAnalysisResult.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class TrialAnalysisResult extends TEABaseResult {
    def trial
    Long expCount

    def getProtocol() {
        def file = null
        for (cr in trial.files) {
            if (cr.type == "Protocol") {
                file = cr
            }
        }
        return file
    }

    def getFiles() {
        def files = []
        for (cr in trial.files) {
            if (cr.type != "Protocol") {
                files.add(cr)
            }
        }
        return files
    }

    def hasResult() {
        return analysisCount > 0;
    }

}
