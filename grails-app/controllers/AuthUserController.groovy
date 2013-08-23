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
  

 /***
 * $Id: AuthUserController.groovy 10098 2011-10-19 18:39:32Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 10098 $
 */

import groovy.sql.Sql

import java.sql.BatchUpdateException
import java.sql.SQLException

import org.codehaus.groovy.grails.exceptions.InvalidPropertyException
import org.transmart.searchapp.AccessLog;
import org.transmart.searchapp.AuthUser;
import org.transmart.searchapp.AuthUserSecureAccess;
import org.transmart.searchapp.Role;

import org.transmart.searchapp.GeneSignature

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
			}
			else {				
				log.info("Deleting ${person.username} from the roles")
				Role.findAll().each {it.removeFromPeople(person)}
				log.info("Deleting ${person.username} from secure access list")				 
				AuthUserSecureAccess.findAllByAuthUser(person).each {it.delete()}
				log.info("Deleting the gene signatures created by ${person.username}")
                try {
                    GeneSignature.findAllByCreatedByAuthUser(person).each {it.delete()}
                } catch(InvalidPropertyException ipe)   {
                    log.warn("AuthUser properties in the GeneSignature domain need to be enabled")
                }
				log.info("Finally, deleting ${person.username}")															
				person.delete()
				def msg = "$person.userRealName has been deleted."
				flash.message = msg
				new AccessLog(username: userName, event:"User Deleted",
					eventmessage: msg,
					accesstime:new Date()).save()
			}
		}
		else {
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
		
		if(!params.passwd.equals(person.getPersistentValue("passwd")))	{
			log.info("Password has changed, encrypting new password")	
			person.passwd = springSecurityService.encodePassword(params.passwd)
		}
		
		def msg = new StringBuilder("${person.username} has been updated.  Changed fields include: ")				
		def modifiedFieldNames = person.getDirtyPropertyNames()
		for (fieldName in modifiedFieldNames)	{			
			def currentValue =person."$fieldName"
			def origValue = person.getPersistentValue(fieldName)
			if (currentValue != origValue)	{
				msg.append(" ${fieldName} ")
			}			
		}
		
		if (person.save()) {
			new AccessLog(username: springSecurityService.getPrincipal().username, event:"User Updated",
				eventmessage: msg,
				accesstime:new Date()).save()
			Role.findAll().each { it.removeFromPeople(person) }
			addRoles(person)
			redirect action: show, id: person.id
		}
		else {
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
        def next_id

        if(params.id==null || params.id=="") {
            def sql = new Sql(dataSource);
            def seqSQL = "SELECT nextval('searchapp.hibernate_sequence')";
            def result = sql.firstRow(seqSQL);
            next_id = result.nextval
        }
        else
            next_id = new Long(params.id)

        if(params.email==null || params.email=="") {
            flash.message = 'Please enter an email'
            return render (view:'create', model:[person: new AuthUser(params), authorityList: Role.list()])
        }

        person.id = next_id
		person.passwd = springSecurityService.encodePassword(params.passwd)
		person.uniqueId = ''
		person.name=person.userRealName;

        try {
            if (person.save()) {
                addRoles(person)
                def msg = "User: ${person.username} for ${person.userRealName} created";
                new AccessLog(username: springSecurityService.getPrincipal().username, event:"User Created",
                    eventmessage: msg,
                    accesstime:new Date()).save()
                redirect action: show, id: person.id
            }
            else {
                render view: 'create', model: [authorityList: Role.list(), person: person]
            }            
        } catch(BatchUpdateException bue)   {
            flash.message = 'Cannot create user'
            log.error(bue.getLocalizedMessage(), bue)
            render view: 'create', model: [authorityList: Role.list(), person: person]
        } catch(SQLException sqle)    {
            flash.message = 'Cannot create user'            
            log.error(sqle.getNextException().getMessage())
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
