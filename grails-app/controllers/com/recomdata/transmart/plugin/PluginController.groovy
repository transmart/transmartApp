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
  



package com.recomdata.transmart.plugin

class PluginController {
    def pluginService
	
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        [ pluginInstanceList: Plugin.list( params ), pluginInstanceTotal: Plugin.count() ]
    }
	
	def modules = {
		params.pluginName = params.pluginName?.trim() ? params.pluginName?.trim() : 'R-Modules'
		def result = pluginService.getPluginModulesAsJSON(params.pluginName)
		
		response.setContentType("text/json")
		response.outputStream << result?.toString()
	}

    def show = {
        def pluginInstance = Plugin.get( params.id )

        if(!pluginInstance) {
            flash.message = "Plugin not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ pluginInstance : pluginInstance ] }
    }

    def delete = {
        def pluginInstance = Plugin.get( params.id )
        if(pluginInstance) {
            try {
                pluginInstance.delete()
                flash.message = "Plugin ${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "Plugin ${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "Plugin not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def pluginInstance = Plugin.get( params.id )

        if(!pluginInstance) {
            flash.message = "Plugin not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ pluginInstance : pluginInstance ]
        }
    }

    def update = {
        def pluginInstance = Plugin.get( params.id )
        if(pluginInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(pluginInstance.version > version) {
                    
                    pluginInstance.errors.rejectValue("version", "plugin.optimistic.locking.failure", "Another user has updated this Plugin while you were editing.")
                    render(view:'edit',model:[pluginInstance:pluginInstance])
                    return
                }
            }
            pluginInstance.properties = params
            if(!pluginInstance.hasErrors() && pluginInstance.save()) {
                flash.message = "Plugin ${params.id} updated"
                redirect(action:show,id:pluginInstance.id)
            }
            else {
                render(view:'edit',model:[pluginInstance:pluginInstance])
            }
        }
        else {
            flash.message = "Plugin not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def pluginInstance = new Plugin()
        pluginInstance.properties = params
        return ['pluginInstance':pluginInstance]
    }

    def save = {
        def pluginInstance = new Plugin(params)
        if(!pluginInstance.hasErrors() && pluginInstance.save()) {
            flash.message = "Plugin ${pluginInstance.id} created"
            redirect(action:show,id:pluginInstance.id)
        }
        else {
            render(view:'create',model:[pluginInstance:pluginInstance])
        }
    }
}
