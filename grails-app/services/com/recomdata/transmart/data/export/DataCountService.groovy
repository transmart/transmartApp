/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/


package com.recomdata.transmart.data.export

import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.querytool.QueriesResource
import org.transmartproject.core.querytool.QueryResult

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess

/**
 * @deprecated now only used for SNP, which is not in core-db
 */
@Deprecated
class DataCountService {

    def dataSource

    static transactional = false

    @Autowired
    QueriesResource queriesResource

    //For the given list of Subjects get counts of what kind of data we have for those cohorts.
    //We want to return a map that looks like {"PLINK": "102","RBM":"28"}
    Map getDataCounts(Long rID, Long[] resultInstanceIds) {
        checkQueryResultAccess(*resultInstanceIds)

        //This is the map we build for each subset that contains the data type and count for that data type.
        def resultMap = [:]

        StringBuilder snpQuery = new StringBuilder()
        StringBuilder subjectsQuery = new StringBuilder()

        subjectsQuery.append("SELECT DISTINCT patient_num FROM qt_patient_set_collection WHERE result_instance_id = ?")
                .append(" AND patient_num IN (select patient_num from patient_dimension where sourcesystem_cd not like '%:S:%')")

        //Build the query we use to get the SNP data. patient_num should be unique across all studies.
        snpQuery.append("SELECT count(distinct snp.patient_num) FROM de_subject_snp_dataset snp WHERE snp.patient_num IN (")
                .append(subjectsQuery).append(")");

        //Get the count of SNP Data for the given list of subject IDs.
        resultMap['SNP'] = StringUtils.isNotEmpty(snpQuery.toString()) && rID ?
                getCountFromDB(snpQuery.toString(), String.valueOf(rID)) :
                0

        return resultMap
    }

    /**
     * Returns the number of patients within a given subset that has clinical data
     * @param resultInstanceId
     * @return The number of patients within the given subset that have clinical data
     */
    Long getClinicalDataCount(Long resultInstanceId) {
        // TODO: Convert this into using
        if (!resultInstanceId)
            return 0

        QueryResult queryResult = queriesResource.getQueryResultFromId(resultInstanceId)
        queryResult.getSetSize()
    }

    def getCountFromDB(String commandString, String rID = null) {
        //We use a groovy object to handle the DB connections.
        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

        //We will store the count results here.
        def patientSampleCount = 0;

        //Iterate over results (Should only be 1 row) and grab the count).
        if (rID && rID?.trim() != '') {
            sql.eachRow(commandString, [rID], { row -> patientSampleCount = row[0]; });
        } else {
            sql.eachRow(commandString, { row -> patientSampleCount = row[0]; });
        }

        return patientSampleCount;
    }

}
