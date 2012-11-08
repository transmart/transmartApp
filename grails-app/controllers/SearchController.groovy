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

import i2b2.SnpInfo;
import grails.converters.*

import org.hibernate.*

import search.CustomFilter
import search.GeneSignature;
import search.GeneSignatureItem;
import search.SearchKeyword
import search.SearchKeywordTerm
import au.com.bytecode.opencsv.CSVWriter;
import bio.BioAssayAnalysis;
import bio.BioDataExternalCode

import com.recomdata.util.*
import static java.util.UUID.randomUUID;

public class SearchController{
	def sessionFactory
	def springSecurityService
	def experimentAnalysisQueryService
	def literatureQueryService
	def TrialQueryService
	def SearchService
	def documentService
	def searchKeywordService
	def RModulesFileWritingService
	def RModulesJobProcessingService
	def RModulesOutputRenderService
	def regionSearchService
	
	String authKeyGG = null
	
	def SEARCH_DELIMITER='SEARCHDELIMITER'

	def index = {
		session.setAttribute('searchFilter', new SearchFilter())
	}

	def list = {
		if(!params.max) params.max = 20
		//	session["opengenerifs"]=[:]
		//	session["details"]=[:]
		def results = GeneExprAnalysis.list( params )
		[ geneExprAnalysisList: results,total:GeneExprAnalysis.count() ,page:true]
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
			def queryStr = "SELECT distinct t.searchKeyword, t.keywordTerm, t.rank, t.termLength FROM search.SearchKeywordTerm t WHERE t.keywordTerm LIKE :term || '%' "
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

		def categories = SearchKeyword.executeQuery("select distinct k.dataCategory as value, k.displayDataCategory as label from search.SearchKeyword k order by k.dataCategory")
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
		render params.callback + "(" + (result as JSON) + ")"

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
			//			def query = "select k from search.SearchKeyword k, bio.BioDataCorrelation c where k.bioDataId=c.associatedBioDataId and c.bioDataId=?"
			//			genes = search.SearchKeyword.executeQuery(query, keyword.bioDataId)
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

		//	def queryStr = "SELECT distinct k FROM search.SearchKeyword k left join k.externalCodes c WHERE k.dataCategory IN ('GENE', 'PATHWAY') AND (UPPER(k.keyword) LIKE '"+values+"%' OR (c.codeType='SYNONYM' AND UPPER(c.code) LIKE '"+values+"%')) ORDER BY LENGTH(k.keyword), k.keyword";
		StringBuffer qBuf = new StringBuffer();
		qBuf.append("SELECT distinct t.searchKeyword, t.keywordTerm, t.rank, t.termLength ");
		qBuf.append("FROM search.SearchKeywordTerm t ");
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
			allSynonyms = BioDataExternalCode.executeQuery("SELECT DISTINCT bdec FROM bio.BioDataExternalCode bdec WHERE bdec.bioDataId IN(:ids) AND bdec.codeType='SYNONYM'",[ids:dataIds])
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
		render params.callback+"("+ (result as JSON) +")"

	}

