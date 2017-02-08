import com.recomdata.transmart.domain.searchapp.Subset
import grails.converters.JSON
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.querytool.ConstraintByOmicsValue

class SubsetController {

    def index = {}

    def i2b2HelperService
    def queriesResourceService
    def queryDefinitionXmlService
    def springSecurityService

    def getQueryForSubset = {
        def subsetId = params["subsetId"];
        Subset subset = Subset.get(subsetId);
        def result = [:]

        // We have to bypass core-api implementation tests for user permission
        // But we still need to be coherent in who can retrieve what
        // Publicity and user checks are still necessary
        if (!subset.deletedFlag && (subset.publicFlag || subset.creatingUser == springSecurityService.getPrincipal().username)) {

            if (subset.queryID1 && subset.queryID1 >= 0)
                result["query1"] = queryDefinitionXmlService.toXml(queriesResourceService.getQueryDefinitionForResult(queriesResourceService.getQueryResultFromId(subset.queryID1)))
            if (subset.queryID2 && subset.queryID2 >= 0)
                result["query2"] = queryDefinitionXmlService.toXml(queriesResourceService.getQueryDefinitionForResult(queriesResourceService.getQueryResultFromId(subset.queryID2)))

        }

        render result as JSON
    }

    def getQueryForResultInstance = {

        def result = [:]

        // We have to bypass core-api implementation tests for user permission
        // But we still need to be coherent in who can retrieve what
        // Publicity and user checks are still necessary

        if (params["1"] && params["1"].toLong() >= 0)
            result["query1"] = queryDefinitionXmlService.toXml(queriesResourceService.getQueryDefinitionForResult(queriesResourceService.getQueryResultFromId(params["1"].toLong())))
        if (params["2"] && params["2"].toLong() >= 0)
            result["query2"] = queryDefinitionXmlService.toXml(queriesResourceService.getQueryDefinitionForResult(queriesResourceService.getQueryResultFromId(params["2"].toLong())))

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
                if (i.constraintByOmicsValue) {
                    result += " - " + i.constraintByOmicsValue.selector + " " +
                            Projection.prettyNames.get(i.constraintByOmicsValue.projectionType,
                                    i.constraintByOmicsValue.projectionType) + " " +
                            i.constraintByOmicsValue.operator.value + " "
                    if (i.constraintByOmicsValue.operator == ConstraintByOmicsValue.Operator.BETWEEN) {
                        String[] bounds = i.constraintByOmicsValue.constraint.split(':')
                        if (bounds.length != 2) {
                            log.error "BETWEEN constraint type found with values not seperated by ':'"
                            result += i.constraintByOmicsValue.constraint
                        }
                        else {
                            result += bounds.join(" and ")
                        }
                    }
                    else {
                        result += i.constraintByOmicsValue.constraint
                    }
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
}