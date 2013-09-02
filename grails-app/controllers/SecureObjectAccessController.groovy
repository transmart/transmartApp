/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  
import command.SecureObjectAccessCommand
import org.transmart.searchapp.AccessLog
import org.transmart.searchapp.AuthUser;
import org.transmart.searchapp.Principal;
import org.transmart.searchapp.Role;
import org.transmart.searchapp.SecureAccessLevel;
import org.transmart.searchapp.SecureObject;
import org.transmart.searchapp.SecureObjectAccess;

class SecureObjectAccessController {

	def springSecurityService
	
	
	def index = { redirect(action:list,params:params)
	}

	// the delete, save and update actions only accept POST requests
	def allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def list = {
		if(!params.max) params.max = 10
		[ secureObjectAccessInstanceList: SecureObjectAccess.list( params ) ]
	}

	def show = {
		def secureObjectAccessInstance = SecureObjectAccess.get( params.id )

		if(!secureObjectAccessInstance) {
			flash.message = "SecureObjectAccess not found with id ${params.id}"
			redirect(action:list)
		}
		else { return [ secureObjectAccessInstance : secureObjectAccessInstance ]
		}
	}

	def delete = {
		def secureObjectAccessInstance = SecureObjectAccess.get( params.id )
		if(secureObjectAccessInstance) {
			secureObjectAccessInstance.delete()
			flash.message = "SecureObjectAccess ${params.id} deleted"
			redirect(action:list)
		}
		else {
			flash.message = "SecureObjectAccess not found with id ${params.id}"
			redirect(action:list)
		}
	}

	def edit = {
		def secureObjectAccessInstance = SecureObjectAccess.get( params.id )

		if(!secureObjectAccessInstance) {
			flash.message = "SecureObjectAccess not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			return [ secureObjectAccessInstance : secureObjectAccessInstance ]
		}
	}

	def update = {
		def secureObjectAccessInstance = SecureObjectAccess.get( params.id )
		if(secureObjectAccessInstance) {
			secureObjectAccessInstance.properties = params
			if(!secureObjectAccessInstance.hasErrors() && secureObjectAccessInstance.save()) {
				flash.message = "SecureObjectAccess ${params.id} updated"
				redirect(action:show,id:secureObjectAccessInstance.id)
			}
			else {
				render(view:'edit',model:[secureObjectAccessInstance:secureObjectAccessInstance])
			}
		}
		else {
			flash.message = "SecureObjectAccess not found with id ${params.id}"
			redirect(action:edit,id:params.id)
		}
	}

	def create = {
		def secureObjectAccessInstance = new SecureObjectAccess()
		secureObjectAccessInstance.properties = params
		return ['secureObjectAccessInstance':secureObjectAccessInstance]
	}

	def save = {
		def secureObjectAccessInstance = new SecureObjectAccess(params)
		if(!secureObjectAccessInstance.hasErrors() && secureObjectAccessInstance.save()) {
			flash.message = "SecureObjectAccess ${secureObjectAccessInstance.id} created"
			redirect(action:show,id:secureObjectAccessInstance.id)
		}
		else {
			render(view:'create',model:[secureObjectAccessInstance:secureObjectAccessInstance])
		}
	}


    def manageAccessBySecObj = {
        def secureObjInstance
        if(params.secureobjectid!=null)
            secureObjInstance =	SecureObject.get( params.secureobjectid);
        if (secureObjInstance == null)
            secureObjInstance =	SecureObject.get(SecureObject.listOrderByDisplayName().first().id)

        def access = SecureAccessLevel.findByAccessLevelName("VIEW");
        def accessid = params.accesslevelid
        if(accessid!=null){
            access = SecureAccessLevel.get(accessid);
        }

        def searchtext=params.searchtext;
        if(searchtext==null)
            searchtext=''

        def secureObjectAccessList = getSecureObjAccessList(secureObjInstance, access);
        def userwithoutaccess = getPrincipalsWithoutAccess(secureObjInstance, access, searchtext);

        render(view:'managePrincipalAccess',model:[
                secureObjectInstance:secureObjInstance,
                secureObjectAccessList: secureObjectAccessList,
                userwithoutaccess: userwithoutaccess,
                accesslevelid:access.id
        ] )
    }

	def addPrincipalToAccessList = {

			SecureObjectAccessCommand fl ->
			def secureObjInstance
			def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
			def msg = new StringBuilder(" Grant new access permission: ");

			if(params.secureobjectid!=null)
				secureObjInstance =	SecureObject.get( params.secureobjectid);
			def access = SecureAccessLevel.findByAccessLevelName("VIEW");
			def accessid = params.accesslevelid
			if(accessid!=null){
				access = SecureAccessLevel.get(accessid);
			}
			def searchtext=params.searchtext;
			if(fl.groupstoadd!=null)
			{
				def groupsToAdd = Principal.findAll("from Principal r where r.id in (:p)", [p:fl.groupstoadd.collect{it.toLong()
				}]);

				groupsToAdd.each{ r ->
					//println(principalInstance);
				//	println(r)
				//	println(access)
					addAccess(r, secureObjInstance, access);
					//println("Adding report:"+r.id);
					msg.append("<User:").append(r.name).append(", Permission:").append(access.accessLevelName).append(", Study:").append( secureObjInstance.bioDataUniqueId).append(">");
				};
			}

			new AccessLog(username:user.username, event:"ADMIN",	eventmessage: msg.toString(), accesstime:new Date()).save()
			
			def secureObjectAccessList = getSecureObjAccessList(secureObjInstance, access);
			def userwithoutaccess = getPrincipalsWithoutAccess(secureObjInstance, access, searchtext);
			render(template:'addremovePrincipal',model:[secureObjectAccessList: secureObjectAccessList,userwithoutaccess: userwithoutaccess])
	}
	
