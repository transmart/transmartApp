import org.transmart.searchapp.AuthUser;
import org.transmart.searchapp.AuthUserSecureAccess;
import org.transmart.searchapp.Role;
import org.transmart.searchapp.SecureAccessLevel;

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
  



class AuthUserSecureAccessController {

	def index = { redirect(action:list,params:params) }

	// the delete, save and update actions only accept POST requests
	static allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def list = {
	   params.max = Math.min( params.max ? params.max.toInteger() : grailsApplication.config.com.recomdata.admin.paginate.max,  100)
   //    params.max = Math.min( params.max ? params.max.toInteger() : 20,  100)

		// NOTE: grails <g:sortableColumn> can't reference subobjects, which means that we can just use .list(params) to
		// query AuthUserSecureAccess. Instead we need to build a hibernate query and map custom property names to the subobjects.
		// http://grails.org/GSP+Tag+-+sortableColumn
		params.offset = params.offset ? params.offset.toInteger() : 0
		params.order = params.order ? params.order : "asc"
		params.sort = params.sort ? params.sort : "username"
		def list = AuthUserSecureAccess.withCriteria {
			maxResults(params.max)
			firstResult(params.offset)
			if (params.sort == "username") {
				authUser {
					order("username", params.order)
				}
			} else if (params.sort == "accessLevelName") {
				accessLevel {
					order("accessLevelName", params.order)
				}
			} else if (params.sort == "displayName") {
				secureObject {
					order("displayName", params.order)
				}
			} else {
				order(params.sort, params.order)
			}
		}
		[ authUserSecureAccessInstanceList: list, authUserSecureAccessInstanceTotal: AuthUserSecureAccess.count() ]
	}

	def show = {
		def authUserSecureAccessInstance = AuthUserSecureAccess.get( params.id )

		if(!authUserSecureAccessInstance) {
			flash.message = "AuthUserSecureAccess not found with id ${params.id}"
			redirect(action:list)
		}
		else { return [ authUserSecureAccessInstance : authUserSecureAccessInstance ] }
	}

	def delete = {
		def authUserSecureAccessInstance = AuthUserSecureAccess.get( params.id )
		if(authUserSecureAccessInstance) {
			try {
				authUserSecureAccessInstance.delete()
				flash.message = "AuthUserSecureAccess ${params.id} deleted"
				redirect(action:list)
			}
			catch(org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "AuthUserSecureAccess ${params.id} could not be deleted"
				redirect(action:show,id:params.id)
			}
		}
		else {
			flash.message = "AuthUserSecureAccess not found with id ${params.id}"
			redirect(action:list)
		}
	}

	def edit = {
		def authUserSecureAccessInstance = AuthUserSecureAccess.get( params.id )

		if(!authUserSecureAccessInstance) {
			flash.message = "AuthUserSecureAccess not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			def id = authUserSecureAccessInstance.authUser.id
			return [ authUserSecureAccessInstance : authUserSecureAccessInstance, accessLevelList:getAccessLevelList(id)]
		}
	}

	def update = {
		def authUserSecureAccessInstance = AuthUserSecureAccess.get( params.id )
		if(authUserSecureAccessInstance) {
			if(params.version) {
				def version = params.version.toLong()
				if(authUserSecureAccessInstance.version > version) {

					authUserSecureAccessInstance.errors.rejectValue("version", "authUserSecureAccess.optimistic.locking.failure", "Another user has updated this AuthUserSecureAccess while you were editing.")
					render(view:'edit',model:[authUserSecureAccessInstance:authUserSecureAccessInstance])
					return
				}
			}
			authUserSecureAccessInstance.properties = params
			if(!authUserSecureAccessInstance.hasErrors() && authUserSecureAccessInstance.save()) {
				flash.message = "AuthUserSecureAccess ${params.id} updated"
				redirect(action:show,id:authUserSecureAccessInstance.id)
			}
			else {
				render(view:'edit',model:[authUserSecureAccessInstance:authUserSecureAccessInstance])
			}
		}
		else {
			flash.message = "AuthUserSecureAccess not found with id ${params.id}"
			redirect(action:edit,id:params.id)
		}
	}

	def create = {
		def authUserSecureAccessInstance = new AuthUserSecureAccess()
		authUserSecureAccessInstance.properties = params
		return ['authUserSecureAccessInstance':authUserSecureAccessInstance]
	}

	def save = {
		def authUserSecureAccessInstance = new AuthUserSecureAccess(params)
		if(!authUserSecureAccessInstance.hasErrors() && authUserSecureAccessInstance.save()) {
			flash.message = "AuthUserSecureAccess ${authUserSecureAccessInstance.id} created"
			redirect(action:show,id:authUserSecureAccessInstance.id)
		}
		else {
			render(view:'create',model:[authUserSecureAccessInstance:authUserSecureAccessInstance])
		}
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
}
