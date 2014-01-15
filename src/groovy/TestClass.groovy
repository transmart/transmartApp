import com.google.common.collect.Lists
import grails.util.Holders
import org.hibernate.Query
import org.hibernate.ScrollMode
import org.hibernate.Session
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.HighDimensionResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.db.dataquery.highdim.mrna.DeSubjectMicroarrayDataCoreDb
import org.transmartproject.db.dataquery.highdim.projections.GenericProjection

/**
 * Created by jan on 12/30/13.
 */
class TestClass {
    static String version = "version 13"

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

    static apiquery() {

        HighDimensionDataTypeResource dataTypeResource = highDimensionResource.getSubResourceForType('mrna')

        def resultInstanceId = 23306  //22967
        def trialName = 'GSE8581'
        def gplIds = ['GPL570']
        gplIds = [570]
//        resultInstanceId = 22967

        def conceptPath = /\\Public Studies\GSE8581\MRNA\Biomarker Data\Affymetrix Human Genome U133A 2.0 Array\Lung\ /.trim()
        conceptPath = /\\Public Studies\Public Studies\GSE8581\MRNA\Biomarker Data\Affymetrix Human Genome U133A 2.0 Array\Lung /.trim()


//        def ontologyTerm = /\\Public Studies\Public Studies\GSE8581\MRNA\Biomarker Data\Affymetrix Human Genome U133A 2.0 Array\Lung\ /.trim()

        def assayconstraints = []
        def dataconstraints = []
        def projection = new GenericProjection(DeSubjectMicroarrayDataCoreDb)
        //dataTypeResource.createProjection([:], 'zscore')

//        assayconstraints << dataTypeResource.createAssayConstraint(
//                AssayConstraint.TRIAL_NAME_CONSTRAINT,
//                name: trialName)
//
//        assayconstraints << dataTypeResource.createAssayConstraint(
//                AssayConstraint.ASSAY_ID_LIST_CONSTRAINT,
//                ids: gplIds)

        assayconstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.PATIENT_SET_CONSTRAINT,
                result_instance_id: resultInstanceId)

        assayconstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                concept_key: conceptPath)

//        assayconstraints << dataTypeResource.createAssayConstraint(
//                        AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
//                        concept_key: ontologyTerm)

        def tabularResult = dataTypeResource.retrieveData(assayconstraints, dataconstraints, projection)

        def assayList = tabularResult.indicesList

        int count = 0
        for (DataRow row : tabularResult) {
            for (AssayColumn assay : assayList) {
                count++
                if (count < 55) continue
                if (count > 75) return [assay, row]
                //println "$assay: ${row[assay]}"
                Map data = row[assay]

                /* geneId: option 1: extend ProbeRow for each datatype, option 2: add to projection*/
                String assayId =        assay.id
                String rawIntensity =   data.rawIntensity
                String zscore =         data.zscore
                String logIntensity =   data.logIntensity
                String probeId =        row.probe
                String geneId =         '<geneId>'
                String geneSymbol =     row.geneSymbol
                String sourcesystemCd = assay.patientInTrialId
                String sampleTypeName = assay.sampleType.label
                String timepointName =  assay.timepoint.label
                String tissueTypeName = assay.tissueType.label
                String platform =       assay.platform.id

//                        [
//                        assay.id, data.rawIntensity, data.zscore, data.logIntensity, row.probe, '<geneId>',
//                        row.geneSymbol, assay.patientInTrialId, assay.sampleType.label, assay.timepoint.label, assay.tissueType.label /*<assay.tissueTypeName>*/,
//                        assay.platform.id /*<platform>*/]

                println([assayId, rawIntensity, zscore, logIntensity, probeId, geneId, geneSymbol, sourcesystemCd,
                        sampleTypeName, timepointName, tissueTypeName, platform].join(' '))
            }
        }
    }

    static toList(it) {
        Lists.newArrayList(it)
    }

}
