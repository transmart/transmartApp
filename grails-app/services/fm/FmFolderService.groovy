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

import javax.tools.FileObject;
import java.io.File;
import java.net.URL;
import java.net.URLEncoder;
import fm.FmFolder;
import fm.FmFile;
import org.apache.solr.util.SimplePostTool;
import org.apache.commons.io.FileUtils;

import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import com.recomdata.util.FolderType;

class FmFolderService {

	boolean transactional = true;
	def config = ConfigurationHolder.config;
	String importDirectory = config.com.recomdata.FmFolderService.importDirectory.toString();
	String filestoreDirectory = config.com.recomdata.FmFolderService.filestoreDirectory.toString();
	String fileTypes = config.com.recomdata.FmFolderService.fileTypes.toString();;
	String solrUrl = config.com.recomdata.solr.baseURL.toString() + "/update";
	
	/**
	 * Imports files processing them into filestore and indexing them with SOLR.
	 *
	 * @return
	 */
	def importFiles() {
		
		log.info("importFiles called");
		if (importDirectory == null || filestoreDirectory == null) {
			if (importDirectory == null) {
				log.error("Unable to check for new files. config.com.recomdata.FmFolderService.importDirectory property has not been defined in the Config.groovy file.");
			}
			if (filestoreDirectory == null) {
				log.error("Unable to check for new files. config.com.recomdata.FmFolderService.filestoreDirectory property has not been defined in the Config.groovy file.");
			}
			return;
		}
		
		if (fileTypes == null) {
			fileTypes = "xml,json,csv,pdf,doc,docx,ppt,pptx,xls,xlsx,odt,odp,ods,ott,otp,ots,rtf,htm,html,txt,log";
		}
		
		processDirectory(new File(importDirectory));
		
	}	
	
	/**
	 * Re-index all files through SOLR.
	 * @return
	 */
	def reindexFiles() {
		
		log.info("reindexFiles called");
		def fmFiles = FmFile.findAll();
		for (fmFile in fmFiles) {
			indexFile(fmFile);
		}
		
	}
	
