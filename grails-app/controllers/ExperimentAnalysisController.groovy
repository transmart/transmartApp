/**
 * $Id: ExperimentAnalysisController.groovy 10098 2011-10-19 18:39:32Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 10098 $
 */

import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import com.recomdata.util.DomainObjectExcelHelper
import com.recomdata.util.ElapseTimer
import fm.FmFolder
import fm.FmFolderAssociation
import org.transmart.SearchResult
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.Experiment

class ExperimentAnalysisController {

    def experimentAnalysisQueryService
    def filterQueryService
    def analysisDataExportService
    def searchService
    def experimentAnalysisTEAService
    def formLayoutService

    // session attribute
    static def TEA_PAGING_DATA = "analListPaging"

    def showFilter = {
        def filter = session.searchFilter

        def datasources = []
        def stimer = new ElapseTimer();
        //log.info ">> Compound query:"
        def compounds = filterQueryService.experimentCompoundFilter("Experiment");

        //log.info ">> Diseases query:"
        def diseases = filterQueryService.findExperimentDiseaseFilter(session.searchFilter, "Experiment");
        //if(diseases==null) diseases=[]
        //log.info "diseases: " + diseases)

        //log.info ">> Exp designs query:"
        def expDesigns = experimentAnalysisQueryService.findExperimentDesignFilter(filter)
        if (expDesigns == null) expDesigns = []
        //log.info "expDesigns: " + expDesigns

        // no data?
        def celllines = [] //GeneExprAnalysis.executeQuery(queryCellLines.toString(),filter.gids)

        // no data?
        def expTypes = [] //experimentAnalysisQueryService.findExperimentTypeFilter()

        def platformOrganisms = experimentAnalysisQueryService.findPlatformOrganizmFilter(filter)

        stimer.logElapsed("Loading Exp Analysis Filters", true);
        // note: removed datasource, celllines and expTypes since no data being retrieved (removed from filter page too)
        render(template: 'expFilter', model: [diseases: diseases, compounds: compounds, expDesigns: expDesigns, platformOrganisms: platformOrganisms])
    }

    def filterResult = {
        def sResult = new SearchResult()
        session.searchFilter.datasource = "experiment"
        bindData(session.searchFilter.expAnalysisFilter, params)

        //  log.info params
        searchService.doResultCount(sResult, session.searchFilter)
        render(view: '/search/list', model: [searchresult: sResult, page: false])
    }

    /**
     * summary result view
     */
    def datasourceResult = {
        //def diseases = experimentAnalysisQueryService.findExperimentDiseaseFilter(session.searchFilter, "Experiment");
        //log.info diseases
        def stimer = new ElapseTimer();

        //	log.info params
        def max = grailsApplication.config.com.recomdata.search.paginate.max
        def paramMap = searchService.createPagingParamMap(params, max, 0)

        def sResult = new SearchResult()
        //	sResult.experimentCount = experimentAnalysisQueryService.countExperiment(session.searchFilter);
        sResult.experimentCount = experimentAnalysisQueryService.countExperimentMV(session.searchFilter);

        def expAnalysisCount = experimentAnalysisQueryService.countAnalysisMV(session.searchFilter);
        //def expAnalysisCount = 9;

        stimer.logElapsed("Loading Exp Analysis Counts", true);

        sResult.result = experimentAnalysisQueryService.queryExperiment(session.searchFilter, paramMap)
        sResult.result.analysisCount = expAnalysisCount;
        sResult.result.expCount = sResult.experimentCount;
        //	sResult.experimentCount = experimentAnalysisTEAService.countAnalysis(session.searchFilter);
        //	sResult.result = experimentAnalysisTEAService.queryExperiment(session.searchFilter, paramMap)
        render(template: 'experimentResult', model: [searchresult: sResult, page: false])
    }

