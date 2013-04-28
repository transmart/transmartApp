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

import grails.test.GrailsUnitTestCase

class PluginControllerTests extends GrailsUnitTestCase {
	def pluginService
	def PluginController pluginController
	
    protected void setUp() {
        super.setUp()
		pluginService = new PluginService()
		
		mockController(PluginController)
		pluginController = new PluginController()
		
		mockDomain(Plugin, [new Plugin(name: 'Test Plugin',
			pluginName: 'TestPlugin',
			active: true,
			hasModules: false)])
    }

    protected void tearDown() {
        super.tearDown()
    }

	void testIndex() {
		pluginController.index()
		assertEquals pluginController.list, pluginController.redirectArgs["action"]
	}
	
	void testList() {
		def model = pluginController.list()
		assertEquals 1, model.pluginInstanceTotal
	}
	
    void testShow() {
		//MockDomain by default sets the id of the domain objects from 0..N (unless you specify the id explicitly) 
		//where N is the number of domain objects that we plan to mock
		pluginController.params.id = 0
		def returnMap = pluginController.show()
		//If it fails to load the plugin there will be a message 
		println pluginController.flash?.message
		assertEquals 'Test Plugin', returnMap?.pluginInstance?.name
    }	
}
