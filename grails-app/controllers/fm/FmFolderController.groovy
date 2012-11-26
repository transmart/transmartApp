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

import fm.FmFolder;
import fm.FmFile

import groovy.xml.StreamingMarkupBuilder
// import grails.web.JSONBuilder
import com.recomdata.util.FolderType


import grails.converters.*

class FmFolderController {

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

		List<FmFolderController> folders = getFolder(FolderType.PROGRAM.name(), null)
		// 	[fn:this.folderFullName+"%", fl: (this.folderLevel + 1)])
		
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
	
	def getAllPrograms = {
		def folders = FmFolder.executeQuery("from FmFolder as fd where fd.folderType = 'Program' order by folderName")
		render(template:'programs', model: [folders: folders])
	}
	
	def getFolderContents = {
		def id = params.id
		def parent = FmFolder.get(id)
		
		//Temporary stupid way to do this - get all folders at level+1 with matching path
		def folders = FmFolder.executeQuery("from FmFolder as fd where fd.folderLevel = :level and fd.folderFullName like '" + parent.folderFullName + "%' order by folderName", [level: parent.folderLevel + 1])
		render(template:'folders', model: [folders: folders, files: parent.fmFiles])
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

	private List<FmFolderController> getFolder(String folderType, String parentPath)  
	{		
		if(parentPath == null)
		{
		 return FmFolder.executeQuery("from FmFolder as fd where fd.folderType = :fl ", [fl: folderType])
		}
		else
		{
		 return FmFolder.executeQuery("from FmFolder as fd where fd.folderType = :fl and fd.folderFullName like :fn ",
				 [fl: folderType, fn:this.folderFullName+"%"])
		}
	
	}

}