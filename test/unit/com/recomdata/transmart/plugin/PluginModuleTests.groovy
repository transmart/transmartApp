package com.recomdata.transmart.plugin

import grails.test.GrailsUnitTestCase

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
        pluginModule.version = '0.1'
        pluginModule.active = true
        pluginModule.hasForm = false
        pluginModule.formLink = null
        pluginModule.formPage = null

        pluginModule.save(flush: true)

        println 'Finished Creating Plugin Module'

        pluginModule = PluginModule.findByModuleName('TestPluginModule')
        assertNotNull(pluginModule)

        println 'Finished Reading Plugin Module'

        try {
            pluginModule.name = 'Test Plugin Module updated'
            pluginModule.save()
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            println "Plugin Module could not be updated"
        }

        println 'Finished Updating Plugin Module'

        try {
            pluginModule.delete()
            println "Plugin Module deleted"
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            println "Plugin Module could not be deleted"
        }

        println 'Finished Deleting Plugin Module'
    }
}
