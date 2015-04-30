package org.transmartproject.export

import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.Platform
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.chromoregion.RegionRowImpl

import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.hasSize
import static org.junit.Assert.assertThat

/**
 *
 */
@WithGMock
class RnaSeqBedExporterTests {

    @Delegate
    MockTabularResultHelper mockTabularResultHelper

    RnaSeqBedExporter exporter
    TabularResult tabularResult
    Projection projection

    @Before
    void before() {
        mockTabularResultHelper = new MockTabularResultHelper()
        exporter = new RnaSeqBedExporter()
        projection = mock(Projection)
    }

    @Test
    void "test whether RNASeq data is exported properly"() {

        tabularResult = createMockRnaSeqTabularResult()

        List<ByteArrayOutputStream> outputStreams = []

        play {
            exporter.export(tabularResult, projection, { name, ext ->
                outputStreams << new ByteArrayOutputStream()
                outputStreams[-1]
            })

            assertThat outputStreams, hasSize(2)

            String assay1Output = outputStreams[0].toString("UTF-8")
            String assay2Output = outputStreams[1].toString("UTF-8")

            List assay1Lines = assay1Output.readLines()
            //NOTE: bioMarker is used as label if it's present
            assertThat assay1Lines, contains(
                    'track name="sample_code_1" useScore="1" genome_build="hg19"',
                    'chrX\t1234\t1300\ttest-region1\t100',
                    'CHRY\t1301\t1400\ttest-bio-marker\t300',
                    'chr9\t1000\t2000\ttest-region3\t500',
            )

            List assay2Lines = assay2Output.readLines()
            assertThat assay2Lines, contains(
                    'track name="sample_code_2" useScore="1" genome_build="hg19"',
                    'chrX\t1234\t1300\ttest-region1\t200',
                    'CHRY\t1301\t1400\ttest-bio-marker\t400',
                    'chr9\t1000\t2000\ttest-region3\t600',
            )
        }
    }

    protected createMockRnaSeqTabularResult() {
        // Setup tabularResult and projection to test with
        List<AssayColumn> sampleAssays = createSampleAssays(2)
        Map<String, List<Object>> dataRows = [
                "row1": [
                        ["readcount": 100],
                        ["readcount": 200]
                ],
                "row2": [
                        ["readcount": 300],
                        ["readcount": 400]
                ],
                "row3": [
                        ["readcount": 500],
                        ["readcount": 600]
                ],
        ]

        Map<String, Map> rnaSeqRowProperties = [
                "row1": [
                        chromosome: 'chrX',
                        start     : 1234,
                        end       : 1300,
                        name      : 'test-region1',
                        platform  : [genomeReleaseId: 'hg19']
                ],
                "row2": [
                        chromosome: 'CHRY',
                        start     : 1301,
                        end       : 1400,
                        name      : 'test-region2',
                        bioMarker : 'test-bio-marker',
                        platform  : [genomeReleaseId: 'hg19']
                ],
                "row3": [
                        chromosome: '9',
                        start     : 1000,
                        end       : 2000,
                        name      : 'test-region3',
                        platform  : [genomeReleaseId: 'hg19']
                ],
        ]


        def iterator = dataRows.collect { String rowName, List data ->
            createRnaSeqRowForAssays(sampleAssays, data, rnaSeqRowProperties[rowName], rowName)
        }.iterator()

        TabularResult highDimResult = mock TabularResult
        highDimResult.indicesList.returns(sampleAssays).stub()
        highDimResult.getRows().returns(iterator).stub()
        highDimResult.iterator().returns(iterator).stub()

        highDimResult
    }

    DataRow createRnaSeqRowForAssays(List<AssayColumn> assays,
                                   List data,
                                   Map<String, Object> properties,
                                   String label) {
        createMockRnaSeqRow(
                dot(assays, data, { a, b -> [a, b] })
                        .collectEntries(Closure.IDENTITY),
                properties,
                label)
    }

    private DataRow<AssayColumn, Object> createMockRnaSeqRow(Map<AssayColumn, Object> values,
                                                           Map<String, Object> properties, String label) {
        DataRow row = mock(RegionRowImpl)
        row.label.returns(label).stub()

        row.chromosome.returns(properties.chromosome).stub()
        row.start.returns(properties.start).stub()
        row.end.returns(properties.end).stub()
        row.name.returns(properties.name).stub()
        row.bioMarker.returns(properties.bioMarker).stub()
        if (properties.platform) {
            Platform platform = mock(Platform)
            platform.genomeReleaseId.returns(properties.platform.genomeReleaseId).stub()
            row.platform.returns(platform).stub()
        }

        values.eachWithIndex { entry, i ->
            row.getAt(i).returns(entry.value).stub()
            row.getAt(entry.key).returns(entry.value).stub()
        }
        row.iterator().returns(values.values().iterator()).stub()

        row
    }
}
