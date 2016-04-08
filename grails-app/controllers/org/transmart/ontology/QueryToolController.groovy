package org.transmart.ontology

import grails.converters.JSON
import org.transmart.authorization.CurrentUserBeanProxyFactory
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.core.users.User

import javax.annotation.Resource

class QueryToolController {

    def queryDefinitionXmlService
    def queriesResourceAuthorizationDecorator
    User currentUserBean

    /**
     * Creates a query definition and runs it. The input format is a subset
     * of the i2b2 XML query definition.
     *
     * The result is a JSON serialized QueryResult.
     */
    def runQueryFromDefinition() {
        QueryDefinition definition =
                queryDefinitionXmlService.fromXml(request.reader)
        String username = currentUserBean.username

        def result = queriesResourceAuthorizationDecorator.runQuery(
                definition, username)
        render result as JSON
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
