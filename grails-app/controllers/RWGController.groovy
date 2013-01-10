/**
* $Id: $
* @author $Author: $
* @version $Revision: 14836 $
*/

import org.json.*

import bio.BioAnalysisAttribute
import bio.ClinicalTrial
import bio.Experiment
import bio.BioMarkerCorrelationMV

import org.apache.commons.codec.binary.Base64

import RWGVisualizationDAO

import grails.converters.*				// so we can render as JSON

import search.SearchKeyword
import search.SearchTaxonomy
import search.SearchTaxonomyRels
import search.GeneSignature
import search.GeneSignatureItem
import search.SavedFacetedSearch

import groovy.time.TimeCategory;
import groovy.time.TimeDuration;
import groovyx.net.http.HTTPBuilder

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.springframework.context.MessageSource
import org.transmart.searchapp.AccessLog;

class RWGController {
	def trialQueryService
	def searchService
	def searchKeywordService	
	def springSecurityService
	
    def index = {}

	/**
	 * START: Methods for the faceted search filter
	 */
	
	
   /*
   * Retrieve the SOLR field name equivalent for a term name 
   */
   private String getSOLRCategoryName(String field)  {
	   // set to uppercase and replace spaces with underscores
	   return field.toUpperCase().replaceAll(' ', '_')
   }	
   
   /**
   * Add a new node to the taxonomy Dynatree (and recursively add children if any exist).
   * parentNode: Node to add to tree
   * json: JSON array containing the "children" of the jQuery dynaTree
   * isCategory: boolean indicating whether the node being added is a category
   * categoryName: name of the category (i.e. as stored in database and displayed in tree)
   * uniqueTreeId: unique identifier for the node being added. This ill be a concatenation of the parent's unique id + the index of this child's index in children list
   *     e.g. category nodes will be 1,2,3; their children will be 1:1, 1:2, 1:3, 2:1, ...; their children 1:1:1, 1:1:2, ...
   * initialFacetCounts: JSONObject containing the initial facet counts for the nodes in the tree
   */
  // note: use a function instead of a closure - this is being called hundreds of times, and being a fn makes a big difference
  //def addDynaNode = {SearchTaxonomy parentNode, JSONArray json, boolean isCategory, String categoryName  ->
  public void addDynaNode(SearchTaxonomy parentNode, JSONArray json, boolean isCategory, 
	                      String categoryName, String uniqueTreeId, JSONObject initialFacetCounts) {
	  JSONArray children = new JSONArray()
	  
	  // create map for attributes of node
	  def parent = [:]
	  
	  // create a custom attribute for term name
	  parent["termName"] = parentNode.termName
	  
	  // generate the id for use in tree and for link to active terms
	  // if there is a link to an active term, use that as id (i.e. search_keyword_id)
	  // if not, use the id from the search_taxonomy table prepended with a letter (to ensure that there are no id collisions)
      def id 
	  if (parentNode.searchKeywordId)  {
		  id = parentNode.searchKeywordId
	  }
	  else  {
		  id = 'X' + parentNode.id
	  }
	  parent["id"] = id
	  	  
	  // if category, then display as folder and don't show checkbox; other levels, not a folder and show checkbox
	  parent["isFolder"] = isCategory 
	  parent["hideCheckbox"] = isCategory

	  // add custom attributes for each node 
	  parent["isCategory"] = isCategory

	  // create a uniqueTreeId for each node so we can identify it from it's copies
	  //  (id and key are not unique amongst copies)
	  parent["uniqueTreeId"] = uniqueTreeId

	  // TODO - decouple these 2 - retrieve id from searchKeyword, don't just assume it's same as display
	  parent["categoryId"] = categoryName.toUpperCase();
	  parent["categoryDisplay"] = categoryName;

	  parent["categorySOLR"] = getSOLRCategoryName(categoryName);
	  
 	  // Create custom attributes for the facet count for this node, and one for the initial facet
	  //   count which will be used to save the value when the tree gets cleared so we don't have to requery 
	  // Set to -1 for category nodes
	  if (isCategory)  {		 
		  parent["facetCount"] = -1
		  parent["initialFacetCount"] = -1
		  
		  //title is same as term name for categories
          parent["title"] = parentNode.termName
	  }
	  else  {
		  // get the json object for the category	  
		  JSONObject jo = (JSONObject)initialFacetCounts.get(getSOLRCategoryName(categoryName))
		  
		  // convert the term id to a string
          String idString = id.toString()

		  // retrieve the count for the term id if it exists in the json object, otherwise
		  //  none found so it's zero
		  int count		  
		  if (jo.has(idString))  {
		     count =  jo.getInt(idString)
		  }
	      else {
			  count = 0
		  } 

		  parent["facetCount"] = count
		  parent["initialFacetCount"] = count
		  
		  
		  // if the initial count is zero, don't add to tree
		  if (count == 0)  {
			  return
		  }
		  
		  
		  // include facet count in title for non-category nodes
		  parent["title"] = /${parentNode.termName} (${count})/ 
	  }
	  
	  def childIndex = 1
	  if (parentNode.children) {
		  // recursively add each child
		  for (childNode in parentNode.children)  {			 
			  addDynaNode(childNode, children, false, categoryName, uniqueTreeId + ":" + childIndex, initialFacetCounts)
			  childIndex++
		  }
	  }

	  // don't add categories without children to tree
	  if (isCategory && (children.length() == 0))  {
		  return
	  }
	  
	  // add children to parent map
	  parent["children"] = children
	  
	  // add parent map to json array
	  json.put(parent)
  }

