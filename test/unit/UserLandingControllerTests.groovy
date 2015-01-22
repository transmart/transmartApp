import grails.test.mixin.TestFor
import org.junit.Before

@TestFor(UserLandingController)
class UserLandingControllerTests {

    @Before
    void setUp() {
        grailsApplication.config.clear()
    }

    void testDefaultLandingPage() {
        assertEquals '/RWG', controller.userLandingPath
    }

    void testHideBrowseTab() {
        grailsApplication.config.ui.tabs.browse.hide = true
        assertEquals '/datasetExplorer', controller.userLandingPath
    }

    void testPresetLandingPage() {
        def expectedPath = '/custom-path'
        grailsApplication.config.com.recomdata.defaults.landing = expectedPath
        assertEquals expectedPath, controller.userLandingPath
    }

}
