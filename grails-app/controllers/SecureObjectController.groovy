import org.transmart.searchapp.SecureObject;

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
  



class SecureObjectController {

	def index = { redirect(action:list,params:params) }

	// the delete, save and update actions only accept POST requests
	static allowedMethods = [delete:'POST', save:'POST', update:'POST']

	def list = {
		params.max = Math.min( params.max ? params.max.toInteger() : grailsApplication.config.com.recomdata.admin.paginate.max,  100)
		[ secureObjectInstanceList: SecureObject.list( params ), secureObjectInstanceTotal: SecureObject.count() ]
	}

	def show = {
		def secureObjectInstance = SecureObject.get( params.id )

		if(!secureObjectInstance) {
			flash.message = "SecureObject not found with id ${params.id}"
			redirect(action:list)
		}
		else { return [ secureObjectInstance : secureObjectInstance ] }
	}

	def delete = {
		def secureObjectInstance = SecureObject.get( params.id )
		if(secureObjectInstance) {
			try {
				secureObjectInstance.delete()
				flash.message = "SecureObject ${params.id} deleted"
				redirect(action:list)
			}
			catch(org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "SecureObject ${params.id} could not be deleted"
				redirect(action:show,id:params.id)
			}
		}
		else {
			flash.message = "SecureObject not found with id ${params.id}"
			redirect(action:list)
		}
	}

	def edit = {
		def secureObjectInstance = SecureObject.get( params.id )

		if(!secureObjectInstance) {
			flash.message = "SecureObject not found with id ${params.id}"
			redirect(action:list)
		}
		else {
			return [ secureObjectInstance : secureObjectInstance ]
		}
	}

	def update = {
		def secureObjectInstance = SecureObject.get( params.id )
		if(secureObjectInstance) {
			if(params.version) {
				def version = params.version.toLong()
				if(secureObjectInstance.version > version) {

					secureObjectInstance.errors.rejectValue("version", "secureObject.optimistic.locking.failure", "Another user has updated this SecureObject while you were editing.")
					render(view:'edit',model:[secureObjectInstance:secureObjectInstance])
					return
				}
			}
			secureObjectInstance.properties = params
			if(!secureObjectInstance.hasErrors() && secureObjectInstance.save()) {
				flash.message = "SecureObject ${params.id} updated"
				redirect(action:show,id:secureObjectInstance.id)
			}
			else {
				render(view:'edit',model:[secureObjectInstance:secureObjectInstance])
			}
		}
		else {
			flash.message = "SecureObject not found with id ${params.id}"
			redirect(action:edit,id:params.id)
		}
	}

	def create = {
		def secureObjectInstance = new SecureObject()
		secureObjectInstance.properties = params
		return ['secureObjectInstance':secureObjectInstance]
	}

	def save = {
		def secureObjectInstance = new SecureObject(params)
		if(!secureObjectInstance.hasErrors() && secureObjectInstance.save()) {
			flash.message = "SecureObject ${secureObjectInstance.id} created"
			redirect(action:show,id:secureObjectInstance.id)
		}
		else {
			render(view:'create',model:[secureObjectInstance:secureObjectInstance])
		}
	}
}
