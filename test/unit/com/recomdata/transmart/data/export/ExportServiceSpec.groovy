package com.recomdata.transmart.data.export

import grails.converters.JSON
import grails.test.mixin.*
import grails.test.mixin.support.*

import org.junit.*
import org.transmart.authorization.CurrentUserBeanProxyFactory
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
@TestFor(ExportService)
class ExportServiceSpec extends Specification {

    @Before
    void init() {
        defineBeans {
            // the dependency only has to be satisfied; it's not used in the
            // tested method here
            "${CurrentUserBeanProxyFactory.BEAN_BAME}"(Object)
        }
    }

    void "test getHighDimDataTypesAndFormats basic functionality"() {
        given: "a set of selected checkboxes"
        def selectedCheckBoxList = [
                [
                        subset: 'subset1',
                        dataTypeId: 'mrna',
                        fileType: '.TXT',
                        gplId: 'GPL570',
                ],
                [
                        subset: 'subset1',
                        dataTypeId: 'mrna',
                        fileType: '.TXT',
                        gplId: 'GPL571',
                ],
                [
                        subset: 'subset1',
                        dataTypeId: 'mrna',
                        fileType: '.XLS',
                        gplId: 'GPL570',
                ],
                [
                        subset: 'subset1',
                        dataTypeId: 'mirna',
                        fileType: '.TXT',
                        gplId: 'GPL570',
                ],
                [
                        subset: 'subset2',
                        dataTypeId: 'mrna',
                        fileType: '.TXT',
                        gplId: 'GPL570',
                ],
        ].collect { (it as JSON).toString() }

        when: "the strings are parsed"
        def formats = service.getHighDimDataTypesAndFormats(selectedCheckBoxList)

        then: "the output is a properly formatted map"
        // Expected
        //      subset1={mrna={TXT=[GPL570, GPL571], XLS=[GPL570]}, mirna={TXT=[GPL570]}}
        //      subset2={mrna={TXT=[GPL570]}}
        formats.keySet().size() == 2
        formats.containsKey("subset1")
        formats.containsKey("subset2")

        formats.subset1.keySet().size() == 2
        formats.subset1.containsKey("mrna")
        formats.subset1.containsKey("mirna")

        formats.subset1.mrna.keySet().size() == 2
        formats.subset1.mrna.containsKey("TXT")
        formats.subset1.mrna.containsKey("XLS")

        formats.subset1.mrna.TXT.size() == 2
        formats.subset1.mrna.TXT.contains("GPL570")
        formats.subset1.mrna.TXT.contains("GPL571")

        formats.subset1.mrna.XLS.size() == 1
        formats.subset1.mrna.XLS.contains("GPL570")

        formats.subset1.mirna.keySet().size() == 1
        formats.subset1.mirna.containsKey("TXT")

        formats.subset1.mirna.TXT.size() == 1
        formats.subset1.mirna.TXT.contains("GPL570")

        formats.subset2.keySet().size() == 1
        formats.subset2.containsKey("mrna")

        formats.subset2.mrna.keySet().size() == 1
        formats.subset2.mrna.containsKey("TXT")

        formats.subset2.mrna.TXT.size() == 1
        formats.subset2.mrna.TXT.contains("GPL570")

    }
}
