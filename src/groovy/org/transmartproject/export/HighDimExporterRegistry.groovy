package org.transmartproject.export

import org.springframework.stereotype.Component;

@Component
class HighDimExporterRegistry {
    
    protected Map<String, Class> exporterRegistry = new HashMap()
    
    /**
     * Register a new high dimensional data exporter.
     * @param exporterFormat
     * @param exporterClass
     */
    void registerHighDimensionExporter(String exporterFormat,
                                             Class<HighDimExporter> exporterClass) {
                                             
                                             
        this.exporterRegistry[exporterFormat] = exporterClass
        log.debug "Registered high dimensional exporter '$exporterFormat'"
    }

     
    HighDimExporter getExporterForFormat(String exporterFormat)
            throws NoSuchExporterException {
        if (!exporterRegistry.containsKey(exporterFormat)) {
            throw new NoSuchExporterException("Unknown format: $exporterFormat")
        }
        
        exporterRegistry[exporterFormat].newInstance()
    }

}
