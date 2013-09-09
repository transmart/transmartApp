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
* $Id: SearchController.groovy 10098 2011-10-19 18:39:32Z mmcduffie $
* @author $Author: mmcduffie $
* @version $Revision: 10098 $
*
*/

import grails.converters.*

import org.hibernate.*
import org.transmart.GlobalFilter;
import org.transmart.SearchFilter;
import org.transmart.SearchResult;
import org.transmart.biomart.BioDataExternalCode;
import org.transmart.searchapp.AccessLog;
import org.transmart.searchapp.AuthUser;

import org.transmart.searchapp.CustomFilter
import org.transmart.searchapp.SearchKeyword
import org.transmart.searchapp.SearchKeywordTerm

import com.recomdata.util.*

public class SearchController{
	def sessionFactory
	def springSecurityService
	def experimentAnalysisQueryService
	def literatureQueryService
	def TrialQueryService
	def SearchService
	def documentService
	def searchKeywordService
	String authKeyGG = null
	
	def SEARCH_DELIMITER='SEARCHDELIMITER'

	def index = {
		session.setAttribute('searchFilter', new SearchFilter())
	}

	
	def loadSearchAnalysis = {
			def value = params.query.toUpperCase()
			params.query = 'gene'+SEARCH_DELIMITER+'pathway'+SEARCH_DELIMITER+'genelist'+SEARCH_DELIMITER+'genesig:'+params.query
			loadSearch()
	}
	/**
	 * find top 20 biomarkers that exist in the keyword table  with case insensitive LIKE match
	 */
	def loadSearch = {
		def category
		def values
		if (params.query.indexOf(":") != -1) {
			category = params.query.substring(0, params.query.indexOf(":")).toUpperCase().replace("-", " ")
			values = params.query.substring(params.query.indexOf(":") + 1).toUpperCase()
		} else {
			category = "ALL"
			values = params.query.toUpperCase()
		}

		def keywords = new LinkedHashSet()
		// don't execute query if category is All and the term is empty
		if (!("ALL".equals(category) && values.length() == 0)) {
			def queryStr = "SELECT distinct t.searchKeyword, t.keywordTerm, t.rank, t.termLength FROM org.transmart.searchapp.SearchKeywordTerm t WHERE t.keywordTerm LIKE :term || '%' "
			def queryParams = ["term":values]
			// filter by category if specified
			if (!"ALL".equals(category)) {
				queryStr += " AND t.searchKeyword.dataCategory IN (:category) "
				queryParams["category"] = category.toString().split(SEARCH_DELIMITER)
			}
			// this is generic way to access AuthUser
			def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
			// permission to view search keyword (Admin gets all)
			if (!user.isAdmin()) {
				queryStr += " AND (t.ownerAuthUserId = :uid OR t.ownerAuthUserId IS NULL)"
				queryParams["uid"] = user.id
			}
			// order by rank/term, if no term specified. otherwise, order by rank/length/term so short terms are matched first.
			if (values.length() == 0) {
				queryStr += " ORDER BY t.rank ASC, t.keywordTerm"
			} else {
				queryStr += " ORDER BY t.rank ASC, t.termLength ASC, t.keywordTerm"
			}
			def keywordResults = SearchKeywordTerm.executeQuery(queryStr, queryParams, [max:20])
			for(k in keywordResults){
				keywords.add(k[0])
			}
		}
	//	log.info "keywords size:"+keywords.size()

		renderSearchKeywords(keywords)
	}

	def loadCategories = {

		def categories = SearchKeyword.executeQuery("select distinct k.dataCategory as value, k.displayDataCategory as label from org.transmart.searchapp.SearchKeyword k order by k.dataCategory")
		def rows = []
		rows.add([value: "all", label:"all"])
		for (category in categories) {
			def row = [:]
			if(category[0].equalsIgnoreCase("study")){
				row.value="study";
				row.label="geo/ebi";
			}else{
				row.value = category[0].toLowerCase().replace(" ", "-")
				if (category[1] != null)	{
				    row.label = category[1].toLowerCase()
				}
			}
			rows.add(row)
		}
		def result = [rows:rows]
        render(text:params.callback + "(" + (result as JSON) + ")", contentType:"application/javascript")
	}

