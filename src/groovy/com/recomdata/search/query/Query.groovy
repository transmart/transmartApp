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
  

package com.recomdata.search.query

import org.transmart.GlobalFilter;

/**
 * $Id: Query.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 *$Revision: 9178 $
 */
public class Query {

	def setDistinct
	String mainTableAlias

	LinkedHashSet selectClause = new LinkedHashSet()
	LinkedHashSet fromClause = new LinkedHashSet()
	LinkedHashSet whereClause = new LinkedHashSet()
	LinkedHashSet groupbyClause = new LinkedHashSet()
	LinkedHashSet orderbyClause = new LinkedHashSet()


	def addSelect(String columnWithAlias){
		selectClause.add(columnWithAlias.trim())
	}
	def addTable(String tableWithAlias){
		fromClause.add(tableWithAlias.trim())
	}
	def addCondition(String condition){
		whereClause.add(condition.trim())
	}
	def addGroupBy(String groupby){
		groupbyClause.add(groupby.trim())
	}
	def addOrderBy(String orderby){
		orderbyClause.add(orderby.trim())
	}

	/**
	 * create criteria based on globalfilter objects
	 */
	def createGlobalFilterCriteria(GlobalFilter gfilter){
		return createGlobalFilterCriteria(gfilter, true)
	}


	/**
	 * create criteria based on globalfilter objects
	 */
	def createGlobalFilterCriteria(GlobalFilter gfilter, boolean expandBioMarkers){

		// biomarkers
		buildGlobalFilterBioMarkerCriteria(gfilter, expandBioMarkers)

		// disease
		buildGlobalFilterDiseaseCriteria(gfilter)
		// compound
		buildGlobalFilterCompoundCriteria(gfilter)
		// trials
		// by default not all query handles trials
		buildGlobalFilterExperimentCriteria(gfilter)
		// free text -
		buildGlobalFilterFreeTextCriteria(gfilter)

		// gene signature

		// studies
		buildGlobalFilterStudyCriteria(gfilter)
	}

	def createGlobalFilterCriteriaMV(GlobalFilter gfilter){

		// biomarkers
		buildGlobalFilterBioMarkerCriteriaMV(gfilter)

		// disease
		buildGlobalFilterDiseaseCriteria(gfilter)
		// compound
		buildGlobalFilterCompoundCriteria(gfilter)
		// trials
		// by default not all query handles trials
		buildGlobalFilterExperimentCriteria(gfilter)
		// free text -
		buildGlobalFilterFreeTextCriteria(gfilter)

		// gene signature

		// studies
		buildGlobalFilterStudyCriteria(gfilter)
	}

	/**
	 * default criteria builder for biomarkers
	 */
	def buildGlobalFilterBioMarkerCriteria(GlobalFilter gfilter,
	boolean expandBioMarkers){
		def biomarkerFilters = gfilter.getBioMarkerFilters()

		if(!biomarkerFilters.isEmpty()){
			def markerAlias = mainTableAlias+"_bm"
			def markerTable =getBioMarkerTable()+markerAlias;
			addTable("JOIN "+markerTable)
			if(expandBioMarkers){
				addCondition(createExpandBioMarkerCondition(markerAlias,gfilter ));
				//	addCondition(markerAlias+".id IN ("+createExpandBioMarkerSubQuery(biomarkerFilters.getKeywordDataIdString())+") ")

			}else{
				addCondition(markerAlias+".id IN ("+biomarkerFilters.getKeywordDataIdString()+") ")
			}
		}
	}


	def buildGlobalFilterBioMarkerCriteriaMV(GlobalFilter gfilter){

		def biomarkerFilters = gfilter.getBioMarkerFilters()

		if(!biomarkerFilters.isEmpty()){
			addCondition(createExpandBioMarkerConditionMV(mainTableAlias, gfilter));
		}
	}


	/**
	 * create biomarker table alias
	 */
	def String getBioMarkerTable(){
		return mainTableAlias+".markers ";
	}
	/**
	 * default criteria builder for disease
	 */
	def buildGlobalFilterDiseaseCriteria(GlobalFilter gfilter){
		if(!gfilter.getDiseaseFilters().isEmpty()){
			def dAlias = mainTableAlias+"_dis"
			def dtable =mainTableAlias+".diseases "+dAlias;
			addTable("JOIN "+dtable)
			addCondition(dAlias+".id IN ("+gfilter.getDiseaseFilters().getKeywordDataIdString()+") ")
		}
	}

	def buildGlobalFilterFreeTextCriteria(GlobalFilter gfilter){
		if(gfilter.isTextOnly()){
			addCondition(" 1 = 0")
		}
	}


	/**
	 * default criteria builder for compound
	 */

	def buildGlobalFilterCompoundCriteria(GlobalFilter gfilter){
		if(!gfilter.getCompoundFilters().isEmpty()){
			def dAlias = mainTableAlias+"_cpd"
			def dtable =mainTableAlias+".compounds "+dAlias;
			addTable("JOIN "+dtable)
			addCondition(dAlias+".id IN ("+gfilter.getCompoundFilters().getKeywordDataIdString()+") ")
		}
	}

	/**
	 * default criteria builder for experiment
	 */
	def buildGlobalFilterExperimentCriteria(GlobalFilter gfilter){
	}

