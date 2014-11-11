import org.transmart.searchapp.SecureObject;

class SecureObjectController {

    def index = { redirect(action: list, params: params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def list = {
        params.max = Math.min(params.max ? params.max.toInteger() : grailsApplication.config.com.recomdata.admin.paginate.max, 100)
        [secureObjectInstanceList: SecureObject.list(params), secureObjectInstanceTotal: SecureObject.count()]
    }

    def show = {
        def secureObjectInstance = SecureObject.get(params.id)

        if (!secureObjectInstance) {
            flash.message = "SecureObject not found with id ${params.id}"
            redirect(action: list)
        } else {
            return [secureObjectInstance: secureObjectInstance]
        }
    }

    def delete = {
        def secureObjectInstance = SecureObject.get(params.id)
        if (secureObjectInstance) {
            try {
                secureObjectInstance.delete()
                flash.message = "SecureObject ${params.id} deleted"
                redirect(action: list)
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "SecureObject ${params.id} could not be deleted"
                redirect(action: show, id: params.id)
            }
        } else {
            flash.message = "SecureObject not found with id ${params.id}"
            redirect(action: list)
        }
    }

    def edit = {
        def secureObjectInstance = SecureObject.get(params.id)

        if (!secureObjectInstance) {
            flash.message = "SecureObject not found with id ${params.id}"
            redirect(action: list)
        } else {
            return [secureObjectInstance: secureObjectInstance]
        }
    }

    def update = {
        def secureObjectInstance = SecureObject.get(params.id)
        if (secureObjectInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (secureObjectInstance.version > version) {

                    secureObjectInstance.errors.rejectValue("version", "secureObject.optimistic.locking.failure", "Another user has updated this SecureObject while you were editing.")
                    render(view: 'edit', model: [secureObjectInstance: secureObjectInstance])
                    return
                }
            }
            secureObjectInstance.properties = params
            if (!secureObjectInstance.hasErrors() && secureObjectInstance.save()) {
                flash.message = "SecureObject ${params.id} updated"
                redirect(action: show, id: secureObjectInstance.id)
            } else {
                render(view: 'edit', model: [secureObjectInstance: secureObjectInstance])
            }
        } else {
            flash.message = "SecureObject not found with id ${params.id}"
            redirect(action: edit, id: params.id)
        }
    }

    def create = {
        def secureObjectInstance = new SecureObject()
        secureObjectInstance.properties = params
        return ['secureObjectInstance': secureObjectInstance]
    }

    def save = {
        def secureObjectInstance = new SecureObject(params)
        if (!secureObjectInstance.hasErrors() && secureObjectInstance.save()) {
            flash.message = "SecureObject ${secureObjectInstance.id} created"
            redirect(action: show, id: secureObjectInstance.id)
        } else {
            render(view: 'create', model: [secureObjectInstance: secureObjectInstance])
        }
    }
}
