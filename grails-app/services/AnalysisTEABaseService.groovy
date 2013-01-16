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
  

import org.transmart.biomart.BioMarker
import org.transmart.biomart.Compound
import org.transmart.biomart.Disease
import org.transmart.biomart.ClinicalTrial
import org.transmart.biomart.Experiment

import org.transmart.AnalysisResult;
import org.transmart.AssayAnalysisValue;
import org.transmart.ExpAnalysisResultSet;
import org.transmart.SearchFilter;
import org.transmart.biomart.BioAssayAnalysis;
import org.transmart.biomart.BioAssayAnalysisData;
import org.transmart.biomart.BioAssayAnalysisDataTea;
import org.transmart.searchapp.SearchBioMarkerCorrelFastMV
import org.transmart.biomart.BioMarkerCorrelationMV
import com.recomdata.search.query.AssayAnalysisDataQuery
import com.recomdata.search.query.AssayAnalysisDataTeaQuery
import com.recomdata.search.query.Query
import com.recomdata.genesignature.TEAScoreManager

/**
 * $Id: AnalysisTEABaseService.groovy 11072 2011-12-08 19:03:28Z jliu $
 * @author $Author: jliu $
 * @version $Revision: 11072 $
 */

class AnalysisTEABaseService {

	/**
	 * count analysis with criteria
	 */
	def countAnalysis(SearchFilter filter){

		if(filter == null || filter.globalFilter.isTextOnly()){
			return 0
		}

		return org.transmart.biomart.BioAssayAnalysisData.executeQuery(createCountQuery(filter))[0]
	}

	/**
	 * retrieve trials with criteria
	 */
	def queryExpAnalysis(SearchFilter filter, paramMap) {

		if(filter == null || filter.globalFilter.isTextOnly()){
			return []
		}
		def result = queryExpAnalysis(filter)//org.transmart.biomart.BioAssayAnalysisData.executeQuery(createQuery(false, filter), paramMap==null?[:]:paramMap)

		List trialResult = []
		if(result!=null)
			trialResult.add(result)

	//	log.info("queryExpAnalysis result class: "+result.getClass().getName())
		return new ExpAnalysisResultSet(expAnalysisResults:trialResult, analysisCount:result.analysisCount, expCount:result.expCount, groupByExp:false)
	}

	/**
	 * template methods
	 */
	def getExpType(){
		return "Experiment";
	}

	def createResultObject(){
		return null;
	}

	def createSubFilterCriteria(SearchFilter filter, Query query){

	}

	def createNPVCondition(query){
		query.addCondition("baad.teaNormalizedPValue<=0.05")
	}

	/**
	 *
	 */
	def createCountQuery(SearchFilter filter){
		if(filter == null || filter.globalFilter.isTextOnly()){
			return " WHERE 1=0"
		}
		def gfilter = filter.globalFilter

		def query =new AssayAnalysisDataQuery(mainTableAlias:'baad');
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad ");
		query.addCondition(" baad.experiment.type='"+getExpType()+"'")

		////query.addTable ("org.transmart.biomart.ClinicalTrial ct ");
		//query.addCondition("baad.experiment.id = ct.id ")

		query.createGlobalFilterCriteria(gfilter);
		createSubFilterCriteria(filter, query);

		query.addSelect("COUNT(DISTINCT baad.analysis.id) ");

		def q= query.generateSQL()
		return q
	}


	/**
	 * find distinct trial analyses with current filters
	 */

	def createAnalysisIDSelectQuery(SearchFilter filter){
		if(filter == null || filter.globalFilter.isTextOnly()){
			return " SELECT -1 FROM org.transmart.biomart.BioAssayAnalysisData baad WHERE 1 = 1 "
		}
		def gfilter = filter.globalFilter

		def query =new AssayAnalysisDataQuery(mainTableAlias:"baad", setDistinct:true);
		query.addTable("org.transmart.biomart.BioAssayAnalysisDataTea baad ");
		query.addCondition(" baad.experiment.type='"+getExpType()+"'")

		query.createGlobalFilterCriteria(gfilter);
		createSubFilterCriteria(filter, query);
	//	createNPVCondition(query)
		query.addSelect("baad.analysis.id")

		return query.generateSQL()
	}

