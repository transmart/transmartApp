package com.recomdata.transmart.data.export

import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.export.HighDimExporter

class HighDimExportService {

    def highDimensionResourceService
    def highDimExporterRegistry
    def queriesResourceService
    def conceptsResourceService

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
        String jobName = args.jobName

        if (jobResultsService.isJobCancelled(jobName)) {
            return null
        }

        def fileNames = []

        HighDimensionDataTypeResource dataTypeResource = highDimensionResourceService.getSubResourceForType(args.dataType)
        def ontologyTerms
        if (conceptKeys) {
            ontologyTerms = conceptKeys.collectAll { conceptsResourceService.getByKey it }
        } else {
            def queryResult = queriesResourceService.getQueryResultFromId(resultInstanceId)
            ontologyTerms = dataTypeResource.getAllOntologyTermsForDataTypeBy(queryResult)
        }

        ontologyTerms.each { OntologyTerm term ->
            // Add constraints to filter the output
            List<File> files = exportForSingleNode(
                    term,
                    resultInstanceId,
                    args.studyDir,
                    args.format,
                    args.dataType,
                    args.jobName)

            fileNames.addAll(files*.absolutePath)
        }

        fileNames
    }

    List<File> exportForSingleNode(OntologyTerm term, Long resultInstanceId, File studyDir, String format, String dataType, String jobName) {

        List<File> outputFiles = []

        HighDimensionDataTypeResource dataTypeResource = highDimensionResourceService.getSubResourceForType(dataType)

        def assayConstraints = []

        assayConstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.PATIENT_SET_CONSTRAINT,
                result_instance_id: resultInstanceId)

        assayConstraints << dataTypeResource.createAssayConstraint(
                AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                concept_key: term.key)

        // Setup class to export the data
        HighDimExporter exporter = highDimExporterRegistry.getExporterForFormat(format)
        Projection projection = dataTypeResource.createProjection(exporter.projection)

        // Retrieve the data itself
        TabularResult<AssayColumn, DataRow> tabularResult =
                dataTypeResource.retrieveData(assayConstraints, [], projection)

        try {
            exporter.export(
                    tabularResult,
                    projection,
                    { String dataFileName, String dataFileExt ->
                        File nodeDataFolder = new File(studyDir, getRelativeFolderPathForSingleNode(term))
                        File outputFile = new File(nodeDataFolder,
                                "${dataFileName}_${dataType}.${dataFileExt.toLowerCase()}")
                        if (outputFile.exists()) {
                            throw new RuntimeException("${outputFile} file already exists.")
                        }
                        nodeDataFolder.mkdirs()
                        outputFiles << outputFile
                        outputFile.newOutputStream()
                    },
                    { jobResultsService.isJobCancelled(jobName) })
        } catch (RuntimeException e) {
            log.error('Data export to the file has thrown an exception', e)
        } finally {
            tabularResult.close()
        }
        outputFiles
    }

    static String getRelativeFolderPathForSingleNode(OntologyTerm term) {
        def leafConceptFullName = term.fullName
        String resultConceptPath = leafConceptFullName
        def study = term.study
        if (study) {
            def studyConceptFullName = study.ontologyTerm.fullName
            //use internal study folders only
            resultConceptPath = leafConceptFullName.replace(studyConceptFullName, '')
        }

        resultConceptPath.split('\\\\').findAll().collect { String folderName ->
            //Reversible way to encode a string to use as filename
            //http://stackoverflow.com/questions/1184176/how-can-i-safely-encode-a-string-in-java-to-use-as-a-filename
            URLEncoder.encode(folderName, 'UTF-8')
        }.join(File.separator)
    }

}
