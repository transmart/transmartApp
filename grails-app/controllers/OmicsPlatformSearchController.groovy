import grails.converters.JSON

/**
 * Created by dverbeec on 19/03/2015.
 */
class OmicsPlatformSearchController {

    def omicsQueryService

    def searchAutoComplete = {
        if (params.concept_key == "" || params.term == "") {
            return [] as JSON
        }
        def result = omicsQueryService.getSearchResultsForGene(params.term, params.concept_key)
        render result as JSON
    }

}
