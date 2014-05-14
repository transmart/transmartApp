package org.transmartproject.export

import grails.test.mixin.*

import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.dataquery.highdim.vcf.VcfCohortInfo

import spock.lang.*

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
        assert exporter.isDataTypeSupported( "vcf" )
        
        assert !exporter.isDataTypeSupported( "other" )
        assert !exporter.isDataTypeSupported( null )
     }
    
    @Test
    void "test whether a cancelled export doesn't produce output"() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        
        def tabularResult = mock(TabularResult)
        def projection = mock(Projection)
        
        play {
            
            exporter.export( tabularResult, projection, outputStream, { true } )
            
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
            exporter.export( tabularResult, projection, outputStream )
            
            // Assert we have at least some text, in UTF-8 encoding
            String output = outputStream.toString("UTF-8")
            assert output
            
            // We expect 4 lines: 1 header line, two lines  for assay 1, and one line for assay 2
            List lines = output.readLines()
            assert lines.size() == 7
            
            expectHeader( lines[0..2] )
            expectDataHeader( lines[3] )
            
            // Check first chromosome
            assert lines[4] == [
                    "1", "100", "rs0010", "G", "", "50", "PASS", "", "GT",
                    "0/0", "0/0" ].join( "\t" )
                
            assert lines[5] == [
                    "2", "190", ".", "A", "G", "90", "q10", "", "GT",   // Inversion of reference and alternatives
                    "1/0", "0/0" ].join( "\t" )
                
            assert lines[6] == [
                    "X", "190", ".", "G", "A", "90", "PASS", "", "GT",    
                    "0", "1" ].join( "\t" )
            
        }
     }
    
    
    protected expectHeader( List<String> headerLines ) {
        assert headerLines[0].equals( "##fileformat=VCFv4.2" )
        assert headerLines[1].equals( "##fileDate=" + new Date().format('yyyyMMdd') )
        assert headerLines[2].startsWith( "##source=transmart" )
    }
    
    protected expectDataHeader( String dataHeaderLine ) {
        assert dataHeaderLine == "#" + [
            "CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO", "FORMAT",
            "assay_1", "assay_2" ].join( "\t" )

    }
    
    protected createMockVCFTabularResult() {
        // Setup tabularResult and projection to test with
        List<AssayColumn> sampleAssays = createSampleAssays(2)
        Map<String, List<Object>> labelToData = [
            "row1": [[ "allele1": 0, "allele2": 0 ], [ "allele1": 0, "allele2": 0 ]],
            "row2": [[ "allele1": 0, "allele2": 1 ], [ "allele1": 1, "allele2": 1 ]],
            "row3": [[ "allele1": 0 ], [ "allele1": 1 ]],   // X chromosome
        ]
        
        Map<String,Map> vcfProperties = [
            "row1": [ chromosome: 1, position: 100, rsId: "rs0010", 
                    referenceAllele: "G", alternativeAlleles: [], 
                    quality: 50, filter: "PASS", cohortInfo: [
                            referenceAllele: "G",
                            alternativeAlleles: [],
                            alleles: [ "G" ]
                    ]],
            "row2": [ chromosome: 2, position: 190, rsId: ".", 
                    referenceAllele: "G", alternativeAlleles: [ "A" ], 
                    quality: 90, filter: "q10", cohortInfo: [
                            referenceAllele: "A",
                            alternativeAlleles: [ "G" ],
                            alleles: [ "A", "G" ]
                    ]],
            "row3": [ chromosome: "X", position: 190, rsId: ".", 
                    referenceAllele: "G", alternativeAlleles: [ "A" ], 
                    quality: 90, filter: "PASS", cohortInfo: [
                            referenceAllele: "G",
                            alternativeAlleles: [ "A" ],
                            alleles: [ "G", "A" ]
                    ]],
        ]

        
        def iterator = labelToData.collect { String label, List<Object> data ->
            createVCFRowForAssays(sampleAssays, data, vcfProperties[ label ], label)
        }.iterator()
                
        TabularResult highDimResult = mock TabularResult
        highDimResult.indicesList.returns(sampleAssays).stub()
        highDimResult.getRows().returns(iterator).stub()
        highDimResult.iterator().returns(iterator).stub()
        
        highDimResult
    }
    
    DataRow createVCFRowForAssays(List<AssayColumn> assays,
            List<Object> data,
            Map<String,Object> vcfProperties, 
            String label) {
        createMockVCFRow(
                dot(assays, data, {a, b -> [ a, b ]})
                         .collectEntries(Closure.IDENTITY),
                vcfProperties,
                label)
    }
            
    private DataRow<AssayColumn, Object> createMockVCFRow(Map<AssayColumn, Object> values,
            Map<String,Object> vcfProperties, String label) {
        DataRow row = mock(DataRow)
        row.label.returns(label).stub()
        
        row.chromosome.returns( vcfProperties.chromosome ).stub()
        row.position.returns( vcfProperties.position ).stub()
        row.rsId.returns( vcfProperties.rsId ).stub()
        row.referenceAllele.returns( vcfProperties.referenceAllele ).stub()
        row.alternativeAlleles.returns( vcfProperties.alternativeAlleles ).stub()
        row.quality.returns( vcfProperties.quality ).stub()
        row.filter.returns( vcfProperties.filter ).stub()
        row.cohortInfo.returns(vcfProperties.cohortInfo).stub()

        // Add values for each assay
        
        values.eachWithIndex { entry, i ->
            row.getAt(i).returns(entry.value).stub()
        }
        values.keySet().each { column ->
            row.getAt(column).returns(values[column]).stub()
        }
        row.iterator().returns(values.values().iterator()).stub()
        
        row
    }

}
