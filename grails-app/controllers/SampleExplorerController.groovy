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
  

import org.transmart.searchapp.AccessLog;

import com.recomdata.snp.SnpData
import grails.converters.JSON


/**
 * Class for controlling the Sample Explorer page.
 * @author MMcDuffie
 *
 */
class SampleExplorerController {

	//This server is used to access security objects.
	def springSecurityService
	//This service is used to communicate with solr.
	def solrService

	def i2b2HelperService;
	
	//This is to show what the SearchJSON looks like.
	//{"SearchJSON":{"DataSet":["GSE12391"],"DataType":["Gene Expression","RBM"],"Pathology":["Melanoma","Normal"]}}
	
	/**
	 * If we hit just the index, we need to log an event and redirect to the list page.
	 */
	def index =
	{
		//Create an event record for this access.
		def al = new AccessLog(username: springSecurityService.getPrincipal().username, event:"SampleExplorer-Summary", eventmessage:"Sample Explorer summary page", accesstime:new Date())
		al.save();

		redirect(action:'list')
	}
	
	/**
	 * Display all the summary links.
	 */
	def list =
	{
		render(view: "sampleExplorer", model:[columnList:verifyFieldList().join(",")])
	}
	
	/**
	 * This shows the page that has different groups for each category, and the links to filter with.
	 */
	def showTopLevelListPage =
	{
		//Grab the list of fields we will concern ourselves with.
		String solrFieldList = verifyFieldList().join("|");
		
		//Call the solr service to get a hash that looks like category:[item:count]. We pass in an empty string because we want all the documents in the solr search.
		def termMap = solrService.facetSearch("",solrFieldList)

		//Render the list of links in their own categories.
		render(template:"searchTopLevel", model:[termsMap:termMap]);
	}
	
	def showMainSearchPage =
	{
		//We need to pass in the top X news stories so we can draw them on the screen.
		def newsUpdates = NewsUpdate.list(max:grailsApplication.config.com.recomdata.solr.maxNewsStories, sort:"updateDate", order:"desc")
		
		render(template:"categorySearch", model:[newsUpdates:newsUpdates]);
	}
	
	/**
	 * Show the box to the west that has the category links with checkboxes.
	 */
	def showWestPanelSearch =
	{
		//Grab the list of fields we will concern ourselves with.
		String solrFieldList = verifyFieldList().join("|");
		
		//Call the solr service to get a hash that looks like category:[item:count].
		def termMap = solrService.facetSearch(request.JSON.SearchJSON,solrFieldList)
		
		//We need to recreate the hash with the item we selected on top so it doesn't get lost in the list collapse.
		//Taking this out. Solr will return the highest count items first, so our selections shouldn't get pushed into the "More [+]" menu.
		//termMap = solrService.floatTopValue(termMap,mainSearchTerm)
		
		//Render the list of checkboxes and links based on the items in our search JSON.
		render(template:"categorySearchWithCheckboxes", model:[termsMap:termMap,JSONData:request.JSON.SearchJSON]);
	}

	/**
	 * This draws the simple HTML page that has the DIV that gets populated by the ExtJS datagrid.	
	 */
	def showDataSetResults =
	{
		// With the Grails 2.0 upgrade, the straight render does not work here.  Using the g tag to get the content and then render it
		String content = g.render(template:"dataSetResults")
		render content
	}
	
	/**
	 * This will pull a result set from Solr using a query based on the JSON data passed in. Returns results as JSON.	
	 */
	def getDataSetResults =
	{
		
		//Grab the string of columns we want in the results.
		String solrResultColumns = verifyGridFieldList().join(",")
		//Grab the string for the maximum number of result rows to return.
		String solrMaxRows = grailsApplication.config.com.recomdata.solr.maxRows
		
		//In the JSON result there is a list of the columns we expect to get back.
		String selectedResultColumns = request.JSON.SearchJSON.GridColumnList.join(",").replace("\"","")
		
		//This will be the hash to store our results.
		def resultsHash = solrService.pullResultsBasedOnJson(request.JSON.SearchJSON,selectedResultColumns)

		render resultsHash as JSON
	}
	
