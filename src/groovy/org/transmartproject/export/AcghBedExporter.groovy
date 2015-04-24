package org.transmartproject.export

import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.acgh.CopyNumberState
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.chromoregion.RegionRowImpl

import javax.annotation.PostConstruct
import javax.annotation.Resource

@Log4j
class AcghBedExporter extends AbstractChromosomalRegionBedExporter {

    @Autowired
    HighDimExporterRegistry highDimExporterRegistry

    @Resource
    Map acghBedExporterRgbColorScheme

    Map<Integer, String> flagToRgbStringMap

    private static final Map<CopyNumberState, List<Integer>> FLAG_TO_RGB_DEFAULT_MAP = [
                //white
                (CopyNumberState.INVALID):         [255, 255, 255],
                //blue
                (CopyNumberState.LOSS):            [0, 0, 205],
                //gray
                (CopyNumberState.NORMAL):          [169, 169, 169],
                //red
                (CopyNumberState.GAIN):            [205, 0, 0],
                //dark red
                (CopyNumberState.AMPLIFICATION):   [88, 0, 0],
        ]

    private Map<Integer, String> prepareFlagToRgbStringMap() {
        Map<Integer, String> result = [:]
        for (CopyNumberState cpNState : CopyNumberState.values()) {
            List<Integer> rgbValues = FLAG_TO_RGB_DEFAULT_MAP[cpNState]
            if (acghBedExporterRgbColorScheme && acghBedExporterRgbColorScheme[cpNState.name().toLowerCase()]) {
                rgbValues = acghBedExporterRgbColorScheme[cpNState.name().toLowerCase()]
            }
            result[cpNState.intValue] = rgbValues.join(',')
        }
        result
    }

    @PostConstruct
    void init() {
        this.flagToRgbStringMap = prepareFlagToRgbStringMap()
        this.highDimExporterRegistry.registerHighDimensionExporter(format, this)
    }

    @Override
    boolean isDataTypeSupported(String dataType) {
        dataType == 'acgh'
    }

    @Override
    String getProjection() {
        Projection.ALL_DATA_PROJECTION
    }

    @Override
    protected calculateRow(RegionRowImpl datarow, AssayColumn assay) {
        Map cell = datarow[assay]
        int flag = cell['flag']

        [
                datarow.chromosome,
                datarow.start,
                datarow.end,
                //Name of the BED line
                datarow.bioMarker ?: datarow.name,
                //Score
                flag,
                //Strand. We do not use strand information
                '.',
                //Thick start
                datarow.start,
                //Thick end
                datarow.end,
                //Item RGB
                flagToRgbStringMap[flag]
        ]
    }

}
