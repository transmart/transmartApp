package com.recomdata.transmart.plugin

import grails.converters.JSON
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.json.JSONObject

class PluginModuleController {

    def index = { redirect(action: list, params: params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', validateModuleParams: 'POST']

    def list = {
        params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        if (null == params.sort) params.sort = 'name'
        if (null == params.order) params.order = 'asc'
        [pluginModuleInstanceList: PluginModule.list(params), pluginModuleInstanceTotal: PluginModule.count()]
    }

    def show = {
        def pluginModuleInstance = PluginModule.get(params.id)

        if (!pluginModuleInstance) {
            flash.message = "PluginModule not found with id ${params.id}"
            redirect(action: list)
        } else {
            return [pluginModuleInstance: pluginModuleInstance, 'paramsStr': pluginModuleInstance.getParamsStr()]
        }
    }

    def delete = {
        def pluginModuleInstance = PluginModule.get(params.id)
        if (pluginModuleInstance) {
            try {
                pluginModuleInstance.delete()
                flash.message = "PluginModule ${params.id} deleted"
                redirect(action: list)
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "PluginModule ${params.id} could not be deleted"
                redirect(action: show, id: params.id)
            }
        } else {
            flash.message = "PluginModule not found with id ${params.id}"
            redirect(action: list)
        }
    }

    def edit = {
        def pluginModuleInstance = PluginModule.get(params.id)

        if (!pluginModuleInstance) {
            flash.message = "PluginModule not found with id ${params.id}"
            redirect(action: list)
        } else {
            return [pluginModuleInstance: pluginModuleInstance, 'paramsStr': pluginModuleInstance.getParamsStr()]
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
            jsonResponse.put('message', 'Parameters should be a well formed JSON string : \n' + e.message)
        }

        return jsonResponse
    }

    def update = {
        def pluginModuleInstance = PluginModule.get(params.id)
        if (pluginModuleInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (pluginModuleInstance.version > version) {

                    pluginModuleInstance.errors.rejectValue("version", "pluginModule.optimistic.locking.failure", "Another user has updated this PluginModule while you were editing.")
                    render(view: 'edit', model: [pluginModuleInstance: pluginModuleInstance])
                    return
                }
            }
            pluginModuleInstance.properties = params
            try {
                pluginModuleInstance.setParamsStr(params.paramsStr)
            } catch (ConverterException e) {
                pluginModuleInstance.errors.rejectValue("params", 'Parameters should be a well formed JSON string : ' + e.message + ' : ' + e.cause?.message?.substring(0, 50) + '...')
            }
            if (!pluginModuleInstance.hasErrors() && pluginModuleInstance.save()) {
                flash.message = "PluginModule ${params.id} updated"
                redirect(action: show, id: pluginModuleInstance.id)
            } else {
                render(view: 'edit', model: [pluginModuleInstance: pluginModuleInstance, paramsStr: params.paramsStr])
            }
        } else {
            flash.message = "PluginModule not found with id ${params.id}"
            redirect(action: edit, id: params.id)
        }
    }

    def create = {
        def pluginModuleInstance = new PluginModule()
        pluginModuleInstance.properties = params
        return ['pluginModuleInstance': pluginModuleInstance, 'paramsStr': pluginModuleInstance.getParamsStr()]
    }

    def save = {
        def pluginModuleInstance = new PluginModule(params)
        pluginModuleInstance.setParamsStr(params.paramsStr)
        if (!pluginModuleInstance.hasErrors() && pluginModuleInstance.save()) {
            flash.message = "PluginModule ${pluginModuleInstance.id} created"
            redirect(action: show, id: pluginModuleInstance.id)
        } else {
            render(view: 'create', model: [pluginModuleInstance: pluginModuleInstance])
        }
    }
}
