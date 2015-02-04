package org.transmartproject.export

import org.springframework.stereotype.Component

@Component
class HighDimExporterRegistry {

    protected Map<String, HighDimExporter> exporterRegistry = new HashMap()

    /**
     * Register a new high dimensional data exporter.
     * @param exporterFormat
     * @param exporterClass
     */
    void registerHighDimensionExporter(String exporterFormat,
                                       HighDimExporter exporter) {


        this.exporterRegistry[exporterFormat] = exporter
        log.debug "Registered high dimensional exporter '$exporterFormat'"
    }

    /**
     * Returns an exporter to export a specific named format 
     * @param exporterFormat Format to export
     * @return
     * @throws NoSuchExporterException
     */
    HighDimExporter getExporterForFormat(String exporterFormat)
            throws NoSuchExporterException {
        if (!exporterRegistry.containsKey(exporterFormat)) {
            throw new NoSuchExporterException("Unknown format: $exporterFormat")
        }

        exporterRegistry[exporterFormat]
    }

    /**
     * Returns a set of exporters that are able to export
     * a certain datatype
     * @param dataType Name of the datatype to export
     * @return
     */
    Set<Closure<HighDimExporter>> getExportersForDataType(String dataType) {
        return exporterRegistry.values().findAll { exporter ->
            exporter.isDataTypeSupported(dataType)
        }
    }

}
