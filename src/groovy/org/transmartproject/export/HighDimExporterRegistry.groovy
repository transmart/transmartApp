package org.transmartproject.export

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import groovy.util.logging.Log4j
import org.springframework.stereotype.Component

@Component
@Log4j
class HighDimExporterRegistry {

    protected Multimap<String, HighDimExporter> exporterRegistry = HashMultimap.create()

    /**
     * Register a new high dimensional data exporter.
     * @param exporterFormat
     * @param exporterClass
     */
    void registerHighDimensionExporter(String exporterFormat,
                                       HighDimExporter exporter) {
        this.exporterRegistry.put(exporterFormat, exporter)
        log.debug "Registered high dimensional exporter '$exporterFormat'"
    }

    /**
     * @param criteriaMap.dataType Name of the datatype to export
     * @param criteriaMap.fileFormat Format to export
     * @return Returns a set of exporters that are able to export
     * a certain datatype or certain file format.
     */
    Set<HighDimExporter> findExporters(Map criteriaMap = [:]) {
        String fileFormat = criteriaMap.fileFormat
        String dataType = criteriaMap.dataType

        final Set<HighDimExporter> exporters
        if (fileFormat) {
            if (!exporterRegistry.containsKey(fileFormat)) {
                throw new NoSuchExporterException("Unknown format: ${fileFormat}")
            }
            exporters = exporterRegistry.get(fileFormat) as Set
        } else {
            exporters = exporterRegistry.values() as Set
        }

        if (dataType) {
            exporters.findAll { exporter ->
                exporter.isDataTypeSupported(dataType)
            }
        } else {
            exporters
        }
    }

}