	def doSearch = {

		def filter = session.searchFilter;
		def sResult = new SearchResult()
		//	log.info "doSearch:"+params
		//log.info "isTextOnly = " + filter.globalFilter.isTextOnly()
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
	 * conduct a search, params expected or keywork id or keyword text
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
			}
		} else {
			flash.message = "The custom filter with ID ${params.id} no longer exists."
			redirect(action:'index')
		}
		sfilter.searchText = ""
		session.searchFilter = sfilter

		redirect(action:'doSearch', params:[ts:new Date().getTime()])

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
	def createSummaryFilter(search.SearchKeyword keyword){

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

	def webStartPlotter = {
		
		def codebase = grailsApplication.config.com.recomdata.rwg.webstart.codebase
		def href = grailsApplication.config.com.recomdata.rwg.webstart.href
		def jar = grailsApplication.config.com.recomdata.rwg.webstart.jar
		def mainClass = grailsApplication.config.com.recomdata.rwg.webstart.mainClass
		def analysisIds = params.analysisIds
		def geneSource = params.geneSource
		def snpSource = params.snpSource
		def pvalueCutoff = params.pvalueCutoff
		def searchRegions = getWebserviceCriteria(session['solrSearchFilter'])
		def regionStrings = []
		for (region in searchRegions) {
			regionStrings += region[0] + "," + region[1]
		}
		def regions = regionStrings.join(";")
		//Set defaults - JNLP does not take blank arguments
		if (!regions) { regions = "0" }
		if (!pvalueCutoff) { pvalueCutoff = 0 }
		
		def responseText = """<?xml version="1.0" encoding="utf-8"?> 
							<jnlp 
							  spec="1.0+" 
							  codebase="${codebase}" 
							  href="/transmartPfizer/search/webStartPlotter;jsessionid=""" + session.getId() + """"> 
							  <information> 
							    <title>GWAVA Gene Wide Association Visual Analyzer with search set</title> 
							    <vendor>Pfizer Inc</vendor> 
							    <homepage href="./index.html"/> 
							    <description>Tool for Manhattan plot visualization of GWAS data.</description> 
							    <description kind="short">GWAVA gene wide association visual analysis</description> 
							    <shortcut>
							      <desktop/>
							      <menu submenu="GWAVA Transmart"/>
							    </shortcut>
							    <icon href="./images/guava_16.jpg"/> 
							    <icon href="./images/guava_24.jpg"/> 
							    <icon href="./images/guava_48.jpg"/> 
							    <icon kind="splash" href="./images/gwava_splash2.jpg"/>
							    <offline-allowed/> 
							  </information> 
							  <security> 
							      <all-permissions/> 
							  </security> 
							  <update check="always"/>
							  <resources> 
							    <j2se version="1.6+" java-vm-args="-Xmx800m"/>
							    
							    <jar href="./lib/BioServicesClient.jar"/>  
							    <jar href="./lib/commons-beanutils-1.8.3.jar"/>  
							    <jar href="./lib/commons-beanutils-bean-collections-1.8.3.jar"/>  
							    <jar href="./lib/commons-beanutils-core-1.8.3.jar"/>  
							    <jar href="./lib/BioServicesUtil.jar"/>  
							    <jar href="./lib/commons-codec-1.6.jar"/>  
							    <jar href="./lib/commons-digester3-3.2.jar"/>  
							    <jar href="./lib/commons-lang3-3.1.jar"/>  
							    <jar href="./lib/commons-logging-1.1.1.jar"/>  
							    <jar href="./lib/httpclient-4.0.jar"/>  
							    <jar href="./lib/httpcore-4.2.1.jar"/>  
							    <jar href="./lib/jersey-client-1.4.jar"/>  
							    <jar href="./lib/jersey-core-1.4.jar"/>  
							    <jar href="./lib/jgoodies-common-1.3.1.jar"/>  
							    <jar href="./lib/jgoodies-looks-2.5.1.jar"/>  
							    <jar href="./lib/jnlp.jar"/>  
							    <jar href="./lib/log4j-1.2.17.jar"/>
							    <jar href="./lib/TDBApi.jar"/>  
							    <jar href="${jar};jsessionid=""" + session.getId() + """" main="true" />
							    
							    <property name="jsessionid" value='""" + session.getId() + """'/>
                                <property name="serviceHost" value='""" + request.getServerName() + """'/>
                                <property name="sun.java2d.noddraw" value="true"/>
							  </resources> 
							  <application-desc main-class="com.pfizer.mrbt.genomics.Driver"> 
								<argument>""" + analysisIds + """</argument>
								<argument>""" + regions + """</argument>
								<argument>""" + geneSource + """</argument>
								<argument>""" + snpSource + """</argument>
								<argument>""" + pvalueCutoff + """</argument>
								<argument>-services=transmart</argument>
							  </application-desc>
								
							</jnlp>                           
		"""
								
//		<?xml version="1.0" encoding="UTF-8"?>
//		<jnlp spec="6.0+" codebase="${codebase}" href="search/webStartPlotter;jsessionid=""" + session.getId() + """">
//			<information>
//				<title>Testing Java WS</title>
//				<vendor>Recombinant</vendor>
//			</information>
//			<resources>
//				<!-- Application Resources -->
//				<j2se version="1.6+" href="http://java.sun.com/products/autodl/j2se"/>
//				<property name="jsessionid" value='""" + session.getId() + """'/>
//				<property name="serviceHost" value='""" + request.getServerName() + """'/>
//				<jar href="${jar};jsessionid=""" + session.getId() + """" main="true" />
//			</resources>
//					<application-desc name="Test WS" main-class="${mainClass}" width="300" height="300">
//				"""
//
//				responseText += '<argument>' + analysisIds + '</argument>\n'
//				responseText += '<argument>' + regions + '</argument>\n'
//				responseText += '<argument>' + geneSource + '</argument>\n'
//				responseText += '<argument>' + snpSource + '</argument>\n'
//				responseText += '<argument>' + pvalueCutoff + '</argument>\n'
//	responseText +=	"""
//
//					</application-desc>
//			<update check="background"/>
//		</jnlp>
		
		
		render(text:responseText,contentType:"application/x-java-jnlp-file")
		//render(text:responseText,contentType:"text/html")
	}
	
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
		render(view:'noresult')

	}
		
	//Retrieve the results for the search filter. This is used to populate the result grids on the search page.
	def getAnalysisResults = {
		
		//TODO Combine this and the table method, they're now near-identical
		def paramMap = params;
		def max = params.long('max')
		def offset = params.long('offset')
		def cutoff = params.double('cutoff')
		def sortField = params.sortField
		def order = params.order
		def search = params.search
		
		def analysisId = params.long('analysisId')
		def export = params.boolean('export')
		
		def filter = session['filterAnalysis' + analysisId];
		if (filter == null) {
			filter = [:]
		}
		if (max != null) { filter.max = max }
		if (!filter.max || filter.max < 10) {filter.max = 10;}
		
		if (offset != null) { filter.offset = offset }
		if (!filter.offset || filter.offset < 0) {filter.offset = 0;}
		
		if (cutoff != null) { filter.cutoff = cutoff }
		
		if (sortField != null) { filter.sortField = sortField }
		if (!filter.sortField) {filter.sortField = 'rsid';}
		
		if (order != null) { filter.order = order }
		if (!filter.order) {filter.order = 'asc';}
		
		if (search != null) { filter.search = search }
		
		def analysisIds = []
		analysisIds.push(analysisId)
		
		session['filterAnalysis' + analysisId] = filter;
		
		//Override max and offset if we're exporting
		def maxToUse = filter.max
		def offsetToUse = filter.offset
		if (export) {
			maxToUse = 0
			offsetToUse = 0
		}
		
		def regionSearchResults
		try {
			regionSearchResults = getRegionSearchResults(maxToUse, offsetToUse, filter.cutoff, filter.sortField, filter.order, filter.search, analysisIds)
		}
		catch (Exception e) {
			render(text: "<pre>" + e.getMessage() + "</pre>")
			return
		}
		
		//regionSearchResults will either contain GWAS or EQTL data. Overwrite the base object with the one that's populated
		if (regionSearchResults.gwasResults) {
			regionSearchResults = regionSearchResults.gwasResults
		}
		else {
			regionSearchResults = regionSearchResults.eqtlResults
		}

		//Return the data as a GRAILS template or CSV
		if (export) {
			exportResults(regionSearchResults.columnNames, regionSearchResults.analysisData, "analysis" + analysisId + ".csv")
		}
		else {
			render(template: "analysisResults", model: [analysisData: regionSearchResults.analysisData, columnNames: regionSearchResults.columnNames, max: regionSearchResults.max, offset: regionSearchResults.offset, cutoff: filter.cutoff, sortField: filter.sortField, order: filter.order, search: filter.search, totalCount: regionSearchResults.totalCount, wasRegionFiltered: regionSearchResults.wasRegionFiltered, analysisId: analysisId])
		}
	}
	
	//Retrieve the results for all analyses currently examined.
	def getTableResults = {
		
		def paramMap = params;
		def max = params.long('max')
		def offset = params.long('offset')
		def cutoff = params.double('cutoff')
		def sortField = params.sortField
		def order = params.order
		def search = params.search

		def export = params.boolean('export')
		
		def filter = session['filterTableView'];
		if (filter == null) {
			filter = [:]
		}
		
		if (max != null) { filter.max = max }
		if (!filter.max || filter.max < 10) {filter.max = 10;}
		
		if (offset != null) { filter.offset = offset }
		if (!filter.offset || filter.offset < 0) {filter.offset = 0;}
		
		if (cutoff != null) { filter.cutoff = cutoff }
		
		if (sortField != null) { filter.sortField = sortField }
		if (!filter.sortField) {filter.sortField = 'rsid';}
		
		if (order != null) { filter.order = order }
		if (!filter.order) {filter.order = 'asc';}
		
		if (search != null) { filter.search = search }
		
		def analysisIds = session['solrAnalysisIds']
		
		session['filterTableView'] = filter
		
		if (analysisIds.size() >= 100) {
			render(text: "<p>The table view cannot be used with more than 100 analyses (${analysisIds.size()} analyses in current search results). Narrow down your results by adding filters.</p>")
			return
		}
		else if (analysisIds.size() == 0) {
			render(text: "<p>No analyses were found for the current filter!</p>")
			return
		}
		else if (analysisIds[0] == -1) {
			render(text: "<p>The current search does not filter on any analyses. Select a study or set of analyses to display.</p>")
			return
		}
		
		//Override max and offset if we're exporting
		def maxToUse = filter.max
		def offsetToUse = filter.offset
		if (export) {
			maxToUse = 0
			offsetToUse = 0
		}
		
		def regionSearchResults
		try {
			regionSearchResults = getRegionSearchResults(maxToUse, offsetToUse, filter.cutoff, filter.sortField, filter.order, filter.search, analysisIds)
		}
		catch (Exception e) {
			render(text: "<pre>" + e.getMessage() + "</pre>")
			return
		}
		//Return the data as a GRAILS template or CSV
		if (export) {
			if (params.type?.equals('GWAS')) {
				exportResults(regionSearchResults.gwasResults.columnNames, regionSearchResults.gwasResults.analysisData, "results.csv")
			}
			else {
				exportResults(regionSearchResults.eqtlResults.columnNames, regionSearchResults.eqtlResults.analysisData, "results.csv")
			}
		}
		else {
			render(template: "gwasAndEqtlResults", model: [results: regionSearchResults, cutoff: filter.cutoff, sortField: filter.sortField, order: filter.order, search: filter.search])
		}
	}
	
	def exportResults(columns, rows, filename) {
		
		response.setHeader('Content-disposition', 'attachment; filename=' + filename)
		response.contentType = 'text/plain'
		
		String lineSeparator = System.getProperty('line.separator')
		CSVWriter csv = new CSVWriter(response.writer)
		def headList = []
		for (column in columns) {
			headList.push(column.sTitle)
		}
		String[] head = headList
		csv.writeNext(head)
		
		for (row in rows) {
			def rowData = []
			for (data in row) {
				rowData.push(data)
			}
			String[] vals = rowData
			csv.writeNext(vals)
		}
		csv.close()
	}
	
	def getSearchRegions(solrSearch) {
		def regions = []
		
		for (s in solrSearch) {
			if (s.startsWith("REGION")) {
				//Cut off REGION:, split by pipe and interpret chromosomes and genes
				s = s.substring(7)
				def regionparams = s.split("\\|")
				for (r in regionparams) {
					//Chromosome
					if (r.startsWith("CHROMOSOME")) {
						def region = r.split(";")
						def chrom = region[1]
						def position = region[3] as long
						def direction = region[4]
						def range = region[5] as long
						def ver = region[6]
						def low = position
						def high = position
						
						if (direction.equals("plus")) {
							high = position + range;
						}
						else if (direction.equals("minus")) {
							low = position - range;
						}
						else {
							high = position + range;
							low = position - range;
						}
						
						regions.push([gene: null, chromosome: chrom, low: low, high: high, ver: ver])
					}
					//Gene
					else {
						def region = r.split(";")
						def geneId = region[1] as long
						def direction = region[2]
						def range = region[3] as long
						def ver = region[4]
						def searchKeyword = SearchKeyword.get(geneId)
						def limits
						if (searchKeyword.dataCategory.equals("GENE")) {
							limits = regionSearchService.getGeneLimits(geneId, ver)
						}
						else if (searchKeyword.dataCategory.equals("SNP")) {
							limits = regionSearchService.getSnpLimits(geneId, ver)
						}
						def low = limits.get('low')
						def high = limits.get('high')
						def chrom = limits.get('chrom')
						
						if (direction.equals("plus")) {
							high = high + range;
						}
						else if (direction.equals("minus")) {
							low = low - range;
						}
						else {
							high = high + range;
							low = low - range;
						}
						regions.push([gene: geneId, chromosome: chrom, low: low, high: high, ver: ver])
					}
				}
			}
			else if (s.startsWith("GENESIG")) {
				//Expand regions to genes and get their limits
				s = s.substring(8)
				def sigIds = s.split("\\|")
				for (sigId in sigIds) {
					def sigSearchKeyword = SearchKeyword.get(sigId as long)
					def sigItems = GeneSignatureItem.createCriteria().list() {
						eq('geneSignature', GeneSignature.get(sigSearchKeyword.bioDataId))
						like('bioDataUniqueId', 'GENE%')
					}
					for (sigItem in sigItems) {
						def searchGene = SearchKeyword.findByUniqueId(sigItem.bioDataUniqueId)
						def geneId = searchGene.id
						def limits = regionSearchService.getGeneLimits(geneId, '19')
						regions.push([gene: geneId, chromosome: limits.get('chrom'), low: limits.get('low'), high: limits.get('high'), ver: "19"])
					}
				}
			}
			else if (s.startsWith("GENE")) {
				//If just plain genes, get the limits and default to HG19 as the version
				s = s.substring(5)
				def geneIds = s.split("\\|")
				for (geneString in geneIds) {
					def geneId = geneString as long
					def limits = regionSearchService.getGeneLimits(geneId, '19')
					regions.push([gene: geneId, chromosome: limits.get('chrom'), low: limits.get('low'), high: limits.get('high'), ver: "19"])
				}
			}
			else if (s.startsWith("SNP")) {
				//If plain SNPs, as above (default to HG19)
				s = s.substring(4)
				def rsIds = s.split("\\|")
				for (rsId in rsIds) {
					def limits = regionSearchService.getSnpLimits(rsId as long, '19')
					regions.push([gene: rsId, chromosome: limits.get('chrom'), low: limits.get('low'), high: limits.get('high'), ver: "19"])
				}
			}
		}
		
		return regions
	}
	
	def getWebserviceCriteria(solrSearch) {
		def genes = []
		
		for (s in solrSearch) {
			if (s.startsWith("REGION")) {
				//Cut off REGION:, split by pipe and interpret chromosomes and genes
				s = s.substring(7)
				def regionparams = s.split("\\|")
				for (r in regionparams) {
					//Chromosome
					if (r.startsWith("CHROMOSOME")) {
						//Do nothing for now
					}
					//Gene
					else {
						def region = r.split(";")
						def geneId = region[1] as long
						def direction = region[2]
						def range = region[3] as long
						def ver = region[4]
						def searchKeyword = SearchKeyword.get(geneId)
						def limits
						if (searchKeyword.dataCategory.equals("GENE")) {
							genes.push([searchKeyword.keyword, range])
						}
						else if (searchKeyword.dataCategory.equals("SNP")) {
							//Get the genes associated with this SNP
							def snpGenes = regionSearchService.getGenesForSnp(searchKeyword.keyword)
							//Push each gene and the radius
							for (snpGene in snpGenes) {
								genes.push([snpGene, range])
							}
						}
						
					}
				}
			}
			else if (s.startsWith("GENESIG")) {
				//Expand regions to genes and get their limits
				s = s.substring(8)
				def sigIds = s.split("\\|")
				for (sigId in sigIds) {
					def sigSearchKeyword = SearchKeyword.get(sigId as long)
					def sigItems = GeneSignatureItem.createCriteria().list() {
						eq('geneSignature', GeneSignature.get(sigSearchKeyword.bioDataId))
						like('bioDataUniqueId', 'GENE%')
					}
					for (sigItem in sigItems) {
						def searchGene = SearchKeyword.findByUniqueId(sigItem.bioDataUniqueId)
						def geneId = searchGene.id
						genes.push([searchGene.keyword, 0]);
					}
				}
			}
			else if (s.startsWith("GENE")) {
				s = s.substring(5)
				def geneIds = s.split("\\|")
				for (geneString in geneIds) {
					def geneId = geneString as long
					def searchKeyword = SearchKeyword.get(geneId)
					genes.push([searchKeyword.keyword, 0])
				}
			}
			else if (s.startsWith("SNP")) {
				//If plain SNPs, as above (default to HG19)
				s = s.substring(4)
				def rsIds = s.split("\\|")
				for (rsId in rsIds) {
					//Get the genes associated with this SNP
					def snpGenes = regionSearchService.getGenesForSnp(searchKeyword.keyword)
					//Push each gene and the radius
					for (snpGene in snpGenes) {
						genes.push([snpGene, range])
					}
				}
			}
		}
		
		return genes
	}
	
	def getRegionSearchResults(Long max, Long offset, Double cutoff, String sortField, String order, String search, List analysisIds) throws Exception {
	
		//Get list of REGION restrictions from session and translate to regions
		def regions = getSearchRegions(session['solrSearchFilter'])
		
		//Find out if we're querying for EQTL, GWAS, or both
		def hasGwas = BioAssayAnalysis.createCriteria().list([max: 1]) {
			or {
				eq('assayDataType', 'GWAS')
				eq('assayDataType', 'Metabolic GWAS')
			}
			'in'('id', analysisIds)
		}
		
		def hasEqtl = BioAssayAnalysis.createCriteria().list([max: 1]) {
			eq('assayDataType', 'EQTL')
			'in'('id', analysisIds)
		}
		
		def gwasResult
		def eqtlResult
				
		if (hasGwas) {
			gwasResult = runRegionQuery(analysisIds, regions, max, offset, cutoff, sortField, order, search, "gwas")
		}
		if (hasEqtl) {
			eqtlResult = runRegionQuery(analysisIds, regions, max, offset, cutoff, sortField, order, search, "eqtl")
		}
		
		return [gwasResults: gwasResult, eqtlResults: eqtlResult]
	}
	
	
	def runRegionQuery(analysisIds, regions, max, offset, cutoff, sortField, order, search, type) {
		
		//This will hold the index lookups for deciphering the large text meta-data field.
		def indexMap = [:]
		
		//Set a flag to record that the list was filtered by region
		def wasRegionFiltered = regions ? true : false
		
		def columnNames = []
		def searchDAO = new SearchDAO()
		def queryResult = regionSearchService.getAnalysisData(analysisIds, regions, max, offset, cutoff, sortField, order, search, type)
		def analysisData = queryResult.results
		def totalCount = queryResult.total
		def analysisIndexData
		if (type.equals("eqtl")) {
			analysisIndexData = searchDAO.getEqtlIndexData()
		}
		else {
			analysisIndexData = searchDAO.getGwasIndexData()
		}
		def returnedAnalysisData = []
		
		//These columns aren't dynamic and should always be included. Might be a better way to do this than just dropping it here.
		columnNames.add(["sTitle":"Analysis", "sortField":"baa.analysis_name"])
		columnNames.add(["sTitle":"Probe ID", "sortField":"data.rs_id"])
		columnNames.add(["sTitle":"p-value", "sortField":"data.p_value"])
		columnNames.add(["sTitle":"Adjusted p-value", "sortField":"data.log_p_value"])
		columnNames.add(["sTitle":"RS Gene", "sortField":"bm.bio_marker_name"])
		columnNames.add(["sTitle":"Chromosome", "sortField":"info.chrom"])
		columnNames.add(["sTitle":"Position", "sortField":"info.pos"])
		
		if (type.equals("eqtl")) {
			columnNames.add(["sTitle":"Gene", "sortField":"data.gene"])
		}

		analysisIndexData.each()
		{
			//Put the index information into a map so we can look it up later.
			indexMap[it.field_idx] = it.display_idx
			
			//We need to take the data from the index table and extract the list of column names.
			columnNames.add(["sTitle":it.field_name])
		}
		


		//The returned data needs to have the large text field broken out by delimiter.
		analysisData.each()
		{
			//This temporary list is used so that we return a list of lists.
			def temporaryList = []
			
			//The third element is our large text field. Split it into an array, leaving trailing empties.
			def largeTextField = it[3].split(";", -1)
			
			//This will be the array that is reordered according to the meta-data index table.
			String[] newLargeTextField = new String[largeTextField.size()]
			
			//Loop over the elements in the index map.
			indexMap.each()
			{
				//Reorder the array based on the index table.
				newLargeTextField[it.value-1] = largeTextField[it.key-1]
			}
			
			//Swap around the data types for easy array addition.
			def finalFields = new ArrayList(Arrays.asList(newLargeTextField));
			
			//Add the non-dynamic meta data fields to the returned data.
			temporaryList.add(it[4])
			temporaryList.add(it[0])
			temporaryList.add(it[1])
			temporaryList.add(it[2])
			temporaryList.add(it[5])
			temporaryList.add(it[6])
			temporaryList.add(it[7])
			if (type.equals("eqtl")) {
				temporaryList.add(it[8])
			}
			
			//Add the dynamic fields to the returned data.
			temporaryList+=finalFields
			
			returnedAnalysisData.add(temporaryList)
		}
		
		return [analysisData: returnedAnalysisData, columnNames: columnNames, max: max, offset: offset, cutoff: cutoff, totalCount: totalCount, wasRegionFiltered: wasRegionFiltered]
		
	}
	
	
	def getQQPlotImage = {
		
		//We need to determine the data type of this analysis so we know where to pull the data from.
		def currentAnalysis = bio.BioAssayAnalysis.get(params.analysisId)
		
		def pvalueCutoff = params.pvalueCutoff as double
		def search = params.search
		
		if (!pvalueCutoff) {pvalueCutoff = 0}
		if (!search) {search = ""}
		
		//Throw an error if we don't find the analysis for some reason.
		if(!currentAnalysis)
		{
			throw new Exception("Analysis not found.")
		}
		
		//This will hold the index lookups for deciphering the large text meta-data field.
		def indexMap = [:]
		
		//Initiate Data Access object to get to search data.
		def searchDAO = new SearchDAO()
		
		//Get the GWAS Data. Call a different class based on the data type.
		def analysisData
		
		//Get the data from the index table for GWAS.
		def analysisIndexData
		
		def returnedAnalysisData = []
		def returnJSON = [:]
		
		//Get list of REGION restrictions from session and translate to regions
		def regions = getSearchRegions(session['solrSearchFilter'])
		def analysisIds = [currentAnalysis.id]
		
		switch(currentAnalysis.assayDataType)
		{
			case "GWAS" :
			case "Metabolic GWAS" :
				analysisData = regionSearchService.getAnalysisData(analysisIds, regions, 0, 0, pvalueCutoff, "rsid", "asc", search, "gwas").results
				analysisIndexData = searchDAO.getGwasIndexData()
				break;
			case "EQTL" :
				analysisData = regionSearchService.getAnalysisData(analysisIds, regions, 0, 0, pvalueCutoff, "rsid", "asc", search, "eqtl").results
				analysisIndexData = searchDAO.getEqtlIndexData()
				break;
			default :
				throw new Exception("Not Applicable Data Type Found.")
		}
		
		analysisIndexData.each()
		{
			//Put the index information into a map so we can look it up later. Only add the GOOD_CLUSTERING column.
			if(it.field_name == "GOOD_CLUSTERING")
			{
				indexMap[it.field_idx] = it.display_idx
			}
		}

		//Create an entry that represents the headers to print to the file.
		def columnHeaderList = ["PROBEID","pvalue","good_clustering"]
		returnedAnalysisData.add(columnHeaderList)
		
		//The returned data needs to have the large text field broken out by delimiter.
		analysisData.each()
		{
			//This temporary list is used so that we return a list of lists.
			def temporaryList = []
			
			//This will be used to fill in the data array.
			def indexCount = 0;
			
			//The third element is our large text field. Split it into an array.
			def largeTextField = it[3].split(";", -1)
			
			//This will be the array that is reordered according to the meta-data index table.
			String[] newLargeTextField = new String[indexMap.size()]
			
			//Loop over the elements in the index map.
			indexMap.each()
			{
				//Reorder the array based on the index table.
				newLargeTextField[indexCount] = largeTextField[it.key-1]
				
				indexCount++;
			}
			
			//Swap around the data types for easy array addition.
			def finalFields = new ArrayList(Arrays.asList(newLargeTextField));
			
			//Add the non-dynamic meta data fields to the returned data.
			temporaryList.add(it[0])
			temporaryList.add(it[1])
			
			//Add the dynamic fields to the returned data.
			temporaryList+=finalFields
			
			returnedAnalysisData.add(temporaryList)
		}
		
		println "QQPlot row count = " + returnedAnalysisData.size()
//		for (int i = 0; i < returnedAnalysisData.size() && i < 10; i++) {
//			println returnedAnalysisData[i]
//		}
		
		//Get a unique key for the image file.
		def uniqueId = randomUUID() as String
		
		//Create a unique name using the id.
		def uniqueName = "QQPlot-" + uniqueId
		
		//Create the temporary directories for processing the image.
		def currentTempDirectory = RModulesFileWritingService.createTemporaryDirectory(uniqueName)
		
		def currentWorkingDirectory =  currentTempDirectory + File.separator + "workingDirectory" + File.separator
		
		//Write the data file for generating the image.
		def currentDataFile = RModulesFileWritingService.writeDataFile(currentWorkingDirectory, returnedAnalysisData,"QQPlot.txt")
		
		//Run the R script to generate the image file.
		RModulesJobProcessingService.runRScript(currentWorkingDirectory,"/QQ/QQPlot.R","create.qq.plot('QQPlot.txt')")
		
		//Verify the image file exists.
		def imagePath = currentWorkingDirectory + File.separator + "QQPlot.png"
		
		if(!new File(imagePath))
		{
			throw new Exception("Image file creation failed!")
		}
		else
		{
			//Move the image to the web directory so we can render it.
			def imageURL = RModulesOutputRenderService.moveImageFile(imagePath,uniqueName + ".png","QQPlots")
			
			returnJSON['imageURL'] = imageURL
			
			//Delete the working directory.
			def directoryToDelete = new File(currentTempDirectory)
			
			//This isn't working. I think something is holding the directory open? We need a way to clear out the temp files.
			directoryToDelete.deleteDir()
			
			//Render the image URL in a JSON object so we can reference it later.
			render returnJSON as JSON
		}

	} 
	
}
