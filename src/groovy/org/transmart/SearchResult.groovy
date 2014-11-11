package org.transmart

/**
 * $Id: SearchResult.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class SearchResult {

    def countmap = [:]

    // trial tab
    int analysisCount = 0
    int trialCount = 0

    // mRNA tab
    int mRNAAnalysisCount = 0
    int experimentCount = 0
    int allAnalysiCount = 0

    int documentCount = 0
    int litJubOncAltCount = 0
    int litJubOncInhCount = 0
    int litJubOncIntCount = 0
    int litJubAsthmaAltCount = 0
    int litJubAsthmaInhCount = 0
    int litJubAsthmaIntCount = 0
    int litJubAsthmaPECount = 0
    int resultCount = 0
    int profileCount = 0
    def summary
    def result
    String resultType

    def totalCount = {
        return experimentCount + literatureCount() + trialCount + documentCount + profileCount
    }

    def litJubOncCount = {
        return litJubOncAltCount + litJubOncInhCount + litJubOncIntCount
    }

    def litJubAsthmaCount = {
        return litJubAsthmaAltCount + litJubAsthmaInhCount + litJubAsthmaIntCount + litJubAsthmaPECount
    }

    def literatureCount = {
        return litJubOncCount() + litJubAsthmaCount()
    }

}
