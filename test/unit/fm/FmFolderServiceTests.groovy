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


package fm

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import org.junit.Before
import org.junit.Test
import org.transmart.searchapp.AuthUser
import org.transmart.searchapp.Role

import static com.recomdata.util.FolderType.*

@TestFor(FmFolderService)
@Mock(FmFolderAssociation)
@TestMixin(GrailsUnitTestMixin)
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
    }

    @Test
    void testGetAccessLevelInfoForFolders_no_folders() {
        assertEquals [:], service.getAccessLevelInfoForFolders(user, [])
    }

    @Test
    void testGetAccessLevelInfoForFolders_admin() {
        user.authorities = [new Role(authority: Role.ADMIN_ROLE)]

        def foldersMap = service.getAccessLevelInfoForFolders(user, studyFolders)

        assertEquals 2, foldersMap?.size()
        assertEquals studyFolders, foldersMap.keySet().toList()
        assertEquals(['ADMIN', 'ADMIN'], foldersMap.values().toList())
    }

    @Test
    void testGetAccessLevelInfoForFolders_dse_admin() {
        user.authorities = [new Role(authority: Role.DS_EXPLORER_ROLE)]

        def foldersMap = service.getAccessLevelInfoForFolders(user, studyFolders)

        assertEquals 2, foldersMap?.size()
        assertEquals studyFolders, foldersMap.keySet().toList()
        assertEquals(['ADMIN', 'ADMIN'], foldersMap.values().toList())
    }

    @Test
    void testGetAccessLevelInfoForFolders_not_applicable() {
        user.authorities = [new Role(authority: Role.ADMIN_ROLE), new Role(authority: Role.DS_EXPLORER_ROLE)]

        def foldersMap = service.getAccessLevelInfoForFolders(user, [program1])

        assertEquals 1, foldersMap?.size()
        assertEquals([program1], foldersMap.keySet().toList())
        assertEquals(['NA'], foldersMap.values().toList())
    }

    @Test
    void testGetAccessLevelInfoForFolders_locked() {
        def i2b2HelperServiceControll = mockFor(Class.forName('I2b2HelperService'))

        service.i2b2HelperService =  i2b2HelperServiceControll.createMock()

        def foldersMap = service.getAccessLevelInfoForFolders(user, studyFolders)

        assertEquals 2, foldersMap?.size()
        assertEquals studyFolders, foldersMap.keySet().toList()
        assertEquals(['LOCKED', 'LOCKED'], foldersMap.values().toList())
    }
}