   /*
   * Create the JSON string used as the "children" of the taxonomy DynaTree
   */
  def getDynatree = {
	  def cachedJson = grailsApplication.config.getProperty("dynatree")

	  def rwgDAO = new RWGVisualizationDAO()
	  
	  // retrieve the last time data was loaded/modified in the database, and the current time in the db
	  // this needs to be done regardless of whether we have a cached value because the current db time is needed for
	  //   storing the new string that will be cached
	  def dbTimes = rwgDAO.getLastDataLoadTime()
	  def dbLastUpdateTime = dbTimes['lastUpdateTime']
	  def dbCurrentTime = dbTimes['currentDbTime']
	  
	  def useCachedValue = false
	  if (cachedJson)  {
		    // there is a cached dynatree json string, get when it was cached
			def cachedTime = cachedJson['time']
		
			// if the cachedTime was later than the db time or the db time is not set use the cached value
			if (!dbLastUpdateTime || (cachedTime>dbLastUpdateTime))  {
				useCachedValue = true
			}
	  }
		
	  def json
	  if (useCachedValue)  {
		json = cachedJson['json']
	  }
	  else {
	  
		  // find all relationships
		  def rels = SearchTaxonomyRels.list(sort:"child.termName")
		  
		  // retrieve all taxonomy records (i.e. nodes in the tree)
		  def allNodes = SearchTaxonomy.list()
		  
		  def rootNode = null
	
		  // loop through every node, and link it to its parent and children to create tree
		  for (node in allNodes) {
			  for (rel in rels) {
				  
				  if (rel.parent) {   // non root node
					  // check if relationship represents a parent rel for the current node, and if so add the
					  // child to the node's children list
					  if (node.id == rel.parent.id) {
						  node.children.add(rel.child)
					  }
					  
					  // check if relationship represents a child rel for the current node, and if so add the
					  // parent to the node's parent list
					  if (node.id == rel.child.id) {
						  node.parents.add(rel.parent)
					  }
				  }
				  else {    // root node found
					  rootNode = rel.child
				  }
			  }
		  }
	
		  JSONArray categories = new JSONArray()
		  
		  if (rootNode.children)  {
			  
			  def categoriesList = []
			  // loop thru all children of root and create a list of categories to be used for initial facet search
			  for (categoryNode in rootNode.children)  {
				  String catName = categoryNode.termName
				  
				  // SOLR equivalent field is all uppercases with underscores instead of spaces
				  catName = getSOLRCategoryName(catName)
				  categoriesList.push(catName)
			  }
	
			  // retrieve initial facet counts to be used in tree
			  JSONObject initialFacetCounts = getInitialFacetResults(categoriesList)
	
			  // CREATE JSON ARRAY FOR TREE	  		  		  		  		  
			  def nodeIndex = 1
			  
			  // loop thru all children of root and add to JSON array for categories (addNode will recursively add children)
			  for (categoryNode in rootNode.children)  {	
				  
				  // give each node a unique id within tree (id and key are not necessarily unique)
				  // the unique id will be a concatenation of the parent's unique id + the index of this child's index in children list
				  // e.g. category nodes will be 1,2,3; their children will be 1:1, 1:2, 1:3, 2:1, ...; their children 1:1:1, 1:1:2, ...
				  String uniqueTreeId = nodeIndex
				  
				  addDynaNode(categoryNode, categories, true, categoryNode.termName, uniqueTreeId, initialFacetCounts)
				  nodeIndex++
			  }
		  }
		  else  {
			  throw new Exception("Root node not found")
		  }
		  json = categories?.toString()
		  
		  cachedJson = [:]
		  cachedJson.put('time', dbCurrentTime)   
		  cachedJson.put('json', json)
		  grailsApplication.config.setProperty("dynatree", cachedJson)
		  
	  }
	  
	  response.setContentType("text/json")
	  response.outputStream << json

   }
  
   /**
    * Create a query string for the category in the form of (<cat1>:("term1" OR "term2"))
    */
   def createCategoryQueryString = {category, termList -> 

       // create a query for the category in the form of (<cat1>:("term1" OR "term2"))
       String categoryQuery = ""
       for (t in termList.tokenize("|"))  {
	   
	       def queryTerm = /"${t}"/
	   
	       if (categoryQuery == "")  {
		       categoryQuery = queryTerm
	       }
	       else  {
		       categoryQuery = /${categoryQuery} OR ${queryTerm}/
	       }
       }

	   // enclose query clause in parens
       categoryQuery = /(${category}:(${categoryQuery}))/
	   
	   return categoryQuery
  }
   

   
   /**
   * Create the SOLR query string for the faceted fields (i.e. those that are in tree) that
   *   are not being filtered on
   * It will be of form facet.field=<cat1>&facet.field=<cat2>
   */
	def createSOLRFacetedFieldsString = {facetFieldsParams ->
	   def facetedFields=""
	   // loop through each regular query parameter
	   for (ff in facetFieldsParams)  {
		   
		   // skip TEXT search fields (these wouldn't be in tree so throw exception since this should never happen)
		   if (ff =="TEXT")  {
			   throw new Exception("TEXT field encountered when creating faceted fields string")
		   }
	
		   def ffClause = /facet.field=${ff}/
		   
		   if (facetedFields=="")  {
			   facetedFields = /${ffClause}/			   
		   }
		   else  {
			   facetedFields = /${facetedFields}&${ffClause}/
		   }
		   
	   }
	
	   return facetedFields
	}

   /**
       * Create the SOLR query string for the faceted fields (i.e. those that are in tree) that are being filtered
       * It will be of form facet=true&facet.field=(!ex=c1)<cat1>&facet.field=(!ex=c2)<cat2>&
       *     fq={!tag=c1}(<cat1>:"term1" OR <cat1>:"term2")&.... )
       * Each category query gets tagged in fq clauses {!tag=c1}, and then the category query is excluded
       *   for determining the facet counts (!ex=c1) in facet.field clauses
       */         
   def createSOLRFacetedQueryString = {facetQueryParams ->
	   def facetedQuery=""
	   // loop through each regular query parameter
	   for (qp in facetQueryParams)  {
		   
    	   // each queryParam is in form cat1:term1|term2|term3
	       String category = qp.split(":")[0]
	       String termList = qp.split(":")[1]
		   
		   // skip TEXT search fields (these wouldn't be in tree so throw exception since this should never happen)
		   if (category =="TEXT")  {
			   throw new Exception("TEXT field encountered when creating faceted search string")
		   }
	
		   def categoryQueryString = createCategoryQueryString(category, termList)
		   
		   def categoryTag = /{!tag=${category}}/
		   
		   def fqClause = /fq=${categoryTag}${categoryQueryString}/

		   def categoryExclusion = /{!ex=${category}}/		   
		   def ffClause = /facet.field=${categoryExclusion}${category}/
		   
		   def categoryClause = /${ffClause}&${fqClause}/

		   if (facetedQuery=="")  {
			   facetedQuery = /${categoryClause}/
		   }
		   else  {
		       facetedQuery = /${facetedQuery}&${categoryClause}/
		   }
		   
	   }	   

	   return facetedQuery
   }

