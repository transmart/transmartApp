import groovy.time.TimeCategory
import org.transmart.SearchFilter
import org.transmart.SearchResult

/**
 * $Id: SearchService.groovy 10098 2011-10-19 18:39:32Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 10098 $
 *
 */
public class SearchService {
    def literatureQueryService
    def experimentAnalysisQueryService
    def trialQueryService
    def documentService
    def expressionProfileQueryService
    def clinicalTrialAnalysisTEAService

    def doResultCount(SearchResult sResult, SearchFilter searchFilter) {

        // Closure to measure the time performance
        def benchmark = { closure ->
            def start = new Date()
            closure.call()
            return TimeCategory.minus(new Date(), start)
        }

        def duration = benchmark { sResult.litJubOncAltCount = literatureQueryService.litJubOncAltCount(searchFilter) }
        log.info("Literature Oncology Alteration Count Duration: ${duration}")
        duration = benchmark { sResult.litJubOncInhCount = literatureQueryService.litJubOncInhCount(searchFilter) }
        log.info("Literature Oncology Inhibitor Count Duration: ${duration}")
        duration = benchmark { sResult.litJubOncIntCount = literatureQueryService.litJubOncIntCount(searchFilter) }
        log.info("Literature Oncology Interaction Count Duration: ${duration}")
        duration = benchmark {
            sResult.litJubAsthmaAltCount = literatureQueryService.litJubAsthmaAltCount(searchFilter)
        }
        log.info("Literature Asthma Alteration Count Duration: ${duration}")
        duration = benchmark {
            sResult.litJubAsthmaInhCount = literatureQueryService.litJubAsthmaInhCount(searchFilter)
        }
        log.info("Literature Asthma Inhibitor Count Duration: ${duration}")
        duration = benchmark {
            sResult.litJubAsthmaIntCount = literatureQueryService.litJubAsthmaIntCount(searchFilter)
        }
        log.info("Literature Asthma Interaction Count Duration: ${duration}")
        duration = benchmark { sResult.litJubAsthmaPECount = literatureQueryService.litJubAsthmaPECount(searchFilter) }
        log.info("Literature Asthma Protein Effect Count Duration: ${duration}")
        duration = benchmark {
            sResult.experimentCount = experimentAnalysisQueryService.countExperimentMV(searchFilter)
        }
        log.info("Expression Analysis Count Duration: ${duration}")
        duration = benchmark { sResult.trialCount = trialQueryService.countAnalysis(searchFilter) }
        log.info("Trial Count Duration: ${duration}")
        duration = benchmark {
            sResult.analysisCount = clinicalTrialAnalysisTEAService.queryExpAnalysisCount(searchFilter)
        }
        log.info("Analysis count and duration: ${sResult.analysisCount} and ${duration}")
        duration = benchmark {
            sResult.mRNAAnalysisCount = experimentAnalysisQueryService.countTEAAnalysis(searchFilter)
        }
        log.info("mRNA Analysis count and duration: ${sResult.mRNAAnalysisCount} and ${duration}")
        duration = benchmark { sResult.allAnalysiCount = experimentAnalysisQueryService.countAnalysisMV(searchFilter) }
        log.info("All Analysis count and duration: ${sResult.allAnalysiCount} and ${duration}")
        duration = benchmark { sResult.documentCount = documentService.documentCount(searchFilter) }
        log.info("Document Count Duration: ${duration}")
        duration = benchmark { sResult.profileCount = expressionProfileQueryService.countExperiment(searchFilter) }
        log.info("Profile Count Duration: ${duration}")
    }

    def createPagingParamMap(params, defaultmax, defaultoffset) {
        def paramMap = [:]
        def max = params.max
        def offset = params.offset
        if (max == null && defaultmax != null)
            max = defaultmax
        if (offset == null && defaultoffset != null)
            offset = defaultoffset
        // dynamic typing sucks here..
        if (max != null)
            paramMap["max"] = Integer.valueOf(String.valueOf(max))
        if (offset != null)
            paramMap["offset"] = Integer.valueOf(String.valueOf(offset))
        return paramMap;
    }
}
