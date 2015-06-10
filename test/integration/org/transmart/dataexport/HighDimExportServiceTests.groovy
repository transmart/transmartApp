package org.transmart.dataexport

import com.google.common.io.Files
import com.recomdata.asynchronous.JobResultsService
import grails.test.mixin.TestMixin
import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.querytool.Item
import org.transmartproject.core.querytool.Panel
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.db.dataquery.highdim.mrna.MrnaTestData
import org.transmartproject.db.ontology.I2b2
import org.transmartproject.db.ontology.StudyTestData
import org.transmartproject.db.test.RuleBasedIntegrationTestMixin
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertTrue

@TestMixin(RuleBasedIntegrationTestMixin)
@WithGMock
class HighDimExportServiceTests {

    MrnaTestData testData = new MrnaTestData()
    StudyTestData studyTestData = new StudyTestData()
    I2b2 i2b2Node

    def highDimExportService
    def queriesResourceService

    File tmpDir

    def queryResult

    @Before
    void setUp() {
        tmpDir = Files.createTempDir()
        studyTestData.saveAll()
        i2b2Node = studyTestData.i2b2List.find { it.fullName == '\\foo\\study1\\bar\\' }

        testData = new MrnaTestData(conceptCode: i2b2Node.code, patients: studyTestData.i2b2Data.patients)
        testData.saveAll()

        highDimExportService.jobResultsService = new JobResultsService(jobResults: [test: [Status: 'In Progress']])

        def definition = new QueryDefinition([
                new Panel(
                        items: [
                                new Item(
                                        conceptKey: i2b2Node.key.toString()
                                )
                        ]
                )
        ])

        queryResult = queriesResourceService.runQuery(definition, 'test')
    }

    @Test
    void testWithConceptPathSpecified() {
        def files = highDimExportService.exportHighDimData(
                jobName: 'test',
                resultInstanceId: queryResult.id,
                conceptKeys: [ i2b2Node.key.toString() ],
                dataType: 'mrna',
                format: 'TSV',
                studyDir: tmpDir,
                exportMetaData: true)

        assertThat files, containsInAnyOrder(
                hasProperty('absolutePath', endsWith('/bar/data_mrna.tsv')),
                hasProperty('absolutePath', endsWith('/bar/meta.tsv')),
        )

        files.each { File file ->
            assertTrue(file.exists())
            assertThat file.length(), greaterThan(0l)
        }
    }

    @Test
    void testWithoutConceptPathSpecified() {
        def files = highDimExportService.exportHighDimData(
                jobName: 'test',
                resultInstanceId: queryResult.id,
                dataType: 'mrna',
                format: 'TSV',
                studyDir: tmpDir,
                exportMetaData: true)

        assertThat files, containsInAnyOrder(
                hasProperty('absolutePath', endsWith('/bar/data_mrna.tsv')),
                hasProperty('absolutePath', endsWith('/bar/meta.tsv')),
        )

        files.each { File file ->
            assertTrue(file.exists())
            assertThat file.length(), greaterThan(0l)
        }
    }
}
