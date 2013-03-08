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

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.domain.DomainClassUnitTestMixin
import org.junit.Before

@TestFor(PluginController)
@TestMixin(DomainClassUnitTestMixin)
class PluginControllerTests {

    @Before
    void setUp() {
		mockDomain(Plugin);
        def p = new Plugin(name: 'Test Plugin', pluginName: 'TestPlugin', active: true, hasForm: false, defaultLink: '', hasModules: false)
                .save(flush: true)
        assert p != null
    }

	void testIndex() {
		controller.index()
        assert response.status == 302
        assert response.redirectedUrl == "/plugin/list"
	}

	void testList() {
		def model = controller.list()
        assert model.pluginInstanceTotal == 1
        assert model.pluginInstanceList[0].name == 'Test Plugin'
	}
	
    void testShow() {
		//MockDomain by default sets the id of the domain objects from 0..N (unless you specify the id explicitly)
		//where N is the number of domain objects that we plan to mock
        controller.params.id = 0
		def returnMap = controller.show()
		//If it fails to load the plugin there will be a message
		assert controller.flash?.message == null
		assertEquals 'Test Plugin', returnMap?.pluginInstance?.name
    }	
}