	def removePrincipalFromAccessList = {
		
		SecureObjectAccessCommand fl ->
			def secureObjInstance
			def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
			def msg = new StringBuilder(" Revoke access permission: ");
				
			if(params.secureobjectid!=null)
				secureObjInstance =	SecureObject.get( params.secureobjectid);
			def access = SecureAccessLevel.findByAccessLevelName("VIEW");
			def accessid = params.accesslevelid
			if(accessid!=null){
				access = SecureAccessLevel.get(accessid);
			}
			def searchtext=params.searchtext;
			if(fl.groupstoremove!=null)
			{
				def groupsToRemove = SecureObjectAccess.findAll("from SecureObjectAccess r where r.id in (:p)", [p:fl.groupstoremove.collect{it.toLong()
				}]);

				groupsToRemove.each{ r ->
						r.delete(flush:true);
						msg.append("<User:").append(r.principal.name).append(", Permission:").append(r.accessLevel.accessLevelName).append(", Study:").append( r.secureObject.bioDataUniqueId).append(">");
						
				};
			}
			
			new AccessLog(username:user.username, event:"ADMIN",	eventmessage: msg.toString(), accesstime:new Date()).save()
			
		def 	secureObjectAccessList = getSecureObjAccessList(secureObjInstance, access);
		def 	userwithoutaccess = getPrincipalsWithoutAccess(secureObjInstance, access, searchtext);

			render(template:'addremovePrincipal',model:[
			                          		secureObjectAccessList: secureObjectAccessList,
			                          		userwithoutaccess: userwithoutaccess
			                          		] )
	}

	def manageAccess = {
		def pid = params.currentprincipalid;
		def access = SecureAccessLevel.findByAccessLevelName("VIEW");
		def accessid = params.accesslevelid
		if(accessid!=null){
			access = SecureAccessLevel.get(accessid);
		}
		def principalInstance
		if (pid!=null)	{
			principalInstance = Principal.get(pid)
		}
		def secureObjectAccessList=getSecureObjAccessListForPrincipal(principalInstance, access);
		def objectswithoutaccess=getObjsWithoutAccessForPrincipal(principalInstance, '');
		render(view:'manageAccess',model:[principalInstance:principalInstance,
		accessLevelList:SecureAccessLevel.listOrderByAccessLevelValue(),
		secureObjectAccessList: secureObjectAccessList,
		objectswithoutaccess: objectswithoutaccess,
		accesslevelid:access.id] )
	}

	def accessLevelChange = {

	}

	def listAccessForPrincipal = {
	//	println(params)
		def principalInstance = Principal.get( params.id)
		def accesslevelid = params.accesslevelid;
		def access = SecureAccessLevel.findByAccessLevelName("VIEW");
		if(accesslevelid!=null){
			access = SecureAccessLevel.get(accesslevelid)
		}
		accesslevelid = access.id;
		if(!principalInstance) {
			flash.message = "Please select a user/group."
			render(template:'addremove',model:[principalInstance: principalInstance, secureObjectAccessList: [], objectswithoutaccess: []])
			return;
		}

		def searchtext=params.searchtext;
		//println(searchtext)
		def secureObjectAccessList=getSecureObjAccessListForPrincipal(principalInstance, access);
		def objectswithoutaccess=getObjsWithoutAccessForPrincipal(principalInstance, searchtext);
		render(template:'addremoveAccess',model:[principalInstance: principalInstance,
		secureObjectAccessList: secureObjectAccessList,
		objectswithoutaccess: objectswithoutaccess,
		accesslevelid:accesslevelid])
	}

	def addSecObjectsToPrincipal = {SecureObjectAccessCommand fl ->
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		def msg = new StringBuilder(" Grant new access permission: ");
			
		//println("INCOMOING users to add:"+params.userstoadd);
		def principalInstance = Principal.get( params.id);
		def access = SecureAccessLevel.get(params.accesslevelid)
		if(fl.sobjectstoadd!=null)
		{
			def objectsToAdd = SecureObject.findAll("from SecureObject r where r.id in (:p)", [p:fl.sobjectstoadd.collect{it.toLong()
			}]);

			objectsToAdd.each{ r ->
			//	println(principalInstance);
			//	println(r)
			//	println(access)
				addAccess(principalInstance, r, access);
				//println("Adding report:"+r.id);
				msg.append("<User:").append(principalInstance.name).append(", Permission:").append(access.accessLevelName).append(", Study:").append(r.bioDataUniqueId).append(">");
				
			};
		}
		new AccessLog(username:user.username, event:"ADMIN",	eventmessage: msg.toString(), accesstime:new Date()).save()
		def searchtext=params.searchtext;
		def secureObjAccessList=getSecureObjAccessListForPrincipal(principalInstance, access);
		def objectswithoutaccess=getObjsWithoutAccessForPrincipal(principalInstance, searchtext);
		render(template:'addremoveAccess',model:[principalInstance: principalInstance, secureObjectAccessList: secureObjAccessList, objectswithoutaccess: objectswithoutaccess])
	}

