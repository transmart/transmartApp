package com.recomdata.transmart.data.association

import grails.test.mixin.TestFor

@TestFor(RModulesController)
class RModulesControllerTests {

    void testKnownDataTypes() {
        println(controller.knownDataTypes())
    }
}
