import grails.converters.JSON
import org.transmart.searchapp.AuthUser

class CrossTrialController {
	
	def crossTrialService
	def i2b2DemoDataService
	def springSecurityService
	
	def generateTree = {
	
		def treeJson
		
		if(params.treeType && params.treeType == "related")
		{
			treeJson = crossTrialService.generateRelatedTree(params.searchTerm, params.nodeLevel, params.nodeToOpen)
		}
		else if(params.nodeToOpen && params.nodeToOpen == "-1"  && params.searchTerm == "")
		{
			treeJson = crossTrialService.generateRootTreeNodes()
		}
		else
		{
			treeJson = crossTrialService.generateTree(params.searchTerm,params.nodeLevel, params.nodeToOpen, params.inOutCode)
		}
		
		//These objects are used to construct the format of the JSON, in hashmaps and arrays.
		def JSONDataNode = [:]
		def JSONData = []
		def JSONDataArrayNode = []
		
		def currentNodeLevel = params.nodeLevel.toInteger()
		
		//Loop through each of the nodes.
		treeJson.each()
		{
			currentNode ->
			
			Integer nodeLevelMinusOne = currentNode.modifierLevel.toInteger() - 1
			
			//If the current node is a time series node, and we aren't opening the true leaf, create a dummy folder for the time value.
			if(currentNode.visitInd == "Y" && nodeLevelMinusOne == currentNodeLevel && params.visitInd != "Y")
			{
				JSONData.add(crossTrialService.createTimeSeriesJSNodeFromModifier(currentNode, false))
			}
			else if(currentNode.visitInd == "Y" && params.visitInd == "Y")
			{
				//If we are opening the leaf node of the time series folder, create it with slightly different options.
				JSONData.add(crossTrialService.createTimeSeriesJSNodeFromModifier(currentNode, true))
			}
			else
			{
				//Add the node to the JSON Data.
				JSONData.add(crossTrialService.createJSNodeFromModifier(currentNode))
			}
		}
	
		//Initialize the data node.
		JSONDataNode["data"] = JSONData
		JSONDataArrayNode.add(JSONData)
		
				
		def json = JSONData as JSON
		
		//This prints the JSON in a pretty format.
		//json.prettyPrint = true
		
		json.render response
	}
	
	def generateCohorts = {

		//The first part of our query is to generate the new result_instance_id.
		def newResultInstanceId = i2b2DemoDataService.generateResultInstanceId()
		
		//Generate an XML blurb that contains our value criteria.
		def requestXML = i2b2DemoDataService.generateRequestXML(request.JSON.criteria)
		
		//Create our query records.
		i2b2DemoDataService.createQueryRecords(newResultInstanceId, requestXML)
		
		//Now we need to insert our qualifying patients into qt_patient_set_collection.
		i2b2DemoDataService.populatePatientSetFromJSON(request.JSON.criteria, newResultInstanceId, AuthUser.findByUsername(springSecurityService.getPrincipal().username))
		
		def returnResult = ["result_instance_id":newResultInstanceId] 
		
		render returnResult as JSON
	}
	
}