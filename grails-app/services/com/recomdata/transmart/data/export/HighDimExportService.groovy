package com.recomdata.transmart.data.export

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.export.HighDimExporter

class HighDimExportService {

    def highDimensionResourceService
    def highDimExporterRegistry
    def queriesResourceService

    // FIXME: jobResultsService lives in Rmodules, so this is probably not a dependency we should have here
    def jobResultsService

    /**
     * - args.conceptPaths (optional) - collection with concept keys (\\<tableCode>\<conceptFullName>) denoting data
     * nodes for which to export data for.
     * - args.resultInstanceId        - id of patient set for denoting patients for which export data for.
     * - args.studyDir (File)         - directory where to store exported files
     * - args.format                  - data file format (e.g. "TSV", "VCF"; see HighDimExporter.getFormat())
     * - args.dataType                - data format (e.g. "mrna", "acgh"; see HighDimensionDataTypeModule.getName())
     * - args.jobName                 - name of the current export job to check status whether we need to break export.
     */
    def exportHighDimData(Map args) {
        Long resultInstanceId = args.resultInstanceId as Long
        List<String> conceptKeys = args.conceptKeys

        if (jobIsCancelled(args.jobName)) {
            return null
        }

        def fileNames = []
        HighDimensionDataTypeResource dataTypeResource = highDimensionResourceService.getSubResourceForType(args.dataType)
        if (!conceptKeys) {
            def queryResult = queriesResourceService.getQueryResultFromId(resultInstanceId)
            def ontologyTerms = dataTypeResource.getAllOntologyTermsForDataTypeBy(queryResult)
            conceptKeys = ontologyTerms.collect { it.key }
        }
        conceptKeys.eachWithIndex { String conceptPath, int index ->
            // Add constraints to filter the output
            List<File> files = exportForSingleNode(
                    conceptPath,
                    resultInstanceId,
                    args.studyDir,
                    args.format,
                    args.dataType,
                    index,
                    args.jobName)

            fileNames.addAll(files*.absolutePath)
        }

        fileNames
    }

    private List<File> exportForSingleNode(String conceptPath, Long resultInstanceId, File studyDir, String format, String dataType, Integer index, String jobName) {

        HighDimensionDataTypeResource dataTypeResource = highDimensionResourceService.getSubResourceForType(dataType)

        def assayConstraints = []

        assayConstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.PATIENT_SET_CONSTRAINT,
                result_instance_id: resultInstanceId)

        assayConstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                concept_key: conceptPath)

        // Setup class to export the data
        HighDimExporter exporter = highDimExporterRegistry.getExporterForFormat(format)
        Projection projection = dataTypeResource.createProjection(exporter.projection)

        // Retrieve the data itself
        TabularResult<AssayColumn, DataRow> tabularResult =
                dataTypeResource.retrieveData(assayConstraints, [], projection)

        List<File> outputFiles = []

        try {
            exporter.export(
                    tabularResult,
                    projection,
                    { String fileName ->
                        //TODO Imrove naming for parent folder
                        File parentFolder = new File(studyDir, "${dataType}_${makeFileNameFromConceptPath(conceptPath)}_${index}")
                        parentFolder.mkdirs()
                        File outputFile = new File(parentFolder, fileName)
                        if (outputFile.exists()) {
                            throw new RuntimeException("${outputFile} file already exists.")
                        }
                        outputFiles << outputFile
                        outputFile.newOutputStream()
                    },
                    { jobIsCancelled(jobName) })
        } catch (RuntimeException e) {
            log.error('Data export to the file has thrown an exception', e)
        } finally {
            tabularResult.close()
        }

        outputFiles
    }

    private String makeFileNameFromConceptPath(String conceptPath) {
        conceptPath
                .split('\\\\')
                .reverse()[0..1]
                .join('_')
                .replaceAll('[\\W_]+', '_')
    }

    def boolean jobIsCancelled(jobName) {
        if (jobResultsService[jobName]["Status"] == "Cancelled") {
            log.warn("${jobName} has been cancelled")
            return true
        }
        return false
    }
}
