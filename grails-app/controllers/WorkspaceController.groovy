import com.recomdata.transmart.domain.searchapp.Report;
import com.recomdata.transmart.domain.searchapp.ReportItem;
import com.recomdata.transmart.domain.searchapp.Subset

class WorkspaceController {

    def springSecurityService

    def index = { }

    def listWorkspaceItems =
        {
            /////////////////////Get all the subsets for this user and any public subsets.
            //def subsets = Subset.findAllByCreatingUserOrPublicFlag(springSecurityService.getPrincipal().username,true)
            def subsetsCriteria = Subset.createCriteria()
            def subsets = subsetsCriteria{
                and{
                    or{
                        eq("creatingUser",springSecurityService.getPrincipal().username)
                        eq("publicFlag",true)
                    }
                    eq("deletedFlag",false)
                }
            }

            //Construct the email link with placeholders for subset ids.
            String pathStr =  request.getScheme() + '://' + request.getServerName() + ((request.getLocalPort() != 80) ? ':' + request.getLocalPort() : '') + request.getContextPath() + '/datasetExplorer/index?sId=SUBSETID'

            StringBuilder linkBuilder = new StringBuilder()
            //linkBuilder.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"")
            linkBuilder.append("mailto:?subject=Link to ")
            linkBuilder.append("Saved comparison ID=SUBSETID")
            linkBuilder.append("&body=The following is a link to the saved comparison in tranSMART.  Please, note that you need to be logged into tranSMART prior to using this link.%0A%0A")
            linkBuilder.append(pathStr)
            //linkBuilder.append("\" target=\"_blank\" class=\"tiny\" style=\"text-decoration:underline;color:blue;font-size:11px;\">Email this comparison</a><br /><br />")

            String emailLink = linkBuilder.toString()

            subsets.each{subset->
                subset.emailLink = emailLink.replaceAll("SUBSETID", Long.toString(subset.id))
                subset.link = pathStr.replaceAll("SUBSETID", Long.toString(subset.id))
            }


            /////////////////////Get all the reports for this user, and any public reports.
            def reports = Report.findAllByCreatingUserOrPublicFlag(springSecurityService.getPrincipal().username,"Y")


            //Read the currently selected subset id from the request
            def selectedSubsetId = params['selectedSubsetId']
            try{
                selectedSubsetId=Integer.parseInt(selectedSubsetId)
            }catch (NumberFormatException nfe){
                selectedSubsetId=-1;
            }

            /////////////////////Pass in the reports, subsets and the username. The username is used to determine whether or not to show the delete link.
            render(template:'/workspace/list',model:[reports: reports,
                    subsets: subsets,
                    currentUser: springSecurityService.getPrincipal().username,
                    selectedSubsetId:selectedSubsetId])
        }
}
