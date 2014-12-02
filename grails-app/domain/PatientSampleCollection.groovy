class PatientSampleCollection {

    String id
    String patientId
    String resultInstanceId

    static mapping = {
        table 'QT_PATIENT_SAMPLE_COLLECTION'
        version false

        columns {
            id column: 'SAMPLE_ID'
            patientId column: 'PATIENT_ID'
            resultInstanceId column: 'RESULT_INSTANCE_ID'
        }

    }
}