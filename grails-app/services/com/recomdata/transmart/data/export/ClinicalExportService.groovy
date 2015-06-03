package com.recomdata.transmart.data.export

import au.com.bytecode.opencsv.CSVWriter
import com.google.common.collect.Iterators
import com.google.common.collect.PeekingIterator
import org.transmartproject.core.dataquery.TabularResult
import org.transmartproject.core.dataquery.clinical.ClinicalVariableColumn
import org.transmartproject.core.dataquery.clinical.PatientRow
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.OntologyTermTag
import org.transmartproject.core.ontology.Study
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.db.concept.ConceptKey
import org.transmartproject.db.dataquery.clinical.variables.NormalizedLeafsVariable

import static org.transmartproject.core.dataquery.clinical.ClinicalVariable.NORMALIZED_LEAFS_VARIABLE

class ClinicalExportService {

    def queriesResourceService
    def clinicalDataResourceService
    def studiesResourceService
    def ontologyTermTagsResourceService
    def conceptsResourceService

    final static String DATA_FILE_NAME = 'data_clinical.tsv'
    final static String META_FILE_NAME = 'meta.tsv'
    final static String SUBJ_ID_TITLE = 'Subject ID'
    final static char COLUMN_SEPARATOR = '\t' as char

    def jobResultsService

    List<File> exportClinicalData(Map args) {
        String jobName = args.jobName
        if (jobResultsService.isJobCancelled(jobName)) {
            return null
        }

        Long resultInstanceId = args.resultInstanceId as Long
        List<String> conceptKeys = args.conceptKeys
        File studyDir = args.studyDir

        QueryResult queryResult = queriesResourceService.getQueryResultFromId(resultInstanceId)
        List<NormalizedLeafsVariable> variables
        Set<OntologyTerm> terms
        if(conceptKeys) {
            variables = getVariables(conceptKeys)
            terms = conceptKeys.collect { it -> conceptsResourceService.getByKey it }
        } else {
            Set<Study> studies = getQueriedStudies(queryResult)
            variables = getAllVariablesFor(studies)
            terms = studies*.ontologyTerm
        }

        def file = []

        file << exportClinicalDataToFile(queryResult, variables, studyDir, jobName)
        file << exportAllTags(terms, studyDir)

        file
    }

    private File exportClinicalDataToFile(QueryResult queryResult,
                                          List<NormalizedLeafsVariable> variables,
                                          File studyDir,
                                          String jobName) {

        TabularResult<ClinicalVariableColumn, PatientRow> tabularResult =
                clinicalDataResourceService.retrieveData(queryResult, variables)

        try {
            return writeToFile(tabularResult, variables, studyDir, jobName)
        } finally {
            tabularResult.close()
        }
    }

    private File writeToFile(TabularResult<ClinicalVariableColumn, PatientRow> tabularResult,
                             List<NormalizedLeafsVariable> variables,
                             File studyDir,
                             String jobName) {
        PeekingIterator peekingIterator = Iterators.peekingIterator(tabularResult.iterator())

        File clinicalDataFile = new File(studyDir, DATA_FILE_NAME)
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

    File exportAllTags(Set<OntologyTerm> terms, File studyDir) {
        def tagsMap = ontologyTermTagsResourceService.getTags(terms, true)

        if (tagsMap) {
            def resultFile = new File(studyDir, META_FILE_NAME)

            resultFile.withWriter { Writer writer ->
                CSVWriter csvWriter = new CSVWriter(writer, COLUMN_SEPARATOR)
                tagsMap.each { OntologyTerm keyTerm, List<OntologyTermTag> valueTags ->
                    valueTags.each { OntologyTermTag tag ->
                        csvWriter.writeNext([keyTerm.fullName, tag.name, tag.description] as String[])
                    }
                }
            }

            resultFile
        }
    }

    private Collection<NormalizedLeafsVariable> getVariables(Collection<String> conceptKeys) {
        conceptKeys.collectAll {
            def conceptKey = new ConceptKey(it)
            clinicalDataResourceService.createClinicalVariable(
                    NORMALIZED_LEAFS_VARIABLE,
                    concept_path: conceptKey.conceptFullName.toString())
        }
    }

    private Set<Study> getQueriedStudies(QueryResult queryResult) {
        def trials = queryResult.patients*.trial as Set
        trials.collect { String trialId ->
            studiesResourceService.getStudyById(trialId)
        } as Set
    }

    private Collection<NormalizedLeafsVariable> getAllVariablesFor(Set<Study> queriedStudies) {
        queriedStudies.collect { Study study ->
            clinicalDataResourceService.createClinicalVariable(
                    NORMALIZED_LEAFS_VARIABLE,
                    concept_path: study.ontologyTerm.fullName)
        }
    }

}
