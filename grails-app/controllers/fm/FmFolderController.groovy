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

import annotation.AmTagDisplayValue;

import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import grails.converters.*
import groovy.xml.StreamingMarkupBuilder
import com.recomdata.util.FolderType



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

    def show = {
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
        def fmFolderInstance = FmFolderController.get(params.id)
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

		//List<FmFolderController> folders = getFolder(FolderType.PROGRAM.name(), null)
		// 	[fn:this.folderFullName+"%", fl: (this.folderLevel + 1)])
		List<FmFolderController> folders = getFolder("Program", null)
		
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
		def folderContents = fmFolderService.getFolderContents(id)
		
		def folderSearchList = session['folderSearchList']
		def folderSearchString = folderSearchList ? folderSearchList.join("\\,") + "\\," : "" //Extra , - used to identify leaves
		
		render(template:'folders', model: [folders: folderContents.folders, files: folderContents.files, folderSearchString: folderSearchString])
	}

	/**
	 * Update incorrect or missing tag vaulues for folders.
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
			def parentFolder = FmFolderController.getAt(parentId)
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
		def folder = FmFolderController.getAt(params.folderId)
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
		def folder = FmFolderController.getAt(folderId)
		def oldFullName = folder.folderFullName
		def oldLevel = folder.folderLevel
		folder.folderFullName = newFolderFullName
		folder.folderLevel = newFolderLevel
			
		if(folder.save())
		{
			List<FmFolderController> subFolderList = FmFolderController.findAll("from FmFolder as fd where fd.folderFullName like :fn",
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
		def folder = FmFolderController.getAt(folderId)
		folder.activeInd = false
		
		if(folder.save())
		{
			List<FmFolderController> subFolderList = FmFolderController.findAll("from FmFolder as fd where fd.folderFullName like :fn and fd.folderLevel = :fl",
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
		if(parentPath == null)
		{
		 return FmFolder.executeQuery("from FmFolder as fd where fd.folderType = :fl ", [fl: folderType])
		}
		else
		{
		 return FmFolder.executeQuery("from FmFolder as fd where fd.folderType = :fl and fd.folderFullName like :fn ",
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
		return FmFolder.executeQuery("from FmFolder as fd where fd.folderFullName like :fn and fd.folderLevel= :fl and fd.folderType= :ft",[fl: folder.folderLevel+1, fn:folder.folderFullName+"%", ft: folderType])
	}

	//method which returns a list of folders which are the children of the folder of which the identifier is passed as parameter
	private List getChildrenFolderTypes(Long parentId)
	{
		def folder = FmFolder.get(parentId)
		return FmFolder.executeQuery("select distinct(fd.folderType) from FmFolder as fd where fd.folderFullName like :fn and fd.folderLevel= :fl ",[fl: folder.folderLevel+1, fn:folder.folderFullName+"%"])
	}

	private String createDataTable(layoutColumns, folders)
	{
/*		FormLayout				Long id
		String key;
				String column;
				String displayName;
				String dataType;
				Integer sequence;
				Boolean display=true;
	*/
		
		if (folders == null || folders.size() < 1) return
					
		ExportTableNew table=new ExportTableNew();
		
		def dataObject = getBioDataObject(folders[0])
		layoutColumns.each 
		{
			if(it.display && dataObject.hasProperty(it.column))
			{
				log.info ("COLUMNS == " + it.column + " " + it.displayName + " " +  it.dataType)
				table.putColumn(it.column, new ExportColumn(it.column, it.displayName, "", it.dataType));
			}
			else
			{
				log.error("COLUMN " + it.column + " is either set not to display or is not a propery of " + dataObject)
			}

		}
		
		folders.each 
		{
			def bioDataObject = getBioDataObject(it)
			
				ExportRowNew newrow=new ExportRowNew();
				layoutColumns.eachWithIndex()
				{obj, i -> 
					if(obj.display && bioDataObject.hasProperty(obj.column))
					{
						log.info("ROWS == " + obj.column + " " + bioDataObject[obj.column])
						newrow.put(obj.column,bioDataObject[obj.column]?bioDataObject[obj.column]:'');
					}		
				}
				
				table.putRow(bioDataObject.id.toString(), newrow);
		}
		
		return table.toJSON_DataTables("").toString(5);
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
		if (folderId) 
		{
			folder = FmFolder.get(folderId)
			
			if(folder)
			{
				// If the folder is a study then get the analysis and the assay
				if (folder.folderType.toLowerCase() == FolderType.STUDY.name().toLowerCase())
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
								String gridData = createDataTable(subFolderLayout,subFolders)
								log.info gridData
								jSONForGrids.add(gridData)
								log.info "ADDING JSON GRID"
						}
					}
				}
						
				bioDataObject = getBioDataObject(folder)
				//def fmData = FmData.get(folder.id)
				
				amTagTemplate = amTagTemplateService.getTemplate(folder.uniqueId)
				if(amTagTemplate)
				{
					metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
				}
				else
				{
					log.error "Unable to find amTagTemplate for object Id = " + folder.uniqueId
				}
	
				//  amTagTemplate.amTagItems
			}
		}

		log.info "FolderInstance = " + bioDataObject.toString()
		render(template:'/fmFolder/folderDetail', model:[folder:folder, bioDataObject:bioDataObject, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems, jSONForGrids: jSONForGrids])
		
	}


	private Object getBioDataObject(folder)
	{
		def bioDataObject
		def folderAssociation
		def fmData = FmData.get(folder.id)
		if(fmData)
		{
			folderAssociation = FmFolderAssociation.findByFmFolder(folder)
		}
		else
		{
			log.error("FmDataUid record was not found for folder id = " + folder.id)
		}
		
		
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
			
				amTagTemplate = amTagTemplateService.getTemplate(folder.uniqueId)
				if(amTagTemplate)
				{
					metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
				}
				else
				{
					log.error "Unable to find amTagTemplate for object Id = " + folder.uniqueId
				}
			}
			else
			{
				log.error "Unable to find folder for folder Id = " + folderId				
			}

		} 

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
				
				amTagTemplate = amTagTemplateService.getTemplate(folder.getUniqueId())
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
					else if (tagItem.tagItemType.equals('CUSTOM')) {
						//Look for new value by tag item ID
						def newValue = params."amTagItem_${tagItem.id}"
						if (newValue != null) {
							//TODO Update/create the tag value here!
//							def tagValueId = AmTagDisplayValue.get(folder.getUniqueId(), tagItem.id).objectId
//							if (displayValue) {
//								displayValue.displayValue = newValue
//							}
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
					def errors = bioDataObject.errors
					
					amTagTemplate = amTagTemplateService.getTemplate(folder.getUniqueId())
					if(amTagTemplate)
					{
						metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
					}
					else
					{
						log.error "Unable to find amTagTemplate for object Id = " + folder.uniqueId
					}
	
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
