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
  

import org.jfree.util.Log;

import groovyx.net.http.HTTPBuilder

class SolrService {
	def grailsApplication

    boolean transactional = true

	/**
	 * This method will run a faceted search on the term provided and return a hashmap with hashmap[term]=facet_count
	 * @param solrServer Base URL for the solr server.
	 * @param fieldList "|" separated list of fields.
	 * @return
	 */
    def facetSearch(JSONObject, String fieldList) 
	{
		String solrServerUrl = grailsApplication.config.com.recomdata.solr.baseURL
		
		//Get the solr Query based on the JSON object.
		def solrQuery = generateSolrQueryFromJson(JSONObject)
		
		//If the query is empty, change it to be 'return all results' in Solr language.
		if(solrQuery=="()") solrQuery="*:*";
		
		//Create the http object we will use to retrieve the faceted counts.
		def http = new HTTPBuilder(solrServerUrl)
		
		//This holds the map till we can get it into the parent map.
		def tempMap = [:]

		//This will be a map of {termType: {term (count)}} that we pass to the view.
		def termMap = [:]

		//For each of the categories, run a faceted search.
		fieldList.tokenize("|").each()
		{
			currentTerm ->
			
			//Facet the search on the field specified in the parameter.
			def html = http.get( path : '/solr/select/', query : ['q':solrQuery,'facet':'true','facet.field':currentTerm,'facet.sort':'index'] )
			{
				resp, xml ->
				
				//We should probably do something with the status.
				if(resp.status!="200") Log.error("Response status from solr web service call: ${resp.status}")
				
				//Loop through all the list items to find the one for "facet_counts".
				xml.lst.each
				{
					outerlst ->
					
					//We only want the fact_counts node.
					if(outerlst.@name=='facet_counts')
					{
						//Under this we only want the facet_fields node.
						outerlst.lst.each
						{
							innerlst ->
							
							if(innerlst.@name=='facet_fields')
							{
								innerlst.lst.each
								{
									innermostlst ->
									
									//Find the node whose "name" is our term.
									if(innermostlst.@name==currentTerm)
									{
										innermostlst.int.each
										{
											termItem ->
											
											//To the temp map add an entry with the current term name and the count of documents found.
											tempMap[termItem.@name.toString()] = termItem.toString()
										}
									}
								}
							}
						}
					}
				}
			}
			
			//The term hash goes under the current category.
			termMap[currentTerm] = tempMap
			
			//Reinitialize the temporary map variable.
			tempMap = [:]
			
		}

		return termMap
    }
	
	/**
	 * This method takes a hashmap in an expected solr layout category:[item:count] and reorders it so that the item specified by the second parameter is on top of that categories list.
	 * @param mapToModify A hashmap in the expected solr format category:[item:count,item:count]
	 * @param termToFloat A string representing the name of the item that should be preserved on top.
	 * @return
	 */
	def floatTopValue(mapToModify,termToFloat)
	{
		//For each category in the hash, we attempt to remove a term, if succesful we put it back on top.
		mapToModify.each
		{
			termList ->
			
			//Attempt to remove a term. null is returned if the term was not found.
			def valueRemoved = termList.value.remove(termToFloat)

			//If a value is removed, the value returned is the value of the term.
			if(valueRemoved)
			{
				//The new map with the desired ordering.
				def newMap = [:]

				//Add our item first.
				newMap[termToFloat] = valueRemoved
				//Put the rest of the items back in the list.
				newMap.putAll(termList.value)

				//Assign the map back to the category list.
				termList.value = newMap
			}

		}

		return mapToModify
	}
	
