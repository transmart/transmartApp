import com.google.common.collect.ImmutableSet
import com.google.common.collect.Lists
import grails.converters.JSON
import grails.util.Holders
import org.hibernate.Query
import org.hibernate.ScrollMode
import org.hibernate.Session
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.assay.SampleType
import org.transmartproject.core.dataquery.assay.Timepoint
import org.transmartproject.core.dataquery.assay.TissueType
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.BioMarkerDataRow
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.Platform
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.db.dataquery.highdim.HighDimensionResourceService
import org.transmartproject.db.dataquery.highdim.mrna.DeSubjectMicroarrayDataCoreDb
import org.transmartproject.db.dataquery.highdim.projections.GenericProjection

/**
 * Created by jan on 12/30/13.
 */
class TestClass {
    static String version = "version 14"

    static Object getBean(bean) {
        Holders.grailsApplication.mainContext.getBean(bean)
    }

    static doIt() {
        println(version)
        def hdrs = Holders.grailsApplication.mainContext.getBean('highDimensionResourceService')
        def mrnaResource = hdrs.getSubResourceForType('mrna')
        //def projection = mrnaResource.createProjection([:], Projection.ZSCORE_PROJECTION)
        def projection = new GenericProjection(DeSubjectMicroarrayDataCoreDb)

        def trialNameConstraint = mrnaResource.createAssayConstraint(
                        AssayConstraint.TRIAL_NAME_CONSTRAINT,
                        name: 'GSE8581')

        def dqresult = mrnaResource.retrieveData([trialNameConstraint], [], projection)



    }

    static query() {
        Session session = getBean('sessionFactory').currentSession
//        def q = session.createQuery("""
//            select
//                data,
//                data.probe,
//                data.assay.patient.sourcesystemCd,
//                data.assay.sampleTypeName, data.assay.sampleTypeCd, data.assay.timepointName, data.assay.tissueTypeName, data.assay.trialName
//            from DeSubjectMicroarrayDataCoreDb as data,
//                QtPatientSetCollection as sc
//            where sc.patient = data.assay.patient
//                and data.assay.platform.id = data.probe.gplId
//                and data.assay.trialName = 'GSE8581'
//                and data.assay.platform.id = 'GPL570'
//                and sc.resultInstance.id = 22967
//            """).setReadOnly(true).setCacheable(false) //.scroll(ScrollMode.FORWARD_ONLY)
        Query query = session.createQuery("""
                select
                    data.assay.id, data.intensity, data.zscore,

                    data.assay.patient.sourcesystemCd,
                    data.assay.sampleTypeName, data.assay.timepointName, data.assay.tissueTypeName, data.assay.platform.id
                from DeSubjectMicroarrayDataCoreDb as data,
                    QtPatientSetCollection as sc
                where sc.patient = data.assay.patient
                    and data.assay.platform.id = data.probe.gplId
                    and data.assay.platform.id in (:platform)
                    and data.assay.trialName = :trialName
                    and sc.resultInstance.id = :resultInstanceId
                """).setReadOnly(true).setCacheable(false).setFetchSize(5000)
        query.setParameter('trialName', 'GSE8581').
                setParameterList('platform', 'GPL570').
                setParameter('resultInstanceId', 22967 as long)

        query.scroll(ScrollMode.FORWARD_ONLY)

    }

    static HighDimensionResource highDimensionResource = getBean(HighDimensionResource)
    static HighDimensionResourceService highDimensionResourceService = getBean(HighDimensionResourceService)

    static findSingleDataType(List<String> conceptpaths) {

        def dataTypeConstraint = highDimensionResourceService.createAssayConstraint(
                AssayConstraint.DISJUNCTION_CONSTRAINT,
                subconstraints:
                        [(AssayConstraint.ONTOLOGY_TERM_CONSTRAINT): conceptpaths.collect {[concept_key: it]}])

        def datatypes = highDimensionResourceService.getSubResourcesAssayMultiMap([dataTypeConstraint]).keySet()*.dataType

        if (datatypes.size() > 1) {
            throw new IllegalArgumentException("The provided concepts must have the same type, but they have types \"${datatypes.join(', ')}\"")
        }

        datatypes[0]
    }