    /**
     * tea result view
     */
    def datasourceResultTEA = {
        //def diseases = experimentAnalysisQueryService.findExperimentDiseaseFilter(session.searchFilter, "Experiment");
        //log.info diseases
        def stimer = new ElapseTimer();

        def max = grailsApplication.config.com.recomdata.search.paginate.max
        def paramMap = searchService.createPagingParamMap(params, max, 0)

        def sResult = new SearchResult()
        //sResult.result=experimentAnalysisQueryService.queryExperiment(session.searchFilter, paramMap)
        //sResult.experimentCount = experimentAnalysisTEAService.countAnalysis(session.searchFilter);

        sResult.experimentCount = experimentAnalysisQueryService.countExperimentMV(session.searchFilter);
        //sResult.experimentCount = experimentAnalysisQueryService.countExperiment(session.searchFilter);

        sResult.result = experimentAnalysisTEAService.queryExpAnalysis(session.searchFilter, paramMap)
        stimer.logElapsed("Loading Exp TEA Counts", true);
        sResult.result.expCount = sResult.experimentCount;

        def ear = sResult.result.expAnalysisResults[0]
        ear.pagedAnalysisList = pageTEAData(ear.analysisResultList, 0, max);

        // store in session for paging requests
        session.setAttribute(TEA_PAGING_DATA, sResult)

        render(template: 'experimentResult', model: [searchresult: sResult, page: true])
    }

    /**
     * page TEA analysis view
     */
    def pageTEAAnalysisView = {

        def max = Integer.parseInt(params.max)
        def offset = Integer.parseInt(params.offset)

        // retrieve session data, page analyses
        def sResult = session.getAttribute(TEA_PAGING_DATA);
        def ear = sResult.result.expAnalysisResults[0]
        ear.pagedAnalysisList = pageTEAData(ear.analysisResultList, offset, max);

        render(template: 'experimentResult', model: [searchresult: sResult, page: true])
    }

    def expDetail = {
        log.info "** action: expDetail called!"
        log.info params
        def expid = params.id
        def expaccession = params.accession

        def exp
        if (expid) {
            exp = Experiment.get(expid)
        } else {
            exp = Experiment.findByAccession(expaccession)
        }
        log.info "exp.id = " + exp.id
        def platforms = experimentAnalysisQueryService.getPlatformsForExperment(exp.id);
        def organisms = new HashSet()
        for (pf in platforms) {
            organisms.add(pf.organism)
        }

        def formLayout = formLayoutService.getLayout('study');

        def parent = FmFolderAssociation.findByObjectUid(expid)

        log.info "Parent = " + parent

//		def analysisFolders = FmFolder.executeQuery("from FmFolder as fd where fd.folderType = :folderType and fd.folderLevel = :level and fd.folderFullName like '" + parent.folderFullName + "%' order by folderName", [folderType: FolderType.ANALYSIS.name(), level: parent.folderLevel + 1])

//		log.info "Subfolders = " + analysisFolders

        ExportTableNew table;

        //Keep this if you want to cache the grid data
        //ExportTableNew table=(ExportTableNew)request.getSession().getAttribute("gridtable");

        if (table == null) {
            table = new ExportTableNew();
        }

        def table2 = new ExportTableNew();
        table2.putColumn("name", new ExportColumn("name", "Name", "", "String"));
        table2.putColumn("biosource", new ExportColumn("biosource", "Biosource", "", "String"));
        table2.putColumn("Technology", new ExportColumn("Technology", "Technology", "", "String"));
        table2.putColumn("Biomarkersstudied", new ExportColumn("Biomarkersstudied", "Biomarkers studied", "", "String"));

        ExportRowNew newrow4 = new ExportRowNew();
        newrow4.put("name", "My Analysis");
        newrow4.put("biosource", "Endometrial tumor");
        newrow4.put("Technology", "IHC");
        newrow4.put("Biomarkersstudied", "PTEN");
        table2.putRow("somerow", newrow4);


        table.putColumn("name", new ExportColumn("name", "Name", "", "String"));
        table.putColumn("biosource", new ExportColumn("biosource", "Biosource", "", "String"));
        table.putColumn("Technology", new ExportColumn("Technology", "Technology", "", "String"));
        table.putColumn("Biomarkersstudied", new ExportColumn("Biomarkersstudied", "Biomarkers studied", "", "String"));

        ExportRowNew newrow = new ExportRowNew();
        newrow.put("name", "Assay 1");
        newrow.put("biosource", "Endometrial tumor");
        newrow.put("Technology", "IHC");
        newrow.put("Biomarkersstudied", "PTEN");

        ExportRowNew newrow2 = new ExportRowNew();
        newrow2.put("name", "Assay 2");
        newrow2.put("biosource", "Endometrial tumor");
        newrow2.put("Technology", "H&E");
        newrow2.put("Biomarkersstudied", "None");

        ExportRowNew newrow3 = new ExportRowNew();
        newrow3.put("name", "Assay 3");
        newrow3.put("biosource", "Endometrial tumor");
        newrow3.put("Technology", "nucleotide sequencing");
        newrow3.put("Biomarkersstudied", "AKT1; BRAF; ESR1; HRAS; KRAS;");

        table.putRow("somerow", newrow);
        table.putRow("somerow2", newrow2);
        table.putRow("somerow3", newrow3);

        def jSONToReturn1 = table.toJSON_DataTables("").toString(5);
        def jSONToReturn2 = table2.toJSON_DataTables("").toString(5);

        request.getSession().setAttribute("gridtable", table);

        log.info "formLayout = " + formLayout
        render(template: '/experiment/expDetail', model: [layout: formLayout, experimentInstance: exp, expPlatforms: platforms, expOrganisms: organisms, search: 1, jSONForGrid: jSONToReturn2, jSONForGrid1: jSONToReturn1])
    }

