/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/


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
        def subsetId = params.sId;
        log.trace("DatasetExplorer Controller found saved comparison id=" + subsetId);
        def qid1 = null;
        def qid2 = null;
        Boolean restorecomparison = false;

        if (subsetId != null && subsetId != "") {
            Subset subset = Subset.get(subsetId);
            if (subset != null) {
                restorecomparison = true;
                qid1 = subset.queryID1;
                qid2 = subset.queryID2;
            }

        }

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
                                                restorecomparison : restorecomparison,
                                                qid1              : qid1,
                                                qid2              : qid2,
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
}
