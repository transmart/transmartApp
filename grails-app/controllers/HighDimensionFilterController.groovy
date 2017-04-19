import grails.converters.JSON
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping
/**
 * Author: Denny Verbeeck (dverbeec@its.jnj.com)
 */
class HighDimensionFilterController {

    def i2b2HelperService
    def highDimensionResourceService

    /**
     * Render the filter dialog.
     * @param gpl_id The GPL ID of the platform for this high dimensional dataset
     * @param filter Should be "true" for filter dialog (cohort selection), "false" for selection dialog (when dropping
     *        into summary statistics or grid view)
     * @param concept_key The concept key of the high dimensional concept
     */
    def filterDialog = {
        def template = "highDimensionFilterDialog"
        def gpl_id = params.gpl_id ?: null
        def filter = params.filter ?: true
        def concept_key = params.concept_key ?: null
        if (gpl_id == null) {
            render template: template, model: [error: "No GPL ID provided."]
            return
        }

        if (concept_key == null) {
            render template: template, model: [error: "No concept key provided"]
            return
        }

        def platform = DeGplInfo.findById gpl_id ?: null

        if (platform == null) {
            render template: template, model: [error: "Unknown GPL ID provided."]
            return
        }

        def resource = highDimensionResourceService.getHighDimDataTypeResourceFromConcept(concept_key)

        if (resource.dataTypeName == 'vcf') {
            render "Small Variant data is not supperted yet, stay tuned!"
        }
        else {
            def model = [gpl_id               : platform.id,
                         marker_type          : platform.markerType,
                         filter_type          : resource.getHighDimensionFilterType(),
                         searchable_properties: resource.getSearchableAnnotationProperties().collectEntries {
                             [it, searchableAnnotationPropertiesDictionary.get(it, it)]
                         },
                         filter               : filter,
                         projections          : resource.getSearchableProjections().collectEntries {
                             [it, Projection.prettyNames.get(it, it)]
                         }]

            render(template: template, model: model)
        }
    }

    /**
     * Get general information on the high dimensional filter.
     * @param filter Should be "true" for filter dialog (cohort selection), "false" for selection dialog (when dropping
     *        into summary statistics or grid view)
     * @param concept_key The concept key of the high dimensional concept
     * @return JSON object with following properties: platform, auto_complete_source, filter_type,
     *         filter, concept_key, concept_code
     */
    def filterInfo = {
        def template = "highDimensionFilterDialog"
        def concept_key = params.concept_key ?: null
        def filter = params.filter ?: true
        if (concept_key == null) {
            render template: template, model: [error: "No concept key provided."]
            return
        }
        def concept_code = i2b2HelperService.getConceptCodeFromKey(concept_key)

        def platform = DeSubjectSampleMapping.findByConceptCode(concept_code).getPlatform()
        def resource = highDimensionResourceService.getHighDimDataTypeResourceFromConcept(concept_key)

        def result = [platform: platform,
                      auto_complete_source: "/transmart/highDimensionFilter/searchAutoComplete", //?concept_key=" + URLEncoder.encode(concept_key, "UTF-8"),
                      filter_type: resource.getHighDimensionFilterType(),
                      filter: filter,
                      concept_key: concept_key,
                      concept_code: concept_code]

        if (result.filter_type == "") result['error'] = "Unrecognized marker type " + platform.markerType
        render result as JSON
    }

    def searchAutoComplete = {
        if (params.concept_key == "" || params.term == "" || params.search_property == "") {
            return [] as JSON
        }
        //def result = omicsQueryService.getSearchResults(params.term, params.concept_key, params.search_property)

        def resource = highDimensionResourceService.getHighDimDataTypeResourceFromConcept(params.concept_key)
        def concept_code = i2b2HelperService.getConceptCodeFromKey(params.concept_key)
        def symbols = resource.searchAnnotation(concept_code, params.term, params.search_property)
        symbols.collect {[label: it]}

        render symbols as JSON
    }

    /**
     * Dictonary to convert from searchable annotation property names to a format suitable for user display
     */
    static Map<String, String> searchableAnnotationPropertiesDictionary =
            ['geneSymbol': 'Gene Symbol',
             'probeId': 'Probe ID',
             'cytoband': 'Cytoband',
             'name': ' Region Name',
             'hmdbId': 'HMDB ID',
             'biochemicalName': 'Biochemical Name',
             'probeId': 'Probe ID',
             'mirnaId': 'miRNA ID',
             'uniprotName': 'Uniprot Name',
             'peptide': 'Peptide',
             'antigenName': 'Antigen Name',
             'annotationId': 'Annotation ID',
             'chromosome': 'Chromosome',
             'position': 'Position',
             'rsId': 'RSID',
             'referenceAllele': 'Reference Allele',
             'detector': 'miRNA Symbol',
             'transcriptId': 'Transcript ID']
}