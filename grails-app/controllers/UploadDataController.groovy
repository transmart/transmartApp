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
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");

	def index =
	{
		
		//Create an event record for this access.
		def al = new AccessLog(username: springSecurityService.getPrincipal().username, event:"UploadData-Index", eventmessage:"Upload Data index page", accesstime:new Date())
		al.save();
		
		def model = [uploadDataInstance: new AnalysisMetadata()]
		addFieldData(model, null)
		//TODO Retrieve the lists needed for selects
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
		
		//Handle special cases where comma-separated lists must be saved
		
		if (params.tags) {
			if (params.tags instanceof String) {
				upload.phenotypeIds = params.tags
			}
			else {
				upload.phenotypeIds = params.tags.join(",")
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
				upload.genotypePlatformIds = params.genotypePlatform.join(",")
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
				upload.expressionPlatformIds = params.expressionPlatform.join(",")
			}
		}
		else {
			upload.expressionPlatformIds = "";
		}
		
		
		String uploadsDir = grailsApplication.config.com.recomdata.dataUpload.uploads.dir;
		def f = request.getFile('file');
		upload.etlDate = new Date()
		def filename = sdf.format(upload.etlDate) + f.getOriginalFilename()
		upload.filename = filename
		
		if (upload.save(flush: true)) {
			
			//Save the uploaded file
			
			OutputStream out = null;
			try {
				out = new FileOutputStream(uploadsDir + "/" + filename)
				out.write(f.getBytes())
			}
			catch (Exception e) {
				upload.status = "ERROR"
				upload.save(flush: true)
				render(view: "complete", model: [success:false, error: "Could not write file: " + e.getMessage(), uploadDataInstance: upload]);
				return;
			}
			finally {
				if (out != null) {
					out.close();
				}
			}
			
			//Read the first line and flag this metadata with an error immediately if missing required fields
			
			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(f));
				String header = br.readLine();
				
			}
			catch (Exception e) {
				upload.status = "ERROR"
				upload.save(flush: true)
				render(view: "complete", model: [success:false, error: "Could not read file: " + e.getMessage(), uploadDataInstance: upload]);
				return;
			}
			finally {
				br.close();
			}
			
			//If we've reached here, everything is OK - set our state to PENDING
			
			upload.status = "PENDING"
			upload.save(flush: true)
			render(view: "complete", model: [success:true, uploadDataInstance: upload]);
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
				for (tag in upload.phenotypeIds.split(",")) {
					def disease = Disease.findByMeshCode(tag)
					tagMap.put(tag, disease.disease)
				}
			}
			
			//Platform ID display and ID are both codes
			if (upload.genotypePlatformIds) {
				for (tag in upload.genotypePlatformIds.split(",")) {
					def platform = BioAssayPlatform.findByName(tag)
					genotypeMap.put(tag, platform.vendor + ": " + tag)
				}
			}
			
			if (upload.expressionPlatformIds) {
				for (tag in upload.expressionPlatformIds.split(",")) {
					def platform = BioAssayPlatform.findByName(tag)
					expressionMap.put(tag, platform.vendor + ": " + tag)
				}
			}
			
			
			model.put('tags', tagMap)
			model.put('genotypePlatforms', genotypeMap)
			model.put('expressionPlatforms', expressionMap)
			
			model.put('study', Experiment.get(upload.study?.id))
		}
		
		//Vendor names can be null - avoid adding these
		def vendorlist = []
		def vendors = BioAssayPlatform.executeQuery("SELECT DISTINCT vendor FROM BioAssayPlatform p ORDER BY vendor")
		for (vendor in vendors) {
			if (vendor) {
				vendorlist.add(vendor);
			}
		}
		model.put('vendors', vendorlist)
	}
	
}
