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



import fm.FmFile
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.plugins.PluginManagerHolder

import java.text.SimpleDateFormat;

import org.transmart.biomart.AnalysisMetadata;
import org.transmart.biomart.BioAssayPlatform;
import org.transmart.biomart.Disease;
import org.transmart.biomart.Experiment;
import org.transmart.biomart.Observation;
import org.transmart.biomart.ConceptCode;

import com.recomdata.upload.DataUploadResult;

import com.recomdata.snp.SnpData
import grails.converters.JSON
import org.transmart.searchapp.AccessLog


/**
 * Class for controlling the Upload Data page.
 * @author DNewton
 *
 */
class UploadDataController {

    //This server is used to access security objects.
    def springSecurityService
    def dataUploadService
    def fmFolderService

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    def index =
            {

                //Create an event record for this access.
                def al = new AccessLog(username: springSecurityService.getPrincipal().username, event:"UploadData-Index", eventmessage:"Upload Data index page", accesstime:new Date())
                al.save();
                def uploadDataInstance = new AnalysisMetadata()
                def uploadFileInstance
                if (PluginManagerHolder.pluginManager.hasGrailsPlugin('folder-management')) {
                    uploadFileInstance = new FmFile()
                }
                def model = [uploadDataInstance: uploadDataInstance, uploadFileInstance: uploadFileInstance]

                addFieldData(model, null)
                render(view: "uploadData", model:model)
            }

    def edit =
            {
                def uploadDataInstance = null;
                if (params.id) {
                    uploadDataInstance = AnalysisMetadata.get(params.id);
                }
                if (!uploadDataInstance) {
                    uploadDataInstance = new AnalysisMetadata();
                }
                def model = [uploadDataInstance: uploadDataInstance];



                uploadDataInstance.setSensitiveFlag("0");
                addFieldData(model, uploadDataInstance)
                render(view: "uploadData", model:model)
            }

    def template = {
        String templatesDir = grailsApplication.config.com.recomdata.dataUpload.templates.dir;
        def type = params.type;

        if (!type) {
            render(status:500, text:"No template type given")
            return
        }

        def filename = type + "-template.txt"
        def templateFile = new File(templatesDir + "/" + filename)
        def template = templateFile.getBytes()
        response.setContentType("text/plain")
        response.setHeader("Content-Disposition", "attachment;filename=" + filename)
        response.setIntHeader('Content-length', template.length)
        response.outputStream << template
        response.outputStream.flush()
    }
    def skipp ={
        def paramMap=params
        def upload=null;
        def result = new DataUploadResult(success: true)
        if(params.id){
            upload=AnalysisMetadata.get(params.id)
        }
        else{
            upload=new AnalysisMetadata(params)
            result.success=false
            result.error="Could not find id for the analysis, something is wrong..."
            render(view: "complete",model: [result: result, uploadDataInstance: upload]);
        }
        bindData(upload, params)
        upload.status="PENDING";
        upload.save(flush: true)
        try {
            dataUploadService.runStaging(upload.id);
        }
        catch (Exception e) {
            log.error(e.getMessage(), e)
        }
        if (upload.hasErrors()) {
            flash.message = "The metadata could not be saved - please correct the highlighted errors."
            def errors = upload.errors
            def model = [uploadDataInstance: upload]
            addFieldData(model, upload)
            render(view: "uploadData", model: model)
        }
        else {
            render(view: "complete", model: [result: result, uploadDataInstance: upload]);
        }
    }

    def uploadFile = {
        def paramMap = params
        def f = request.getFile('uploadFile');
        def description = params.fileDescription
        def fileName = params.displayName
        def accession = params.study

        if (!fileName) {
            fileName = f.getOriginalFilename()
        }

        //Get the fmFolder associated with this study
        Experiment experiment = Experiment.findByAccession(accession)
        def folder = fmFolderService.getFolderByBioDataObject(experiment)
        def tempFile = new File(ConfigurationHolder.config.com.recomdata.FmFolderService.filestoreDirectory, f.getOriginalFilename())
        f.transferTo(tempFile)
        fmFolderService.processFile(tempFile, folder, fileName, description)
        tempFile.delete()
        render(view: "fileComplete");
    }

