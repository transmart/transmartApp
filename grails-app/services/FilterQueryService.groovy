import com.recomdata.search.query.AssayAnalysisDataQuery
import org.transmart.SearchFilter
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.biomart.ClinicalTrial
import org.transmart.biomart.Experiment
import org.transmart.searchapp.SearchKeyword

/**
 * $Id: FilterQueryService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */

class FilterQueryService {

    def trialDiseaseFilter() {
        return experimentDiseaseFilter("Clinical Trial")
    }

    def trialDiseaseFilter(SearchFilter filter) {
        return findExperimentDiseaseFilter(filter, "Clinical Trial")
    }

    def trialCompoundFilter(SearchFilter filter) {
        return findExperimentCompoundFilter(filter, "Clinical Trial")
    }

    def findExperimentDiseaseFilter(SearchFilter filter, experimentType) {
        def gfilter = filter.globalFilter

        def query = new AssayAnalysisDataQuery(mainTableAlias: "baad", setDistinct: true)
        def alias = query.mainTableAlias + "_dis"
        query.addTable("org.transmart.biomart.BioAssayAnalysisData baad");
        query.addTable("JOIN " + query.mainTableAlias + ".experiment.diseases " + alias)
        query.addSelect(alias)
        query.addOrderBy(alias + ".preferredName");
        query.addCondition(query.mainTableAlias + ".experiment.type='" + experimentType + "'")
        query.createGlobalFilterCriteria(gfilter, true);

        // createSubFilterCriteria(filter.expAnalysisFilter, query);
        //	println(query.generateSQL());


        return BioAssayAnalysisData.executeQuery(query.generateSQL());
    }

//	def experimentDiseaseFilter(String experimentType){
//		def query = "SELECT distinct sk FROM org.transmart.searchapp.SearchKeyword sk, org.transmart.biomart.Experiment exp JOIN exp.diseases ds "+
//		" WHERE sk.bioDataId = ds.id AND exp.type=? ORDER BY sk.keyword";
//		return org.transmart.searchapp.SearchKeyword.executeQuery(query, experimentType);
//	}

//	def experimentDiseaseFilterNew(String experimentType){
//		def query = "SELECT distinct sk FROM org.transmart.searchapp.SearchKeyword sk, org.transmart.biomart.Experiment exp JOIN exp.diseases ds "+
//		" WHERE sk.bioDataId = ds.id AND exp.type=? ORDER BY sk.keyword";
//		return org.transmart.searchapp.SearchKeyword.executeQuery(query, experimentType);
//}

    def experimentCompoundFilter(String experimentType) {
        def query = "SELECT distinct sk FROM org.transmart.searchapp.SearchKeyword sk, org.transmart.biomart.Experiment exp JOIN exp.compounds cpd " +
                " WHERE sk.bioDataId = cpd.id AND exp.type=? ORDER BY sk.keyword";
        return SearchKeyword.executeQuery(query, experimentType);
    }

    def findExperimentCompoundFilter(SearchFilter filter, experimentType) {
        def gfilter = filter.globalFilter

        def query = new AssayAnalysisDataQuery(mainTableAlias: "baad", setDistinct: true)
        def alias = query.mainTableAlias + "_cpd"
        query.addTable("org.transmart.biomart.BioAssayAnalysisData baad");
        query.addTable("JOIN " + query.mainTableAlias + ".experiment.compounds " + alias)
        query.addSelect(alias)
        query.addOrderBy(alias + ".genericName");
        query.addCondition(query.mainTableAlias + ".experiment.type='" + experimentType + "'")
        query.createGlobalFilterCriteria(gfilter, true);

        // createSubFilterCriteria(filter.expAnalysisFilter, query);
        //	println(query.generateSQL());

        return BioAssayAnalysisData.executeQuery(query.generateSQL());
    }

    def studyTypeFilter() {
        def query = "SELECT distinct exp.studyType from org.transmart.biomart.ClinicalTrial exp WHERE exp.studyType IS NOT NULL ORDER BY exp.studyType"
        return ClinicalTrial.executeQuery(query)
    }

    def trialPhaseFilter() {
        def query = "SELECT distinct exp.studyPhase FROM org.transmart.biomart.ClinicalTrial exp WHERE exp.studyPhase IS NOT NULL ORDER BY exp.studyPhase"
        return ClinicalTrial.executeQuery(query)
    }

    def studyDesignFilter(String experimentType) {
        return Experiment.executeQuery("SELECT DISTINCT exp.design FROM org.transmart.biomart.Experiment exp WHERE exp.type=? AND exp.design IS NOT NULL ORDER BY exp.design", experimentType);
    }

}
