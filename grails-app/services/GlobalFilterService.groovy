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
  

import com.recomdata.search.query.Query
 /**
  * $Id: GlobalFilterService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
  * @author $Author: mmcduffie $
  * @version $Revision: 9178 $
  *
  */
import org.codehaus.groovy.grails.commons.ConfigurationHolder

public class GlobalFilterService{

/**
 *
 */
	//def createGlobalFilterCriteria(GlobalFilter gfilter,
	//		Query query){
	//return createGlobalFilterCriteria(gfilter, query, false)

	//}

/**
 *
 */

/*	def createGlobalFilterCriteria(GlobalFilter gfilter,
			Query query,
			boolean expandBioMarkers){
		StringBuilder s = new StringBuilder();
		// biomarkers
		def biomarkerFilters = gfilter.getBioMarkerFilters()
		if(!biomarkerFilters.isEmpty()){
			def markerAlias = query.mainTableAlias+"_bm"
			def markerTable =query.mainTableAlias+".markers "+markerAlias;
			query.addTable(" JOIN "+markerTable)
			if(expandBioMarkers){
				query.addCondition(markerAlias+".id IN ("+createExpandBioMarkerSubQuery(biomarkerFilters.getKeywordDataIdString())+") ")
			}else{
			query.addCondition(markerAlias+".id IN ("+biomarkerFilters.getKeywordDataIdString()+") ")
			}
		}
		// disease
		if(!gfilter.getDiseaseFilters().isEmpty()){
			def dAlias = query.mainTableAlias+"_dis"
			def dtable =query.mainTableAlias+".diseases "+dAlias;
			query.addTable(" JOIN "+dtable)
			query.addCondition(dAlias+".id IN ("+gfilter.getDiseaseFilters().getKeywordDataIdString()+") ")
			}
		// compound
		if(!gfilter.getCompoundFilters().isEmpty()){
				def dAlias = query.mainTableAlias+"_cpd"
				def dtable =query.mainTableAlias+".compounds "+dAlias;

				query.addTable(" JOIN "+dtable)
			query.addCondition(dAlias+".id IN ("+gfilter.getCompoundFilters().getKeywordDataIdString()+") ")
		}
		// trials
		if(!gfilter.getTrialFilters().isEmpty()){
			if(query.allowExperimentFilter()){
				query.addCondtion(mainTableAlias+".experimentId IN ("+gfilter.getTrialFilters().getKeywordDataIdString()+")")
			}else {
				query.addCondition(" 1 = 0 ")
			}
		}

	}
	*/

/**
 *
 */

 /* def createExpandBioMarkerSubQuery(ids){
	 StringBuilder s = new StringBuilder()
	 s.append("SELECT DISTINCT marker.id FROM org.transmart.biomart.BioMarker marker ")
	 s.append(" LEFT JOIN marker.associatedCorrels marker_cor")
	 s.append(" WHERE marker_cor.bioDataId IN (").append(ids).append(")")
	 s.append (" AND marker_cor.correlationDescr.correlation='PATHWAY GENE'")
	 return s.toString()
	}
*/


	def createPagingParamMap(params){
		def paramMap =[:]
		def max = params.max
		def offset = params.offset
		if(max==null)
			max = ConfigurationHolder.config.com.recomdata.search.paginate.max
		if(offset==null)
			offset = 0
			// dynamic typing sucks here..
		if(max!=null)
			paramMap["max"]=Integer.valueOf(String.valueOf(max))
		if(offset!=null)
			paramMap["offset"]=Integer.valueOf(String.valueOf(offset))
		return paramMap;
	}

}

