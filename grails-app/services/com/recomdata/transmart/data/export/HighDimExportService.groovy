package com.recomdata.transmart.data.export

import au.com.bytecode.opencsv.CSVWriter
import org.transmartproject.core.dataquery.DataRow
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.assay.Assay
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.Platform
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.projections.Projection
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.OntologyTermTag
import org.transmartproject.export.HighDimExporter

class HighDimExportService {

    final static List<String> META_FILE_HEADER = ['Attribute', 'Description']
    private static final META_FILE_NAME = 'meta.tsv'

    final static List<String> SAMPLE_FILE_HEADER = ['Assay ID',
                                                    'Subject ID',
                                                    'Sample Type',
                                                    'Time Point',
                                                    'Tissue Type',
                                                    'Platform ID',
                                                    'Sample Code']
    private static final SAMPLES_FILE_NAME = 'samples.tsv'

    final static List<String> PLATFORM_FILE_HEADER = ['Platform ID',
                                                    'Title',
                                                    'Genome Release ID',
                                                    'Organism',
                                                    'Marker Type',
                                                    'Annotation Date']
    private static final PLATFORM_FILE_NAME = 'platform.tsv'

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
     * - args.exportOptions           - map with boolean values. Specifies what kind of data to export.
     *                                  Possible keys:
     *                                  - meta - ontology tags data
     *                                  - samples - subject sample mapping
     *                                  - platform - platform information
     * @return list of exported files
     */
    List<File> exportHighDimData(Map args) {
        Long resultInstanceId    = args.resultInstanceId as Long
        List<String> conceptKeys = args.conceptKeys
        String jobName           = args.jobName
        File studyDir            = args.studyDir
        String format            = args.format
        Map exportOptions        = args.exportOptions ?: [:].withDefault { true } //export everything by default

        def files = []

        log.info("Start a HD data export job: ${jobName}")

        HighDimensionDataTypeResource dataTypeResource = highDimensionResourceService
                .getSubResourceForType(args.dataType)

        def ontologyTerms
        if (conceptKeys) {
            ontologyTerms = conceptKeys.collectAll { conceptsResourceService.getByKey it }
        } else {
            def queryResult = queriesResourceService.getQueryResultFromId(resultInstanceId)
            ontologyTerms = dataTypeResource.getAllOntologyTermsForDataTypeBy(queryResult)
        }

        ontologyTerms.each { OntologyTerm term ->
            log.info("[${jobName}] Start export for a term: ${term.key}")
            if (!jobResultsService.isJobCancelled(jobName)) {
                files.addAll(
                        exportForSingleNode(
                                term,
                                resultInstanceId,
                                studyDir,
                                format,
                                dataTypeResource,
                                jobName,
                                exportOptions))
            }
        }

        files.findAll()
    }

