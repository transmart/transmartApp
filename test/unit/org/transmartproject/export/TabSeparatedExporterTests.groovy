package org.transmartproject.export

import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.exceptions.NoSuchResourceException

/**
 *
 */
@WithGMock
class TabSeparatedExporterTests {
    @Delegate
    MockTabularResultHelper mockTabularResultHelper

    TabSeparatedExporter exporter
    TabularResult tabularResult
    Projection projection

    @Before
    void before() {
        mockTabularResultHelper = new MockTabularResultHelper()
        mockTabularResultHelper.gMockController = $gmockController

        // Setup exporter
        exporter = new TabSeparatedExporter()
    }

    @Test
    void "test whether supported datatypes are recognized"() {

        def mrnaResource = mock(HighDimensionDataTypeResource) {
            getSupportedProjections().returns(["all_data"])
        }

        def mirnaResource = mock(HighDimensionDataTypeResource) {
            getSupportedProjections().returns(["default_real_projection", "some_other_projection", "all_data"])
        }

        def otherResource = mock(HighDimensionDataTypeResource) {
            getSupportedProjections().returns(["default_real_projection", "some_other_projection"])
        }

        def resourceService = mock(HighDimensionResource) {
            getSubResourceForType("mrna").returns(mrnaResource)
            getSubResourceForType("mirna").returns(mirnaResource)
            getSubResourceForType("other").returns(otherResource)
            getSubResourceForType(null).raises(new NoSuchResourceException("Unknown data type: null"))
            getSubResourceForType("unknownFormat").raises(new NoSuchResourceException("Unknown data type: other"))
        }

        exporter.highDimensionResourceService = resourceService

        play {
            // Tab separated export is supported on every datatype
            // with ALL_DATA_PROJECTION
            assert exporter.isDataTypeSupported("mrna")
            assert exporter.isDataTypeSupported("mirna")

            assert !exporter.isDataTypeSupported("other")
            assert !exporter.isDataTypeSupported(null)
            assert !exporter.isDataTypeSupported("unknownFormat")
        }
    }

    @Test
    void "test whether a cancelled export doesn't produce output"() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        def tabularResult = mock(TabularResult)
        def projection = mock(Projection)

        play {

            exporter.export(tabularResult, projection, { name, ext -> outputStream}, { true })

            // As the export is cancelled, 
            assert !outputStream.toString()
        }
    }

    @Test
    void "test whether a basic tabular result is exported properly"() {
        // Setup tabularResult and projection to test with
        List<AssayColumn> sampleAssays = createSampleAssays(2)
        Map<String, List<Object>> labelToData = [
                "row1": [["property1": 4,], ["property1": 2, "property2": 3]],
                "row2": [["property1": 5, "property2": 10], null],
        ]
        tabularResult = createMockTabularResult(assays: sampleAssays, data: labelToData)

        // Create all data projection, as that is used for exporting tab separated files
        def projection = mock(Projection) {
            getRowProperties().returns(["label": String])
            getDataProperties().returns(["property1": Integer, "property2": Integer])
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        play {
            exporter.export(tabularResult, projection, { name, ext -> outputStream })

            // Assert we have at least some text, in UTF-8 encoding
            String output = outputStream.toString("UTF-8")
            assert output

            // We expect 4 lines: 1 header line, two lines  for assay 1, and one line for assay 2
            List lines = output.readLines()
            assert lines.size() == 4

            // Check header line
            assert lines[0] == ["Assay ID", "PROPERTY1", "PROPERTY2", "LABEL"].join(exporter.SEPARATOR)

            // Check the data lines. First line should contain 'null' for property2, as that one was not set
            assert lines[1] == ["1", "4", "null", "row1"].join(exporter.SEPARATOR)

            assert lines[2] == ["2", "2", "3", "row1"].join(exporter.SEPARATOR)

            assert lines[3] == ["1", "5", "10", "row2"].join(exporter.SEPARATOR)

        }
    }

}
