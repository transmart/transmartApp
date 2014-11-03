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
        def p = new Plugin(
                name: 'Test Plugin',
                pluginName: 'TestPlugin',
                active: true,
                hasForm: false,
                defaultLink: 'bogus_link',
                hasModules: false)
        assert p.save() != null
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
