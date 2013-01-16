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
  

import org.transmart.ExpAnalysisResultSet;
import org.transmart.ExperimentAnalysisResult;
import org.transmart.SearchFilter;

import org.transmart.biomart.BioMarker
import org.transmart.biomart.Compound
import org.transmart.biomart.Disease
import org.transmart.biomart.Experiment
import com.recomdata.search.query.AssayAnalysisDataQuery
import com.recomdata.search.query.ExperimentAssayAnalysisMVQuery
import com.recomdata.search.query.Query
import org.transmart.AnalysisResult
import org.transmart.AssayAnalysisValue
import org.transmart.biomart.BioAssayAnalysis;
import org.transmart.biomart.BioAssayAnalysisData;

import com.recomdata.util.ElapseTimer;
/**
 * $Id: ExperimentAnalysisQueryService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 * todo -- make a super class for experimentanalysisqueryservice and trialqueryservice
 */
class ExperimentAnalysisQueryService {

	/**
	 * count experiment with criteria
	 */
	def countExperiment(SearchFilter filter){

		if(filter == null || filter.globalFilter.isTextOnly()){
			return 0
		}

		return BioAssayAnalysisData.executeQuery(createMVExperimentQuery("COUNT_EXP", filter))[0]
	}

	def countExperimentMV(SearchFilter filter){
		if(filter == null || filter.globalFilter.isTextOnly()){
			return 0
		}
		if(!filter.expAnalysisFilter.isUsed())
			return org.transmart.biomart.BioMarkerExpAnalysisMV.executeQuery(createExpAnalysisMVQuery("COUNT_EXP", filter))[0]
		//	elapseTimer.logElapsed("query count Experiment MV:",true)
		else
			return countExperiment(filter)
	}

	/**
	 * count number of analyses with TEA filtering criteria
	 */
	def countTEAAnalysis(SearchFilter filter){

		if(filter == null || filter.globalFilter.isTextOnly()) return 0;

		def gfilter = filter.globalFilter

		def query =new AssayAnalysisDataQuery(mainTableAlias:"baad")
		query.addTable("org.transmart.biomart.BioAssayAnalysisDataTea baad")
		query.addCondition(" baad.experiment.type='Experiment'")
		query.addCondition(" baad.analysis.teaDataCount IS NOT NULL")
		query.createGlobalFilterCriteria(gfilter);
		createSubFilterCriteria(filter.expAnalysisFilter,query);
		query.addSelect("COUNT(DISTINCT baad.analysis.id) ");
		return org.transmart.biomart.BioAssayAnalysisDataTea.executeQuery(query.generateSQL())[0];
	}

	def countAnalysis(SearchFilter filter){
		if(filter == null || filter.globalFilter.isTextOnly()) return 0;

		return org.transmart.biomart.BioAssayAnalysisData.executeQuery(createMVExperimentQuery("COUNT_ANALYSIS", filter))[0]
	}

	def countAnalysisMV(SearchFilter filter){
		if(filter == null || filter.globalFilter.isTextOnly()) return 0;
		if(!filter.expAnalysisFilter.isUsed())
			return org.transmart.biomart.BioMarkerExpAnalysisMV.executeQuery(createExpAnalysisMVQuery("COUNT_ANALYSIS", filter))[0]
		else
			return countAnalysis(filter)
	}


