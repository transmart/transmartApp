import org.transmart.searchapp.SecureObjectPath;

class SecureObjectPathController {

    def index = { redirect(action: "list", params: params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def list = {
        params.max = Math.min(params.max ? params.max.toInteger() : grailsApplication.config.com.recomdata.admin.paginate.max, 100)
        [secureObjectPathInstanceList: SecureObjectPath.list(params), secureObjectPathInstanceTotal: SecureObjectPath.count()]
    }

    def show = {
        def secureObjectPathInstance = SecureObjectPath.get(params.id)

        if (!secureObjectPathInstance) {
            flash.message = "SecureObjectPath not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [secureObjectPathInstance: secureObjectPathInstance]
        }
    }

    def delete = {
        def secureObjectPathInstance = SecureObjectPath.get(params.id)
        if (secureObjectPathInstance) {
            try {
                secureObjectPathInstance.delete()
                flash.message = "SecureObjectPath ${params.id} deleted"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "SecureObjectPath ${params.id} could not be deleted"
                redirect(action: "show", id: params.id)
            }
        } else {
            flash.message = "SecureObjectPath not found with id ${params.id}"
            redirect(action: "list")
        }
    }

    def edit = {
        def secureObjectPathInstance = SecureObjectPath.get(params.id)

        if (!secureObjectPathInstance) {
            flash.message = "SecureObjectPath not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [secureObjectPathInstance: secureObjectPathInstance]
        }
    }

    def update = {
        def secureObjectPathInstance = SecureObjectPath.get(params.id)
        if (secureObjectPathInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (secureObjectPathInstance.version > version) {

                    secureObjectPathInstance.errors.rejectValue("version", "secureObjectPath.optimistic.locking.failure", "Another user has updated this SecureObjectPath while you were editing.")
                    render(view: 'edit', model: [secureObjectPathInstance: secureObjectPathInstance])
                    return
                }
            }
            secureObjectPathInstance.properties = params
            if (!secureObjectPathInstance.hasErrors() && secureObjectPathInstance.save()) {
                flash.message = "SecureObjectPath ${params.id} updated"
                redirect(action: "show", id: secureObjectPathInstance.id)
            } else {
                render(view: 'edit', model: [secureObjectPathInstance: secureObjectPathInstance])
            }
        } else {
            flash.message = "SecureObjectPath not found with id ${params.id}"
            redirect(action: "edit", id: params.id)
        }
    }

    def create = {
        def secureObjectPathInstance = new SecureObjectPath()
        secureObjectPathInstance.properties = params
        return ['secureObjectPathInstance': secureObjectPathInstance]
    }

    def save = {
        def secureObjectPathInstance = new SecureObjectPath(params)
        if (!secureObjectPathInstance.hasErrors() && secureObjectPathInstance.save()) {
            flash.message = "SecureObjectPath ${secureObjectPathInstance.id} created"
            redirect(action: "show", id: secureObjectPathInstance.id)
        } else {
            render(view: 'create', model: [secureObjectPathInstance: secureObjectPathInstance])
        }
    }
}