	/**
	 * This method will pull "documents" from solr based on the passed in JSON Criteria.
	 * @param solrServer Base URL for the solr server.
	 * @param JSONObject An object that looks like {"SearchJSON":{"Pathology":["Liver, Cancer of","Colorectal Cancer"]}}
	 * @param resultColumns The list of columns we want returned.
	 * @param maxRows Solr requires that we specify the max rows we want returned. We should feed in a number much higher than the number of rows we ever expect.
	 * @return
	 */
	def pullResultsBasedOnJson(JSONObject,String resultColumns)
	{
		//Get the solr Query based on the JSON object.
		def solrQuery = ""
		
		//If we have the detailed records JSON, we use a different method to parse the JSON.
		if(JSONObject.Records)
		{
			solrQuery = generateSolrQueryFromJsonDetailed(JSONObject)
		}
		else
		{
			solrQuery = generateSolrQueryFromJson(JSONObject)
		}
		println(solrQuery)
		//Get the max rows and URL from the config file.
		String solrMaxRows = grailsApplication.config.com.recomdata.solr.maxRows
		String solrServerUrl = grailsApplication.config.com.recomdata.solr.baseURL
			
		//If the query is empty, abort here.
		if(solrQuery=="()") return ['results':[]];
		
		//Construct the rest of the query based on the columns we want back and the number of rows we want.
		solrQuery += "&fl=" + resultColumns + "&sort=id desc&rows=" + solrMaxRows
		
		//Throw our query into the debugger log in the event we ever need to analyze it.
		log.debug("Printing solr Query to be run")
		log.debug(solrQuery)
		
		//Create the http object we will use to retrieve the data that meets our criteria.
		def http = new HTTPBuilder(solrServerUrl)

		//We want [results:[{'Pathology':'blah','Tissue':'blah'},{'Pathology':'blah','Tissue':'blah'}]]
		
		//This will be the hash to store our results.
		def resultsHash = [:]
		
		//Find the results based on the solr query.
		def html = http.get( path : '/solr/select/', query : ['q':solrQuery] )
		{
			resp, xml ->
			
			//We should probably do something with the status.
			if(resp.status!="200") Log.error("Response status from solr web service call: ${resp.status}")
			
			//For now we are going to create a hash which will group our rows for us.
			xml.result.doc.each
			{
				resultDoc ->
				
				//This string will hold the text for each column in the output.
				String resultConcat = ""
				String resultID = ""
				
				resultDoc.str.each
				{
					//If this isn't the first column add a seperator.
					if(resultConcat!="") resultConcat+="|"

					//Add tag name : tag value to the hash key.
					resultConcat += it.@name.toString() + "?:?:?" + it.toString()
				}
				
				//If a hash entry doesn't exist for the data concat'ed together, create one.
				if(!resultsHash[resultConcat]) resultsHash[resultConcat] = 0;
				
				//Increment the hash for our concat'ed string.
				resultsHash[resultConcat] += 1
			}
		}
		
		//This will be the final hash we pass out of this function.
		def finalHash = ['results':[]];

		//Now that we have this ugly hash we have to convert it to a meaningful hash that can be parsed into JSON.
		resultsHash.each
		{
			hashKey ->
			
			//We build a hash with an entry for each field, and the value for that field.
			def tempHash = [:];
			
			//For each of the keys break on the "|" character.
			hashKey.key.toString().tokenize("|").each 
			{
				//Within each "|" there is a funky set of characters that delimits the field:value.
				def keyValueBreak = it.tokenize("?:?:?");
				
				//Add the key/value to the hash.
				tempHash[keyValueBreak[0]] = keyValueBreak[1];
			}
			
			//Each value of the parent hash is actually a count of how many of items matching the key were found.
			tempHash['count'] = hashKey.value;
			
			//Add this category to the final hash.
			finalHash['results'].add(tempHash);
		}
				
		//Return the results hash.
		return finalHash
	}
	
