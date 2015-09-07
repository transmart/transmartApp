package org.transmartproject.export

import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.BioMarkerDataRow
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.projections.Projection

import javax.annotation.PostConstruct

@Log4j
class ProteomicsBedExporter extends AbstractChromosomalRegionBedExporter {

    static final BigDecimal HIGH_ZSCORE_THRESHOLD = new BigDecimal(1.5)
    static final BigDecimal LOW_ZSCORE_THRESHOLD = HIGH_ZSCORE_THRESHOLD.negate()

    static final String LOW_VALUE_RGB = '0,0,205'
    static final String HIGH_VALUE_RGB = '205,0,0'
    static final String DEFAULT_RGB = '196,196,196'

    @Autowired
    HighDimExporterRegistry highDimExporterRegistry

    @PostConstruct
    void init() {
        this.highDimExporterRegistry.registerHighDimensionExporter(format, this)
    }

    @Override
    boolean isDataTypeSupported(String dataType) {
        dataType == 'protein'
    }

    @Override
    String getProjection() {
        Projection.ALL_DATA_PROJECTION
    }

    @Override
    protected calculateRow(RegionRow datarow, AssayColumn assay) {
        def assayDataRow = datarow[assay]
        BigDecimal zscore = assayDataRow['zscore']

        [
                datarow.chromosome,
                datarow.start,
                datarow.end,
                //Name of the BED line
                datarow instanceof BioMarkerDataRow ?
                        datarow.bioMarker ?: datarow.name
                        : datarow.name,
                //Score
                zscore,
                //Strand. We do not use strand information
                '.',
                //Thick start
                datarow.start,
                //Thick end
                datarow.end,
                //Item RGB
                getColor(zscore)
        ]
    }

    private static String getColor(BigDecimal zscore) {
        if (zscore < LOW_ZSCORE_THRESHOLD) {
            LOW_VALUE_RGB
        } else if (zscore > HIGH_ZSCORE_THRESHOLD) {
            HIGH_VALUE_RGB
        } else {
            DEFAULT_RGB
        }

    }

}
