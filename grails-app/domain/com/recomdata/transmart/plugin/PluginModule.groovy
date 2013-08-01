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

import java.sql.Clob;

import org.hibernate.Hibernate;

class PluginModule {
	
	def pluginService
	
	long id
	String name
	String moduleName
	//Clob params
	String params
	Boolean active
	Boolean hasForm
	String formLink
	String formPage
	PluginModuleCategory category
	
	static belongsTo = [plugin: Plugin]
	
	static mapping = {
		table 'SEARCHAPP.PLUGIN_MODULE'
		id column:'MODULE_SEQ',
		   generator: 'sequence',
		   params: [sequence:'SEARCHAPP.PLUGIN_MODULE_SEQ']
		active type:'yes_no'
		hasForm type:'yes_no'
		plugin column:'PLUGIN_SEQ'
		params lazy: true
	}
	
	static constraints = {
		name(nullable:false)
		moduleName(nullable:false, unique:true)
		active(nullable:false)
		hasForm(nullable:false)
		formLink(nullable:true)
		formPage(nullable:true)
	}
	
	def private setParamsStr(moduleParams) {
		if (moduleParams?.trim()) {
			def jsonObject = JSON.parse(moduleParams)

			//params = Hibernate.createClob(jsonObject?.toString())
			params = jsonObject?.toString()
		}
	}
	
	def private getParamsStr() {
		/*def InputStream textStream = params?.getAsciiStream()
		def paramsAsStr = ''
		if (null != textStream) paramsAsStr = pluginService.convertStreamToString(textStream).replace('\n',' ')
		return paramsAsStr*/
		return params
	}
}
