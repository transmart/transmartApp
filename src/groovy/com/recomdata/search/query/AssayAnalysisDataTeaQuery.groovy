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
  


package  com.recomdata.search.query

import org.transmart.GlobalFilter


/**
 * @author $Author: mmcduffie $
 * $Id: AssayAnalysisDataTeaQuery.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * $Version$
 *
 */
public class AssayAnalysisDataTeaQuery extends Query{

	/**
	 * create biomarker table alias
	 */
	def String getBioMarkerTable(){
		return mainTableAlias+".featureGroup.markers ";
	}
	/**
	 *  criteria builder for disease,override default
	 */
	 def buildGlobalFilterDiseaseCriteria(GlobalFilter gfilter){
			if(!gfilter.getDiseaseFilters().isEmpty()){
				def dAlias = mainTableAlias+"_dis"
				def dtable =mainTableAlias+".experiment.diseases "+dAlias;
				addTable("JOIN "+dtable)
				addCondition(dAlias+".id IN ("+gfilter.getDiseaseFilters().getKeywordDataIdString()+") ")
				}

	 }

		/**
		 *  criteria builder for compound,override default
		 */

	  def buildGlobalFilterCompoundCriteria(GlobalFilter gfilter){
			if(!gfilter.getCompoundFilters().isEmpty()){
				def dAlias = mainTableAlias+"_cpd"
				def dtable =mainTableAlias+".experiment.compounds "+dAlias;
				addTable("JOIN "+dtable)
				addCondition(dAlias+".id IN ("+gfilter.getCompoundFilters().getKeywordDataIdString()+") ")
		}

	  }


		/**
		 *  criteria builder for experiment,override default
		 */

		 def buildGlobalFilterExperimentCriteria(GlobalFilter gfilter){
				if(!gfilter.getTrialFilters().isEmpty()){
				addCondition(mainTableAlias+".experiment.id IN ("+gfilter.getTrialFilters().getKeywordDataIdString()+")")
			}
				if(!gfilter.getStudyFilters().isEmpty()){
					addCondition(mainTableAlias+".experiment.id IN ("+gfilter.getStudyFilters().getKeywordDataIdString()+")")
				}
		 }




}
