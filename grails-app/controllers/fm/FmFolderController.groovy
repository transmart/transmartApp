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

import am.AmData;
import annotation.AmTagAssociation;
import annotation.AmTagDisplayValue;
import annotation.AmTagItem;
import annotation.AmTagTemplate;
import annotation.AmTagValue;

import bio.ConceptCode
import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import com.recomdata.util.FolderType
import grails.converters.*
import groovy.xml.StreamingMarkupBuilder


class FmFolderController {

	def formLayoutService
	def amTagTemplateService
	def amTagItemService
	def fmFolderService
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

	def createAssay = {
		log.info "createAssay called"
		log.info "params = " + params
		//log.info "** action: expDetail called!"
		
		def folder = new FmFolder()
		folder.folderType = FolderType.ASSAY.name()
		def parentFolder = FmFolder.getAt(params.folderId)
		folder.parent = parentFolder
		def bioDataObject = folder
		def amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.ASSAY.name())
		def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
		def title = "Create Assay"
		def templateType = "createAssayForm"
		render(template: "createAssay", model:[bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
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
		def bioDataObject = folder
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
		def fmFolderInstance = new FmFolder(params)
		if (fmFolderInstance.save(flush: true)) {
			log.info "Assay saved"
			def result = [id: fmFolderInstance.id]
			render result as JSON
			return
		}
		else {
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
			fmFolderInstance.folderFullName = parentFolder.folderFullName + "\\" + fmFolderInstance.folderName
			fmFolderInstance.folderLevel = parentFolder.folderLevel + 1
			fmFolderInstance.folderType = FolderType.STUDY.name()
			fmFolderInstance.parent = parentFolder
		}
		else
		{
			log.error "Parent folder is null"
		}
		
		log.info fmFolderInstance
		if (fmFolderInstance.save(flush: true)) {
			log.info "Folder saved"
			def result = [id: fmFolderInstance.id]
			render result as JSON
			return
		}
		else {
			log.error "Saved folder failed"
			def bioDataObject = fmFolderInstance
			def amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.FOLDER.name())
			if(!amTagTemplate) log.error ("Unable to find tag template for folder type = ")
			
			def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
			render(template: "createFolder", model:[bioDataObject:bioDataObject, folder:fmFolderInstance, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
		}
	}

	def saveProgram = {
		log.info "saveProgram called"
		log.info params
		def fmFolderInstance = new FmFolder(params)
		fmFolderInstance.folderFullName = "\\" + fmFolderInstance.folderName
		fmFolderInstance.folderLevel = 0
		
		log.info(fmFolderInstance)
		if (fmFolderInstance.save(flush: true)) {
			log.info "Program saved"
			log.info(fmFolderInstance)
			log.info(fmFolderInstance.getUniqueId())
			
			def result = [id: fmFolderInstance.id]
			render result as JSON
			return
			
//			redirect(action: "show", id: fmFolderInstance.id)
		}
		else {
			log.error "Unable to save program"
			
			def folder = fmFolderInstance
			folder.folderType = FolderType.PROGRAM.name()
			def bioDataObject = folder
			def amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.PROGRAM.name())
			def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
			render(template: "createProgram", model:[bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
		}
	}

	def saveStudy = {
		log.info "saveStudy called"
		log.info params
		def fmFolderInstance = new FmFolder(params)
		if (fmFolderInstance.save(flush: true)) {
			log.info "Study saved"
			def result = [id: fmFolderInstance.id]
			render result as JSON
			return
		}
		else {
			render(view: "create", model: [fmFolderInstance: fmFolderInstance])
		}
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

		List<FmFolderController> folders = getFolder(FolderType.PROGRAM.name(), null)
		// 	[fn:this.folderFullName+"%", fl: (this.folderLevel + 1)])
		//List<FmFolderController> folders = getFolder("Program", null)
		
		render folders as XML
		
	}

	def getStudies = {
		
		List<FmFolderController> folders = getFolder(FolderType.STUDY.name(), params.parentPath)
				// 	[fn:this.folderFullName+"%", fl: (this.folderLevel + 1)])
		
		render folders as XML
				
	}

	def getFolders = {
		
		List<FmFolderController> folders = getFolder(FolderType.FOLDER.name(), params.parentPath)
				// 	[fn:this.folderFullName+"%", fl: (this.folderLevel + 1)])
				
		render folders as XML
	}

	def getAnalysises = {
		
		List<FmFolderController> folders = getFolder(FolderType.ANALYSIS.name(), params.parentPath)
				// 	[fn:this.folderFullName+"%", fl: (this.folderLevel + 1)])
		
		render folders as XML
				
	}

	def getAssayes= {
		
		List<FmFolderController> folders = getFolder(FolderType.ASSAY.name(), params.parentPath)
				// 	[fn:this.folderFullName+"%", fl: (this.folderLevel + 1)])
		
		render folders as XML
				
	}
	
	//service to call to get all the children of a folder, regardless their type
	//need a parameter parentId corresponding to the parent identifier
	def getAllChildren ={
		List<FmFolderController> children = getChildrenFolder(params.parentId)
		render children as XML
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
		
		def folderSearchList = session['folderSearchList']
		def folderSearchString = folderSearchList ? folderSearchList.join("\\,") + "\\," : "" //Extra , - used to identify leaves
		
		render(template:'folders', model: [folders: folderContents.folders, files: folderContents.files, folderSearchString: folderSearchString, auto: auto])
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
			
			// Create UID for folder.
			def data = new FmData(type:'FM_FOLDER', uniqueId:'FOL:' + folder.id);
			data.id = folder.id;
			data.save(flush:true);
	
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
			List<FmFolderController> subFolderList = FmFolder.findAll("from FmFolder as fd where fd.folderFullName like :fn",
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
			List<FmFolderController> subFolderList = FmFolder.findAll("from FmFolder as fd where fd.folderFullName like :fn and fd.folderLevel = :fl",
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
			childMetaDataTagItems = getMetaDataItems(folder)
			if (dataObject && childMetaDataTagItems) break
		}
		
		
		childMetaDataTagItems.eachWithIndex() 
		{obj, i ->    // 
			AmTagItem amTagItem = obj
			if(amTagItem.viewInChildGrid) 
			{
				if(amTagItem.tagItemType == 'FIXED')
				{
					log.info ("FIXED TYPE == " + amTagItem.id + " " + amTagItem.displayName)
					
					if(dataObject.hasProperty(amTagItem.tagItemAttr))
					{
						log.info ("FIXED COLUMNS == " + amTagItem.tagItemAttr + " " + amTagItem.displayName)
						table.putColumn(amTagItem.id.toString(), new ExportColumn(amTagItem.id.toString(), amTagItem.displayName, "", 'String'));
					}
					else
					{
						log.error("TAG ITEM ID = " + amTagItem.id + " COLUMN " + amTagItem.tagItemAttr + " is not a propery of " + dataObject)
					}
					
				}
				else if(amTagItem.tagItemType == 'CUSTOM')
				{
					log.info ("CUSTOM COLUMNS == " + amTagItem.displayName)
					table.putColumn(amTagItem.id.toString(), new ExportColumn(amTagItem.id.toString(), amTagItem.displayName, "", 'String'));

				}
				else
				{
					log.info ("BUSINESS OBJECT == " + amTagItem.tagItemType + " ID = " + amTagItem.id + " " + amTagItem.displayName)
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
							if(amTagItem.tagItemSubtype == 'PICKLIST')
							{
								def cc = ConceptCode.findByUniqueId(bioDataPropertyValue)
								bioDataDisplayValue = cc.codeName
								
							}
							else if(amTagItem.tagItemSubtype == 'PICKLIST')
							{
								def cc = ConceptCode.findByUniqueId(bioDataPropertyValue)
								bioDataDisplayValue = cc.codeName
								
							}
							else if(amTagItem.tagItemSubtype == 'FREETEXT')
							{
								bioDataDisplayValue = bioDataPropertyValue
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
							log.info("PARAMETERS " + bioDataObject.getUniqueId() + " " + amTagItem.id)
							def tagValues = AmTagDisplayValue.findAll('from AmTagDisplayValue a where a.subjectUid=? and a.amTagItem.id=?',[bioDataObject.getUniqueId().toString(),amTagItem.id])
							newrow.put(amTagItem.id.toString(),createDisplayString(tagValues));
						}
						else
						{
						    def tagValues = AmTagDisplayValue.findAllDisplayValue(it.uniqueId,amTagItem.id)
							newrow.put(amTagItem.id.toString(),createDisplayString(tagValues));
						}
	
					}
				}
				
				table.putRow(bioDataObject.id.toString(), newrow);
		}
		
		return table.toJSON_DataTables("", folderType).toString(5);
	}
	
	private createDisplayString(tagValues)
	{
		log.info ("TAGVALUES == " + tagValues)
		
		def displayValue = ""
		def counter = 0
						
		tagValues.each
		{
			log.info("TAGVALUE = " + it)
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

		if (folderId) 
		{
			folder = FmFolder.get(folderId)
			
			if(folder)
			{
				bioDataObject = getBioDataObject(folder)
				metaDataTagItems = getMetaDataItems(folder)
	
				// If the folder is a study then get the analysis and the assay
				if (folder.folderType.equalsIgnoreCase(FolderType.STUDY.name()) || folder.folderType.equalsIgnoreCase(FolderType.PROGRAM.name()))
				{
					def subFolderTypes = getChildrenFolderTypes(folder.id)
					log.info "subFolderTypes = " + subFolderTypes
					subFolderTypes.each
					{
						log.info "it = " + it
						subFolders = getChildrenFolderByType(folder.id, it)
						if(subFolders!=null && subFolders.size()>0)
						{
							log.info("subFolders == " + subFolders)

								subFolderLayout = formLayoutService.getLayout(it.toLowerCase());
								String gridData = createDataTable(subFolders, subFolders[0].folderType)
								log.info gridData
								jSONForGrids.add(gridData)
								log.info "ADDING JSON GRID"
						}
					}
				}
			}
		}
		
		log.info "FolderInstance = " + bioDataObject.toString()
		render(template:'/fmFolder/folderDetail', model:[folder:folder, bioDataObject:bioDataObject, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems, jSONForGrids: jSONForGrids])
		
	}

	private Object getMetaDataItems(folder)
	{
		def amTagTemplate = amTagTemplateService.getTemplate(folder.getUniqueId())
		def metaDataTagItems
		if(amTagTemplate)
		{
			metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
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
				metaDataTagItems = getMetaDataItems(folder)
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
		
		def paramMap = params
		def folderId = params.id
		def amTagTemplate
		def metaDataTagItems
		def folder
		def bioDataObject
		if (folderId)
		{
			folder = FmFolder.get(folderId)
			if(folder)
			{
				def folderUniqueId = folder.getUniqueId()
				def folderAssociation = FmFolderAssociation.findByFmFolder(folder)
				if(folderAssociation)
				{
					log.info "folderAssociation = " + folderAssociation
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
				
				amTagTemplate = amTagTemplateService.getTemplate(folderUniqueId)
				if(amTagTemplate)
				{
					metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
				}
				
				//Use metaDataTagItems to update fields
				for (tagItem in metaDataTagItems) {
					if (tagItem.tagItemType.equals('FIXED')) {
						def newValue = params."${tagItem.tagItemAttr}"
						if (newValue != null) {
							bioDataObject."${tagItem.tagItemAttr}" = newValue
						}
					}
					else if (tagItem.tagItemType.equals('CUSTOM') && tagItem.tagItemSubtype.equals('FREETEXT')) {
						//Look for new value by tag item ID
						def newValue = params."amTagItem_${tagItem.id}"
						if (newValue != null) {
							

							//Create a new AmTagValue and point to it
							def newTagValue = new AmTagValue(value: newValue)
							newTagValue.save()
							//TODO This is an awful way of generating UIDs and should be changed forthwith, posthaste, etc
							def newUid = newTagValue.id + ":" + newTagValue.value
							
							AmData amData = new AmData(uniqueId: newUid, amDataType: 'AM_TAG_VALUE')
							amData.id = newTagValue.id
							amData.save()
							
							AmTagAssociation ata = new AmTagAssociation(objectType: 'CUSTOM', subjectUid: folderUniqueId, objectUid: newUid, tagItemId: tagItem.id)
							ata.save()
							
//							
							
						}
					}
				}
				if (!bioDataObject.hasErrors() && bioDataObject.save(flush: true)) {
					log.info "Meta data saved"
					def result = [id: folderId]
					render result as JSON
					return
				}
				else {
					log.info "Errors occurred saving Meta data"
					metaDataTagItems  =  getMetaDataItems(folder)

					render(view: "editMetaData", model:[bioDataObject:bioDataObject, folder:folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
					//	render(view: "edit", model: [fmFolderInstance: fmFolderInstance])
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

}



//		log.info("Formlayout = " + formLayout)
/*		def subFolders
		def subFolderLayout
		if (folder)
		{
			subFolders = FmFolder.executeQuery("from FmFolder as fd where fd.folderLevel = :level and fd.folderFullName like '" + folder.folderFullName + "%' order by folderName", [level: folder.folderLevel + 1])
			if(subFolders!=null && subFolders.size()>0)
			{
				log.info(subFolders)
				def layoutType = subFolders[0].folderType
				subFolderLayout = formLayoutService.getLayout(layoutType.toLowerCase()); //'study');
			}
		}

		log.info "folder.id = " + folder.id
		if(folder.fmFiles){log.info("Files = " + folder.fmFiles)}
		
		ExportTableNew table;
		
		//Keep this if you want to cache the grid data
		//ExportTableNew table=(ExportTableNew)request.getSession().getAttribute("gridtable");
		
		if(table==null)
		{
			table=new ExportTableNew();
		}

		if(subFolders!=null && subFolders.size()>0)
		{
			subFolderLayout.each {
				table.putColumn("ident", new ExportColumn(it.column, it.displayName, "", "String"));
			}
		}
		
		ExportRowNew newrow=new ExportRowNew();
		newrow.put("ident", "foo.id");
		newrow.put("name", "foo.name");
		newrow.put("description", "foo.description");
		table.putRow("somerow", newrow);

		def jSONToReturn = table.toJSON_DataTables("").toString(5);
		
		request.getSession().setAttribute("gridtable", table);
*/
//		render(template:'/fmFolder/folderDetail', model:[layout: formLayout, folderInstance:folder, subFolderInstances:subFolders, subFolderLayout: subFolderLayout, jSONForGrid: jSONToReturn])
