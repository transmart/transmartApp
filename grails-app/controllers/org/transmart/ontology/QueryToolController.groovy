package org.transmart.ontology

import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject
import org.transmart.authorization.CurrentUserBeanProxyFactory
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.core.users.User
import org.apache.commons.io.IOUtils

import javax.annotation.Resource

class QueryToolController {

    def queryDefinitionXmlService
    def queriesResourceAuthorizationDecorator
    def i2b2HelperService
    def omicsQueryService
    @Resource(name = CurrentUserBeanProxyFactory.BEAN_BAME)
    User currentUser

    /**
     * Creates a query definition and runs it. The input format is a subset
     * of the i2b2 XML query definition.
     *
     * The result is a JSON serialized QueryResult.
     */
    def runQueryFromDefinition() {
        String request = IOUtils.toString(request.reader)
        QueryDefinition definition =
                queryDefinitionXmlService.fromXml(new StringReader(request))

        String username = currentUser.username

        def result = queriesResourceAuthorizationDecorator.runQuery(
                definition, username)



        //render result as JSON

        def omics_result = omicsQueryService.applyOmicsFilters(result.id, request, definition.name)
        def newresult = JSON.parse((result as JSON).toString())
        newresult.putAt("omics_filter_result", omics_result)
        render newresult as JSON
    }

    /**
     * Fetches the query definition (in XML form) used to obtain the results
     * with the passed in id.
     */
    def getQueryDefinitionFromResultId() {
        Long id = params.long('result_id')
        if (!id) {
            throw new InvalidRequestException('result_id param not specified')
        }

        def queryDefinition =
                queriesResourceAuthorizationDecorator.getQueryDefinitionForResult(
                        queriesResourceAuthorizationDecorator.getQueryResultFromId(id))

        /* we actually converted from XML above and now we're converting back
         * to XML. Oh well... */
        render(
                contentType: 'application/xml',
                text: queryDefinitionXmlService.toXml(queryDefinition))
    }

}
