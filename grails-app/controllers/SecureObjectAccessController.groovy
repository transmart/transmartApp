import command.SecureObjectAccessCommand
import org.transmart.searchapp.*
import org.transmartproject.core.users.User

class SecureObjectAccessController {

    def accessLogService
    def springSecurityService
    User currentUserBean


    def index = {
        redirect(action: "list", params: params)
    }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def list = {
        if (!params.max) {
            params.max = 10
        }
        [secureObjectAccessInstanceList: SecureObjectAccess.list(params)]
    }

    def show = {
        def secureObjectAccessInstance = SecureObjectAccess.get(params.id)

        if (!secureObjectAccessInstance) {
            flash.message = "SecureObjectAccess not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [secureObjectAccessInstance: secureObjectAccessInstance]
        }
    }

    def delete = {
        def secureObjectAccessInstance = SecureObjectAccess.get(params.id)
        if (secureObjectAccessInstance) {
            secureObjectAccessInstance.delete()
            flash.message = "SecureObjectAccess ${params.id} deleted"
            redirect(action: "list")
        } else {
            flash.message = "SecureObjectAccess not found with id ${params.id}"
            redirect(action: "list")
        }
    }

    def edit = {
        def secureObjectAccessInstance = SecureObjectAccess.get(params.id)

        if (!secureObjectAccessInstance) {
            flash.message = "SecureObjectAccess not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [secureObjectAccessInstance: secureObjectAccessInstance]
        }
    }

    def update = {
        def secureObjectAccessInstance = SecureObjectAccess.get(params.id)
        if (secureObjectAccessInstance) {
            secureObjectAccessInstance.properties = params
            if (!secureObjectAccessInstance.hasErrors() && secureObjectAccessInstance.save()) {
                flash.message = "SecureObjectAccess ${params.id} updated"
                redirect(action: "show", id: secureObjectAccessInstance.id)
            } else {
                render(view: 'edit', model: [secureObjectAccessInstance: secureObjectAccessInstance])
            }
        } else {
            flash.message = "SecureObjectAccess not found with id ${params.id}"
            redirect(action: "edit", id: params.id)
        }
    }

    def create = {
        def secureObjectAccessInstance = new SecureObjectAccess()
        secureObjectAccessInstance.properties = params
        return ['secureObjectAccessInstance': secureObjectAccessInstance]
    }

    def save = {
        def secureObjectAccessInstance = new SecureObjectAccess(params)
        if (!secureObjectAccessInstance.hasErrors() && secureObjectAccessInstance.save()) {
            flash.message = "SecureObjectAccess ${secureObjectAccessInstance.id} created"
            redirect(action: "show", id: secureObjectAccessInstance.id)
        } else {
            render(view: 'create', model: [secureObjectAccessInstance: secureObjectAccessInstance])
        }
    }


    def manageAccessBySecObj = {
        def secureObjInstance
        if (params.secureobjectid != null) {
            secureObjInstance = SecureObject.get(params.secureobjectid);
        }
        if (secureObjInstance == null) {
            secureObjInstance = SecureObject.list(
                    sort: 'displayName', order: 'asc', max: 1)?.first()
        }

        def access = SecureAccessLevel.findByAccessLevelName("VIEW");
        def accessid = params.accesslevelid
        if (accessid != null) {
            access = SecureAccessLevel.get(accessid);
        }
        def searchtext = params.searchtext;
        if (searchtext == null) {
            searchtext = ''
        }
        def secureObjectAccessList = getSecureObjAccessList(secureObjInstance, access)
        def userwithoutaccess = getPrincipalsWithoutAccess(secureObjInstance, access, searchtext)

        log.debug("accesslist: $secureObjectAccessList")
        log.debug("noaccess: $userwithoutaccess")
        log.debug("sec: $secureObjInstance")

        render(view: 'managePrincipalAccess', model: [
                secureObjectInstance  : secureObjInstance,
                secureObjectAccessList: secureObjectAccessList,
                userwithoutaccess     : userwithoutaccess,
                accesslevelid         : access?.id
        ])
    }