	/**
	 * default criteria builder for study
	 */
	def buildGlobalFilterStudyCriteria(GlobalFilter gfilter){
	}

	/**
	 * generate a Hibernate Query from this query object
	 */
	def generateSQL(){
		StringBuilder s = new StringBuilder("SELECT ")
		if (setDistinct) {
			s.append (" DISTINCT ")
		}
		s.append(createClause(selectClause, ", ", null))
		s.append(" FROM ")
		// create from clause but don't put a separator if JOIN presents
		s.append(createClause(fromClause, ", ", "JOIN"))
		if(!whereClause.isEmpty()){
			s.append(" WHERE ")
			s.append(createClause(whereClause, " AND ", null))
		}
		if(!groupbyClause.isEmpty()){
			s.append(" GROUP BY ")
			s.append(createClause(groupbyClause, ", ", null))
		}
		if(!orderbyClause.isEmpty()){
			s.append(" ORDER BY ")
			s.append(createClause(orderbyClause, ", ", null))
		}
		return s.toString()
	}


	/**
	 * create clause
	 */
	def createClause(LinkedHashSet clause, String separator, String ignoreSepString){

		StringBuilder s = new StringBuilder()
		for(sc in clause){
			if(sc.length()>0){
				if((ignoreSepString==null) || (ignoreSepString!=null && !sc.trim().startsWith(ignoreSepString))){
					if(s.length()>0){
						s.append(separator)
					}
				}

				s.append(" ").append(sc);
			}
		}
		return s.toString()
	}

	def createExpandBioMarkerSubQuery(ids){

		StringBuilder s = new StringBuilder();
		s.append("SELECT DISTINCT bdc.associatedBioDataId FROM org.transmart.biomart.BioDataCorrelation bdc ");
		s.append(" WHERE bdc.bioDataId in (").append(ids).append(")");
		// s.append("SELECT DISTINCT marker.id FROM org.transmart.biomart.BioMarker marker ")
		// s.append(" LEFT JOIN marker.associatedCorrels marker_cor")
		// s.append(" WHERE marker_cor.bioDataId IN (").append(ids).append(")")
		//s.append (" AND marker_cor.correlationDescr.correlation='PATHWAY GENE'")
		return s.toString()
	}

	/**
	 * link biomarkers to those defined in the materialized views which exposes domain objects to search
	 */
	def createExpandBioMarkerCondition(String markerAlias, GlobalFilter gfilter){

		/*
		 // query to use if only using 1 MV from searchapp
		 s.append(markerAlias).append(".id IN (")
		 s.append("SELECT DISTINCT sbmcmv.assocBioMarkerId FROM org.transmart.searchapp.SearchBioMarkerCorrelFastMV sbmcmv ");
		 s.append(" WHERE sbmcmv.domainObjectId in (").append(ids).append(")");
		 */

		// aggregate ids from both static and refresh MVs
		StringBuilder s = new StringBuilder();
		s.append("(");
		if(!gfilter.getGeneSigListFilters().isEmpty()){
			s.append(markerAlias).append(".id IN (")
			s.append("SELECT DISTINCT sbmcmv.assocBioMarkerId FROM org.transmart.searchapp.SearchBioMarkerCorrelFastMV sbmcmv ");
			s.append(" WHERE sbmcmv.domainObjectId in (").append(gfilter.getGeneSigListFilters().getKeywordDataIdString()).append("))");
		}
		if(!gfilter.getGenePathwayFilters().isEmpty()){
			if(s.length()>1){
				s.append(" OR ");
			}
			s.append(markerAlias).append(".id IN (")
			s.append("SELECT DISTINCT bmcmv.assoBioMarkerId FROM org.transmart.biomart.BioMarkerCorrelationMV bmcmv ");
			s.append(" WHERE bmcmv.bioMarkerId in (").append(gfilter.getGenePathwayFilters().getKeywordDataIdString()).append(")) ");
		}
		s.append(")");
		return s.toString()
	}

	
	def createExpandBioMarkerConditionMV(String markerAlias, GlobalFilter gfilter){

		// aggregate ids from both static and refresh MVs
		StringBuilder s = new StringBuilder();
		s.append("(");
		if(!gfilter.getGeneSigListFilters().isEmpty()){
			s.append(markerAlias).append(".id IN (")
			s.append("SELECT DISTINCT sbmcmv.assocBioMarkerId FROM org.transmart.searchapp.SearchBioMarkerCorrelFastMV sbmcmv ");
			s.append(" WHERE sbmcmv.domainObjectId in (").append(gfilter.getGeneSigListFilters().getKeywordDataIdString()).append("))");
		}
		if(!gfilter.getGenePathwayFilters().isEmpty()){
			if(s.length()>1){
				s.append(" OR ");
			}
			s.append(markerAlias).append(".id IN (")
			s.append("SELECT DISTINCT bmcmv.assoBioMarkerId FROM org.transmart.biomart.BioMarkerCorrelationMV bmcmv ");
			s.append(" WHERE bmcmv.bioMarkerId in (").append(gfilter.getGenePathwayFilters().getKeywordDataIdString()).append(")) ");
		}
		s.append(")");
		return s.toString()
	}

	
	String toString(){
		return generateSQL();
	}
}
