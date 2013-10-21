import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.transmart.searchapp.AccessLog;

/**
 * Logout Controller just writes an entry to the log and redirects to the login page (Identity Vault or form based)
 */
class LogoutController {

	def springSecurityService

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */	
	def index = {		
		new AccessLog(username: springSecurityService.getPrincipal().username, event:"Logout", accesstime:new Date()).save()		
		redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl 
	}
}