	// Used by EditFiltersWindow to load records for list of global filters.
	def loadCurrentFilters = {

		def filters = session.searchFilter.globalFilter.getAllFilters()
		log.info "SearchController.loadCurrentFilters() count = " + filters?.size()
		renderSearchKeywords(filters)

	}

	// Used by EditFiltersWindow to split a pathway.
	def loadPathwayFilters = {

		def genes = []
		if (params.id != null && params.id.length() > 0) {
			def keyword = getSearchKeyword(params.id)
			genes = searchKeywordService.expandPathwayToGenes(keyword.bioDataId.toString())
			//			def query = "select k from org.transmart.searchapp.SearchKeyword k, org.transmart.biomart.BioDataCorrelation c where k.bioDataId=c.associatedBioDataId and c.bioDataId=?"
			//			genes = org.transmart.searchapp.SearchKeyword.executeQuery(query, keyword.bioDataId)
		}
		renderSearchKeywords(genes)
	}


	/**
	 * load pathway and gene for heatmap
	 */
	def loadHeatMapFilterAJAX = {

		def values = params.query
		if (values != null)
		values = values.replace("-", "").toUpperCase()

		//def user=authenticateService.userDomain();  // Using form login and GrailsUserImpl domain object
		//if (user == null)	{
		//	user=authenticateService.principal();   // Using Identity Vault and WWIDPrinicpal
		//}

		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		def uid = user.id;

		//	def queryStr = "SELECT distinct k FROM org.transmart.searchapp.SearchKeyword k left join k.externalCodes c WHERE k.dataCategory IN ('GENE', 'PATHWAY') AND (UPPER(k.keyword) LIKE '"+values+"%' OR (c.codeType='SYNONYM' AND UPPER(c.code) LIKE '"+values+"%')) ORDER BY LENGTH(k.keyword), k.keyword";
		StringBuffer qBuf = new StringBuffer();
		qBuf.append("SELECT distinct t.searchKeyword, t.keywordTerm, t.rank, t.termLength ");
		qBuf.append("FROM org.transmart.searchapp.SearchKeywordTerm t ");
		qBuf.append("WHERE t.searchKeyword.dataCategory IN ('GENE', 'PATHWAY', 'GENESIG','GENELIST') AND t.keywordTerm LIKE'"+values+"%' ");
        qBuf.append(" AND (t.ownerAuthUserId ="+ uid+" OR t.ownerAuthUserId IS NULL) ORDER BY t.rank ASC, t.termLength ASC, t.keywordTerm");
		def keywordResults = SearchKeywordTerm.executeQuery(qBuf.toString(), [max:20])

		def keywords = new LinkedHashSet()
		for(k in keywordResults){
			keywords.add(k[0])
		}		//log.info "keywords:"+keywords.size()
		renderSearchKeywords(keywords)
	}

	/**
	 * render keywords json object
	 */
	def renderSearchKeywords(keywords){

		def itemlist =[]
		def dataIds = []
		for(keyword in keywords){
			if (keyword.dataCategory != "TEXT") {
				dataIds.add(keyword.bioDataId)
			}
		}
		def allSynonyms
		if (dataIds?.size() > 0) {
			allSynonyms = BioDataExternalCode.executeQuery("SELECT DISTINCT bdec FROM org.transmart.biomart.BioDataExternalCode bdec WHERE bdec.bioDataId IN(:ids) AND bdec.codeType='SYNONYM'",[ids:dataIds])
		}
		def synMap =[:]
		def synList = null
		for(syn in allSynonyms){
			synList = synMap.get(syn.bioDataId)
			if(synList==null){
				synList = []
				synMap.put(syn.bioDataId, synList)
			}
			synList.add(syn)
		}

		for(keyword in keywords){
			if(keyword.dataCategory !="TEXT"){
				def synonyms =  synMap.get(keyword.bioDataId)
				def syntext = formatSynonyms(synonyms)
				def category = keyword.dataCategory;
				def display = keyword.displayDataCategory;
				def ssource = keyword.dataSource;
				if (ssource!=null && ssource.length()>0){
					ssource = ssource+">"
				} else {
					ssource = "";
				}
				itemlist.add([id:keyword.id, source:ssource, keyword:keyword.keyword, synonyms:syntext, category:category, display:display])
				//				log.info "new Keyword(id:" + keyword.bioDataId + ", source:'" + ssource + "', keyword:'" + keyword.keyword +
				//						"', synonyms:'" + syntext + "', category:'" + category + "', display:'" + display + "').save()"
			} else {
				itemlist.add([id:keyword.id, source:"", keyword:keyword.keyword, synonyms:"", category:"TEXT", display:"Text"])
			}
		}
		def result = [rows:itemlist]
        render(text:params.callback + "(" + (result as JSON) + ")", contentType:"application/javascript")

	}

