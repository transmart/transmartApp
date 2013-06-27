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
  

package transmartapp

import fm.FmFile
import fm.FmFolder;
import fm.FmFolderAssociation;
import fm.FmFolderService

import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream

import search.SearchKeyword;

import bio.BioAssayPlatform;
import bio.BioData;
import bio.BioMarker;
import bio.Compound;
import bio.ConceptCode;
import bio.Disease;

import annotation.AmTagTemplate;
import annotation.AmTagValue;
import annotation.AmTagAssociation;


class FileExportController {
	
	def fmFolderService
	def amTagItemService

    def add = {
		def paramMap = params
		def idList = params.id.split(',')
		
		def exportList = session['export']
		
		if (exportList == null) {
			exportList = [];
		}
		for (id in idList) {
			if (id && !exportList.contains(id)) {
				exportList.push(id)
			}
		}
		session['export'] = exportList;
		
		//Render back the number to display
		render (status: 200, text: exportList.size())
	}
	
	def remove = {
		def idList = params.list('id')
		
		def exportList = session['export']
		
		if (exportList == null) {
			exportList = [];
		}
		for (id in idList) {
			if (id && exportList.contains(id)) {
				exportList.remove(id)
			}
		}
		session['export'] = exportList;
		
		//Render back the number to display
		render (status: 200, text: exportList.size())
	}
	
	def view = {
		
		def exportList = session['export']
		def files = []
		for (id in exportList) {
			FmFile f = FmFile.get(id)
			if (f) {
				files.push([id: f.id, fileType: f.fileType, displayName: f.displayName, folder: fmFolderService.getPath(f.folder)])
			}
		}
		files.sort { a, b ->
			if (!a.folder.equals(b.folder)) {
				return a.folder.compareTo(b.folder)
			}
			return a.displayName.compareTo(b.displayName)
		}
		
		render(template: '/RWG/export', model: [files: files])
	}
	
	def export = {
		
		def errorResponse = []
		def filestorePath = grailsApplication.config.com.recomdata.FmFolderService.filestoreDirectory
		
		def exportList
		def metadataExported=new HashSet();
		try {
			
			//Final export list comes from selected checkboxes
			exportList = params.id.split(",")
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream()
			def zipStream = new ZipOutputStream(baos)
			
			def manifestMap = [:]
			
			for (f in exportList) {
				FmFile fmFile = FmFile.get(f)
				def fileLocation = filestorePath + "/" + fmFile.filestoreLocation + "/" + fmFile.filestoreName
				File file = new File(fileLocation)
				//if (file.exists()) {
				if(1){
					/*String dirName = fmFolderService.getPath(fmFile.folder, true)
					if (dirName.startsWith("/") || dirName.startsWith("\\")) { dirName = dirName.substring(1) } //Lose the first separator character, this would cause a blank folder name in the zip
					def fileEntry = new ZipEntry(dirName + "/" + fmFolderService.safeFileName(fmFile.displayName))
					zipStream.putNextEntry(fileEntry)
					file.withInputStream({is -> zipStream << is})
					zipStream.closeEntry()
					
					//For manifest files, add this file to a map, keyed by folder names.
					def manifestList = []
					if (manifestMap.containsKey(dirName)) {
						manifestList = manifestMap.get(dirName)
					}
					
					manifestList.push(fmFile)
					manifestMap.put(dirName, manifestList)*/
					
					//for each folder of the hieararchy of the file path, add file with metadata
					def path=fmFile.folder.folderFullName
					for(folderId in path.split("\\\\", -1)){
						if(!folderId.equals("")){
							if(metadataExported.add(folderId)) exportMetadata(folderId.split(":", 2)[1], zipStream);
						}
					}
				}
				else {
					def errorMessage = "File not found for export: " + fileLocation
					log.error errorMessage
					errorResponse += errorMessage
				}
			}
			
			//Now for each item in the manifest map, create a manifest file and add it to the ZIP.
			def keyset = manifestMap.keySet()
			for (key in keyset) {
				def manifestEntry = new ZipEntry(key + "/" + "manifest.txt")
				zipStream.putNextEntry(manifestEntry)
				def manifestList = manifestMap.get(key)					
				for (fmFile in manifestList) {
					zipStream.write((fmFile.displayName + "\t" + fmFile.fileType + "\t" + fmFile.fileSize + "\n").getBytes())
				}
				zipStream.closeEntry()
			}
			
			zipStream.flush();
			zipStream.close();
			
			response.setHeader('Content-disposition', 'attachment; filename=export.zip')
			response.contentType = 'application/zip'
			response.outputStream << baos.toByteArray()
			response.outputStream.flush()
		}
		catch (Exception e) {
			e.printStackTrace()
			log.error("Error writing ZIP", e)
			render(contentType: "text/plain", text: errorResponse.join("\n") + "\nError writing ZIP: " + e.getMessage())
		}catch(OutOfMemoryError oe){
			log.error("Files too large to be exported: "+exportList)
			render(contentType: "text/plain", text:"Error: Files too large to be exported.\nPlease click on the \"Previous\" button on your web browser to go back to tranSMART.")
		}
	}
	
