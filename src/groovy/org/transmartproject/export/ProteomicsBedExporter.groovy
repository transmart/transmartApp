package org.transmartproject.export

import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.colorscheme.HsbColorPicker
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.BioMarkerDataRow
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.projections.Projection

import javax.annotation.PostConstruct

@Log4j
class ProteomicsBedExporter extends AbstractChromosomalRegionBedExporter {

    private static final HIGH_ZSCORE = 3
    private static final LOW_ZSCORE = -HIGH_ZSCORE

    private final HsbColorPicker colorPicker = new HsbColorPicker(LOW_ZSCORE, HIGH_ZSCORE)

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
        def intensity = assayDataRow['intensity']
        def zscore = assayDataRow['zscore']

        [
                datarow.chromosome,
                datarow.start,
                datarow.end,
                //Name of the BED line
                datarow instanceof BioMarkerDataRow ?
                        datarow.bioMarker ?: datarow.name
                        : datarow.name,
                //Score
                intensity,
                //Strand. We do not use strand information
                '.',
                //Thick start
                datarow.start,
                //Thick end
                datarow.end,
                //Item RGB
                colorPicker.scaleLinearly(zscore).join(',')
        ]
    }

}