   /**
    * Create the SOLR query string for the nonfaceted fields (i.e. those that are not in tree)
    * It will be of form ((<cat1>:"term1" OR <cat1>:"term2") AND ( (<cat2>:"term3") ) AND () .. )
    */
   protected String createSOLRNonfacetedQueryString(List queryParams) {

	   def nonfacetedQuery=""
	   
	   // loop through each regular query parameter
	   for (qp in queryParams)  {
	
		   // each queryParam is in form cat1:term1|term2|term3
	       String category = qp.split(":")[0]
	       String termList = qp.split(":")[1]
	
		   def categoryQueryString = createCategoryQueryString(category, termList)
		   
		   // skip TEXT search fields (or do we need to handle them somehow)
		   if (category =="TEXT")  {
			   continue
		   }
	
		   // add category query to main nonfaceted query string using ANDs between category clauses
		   if (nonfacetedQuery == "")  {
			   nonfacetedQuery = categoryQueryString
		   }
		   else  {
			   nonfacetedQuery = /${nonfacetedQuery} AND ${categoryQueryString}/
		   }
	   }
	   
	   // use all query if no params provided 
	   if (nonfacetedQuery == "")  {
		   nonfacetedQuery = "*:*"
	   }

	   nonfacetedQuery = /q=(${nonfacetedQuery})/
	   
       return nonfacetedQuery
   }

   /**
   * Execute the SOLR faceted query
   * @param solrRequestUrl - the base URL for the SOLR request
   * @param solrQueryParams - the query string for the faceted search, to be passed into the data for the POST request
   * @return JSONObject containing the facet counts
   */
   def executeSOLRFacetedQuery = {solrRequestUrl, solrQueryParams ->
	   
	   JSONObject facetCounts = new JSONObject()
	   
	   def slurper = new XmlSlurper()

   	   // submit request
	   def solrConnection = new URL(solrRequestUrl).openConnection()
	   solrConnection.requestMethod= "POST"
	   solrConnection.doOutput = true

	   // add params to request 	   	   
	   def dataWriter = new OutputStreamWriter(solrConnection.outputStream)
	   dataWriter.write(solrQueryParams)

	   dataWriter.flush()
	   dataWriter.close()
	   
	   def facetCategoryNodes   // will store the facet category nodes from the xml response in here
	   
	   // process response
	   if (solrConnection.responseCode == solrConnection.HTTP_OK)  {
		   def xml
		   
		   solrConnection.inputStream.withStream {
			   xml = slurper.parse(it)
		   }
		   // retrieve all the category nodes for the facet fields (contain subnodes which have the actual counts)
		   facetCategoryNodes = xml.lst.find{it.@name == 'facet_counts'}.lst.find{it.@name == 'facet_fields'}.lst
	   }
	   else {
		   throw new Exception("SOLR Request failed! Request url:" + solrRequestUrl + "  Response code:" + solrConnection.responseCode + "  Response message:" + solrConnection.responseMessage)
	   }
	   
	   solrConnection.disconnect()
			  
	   // put counts for each category/term into a json string to pass back
	   for (catNode in facetCategoryNodes) {
		   // retrieve the category name from the xml node
		   def catName = catNode.@name

		   JSONObject catArray = new JSONObject()   // json object for current category
			for (countNode in catNode.int) {
				def skId = countNode.@name    // search keyword id
				def c = countNode.text()
				
				// add term to category object
				catArray.put(skId.toString(), c.toString())
		   }

			// add category array object to all objects
			facetCounts.put(catName.toString(), catArray)
	   }
	   
	   return facetCounts
   }

   /**
   * Create the SOLR query string for the faceted query
   * @param nonfacetedQueryString - the portion of the URL containing the non faceted query string
   * @param facetedQueryString - the portion of the URL containing the faceted query string
   * @param facetedFieldsString - the portion of the URL containing the faceted fields string
   * @param maxRows - max number of result rows to return (default to 0
   * @return string containing the SOLR query string
   */
   def createSOLRQueryString = {
	   nonfacetedQueryString, facetedQueryString, facetedFieldsString, maxRows=0, facetFlag=true ->
	   def solrQuery = /${nonfacetedQueryString}&facet=${facetFlag}&rows=${maxRows}/
	
	   if (facetedQueryString != "")  {
  	       solrQuery = /${solrQuery}&${facetedQueryString}/	   
	   }

	   if (facetedFieldsString != "")  {
			 solrQuery = /${solrQuery}&${facetedFieldsString}/
	   }
	   return solrQuery
   }

   /**
   * Create the base URL for the SOLR request
   * @return string containing the base URL for the SOLR query
   */
   def createSOLRQueryPath = {
	   	   
	   String solrScheme = grailsApplication.config.com.rwg.solr.scheme
	   String solrHost = grailsApplication.config.com.rwg.solr.host
	   String solrPath = grailsApplication.config.com.rwg.solr.path
	   String solrRequestUrl = new URI(solrScheme, solrHost, solrPath, "", "").toURL()
	   
	   return solrRequestUrl
   }

   
   private String getGeneSignatureErrorMessage(long gsKeywordId, boolean catchUnknownError)  {
	   def geneSigs = GeneSignature.executeQuery("select gs.id, gs.name, gs.fileSchema.id " +
		   " from search.SearchKeyword k_gs, search.GeneSignature gs" +
		   " where k_gs.bioDataId = gs.id " +
		   " and k_gs.id = ?", gsKeywordId)
	   
	   if (!geneSigs)  {
		   def sk = SearchKeyword.get(gsKeywordId)
		   
		   if (sk) {
			  def keyword = sk.keyword
			  return "Filter term <${keyword}> ignored! :: Improperly defined gene signature/gene list! Unable to retrieve Gene list/signature '${keyword}' with search keyword id ${gsKeywordId}"			  
		   } 
		   else  {
			  return "Filter term ignored! :: Unable to retrieve search keyword with search keyword id ${gsKeywordId}.  "
		   }		   
		   
	   }
	   
	   // retrieve the first record returned (should only be one)
	   def geneSig = geneSigs[0]
	   
	   def probeFileSchema = "3"
	   Long geneSignatureId = geneSig[0]
	   String geneSignatureName = geneSig[1]
	   String geneSignatureType = geneSig[2].toString()
	   if (geneSignatureType == probeFileSchema)  {
		   return "Filter term <${geneSignatureName}> ignored! :: Gene list/signature with id '${geneSignatureId}' and name '${geneSignatureName}' contains probes.  This type of gene signature is not currently supported in faceted search."
	   }
	   
	   def geneSigItemCount = GeneSignature.executeQuery("select count(gsi.id)  " +
		   " from search.GeneSignatureItem gsi " +
		   " where gsi.geneSignature.id = ?", geneSignatureId)

	   if (geneSigItemCount[0].toString() == "0")  {
		   return "Filter term <${geneSignatureName}> ignored! :: Gene list/signature with id '${geneSignatureId}' and name '${geneSignatureName}' contains zero items.  This type of gene signature is not supported in faceted search."
	   }
	   
	   if (catchUnknownError)  {
		   // don't know exactly what the error is
		   return "Filter term ignored! :: Data error with Gene list/signature with search keyword id ${gsKeywordId}.  Error could be caused by a list that contains genes that are not properly defined."	
	   }
	   else {
		   return ""
	   }
}
   
