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
  


import fm.FmFolderAssociation;
import grails.converters.*
import org.json.*;
import edu.mit.wi.haploview.*;
import annotation.AmTagItem;
import annotation.AmTagItemService;
import annotation.AmTagTemplateService;

class OntologyController {

    def index = { }
    def i2b2HelperService
    def springSecurityService
	def ontologyService
	def amTagTemplateService
	def amTagItemService
    
    def showOntTagFilter= {
    		def tagtypesc=[]
    		tagtypesc.add("ALL")
    		def tagtypes=i2b2.OntNodeTag.executeQuery("SELECT DISTINCT o.tagtype FROM i2b2.OntNodeTag as o order by o.tagtype")
    		tagtypesc.addAll(tagtypes)
    		def tags=i2b2.OntNodeTag.executeQuery("SELECT DISTINCT o.tag FROM i2b2.OntNodeTag o order by o.tag")  /*WHERE o.tagtype='"+tagtypesc[0]+"'*/
    		log.trace(tags as JSON)
    		render(template:'filter',model:[tagtypes:tagtypesc, tags:tags])
    }
    
    def ajaxGetOntTagFilterTerms = {
    		def tagtype = params.tagtype
    		log.trace("calling search for tagtype:"+tagtype)
       		def tags=i2b2.OntNodeTag.executeQuery("SELECT DISTINCT o.tag FROM i2b2.OntNodeTag o WHERE o.tagtype='"+tagtype+"' order by o.tag")
       		log.trace (tags as JSON)
       		render(template:'depSelectTerm', model:[tagtype:tagtype, tags:tags])
    }
    
    def ajaxOntTagFilter =
    {
    		log.trace("called ajaxOntTagFilter")
    		log.trace("tagterm:"+params.tagterm)
			def tagterm = params.tagterm
			def ontsearchterm = params.ontsearchterm
			def tagtype = params.tagtype
			def result = ontologyService.searchOntology(tagterm, [ontsearchterm], tagtype, 'JSON')
			render result as JSON
    }
    
    
    def getInitialSecurity=
    {
    		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
    		def result=i2b2HelperService.getAccess(i2b2HelperService.getRootPathsWithTokens(), user);
    		render result as JSON
    }
    def sectest=
    {
    		log.trace("KEYS:"+params.keys)
    		def keys=params.keys.toString().split(",");
    		def paths=[];
    		def access;
    		if(params.keys!="")
    		{
    		keys.each{ key ->
    			log.debug("in LOOP")
    			paths.add(i2b2HelperService.keyToPath(key))	
    		}	
    		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
    	
    		
    		 access=i2b2HelperService.getConceptPathAccessCascadeForUser(paths, user)
    		}
    		log.trace(access as JSON)
    }
	
	def showConceptDefinition =
	{
		def conceptPath=i2b2HelperService.keyToPath(params.conceptKey);
		def node=i2b2.OntNode.get(conceptPath);
		
		//Disabled check for trial - show all study metadata in the same way as the Browse view
		//def testtag=new i2b2.OntNodeTag(tag:'test', tagtype:'testtype');
		//node.addToTags(testtag);
		//node.save();
//		def trial=node.tags.find{ w -> w.tagtype =="Trial" }
//		if(trial!=null)
//		{
//			def trialid=trial.tag;
//			chain(controller:'trial', action:'trialDetailByTrialNumber', id:trialid)
//		}
		//Check for study by visual attributes
		if (node.visualattributes.contains("S")) {
			def accession = node.sourcesystemcd
			def study = bio.Experiment.findByAccession(accession.toUpperCase())
			def folder
			if (study) {
				folder = FmFolderAssociation.findByObjectUid(study.getUniqueId().uniqueId)?.fmFolder
			}
			else {
				render(status: 200, text: "No study definition found for accession: " + accession)
				return
			}
			
			def amTagTemplate = amTagTemplateService.getTemplate(folder.getUniqueId())
			List<AmTagItem> metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
			
			render(template:'showStudy', model:[folder:folder, bioDataObject:study, metaDataTagItems: metaDataTagItems])
		}
		else {
			render(template:'showDefinition', model:[tags:node.tags])
		}
	}
	
}
