import com.recomdata.search.query.AssayAnalysisDataQuery
import com.recomdata.search.query.Query
import org.transmart.SearchFilter
import org.transmart.TrialAnalysisResult

/**
 * $Id: ClinicalTrialAnalysisTEAService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class ClinicalTrialAnalysisTEAService extends AnalysisTEABaseService {

    def trialQueryService

    def getExpType() {
        return "Clinical Trial";
    }

    def createResultObject() {
        return new TrialAnalysisResult();
    }

    def createSubFilterCriteria(SearchFilter filter, Query query) {
        return trialQueryService.createTrialFilterCriteria(filter.trialFilter, query);
    }

    /**
     * find distinct trial analyses with current filters
     */
    def createAnalysisIDSelectQuery(SearchFilter filter) {
        if (filter == null || filter.globalFilter.isTextOnly()) {
            return " SELECT -1 FROM org.transmart.biomart.BioAssayAnalysisData baad WHERE 1 = 1 "
        }
        def gfilter = filter.globalFilter

        def query = new AssayAnalysisDataQuery(mainTableAlias: "baad", setDistinct: true);
        query.addTable("org.transmart.biomart.BioAssayAnalysisData baad ");
        query.addTable("org.transmart.biomart.ClinicalTrial ct ");
        query.addCondition("baad.experiment.id = ct.id ")

        query.createGlobalFilterCriteria(gfilter);
        createSubFilterCriteria(filter, query);

        query.addSelect("baad.analysis.id")

        return query.generateSQL()
    }

}
