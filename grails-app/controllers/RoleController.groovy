/**
 * $Id: RoleController.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
import org.transmart.searchapp.Role;

/**
 * Authority Controller.
 */
class RoleController {

    // the delete, save and update actions only accept POST requests
    static Map allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def springSecurityService

    def index = {
        redirect action: "list", params: params
    }

    /**
     * Display the list authority page.
     */
    def list = {
        if (!params.max) {
            params.max = grailsApplication.config.com.recomdata.search.paginate.max
        }
        [authorityList: Role.list(params)]
    }

    /**
     * Display the show authority page.
     */
    def show = {
        def authority = Role.get(params.id)
        if (!authority) {
            flash.message = "Role not found with id $params.id"
            redirect action: "list"
            return
        }
        def people = authority.people
        if (params.sort) {
            def sortFunction = {o1,o2 ->
                def v1 = peopleFieldSelector(params.sort,o1)
                def v2 = peopleFieldSelector(params.sort,o2)
                v1.compareTo(v2)
            }
            if (params.order.equals("asc")) {
                people = people.sort(sortFunction)
            } else {
                people = people.sort(sortFunction).reverse()
            }
        } else {
            people = people.sort({it.id})
        }
        [authority: authority, sortedPeople: people]
    }

    def peopleFieldSelector(feildName,authUser) {
        switch (feildName) {
            case "id" :
                return authUser.id
            case "username" :
                return authUser.username
            case "userRealName" :
                return authUser.userRealName
            case "enabled" :
                return authUser.enabled
            case "description" :
                return authUser.description
            default :
                return authUser.id
        }
    }

    /**
     * Delete an authority.
     */
    def delete = {
        def authority = Role.get(params.id)
        if (!authority) {
            flash.message = "Role not found with id $params.id"
            redirect action: "list"
            return
        }

        springSecurityService.deleteRole(authority)

        flash.message = "Role $params.id deleted."
        redirect action: "list"
    }

    /**
     * Display the edit authority page.
     */
    def edit = {
        def authority = Role.get(params.id)
        if (!authority) {
            flash.message = "Role not found with id $params.id"
            redirect action: "list"
            return
        }

        [authority: authority]
    }

    /**
     * Authority update action.
     */
    def update = {

        def authority = Role.get(params.id)
        if (!authority) {
            flash.message = "Role not found with id $params.id"
            redirect action: "edit", id: params.id
            return
        }

        long version = params.version.toLong()
        if (authority.version > version) {
            authority.errors.rejectValue 'version', 'authority.optimistic.locking.failure',
                    'Another user has updated this Role while you were editing.'
            render view: 'edit', model: [authority: authority]
            return
        }

        if (springSecurityService.updateRole(authority, params)) {
            springSecurityService.clearCachedRequestmaps()
            redirect action: "show", id: authority.id
        } else {
            render view: 'edit', model: [authority: authority]
        }
    }

    /**
     * Display the create new authority page.
     */
    def create = {
        [authority: new Role()]
    }

    /**
     * Save a new authority.
     */
    def save = {
        def role = new Role()
        role.properties = params

        // authority valdiation
        if (params.authority == null || params.authority == "") {
            flash.message = "Please enter a role name"
            role.authority = params.authority
            role.description = params.description
            return render(view: 'create', model: [authority: role])
        }

        // description validation
        if (params.description == null || params.description == "") {
            flash.message = "Please enter a role description"
            role.authority = params.authority
            role.description = params.description
            return render(view: 'create', model: [authority: role])
        }

        if (role.save()) {
            redirect action: "show", id: role.id
        } else {
            render view: 'create', model: [authority: role]
        }
    }
}
