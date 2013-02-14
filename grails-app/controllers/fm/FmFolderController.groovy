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
package fm

import annotation.AmData;
import annotation.AmTagAssociation;
import annotation.AmTagDisplayValue;
import annotation.AmTagItem;
import annotation.AmTagTemplate;
import annotation.AmTagTemplateAssociation
import annotation.AmTagValue;
import auth.AuthUser;

import bio.BioData
import bio.ConceptCode
import bio.Experiment
import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import com.recomdata.util.FolderType
import grails.converters.*
import groovy.xml.StreamingMarkupBuilder
import grails.plugins.springsecurity.SpringSecurityService

import org.apache.commons.lang.StringUtils

class FmFolderController {

	def formLayoutService
	def amTagTemplateService
	def amTagItemService
	def fmFolderService
	def ontologyService
	def solrFacetService
	def springSecurityService
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
		
        [fmFolderInstanceList: FmFolder.list(params), fmFolderInstanceTotal: FmFolder.count()]
    }
	
	
	public String serializeFoldersToXMLFile() 
	{
		def writer = new FileWriter("c:\\temp\\SerializedAsXML.xml")

//		List<FmFolder> folderList = FmFolder.list()
		def fmFolderInstance = FmFolder.get(8)
		

		def builder = new StreamingMarkupBuilder().bind {
	//	mkp.xmlDeclaration()
		unescaped << '<fmFolders>'
//		folderList.each {folder ->
		out << fmFolderInstance
//		}
		unescaped << '</fmFolders>'
		}
		writer << builder
		
		return builder.toString()
	}
	

    def create = {
		log.info "Creating == " + params
		
		def fmFolderInstance = new FmFolder()
		fmFolderInstance.properties = params

        return [fmFolderInstance: fmFolderInstance]
    }

	def createAnalysis = {
		log.info "createAnalysis called"
		log.info "params = " + params
	
		//log.info "** action: expDetail called!"
		
		def folder = new FmFolder()
		folder.folderType = FolderType.ANALYSIS.name()
		def parentFolder = FmFolder.get(params.folderId)
		folder.parent = parentFolder
		def bioDataObject = new bio.BioAssayAnalysis()
		def amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.ANALYSIS.name())
		def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
		def title = "Create Analysis"
		def templateType = "createAnalysisForm"
		def measurements  = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT platformType FROM BioAssayPlatform as p ORDER BY p.platformType")
		def vendors = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT vendor FROM BioAssayPlatform as p ORDER BY p.vendor")
		def technologies = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ORDER BY p.platformTechnology")
		def platforms = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT name FROM BioAssayPlatform as p ORDER BY p.name")

		log.info measurements
		log.info technologies
		log.info vendors
		log.info platforms
		
		render(template: "createAnalysis", model:[bioDataObject:bioDataObject, measurements:measurements, technologies:technologies, vendors:vendors, platforms:platforms, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
	}

	def createAssay = {
		log.info "createAssay called"
		log.info "params = " + params
	
		//log.info "** action: expDetail called!"
		
		def folder = new FmFolder()
		folder.folderType = FolderType.ASSAY.name()
		def parentFolder = FmFolder.get(params.folderId)
		folder.parent = parentFolder
		def bioDataObject = folder
		def amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.ASSAY.name())
		def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
		def title = "Create Assay"
		def templateType = "createAssayForm"
		def measurements  = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT platformType FROM BioAssayPlatform as p ORDER BY p.platformType")
		def vendors = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT vendor FROM BioAssayPlatform as p ORDER BY p.vendor")
		def technologies = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ORDER BY p.platformTechnology")
		def platforms = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT name FROM BioAssayPlatform as p ORDER BY p.name")

		log.info measurements
		log.info technologies
		log.info vendors
		log.info platforms

		render(template: "createAssay", model:[bioDataObject:bioDataObject, measurements:measurements, technologies:technologies, vendors:vendors, platforms:platforms, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
	}

	def createFolder = {
		log.info "createFolder called"
		log.info "params = " + params
		//log.info "** action: expDetail called!"
		
		def folder = new FmFolder()
		folder.folderType = FolderType.FOLDER.name()
		def parentFolder = FmFolder.get(params.folderId)
		folder.parent = parentFolder
		
		def bioDataObject = folder
		def amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.FOLDER.name())
		if(!amTagTemplate) log.error ("Unable to find tag template for folder type = ")
		
		def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
		def title = "Create Folder"
		def templateType = "createFolderForm"
		render(template: "createFolder", model:[bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
	}

		def createStudy = {
		log.info "createStudy called"
		log.info "params = " + params		
		//log.info "** action: expDetail called!"
		
		def folder = new FmFolder()
		folder.folderType = FolderType.STUDY.name()
		def parentFolder = FmFolder.get(params.folderId)
		folder.parent = parentFolder
		def bioDataObject = new bio.Experiment()
		def amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.STUDY.name())
		def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
		def title = "Create Study"
		def templateType = "createStudyForm"
		render(template: "createStudy", model:[bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
	}

		def createProgram = {
			log.info "createProgram called"
			log.info "params = " + params
			//log.info "** action: expDetail called!"
			
			def folder = new FmFolder()
			folder.folderType = FolderType.PROGRAM.name()
			def bioDataObject = folder
			def amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.PROGRAM.name())
			def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
			render(template: "createProgram", model:[bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
		}
	
		
	private createAmTagTemplateAssociation(folderType, folder)
	{
		def amTagTemplate = AmTagTemplate.findByTagTemplateType(folderType)
		AmTagTemplateAssociation atta = new AmTagTemplateAssociation(tagTemplateId:amTagTemplate.id, objectUid:folder.getUniqueId())
		
		if(!atta.save(flush:true))
		{
			atta.errors.each {
				log.error it
			}
		}
	}
	
	private Object saveMetaData(FmFolder folder, bioDataObject, params)
	{
		log.info "saveMetaData called"
		def paramMap = params
		def folderId = folder.id
		def amTagTemplate
		List<AmTagItem> metaDataTagItems
		
			if(folder)
			{
				def folderAssociation = FmFolderAssociation.findByFmFolder(folder)
				
				if(!bioDataObject)
				{
					bioDataObject = getBioDataObject(folder)
				}
				
				amTagTemplate = AmTagTemplate.findByTagTemplateType(folder.folderType)
				if(!amTagTemplate) log.error ("Unable to find tag template for folder type = ")
			    metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
		
				log.info metaDataTagItems
				
				//Use metaDataTagItems to update fields
				for (tagItem in metaDataTagItems) {
					log.info tagItem.id + " " + tagItem.tagItemType + " " + tagItem.tagItemSubtype
					def newValue = null
					if (tagItem.tagItemType.equals('FIXED')) {
						newValue = params."${tagItem.tagItemAttr}"
						log.info "SAVING FIXED -- ${tagItem.tagItemAttr} == " + newValue
						if (newValue != null) {
							def value = ""
							if (tagItem.tagItemSubtype.equals('MULTIPICKLIST'))
							{
								newValue = params.list("${tagItem.tagItemAttr}")
								if (newValue != null && newValue != "" && newValue.size() > 0)
								{
									newValue.each {
									if(value != ""){value += "|"}
										value += it
									}
								}
							}
							else
							{
								value = newValue
							}
							
							log.info "SAVING FIXED -- ${tagItem.tagItemAttr} == " + value
							
							bioDataObject."${tagItem.tagItemAttr}" = value
				
						}
					}
					else if (tagItem.tagItemType.equals('CUSTOM'))
					{
						newValue = params."amTagItem_${tagItem.id}"
						if(tagItem.tagItemSubtype.equals('FREETEXT')||tagItem.tagItemSubtype.equals('FREETEXTAREA'))
						{
								
							// Save the new tag value
							if (newValue != null && newValue != "")
							{
								AmTagValue newTagValue = new AmTagValue(value: newValue)
								if(newTagValue.save(flush: true))
								{
										//Create a new AmTagValue and point to it
									log.info "'CUSTOM', subjectUid: " + folder.getUniqueId() + " tagItemId: " + tagItem.id + " objectUid: " + newValue
									AmTagAssociation.executeUpdate ("delete from AmTagAssociation as ata where ata.objectType=:objectType and ata.subjectUid=:subjectUid and ata.tagItemId=:tagItemId", [objectType: "BIO_CONCEPT_CODE", subjectUid: folder.getUniqueId(),tagItemId: tagItem.id])
									AmTagAssociation ata = new AmTagAssociation(objectType: 'BIO_CONCEPT_CODE', subjectUid: folder.getUniqueId(), objectUid: newTagValue.getUniqueId(), tagItemId: tagItem.id)
									if(!ata.save(flush: true))
									{
										ata.errors.each
										{
											log.error it
										}
									}
								}
								else
								{
									newTagValue.errors.each
									{
										log.error it
									}
								}
							}
	
						}
						else if(tagItem.tagItemSubtype.equals('PICKLIST'))
						{
							// Save the new tag value
							if (newValue != null && newValue != "")
							{
						
							    log.info "'CUSTOM', subjectUid: " + folder.getUniqueId() + " tagItemId: " + tagItem.id + " objectUid: " + newValue
								AmTagAssociation.executeUpdate ("delete from AmTagAssociation as ata where ata.objectType=:objectType and ata.subjectUid=:subjectUid and ata.tagItemId=:tagItemId", [objectType: "BIO_CONCEPT_CODE", subjectUid: folder.getUniqueId(),tagItemId: tagItem.id])
								
								AmTagAssociation ata1 = new AmTagAssociation(objectType: 'BIO_CONCEPT_CODE', subjectUid: folder.getUniqueId(), objectUid: newValue, tagItemId: tagItem.id)
									
								if (!ata1.save(flush:true)) {
									ata1.errors.each {
										println it
									}
								}
							}
						}
						else if(tagItem.tagItemSubtype.equals('MULTIPICKLIST'))
						{
							newValue = params.list("amTagItem_${tagItem.id}")
							// Save the new tag value
							if (newValue != null && newValue != "" && newValue.size() > 0)
							{
								log.info "REMOVING - objectType: CUSTOM " + " subjectUid = " + folder.getUniqueId() + " tagItemId: " + tagItem.id + " newValue: " + newValue.size() + " " + newValue
								AmTagAssociation.executeUpdate ("delete from AmTagAssociation as ata where ata.objectType=:objectType and ata.subjectUid=:subjectUid and ata.tagItemId=:tagItemId", [objectType: "BIO_CONCEPT_CODE", subjectUid: folder.getUniqueId(),tagItemId: tagItem.id])
								
								newValue.each
								{
									log.info "NEWVALUE " + it
									if(it)
									{
										AmTagAssociation ata1 = new AmTagAssociation(objectType: 'BIO_CONCEPT_CODE', subjectUid: folder.getUniqueId(), objectUid: it, tagItemId: tagItem.id)
										
										if (!ata1.save(flush:true)) {
											ata1.errors.each {
												println it
											}
										}
									}
									else
									 {
										 log.error("amTagItem_${tagItem.id} is null")
									 }
								}
							}
						}
						else
						{
							// TODO: throw an exception
							// unrcognized subtype
						}
						

					}
					else
					{

						newValue = params.list("amTagItem_${tagItem.id}")
						//Look for new value by tag item ID
						log.info "SAVING BUSINESS OBJECT == " + newValue
						// Save the new tag value
						if (newValue != null && newValue != "" && newValue.size() > 0)
						{
							log.info "REMOVING - objectType: CUSTOM " + " subjectUid = " + folder.getUniqueId() + " tagItemId: " + tagItem.id + " newValue: " + newValue.size() + " " + newValue
							AmTagAssociation.executeUpdate ("delete from AmTagAssociation as ata where ata.objectType=:objectType and ata.subjectUid=:subjectUid and ata.tagItemId=:tagItemId", [objectType: tagItem.tagItemType, subjectUid: folder.getUniqueId(),tagItemId: tagItem.id])
							
							newValue.each
							{
								log.info "NEWVALUE " + it
								if(it)
								{
									AmTagAssociation ata1 = new AmTagAssociation(objectType: tagItem.tagItemType, subjectUid: folder.getUniqueId(), objectUid: it, tagItemId: tagItem.id)
									
									if (!ata1.save(flush:true)) {
										ata1.errors.each {
											println it
										}
									}
								}
								else
								{
									log.error("amTagItem_${tagItem.id} is null")
								}
							}
						}

					}
					
				}
				
				if (bioDataObject.save(flush: true))
				{
					log.info "bioDataObject.id = " + bioDataObject.id + " Meta data saved == " + bioDataObject.getUniqueId()
					
				}
				else
				{
					log.info bioDataObject
					bioDataObject.errors.each {
						log.error it
					}
				}

			}	
			
			return bioDataObject
	}
	
    def save = {
		log.info params
        def fmFolderInstance = new FmFolder(params)
        if (fmFolderInstance.save(flush: true)) {
            redirect(action: "show", id: fmFolderInstance.id)
        }
        else {
            render(view: "create", model: [fmFolderInstance: fmFolderInstance])
        }
    }

	def saveAssay = {
		log.info "saveAssay called"
		log.info params
		def parentFolder = FmFolder.get(params.parentId)
		log.info("parentFolder = " + parentFolder)
		def fmFolderInstance = new FmFolder(params)
		if(parentFolder)
		{
			// fmFolderInstance.folderFullName = "folder"
			fmFolderInstance.folderLevel = parentFolder.folderLevel + 1
			fmFolderInstance.folderType = FolderType.ASSAY.name()
			fmFolderInstance.parent = parentFolder
		}
		else
		{
			log.error "Parent folder is null"
		}

		if (fmFolderInstance.save(flush: true)) {
			log.info "Assay saved"
			createAmTagTemplateAssociation(FolderType.ASSAY.name(), fmFolderInstance)
			saveMetaData(fmFolderInstance, null, params)
			
			def result = [id: fmFolderInstance.id, parentId: fmFolderInstance.parent.id]
			render result as JSON
			return
		}
		else {
			fmFolderInstance.errors.each {
				log.error it
			}
			render(view: "create", model: [fmFolderInstance: fmFolderInstance])
		}
	}

	def saveFolder = {
		log.info "saveFolder called"
		log.info params
		def parentFolder = FmFolder.get(params.parentId)
		log.info("parentFolder = " + parentFolder)
		def fmFolderInstance = new FmFolder(params)
		if(parentFolder)
		{
			// fmFolderInstance.folderFullName = "folder"
			fmFolderInstance.folderLevel = parentFolder.folderLevel + 1
			fmFolderInstance.folderType = FolderType.FOLDER.name()
			fmFolderInstance.parent = parentFolder
		}
		else
		{
			log.error "Parent folder is null"
		}
		
		log.info fmFolderInstance
		if (!fmFolderInstance.hasErrors() && fmFolderInstance.save(flush: true)) {
			log.info "Folder saved"
			createAmTagTemplateAssociation(FolderType.FOLDER.name(), fmFolderInstance)
			saveMetaData(fmFolderInstance, null, params)
			
			def result = [id: fmFolderInstance.id, parentId: fmFolderInstance.parent.id]
			render result as JSON
			return
		}
		else {
			log.error "Saved folder failed"
			fmFolderInstance.errors.each {
				log.error it
			}

			def bioDataObject = fmFolderInstance
			def amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.FOLDER.name())
			if(!amTagTemplate) log.error ("Unable to find tag template for folder type = ")
			
			def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
			
			render
//			render(template: "createFolder", model:[bioDataObject:bioDataObject, folder:fmFolderInstance, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
		}
	}
	

	def saveProgram = {
		log.info "saveProgram called"
		log.info params
		def fmFolderInstance = new FmFolder(params)
//		fmFolderInstance.folderFullName = "\\" + fmFolderInstance.folderName
	//	fmFolderInstance.folderFullName = "program"
		fmFolderInstance.folderLevel = 0
		
		log.info(fmFolderInstance)
		if (fmFolderInstance.save(flush: true)) {
			log.info "Program saved"
			log.info(fmFolderInstance)
			log.info(fmFolderInstance.getUniqueId())
			
			// Save metadata
			createAmTagTemplateAssociation(FolderType.PROGRAM.name(), fmFolderInstance)
			saveMetaData(fmFolderInstance, null, params)
			def result = [id: fmFolderInstance.id]
			render result as JSON
			return
			
//			redirect(action: "show", id: fmFolderInstance.id)
		}
		else {
			log.error "Unable to save program"
			fmFolderInstance.errors.each {
				log.error it
			}
			
			def folder = fmFolderInstance
			folder.folderType = FolderType.PROGRAM.name()
			def bioDataObject = folder
			def amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.PROGRAM.name())
			def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
			render(template: "createProgram", model:[bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
		}
	}


	def saveAnalysis = {
		log.info "saveAnalysis called"
		log.info params
	
		def parentFolder = FmFolder.get(params.parentId)
		log.info("parentFolder = " + parentFolder)
		def fmFolderInstance = new FmFolder(params)
		if(parentFolder)
		{
			fmFolderInstance.folderLevel = parentFolder.folderLevel + 1
			fmFolderInstance.folderType = FolderType.ANALYSIS.name()
			fmFolderInstance.parent = parentFolder
		}
		else
		{
			log.error "Parent folder is null"
		}

		if (fmFolderInstance.save(flush: true))
		{
			log.info "Analysis folder saved"
			createAmTagTemplateAssociation(FolderType.ANALYSIS.name(), fmFolderInstance)
			
			log.info "Analysis folder association saved"
			def bioDataObject = new bio.BioAssayAnalysis()
			bioDataObject.name = fmFolderInstance.folderName
			bioDataObject.shortDescription = fmFolderInstance.description
			bioDataObject.longDescription = fmFolderInstance.description
			bioDataObject.analysisMethodCode="TBD"
			bioDataObject.assayDataType="TBD"
			bioDataObject.dataCount=-1
			bioDataObject.teaDataCount=-1
			bioDataObject = saveMetaData(fmFolderInstance, bioDataObject, params)
			BioData bioData = BioData.get(bioDataObject.id)
			if(!bioData){
				 log.error "Biodata for " + bioDataObject.id + " is not found"
			}
			else{
				log.info "Saving analysis objectUid: " + bioData.uniqueId + " objectType: bio.BioAssayAnalysis  fmFolder: " + fmFolderInstance
				FmFolderAssociation ffa = new FmFolderAssociation(objectUid: bioData.uniqueId, objectType:"bio.BioAssayAnalysis",fmFolder:fmFolderInstance)
				if(ffa.save(flush: true))
				{
					log.info "Analysis experiment folder association saved"
					
					def result = [id: fmFolderInstance.id, parentId: fmFolderInstance.parent.id]
					render result as JSON
					return
				}
				else
				{
					ffa.errors.each {
						log.error it
					}
				}
				
			}

		}
		else
		{
			fmFolderInstance.errors.each {
				log.error it
			}
		}
		
		render(view: "create", model: [fmFolderInstance: fmFolderInstance])
	}

	
	def saveStudy = {
		log.info "saveStudy called"
		log.info params
	
		def parentFolder = FmFolder.get(params.parentId)
		log.info("parentFolder = " + parentFolder)
		def fmFolderInstance = new FmFolder(params)
		if(parentFolder)
		{
			fmFolderInstance.folderLevel = parentFolder.folderLevel + 1
			fmFolderInstance.folderType = FolderType.STUDY.name()
			fmFolderInstance.parent = parentFolder
		}
		else
		{
			log.error "Parent folder is null"
		}

		if (fmFolderInstance.save(flush: true)) 
		{
			log.info "Study folder saved"
  			createAmTagTemplateAssociation(FolderType.STUDY.name(), fmFolderInstance)
			
			log.info "Study tag template association saved"
			def bioDataObject = new bio.Experiment()
			bioDataObject.title = fmFolderInstance.folderName
			bioDataObject.description = fmFolderInstance.description
		//	bioDataObject.accession = fmFolderInstance.folderName
		//	log.info "bioDataObject.accession  = " + bioDataObject.accession 
			bioDataObject.type="Experiment"
			bioDataObject = saveMetaData(fmFolderInstance, bioDataObject, params)
			BioData bioData = BioData.get(bioDataObject.id)
			if(!bioData){
				 log.error "Biodata for " + bioDataObject.id + " is not found"
			}
			else{
				log.info "Study experiment saved " + bioDataObject.id
				FmFolderAssociation ffa = new FmFolderAssociation(objectUid: bioData.uniqueId, objectType:"bio.Experiment",fmFolder:fmFolderInstance)
				if(ffa.save(flush: true))
				{
					log.info "Study experiment folder association saved " + bioData.uniqueId
					
					def result = [id: fmFolderInstance.id, parentId: fmFolderInstance.parent.id]
					render result as JSON
					return
				}
				else
				{
					ffa.errors.each {
						log.error it
					} 
				}
				
			}

		}
		else 
		{
			fmFolderInstance.errors.each {
				log.error it
			}
		}
		
		render(view: "create", model: [fmFolderInstance: fmFolderInstance])
    }

	
    def showStudy = {
        def fmFolderInstance = FmFolder.get(params.id)

		// test the class
//		def json = new JSONSerializer(target: fmFolderInstance).getJSON()
//		log.info json
		
		def data = serializeFoldersToXMLFile()
		log.info data
		
        if (!fmFolderInstance) {
            redirect(action: "list")
        }
        else {
            [fmFolderInstance: fmFolderInstance, data:data]
        }
    }

    def edit = {
        def fmFolderInstance = FmFolder.get(params.id)
        if (!fmFolderInstance) {
            redirect(action: "list")
        }
        else {
           return [fmFolderInstance: fmFolderInstance]
        }
    }

    def update = {
		
		log.info "UPDATING == " + params
        def fmFolderInstance = FmFolder.get(params.id)
  
      if (fmFolderInstance) {
            fmFolderInstance.properties = params
			
            if (!fmFolderInstance.hasErrors() && fmFolderInstance.save(flush: true)) {
                redirect(action: "show", id: fmFolderInstance.id)
            }
            else {
                render(view: "edit", model: [fmFolderInstance: fmFolderInstance])
            }
        }
        else {
            redirect(action: "list")
        }
    }

    def delete = {
        def fmFolderInstance = FmFolder.get(params.id)
        if (fmFolderInstance) {
            try {
                fmFolderInstance.delete(flush: true)
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                redirect(action: "show", id: params.id)
            }
        }
        else {
            redirect(action: "list")
        }
    }

	def getPrograms = {
		List<FmFolder> folders = getFolder(FolderType.PROGRAM.name(), null)
		render folders as XML
	}

	def getStudies = {
		List<FmFolder> folders = getFolder(FolderType.STUDY.name(), params.parentPath)
		render folders as XML
	}

	def getFolders = {
		List<FmFolderController> folders = getFolder(FolderType.FOLDER.name(), params.parentPath)
		render folders as XML
	}

	def getAnalysises = {
		List<FmFolder> folders = getFolder(FolderType.ANALYSIS.name(), params.parentPath)
		render folders as XML
	}

	def getAssayes= {
		List<FmFolder> folders = getFolder(FolderType.ASSAY.name(), params.parentPath)
		render folders as XML
	}
	
	//service to call to get all the children of a folder, regardless their type
	//need a parameter parentId corresponding to the parent identifier
	def getAllChildren ={
		List<FmFolder> children = getChildrenFolder(params.parentId)
		render children as XML
	}

	//service to call to get all experiments objects that are associated with a folder in fm_folder_association table 
	def getExperiments = {
		List<FmFolderAssociation> assoc=FmFolderAssociation.findAll("from FmFolderAssociation as fd where fd.objectType='bio.Experiment'")
		List<Experiment> experiments=new ArrayList<Experiment>();
		for(FmFolderAssociation a: assoc){
			Experiment exp=a.getBioObject();
			if(exp!=null) experiments.add(exp)
		}
		render experiments as XML
	}

	def addProgram = {
		def p = new FmFolder(params['fmFolder'])
		addFolder(FolderType.PROGRAM.name(), p, null)
	}

	def addStudy = {
		def p = new FmFolder(params['fmFolder'])
		addFolder(FolderType.STUDY.name(), p, params['parentId'])
	}

	def addFolder = {
		def p = new FmFolder(params['fmFolder'])
		addFolder(FolderType.STUDY.name(), p, params['parentId'])
	}

	def addAnalysis = {
		def p = new FmFolder(params['fmFolder'])
		addFolder(FolderType.ANALYSIS.name(), p, params['parentId'])
	}
	
	def addFile = {
		FmFolder p = FmFolder.get(params['folderId'])
		FmFile f = new FmFile()
		f.properties = params	
		if(f.save(flush:true))	
		{
			p.addToFmFiles(f)
			if(p.save(flush:true))
			{
				render p as XML
			}
			else
			{
				render p.errors
			}

		}
		else 
		{
			render f.errors
		}
		
		
		addFolder(FolderType.STUDY.name(), p, params['parentId'])
	}
	
	def getFolderContents = {
		def id = params.id
		if (!id) {
			def uid = params.uid
			id = FmFolder.findByUniqueId(uniqueId).id
		}
		def auto = params.boolean('auto') //Flag for whether folder was automatically opened - if not, then it shouldn't respect the folder mask
		def folderContents = fmFolderService.getFolderContents(id)
		
		def folderSearchLists = session['folderSearchList']
		if (!folderSearchLists) {
			folderSearchLists = [[],[]]
		}
		def folderSearchString = folderSearchLists[0] ? folderSearchLists[0].join(",") + "," : "" //Extra , - used to identify leaves
		def uniqueLeavesString = folderSearchLists[1] ? folderSearchLists[1].join(",") + "," : ""
		
		render(template:'folders', model: [folders: folderContents, folderSearchString: folderSearchString, uniqueLeavesString: uniqueLeavesString, auto: auto])
	}

	/**
	 * Update incorrect or missing tag values for folders.
	 */
	def fixFolderTags = {
		
		def folders = FmFolder.findAll();
		log.info("fixFolderTags found " + folders?.size());
		for (folder in folders) {
			log.info("checking tag for folder " + folder.id);
			if (folder.folderTag == null || folder.folderTag != Long.toString(folder.id, 36).toUpperCase()) {
				folder.folderTag = Long.toString(folder.id, 36).toUpperCase();
				log.info("updating tag to " + folder.folderTag);
				folder.save();
			}
		}
		
	}
	
	private void addFolder(String folderType, FmFolder folder, long parentId)
	{
		folder.folderType = folderType
		
		if (FolderType.PROGRAM.name() == folderType)
		{
			folder.folderLevel = 0			
		}
		else
		{
			def parentFolder = FmFolder.getAt(parentId)
			folder.folderLevel = parentFolder.folderLevel + 1
			folder.parent = parentFolder
		}
		
		if (folder.save(flush:true)) 
		{
			// Set folder's tag value based on a radix-36 conversion of its ID.
			folder.tag = Long.toString(folder.id, 36).toUpperCase();
			folder.save(flush:true);
			
			// This is now done in the database
			// Create UID for folder.
		//	def data = new FmData(type:'FM_FOLDER', uniqueId:'FOL:' + folder.id);
		//	data.id = folder.id;
		//	data.save(flush:true);
	
			render folder as XML
		}
		else {
			render folder.errors
		}
	}

	private void updateFolder()
	{
		def folder = FmFolder.getAt(params.folderId)
		folder.properties = params
		
		if(folder.save())
		{
			render folder as XML
		}
		else {
			render folder.errors
		}
	}
	
	private void moveFolder(long folderId, String newFolderFullName, String newFolderLevel)
	{
		def folder = FmFolder.getAt(folderId)
		def oldFullName = folder.folderFullName
		def oldLevel = folder.folderLevel
		folder.folderFullName = newFolderFullName
		folder.folderLevel = newFolderLevel
			
		if(folder.save())
		{
			List<FmFolder> subFolderList = FmFolder.findAll("from FmFolder as fd where fd.folderFullName like :fn",
				[fn:oldFullName+"%"])

			subFolderList.each {
					
					println it
					def folderLevelDifferential = it.folderLevel - oldLevel
					// it.folderLevel = 
					// it.folderFullName = 
					moveFolder(it.id,newFolderFullName + it.folderName + "\\", newFolderLevel + folderLevelDifferential)
					
				}
			
			render folder as XML
		
		}
		else {
			render folder.errors
		}
	}
	
	private void removeFolder(long folderId)
	{
		def folder = FmFolder.getAt(folderId)
		folder.activeInd = false
		
		if(folder.save())
		{
			List<FmFolder> subFolderList = FmFolder.findAll("from FmFolder as fd where fd.folderFullName like :fn and fd.folderLevel = :fl",
				[fn:folder.folderFullName+"%", fl: (folder.folderLevel + 1)])

			subFolderList.each {
					println it
					removeFolder(it.id)
					/* it.activeInd = false
					if(!it.save())
					{
						render it.errors
					}
					*/
				}
			
			render folder as XML
		
		}
		else {
			render folder.errors
		}
	}

	private List<FmFolder> getFolder(String folderType, String parentPath)  
	{
		log.info("getFolder(" + folderType + ", " + parentPath + ")")
 		if(parentPath == null)
		{
			return FmFolder.executeQuery("from FmFolder as fd where upper(fd.folderType) = upper(:fl) ", [fl: folderType])
		}
		else
		{
			return FmFolder.executeQuery("from FmFolder as fd where upper(fd.folderType) = upper(:fl) and fd.folderFullName like :fn ",
				 [fl: folderType, fn:parentPath+"%"])
		}
	
	}
	
	//method which returns a list of folders which are the children of the folder of which the identifier is passed as parameter
	private List<FmFolder> getChildrenFolder(String parentId)  
	{		
		def folder = FmFolder.get(parentId)
		return FmFolder.executeQuery("from FmFolder as fd where fd.folderFullName like :fn and fd.folderLevel= :fl ",[fl: folder.folderLevel+1, fn:folder.folderFullName+"%"])
	}

	//method which returns a list of folders which are the children of the folder of which the identifier is passed as parameter by folder types
	private List<FmFolder> getChildrenFolderByType(Long parentId, String folderType)
	{
		def folder = FmFolder.get(parentId)
		return FmFolder.executeQuery("from FmFolder as fd where fd.folderFullName like :fn and fd.folderLevel= :fl and upper(fd.folderType) = upper(:ft)", [fl: folder.folderLevel+1, fn:folder.folderFullName+"%", ft: folderType])
	}

	//method which returns a list of folders which are the children of the folder of which the identifier is passed as parameter
	private List getChildrenFolderTypes(Long parentId)
	{
		def folder = FmFolder.get(parentId)
		return FmFolder.executeQuery("select distinct(fd.folderType) from FmFolder as fd where fd.folderFullName like :fn and fd.folderLevel= :fl ", [fl: folder.folderLevel+1, fn:folder.folderFullName+"%"])
	}

	private String createDataTable(folders, folderType)
	{
		
		if (folders == null || folders.size() < 1) return
					
		ExportTableNew table=new ExportTableNew();
		
		def dataObject
		def childMetaDataTagItems
		
		for (folder in folders) {
			dataObject = getBioDataObject(folder)
			childMetaDataTagItems = getChildMetaDataItems(folder)
			if (dataObject && childMetaDataTagItems) break
		}
		
		childMetaDataTagItems.eachWithIndex() 
		{obj, i ->    // 
			AmTagItem amTagItem = obj
			if(amTagItem.viewInChildGrid) 
			{
				if(amTagItem.tagItemType == 'FIXED')
				{
					log.info ("CREATEDATATABLE::FIXED TYPE == " + amTagItem.tagItemType + " ID = " + amTagItem.id + " " + amTagItem.displayName)
					
					if(dataObject.hasProperty(amTagItem.tagItemAttr))
					{
						//log.info ("CREATEDATATABLE::FIXED COLUMNS == " + amTagItem.tagItemAttr + " " + amTagItem.displayName)
						table.putColumn(amTagItem.id.toString(), new ExportColumn(amTagItem.id.toString(), amTagItem.displayName, "", 'String'));
					}
					else
					{
						log.error("CREATEDATATABLE::TAG ITEM ID = " + amTagItem.id + " COLUMN " + amTagItem.tagItemAttr + " is not a propery of " + dataObject)
					}
					
				}
				else if(amTagItem.tagItemType == 'CUSTOM')
				{
					log.info ("CREATEDATATABLE::CUSTOM == " + amTagItem.tagItemType + " ID = " + amTagItem.id + " " + amTagItem.displayName)
					table.putColumn(amTagItem.id.toString(), new ExportColumn(amTagItem.id.toString(), amTagItem.displayName, "", 'String'));

				}
				else
				{
					log.info ("CREATEDATATABLE::BUSINESS OBJECT == " + amTagItem.tagItemType + " ID = " + amTagItem.id + " " + amTagItem.displayName)
					table.putColumn(amTagItem.id.toString(), new ExportColumn(amTagItem.id.toString(), amTagItem.displayName, "", 'String'));
				}
			}
			else
			{
				log.info("COLUMN " + amTagItem.displayName + " is not to display in grid")
			}

		}
		
		folders.each 
		{
				log.info "FOLDER::" + it
				def bioDataObject = getBioDataObject(it)
				def folderObject = it
				ExportRowNew newrow=new ExportRowNew();
				childMetaDataTagItems.eachWithIndex()
				{obj, i -> 
					AmTagItem amTagItem = obj
					if(amTagItem.viewInChildGrid)
					{
						if(amTagItem.tagItemType == 'FIXED' && bioDataObject.hasProperty(amTagItem.tagItemAttr))
						{
							def bioDataDisplayValue = null 
							def bioDataPropertyValue = bioDataObject[amTagItem.tagItemAttr]
							if(amTagItem.tagItemSubtype == 'PICKLIST' || amTagItem.tagItemSubtype == 'MULTIPICKLIST')
							{
								if(bioDataPropertyValue)
								{
								def cc = ConceptCode.findByUniqueId(bioDataPropertyValue)
								if(cc) 
								{ 
									bioDataDisplayValue = cc.codeName
								}
								else
								{
									 bioDataDisplayValue = ""
								}
								}
								else
								{
									 bioDataDisplayValue = ""
								}

							}
							else if(amTagItem.tagItemSubtype == 'FREETEXT')
							{
								bioDataDisplayValue = createTitleString(amTagItem, bioDataPropertyValue, folderObject)
							}
							else if(amTagItem.tagItemSubtype == 'FREETEXTAREA')
							{
								bioDataDisplayValue =  amTagItem.displayName
							}
							else
							{
								log.error "FIXED ATTRIBUTE ERROR::Unknown tagItemSubType"
							}
							
							log.info("ROWS == " + amTagItem.tagItemAttr + " " + bioDataObject[amTagItem.tagItemAttr])
							newrow.put(amTagItem.id.toString(),bioDataDisplayValue?bioDataDisplayValue:'');
						}
						else if(amTagItem.tagItemType == 'CUSTOM')
						{
							def tagValues = AmTagDisplayValue.findAll('from AmTagDisplayValue a where a.subjectUid=? and a.amTagItem.id=?',[bioDataObject.getUniqueId().toString(),amTagItem.id])
							log.info("CUSTOM PARAMETERS " + bioDataObject.getUniqueId() + " " + amTagItem.id + " tagValues " +tagValues)
							newrow.put(amTagItem.id.toString(),createDisplayString(tagValues));
						}
						else
						{
						    def tagValues = AmTagDisplayValue.findAllDisplayValue(it.uniqueId,amTagItem.id)
							log.info("BIOOBJECT PARAMETERS " + it.uniqueId + " " + amTagItem.id + " tagValues " +tagValues)
							
							newrow.put(amTagItem.id.toString(),createDisplayString(tagValues));
						}
	
					}
				}
				
				table.putRow(bioDataObject.id.toString(), newrow);
		}
		
		return table.toJSON_DataTables("", folderType).toString(5);
	}
	
	//FIXME Quick hack to make title properties act as hyperlinks
	def nameProperties = ['assay name', 'analysis name', 'study title', 'program title', 'folder name']
	private createTitleString(amTagItem, name, folderObject) {
		def tagName = amTagItem.displayName
		if (nameProperties.contains(tagName.toLowerCase())) {
			return ("<a href='#' onclick='openFolderAndShowChild(${folderObject.parent?.id}, ${folderObject.id})'>" + name + "</a>")
		}
		return name
	}
	
	private createDisplayString(tagValues)
	{
//		log.info ("createDisplayString::TAGVALUES == " + tagValues)
		
		def displayValue = ""
		def counter = 0
						
		tagValues.each
		{
			log.info("createDisplayString::TAGVALUE = " + it)
			displayValue += counter>0? ", " + it.displayValue : it.displayValue
		}
		
		if(displayValue == null) displayValue = ""
		
		return displayValue
	}
	
	def folderDetail = {
		log.info "** action: folderDetail called!"
		def folderId = params.id
		log.info("PARAMS = " + params)
			
		def folder
		def subFolders
		def subFolderLayout
		def bioDataObject
		def amTagTemplate
		def metaDataTagItems
		def jSONForGrids = []
		def childMetaDataTagItems = []
		def subjectLevelDataAvailable = false 
		def measurements
		def technologies
		def vendors
		def platforms
		
		if (folderId) 
		{
			folder = FmFolder.get(folderId)
			
			if(folder)
			{
				bioDataObject = getBioDataObject(folder)
				metaDataTagItems = getMetaDataItems(folder, false)
				log.info "metaDataTagItems  = " + metaDataTagItems
				
				//If the folder is a study, check for subject-level data being available
				if (folder.folderType.equalsIgnoreCase(FolderType.STUDY.name()) && bioDataObject!=null && bioDataObject.hasProperty("accession"))
			   {
					subjectLevelDataAvailable = ontologyService.checkSubjectLevelData(bioDataObject.accession)
				}
			   
			   if (folder.folderType.equalsIgnoreCase(FolderType.ASSAY.name()) && folder.folderType.equalsIgnoreCase(FolderType.ANALYSIS.name())) 
			   {
				   measurements  = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT platformType FROM BioAssayPlatform as p ORDER BY p.platformType")
				   vendors = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT vendor FROM BioAssayPlatform as p ORDER BY p.vendor")
				   technologies = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ORDER BY p.platformTechnology")
				   platforms = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT name FROM BioAssayPlatform as p ORDER BY p.name")
			   }
			   			   
				// If the folder is a study then get the analysis and the assay
				// if (folder.folderType.equalsIgnoreCase(FolderType.STUDY.name()) || folder.folderType.equalsIgnoreCase(FolderType.PROGRAM.name()))
				// {
					def subFolderTypes = getChildrenFolderTypes(folder.id)
					log.info "subFolderTypes = " + subFolderTypes
					subFolderTypes.each
					{
						log.info "it = " + it
						subFolders = getChildrenFolderByType(folder.id, it)
						if(subFolders!=null && subFolders.size()>0)
						{
							log.info(subFolders.size() + " subFolders == " + subFolders)

								subFolderLayout = formLayoutService.getLayout(it.toLowerCase());
								String gridTitle = "Associated " + StringUtils.capitalize(subFolders[0].pluralFolderTypeName.toLowerCase())
								String gridData = createDataTable(subFolders, gridTitle)
							//	log.info gridData
								jSONForGrids.add(gridData)
								log.info "ADDING JSON GRID"
						}
					}
				// }
			}
		}
		
		log.info "FolderInstance = " + bioDataObject.toString()
		render(template:'/fmFolder/folderDetail', model:[folder:folder, bioDataObject:bioDataObject, measurements:measurements, technologies:technologies, vendors:vendors, platforms:platforms, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems, jSONForGrids: jSONForGrids, subjectLevelDataAvailable: subjectLevelDataAvailable])
		
	}

	
	private List<AmTagItem> getChildMetaDataItems(folder)
	{
		def amTagTemplate = amTagTemplateService.getTemplate(folder.getUniqueId())
		List<AmTagItem> metaDataTagItems
		if(amTagTemplate)
		{
			metaDataTagItems = amTagItemService.getChildDisplayItems(amTagTemplate.id)
		}
		else
		{
			log.error "Unable to find child amTagTemplate for object Id = " + folder.getUniqueId()
		}
		
		return metaDataTagItems
	}

		private List<AmTagItem> getMetaDataItems(folder, editable)
	{
		def amTagTemplate = amTagTemplateService.getTemplate(folder.getUniqueId())
		List<AmTagItem> metaDataTagItems
		if(amTagTemplate)
		{
			if(editable)
			{
				metaDataTagItems = amTagItemService.getEditableItems(amTagTemplate.id)
			}
			else
			{
				metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
			}
		}
		else
		{
			log.error "Unable to find amTagTemplate for object Id = " + folder.getUniqueId()
		}
		
		return metaDataTagItems
	}

	private Object getBioDataObject(folder)
	{
		def bioDataObject
		log.info "getBioDataObject::folder = " + folder
		
		def folderAssociation = FmFolderAssociation.findByFmFolder(folder)
		
		if(folderAssociation)
		{
			log.info "getBioDataObject::folderAssociation = " + folderAssociation
			bioDataObject =folderAssociation.getBioObject()
		}
		else
		{
			log.error "Unable to find folderAssociation for folder Id = " + folder.id
		}

		if(!bioDataObject)
		{
			log.info "Unable to find bio data object. Setting folder to the biodata object "
			bioDataObject = folder
		}

		return bioDataObject
	} 
	  
	def editMetaData = 
	{		
		log.info "editMetaData called"
		log.info "params = " + params
		
		
		if("anonymousUser" != springSecurityService.getPrincipal())
		{
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		if(!user.isAdmin()) {
			render(status: 200, text: "You do not have permission to edit this object's metadata.")
			return
		}
		}
		else
		{
			render(status: 200, text: "You do not have permission to edit this object's metadata.")
			return
		}

			 
		//log.info "** action: expDetail called!"
		def folderId = params.folderId
		
		def folder
		def bioDataObject
		def amTagTemplate
		def metaDataTagItems
		if (folderId)
		{
			folder = FmFolder.get(folderId)
			if(folder)
			{
				bioDataObject = getBioDataObject(folder)
				metaDataTagItems = getMetaDataItems(folder,true)
			}
			else
			{
				log.error "Unable to find folder for folder Id = " + folderId				
			}

		} 

		def title = "Edit Meta Data"
		def templateType = "editMetadataForm"
		render(template: "editMetaData", model:[bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
	}
	
	def updateMetaData =
	{
		log.info "updateMetaData called"
		log.info params
		
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		if(!user.isAdmin()) {
			render(status: 200, text: "You do not have permission to edit this object's metadata.")
			return
		}
		
		def paramMap = params
		def folderId = params.id
		def amTagTemplate
		List<AmTagItem> metaDataTagItems
		def folder
		def bioDataObject
		if (folderId)
		{
			folder = FmFolder.get(folderId)
			
			if(folder)
			{
				if(params.description)
				{
					folder.description = params.description 
				}
				def folderAssociation = FmFolderAssociation.findByFmFolder(folder)
				
				bioDataObject = getBioDataObject(folder)
				metaDataTagItems = getMetaDataItems(folder, true)
				log.info metaDataTagItems
				//Use metaDataTagItems to update fields
				for (tagItem in metaDataTagItems) {
					log.info "Processing tag = " + tagItem.id 
					def newValue = null
					if (tagItem.tagItemType.equals('FIXED')) {
						newValue = params."${tagItem.tagItemAttr}"
						
						if (newValue != null) {
							def value = ""
							if (tagItem.tagItemSubtype.equals('MULTIPICKLIST'))
							{
								newValue = params.list("${tagItem.tagItemAttr}")
								if (newValue != null && newValue != "" && newValue.size() > 0)
								{
									newValue.each {
									if(value != ""){value += "|"}
										value += it
									}
								}
								
								
							}
							else
							{
								value = newValue
							}
							
							log.info "SAVING FIXED -- ${tagItem.tagItemAttr} == " + value
							
							bioDataObject."${tagItem.tagItemAttr}" = value
							
							if(tagItem.tagItemAttr.equalsIgnoreCase("title"))
							{
								folder.folderName= newValue
							}
							
							if(tagItem.tagItemAttr.equalsIgnoreCase("description"))
							{
								folder.description = newValue
							}
			
						}
					}
					else if (tagItem.tagItemType.equals('CUSTOM'))
					{
						newValue = params."amTagItem_${tagItem.id}"
						if(tagItem.tagItemSubtype.equals('FREETEXT') || tagItem.tagItemSubtype.equals('FREETEXTAREA')) 
						{
														// Save the new tag value
							if (newValue != null && newValue != "")
							{
								//Look for new value by tag item ID
								log.info "SAVING CUSTOM::FREETEXT == " + newValue
								
								AmTagValue newTagValue = new AmTagValue(value: newValue)
								if(newTagValue.save(flush: true))
								{
									log.info "'CUSTOM', subjectUid: " + folder.getUniqueId() + " tagItemId: " + tagItem.id + " objectUid: " + newValue
								
									AmTagAssociation.executeUpdate ("delete from AmTagAssociation as ata where ata.objectType=:objectType and ata.subjectUid=:subjectUid and ata.tagItemId=:tagItemId", [objectType: "BIO_CONCEPT_CODE", subjectUid: folder.getUniqueId(),tagItemId: tagItem.id])
									AmTagAssociation ata = new AmTagAssociation(objectType: 'BIO_CONCEPT_CODE', subjectUid: folder.getUniqueId(), objectUid: newTagValue.getUniqueId(), tagItemId: tagItem.id)
									if(!ata.save(flush: true))
									{
										ata.errors.each
										{
											log.error it
										}
									}
								}
								else
								{
									newTagValue.errors.each
									{
										log.error it
									}
								}
							}

	
						}
						else if(tagItem.tagItemSubtype.equals('PICKLIST'))
						{
							if (newValue != null && newValue != "")
							{
								//Look for new value by tag item ID
								log.info "SAVING CUSTOM::PICKLIST == " + newValue
								log.info "'CUSTOM', subjectUid: " + folder.getUniqueId() + " tagItemId: " + tagItem.id + " objectUid: " + newValue
								AmTagAssociation.executeUpdate ("delete from AmTagAssociation as ata where ata.objectType=:objectType and ata.subjectUid=:subjectUid and ata.tagItemId=:tagItemId", [objectType: "BIO_CONCEPT_CODE", subjectUid: folder.getUniqueId(),tagItemId: tagItem.id])
								AmTagAssociation ata = new AmTagAssociation(objectType: 'BIO_CONCEPT_CODE', subjectUid: folder.getUniqueId(), objectUid: newValue, tagItemId: tagItem.id)
									
								if (!ata.save(flush:true)) {
									ata.errors.each {
										println it
									}
								}
							}
						}
						else if(tagItem.tagItemSubtype.equals('MULTIPICKLIST'))
						{
							newValue = params.list("amTagItem_${tagItem.id}")
							//Look for new value by tag item ID
							log.info "SAVING CUSTOM::MULTIPICKLIST == " + newValue
							// Save the new tag value
							if (newValue != null && newValue != "" && newValue.size() > 0)
							{
								log.info "REMOVING - objectType: CUSTOM " + " subjectUid = " + folder.getUniqueId() + " tagItemId: " + tagItem.id + " newValue: " + newValue.size() + " " + newValue
								AmTagAssociation.executeUpdate ("delete from AmTagAssociation as ata where ata.objectType=:objectType and ata.subjectUid=:subjectUid and ata.tagItemId=:tagItemId", [objectType: "BIO_CONCEPT_CODE", subjectUid: folder.getUniqueId(),tagItemId: tagItem.id])
								
								newValue.each
								{   
									log.info "NEWVALUE = " + it
 									if(it)
									{
										 AmTagAssociation ata1 = new AmTagAssociation(objectType: 'BIO_CONCEPT_CODE', subjectUid: folder.getUniqueId(), objectUid: it, tagItemId: tagItem.id)
									
										if (!ata1.save(flush:true)) {
											ata1.errors.each {
												println it
											}
									
										}
									}
									else
								 	{
										 log.error("amTagItem_${tagItem.id} is null")
									 }
								}
							}
						}

						else
						{
							// TODO: throw an exception
							// unrcognized subtype
						}
						

					}
					else 
					{
						newValue = params.list("amTagItem_${tagItem.id}")
						//Look for new value by tag item ID
						log.info "SAVING BUSINESS OBJECT == " + newValue
						// Save the new tag value
						if (newValue != null && newValue != "" && newValue.size() > 0)
						{
							log.info "REMOVING - objectType: CUSTOM " + " subjectUid = " + folder.getUniqueId() + " tagItemId: " + tagItem.id + " newValue: " + newValue.size() + " " + newValue
							AmTagAssociation.executeUpdate ("delete from AmTagAssociation as ata where ata.objectType=:objectType and ata.subjectUid=:subjectUid and ata.tagItemId=:tagItemId", [objectType: tagItem.tagItemType, subjectUid: folder.getUniqueId(),tagItemId: tagItem.id])
							
							newValue.each
							{
								log.info "NEWVALUE " + it
								if(it)
								{
									AmTagAssociation ata1 = new AmTagAssociation(objectType: tagItem.tagItemType, subjectUid: folder.getUniqueId(), objectUid: it, tagItemId: tagItem.id)
									
									if (!ata1.save(flush:true)) {
										ata1.errors.each {
											println it
										}
									}
								}
								else
								 {
									 log.error("amTagItem_${tagItem.id} is null")
								 }
							}
						}
					 	
					}
				}
				if (!folder.hasErrors() && folder.save(flush: true)) 
				{
					log.info "Folder saved"
					
					if (!bioDataObject.hasErrors() && bioDataObject.save(flush: true)) {
						log.info "Meta data saved"
						def result = [id: folderId]
						render result as JSON
						return
					}
					else {
						log.error "Errors occurred saving Meta data"
						metaDataTagItems  =  getMetaDataItems(folder, true)
						log.info "metaDataTagItems  = " + metaDataTagItems   
						render(view: "editMetaData", model:[bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
						//	render(view: "edit", model: [fmFolderInstance: fmFolderInstance])
					}
				}
				else
				{
					log.error "Errors occurred saving folder description"
					render(view: "editMetaData", model:[bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
					
				}
			}
		}
	}
			
	def subFolders = 
	{
		ExportTableNew table;
		
		//Keep this if you want to cache the grid data
		//ExportTableNew table=(ExportTableNew)request.getSession().getAttribute("gridtable");
		
		if(table==null)
		{
			table=new ExportTableNew();
		}

		table.putColumn("ident", new ExportColumn("ident", "ID", "", "String", 50));
		table.putColumn("name", new ExportColumn("name", "Name", "", "String", 50));
		table.putColumn("description", new ExportColumn("description", "Description", "", "String", 50));
		
		ExportRowNew newrow=new ExportRowNew();
		newrow.put("ident", "foo.id");
		newrow.put("name", "foo.name");
		newrow.put("description", "foo.description");
		table.putRow("somerow", newrow);

		def jSONToReturn = table.toJSON_DataTables("").toString(5);
		
		request.getSession().setAttribute("gridtable", table);
		
		[jSONForGrid: jSONToReturn]
		
	}

def getFdDetails = {
}

	/**
	 * Calls service to import files into tranSMART filestore and index them with SOLR
	 */
	def importFiles = {
		
		fmFolderService.importFiles();
		
	}
	
	/**
	 * Calls service to re-index existing files with SOLR
	 */
	def reindexFiles = {
		
		fmFolderService.reindexFiles();
		
	}
	
	def reindexFolder = {
		solrFacetService.reindexFolder(params.uid)
	}


	def ajaxTechnologies =
	{
		def queryString = ""
		if(params.measurementName != 'null')
		{
			queryString = " where platformType = '" + params.measurementName  + "'"
		}

		def technologies  = bio.BioAssayPlatform.executeQuery("SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p " + queryString + "  ORDER BY p.platformTechnology")
		log.info queryString + " " + technologies
		render(template: "selectTechnologies", model: [technologies:technologies])
	}

	def ajaxVendors = 
	{
		log.info params
		def queryString = " where 1=1"

		if(params.technologyName!=null&&params.technologyName != 'null')
		{
			queryString += " and platformTechnology = '" + params.technologyName  + "'"
		}
		
		if(params.measurementName!=null&&params.measurementName != 'null')
		{
			queryString += " and platformType = '" + params.measurementName + "'"
		}

		queryString = "SELECT DISTINCT vendor FROM BioAssayPlatform as p " + queryString + "  ORDER BY p.vendor"
		def vendors  = bio.BioAssayPlatform.executeQuery(queryString)
		log.info queryString + " " +vendors
		render(template: "selectVendors", model: [vendors:vendors])
	}


}