    def addPrincipalToAccessList = {

        SecureObjectAccessCommand fl ->
            def secureObjInstance
            def msg = new StringBuilder(" Grant new access permission: ");

            if (params.secureobjectid != null) {
                secureObjInstance = SecureObject.get(params.secureobjectid)
            };
            def access = SecureAccessLevel.findByAccessLevelName("VIEW");
            def accessid = params.accesslevelid
            if (accessid != null) {
                access = SecureAccessLevel.get(accessid);
            }
            def searchtext = params.searchtext;
            if (fl.groupstoadd != null) {
                def groupsToAdd = Principal.findAll("from Principal r where r.id in (:p)", [p: fl.groupstoadd.collect {
                    it.toLong()
                }]);

                groupsToAdd.each { r ->
                    addAccess(r, secureObjInstance, access);
                    msg
                            .append("<User:")
                            .append(r.name)
                            .append(", Permission:")
                            .append(access.accessLevelName)
                            .append(", Study:")
                            .append(secureObjInstance.bioDataUniqueId).append(">")
                };
            }
            accessLogService.report(currentUserBean, 'ADMIN', eventMessage:  msg.toString())
            def secureObjectAccessList = getSecureObjAccessList(secureObjInstance, access);
            def userwithoutaccess = getPrincipalsWithoutAccess(secureObjInstance, access, searchtext);

            render(template: 'addremovePrincipal', model: [
                    secureObjectAccessList: secureObjectAccessList,
                    userwithoutaccess     : userwithoutaccess
            ])
    }
    def removePrincipalFromAccessList = {

        SecureObjectAccessCommand fl ->
            def secureObjInstance
            def msg = new StringBuilder(" Revoke access permission: ");

            if (params.secureobjectid != null) {
                secureObjInstance = SecureObject.get(params.secureobjectid)
            };
            def access = SecureAccessLevel.findByAccessLevelName("VIEW");
            def accessid = params.accesslevelid
            if (accessid != null) {
                access = SecureAccessLevel.get(accessid);
            }
            def searchtext = params.searchtext;
            if (fl.groupstoremove != null) {
                def groupsToRemove = SecureObjectAccess.findAll("from SecureObjectAccess r where r.id in (:p)", [p: fl.groupstoremove.collect {
                    it.toLong()
                }]);

                groupsToRemove.each { r ->
                    r.delete(flush: true);
                    msg.append("<User:")
                            .append(r.principal.name)
                            .append(", Permission:")
                            .append(r.accessLevel.accessLevelName)
                            .append(", Study:")
                            .append(r.secureObject.bioDataUniqueId).append(">")

                };
            }

            accessLogService.report(currentUserBean, 'ADMIN', eventMessage:  msg.toString())

            def secureObjectAccessList = getSecureObjAccessList(secureObjInstance, access);
            def userwithoutaccess = getPrincipalsWithoutAccess(secureObjInstance, access, searchtext);

            render(template: 'addremovePrincipal', model: [
                    secureObjectAccessList: secureObjectAccessList,
                    userwithoutaccess     : userwithoutaccess
            ])
    }

    def manageAccess = {
        def pid = params.currentprincipalid;
        def access = SecureAccessLevel.findByAccessLevelName("VIEW");
        def accessid = params.accesslevelid
        if (accessid != null) {
            access = SecureAccessLevel.get(accessid);
        }
        def principalInstance
        if (pid != null) {
            principalInstance = Principal.get(pid)
        }
        def secureObjectAccessList = getSecureObjAccessListForPrincipal(principalInstance, access);
        def objectswithoutaccess = getObjsWithoutAccessForPrincipal(principalInstance, '');

        render(view: 'manageAccess', model: [principalInstance     : principalInstance,
                                             accessLevelList       : SecureAccessLevel.listOrderByAccessLevelValue(),
                                             secureObjectAccessList: secureObjectAccessList,
                                             objectswithoutaccess  : objectswithoutaccess,
                                             accesslevelid         : access?.id])
    }

    def accessLevelChange = {

    }

    def listAccessForPrincipal = {
        def principalInstance = Principal.get params.currentprincipalid
        def accesslevelid = params.accesslevelid;
        def access = SecureAccessLevel.findByAccessLevelName("VIEW");
        if (accesslevelid != null) {
            access = SecureAccessLevel.get(accesslevelid)
        }
        accesslevelid = access.id;
        if (!principalInstance) {
            flash.message = "Please select a user/group."
            render(template: 'addremoveAccess', model: [principalInstance: principalInstance, secureObjectAccessList: [], objectswithoutaccess: []])
            return;
        }

        def searchtext = params.searchtext;
        def secureObjectAccessList = getSecureObjAccessListForPrincipal(principalInstance, access);
        def objectswithoutaccess = getObjsWithoutAccessForPrincipal(principalInstance, searchtext);
        render(template: 'addremoveAccess', model: [principalInstance     : principalInstance,
                                                    secureObjectAccessList: secureObjectAccessList,
                                                    objectswithoutaccess  : objectswithoutaccess,
                                                    accesslevelid         : accesslevelid])
    }

    def addSecObjectsToPrincipal = { SecureObjectAccessCommand fl ->
        def user = springSecurityService.getPrincipal()
        def msg = new StringBuilder(" Grant new access permission: ");

        def principalInstance = Principal.get(params.currentprincipalid);
        def access = SecureAccessLevel.get(params.accesslevelid)
        if (principalInstance && access && fl.sobjectstoadd) {
            def objectsToAdd = SecureObject.findAll("from SecureObject r where r.id in (:p)", [p: fl.sobjectstoadd.collect {
                it.toLong()
            }]);

            objectsToAdd.each { r ->
                addAccess(principalInstance, r, access);
                msg.append("<User:").append(principalInstance.name).append(", Permission:").append(access.accessLevelName).append(", Study:").append(r.bioDataUniqueId).append(">");

            };

            new AccessLog(username: user.username, event: "ADMIN", eventmessage: msg.toString(), accesstime: new Date()).save()
        }
        def searchtext = params.searchtext;
        def secureObjAccessList = getSecureObjAccessListForPrincipal(principalInstance, access);
        def objectswithoutaccess = getObjsWithoutAccessForPrincipal(principalInstance, searchtext);
        render(template: 'addremoveAccess', model: [principalInstance: principalInstance, secureObjectAccessList: secureObjAccessList, objectswithoutaccess: objectswithoutaccess])
    }

