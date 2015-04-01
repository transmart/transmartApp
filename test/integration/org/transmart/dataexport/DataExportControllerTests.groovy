package org.transmart.dataexport

import com.recomdata.transmart.data.export.DataExportController
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.TestMixin
import groovy.json.JsonSlurper
import org.gmock.WithGMock
import org.hibernate.SessionFactory
import org.junit.Before
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.transmart.authorization.SpringSecurityProxyMetaClassInterceptor
import org.transmartproject.core.exceptions.AccessDeniedException
import org.transmartproject.core.querytool.QueryResult
import org.transmartproject.db.i2b2data.I2b2Data
import org.transmartproject.db.i2b2data.PatientDimension
import org.transmartproject.db.i2b2data.PatientTrialCoreDb
import org.transmartproject.db.querytool.QueryResultData
import org.transmartproject.db.test.RuleBasedIntegrationTestMixin
import org.transmartproject.db.user.AccessLevelTestData

import static groovy.util.GroovyAssert.shouldFail
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.transmartproject.db.i2b2data.I2b2Data.createPatientTrialLinks
import static org.transmartproject.db.i2b2data.I2b2Data.createTestPatients

@TestMixin(RuleBasedIntegrationTestMixin)
@WithGMock
class DataExportControllerTests {

    public static final String JOB_NAME = 'test-foo'
    @Autowired
    SessionFactory sessionFactory

    @Autowired
    DataExportController dataExportController

    @Autowired
    SpringSecurityService springSecurityService

    AccessLevelTestData accessLevelTestData = AccessLevelTestData.createDefault()

    I2b2Data i2b2Data

    QueryResult study2QueryResult

    @Before
    void setUp() {
        accessLevelTestData.saveAll()
        i2b2Data = createI2b2Data()
        i2b2Data.saveAll()
        study2QueryResult = createQueryResult()
        dataExportController.request.parameters = [
                result_instance_id1: study2QueryResult.id as String,
        ]
    }

    def createI2b2Data() {
        String trialName = AccessLevelTestData.STUDY2
        List<PatientDimension> patients = createTestPatients(3, -100, trialName)
        List<PatientTrialCoreDb> patientTrials = createPatientTrialLinks(patients, trialName)
        new I2b2Data(
                trialName: trialName,
                patients: patients,
                patientTrials: patientTrials)
    }

    def createQueryResult() {
        def queryMaster = QueryResultData.createQueryResult i2b2Data.patients
        queryMaster.save(failOnError: true)
        study2QueryResult =
                QueryResultData.getQueryResultFromMaster(queryMaster)
    }

    @Test
    void testGetMetadataAdministrator() {
        // 1st user is an admin
        def user = accessLevelTestData.users[0]

        mockCurrentUser(user.username) {
            dataExportController.getMetaData()
        }

        def slurper = new JsonSlurper()
        def result = slurper.parseText(dataExportController.response.text)

        assertThat result, hasEntry(
                equalTo('exportMetaData'),
                contains(allOf(
                        hasEntry('dataTypeId', 'CLINICAL'),
                        hasEntry('isHighDimensional', false),
                        hasEntry(equalTo('subset1'), allOf(
                                hasEntry(equalTo('exporters'), contains(allOf(
                                        hasEntry('format', 'TSV'),
                                        hasEntry('description', 'Tab separated file.'),
                                ))),
                                hasEntry(equalTo('patientsNumber'), equalTo(3)),
                        )))))
    }

    @Test
    void testGetMetadataOnlyViewAccess() {
        // 5 fifth user has only VIEW permissions on study 2
        def user = accessLevelTestData.users[4]

        def exception = shouldFail AccessDeniedException, {
            mockCurrentUser(user.username) {
                dataExportController.getMetaData()
            }
        }

        assertThat exception.message, startsWith(
                "User user_${user.id} has no EXPORT permission on one of the result set")
    }

    @Test
    void testGetMetadataExportAccess() {
        // 6 sixth user has both VIEW and EXPORT permissions on study2
        def user = accessLevelTestData.users[5]

        mockCurrentUser(user.username) {
            dataExportController.getMetaData()
        }

        def slurper = new JsonSlurper()
        def result = slurper.parseText(dataExportController.response.text)

        assertThat result, hasEntry(
                equalTo('exportMetaData'),
                contains(
                        hasEntry('dataTypeId', 'CLINICAL')))
    }

    @Test
    void testCreateNewJobAdministrator() {
        // 1st user is an admin
        createNewJob accessLevelTestData.users[0]
    }

    @Test
    void testCreateNewJobOnlyViewAccess() {
        def exception = shouldFail AccessDeniedException, {
            createNewJob accessLevelTestData.users[4]
        }

        assertThat exception.message, startsWith(
                "User ${accessLevelTestData.users[4].username} has no EXPORT permission on one of the result set")
    }

    @Test
    void testCreateNewJobExportAccess() {
        createNewJob accessLevelTestData.users[5]
    }


    void createNewJob(user) {
        user.username = user.username.replace('-', '_')

        mockCurrentUser(user.username) {
            dataExportController.request.parameters = [analysis: 'DataExport']
            dataExportController.createnewjob()
            def jobName = new JsonSlurper().
                    parseText(dataExportController.response.text).
                    jobName
            dataExportController.response.reset()
            [
                    result_instance_id1        : study2QueryResult.id as String,
                    analysis                   : 'DataExport',
                    jobName                    : jobName,
                    selectedSubsetDataTypeFiles: '{subset: subset1, dataTypeId: CLINICAL, fileType: .TXT}',
                    selection                  : '{"subset1":{"clinical":{"selector":[]}}}',
            ].each { k, v -> dataExportController.params[k] = v }
            dataExportController.runDataExport()
        }
    }

    void mockCurrentUser(String username, Closure closure) {
        // so access to the query result retrieval is not denied
        study2QueryResult.queryInstance.userId = username

        def proxyMetaClass = ProxyMetaClass.getInstance(SpringSecurityService)
        proxyMetaClass.interceptor =
                new SpringSecurityProxyMetaClassInterceptor(username: username)
        proxyMetaClass.use springSecurityService, closure
    }
}
