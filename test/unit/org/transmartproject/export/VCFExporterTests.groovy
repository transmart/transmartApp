package org.transmartproject.export

import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection

/**
 *
 */
@WithGMock
class VCFExporterTests {
    @Delegate
    MockTabularResultHelper mockTabularResultHelper

    VCFExporter exporter
    TabularResult tabularResult
    Projection projection

    @Before
    void before() {
        mockTabularResultHelper = new MockTabularResultHelper()
        mockTabularResultHelper.gMockController = $gmockController

        // Setup exporter
        exporter = new VCFExporter()
    }

    @Test
    void "test whether supported datatypes are recognized"() {
        // Only VCF datatype is supported
        assert exporter.isDataTypeSupported("vcf")

        assert !exporter.isDataTypeSupported("other")
        assert !exporter.isDataTypeSupported(null)
    }

    @Test
    void "test whether a cancelled export doesn't produce output"() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        def tabularResult = mock(TabularResult)
        def projection = mock(Projection)

        play {

            exporter.export(tabularResult, projection, { name, ext -> outputStream}, { true })

            // As the export is cancelled, 
            assert !outputStream.toString()
        }
    }

    @Test
    void "test whether a basic tabular result is exported properly"() {
        tabularResult = createMockVCFTabularResult()

        // Create cohort projection, as that is used for exporting tab separated files
        def projection = mock(Projection)

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        play {
            exporter.export(tabularResult, projection, { name, ext -> outputStream })

            // Assert we have at least some text, in UTF-8 encoding
            String output = outputStream.toString("UTF-8")
            assert output

            // We expect 4 lines: 1 header line, two lines  for assay 1, and one line for assay 2
            List lines = output.readLines()
            assert lines.size() == 8

            expectHeader(lines[0..2])
            expectDataHeader(lines[3])

            // Check different chromosomal positions
            assert lines[4].startsWith(
                    ["1", "100", "rs0010", "G", ".", "50", "PASS"].join("\t"))
            assert lines[4].endsWith(
                    ["GT:DP", "0/0:3", "0/0:7"].join("\t"))

            assert lines[5].startsWith(
                    ["2", "190", ".", "A", "G", "90", "q10"].join("\t"))
            assert lines[5].endsWith(
                    // There is no T nucleotide anymore. That's why index was changed from 2 to 1.
                    ["GT", "0/1", "1/1"].join("\t"))

            assert lines[6].startsWith(
                    ["X", "190", ".", "G", "A", "90", "PASS"].join("\t"))
            assert lines[6].endsWith(
                    ["GT:DP", "0:3", "1:7"].join("\t"))

            assert lines[7].startsWith(
                    ["6", "190", ".", "G", "A,T,C", "90", "q10"].join("\t"))
            assert lines[7].endsWith(
                    ["DP:GT:QS", "1:0/1:2", "5:2/3:8"].join("\t"))

        }
    }

    @Test
    void "test whether a infofields are exported properly"() {
        tabularResult = createMockVCFTabularResult()

        // Create cohort projection, as that is used for exporting tab separated files
        def projection = mock(Projection)

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()

        play {
            exporter.export(tabularResult, projection, { name, ext -> outputStream })

            // Assert we have at least some text, in UTF-8 encoding
            String output = outputStream.toString("UTF-8")
            assert output

            // We expect 4 lines: 1 header line, two lines  for assay 1, and one line for assay 2
            List lines = output.readLines()
            assert lines.size() == 8

            def infoFields = lines[4..6].collect { it.split("\t")[7].tokenize(";") }

            assert infoFields[0].contains("AA=0")
            assert infoFields[0].contains("AN=4")
            assert infoFields[0].contains("NS=2")

            assert infoFields[1].contains("AA=0")
            assert infoFields[1].contains("AC=1")
            assert infoFields[1].contains("AF=0.25")
            assert infoFields[1].contains("AN=4")
            assert infoFields[1].contains("NS=2")

            assert infoFields[2].contains("H3")
            assert infoFields[2].contains("AC=1")
            assert infoFields[2].contains("AF=0.5")
            assert infoFields[2].contains("AN=2")
            assert infoFields[2].contains("NS=2")
        }
    }


    protected expectHeader(List<String> headerLines) {
        assert headerLines[0].equals("##fileformat=VCFv4.2")
        assert headerLines[1].equals("##fileDate=" + new Date().format('yyyyMMdd'))
        assert headerLines[2].startsWith("##source=transmart")
    }

    protected expectDataHeader(String dataHeaderLine) {
        assert dataHeaderLine == "#" + [
                "CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO", "FORMAT",
                "assay_1", "assay_2"].join("\t")

    }

    protected createMockVCFTabularResult() {
        // Setup tabularResult and projection to test with
        List<AssayColumn> sampleAssays = createSampleAssays(2)
        def sampleCodes = sampleAssays*.sampleCode
        Map<String, List<Object>> labelToData = [
                "row1"   : [
                        ["allele1"  : 0, "allele2": 0,
                         "subjectId": sampleCodes[0], "subjectPosition": 1],
                        ["allele1"  : 0, "allele2": 0,
                         "subjectId": sampleCodes[1], "subjectPosition": 2],
                ], "row2": [
                ["allele1"  : 0, "allele2": 2,
                 "subjectId": sampleCodes[0], "subjectPosition": 1],
                ["allele1"  : 2, "allele2": 2,
                 "subjectId": sampleCodes[1], "subjectPosition": 2],
        ], "row3"        : [ // X chromosome
                             ["allele1"  : 0,
                              "subjectId": sampleCodes[0], "subjectPosition": 1],
                             ["allele1"  : 1,
                              "subjectId": sampleCodes[1], "subjectPosition": 2]
        ], "row4"        : [
                ["allele1"  : 0, "allele2": 1,
                 "subjectId": sampleCodes[0], "subjectPosition": 1],
                ["allele1"  : 2, "allele2": 3,
                 "subjectId": sampleCodes[1], "subjectPosition": 2],
        ]
        ]

        Map<String, Map> vcfProperties = [
                "row1": [chromosome     : 1, position: 100, rsId: "rs0010",
                         referenceAllele: "G", alternativeAlleles: [],
                         quality        : 50, filter: "PASS", infoFields: ["AA": 0, "XYZ": 100],
                         format         : "GT:DP", variants: "0/0:3\t0/0:7",
                         cohortInfo     : [
                                 referenceAllele        : "G",
                                 alternativeAlleles     : [],
                                 alleles                : ["G"],
                                 alleleCount            : [4],
                                 alleleFrequency        : [1.0],
                                 totalAlleleCount       : 4,
                                 numberOfSamplesWithData: 2
                         ]],
                "row2": [chromosome     : 2, position: 190, rsId: ".",
                         referenceAllele: "A", alternativeAlleles: ["T", "G"],
                         quality        : 90, filter: "q10", infoFields: ["AA": 0, "NS": 100],
                         format         : "GT", variants: "0/2\t2/2", cohortInfo: [
                        referenceAllele        : "A",
                        alternativeAlleles     : ["G"],
                        alleles                : ["A", "G"],
                        alleleCount            : [3, 1],
                        alleleFrequency        : [0.75, 0.25],
                        totalAlleleCount       : 4,
                        numberOfSamplesWithData: 2
                ]],
                "row3": [chromosome     : "X", position: 190, rsId: ".",
                         referenceAllele: "G", alternativeAlleles: ["A"],
                         quality        : 90, filter: "PASS", infoFields: ["AN": 1, "TEST": 3, "H3": true],
                         format         : "GT:DP", variants: "0:3\t1:7", cohortInfo: [
                        referenceAllele        : "G",
                        alternativeAlleles     : ["A"],
                        alleles                : ["G", "A"],
                        alleleCount            : [1, 1],
                        alleleFrequency        : [0.5, 0.5],
                        totalAlleleCount       : 2,
                        numberOfSamplesWithData: 2
                ]],
                "row4": [chromosome     : 6, position: 190, rsId: ".",
                         referenceAllele: "G", alternativeAlleles: ["A", "T", "C"],
                         quality        : 90, filter: "q10", infoFields: ["AA": 0, "NS": 100],
                         format         : "DP:GT:QS", variants: "1:0/1:2\t5:2/3:8", cohortInfo: [
                        referenceAllele        : "G",
                        alternativeAlleles     : ["A", "T", "C"],
                        alleles                : ["G", "A", "T", "C"],
                        alleleCount            : [1, 1, 1, 1],
                        alleleFrequency        : [0.25, 0.25, 0.25, 0.25],
                        totalAlleleCount       : 4,
                        numberOfSamplesWithData: 2
                ]],
        ]


        def iterator = labelToData.collect { String label, List<Object> data ->
            createVCFRowForAssays(sampleAssays, data, vcfProperties[label], label)
        }.iterator()

        TabularResult highDimResult = mock TabularResult
        highDimResult.indicesList.returns(sampleAssays).stub()
        highDimResult.getRows().returns(iterator).stub()
        highDimResult.iterator().returns(iterator).stub()

        highDimResult
    }

    DataRow createVCFRowForAssays(List<AssayColumn> assays,
                                  List<Object> data,
                                  Map<String, Object> vcfProperties,
                                  String label) {
        createMockVCFRow(
                dot(assays, data, { a, b -> [a, b] })
                        .collectEntries(Closure.IDENTITY),
                vcfProperties,
                label)
    }

    private DataRow<AssayColumn, Object> createMockVCFRow(Map<AssayColumn, Object> values,
                                                          Map<String, Object> vcfProperties, String label) {
        DataRow row = mock(DataRow)
        row.label.returns(label).stub()

        row.chromosome.returns(vcfProperties.chromosome).stub()
        row.position.returns(vcfProperties.position).stub()
        row.rsId.returns(vcfProperties.rsId).stub()
        row.referenceAllele.returns(vcfProperties.referenceAllele).stub()
        row.alternativeAlleles.returns(vcfProperties.alternativeAlleles).stub()
        row.quality.returns(vcfProperties.quality).stub()
        row.filter.returns(vcfProperties.filter).stub()
        row.infoFields.returns(vcfProperties.infoFields).stub()
        row.format.returns(vcfProperties.format).stub()
        row.variants.returns(vcfProperties.variants).stub()
        row.cohortInfo.returns(vcfProperties.cohortInfo).stub()

        // Add values for each assay
        def variants = vcfProperties.variants ? vcfProperties.variants.tokenize("\t") : []
        values.eachWithIndex { entry, i ->
            row.getAt(i).returns(entry.value).stub()
            row.getAt(entry.key).returns(entry.value).stub()
            row.getOriginalSubjectData(entry.key).returns(variants.size() > i ? variants[i] : null)
        }
        row.iterator().returns(values.values().iterator()).stub()

        row
    }

}
