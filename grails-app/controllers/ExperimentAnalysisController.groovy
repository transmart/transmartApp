/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

/**
 * $Id: ExperimentAnalysisController.groovy 10098 2011-10-19 18:39:32Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 10098 $
 */

import org.transmart.SearchResult;

import com.recomdata.util.DomainObjectExcelHelper;
import com.recomdata.util.ExcelGenerator;
import com.recomdata.util.ExcelSheet;
import org.transmart.biomart.Experiment
import com.recomdata.util.ElapseTimer;

class ExperimentAnalysisController {

	def experimentAnalysisQueryService
	def filterQueryService
	def analysisDataExportService
	def searchService
	def experimentAnalysisTEAService

	// session attribute
	static def TEA_PAGING_DATA = "analListPaging"

	def showFilter = {
		def filter =session.searchFilter

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
		if(expDesigns==null) expDesigns=[]
		//log.info "expDesigns: " + expDesigns

		// no data?
		def celllines = [] 

		// no data?
		def expTypes=[] //experimentAnalysisQueryService.findExperimentTypeFilter()

		def platformOrganisms = experimentAnalysisQueryService.findPlatformOrganizmFilter(filter)

		stimer.logElapsed("Loading Exp Analysis Filters", true);
		// note: removed datasource, celllines and expTypes since no data being retrieved (removed from filter page too)
		render(template:'expFilter', model:[diseases:diseases, compounds:compounds, expDesigns:expDesigns, platformOrganisms:platformOrganisms])
	}

	def filterResult = {
		def sResult = new SearchResult()
		session.searchFilter.datasource="experiment"
		bindData(session.searchFilter.expAnalysisFilter, params)

		//  log.info params
		searchService.doResultCount(sResult,session.searchFilter)
		render(view:'/search/list',model:[searchresult:sResult, page:false])
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
		def paramMap = searchService.createPagingParamMap(params,max,0)

		def sResult = new SearchResult()
	//	sResult.experimentCount = experimentAnalysisQueryService.countExperiment(session.searchFilter);
		sResult.experimentCount = experimentAnalysisQueryService.countExperimentMV(session.searchFilter);
		
		def expAnalysisCount = experimentAnalysisQueryService.countAnalysisMV(session.searchFilter);
		//def expAnalysisCount = 9;
		
		stimer.logElapsed("Loading Exp Analysis Counts", true);

		sResult.result=experimentAnalysisQueryService.queryExperiment(session.searchFilter, paramMap)
		sResult.result.analysisCount = expAnalysisCount;
		sResult.result.expCount = sResult.experimentCount;
		//	sResult.experimentCount = experimentAnalysisTEAService.countAnalysis(session.searchFilter);
		//	sResult.result = experimentAnalysisTEAService.queryExperiment(session.searchFilter, paramMap)
		render(template:'experimentResult',model:[searchresult:sResult, page:false])
	}

	/**
	 * tea result view
	 */
	def datasourceResultTEA = {
		//def diseases = experimentAnalysisQueryService.findExperimentDiseaseFilter(session.searchFilter, "Experiment");
		//log.info diseases
		def stimer = new ElapseTimer();

		def max = grailsApplication.config.com.recomdata.search.paginate.max
		def paramMap = searchService.createPagingParamMap(params,max,0)

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

		render(template:'experimentResult',model:[searchresult:sResult, page:true])
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

		render(template:'experimentResult',model:[searchresult:sResult, page:true])
	}

	def expDetail = {
		//log.info "** action: expDetail called!"
		def expid = params.id
		def exp = Experiment.get(expid)
		log.info "exp.id = " + exp.id
		def platforms = experimentAnalysisQueryService.getPlatformsForExperment(exp.id);
		def organisms = new HashSet()
		for(pf in platforms){
			organisms.add(pf.organism)
		}

		render(template:'/experiment/expDetail', model:[experimentInstance:exp, expPlatforms:platforms, expOrganisms:organisms,search:1])
	}

	def getAnalysis = {
		def expid = params.id
		def tResult = experimentAnalysisQueryService.queryAnalysis(expid, session.searchFilter)
		render(template:'/trial/trialAnalysis', model:[trialresult:tResult])
	}

	// download search result into excel
	def downloadanalysisexcel ={

		response.setHeader("Content-Type", "application/vnd.ms-excel; charset=utf-8")
		response.setHeader("Content-Disposition", "attachment; filename=\"pre_clinical.xls\"")
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0")
		response.setHeader("Pragma", "public");
		response.setHeader("Expires", "0");
		def analysis = org.transmart.biomart.BioAssayAnalysis.get(Long.parseLong(params.id.toString()))
		response.outputStream<<analysisDataExportService.renderAnalysisInExcel(analysis)
	}

	//	 download search result to GPE file for Pathway Studio
	def downloadanalysisgpe ={
		response.setHeader("Content-Disposition", "attachment; filename=\"expression.gpe\"")
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0")
		response.setHeader("Pragma", "public");
		response.setHeader("Expires", "0");
		def analysis = org.transmart.biomart.BioAssayAnalysis.get(Long.parseLong(params.id.toString()))
		response.outputStream<<analysisDataExportService.renderAnalysisInExcel(analysis)
	}

	/**
	 * page the tea analysis data
	 */
	private List pageTEAData(List analysisList, int offset, int pageSize) {

		List pagedData = new ArrayList()
		int numRecs = analysisList.size()
		int lastIndex = (offset+pageSize<=numRecs) ? (offset+pageSize-1) : numRecs;

		// iteratre through list starting from start index
		ListIterator it = analysisList.listIterator(offset);

		while(it.hasNext()) {
			//attach to hibernate session
			def ar = it.next();
			if(!ar.analysis.isAttached()) ar.analysis.attach();

			pagedData.add(ar)
			int nextIdx = it.nextIndex()
			if(nextIdx>lastIndex) break;
		}
		log.info("Paged data: start Idx: "+offset+"; last idx: "+lastIndex+" ; size: "+pagedData.size())
		return pagedData;
	}

	def downloadAnalysis = {
	    log.info("Downloading the Experimental Analysis (Study) view");
		def sResult = new SearchResult()
		def analysisRS = null
		def eaMap = [:]

		sResult.result=experimentAnalysisQueryService.queryExperiment(session.searchFilter,null)
		sResult.result.expAnalysisResults.each()	{
			analysisRS=experimentAnalysisQueryService.queryAnalysis(it.experiment.id, session.searchFilter)
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
}
