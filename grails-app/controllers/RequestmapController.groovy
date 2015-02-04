/**
 * $Id: RequestmapController.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
import org.transmart.searchapp.Requestmap

/**
 * Requestmap controller.
 */
class RequestmapController {

    def springSecurityService

    // the delete, save and update actions only accept POST requests
    static Map allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index = {
        redirect action: "list", params: params
    }

    def list = {
        if (!params.max) {
            params.max = grailsApplication.config.com.recomdata.admin.paginate.max
        }
        [requestmapList: Requestmap.list(params)]
    }

    def show = {
        def requestmap = Requestmap.get(params.id)
        if (!requestmap) {
            flash.message = "Requestmap not found with id $params.id"
            redirect action: "list"
            return
        }
        [requestmap: requestmap]
    }

    def delete = {
        def requestmap = Requestmap.get(params.id)
        if (!requestmap) {
            flash.message = "Requestmap not found with id $params.id"
            redirect action: "list"
            return
        }

        requestmap.delete()

        springSecurityService.clearCachedRequestmaps()

        flash.message = "Requestmap $params.id deleted."
        redirect(action: "list")
    }

    def edit = {
        def requestmap = Requestmap.get(params.id)
        if (!requestmap) {
            flash.message = "Requestmap not found with id $params.id"
            redirect(action: "list")
            return
        }

        [requestmap: requestmap]
    }

    /**
     * Update action, called when an existing Requestmap is updated.
     */
    def update = {

        def requestmap = Requestmap.get(params.id)
        if (!requestmap) {
            flash.message = "Requestmap not found with id $params.id"
            redirect(action: "edit", id: params.id)
            return
        }

        long version = params.version.toLong()
        if (requestmap.version > version) {
            requestmap.errors.rejectValue 'version', "requestmap.optimistic.locking.failure",
                    "Another user has updated this Requestmap while you were editing."
            render view: 'edit', model: [requestmap: requestmap]
            return
        }

        requestmap.properties = params
        if (requestmap.save()) {
            springSecurityService.clearCachedRequestmaps()
            redirect action: "show", id: requestmap.id
        } else {
            render view: 'edit', model: [requestmap: requestmap]
        }
    }

    def create = {
        [requestmap: new Requestmap(params)]
    }

    /**
     * Save action, called when a new Requestmap is created.
     */
    def save = {
        def requestmap = new Requestmap(params)
        if (requestmap.save()) {
            springSecurityService.clearCachedRequestmaps()
            redirect action: "show", id: requestmap.id
        } else {
            render view: 'create', model: [requestmap: requestmap]
        }
    }
}
