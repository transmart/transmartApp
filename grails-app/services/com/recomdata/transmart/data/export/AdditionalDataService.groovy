package com.recomdata.transmart.data.export

import com.recomdata.transmart.data.export.util.FileWriterUtil

import static org.transmart.authorization.QueriesResourceAuthorizationDecorator.checkQueryResultAccess;

class AdditionalDataService {

    boolean transactional = true

    def i2b2HelperService

    def dataSource

    def geneExpressionDataService

    def findAdditionalDataFiles(String resultInstanceId, studyList) {
        checkQueryResultAccess resultInstanceId

        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
        def filesList = []

        String studies = geneExpressionDataService.convertList(studyList, false, 1000)

        try {
            def additionalFilesQuery = """
				select * from bio_content b where exists (
				  select distinct s.sample_cd from de_subject_sample_mapping s
				  where s.trial_name in ${studies} and patient_id in (
					SELECT DISTINCT sc.patient_num FROM qt_patient_set_collection sc, patient_dimension pd
					WHERE sc.result_instance_id = ? AND sc.patient_num = pd.patient_num
				  ) and s.sample_cd is not null and b.file_name like s.sample_cd||'%'
				)
				"""
            log.debug(additionalFilesQuery)
            log.debug('ResultInstanceId :: ' + resultInstanceId)
            def sample, mapKey, mapValue = null
            filesList = sql.rows(additionalFilesQuery, [study, resultInstanceId])
        } catch (Exception e) {
            log.error("Problem finding Files for Additional Data :: " + e.getMessage())
        } finally {
            sql?.close()
        }

        return filesList
    }

    def downloadFiles(String resultInstanceId, studyList, File studyDir, String jobName) {
        def filesList = findAdditionalDataFiles(resultInstanceId, studyList)
        if (filesList?.size > 0) {
            def char separator = '\t';
            for (file in filesList) {
                def fileURL = (new StringBuffer(file.CEL_LOCATION).append(file.FILE_NAME).append(file.CEL_FILE_SUFFIX)).toString()
                FileWriterUtil writerUtil = new FileWriterUtil(studyDir, file.FILE_NAME, jobName, "Additional_Files", null, separator)
                writerUtil.writeFile(fileURL, writerUtil.outputFile)
            }
        } else {
            log.debug('No Additional data files found to download')
        }
    }
}
