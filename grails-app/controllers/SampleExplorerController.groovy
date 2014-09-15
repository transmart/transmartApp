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


import com.recomdata.snp.SnpData
import grails.converters.JSON
import org.transmart.searchapp.AccessLog

/**
 * Class for controlling the Sample Explorer page.
 * @author MMcDuffie
 *
 */
class SampleExplorerController {

    def springSecurityService
    def i2b2HelperService
    def variantService
    def sampleService
    def solrService

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


    //We'll take a result_instance_id and dump all the sample IDs for those patients into another table.
    def generateSampleCohort =
            {
                sampleService.generateSampleCollection(params.result_instance_id);
                render true
            }

    //Render the data grid screen based on the samples linked to the result_instance_id.
    def showCohortSamples =
            {
                render(view: "sampleExplorer", model:[sampleRequestType:"cohort", columnData:verifyGridFieldList() as JSON, result_instance_id: params.result_instance_id]);
            }

    /**
     * Display all the summary links.
     */
    def list =
            {
                def columnMap = verifyGridFieldList();


                render(view: "sampleExplorer", model:[sampleRequestType:"search", columnData:columnMap as JSON])
            }

    /**
     * This shows the page that has different groups for each category, and the links to filter with.
     */
    def showTopLevelListPage =
            {
                //Call the solr service to get a hash that looks like category:[item:count]. We pass in an empty string because we want all the documents in the solr search.
                def termMap = solrService.facetSearch("",verifyFieldList(), 'browse')

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
                def solrFieldList = verifyGridFieldList();

                //Call the solr service to get a hash that looks like category:[item:count].
                def termMap = solrService.facetSearch(request.JSON.SearchJSON,solrFieldList, 'browse')

                //Render the list of checkboxes and links based on the items in our search JSON.
                render(template:"categorySearchWithCheckboxes", model:[termsMap:termMap,JSONData:request.JSON.SearchJSON]);
            }

    /**
     * This draws the simple HTML page that has the DIV that gets populated by the ExtJS datagrid.
     */
    def showDataSetResults =
            {
                Boolean includeCohortInformation = false

                def sampleSummary = [:]

                if(request.JSON?.showCohortInformation == "TRUE")
                {
                    sampleSummary = sampleService.loadSampleStatisticsObject(request.JSON?.result_instance_id)
                    includeCohortInformation = true
                }

                render(template:"dataSetResults", model:[includeCohortInformation : includeCohortInformation, sampleSummary : sampleSummary]);
            }

    /**
     * This will pull a result set from Solr using a query based on the JSON data passed in. Returns results as JSON.
     */
    def getDataSetResults =
            {
                //Grab the string for the maximum number of result rows to return.
                String solrMaxRows = grailsApplication.config.com.recomdata.solr.maxRows

                String selectedResultColumns = ""

                if(request.JSON.PanelNumber)
                {
                    //In the JSON result there is a list of the columns we expect to get back.
                    selectedResultColumns = request.JSON.SearchJSON["GridColumnList" + request.JSON.PanelNumber ].join(",").replace("\"","")

                    selectedResultColumns = selectedResultColumns.replace("GridColumnList" + request.JSON.PanelNumber, "")
                }
                else
                {
                    //In the JSON result there is a list of the columns we expect to get back.
                    selectedResultColumns = request.JSON.SearchJSON.GridColumnList.join(",").replace("\"","")
                }

                //This will be the hash to store our results.
                def resultsHash = solrService.pullResultsBasedOnJson(request.JSON.SearchJSON,selectedResultColumns, false, 'sampleExplorer')

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
                def resultsHash = solrService.suggestTerms(category,values,grailsApplication.config.com.recomdata.solr.numberOfSuggestions.toString(), 'sampleExplorer')

                //Render the results as JSON.
                render params.callback+"("+(resultsHash as JSON)+")"
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
                fieldList.columns.each
                        {
                            def tempMap = [:]

                            tempMap['value'] = it.dataIndex
                            tempMap['label'] = it.header

                            categoryMap['rows'].add(tempMap)
                        }

                render params.callback+"("+(categoryMap as JSON)+")"
            }

    /**
     * For the samples specified we want to gather all the data residing in SOLR for them.
     */
    def bioBank =
            {

                def fullColumnList = [];

                loadEntireFieldList().columns.each{fullColumnList.push(it.dataIndex)}

                def columnPrettyNameMapping = loadFieldPrettyNameMapping()

                //This will be the hash to store our results.
                def resultsHash = solrService.pullResultsBasedOnJson(request.JSON.SearchJSON,fullColumnList.join(",").replace("\"",""), true, 'sampleExplorer')

                //Render the BioBank data.
                render(template:"BioBankList", model:[samples:resultsHash.results, columnPrettyNameMapping:columnPrettyNameMapping]);
            }

