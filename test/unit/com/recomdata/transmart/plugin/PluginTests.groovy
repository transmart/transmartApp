package com.recomdata.transmart.plugin

import grails.test.GrailsUnitTestCase

class PluginTests extends GrailsUnitTestCase {
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testCRUD() {
        def pluginInstance = new Plugin()
        mockDomain(Plugin, [pluginInstance])
        pluginInstance.name = 'Test Plugin'
        pluginInstance.pluginName = 'TestPlugin'
        pluginInstance.active = true
        pluginInstance.defaultLink = null
        pluginInstance.hasForm = false
        pluginInstance.formLink = null
        pluginInstance.formPage = null
        pluginInstance.hasModules = false

        pluginInstance.save(flush: true)

        println 'Finished Create'

        pluginInstance = Plugin.findByPluginName('TestPlugin')
        assertNotNull(pluginInstance)

        println 'Finished Reading'

        try {
            pluginInstance.name = 'Test Plugin updated'
            pluginInstance.save()
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            println "Plugin could not be updated"
        }

        println 'Finished Updating'

        try {
            pluginInstance.delete()
            println "Plugin deleted"
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            println "Plugin could not be deleted"
        }

        println 'Finished Deleting'
    }
}
