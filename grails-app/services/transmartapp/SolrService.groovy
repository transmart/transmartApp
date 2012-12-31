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
  

package transmartapp

import java.util.List;

import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.json.*

import fm.FmFile;
import fm.FmFolder;
import groovy.xml.StreamingMarkupBuilder

import javax.xml.transform.TransformerFactory
import javax.xml.transform.Transformer
import javax.xml.transform.OutputKeys
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

class SolrService {
	
	def ontologyService

    boolean transactional = true

   def getSolrResults(queryParams, facetQueryParams, facetFieldsParams, returntype) {
		   
	   // build the SOLR query
	   def nonfacetedQueryString = createSOLRNonfacetedQueryString(queryParams)
	   def facetedQueryString = createSOLRFacetedQueryString(facetQueryParams)
	   def dataNodeSearchTerms = getDataNodeSearchTerms(queryParams)
	   def facetedFieldsString = createSOLRFacetedFieldsString(facetFieldsParams)
	   
	   String solrRequestUrl = createSOLRQueryPath()
	   String solrQueryString = createSOLRQueryString(nonfacetedQueryString, facetedQueryString, facetedFieldsString)
	
	   //Get initial folder map from SOLR query
	   def xml = executeSOLRFacetedQuery(solrRequestUrl, solrQueryString, false)
	   
	   if(returntype.equals('foldermap')) {
		   def folderMap = getFolderMap(xml, dataNodeSearchTerms, solrRequestUrl)

		   //Gather folders from i2b2 that match the free text search terms, if there are any
		   if (dataNodeSearchTerms) {
			   folderMap = addOntologyResults(folderMap, dataNodeSearchTerms, solrRequestUrl)
		   }
		   
		   return folderMap
	   }
	   else if(returntype.equals('JSON')) {
		   def accessions = getAccessions(xml)
		   def ontologyResult = ontologyService.searchOntology(null, dataNodeSearchTerms, 'ALL', 'JSON', accessions)
		   return ontologyResult
	   }
	   
	   return null
		   
   }
   
   def getAccessions(xml) {
	   def result = new StreamingMarkupBuilder().bind{
		   mkp.yield xml
	   }
	   println result
	   
	   def accessionNodes = xml.result.doc.str.findAll{it.@name == 'ACCESSION'}
	   
	   def accessions = []
	   for (accession in accessionNodes) {
		   accessions.push(accession.text())
	   }
	   
	   return accessions
   }
   
   def getFolderMap(xml, dataNodeSearchTerms, solrRequestUrl) {
	   
	   // retrieve all folderIds from the returned data
	   
	   def result = new StreamingMarkupBuilder().bind{
		   mkp.yield xml
	   }
	   println result
	   
	   def folderIdNodes = xml.result.doc.str.findAll{it.@name == 'FOLDERID'}
			  
	   def folderMap = [:]
	   
	   // Construct map of matching folders
	   for (node in folderIdNodes) {
		   // retrieve the category name from the xml node
		   def folderId = node.text()
		   println ("Got folderId: " + folderId)
		   def folder = FmFolder.get(folderId);
		   
		   while (folder != null) {
			   def currentEntry = folderMap[folder.folderLevel]
			   if (currentEntry == null) {
				   currentEntry = []
			   }
			   if (!currentEntry.contains(folder.id)) {
				   currentEntry.push(folder.id)
				   folderMap[folder.folderLevel] = currentEntry;
			   }
			   println ("Added: " + folder.folderLevel + ", " + folder.id);
			   folder = folder.parent
		   }
	   }
	   
	   return folderMap
   }
   