	/**
	 * get count of relevant analyses in the search according to TEA criteria
	 */
	def queryExpAnalysisCount(SearchFilter filter) {

		def gfilter = filter.globalFilter
		def analysisCount = 0

		if(filter == null || gfilter.isTextOnly()) return analysisCount;

		// get distinct analyses
		def query = new AssayAnalysisDataTeaQuery(mainTableAlias:"baad", setDistinct:true)
		query.addTable("org.transmart.biomart.BioAssayAnalysisDataTea baad")
		//query.addTable("JOIN baad.markers baad_bm")
		query.addCondition(" baad.experiment.type='"+getExpType()+"'")
		query.addCondition(" baad.analysis.teaDataCount IS NOT NULL")
		// distinct analyses
		query.addSelect("COUNT(DISTINCT baad.analysis.id)")

		// add critiera
		query.createGlobalFilterCriteria(gfilter, true);
		createSubFilterCriteria(filter, query);
	//	createNPVCondition(query)

		// get count
		def result = org.transmart.biomart.BioAssayAnalysisDataTea.executeQuery(query.generateSQL())
		log.info "anal ct result: "+result
		if(result!=null && result.size()>0) analysisCount = result[0]
		return analysisCount
	}

	/**
	 *
	 */
	def queryExpAnalysis(SearchFilter filter){
		def gfilter = filter.globalFilter
		def result = null;
		def tResult = createResultObject();

		if(!gfilter.getBioMarkerFilters().isEmpty()){
			def query = new AssayAnalysisDataQuery(mainTableAlias:"baad", setDistinct:true)
			query.addTable("org.transmart.biomart.BioAssayAnalysisDataTea baad")
			//query.addTable("JOIN FETCH baad.analysis ")
			query.addTable("JOIN baad.featureGroup.markers baad_bm")
			query.addCondition(" baad.experimentType='"+getExpType()+"'")

			query.addSelect("baad")
			query.addSelect("baad_bm")
			query.addSelect("baad.experiment.id")
			query.addSelect("baad.experiment.accession")
			
			// expand biomarkers
			query.createGlobalFilterCriteria(gfilter, true);
			createSubFilterCriteria(filter, query);
			//createNPVCondition(query)

			def sql = query.generateSQL();
			result = org.transmart.biomart.BioAssayAnalysisDataTea.executeQuery(sql)

			// get up/down info from mv for all biomarkers
			def biomarkerFilters = gfilter.getBioMarkerFilters()
			def mids = biomarkerFilters.getKeywordDataIdString()
			def updownResult = []

			// filter contain gene sig or list?
			if(gfilter.getGeneSigListFilters().size()>0) {

				// switch for gene sig or list
				def dynamicValuesQuery
				if(gfilter.getGeneSignatureFilters().size()>0) {
					dynamicValuesQuery = "SELECT DISTINCT sbmcmv.assocBioMarkerId, sbmcmv.valueMetric FROM org.transmart.searchapp.SearchBioMarkerCorrelFastMV sbmcmv WHERE sbmcmv.domainObjectId in ("+mids+")";
				} else {
					// always up regulated for gene list
					dynamicValuesQuery = "SELECT DISTINCT sbmcmv.assocBioMarkerId, 1 as valueMetric FROM org.transmart.searchapp.SearchBioMarkerCorrelFastMV sbmcmv WHERE sbmcmv.domainObjectId in ("+mids+")";
				}
				updownResult.addAll(SearchBioMarkerCorrelFastMV.executeQuery(dynamicValuesQuery))
				log.info "number of search app biomarkers: "+updownResult.size()
			}

			// add static biomarkers
			// make sure no homology gene is searched
			def bioMarkersQuery = "SELECT DISTINCT bmcmv.assoBioMarkerId as assocBioMarkerId, 0 as valueMetric FROM org.transmart.biomart.BioMarkerCorrelationMV bmcmv WHERE bmcmv.bioMarkerId in ("+mids+") AND bmcmv.correlType <>'HOMOLOGENE_GENE'";
			def staticResult = BioMarkerCorrelationMV.executeQuery(bioMarkersQuery);
			log.info "number of static biomarkers: "+staticResult.size()

			// merge to get complete gene list
			updownResult.addAll(staticResult)
			def bmCount = updownResult.size();
			log.info "total biomarkers: "+bmCount

			// build biomarker/metric map
			Map mvMap = new HashMap();
			def testMetric
			for(mv in updownResult) {
				testMetric = mvMap.get(mv[0])
				if(testMetric==null) {
					mvMap.put(mv[0],mv[1]);
				} else {
					// if no metric value, keep one with a value
					if(testMetric==null && mv[1]!=null) mvMap.put(mv[0],mv[1]);

					// keep larger abs(fold change)
					if(testMetric!=null && mv[1]!=null && Math.abs(mv[1])>Math.abs(testMetric)) {
						log.warn "overriding metric value for biomarker: "+mv[0]+" [ orig: "+testMetric+" new: "+mv[1]+" ]"
						mvMap.put(mv[0],mv[1]);
					}
				}
			}
			processAnalysisResult(result, tResult, mvMap)
		}
		else {
			log.info "in queryExpAnalysis() did not detect any biomarkers!"
			def allAnalysis = getAllAnalyses(filter);
			def expMap = new HashMap();
			def expLkup = null

			for(row in allAnalysis){
				def analysisId = row[0]
				def expId = row[1]
				def expAccession = row[2];
				def countGene = row[3]
				//log.info "extracting analysisId: "+analysisId+"; expId: "+expId+"; gene ct: "+countGene

				// create analysis result
			//	expLkup = expMap.get(expId)
			//	if(expLkup==null) {
			//		expLkup = Experiment.get(expId);
			//		expMap.put(expId, expLkup)
			//	}
				def analysisResult = new AnalysisResult(analysis: BioAssayAnalysis.get(analysisId), experimentId: expId,experimentAccession:expAccession, bioMarkerCount:countGene)
				tResult.analysisResultList.add(analysisResult)

				// get top 50 biomarkers
				def bioMarkers = BioAssayAnalysisDataTea.getTop50AnalysisDataForAnalysis(analysisId);
				processAnalysisResultNoSort(bioMarkers, analysisResult)
			}
			tResult.analysisCount = tResult.analysisResultList.size();
			tResult.expCount = expMap.size();
		}
		//log.info "tResult: "+tResult+"; class: "+tResult.getClass().getName()
		return tResult;
	}

