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

import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException;
import org.json.JSONObject;

import grails.converters.JSON;

class PluginModuleController {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST', validateModuleParams:'POST']

    def list = {
        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
		if (null == params.sort) params.sort = 'name'
		if (null == params.order) params.order = 'asc'
        [ pluginModuleInstanceList: PluginModule.list( params ), pluginModuleInstanceTotal: PluginModule.count() ]
    }

    def show = {
        def pluginModuleInstance = PluginModule.get( params.id )

        if(!pluginModuleInstance) {
            flash.message = "PluginModule not found with id ${params.id}"
            redirect(action:list)
        }
        else { return [ pluginModuleInstance : pluginModuleInstance, 'paramsStr' : pluginModuleInstance.getParamsStr() ] }
    }

    def delete = {
        def pluginModuleInstance = PluginModule.get( params.id )
        if(pluginModuleInstance) {
            try {
                pluginModuleInstance.delete()
                flash.message = "PluginModule ${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "PluginModule ${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "PluginModule not found with id ${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def pluginModuleInstance = PluginModule.get( params.id )

        if(!pluginModuleInstance) {
            flash.message = "PluginModule not found with id ${params.id}"
            redirect(action:list)
        }
        else {
            return [ pluginModuleInstance : pluginModuleInstance, 'paramsStr' : pluginModuleInstance.getParamsStr() ]
        }
    }
	
	def validateModuleParams = {
		def jsonResponse = new JSONObject()
		
		try {
			def jsonObject = JSON.parse(params.paramsStr)
			jsonResponse.put('status', true)
			jsonResponse.put('message', 'Plugin Module has valid parameters')
		} catch (ConverterException e) {
			jsonResponse.put('status', false)
			jsonResponse.put('message', 'Parameters should be a well formed JSON string : \n'+e.message)
		}
		
		return jsonResponse
	}

    def update = {
        def pluginModuleInstance = PluginModule.get( params.id )
        if(pluginModuleInstance) {
            if(params.version) {
                def version = params.version.toLong()
                if(pluginModuleInstance.version > version) {
                    
                    pluginModuleInstance.errors.rejectValue("version", "pluginModule.optimistic.locking.failure", "Another user has updated this PluginModule while you were editing.")
                    render(view:'edit',model:[pluginModuleInstance:pluginModuleInstance])
                    return
                }
            }
            pluginModuleInstance.properties = params
			try {
				pluginModuleInstance.setParamsStr(params.paramsStr)
			} catch (ConverterException e) {
				pluginModuleInstance.errors.rejectValue("params", 'Parameters should be a well formed JSON string : '+e.message+' : '+e.cause?.message?.substring(0, 50)+'...')
			}
            if(!pluginModuleInstance.hasErrors() && pluginModuleInstance.save()) {
                flash.message = "PluginModule ${params.id} updated"
                redirect(action:show,id:pluginModuleInstance.id)
            }
            else {
                render(view:'edit',model:[pluginModuleInstance:pluginModuleInstance, paramsStr:params.paramsStr])
            }
        }
        else {
            flash.message = "PluginModule not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def pluginModuleInstance = new PluginModule()
        pluginModuleInstance.properties = params
        return ['pluginModuleInstance':pluginModuleInstance, 'paramsStr' : pluginModuleInstance.getParamsStr()]
    }

    def save = {
        def pluginModuleInstance = new PluginModule(params)
		pluginModuleInstance.setParamsStr(params.paramsStr)
        if(!pluginModuleInstance.hasErrors() && pluginModuleInstance.save()) {
            flash.message = "PluginModule ${pluginModuleInstance.id} created"
            redirect(action:show,id:pluginModuleInstance.id)
        }
        else {
            render(view:'create',model:[pluginModuleInstance:pluginModuleInstance])
        }
    }
}