   /**
   * Replace any gene lists or signatures in the query parameters with a list of individual genes
   * @param params list of query params
   * @param genesField SOLR search field that gene searches are executed against (i.e. ALLGENE or SIGGENE)
   * @return string containing the new query parameters or an error message 
   */
   def replaceGeneLists = {  params, genesField  ->
	   def newParams = []
	   def genesList = []  

	   String errorMsg = ''
	   // loop through each regular query parameter
	   for (p in params)  {
		   
		   // each queryParam is in form cat1:term1|term2|term3
		   String category = p.split(":")[0]
		   String termList = p.split(":")[1]

		   // add all the genes from a gene list/sig to the List of genes		   
		   if (category == 'GENELIST' || category == 'GENESIG')  {
			   for (t in termList.tokenize("|"))  {

				   // create the paramter list for the hibernate query (need to convert the id explicitly to long) 
				   def queryParams = [:]
				   Long l = t.toLong()
				   queryParams["tid"] = l

				   def geneKeywords = SearchKeyword.executeQuery("select k_gsi.id " +
						            " from search.SearchKeyword k_gs, search.GeneSignature gs," +
									" search.GeneSignatureItem gsi, search.SearchKeyword k_gsi " +
									" where k_gs.bioDataId = gs.id " +
									" and gs.id = gsi.geneSignature " +
									" and gsi.bioMarker = k_gsi.bioDataId" + 
                                    " and k_gs.id = :tid ", queryParams)
				   if (geneKeywords.size == 0)  {					   

					   if (errorMsg != '')  {
						   errorMsg += '\n--------------------\n'
					   }
					   errorMsg += getGeneSignatureErrorMessage(l, true)
				   }
				   
				   // loop through each keyword for the gene list items and add to list 
				   geneKeywords.each {
					   // don't add duplicates
					   if (genesList.indexOf(it)<0)   {
						   genesList.add it
					   }
				   }			 
				   
			   }
		   }
		   // add all the genes from a pathway to the List of genes		   
		   else if (category == 'PATHWAY')  {
			   for (t in termList.tokenize("|"))  {
				   
				   // create the parameter list for the hibernate query (need to convert the id explicitly to long) 
				   def queryParams = [:]
				   Long l = t.toLong()
				   queryParams["tid"] = l
				   def geneKeywords = SearchKeyword.executeQuery("select k_gene.id " +
						            " from search.SearchKeyword k_pathway, bio.BioMarkerCorrelationMV b," +
									" search.SearchKeyword k_gene " +
									" where b.correlType = 'PATHWAY_GENE' " +  
                                    " and b.bioMarkerId = k_pathway.bioDataId " + 
									" and k_pathway.dataCategory = 'PATHWAY' " +
									" and b.assoBioMarkerId = k_gene.bioDataId " +
									" and k_gene.dataCategory = 'GENE' " +
                                    " and k_pathway.id = :tid ", queryParams) 
 
				   if (geneKeywords.size == 0)  {
					   throw new Exception("Data error with Pathway with id ${l}.  Error could be caused by an empty pathway, a pathway that does not exist, or pathway contains genes that are not preoperly defined.")					   
				   }

				   // loop through each keyword for the gene list items and add to list 
				   geneKeywords.each {
					   // don't add duplicates
					   if (genesList.indexOf(it)<0)   {
						   genesList.add it
					   }
				   }			 
				   
			   }
		   }
		   // add all the individual genes to the List of genes
		   else if (category == 'PROTEIN' )  {
			   for (t in termList.tokenize("|"))  {
				   genesList.add t				   
			   }
		   }
		   // add all the individual genes to the List of genes
		   else if (category == 'GENE' )  {
			   for (t in termList.tokenize("|"))  {
				   genesList.add t				   
			   }
		   }
		   else  {
			   // create the new params with everything that is not a gene or list
			   newParams.add p
		   }
	   }
	   
	   // create the new string to be used for genes and lists/sigs and add back to params 
	   def newGeneString = ""
	   
	   if (genesList.size > 0)  {
		   newGeneString = /${genesField}:${genesList.join('|')}/
           newParams.add newGeneString
	   }
	   
	   log.info("Gene parameter: ${newParams}")
	   return ['errorMsg': errorMsg, 'newParams':newParams]
   }
           
   /**
   * Load the search results for the given search terms (used for AJAX calls)
   * @return JSON object containing facet counts
   */
   def getFacetResults = {
	   	   
	   def startTime = new Date()								// Clock starts running now!
	   
	   // determine whether we have set the showAllResults param
	   boolean showSigGenesOnly = true
	   if (request.getParameter('showSignificantResults') && request.getParameter('showSignificantResults').toLowerCase()=='false')  {
		   showSigGenesOnly = false
	   }

	   // q params are filtered on but not faceted
       def queryParams = request.getParameterValues('q')
	   
	   // get name of SOLR search field to be used for gene queries (SIGGENE or ALLGENE) and set session var
	   
	   def solrGenesField = setSOLRGenesField(showSigGenesOnly)  
	   // replace gene signatures or gene list terms into their list of individual genes
	   def result = replaceGeneLists(queryParams, solrGenesField)	 
	   queryParams = result.get('newParams')
	   def errorMsg = result.get('errorMsg')		   
	   
	   if (showSigGenesOnly)  {
		   queryParams.add "ANY_SIGNIFICANT_GENES:1"
	   }
	   
	   //fq params are also faceted and also filtered on
	   def facetQueryParams = request.getParameterValues('fq')

	   // save all the filter params to a session List variable   
	   def sessionFilterParams = []	   
	   for (p in queryParams)  {
		   sessionFilterParams.add p
	   }	   
	   for (p in facetQueryParams)  {
		   sessionFilterParams.add p
	   }
	   session['solrSearchFilter'] = sessionFilterParams
	   
	   // ff params are faceted, but not filtered on
	   def facetFieldsParams = request.getParameterValues('ff')	  
	   
	   log.info("facet search: " + params)
	   
	   // build the SOLR query
	   def nonfacetedQueryString = createSOLRNonfacetedQueryString(queryParams)
	   def facetedQueryString = createSOLRFacetedQueryString(facetQueryParams)
	   def facetedFieldsString = createSOLRFacetedFieldsString(facetFieldsParams)
	   
	   String solrRequestUrl = createSOLRQueryPath()
	   String solrQueryString = createSOLRQueryString(nonfacetedQueryString, facetedQueryString, facetedFieldsString)
       JSONObject facetCounts = executeSOLRFacetedQuery(solrRequestUrl, solrQueryString)

	   def studyCounts = facetCounts['STUDY_ID']
	   
	   // retrieve the html string for the results template	   
	   def html = loadSearchResults(studyCounts, startTime)
	   
	   // create a return json object containing both the facet counts to load into tree and html to load into results section
	   JSONObject ret = new JSONObject()
	   ret.put('facetCounts', facetCounts)
	   ret.put('html', html)
	   ret.put('errorMsg', errorMsg)
       response.setContentType("text/json")
	   response.outputStream << ret?.toString()	   
   }

