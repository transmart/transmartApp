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

import search.SearchKeyword;

import bio.BioDataExternalCode;
import bio.ConceptCode;

import fm.FmData;
import fm.FmFile;
import fm.FmFolder;
import fm.FmFolderAssociation;
import groovy.xml.StreamingMarkupBuilder

import javax.xml.transform.TransformerFactory
import javax.xml.transform.Transformer
import javax.xml.transform.OutputKeys
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource

class SolrFacetService {
	
	def ontologyService
	def fmFolderService

    boolean transactional = true
	def searchLog = [] //Search log for debug only! Will be shared across all sessions
	
	def getCombinedResults(categoryList, page, globalOperator, passedInSearchLog) {
		
		String solrRequestUrl = createSOLRQueryPath()
		
		def searchResultIds = []
		searchLog = passedInSearchLog
		
		//For each category (except Datanode), construct a SOLR query
		for (category in categoryList) {
			searchLog += " - - - Examining category: " + category
			//Split off the operator with ::
			category = ((String) category)
			def operator = category.split("::")[1].toUpperCase()
			category = category.split("::")[0]
			
			def categoryName = category.split(":", 2)[0]
			def termList = category.split(":", 2)[1].split("\\|")
			
			//If in Browse, we're gathering folder paths. If in Analyze, we want i2b2 paths
			//CategoryResultIds is used to gather IDs returned by this category - always add to this list, don't intersect (producing OR).
			def categoryResultIds = []

			/*
			* Start HERE if we're looking for metadata (anything other than text)
			*/
			if (!categoryName.equals("text")) {
				//Make this metadata field into a SOLR query
				searchLog += "Searching for metadata: " + termList.join(",")

				def categoryQuery = createCategoryQueryString(categoryName, termList, operator)
				def solrQuery = "q=" + createSOLRQueryString(categoryQuery, "", "")
				searchLog += solrQuery
				def xml = executeSOLRFacetedQuery(solrRequestUrl, solrQuery, false)
				
				//If browse, convert this to a folder path list - if analyze, a node path list
				if (page.equals('RWG')) {
					categoryResultIds += getFolderList(xml)
				}
				else {
					categoryResultIds += getNodesByAccession(xml)
				}
			}
			
			/*
			* Start HERE if we're looking for text - and include datanodes (as OR)
			*/
			else {
				//Content: Get the list of terms, and search in text field OR the data nodes
				searchLog += "Searching for freetext: " + termList.join(",")
				
				def categoryQuery = createCategoryQueryString("text", termList, operator)
				def solrQuery = "q=" + createSOLRQueryString(categoryQuery, "", "")
				searchLog += solrQuery
				def xml = executeSOLRFacetedQuery(solrRequestUrl, solrQuery, false)
				
				//If browse, convert this to a folder path list - if analyze, a node path list
				if (page.equals('RWG')) {
					categoryResultIds += getFolderList(xml)
				}
				else {
					categoryResultIds += getNodesByAccession(xml)
				}

				//If browse, get the studies from SOLR that correspond to the returned accessions - if analyze, just add the paths.
				if (page.equals('RWG')) {
					searchLog += "Getting accessions for datanode search: " + termList
					def ontologyAccessions = ontologyService.searchOntology(null, termList, 'ALL', 'accession', null, operator)
					
					if (ontologyAccessions) {
						def solrQueryString = ""
						
						for (accession in ontologyAccessions) {
							searchLog += ("Got accession from i2b2 search: " + accession)
							
							if (solrQueryString) {solrQueryString += " OR "; }
							
							solrQueryString += "ACCESSION:\"" + accession + "\""
						}
						
						solrQueryString = "q=(" + solrQueryString + ")"
						
						searchLog += "Searching SOLR for studies with accessions: " + solrQueryString
						xml = executeSOLRFacetedQuery(solrRequestUrl, solrQueryString, false)
						categoryResultIds += getFolderList(xml)
					}
					else {
						searchLog += "No accessions found."
					}
				}
				else {
					searchLog += "Getting paths for datanode search: " + termList
					categoryResultIds += ontologyService.searchOntology(null, termList, 'ALL', 'path', null, operator)
				}
			}
			
			searchLog += "Category result IDs: " + categoryResultIds
			
			//If the master searchResultsIds list is empty, copy this in - otherwise intersect.
			//If we have nothing for a search category during an AND search, return nothing immediately!
			if (!categoryResultIds && globalOperator.equals("AND")) {
				searchLog += "No results for this category during an AND search - stopping."
				return [paths: [], searchLog: searchLog]
			}
			
			if (!searchResultIds) {
				searchLog += "Starting search results list with the above IDs."
				searchResultIds = categoryResultIds
			}
			else {
				searchLog += "Search results so far are these IDs: " + searchResultIds
				if (globalOperator.equals("AND")) {
					searchLog += "Doing hierarchical intersect for AND search."
					searchResultIds = hierarchicalIntersect(searchResultIds, categoryResultIds)
				}
				else {
					searchLog += "Combining for OR search."
					searchResultIds = searchResultIds + categoryResultIds
				}
				searchLog += "Search results after combining: " + searchResultIds
			}
			
		}
		
		//And return the complete list of folder/i2b2 paths!
		return [paths: searchResultIds, searchLog: searchLog]
		
	}
	
