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
class AcghBedExporterTests {

    @Delegate
    MockTabularResultHelper mockTabularResultHelper

    AcghBedExporter exporter
    TabularResult tabularResult
    Projection projection

    @Before
    void before() {
        mockTabularResultHelper = new MockTabularResultHelper()
        exporter = new AcghBedExporter()
        exporter.highDimExporterRegistry = [
                registerHighDimensionExporter: { format, exporter -> }
        ] as HighDimExporterRegistry
        projection = mock(Projection)
        exporter.init()
    }

    @Test
    void "test whether aCGH data is exported properly"() {

        tabularResult = createMockAcghTabularResult()

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
                    'chrX\t1234\t1300\ttest-region1\t-1\t.\t1234\t1300\t0,0,205',
                    'CHRY\t1301\t1400\ttest-bio-marker\t1\t.\t1301\t1400\t205,0,0',
                    'chr9\t1000\t2000\ttest-region3\t11\t.\t1000\t2000\t255,255,255',
            )

            List assay2Lines = assay2Output.readLines()
            assertThat assay2Lines, contains(
                    'track name="sample_code_2" itemRgb="On" genome_build="hg19"',
                    'chrX\t1234\t1300\ttest-region1\t0\t.\t1234\t1300\t169,169,169',
                    'CHRY\t1301\t1400\ttest-bio-marker\t2\t.\t1301\t1400\t88,0,0',
                    'chr9\t1000\t2000\ttest-region3\t-1\t.\t1000\t2000\t0,0,205',
            )
        }
    }

    @Test
    void "test custom color scheme"() {

        exporter.acghBedExporterRgbColorScheme = [
                //white
                invalid      : [250, 250, 250],
                //red
                loss         : [205, 0, 0],
                //dark
                normal       : [10, 10, 10],
                //green
                gain         : [0, 255, 0],
                //dark green
                amplification: [0, 100, 0],
        ]

        exporter.init()

        tabularResult = createMockAcghTabularResult()

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
                    'chrX\t1234\t1300\ttest-region1\t-1\t.\t1234\t1300\t205,0,0',
                    'CHRY\t1301\t1400\ttest-bio-marker\t1\t.\t1301\t1400\t0,255,0',
                    'chr9\t1000\t2000\ttest-region3\t11\t.\t1000\t2000\t250,250,250',
            )

            List assay2Lines = assay2Output.readLines()
            assertThat assay2Lines, contains(
                    'track name="sample_code_2" itemRgb="On" genome_build="hg19"',
                    'chrX\t1234\t1300\ttest-region1\t0\t.\t1234\t1300\t10,10,10',
                    'CHRY\t1301\t1400\ttest-bio-marker\t2\t.\t1301\t1400\t0,100,0',
                    'chr9\t1000\t2000\ttest-region3\t-1\t.\t1000\t2000\t205,0,0',
            )
        }
    }

    protected createMockAcghTabularResult() {
        // Setup tabularResult and projection to test with
        List<AssayColumn> sampleAssays = createSampleAssays(2)
        Map<String, List<Object>> dataRows = [
                "row1": [
                        ["flag": -1],
                        ["flag": 0]
                ],
                "row2": [
                        ["flag": 1],
                        ["flag": 2]
                ],
                "row3": [
                        ["flag": 11],
                        ["flag": -1]
                ],
        ]

        Map<String, Map> acghRowProperties = [
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
            createAcghRowForAssays(sampleAssays, data, acghRowProperties[rowName], rowName)
        }.iterator()

        TabularResult highDimResult = mock TabularResult
        highDimResult.indicesList.returns(sampleAssays).stub()
        highDimResult.getRows().returns(iterator).stub()
        highDimResult.iterator().returns(iterator).stub()

        highDimResult
    }

    DataRow createAcghRowForAssays(List<AssayColumn> assays,
                                   List data,
                                   Map<String, Object> acghProperties,
                                   String label) {
        createMockAcghRow(
                dot(assays, data, { a, b -> [a, b] })
                        .collectEntries(Closure.IDENTITY),
                acghProperties,
                label)
    }

    private DataRow<AssayColumn, Object> createMockAcghRow(Map<AssayColumn, Object> values,
                                                           Map<String, Object> acghProperties, String label) {
        DataRow row = mock(RegionRowImpl)
        row.label.returns(label).stub()

        row.chromosome.returns(acghProperties.chromosome).stub()
        row.start.returns(acghProperties.start).stub()
        row.end.returns(acghProperties.end).stub()
        row.name.returns(acghProperties.name).stub()
        row.bioMarker.returns(acghProperties.bioMarker).stub()
        if (acghProperties.platform) {
            Platform platform = mock(Platform)
            platform.genomeReleaseId.returns(acghProperties.platform.genomeReleaseId).stub()
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
