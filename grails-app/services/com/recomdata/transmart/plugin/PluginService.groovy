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

import grails.converters.JSON;

import org.hibernate.Hibernate
import org.json.JSONArray;
import org.json.JSONObject;

import com.recomdata.plugins.PluginDescriptor

class PluginService {

	def grailsApplication
	
    boolean transactional = false

   /**
    * Deprecated, will be removed once the registerPlugin and registerPluginModule methods are in-place
    * 
    * @param convertor
    * @param processor
    * @param view
    * @param dataTypes
    * @param name
    * @param id
    * @param renderer
    * @param variableMapping
    * @param pivotData
    * @param dataFileInputMapping
    * @return
    */
	def PluginDescriptor registerPlugin(convertor, processor, view, dataTypes, name, id,renderer,variableMapping,pivotData,dataFileInputMapping){
		PluginDescriptor plugin = new PluginDescriptor();
		plugin.setConverter(convertor);
		plugin.setProcessor(processor);
		plugin.setView(view);
		plugin.setDataTypes(dataTypes);
		plugin.setName(name);
		plugin.setId(id);
		plugin.setRenderer(renderer)
		plugin.setVariableMapping(variableMapping);
		plugin.setPivotData(pivotData);		
		plugin.setDataFileInputMapping(dataFileInputMapping);
		return plugin
	}
   
	def private validatePluginModule(moduleMap) throws Exception {
		def isValid = false
		if (null != moduleMap && null != moduleMap.pluginName
				&& null != moduleMap.params && null != moduleMap.params.name
				&& null != moduleMap.params.id) {
			isValid = true
		}
		if (!isValid)
		  throw new Exception('Module Registration Failed!!! Module does not contain valid data. Plugin-name and module-params are required to register a Module.')
	}
	
	def boolean hasModule(pluginName, moduleName) {
		def pluginInstance = Plugin.findByName(pluginName)
		def boolean moduleExists = false
		pluginInstance?.modules?.each { module ->
			if (module.moduleName.equalsIgnoreCase(moduleName)) {
				moduleExists = true
			}
		}
		
		return moduleExists
	}
	
	def getPluginModules(pluginName) {
		def pluginInstance = Plugin.findByName(pluginName)
		
		return PluginModule.findAllByPluginAndActive(pluginInstance, true);

		//return pluginInstance?.modules
	}
	
	def getPluginModulesAsJSON(pluginName) {
		def pluginInstance = Plugin.findByName(pluginName)
		def result = new JSONObject()
		def modulesJSON = new JSONArray()
		
		if (pluginName) {
			def c = PluginModule.createCriteria()
			def modules = c {
				projections {
					property 'moduleName', 'moduleName'
					property 'name', 'name'
					property 'category', 'category'
				}
				eq('plugin', pluginInstance)
				eq('active', true)
				order('name', 'asc')
				order('category', 'asc')
			}
			
			if (modules?.size() > 0) {
				result.put('success', true)
				for (module in modules) {
					// Since each module has the heavy params object, 
					// we will not use "obj as JSON" as we don't want to pass the heavy params to the UI
					if (module.length == 3) {
						def moduleJSON = new JSONObject()
						moduleJSON.put('id', module[0])
						moduleJSON.put('text', module[1])
						moduleJSON.put('group', module[2])
						modulesJSON.put(moduleJSON)
					}
				}
			} else {
				result.put('success', false)
			}
			result.put('count', modules ? modules?.size() : 0)
			result.put('modules', modulesJSON)
		} else {
			result.put('success', false)
			result.put('message', 'Plugin name is missing!!! Modules cannot be looked up.')
		}
		
		return result
	}
	
	def findPluginModuleById(moduleId) {
		def pluginModuleInstance = PluginModule.get(moduleId)
		
		return pluginModuleInstance
	}
	
	def findPluginModuleByModuleName(moduleId) {
		def pluginModuleInstance = PluginModule.findByModuleName(moduleId)
		
		return pluginModuleInstance
	}
	
	def registerPluginModule(moduleMap) throws Exception {
		Plugin.withTransaction{tx ->
		   validatePluginModule(moduleMap)
		   def pluginInstance = Plugin.findByName(moduleMap.pluginName)
		   def pluginModuleInstance = null
		   def pluginModuleExists = false
		   
		   if (null == pluginInstance) throw new Exception('Module Registration Failed!!! Module Plugin-name ${moduleMap.pluginName} is not a valid Plugin. Please see the list of plugins installed.')
		   
		   pluginModuleInstance = PluginModule.findByModuleName(moduleMap.params?.id)
		   if (null == pluginModuleInstance) {
			   pluginModuleInstance = new PluginModule()
		   } else {
		   	   pluginModuleExists = true
		   }
		   
		   pluginModuleInstance.name = moduleMap.params?.name
		   pluginModuleInstance.moduleName = moduleMap.params?.id
		   pluginModuleInstance.formPage = moduleMap.params?.view
		   def jsonObject = moduleMap.params as JSON
		   pluginModuleInstance.params = Hibernate.createClob(jsonObject?.toString())
		   pluginModuleInstance.version = moduleMap.version
		   
		   pluginModuleInstance.active = true
		   pluginModuleInstance.hasForm = false
		   
		   //Add module to plugin
		   if (!pluginModuleExists) {
			   pluginInstance.addToModules(pluginModuleInstance)
		   } else {
		   	   pluginModuleInstance.merge()
		   }
		   //Save the plugin
		   pluginInstance.save(flush:true,failOnError:true)
		}
   }
	
	def String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		
		while ((line = reader.readLine()) != null) {
		sb.append(line + "\n");
		}
		is.close();
		return sb.toString();
	}
}