	/**
	 * This method will run a solr 'terms' query with a prefix on the provided list of columns and return some results.
	 * @param solrServer Base URL for the solr server. 
	 * @param fieldList "," separated list of fields that we search for the term within.
	 * @param termPrefix We search for values like this prefix.
	 * @return We want the hash to look like ['Pathology:['SomeDisease':22,'SomeOtherDisease':33],'Tissue':['Skin':32]]
	 */
	def suggestTerms(String fieldList, String termPrefix,String numberOfSuggestions)
	{
		//Get the URL from the config file.
		String solrServerUrl = grailsApplication.config.com.recomdata.solr.baseURL
		
		//Create the http object we will use to retrieve the search terms.
		def http = new HTTPBuilder(solrServerUrl)

		def resultMapList = ['rows':[]]
		
		//Facet the search on the field specified in the parameter.
		def html = http.get( path : '/solr/terms', query : ['terms.regex':termPrefix.toString() + ".*",'terms.fl':fieldList.tokenize(","),'rows':numberOfSuggestions, 'terms.regex.flag':'case_insensitive'] )
		{
			resp, xml ->
			
			//We should probably do something with the status.
			if(resp.status!="200") Log.error("Response status from solr web service call: ${resp.status}")
			
			//For each lst we look for the "terms" one.
			xml.lst.each
			{
				outerlst ->
				
				//If we are on the terms one, we cycle through the children.
				if(outerlst.@name=='terms')
				{
					//For each of these lst tags with int children we need to create an entry in the result hash.
					outerlst.lst.each
					{
						innerlst ->
						
						//If this lst has children, add an entry to the result hash.
						innerlst.int.each
						{
							termItem ->

							//Create a temporary hash to hold the mapped results.
							def tempMap = [:]
														
							//To the temp map add entries for the category and display.
							tempMap['id'] = innerlst.@name.toString() + "|" + termItem.@name.toString()
							tempMap['source'] = ""
							tempMap['keyword'] = termItem.@name.toString() + " (" + termItem.toString() + ")"
							tempMap['synonyms'] = ""
							tempMap['category'] = innerlst.@name.toString()
							tempMap['display'] = innerlst.@name.toString().replace("_"," ")
							
							//Add the mapping to our master map.
							resultMapList['rows'].add(tempMap)
						}
						
					}
				}
			}
		}
		
		return resultMapList
			
	}
	
	
	/**
	 * Based on the JSON object passed in we run a query and return only the ID's.
	 * @param solrServer Base URL for the solr server.
	 * @param JSONObject An object that looks like {"SearchJSON":{"Pathology":["Liver, Cancer of","Colorectal Cancer"]}}
	 * @param maxRows Solr requires that we specify the max rows we want returned. We should feed in a number much higher than the number of rows we ever expect.
	 * @return
	 */
	def getIDList(JSONObject)
	{
		//Get the max rows and URL from the config file.
		String solrMaxRows = grailsApplication.config.com.recomdata.solr.maxRows
		String solrServerUrl = grailsApplication.config.com.recomdata.solr.baseURL
		
		//Get the solr Query based on the JSON object.
		def solrQuery = ""
		
		//If we have the detailed records JSON, we use a different method to parse the JSON.
		if(JSONObject.Records)
		{
			solrQuery = generateSolrQueryFromJsonDetailed(JSONObject)
		}
		else
		{
			solrQuery = generateSolrQueryFromJson(JSONObject)
		}
		
		//If the query is empty, abort here.
		if(solrQuery=="()") return []
		
		//Construct the rest of the query based on the columns we want back and the number of rows we want.
		solrQuery += "&fl=id&rows=" + solrMaxRows
		
		//Create the http object we will use to retrieve the data that meets our criteria.
		def http = new HTTPBuilder(solrServerUrl)

		//We need a list of ID's from the documents.
		def IdList = []
		
		//Throw our query into the debugger log in the event we ever need to analyze it.
		log.debug("Printing solr Query to be run")
		log.debug(solrQuery)
				
		//Find the results based on the JSON object.
		def html = http.get( path : '/solr/select/', query : ['q':solrQuery] )
		{
			resp, xml ->
			
			//We should probably do something with the status.
			if(resp.status!="200") Log.error("Response status from solr web service call: ${resp.status}")
			
			//For each document we expect a str tag that has the id in it.
			xml.result.doc.each
			{
				resultDoc ->
				
				resultDoc.str.each
				{
					//Add the ID to our list.
					IdList.add(it.toString());
				}
			}
		}
		
		//Return the list of ID's.
		return IdList;
	}
	
	/**
	 * Gets a list of all the available fields from Solr.
	 */
	def getCategoryList(String fieldExclusionList)
	{
		//Get the URL from the config file.
		String solrServerUrl = grailsApplication.config.com.recomdata.solr.baseURL
		
		//Create the http object we will use to retrieve the field list.
		def http = new HTTPBuilder(solrServerUrl)

		def resultList = []

		//The luke request handler returns schema data.
		def html = http.get( path : '/solr/admin/luke' )
		{
			resp, xml ->
			
			//For each lst we look for the "fields" node.
			xml.lst.each
			{
				outerlst ->

				//If we are on the "fields" node, we cycle through the children.
				if(outerlst.@name=='fields')
				{
					//For each of these lst tags we grab the name attribute which represents a field name.
					outerlst.lst.each
					{
						innerlst ->

						//We don't want to return the fields in the exclusion list.
						if(!(fieldExclusionList.contains(innerlst.@name.toString() + "|")))
						{
							//Add the mapping to our master map.
							resultList.add(innerlst.@name.toString())
						}
					}
				}
			}
			
			return resultList
		}
			
	}
	
