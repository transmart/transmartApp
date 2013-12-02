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
  

import org.transmart.searchapp.AuthUser;

import org.transmart.searchapp.SearchKeyword
import org.transmart.searchapp.GeneSignature
import org.transmart.biomart.Experiment
import org.transmart.biomart.Compound
import org.transmart.biomart.Disease

public class SearchHelpController{

	def springSecurityService
	def geneSignatureService

	def list = {}

	def listAllTrials = {
		def all = SearchKeyword.findAllByDataCategory("TRIAL", [sort:"keyword", cache:'read-only'])
		render(view:'trialhelp', model:[trials:all])
	}

	def listAllDiseases = {
		def all = SearchKeyword.findAllByDataCategory("DISEASE", [sort:"keyword", cache:'read-only'])
		render(view:'diseasehelp', model:[diseases:all])
	}

	def listAllCompounds = {
		def all = SearchKeyword.findAllByDataCategory("COMPOUND", [sort:"keyword", cache:'read-only'])
		render(view:'compoundhelp', model:[compounds:all])
	}
	
	def listAllPathways = {
		def all = SearchKeyword.findAllByDataCategory("PATHWAY", [sort:"keyword", cache:'read-only'])
		render(view:'pathwayhelp', model:[pathways:all])
	}

	def listAllGeneSignatures = {
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)		
		def signatures = geneSignatureService.listPermissionedGeneSignatures(user)

		def mapKeywordsGS = new HashMap()
		def mapKeywordsGL = new HashMap()
		def keyword

		signatures.each {
			keyword = SearchKeyword.findByUniqueId(it.uniqueId)
			mapKeywordsGS.putAt(it.id, keyword)
			if (it.foldChgMetricConceptCode != GeneSignatureService.METRIC_CODE_GENE_LIST) {
				keyword = SearchKeyword.findByUniqueId(GeneSignature.DOMAIN_KEY_GL+":"+it.id)
				mapKeywordsGL.putAt(it.id, keyword)
			}
		}
		render(view:'geneSigHelp', model:[signatures:signatures, 
			gsMap:mapKeywordsGS, glMap:mapKeywordsGL])
	}
}