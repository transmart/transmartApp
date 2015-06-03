package com.recomdata.transmart.data.export

import au.com.bytecode.opencsv.CSVWriter
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.OntologyTermTag
import org.transmartproject.export.HighDimExporter

class HighDimExportService {

    private static final META_FILE_NAME = 'meta.tsv'
    private final static char COLUMN_SEPARATOR = '\t' as char

    def highDimensionResourceService
    def highDimExporterRegistry
    def queriesResourceService
    def conceptsResourceService
    def ontologyTermTagsResourceService

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
    List<File> exportHighDimData(Map args) {
        Long resultInstanceId = args.resultInstanceId as Long
        List<String> conceptKeys = args.conceptKeys
        String jobName = args.jobName
        File studyDir = args.studyDir

        if (jobResultsService.isJobCancelled(jobName)) {
            return null
        }

        def files = []

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
            List<File> dataFiles = exportForSingleNode(
                    term,
                    resultInstanceId,
                    studyDir,
                    args.format,
                    args.dataType,
                    args.jobName)

            File tagFile = exportTagsForSingleNode(term, studyDir)

            files.addAll(dataFiles + tagFile)
        }

        files
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

    File exportTagsForSingleNode(OntologyTerm term, File studyDir) {
        def tagsMap = ontologyTermTagsResourceService.getTags([term] as Set, false)

        if (tagsMap) {
            def metaDataFolder = new File(studyDir, getRelativeFolderPathForSingleNode(term))
            metaDataFolder.mkdirs()

            def resultFile = new File(metaDataFolder, META_FILE_NAME)

            resultFile.withWriter { Writer writer ->
                CSVWriter csvWriter = new CSVWriter(writer, COLUMN_SEPARATOR)
                tagsMap.each { OntologyTerm keyTerm, List<OntologyTermTag> valueTags ->
                    valueTags.each { OntologyTermTag tag ->
                        csvWriter.writeNext([tag.name, tag.description] as String[])
                    }
                }
            }

            resultFile
        }
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
