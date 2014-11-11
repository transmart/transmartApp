import com.recomdata.transmart.domain.searchapp.Subset
import grails.converters.JSON

class SubsetController {

    def index = {}

    def i2b2HelperService
    def queriesResourceService
    def springSecurityService

    def getQueryIdsForSubset = {
        def subsetId = params["subsetId"];
        Subset subset = Subset.get(subsetId);
        def queryId1 = subset.queryID1;
        def queryId2 = subset.queryID2;

        def result = [queryId1: queryId1, queryId2: queryId2]

        render result as JSON
    }

    def save = {
        def qid1 = request.getParameter("result_instance_id1");
//i2b2HelperService.getQIDFromRID(request.getParameter("result_instance_id1"))
        def qid2 = request.getParameter("result_instance_id2");
//i2b2HelperService.getQIDFromRID(request.getParameter("result_instance_id2"))
        def subset = new Subset()

        try {
            subset.queryID1 = Integer.parseInt(qid1)
        } catch (NumberFormatException nfe) {
            subset.queryID1 = -1
        }

        try {
            subset.queryID2 = Integer.parseInt(qid2)
        } catch (NumberFormatException nfe) {
            subset.queryID2 = -1
        }

        def user = springSecurityService.getPrincipal()

        subset.creatingUser = user.username
        subset.description = params["description"]

        def isSubsetPublic = params["isSubsetPublic"]
        subset.publicFlag = (isSubsetPublic == "true") ? true : false

        def study = params["study"]
        subset.study = study
        boolean success = false

        try {
            success = subset.save(flush: true)
        }
        catch (Exception e) {
            subset.errors.each { error ->
                System.err.println(error);
            }
        }

        def result = [success: success]
        log.trace(result as JSON)
        render result as JSON
    }

    def query = {
        def subsetId = params["subsetId"]
        def displayQuery1
        def displayQuery2

        Subset subset = Subset.get(subsetId)

        def queryID1 = queriesResourceService.getQueryDefinitionForResult(
                queriesResourceService.getQueryResultFromId(subset.queryID1))
        displayQuery1 = generateDisplayOutput(queryID1)

        if (subset.queryID2 != -1) {
            def queryID2 = queriesResourceService.getQueryDefinitionForResult(
                    queriesResourceService.getQueryResultFromId(subset.queryID2))
            displayQuery2 = generateDisplayOutput(queryID2)
        }

        render(template: '/subset/query', model: [query1: displayQuery1, query2: displayQuery2])
    }

    def generateDisplayOutput(qd) {
        def result = "";
        qd.panels.each { p ->
            result += result.size() > 0 ? ("<b>" + (p.invert ? 'NOT' : 'AND') + "</b><br/>") : ''
//            result += (p.invert ? "NOT" : "AND") + "<br/>"
            p.items.each { i ->
                result += i.conceptKey
                if (i.constraint) {
                    result += "( with constraints )"
                }
                result += "<br/>"
            }
        }
        result
    }

    def delete = {
        def subsetId = params["subsetId"]
        def subset = Subset.get(subsetId)
        subset.deletedFlag = true
        subset.save(flush: true)

        render subset.deletedFlag
    }

    def togglePublicFlag = {
        def subsetId = params["subsetId"]
        def subset = Subset.get(subsetId)
        subset.publicFlag = !subset.publicFlag
        subset.save(flush: true)

        render subset.publicFlag
    }

    def updateDescription = {
        def subsetId = params["subsetId"]
        def description = params["description"]
        def subset = Subset.get(subsetId)
        subset.description = description
        subset.save(flush: true)

        render 'success'
    }

    def showSubsetPanels = {

        render(template: '/subset/subsetPanel')

    }


}