	def createExpAnalysisMVQuery(countType, SearchFilter filter){
		if(filter == null || filter.globalFilter.isTextOnly()){
			return " WHERE 1=0"
		}
		def gfilter = filter.globalFilter

		def query =new ExperimentAssayAnalysisMVQuery(mainTableAlias:"baad")
		query.addTable("org.transmart.biomart.BioMarkerExpAnalysisMV baad")

		query.createGlobalFilterCriteria(gfilter);
		createSubFilterCriteria(filter.expAnalysisFilter,query);

		if("COUNT_EXP".equals(countType)){
			query.addSelect("COUNT(DISTINCT baad.experiment.id) ");
		} else if ("COUNT_ANALYSIS".equals(countType)){
			//query.addTable("JOIN baad.markers baad_bm")
			query.addSelect("COUNT(DISTINCT baad.analysis.id) ");
		} else if ("COUNT_ANALYSIS_TEA".equals(countType)){
			//	query.addTable("JOIN baad.markers baad_bm")
			query.addSelect("COUNT(DISTINCT baad.analysis.id) ");
			createNPVCondition(query)
		} else {
			query.setDistinct=true
			query.addSelect(" baad.experiment.id")
			query.addSelect(" COUNT(DISTINCT baad.analysis.id)")
			query.addGroupBy("baad.experiment.id")
			query.addOrderBy(" COUNT(DISTINCT baad.analysis.id) DESC")
		}

		def q = query.generateSQL();
		//	println(q)
		return q;
	}
	/**
	 * retrieve trials with criteria
	 */
	def queryExperiment(SearchFilter filter, paramMap) {

		if(filter == null || filter.globalFilter.isTextOnly()){
			return new ExpAnalysisResultSet()
		}
		def time = System.currentTimeMillis();
		def elapseTimer  = new com.recomdata.util.ElapseTimer();

		def gfilter = filter.globalFilter

		def query =new ExperimentAssayAnalysisMVQuery(mainTableAlias:"baad")
		query.addTable("org.transmart.biomart.BioMarkerExpAnalysisMV baad")
	
		query.createGlobalFilterCriteria(gfilter);
		createSubFilterCriteria(filter.expAnalysisFilter,query);

			query.addSelect(" baad.experiment.id")
			query.addSelect(" COUNT(DISTINCT baad.analysis.id)")
			query.addGroupBy("baad.experiment.id")

		def q = query.generateSQL();
		def result =org.transmart.biomart.BioAssayAnalysisData.executeQuery(q, paramMap==null?[:]:paramMap)
		//println("exp query:"+(System.currentTimeMillis()-time))
		List expResult = []
		elapseTimer.logElapsed("query Experiment:",true)
		//	println(result.size())
		//def analysisCount = 0;
		//def expCount = 0;
		for(row in result){
			//analysisCount +=row[1];
			//expCount++;
			expResult.add(new ExperimentAnalysisResult(experiment:org.transmart.biomart.Experiment.get(row[0]), analysisCount:row[1], groupByExp:true))
		}
		return new ExpAnalysisResultSet(expAnalysisResults:expResult,groupByExp:true)
	}


	def createMVExperimentQuery(countType, SearchFilter filter){
		if(filter == null || filter.globalFilter.isTextOnly()){
			return " WHERE 1=0"
		}
		def gfilter = filter.globalFilter

		def query =new AssayAnalysisDataQuery(mainTableAlias:"baad")
		query.addTable("org.transmart.biomart.BioMarkerExpAnalysisMV baad")

		//query.createGlobalFilterCriteria(gfilter);
		query.createGlobalFilterCriteriaMV(gfilter);
		createSubFilterCriteria(filter.expAnalysisFilter,query);

		if("COUNT_EXP".equals(countType)){
			query.addSelect("COUNT(distinct baad.experiment.id) ");
		} else if ("COUNT_ANALYSIS".equals(countType)){
			//query.addTable("JOIN baad.markers baad_bm")
			query.addSelect("COUNT(distinct baad.analysis.id) ");
		} else if ("COUNT_ANALYSIS_TEA".equals(countType)){
			//	query.addTable("JOIN baad.markers baad_bm")
			query.addSelect("COUNT(DISTINCT baad.analysis.id) ");
			createNPVCondition(query)
		} else {
			query.setDistinct=true
			query.addSelect(" baad.experiment.id")
			query.addSelect(" COUNT(DISTINCT baad.analysis.id)")
			query.addGroupBy("baad.experiment.id")
			query.addOrderBy(" COUNT(DISTINCT baad.analysis.id) DESC")
		}

		def q = query.generateSQL();
		//	println(q)
		return q;
	}

	def createExperimentQuery(SearchFilter filter){
		if(filter == null || filter.globalFilter.isTextOnly()){
			return "SELECT 0,0 FROM DURAL WHERE 1=0"
		}
		def gfilter = filter.globalFilter

		def query =new AssayAnalysisDataQuery(mainTableAlias:"baad")
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad")
		query.addCondition(" baad.experiment.type='Experiment'")

		query.createGlobalFilterCriteria(gfilter);
		createSubFilterCriteria(filter.expAnalysisFilter,query);
		if("DISTINCT".equals(countType)){
			query.addSelect("")
		}
		else if("COUNT_EXP".equals(countType)){
			query.addSelect("COUNT(DISTINCT baad.experiment.id) ");
		} else if ("COUNT_ANALYSIS".equals(countType)){
			//query.addTable("JOIN baad.markers baad_bm")
			query.addSelect("COUNT(DISTINCT baad.analysis.id) ");
		}

		def q = query.generateSQL();
		//	println(q)
		return q;
	}