	def doSearch = {

		def filter = session.searchFilter;
		def sResult = new SearchResult()

		log.info "doSearch:"+params
		log.info "isTextOnly = " + filter.globalFilter.isTextOnly()

		SearchService.doResultCount(sResult,filter)
		filter.summaryWithLinks = createSummaryWithLinks(filter)
		filter.createPictorTerms()
		boolean defaultSet = false;

		if (sResult.trialCount>0) {
			session.searchFilter.datasource="trial" ;
			defaultSet = true;
		} else if (!defaultSet && sResult.experimentCount>0) {
			session.searchFilter.datasource="experiment"
			defaultSet = true;
		}
		else if (!defaultSet && sResult.profileCount>0) {
			session.searchFilter.datasource="profile"
			defaultSet = true;
		} else if(!defaultSet && sResult.literatureCount()>0){
			session.searchFilter.datasource="literature"
			defaultSet = true;
		} else if(!defaultSet && sResult.documentCount>0){
			session.searchFilter.datasource="document"
			defaultSet = true;
		} else {
			session.searchFilter.datasource="document"
		}

		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		def al = new AccessLog(username: user.username, event:"Search", eventmessage:session.searchFilter.marshal(), accesstime:new Date())
		al.save();

		render(view:'list',model:[searchresult:sResult,page:false])
	}

	/**
	 * conduct a search, params expected or keyword id or keyword text
	 */
	def search = {
		def keyword
		log.info "search: "+params

		if (params.id != null && params.id.length() > 0) {
			keyword = getSearchKeyword(params.id)
		}

		if (keyword != null) {
			session.searchFilter = new SearchFilter()
			createUpdateSessionFilter(keyword)
			session.searchFilter.searchText = keyword.keyword
			redirect(action:'doSearch')
		} else {
			redirect(action:'index')
		}

	}

	def newSearch = {
		session.searchFilter = new SearchFilter()
		redirect(action:'search',params:params)
	}

	def searchCustomFilter = {

		def sfilter = new SearchFilter()
		def gfilter = sfilter.globalFilter
		def customFilter = CustomFilter.get( params.id )
		def redirected = false

		log.info "searchCustomFilter: '${customFilter}'"
		if (customFilter != null) {
			def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
			if (customFilter.privateFlag != 'Y' || customFilter.searchUserId == user.id) {
				def uniqueIds = []
				for (item in customFilter.items) {
					def id = item.uniqueId
					if (item.bioDataType == "TEXT") {
						def keyword = new SearchKeyword()
						keyword.keyword = id.substring(id.indexOf(":") + 1)
						keyword.id = -1
						keyword.uniqueId = id
						keyword.displayDataCategory = "Text"
						keyword.dataCategory = "TEXT"
						gfilter.addKeywordFilter(keyword)
					} else {
						uniqueIds.add(item.uniqueId)
					}
				}

				def keywords = SearchKeyword.findAllByUniqueIdInList(uniqueIds)
				for (keyword in keywords) {
					gfilter.addKeywordFilter(keyword)
				}
			} else {
				flash.message = "You are not authorized to view the custom filter with ID ${params.id}."
				redirect(action:'index')
				redirected = true
			}
		} else {
			flash.message = "The custom filter with ID ${params.id} no longer exists."
			redirect(action:'index')
			redirected = true
		}

		if(!redirected) {
			sfilter.searchText = ""
			session.searchFilter = sfilter
			redirect(action:'doSearch', params:[ts:new Date().getTime()])
		}
	}