    def sampleContactScreen =
            {
                def fullDataGroupedByContact 	= [:]
                def columnPrettyNameMapping 	= loadFieldPrettyNameMapping()
                def contactSampleIdMap			= [:]
                def idColumn 					= grailsApplication.config.sampleExplorer.idfield

                if(!idColumn) throw new Exception("SOLR ID Field Configuration not set!")

                //We need to pull all the columns for the data referenced in the Search JSON.
                def fullColumnList = [];
                loadEntireFieldList().columns.each
                        {
                            fullColumnList.push(it.dataIndex)
                        }

                def allSamplesHash = solrService.pullResultsBasedOnJson(request.JSON.SearchJSON,fullColumnList.join(",").replace("\"",""), true, 'sampleExplorer')

                //Get the distinct contact fields for this data.
                def contactHash = solrService.pullResultsBasedOnJson(request.JSON.SearchJSON,"CONTACT", true, 'sampleExplorer')

                //We need to group the data by the contact field. Loop through the contact data outside, then the actual data inside.
                contactHash.results.each {
                    currentContact ->

                        if(currentContact.CONTACT) {
                            fullDataGroupedByContact[currentContact.CONTACT] = [];
                            contactSampleIdMap[currentContact.CONTACT] = [];
                        }
                        else{
                            currentContact.CONTACT = "NO_CONTACT";
                            fullDataGroupedByContact["NO_CONTACT"] 	= [];
                            contactSampleIdMap["NO_CONTACT"] 		= [];
                        }

                        //Now loop through the actual results and group our contacts under their respective contact hash entry.
                        allSamplesHash.results.each {
                            currentSample ->

                                if(currentSample[idColumn] && (currentSample["CONTACT"] == currentContact.CONTACT))
                                {
                                    contactSampleIdMap[currentContact.CONTACT].add(currentSample[idColumn])

                                    fullDataGroupedByContact[currentContact.CONTACT].add(currentSample)
                                }
                                else if(currentSample[idColumn] && !currentSample["CONTACT"])
                                {
                                    contactSampleIdMap["NO_CONTACT"].add(currentSample[idColumn])

                                    fullDataGroupedByContact["NO_CONTACT"].add(currentSample)
                                }
                        }
                }



                render(template:"sampleContactInfo", model:[allSamplesByContact:fullDataGroupedByContact, contactSampleIdMap:contactSampleIdMap, columnPrettyNameMapping:columnPrettyNameMapping])
            }

    def sampleValidateAdvancedWorkflow =
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
                                def idList = solrService.getIDList(subset.value, 'sampleExplorer');

                                //Add the ID's to the result object.
                                result[subset.key] = idList;
                        }

                result=result.sort{it.key}

                HashMap sampleIdList = [:]

                sampleIdList["SampleIdList"] = result;

                render sampleIdList as JSON;
            }

    /**
     * This method checks to make sure the list of fields we want to use are in session. If they aren't, it adds them to the session.
     */
    def verifyFieldList =
            {
                //This field list always has all the fields we want to display.
                //if(!session['fieldList']) session['fieldList'] = loadFieldList()

                return loadFieldList()
            }


    /**
     * This method checks to make sure the list of fields we want to use are in session. If they aren't, it adds them to the session.
     */
    def verifyGridFieldList =
            {
                //This field list might get modified later and contains only the fields being display in the gridpanel.
                //if(!session['gridFieldList']) session['gridFieldList'] = loadEntireFieldList()

                return loadEntireFieldList()
            }

    /**
     * This will get the list of available fields from the Solr server.
     */
    def loadFieldList = {

        //Pull the field map from the configuration file.
        def resultsList = grailsApplication.config.sampleExplorer.fieldMapping.clone()

        if(!resultsList) throw new Exception("Field Mapping Configuration not set!")

        def columnConfigsToRemove = []

        resultsList.columns.each
                {
                    currentColumn ->
                        if(!currentColumn.mainTerm)
                        {
                            columnConfigsToRemove.add(currentColumn)
                        }
                }

        resultsList.columns = resultsList.columns - columnConfigsToRemove

        return resultsList
    }

    /**
     * This will get the list of available fields from the Solr server.
     */
    def loadGridFieldList = {

        //Pull the field map from the configuration file.
        def resultsList = grailsApplication.config.sampleExplorer.fieldMapping.clone()

        if(!resultsList) throw new Exception("Field Mapping Configuration not set!")

        def columnConfigsToRemove = []

        resultsList.columns.each
                {
                    currentColumn ->
                        if(!currentColumn.showInGrid)
                        {
                            columnConfigsToRemove.add(currentColumn)
                        }
                }

        resultsList.columns = resultsList.columns - columnConfigsToRemove

        return resultsList
    }

    def loadEntireFieldList = {

        //Pull the field map from the configuration file.
        def fullColumnList = grailsApplication.config.sampleExplorer.fieldMapping.clone()

        if(!fullColumnList) throw new Exception("Field Mapping Configuration not set!")

        return fullColumnList
    }

    def loadFieldPrettyNameMapping = {

        def fullColumnMapping = grailsApplication.config.sampleExplorer.fieldMapping.clone()

        if(!fullColumnMapping) throw new Exception("Field Mapping Configuration not set!")

        def returnHash = [:]

        fullColumnMapping.columns.each {
            currentColumn ->

                returnHash[currentColumn.dataIndex] = currentColumn.header

        }

        return returnHash

    }


}