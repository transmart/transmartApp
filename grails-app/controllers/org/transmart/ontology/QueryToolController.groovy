package org.transmart.ontology

import grails.converters.JSON
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.querytool.QueryDefinition

class QueryToolController {

    def queryDefinitionXmlService
    def queriesResourceService

    /**
     * Creates a query definition and runs it. The input format is a subset
     * of the i2b2 XML query definition.
     *
     * The result is a JSON serialized QueryResult.
     */
    def runQueryFromDefinition() {
        QueryDefinition definition =
            queryDefinitionXmlService.fromXml(request.reader)

        def result = queriesResourceService.runQuery(definition)
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
            queriesResourceService.getQueryDefinitionForResult(
                    queriesResourceService.getQueryResultFromId(id))

        /* we actually converted from XML above and now we're converting back
         * to XML. Oh well... */
        render(
                contentType: 'application/xml',
                text: queryDefinitionXmlService.toXml(queryDefinition))
    }

}
