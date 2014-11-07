package org.transmart

/**
 * $Id: SearchFilter.groovy 10125 2011-10-20 19:12:48Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 10125 $
 * */
import org.apache.log4j.Logger

class SearchFilter {

    static Logger log = Logger.getLogger(SearchFilter.class)

    def searchKeywordService = new SearchKeywordService()

    String searchText
    String datasource
    GeneExprFilter geFilter = new GeneExprFilter()
    LiteratureFilter litFilter = new LiteratureFilter()
    TrialFilter trialFilter = new TrialFilter()
    DocumentFilter documentFilter = new DocumentFilter()
    GlobalFilter globalFilter = new GlobalFilter()
    HeatmapFilter heatmapFilter = new HeatmapFilter()
    ExperimentAnalysisFilter expAnalysisFilter = new ExperimentAnalysisFilter()
    ExpressionProfileFilter exprProfileFilter = new ExpressionProfileFilter()
    String summaryWithLinks
    String pictorTerms

    def acttab = {

        if ("trial".equals(datasource))
            return 0
        else if ("experiment".equals(datasource))
            return 1
        else if ("profile".equals(datasource))
            return 2
        else if (datasource?.startsWith("literature"))
            return 3
        else if ("document".equals(datasource))
            return 4
        else
            return 5
    }

    def acttabname = {

        if ("trial".equals(datasource))
            return "trial"
        else if ("experiment".equals(datasource))
            return "pretrial"
        else if ("profile".equals(datasource))
            return "profile"
        else if (datasource?.startsWith("literature"))
            return "jubilant"
        else if ("document".equals(datasource))
            return "doc"
        else
            return datasource;
    }

    def createPictorTerms = {

        def geneFilters = globalFilter.getGeneFilters();
        // Get all pathway ids from globalFilter
        def pathwayIds = globalFilter.formatIdList(globalFilter.getAllListFilters(), ",")
        // If there are pathways, then get all genes in pathways and add them to the geneFilters (hash set)
        if (pathwayIds.size() > 0) {
            geneFilters.addAll(searchKeywordService.expandAllListToGenes(pathwayIds))
        }

        // Format the gene filter keywords into comma separated strings
        if (geneFilters?.size() > 0) {
            pictorTerms = globalFilter.formatKeywordList(geneFilters, ",", "", 1900)
        } else {
            pictorTerms = null
        }

    }

    def marshal() {
        def s = new StringBuilder("<SearchFilter.searchText:").append(searchText).append(">");
        // todo -- add filter stuff in
        return s.toString();
    }

    /** This method is used for the ResNet and the GeneGo tabs */
    def getExternalTerms() {
        StringBuilder s = new StringBuilder()

        def geneFilters = globalFilter.getGeneFilters();
        def pathwayIds = globalFilter.formatIdList(globalFilter.getAllListFilters(), ",")
        if (pathwayIds.size() > 0) {
            geneFilters.addAll(searchKeywordService.expandAllListToGenes(pathwayIds))
        }
        if (geneFilters?.size() > 0) {
            s.append(globalFilter.formatKeywordList(geneFilters, " OR ", "", 1900))
        }

        if (!globalFilter.getTextFilters().isEmpty()) {
            if (s.length() > 0) {
                s.append(" AND ")
            }
            s.append(globalFilter.formatKeywordList(globalFilter.getTextFilters(), " OR ", "", 1900))
        }

        if (!globalFilter.getDiseaseFilters().isEmpty()) {
            if (s.length() > 0) {
                s.append(" AND ")
            }
            s.append(globalFilter.formatKeywordList(globalFilter.getDiseaseFilters(), " OR ", "", 1900))
        }

        if (!globalFilter.getCompoundFilters().isEmpty()) {
            if (s.length() > 0) {
                s.append(" AND ")
            }
            s.append(globalFilter.formatKeywordList(globalFilter.getCompoundFilters(), " OR ", "", 1900))
        }

        if (!globalFilter.getTrialFilters().isEmpty()) {
            if (s.length() > 0) {
                s.append(" AND ")
            }
            s.append(globalFilter.formatKeywordList(globalFilter.getTrialFilters(), " OR ", "", 1900))
        }

        if (s.length() < 1) {
            s.append(searchText)
        }
        return s.toString()
    }
}