	def hierarchicalIntersect(searchResults, categoryResults) {
		
		//Add to the list if:
		//This is the first category result (handled above)
		//The current path exactly matches an existing path
		//There is a path in the existing path results that is an exact subset of the current path
		//The current path is an exact subset of an existing path result
		for (result in categoryResults) {
			for (existingResult in searchResults) {
				if (existingResult.startsWith(result) || result.startsWith(existingResult)) {
					searchResults += result; break;
				}
			}
		}
		
		//Find the maximum depth
		def maxDepth = 0
		for (result in searchResults) {
			def depth = result.split("\\\\").size()
			if (maxDepth < depth) {maxDepth = depth;}
		}
		
		def newSearchResults = []
		for (result in searchResults) {
			//If this result is below the maximum depth, check for a superset of this result - if found, add it.
			def depth = result.split("\\\\").size()
			if (depth == maxDepth) { newSearchResults.add(result) }
			else {
				for (comparedResult in searchResults) {
					if (comparedResult.startsWith(result) && !comparedResult.equals(result)) {
						//Superset found
						newSearchResults.add(result)
						break
					}
				}
			}
		}
		
		return newSearchResults
		
	}
	
//	def convertTermList(category, termList) {
//		def textList = []
//		
//		//Find terms and synonyms depending on category
//		if (category.equals("GENE") || category.equals("DISEASE") || category.equals("COMPOUND") || category.equals("PATHWAY")) {
//			
//			if (category.equals("GENE")) {
//				//Expand the term list
//				def newTermList = []
//				for (term in termList) {
//					def splitList = term.split("/")
//					for (item in splitList) {
//						newTermList += item
//					}
//				}
//				termList = newTermList
//			}
//			
//			for (term in termList) {
//				
//				def sk = SearchKeyword.findByUniqueId(term)
//				textList += sk.keyword.toLowerCase()
//				def synonyms = BioDataExternalCode.findAllWhere(bioDataId: sk.bioDataId, codeType: "SYNONYM")
//				for (s in synonyms) {
//					textList += s.code
//				}
//			}
//		}
//		//Fields of a program target are plain text and shouldn't be altered
//		else if (category.equals("MEASUREMENT_TYPE") || category.equals("VENDOR") || category.equals("TECHNOLOGY") || category.equals ("PROGRAM_TARGET")) {
//			textList = termList
//		}
//		//Assume ConceptCode UID for everything else
//		else {
//			for (term in termList) {
//				def cc = ConceptCode.findByUniqueId(term)
//				def lowerCaseName = cc.codeName.toLowerCase()
//				textList += lowerCaseName
//			}
//		}
//		
//		return textList
//	}

//   def getSolrResults(queryParams, facetQueryParams, facetFieldsParams, returntype) {
//		   
//	   // build the SOLR query
//	   def nonfacetedQueryString = createSOLRNonfacetedQueryString(queryParams)
//	   def facetedQueryString = createSOLRFacetedQueryString(facetQueryParams)
//	   def dataNodeSearchTerms = getDataNodeSearchTerms(queryParams)
//	   def facetedFieldsString = createSOLRFacetedFieldsString(facetFieldsParams)
//	   
//	   String solrRequestUrl = createSOLRQueryPath()
//	   String solrQueryString = createSOLRQueryString(nonfacetedQueryString, facetedQueryString, facetedFieldsString)
//	
//	   //Get initial folder map from SOLR query
//	   def xml = executeSOLRFacetedQuery(solrRequestUrl, solrQueryString, false)
//	   
//	   if(returntype.equals('foldermap')) {
//		   def folderSearchList = getFolderList(xml, dataNodeSearchTerms, solrRequestUrl)
//
//		   //Gather folders from i2b2 that match the free text search terms, if there are any
//		   if (dataNodeSearchTerms) {
//			   folderSearchList = addOntologyResults(folderSearchList, dataNodeSearchTerms, solrRequestUrl, "OR")
//		   }
//		   
//		   return folderSearchList
//	   }
//	   else if(returntype.equals('JSON')) {
//		   def accessions = getAccessions(xml)
//		   def ontologyResult = ontologyService.searchOntology(null, dataNodeSearchTerms, 'ALL', 'JSON', accessions)
//		   return ontologyResult
//	   }
//	   
//	   return null
//		   
//   }
   
