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



import org.transmart.searchapp.AccessLog
import org.transmart.searchapp.AuthUser;
import org.transmart.searchapp.Principal
import org.transmart.searchapp.Role;
import org.transmart.searchapp.UserGroup;

import groovy.sql.Sql;
import command.UserGroupCommand;
import grails.converters.*

import java.sql.BatchUpdateException
import java.sql.SQLException;

class UserGroupController {
	def dataSource ;


    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        if(!params.max) {
            params.max = 10
        }
        [ groupList: UserGroup.findAllByIdGreaterThanEquals(0, params ) ]
    }

    def membership = {

    }

    def show = {
        def group = UserGroup.get( params.id )

        if(!group) {
            flash.message = "UserGroup not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ group : group ] }
    }

    def delete = {
        def group = UserGroup.get( params.id )
        if(group) {
            group.delete()
            flash.message = "UserGroup ${params.id} deleted"
            redirect(action:list)
        }
        else {
            flash.message = "UserGroup not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def group = UserGroup.get( params.id )

        if(!group) {
            flash.message = "UserGroup not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ group : group ]
        }
    }

    def update = {
        def group = UserGroup.get( params.id )
        if(group) {
            group.properties = params
            if(!group.hasErrors() && group.save()) {
                flash.message = "UserGroup ${params.id} updated"
                redirect(action:show,id:group.id)
            }
            else {
                render(view:'edit',model:[group:group])
            }
        }
        else {
            flash.message = "UserGroup not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        [group: new UserGroup(params)]
    }

    def save = {
        def group = new UserGroup()
        group.properties = params
        def next_id = 0

        if(params.id==null || params.id=="") {
            def c = UserGroup.createCriteria()
            next_id = c.get {
                projections {
                    max('id')
                }
            }
            next_id++
        }
        else
            next_id = new Long(params.id)

        if(params.name==null || params.name=="") {
            flash.message = 'Please enter a name'
            return render (view:'create', model:[group: new UserGroup(params)])
        }
        if(params.name==null || params.name=="") {
            flash.message = 'Please enter a name'
            return render (view:'create', model:[group: new UserGroup(params)])
        }

        group.id = next_id
        group.name = params.name;
        group.description = params.description;

        try {
            if (group.save()) {
                def msg = "Group: ${group.name} created.";
                new AccessLog(username: springSecurityService.getPrincipal().username, event:"Group created",
                        eventmessage: msg,
                        accesstime:new Date()).save()
                redirect action: show, id: group.id
            }
            else {
                render view: 'create', model: [group: group]
            }
        } catch(BatchUpdateException bue)   {
            flash.message = 'Cannot create group'
            log.error(bue.getLocalizedMessage(), bue)
            render view: 'create', model: [group: group]
        } catch(SQLException sqle)    {
            flash.message = 'Cannot create group'
            log.error(sqle.getNextException().getMessage())
            render view: 'create', model: [group: group]
        }
    }


    def ajaxGetUserSearchBoxData=
    {
    	String searchText = request.getParameter("query");
		def userdata=[];
		def users=AuthUser.executeQuery("from AuthUser p where upper(p.name) like upper ('%"+searchText+"%') order by p.name");
		    users.each{user ->
                userdata.add([name:user.name, username:user.username,  type:user.type, description:user.description, uid:user.id ])
			}
		def result = [rows:userdata]
        render(text:params.callback + "(" + (result as JSON) + ")", contentType:"application/javascript")
    }



    def searchUsersNotInGroup =
    {
    	def group = UserGroup.get( params.id )
    	def groupid=Long.parseLong(params.id);
    	def searchtext=params.searchtext;
    	def users=searchForUsersNotInGroup(groupid, searchtext);
    	render(template:'addremove',model:[group:group, usersToAdd:users])
   }
    def searchGroupsWithoutUser =
    {
    	def userInstance = AuthUser.get( params.id )
    	def searchtext=params.searchtext;
    	def groupswithuser=getGroupsWithUser(userInstance.id);
    	def groupswithoutuser=getGroupsWithoutUser(userInstance.id, searchtext);
    	render(template:'addremoveg',model:[userInstance: userInstance, groupswithuser: groupswithuser, groupswithoutuser: groupswithoutuser])
   }


def addUserToGroups =
{UserGroupCommand fl ->
		def userInstance = AuthUser.get( params.id )
		def groupsToAdd = UserGroup.findAll("from UserGroup r where r.id in (:p)", [p:fl.groupstoadd.collect{it.toLong()}]);
        if(userInstance) {
            groupsToAdd.each{ g -> g.addToMembers(userInstance);
            					   g.save(flush:true);} //add to each group and save the group

        }

         def searchtext=params.searchtext;


      // userInstance().save();

       def groupswithuser=getGroupsWithUser(userInstance.id);
    	def groupswithoutuser=getGroupsWithoutUser(userInstance.id, searchtext);
    	// println(groupswithuser);
    	render(template:'addremoveg',model:[userInstance: userInstance, groupswithuser: groupswithuser, groupswithoutuser: groupswithoutuser])
}


def removeUserFromGroups =
{UserGroupCommand fl ->
		def userInstance = AuthUser.get( params.id )
		def groupsToRemove= UserGroup.findAll("from UserGroup r where r.id in (:p)", [p:fl.groupstoremove.collect{it.toLong()}]);
        if(userInstance) {
            groupsToRemove.each{ g -> g.removeFromMembers(userInstance)
            						g.save(flush:true)} //remove from each group and save the group
            };

         def searchtext=params.searchtext;
    	def groupswithuser=getGroupsWithUser(userInstance.id);
    	def groupswithoutuser=getGroupsWithoutUser(userInstance.id, searchtext);
    	render(template:'addremoveg',model:[userInstance: userInstance, groupswithuser: groupswithuser, groupswithoutuser: groupswithoutuser])
}

   def addUsersToUserGroup =
    {UserGroupCommand fl ->

    		println("INCOMOING to add Group:"+params.userstoadd);
    		def group = UserGroup.get( params.id )
    		fl.userstoadd.collect{println("collecting:"+it.toLong())};
    		def usersToAdd = AuthUser.findAll("from AuthUser r where r.id in (:p)", [p:fl.userstoadd.collect{it.toLong()}]);
            if(group) {
                if(params.version) {
                    def version = params.version.toLong()
                    if(group.version > version) {

                        group.errors.rejectValue("version", "userGroup.optimistic.locking.failure", "Another user has updated this UserGroup while you were editing.")
                        render(template:'addremove',model:[group:group])
                    }
                }
                usersToAdd.each{ r -> group.members.add(r);
                	println("Adding user:"+r.id);
                };

                if(!group.hasErrors() && group.save(flush:true)) {
                    flash.message = "UserGroup ${params.id} updated"
                    	render(template:'addremove',model:[group:group, usersToAdd: searchForUsersNotInGroup(params.id.toLong(), fl.searchtext) ])
                }
                else {
                	render(template:'addremove',model:[group:group, usersToAdd: searchForUsersNotInGroup(params.id.toLong(), fl.searchtext) ])
                }
            }
            else {
                flash.message = "UserGroup not found with id ${params.id}"
                	render(template:'addremove',model:[group:group, usersToAdd: searchForUsersNotInGroup(params.id.toLong(), fl.searchtext) ])
            }
    }


    def removeUsersFromUserGroup =
    {
    		UserGroupCommand fl ->
    		def group = UserGroup.get( params.id )
    		if(!fl.hasErrors()){println("no errors")}
    		fl.errors.allErrors.each {
		        println it
		    }
    		fl.userstoremove.collect{println("collecting:"+it.toLong())};
    		def usersToRemove = AuthUser.findAll("from AuthUser r where r.id in (:p)", [p:fl.userstoremove.collect{it.toLong()}]);
            if(group) {
                if(params.version) {
                    def version = params.version.toLong()
                    if(group.version > version) {

                        group.errors.rejectValue("version", "userGroup.optimistic.locking.failure", "Another user has updated this userGroup while you were editing.")
                        render(template:'addremove',model:[group:group])
                    }
                }
                usersToRemove.each{ r -> group.members.remove(r);
                	println("Removing user:"+r.id);
                };
                if(!group.hasErrors() && group.save(flush:true)) {
                    flash.message = "UserGroup ${params.id} updated"
                    	render(template:'addremove',model:[group:group, usersToAdd: searchForUsersNotInGroup(params.id.toLong(), fl.searchtext) ])
                }
                else {
                	render(template:'addremove',model:[group:group, usersToAdd: searchForUsersNotInGroup(params.id.toLong(), fl.searchtext) ])
                }
            }
            else {
                flash.message = "UserGroup not found with id ${params.id}"
                	render(template:'addremove',model:[group:group, usersToAdd: searchForUsersNotInGroup(params.id.toLong(), fl.searchtext) ])
            }
    }


    def searchForUsersNotInGroup(groupid, insearchtext)
    {
      	def searchtext='%'+insearchtext.toString().toUpperCase()+'%';
      	return AuthUser.executeQuery('from AuthUser us WHERE us NOT IN (select u.id from UserGroup g, IN (g.members) u where g.id=?) AND upper(us.name) LIKE ? ORDER BY us.userRealName',[groupid, searchtext]).sort{it.name};
    }

	 def ajaxGetUsersAndGroupsSearchBoxData=
	    {
	    	String searchText = request.getParameter("query");
			def userdata=[];
			 def users=Principal.executeQuery("from Principal p where upper(p.name) like upper ('%"+searchText+"%') order by p.name");
				users.each{user ->

						if(user.type=='USER')
						{
						userdata.add([name:user.name, username:user.username,  type:user.type, description:user.description, uid:user.id ])
						}
						else
						{
						userdata.add([name:user.name, username:"No Login", type:user.type, description:user.description, uid:user.id ])
						}
					}
			def result = [rows:userdata]
            render(text:params.callback + "(" + (result as JSON) + ")", contentType:"application/javascript")
	    }

	private getGroupsWithUser(userid)
	{
        return UserGroup.executeQuery('Select g FROM UserGroup g, IN (g.members) m WHERE m.id=?', userid);
	}

	private getGroupsWithoutUser(userid, insearchtext)
	{
		def searchtext='%'+insearchtext.toString().toUpperCase()+'%'
		return UserGroup.executeQuery('from UserGroup g WHERE g.id<>-1 AND g.id NOT IN (SELECT g2.id from UserGroup g2, IN (g2.members) m WHERE m.id=?) AND upper(g.name) like ?', [userid, searchtext] );
	} 
}