	/**
	 *  get ananlysis only
	 *
	 */
	def getAllAnalyses(SearchFilter filter){
		// need both filters here
		def analysisQuery = new AssayAnalysisDataQuery(mainTableAlias:"baad", setDistinct:true)
		analysisQuery.addTable("org.transmart.biomart.BioAssayAnalysisDataTea baad")
		analysisQuery.addCondition(" baad.experiment.type='"+getExpType()+"'")
		analysisQuery.addCondition(" baad.analysis.teaDataCount IS NOT NULL");

		analysisQuery.addSelect("baad.analysis.id")
		analysisQuery.addSelect("baad.experiment.id")
		analysisQuery.addSelect("baad.experiment.accession")
		
		analysisQuery.addSelect("baad.analysis.teaDataCount")
		analysisQuery.addOrderBy("baad.analysis.teaDataCount DESC")
	//	createNPVCondition(analysisQuery)
		analysisQuery.createGlobalFilterCriteria(filter.globalFilter, true);
		createSubFilterCriteria(filter, analysisQuery);

		return org.transmart.biomart.BioAssayAnalysisDataTea.executeQuery(analysisQuery.generateSQL())
	}

	/**
	 * process analysis result
	 */
	def processAnalysisResultNoSort(List result, AnalysisResult aresult){

		//def aresult = new AnalysisResult(analysis);
		for(row in result){
			def analysisData = row[0]
			def biomarker = row[1];
			aresult.assayAnalysisValueList.add(new AssayAnalysisValue(analysisData:analysisData, bioMarker:biomarker))
		}
	}