    List<File> exportForSingleNode(OntologyTerm term,
                                   Long resultInstanceId,
                                   File studyDir,
                                   String format,
                                   HighDimensionDataTypeResource dataTypeResource,
                                   String jobName,
                                   Map exportOptions) {

        if (jobResultsService.isJobCancelled(jobName)) {
            return []
        }

        List<File> outputFiles = []

        def assayConstraints = [
                dataTypeResource.createAssayConstraint(
                        AssayConstraint.PATIENT_SET_CONSTRAINT,
                        result_instance_id: resultInstanceId),
                dataTypeResource.createAssayConstraint(
                        AssayConstraint.ONTOLOGY_TERM_CONSTRAINT,
                        concept_key: term.key)
        ]

        Set<HighDimExporter> exporters = highDimExporterRegistry.findExporters(fileFormat: format,
                dataType: dataTypeResource.dataTypeName)

        if (!exporters) {
            throw new RuntimeException("No exporter was found for ${dataTypeResource.dataTypeName} data type" +
                    " and ${format} file format.")
        } else if (exporters.size() > 1) {
            log.warn("There are more than one exporter for ${dataTypeResource.dataTypeName} data type" +
                    " and ${format} file format. Using first one: ${exporters?.getAt(0)}")
        }

        HighDimExporter exporter = exporters.getAt(0)

        Projection projection = dataTypeResource.createProjection(exporter.projection)

        if (log.debugEnabled) {
            log.debug("[job=${jobName} key=${term.key}] " +
                    "Retrieving the HD data for the term and a patient set: ${resultInstanceId}.")
        }
        TabularResult<AssayColumn, DataRow> tabularResult =
                dataTypeResource.retrieveData(assayConstraints, [], projection)

        File nodeDataFolder = new File(studyDir, getRelativeFolderPathForSingleNode(term))
        if (log.debugEnabled) {
            log.debug("Create a node data folder: ${nodeDataFolder.path}.")
        }
        nodeDataFolder.mkdirs()

        try {
            if (log.debugEnabled) {
                log.debug("[job=${jobName} key=${term.key}] Export the HD data to the file.")
            }
            exporter.export(
                    tabularResult,
                    projection,
                    { String dataFileName, String dataFileExt ->
                        File outputFile = new File(nodeDataFolder,
                                "${dataFileName}_${dataTypeResource.dataTypeName}.${dataFileExt.toLowerCase()}")
                        if (outputFile.exists()) {
                            throw new RuntimeException("${outputFile} file already exists.")
                        }
                        nodeDataFolder.mkdirs()
                        outputFiles << outputFile
                        if (log.debugEnabled) {
                            log.debug("Inflating the data file: ${outputFile.path}.")
                        }
                        outputFile.newOutputStream()
                    },
                    { jobResultsService.isJobCancelled(jobName) })
        } catch (RuntimeException e) {
            log.error('Data export to the file has thrown an exception', e)
        } finally {
            tabularResult.close()
        }

        if (exportOptions.samples && !jobResultsService.isJobCancelled(jobName)) {
            if (tabularResult.indicesList) {
                if (log.debugEnabled) {
                    log.debug("[job=${jobName} key=${term.key}] Export the assays to the file.")
                }
                outputFiles << exportAssays(tabularResult.indicesList, nodeDataFolder)
            }
        }

        if (exportOptions.platform && !jobResultsService.isJobCancelled(jobName)) {
            Set<Platform> platforms = tabularResult.indicesList*.platform
            if (platforms) {
                if (log.debugEnabled) {
                    log.debug("[job=${jobName} key=${term.key}] Export the platform to the file.")
                }
                outputFiles << exportPlatform(platforms, nodeDataFolder)
            }
        }

        if (exportOptions.meta && !jobResultsService.isJobCancelled(jobName)) {
            def tagsMap = ontologyTermTagsResourceService.getTags([term] as Set, false)
            if (tagsMap && tagsMap[term]) {
                if (log.debugEnabled) {
                    log.debug("[job=${jobName} key=${term.key}] Export the tags to the file.")
                }
                outputFiles << exportMetaTags(tagsMap[term], nodeDataFolder)
            }
        }

        outputFiles
    }

    static File exportMetaTags(Collection<OntologyTermTag> tags, File parentFolder) {
        def metaTagsFile = new File(parentFolder, META_FILE_NAME)

        metaTagsFile.withWriter { Writer writer ->
            CSVWriter csvWriter = new CSVWriter(writer, COLUMN_SEPARATOR)
            csvWriter.writeNext(META_FILE_HEADER as String[])
            tags.each { OntologyTermTag tag ->
                csvWriter.writeNext([tag.name, tag.description] as String[])
            }
        }

        metaTagsFile
    }

    static File exportAssays(Collection<Assay> assays, File parentFolder) {
        def samplesFile = new File(parentFolder, SAMPLES_FILE_NAME)

        samplesFile.withWriter { Writer writer ->
            CSVWriter csvWriter = new CSVWriter(writer, COLUMN_SEPARATOR)
            csvWriter.writeNext(SAMPLE_FILE_HEADER as String[])
            assays.each { Assay assay ->

                List<String> line = [
                        assay.id,
                        assay.patientInTrialId,
                        assay.sampleType.label,
                        assay.timepoint.label,
                        assay.tissueType.label,
                        assay.platform.id,
                        assay.sampleCode
                ]

                csvWriter.writeNext(line as String[])
            }
        }

        samplesFile
    }

    static File exportPlatform(Collection<Platform> platforms, File parentFolder) {
        def platformFile = new File(parentFolder, PLATFORM_FILE_NAME)

        platformFile.withWriter { Writer writer ->
            CSVWriter csvWriter = new CSVWriter(writer, COLUMN_SEPARATOR)
            csvWriter.writeNext(PLATFORM_FILE_HEADER as String[])
            platforms.each { Platform platform ->

                List<String> line = [
                        platform.id,
                        platform.title,
                        platform.genomeReleaseId,
                        platform.organism,
                        platform.markerType,
                        platform.annotationDate,
                ]

                csvWriter.writeNext(line as String[])
            }
        }

        platformFile
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
