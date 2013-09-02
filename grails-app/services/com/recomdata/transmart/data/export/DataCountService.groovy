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
 * You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/.
 * 
 *
 ******************************************************************/
  

package com.recomdata.transmart.data.export

class DataCountService {
	
	def dataSource
    boolean transactional = true

	//For the given list of Subjects get counts of what kind of data we have for those cohorts.
	//We want to return a map that looks like {"PLINK": "102","RBM":"28"}
    Map getDataCounts(Long rId, Long[] resultInstanceIds)
	{
		//This is the map we build for each subset that contains the data type and count for that data type.
        def resultMap = [
                MRNA       : [:],
                MRNA_CEL   : 0,
                CLINICAL   : 0,
                RBM        : 0,
                SNP        : 0,
                SNP_CEL    : 0,
                ADDITIONAL : 0,
                GSEA       : [:]
        ]

        if (!rId) {
            return resultMap
        }

        if (!resultInstanceIds) {
            throw new IllegalArgumentException(
                    "resultInstanceIds cannot be empty if an rId is passed in")
        }

        String mrnaQuery,
               subjectsQuery,
               subjectsFromBothSubsetsQuery,
               clinicalQuery,
               rbmQuery,
               snpQuery,
               snpCelQuery,
               gseaQuery,
               mrnaCelQuery,
               additionalDataQuery;
		
		subjectsQuery = '''
            SELECT DISTINCT patient_num
            FROM qt_patient_set_collection
            WHERE result_instance_id = :rId
                AND patient_num NOT IN (
                    SELECT patient_num FROM patient_dimension WHERE sourcesystem_cd LIKE '%:S:%'
                )'''
		
		subjectsFromBothSubsetsQuery = """
            SELECT DISTINCT patient_num
            FROM qt_patient_set_collection
            WHERE
                result_instance_id IN (${resultInstanceIds.join(', ')})
                AND patient_num NOT IN (
                    SELECT patient_num from patient_dimension where sourcesystem_cd LIKE '%:S:%'
                )"""
        // Groovy doesn't support binding arrays to 'IN ?'; see GROOVY-5436
		
		//Build the query we use to get MRNA Data. patient_id should be unique to a given study for each patient. 
		//We count the distinct ID's with MRNA data.
		mrnaQuery = """
            SELECT count(distinct s.patient_id), s.gpl_id
            FROM de_subject_sample_mapping s
            WHERE
                s.patient_id IN ($subjectsQuery)
                AND s.platform = 'MRNA_AFFYMETRIX'
                AND s.assay_id IS NOT NULL group by s.gpl_id"""
		
		//Build the query we use to get MRNA "CEL" Data. patient_id should be unique to a given study for each patient.
		//We count the distinct ID's with MRNA data.
		mrnaCelQuery = """
            SELECT count(distinct s.patient_id)
            FROM
                de_subject_sample_mapping s
                INNER JOIN bio_content b on s.trial_name = b.study_name
            WHERE
                s.patient_id IN ($subjectsQuery)
                AND s.platform = 'MRNA_AFFYMETRIX'
                AND s.assay_id IS NOT NULL
                AND b.cel_location IS NOT NULL
                AND s.sample_cd IS NOT NULL"""
		
		//Build the query we use to get the clinical data. patient_num should be unique across all studies.
		clinicalQuery = """
            SELECT count(distinct obsf.patient_num)
            FROM observation_fact obsf
            WHERE obsf.patient_num IN ($subjectsQuery)"""
		
		//Build the query we use to get the RBM data. patient_id should be unique across all studies.
		rbmQuery = """
            SELECT count(distinct rbm.patient_id)
            FROM DE_SUBJECT_RBM_DATA rbm
            WHERE rbm.patient_id IN ($subjectsQuery)"""

		//Build the query we use to get the SNP data. patient_num should be unique across all studies.
		snpQuery = """
            SELECT count(distinct snp.patient_num)
            FROM de_subject_snp_dataset snp
            WHERE snp.patient_num IN ($subjectsQuery)"""
	
		snpCelQuery = """
            SELECT count(DISTINCT ssd.patient_num)
            FROM de_subject_snp_dataset ssd
				INNER JOIN bio_content b ON b.study_name = ssd.trial_name
				WHERE ssd.patient_num IN ($subjectsQuery)"""
		
		//Build the query we use to get Additional Data. patient_id should be unique to a given study for each patient.
		//We count the distinct ID's with additional data. TODO:change != to "ADDTIONAL" or something like that
		def additionalDataPlatformSubQuery = """
		    SELECT distinct platform
		    FROM de_subject_sample_mapping x
		    WHERE
		        x.trial_name = s.trial_name
		        AND x.platform NOT IN ('MRNA_AFFYMETRIX','SNP','RBM','PROTEIN')"""
		
		additionalDataQuery = """
            SELECT count(distinct s.patient_id)
            FROM de_subject_sample_mapping s
                INNER JOIN bio_content b on s.trial_name = b.study_name
            WHERE
                s.patient_id IN ($subjectsQuery)
                AND s.platform IN ($additionalDataPlatformSubQuery)
                AND s.assay_id IS NOT NULL
		        AND b.cel_location IS NOT NULL
		        AND s.sample_cd IS NOT NULL"""
		
		//Build the query we use to get GSEA Data. patient_id should be unique to a given study for each patient.
		//We count the distinct ID's with GSEA data.
		gseaQuery = """
            SELECT
                count(distinct s.patient_id),
                s.gpl_id FROM de_subject_sample_mapping s
            WHERE
                s.patient_id IN ($subjectsFromBothSubsetsQuery)
                AND s.platform = 'MRNA_AFFYMETRIX'
                AND s.assay_id IS NOT NULL
            GROUP BY s.gpl_id"""

        //////

        resultMap['MRNA']       = getCountsPerPlatformFromDB(mrnaQuery, rId)
        resultMap['MRNA_CEL']   = getCountFromDB(mrnaCelQuery,          rId)
        resultMap['CLINICAL']   = getCountFromDB(clinicalQuery,         rId)
        resultMap['RBM']        = getCountFromDB(rbmQuery,              rId)
        resultMap['SNP']        = getCountFromDB(snpQuery,              rId)
        resultMap['SNP_CEL']    = getCountFromDB(snpCelQuery,           rId)
        resultMap['ADDITIONAL'] = getCountFromDB(additionalDataQuery,   rId)
        resultMap['GSEA']       = getCountsPerPlatformFromDB(gseaQuery)
		
		return resultMap
    }
	
	
	def getCountFromDB(String commandString, Long rId)
	{
		def sql = new groovy.sql.Sql(dataSource);

        def params = [rId: rId]

        if (log.isDebugEnabled()) {
            log.debug("About to issue data count query: $commandString, rId: $rId")
        }

		sql.firstRow(params, commandString)[0]
	}
	
	def getCountsPerPlatformFromDB(String commandString, Long rId = null)
	{
		def sql = new groovy.sql.Sql(dataSource);

        def sqlEachRow = sql.&eachRow

        if (rId != null) {
            /* we have to call a different variant of eachRow (without
             * the params array) if rId is null (meaning it's not present
             * in the query) because if we pass and empty map as the
             * params array, it will be interpreted as a parameter list
             * with one element: an empty array
             */
            sqlEachRow = sqlEachRow.curry([rId: rId])
        }

        if (log.isDebugEnabled()) {
            log.debug("About to issue per-platform data count query: " +
                    "$commandString, rId: $rId")
        }
		
		Map countsPerPlatform = [:]

        sqlEachRow commandString, { row ->
            def patientSampleCount = row[0],
                gplId = row['gpl_id']

            countsPerPlatform."$gplId" = patientSampleCount
        }
		
		countsPerPlatform;
	}

}
