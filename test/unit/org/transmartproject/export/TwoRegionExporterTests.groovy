package org.transmartproject.export

import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionEvent
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionEventGene
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionJunction
import org.transmartproject.db.dataquery.highdim.tworegion.DeTwoRegionJunctionEvent
import org.transmartproject.db.dataquery.highdim.tworegion.JunctionRow
import static org.hamcrest.Matchers.contains
import static org.hamcrest.Matchers.hasSize
import static org.junit.Assert.assertThat

/**
 * Created by j.hudecek on 15-6-2015.
 */
@WithGMock
class TwoRegionExporterTests {

    @Delegate
    MockTabularResultHelper mockTabularResultHelper

    TwoRegionExporter exporter
    TabularResult tabularResult
    Projection projection

    @Before
    void before() {
        mockTabularResultHelper = new MockTabularResultHelper()
        exporter = new TwoRegionExporter()
        exporter.highDimExporterRegistry = [
                registerHighDimensionExporter: { format, exporter -> }
        ] as HighDimExporterRegistry
        projection = mock(Projection)
        exporter.init()
    }

    @Test
    void "test whether two region data is exported properly"() {

        tabularResult = createMockTwoRegionTabularResult()
        List<ByteArrayOutputStream> outputStreams = []

        play {
            exporter.export(tabularResult, projection, { name, ext ->
                outputStreams << new ByteArrayOutputStream()
                outputStreams[-1]
            })

            //3 assays, have both events and junction files=6 files in total
            assertThat outputStreams, hasSize(6)

            String junctionOutput = outputStreams[0].toString("UTF-8")
            String eventOutput = outputStreams[1].toString("UTF-8")

            List junctions = junctionOutput.readLines()
            assertThat junctions, contains(
                    'id\tup_chr\tup_pos\tup_strand\tup_end\tdown_chr\tdown_pos\tdown_strand\tdown_end\tis_in_frame',
                    '1\t3\t12\t-\t18\t1\t2\t+\t10\ttrue',
                    '2\t13\t12\t-\t18\t10\t2\t+\t10\ttrue'
            )

            List events = eventOutput.readLines()
            assertThat events, contains(
                    'reads_span\treads_junction\tpairs_span\tpairs_junction\tpairs_end\treads_counter\tbase_freq\tjunction_id\tcga_type\tsoap_class\tgene_ids\tgene_effect',
                    'null\tnull\t10\tnull\tnull\tnull\tnull\t1\tdeletion\tnull\tnull\tnull',
                    'null\tnull\t10\tnull\tnull\tnull\tnull\t2\tdeletion\tnull\tnull\tnull')

            junctionOutput = outputStreams[2].toString("UTF-8")
            eventOutput = outputStreams[3].toString("UTF-8")

            junctions = junctionOutput.readLines()
            assertThat junctions, contains(
                    'id\tup_chr\tup_pos\tup_strand\tup_end\tdown_chr\tdown_pos\tdown_strand\tdown_end\tis_in_frame',
                    '3\t3\t12\t-\t18\tX\t2\t+\t10\ttrue',
                    '4\t3\t12\t-\t18\tY\t2\t+\t10\ttrue'
            )

            events = eventOutput.readLines()
            assertThat events, contains(
                    'reads_span\treads_junction\tpairs_span\tpairs_junction\tpairs_end\treads_counter\tbase_freq\tjunction_id\tcga_type\tsoap_class\tgene_ids\tgene_effect',
                    'null\tnull\t10\tnull\tnull\tnull\tnull\t3\tnull\ttranslocation\tTP53;\tfusion;')
        }
    }

