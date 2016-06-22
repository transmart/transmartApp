/*
 * Copyright © 2013-2016 The Hyve B.V.
 *
 * This file is part of Transmart.
 *
 * Transmart is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Transmart.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmart.audit

import javax.annotation.Resource
import grails.plugin.cache.Cacheable
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.Study
import org.transmartproject.core.ontology.ConceptsResource
import org.transmartproject.core.querytool.QueriesResource
import org.transmartproject.core.querytool.QueryResult

class StudyIdService {

    @Resource
    QueriesResource queriesResourceService

    @Resource
    ConceptsResource conceptsResourceService

    /**
     * Fetches the study id associated with a concept from the 
     * {@link ConceptsResource} using the concept key.
     * 
     * @param concept_key the concept key.
     * @param options map with optional parameters:
     *  - 'studyConceptOnly': if set, a study name will only be returned if the
     *    concept is a study.
     * @return the study id as string if the concept is found; null if the
     *         concept key is null; the empty string if the concept key is empty 
     *         or the concept could not be found.
     */
    @Cacheable('org.transmart.audit.StudyIdService')
    public String getStudyIdForConceptKey(Map options = [:], String concept_key) {
        if (concept_key == null) {
            return null
        }
        concept_key = concept_key.trim()
        if (concept_key.empty) {
            return ""
        }
        String studyId = ""
        try {
            log.debug "Query study id for concept key: ${concept_key}"
            OntologyTerm term = conceptsResourceService.getByKey(concept_key)
            Study study = term?.study
            studyId = study?.id
            if (options?.studyConceptOnly && study?.ontologyTerm != term) {
                studyId = null
            }
            log.debug "Study id for concept key ${concept_key} is: ${studyId}"
        } catch(NoSuchResourceException e) {
            log.warn "Resource not found: " +
                    "ConceptResource.getByKey(${concept_key})"
        }
        studyId
    }

    @Cacheable('org.transmart.audit.StudyIdService')
    Set<String> getStudyIdsForQueryId(Long queryId) {
        Set<String> result = []
        try {
            log.debug "Query trials for query id: ${queryId}"
            QueryResult queryResult = queriesResourceService.getQueryResultFromId(queryId)
            result = queryResult.patients*.trial as Set
        } catch (NoSuchResourceException e) {
            log.warn "Resource not found: " +
                    "QueriesResource.getQueryResultFromId(${queryId})"
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