	/**
	 * fetch analysis detail for given experiment
	 */
	def queryAnalysis(expId, SearchFilter filter){

		def gfilter = filter.globalFilter
		def query = new AssayAnalysisDataQuery(mainTableAlias:"baad",setDistinct:true)
		//query.setDistinct=true;
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad")
		query.addTable("JOIN baad.featureGroup.markers baad_bm")
		query.addSelect("baad")
		query.addSelect("baad_bm")
		query.addCondition("baad.experiment.id = "+expId)
		// expand biomarkers
		query.createGlobalFilterCriteria(gfilter, true);
		createSubFilterCriteria(filter.expAnalysisFilter, query);
		// query.addOrderBy("abs(baad.foldChangeRatio) DESC ");           
		query.addOrderBy("baad.rValue DESC ");
		query.addOrderBy("baad.rhoValue DESC ");

		def sql = query.generateSQL();
		def result = null;
		def tResult = new ExperimentAnalysisResult(experiment:Experiment.get(expId))
		println("exe sql:"+sql)
		def stimer = new ElapseTimer();

		if(!gfilter.getBioMarkerFilters().isEmpty()){
			result = org.transmart.biomart.BioAssayAnalysisData.executeQuery(sql)
			stimer.logElapsed("Query Analysis with biomarker ",true);
			processAnalysisResult(result, tResult)
		}
		else {
			println("INFO: **** executing no biomarkers analysis query!!!!!")
			def allAnalysis = getAnalysesForExpriment(expId, filter);
			result=[]
			for(row in allAnalysis){
				def analysisId = row[0]
				def countGene = row[1]
				//result = getTopAnalysisDataForAnalysis(analysisId, 5);
				result = BioAssayAnalysis.getTopAnalysisDataForAnalysis(analysisId, 50);
				def analysisResult = new AnalysisResult(analysis:org.transmart.biomart.BioAssayAnalysis.get(analysisId),bioMarkerCount:countGene)
				tResult.analysisResultList.add(analysisResult)
				processAnalysisResultNoSort(result, analysisResult)
				stimer.logElapsed("Query Analysis without biomarker ",true);
			}
		}

		return tResult;
	}

	/**
	 *  get ananlysis only
	 */
	def getAnalysesForExpriment(clinicalTrialId, SearchFilter filter){
		// need both filters here
		def query = createBaseQuery(filter, filter.expAnalysisFilter);
		query.addSelect("baad.analysis.id")
		query.addTable("JOIN baad.featureGroup.markers baad_bm")
		query.addSelect("COUNT(DISTINCT baad_bm.id)")
		query.addCondition("baad.experiment.id ="+clinicalTrialId)
		query.addGroupBy("baad.analysis")
		query.addOrderBy("COUNT(DISTINCT baad_bm.id) DESC")
		return org.transmart.biomart.BioAssayAnalysisData.executeQuery(query.generateSQL());
	}

	def createBaseQuery(SearchFilter filter, subFilter){
		def gfilter = filter.globalFilter

		def query = new AssayAnalysisDataQuery(mainTableAlias:"baad", setDistinct:true)
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad")
		if(filter!=null)
			query.createGlobalFilterCriteria(gfilter, true);
		if(subFilter!=null)
			createSubFilterCriteria(subFilter, query);
		return query;
	}

	/**
	 * process analysis result
	 */
	def processAnalysisResultNoSort(List result,
	AnalysisResult  aresult){

		//def aresult = new AnalysisResult(analysis);
		for(row in result){
			def analysisData = row[0]
			def biomarker = row[1];
			aresult.assayAnalysisValueList.add(new AssayAnalysisValue(analysisData:analysisData, bioMarker:biomarker))

		}
	}

