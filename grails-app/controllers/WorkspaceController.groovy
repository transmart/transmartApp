import com.recomdata.transmart.domain.searchapp.Report
import com.recomdata.transmart.domain.searchapp.Subset

class WorkspaceController {

    def springSecurityService

    def index = {}

    def listWorkspaceItems =
            {
                /////////////////////Get all the subsets for this user and any public subsets.
                //def subsets = Subset.findAllByCreatingUserOrPublicFlag(springSecurityService.getPrincipal().username,true)
                def subsetsCriteria = Subset.createCriteria()
                def subsets = subsetsCriteria {
                    and {
                        or {
                            eq("creatingUser", springSecurityService.getPrincipal().username)
                            eq("publicFlag", true)
                        }
                        eq("deletedFlag", false)
                    }
                }

                /////////////////////Get all the reports for this user, and any public reports.
                def reports = Report.findAllByCreatingUserOrPublicFlag(springSecurityService.getPrincipal().username, "Y")

                //Read the currently selected subset id from the request
                def selectedSubsetId = params['selectedSubsetId']
                try {
                    selectedSubsetId = Integer.parseInt(selectedSubsetId)
                } catch (NumberFormatException nfe) {
                    selectedSubsetId = -1;
                }

                /////////////////////Pass in the reports, subsets and the username. The username is used to determine whether or not to show the delete link.
                render(template: '/workspace/list', model: [reports         : reports,
                                                            subsets         : subsets,
                                                            currentUser     : springSecurityService.getPrincipal().username,
                                                            selectedSubsetId: selectedSubsetId])
            }
}
