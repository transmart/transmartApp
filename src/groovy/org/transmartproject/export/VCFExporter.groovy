package org.transmartproject.export

import grails.util.Metadata

import javax.annotation.PostConstruct

import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.projections.Projection

class VCFExporter implements HighDimExporter {

    @Autowired
    HighDimensionResource highDimensionResourceService
    
    @Autowired
    HighDimExporterRegistry highDimExporterRegistry
    
    @PostConstruct
    void init() {
        this.highDimExporterRegistry.registerHighDimensionExporter(
                format, this )
    }
    
    @Override
    public boolean isDataTypeSupported(String dataType) {
        return dataType == "vcf"
    }

    @Override
    public String getFormat() {
        return "VCF"
    }

    @Override
    public String getDescription() {
        return "VCF formatted variants"
    }

    @Override
    public void export(TabularResult tabularResult, Projection projection,
            OutputStream outputStream) {
        export( tabularResult, projection, outputStream, { false } )
    }
            
    @Override
    public void export(TabularResult tabularResult, Projection projection,
            OutputStream outputStream, Closure isCancelled) {

        log.info("started exporting to $format ")
        def startTime = System.currentTimeMillis()
        
        if (isCancelled() ) {
            return
        }

        outputStream.withWriter( "UTF-8" ) { writer ->
            
            // Write the headers
            headers.each {
                writer << "##" << it << "\n"
            }
            
            // Write the header row for the data
            writer << "#" << getDataColumns( tabularResult ).join( "\t" ) << "\n"
            
            // Determine the order of the assays
            List<AssayColumn> assayList = tabularResult.indicesList
            
            // Start looping 
            writeloop:
            for (DataRow datarow : tabularResult) {
                // Test periodically if the export is cancelled
                if (isCancelled() ) {
                    return
                }
                
                writer << getDataForPosition( datarow, assayList ).join( "\t" ) << "\n"
            }
        }
        
        log.info("Exporting data took ${System.currentTimeMillis() - startTime} ms")
    }
            
    /**
     * Returns a list of VCF headers to be put into the output file
     * @return List of headers to be put into the output file
     */
    protected List<String> getHeaders() {
        [
            "fileformat=VCFv4.2",
            "fileDate=" + new Date().format('yyyyMMdd'),
            "source=transmart v" + Metadata.current[ "app.version" ]
        ]
    }
    
    /**
     * Returns a list with all the columns 
     * @param tabularResult
     * @return
     */
    protected List<String> getDataColumns( TabularResult tabularResult ) {
        [ "CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO", "FORMAT" ] + tabularResult.indicesList*.label
    }
    
    protected List<String> getDataForPosition( DataRow datarow, List<AssayColumn> assayList ) {
        def data = []
        
        // First add general info from the summary
        data << datarow.chromosome
        data << datarow.position
        data << datarow.rsId
        data << datarow.cohortInfo.referenceAllele
        data << datarow.cohortInfo.alternativeAlleles.join(',')
        
        // TODO: Determine whether these values still apply for the cohort selected
        data << datarow.quality
        data << datarow.filter

        // TODO: Determine which info fields can be exported (if any)
        data << ''
        data << datarow.format
        
        // Determine a list of original variants and new variants, to do translation
        List<String> originalVariants = [] + datarow.referenceAllele + datarow.alternativeAlleles
        List<String> newVariants = datarow.cohortInfo.alleles
        
        // Every line must always have a GT field in the format column
        // to follow the specification.
        def formats = datarow.format.tokenize(":")
        def genotypeIndex = formats.indexOf("GT")
        
        if (genotypeIndex == -1)
            throw new Exception( "No GT field found for position ${datarow.chromosome}:${datarow.position}" )
            
        // Now add the data for each assay
        for (AssayColumn assay : assayList) {
            // Retrieve data for the current assay from the datarow
            Map<String, String> assayData = datarow[assay]
    
            if (assayData == null) {
                data << "."
                continue
            }
    
            // Convert the old indices (e.g. 1 and 0) to the
            // new indices that were computed
            def convertedIndices = []
            ["allele1", "allele2"].each {
                if( assayData.containsKey( it ) ) {
                    int oldIndex = assayData[ it ]
                    
                    if( oldIndex != null ) {
                        String variant = originalVariants[ oldIndex ]
                        int newIndex = newVariants.indexOf( variant )
                        
                        convertedIndices << newIndex
                    }
                }
            }
            
            // Restore the original subject data for this subject
            def originalData = datarow.getOriginalSubjectData( assay )
            def newData
            
            if (originalData) {
                newData = originalData.tokenize(":")
            } else {
                // Generate data to state that we don't know
                newData = [ "." ].times( formats.size() )
            }

            // Put the computed genotype into the originaldata
            // TODO: Take phase of the original read into account (unphased or phased, / or |)
            newData[ genotypeIndex ] = convertedIndices.join( "/" )
             
            data << newData.join(":") 
        }

        data
    }
    
    @Override
    public String getProjection() {
        "cohort"
    }

}
