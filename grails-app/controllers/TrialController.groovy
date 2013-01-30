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
 * $Id: TrialController.groovy 10280 2011-10-29 03:00:52Z jliu $
 * @author $Author: jliu $
 * @version $Revision: 10280 $
 */
import com.recomdata.util.DomainObjectExcelHelper;
import grails.converters.*
import org.transmart.biomart.ClinicalTrial
import org.transmart.biomart.Experiment

import org.transmart.SearchResult;
import org.transmart.biomart.BioAssayAnalysis;
import org.transmart.biomart.BioAssayPlatform;
import org.transmart.searchapp.SearchKeyword

class TrialController {

	def trialQueryService
	def heatmapService
	def filterQueryService
	def analysisDataExportService
	def searchService
	def clinicalTrialAnalysisTEAService

	def showTrialFilter = {
		def contentType = BioAssayAnalysis.executeQuery("SELECT DISTINCT assayDataType FROM org.transmart.biomart.BioAssayAnalysis WHERE assayDataType IS NOT NULL")
		if(contentType==null) contentType=[]

		def diseases = filterQueryService.trialDiseaseFilter(session.searchFilter);
		def compounds = filterQueryService.trialCompoundFilter(session.searchFilter);
		def phases=filterQueryService.trialPhaseFilter();
		def studyTypes=filterQueryService.studyTypeFilter();
		def studyDesigns=filterQueryService.studyDesignFilter("Clinical Trial");

		render(template:'trialFilter', model:[studyPlatform:contentType,
					diseases:diseases,
					compounds:compounds,
					phases:phases,
					studyTypes:studyTypes,
					studyDesigns:studyDesigns])
	}

	def filterTrial = {
		// selected trials before this post
		def befCTrials = session.searchFilter.trialFilter.selectedtrials;

		bindData(session.searchFilter.trialFilter, params)
		def ctrials = params.checked;
		session.searchFilter.trialFilter.selectedtrials = []
		if(ctrials!=null && ctrials.length()>0){
			def allselected = ctrials.split(",").toList()
			if (!allselected.contains("EmptyTrial")) { // EmptyTrial indicates All has been checked
				session.searchFilter.trialFilter.selectedtrials.addAll(allselected)
			}
		} else {
			// session.searchFilter.trialFilter.selectedtrials.add("-1")

			// selecting no trials does not make sense since the result is always nothing!
			// In this case, assume the previous search trials will be used (usually this happens when user clicks filter button
			// before the tree has populated with trials)
			session.searchFilter.trialFilter.selectedtrials = befCTrials;
		}

		log.info "filterTrial:"+session.searchFilter.trialFilter.selectedtrials
		def sResult = new SearchResult()

		session.searchFilter.datasource="trial"
		searchService.doResultCount(sResult,session.searchFilter)
		render(view:'/search/list',model:[searchresult:sResult, page:false])
	}

	def datasourceTrial = {
		def sResult = new SearchResult()
		def max = grailsApplication.config.com.recomdata.search.paginate.max
		def paramMap =searchService.createPagingParamMap(params,max,0)
		sResult.trialCount = trialQueryService.countTrial(session.searchFilter)
		def trialAnalysisCount = trialQueryService.countAnalysis(session.searchFilter)
		sResult.result=trialQueryService.queryTrial(false, session.searchFilter, paramMap)
		sResult.result.analysisCount = trialAnalysisCount;
		sResult.result.expCount = sResult.trialCount;
		render(template:'trialResult',model:[searchresult:sResult, page:false])
	}

	def datasourceTrialTEA = {
		def sResult = new SearchResult()
		def max = grailsApplication.config.com.recomdata.search.paginate.max
		def paramMap =searchService.createPagingParamMap(params,max,0)
		sResult.trialCount = trialQueryService.countTrial(session.searchFilter)
		//sResult.result=trialQueryService.queryTrial(false, session.searchFilter, paramMap)
		//sResult.trialCount =clinicalTrialAnalysisTEAService.countAnalysis(session.searchFilter)
		sResult.result=clinicalTrialAnalysisTEAService.queryExpAnalysis(session.searchFilter, paramMap)
		sResult.result.expCount = sResult.trialCount;
		render(template:'trialResult',model:[searchresult:sResult, page:false])
	}

	def showAnalysis = {
		def analysis = BioAssayAnalysis.get(params.id)
		render(template:'analysisdetail', model:[analysis:analysis])
	}

	def expDetail = {
		def trialid = Long.valueOf(String.valueOf(params.id))
		render(template:'clinicaltrialdetail', model:[clinicalTrial:ClinicalTrial.get(trialid), search:1])
	}

	/**
	 * Renders the trial details in the pop up window when a user right clicks a trial in datasetExplorer
	 */
	def trialDetailByTrialNumber = {
		def trialNumber = params['id'].toUpperCase()
		def conceptType = params['conceptType']
		def istrial = true;
		def exp = ClinicalTrial.findByTrialNumber(trialNumber);
		if(exp==null){
			exp = Experiment.findByAccession(trialNumber);
			istrial = false;
		}
	//	log.info("test encode:"+URLEncoder.encode("https://sss.ss/ge/pub/220202"));
		
		def skid  = null;
		def sk = SearchKeyword.findByKeyword(trialNumber);
		if(sk!=null){
			skid=sk.id
		}
		if(exp!=null)	{

			if(istrial){
				def trialview = grailsApplication.config.com.recomdata?.view?.studyview?:"_clinicaltrialdetail";
				
				if(trialview.startsWith("_")){
					render(template:trialview.substring(1), model:[clinicalTrial:exp, searchId:skid])
				}else{
					render(view:trialview, model:[clinicalTrial:exp, searchId:skid])
				}

			}else {

				render(template:'/experiment/expDetail', model:[experimentInstance:exp, searchId:skid])
			}


		} else	{
			log.warn "Experiment is null, indicating that to the user..."
			render(view:'/experiment/noresults')
		}
	}

