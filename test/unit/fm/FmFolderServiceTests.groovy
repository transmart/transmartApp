package fm

import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.transmart.biomart.BioData
import org.transmart.biomart.Experiment
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.Role

import static com.recomdata.util.FolderType.*

@TestFor(FmFolderService)
//@Mock([FmFolder, FmFolderAssociation, BioData, Experiment])
@TestMixin(GrailsUnitTestMixin)
@Ignore
class FmFolderServiceTests {
    AuthUser user
    List<FmFolder> studyFolders
    def program1, study1, assay111, folder121, study2, analysys122

    @Before
    void setUp() {
        user = new AuthUser()

        program1 = new FmFolder(
                id: 1L,
                folderName: 'Test program 1',
                folderFullName: '\\FOL:1\\',
                folderLevel: 0,
                folderType: PROGRAM.name())

        study1 = new FmFolder(
                id: 11L,
                folderName: 'Test study 11',
                folderFullName: '\\FOL:1\\FOL:11\\',
                folderLevel: 1,
                folderType: STUDY.name(),
                parent: program1)
        assay111 = new FmFolder(
                id: 111L,
                folderName: 'Test assay 111',
                folderFullName: '\\FOL:1\\FOL:11\\FOL:111\\',
                folderLevel: 2,
                folderType: ASSAY.name(),
                parent: study1)
        folder121 = new FmFolder(
                id: 121L,
                folderName: 'Test folder 121',
                folderFullName: '\\FOL:1\\FOL:12\\FOL:121\\',
                folderLevel: 2,
                folderType: FOLDER.name(),
                parent: study1)

        study2 = new FmFolder(
                id: 12L,
                folderName: 'Test study 12',
                folderFullName: '\\FOL:1\\FOL:12\\',
                folderLevel: 1,
                folderType: STUDY.name(),
                parent: program1)
        analysys122 = new FmFolder(
                id: 122L,
                folderName: 'Test analysys 122',
                folderFullName: '\\FOL:1\\FOL:12\\FOL:122\\',
                folderLevel: 2,
                folderType: ANALYSIS.name(),
                parent: study2)

        studyFolders = [study1, study2]

        def allFolders = [program1, study1, assay111,
                          folder121, study2, analysys122]
        allFolders*.description = 'description'
        allFolders*.save(failOnError: true)
        // setup authorization information for study1
        def study2folderAssociation = new FmFolderAssociation(
                fmFolder: study1,
                objectType: Experiment.class.name,
                objectUid: 'Omicsoft:STUDY1')
        study2folderAssociation.save(failOnError: true)
        def bioData = new BioData(
                uniqueId: study2folderAssociation.objectUid,
                type: 'EXP')
        bioData.id = -142L
        bioData.save(failOnError: true)
        def bioExperiment = new Experiment(accession: 'STUDY1')
        bioExperiment.id = bioData.id
        bioExperiment.save(failOnError: true)
    }

    @Test
    void testGetAccessLevelInfoForFolders_no_folders() {
        assertEquals [:], service.getAccessLevelInfoForFolders(user, [])
    }

    @Test
    void testGetAccessLevelInfoForFolders_admin() {
        user.authorities = [new Role(authority: Role.ADMIN_ROLE)]

        def foldersMap = service.getAccessLevelInfoForFolders(user, studyFolders)

        Assert.assertEquals 2, foldersMap?.size()
        assertEquals studyFolders, foldersMap.keySet().toList()
        assertEquals(['ADMIN', 'ADMIN'], foldersMap.values().toList())
    }

    @Test
    void testGetAccessLevelInfoForFolders_dse_admin() {
        user.authorities = [new Role(authority: Role.DS_EXPLORER_ROLE)]

        def foldersMap = service.getAccessLevelInfoForFolders(user, studyFolders)

        Assert.assertEquals 2, foldersMap?.size()
        assertEquals studyFolders, foldersMap.keySet().toList()
        assertEquals(['ADMIN', 'ADMIN'], foldersMap.values().toList())
    }

    @Test
    void testGetAccessLevelInfoForFolders_not_applicable() {
        user.authorities = [new Role(authority: Role.ADMIN_ROLE), new Role(authority: Role.DS_EXPLORER_ROLE)]

        def foldersMap = service.getAccessLevelInfoForFolders(user, [program1])

        Assert.assertEquals 1, foldersMap?.size()
        assertEquals([program1], foldersMap.keySet().toList())
        assertEquals(['NA'], foldersMap.values().toList())
    }

    @Test
    void testGetAccessLevelInfoForFolders_locked() {
        def i2b2HelperServiceControll = mockFor(Class.forName('I2b2HelperService'))
        i2b2HelperServiceControll.demand.getSecureTokensForStudies { param ->
            assert param == ['STUDY1']
            return [STUDY1: 'EXP:STUDY1']
        }
        i2b2HelperServiceControll.demand.getSecureTokensWithAccessForUser { passedUser ->
            assert user == passedUser
            ['EXP:PUBLIC': 'OWN'] // don't return a token for STUDY1 for this user; will be locked
        }

        service.i2b2HelperService = i2b2HelperServiceControll.createMock()

        def foldersMap = service.getAccessLevelInfoForFolders(user, studyFolders)

        Assert.assertEquals 2, foldersMap?.size()
        assertEquals studyFolders, foldersMap.keySet().toList()
        // study2 does not have a folder association linking it to a biomart
        // object and from there to a study. Should be locked too
        assertEquals(['LOCKED', 'LOCKED'], foldersMap.values().toList())
    }

    // TODO: test access granted to regular users

}
