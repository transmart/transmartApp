package org.transmartproject.export

import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionJunction
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionJunctionEvent
import org.transmartproject.db.dataquery.highdim.tworegion.JunctionRow
import groovy.util.logging.Log4j
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.transmartproject.core.dataquery.TabularResult;
import org.transmartproject.core.dataquery.highdim.projections.Projection;

@Log4j
public class TwoRegionExporter implements HighDimExporter {
    @Autowired
    private HighDimExporterRegistry highDimExporterRegistry;

    @PostConstruct
    public void init() {
        this.highDimExporterRegistry.registerHighDimensionExporter(format, this)
    }

    public boolean isDataTypeSupported(String dataType) {
        dataType == "two_region"
    }

    public String getProjection() {
        Projection.ALL_DATA_PROJECTION
    }


    @Override
    String getFormat() {
        'two region'
    }

    public String getDescription() {
        "Exports two region data in junction and events file"
    }

    public void export(TabularResult data, Projection projection, Closure<OutputStream> newOutputStream) {
        export(data, projection, newOutputStream, null);
    }

    public void export(TabularResult data, Projection projection, Closure<OutputStream> newOutputStream, Closure<java.lang.Boolean> isCancelled) {
        log.info("started exporting to $format ")
        def startTime = System.currentTimeMillis()

        if (isCancelled && isCancelled()) {
            return
        }

        //exports two files per sample - junctions and events which link to junctions via ids
        List<AssayColumn> assayList = data.indicesList
        Map<String, Writer> junctionStreamPerSample = [:]
        Map<String, Writer> eventStreamPerSample = [:]

        try {
            for (JunctionRow datarow : data) {
                if (isCancelled && isCancelled()) {
                    return
                }

                for (AssayColumn assay : assayList) {
                    if (isCancelled && isCancelled()) {
                        return
                    }
                    DeTwoRegionJunction junction = datarow[assay]
                    if (junction == null)
                        continue; //not all assays have all junctions
                    def assaytag = "${junction.assay.patientInTrialId}_${junction.assay.id}"

                    def junctionStream = junctionStreamPerSample[assaytag]
                    if (junctionStream == null) {
                        junctionStream = new BufferedWriter(new OutputStreamWriter(
                                newOutputStream(assaytag + "_junctions", 'tsv'), 'UTF-8'))
                        junctionStream << "id\tup_chr\tup_pos\tup_strand\tup_end\tdown_chr\tdown_pos\tdown_strand\tdown_end\tis_in_frame\n"
                        junctionStreamPerSample[assaytag] = junctionStream
                    }
                    def eventStream = eventStreamPerSample[assaytag]
                    if (eventStream == null) {
                        eventStream = new BufferedWriter(new OutputStreamWriter(
                                newOutputStream(assaytag + "_events", 'tsv'), 'UTF-8'))
                        eventStream << "reads_span\treads_junction\tpairs_span\tpairs_junction\tpairs_end\treads_counter\tbase_freq\tjunction_id\tcga_type\tsoap_class\tgene_ids\tgene_effect\n"
                        eventStreamPerSample[assaytag] = eventStream
                    }

                    junction.with {
                        junctionStream << "$id\t$upChromosome\t$upPos\t$upStrand\t$upEnd\t$downChromosome\t$downPos\t$downStrand\t$downEnd\t$isInFrame\n"
                    }

                    for (DeTwoRegionJunctionEvent junctionEvent : junction.junctionEvents) {
                        junctionEvent.with {
                            eventStream << "$readsSpan\t$readsJunction\t$pairsSpan\t$pairsJunction\t$pairsEnd\t$pairsCounter\t$baseFreq\t${junction.id}"
                        }
                        junctionEvent.event.with {
                            eventStream << "\t$cgaType\t$soapClass\t"
                        }
                        StringBuilder sbGenes = new StringBuilder(), sbEffects = new StringBuilder()
                        for (def gene : junctionEvent.event.eventGenes) {
                            sbGenes.append(gene.geneId).append(';')
                            sbEffects.append(gene.effect).append(';')
                        }
                        if (sbGenes.length() > 0)
                            eventStream << sbGenes << '\t' << sbEffects
                        else
                            eventStream << 'null\tnull'
                        eventStream << '\n'
                    }
                }
            }
        } finally {
            junctionStreamPerSample.values().each {
                it.close()
            }
            eventStreamPerSample.values().each {
                it.close()
            }
        }
        log.info("Exporting data took ${System.currentTimeMillis() - startTime} ms")
    }
}
