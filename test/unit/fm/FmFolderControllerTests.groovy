package fm

import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.apache.tools.ant.filters.StringInputStream
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize

@TestFor(FmFolderController)
@Mock(FmFile)
class FmFolderControllerTests {

    @Test
    void basicDownload() {
        String originalFilename = 'test original Name 丈.pdf'
        Long fileSize = 2009
        def file = new FmFile(
                displayName: 'test display name',
                originalName: originalFilename,
                fileSize: fileSize,
        )
        assertNotNull file.save()
        controller.fmFolderService = new FmFolderService()
        controller.fmFolderService.metaClass.getFile = { FmFile f ->
            def bogusFile = new File('bogus')
            bogusFile.metaClass.newInputStream = { ->
                new StringInputStream('foobar')
            }
            bogusFile
        }

        params.id = file.id
        controller.downloadFile()

        assertThat response.headers('Content-disposition'), hasSize(1)
        assertThat response.header('Content-disposition').decodeURL(),
                equalTo("attachment; filename*=UTF-8''$originalFilename".toString())
        assertThat response.header('Content-length'), is(equalTo(fileSize as String))
        assertEquals response.text, 'foobar'
    }
}
