import grails.converters.JSON
import org.transmartproject.core.querytool.ConstraintByOmicsValue
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

        def info = ConstraintByOmicsValue.markerInfo[platform.markerType]
        if (info == null) {
            render template: template, model: [error: "Marker type unsupported: $platform.markerType"]
            return
        }

        def model = [gpl_id: platform.id,
                     marker_type: platform.markerType,
                     selector_name: "Gene",
                     filter_type: info.filter_type,
                     filter: filter,
                     projections: info.allowed_projections.collectEntries {[it.name(), it.value]}]

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
                      filter_type: ConstraintByOmicsValue.markerInfo[platform.markerType]?.filter_type,
                      filter: filter,
                      concept_key: concept_key]

        if (result.filter_type == "") result['error'] = "Unrecognized marker type " + platform.markerType
        render result as JSON
    }
}