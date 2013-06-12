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
  

package annotation

class AmTagTemplateService {

    boolean transactional = true

    def serviceMethod() {

    }
	
	def getTemplate(String key){
		
		log.info "Searching amTagTemplateAssociation for " +  key
		
		def amTagTemplateAssociation 
		def amTagTemplate
		
		if(key)
		{
			amTagTemplateAssociation = AmTagTemplateAssociation.findByObjectUid(key)
			log.info "amTagTemplateAssociation = " + amTagTemplateAssociation + " for key = " + key	
		}
		else
		{
			log.error "Unable to retrieve an AmTagTemplateAssociation with a null key value"
		}
		
		if(amTagTemplateAssociation)
		{
			log.info "Searching amTagTemplate"
			amTagTemplate = AmTagTemplate.get(amTagTemplateAssociation.tagTemplateId)
			log.info "amTagTemplate = " + amTagTemplate.toString()
			log.info "amTagTemplate.tagItems = " + amTagTemplate.amTagItems
			
		}
		else
		{
			log.error "AmTagTemplate is null for tag template association = " + key
		} 
		
		return amTagTemplate
	}
	
}