	/**
	 * process experiment analysis
	 */
	def processAnalysisResult(List result, ExperimentAnalysisResult tar)  {
		LinkedHashMap analysisResultMap = new LinkedHashMap()
		for(row in result){
			def analysisData = row[0]
			def biomarker = row[1];//org.transmart.biomart.BioMarker.get(row[1])
			//println(biomarker)
			def aresult =analysisResultMap.get(analysisData.analysis.id)
			if(aresult==null){
				org.transmart.biomart.BioAssayAnalysis analysisInst =org.transmart.biomart.BioAssayAnalysis.get(analysisData.analysis.id);
				aresult = new AnalysisResult(analysis:analysisInst)
				analysisResultMap.put(analysisData.analysis.id, aresult)
			}
			aresult.assayAnalysisValueList.add(new AssayAnalysisValue(analysisData:analysisData, bioMarker:biomarker))
		}

		def mc= [
					compare: {a,b->
						a.equals(b)? 0: (((double)a.size())/((double)a.analysis.dataCount))>(((double)b.size())/((double)b.analysis.dataCount))? -1: 1 }
				] as Comparator

		Collection allanalysis = analysisResultMap.values().sort(mc)
		tar.analysisResultList.addAll(allanalysis)
	}

	/**
	 * find experiment platforms
	 */
	def getPlatformsForExperment(expid){
		return org.transmart.biomart.BioAssayAnalysisData.executeQuery("SELECT DISTINCT baad.assayPlatform FROM org.transmart.biomart.BioAssayAnalysisData baad WHERE baad.experiment.id =?",expid);
	}


	def findPlatformOrganizmFilter(filter){
		def gfilter = filter.globalFilter
		def query = new AssayAnalysisDataQuery(mainTableAlias:"baad",setDistinct:true)
		def alias = query.mainTableAlias+"_dis"
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad");
		query.addSelect("baad.assayPlatform.organism")
		query.addCondition(query.mainTableAlias+".experiment.type='Experiment'")

		query.createGlobalFilterCriteria(gfilter, true);
		return org.transmart.biomart.BioAssayAnalysisData.executeQuery(query.generateSQL());	}

	/**
	 * load experiment type filter
	 */
	def findExperimentTypeFilter(){
		return org.transmart.biomart.Experiment.executeQuery("SELECT DISTINCT exp.type FROM org.transmart.biomart.Experiment exp WHERE exp.type IS NOT NULL");
	}

	/**
	 * load experiment design filter
	 */
	def findExperimentDesignFilter(filter){

		// def query = "SELECT DISTINCT bda.propertyValue FROM org.transmart.biomart.BioDataAttribute bda WHERE bda.propertyCode='Experiment Design' ORDER BY bda.propertyValue";
		// return org.transmart.biomart.BioDataAttribute.executeQuery();
		def gfilter = filter.globalFilter
		def query = new AssayAnalysisDataQuery(mainTableAlias:"baad",setDistinct:true)
		def alias = query.mainTableAlias+"_dis"
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad");
		query.addTable("org.transmart.biomart.BioDataAttribute bda")
		query.addCondition("baad.experiment.id = bda.bioDataId")
		query.addSelect("bda.propertyValue")
		query.addOrderBy("bda.propertyValue");
		query.addCondition(query.mainTableAlias+".experiment.type='Experiment'")
		query.addCondition("bda.propertyCode='Experiment Design'")
		query.addCondition("bda.propertyValue IS NOT NULL")
		query.createGlobalFilterCriteria(gfilter, true);
		// createSubFilterCriteria(filter.expAnalysisFilter, query);
		// println(query.generateSQL());
		return org.transmart.biomart.BioAssayAnalysisData.executeQuery(query.generateSQL());

		/*
		 return org.transmart.biomart.BioDataAttribute.executeQuery("SELECT DISTINCT bda.propertyValue " +
		 "FROM org.transmart.biomart.BioDataAttribute bda " +
		 "WHERE bda.propertyCode='Experiment Design' and bda.propertyValue is not null ORDER BY bda.propertyValue");
		 */
	}

