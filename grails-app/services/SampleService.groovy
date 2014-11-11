class SampleService {

    def dataSource
    def i2b2HelperService
    def grailsApplication
    def solrService

    boolean transactional = true

    //Populate the QT_PATIENT_SAMPLE_COLLECTION table based on a result_instance_id.
    public void generateSampleCollection(String result_instance_id) {
        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
        sql.execute("INSERT INTO QT_PATIENT_SAMPLE_COLLECTION (SAMPLE_ID, PATIENT_ID, RESULT_INSTANCE_ID) SELECT DISTINCT DSSM.SAMPLE_ID, DSSM.patient_id, ? FROM QT_PATIENT_SET_COLLECTION QT INNER JOIN DE_SUBJECT_SAMPLE_MAPPING DSSM ON DSSM.PATIENT_ID = QT.PATIENT_NUM WHERE RESULT_INSTANCE_ID = ?", [result_instance_id.toInteger(), result_instance_id.toInteger()])
    }

    public loadSampleStatisticsObject(String result_instance_id) {
        //This is the value object we store the count values in.
        def sampleSummary = [:]

        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

        StringWriter writer1 = new StringWriter()
        PrintWriter pw1 = new PrintWriter(writer1)

        i2b2HelperService.renderQueryDefinition(result_instance_id, "Query Definition", pw1)

        sampleSummary["queryDefinition"] = writer1.toString()

        grailsApplication.config.edu.harvard.transmart.sampleBreakdownMap.each {
            currentCountVariable ->

                sampleSummary[currentCountVariable.value] = solrService.getFacetCountForField(currentCountVariable.key, result_instance_id, 'sample')

                log.debug("Finished count for field ${currentCountVariable.value} - ${currentCountVariable.key}")
                log.debug(sampleSummary[currentCountVariable.value])

        }

        return sampleSummary
    }

}