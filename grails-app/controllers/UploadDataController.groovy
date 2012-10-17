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
  

import java.text.SimpleDateFormat;

import bio.AnalysisMetadata;
import bio.BioAssayPlatform;
import bio.Disease;
import bio.Experiment;

import com.recomdata.upload.DataUploadResult;

import com.recomdata.snp.SnpData
import grails.converters.JSON


/**
 * Class for controlling the Upload Data page.
 * @author DNewton
 *
 */
class UploadDataController {

	//This server is used to access security objects.
	def springSecurityService
	def dataUploadService
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	def index =
	{
		
		//Create an event record for this access.
		def al = new AccessLog(username: springSecurityService.getPrincipal().username, event:"UploadData-Index", eventmessage:"Upload Data index page", accesstime:new Date())
		al.save();
		
		def model = [uploadDataInstance: new AnalysisMetadata()]
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
		
		addFieldData(model, uploadDataInstance);
		
		render(view: "uploadData", model:model)
	}
	
	def template = {
		String templatesDir = grailsApplication.config.com.recomdata.dataUpload.templates.dir;
		def type = params.type;
		
		if (!type) {
			render(status:500, text:"No template type given")
			return
		}
		
		def filename = type + "-template.csv"
		def templateFile = new File(templatesDir + "/" + filename)
		def template = templateFile.getBytes()
		response.setContentType("text/plain")
		response.setHeader("Content-Disposition", "attachment;filename=" + filename)
		response.setIntHeader('Content-length', template.length)
		response.outputStream << template
		response.outputStream.flush()
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
		
		if (upload.save(flush: true)) {
			
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
					result = new DataUploadResult(success:false, error: "Could not verify file: " + e.getMessage());
					render(view: "complete", model: [result: result, uploadDataInstance: upload]);
					return
				}
				
				//If we've reached here, everything is OK - set our state to PENDING to be picked up by ETL
				upload.status = "PENDING"
				upload.save(flush: true)
			}
			else {
				//This file was previously uploaded with an error - flag this!
				if (upload.status.equals("ERROR")) {
					result.error = "The existing file for this metadata failed to upload and needs to be replaced. Please upload a new file."
				}
			}
			
			result.success = upload.status.equals("PENDING");
			
			//If the file is now pending, start the staging process
			if (result.success) {
				dataUploadService.runStaging(upload.id);
			}
			render(view: "complete", model: [result: result, uploadDataInstance: upload]);
			return
		}
		else {
			flash.message = "The metadata could not be saved - please correct the highlighted errors."
			def model = [uploadDataInstance: upload]
			addFieldData(model, upload)
			render(view: "uploadData", model: model)
		}
	}
	
	private void addFieldData(model, upload) {
		def tagMap = [:]
		def genotypeMap = [:]
		def expressionMap = [:]
		
		if (upload) {
			if (upload.phenotypeIds) {
				for (tag in upload.phenotypeIds.split(";")) {
					def meshCode = tag.split(":")[1]
					def disease = Disease.findByMeshCode(meshCode)
					tagMap.put(tag, disease.disease)
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
			
			model.put('tags', tagMap)
			model.put('genotypePlatforms', genotypeMap)
			model.put('expressionPlatforms', expressionMap)
			
			model.put('study', Experiment.get(upload.study?.id))
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
	}
	
}