	/**
	 * load disease filter
	 */
	def findExperimentDiseaseFilter(SearchFilter filter, experimentType){
		def gfilter = filter.globalFilter

		def query = new AssayAnalysisDataQuery(mainTableAlias:"baad",setDistinct:true)
		def alias = query.mainTableAlias+"_dis"
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad");
		query.addTable("JOIN "+query.mainTableAlias+".experiment.diseases "+alias)
		query.addSelect(alias)
		query.addOrderBy(alias+".preferredName");
		query.addCondition(query.mainTableAlias+".experiment.type='"+experimentType+"'")
		query.createGlobalFilterCriteria(gfilter, true);
		// createSubFilterCriteria(filter.expAnalysisFilter, query);
		// println(query.generateSQL());
		return org.transmart.biomart.BioAssayAnalysisData.executeQuery(query.generateSQL());
	}

	/**
	 * load platform filter
	 */
	def findExperimentPlatformFilter(SearchFilter filter, experimentType){
		def gfilter = filter.globalFilter

		def query = new AssayAnalysisDataQuery(mainTableAlias:"baad",setDistinct:true)
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad");
		query.addSelect("baad.assayPlatform.name")
		query.addOrderBy("baad.assayPlatform.name");
		query.addCondition(query.mainTableAlias+".experiment.type='"+experimentType+"'")
		query.createGlobalFilterCriteria(gfilter, true);
		// createSubFilterCriteria(filter.expAnalysisFilter, query);
		// println(query.generateSQL());
		return org.transmart.biomart.BioAssayAnalysisData.executeQuery(query.generateSQL());
	}

	/**
	 * TEA filter criteria for NPV
	 */
	def createNPVCondition(query){
		query.addCondition("baad.teaNormalizedPValue<=0.05")
	}

	// apply filter to main search results
	def createSubFilterCriteria(expfilter, Query query){
		// disease
		if(expfilter.filterDisease()){
			def alias = query.mainTableAlias+"_dis"
			query.addTable("JOIN "+query.mainTableAlias+".experiment.diseases "+alias)
			query.addCondition(alias+".id = "+expfilter.bioDiseaseId)
		}

		// compound
		if(expfilter.filterCompound()){
			def alias = query.mainTableAlias+"_cpd"
			query.addTable("JOIN "+query.mainTableAlias+".experiment.compounds "+alias)
			query.addCondition(alias+".id = "+expfilter.bioCompoundId)
		}

		// design
		if(expfilter.filterExpDesign()){
			def alias = query.mainTableAlias+".experiment"
			//query.addTable(alias+" exp")
			query.addTable("org.transmart.biomart.BioDataAttribute bda")
			query.addCondition(alias+".id = bda.bioDataId")
			query.addCondition("bda.propertyValue ='"+expfilter.expDesign+"'")
		}
		//		 platform species
		if(expfilter.filterSpecies()){
			//	def alias = query.mainTableAlias+".assayPlatform.organism"
			//query.addTable(alias+" exp")
			query.addTable("org.transmart.biomart.BioAssayAnalysisData baad_platform")
			query.addCondition(" baad_platform.assayPlatform.organism ='"+expfilter.species+"'")
		}
		// type
		//	if(expfilter.filterExpType()){
		//		def alias = query.mainTableAlias+".experiment"

		//	query.addTable("org.transmart.biomart.Experiment exp");
		//		query.addCondition(alias+".type='"+expfilter.expType+"'")
		//	}

		// fold change on BioAssayAnalysisData
		if(expfilter.filterFoldChange()){
			def StringBuilder s = new StringBuilder();
			query.addTable("org.transmart.biomart.BioAssayAnalysisData baad_foldChange")
			def symbol = "abs(baad_foldChange.foldChangeRatio)" // abs value
			s.append (" ((").append(symbol).append(" >=").append(expfilter.foldChange).append(" )")
					.append(" OR ").append(symbol).append(" IS NULL )");
			query.addCondition(s.toString())

			//s.append (" (").append(symbol).append(" >=").append(expfilter.foldChange)
			//		.append(" OR ").append(symbol).append(" <= ").append(-expfilter.foldChange)
			//		.append(" OR ").append(symbol).append(" IS NULL)")
		}

		// pvalue on BioAssayAnalysisData
		if(expfilter.filterPValue()){
			def StringBuilder s = new StringBuilder();
			query.addTable("org.transmart.biomart.BioAssayAnalysisData baad_pValue")
			def symbol = "baad_pValue.preferredPvalue"
			s.append(" (").append(symbol).append(" <= ").append(expfilter.pValue).append( ")")
			// .append(" OR ").append(symbol).append(" IS NULL)");
			query.addCondition(s.toString())
		}
	}

}