	def removeSecObjectsFromPrincipal = {SecureObjectAccessCommand fl ->

		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		def msg = new StringBuilder(" Revoke access permission: ");
			
	
		//println("INCOMOING users to add:"+params.userstoadd);
		def principalInstance = Principal.get( params.id);
		def access = SecureAccessLevel.get(params.accesslevelid)
		if(fl.sobjectstoremove!=null)
		{
			def objectsToRemove = SecureObjectAccess.findAll("from SecureObjectAccess r where r.id in (:p)", [p:fl.sobjectstoremove.collect{it.toLong()
			}]);

			objectsToRemove.each{ r ->
				r.delete(flush:true);
				msg.append("<User:").append(r.principal.name).append(", Permission:").append(r.accessLevel.accessLevelName).append(", Study:").append( r.secureObject.bioDataUniqueId).append(">");
				
			};
		}
		
		new AccessLog(username:user.username, event:"ADMIN",	eventmessage: msg.toString(), accesstime:new Date()).save()
		
		def searchtext=params.searchtext;
		def secureObjAccessList=getSecureObjAccessListForPrincipal(principalInstance, access);
		def objectswithoutaccess=getObjsWithoutAccessForPrincipal(principalInstance, searchtext);
		render(template:'addremoveAccess',model:[principalInstance: principalInstance, secureObjectAccessList: secureObjAccessList, objectswithoutaccess: objectswithoutaccess])
	}



	def getObjsWithoutAccessForPrincipal(principal, insearchtext) {
		def searchtext='%'+insearchtext.toString().toUpperCase()+'%'
		//	println(searchtext)
		println(principal)
		if(principal!=null)
			return SecureObject.findAll(" FROM SecureObject s WHERE s.dataType='BIO_CLINICAL_TRIAL' AND s.id NOT IN(SELECT so.secureObject.id FROM SecureObjectAccess so WHERE so.principal =:p ) and upper(s.displayName) like :dn ORDER BY s.displayName ",[p:principal,dn:searchtext]);
		else
			return [];//SecureObject.findAll(" FROM SecureObject s WHERE 1=0);

	}

	def getSecureObjAccessListForPrincipal(principal, access) {
		if(principal!=null)
			return SecureObjectAccess.findAll(" FROM SecureObjectAccess s WHERE s.principal =:p and s.accessLevel=:ac ORDER BY s.principal.name ", [p:principal,ac:access])
		else
			return [];//SecureObjectAccess.findAll(" FROM SecureObject s where 1=0");
	}

	def addAccess(principal, secobject,access){
		def secureObjectAccessInstance = new SecureObjectAccess();
		secureObjectAccessInstance.principal = principal;
		secureObjectAccessInstance.secureObject = secobject;
		secureObjectAccessInstance.accessLevel = access;
		secureObjectAccessInstance.save(flush:true);

	}


	def isAllowOwn(id){
		def authUser = AuthUser.get(id);
		for(role in authUser.authorities){
			if(Role.SPECTATOR_ROLE.equalsIgnoreCase(role.authority)){
				return false;
			}
		}
		return true;
	}

	def getAccessLevelList(id){
		def accessLevelList =[];
		if(!isAllowOwn(id)){

			accessLevelList= SecureAccessLevel.findAll("FROM SecureAccessLevel WHERE accessLevelName <>'OWN' ORDER BY accessLevelValue")
		}else{
			accessLevelList=	SecureAccessLevel.listOrderByAccessLevelValue();
		}
	}


	def listAccessLevel ={
		//log.debug(params);

		//	log.debug(accessLevelList);
		render(template:'accessLevelList', model:[accessLevelList:getAccessLevelList(params.id)]);
	}


	 def getSecureObjAccessList(secureObj, access)
	{
		if(secureObj==null)
			return []

		return SecureObjectAccess.findAll(" FROM SecureObjectAccess s WHERE s.secureObject = :so AND s.accessLevel = :al ORDER BY s.principal.name", [so:secureObj,al:access]);
	}

	def getPrincipalsWithoutAccess(secureObj, access, insearchtext)
	{
		//println(secureObj)
		if(secureObj == null)
			return []
		def searchtext='%'+insearchtext.toString().toUpperCase()+'%'
//		println(searchtext)
		return Principal.findAll('from Principal g WHERE g.id NOT IN (SELECT so.principal.id from SecureObjectAccess so WHERE so.secureObject =:secObj AND so.accessLevel =:al ) AND upper(g.name) like :st ORDER BY g.name', [secObj:secureObj, al:access, st:searchtext] );
	}
}