	/**
	 * Action which updates global filters after editing with advanced filter window
	 */
	def searchEdit = {

		def sfilter = new SearchFilter()
		def gfilter = sfilter.globalFilter
		def ids = params?.ids.split(",")
		def texts = params?.texts.split(",")
		if (ids.size() > 0 || texts.size() > 0) {
			for (id in ids) {
				def keyword = getSearchKeyword(id)
				if (keyword != null) {
					gfilter.addKeywordFilter(keyword)
				}
			}
			for (text in texts) {
				def keyword = getSearchKeyword(text)
				if (keyword != null) {
					gfilter.addKeywordFilter(keyword)
				}
			}
		}
		if (!gfilter.isEmpty()) {
			session.searchFilter = sfilter
			redirect(action:'doSearch')
		} else {
			redirect(action:'index')
		}

	}

	/**
	 * Action which removes a global filter
	 */
	def remove = {
		def id = params?.id
		def gfilter = session.searchFilter.globalFilter
		if (id != null && id.length() > 0)
		{
			def keyword = getSearchKeyword(id)
			if (keyword != null) {
				gfilter.removeKeywordFilter(keyword)
			}
		}
		if (gfilter.isEmpty()) {
			session.searchFilter = new SearchFilter()
			redirect(action:'index')
		} else {
			redirect(action:'doSearch')
		}
	}

	def searchHeaderSearch = {
		params.sourcepage="search"
		redirect(action:'search', params:params)
	}

	def showDefaultFilter = {
		render(template:'defaultFilter')
	}

	/**
	 * Parses the searchText and populates the global search filter
	 */
	def createUpdateSessionFilter(keyword) {
		def filter = session.searchFilter == null ? new SearchFilter() : session.searchFilter
		updateSearchFilter(keyword, filter)
		session.setAttribute('searchFilter', filter)
	}

	/**
	 * update existing search Filter
	 */
	def updateSearchFilter(keyword, SearchFilter filter) {
		filter.searchText = keyword.keyword;
		filter.globalFilter.addKeywordFilter(keyword)
	}

	/**
	 * Creates link to detatils for specified filter keyword.
	 */
	def createSummaryFilter(org.transmart.searchapp.SearchKeyword keyword){

		def link = new StringBuilder()
		def type = keyword.dataCategory.toLowerCase()

		link.append("<nobr>")
		if (type == "text") {
			link.append(createFilterDetailsLink(id: keyword.keyword, label: keyword.keyword, type: type))
			link.append(createRemoveFilterLink(id: keyword.keyword))
		} else if (type == "pathway") {
			def label = keyword.keyword
			if (keyword.dataSource != null && keyword.dataSource != "") {
				label = keyword.dataSource + "-" + label
			}
			link.append(createFilterDetailsLink(id: keyword.bioDataId, label: label, type: type))
			link.append(createRemoveFilterLink(id: keyword.id))
		} else {
			link.append(createFilterDetailsLink(id: keyword.bioDataId, label: keyword.keyword, type: type))
			link.append(createRemoveFilterLink(id: keyword.id))
		}
		link.append("</nobr>")
		return link.toString()

	}

	/**
	 * Creates section in summary for given categories filters.
	 */
	def createSummarySection(category, gfilter) {

		def section = new StringBuilder()
		def filters = gfilter.findFiltersByCategory(category)
		for (filter in filters) {
			if (section.length() > 0) {
				section.append(" OR ")
			}
			section.append(createSummaryFilter(filter))
		}

		if (section.length() == 0) {
			return ""
		}

		def span = new StringBuilder()
		span.append("<span class=\"filter-item filter-item-")
		span.append(category.toLowerCase())
		span.append("\">")
		span.append(formatCategory(category))
		if (filters.size() > 1) {
			span.append("s")
		}
		span.append("&gt; </span>")
		span.append(section)

		return span.toString()

	}

