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
import static org.transmart.dataexport.FileContentTestUtils.parseSepValTable

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
    void testDataWithConceptPathSpecified() {
        def files = highDimExportService.exportHighDimData(
                jobName: 'test',
                resultInstanceId: queryResult.id,
                conceptKeys: [i2b2Node.key.toString()],
                dataType: 'mrna',
                format: 'TSV',
                studyDir: tmpDir,
                exportOptions: [meta: false, samples: false, platform: false])

        assertThat files, contains(
                hasProperty('absolutePath', endsWith('/bar/data_mrna.tsv')),
        )

        def dataFile = files[0]

        def dataTable = parseSepValTable(dataFile)
        assertThat dataTable, contains(
                contains('Assay ID', 'TRIALNAME', 'VALUE', 'LOG2E', 'ZSCORE', 'PROBE', 'GENE ID', 'GENE SYMBOL'),
                allOf(hasItem('-403'), hasItem('1553513_at')),
                allOf(hasItem('-402'), hasItem('1553513_at')),
                allOf(hasItem('-403'), hasItem('1553510_s_at')),
                allOf(hasItem('-402'), hasItem('1553510_s_at')),
                allOf(hasItem('-403'), hasItem('1553506_at')),
                allOf(hasItem('-402'), hasItem('1553506_at')),
        )
    }

    @Test
    void testDataWithoutConceptPathSpecified() {
        def files = highDimExportService.exportHighDimData(
                jobName: 'test',
                resultInstanceId: queryResult.id,
                dataType: 'mrna',
                format: 'TSV',
                studyDir: tmpDir,
                exportOptions: [meta: false, samples: false, platform: false])

        assertThat files, contains(
                hasProperty('absolutePath', endsWith('/bar/data_mrna.tsv')),
        )

        def dataFile = files[0]

        def dataTable = parseSepValTable(dataFile)
        assertThat dataTable, contains(
                contains('Assay ID', 'TRIALNAME', 'VALUE', 'LOG2E', 'ZSCORE', 'PROBE', 'GENE ID', 'GENE SYMBOL'),
                allOf(hasItem('-403'), hasItem('1553513_at')),
                allOf(hasItem('-402'), hasItem('1553513_at')),
                allOf(hasItem('-403'), hasItem('1553510_s_at')),
                allOf(hasItem('-402'), hasItem('1553510_s_at')),
                allOf(hasItem('-403'), hasItem('1553506_at')),
                allOf(hasItem('-402'), hasItem('1553506_at')),
        )
    }

    @Test
    void testExportMetaTags() {
        def files = highDimExportService.exportHighDimData(
                jobName: 'test',
                resultInstanceId: queryResult.id,
                conceptKeys: [i2b2Node.key.toString()],
                dataType: 'mrna',
                format: 'TSV',
                studyDir: tmpDir,
                exportOptions: [meta: true, samples: false, platform: false])

        assertThat files, containsInAnyOrder(
                hasProperty('absolutePath', endsWith('/bar/data_mrna.tsv')),
                hasProperty('absolutePath', endsWith('/bar/meta.tsv')),
        )

        files.each { File file ->
            assertTrue(file.exists())
            assertThat file.length(), greaterThan(0l)
        }

        def metaFile = files.find { it.absolutePath.endsWith '/meta.tsv' }

        def metaTable = parseSepValTable(metaFile)
        assertThat metaTable, contains(
                contains('Attribute', 'Description'),
                allOf(hasItem('2 name 2'), hasItem('2 description 2')),
                allOf(hasItem('2 name 1'), hasItem('2 description 1')),
        )
    }

    @Test
    void testExportAssays() {
        def files = highDimExportService.exportHighDimData(
                jobName: 'test',
                resultInstanceId: queryResult.id,
                conceptKeys: [i2b2Node.key.toString()],
                dataType: 'mrna',
                format: 'TSV',
                studyDir: tmpDir,
                exportOptions: [meta: false, samples: true, platform: false])

        assertThat files, containsInAnyOrder(
                hasProperty('absolutePath', endsWith('/bar/data_mrna.tsv')),
                hasProperty('absolutePath', endsWith('/bar/samples.tsv')),
        )

        files.each { File file ->
            assertTrue(file.exists())
            assertThat file.length(), greaterThan(0l)
        }

        def samplesFile = files.find { it.absolutePath.endsWith '/samples.tsv' }

        def samplesTable = parseSepValTable(samplesFile)
        assertThat samplesTable, containsInAnyOrder(
                contains('Assay ID', 'Subject ID', 'Sample Type', 'Time Point',
                        'Tissue Type', 'Platform ID', 'Sample Code'),
                allOf(hasItem('-403'), hasItem('SUBJ_ID_3'), hasItem('SAMPLE_FOR_-103')),
                allOf(hasItem('-402'), hasItem('SUBJ_ID_2'), hasItem('SAMPLE_FOR_-102')),
        )

    }

    @Test
    void testExportPlatform() {
        def files = highDimExportService.exportHighDimData(
                jobName: 'test',
                resultInstanceId: queryResult.id,
                conceptKeys: [i2b2Node.key.toString()],
                dataType: 'mrna',
                format: 'TSV',
                studyDir: tmpDir,
                exportOptions: [meta: false, samples: false, platform: true])

        assertThat files, containsInAnyOrder(
                hasProperty('absolutePath', endsWith('/bar/data_mrna.tsv')),
                hasProperty('absolutePath', endsWith('/bar/platform.tsv')),
        )

        files.each { File file ->
            assertTrue(file.exists())
            assertThat file.length(), greaterThan(0l)
        }

        def platformFile = files.find { it.absolutePath.endsWith '/platform.tsv' }

        def platformTable = parseSepValTable(platformFile)
        assertThat platformTable, contains(
                contains('Platform ID', 'Title', 'Genome Release ID', 'Organism', 'Marker Type', 'Annotation Date'),
                allOf(hasItem('BOGUSGPL570'), hasItem('Gene Expression')),
        )
    }
}