   /**
   * Determine field to be used for genes within the SOLR queries and set session parameter
   * @param showSigGenesOnly boolean indicating whether analysis for all genes or only significant genes (default) will be shown
   * @return SOLR field to be used for gene searches
   */
   private String setSOLRGenesField(boolean showSigGenesOnly = true)  {
	   
	   def solrGenesField = ""  // name of SOLR search field to be used for gene queries (SIGGENE or ALLGENE)
	   if (showSigGenesOnly)  {
		   solrGenesField = 'SIGGENE'
	   }
	   else  {
		   solrGenesField = 'ALLGENE'
	   }
	   session['solrGenesField'] = solrGenesField
	   
	   return solrGenesField
   }

      
   /**
   * Load the initial facet results for the tree (no filters)
   * @return JSON object containing facet counts
   */
   private getInitialFacetResults = {List categoriesList  ->
	   // initial state of the significant field is checked, so need to add the search field to the SOLR query to get the initial facet coutns
	   //  and save the search term to the session variable so that is applied to the query to get the analysis list 
	   def queryParams = ["ANY_SIGNIFICANT_GENES:1"]
	   session['solrSearchFilter'] = queryParams
	   log.info("Initial facet search: " + queryParams)

	   // set session var for SOLR genes field (no param passed so default will be used)
	   setSOLRGenesField() 
	   	   	   
	   // build the SOLR query
	   
	   // get the base query string (i.e. "q=(*:*)" since no filters for initial search
	   def nonfacetedQueryString = createSOLRNonfacetedQueryString(queryParams)
	   def facetedQueryString = ""
	   def facetedFieldsString = createSOLRFacetedFieldsString(categoriesList)

	   String solrRequestUrl = createSOLRQueryPath()
	   String solrQueryString = createSOLRQueryString(nonfacetedQueryString, facetedQueryString, facetedFieldsString)

	   JSONObject facetCounts = executeSOLRFacetedQuery(solrRequestUrl, solrQueryString)
		
       return facetCounts
	   
   }

      
   /**
   * START: Methods for the keyword search
   */   
   // Sets the search filter for the new search.  
   def newSearch =	{
	   session['solrSearchFilter'] = []
	   render(status: 200)
   }

   // Return search categories for the drop down 
   def getSearchCategories = {
	   render searchKeywordService.findSearchCategories() as JSON	   
   }

   // Return search keywords for cross trial
   def searchAutoCompleteCTA = {
	   def category = ['PATHWAY', 'GENELIST', 'GENESIG', 'GENE', 'PROTEIN']
	   render searchKeywordService.findSearchKeywords(category, params.term) as JSON
   }

   
   // Return search keywords
   def searchAutoComplete = {
	   def category = params.category == null ? "ALL" : params.category	  
	   render searchKeywordService.findSearchKeywords(category, params.term) as JSON	   
   }
        
   // Load the search results for the given search terms using the new annotation tables
   // return the html string to be rendered for the results panel
   def loadSearchResults = { studyCounts, startTime ->	   
	   def trialAnalysis = [:]						// Map of the trial objects and the number of analysis per trial
	   def total = 0								// Running total of analysis to show in the top banner

	   def studyWithResultsFound = false   
   	   for (studyId in studyCounts.keys().sort()) {
		   def c = studyCounts[studyId].toInteger()

		   if (c > 0)  {
			   studyWithResultsFound = true
		   
			   def trialNumber= studyId

			   def exp = Experiment.createCriteria()
			   def trial = exp.get	{
				   eq("accession", trialNumber, [ignoreCase: true])
			   }
			   if (trial == null)	{
				   log.warn "Unable to find a trial for ${trialNumber}"
			   }  
			   else  {
				   
				   def trialMap = [:]
				   trialMap.put('studyId', trial.accession)
				   trialMap.put('experimentId', trial.id)
				   trialMap.put('title', trial.title)
				   trialMap.put('analysisCount', c)
				   
			       trialAnalysis.put(trial.id, trialMap)
			       total += c
			   }
		   }
	   }
	   // capture html as a string that will be passed back in JSON object
	   def html
	   if (!studyWithResultsFound)	{
		   html = g.render(template:'/search/noResult').toString()
	   } else	{
	       html = g.render(template:'/RWG/trials',model:[trials:trialAnalysis, analysisCount:total, duration:TimeCategory.minus(new Date(), startTime)]).toString()
	   }
	   
	   return html	   
   }
      
   // Load the trial analysis for the given trial
   def getTrialAnalysis = {	   
	   new AccessLog(username: springSecurityService.getPrincipal().username,
		   event:"Loading trial analysis", eventmessage:params.trialNumber, accesstime:new Date()).save()

	   def analysisList = trialQueryService.querySOLRTrialAnalysis(params, session.solrSearchFilter)
	   render(template:'/RWG/analysis', model:[aList:analysisList])
   }
 
   // First iteration of the method to get the heatmap data for RWG  
   def getHeatmapData = {
	   def r = setGenesAndSignificantFlag(params)
	   
	   def genes = r.get('genes')
	   def errorMsg = r.get('errorMsg')
	   def showSigResultsOnly = r.get('showSigResultsOnly') 	   

	   def rwgDAO = new RWGVisualizationDAO()
	   def hmData = rwgDAO.getHeatmapData(params.id, genes, showSigResultsOnly,  params.probesPage, params.probesPerPage)	  
  
	  	   
	   render hmData as JSON	   	 	
   }
   
   
   // First iteration of the method to get the heatmap for exporting as a csv file
   def getHeatmapDataForExport2 = {def rwgDAO = new RWGVisualizationDAO()
	   def genes = null
	   def filterTerms = session.solrSearchFilter
	   def solrGenesField = session.solrGenesField
	   
	   boolean showSigResultsOnly = solrGenesField == 'SIGGENE' ? true : false
	   
	   log.info("Only pass the gene filter")
	   for (filterTerm in filterTerms)	{
		   if (filterTerm.indexOf("${solrGenesField}:") > -1)	{
			   genes = filterTerm.replace("${solrGenesField}:", '')
			   break
		   }
	   }
	   
	   def hmData = rwgDAO.getHeatmapDataForExport2(params.id, params.probesList, genes, showSigResultsOnly,  1)
   
	   
	   response.setHeader("Content-Disposition", "attachment; filename=\"" +params.id +"_export.csv\"")
	   response.contentType = 'text/csv'
	   response.outputStream << hmData
	   response.outputStream.flush()
   }
   