   def getNodesByAccession(xml) {
	   def accessions = getAccessions(xml)
	   if (!accessions) {
		   return []
	   }
	   
	   //If we have any accessions, return the node paths from i2b2 (on the study level)
	   else {
		   searchLog += "Finding study paths in i2b2 with these accessions: " + accessions
		   def results = ontologyService.searchOntology(null, null, 'ALL', 'path', accessions, "")
		   searchLog += "Got paths: " + results
		   return results
	   }
   }
   
   def getAccessions(xml) {
	   searchLog += "Getting accessions from SOLR search results"
	   def result = new StreamingMarkupBuilder().bind{
		   mkp.yield xml
	   }
	   
	   def resultNodes = xml.result.doc
	   
	   def accessions = []
	   
	   for (node in resultNodes) {
		   
		   def folderId
		   //Use "folder" if this is a file result, "id" otherwise
		   def folderNode = node.str.findAll{it.@name == 'folder'}
		   if (folderNode.size() > 0) {
			   folderId = folderNode.text()
			   searchLog += "Got folder ID from SOLR file result: " + folderId
		   } else {
		   		def idNode = node.str.findAll{it.@name == 'id'}
				if (idNode.size() > 0) {
				   folderId = idNode.text()
				   searchLog += "Got folder ID from SOLR folder result: " + folderId
				} else {
					log.error "SolrFacetService.getAccessions: result node does not contain an id or folder"
				}
		   }
		   
		   def fmFolder = FmFolder.findByUniqueId(folderId)
		   searchLog += "Finding associated accession for folder: " + folderId
		   def accession = fmFolderService.getAssociatedAccession(fmFolder)
		   if (accession) {
			   searchLog += "Got accession: " + accession
			   accessions.push(accession)
		   }
		   else {
			   searchLog += "No accession found"
		   }
	   }
	   
	   return accessions
   }
   
   def getFolderList(xml) {
	   
	   //retrieve all folderUIDs from the returned data
	   
	   def result = new StreamingMarkupBuilder().bind{
		   mkp.yield xml
	   }
	   log.debug result
	   
	   def resultNodes = xml.result.doc
	   
	   def folderSearchList = [];
	   for (node in resultNodes) {
		   
		   def folderId
		   //Use "folder" if this is a file result, "id" otherwise
		   def folderNode = node.str.findAll{it.@name == 'folder'}
		   if (folderNode.size() > 0) {
			   folderId = folderNode.text()
			   searchLog += "Got folder ID from SOLR file result: " + folderId
		   } else {
		   		def idNode = node.str.findAll{it.@name == 'id'}
				if (idNode.size() > 0) {
				   folderId = idNode.text()
				   searchLog += "Got folder ID from SOLR folder result: " + folderId
				} else {
					log.error "SolrFacetService.getFolderList: result node does not contain an id or folder"
				}
		   }
		   
		   def fmFolder = FmFolder.findByUniqueId(folderId)
		   folderSearchList.push(fmFolder?.folderFullName)
	   }
	   
	   return folderSearchList
   }
   
   // Add ontology results to the current search
//   def addOntologyResults(folderList, dataNodeSearchTerms, solrRequestUrl, operator) {
//	   
//	   def accessions = ontologyService.searchOntology(null, dataNodeSearchTerms, 'ALL', 'accession', [])
//	   
//	   if (!accessions) {
//		   if (operator.equals("AND")) {
//			   return "" //No matches
//		   }
//		   else {
//			   return folderList
//		   }
//	   }
//	   
//	   def solrQueryString = ""
//	   
//	   for (accession in accessions) {
//		   log.debug("Got accession: " + accession)
//		   
//		   if (solrQueryString) {solrQueryString += " OR "; }
//		   
//		   solrQueryString += "ACCESSION:\"" + accession + "\""
//	   }
//	   
//	   solrQueryString = "q=(" + solrQueryString + ")"
//	   
//	   def xml = executeSOLRFacetedQuery(solrRequestUrl, solrQueryString, false)
//	   def newFolderList = getFolderList(xml, dataNodeSearchTerms, solrRequestUrl)
//	   
//	   //Now intersect/add to the lists
//   
//	   if (operator.equals("AND")) {
//		   if (!folderList) { return newFolderList; }
//		   return folderList.intersect(newFolderList)
//	   }
//	   
//	   return folderList + newFolderList //Union
//   }
   
