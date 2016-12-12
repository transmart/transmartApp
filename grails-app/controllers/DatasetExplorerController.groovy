import com.recomdata.transmart.domain.searchapp.Subset
import grails.converters.JSON
import org.transmart.searchapp.AuthUser

class DatasetExplorerController {
    def springSecurityService
    def i2b2HelperService
    def ontologyService

    def defaultAction = "index"

    def index = {
        log.trace("in index");

        def pathToExpand
        //If we have an accession passed, retrieve its path
        if (params.accession) {
            pathToExpand = ontologyService.getPathForAccession(params.accession)
        }

        //code for retrieving a saved comparison
        pathToExpand = pathToExpand ?: params.path;
        def rwgSearchFilter = session['rwgSearchFilter'];
        if (rwgSearchFilter) {
            rwgSearchFilter = rwgSearchFilter.join(",,,")
        } else {
            rwgSearchFilter = "";
        }

        def rwgSearchOperators = session['rwgSearchOperators'];
        if (rwgSearchOperators) {
            rwgSearchOperators = rwgSearchOperators.join(";")
        } else {
            rwgSearchOperators = "";
        }

        def searchCategory = session['searchCategory'];
        def globalOperator = session['globalOperator'];
        def dseOpenedNodes = session["dseOpenedNodes"];
        def dseClosedNodes = session['dseClosedNodes'];

        //Grab i2b2 credentials from the config file
        def i2b2Domain = grailsApplication.config.com.recomdata.i2b2.subject.domain
        def i2b2ProjectID = grailsApplication.config.com.recomdata.i2b2.subject.projectid
        def i2b2Username = grailsApplication.config.com.recomdata.i2b2.subject.username
        def i2b2Password = grailsApplication.config.com.recomdata.i2b2.subject.password

        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        def admin = i2b2HelperService.isAdmin(user);
        def tokens = i2b2HelperService.getSecureTokensCommaSeparated(user)
        def initialaccess = new JSON(i2b2HelperService.getAccess(i2b2HelperService.getRootPathsWithTokens(), user)).toString();
        log.trace("admin =" + admin)
        render(view: "datasetExplorer", model: [pathToExpand      : pathToExpand,
                                                admin             : admin,
                                                tokens            : tokens,
                                                initialaccess     : initialaccess,
                                                i2b2Domain        : i2b2Domain,
                                                i2b2ProjectID     : i2b2ProjectID,
                                                i2b2Username      : i2b2Username,
                                                i2b2Password      : i2b2Password,
                                                rwgSearchFilter   : rwgSearchFilter,
                                                rwgSearchOperators: rwgSearchOperators,
                                                globalOperator    : globalOperator,
                                                rwgSearchCategory : searchCategory,
                                                debug             : params.debug,
                                                dseOpenedNodes    : dseOpenedNodes,
                                                dseClosedNodes    : dseClosedNodes])
    }

    def queryPanelsLayout = {
        render(view: '_queryPanel', model: [])
    }
}
