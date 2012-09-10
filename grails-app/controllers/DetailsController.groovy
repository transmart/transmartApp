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
  

/*
 * $Id: DetailsController.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 */
import grails.converters.JSON

/**
 * This controller is used to display details data for genes, pathways, and compounds. For each of these there
 * is a corresponsing action and view which displays a window with tabs, e.g. "gene" action and view. And then
 * if a summary is define a summary action and view, e.g. compoundSumary action and view.
 *
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class DetailsController {

	def detailsService

	def gene = {
		def bioDataId = params?.id
		def altId = params?.altId
		def geneSymbol = ""
		def uniqueId = ""
		def geneId = ""
		if ((bioDataId == null || bioDataId.length() ==  0) && (altId != null & altId.length() > 0)) {
			// TODO: Add type criteria
			def result = org.transmart.biomart.BioMarker.findByPrimaryExternalId(altId)
			if (result != null) {
				bioDataId = result.id.toString()
			}
		}
		if (bioDataId != null && bioDataId.length() > 0) {
			def marker = org.transmart.biomart.BioMarker.get(Long.valueOf(bioDataId))
		//	def searchKeyword = org.transmart.searchapp.SearchKeyword.findByBioDataId(Long.valueOf(bioDataId))
		//	if (searchKeyword != null) {
		//		geneSymbol = searchKeyword.keyword
		//		uniqueId = searchKeyword.uniqueId
		//		geneId = uniqueId.substring(uniqueId.indexOf(":") + 1)
		//	}
			if(marker!=null){
				geneSymbol = marker.name;
				geneId = marker.primaryExternalId;
			}
		}
		def hydraGeneID = detailsService.getHydraGeneID(bioDataId)

	//	println("hydraGene:"+hydraGeneID)
		render(view:"gene", model:[id:bioDataId, symbol:geneSymbol, geneId:geneId, hydraGeneID:hydraGeneID])
	}

	def pathway = {
		def bioDataId = params.id
		def pathwaySymbol = ""
		def uniqueId = ""
		def pathwayType = ""
		def searchKeyword = org.transmart.searchapp.SearchKeyword.findByBioDataId(Long.valueOf(bioDataId))
		if (searchKeyword != null) {
			pathwaySymbol = searchKeyword.keyword
			uniqueId = searchKeyword.uniqueId
			pathwayType = uniqueId.substring(uniqueId.indexOf(":") + 1, uniqueId.lastIndexOf(":"))
		}
		render(view:"pathway", model:[id:bioDataId, symbol:pathwaySymbol, type:pathwayType])
	}

	def pathwaySummary = {
		def bioDataId = params.id
		def pathway = org.transmart.biomart.BioMarker.get(Long.valueOf(bioDataId))
		def genes
		if (pathway != null) {
			def query = "select k from org.transmart.searchapp.SearchKeyword k, org.transmart.biomart.BioDataCorrelation c where k.bioDataId=c.associatedBioDataId and c.bioDataId=?"
			genes = org.transmart.searchapp.SearchKeyword.executeQuery(query, Long.valueOf(bioDataId))
		}
		render(view:"pathwaySummary", model:[pathway:pathway,genes:genes])
	}

	def compound = {
		def bioDataId = params.id
		def compoundSymbol = ""
		def uniqueId = ""
		def searchKeyword = org.transmart.searchapp.SearchKeyword.findByBioDataId(Long.valueOf(bioDataId))
		if (searchKeyword != null) {
			compoundSymbol = searchKeyword.keyword
			uniqueId = searchKeyword.uniqueId
		}
		render(view:"compound", model:[id:bioDataId, symbol:compoundSymbol])
	}

	def compoundSummary = {
		def bioDataId = params.id
		def compound = org.transmart.biomart.Compound.get(Long.valueOf(bioDataId))
		render(view:"compoundSummary", model:[compound:compound])
	}
}
