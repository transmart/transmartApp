package com.recomdata.transmart.data.export

import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.highdim.AssayColumn
import org.transmartproject.core.dataquery.highdim.BioMarkerDataRow
import org.transmartproject.core.dataquery.highdim.HighDimensionDataTypeResource
import org.transmartproject.core.dataquery.highdim.assayconstraints.AssayConstraint
import org.transmartproject.core.dataquery.highdim.projections.AllDataProjection
import org.transmartproject.core.dataquery.highdim.projections.Projection

/**
 * Created by jan on 1/20/14.
 */
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

        /*
         dataType one of: mrna, mirna, protein, rbm, rnaseqcog, metabolite

         example inputs:
         resultInstanceId = 23306  //22967
         conceptPaths = [/\\Public Studies\Public Studies\GSE8581\MRNA\Biomarker Data\Affymetrix Human Genome U133A 2.0 Array\Lung/]
        */

        // These maps specify the row header in the output file for each database field name.
        Map dataFields = [rawIntensity: 'value', intensity: 'value', value: 'value', logIntensity: 'log2e', zscore: 'zscore']
        Map rowFields = [geneSymbol: 'gene symbol', geneId: 'gene id', mirnaId: 'mirna id', peptide: 'peptide sequence',
                antigenName: 'analyte name', uniprotId: 'uniprot id', transcriptId: 'transcript id']


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

        String[] header = ['PATIENT ID']
        header += splitAttributeColumn ? ["SAMPLE TYPE", "TIMEPOINT", "TISSUE TYPE", "GPL ID"] : ["SAMPLE"]
        header += ["ASSAY ID", "SAMPLE CODE"]

        Map<String, String> dataKeys = projection.dataProperties.collectEntries {[it, dataFieldHeaders.get(it, it).toUpperCase()]}

        Map<String, String> rowKeys = projection.rowProperties.collectEntries {[it, rowFieldHeaders.get(it, it).toUpperCase()]}

        header += dataKeys.values()
        header += rowKeys.values()


        Writer writer = null
        String fileName = null
        TabularResult<AssayColumn, BioMarkerDataRow<Map<String, String>>> tabularResult = null
        long rowsFound = 0
        long startTime
        try {
            File outputFile = new File(studyDir, dataType+'.txt')
            fileName = outputFile.getAbsolutePath()
            writer = outputFile.newWriter(true)

            log.info("start sample retrieving query")

            startTime = System.currentTimeMillis()

            tabularResult = dataTypeResource.retrieveData(assayconstraints, [], projection)

            List<AssayColumn> assayList = tabularResult.indicesList

            log.info("started file writing to $fileName")
            writer << header.join('\t') << '\n'

            writeloop:
            for (BioMarkerDataRow<Map<String, String>> datarow : tabularResult) {
                for (AssayColumn assay : assayList) {
                    rowsFound++
                    // test periodically if the job is cancelled
                    if (rowsFound % 1024 == 0 && jobIsCancelled(jobName)) {
                        return null
                    }

                    Map<String, String> data = datarow[assay]

                    // TODO: This probably shouldn't happen, but it does!
                    if (data == null) {
                        continue
                    }

                    String assayId =        assay.id
                    String patientId =      assay.patientInTrialId
                    String sampleTypeName = assay.sampleType.label
                    String timepointName =  assay.timepoint.label
                    String tissueTypeName = assay.tissueType.label
                    String platform =       assay.platform.id
                    String sampleCode =     assay.sampleCode

                    List<String> line = [patientId]
                    if (splitAttributeColumn)
                    //       SAMPLE TYPE     TIMEPOINT      TISSUE TYPE     GPL ID
                        line += [sampleTypeName, timepointName, tissueTypeName, platform]
                    else
                    //      SAMPLE
                        line << [sampleTypeName, timepointName, tissueTypeName, platform].grep().join('_')

                    line << assayId << sampleCode

                    for (String dataField: dataKeys.keySet()) {
                        line << data[dataField]
                    }

                    for (String rowField: rowKeys.keySet()) {
                        line << datarow."$rowField"
                    }

                    writer << line.join('\t') << '\n'

                }
            }
            if (!rowsFound) {
                log.error("No data found while trying to export $dataType data")
                if (!outputFile.delete())
                    log.error("Unable to delete empty output file $fileName")
            }
            log.info("Retrieving data took ${System.currentTimeMillis() - startTime} ms")

        } finally {
            writer?.close()
            tabularResult?.close()
        }

        return [outFile: fileName, dataFound: rowsFound]
    }

    def boolean jobIsCancelled(jobName) {
        if (jobResultsService[jobName]["Status"] == "Cancelled") {
            log.warn("${jobName} has been cancelled")
            return true
        }
        return false
    }
}
