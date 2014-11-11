import org.transmart.searchapp.AccessLog

class SecureController {

    def springSecurityService

    def index = {
        def al = new AccessLog(username: springSecurityService.getPrincipal().username, event: "Access Dataset Explorer", accesstime: new Date())
        al.save();
        redirect(controller: "datasetExplorer", action: "index")
    }
}