	//add in a zip a file containing metadata for a given folder
	private void exportMetadata(String folderId, ZipOutputStream zipStream){
		try{
			def folder=FmFolder.get(folderId)
			
			String dirName = fmFolderService.getPath(folder, true)
			if (dirName.startsWith("/") || dirName.startsWith("\\")) { dirName = dirName.substring(1) } //Lose the first separator character, this would cause a blank folder name in the zip
			def fileEntry = new ZipEntry(dirName + "/"+ folder.folderName+ "_metadata.txt")
			zipStream.putNextEntry(fileEntry)
					
			def amTagTemplate = AmTagTemplate.findByTagTemplateType(folder.folderType)
			def metaDataTagItems = amTagItemService.getDisplayItems(amTagTemplate.id)
			
			zipStream.write((folder.folderType+": "+folder.folderName+"\n").getBytes())
			zipStream.write(("Description: "+folder.description+"\n").getBytes())
			
			//get associated bioDataObject
			def bioDataObject
			def folderAssociation = FmFolderAssociation.findByFmFolder(folder)
			if(folderAssociation)
			{
				bioDataObject = folderAssociation.getBioObject()
			}
			if(!bioDataObject)
			{
				bioDataObject = folder
			}
			
			for(amTagItem in metaDataTagItems){
				if(amTagItem.tagItemType == 'FIXED'){
					if(amTagItem.tagItemAttr!=null?bioDataObject?.hasProperty(amTagItem.tagItemAttr):false){
						def values=""
						def value=fieldValue(bean:bioDataObject, field: amTagItem.tagItemAttr)
						for(v in (value.split("\\|", -1))){
							def bioData=BioData.findByUniqueId(v)
							if(bioData!=null){
								def concept=ConceptCode.findById(bioData.id)
								if(concept!=null){
									if(values!="") values+="; "
									values+=concept.codeName
								}
							}
						}
						if(values.compareTo("")==0 && value!=null) values=value;
 						zipStream.write((amTagItem.displayName+": "+values+"\n").getBytes())
					}
				}else if(amTagItem.tagItemType == 'CUSTOM'){
					if(amTagItem.tagItemSubtype == 'FREETEXT'){
						def value=""
						def tagAssoc=AmTagAssociation.find("from AmTagAssociation where subjectUid=? and tagItemId=?",["FOL:"+folderId, amTagItem.id])
						if(tagAssoc!=null){
							if((tagAssoc.objectUid).split("TAG:", 2).size()>0){
								def tagValue=AmTagValue.findById((tagAssoc.objectUid).split("TAG:", 2)[1]);
								if(tagValue!=null) value=tagValue.value
							}
						}
						zipStream.write((amTagItem.displayName+": "+value+"\n").getBytes());
					}else if(amTagItem.tagItemSubtype == 'PICKLIST'){
						def value=""
						def tagAssoc=AmTagAssociation.find("from AmTagAssociation where subjectUid=? and tagItemId=?",["FOL:"+folderId, amTagItem.id])
						if(tagAssoc!=null){
							def valueUId=tagAssoc.objectUid
							def bioData=BioData.findByUniqueId(valueUId)
							if(bioData!=null){
								def concept=ConceptCode.findById(bioData.id)
								if(concept!=null){
									value=concept.codeName
								}
							}
						}
						zipStream.write((amTagItem.displayName+": "+value+"\n").getBytes());
					}else if(amTagItem.tagItemSubtype == 'MULTIPICKLIST'){
						def values=""
						def tagAssocs=AmTagAssociation.findAll("from AmTagAssociation where subjectUid=? and tagItemId=?",["FOL:"+folderId, amTagItem.id])
						for(tagAssoc in tagAssocs){
							def valueUId=tagAssoc.objectUid
							def bioData=BioData.findByUniqueId(valueUId)
							if(bioData!=null){
								def concept=ConceptCode.findById(bioData.id)
								if(concept!=null){
									if(values!="") values+="; "
									values+=concept.codeName
								}
							}
						}
						zipStream.write((amTagItem.displayName+": "+values+"\n").getBytes());
					}
				
				}else if(amTagItem.tagItemType == 'BIO_ASSAY_PLATFORM'){
					def values=""
					def tagAssocs=AmTagAssociation.findAll("from AmTagAssociation where subjectUid=? and objectType=?",["FOL:"+folderId, amTagItem.tagItemType])
					for(tagAssoc in tagAssocs){
						def tagValue=(tagAssoc.objectUid).split(":", 2)[1];
		                def bap=BioAssayPlatform.findByAccession(tagValue)
		                if(bap!=null){
		                  if(values!="") values+="; "
		                  values+=bap.platformType+"/"+bap.platformTechnology+"/"+bap.vendor+"/"+bap.name
		                }
					}
					zipStream.write((amTagItem.displayName+": "+values+"\n").getBytes());
                }else{//bio_disease, bio_coumpound...
					def values=""
					def tagAssocs=AmTagAssociation.findAll("from AmTagAssociation where subjectUid=? and objectType=?",["FOL:"+folderId, amTagItem.tagItemType])
					for(tagAssoc in tagAssocs){
						def key=SearchKeyword.findByUniqueId(tagAssoc.objectUid)
						if(key!=null){
							if(values!="") values+="; "
							values+=key.keyword
						}else{
							def bioData=BioData.findByUniqueId(tagAssoc.objectUid)
							if(bioData!=null){
								def concept=ConceptCode.findById(bioData.id)
								if(concept!=null){
									if(values!="") values+="; "
									values+=concept.codeName
								}
							}
						}
					}
					zipStream.write((amTagItem.displayName+": "+values+"\n").getBytes());
				}
			}
			zipStream.closeEntry()	
		}catch (Exception e) {
			log.error("Error writing ZIP", e)
		}
	}
}
