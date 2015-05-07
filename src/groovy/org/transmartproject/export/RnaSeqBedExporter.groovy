package org.transmartproject.export

import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.chromoregion.RegionRowImpl

import javax.annotation.PostConstruct

@Log4j
class RnaSeqBedExporter extends AbstractChromosomalRegionBedExporter {

    @Autowired
    HighDimExporterRegistry highDimExporterRegistry

    @PostConstruct
    void init() {
        this.highDimExporterRegistry.registerHighDimensionExporter(format, this)
    }

    @Override
    boolean isDataTypeSupported(String dataType) {
        dataType == 'rnaseq'
    }

    @Override
    String getProjection() {
        Projection.ALL_DATA_PROJECTION
    }

    @Override
    protected calculateRow(RegionRowImpl datarow, AssayColumn assay) {
        int readcount = datarow[assay]['readcount']
        [
                datarow.chromosome,
                datarow.start,
                datarow.end,
                //Name of the BED line
                datarow.bioMarker ?: datarow.name,
                //Score
                readcount
        ]
    }

}
