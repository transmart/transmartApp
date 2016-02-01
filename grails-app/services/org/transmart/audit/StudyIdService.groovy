package org.transmart.audit

import javax.annotation.Resource
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.querytool.QueriesResource
import org.transmartproject.core.querytool.QueryResult

class StudyIdService {

    @Resource
    QueriesResource queriesResourceService

    @Resource
    ConceptsResource conceptsResourceService

    private Map<Long, Set<String>> conceptKeyToStudyIdMap = [:]

    /**
     * Fetches the study id associated with a concept from the 
     * {@link ConceptsResource} using the concept key.
     * 
     * @param concept_key the concept key.
     * @return the study id as string if the concept is found; null if the
     *         concept key is null; the empty string if the concept key is empty 
     *         or the concept could not be found.
     */
    public String getStudyIdForConceptKey(String concept_key) {
        if (concept_key == null) {
            return null
        }
        concept_key = concept_key.trim()
        if (concept_key.empty) {
            return ""
        }
        String studyId = conceptKeyToStudyIdMap[concept_key]
        if (studyId == null) {
            try {
                studyId = conceptsResourceService.getByKey(concept_key)?.study?.id
            } catch(NoSuchResourceException e) {
                log.warn "Resource not found: " +
                        "ConceptResource.getByKey(${concept_key})"
                studyId = ""
            }
            conceptKeyToStudyIdMap[concept_key] = studyId
        }
        studyId
    }

    private Map<Long, Set<String>> queryIdToStudyIdsMap = [:]

    private Set<String> getStudyIdsForQueryId(Long queryId) {
        Set<String> result = queryIdToStudyIdsMap[queryId]
        if (result == null) {
            try {
                QueryResult queryResult = queriesResourceService.getQueryResultFromId(queryId)
                result = queryResult.patients*.trial as Set
            } catch (NoSuchResourceException e) {
                log.warn "Resource not found: " +
                        "QueriesResource.getQueryResultFromId(${queryId})"
                result = []
            }
            queryIdToStudyIdsMap[queryId] = result
        }
        result
    }

    /**
     * Fetches the study ids associated with a collection of queries from the 
     * {@link QueriesResource} using their query ids.
     * Empty query ids and non-integer values are ignored. If a query cannot be
     * found for a certain query id, that result is ignored.
     *
     * @param queryIds a list of query ids (a.k.a. result_instance_ids). The ids
     *        are passed as string.
     * @return a string with the comma-separated list study ids.
     */
    public String getStudyIdsForQueries(List<String> queryIds) {
        Set<String> studyIds = []
        for (String queryId: queryIds) {
            if (queryId != null) {
                queryId = queryId.trim()
                if (!queryId.empty) {
                    if (!queryId.isLong()) {
                        log.warn "Query id is not an integer: ${queryId}."
                    } else {
                        Long qId = queryId.toLong()
                        studyIds += getStudyIdsForQueryId(qId)
                    }
                }
            }
        }
        List<String> studyIdList = studyIds as List
        studyIdList.sort()
        studyIdList.join(',')
    }
}