    def upload = {
        def paramMap = params
        def upload = null;
        if (params.id) {
            upload = AnalysisMetadata.get(params.id)
        }
        else {
            upload = new AnalysisMetadata(params)
        }
        bindData(upload, params)

        //Handle special cases where separated lists must be saved

        if (params.tags) {
            if (params.tags instanceof String) {
                upload.phenotypeIds = params.tags
            }
            else {
                upload.phenotypeIds = params.tags.join(";")
            }
        }
        else {
            upload.phenotypeIds = "";
        }

        if (params.genotypePlatform) {
            if (params.genotypePlatform instanceof String) {
                upload.genotypePlatformIds = params.genotypePlatform
            }
            else {
                upload.genotypePlatformIds = params.genotypePlatform.join(";")
            }
        }
        else {
            upload.genotypePlatformIds = "";
        }

        if (params.expressionPlatform) {
            if (params.expressionPlatform instanceof String) {
                upload.expressionPlatformIds = params.expressionPlatform
            }
            else {
                upload.expressionPlatformIds = params.expressionPlatform.join(";")
            }
        }
        else {
            upload.expressionPlatformIds = "";
        }

        if (params.researchUnit) {
            if (params.researchUnit instanceof String) {
                upload.researchUnit = params.researchUnit
            }
            else {
                upload.researchUnit = params.researchUnit.join(";")
            }
        }
        else {
            upload.researchUnit = "";
        }
        def f = null;
        def filename = null;
        def uploadsDir = null;
        f = request.getFile('file');

        if (f && !f.isEmpty()) {
            uploadsDir = grailsApplication.config.com.recomdata.dataUpload.uploads.dir;

            upload.etlDate = new Date()
            filename = sdf.format(upload.etlDate) + f.getOriginalFilename()
            upload.filename = uploadsDir + "/" + filename
        }

        //Save the uploaded file, if any
        def result = new DataUploadResult();

        if (f && !f.isEmpty()) {
            def fullpath = uploadsDir + "/" + filename
            try {
                result = dataUploadService.writeFile(fullpath, f, upload)
                if (!result.success) {
                    upload.status = "ERROR"
                    upload.save(flush: true)
                    render(view: "complete", model: [result: result, uploadDataInstance: upload])
                    return
                }
            }
            catch (Exception e) {
                upload.status = "ERROR"
                upload.save(flush: true)
                if(e.getMessage()!=null){
                    flash.message2=e.getMessage()+". If you wish to skip those SNPs, please click 'Continue'. If you wish to reload, click 'Cancel'."
                    def model = [uploadDataInstance: upload]
                    addFieldData(model, upload)
                    render(view: "uploadData", model: model)
                }
                else{
                    result = new DataUploadResult(success:false, error: "Could not verify file: unexpected exception occured." + e.getMessage());
                    render(view: "complete", model: [result: result, uploadDataInstance: upload]);
                }
                return
            }

            //If we've reached here, everything is OK - set our state to PENDING to be picked up by ETL
            upload.status = "PENDING"
        }
        else {
            //This file was previously uploaded with an error - flag this!
            if (upload.status.equals("ERROR")) {
                result.error = "The existing file for this metadata failed to upload and needs to be replaced. Please upload a new file."
            }
        }

        upload.save(flush: true)
        result.success = upload.status.equals("PENDING");

        //If the file is now pending, start the staging process
        if (result.success) {
            try {
                dataUploadService.runStaging(upload.id);
            }
            catch (Exception e) {
                log.error(e.getMessage(), e)
            }
        }

        if (upload.hasErrors()) {
            flash.message = "The metadata could not be saved - please correct the highlighted errors."
            def errors = upload.errors
            def model = [uploadDataInstance: upload]
            addFieldData(model, upload)
            render(view: "uploadData", model: model)
        }
        else {
            render(view: "complete", model: [result: result, uploadDataInstance: upload]);
        }
    }