   /**
   * Create a query string for the category in the form of (<cat1>:"term1" OR <cat1>:"term2")
   */
  def createCategoryQueryString = {category, termList, operator ->

	  // create a query for the category in the form of (<cat1>:"term1" OR <cat1>:"term2")
	  String categoryQuery = ""
	  for (t in termList)  {
		  
		  t = cleanForSOLR(t)
		  
		  //If searching on text, add wildcards (instead of quote marks)
		  if (category.equals("text")) {
			  t = ("\"" + t.toLowerCase() + "\"");
		  }
		  else if (category.equals("GENE")) {
			  //GENE may have individual genes separated by slashes. OR these, and quote each individual one
			  def geneList = []
			  for (g in t.split("/")) {
				  geneList += ("\"" + g + "\"")
			  }
			  t = "(" + geneList.join(" OR ") + ")"
		  }
		  else {
			  t = ("\"" + t + "\"")
		  }
	  
		  def queryTerm = /${category}:${t}/
	  
		  if (categoryQuery == "")  {
			  categoryQuery = queryTerm
		  }
		  else  {
			  categoryQuery = /${categoryQuery} ${operator} ${queryTerm}/
		  }
	  }

	  // enclose query clause in parens
	  categoryQuery = /(${categoryQuery})/
	  
	  return categoryQuery
 }
  
  def cleanForSOLR(t) {
	  return t.replace("&", "%26").replace("(", "\\(").replace(")", "\\)");
  }
  
  /**
  * Create a query string for the category in the form of (<cat1>:"term1" OR <cat1>:"term2")
  */
 def getDataNodeSearchTerms = {queryParams ->

	 def datanodeterms = []
	 for (qp in queryParams)  {
		 
		 // each queryParam is in form cat1:term1|term2|term3
		 String category = qp.split(":")[0]
		 
		 if (category.equals("DATANODE") || category.equals("text")) {
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
		  String category = qp.split(";")[0]
		  String termList = qp.split(";")[1]
		  
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
		  String category = ((String) qp).split(":", 2)[0]
		  String termList = ((String) qp).split(":", 2)[1]

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
  
  def createSOLRUpdatePath = {
	  
		String solrScheme = ConfigurationHolder.config.com.rwg.solr.scheme
		String solrHost = ConfigurationHolder.config.com.rwg.solr.host
		String solrPath = ConfigurationHolder.config.com.rwg.solr.update.path
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
	  
	  log.debug(solrQueryParams)
	  
	  JSONObject facetCounts = new JSONObject()
	  
	  def slurper = new XmlSlurper()

		 // submit request
	  def solrConnection = new URL(solrRequestUrl).openConnection()
	  solrConnection.requestMethod= "POST"
	  solrConnection.doOutput = true

	  // add params to request
	  def dataWriter = new OutputStreamWriter(solrConnection.outputStream)
	  dataWriter.write(solrQueryParams)
	  dataWriter.write("&fl=id,folder")
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
  
  def reindexFolder = {folderUid, folderType = "" ->
	  
	  def solrRequestUrl = createSOLRUpdatePath()
	  def solrUpdateParams = "command=full-import&commit=true&clean=false&uid=" + folderUid
	  if (folderType) {
		  solrUpdateParams += "&entity=" + folderType
	  }
	  
	  // submit request
	  def solrConnection = new URL(solrRequestUrl).openConnection()
	  solrConnection.requestMethod= "POST"
	  solrConnection.doOutput = true

	  // add params to request
	  def dataWriter = new OutputStreamWriter(solrConnection.outputStream)
	  dataWriter.write(solrUpdateParams)
	  dataWriter.flush()
	  dataWriter.close()
	  
	  //If HTTP OK, return success
	  if (solrConnection.responseCode == solrConnection.HTTP_OK)  {
		  solrConnection.disconnect()
		  return true
	  }
	  else {
		  throw new Exception("SOLR update failed! Request url:" + solrRequestUrl + "  Response code:" + solrConnection.responseCode + "  Response message:" + solrConnection.responseMessage)
	  }
	  
  }
   
   
}