	/**
	 * process each analysis
	 */
	def processAnalysisResult(List result, tar, mvMap) {
		LinkedHashMap analysisResultMap = new LinkedHashMap()
		Map expMap = new HashMap();

		// loop through data
		for(row in result){
			def analysisData = row[0]
			def biomarker = row[1]; //org.transmart.biomart.BioMarker.get(row[1])
			
			def mvlookup = mvMap.get(biomarker.id)
			def aid = analysisData.analysis.id;
			//println ("before get id")
			def aresult = analysisResultMap.get(aid)
			def expId = row[2];
			def expAccession = row[3];

			if(aresult==null){
		//		log.info "BAAD: "+analysisData
		//		log.info "BAAD experiment: "+exp+"; class: "+exp.getClass().getName()
		//		log.info "BAAD experiment type: "+exp.type+"; id: "+expId;

				// build experiment lookup map
				//def mapExp = expMap.get(exp.id)
				//if(mapExp==null) expMap.put(exp.id, exp);
			//	def mapExp = expMap.get(expId)
			//	if(mapExp==null) expMap.put(expId, exp);
				//println("before cache")
				def analysisCache = BioAssayAnalysis.get(aid);
	
				aresult = new AnalysisResult(analysis:analysisCache, experimentId:expId, experimentAccession:expAccession);
				analysisResultMap.put(aid, aresult)
			}
			//	log.info "mvlookup: "+mvlookup
			aresult.assayAnalysisValueList.add(new AssayAnalysisValue(analysisData:analysisData, bioMarker:biomarker, valueMetric:mvlookup))
		}

		def aResults = analysisResultMap.values()

		// populate model
		tar.bioMarkerCt = mvMap.size()
		tar.expCount = expMap.size()
		tar.analysisCount = analysisResultMap.size()

		// don't run TEA service for single gene
		if(tar.bioMarkerCt<=1) {
			tar.analysisResultList.addAll(aResults)

			// TODO: sort by analyses by what in this case?

		} else {
			// TEA ranking service
			def teaRankedAnalyses = assignTEAScoresAndRank(aResults, tar.bioMarkerCt?.intValue())

			// populate model
			tar.analysisResultList.addAll(teaRankedAnalyses)
			tar.populateInsignificantTEAAnalysisList();
		}
	}

	/**
	 * function applies the TEA scoring algorithm to each AnalysisResult object and its associated collection of bio markers.
	 * This function assigns the TEA metrics to each AnalysisResult and returns the supplied list in ascending TEA score order
	 */
	def assignTEAScoresAndRank(Collection<AnalysisResult> analyses, int geneCount) {

		AnalysisResult ar;
		List<AnalysisResult> rankedAnalyses = new ArrayList();

		// score manager for TEA
		TEAScoreManager scoreManager = new TEAScoreManager()
		scoreManager.geneCount = geneCount

		// score each analysis
		analyses.each {
			ar = (AnalysisResult) it
		//	log.info ""
		//	log.info ">>>> Assigning TEA metrics to analysis: "+ar.analysis.name+" (N: "+geneCount+")"
			scoreManager.assignTEAMetrics(ar)
			rankedAnalyses.add(ar);

			// sort value list on comparison NPV
			Collections.sort(ar.assayAnalysisValueList)
		}

		// AnalysisResult implements Comparable
		Collections.sort(rankedAnalyses)
		return rankedAnalyses
	}

}