    def removeSecObjectsFromPrincipal = { SecureObjectAccessCommand fl ->

        def user = springSecurityService.getPrincipal()
        def msg = new StringBuilder(" Revoke access permission: ");

        def principalInstance = Principal.get(params.currentprincipalid);
        def access = SecureAccessLevel.get(params.accesslevelid)
        if (principalInstance && access && fl.sobjectstoremove) {
            def objectsToRemove = SecureObjectAccess.findAll("from SecureObjectAccess r where r.id in (:p)", [p: fl.sobjectstoremove.collect {
                it.toLong()
            }]);

            objectsToRemove.each { r ->
                r.delete(flush: true);
                msg.append("<User:").append(r.principal.name).append(", Permission:").append(r.accessLevel.accessLevelName).append(", Study:").append(r.secureObject.bioDataUniqueId).append(">");

            };

            new AccessLog(username: user.username, event: "ADMIN", eventmessage: msg.toString(), accesstime: new Date()).save()
        }

        def searchtext = params.searchtext;
        def secureObjAccessList = getSecureObjAccessListForPrincipal(principalInstance, access);
        def objectswithoutaccess = getObjsWithoutAccessForPrincipal(principalInstance, searchtext);
        render(template: 'addremoveAccess', model: [principalInstance: principalInstance, secureObjectAccessList: secureObjAccessList, objectswithoutaccess: objectswithoutaccess])
    }


    def getObjsWithoutAccessForPrincipal(principal, insearchtext) {
        def searchtext = '%' + insearchtext.toString().toUpperCase() + '%'
        if (principal != null) {
            return SecureObject.findAll(" FROM SecureObject s WHERE s.dataType='BIO_CLINICAL_TRIAL' AND s.id NOT IN(SELECT so.secureObject.id FROM SecureObjectAccess so WHERE so.principal =:p ) and upper(s.displayName) like :dn ORDER BY s.displayName ", [p: principal, dn: searchtext])
        } else {
            return []
        }; //SecureObject.findAll(" FROM SecureObject s WHERE 1=0);

    }

    private getSecureObjAccessListForPrincipal(principal, access) {
        if (principal != null) {
            return SecureObjectAccess.findAll(" FROM SecureObjectAccess s WHERE s.principal =:p and s.accessLevel=:ac ORDER BY s.principal.name ", [p: principal, ac: access])
        } else {
            return []
        }
    }

    def addAccess(principal, secobject, access) {
        def secureObjectAccessInstance = new SecureObjectAccess();
        secureObjectAccessInstance.principal = principal;
        secureObjectAccessInstance.secureObject = secobject;
        secureObjectAccessInstance.accessLevel = access;
        secureObjectAccessInstance.save(flush: true);

    }


    def isAllowOwn(id) {
        def authUser = AuthUser.get(id);
        for (role in authUser.authorities) {
            if (Role.SPECTATOR_ROLE.equalsIgnoreCase(role.authority)) {
                return false;
            }
        }
        return true;
    }

    def getAccessLevelList(id) {
        def accessLevelList = [];
        if (!isAllowOwn(id)) {

            accessLevelList = SecureAccessLevel.findAll("FROM SecureAccessLevel WHERE accessLevelName <>'OWN' ORDER BY accessLevelValue")
        } else {
            accessLevelList = SecureAccessLevel.listOrderByAccessLevelValue();
        }
    }


    def listAccessLevel = {
        render(template: 'accessLevelList', model: [accessLevelList: getAccessLevelList(params.id)]);
    }


    def getSecureObjAccessList(secureObj, access) {
        if (secureObj == null) {
            return []
        }

        return SecureObjectAccess.findAll(" FROM SecureObjectAccess s WHERE s.secureObject = :so AND s.accessLevel = :al ORDER BY s.principal.name", [so: secureObj, al: access]);
    }

    def getPrincipalsWithoutAccess(secureObj, access, insearchtext) {
        if (secureObj == null) {
            return []
        }
        def searchtext = '%' + insearchtext.toString().toUpperCase() + '%'
        return Principal.findAll('from Principal g WHERE g.id NOT IN (SELECT so.principal.id from SecureObjectAccess so WHERE so.secureObject =:secObj AND so.accessLevel =:al ) AND upper(g.name) like :st ORDER BY g.name', [secObj: secureObj, al: access, st: searchtext]);
    }
}