   // assemble the gene list and determine the siginicance flag bases on parameters passed in 
   def setGenesAndSignificantFlag = {params ->
	   def genes = null
	   
	   def keywordsQueryString = params?.keywordsQueryString
	   def isSA = params?.isSA
			  
	   def filterTerms;
	   def solrGenesField;
	   def errorMsg = ''
	   if (isSA)  {
		   if (keywordsQueryString == '')  {
			   filterTerms = []
			   solrGenesField = 'SIGGENE';
		   }
		   else  {
			   def categories = keywordsQueryString.split('&')
			   solrGenesField = 'ALLGENE';
			   
			   def result = replaceGeneLists(categories, solrGenesField)
			   filterTerms = result.get('newParams')
			   errorMsg = result.get('errorMsg')
		
		   }
	   }
	   else {
		   // keywords not passed in, get from session variables
		   filterTerms = session.solrSearchFilter
		   solrGenesField = session.solrGenesField
	   }
	   
	   boolean showSigResultsOnly = solrGenesField == 'SIGGENE' ? true : false
	   
	   log.info("Only pass the gene filter")
	   for (filterTerm in filterTerms)	{
		   if (filterTerm.indexOf("${solrGenesField}:") > -1)	{
			   genes = filterTerm.replace("${solrGenesField}:", '')
			   break
		   }
	   }
	   
	   def retMap = [:]
	   retMap.put('genes', genes)
	   retMap.put('errorMsg', errorMsg)
	   retMap.put('showSigResultsOnly', showSigResultsOnly)
	   
	   return retMap

   }
   
   // Method to get the number of probes for the heatmap for the given filters
   // keywordsQueryString provided indicates that it is the CTA version of this heatmap (otherwise known as the SA or Selected Analysis heatmap) 
   def getHeatmapNumberProbes = {

	   def r = setGenesAndSignificantFlag(params)
	   
	   def genes = r.get('genes')
	   def errorMsg = r.get('errorMsg')
	   def showSigResultsOnly = r.get('showSigResultsOnly') 	   
	   
	   def rwgDAO = new RWGVisualizationDAO()
	   def maxProbeIndex = rwgDAO.getNumberProbes(params.id, genes, showSigResultsOnly)	 
	   
	   JSONObject ret = new JSONObject()
	   ret.put('maxProbeIndex', maxProbeIndex)
	   ret.put('errorMsg', errorMsg)
	   
	   response.setContentType("text/json")
	   response.outputStream << ret?.toString()
	   	   	   
   }
   
   def exportAsImage = {
	   
	   def imagedata = params.imgData
	   
//	   imagedata.replace(' ','+')
	   
//	   imagedata = Base64.decodeBase64(imagedata)
	   
	   imagedata=Base64.decodeBase64(imagedata)
	   
	   response.setHeader("Content-Disposition", "attachment; filename=\"export.png\"")
	   response.contentType = 'image/png'
	   response.outputStream << imagedata
	   response.outputStream.flush()
	   
   }
   
   /**
    * Returns the data for the box plot visualization
    */
   def getBoxPlotData = {	   
	   def rwgDAO = new RWGVisualizationDAO()	   
	   def m = rwgDAO.getBoxplotData(params.id, params.probeID)
	   render m as JSON
   }
   
   /**
    * Returns the data for the box plot visualization for Cross Trial Analysis
    */
   def getBoxPlotDataCTA = {	   
	   def rwgDAO = new RWGVisualizationDAO()
	 
	   def m = rwgDAO.getBoxplotDataCTA(params.ids.split(/\|/), params.keywordId)

	   render m as JSON
   }

   /**
    * Returns the data for the line plot visualization
    */
   def getLinePlotData = {	   
	   def rwgDAO = new RWGVisualizationDAO()	   
	   def m = rwgDAO.getLineplotData(params.id, params.probeID)
	   render m as JSON
   }

   /**
	* Method to total number of rows for the CTA heatmap for the given filters
	* 
	* @param params.analysisIds: list of analysis ids
	* @param params.category: either PATHWAY, GENELIST, or GENESIG  
	* @param params.searchKeywordId: search keyword id of the pathway, genelist or gene signature
	* 
	*/
   def getHeatmapCTARowCount = {
	   def rwgDAO = new RWGVisualizationDAO()
       // Render the template for the favorites dialog
	   
	   def analysisIdsList = params.analysisIds.split(/\|/)

	   def ret = [:]
	   def totalCount
	   totalCount = rwgDAO.getHeatmapRowCountCTA(analysisIdsList, params.category, params.searchKeywordId)
	   ret.put("totalCount",  totalCount) 

	   // no rows, check if because of invalid gene sig/list
	   def errorMsg = ''
	   
	   if (totalCount == 0 )  {
		   
		   if ((params.category == 'GENELIST') || (params.category == 'GENESIG') )  {
			   // send in false for second param (i.e. catchUnknownError) because there may not really be an error here
			   errorMsg = getGeneSignatureErrorMessage(params.searchKeywordId.toLong(), false)
		   }
	   }
	   
	   ret.put('errorMsg', errorMsg)
	   
	   render ret as JSON	   
   }
   
   /**
	* Method to get the list of rows on a certain page of the CTA heatmap for the given filters
	* 
	* @param params.analysisIds: list of analysis ids
	* @param params.category: either PATHWAY, GENELIST, or GENESIG  
	* @param params.searchKeywordId: search keyword id of the pathway, genelist or gene signature
	* @param params.startRank: start index of page to be returned
	* @param params.endRank: end index of page to be returned
	* 
	*/
   def getHeatmapCTARows = {
	   def rwgDAO = new RWGVisualizationDAO()
       // Render the template for the favorites dialog
	   
	   def analysisIdsList = params.analysisIds.split(/\|/)

	   def ret = [:]

	   int startRank = params.startRank.toInteger()
	   int endRank = params.endRank.toInteger()
	   
	   def rows = rwgDAO.getHeatmapRowsCTA(analysisIdsList, params.category, params.searchKeywordId, startRank, endRank)
	   
	   // loop through to get list of search keyword ids for page
	   def searchKeywordIds = []
	   for (def i=startRank; i<=endRank; i++)  {
		   def kwId = rows.get(i.toBigDecimal()).get("searchKeywordId")
		   
		   searchKeywordIds.add(kwId)
	   }

	   def data = rwgDAO.getHeatmapDataCTA(analysisIdsList, searchKeywordIds)
	   // add the data returned to the rows
	   def r = 0
	   for (def i=startRank; i<=endRank; i++)  {
		   def row = rows.get(i.toBigDecimal())
		   def kwId = row.get("searchKeywordId")
		   
		   def rowData = [:]
		   def c = 0
		   analysisIdsList.each {aId ->
			   def colData = data?.get(aId.toString())?.get(kwId.toString())
			   // no data for col, create an empty map
			   if (!colData)  {
				   colData = [:]
			   }
			   colData.put("x", c)
			   colData.put("y", r)
			   
			   rowData.put(c, colData)
			   
			   c++
		   }
		   row.put("data",  rowData)
		   r++
	   }
	   
	   ret.put("rows",  rows)
	   
	   render ret as JSON	   
   }

