import grails.converters.JSON
import org.codehaus.groovy.grails.web.json.JSONObject

/**
 * Created by dverbeec on 19/03/2015.
 */
class OmicsPlatformSearchController {

    def omicsQueryService

    /**
     * Gets all gene symbols defined for a platform, for search auto-complete
     */
    def searchAutoComplete = {
        if (params.gplid == "" || params.term == "") {
            return [] as JSON
        }
        def result = omicsQueryService.getSearchResultsForGene(params.term, params.gplid)
        render result as JSON
    }

    def getPlatform() {
        def concept = params.get('concept_key')
        def platform = omicsQueryService.getPlatform(concept)
        if (platform) {
            // let's add the requested concept key back in the response
            def result = JSON.parse((platform as JSON).toString())
            result.concept_key = concept
            render result as JSON
        }
        else
            render JSONObject.NULL
    }

}
