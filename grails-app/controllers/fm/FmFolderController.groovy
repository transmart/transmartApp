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

import bio.Experiment

import fm.FmFolder
import fm.FmFile
import fm.FmFolderService
import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew

import groovy.xml.StreamingMarkupBuilder
// import grails.web.JSONBuilder
import com.recomdata.util.FolderType


import grails.converters.*
import annotation.AmTagItem

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
		
		def fmFolderInstance = new FmFolderController()
		fmFolderInstance.properties = params

        return [fmFolderInstance: fmFolderInstance, tenants:tenants ]
    }

    def save = {
        def fmFolderInstance = new FmFolderController(params)
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
		
		log.info params
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
		def folderContents = fmFolderService.getFolderContents(id, session['folderSearchMap'])
		
		render(template:'folders', model: [folders: folderContents.folders, files: folderContents.files])
	}


	
	private void addFolder(String folderType, FmFolderController fd, long parentId)
	{
		fd.folderType = folderType
		
		if(FolderType.PROGRAM.name() == folderType)
		{
			fd.folderLevel = 0			
		}
		else
		{
			def parentFolder = FmFolderController.getAt(parentId)
			fd.folderLevel = parentFolder.folderLevel + 1
			fd.parent = parentFolder
		}
		
		if(p.save()) 
		{
			render p as XML
		}
		else {
			render p.errors
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
	
	def folderDetail = {
		log.info "** action: folderDetail called!"
		def folderId = params.id
		log.info("PARAMS = " + params)
			
		def folder
		def bioDataObject
		def formLayout
		def amTagTemplate
		def metaDataTagItems
		if (folderId) 
		{
			folder = FmFolder.get(folderId)
			log.info "folder.objectUid = " + folder.objectUid
			
			def folderAssociation = FmFolderAssociation.findByObjectUid(folder.objectUid)
			
			
			if(folderAssociation)
			{
				log.info "folderAssociation = " + folderAssociation
				bioDataObject =folderAssociation.getBioObject()
			}
			else
			{
				log.error "Unable to find folderAssociation for object Id = " + folder.objectUid
			}

			if(!bioDataObject)
			{
				bioDataObject = folder
			}

						
			amTagTemplate = amTagTemplateService.getTemplate(folder.objectUid)
			if(amTagTemplate)
			{
				metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
			}
			else
			{
				log.error "Unable to find amTagTemplate for object Id = " + folder.objectUid
			}

			//  amTagTemplate.amTagItems
		}
		
		if(!formLayout)
		{			
	//		formLayout = formLayoutService.getProgramLayout()		
		}
		

		log.info "FolderInstance = " + bioDataObject.toString()
		render(template:'/fmFolder/folderDetail', model:[layout: formLayout, folderInstance:bioDataObject, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems])
		
	}

   
	def editMetaData = 
	{		
		log.info "editMetaData called"
		log.info "params = " + params
	
	//	FmFolder folder = new FmFolder("My Study") 
	// def layout = formLayoutService.getLayout(folder.folderType.toLowerCase()); //'study');
		
		//log.info "** action: expDetail called!"
		def expid = params.experimentId
		
		def exp
		if (expid) {
			exp = Experiment.get(expid)
		}
		
		log.info "exp.id = " + exp.id

		
		def layout = formLayoutService.getLayout('study');
		log.error "layout = " + layout
		
		def folder = exp
		log.error "folder = " + folder
		
		// due to 1.37 grails plugin bug, defer to view, then template
		// render(template:'/fmFolder/editMetaData', model:[folder:folder,layout:layout]);
		render(view: "editMetaData", model:[folder:folder,layout:layout]);
	}
	
	def updateMetaData =
	{
		log.info "updateMetaData called"
		
		// TODO
		// We need to only save the meta data of the FmFolder

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

def getStudyDetails = {
	/*
	Study access type	yes	Internal
	Study phase	yes	Phase II
	Study objective	yes	Biomarker discovery
	Study biomarker type	yes	PD biomarker, Patient selection biomarker
	Study compound/biologics/company	yes	SAR245408 (XL147)
	Pathology	yes	endometrial cancer
	Study design factors	yes	treatment
	Number of followed subjects	yes	37
	Organism	yes	Homo sapiens
	Study design	yes	Interventional
	Study date	yes	2009
*/
	}

def getAssayDetails = {
	/*
	Biosource	yes	Endometrial tumor
	Measurement type	yes	Histology
	Technology	yes	H&E
	Vendor	yes	none
	Platform design	yes	none
	Biomarkers studied	yes	none
	Type of biomarkers studied	yes	none
	Assay description	yes	Hematoxylin and Eosin Staining of Formalin Fixed Paraffin Embedded Tissues
	*/
	}

def getAnalysisDetails = {
}

def getFdDetails = {
}

	def checkForNewFiles = {
		
		fmFolderService.checkForNewFiles();
		
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