   def renderFavoritesTemplate = {
	   
	   def html
	   	   
	   def favorites = getFavorites(params.searchType)
	   
	   render(template:'loadFavoritesModal', model:[favorites:favorites]).toString()	   	   	   
			  
   }
   
   def renderHomeFavoritesTemplate = {
	   
	   def html
	   
	   def favorites = getFavorites(params.searchType)

	   def title, id, isXT
	   if (params.searchType == 'XT')  {
		   title = 'Saved Cross Trial Analyses'
		   id =  'nonxt'
		   isXT = true
	   }
	   else  {
		   title = 'Saved Filters'
		   id =  'nonxt'
		   isXT = false
	   }

	   render(template:'favorites', model:[favorites:favorites, title:title, id:id, isXT:isXT]).toString()
			  
   }

      
   // Save the faceted search to database
   def saveFacetedSearch = {
	   	   
	   def name = params.name	   
	   def keywords = params.keywords
	   def analysisIds = params?.analysisIds
	   def searchType = params.searchType
	   
	   def authPrincipal = springSecurityService.getPrincipal()
	   def userId = authPrincipal.id   

	   SavedFacetedSearch s = new SavedFacetedSearch()
	   s.name = name
	   s.keywords = keywords
	   s.analysisIds = analysisIds
	   s.userId = userId
	   s.searchType = searchType
	   
	   boolean successFlag
	   def msg = ""	   

	   if (s.save()) {
		   successFlag = true
		   
		   msg = message(code: "search.SavedFacetedSearch.save.success")
		   
  	       log.info("Saved faceted search ${name} for userId ${userId}")
	   }
	   else {
		   String errorString =  s.errors.toString()

		   if (errorString.contains("search.SavedFacetedSearch.name.unique.error") )  {
			   msg = message(code: "search.SavedFacetedSearch.name.unique.error")
		   }
		   else  {
			   msg = message(code: "search.SavedFacetedSearch.save.failed.default")
		   }
		   successFlag = false
		   
		   log.info("Failed to save faceted search ${name} for userId ${userId}.  Error:" + errorString)
		   
	   }
	   	   
	   JSONObject ret = new JSONObject()
	   ret.put('success', successFlag)
	   ret.put('message', msg)
	   
	   response.setContentType("text/json")
	   response.outputStream << ret?.toString()
   }

   // Update the faceted search in the database
   def updateFacetedSearch = {

	   def id = params.id	   	   
	   def name = params.name	   
	   
	   def authPrincipal = springSecurityService.getPrincipal()
	   def userId = authPrincipal.id   

	   SavedFacetedSearch s = SavedFacetedSearch.findByUserIdAndId(userId, id)
	   
	   s.name = name
	   
	   boolean successFlag
	   def msg = ""	   

	   if (s.save()) {
		   successFlag = true
		   
		   msg = message(code: "search.SavedFacetedSearch.save.success")
		   
  	       log.info("Updated faceted search ${name} for userId ${userId}")
	   }
	   else {
		   String errorString =  s.errors.toString()

		   if (errorString.contains("search.SavedFacetedSearch.name.unique.error") )  {
			   msg = message(code: "search.SavedFacetedSearch.name.unique.error")
		   }
		   else  {
			   msg = message(code: "search.SavedFacetedSearch.save.failed.default")
		   }
		   successFlag = false
		   
		   log.info("Failed to update faceted search ${name} for userId ${userId}.  Error:" + errorString)
		   
	   }
	   	   
	   JSONObject ret = new JSONObject()
	   ret.put('success', successFlag)
	   ret.put('message', msg)
	   
	   response.setContentType("text/json")
	   response.outputStream << ret?.toString()
   }

      // Delete the faceted search from database
   def deleteFacetedSearch = {
			  
	   def id = params.id
	   
	   def authPrincipal = springSecurityService.getPrincipal()
	   def userId = authPrincipal.id

	   SavedFacetedSearch s = SavedFacetedSearch.findByUserIdAndId(userId, id)
	   
	   boolean successFlag
	   def msg = ""

	   if (s == null)  {
		   msg = message(code: "search.SavedFacetedSearch.delete.failed.notfound")
		   successFlag = false
	   }
	   else   {
		   s.delete()
		   
		   if (!s.hasErrors())  {
			   successFlag = true
		   
			   msg = message(code: "search.SavedFacetedSearch.delete.success")
		   
			   log.info("Deleted faceted search ${id} for userId ${userId}")
		   }
		   else {
			   String errorString =  s.errors.toString()

			   msg = message(code: "search.SavedFacetedSearch.delete.failed.default")
			   successFlag = false
		   
			   log.info("Failed to delete faceted search ${id} for userId ${userId}.  Error:" + errorString)
		   }
	   }
			  
	   JSONObject ret = new JSONObject()
	   ret.put('success', successFlag)
	   ret.put('message', msg)
	   
	   response.setContentType("text/json")
	   response.outputStream << ret?.toString()	   
   }


