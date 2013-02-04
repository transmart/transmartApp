package annotation
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


import annotation.AmData;
import annotation.AmTagAssociation;
import annotation.AmTagDisplayValue;
import annotation.AmTagItem;
import annotation.AmTagTemplate;
import annotation.AmTagTemplateAssociation
import annotation.AmTagValue;

import bio.BioData
import bio.BioMarker
import bio.Compound
import bio.ConceptCode
import bio.Disease

import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import com.recomdata.util.FolderType
import grails.converters.*
import groovy.xml.StreamingMarkupBuilder



class MetaDataController {

	def formLayoutService
	def amTagTemplateService
	def amTagItemService
	def fmFolderService
	def ontologyService
	def solrFacetService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		
    }
	
	def searchAction = 
	{
		redirect(action: "list", params: params)
	}
	
	/**
	 * Find the top 15 diseases with a case-insensitive LIKE
	 */
	def extSearch = {
		log.info "EXT SEARCH called"
		def paramMap = params
		log.info params
		
		def value = params.term?params.term.toUpperCase():''
		def codeTypeName = params.codeTypeName?params.codeTypeName:'';
		
		def observations = null;
		def conceptCodes = ConceptCode.executeQuery("FROM ConceptCode cc WHERE cc.codeTypeName = :codeTypeName and  upper(cc.codeName) LIKE :codeName order by codeTypeName", [codeTypeName: codeTypeName, codeName: "%"+value+"%"], [max: 10]);
		
		def itemlist = [];
		for (conceptCode in conceptCodes) {
			itemlist.add([id:conceptCode.uniqueId, keyword:conceptCode.codeName, sourceAndCode:conceptCode.uniqueId, category:"", display:""]);
		}
		
		render itemlist as JSON;
	}
	
	/**
	 * Find the top 15 diseases with a case-insensitive LIKE
	 */
	def bioCompoundSearch = {
		log.info "EXT bioCompoundSearch called"
		def paramMap = params
		log.info params
		
		def value = params.term?params.term.toUpperCase():''
		
		def observations = null;
		def compounds = Compound.executeQuery("FROM Compound cc WHERE upper(cc.codeName) LIKE :codeName order by codeName", [codeName: "%"+value+"%"], [max: 10]);
		
		def itemlist = [];
		for (compound in compounds) {
			itemlist.add([id:compound.uniqueId, keyword:compound.codeName, sourceAndCode:compound.uniqueId, category:"", display:""]);
		}
		
		render itemlist as JSON;
	}
	
	/**
	 * Find the top 15 diseases with a case-insensitive LIKE
	 */
	def bioDiseaseSearch = {
		log.info "EXT bioDiseaseSearch called"
		def paramMap = params
		log.info params
		
		def value = params.term?params.term.toUpperCase():''
			
		def observations = null;
		def diseases = Disease.executeQuery("FROM Disease cc WHERE upper(cc.disease) LIKE :disease order by disease", [disease: "%"+value+"%"], [max: 10]);
		
		def itemlist = [];
		for (disease in diseases) {
			itemlist.add([id:disease.uniqueId, keyword:disease.disease, sourceAndCode:disease.uniqueId, category:"", display:""]);
		}
		
		render itemlist as JSON;
	}
	
	/**
	 * Find the top 15 diseases with a case-insensitive LIKE
	 */
	def bioMarkerSearch = {
		log.info "EXT bioMarkerSearch called"
		def paramMap = params
		log.info params
		
		def value = params.term?params.term.toUpperCase():''
		
		def observations = null;
		def bioMarkers = BioMarker.executeQuery("FROM BioMarker cc WHERE upper(cc.name) LIKE :name order by name", [name: "%"+value+"%"], [max: 10]);
		log.info bioMarkers
		def itemlist = [];
		for (bioMarker in bioMarkers) {
			itemlist.add([id:bioMarker.uniqueId, keyword:bioMarker.name, sourceAndCode:bioMarker.uniqueId, category:"", display:""]);
		}
		
		render itemlist as JSON;
	}

}