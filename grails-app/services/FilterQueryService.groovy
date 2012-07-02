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
  

import org.transmart.biomart.Disease
import org.transmart.biomart.Compound

import org.transmart.SearchFilter;
import org.transmart.biomart.BioAssayAnalysis;
import org.transmart.biomart.BioAssayAnalysisData;
import org.transmart.searchapp.SearchKeyword
import org.transmart.biomart.ClinicalTrial
import org.transmart.biomart.BioData
import org.transmart.biomart.Experiment
import com.recomdata.search.query.AssayAnalysisDataQuery

/**
 * $Id: FilterQueryService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */

class FilterQueryService {

	def trialDiseaseFilter(){
		return experimentDiseaseFilter("Clinical Trial")
	}

	def trialDiseaseFilter(SearchFilter filter){
		return findExperimentDiseaseFilter(filter, "Clinical Trial")
	}

	def trialCompoundFilter(SearchFilter filter){
		return findExperimentCompoundFilter(filter, "Clinical Trial")
	}

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
	//	println(query.generateSQL());


		return org.transmart.biomart.BioAssayAnalysisData.executeQuery(query.generateSQL());
	}

//	def experimentDiseaseFilter(String experimentType){
//		def query = "SELECT distinct sk FROM org.transmart.searchapp.SearchKeyword sk, org.transmart.biomart.Experiment exp JOIN exp.diseases ds "+
//		" WHERE sk.bioDataId = ds.id AND exp.type=? ORDER BY sk.keyword";
//		return org.transmart.searchapp.SearchKeyword.executeQuery(query, experimentType);
//	}

//	def experimentDiseaseFilterNew(String experimentType){
//		def query = "SELECT distinct sk FROM org.transmart.searchapp.SearchKeyword sk, org.transmart.biomart.Experiment exp JOIN exp.diseases ds "+
//		" WHERE sk.bioDataId = ds.id AND exp.type=? ORDER BY sk.keyword";
//		return org.transmart.searchapp.SearchKeyword.executeQuery(query, experimentType);
//}

	def experimentCompoundFilter(String experimentType){
		def query = "SELECT distinct sk FROM org.transmart.searchapp.SearchKeyword sk, org.transmart.biomart.Experiment exp JOIN exp.compounds cpd "+
		" WHERE sk.bioDataId = cpd.id AND exp.type=? ORDER BY sk.keyword";
		return org.transmart.searchapp.SearchKeyword.executeQuery(query, experimentType);
	}

	def findExperimentCompoundFilter(SearchFilter filter, experimentType){
		def gfilter = filter.globalFilter

		def query = new AssayAnalysisDataQuery(mainTableAlias:"baad",setDistinct:true)
		def alias = query.mainTableAlias+"_cpd"
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad");
		query.addTable("JOIN "+query.mainTableAlias+".experiment.compounds "+alias)
		query.addSelect(alias)
		query.addOrderBy(alias+".genericName");
		query.addCondition(query.mainTableAlias+".experiment.type='"+experimentType+"'")
		 query.createGlobalFilterCriteria(gfilter, true);

		// createSubFilterCriteria(filter.expAnalysisFilter, query);
	//	println(query.generateSQL());

		return org.transmart.biomart.BioAssayAnalysisData.executeQuery(query.generateSQL());
	}

	def studyTypeFilter(){
		def query = "SELECT distinct exp.studyType from org.transmart.biomart.ClinicalTrial exp WHERE exp.studyType IS NOT NULL ORDER BY exp.studyType"
		return org.transmart.biomart.ClinicalTrial.executeQuery(query)
	}

	def trialPhaseFilter(){
		def query = "SELECT distinct exp.studyPhase FROM org.transmart.biomart.ClinicalTrial exp WHERE exp.studyPhase IS NOT NULL ORDER BY exp.studyPhase"
		return org.transmart.biomart.ClinicalTrial.executeQuery(query)
	}

	def studyDesignFilter(String experimentType){
		return org.transmart.biomart.Experiment.executeQuery("SELECT DISTINCT exp.design FROM org.transmart.biomart.Experiment exp WHERE exp.type=? AND exp.design IS NOT NULL ORDER BY exp.design",experimentType);
	}

}