   def addOntologyResults(folderMap, dataNodeSearchTerms, solrRequestUrl) {
	   
	   def accessions = ontologyService.searchOntology(null, dataNodeSearchTerms, 'ALL', 'accession', [])
	   
	   if (!accessions) {
		   return [0:[], 1:[], 2:[], 3:[]] //No matches
	   }
	   
	   def solrQueryString = ""
	   
	   for (accession in accessions) {
		   println ("Got accession: " + accession)
		   
		   if (solrQueryString) {solrQueryString += " OR "; }
		   
		   solrQueryString += "ACCESSION:" + accession
	   }
	   
	   solrQueryString = "q=(" + solrQueryString + ")"
	   
	   def xml = executeSOLRFacetedQuery(solrRequestUrl, solrQueryString, false)
	   def newFolderMap = getFolderMap(xml, dataNodeSearchTerms, solrRequestUrl)
	   
	   //Now intersect the maps
	   
	   def keys = newFolderMap?.keySet()
	   for (key in keys) {
		   def folderList = folderMap[key]
		   def newFolderList = newFolderMap[key]
		   
		   //If the original folder list is null, then no mask is in place - just use ours
		   if (folderList == null && newFolderList?.size() > 0) {
			   folderMap[key] = newFolderList
			   continue
		   }
		   
		   def intersection = []
		   
		   //Otherwise, intersect this level
		   for (folder in newFolderList) {
				   if (folderList.contains(folder)) {
					   intersection.add(folder)
				   }
		   }
		   
		   folderMap[key] = intersection
	   }
   
   return folderMap
	   
   }
   
   /**
   * Create a query string for the category in the form of (<cat1>:"term1" OR <cat1>:"term2")
   */
  def createCategoryQueryString = {category, termList ->

	  // create a query for the category in the form of (<cat1>:"term1" OR <cat1>:"term2")
	  String categoryQuery = ""
	  for (t in termList.tokenize("|"))  {
		  
		  //If searching on text, add wildcards.
		  if (category.equals("FREETEXT")) {
			  t = "*" + t + "*";
		  }
	  
		  def queryTerm = /${category}:${t}/
	  
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
  * Create a query string for the category in the form of (<cat1>:"term1" OR <cat1>:"term2")
  */
 def getDataNodeSearchTerms = {queryParams ->

	 def datanodeterms = []
	 for (qp in queryParams)  {
		 
		 // each queryParam is in form cat1:term1|term2|term3
		 String category = qp.split(":")[0]
		 
		 if (category.equals("DATANODE") || category.equals("FREETEXT")) {
			 String termList = qp.split(":")[1]
		 
			 for (t in termList.tokenize("|"))  {
					 datanodeterms.push(t)
				 }
		 }
	 }

	 return datanodeterms
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
		  
		  // skip DATANODE search fields
		  if (category =="DATANODE")  {
			  continue;
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
		  
		  // each queryParam is in form cat1:term1|term2|term3
		  String category = qp.split(":")[0]
		  String termList = qp.split(":")[1]

			 def categoryQueryString = createCategoryQueryString(category, termList)
		  
		  // skip DATANODE search fields - handled later
		  if (category =="DATANODE")  {
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
  * Create the base URL for the SOLR request
  * @return string containing the base URL for the SOLR query
  */
  def createSOLRQueryPath = {
			 
	  String solrScheme = ConfigurationHolder.config.com.rwg.solr.scheme
	  String solrHost = ConfigurationHolder.config.com.rwg.solr.host
	  String solrPath = ConfigurationHolder.config.com.rwg.solr.path
	  String solrRequestUrl = new URI(solrScheme, solrHost, solrPath, "", "").toURL()
	  
	  return solrRequestUrl
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
	  nonfacetedQueryString, facetedQueryString, facetedFieldsString, maxRows=1000, facetFlag=false ->
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
  * Execute the SOLR faceted query
  * @param solrRequestUrl - the base URL for the SOLR request
  * @param solrQueryParams - the query string for the faceted search, to be passed into the data for the POST request
  * @return JSONObject containing the facet counts
  */
  def executeSOLRFacetedQuery = {solrRequestUrl, solrQueryParams, returnAnalysisIds ->
	  
	  println (solrQueryParams)
	  
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
	  
	  def folderIdNodes   // will store the folder IDs
	  
	  // process response
	  if (solrConnection.responseCode == solrConnection.HTTP_OK)  {
		  def xml
		  
		  solrConnection.inputStream.withStream {
			  xml = slurper.parse(it)
		  }
		  
		  solrConnection.disconnect()
		  return xml
	  }
	  else {
		  throw new Exception("SOLR Request failed! Request url:" + solrRequestUrl + "  Response code:" + solrConnection.responseCode + "  Response message:" + solrConnection.responseMessage)
	  }
  }
   
   
}
