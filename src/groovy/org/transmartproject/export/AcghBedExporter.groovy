package org.transmartproject.export

import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.acgh.CopyNumberState
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.chromoregion.RegionRowImpl

import javax.annotation.PostConstruct
import javax.annotation.Resource

@Log4j
class AcghBedExporter implements HighDimExporter {

    final static COLUMN_SEPARATOR = '\t'

    @Autowired
    HighDimExporterRegistry highDimExporterRegistry

    @Resource
    Map bedExporterRgbColorScheme

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
            if (bedExporterRgbColorScheme && bedExporterRgbColorScheme[cpNState.name().toLowerCase()]) {
                rgbValues = bedExporterRgbColorScheme[cpNState.name().toLowerCase()]
            }
            result[cpNState.intValue] = rgbValues.join(',')
        }
        result
    }

    @PostConstruct
    void init() {
        this.highDimExporterRegistry.registerHighDimensionExporter(
                format, this)
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
    String getFormat() {
        'BED'
    }

    @Override
    String getDescription() {
        '''BED format provides a flexible way to define the data lines that are displayed in an annotation track.
        See http://genome.ucsc.edu/FAQ/FAQformat.html#format1'''
    }

    @Override
    void export(TabularResult data, Projection projection, Closure<OutputStream> newOutputStream) {
        export(data, projection, newOutputStream, null)
    }

    @Override
    void export(TabularResult tabularResult, Projection projection,
                Closure<OutputStream> newOutputStream, Closure<Boolean> isCancelled) {
        log.info("started exporting to $format ")
        def startTime = System.currentTimeMillis()

        if (isCancelled && isCancelled()) {
            return
        }

        List<AssayColumn> assayList = tabularResult.indicesList
        Map<Object, Writer> streamsPerSample = [:]
        def flagToRgbMap = prepareFlagToRgbStringMap()

        try {
            for (RegionRowImpl datarow : tabularResult) {
                if (isCancelled && isCancelled()) {
                    return
                }

                //This ensures the bed file can be loaded into UCSC browser
                String chromosome = datarow.chromosome
                if (!chromosome.toLowerCase().startsWith('chr')) {
                    chromosome = "chr${chromosome}"
                }
                //We do not use strand information
                String strand = '.'
                String label = datarow.bioMarker ?: datarow.name

                for (AssayColumn assay : assayList) {
                    if (isCancelled && isCancelled()) {
                        return
                    }
                    Map cell = datarow[assay]
                    int flag = cell['flag']
                    Writer writer = streamsPerSample[assay.id]
                    if (writer == null) {
                        writer = new BufferedWriter(
                                    new OutputStreamWriter(
                                            newOutputStream("${assay.sampleCode}_${assay.id}.${format.toLowerCase()}"), 'UTF-8'))
                        writer << "track name=\"${assay.sampleCode}\" itemRgb=\"On\" genome_build=\"${datarow.platform.genomeReleaseId}\"\n"
                        streamsPerSample[assay.id] = writer
                    }
                    writer << [
                            chromosome,
                            datarow.start,
                            datarow.end,
                            label,
                            flag,
                            strand,
                            datarow.start,
                            datarow.end,
                            flagToRgbMap[flag]
                        ].join(COLUMN_SEPARATOR) << '\n'
                }
            }
        } finally {
            streamsPerSample.values().each {
                it.close()
            }
        }

        log.info("Exporting data took ${System.currentTimeMillis() - startTime} ms")
    }

}