    protected createMockTwoRegionTabularResult() {
        // Setup tabularResult and projection to test with
        List<AssayColumn> sampleAssays = createSampleAssays(3)
        List<DeSubjectSampleMapping> assays = sampleAssays.collect({
            new DeSubjectSampleMapping(id: it.id, patientInTrialId: it.patientInTrialId)
        })

        def junctions = []
        //1st event: deletion assay0, chr1 2-10 - chr3 12-18 + assay0, chr10 2-10 - chr13 12-18 + assay0, chrX 2-10 - chr3 12-18
        //2nd event: deletion assay1, chrX 2-10 - chr3 12-18
        //junction without event assay1, chrY 2-10 - chr3 12-18
        def event = new DeTwoRegionEvent()
        event.cgaType = "deletion"
        def junction = new DeTwoRegionJunction(
                downChromosome: "1",
                downPos: 2,
                downEnd: 10,
                downStrand: "+",
                upChromosome: "3",
                upPos: 12,
                upEnd: 18,
                upStrand: "-",
                isInFrame: true,
                assay: assays[0]
        )
        junction.id = 1
        junctions.add(junction)
        def junctionEvent = new DeTwoRegionJunctionEvent(
                event: event,
                junction: junction,
                pairsSpan: 10
        )
        junction.junctionEvents = [junctionEvent]

        junction = new DeTwoRegionJunction(
                downChromosome: "10",
                downPos: 2,
                downEnd: 10,
                downStrand: "+",
                upChromosome: "13",
                upPos: 12,
                upEnd: 18,
                upStrand: "-",
                isInFrame: true,
                assay: assays[0]
        )
        junction.id = 2
        junctions.add(junction)
        junctionEvent = new DeTwoRegionJunctionEvent(
                event: event,
                junction: junction,
                pairsSpan: 10
        )
        junction.junctionEvents = [junctionEvent]
        def event1 = event

        def gene = new DeTwoRegionEventGene(
                geneId: 'TP53',
                effect: "fusion"
        )

        event = new DeTwoRegionEvent(
                soapClass: "translocation",
                eventGenes: [gene]
        )
        gene.event = event

        def junction2 = new DeTwoRegionJunction(
                downChromosome: "X",
                downPos: 2,
                downEnd: 10,
                downStrand: "+",
                upChromosome: "3",
                upPos: 12,
                upEnd: 18,
                upStrand: "-",
                isInFrame: true,
                assay: assays[1]
        )
        junction2.id = 3
        junctions.add(junction2)
        junctionEvent = new DeTwoRegionJunctionEvent(
                event: event,
                junction: junction2,
                pairsSpan: 10
        )
        junction2.junctionEvents = [junctionEvent]
        junctionEvent = new DeTwoRegionJunctionEvent(
                event: event1,
                junction: junction2,
                pairsSpan: 11
        )

        junction = new DeTwoRegionJunction(
                downChromosome: "Y",
                downPos: 2,
                downEnd: 10,
                downStrand: "+",
                upChromosome: "3",
                upPos: 12,
                upEnd: 18,
                upStrand: "-",
                isInFrame: true,
                assay: assays[1]
        )
        junction.id = 4
        junctions.add(junction)

        junction = new DeTwoRegionJunction(
                downChromosome: "Y",
                downPos: 2,
                downEnd: 10,
                downStrand: "+",
                upChromosome: "3",
                upPos: 12,
                upEnd: 18,
                upStrand: "-",
                isInFrame: true,
                assay: assays[2]
        )
        junction.id = 5
        junctions.add(junction)

        List<JunctionRow> jrs = junctions.collect({ DeTwoRegionJunction dejunction ->
            int assayIndex = assays.findIndexOf { DeSubjectSampleMapping ssm -> dejunction.assay.id == ssm.id }
            new JunctionRow(sampleAssays[assayIndex], assayIndex, 3, dejunction)
        })

        TabularResult highDimResult = mock TabularResult
        highDimResult.indicesList.returns(sampleAssays).stub()
        highDimResult.getRows().returns(jrs.iterator()).stub()
        highDimResult.iterator().returns(jrs.iterator()).stub()

        highDimResult
    }
}
