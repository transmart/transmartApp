package com.recomdata.transmart.data.export

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.projections.AllDataProjection
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.export.HighDimExporter
import org.transmartproject.export.TabSeparatedExporter

class HighDimExportService {

    /**
     * This are the headers that are used in the tab-separated export files for each field type. Before export, they are
     * captialised.
     */
    Map dataFieldHeaders = [
            rawIntensity: 'value',
            intensity: 'value',
            value: 'value',
            logIntensity: 'log2e',
            zscore: 'zscore'
    ]
    Map rowFieldHeaders = [
            geneSymbol: 'gene symbol',
            geneId: 'gene id',
            mirnaId: 'mirna id',
            peptide: 'peptide sequence',
            antigenName: 'analyte name',
            uniprotId: 'uniprot id',
            uniprotName: 'uniprot name',
            transcriptId: 'transcript id',
            probe: 'probe id',
            probeId: 'probe id'
    ]

    def highDimensionResourceService
    // FIXME: jobResultsService lives in Rmodules, so this is probably not a dependency we should have here
    def jobResultsService

    def exportHighDimData(Map args) {
        String jobName =                args.jobName
        String dataType =               args.dataType
        boolean splitAttributeColumn =  args.get('splitAttributeColumn', false)
        def resultInstanceId =          args.resultInstanceId
        List<String> conceptPaths =     args.conceptPaths
        String studyDir =               args.studyDir


        if (jobIsCancelled(jobName)) {
            return null
        }

        HighDimensionDataTypeResource dataTypeResource = highDimensionResourceService.getSubResourceForType(dataType)

        def assayconstraints = []

        assayconstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.PATIENT_SET_CONSTRAINT,
                result_instance_id: resultInstanceId)

        assayconstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.DISJUNCTION_CONSTRAINT,
                subconstraints:
                        [(AssayConstraint.ONTOLOGY_TERM_CONSTRAINT): conceptPaths.collect {[concept_key: it]}])

        AllDataProjection projection = dataTypeResource.createProjection(Projection.ALL_DATA_PROJECTION)

        File outputFile = new File(studyDir, dataType+'.txt')
        String fileName = outputFile.getAbsolutePath()

        TabularResult<AssayColumn, DataRow<Map<String, String>>> tabularResult =
                dataTypeResource.retrieveData(assayconstraints, [], projection)
        try {
            // Start exporting
            HighDimExporter exporter = new TabSeparatedExporter()
            outputFile.withOutputStream { outputStream ->
                exporter.export tabularResult, projection, outputStream
            }
        } finally {
            tabularResult.close()
        }

        return [outFile: fileName]
    }

    def boolean jobIsCancelled(jobName) {
        if (jobResultsService[jobName]["Status"] == "Cancelled") {
            log.warn("${jobName} has been cancelled")
            return true
        }
        return false
    }
}
