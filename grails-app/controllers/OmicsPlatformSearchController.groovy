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
        def result = omicsQueryService.getSearchResults(params.term, params.gplid)
        render result as JSON
    }
}
