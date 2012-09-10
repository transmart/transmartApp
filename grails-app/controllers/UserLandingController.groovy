import org.transmart.searchapp.AccessLog;

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
  


/**
 * Central landing zone controller for post login events
 * 
 * $Id: UserLandingController.groovy 10133 2011-10-20 21:34:43Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 10133 $
 */
class UserLandingController {
	/**
	 * Dependency injection for the springSecurityService.
	 */
    def springSecurityService
	
	   def index = {
            new AccessLog(username: springSecurityService.getPrincipal().username, event:"Login",
                  eventmessage: request.getHeader("user-agent"),
                  accesstime:new Date()).save()
                  def skip_disclaimer = grailsApplication.config.com.recomdata?.skipdisclaimer?:false;
                  if(skip_disclaimer){
                        redirect(uri:'/search');     
                  }else{
                  redirect(uri: '/userLanding/disclaimer.gsp')
                  }
      }
	def agree = {
		new AccessLog(username: springSecurityService.getPrincipal().username, event:"Disclaimer accepted",
			accesstime:new Date()).save()				
		redirect(uri: '/search')
	}
	
	def disagree = {
		new AccessLog(username: springSecurityService.getPrincipal().username, event:"Disclaimer not accepted",			
			accesstime:new Date()).save()
	    redirect(uri: '/logout')
	}
}
