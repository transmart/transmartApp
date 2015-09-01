package org.transmartproject.export

import groovy.util.logging.Log4j
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.chromoregion.RegionRow
import org.transmartproject.core.dataquery.highdim.projections.Projection

@Log4j
abstract class AbstractChromosomalRegionBedExporter implements HighDimExporter {

    final static String COLUMN_SEPARATOR = '\t'
    final static int CHROMOSOME_COLUMN_POSITION = 1
    final static int ITEM_RGB_COLUMN_POSITION = 9

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

        try {
            for (RegionRow datarow : tabularResult) {
                if (isCancelled && isCancelled()) {
                    return
                }

                for (AssayColumn assay : assayList) {
                    if (isCancelled && isCancelled()) {
                        return
                    }
                    if (!datarow[assay]) {
                        log.warn("(datrow.id=${datarow.id}, assay.id=${assay.id}) No cell data.")
                        continue
                    }
                    List row = calculateRow(datarow, assay)
                    if (row[0..2].any { !it }) {
                        log.warn("(datrow.id=${datarow.id}, assay.id=${assay.id}) Row has not required values: ${row}. Skip it.")
                        continue
                    }
                    Writer writer = streamsPerSample[assay.id]
                    if (writer == null) {
                        writer = new BufferedWriter(
                                    new OutputStreamWriter(
                                            newOutputStream("${assay.sampleCode}_${assay.id}", format), 'UTF-8'))

                        //Write header line
                        writer << "track name=\"${assay.sampleCode}\" "
                        if (row.size() >= ITEM_RGB_COLUMN_POSITION) {
                            writer << "itemRgb=\"On\" "
                        } else {
                            writer << "useScore=\"1\" "
                        }
                        if (datarow.platform) {
                            writer << "genome_build=\"${datarow.platform.genomeReleaseId}\""
                        }
                        writer << '\n'

                        streamsPerSample[assay.id] = writer
                    }
                    String chromosome = row[CHROMOSOME_COLUMN_POSITION - 1]
                    if (!chromosome.toLowerCase().startsWith('chr')) {
                        row[CHROMOSOME_COLUMN_POSITION - 1] = "chr${chromosome}"
                    }
                    writer << row.join(COLUMN_SEPARATOR) << '\n'
                }
            }
        } finally {
            streamsPerSample.values().each {
                it.close()
            }
        }

        log.info("Exporting data took ${System.currentTimeMillis() - startTime} ms")
    }

    protected abstract calculateRow(RegionRow datarow, AssayColumn assay)

}
