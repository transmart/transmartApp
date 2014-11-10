import org.codehaus.groovy.grails.exceptions.InvalidPropertyException
import org.springframework.transaction.TransactionStatus
import org.transmart.searchapp.*

/**
 * User controller.
 */
class AuthUserController {
    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService
    def dataSource

    // the delete, save and update actions only accept POST requests
    static Map allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index = {
        redirect action: "list", params: params
    }

    def list = {
        if (!params.max) {
            //Changed this to use the jQuery dataTable, which includes client side paging/searching
            //Need to return all user accounts here
            //params.max = grailsApplication.config.com.recomdata.admin.paginate.max
            params.max = 999999
        }
        [personList: AuthUser.list(params)]
    }

    def show = {
        def person = AuthUser.get(params.id)
        if (!person) {
            flash.message = "AuthUser not found with id $params.id"
            redirect action: "list"
            return
        }
        List roleNames = []
        for (role in person.authorities) {
            roleNames << role.authority
        }
        roleNames.sort { n1, n2 ->
            n1 <=> n2
        }
        [person: person, roleNames: roleNames]
    }

    /**
     * Person delete action. Before removing an existing person,
     * he should be removed from those authorities which he is involved.
     */
    def delete = {
        def person = AuthUser.get(params.id)
        if (person) {
            def userName = person.username
            def authPrincipal = springSecurityService.getPrincipal()
            if (!(authPrincipal instanceof String) && authPrincipal.username == userName) {
                flash.message = "You can not delete yourself, please login as another admin and try again"
            } else {
                log.info("Deleting ${person.username} from the roles")
                Role.findAll().each { it.removeFromPeople(person) }
                log.info("Deleting ${person.username} from secure access list")
                AuthUserSecureAccess.findAllByAuthUser(person).each { it.delete() }
                log.info("Deleting the gene signatures created by ${person.username}")
                try {
                    GeneSignature.findAllByCreatedByAuthUser(person).each { it.delete() }
                } catch (InvalidPropertyException ipe) {
                    log.warn("AuthUser properties in the GeneSignature domain need to be enabled")
                }
                log.info("Finally, deleting ${person.username}")
                person.delete()
                def msg = "$person.userRealName has been deleted."
                flash.message = msg
                new AccessLog(username: springSecurityService.getPrincipal().username, event: "User Deleted",
                        eventmessage: msg,
                        accesstime: new Date()).save()
            }
        } else {
            flash.message = "User not found with id $params.id"
        }
        redirect action: "list"
    }

    def edit = {
        def person = AuthUser.get(params.id)
        if (!person) {
            flash.message = "AuthUser not found with id $params.id"
            redirect action: "list"
            return
        }
        return buildPersonModel(person)
    }

    def update() {
        saveOrUpdate()
    }

    def create = {
        [person: new AuthUser(params), authorityList: Role.list()]
    }

    def save() {
        saveOrUpdate()
    }

    private saveOrUpdate() {
        boolean create = params.id == null
        AuthUser person = create ? new AuthUser() :
                AuthUser.load(params.id as Long)

        bindData person, params, [
                include: [
                        'enabled', 'username', 'userRealName', 'email',
                        'description', 'emailShow', 'authorities']]

        if (params.passwd) {
            person.passwd = springSecurityService.encodePassword(params.passwd)
        }
        person.name = person.userRealName

        /* the auditing should probably be done in the beforeUpdate() callback,
         * but that might cause problems in users created without a spring
         * security login (does this happen?) */
        def msg
        if (create) {
            msg = "User: ${person.username} for ${person.userRealName} created"
        } else {
            msg = "${person.username} has been updated. Changed fields include: "
            msg += person.dirtyPropertyNames.collect { field ->
                def newValue = person."$field"
                def oldValue = person.getPersistentValue(field)
                if (newValue != oldValue) {
                    "$field ($oldValue -> $newValue)"
                }
            }.findAll().join ', '
        }

        AuthUser.withTransaction { TransactionStatus tx ->
            manageRoles(person)
            if (person.validate() && person.save(flush: true)) {
                new AccessLog(
                        username: springSecurityService.getPrincipal().username,
                        event: "User ${create ? 'Created' : 'Updated'}",
                        eventmessage: msg.toString(),
                        accesstime: new Date()).save()
                redirect action: "show", id: person.id
            } else {
                tx.setRollbackOnly()
                flash.message = 'Cannot save user'
                render view: create ? 'create' : 'edit',
                        model: [authorityList: Role.list(), person: person]
            }
        }
    }

    /* the owning side of the many-to-many are the roles */

    private void manageRoles(AuthUser person) {
        def oldRoles = person.authorities.collect {
            Role.findByAuthority(it.authority)
        } ?: Collections.emptySet()
        def newRoles = params.findAll { String key, String value ->
            key.contains('ROLE') && value == 'on'
        }.collect {
            Role.findByAuthority(it.key)
        } as Set

        (newRoles - oldRoles).each {
            it.addToPeople(person)
        }
        (oldRoles - newRoles).each {
            it.removeFromPeople(person)
        }
    }

    private Map buildPersonModel(person) {
        List roles = Role.list()
        roles.sort { r1, r2 ->
            r1.authority <=> r2.authority
        }
        Set userRoleNames = []
        for (role in person.authorities) {
            userRoleNames << role.authority
        }
        LinkedHashMap<Role, Boolean> roleMap = [:]
        for (role in roles) {
            roleMap[(role)] = userRoleNames.contains(role.authority)
        }
        return [person: person, roleMap: roleMap]
    }
}
