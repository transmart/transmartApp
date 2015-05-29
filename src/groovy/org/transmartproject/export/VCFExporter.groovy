package org.transmartproject.export

import grails.util.Metadata
import groovy.util.logging.Log4j
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.projections.Projection

import javax.annotation.PostConstruct

@Log4j
class VCFExporter implements HighDimExporter {
    /**
     * List of info fields that can be exported without any change.
     * This list should only include fields for which the value is the
     * same for each subset of the assays.
     * @see http://www.1000genomes.org/wiki/Analysis/Variant%20Call%20Format/vcf-variant-call-format-version-41
     */
    final List<String> INFOFIELD_WHITELIST = [
            'AA', // ancestral allele
            'DB', // dbSNP membership
            'END', // end position of the variant described in this record (for use with symbolic alleles)
            'H2', // membership in hapmap2
            'H3', // membership in hapmap3
            'SOMATIC', // indicates that the record is a somatic mutation, for cancer genomics
            'VALIDATED', // validated by follow-up experiment
            '1000G', // membership in 1000 Genomes
    ]

    final static String EMPTY_VALUE = '.'

    @Autowired
    HighDimensionResource highDimensionResourceService

    @Autowired
    HighDimExporterRegistry highDimExporterRegistry

    @PostConstruct
    void init() {
        this.highDimExporterRegistry.registerHighDimensionExporter(
                format, this)
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
                       Closure<OutputStream> newOutputStream) {
        export(tabularResult, projection, newOutputStream, { false })
    }

    @Override
    public void export(TabularResult tabularResult, Projection projection,
                       Closure<OutputStream> newOutputStream, Closure<Boolean> isCancelled) {

        log.info("started exporting to $format ")
        def startTime = System.currentTimeMillis()

        if (isCancelled()) {
            return
        }

        newOutputStream("data", format).withWriter("UTF-8") { writer ->

            // Write the headers
            headers.each {
                writer << "##" << it << "\n"
            }

            // Write the header row for the data
            writer << "#" << getDataColumns(tabularResult).join("\t") << "\n"

            // Determine the order of the assays
            List<AssayColumn> assayList = tabularResult.indicesList

            // Start looping 
            for (DataRow datarow : tabularResult) {
                // Test periodically if the export is cancelled
                if (isCancelled()) {
                    return
                }

                writer << getDataForPosition(datarow, assayList).join("\t") << "\n"
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
                "source=transmart v" + Metadata.current["app.version"]
        ]
    }

    /**
     * Returns a list with all the columns 
     * @param tabularResult
     * @return
     */
    protected List<String> getDataColumns(TabularResult tabularResult) {
        ["CHROM", "POS", "ID", "REF", "ALT", "QUAL", "FILTER", "INFO", "FORMAT"] + tabularResult.indicesList*.label
    }

    protected List<String> getDataForPosition(DataRow datarow, List<AssayColumn> assayList) {
        def data = []

        // First add general info from the summary
        data << datarow.chromosome
        data << datarow.position
        data << datarow.rsId
        data << datarow.cohortInfo.referenceAllele
        data << (datarow.cohortInfo.alternativeAlleles.join(',') ?: EMPTY_VALUE)

        // TODO: Determine whether these values still apply for the cohort selected
        data << datarow.quality
        data << datarow.filter

        // TODO: Determine which info fields can be exported (if any)
        data << getInfoFields(datarow).collect {
            it.key + (it.value != true ? '=' + it.value : '')
        }.join(";")
        data << datarow.format

        // Determine a list of original variants and new variants, to do translation
        List<String> originalVariants = [] + datarow.referenceAllele + datarow.alternativeAlleles
        List<String> newVariants = [] + datarow.referenceAllele + datarow.cohortInfo.alternativeAlleles

        // Every line must always have a GT field in the format column
        // to follow the specification.
        List<String> formats = datarow.format.tokenize(":")
        int genotypeIndex = formats.indexOf("GT")

        if (genotypeIndex == -1)
            throw new Exception("No GT field found for position ${datarow.chromosome}:${datarow.position}")

        // Now add the data for each assay
        for (AssayColumn assay : assayList) {
            data << getSubjectData(datarow, assay, originalVariants, newVariants, formats, genotypeIndex).join(":")
        }

        data
    }

    /**
     * Returns a map with info fields and their values for this row
     * @param datarow
     * @return
     */
    protected Map getInfoFields(DataRow datarow) {
        Map<String, String> infoFields = [:]

        // Add info fields from the whitelist
        INFOFIELD_WHITELIST.each { infoField ->
            if (datarow.infoFields[infoField] != null) {
                infoFields[infoField] = datarow.infoFields[infoField]
            }
        }

        // Compute other info fields. Counts include the reference
        // variant, but that should not be included in the VCF file

        // 'AC' : allele count in genotypes, for each ALT allele, in the 
        //        same order as listed
        if (datarow.cohortInfo.alternativeAlleles.size() > 0)
            infoFields["AC"] = datarow.cohortInfo.alleleCount.tail().join(",")

        // 'AF' : allele frequency for each ALT allele in the same order 
        //        as listed: use this when estimated from primary data, 
        //        not called genotypes
        if (datarow.cohortInfo.alternativeAlleles.size() > 0)
            infoFields["AF"] = datarow.cohortInfo.alleleFrequency.tail().join(",")

        // 'AN' : total number of alleles in called genotypes
        infoFields["AN"] = datarow.cohortInfo.totalAlleleCount

        // 'NS' : Number of samples with data
        infoFields["NS"] = datarow.cohortInfo.numberOfSamplesWithData

        infoFields
    }

    /**
     * Returns a list of subject fields that can be put into the VCF file
     * @param datarow
     * @param assay
     * @param originalVariants List of variants from the original VCF file.
     *                          Includes the reference.
     * @param newVariants New list of variants that are exported to the
     *                          VCF file. Includes the reference.
     * @param formats List of format fields that should be present
     *                          for this subject.
     * @param genotypeIndex Index within the formats list of the GT field
     * @return
     */
    protected List getSubjectData(DataRow datarow, AssayColumn assay,
                                  List<String> originalVariants, List<String> newVariants,
                                  List<String> formats, int genotypeIndex) {

        // Retrieve data for the current assay from the datarow
        Map<String, String> assayData = datarow[assay]

        if (assayData == null) {
            return [EMPTY_VALUE]
        }

        // Convert the old indices (e.g. 1 and 0) to the
        // new indices that were computed
        def convertedIndices = []
        ["allele1", "allele2"].each {
            if (assayData.containsKey(it)) {
                def oldIndex = assayData[it]

                if (oldIndex != null) {
                    String variant = originalVariants[oldIndex]
                    int newIndex = newVariants.indexOf(variant)

                    convertedIndices << newIndex
                } else {
                    convertedIndices << EMPTY_VALUE
                }
            }
        }

        // Restore the original subject data for this subject
        def originalData = datarow.getOriginalSubjectData(assay)
        def newData

        if (originalData) {
            newData = originalData.tokenize(":")
        } else {
            // Generate data to state that we don't know
            newData = (1..formats.size()).collect { EMPTY_VALUE }
        }

        // Put the computed genotype into the originaldata
        // TODO: Take phase of the original read into account (unphased or phased, / or |)
        newData[genotypeIndex] = convertedIndices.join("/")

        newData
    }

    @Override
    public String getProjection() {
        "cohort"
    }

}