	/**
	 * Creates summary of filters with links to details for filters.
	 */
	def createSummaryWithLinks(SearchFilter filter) {

		// get global filter
		GlobalFilter gfilter = filter.globalFilter;

		//	log.info " we are in the summary links - "+searchText
		def genes = createSummarySection(gfilter.CATEGORY_GENE, gfilter)
		def pathways = createSummarySection(gfilter.CATEGORY_PATHWAY, gfilter)
		def compounds = createSummarySection(gfilter.CATEGORY_COMPOUND, gfilter)
		def diseases = createSummarySection(gfilter.CATEGORY_DISEASE, gfilter)
		def trials = createSummarySection(gfilter.CATEGORY_TRIAL, gfilter)
		def texts = createSummarySection(gfilter.CATEGORY_TEXT, gfilter)
		def genesigs = createSummarySection(gfilter.CATEGORY_GENE_SIG, gfilter)
		def studies = createSummarySection(gfilter.CATEGORY_STUDY, gfilter)
		def glists = createSummarySection(gfilter.CATEGORY_GENE_LIST, gfilter)


		def summary = new StringBuilder()


		if (genes) {
			summary.append(genes)
		}

		if (summary.length() > 0 && pathways.length() > 0) {
			summary.append(" OR ")
		}

		summary.append(pathways)

		if (summary.length() > 0 && genesigs.length() > 0) {
			summary.append(" OR ")
		}
		summary.append(genesigs)
		if (summary.length() > 0 && glists.length() > 0) {
			summary.append(" OR ")
		}
		summary.append(glists)

		if (summary.length() > 0 && compounds.length() > 0) {
			summary.append(" AND ")
		}
		summary.append(compounds)

		if (summary.length() > 0 && diseases.length() > 0) {
			summary.append(" AND ")
		}
		summary.append(diseases)

		if (summary.length() > 0 && trials.length() > 0) {
			summary.append(" AND ")
		}
		summary.append(trials)

		if (summary.length() > 0 && studies.length() > 0) {
			summary.append(" AND ")
		}
		summary.append(studies)

		if (summary.length() > 0 && texts.length() > 0) {
			summary.append(" AND ")
		}
		summary.append(texts)

		return summary.toString()
	}


	//def loadLinkedGids(Set gidSet){
	///	def gidlist = []
	//	gidlist.addAll(gidSet)

	//}

	def formatSynonyms(synonyms){
		if(synonyms ==null)
			return ""
		def syntext = new StringBuilder("")
		def first = true;
		for(syn in synonyms){
			if(first){
				first =false;
			}else{
				syntext.append(", ")
			}
			syntext.append(syn.code)

		}

		if(syntext.length()>0){
			syntext.insert(0, "(");
			syntext.append(")");
		}

		def stext= syntext.toString();
		if(stext.length()>60) stext=stext.substring(0, 59)+"...";
		return stext;
	}

	def formatCategory(category) {
		return category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase()
	}

	/**
	 * Gets a SearchKeyword record by id or creates a SearchKeyword for free text.
	 */
	SearchKeyword getSearchKeyword(String id) {
		SearchKeyword keyword
		if (id != null && id != "") {
			try {
				// Try to match on numeric ID
				keyword = SearchKeyword.get(Long.valueOf(id))
			} catch (NumberFormatException ex) {
				//				// If not numeric ID, then see if text matches any existing filter keywords.
				//				def filters = session.searchFilter.globalFilter.getAllFilters()
				//				def text = id.toString().toUpperCase()
				//				for (filter in filters) {
				//					if (filter.keyword.toUpperCase() == text) {
				//						keyword = filter
				//						break
				//					}
				//				}
				//
				// If no matching keywords found, then assume field is "free text"
				if (keyword == null) {
					keyword = new SearchKeyword()
					keyword.keyword = id
					keyword.id = -1
					keyword.bioDataId = -1
					keyword.uniqueId = "TEXT:" + id
					keyword.displayDataCategory = "Text"
					keyword.dataCategory = "TEXT"
				}
			}
		}
		return keyword
	}

	def noResult = {
		//Comment
		render(view:'noresult')
	}
}
