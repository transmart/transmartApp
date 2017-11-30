import DataAttestation
import org.springframework.web.servlet.support.RequestContextUtils
import org.transmart.searchapp.AuthUser

class DataAttestationController {

    def springSecurityService
    def messageSource

    def index = {
        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
    	if (DataAttestation.needsDataAttestation(user))
            render(view:"attestation")
        else
            redirect(uri:'/datasetExplorer/index');
    }
    def agree = {
        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        DataAttestation.updateOrAddNewAgreementDate(user)
        if (springSecurityService.currentUser.changePassword) {
            flash.message = messageSource.getMessage('changePassword', new Objects[0], RequestContextUtils.getLocale(request))
            redirect(controller: 'changeMyPassword')
        } else {
            redirect(uri: '/datasetExplorer/index')
        }
    }

    def disagree = {
        redirect(uri: '/logout')
    }

	def clear = {
	    def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
    	def da = DataAttestation.findByAuthUserId(user.id)
    	if (da != null) {
            try {
                da.delete(flush: true)
            }
		    catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "Could not delete user ${user}"
            }
        }
        redirect(uri: '/logout')		
	}    
}