    static apiquery() {

        // The input: a resultInstanceId and a list of concept paths

        def resultInstanceId = 23306  //22967
//        def trialName = 'GSE8581'
//        def gplIds = ['GPL570']
//        gplIds = [570]
//        resultInstanceId = 22967

        def conceptPaths = [/\\Public Studies\Public Studies\GSE8581\MRNA\Biomarker Data\Affymetrix Human Genome U133A 2.0 Array\Lung/]



        HighDimensionDataTypeResource dataTypeResource = highDimensionResource.getSubResourceForType(findSingleDataType(conceptPaths))

        def dataProperties = dataTypeResource.dataProperties as ArrayList
        def rowProperties = dataTypeResource.rowProperties as ArrayList


        def assayconstraints = []
        def dataconstraints = []
        def projection = dataTypeResource.createProjection(Projection.GENERIC_PROJECTION)

        assayconstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.PATIENT_SET_CONSTRAINT,
                result_instance_id: resultInstanceId)

        assayconstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.DISJUNCTION_CONSTRAINT,
                subconstraints:
                    [(AssayConstraint.ONTOLOGY_TERM_CONSTRAINT): conceptPaths.collect {[concept_key: it]}])

        def tabularResult = dataTypeResource.retrieveData(assayconstraints, dataconstraints, projection)

        def assayList = tabularResult.indicesList

        def fixedfields = ['patient id', 'sample', 'assay id', 'value', 'zscore', 'log2e', 'probe id', 'gene symbol', 'gene id']

        int count = 0
        for (BioMarkerDataRow<Map<String, String>> datarow : tabularResult) {
            for (AssayColumn assay : assayList) {
                count++
                if (count < 50) continue
                if (count > 75) return [assay, datarow]
                //println "$assay: ${row[assay]}"
                Map data = datarow[assay]
                Map<String, String> row = datarow.associatedData

                String assayId =        assay.id
                String rawIntensity =   data.rawIntensity
                String zscore =         data.zscore
                String logIntensity =   data.logIntensity
                String probeId =        row.probe
                String geneId =         row.geneId
                String geneSymbol =     row.geneSymbol
                String patientId =      assay.patientInTrialId
                String sampleTypeName = assay.sampleType.label
                String timepointName =  assay.timepoint.label
                String tissueTypeName = assay.tissueType.label
                String platform =       assay.platform.id

//                        [
//                        assay.id, data.rawIntensity, data.zscore, data.logIntensity, row.probe, '<geneId>',
//                        row.geneSymbol, assay.patientInTrialId, assay.sampleType.label, assay.timepoint.label, assay.tissueType.label /*<assay.tissueTypeName>*/,
//                        assay.platform.id /*<platform>*/]

                println([assayId, rawIntensity, zscore, logIntensity, probeId, geneId, geneSymbol, patientId,
                        sampleTypeName, timepointName, tissueTypeName, platform].join(' '))
            }
        }
    }

    static newquery() {
        getBean('geneExpressionDataService').exportData()
    }

    static toList(it) {
        Lists.newArrayList(it)
    }

    static test() {
        ImmutableSet foo = [1,2,3]
        foo
    }

    private static platformToMap(Platform p) {
        Platform.metaClass.properties.
                collect { it.name }.
                minus(['class', 'template']).
                collectEntries {
                    [  it, p."$it" ]
                }
    }

    static nodedetails() {
        def constraints = []

        constraints << highDimensionResourceService.createAssayConstraint(
                AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                concept_key: /\\Public Studies\Public Studies\GSE8581\MRNA\Biomarker Data\Affymetrix Human Genome U133A 2.0 Array\Lung /.trim())

        highDimensionResourceService.getSubResourcesAssayMultiMap(constraints).keySet()*.dataType
    }


    static dataexport() {

    }

}
