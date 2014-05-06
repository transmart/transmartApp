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

        // Add constraints to filter the output
        def assayconstraints = []

        assayconstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.PATIENT_SET_CONSTRAINT,
                result_instance_id: resultInstanceId)

        assayconstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.DISJUNCTION_CONSTRAINT,
                subconstraints:
                        [(AssayConstraint.ONTOLOGY_TERM_CONSTRAINT): conceptPaths.collect {[concept_key: it]}])

        // Setup class to export the data
        HighDimExporter exporter = new TabSeparatedExporter()
        Projection projection = dataTypeResource.createProjection( exporter.projection )

        File outputFile = new File(studyDir, dataType + '.txt')
        String fileName = outputFile.getAbsolutePath()

        // Retrieve the data itself
        TabularResult<AssayColumn, DataRow<Map<String, String>>> tabularResult =
                dataTypeResource.retrieveData(assayconstraints, [], projection)

        // Start exporting
        try {
            outputFile.withOutputStream { outputStream ->
                exporter.export tabularResult, projection, outputStream, { jobIsCancelled(jobName) }
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