	/**
	 * This method will return a JSON object representing the items that match the users search.
	 */
	def loadSearch =
	{
		//Grab the categories from the form. They might be "All".
		def category = params.query.substring(0, params.query.indexOf(":"))
		
		//If all categories are being searched, look in session to get the list.
		if(category=="all") category = verifyFieldList().join(",");
		
		//Grab the value to search for.
		def values = params.query.substring(params.query.indexOf(":") + 1)
		
		//Get the list of possible results.
		def resultsHash = solrService.suggestTerms(category,values,grailsApplication.config.com.recomdata.solr.numberOfSuggestions.toString())

		//Render the results as JSON.
        render(text:params.callback + "(" + (resultsHash as JSON) + ")", contentType:"application/javascript")
	}
	
	/**
	 * This method checks to make sure the list of fields we want to use are in session. If they aren't, it adds them to the session.
	 */
	def verifyFieldList =
	{
		//This field list always has all the fields we want to display.
		if(!session['fieldList']) session['fieldList'] = loadFieldList()
		
		return session['fieldList']
	}

	
	/**
	* This method checks to make sure the list of fields we want to use are in session. If they aren't, it adds them to the session.
	*/
   def verifyGridFieldList =
   {
	   //This field list might get modified later and contains only the fields being display in the gridpanel.
	   if(!session['gridFieldList']) session['gridFieldList'] = loadFieldList()
	   
	   return session['gridFieldList']
   }
			
	/**
	 * This will get the list of available fields from the Solr server.
	 */
	def loadFieldList = 
	{
		//Get the list of fields we don't want to display.
		String solrFieldExclusion = grailsApplication.config.com.recomdata.solr.fieldExclusionList
		
		//Get the list of possible results.
		def resultsList = solrService.getCategoryList(solrFieldExclusion)
		
		return resultsList
	}
	
	/**
	* This returns a JSON object representing the available solr fields. Used mainly to populate picklists.
	*/
   def loadCategories =
   {
	   //Get the field list from session, or retrieve it from Solr.
	   def fieldList = verifyFieldList()
	   
	   //Initialize the map with the all value.
	   def categoryMap = [rows:[["value":"all","label":"all"]]]
	   
	   //We need to put the field list into a format that the pick list expects. Each field gets a label and value entry.
	   fieldList.each
	   {
		   def tempMap = [:]
		   
		   tempMap['value'] = it.toString()
		   tempMap['label'] = it.toString().toLowerCase().replace("_", " ")
		   
		   categoryMap['rows'].add(tempMap)
	   }

       render(text:params.callback + "(" + (categoryMap as JSON) + ")", contentType:"application/javascript")
   }

	/**
	 * When we want to display the BioBank screen we need to requery Solr to get the list of ID's for the row we clicked on.	
	 */
	def bioBank =
	{
		
		//Get the list of Sample ID's based on the criteria in the JSON object.
		def idList = solrService.getIDList(request.JSON.SearchJSON);
		
		//Get all the BioBank results for these IDs.
		def sampleList = BioBankSample.getAll(idList);

		//Render the BioBank data.		
		render(template:"BioBankList", model:[samples:sampleList]);
	}
	
	
	/**
	 * 
	 * TPX2
	 */
	def sampleValidateHeatMap =
	{
		//We need to first retrieve the list of Sample ID's for the dataset we have selected.

		//Get the list of Sample ID's based on the criteria in the JSON object.
		//We need to get an ID list per subset. The JSON we recieved should be [1:[category:[]]]
		def subsetList = request.JSON.SearchJSON
		
		//This is the hashmap we return as JSON.
		HashMap result = [:]
		
		//Loop for each subset.
		subsetList.each 
		{  
			subset ->
			
			//Grab the Sample ID's in this subset.
			def idList = solrService.getIDList(subset.value);

			//Add the ID's to the result object.
			result[subset.key] = idList;
		}
		
		result=result.sort{it.key}
		
		HashMap sampleIdList = [:]
		
		sampleIdList["SampleIdList"] = result;
		
		render sampleIdList as JSON;
	}
	
	def snpDataService
	
	def testSNPData =
	{
		SnpData snpData = new SnpData()
		
		snpDataService.getSnpDataByResultInstanceAndGene("15804","GSE14860","GENE:662",null,snpData,"C:\\SVN\\repo1\\sanofi\\trunk\\biomart\\web-app\\plugins\\ANOVA\\test.txt",true,true)
	}
	
	
}
