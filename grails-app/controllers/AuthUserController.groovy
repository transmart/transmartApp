import org.transmart.searchapp.AccessLog
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.AuthUserSecureAccess
import org.transmart.searchapp.Role
import org.transmart.searchapp.GeneSignature

/**
 * User controller.
 */
class AuthUserController {
    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService

    // the delete, save and update actions only accept POST requests
    static Map allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index = {
        redirect action: list, params: params
    }

    def list = {
        if (!params.max) {
            params.max = grailsApplication.config.com.recomdata.admin.paginate.max
        }
        [personList: AuthUser.list(params)]
    }

    def show = {
        def person = AuthUser.get(params.id)
        if (!person) {
            flash.message = "AuthUser not found with id $params.id"
            redirect action: list
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
                GeneSignature.findAllByCreatedByAuthUser(person).each { it.delete() }
                log.info("Finally, deleting ${person.username}")
                person.delete()
                def msg = "$person.userRealName has been deleted."
                flash.message = msg
                new AccessLog(username: userName, event: "User Deleted",
                        eventmessage: msg,
                        accesstime: new Date()).save()
            }
        } else {
            flash.message = "User not found with id $params.id"
        }
        redirect action: list
    }

    def edit = {
        def person = AuthUser.get(params.id)
        if (!person) {
            flash.message = "AuthUser not found with id $params.id"
            redirect action: list
            return
        }
        return buildPersonModel(person)
    }

    /**
     * Person update action.
     */
    def update = {
        def person = AuthUser.get(params.id)
        person.properties = params

        if (!params.passwd.equals(person.getPersistentValue("passwd"))) {
            log.info("Password has changed, encrypting new password")
            person.passwd = springSecurityService.encodePassword(params.passwd)
        }

        def msg = new StringBuilder("${person.username} has been updated.  Changed fields include: ")
        def modifiedFieldNames = person.getDirtyPropertyNames()
        for (fieldName in modifiedFieldNames) {
            def currentValue = person."$fieldName"
            def origValue = person.getPersistentValue(fieldName)
            if (currentValue != origValue) {
                msg.append(" ${fieldName} ")
            }
        }

        if (person.save()) {
            new AccessLog(username: springSecurityService.getPrincipal().username, event: "User Updated",
                    eventmessage: msg,
                    accesstime: new Date()).save()
            Role.findAll().each { it.removeFromPeople(person) }
            addRoles(person)
            redirect action: show, id: person.id
        } else {
            render view: 'edit', model: buildPersonModel(person)
        }
    }

    def create = {
        [person: new AuthUser(params), authorityList: Role.list()]
    }

    /**
     * Person save action.
     */
    def save = {
        def person = new AuthUser()
        person.properties = params
        def luser = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        if (params.id == null || params.id == "") {
            flash.message = 'Please enter an ID'
            return render(view: 'create', model: [person: new AuthUser(params), authorityList: Role.list()])
        }

        def user = AuthUser.get(params.id)
        if (user != null) {
            flash.message = 'ID: ' + params.id + ' is already taken'
            return render(view: 'create', model: [person: new AuthUser(params), authorityList: Role.list()])
        }

        person.id = new Integer(params.id)
        person.passwd = springSecurityService.encodePassword(params.passwd)
        person.uniqueId = ''
        person.name = person.userRealName;

        if (person.save()) {
            addRoles(person)
            def msg = "User: ${person.username} for ${person.userRealName} created";
            new AccessLog(username: person.username, event: "User Created",
                    eventmessage: msg,
                    accesstime: new Date()).save()
            redirect action: show, id: person.id
        } else {
            render view: 'create', model: [authorityList: Role.list(), person: person]
        }
    }

    private void addRoles(person) {
        for (String key in params.keySet()) {
            if (key.contains('ROLE') && 'on' == params.get(key)) {
                Role.findByAuthority(key).addToPeople(person)
            }
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