	/**
	 * Process files and sub-directories in specified directory.
	 *
	 * @param directory
	 * @return
	 */
	def processDirectory(File directory) {

//		log.info("FmFolderService.processDirectory(" + directory.getPath() + ")");
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				processDirectory(file);
			} else {
				processFile(file);
			}
		}
		
		// TODO: If directory is empty the delete it.

	}

	/**
	 * Processes a file into the filestore associating it with a folder and
	 * indexes file using SOLR
	 *
	 * @param file file to be proceessed
	 * @return
	 */
	def processFile(File file) {
	
//		log.info("FmFolderService.processFile(" + file.getPath() + ")");
		// Use file's parent directory as ID of folder which file will
		// be associated with.
		File directory = file.getParentFile();
		FmFolder fmFolder;
		try {
			long folderId = Long.parseLong(directory.getName());
			fmFolder = FmFolder.get(folderId);
			if (fmFolder == null) {
				log.error("Folder with id " + folderId + " does not exist.")
				return;
			}
		} catch (NumberFormatException ex) {
			return;
		}
		
		// Check if folder already contains file with same name.
		def fmFile;
		for (f in fmFolder.fmFiles) {
			if (f.originalName == file.getName()) {
				fmFile = f;
				break;
			}
		}
		// If it does, then use existing file record and increment its version.
		// Otherwise, create a new file.
		if (fmFile != null) {
			fmFile.fileVersion++;
			fmFile.fileSize = file.length();
			fmFile.linkUrl = "";
			log.info("File = " + file.getName() + " (" + fmFile.id + ") - Existing");
		} else {
			fmFile = new FmFile(
				displayName: file.getName(),
				originalName: file.getName(),
				fileType: getFileType(file),
				fileSize: file.length(),
				filestoreLocation: "",
				filestoreName: "",
				linkUrl: ""
			);
			if (!fmFile.save(flush:true)) {
				fmFile.errors.each {
					log.error(it);
				}
				return;
			}
			fmFile.filestoreLocation = getFilestoreLocation(fmFolder);
			fmFolder.addToFmFiles(fmFile);
			if (!fmFolder.save(flush:true)) {
				fmFolder.errors.each {
					log.error(it);
				}
				return;
			}
			log.info("File = " + file.getName() + " (" + fmFile.id + ") - New");
		}

		fmFile.filestoreName = fmFile.id + "-" + fmFile.fileVersion + "." + fmFile.fileType;

		if (!fmFile.save(flush:true)) {
			fmFile.errors.each {
				log.error(it);
			}
			return;
		 }

		// Use filestore directory based on file's parent study or common directory
		// for files in folders above studies. If directory does not exist, then create it.
		// PREREQUISITE: Service account running tomcat has ownership of filestore directory.
		File filestoreDir = new File(filestoreDirectory + fmFile.filestoreLocation);
		if (!filestoreDir.exists()) {
			if (!filestoreDir.mkdirs()) {
				log.error("unable to create filestore " + filestoreDir.getPath());
				return;
			}
		}
		
		// Move file to appropriate filestore directory. 
		File filestoreFile = new File(filestoreDirectory + fmFile.filestoreLocation + file.separator + fmFile.filestoreName);
		try {
			FileUtils.copyFile(file, filestoreFile);
			if (!file.delete()) {
				log.error("unable to delete file " + file.getPath());
			}
			//if (!file.renameTo(filestoreFile)) {
		} catch (IOException ex) {
			log.error("unable to copy file to " + filestoreFile.getPath());
			return;
		}

		log.info("Moved file to " + filestoreFile.getPath());
		
		// Call file indexer.
		indexFile(fmFile);
		
	}

	/**
	 * Gets type (extension) of specified file.
	 *
	 * @param file
	 * @return
	 */
	def getFileType(File file) {

		String fileType = "";
		int i = file.getName().lastIndexOf('.');
		if (i > -1) {
			fileType = file.getName().substring(i + 1);
		}
		
		return fileType;

	}

	/**
	 * Gets filestore location for specified folder. Files are stored in directories
	 * grouped by their parent study folder id. If the files are being loaded at
	 * the program level, then a default folder, "0" will be used.
	 *
	 * @param folder
	 * @return
	 */
	def getFilestoreLocation(FmFolder fmFolder) {

		String filestoreLocation;
		
		if (fmFolder.folderLevel == 0) {
			filestoreLocation = "0";
		} else if (fmFolder.folderLevel == 1) {
			filestoreLocation = fmFolder.id;
		} else {
//			log.info("folderFullName = " + fmFolder.folderFullName);
			int pos = fmFolder.folderFullName.indexOf("\\", 1);
			pos = fmFolder.folderFullName.indexOf("\\", pos + 1);
//			log.info("find name = " + fmFolder.folderFullName.substring(0, pos));
			FmFolder fmParentFolder = FmFolder.findByFolderFullName(fmFolder.folderFullName.substring(0, pos + 1));	
			if (fmParentFolder == null) {
				log.error("Unable to find folder with folderFullName of " + fmFolder.folderFullName.substring(0, pos + 1));
				filestoreLocation = "0";
			} else {
				filestoreLocation = fmParentFolder.id;
			}
		}

		return File.separator + filestoreLocation;

	}
	
	/**
	 * Indexes file using SOLR.
	 * @param fileId ID of file to be indexed
	 * @return
	 */
	def indexFile(String fileId) {
		
		FmFile fmFile = FmFile.get(fileId);
		if (fmFile == null) {
			log.error("Unable to locate fmFile with id of " + fileId);
			return;
		}
		indexFile(fmFile);
				
	}

	/**
	 * Indexes file using SOLR.
	 * @param fmFile file to be indexed
	 * @return
	 */
	def indexFile(FmFile fmFile) {
		
		try {
			StringBuilder url = new StringBuilder(solrUrl);
			// Use the file's unique ID as the document ID in SOLR
			url.append("?").append("literal.id=").append(URLEncoder.encode(fmFile.uniqueId, "UTF-8"));
			
			
			// Use the file's parent folder's unique ID as the folder_uid in SOLR
			if (fmFile.folder != null) {
				url.append("&").append("literal.folder=").append(URLEncoder.encode(fmFile.folder.uniqueId, "UTF-8"));
			}
			
			// Use the file's name as document name is SOLR
			url.append("&").append("literal.name=").append(URLEncoder.encode(fmFile.originalName, "UTF-8"));
			
			// Get path to actual file in filestore.
			String[] args = [ filestoreDirectory + File.separator + fmFile.filestoreLocation + File.separator + fmFile.filestoreName ] as String[];
			
			// Use SOLR SimplePostTool to manage call to SOLR service.
			SimplePostTool postTool = new SimplePostTool(SimplePostTool.DATA_MODE_FILES, new URL(url.toString()), true,
				null, 0, 0, fileTypes, System.out, true, true, args);
			
			postTool.execute();
		} catch (Exception ex) {
			log.error("Exception while indexing fmFile with id of " + fmFile.id, ex);
		}
		
	}
	
	def getFolderContents(id) {
		
		def parent;
		def folderLevel = 0L;
		if (id != null) {
			parent = FmFolder.get(id)
			folderLevel = parent.folderLevel + 1
		}
		
		def folders = null;
		
		folders = FmFolder.createCriteria().list {
			if (parent != null) {
				eq('parent', parent)
			}
			eq('folderLevel', folderLevel)
			order('folderName', 'asc')
		}
		 
		return folders
	}
	
	def getAssociatedAccession(fmFolder) {
		//Walk up the tree to find the study accession for this folder
		if (!fmFolder) {
			return null
		}
		
		if (fmFolder.folderType.equals(FolderType.STUDY.name())) {
			def experiment = FmFolderAssociation.findByFmFolder(fmFolder)?.getBioObject()
			log.error("No experiment associated with study folder: " + fmFolder.folderFullName)
			return experiment?.accession
		}
		else {
			getAssociatedAccession(fmFolder.parent)
		}
	}
	
}