    private void addFieldData(model, upload) {
        def tagMap = [:]
        def genotypeMap = [:]
        def expressionMap = [:]
        def researchUnitMap= [:]

        if (upload) {
            if (upload.phenotypeIds) {
                for (tag in upload.phenotypeIds.split(";")) {
                    def splitTag = tag.split(":")
                    def meshCode = tag
                    if (splitTag.length > 1) { meshCode = splitTag[1]}
                    def disease = Disease.findByMeshCode(meshCode)
                    def observation = Observation.findByCode(meshCode)
                    if (disease) {
                        tagMap.put(tag, [code: disease.meshCode, type: 'DISEASE'])
                    }
                    if (observation) {
                        tagMap.put(tag, [code: observation.name, type: 'OBSERVATION'])
                    }
                }
            }

            //Platform ID display and ID are both codes
            if (upload.genotypePlatformIds) {
                for (tag in upload.genotypePlatformIds.split(";")) {
                    def platform = BioAssayPlatform.findByAccession(tag)
                    genotypeMap.put(tag, platform.vendor + ": " + tag)
                }
            }

            if (upload.expressionPlatformIds) {
                for (tag in upload.expressionPlatformIds.split(";")) {
                    def platform = BioAssayPlatform.findByAccession(tag)
                    expressionMap.put(tag, platform.vendor + ": " + tag)
                }
            }

            if (upload.researchUnit) {
                for (tag in upload.researchUnit.split(";")) {
                    //def platform = BioAssayPlatform.findByAccession(tag)
                    researchUnitMap.put(tag, tag)
                }
            }

            model.put('tags', tagMap)
            model.put('genotypePlatforms', genotypeMap)
            model.put('expressionPlatforms', expressionMap)
            model.put('researchUnit', researchUnitMap)
            model.put('study', Experiment.findByAccession(upload.study))
        }

        //Vendor names can be null - avoid adding these
        def expVendorlist = []
        def snpVendorlist = []
        def expVendors = BioAssayPlatform.executeQuery("SELECT DISTINCT vendor FROM BioAssayPlatform as p WHERE p.platformType='Gene Expression' ORDER BY p.vendor")
        def snpVendors = BioAssayPlatform.executeQuery("SELECT DISTINCT vendor FROM BioAssayPlatform as p WHERE p.platformType='SNP' ORDER BY p.vendor")

        for (expVendor in expVendors) {
            //println(expVendor)
            if (expVendor) {
                expVendorlist.add(expVendor);
            }
        }
        for (snpVendor in snpVendors) {
            //println(snpVendor)
            if (snpVendor) {
                snpVendorlist.add(snpVendor);
            }
        }
        model.put('expVendors', expVendorlist)
        model.put('snpVendors', snpVendorlist)

        def ResearchUnitlist = []
        def ResearchUnits = ConceptCode.executeQuery("SELECT DISTINCT codeName FROM ConceptCode as p WHERE p.codeTypeName='RESEARCH_UNIT' ORDER BY p.codeName")

        for (ResearchUnit in ResearchUnits) {
            //println(ResearchUnit)
            if (ResearchUnit) {
                ResearchUnitlist.add(ResearchUnit);
            }
        }
        model.put('ResearchUnits', ResearchUnitlist)
    }


    def list = {
        def uploads = AnalysisMetadata.createCriteria().list {
            order('id', 'desc')
            maxResults(20)
        }

        render(view: "list", model: [uploads: uploads])
    }

    def studyHasFolder = {
        //Verify that a given study has a folder to upload to.
        //TODO This assumes folder-management
        def returnData = [:]
        Experiment experiment = Experiment.findByAccession(params.accession)
        if (!experiment) { returnData.message = "No experiment found with accession " + params.accession}
        def folder = fmFolderService.getFolderByBioDataObject(experiment)
        if (!folder) { returnData.message = "No folder association found for accession " + experiment.accession + ", unique ID " + experiment.uniqueId?.uniqueId}
        else {returnData.put('found', true)}
        render returnData as JSON
    }
}