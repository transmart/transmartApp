package org.transmartproject.export

import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.exceptions.NoSuchResourceException

import javax.annotation.PostConstruct

@Log4j
class TabSeparatedExporter implements HighDimExporter {
    final static String SEPARATOR = "\t"

    @Autowired
    HighDimensionResource highDimensionResourceService

    @Autowired
    HighDimExporterRegistry highDimExporterRegistry

    @PostConstruct
    void init() {
        this.highDimExporterRegistry.registerHighDimensionExporter(
                format, this)
    }

    @Override
    public boolean isDataTypeSupported(String dataType) {
        // Each datatype that supports the projection used
        // can be exported as tsv
        try {
            HighDimensionDataTypeResource dataTypeResource =
                    highDimensionResourceService.getSubResourceForType(dataType)
            return projection in
                    dataTypeResource.supportedProjections
        } catch (NoSuchResourceException e) {
            // No resource found for datatype, so not supported.
            log.warn(e.getMessage())
            return false
        }
    }

    @Override
    public String getFormat() {
        return "TSV"
    }

    @Override
    public String getDescription() {
        return "Tab separated file"
    }

    @Override
    public void export(TabularResult tabularResult, Projection projection,
                       Closure<OutputStream> newOutputStream) {
        export(tabularResult, projection, newOutputStream, { false })
    }

    @Override
    public void export(TabularResult tabularResult, Projection projection,
                       Closure<OutputStream> newOutputStream, Closure<Boolean> isCancelled) {

        log.info("started exporting to $format ")
        def startTime = System.currentTimeMillis()

        if (isCancelled()) {
            return
        }

        // Determine the fields to be exported, and the label they get
        Map<String, String> dataKeys = projection.dataProperties.collectEntries {
            [it.key, getFieldTranslation(it.key).toUpperCase()]
        }
        Map<String, String> rowKeys = projection.rowProperties.collectEntries {
            [it.key, getFieldTranslation(it.key).toUpperCase()]
        }

        newOutputStream("data", format).withWriter("UTF-8") { writer ->

            // First write the header
            writeLine(writer, createHeader(dataKeys.values() + rowKeys.values()))

            // Determine the order of the assays
            List<AssayColumn> assayList = tabularResult.indicesList

            // Start looping 
            for (DataRow<AssayColumn, Object> datarow : tabularResult) {
                // Test periodically if the export is cancelled
                if (isCancelled()) {
                    return null
                }

                for (AssayColumn assay : assayList) {
                    // Retrieve data for the current assay from the datarow
                    def data = datarow[assay]

                    if (data == null) {
                        continue
                    }

                    // Add values for default columns
                    List<String> line = [
                            assay.id,
                    ]

                    // Return data for this specific assay
                    for (String dataField : dataKeys.keySet()) {
                        line << data[dataField]
                    }

                    // Return generic data for the row.
                    for (String rowField : rowKeys.keySet()) {
                        line << datarow."$rowField"
                    }

                    writeLine(writer, line)
                }
            }
        }

        log.info("Exporting data took ${System.currentTimeMillis() - startTime} ms")
    }

    /**
     * Returns a list of headers to be put into the output file
     * @param additionalHeaderFields List of headers to be added to the 
     *          default ones
     * @return List of headers to be put into the output file
     */
    protected List<String> createHeader(List additionalHeaderFields) {
        [
                "Assay ID",
        ] + (additionalHeaderFields ?: [])
    }

    /**
     * Translates a database field name into a human readable field name in the output file. 
     * If no translation is specified, the original name is returned
     * @param A field that is found in the rowProperties or the dataProperties of 
     *          the projection used
     * @return A translation from a database field name to a field name in the output file
     */
    protected String getFieldTranslation(String fieldname) {
        // These maps specify the row header in the output file for each database field name.
        Map translationMap = [
                rawIntensity: 'value',
                intensity   : 'value',
                value       : 'value',
                logIntensity: 'log2e',
                zscore      : 'zscore',

                geneSymbol  : 'gene symbol',
                geneId      : 'gene id',
                mirnaId     : 'mirna id',
                peptide     : 'peptide sequence',
                antigenName : 'analyte name',
                uniprotId   : 'uniprot id',
                transcriptId: 'transcript id'
        ]

        translationMap.get(fieldname, fieldname)
    }

    /**
     * Writes a list of data to the writer, ending with a newline
     * @param writer Writer to write the data to
     * @param data List of string to be written
     */
    protected void writeLine(Writer writer, List<String> data) {
        writer << data.join(SEPARATOR) << "\n"

    }

    @Override
    public String getProjection() {
        Projection.ALL_DATA_PROJECTION
    }

}
