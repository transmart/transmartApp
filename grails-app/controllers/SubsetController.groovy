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

class SubsetController {

    def index = { }

    def i2b2HelperService
    def authenticateService
    def springSecurityService
    //def subsetService

    def getQueryIdsForSubset = {
        def subsetId = params["subsetId"];
        Subset subset = Subset.get(subsetId);
        def queryId1 = subset.queryID1;
        def queryId2 = subset.queryID2;

        def result = [queryId1:queryId1, queryId2: queryId2]

        render result as JSON
    }

    def save = {
        def qid1 = request.getParameter("result_instance_id1");//i2b2HelperService.getQIDFromRID(request.getParameter("result_instance_id1"))
        def qid2 = request.getParameter("result_instance_id2");//i2b2HelperService.getQIDFromRID(request.getParameter("result_instance_id2"))
        def subset = new Subset()

        try	{
            subset.queryID1=Integer.parseInt(qid1)
        } catch(NumberFormatException nfe)	{
            subset.queryID1 = -1
        }

        try	{
            subset.queryID2=Integer.parseInt(qid2)
        } catch(NumberFormatException nfe)	{
            subset.queryID2= -1
        }

        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)

        subset.creatingUser = user.username
        subset.description = params["description"]

        def isSubsetPublic = params["isSubsetPublic"]
        subset.publicFlag=(isSubsetPublic=="true")?true:false

        def study = params["study"]
        subset.study = study
        boolean success = false

        try {
            success = subset.save(flush: true)
        }
        catch (Exception e)
        {
            System.err.println(e.getMessage());
            subset.errors.each { error ->
                System.err.println(error);
            }
        }
        /*if(!subset.save(flush:true)){

        }*/

        def result=[success: success]
        log.trace(result as JSON)
        render result as JSON
    }

    def query = {
        def subsetId = params["subsetId"]
        Subset subset = Subset.get(subsetId)

        def queryID1 = subset.queryID1
        def queryID2 = subset.queryID2

        def displayQuery1 = i2b2HelperService.renderQueryDefinitionToString(queryID1.toString(), "", null);
        def displayQuery2 = ""
        if(queryID2 > -1){
            displayQuery2 = i2b2HelperService.renderQueryDefinitionToString(queryID2.toString(), "", null);
        }

        render(template:'/subset/query', model:[query1:displayQuery1, query2:displayQuery2])
    }

    def delete = {
        def subsetId = params["subsetId"]
        def subset = Subset.get(subsetId)
        subset.deletedFlag=true
        subset.save(flush:true)

        render subset.deletedFlag
    }

    def togglePublicFlag = {
        def subsetId = params["subsetId"]
        def subset = Subset.get(subsetId)
        subset.publicFlag = !subset.publicFlag
        subset.save(flush:true)

        render subset.publicFlag
    }

    def updateDescription = {
        def subsetId = params["subsetId"]
        def description = params["description"]
        def subset = Subset.get(subsetId)
        subset.description = description
        subset.save(flush:true)

        render 'success'
    }

    def showSubsetPanels = {

        render(template:'/subset/subsetPanel')

    }


}