import com.recomdata.search.query.AssayDataStatsQuery
import com.recomdata.search.query.AssayStatsExpMarkerQuery
import com.recomdata.search.query.Query
import grails.util.Holders
import org.transmart.SearchFilter
import org.transmart.biomart.BioAssayDataStatistics
import org.transmart.biomart.BioAssayStatsExpMarker
import org.transmart.biomart.BioMarker
import org.transmart.biomart.Disease

/**
 * $Id: ExpressionProfileQueryService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class ExpressionProfileQueryService {

    /**
     * count experiment with criteria
     */
    def countExperiment(SearchFilter filter) {

        //println("profile:"+filter.globalFilter.isTextOnly());

        if (filter == null || filter.globalFilter.isEmpty() || filter.globalFilter.isTextOnly()) {
            return 0
        }

        return BioAssayStatsExpMarker.executeQuery(createCountQuery(filter))[0]
    }

    def createCountQuery(SearchFilter filter) {
        if (filter == null || filter.globalFilter.isEmpty() || filter.globalFilter.isTextOnly()) {
            return " WHERE 1=0"
        }
        def gfilter = filter.globalFilter

        def query = new AssayStatsExpMarkerQuery(mainTableAlias: "asemq")
        query.addTable("org.transmart.biomart.BioAssayStatsExpMarker asemq")
        query.addCondition("asemq.experiment.type='Experiment'")

        query.createGlobalFilterCriteria(gfilter);
        createSubFilterCriteriaForMarker(filter.exprProfileFilter, query);
        query.addSelect("COUNT(DISTINCT asemq.experiment.id)")
        return query.generateSQL();
    }

    def queryStatisticsData(SearchFilter filter) {

        if (filter == null || filter.globalFilter.isTextOnly()) {
            return []
        }
        def query = new AssayDataStatsQuery(mainTableAlias: "bads", setDistinct: true)
        query.addTable("org.transmart.biomart.BioAssayDataStatistics bads")
        //query.addCondition("bads.experiment.type='Experiment'")
        def gfilter = filter.globalFilter
        // expand biomarkers
        query.createGlobalFilterCriteria(gfilter, true);
        createSubFilterCriteria(filter.exprProfileFilter, query)
        query.addSelect("bads")
        query.addOrderBy("bads.experiment")
        def q = query.generateSQL()
        //println(q)
        return BioAssayDataStatistics.executeQuery(q)
    }

    def queryStatisticsDataExpField(SearchFilter filter) {
        def query = new AssayDataStatsQuery(mainTableAlias: "bads", setDistinct: true)
        query.addTable("org.transmart.biomart.BioAssayDataStatistics bads")
        //query.addCondition("bads.experiment.type='Experiment'")
        def gfilter = filter.globalFilter
        // expand biomarkers
        query.createGlobalFilterCriteria(gfilter, true);
        createSubFilterCriteria(filter.exprProfileFilter, query)
        query.addSelect("bads")
        query.addSelect("bads.experiment.accession")
        //	query.addSelect("bads.dataset.name")
        query.addOrderBy("bads.experiment.accession")
        def q = query.generateSQL()
        //println(q)
        return BioAssayDataStatistics.executeQuery(q, [max: 500])
    }

    def listBioMarkers(SearchFilter filter) {
        def query = new AssayStatsExpMarkerQuery(mainTableAlias: "asemq", setDistinct: true)
        query.addTable("org.transmart.biomart.BioAssayStatsExpMarker asemq")
        query.addCondition("asemq.experiment.type='Experiment'")
        //	query.addTable("JOIN FETCH asemq.marker asemq_bm")
        def gfilter = filter.globalFilter
        // expand biomarkers
        query.createGlobalFilterCriteria(gfilter, true);
        createSubFilterCriteriaForMarker(filter.exprProfileFilter, query)
        query.addSelect("asemq.marker")
        query.addOrderBy("asemq.marker.name")
        return BioAssayStatsExpMarker.executeQuery(query.generateSQL(), [max: Holders.config.com.recomdata.search.gene.max])
    }

    def listDiseases(SearchFilter filter) {
        def query = new AssayDataStatsQuery(mainTableAlias: "bads", setDistinct: true)
        query.addTable("org.transmart.biomart.BioAssayDataStatistics bads")
        query.addCondition("bads.experiment.type='Experiment'")
        query.addTable("JOIN bads.experiment.diseases bads_dis")
        def gfilter = filter.globalFilter
        query.createGlobalFilterCriteria(gfilter);
        createSubFilterCriteria(filter.exprProfileFilter, query)
        query.addSelect("bads_dis")
        query.addOrderBy("bads_dis.preferredName")
        //println(">> disease query: " + query.generateSQL())
        return BioAssayDataStatistics.executeQuery(query.generateSQL())
    }

    /**
     * get probesets filtered by marker (i.e. gene) and disease
     */
    def getProbesetsByBioMarker(BioMarker marker, Disease disease) {
        def query = "SELECT distinct bads.featureGroupName " +
                "FROM org.transmart.biomart.BioAssayDataStatistics bads JOIN bads.featureGroup.markers bads_bm JOIN bads.experiment.diseases bads_dis " +
                "WHERE bads_bm.id =:bmid and bads_dis.id=:disid";
        return BioAssayDataStatistics.executeQuery(query, [bmid: marker.id, disid: disease.id]);
    }

    def createSubFilterCriteria(exprfilter, Query query) {
        // disease
        if (exprfilter.filterDisease()) {
            def alias = query.mainTableAlias + "_dis"
            query.addTable("JOIN " + query.mainTableAlias + ".experiment.diseases " + alias)
            query.addCondition(alias + ".id = " + exprfilter.bioDiseaseId)
        }

        // biomarker
        if (exprfilter.filterBioMarker()) {
            def alias = query.mainTableAlias + "_bm"
            query.addTable("JOIN " + query.mainTableAlias + ".featureGroup.markers " + alias)
            query.addCondition(alias + ".id = " + exprfilter.bioMarkerId)
        }

        // probeset
        if (exprfilter.filterProbeSet()) {
            query.addCondition(query.mainTableAlias + ".featureGroupName='" + exprfilter.probeSet + "'")
        }
    }

    def createSubFilterCriteriaForMarker(exprfilter, Query query) {
        // disease
        if (exprfilter.filterDisease()) {
            def alias = query.mainTableAlias + "_dis"
            query.addTable("JOIN " + query.mainTableAlias + ".experiment.diseases " + alias)
            query.addCondition(alias + ".id = " + exprfilter.bioDiseaseId)
        }

        // biomarker
        if (exprfilter.filterBioMarker()) {
            def alias = query.mainTableAlias + ".marker"
            //	query.addTable("JOIN "+query.mainTableAlias+".markers "+alias)
            query.addCondition(alias + ".id = " + exprfilter.bioMarkerId)
        }

        // probeset
        //if(exprfilter.filterProbeSet()){
        //	query.addCondition(query.mainTableAlias+".featureGroupName='" +exprfilter.probeSet+"'")
        //}
    }

}
