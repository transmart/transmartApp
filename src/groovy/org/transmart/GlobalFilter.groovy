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
  

import org.transmart.searchapp.SearchKeyword
import grails.validation.Validateable
/**
 *
 * $Id: GlobalFilter.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
@Validateable
public class GlobalFilter{

	def CATEGORY_GENE = "GENE"
	def CATEGORY_PATHWAY= "PATHWAY"
	def CATEGORY_COMPOUND= "COMPOUND"
	def CATEGORY_DISEASE = "DISEASE"
	def CATEGORY_TRIAL = "TRIAL"
	def CATEGORY_TEXT = "TEXT"
	def CATEGORY_STUDY="STUDY"
	def CATEGORY_GENE_SIG="GENESIG"
	def CATEGORY_GENE_LIST="GENELIST"


	def categoryFilterMap = new LinkedHashMap()
	def selectedpathlist = []
	def processed = false;

	def isEmpty(){
		for (value in categoryFilterMap.values()) {
			if (!value.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	def isTextOnly() {
		def hasText = false;
		for (key in categoryFilterMap.keySet()) {
			if (!categoryFilterMap.get(key).isEmpty()) {
				if (key.equals(CATEGORY_TEXT)) {
					hasText = true;
				} else {
					return false;
				}
			}
		}
		return hasText;
	}

	def containsFilter(filter) {
		KeywordSet set = categoryFilterMap.get(filter.dataCategory)
		return set.contains(filter)
	}

	def getBioMarkerFilters(){
		def all = new KeywordSet()
		all.addAll(getGeneFilters());
		all.addAll(getPathwayFilters());
		all.addAll(getGeneSignatureFilters());
		all.addAll(getGeneListFilters());
		return all;
	}

	def hasAnyListFilters(){
		return !getPathwayFilters().isEmpty()||
			!getGeneSignatureFilters().isEmpty()||
			!getGeneListFilters().isEmpty();

	}

	def getGenePathwayFilters(){
		def all = new KeywordSet()
		all.addAll(getGeneFilters());
		all.addAll(getPathwayFilters());
			return all;
	}

	def getAllListFilters(){
		def all = new KeywordSet()
		//all.addAll(getGeneFilters());
		all.addAll(getPathwayFilters());
		all.addAll(getGeneSignatureFilters());
		all.addAll(getGeneListFilters());
		return all;
	}

	def getGeneFilters(){
		return findFiltersByCategory(CATEGORY_GENE)
	}

	def getGeneSignatureFilters(){
		return findFiltersByCategory(CATEGORY_GENE_SIG)
	}

	def getGeneListFilters(){
		return findFiltersByCategory(CATEGORY_GENE_LIST)
	}

	def getGeneSigListFilters(){
		def all = new KeywordSet()
		all.addAll(getGeneSignatureFilters());
		all.addAll(getGeneListFilters());
			return all;
	}
	def getPathwayFilters(){
		return findFiltersByCategory(CATEGORY_PATHWAY)
	}

	def getDiseaseFilters(){
		return findFiltersByCategory(CATEGORY_DISEASE)
	}

	def getTrialFilters(){
		return findFiltersByCategory(CATEGORY_TRIAL)
	}

	def getStudyFilters(){
		return findFiltersByCategory(CATEGORY_STUDY)
	}

	def getCompoundFilters(){
		return findFiltersByCategory(CATEGORY_COMPOUND)
	}

	def getTextFilters(){
		return findFiltersByCategory(CATEGORY_TEXT)
	}

	def getAllFilters() {
		KeywordSet filters = getGeneFilters()
		filters.addAll(getPathwayFilters())
		filters.addAll(getCompoundFilters())
		filters.addAll(getDiseaseFilters())
		filters.addAll(getTrialFilters())
		filters.addAll(getTextFilters())
		filters.addAll(getStudyFilters())
		filters.addAll(getGeneSignatureFilters())
		filters.addAll(getGeneListFilters())
		return filters
	}

	/**
	 * returns a list of keywords for given category or an empty list if not present
	 */
	def findFiltersByCategory(String category){
		def filters = categoryFilterMap.get(category);
		if(filters == null){
			filters = new KeywordSet();
			categoryFilterMap.put(category, filters)
		}
		return filters.clone();
	}

	// Returns list of keywords for keywordset. Useful for building "in" clauses or search terms.
	def formatKeywordList(KeywordSet set, String separator, String textQualifier, int maxLength) {
		String list = ""
		for (filter in set) {
			String s = ""
			if (list.length() > 0 && separator != null && separator.length() > 0) {
				s = separator
			}
			if (textQualifier != null && textQualifier.length() > 0) {
				s += textQualifier
			}
			s += filter.keyword
			if (textQualifier != null && textQualifier.length() > 0) {
				s += textQualifier
			}
			if (maxLength != null && list.length() + s.length() > maxLength) {
				break
			}
			list += s
		}
		return list.toString()
	}

	// Returns list of bioDataIds for specified category. Useful for building "in" clauses.
	def formatIdList(KeywordSet set, String separator) {
		def list = new StringBuilder()
		for (filter in set) {
			if (list.size() > 0 && separator != null && separator.length() > 0) {
				list.append(separator)
			}
			list.append(filter.bioDataId)
		}
		return list.toString()
	}


	def addKeywordFilter(org.transmart.searchapp.SearchKeyword keyword){
		def klist = categoryFilterMap.get(keyword.dataCategory)
		if(klist == null){
			// make sure no dup
			klist = new KeywordSet()
			categoryFilterMap.put(keyword.dataCategory, klist)
		}
		klist.add(keyword)

	}

	def removeKeywordFilter(org.transmart.searchapp.SearchKeyword keyword) {
		KeywordSet set = categoryFilterMap.get(keyword.dataCategory)
		if (set != null) {
			set.removeKeyword(keyword)
		}
	}

	def dumpFilters() {
		for (category in categoryFilterMap.keySet()) {
			print(category + ": ")
			def keywordset = categoryFilterMap.get(category)
			for (keyword in keywordset) {
				print(keyword.uniqueId + " ")
			}
			println()
		}
	}

	/**
	 * indicates if filter contains a pathway
	 */
	def hasPathway() {
		return getPathwayFilters().size()>0;			 
	}

}


