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

import annotation.AmTagDisplayValue
import annotation.AmTagItem
import annotation.AmTagTemplate
import annotation.AmTagTemplateAssociation
import auth.AuthUser
import org.transmart.biomart.BioAssayAnalysis
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.biomart.BioDataExternalCode
import org.transmart.biomart.ConceptCode
import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew
import com.recomdata.util.FolderType
import de.DeMrnaAnnotation
import grails.converters.JSON
import grails.converters.XML
import grails.validation.ValidationException
import groovy.xml.StreamingMarkupBuilder
import org.apache.commons.lang.StringUtils
import org.slf4j.LoggerFactory
import org.transmart.biomart.BioAssayPlatform
import org.transmart.biomart.Experiment
import search.SearchKeyword

import javax.activation.MimetypesFileTypeMap

class FmFolderController {

    def formLayoutService
    def amTagTemplateService
    def amTagItemService
    def fmFolderService
    def ontologyService
    def solrFacetService
    def springSecurityService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    @Lazy
    static MimetypesFileTypeMap MIME_TYPES_FILES_MAP = {
        File mimeTypesFile = [
                new File(System.getenv('HOME'), '.mime.types'),
                new File(System.getenv('JAVA_HOME'), 'lib/mime.types'),
                new File('/etc/mime.types')
        ].findResult null, {File file ->
            if (file.exists()) {
                return file
            }
        }

        if (!mimeTypesFile) {
            LoggerFactory.getLogger(this).warn 'Could not find a mime.types file'
        } else {
            LoggerFactory.getLogger(this).info "Loading mime.types file on $mimeTypesFile"
        }

        mimeTypesFile.withInputStream {
            new MimetypesFileTypeMap(it)
        }
    }()

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)

        [fmFolderInstanceList: FmFolder.list(params), fmFolderInstanceTotal: FmFolder.count()]
    }


    public String serializeFoldersToXMLFile() {
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
        def bioDataObject = new BioAssayAnalysis()
        def amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.ANALYSIS.name())
        def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
        def title = "Create Analysis"
        def templateType = "createAnalysisForm"
        def measurements = BioAssayPlatform.executeQuery("SELECT DISTINCT platformType FROM BioAssayPlatform as p ORDER BY p.platformType")
        def vendors = BioAssayPlatform.executeQuery("SELECT DISTINCT vendor FROM BioAssayPlatform as p ORDER BY p.vendor")
        def technologies = BioAssayPlatform.executeQuery("SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ORDER BY p.platformTechnology")
        def platforms = BioAssayPlatform.executeQuery("FROM BioAssayPlatform as p ORDER BY p.name")

        log.info measurements
        log.info technologies
        log.info vendors
        log.info platforms

        render(template: "createAnalysis", model: [bioDataObject: bioDataObject, measurements: measurements, technologies: technologies, vendors: vendors, platforms: platforms, folder: folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
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
        def measurements = BioAssayPlatform.executeQuery("SELECT DISTINCT platformType FROM BioAssayPlatform as p ORDER BY p.platformType")
        def vendors = BioAssayPlatform.executeQuery("SELECT DISTINCT vendor FROM BioAssayPlatform as p ORDER BY p.vendor")
        def technologies = BioAssayPlatform.executeQuery("SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ORDER BY p.platformTechnology")
        def platforms = BioAssayPlatform.executeQuery("FROM BioAssayPlatform as p ORDER BY p.name")

        log.info measurements
        log.info technologies
        log.info vendors
        log.info platforms

        render(template: "createAssay", model: [bioDataObject: bioDataObject, measurements: measurements, technologies: technologies, vendors: vendors, platforms: platforms, folder: folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
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
        if (!amTagTemplate) {
            log.error("Unable to find tag template for folder type = ")
        }

        def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
        def title = "Create Folder"
        def templateType = "createFolderForm"
        render(template: "createFolder", model: [bioDataObject: bioDataObject, folder: folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
    }

    def createStudy = {
        log.info "createStudy called"
        log.info "params = " + params
        //log.info "** action: expDetail called!"

        def folder = new FmFolder()
        folder.folderType = FolderType.STUDY.name()
        def parentFolder = FmFolder.get(params.folderId)
        folder.parent = parentFolder
        def bioDataObject = new Experiment()
        def amTagTemplate = AmTagTemplate.findByTagTemplateType(FolderType.STUDY.name())
        def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
        def title = "Create Study"
        def templateType = "createStudyForm"
        render(template: "createStudy", model: [bioDataObject: bioDataObject, folder: folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
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
        render(template: "createProgram", model: [bioDataObject: bioDataObject, folder: folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
    }


    private createAmTagTemplateAssociation(folderType, folder) {
        def amTagTemplate = AmTagTemplate.findByTagTemplateType(folderType)
        AmTagTemplateAssociation atta = new AmTagTemplateAssociation(tagTemplateId: amTagTemplate.id, objectUid: folder.getUniqueId())

        if (!atta.save(flush: true)) {
            atta.errors.each {
                log.error it
            }
        }
    }

    def save = {
        log.info params
        def fmFolderInstance = new FmFolder(params)
        if (fmFolderInstance.save(flush: true)) {
            redirect(action: "show", id: fmFolderInstance.id)
        } else {
            render(view: "create", model: [fmFolderInstance: fmFolderInstance])
        }
    }

    def saveProgram = {
        log.info "saveProgram called"
        log.info params

        def folder = new FmFolder(params)
        folder.folderLevel = 0
        folder.folderType = FolderType.PROGRAM.name()

        try {
            fmFolderService.saveFolder(folder, folder, params)
            def result = [id: folder.id]
            render result as JSON
            solrFacetService.reindexFolder(folder.getUniqueId())
        } catch (ValidationException ex) {
            log.error "Unable to save program"
            def errors = g.renderErrors(bean: ex.errors)
            log.error errors
            def result = [errors: errors]
            render result as JSON
        } catch (Exception ex) {
            log.error "Exception in FmFolderController.saveProgram", ex
            def result = [errors: "<ul><li>An unexpected error has occurred. If this error persits, please click \"Close\" or \"Cancel\" to close this dialog box.<br><br>Error details: " + ex.getMessage() + "</li></ul>"]
            render result as JSON
        }

    }

    def saveStudy = {
        log.info "saveStudy called"
        log.info params

        def parentFolder = FmFolder.get(params.parentId)
        if (!parentFolder) {
            log.error "Parent folder is null"
            def result = [errors: "<ul><li>Unexpected error: the parent folder ID is missing.</li></ul>"]
            render result
            return
        }

        def folder = new FmFolder(params)
        folder.folderLevel = parentFolder.folderLevel + 1
        folder.folderType = FolderType.STUDY.name()
        folder.parent = parentFolder

        def experiment = new Experiment()
        experiment.title = folder.folderName
        experiment.description = folder.description
        experiment.type = "Experiment"

        try {
            fmFolderService.saveFolder(folder, experiment, params)
            def result = [id: folder.id, parentId: folder.parentId]
            render result as JSON
            solrFacetService.reindexFolder(folder.getUniqueId())
        } catch (ValidationException ex) {
            log.error "Unable to save study"
            def errors = g.renderErrors(bean: ex.errors)
            log.error errors
            def result = [errors: errors]
            render result as JSON
        } catch (Exception ex) {
            log.error "Exception in FmFolderController.saveStudy", ex
            def result = [errors: "<ul><li>An unexpected error has occurred. If this error persits, please click \"Close\" or \"Cancel\" to close this dialog box.<br><br>Error details: " + ex.getMessage() + "</li></ul>"]
            render result as JSON
        }

    }

    def saveAssay = {
        log.info "saveAssay called"
        log.info params

        def parentFolder = FmFolder.get(params.parentId)
        if (!parentFolder) {
            log.error "Parent folder is null"
            def result = [errors: "<ul><li>Unexpected error: the parent folder ID is missing.</li></ul>"]
            render result
            return
        }

        def folder = new FmFolder(params)
        folder.folderLevel = parentFolder.folderLevel + 1
        folder.folderType = FolderType.ASSAY.name()
        folder.parent = parentFolder

        try {
            fmFolderService.saveFolder(folder, folder, params)
            def result = [id: folder.id, parentId: folder.parentId]
            render result as JSON
            solrFacetService.reindexFolder(folder.getUniqueId())
        } catch (ValidationException ex) {
            log.error "Unable to save assay"
            def errors = g.renderErrors(bean: ex.errors)
            log.error errors
            def result = [errors: errors]
            render result as JSON
        } catch (Exception ex) {
            log.error "Exception in FmFolderController.saveAssay", ex
            def result = [errors: "<ul><li>An unexpected error has occurred. If this error persits, please click \"Close\" or \"Cancel\" to close this dialog box.<br><br>Error details: " + ex.getMessage() + "</li></ul>"]
            render result as JSON
        }

    }

    def saveAnalysis = {
        log.info "saveAnalysis called"
        log.info params

        def parentFolder = FmFolder.get(params.parentId)
        if (!parentFolder) {
            log.error "Parent folder is null"
            def result = [errors: "<ul><li>Unexpected error: the parent folder ID is missing.</li></ul>"]
            render result
            return
        }

        def folder = new FmFolder(params)
        folder.folderLevel = parentFolder.folderLevel + 1
        folder.folderType = FolderType.ANALYSIS.name()
        folder.parent = parentFolder

        def analysis = new BioAssayAnalysis()
        analysis.name = folder.folderName
        analysis.shortDescription = folder.description
        analysis.longDescription = folder.description
        analysis.analysisMethodCode = "TBD"
        analysis.assayDataType = "TBD"
        analysis.dataCount = -1
        analysis.teaDataCount = -1

        try {
            fmFolderService.saveFolder(folder, analysis, params)
            def result = [id: folder.id, parentId: folder.parentId]
            render result as JSON
            solrFacetService.reindexFolder(folder.getUniqueId())
        } catch (ValidationException ex) {
            log.error "Unable to save study"
            def errors = g.renderErrors(bean: ex.errors)
            log.error errors
            def result = [errors: errors]
            render result as JSON
        } catch (Exception ex) {
            log.error "Exception in FmFolderController.saveAnalysis", ex
            def result = [errors: "<ul><li>An unexpected error has occurred. If this error persits, please click \"Close\" or \"Cancel\" to close this dialog box.<br><br>Error details: " + ex.getMessage() + "</li></ul>"]
            render result as JSON
        }

    }

    def saveFolder = {
        log.info "saveFolder called"
        log.info params

        def parentFolder = FmFolder.get(params.parentId)
        if (!parentFolder) {
            log.error "Parent folder is null"
            def result = [errors: "<ul><li>Unexpected error: the parent folder ID is missing.</li></ul>"]
            render result
            return
        }

        def folder = new FmFolder(params)
        folder.folderLevel = parentFolder.folderLevel + 1
        folder.folderType = FolderType.FOLDER.name()
        folder.parent = parentFolder

        try {
            fmFolderService.saveFolder(folder, folder, params)
            def result = [id: folder.id, parentId: folder.parentId]
            render result as JSON
            solrFacetService.reindexFolder(folder.getUniqueId())
        } catch (ValidationException ex) {
            log.error "Unable to save folder"
            def errors = g.renderErrors(bean: ex.errors)
            log.error errors
            def result = [errors: errors]
            render result as JSON
        } catch (Exception ex) {
            log.error "Exception in FmFolderController.saveFolder", ex
            def result = [errors: "<ul><li>An unexpected error has occurred. If this error persits, please click \"Close\" or \"Cancel\" to close this dialog box.<br><br>Error details: " + ex.getMessage() + "</li></ul>"]
            render result as JSON
        }

    }

    def showStudy = {
        def fmFolderInstance = FmFolder.get(params.id)

        // test the class
        // def json = new JSONSerializer(target: fmFolderInstance).getJSON()
        // log.info json

        def data = serializeFoldersToXMLFile()
        log.info data

        if (!fmFolderInstance) {
            redirect(action: "list")
        } else {
            [fmFolderInstance: fmFolderInstance, data: data]
        }
    }

    def edit = {
        def fmFolderInstance = FmFolder.get(params.id)
        if (!fmFolderInstance) {
            redirect(action: "list")
        } else {
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
            } else {
                render(view: "edit", model: [fmFolderInstance: fmFolderInstance])
            }
        } else {
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
        } else {
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

    def getAssayes = {
        List<FmFolder> folders = getFolder(FolderType.ASSAY.name(), params.parentPath)
        render folders as XML
    }

    //service to call to get all the children of a folder, regardless their type
    //need a parameter parentId corresponding to the parent identifier
    def getAllChildren = {
        List<FmFolder> children = fmFolderService.getChildrenFolder(params.parentId)
        render children as XML
    }

    //service to call to get all experiments objects that are associated with a folder in fm_folder_association table
    def getExperiments = {
        def assocs = FmFolderAssociation.findAll("from FmFolderAssociation as fd where fd.objectType='org.transmart.biomart.Experiment'")
        render(contentType: "text/xml") {
            experiments {
                for (assoc in assocs) {
                    def exp = assoc.getBioObject()
                    if (exp != null) {
                        experiment(id: exp.id) {
                            accession(exp.accession)
                            title(exp.title?.encodeAsHTML())
                            folderId(assoc.fmFolder.id)
                            folderUid(assoc.fmFolder.uniqueId)
                        }
                    }
                }
            }
        }
    }

    //service to get analyses details, mainly analysis unique id and title
    def getAnalysesDetails = {
        def assocs = FmFolderAssociation.findAll("from FmFolderAssociation as fd where fd.objectType='org.transmart.biomart.BioAssayAnalysis'")
        render(contentType: "text/xml") {
            analyses {
                for (assoc in assocs) {
                    def folder = assoc.fmFolder
                    if (folder != null) {
                        analysis(id: assoc.objectUid) {
                            title(assoc.fmFolder.folderName)
                            folderId(assoc.fmFolder.id)
                            parentId(assoc.fmFolder.parent.id)
                        }
                    }
                }
            }
        }
    }

    def addProgram = {
        def p = new FmFolder(params['fmFolder'])
        doAddFolder(FolderType.PROGRAM.name(), p, null)
    }

    def addStudy = {
        def p = new FmFolder(params['fmFolder'])
        doAddFolder(FolderType.STUDY.name(), p, params['parentId'])
    }

    def addFolder = {
        def p = new FmFolder(params['fmFolder'])
        doAddFolder(FolderType.STUDY.name(), p, params['parentId'])
    }

    def addAnalysis = {
        def p = new FmFolder(params['fmFolder'])
        doAddFolder(FolderType.ANALYSIS.name(), p, params['parentId'])
    }

    def addFile = {
        FmFolder p = FmFolder.get(params['folderId'])
        FmFile f = new FmFile()
        f.properties = params
        if (f.save(flush: true)) {
            p.addToFmFiles(f)
            if (p.save(flush: true)) {
                render p as XML
            } else {
                render p.errors
            }

        } else {
            render f.errors
        }


        doAddFolder(FolderType.STUDY.name(), p, params['parentId'])
    }

    def getFolderContents = {
        def id = params.id
        if (!id) {
            id = FmFolder.findByUniqueId(params.uid).id
        }
        def auto = params.boolean('auto')
        //Flag for whether folder was automatically opened - if not, then it shouldn't respect the folder mask
        def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        def folderContentsAccessLevelMap = fmFolderService.getFolderContentsWithAccessLevelInfo(user, id)
        def folderContents = new ArrayList(folderContentsAccessLevelMap.keySet())
        def folderSearchLists = session['folderSearchList']
        if (!folderSearchLists) {
            folderSearchLists = [[], []]
        }
        def folderSearchString = folderSearchLists[0] ? folderSearchLists[0].join(",") + "," : ""
        //Extra , - used to identify leaves
        def uniqueLeavesString = folderSearchLists[1] ? folderSearchLists[1].join(",") + "," : ""
        def nodesToExpand = session['rwgOpenedNodes']
        //check that all folders from folderContents are in the search path, or children of nodes in the search path
        if (folderSearchLists[0].size() > 0) {
            for (def folder : folderContents) {
                boolean found = false
                for (String path : folderSearchLists[0]) {
                    if (path.indexOf(folder.folderFullName) > -1 || folder.folderFullName.indexOf(path) > -1) {
                        found = true
                        break
                    }
                }
                if (!found) {
                    folderContents -= folder
                }
            }
        }
        def displayMetadata = "";
        //if there is an accession in filters, add the study node (there is just one) in the array for nodes to expand
        if (session['rwgSearchFilter'] != null) {
            def filters = session['rwgSearchFilter']
            for (def filter in filters) {
                if (filter != null && filter.indexOf("|ACCESSION;") > -1) {
                    for (def folder : folderContents) {
                        if (folder.folderType == "STUDY") {
                            if (!nodesToExpand.grep(folder.uniqueId)) {
                                nodesToExpand += folder.uniqueId
                                displayMetadata = folder.uniqueId
                            }
                        }
                    }
                }
            }
        }
        render(template: 'folders', model: [folders: folderContents, folderContentsAccessLevelMap: folderContentsAccessLevelMap, folderSearchString: folderSearchString, uniqueLeavesString: uniqueLeavesString, auto: auto, nodesToExpand: nodesToExpand, displayMetadata: displayMetadata])
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

    private void doAddFolder(String folderType, FmFolder folder, long parentId) {
        folder.folderType = folderType

        if (FolderType.PROGRAM.name() == folderType) {
            folder.folderLevel = 0
        } else {
            def parentFolder = FmFolder.getAt(parentId)
            folder.folderLevel = parentFolder.folderLevel + 1
            folder.parent = parentFolder
        }

        if (folder.save(flush: true)) {
            // Set folder's tag value based on a radix-36 conversion of its ID.
            folder.tag = Long.toString(folder.id, 36).toUpperCase();
            folder.save(flush: true);
            solrFacetService.reindexFolder(folder.getUniqueId())

            render folder as XML
        } else {
            render folder.errors
        }
    }

    private void updateFolder() {
        def folder = FmFolder.getAt(params.folderId)
        folder.properties = params

        if (folder.save()) {
            solrFacetService.reindexFolder(folder.getUniqueId())
            render folder as XML
        } else {
            render folder.errors
        }
    }

    private void moveFolder(long folderId, String newFolderFullName, String newFolderLevel) {
        def folder = FmFolder.getAt(folderId)
        def oldFullName = folder.folderFullName
        def oldLevel = folder.folderLevel
        folder.folderFullName = newFolderFullName
        folder.folderLevel = newFolderLevel

        if (folder.save()) {
            List<FmFolder> subFolderList = FmFolder.findAll("from FmFolder as fd where fd.folderFullName like :fn",
                    [fn: oldFullName + "%"])

            subFolderList.each {

                println it
                def folderLevelDifferential = it.folderLevel - oldLevel
                // it.folderLevel =
                // it.folderFullName =
                moveFolder(it.id, newFolderFullName + it.folderName + "\\", newFolderLevel + folderLevelDifferential)

            }

            render folder as XML

        } else {
            render folder.errors
        }
    }

    private void removeFolder(long folderId) {
        def folder = FmFolder.getAt(folderId)
        folder.activeInd = false

        if (folder.save()) {
            List<FmFolder> subFolderList = FmFolder.findAll("from FmFolder as fd where fd.folderFullName like :fn and fd.folderLevel = :fl",
                    [fn: folder.folderFullName + "%", fl: (folder.folderLevel + 1)])

            subFolderList.each {
                println it
                removeFolder(it.id)
            }

            render folder as XML

        } else {
            render folder.errors
        }
    }

    private List<FmFolder> getFolder(String folderType, String parentPath) {
        log.info("getFolder(" + folderType + ", " + parentPath + ")")
        if (parentPath == null) {
            return FmFolder.executeQuery("from FmFolder as fd where fd.activeInd = true and upper(fd.folderType) = upper(:fl) ", [fl: folderType])
        } else {
            return FmFolder.executeQuery("from FmFolder as fd where fd.activeInd = true and upper(fd.folderType) = upper(:fl) and fd.folderFullName like :fn ",
                    [fl: folderType, fn: parentPath + "%"])
        }

    }

    private String createDataTable(Map<FmFolder, String> subFoldersAccessLevelMap, String folderType) {

        if (!subFoldersAccessLevelMap) {
            return '{}'
        }

        Set<FmFolder> folders = subFoldersAccessLevelMap.keySet()
        ExportTableNew table = new ExportTableNew();

        def dataObject
        def childMetaDataTagItems

        for (folder in folders) {
            dataObject = getBioDataObject(folder)
            childMetaDataTagItems = getChildMetaDataItems(folder)
            if (dataObject && childMetaDataTagItems) {
                break
            }
        }

        childMetaDataTagItems.eachWithIndex()
                {obj, i ->
                    //
                    AmTagItem amTagItem = obj
                    if (amTagItem.viewInChildGrid) {
                        if (amTagItem.tagItemType == 'FIXED') {
                            log.info("CREATEDATATABLE::FIXED TYPE == " + amTagItem.tagItemType + " ID = " + amTagItem.id + " " + amTagItem.displayName)

                            if (dataObject.hasProperty(amTagItem.tagItemAttr)) {
                                //log.info ("CREATEDATATABLE::FIXED COLUMNS == " + amTagItem.tagItemAttr + " " + amTagItem.displayName)
                                table.putColumn(amTagItem.id.toString(), new ExportColumn(amTagItem.id.toString(), amTagItem.displayName, "", 'String'));
                            } else {
                                log.error("CREATEDATATABLE::TAG ITEM ID = " + amTagItem.id + " COLUMN " + amTagItem.tagItemAttr + " is not a propery of " + dataObject)
                            }

                        } else if (amTagItem.tagItemType == 'CUSTOM') {
                            log.info("CREATEDATATABLE::CUSTOM == " + amTagItem.tagItemType + " ID = " + amTagItem.id + " " + amTagItem.displayName)
                            table.putColumn(amTagItem.id.toString(), new ExportColumn(amTagItem.id.toString(), amTagItem.displayName, "", 'String'));

                        } else {
                            log.info("CREATEDATATABLE::BUSINESS OBJECT == " + amTagItem.tagItemType + " ID = " + amTagItem.id + " " + amTagItem.displayName)
                            table.putColumn(amTagItem.id.toString(), new ExportColumn(amTagItem.id.toString(), amTagItem.displayName, "", 'String'));
                        }
                    } else {
                        log.info("COLUMN " + amTagItem.displayName + " is not to display in grid")
                    }

                }

        folders.each {folderObject ->
            log.info "FOLDER::$folderObject"

            def bioDataObject = getBioDataObject(folderObject)
            ExportRowNew newrow = new ExportRowNew();
            childMetaDataTagItems.eachWithIndex() {obj, i ->
                AmTagItem amTagItem = obj
                if (amTagItem.viewInChildGrid) {
                    if (amTagItem.tagItemType == 'FIXED' && bioDataObject.hasProperty(amTagItem.tagItemAttr)) {
                        def bioDataDisplayValue = null
                        def bioDataPropertyValue = bioDataObject[amTagItem.tagItemAttr]
                        if (amTagItem.tagItemSubtype == 'PICKLIST' || amTagItem.tagItemSubtype == 'MULTIPICKLIST') {
                            if (bioDataPropertyValue) {
                                def cc = ConceptCode.findByUniqueId(bioDataPropertyValue)
                                if (cc) {
                                    bioDataDisplayValue = cc.codeName
                                } else {
                                    bioDataDisplayValue = ""
                                }
                            } else {
                                bioDataDisplayValue = ""
                            }

                        } else if (amTagItem.tagItemSubtype == 'FREETEXT') {
                            bioDataDisplayValue = createTitleString(amTagItem, bioDataPropertyValue, folderObject, subFoldersAccessLevelMap[folderObject] != 'LOCKED')
                        } else if (amTagItem.tagItemSubtype == 'FREETEXTAREA') {
                            bioDataDisplayValue = amTagItem.displayName
                        } else {
                            log.error "FIXED ATTRIBUTE ERROR::Unknown tagItemSubType"
                        }

                        log.info("ROWS == " + amTagItem.tagItemAttr + " " + bioDataObject[amTagItem.tagItemAttr])
                        newrow.put(amTagItem.id.toString(), bioDataDisplayValue ? bioDataDisplayValue : '');
                    } else if (amTagItem.tagItemType == 'CUSTOM') {
                        def tagValues = AmTagDisplayValue.findAll('from AmTagDisplayValue a where a.subjectUid=? and a.amTagItem.id=?', [bioDataObject.getUniqueId().toString(), amTagItem.id])
                        log.info("CUSTOM PARAMETERS " + bioDataObject.getUniqueId() + " " + amTagItem.id + " tagValues " + tagValues)
                        newrow.put(amTagItem.id.toString(), createDisplayString(tagValues));
                    } else {
                        def tagValues = AmTagDisplayValue.findAllDisplayValue(folderObject.uniqueId, amTagItem.id)
                        log.info("BIOOBJECT PARAMETERS " + folderObject.uniqueId + " " + amTagItem.id + " tagValues " + tagValues)

                        newrow.put(amTagItem.id.toString(), createDisplayString(tagValues));
                    }

                }
            }

            table.putRow(bioDataObject.id.toString(), newrow);
        }

        return table.toJSON_DataTables("", folderType).toString(5);
    }

    //FIXME Quick hack to make title properties act as hyperlinks.
    //These name properties should be indicated in the database, and the sort value should be specified (needs a rewrite of our ExportTable)
    def nameProperties = ['assay name', 'analysis name', 'study title', 'program title', 'folder name']

    private createTitleString(AmTagItem amTagItem, String name, FmFolder folderObject, boolean isLink = true) {
        def tagName = amTagItem.displayName
        if (nameProperties.contains(tagName.toLowerCase())) {
            def titleHtml = isLink ? "<a href='#' onclick='openFolderAndShowChild(${folderObject.parent?.id}, ${folderObject.id})'>$name</a>" : name
            //Comment with name at the start to preserve the sort order - precaution against "--" being included in a name
            return "<!-- ${name.replace('--', '_')} -->$titleHtml"
        }
        return name
    }

    private createDisplayString(tagValues) {
        // log.info ("createDisplayString::TAGVALUES == " + tagValues)

        def displayValue = ""
        if (tagValues) {
            displayValue = tagValues*.displayValue.join(", ")
        }
        return displayValue
    }

    def folderDetail() {
        log.debug "** action: folderDetail called!"
        def folderId = params.id
        log.debug "PARAMS = $params"

        def folder
        def subFolders
        def subFolderLayout
        def bioDataObject
        def amTagTemplate
        def metaDataTagItems
        def jSONForGrids = []
        def subjectLevelDataAvailable = false
        def measurements
        def technologies
        def vendors
        def platforms
        Map searchHighlight

        if (folderId) {
            folder = FmFolder.get(folderId)

            if (folder) {
                if (!folder.activeInd) {
                    render(template: 'deletedFolder')
                    return
                }

                bioDataObject = getBioDataObject(folder)
                metaDataTagItems = getMetaDataItems(folder, false)
                log.info "metaDataTagItems  = " + metaDataTagItems

                //If the folder is a study, check for subject-level data being available
                if (folder.folderType.equalsIgnoreCase(FolderType.STUDY.name()) && bioDataObject != null && bioDataObject.hasProperty("accession")) {
                    subjectLevelDataAvailable = ontologyService.checkSubjectLevelData(bioDataObject.accession)
                }

                if (folder.folderType.equalsIgnoreCase(FolderType.ASSAY.name()) && folder.folderType.equalsIgnoreCase(FolderType.ANALYSIS.name())) {
                    measurements = BioAssayPlatform.executeQuery("SELECT DISTINCT platformType FROM BioAssayPlatform as p ORDER BY p.platformType")
                    vendors = BioAssayPlatform.executeQuery("SELECT DISTINCT vendor FROM BioAssayPlatform as p ORDER BY p.vendor")
                    technologies = BioAssayPlatform.executeQuery("SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ORDER BY p.platformTechnology")
                    platforms = BioAssayPlatform.executeQuery("FROM BioAssayPlatform as p ORDER BY p.name")
                }

                def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
                def subFolderTypes = fmFolderService.getChildrenFolderTypes(folder.id)
                log.debug "subFolderTypes = subFolderTypes"
                subFolderTypes.each {
                    log.debug "it = $it"
                    subFolders = fmFolderService.getChildrenFolderByType(folder.id, it)
                    if (subFolders != null && subFolders.size() > 0) {
                        log.debug "${subFolders.size()} subFolders == $subFolders"
                        def subFoldersAccessLevelMap = fmFolderService.getAccessLevelInfoForFolders(user, subFolders)
                        subFolderLayout = formLayoutService.getLayout(it.toLowerCase());
                        String gridTitle = "Associated " + StringUtils.capitalize(subFolders[0].pluralFolderTypeName.toLowerCase())
                        String gridData = createDataTable(subFoldersAccessLevelMap, gridTitle)
                        //	log.info gridData
                        jSONForGrids.add(gridData)
                        log.debug "ADDING JSON GRID"
                    }
                }

                // Highlight search terms (if specified by RWGController)
                def categoryList = session['rwgCategorizedSearchTerms']
                searchHighlight = solrFacetService.getSearchHighlight(folder, categoryList)
            }
        }

        log.debug "FolderInstance = ${bioDataObject}"
        render template: '/fmFolder/folderDetail',
                model: [
                        folder: folder,
                        bioDataObject: bioDataObject,
                        measurements: measurements,
                        technologies: technologies,
                        vendors: vendors,
                        platforms: platforms,
                        amTagTemplate: amTagTemplate,
                        metaDataTagItems: metaDataTagItems,
                        jSONForGrids: jSONForGrids,
                        subjectLevelDataAvailable: subjectLevelDataAvailable,
                        searchHighlight: searchHighlight
                ]
    }

    def analysisTable = {
        try {
            def analysisId = params.id
            def genes = []
            def geneFilter = session['geneFilter']
            //Convert gene filter to a straight list
            if (geneFilter) {
                geneFilter = geneFilter.substring(5).split("::")[0].replace("|", "/").split("/")
            }

            //For each gene (ignore pathways), add the gene name and any synonyms to the list to match against
            for (item in geneFilter) {
                if (item.startsWith("GENE")) {
                    def sk = SearchKeyword.findByUniqueId(item)
                    def synonyms = BioDataExternalCode.findAllWhere(bioDataId: sk.bioDataId, codeType: "SYNONYM")
                    genes.push(sk.keyword)
                    for (s in synonyms) {
                        genes.push(s.code)
                    }
                }
            }

            def criteriaParams = [:]
            if (!params.boolean('full')) {
                criteriaParams.put('max', 1000)
            }
            def rows = BioAssayAnalysisData.createCriteria().list(criteriaParams) {
                eq('analysis', BioAssayAnalysis.get(analysisId))
                order('rawPvalue', 'asc')
            }

            ExportTableNew table = new ExportTableNew()
            table.putColumn("probe", new ExportColumn("probe", "Probe", "", 'String'));
            table.putColumn("gene", new ExportColumn("gene", "Gene", "", 'String'));
            table.putColumn("pvalue", new ExportColumn("pvalue", "p-value", "", 'Number'));
            table.putColumn("apvalue", new ExportColumn("apvalue", "Adjusted p-value", "", 'Number'));
            table.putColumn("teapvalue", new ExportColumn("teapvalue", "TEA-adjusted p-value", "", 'Number'));
            table.putColumn("foldchangeratio", new ExportColumn("foldchangeratio", "Fold Change Ratio", "", 'Number'));

            rows.each {
                def rowGenes = (DeMrnaAnnotation.findAll("from DeMrnaAnnotation as a where a.probesetId=? and geneSymbol is not null", [it.probesetId]))*.geneSymbol
                def lowerGenes = []
                for (gene in rowGenes) {
                    lowerGenes.push(gene.toLowerCase())
                }
                def foundGene = false
                for (gene in genes) {
                    if (gene.toLowerCase() in lowerGenes) {
                        foundGene = true
                        break
                    }
                }

                if (foundGene || !genes) {
                    ExportRowNew newrow = new ExportRowNew()
                    newrow.put("probe", it.probeset);
                    newrow.put("gene", rowGenes.join(", "));
                    newrow.put("pvalue", it.rawPvalue.toString());
                    newrow.put("apvalue", it.adjustedPvalue.toString());
                    newrow.put("teapvalue", it.teaNormalizedPValue.toString());
                    newrow.put("foldchangeratio", it.foldChangeRatio.toString());
                    table.putRow(it.id.toString(), newrow);
                }
            }

            def analysisData = table.toJSON_DataTables("", "Analysis Data");
            analysisData.put("rowCount", rows.getTotalCount())
            analysisData.put("filteredByGenes", genes.size() > 0)

            render(contentType: "text/json", text: analysisData.toString(5))
        } catch (Exception e) {
            log.error('Error while building analysis table', e)
        }
    }


    private List<AmTagItem> getChildMetaDataItems(folder) {
        def amTagTemplate = amTagTemplateService.getTemplate(folder.getUniqueId())
        List<AmTagItem> metaDataTagItems
        if (amTagTemplate) {
            metaDataTagItems = amTagItemService.getChildDisplayItems(amTagTemplate.id)
        } else {
            log.error "Unable to find child amTagTemplate for object Id = " + folder.getUniqueId()
        }

        return metaDataTagItems
    }

    private List<AmTagItem> getMetaDataItems(folder, editable) {
        def amTagTemplate = amTagTemplateService.getTemplate(folder.getUniqueId())
        List<AmTagItem> metaDataTagItems
        if (amTagTemplate) {
            if (editable) {
                metaDataTagItems = amTagItemService.getEditableItems(amTagTemplate.id)
            } else {
                metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
            }
        } else {
            log.error "Unable to find amTagTemplate for object Id = " + folder.getUniqueId()
        }

        return metaDataTagItems
    }

    private Object getBioDataObject(folder) {
        def bioDataObject
        log.info "getBioDataObject::folder = " + folder

        def folderAssociation = FmFolderAssociation.findByFmFolder(folder)

        if (folderAssociation) {
            log.info "getBioDataObject::folderAssociation = " + folderAssociation
            bioDataObject = folderAssociation.getBioObject()
        } else {
            log.error "Unable to find folderAssociation for folder Id = " + folder.id
        }

        if (!bioDataObject) {
            log.info "Unable to find bio data object. Setting folder to the biodata object "
            bioDataObject = folder
        }

        return bioDataObject
    }

    def editMetaData =
        {
            log.info "editMetaData called"
            log.info "params = " + params


            if (!isAdmin()) {
                return
            };

            //log.info "** action: expDetail called!"
            def folderId = params.folderId

            def folder
            def bioDataObject
            def amTagTemplate
            def metaDataTagItems
            if (folderId) {
                folder = FmFolder.get(folderId)
                if (folder) {
                    bioDataObject = getBioDataObject(folder)
                    metaDataTagItems = getMetaDataItems(folder, true)
                } else {
                    log.error "Unable to find folder for folder Id = " + folderId
                }

            }

            def title = "Edit Meta Data"
            def templateType = "editMetadataForm"

            def measurements = BioAssayPlatform.executeQuery("SELECT DISTINCT platformType FROM BioAssayPlatform as p ORDER BY p.platformType")
            def vendors = BioAssayPlatform.executeQuery("SELECT DISTINCT vendor FROM BioAssayPlatform as p ORDER BY p.vendor")
            def technologies = BioAssayPlatform.executeQuery("SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ORDER BY p.platformTechnology")
            def platforms = BioAssayPlatform.executeQuery("FROM BioAssayPlatform as p ORDER BY p.name")

            render(template: "editMetaData", model: [bioDataObject: bioDataObject, measurements: measurements, technologies: technologies, vendors: vendors, platforms: platforms, folder: folder, amTagTemplate: amTagTemplate, metaDataTagItems: metaDataTagItems]);
        }

    def updateMetaData = {
        log.info "updateMetaData called"
        log.info params

        if (!isAdmin()) {
            return;
        }

        try {
            FmFolder folder = FmFolder.get(params.id)

            // Get associated business object and deal with any special folderName/description inconsistencies.
            Object object
            FmFolderAssociation assoc = FmFolderAssociation.findByFmFolder(folder)
            folder.folderName = params.folderName
            folder.description = params.description
            if (assoc != null) {
                object = assoc.getBioObject()
                if (object instanceof Experiment) {
                    object = (Experiment) object;
                    folder.folderName = params.title
                } else if (object instanceof BioAssayAnalysis) {
                    folder.folderName = params.name
                    folder.description = params.longDescription
                }
            } else {
                object = folder
            }

            fmFolderService.saveFolder(folder, object, params)
            def result = [id: folder.id, folderName: folder.folderName]
            render result as JSON

            solrFacetService.reindexFolder(folder.getUniqueId())
        } catch (ValidationException ex) {
            log.error "Unable to update metadata"
            def errors = g.renderErrors(bean: ex.errors)
            log.error errors
            def result = [errors: errors]
            render result as JSON
        } catch (Exception ex) {
            log.error "Exception in FmFolderController.updateMetaData", ex
            def result = [errors: "<ul><li>An unexpected error has occurred. If this error persits, please click \"Close\" or \"Cancel\" to close this dialog box.<br><br>Error details: " + ex.getMessage() + "</li></ul>"]
            render result as JSON
        }

    }

    def subFolders =
        {
            ExportTableNew table;

            //Keep this if you want to cache the grid data
            //ExportTableNew table=(ExportTableNew)request.getSession().getAttribute("gridtable");

            if (table == null) {
                table = new ExportTableNew();
            }

            table.putColumn("ident", new ExportColumn("ident", "ID", "", "String", 50));
            table.putColumn("name", new ExportColumn("name", "Name", "", "String", 50));
            table.putColumn("description", new ExportColumn("description", "Description", "", "String", 50));

            ExportRowNew newrow = new ExportRowNew();
            newrow.put("ident", "foo.id");
            newrow.put("name", "foo.name");
            newrow.put("description", "foo.description");
            table.putRow("somerow", newrow);

            def jSONToReturn = table.toJSON_DataTables("").toString(5);

            request.getSession().setAttribute("gridtable", table);

            [jSONForGrid: jSONToReturn]

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
        render(contentType: "text/plain", text: "Reindexing...")
        fmFolderService.reindexFiles();
        render(contentType: "text/plain", text: "...complete!")
    }

    def reindexFolder = {
        solrFacetService.reindexFolder(params.uid)
    }

    def removeEntry = {
        fmFolderService.removeSolrEntry(params.uid)
    }

    def deleteFolder = {

        if (!isAdmin()) {
            return
        };

        def id = params.id
        def folder = FmFolder.get(id)
        if (folder) {
            fmFolderService.deleteFolder(folder)
            render(template: 'deletedFolder')
        } else {
            render(status: 500, text: "FmFolder not found")
        }
    }

    def deleteFile = {

        if (!isAdmin()) {
            return
        };

        def id = params.id
        def file = FmFile.get(id)
        def folder = file.getFolder()
        if (file) {
            fmFolderService.deleteFile(file)
            render(template: 'filesTable', model: [folder: folder, hlFileIds: []])
        } else {
            render(status: 404, text: "FmFile not found")
        }

    }

    def downloadFile() {
        FmFile fmFile = FmFile.get params.id
        if (!fmFile) {
            render status: 404, text: 'FmFile not found'
            return
        }

        String mimeType = MIME_TYPES_FILES_MAP.getContentType fmFile.originalName
        log.debug "Downloading file $fmFile, mime type $mimeType"

        response.setContentType mimeType

        /* This form of sending the filename seems to be compatible
         * with all major browsers, except for IE8. See:
         * http://greenbytes.de/tech/tc2231/#attwithfn2231utf8comp
         */
        response.addHeader 'Content-Disposition',
                "attachment; filename*=UTF-8''" +
                        fmFile.originalName.getBytes('UTF-8').collect {
                            '%' + String.format('%02x', it)
                        }.join('')
        response.addHeader 'Content-length', fmFile.fileSize.toString()
        def file = fmFolderService.getFile fmFile
        file.newInputStream().withStream {
            response.outputStream << it
        }
    }

    def ajaxTechnologies =
        {
            def queryString = " where 1=1"
            if (params.measurementName != null && params.measurementName != 'null') {
                queryString += " and platformType = '" + params.measurementName + "'"
            }
            if (params.vendorName != null && params.vendorName != 'null') {
                queryString += " and vendor = '" + params.vendorName + "'"
            }

            queryString = "SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p " + queryString + "  ORDER BY p.platformTechnology"
            def technologies = BioAssayPlatform.executeQuery(queryString)
            log.info queryString + " " + technologies
            render(template: "selectTechnologies", model: [technologies: technologies, technology: params.technologyName])
        }

    def ajaxVendors =
        {
            log.info params
            def queryString = " where 1=1"

            if (params.technologyName != null && params.technologyName != 'null') {
                queryString += " and platformTechnology = '" + params.technologyName + "'"
            }

            if (params.measurementName != null && params.measurementName != 'null') {
                queryString += " and platformType = '" + params.measurementName + "'"
            }

            queryString = "SELECT DISTINCT vendor FROM BioAssayPlatform as p " + queryString + "  ORDER BY p.vendor"
            def vendors = BioAssayPlatform.executeQuery(queryString)
            log.info queryString + " " + vendors
            render(template: "selectVendors", model: [vendors: vendors, vendor: params.vendorName])
        }

    def ajaxMeasurements =
        {
            log.info params
            def queryString = " where 1=1"

            if (params.technologyName != null && params.technologyName != 'null') {
                queryString += " and platformTechnology = '" + params.technologyName + "'"
            }

            if (params.vendorName != null && params.vendorName != 'null') {
                queryString += " and vendor = '" + params.vendorName + "'"
            }

            queryString = "SELECT DISTINCT platformType FROM BioAssayPlatform as p " + queryString + "  ORDER BY p.platformType"
            def measurements = BioAssayPlatform.executeQuery(queryString)
            log.info queryString + " " + measurements
            render(template: "selectMeasurements", model: [measurements: measurements, measurement: params.measurementName])
        }

    def ajaxPlatforms =
        {
            log.info params
            def queryString = " where 1=1"

            if (params.measurementName != null && params.measurementName != 'null') {
                queryString += " and platformType = '" + params.measurementName + "'"
            }

            if (params.technologyName != null && params.technologyName != 'null') {
                queryString += " and platformTechnology = '" + params.technologyName + "'"
            }

            if (params.vendorName != null && params.vendorName != 'null') {
                queryString += " and vendor = '" + params.vendorName + "'"
            }

            queryString = "FROM BioAssayPlatform as p " + queryString + "  ORDER BY p.platformType"
            def platforms = BioAssayPlatform.executeQuery(queryString)
            log.info queryString + " " + platforms
            render(template: "selectPlatforms", model: [platforms: platforms])
        }

    private boolean isAdmin() {
        if ("anonymousUser" != springSecurityService.getPrincipal()) {
            def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
            if (!user.isAdmin()) {
                render(status: 200, text: "You do not have permission to edit this object's metadata.")
                return false
            }
        } else {
            render(status: 200, text: "You do not have permission to edit this object's metadata.")
            return false
        }
        return true
    }


}


