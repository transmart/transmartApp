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
  

import org.transmart.SearchFilter;
import org.transmart.TrialAnalysisResult;
import org.transmart.biomart.BioAssayAnalysis;
import org.transmart.biomart.BioAssayAnalysisData;

import org.transmart.biomart.BioMarker
import org.transmart.biomart.Compound
import org.transmart.biomart.Disease
import org.transmart.biomart.ClinicalTrial
import com.recomdata.search.query.AssayAnalysisDataQuery
import com.recomdata.search.query.Query


/**
 * $Id: ClinicalTrialAnalysisTEAService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class ClinicalTrialAnalysisTEAService extends AnalysisTEABaseService {

	def trialQueryService

	def getExpType(){
		return "Clinical Trial";
	}

	def createResultObject(){
		return new TrialAnalysisResult();
	}

	def createSubFilterCriteria(SearchFilter filter, Query query){
		return trialQueryService.createTrialFilterCriteria(filter.trialFilter, query);
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
		query.addTable("org.transmart.biomart.BioAssayAnalysisData baad ");
		query.addTable ("org.transmart.biomart.ClinicalTrial ct ");
		query.addCondition("baad.experiment.id = ct.id ")

		query.createGlobalFilterCriteria(gfilter);
		createSubFilterCriteria(filter, query);

		query.addSelect("baad.analysis.id")

		return query.generateSQL()
	}

}
