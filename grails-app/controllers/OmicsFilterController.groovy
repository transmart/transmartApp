import grails.converters.JSON
import org.transmartproject.db.dataquery.highdim.DeGplInfo
import org.transmartproject.db.dataquery.highdim.DeSubjectSampleMapping

/**
 * Created by dverbeec on 31/08/2015.
 */
class OmicsFilterController {

    def i2b2HelperService

    def filterDialog = {
        def template = "omicsFilterDialog"
        def gpl_id = params.gpl_id ?: null
        def filter = params.filter ?: true
        if (gpl_id == null) {
            render template: template, model: [error: "No GPL ID provided."]
            return
        }

        def platform = DeGplInfo.findById gpl_id ?: null

        if (platform == null) {
            render template: template, model: [error: "Unknown GPL ID provided."]
            return
        }

        def model = [gpl_id: platform.id,
                     marker_type: platform.markerType,
                     selector_name: "Gene",
                     filter_type: filterType(platform.markerType),
                     filter: filter]

        switch (platform.markerType) {
            case "Gene Expression":
            case "MIRNA_QPCR":
                model['projections'] = [raw_intensity: 'Raw', log_intensity: 'Log', zscore: 'Z-Score']
                break
            case "RNASEQ_RCNT":
                model['projections'] = [normalized_readcount: 'Normalized', log_normalized_readcount: 'Log Normalized', zscore: 'Z-Score']
                break
            case "PROTEOMICS":
                model['projections'] = [intensity: 'Raw', log_intensity: 'Log', zscore: 'Z-Score']
                break
            default:
                model['error'] = "No information for marker type " + platform.getMarkerType()
        }

        render(template: template, model: model)
    }

    def filterInfo = {
        def template = "omicsFilterDialog"
        def concept_key = params.concept_key ?: null
        def filter = params.filter ?: true
        if (concept_key == null) {
            render template: template, model: [error: "No concept key provided."]
            return
        }

        def platform = DeSubjectSampleMapping.findByConceptCode(i2b2HelperService.getConceptCodeFromKey(concept_key)).getPlatform()
        def result = [platform: platform,
                      auto_complete_source: "/transmart/omicsPlatformSearch/searchAutoComplete?gplid=" + URLEncoder.encode(platform.id, "UTF-8"),
                      filter_type: filterType(platform.markerType),
                      filter: filter,
                      concept_key: concept_key]

        if (result.filter_type == "") result['error'] = "Unrecognized marker type " + platform.markerType
        render result as JSON
    }

    private def filterType(String markerType) {
        switch (markerType) {
            case "Gene Expression":
            case "MIRNA_QPCR":
            case "RNASEQ_RCNT":
            case "PROTEOMICS":
                return "numeric"
            case "Chromosomal":
                return "acgh"
                break
            case "VCF":
                return "vcf"
                break
            default:
                log.error "Unrecognized marker type " + markerType
                return ""
        }
    }
}