	/**
	 * This method does the actual work of parsing the JSON data and creating the solr Query with criteria.
	 * @param JSONObject
	 * @return
	 */
	private String generateSolrQueryFromJson(JSONObject)
	{
		//Temp string to hold our Solr Query.
		String solrQuery = "("

		//We need to generate our query using the JSON object.
		JSONObject.each
		{
			category ->

			//Only add to the query if the category has values.
			if(category.value.size() > 0 && category.key.toString() != "count" && category.key.toString() != "GridColumnList")
			{
			
				//We need to AND the groupings of categories together.
				if(solrQuery!="(") solrQuery += ") AND ("
				
				//This will tell us if we need to add an "OR" to the query.
				boolean doWeNeedOr = false
				
				//For each of the values in this category, we add onto the search string.
				category.value.each
				{
					categoryItem ->
					
					String categoryValue = categoryItem.toString()
					
					//Escape any special characters that solr has reserved. + - ! ( ) { } [ ] ^ " ~ * ? : \
					categoryValue = escapeCharList(categoryValue,["\\","+","-","!","(",")","{","}","[","]","^","\"","~","*","?",":"])
					
					//If the query is not empty we need to add an "OR" clause.
					if(doWeNeedOr) solrQuery += " OR "
					
					//For each category item we add something to the filter
					solrQuery += category.key.toString() + ":\"" + categoryValue + "\""
					
					doWeNeedOr = true
				}
			}
		}
			
		//Close the solrQuery.
		solrQuery += ")"
		
		return solrQuery
		
	}	
	
	/**
	* This method does the actual work of parsing the JSON data and creating the solr Query with criteria. The difference between this and generateSolrQueryFromJson is that this looks for JSON Criteria that needs to be interpreted as (1 AND 2 AND 3) OR (4 AND 5 AND 6).
	* @param JSONObject This should look like "Records":[{"Pathology":"Rheumatoid Arthritis","Tissue":"Synovial Membrane","DataSet":"GSE13837","DataType":"Gene Expression","Source_Organism":"Homo Sapiens","Sample_Treatment":"Tumor Necrosis Factor","Subject_Treatment":"Not Applicable","BioBank":"No","Timepoint":"Hour 0","count":3}]
	* @return
	*/
   private String generateSolrQueryFromJsonDetailed(JSONObject)
   {
	   //Temp string to hold our Solr Query.
	   String solrQuery = "("

	   //We need to generate our query using the JSON object.
	   JSONObject.Records.each
	   {
		   record ->
		   
		   //We need to OR the groupings of records together.
		   if(solrQuery!="(") solrQuery += ") OR ("
		   
		   //This will tell us if we need to add an "AND" to the query.
		   boolean doWeNeedAnd = false
		   
		   //Each record has an entry for all the category attributes.
		   record.each
		   {
			   category ->
			   
			   //Only add to the query if the category has values.
			   if(category.value.size() > 0 && category.key.toString() != "count")
			   {
			   
				   //If the query is not empty we need to add an "AND" clause.
				   if(doWeNeedAnd) solrQuery += " AND "
				   
				   //For each of the values in this category, we add onto the search string. There should be only one in the detailed case.
				   category.value.each
				   {
					   categoryItem ->
					   
					   String categoryValue = categoryItem.toString()
					   
					   //Escape any special characters that solr has reserved. + - ! ( ) { } [ ] ^ " ~ * ? : \
					   categoryValue = escapeCharList(categoryValue,["\\","+","-","!","(",")","{","}","[","]","^","\"","~","*","?",":"])
					   
					   //For each category item we add something to the filter
					   solrQuery += category.key.toString() + ":\"" + categoryValue + "\""
					   
				   }
				   
				   //Now that we have one category in the set, we need to add an AND in the future.
				   doWeNeedAnd = true
			   }
		   }		   
	   }
		   
	   //Close the solrQuery.
	   solrQuery += ")"
	   
	   return solrQuery
	   
   }
   
	
	/**
	* This method takes a.. thing (Array?) of characters and prepends a "\" to all those characters to escape them in the string passed in.
	* @param stringToEscapeIn
	* @param charactersToEscape
	* @return
	*/
   def escapeCharList(String stringToEscapeIn,charactersToEscape)
   {
	   charactersToEscape.each
	   {
		   stringToEscapeIn = stringToEscapeIn.replace(it, "\\" + it)
	   }
	   
	   return stringToEscapeIn
   }
   
   def buildSubsetList(JSONData)
   {

	   //Get the URL from the config file.
	   String solrMaxRows = grailsApplication.config.com.recomdata.solr.maxRows
	   String solrServerUrl = grailsApplication.config.com.recomdata.solr.baseURL
	   
	   //This is the hashmap that holds the results.
	   HashMap result = [:]
	   
	   //Loop for each subset.
	   JSONData.each
	   {
		   subset ->
		   
		   //Grab the Sample ID's in this subset.
		   def idList = getIDList(subset.value);
   
		   //Add the ID's to the result object.
		   result[subset.key] = idList;
	   }
	   
	   //Make sure subsets are in order.
	   result=result.sort{it.key}
	   
	   return result
   }
   
}