   // Load the faceted search keyword from database
   JSONObject getFacetedSearchKeywordsAndAnalyses(long id)  {
	   def authPrincipal = springSecurityService.getPrincipal()
	   def userId = authPrincipal.id

	   SavedFacetedSearch s = SavedFacetedSearch.findById(id)
	   
	   boolean successFlag
	   def msg = ""

		  
	   JSONObject searchTerms = new JSONObject()
	   JSONObject analyses = new JSONObject()
	   
	   int i = 0  // keyword order/count
	   def j = 0;   // analysis order/count
	   int termsNotFound = 0
	   int analysesNotFound = 0
	   
	   if (s == null)  {
		   msg = message(code: "search.SavedFacetedSearch.load.failed.notfound")
		   successFlag = false
	   }
	   else   {
		   def keywords = s.keywords

		   // convert the keywords string to a list of search keyword ids
		   def ids = keywords.tokenize('|')

		   ids.each {
			   JSONObject termArray = new JSONObject()   // json object for current search term
			   def skId = it    // search keyword id
			
			   // do thru an HQL query to make faster?
			   SearchKeyword sk = SearchKeyword.get(skId)
			   
			   if (sk)  {
				   termArray.put("id", skId.toString())
				   termArray.put("keyword", sk.keyword)
				   termArray.put("categoryId", sk.dataCategory)
				   termArray.put("categoryDisplay", sk.displayDataCategory)
				   termArray.put("categorySOLR", getSOLRCategoryName(sk.dataCategory))
				   
				   searchTerms.put(i.toString(), termArray)
				   i++
			   }
			   else  {
				   termsNotFound++
			   }
		   }

		   def analysesPiped = s?.analysisIds
		   
		   if (analysesPiped)  {
			   // convert the analyses string to a list of analysis ids
			   def aIds = analysesPiped.tokenize('|')

				// retrieve information for each analysis
			   def results = bio.BioAssayAnalysis.executeQuery("select b.id, b.longDescription " +
						  						" from bio.BioAssayAnalysis b" +
												  " where b.id in (" + aIds.join(',') +  ") ")
					  			   
			   aIds.each {aId->
 				   JSONObject analysisArray = new JSONObject()   // json object for current analyses
 				   def found = false
				   for (def r=0; r<results.size(); r++)  {
					  def row = results[r]
				      if (row[0].toString() == aId.toString())  {
						  analysisArray.put('id', aId.toString())
						  analysisArray.put('title', row[1].replace('_', ', '))
						  
						  def studyInfo = bio.BioAnalysisAttribute.executeQuery("select distinct studyID from BioAnalysisAttribute where bioAssayAnalysisID=?", [row[0]] )
						  
						  def studyId = studyInfo[0];   // assume only one row returned - if no, there is a data issue
						  analysisArray.put('studyId', studyId)
						  
						  analyses.put(j.toString(), analysisArray)
						  j++
						  found = true
						  break						  
					  }    	   
				   }
				   if (!found)  {
						  analysesNotFound++
				   }
			   }
		   }
		   		   
		   successFlag = true
		   msg = ""
	   }
			  
	   JSONObject ret = new JSONObject()
	   ret.put('success', successFlag)
	   ret.put('message', msg)
	   ret.put('searchTerms', searchTerms)
	   ret.put('analyses', analyses)
	   ret.put('keywordCount', i)
	   ret.put('analysisCount', j)
	   ret.put('termsNotFound', termsNotFound)
	   ret.put('analysesNotFound', analysesNotFound)
	   
   }   
   
   // Load the saved faceted search from database (called as action from frontend)
   def loadFacetedSearch = {
			  
	   def id = params.id as Long  // saved faceted search id
	   def searchType = params.searchType
	   
	   JSONObject ret = getFacetedSearchKeywordsAndAnalyses(id)    
	   
	   response.setContentType("text/json")
	   response.outputStream << ret?.toString()
   }

   // Load the current user's saved favorites
   def getFavorites = {searchType->
	   
	   def authPrincipal = springSecurityService.getPrincipal()

	   def userId = authPrincipal.id   
	   def favorites = SavedFacetedSearch.findAllByUserIdAndSearchType(userId, searchType, [sort:"createDt", order:"desc"])
	   
	   return favorites	   
			  
   }
   
   
   
   
   
   
   
   
   def getCrossTrialAnalysis={
	   
	   
	   def html
		   html = g.render(template:'/RWG/crossTrialAnalysis').toString()
	   
	   render(html)
	   
   }
   
   //Render the home page template
   def getHomePage = {
	   
	   def ta=SearchTaxonomy.findByTermName('Therapeutic Areas') //default to ta
	   def d=SearchTaxonomy.findByTermName('Disease') //default to disease
	   def currentsubcategoryid;
	   def currentcharttype="studies";
	   if(params.currentsubcategoryid)
	   {
		   currentsubcategoryid=params.currentsubcategoryid.toLong()
	   }
	   else
	   {
		   currentsubcategoryid=d.id
	   }
	   if(params.currentcharttype)
	   {
		   currentcharttype=params.currentcharttype;
	   }
	   def rwgDAO = new RWGVisualizationDAO()
	   def favorites = getFavorites('FACETED_SEARCH')
	   def favoritesXT = getFavorites('XT')
	   def categories;
	   def showAll=false;
	   if(params.showAll=="true")
	   {
		   categories=rwgDAO.getSearchTaxonomyChildren(ta.id)
		   showAll=true;
	   }
	   else
	   {
		  categories=rwgDAO.getCategoriesWithData(ta.id, currentsubcategoryid);
	   }
	    def subcategories=rwgDAO.getSearchTaxonomyChildren(1); //TODO: is one always root?
		def currentsubcategory=SearchTaxonomy.get(currentsubcategoryid);
	   render(template:'home', model: ['categories': categories, 'subcategories': subcategories, 'currentsubcategoryid':currentsubcategoryid, 'currentsubcategoryname':currentsubcategory.termName, 'favorites':favorites, 'favoritesXT':favoritesXT, 'currentcharttype':currentcharttype, 'showAll':showAll])

 }
 
   /**
	* Returns the data for the pie chart visualizations
	*/
   def getPieChartData = {
	   def rwgDAO = new RWGVisualizationDAO()
	   def m = rwgDAO.getPieChartData(params.catid, params.ddid, params.boolean('drillback'), params.charttype)
	   render m as JSON
   }
   
   
   
   def getTopGenes = {
	   def rwgDAO = new RWGVisualizationDAO()
	   def r = rwgDAO.getTopGenesByFoldChange(params.analysisID)
	   
	   render r as JSON
	   
   } 
   
   
   def getSearchKeywordIDfromExternalID = {
	   def rwgDAO = new RWGVisualizationDAO()
	   def r = rwgDAO.getSearchKeywordIDfromExternalID(params.externalID)
	   
	 //  def map = [:]
	 //  map.put("search_keyword", r)
	   
	   println "r = " +r
	   
	   render r
   }
   
   
   def getCrossTrialSummaryTableStats = {
	   def rwgDAO = new RWGVisualizationDAO()
	   def r = rwgDAO.getCrossTrialSummaryTableStats(params.analysisList)
	   
	   render r as JSON
	   
   }
   
   
   
   
   def getCrossTrialBioMarkerSummary = {
	   def rwgDAO = new RWGVisualizationDAO()
	   def r = rwgDAO.getCrossTrialBioMarkerSummary(params.search_keyword, params.analysisList)
	   
	   render r as JSON
	   
   }
   
   
   
}
