/**
 * $Id: UrlMappings.groovy 9587 2011-09-23 19:08:56Z smunikuntla $
 * @author $Author: smunikuntla $
 * @version $Revision: 9587 $
 */
class UrlMappings {
    static mappings = {
        "/$controller/$action?/$id?" {
            constraints {
                // apply constraints here
            }
        }
        "/"(controller: 'userLanding', action: 'index')
        "/v1/oauth"(controller: 'oauth', action: 'verify')
        "500"(view: '/error')
        "/transmart/dataExport/getJobs"(controller: "dataExport", action: "getJobs")
        //"/transmart/exportData"(controller:"dataExport", action:"processExport")
    }
}
