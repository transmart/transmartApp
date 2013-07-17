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
  





import com.recomdata.search.query.AssayDataStatsQuery
import com.recomdata.search.query.AssayStatsExpMarkerQuery
import com.recomdata.search.query.Query
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.transmart.SearchFilter
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
	def countExperiment(SearchFilter filter){

		//println("profile:"+filter.globalFilter.isTextOnly());

		if(filter == null || filter.globalFilter.isEmpty()|| filter.globalFilter.isTextOnly()){
			return 0
		}

		return org.transmart.biomart.BioAssayStatsExpMarker.executeQuery(createCountQuery(filter))[0]
	}

	def createCountQuery(SearchFilter filter){
		if(filter == null || filter.globalFilter.isEmpty()|| filter.globalFilter.isTextOnly()){
			return " WHERE 1=0"
		}
		def gfilter = filter.globalFilter

		def query =new AssayStatsExpMarkerQuery(mainTableAlias:"asemq")
		query.addTable("org.transmart.biomart.BioAssayStatsExpMarker asemq")
		query.addCondition("asemq.experiment.type='Experiment'")

		query.createGlobalFilterCriteria(gfilter);
		createSubFilterCriteriaForMarker(filter.exprProfileFilter,query);
		query.addSelect("COUNT(DISTINCT asemq.experiment.id)")
		return query.generateSQL();
	}

	def queryStatisticsData(SearchFilter filter){

		if(filter == null || filter.globalFilter.isTextOnly()){
			return []
		}
		def query =new AssayDataStatsQuery(mainTableAlias:"bads",setDistinct:true)
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
		return org.transmart.biomart.BioAssayDataStatistics.executeQuery(q)
	}

	def queryStatisticsDataExpField(SearchFilter filter){
		def query =new AssayDataStatsQuery(mainTableAlias:"bads",setDistinct:true)
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
		return org.transmart.biomart.BioAssayDataStatistics.executeQuery(q, [max:500])
	}

	def listBioMarkers(SearchFilter filter){
		def query =new AssayStatsExpMarkerQuery(mainTableAlias:"asemq",setDistinct:true)
		query.addTable("org.transmart.biomart.BioAssayStatsExpMarker asemq")
		query.addCondition("asemq.experiment.type='Experiment'")
		//	query.addTable("JOIN FETCH asemq.marker asemq_bm")
		def gfilter = filter.globalFilter
		// expand biomarkers
		query.createGlobalFilterCriteria(gfilter, true);
		createSubFilterCriteriaForMarker(filter.exprProfileFilter, query)
		query.addSelect("asemq.marker")
		query.addOrderBy("asemq.marker.name")
		def config = ConfigurationHolder.config
        def result
        try {
            result = org.transmart.biomart.BioAssayStatsExpMarker.executeQuery(query.generateSQL(), [max:config.com.recomdata.search.gene.max])
        } catch (Exception e) {
            e.printStackTrace()
            result = new ArrayList()    // return empty ResultSet
        }
		return result
	}

	def listDiseases(SearchFilter filter){
		def query = new AssayDataStatsQuery(mainTableAlias:"bads",setDistinct:true)
		query.addTable("org.transmart.biomart.BioAssayDataStatistics bads")
		query.addCondition("bads.experiment.type='Experiment'")
		query.addTable("JOIN bads.experiment.diseases bads_dis")
		def gfilter = filter.globalFilter
		query.createGlobalFilterCriteria(gfilter);
		createSubFilterCriteria(filter.exprProfileFilter, query)
		query.addSelect("bads_dis")
		query.addOrderBy("bads_dis.preferredName")
		//println(">> disease query: " + query.generateSQL())
        def result
        try {
            result = org.transmart.biomart.BioAssayDataStatistics.executeQuery(query.generateSQL())
        } catch (Exception e) {
            e.printStackTrace()
            result = new ArrayList()    // return empty ResultSet
        }
        return result
	}

	/**
	 * get probesets filtered by marker (i.e. gene) and disease
	 */
	def getProbesetsByBioMarker(BioMarker marker, Disease disease){
		def query = "SELECT distinct bads.featureGroupName " +
                    "FROM org.transmart.biomart.BioAssayDataStatistics bads JOIN bads.featureGroup.markers bads_bm JOIN bads.experiment.diseases bads_dis " +
                    "WHERE bads_bm.id =:bmid and bads_dis.id=:disid";
		return org.transmart.biomart.BioAssayDataStatistics.executeQuery(query, [bmid:marker.id, disid:disease.id]);
	}

	def createSubFilterCriteria(exprfilter, Query query){
		// disease
		if(exprfilter.filterDisease()){
			def alias = query.mainTableAlias+"_dis"
			query.addTable("JOIN "+query.mainTableAlias+".experiment.diseases "+alias)
			query.addCondition(alias+".id = "+exprfilter.bioDiseaseId)
		}

		// biomarker
		if(exprfilter.filterBioMarker()){
			def alias = query.mainTableAlias+"_bm"
			query.addTable("JOIN "+query.mainTableAlias+".featureGroup.markers "+alias)
			query.addCondition(alias+".id = "+exprfilter.bioMarkerId)
		}

		// probeset
		if(exprfilter.filterProbeSet()){
			query.addCondition(query.mainTableAlias+".featureGroupName='" +exprfilter.probeSet+"'")
		}
	}

	def createSubFilterCriteriaForMarker(exprfilter, Query query){
		// disease
		if(exprfilter.filterDisease()){
			def alias = query.mainTableAlias+"_dis"
			query.addTable("JOIN "+query.mainTableAlias+".experiment.diseases "+alias)
			query.addCondition(alias+".id = "+exprfilter.bioDiseaseId)
		}

		// biomarker
		if(exprfilter.filterBioMarker()){
			def alias = query.mainTableAlias+".marker"
		//	query.addTable("JOIN "+query.mainTableAlias+".markers "+alias)
			query.addCondition(alias+".id = "+exprfilter.bioMarkerId)
		}

		// probeset
		//if(exprfilter.filterProbeSet()){
		//	query.addCondition(query.mainTableAlias+".featureGroupName='" +exprfilter.probeSet+"'")
		//}
	}

}
