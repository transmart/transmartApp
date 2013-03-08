package org.transmart
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
 * $Id: SearchResult.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class SearchResult {

	def countmap =[:]
	
	// trial tab 
	int analysisCount = 0
	int trialCount = 0
	
	// mRNA tab
	int mRNAAnalysisCount = 0
	int experimentCount = 0
	int allAnalysiCount = 0
	
	// Cortellis tab - currently not used
	int cortellisCount = 0
	
	int documentCount = 0
	int litJubOncAltCount = 0
	int litJubOncInhCount = 0
	int litJubOncIntCount = 0
	int litJubAsthmaAltCount = 0
	int litJubAsthmaInhCount = 0
	int litJubAsthmaIntCount = 0
	int litJubAsthmaPECount = 0
	int resultCount = 0
	int profileCount = 0
	def summary
	def result
	String resultType

	def totalCount = {
		return experimentCount + literatureCount() + trialCount + documentCount+ profileCount + cortellisCount
	}

	def litJubOncCount = {
		return litJubOncAltCount + litJubOncInhCount + litJubOncIntCount
	}

	def litJubAsthmaCount = {
		return litJubAsthmaAltCount + litJubAsthmaInhCount + litJubAsthmaIntCount + litJubAsthmaPECount
	}

	def literatureCount = {
		return litJubOncCount() + litJubAsthmaCount()
	}

}