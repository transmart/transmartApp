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

import grails.test.*

class PluginModuleTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testCRUD() {
		def pluginModule = new PluginModule()
		mockDomain(PluginModule, [pluginModule])
		pluginModule.name = 'Test Plugin Module'
		pluginModule.category = PluginModuleCategory.DEFAULT
		pluginModule.moduleName = 'TestPluginModule'
		pluginModule.version = 0.1
		pluginModule.active = true
		pluginModule.hasForm = false
		pluginModule.formLink = null
		pluginModule.formPage = null
		
		pluginModule.save(flush:true)
		
		println 'Finished Creating Plugin Module'
		
		pluginModule = PluginModule.findByModuleName('TestPluginModule')
		assertNotNull(pluginModule)
	
		println 'Finished Reading Plugin Module'
		
		try {
			pluginModule.name = 'Test Plugin Module updated'
			pluginModule.save()
		} catch(org.springframework.dao.DataIntegrityViolationException e) {
			println "Plugin Module could not be updated"
		}
		
		println 'Finished Updating Plugin Module'
		
		try {
			pluginModule.delete()
			println "Plugin Module deleted"
		} catch(org.springframework.dao.DataIntegrityViolationException e) {
			println "Plugin Module could not be deleted"
		}
		
		println 'Finished Deleting Plugin Module'
    }
}
