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
  

 import java.io.File
 import java.io.FileReader
 import java.io.StringWriter
 import java.util.StringTokenizer
 import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.transmart.SearchFilter;

 import com.recomdata.search.DocumentQuery
import com.recomdata.search.DocumentHit

 /**
  * $Id: DocumentService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
  * @author $Author: mmcduffie $
  * @version $Revision: 9178 $
  */
class DocumentService {

	def searchKeywordService
	def globalFilterService
	def String index = ConfigurationHolder.config.com.recomdata.searchengine.index.toString()
	def DocumentQuery documentQuery = new DocumentQuery(index)

	def documentCount(SearchFilter sfilter) {

		LinkedHashMap<String, ArrayList<String> > terms = documentTerms(sfilter)
		LinkedHashMap<String, ArrayList<String> > filters = sfilter.documentFilter.getFilters()
		return documentQuery.searchCount(terms, filters);

	}

	def documentData(SearchFilter sfilter, params) {

		params = globalFilterService.createPagingParamMap(params)
		LinkedHashMap<String, ArrayList<String> > terms = documentTerms(sfilter)
		LinkedHashMap<String, ArrayList<String> > filters = sfilter.documentFilter.getFilters()
		DocumentHit[] documents = documentQuery.search(terms, filters, params.max, params.offset)
		def results = []
		if (documents != null) {
			for (document in documents) {
				results.add(document)
			}
		}
		return results

	}

	LinkedHashMap<String, ArrayList<String> > documentTerms(SearchFilter sfilter) {

		def gfilter = sfilter.globalFilter
		def geneFilters = gfilter.getGeneFilters()
		def pathwayIds = gfilter.formatIdList(gfilter.getAllListFilters(), ",")
		// If there are pathways, then get all genes in pathways and add them to the geneFilters (hash set)
		if (pathwayIds.size() > 0) {
			geneFilters.addAll(searchKeywordService.expandAllListToGenes(pathwayIds))
		}
		def compoundFilters = gfilter.getCompoundFilters()
		def diseaseFilters = gfilter.getDiseaseFilters()
		def trialFilters = gfilter.getTrialFilters()
		def textFilters = gfilter.getTextFilters()

		LinkedHashMap<String, ArrayList<String> > terms = new LinkedHashMap<String, ArrayList<String> >()

		int termCount = 0;
		if (geneFilters.size() > 0) {
			def list = getTermList(geneFilters)
			termCount += list.size()
			if (termCount < DocumentQuery.MAX_CLAUSE_COUNT) {
				terms.put(gfilter.CATEGORY_GENE, list)
			}
		}
		if (compoundFilters.size() > 0) {
			def list = getTermList(compoundFilters)
			termCount += list.size()
			if (termCount < DocumentQuery.MAX_CLAUSE_COUNT) {
				terms.put(gfilter.CATEGORY_COMPOUND, list)
			}
		}
		if (diseaseFilters.size() > 0) {
			def list = getTermList(diseaseFilters)
			termCount += list.size()
			if (termCount < DocumentQuery.MAX_CLAUSE_COUNT) {
				terms.put(gfilter.CATEGORY_DISEASE, list)
			}
		}
		if (trialFilters.size() > 0) {
			def list = getTermList(trialFilters)
			termCount += list.size()
			if (termCount < DocumentQuery.MAX_CLAUSE_COUNT) {
				terms.put(gfilter.CATEGORY_TRIAL, list)
			}
		}
		if (textFilters.size() > 0) {
			def list = getTermList(textFilters)
			termCount += list.size()
			if (termCount < DocumentQuery.MAX_CLAUSE_COUNT) {
				terms.put(gfilter.CATEGORY_TEXT, list)
			}
		}

		return terms

	}

	ArrayList<String> getTermList(keywords) {

		ArrayList<String> terms = new ArrayList<String>()
		for (keyword in keywords) {
			if (terms.size() < DocumentQuery.MAX_CLAUSE_COUNT - 1) {
				terms.add(keyword.keyword)
			} else {
				break
			}
		}
		return terms

	}

	// Encode string value for display on HMTL page and encode out-of-band characters.
	String encodeHTML(String value) {

		if (value == null) {
			return ""
		}
		value = value.replace("<span class=\"search-term\">", "???HIT_OPEN???")
		value = value.replace("</span>", "???HIT_CLOSE???")
		value = value.encodeAsHTML()
		value = value.replace("???HIT_OPEN???", "<span class=\"search-term\">")
		value = value.replace("???HIT_CLOSE???", "</span>")

		def StringBuilder result = new StringBuilder()

		if (value.length() > 0) {
			def len = value.length() - 1
			for (i in 0..len) {
				def int ch = value.charAt(i)
				if (ch < 32) {
					result.append(' ')
				} else if (ch >= 128) {
					result.append("&#")
					result.append(ch)
				} else {
					result.append((char)ch)
				}
			}
		}

		return result.toString()

	}

 }
