/**
* $Id: $
* @author $Author: $
* @version $Revision: 14836 $
*/

import org.json.*

import groovy.xml.StreamingMarkupBuilder

import javax.xml.transform.TransformerFactory
import javax.xml.transform.Transformer
import javax.xml.transform.OutputKeys
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

import bio.BioAnalysisAttribute
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

import groovy.time.TimeCategory;
import groovy.time.TimeDuration;
import groovyx.net.http.HTTPBuilder

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
	   return field.toUpperCase().replace(' ', '_')
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
	  
	  // create the key that matches what we use in javascript to identify search terms
	  // assuming for now that the category and the category display are the same (with category being all caps); may
	  // need to break this out into separate fields	  
	  parent["key"] = categoryName + "|" + categoryName.toUpperCase() + ":" + parentNode.termName + ":" + id	  
	  
	  // if category, then display as folder and don't show checkbox; other levels, not a folder and show checkbox
	  parent["isFolder"] = isCategory 
	  parent["hideCheckbox"] = isCategory

	  // add custom attributes for each node 
	  parent["isCategory"] = isCategory
	  parent["categoryName"] = categoryName + "|" + categoryName.toUpperCase()

	  // create a uniqueTreeId for each node so we can identify it from it's copies
	  //  (id and key are not unique amongst copies)
	  parent["uniqueTreeId"] = uniqueTreeId


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
		  //Removing this for now, we won't have any children in our tree. We are doing browse popups.
		  //return
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
  
	  // find all relationships
	  def rels = SearchTaxonomyRels.list()
	  
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
	  
	  response.setContentType("text/json")
	  response.outputStream << categories?.toString()

   }
  
   /**
    * Create a query string for the category in the form of (<cat1>:"term1" OR <cat1>:"term2")
    */
   def createCategoryQueryString = {category, termList -> 

       // create a query for the category in the form of (<cat1>:"term1" OR <cat1>:"term2")
       String categoryQuery = ""
       for (t in termList.tokenize("|"))  {
	   
	       def queryTerm = /${category}:"${t}"/
	   
	       if (categoryQuery == "")  {
		       categoryQuery = queryTerm
	       }
	       else  {
		       categoryQuery = /${categoryQuery} OR ${queryTerm}/
	       }
       }

	   // enclose query clause in parens
       categoryQuery = /(${categoryQuery})/
	   
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
		   
		   //This list should be in a config, but we don't facet on some of the fields.
		   if(ff != "REGION_OF_INTEREST" && ff != "GENE" && ff != "SNP")
		   {
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
   public String createSOLRNonfacetedQueryString(List queryParams) {
	   def nonfacetedQuery=""
	   // loop through each regular query parameter
	   for (qp in queryParams)  {
		   
		   //Ignore REGIONs here - used later in analysis filter
		   if (qp.startsWith("REGION") || qp.startsWith("GENE") || qp.startsWith("SNP")) {
			   continue;
		   }
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
   def executeSOLRFacetedQuery = {solrRequestUrl, solrQueryParams, returnAnalysisIds ->
	   
	   println (solrQueryParams)
	   
	   JSONObject facetCounts = new JSONObject()
	   //solrQueryParams = "q=(*:*)"
	   //solrQueryParams = solrQueryParams.substring(0, solrQueryParams.lastIndexOf(")")+1)
	   //solrQueryParams += "&facet=true&rows=0&facet.field=ANALYSIS_ID"
	   
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
		   
		   if (returnAnalysisIds) {
			   
			   //outputFormattedXml(xml)
			   def analysisIds = xml.result.doc.str.findAll{it.@name == 'ANALYSIS_ID'}
			   solrConnection.disconnect()
			   def ids = []
			   for (analysisId in analysisIds) {
				   ids.push(analysisId.text() as long)
			   }
			   return ids
		   }
		   else {
		   // retrieve all the category nodes for the facet fields (contain subnodes which have the actual counts)
			   facetCategoryNodes = xml.lst.find{it.@name == 'facet_counts'}.lst.find{it.@name == 'facet_fields'}.lst
		   }
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
   *  pretty prints the GPathResult NodeChild
   *
  def outputFormattedXml(node) {
	  def xml = new StreamingMarkupBuilder().bind {
		  mkp.declareNamespace("":node.namespaceURI())
		  mkp.yield(node)
	  }
   
	  def factory = TransformerFactory.newInstance()
	  def transformer = factory.newTransformer()
	  transformer.setOutputProperty(OutputKeys.INDENT, 'yes')
   
	  // figured this out by looking at Xalan's serializer.jar
	  // org/apache/xml/serializer/output_xml.properties
	  transformer.setOutputProperty("{http\u003a//xml.apache.org/xalan}indent-amount", "2")
	  def result = new StreamResult(new StringWriter())
	  transformer.transform(new StreamSource(new ByteArrayInputStream(xml.toString().bytes)), result)
   
	  println result.writer.toString()
  }
  */

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

   /**
   * Replace any gene lists or signatures in the query parameters with a list of individual genes
   * @param params list of query params
   * @param genesField SOLR search field that gene searches are executed against (i.e. ALLGENE or SIGGENE)
   * @return string containing the new query parameters
   */
   def replaceGeneLists = {  params, genesField  ->
	   def newParams = []
	   def genesList = []  

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
		   //Commenting this out, we don't have to worry about signifigance just yet.
           //newParams.add newGeneString
	   }
	   
	   log.info("Gene parameter: ${newParams}")
	   return newParams
   }
   
   //Get analyses for current SOLR query and store them in session
   def getFacetResultsForTable = {
	   
	   def queryParams = request.getParameterValues('q')
	   
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
	   def nonfacetedQueryString = "";
	   try {
		   nonfacetedQueryString = createSOLRNonfacetedQueryString(sessionFilterParams as List)
	   }
	   catch (Exception e) {
		   e.printStackTrace()
	   }
	   
	   //TODO Patch job - if this is a *.* query, prevent it from running
	   if (nonfacetedQueryString.equals("q=(*:*)")) {
		   session['solrAnalysisIds'] = []
		   render(status: 200, text: "NONE");
		   return;
	   }
	   	   
	   String solrRequestUrl = createSOLRQueryPath()
	   String solrQueryString = /${nonfacetedQueryString}/
	   def analysisIds = executeSOLRFacetedQuery(solrRequestUrl, solrQueryString, true)
	   
	   session['solrAnalysisIds'] = analysisIds
	   render(status: 200, text: analysisIds.join(","))
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
	   queryParams = replaceGeneLists(queryParams, solrGenesField)	   
	   
	   showSigGenesOnly = false
	   
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
       JSONObject facetCounts = executeSOLRFacetedQuery(solrRequestUrl, solrQueryString, false)

	   def studyCounts = facetCounts['STUDY_ID']
	   
	   // retrieve the html string for the results template	   
	   def html = loadSearchResults(studyCounts, startTime)	   
	   // create a return json object containing both the facet counts to load into tree and html to load into results section
	   JSONObject ret = new JSONObject()
	   ret.put('facetCounts', facetCounts)
	   ret.put('html', html)
       response.setContentType("text/json")
	   response.outputStream << ret?.toString()	   
   }

   /**
   * Determine field to be used for genes within the SOLR queries and set session parameter
   * @param showSigGenesOnly boolean indicating whether analysis for all genes or only significant genes (default) will be shown
   * @return SOLR field to be used for gene searches
   */
   def setSOLRGenesField = {
	   showSigGenesOnly = true ->

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
   def getInitialFacetResults = {List categoriesList  ->
	   // initial state of the significant field is checked, so need to add the search field to the SOLR query to get the initial facet coutns
	   //  and save the search term to the session variable so that is applied to the query to get the analysis list 
	   //def queryParams = ["ANY_SIGNIFICANT_GENES:1"]
	   def queryParams = []
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

	   JSONObject facetCounts = executeSOLRFacetedQuery(solrRequestUrl, solrQueryString, false)
		
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
   
   // Return search keywords
   def searchAutoComplete = {
	   def category = params.category == null ? "ALL" : params.category
	   def max = params.long('max') ?: 15
	   render searchKeywordService.findSearchKeywords(category, params.term, max) as JSON	   
   }
        
   // Load the search results for the given search terms using the new annotation tables
   // return the html string to be rendered for the results panel
   def loadSearchResults = { studyCounts, startTime ->	   
	   def exprimentAnalysis = [:]						// Map of the trial objects and the number of analysis per trial
	   def total = 0								// Running total of analysis to show in the top banner

	   def studyWithResultsFound = false   
	   
   	   for (studyId in studyCounts.keys().sort()) {
		   def c = studyCounts[studyId].toInteger()

		   if (c > 0)  {
			   studyWithResultsFound = true
		   
			   Long expNumber= Long.parseLong(studyId)

			   def exp = Experiment.createCriteria()
			   def experiment = exp.get	{
				   eq("id", expNumber)
			   }
			   if (experiment == null)	{
				   log.warn "Unable to find an experiment for ${expNumber}"
			   }
			   else  {
				   exprimentAnalysis.put((experiment), c)
				   total += c
			   }
			   
			   /*
			   def ct = ClinicalTrial.createCriteria()
			   def trial = ct.get	{
				   eq("trialNumber", trialNumber, [ignoreCase: true])
			   }
			   if (trial == null)	{
				   log.warn "Unable to find a trial for ${trialNumber}"
			   }  
			   else  {
			       trialAnalysis.put((trial), c)
			       total += c
			   }
			   */
		   }
	   }
	   // capture html as a string that will be passed back in JSON object
	   def html
	   if (!studyWithResultsFound)	{
		   html = g.render(template:'/search/noResult').toString()
	   } else {
	       html = g.render(template:'/RWG/experiments',model:[experiments:exprimentAnalysis, analysisCount:total, duration:TimeCategory.minus(new Date(), startTime)]).toString()
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
	   def rwgDAO = new RWGVisualizationDAO()
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
   
   
   // Method to get the number of probes for the heatmap for the given filters
   def getHeatmapNumberProbes = {
	   def rwgDAO = new RWGVisualizationDAO()
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
	   def maxProbeIndex = rwgDAO.getNumberProbes(params.id, genes, showSigResultsOnly)	 
	   
	   JSONObject ret = new JSONObject()
	   ret.put('maxProbeIndex', maxProbeIndex)
	   
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
    * Returns the data for the line plot visualization
    */
   def getLinePlotData = {	   
	   def rwgDAO = new RWGVisualizationDAO()	   
	   def m = rwgDAO.getLineplotData(params.id, params.probeID)
	   render m as JSON
   }  
   
   def getPieChart = {
	   
	   
	   
	   render(template:'pie')
   }
   
   
   /**
   * This will render a UI where the user can pick a data type.
   */
  def browseDataTypesMultiSelect = {
	  
	  /*
	  def results = SearchTaxonomy.executeQuery("SELECT id, keyword FROM SearchKeyword s WHERE s.dataCategory = 'DATA_TYPE'");
	  def dataTypes = []
	  for (result in results) {
		  dataTypes.add([key:result[0], value:result[1]])
	  }
	  */
	  
	  def dataTypes = ["GWAS":"GWAS","EQTL":"eQTL","Metabolic GWAS":"Metabolic GWAS"]
	  
	  render(template:'dataTypesBrowseMulti',model:[dataTypes:dataTypes])
  }
  
  /**
   * Renders a UI for selecting regions by gene/RSID or chromosome.
   */
  def getRegionFilter = {
	  render(template:'regionFilter', model: [ranges:['both':'+/-','plus':'+','minus':'-']])
  }
   
   
}