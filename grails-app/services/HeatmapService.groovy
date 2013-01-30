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
 * @author $Author: mmcduffie $
 * $Id: HeatmapService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @version $Revision: 9178 $
 */
 
import org.transmart.HeatmapDataValue;

import org.transmart.biomart.BioMarker
import com.recomdata.search.query.AssayAnalysisDataQuery

public class HeatmapService {
    def trialQueryService
	
	def createHeatMapData (
			sfilter,
			method,
			dataType,
			searchTopGene,
			searchGeneList,
			searchAnalysisIds) {

		if(searchAnalysisIds==null || searchAnalysisIds.isEmpty())	{
		    log.warn("Search analysis IDS are null, returning null")
		    return null
		}
		
		// TODO: Check heatmap filter to determine if it's 100 gene or not
		log.info("Find all bioMarkers to be used in heatmaps")
		log.info("Check for top gene")
		
		def resultList = null
		def total = 50
		if(searchTopGene){
		    log.info("Run top genes heatmap")
			resultList = findTopBioMarkers(sfilter, method, dataType, total,searchAnalysisIds)
			log.info("Total top genes:"+resultList.size())
		}
		else if(searchGeneList!=null && searchGeneList.size()>0){
		    log.info("Run data search")
			resultList = findHeatmapFilterBioMarker(sfilter, method, dataType, searchGeneList,searchAnalysisIds)
		}
		else{
		    log.info("Run global search")
		    resultList = findGlobalFilterBioMarker(sfilter, method, dataType)
		}

		if(resultList==null || resultList.isEmpty())	{
		    log.warn("Result list is empty from the search, returning null")
			return null
		}

		def markerList = []
		for(row in resultList) {
			markerList.add(row[0])
		}

		def dataQuery = new AssayAnalysisDataQuery(mainTableAlias:'baad',setDistinct:true)
		dataQuery.addTable("org.transmart.biomart.BioAssayAnalysisData baad")
		dataQuery.addTable("JOIN baad.featureGroup.markers baad_bm")
		dataQuery.addSelect("baad_bm.id, baad_bm.name, baad.analysis.id,baad.foldChangeRatio, baad.rValue, baad.rhoValue ")
		trialQueryService.createTrialFilterCriteria(sfilter.trialFilter, dataQuery)
		dataQuery.addCondition("baad.analysis.id IN (:analysisIds)")
		dataQuery.addCondition("baad_bm.id IN(:ids)")
		dataQuery.addOrderBy("baad_bm.name")

		if(method!=null)
			dataQuery.addCondition(" baad.analysis.analysisMethodCode = '"+method+"'")
		if(dataType!=null)
			dataQuery.addCondition(" baad.analysis.assayDataType = '"+dataType+"'")
		
		def tquery = dataQuery.generateSQL()
		log.debug(tquery)
		
		def dataList = org.transmart.biomart.BioAssayAnalysisData.executeQuery(tquery, ['analysisIds':searchAnalysisIds,'ids':markerList, max:2000])
		log.info("Total found: " + dataList.size())
		
		def dataMarkerMap = [:]
		def markerId =null
		def valueList = null
		def value = null
		for(data in dataList){
			value = new HeatmapDataValue(bioMarkerId:data[0],
					bioMarkerName:data[1],
					assayAnalysisId:data[2],
					foldChangeRatio:data[3],
					rValue:data[4],
					rhoValue:data[5])

			markerId = value.bioMarkerId
			valueList = dataMarkerMap.get(markerId)
			if(valueList ==null){
				valueList = []
				dataMarkerMap.put(markerId, valueList)
			}

			valueList.add(value)
		}

		def sortedDataList =[]
		for(marker in markerList){
			if(dataMarkerMap.containsKey(marker))
				sortedDataList.addAll(dataMarkerMap.get(marker))
		}
		return sortedDataList;
	}

	/**
	 * search by top genes
	 */
	def findTopBioMarkers(sfilter, method, dataType, total,searchAnalysisIds){

		def query = new AssayAnalysisDataQuery(mainTableAlias:'baad',setDistinct:true);
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad")
		query.addTable("JOIN baad.featureGroup.markers baad_bm")
		query.addSelect("baad_bm.id")
		query.addSelect("COUNT(DISTINCT baad.analysis.id) ")
		query.addCondition("baad.analysis.id IN (:ids)")
		trialQueryService.createTrialFilterCriteria(sfilter.trialFilter, query)
		query.addCondition("baad_bm.bioMarkerType='GENE'")
		
		if(method!=null)
			query.addCondition(" baad.analysis.analysisMethodCode = '"+method+"'")
		if(dataType!=null)
			query.addCondition(" baad.analysis.assayDataType = '"+dataType+"'")
		query.addGroupBy("baad_bm.id")
		query.addOrderBy("COUNT(DISTINCT baad.analysis.id) DESC")
		def q = query.generateSQL()
		log.debug(q)
		
		return org.transmart.biomart.BioAssayAnalysisData.executeQuery(q, [ids:searchAnalysisIds, max:total])
	}

	/**
	 * search by gene and pathways in the global filter
	 */
	def findGlobalFilterBioMarker(sfilter, method, dataType){

	    def query = new AssayAnalysisDataQuery(mainTableAlias:'baad',setDistinct:true);
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad")
		query.addTable("JOIN baad.featureGroup.markers baad_bm")
		query.addSelect("baad_bm.id")
		query.addSelect("COUNT(DISTINCT baad.analysis.id) ")
		query.createGlobalFilterCriteria(sfilter.globalFilter, true)

		if(method!=null)
			query.addCondition(" baad.analysis.analysisMethodCode = '"+method+"'")
		if(dataType!=null)
			query.addCondition(" baad.analysis.assayDataType = '"+dataType+"'")
		
		query.addGroupBy("baad_bm.id")
		query.addOrderBy("COUNT(DISTINCT baad.analysis.id) DESC")
		def q = query.generateSQL()		
		log.debug(q)
		
		return org.transmart.biomart.BioAssayAnalysisData.executeQuery(q, [max:100])
	}

	/**
	 * search by gene and pathways in the heatmap filter
	 */
	def findHeatmapFilterBioMarker(sfilter, method, dataType, geneIds,searchAnalysisIds){

		def query = new AssayAnalysisDataQuery(mainTableAlias:'baad',setDistinct:true);
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad")
		query.addTable("JOIN baad.featureGroup.markers baad_bm")
		query.addSelect("baad_bm.id")
		query.addSelect("COUNT(DISTINCT baad.analysis.id)")
	    query.addCondition("baad_bm.id IN(:ids)")
	   	query.addCondition("baad.analysis.id IN (:analysisIds)")
	
	   	if(method!=null)
			query.addCondition(" baad.analysis.analysisMethodCode = '"+method+"'")
		if(dataType!=null)
			query.addCondition(" baad.analysis.assayDataType = '"+dataType+"'")
		
		query.addGroupBy("baad_bm.id")
		query.addOrderBy("COUNT(DISTINCT baad.analysis.id) DESC")
		def q = query.generateSQL()
		log.debug(q)
		
		return org.transmart.biomart.BioAssayAnalysisData.executeQuery(q, ['ids':geneIds,'analysisIds':searchAnalysisIds,max:100])
	}
}
