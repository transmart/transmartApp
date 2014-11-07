import com.recomdata.search.query.Query
import org.transmart.ExperimentAnalysisResult
import org.transmart.SearchFilter

/**
 * $Id: ExperimentAnalysisTEAService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 * todo -- make a super class for experimentanalysisqueryservice and trialqueryservice
 */


class ExperimentAnalysisTEAService extends AnalysisTEABaseService {

    def experimentAnalysisQueryService

    def getExpType() {
        return "Experiment";
    }

    def createResultObject() {
        return new ExperimentAnalysisResult();
    }


    def createSubFilterCriteria(SearchFilter filter, Query query) {
        experimentAnalysisQueryService.createSubFilterCriteria(filter.expAnalysisFilter, query);
    }


}