    def getAnalysis = {
        def expid = params.id
        def tResult = experimentAnalysisQueryService.queryAnalysis(expid, session.searchFilter)
        render(template: '/trial/trialAnalysis', model: [trialresult: tResult])
    }

    // download search result into excel
    def downloadanalysisexcel = {

        response.setHeader("Content-Type", "application/vnd.ms-excel; charset=utf-8")
        response.setHeader("Content-Disposition", "attachment; filename=\"pre_clinical.xls\"")
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0")
        response.setHeader("Pragma", "public");
        response.setHeader("Expires", "0");
        def analysis = BioAssayAnalysis.get(Long.parseLong(params.id.toString()))
        response.outputStream << analysisDataExportService.renderAnalysisInExcel(analysis)
    }

    //	 download search result to GPE file for Pathway Studio
    def downloadanalysisgpe = {
        response.setHeader("Content-Disposition", "attachment; filename=\"expression.gpe\"")
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0")
        response.setHeader("Pragma", "public");
        response.setHeader("Expires", "0");
        def analysis = BioAssayAnalysis.get(Long.parseLong(params.id.toString()))
        response.outputStream << analysisDataExportService.renderAnalysisInExcel(analysis)
    }

    /**
     * page the tea analysis data
     */
    private List pageTEAData(List analysisList, int offset, int pageSize) {

        List pagedData = new ArrayList()
        int numRecs = analysisList.size()
        int lastIndex = (offset + pageSize <= numRecs) ? (offset + pageSize - 1) : numRecs;

        // iteratre through list starting from start index
        ListIterator it = analysisList.listIterator(offset);

        while (it.hasNext()) {
            //attach to hibernate session
            def ar = it.next();
            if (!ar.analysis.isAttached()) ar.analysis.attach();

            pagedData.add(ar)
            int nextIdx = it.nextIndex()
            if (nextIdx > lastIndex) break;
        }
        log.info("Paged data: start Idx: " + offset + "; last idx: " + lastIndex + " ; size: " + pagedData.size())
        return pagedData;
    }

    def downloadAnalysis = {
        log.info("Downloading the Experimental Analysis (Study) view");
        def sResult = new SearchResult()
        def analysisRS = null
        def eaMap = [:]

        sResult.result = experimentAnalysisQueryService.queryExperiment(session.searchFilter, null)
        sResult.result.expAnalysisResults.each() {
            analysisRS = experimentAnalysisQueryService.queryAnalysis(it.experiment.id, session.searchFilter)
            eaMap.put(it.experiment, analysisRS.analysisResultList)
        }
        DomainObjectExcelHelper.downloadToExcel(response, "analysisstudyviewexport.xls", analysisDataExportService.createExcelEAStudyView(sResult, eaMap));
    }

    def downloadAnalysisTEA = {
        log.info("Downloading the Experimental Analysis TEA view");
        def sResult = new SearchResult()

        sResult.result = experimentAnalysisTEAService.queryExpAnalysis(session.searchFilter, null)
        DomainObjectExcelHelper.downloadToExcel(response, "analysisteaviewexport.xls", analysisDataExportService.createExcelEATEAView(sResult));
    }

    /**
     * This will render a UI where the user can pick an experiment from a list of all the experiments in the system. Selection of multiple studies is allowed.
     */
    def browseAnalysisMultiSelect = {
        def analyses = org.transmart.biomart.BioAssayAnalysis.executeQuery("select id, name, etlId from BioAssayAnalysis b order by b.name");
        render(template: 'browseMulti', model: [analyses: analyses])
    }

}
