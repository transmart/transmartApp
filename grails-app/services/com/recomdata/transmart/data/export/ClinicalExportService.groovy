package com.recomdata.transmart.data.export

import au.com.bytecode.opencsv.CSVWriter
import com.google.common.collect.Iterators
import com.google.common.collect.PeekingIterator
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.clinical.ClinicalVariableColumn
import org.transmartproject.core.dataquery.clinical.PatientRow
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.db.concept.ConceptKey
import org.transmartproject.db.dataquery.clinical.variables.NormalizedLeafsVariable

import static org.transmartproject.core.dataquery.clinical.ClinicalVariable.NORMALIZED_LEAFS_VARIABLE

class ClinicalExportService {

    def queriesResourceService
    def clinicalDataResourceService
    def studiesResourceService

    final static String FILE_NAME = 'data_clinical.tsv'
    final static String SUBJ_ID_TITLE = 'Subject ID'
    final static char COLUMN_SEPARATOR = '\t' as char

    def jobResultsService

    File exportClinicalData(Map args) {
        String jobName = args.jobName
        if (jobResultsService.isJobCancelled(jobName)) {
            return null
        }

        Long resultInstanceId = args.resultInstanceId as Long
        List<String> conceptKeys = args.conceptKeys
        File studyDir = args.studyDir

        QueryResult queryResult = queriesResourceService.getQueryResultFromId(resultInstanceId)
        List<NormalizedLeafsVariable> variables =
                conceptKeys ?
                        getVariables(conceptKeys)
                        : getAllObservedVariablesForQueryResult(queryResult)

        TabularResult<ClinicalVariableColumn, PatientRow> tabularResult =
                clinicalDataResourceService.retrieveData(queryResult, variables)

        File clinicalDataFile

        try {
            clinicalDataFile = writeToFile(tabularResult, variables, studyDir, jobName)
        } finally {
            tabularResult.close()
        }

        clinicalDataFile
    }

    private File writeToFile(TabularResult<ClinicalVariableColumn, PatientRow> tabularResult,
                             List<NormalizedLeafsVariable> variables,
                             File studyDir,
                             String jobName) {
        PeekingIterator peekingIterator = Iterators.peekingIterator(tabularResult.iterator())

        File clinicalDataFile = new File(studyDir, FILE_NAME)
        clinicalDataFile.withWriter { Writer writer ->
            CSVWriter csvWriter = new CSVWriter(writer, COLUMN_SEPARATOR)

            def firstRow = peekingIterator.peek()
            List headRowList = [SUBJ_ID_TITLE] +
                    variables.collectMany { NormalizedLeafsVariable var ->
                        firstRow[var].collect { it.key.label }
                    }

            csvWriter.writeNext(headRowList as String[])

            while (peekingIterator.hasNext()) {
                if (jobResultsService.isJobCancelled(jobName)) {
                    return null
                }
                def row = peekingIterator.next()
                List rowList = [row.patient.inTrialId] +
                        variables.collectMany { NormalizedLeafsVariable var ->
                            row[var].values()
                        }

                csvWriter.writeNext(rowList as String[])
            }
        }
        clinicalDataFile
    }

    private Collection<NormalizedLeafsVariable> getVariables(Collection<String> conceptKeys) {
        conceptKeys.collectAll {
            def conceptKey = new ConceptKey(it)
            clinicalDataResourceService.createClinicalVariable(
                    NORMALIZED_LEAFS_VARIABLE,
                    concept_path: conceptKey.conceptFullName.toString())
        }
    }

    private Collection<NormalizedLeafsVariable> getAllObservedVariablesForQueryResult(QueryResult queryResult) {
        def trials = queryResult.patients*.trial as Set
        trials.collect { String trialId ->
            def study = studiesResourceService.getStudyById(trialId)
            clinicalDataResourceService.createClinicalVariable(
                    NORMALIZED_LEAFS_VARIABLE,
                    concept_path: study.ontologyTerm.fullName)
        }
    }

}