	def getTrialAnalysis = {
		def trialid = params.id
		def tResult = trialQueryService.queryTrialAnalysis(trialid, session.searchFilter)
		render(template:'trialAnalysis', model:[trialresult:tResult])
	}

	def trialFilterJSON = {
		// need to mark  trial with data
		// tmp solution

		def triallist = org.transmart.biomart.ClinicalTrial.executeQuery("SELECT b.id, b.trialNumber, b.title FROM org.transmart.biomart.ClinicalTrial b, org.transmart.searchapp.SearchKeyword s  WHERE s.bioDataId=b.id ORDER BY b.trialNumber");

		//		    def triallist = org.transmart.biomart.ClinicalTrial.listOrderByTrialNumber();
		boolean filtercheck = !session.searchFilter.trialFilter.newFilter;

		Set selectedTrials = session.searchFilter.trialFilter.selectedtrials;
		log.info selectedTrials
		boolean rootcheck = true;
		if(filtercheck)
			rootcheck = selectedTrials.contains("EmptyTrial")
		def ctriallist =[]
		for(trial in triallist){
			boolean c = true;
			def trialid = trial[0];
			def trialnum = trial[1];
			def trialtitle=trial[2];
			if(filtercheck){
				c = selectedTrials.contains(String.valueOf(trialid))
			}

			def tooltip = trialtitle==null?(trialnum):(trialtitle)
			def name = trialnum
			if(trialtitle!=null){
				int maxSize = 95;
				int len = trialtitle.length()>maxSize?maxSize:trialtitle.length()
				if(len<maxSize) {
					name +=" - "+trialtitle.substring(0,len)
				} else {
					name +=" - "+trialtitle.substring(0,len)+"..."
				}
			}
			ctriallist.add([text:name, id:String.valueOf(trialid), leaf:true, checked:c, uiProvider:'Ext.tree.CheckboxUI', qtip:tooltip] )
		}
		session.searchFilter.trialFilter.newFilter = false;

		def trials =[
			[text:'All Trials',
				id:'EmptyTrial',
				leaf:false,
				uiProvider:'Ext.tree.CheckboxUI',
				checked:rootcheck,
				qtip:'All trials',
				children:ctriallist]
		]
		def v = trials as JSON
		render (v)
	}

	def downloadanalysisexcel = {

		def geneexpr = org.transmart.biomart.BioAssayAnalysis.get(Long.parseLong(params.id.toString()))
		def filename = geneexpr.shortDescription.replace("<", "-").replace(">", "-")
		filename = filename.replace(":", "-").replace("\"", "-").replace("/", "-")
		filename = filename.replace("\\", "-").replace("?", "-").replace("*", "-")
		if (filename.length() > 50) {
			filename = filename.substring(0, 50)
		}
		filename += ".xls"
		response.setHeader("Content-Type", "application/vnd.ms-excel; charset=utf-8")
		response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"")
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0")
		response.setHeader("Pragma", "public");
		response.setHeader("Expires", "0");
		//log.info "before call"
		response.outputStream<<analysisDataExportService.renderAnalysisInExcel(geneexpr)
	}

	// download search result to GPE file for Pathway Studio
	def downloadanalysisgpe ={
		response.setHeader("Content-Disposition", "attachment; filename=\"expression.gpe\"")
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0")
		response.setHeader("Pragma", "public");
		response.setHeader("Expires", "0");
		def analysis = org.transmart.biomart.BioAssayAnalysis.get(Long.parseLong(params.id.toString()))
		response.outputStream<<analysisDataExportService.renderAnalysisInExcel(analysis)
	}

	def downloadStudy = {
		log.info("Downloading the Trial Study view");
		def sResult = new SearchResult()
		def trialRS = null
		def trialMap = [:]

		sResult.result=trialQueryService.queryTrial(false, session.searchFilter, null)
		sResult.result.expAnalysisResults.each()	{
			trialRS = trialQueryService.queryTrialAnalysis(it.trial.id, session.searchFilter)
			trialMap.put(it.trial, trialRS.analysisResultList)
		}

		DomainObjectExcelHelper.downloadToExcel(response, "trialstudyviewexport.xls", analysisDataExportService.createExcelTrialStudyView(sResult, trialMap));
	}

	def downloadAnalysisTEA = {
		log.info("Downloading the Trial TEA Analysis view");
		def sResult = new SearchResult()

		sResult.result=clinicalTrialAnalysisTEAService.queryExpAnalysis(session.searchFilter, null)
		DomainObjectExcelHelper.downloadToExcel(response, "trialteaviewexport.xls", analysisDataExportService.createExcelTrialTEAView(sResult));
	}
}
