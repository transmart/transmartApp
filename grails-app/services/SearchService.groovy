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
import groovy.time.*

/**
  * $Id: SearchService.groovy 11850 2012-01-24 16:41:12Z jliu $
  * 
  * @author $Author: jliu $
  * @version $Revision: 11850 $
  *
  */
public class SearchService{
	def literatureQueryService
	def experimentAnalysisQueryService
	def trialQueryService
	def documentService
	def expressionProfileQueryService
	def clinicalTrialAnalysisTEAService

	def doResultCount(SearchResult sResult, SearchFilter searchFilter){

		// Closure to measure the time performance
		def benchmark = { closure ->
			def start = new Date()
			closure.call()
			return TimeCategory.minus(new Date(), start)
		}
			
		def duration = benchmark {sResult.experimentCount = experimentAnalysisQueryService.countExperiment(searchFilter)}
		log.info("Expression Analysis Count Duration: ${duration}")
		duration = benchmark {sResult.trialCount = trialQueryService.countAnalysis(searchFilter)}
		log.info("Trial Count Duration: ${duration}")
		duration = benchmark {sResult.analysisCount = clinicalTrialAnalysisTEAService.queryExpAnalysisCount(searchFilter)}
		log.info("Analysis count and duration: ${sResult.analysisCount} and ${duration}")
		duration = benchmark {sResult.mRNAAnalysisCount = experimentAnalysisQueryService.countTEAAnalysis(searchFilter)}
		log.info("mRNA Analysis count and duration: ${sResult.mRNAAnalysisCount} and ${duration}")
		duration = benchmark {sResult.allAnalysiCount = experimentAnalysisQueryService.countAnalysis(searchFilter)}
		log.info("All Analysis count and duration: ${sResult.allAnalysiCount} and ${duration}")
		duration = benchmark {sResult.documentCount = documentService.documentCount(searchFilter)}
		log.info("Document Count Duration: ${duration}")
		duration = benchmark {sResult.profileCount = expressionProfileQueryService.countExperiment(searchFilter)}
		log.info("Profile Count Duration: ${duration}")
	}

	def createPagingParamMap(params, defaultmax, defaultoffset){
		def paramMap =[:]
		def max = params.max
		def offset = params.offset
		if(max==null && defaultmax!=null)
			max=defaultmax
		if(offset==null&&defaultoffset!=null)
			offset=defaultoffset
			// dynamic typing sucks here..
		if(max!=null)
			paramMap["max"]=Integer.valueOf(String.valueOf(max))
		if(offset!=null)
			paramMap["offset"]=Integer.valueOf(String.valueOf(offset))
		return paramMap;
	}
}