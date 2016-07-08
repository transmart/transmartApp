package org.transmartproject.export

import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.Platform
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.protein.ProteinDataRow

import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.hasSize
import static org.junit.Assert.assertThat


import static org.transmartproject.export.ProteomicsBedExporter.LOW_VALUE_RGB
import static org.transmartproject.export.ProteomicsBedExporter.DEFAULT_RGB
import static org.transmartproject.export.ProteomicsBedExporter.HIGH_VALUE_RGB

import static org.transmartproject.export.ProteomicsBedExporter.LOW_ZSCORE_THRESHOLD
import static org.transmartproject.export.ProteomicsBedExporter.HIGH_ZSCORE_THRESHOLD

/**
 *
 */
@WithGMock
class ProteomicsBedExporterTests {

    @Delegate
    MockTabularResultHelper mockTabularResultHelper

    ProteomicsBedExporter exporter
    TabularResult tabularResult
    Projection projection

    def lowZscoreValue = LOW_ZSCORE_THRESHOLD - 0.1
    def highZscoreValue = HIGH_ZSCORE_THRESHOLD + 0.1

    @Before
    void before() {
        mockTabularResultHelper = new MockTabularResultHelper()
        exporter = new ProteomicsBedExporter()
        projection = mock(Projection)
    }

    @Test
    void "test whether Proteomics data is exported properly"() {

        tabularResult = createMockProteomicsTabularResult()

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
                    'track name="sample_code_1" itemRgb="On" genome_build="hg19"',
                    'chrX\t1234\t1300\ttest-region1\t' + lowZscoreValue + '\t.\t1234\t1300\t' + LOW_VALUE_RGB,
                    'CHRY\t1301\t1400\ttest-bio-marker\t' + 0 + '\t.\t1301\t1400\t' + DEFAULT_RGB,
                    'chr9\t1000\t2000\ttest-region3\t' + lowZscoreValue + '\t.\t1000\t2000\t' + LOW_VALUE_RGB,
            )

            List assay2Lines = assay2Output.readLines()
            assertThat assay2Lines, contains(
                    'track name="sample_code_2" itemRgb="On" genome_build="hg19"',
                    'chrX\t1234\t1300\ttest-region1\t' + highZscoreValue + '\t.\t1234\t1300\t' + HIGH_VALUE_RGB,
                    'CHRY\t1301\t1400\ttest-bio-marker\t' + highZscoreValue + '\t.\t1301\t1400\t' + HIGH_VALUE_RGB,
                    'chr9\t1000\t2000\ttest-region3\t' + 0 + '\t.\t1000\t2000\t' + DEFAULT_RGB,
            )
        }
    }

    protected createMockProteomicsTabularResult() {
        // Setup tabularResult and projection to test with
        List<AssayColumn> sampleAssays = createSampleAssays(2)
        Map<String, List<Object>> dataRows = [
                "row1": [
                        [intensity: 110, zscore: lowZscoreValue],
                        [intensity: 220, zscore: highZscoreValue]
                ],
                "row2": [
                        [intensity: 330, zscore: 0],
                        [intensity: 440, zscore: highZscoreValue]
                ],
                "row3": [
                        [intensity: 550, zscore: lowZscoreValue],
                        [intensity: 660, zscore: 0]
                ],
        ]

        Map<String, Map> proteomicsRowProperties = [
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
            createProteomicsRowForAssays(sampleAssays, data, proteomicsRowProperties[rowName], rowName)
        }.iterator()

        TabularResult highDimResult = mock TabularResult
        highDimResult.indicesList.returns(sampleAssays).stub()
        highDimResult.getRows().returns(iterator).stub()
        highDimResult.iterator().returns(iterator).stub()

        highDimResult
    }

    DataRow createProteomicsRowForAssays(List<AssayColumn> assays,
                                   List data,
                                   Map<String, Object> properties,
                                   String label) {
        createMockProteomicsRow(
                dot(assays, data, { a, b -> [a, b] })
                        .collectEntries(Closure.IDENTITY),
                properties,
                label)
    }

    private DataRow<AssayColumn, Object> createMockProteomicsRow(Map<AssayColumn, Object> values,
                                                           Map<String, Object> properties, String label) {
        DataRow row = mock(ProteinDataRow)
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
