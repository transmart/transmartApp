package org.transmart.dataexport

import com.recomdata.transmart.data.export.HighDimExportService
import org.gmock.WithGMock
import org.junit.Test
import org.transmartproject.core.ontology.OntologyTerm
import org.transmartproject.core.ontology.Study

import java.util.regex.Pattern

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

@WithGMock
class HighDimExportServiceUnitTests {

    HighDimExportService testee = new HighDimExportService()

    String forbiddenFileNameSymbols = '\"/[]:;|=,'

    @Test
    void testRelativeFolderPathForAcrossStudyNode() {
        String testFullName = "\\Clinical Information\\${forbiddenFileNameSymbols}\\"

        String relativePath = testee.getRelativeFolderPathForSingleNode([
                getFullName: { testFullName },
                getStudy   : { null },
        ] as OntologyTerm)

        def matcher = relativePath =~ "Clinical(.)Information${Pattern.quote(File.separator)}(.+)"
        assertTrue relativePath, matcher.matches()

        assertFalse forbiddenFileNameSymbols.contains(matcher[0][1])

        String encodedFolderName = matcher[0][2]
        assertThat encodedFolderName, not(isEmptyOrNullString())

        Set forbiddenSymbolsFound = (encodedFolderName as List).intersect(forbiddenFileNameSymbols as List)
        assertThat "Forbidden symbols ${forbiddenSymbolsFound} are found in folder name: ${relativePath}",
                forbiddenSymbolsFound, hasSize(0)
    }

    @Test
    void testRelativeFolderPathForTheNodeInsideStudy() {
        String studyFolder = '\\Test Studies\\Study-1\\'
        String testFullName = "${studyFolder}Sub Folder\\${forbiddenFileNameSymbols}\\"

        String relativePath = testee.getRelativeFolderPathForSingleNode([
                getFullName: { testFullName },
                getStudy   : {
                    [
                            getOntologyTerm : {
                                [
                                        getFullName: { studyFolder }
                                ] as OntologyTerm
                            }
                    ] as Study
                },
        ] as OntologyTerm)

        def matcher = relativePath =~ "Sub(.)Folder${Pattern.quote(File.separator)}(.+)"
        assertTrue relativePath, matcher.matches()

        assertFalse forbiddenFileNameSymbols.contains(matcher[0][1])

        String encodedFolderName = matcher[0][2]
        assertThat encodedFolderName, not(isEmptyOrNullString())

        Set forbiddenSymbolsFound = (encodedFolderName as List).intersect(forbiddenFileNameSymbols as List)
        assertThat "Forbidden symbols ${forbiddenSymbolsFound} are found in folder name: ${relativePath}",
                forbiddenSymbolsFound, hasSize(0)
    }
}
