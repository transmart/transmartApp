import org.springframework.web.servlet.support.RequestContextUtils
import org.transmart.searchapp.AccessLog

class UserLandingController {
    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService
    def messageSource

    private String getUserLandingPath() {
        grailsApplication.config.with {
            com.recomdata.defaults.landing ?: ui.tabs.browse.hide ? '/datasetExplorer' : '/RWG'
        }
    }

    def index = {
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "Login",
                eventmessage: request.getHeader("user-agent"),
                accesstime: new Date()).save()
        def skip_disclaimer = grailsApplication.config.com.recomdata?.skipdisclaimer ?: false;
        if (skip_disclaimer) {
            if (springSecurityService.currentUser.changePassword) {
                flash.message = messageSource.getMessage('changePassword', new Objects[0], RequestContextUtils.getLocale(request))
                redirect(controller: 'changeMyPassword')
            } else {
                redirect(uri: userLandingPath)
            }
        } else {
            redirect(uri: '/userLanding/disclaimer.gsp')
        }
    }
    def agree = {
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "Disclaimer accepted",
                accesstime: new Date()).save()
        if (springSecurityService.currentUser.changePassword) {
            flash.message = messageSource.getMessage('changePassword', new Objects[0], RequestContextUtils.getLocale(request))
            redirect(controller: 'changeMyPassword')
        } else {
            redirect(uri: userLandingPath)
        }
    }

    def disagree = {
        new AccessLog(username: springSecurityService.getPrincipal().username, event: "Disclaimer not accepted",
                accesstime: new Date()).save()
        redirect(uri: '/logout')
    }

    def checkHeartBeat = {
        render(text: "OK")
    }

}
