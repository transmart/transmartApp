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
import fm.FmFolder;
import fm.FmFile;

import org.codehaus.groovy.grails.commons.ConfigurationHolder;

class FmFolderService {

	boolean transactional = true;
	def config = ConfigurationHolder.config;
	String dropfolderPath = config.com.recomdata.foldermanager.dropfolder.toString();
	String filestorePath = config.com.recomdata.foldermanager.filestore.toString();
	
	/**
	 * Checks dropfolderPath for new files and processes them into filestore.
	 *
	 * @return
	 */
	def checkForNewFiles() {

		if (dropfolderPath == null) {
			log.error("Unable to check for new files. com.recomdata.foldermanager.dropfolder property has not been defined in the Config.groovy file.");
		} else if (filestorePath == null) {
			log.error("Unable to check for new files. com.recomdata.foldermanager.filestore property has not been defined in the Config.groovy file.");
		} else {
			processDirectory(new File(dropfolderPath), 0);
		}
		
	}	
	
	/**
	 * Process files and sub-directories in specified directory.
	 *
	 * @param directory
	 * @param level
	 * @return
	 */
	def processDirectory(File directory, int level) {

		level++;
		for (File file : directory.listFiles()) {
			if (file.isDirectory()) {
				processDirectory(file, level);
			} else {
				processFile(file, level);
			}
		}
		
		// TODO: If directory is empty the delete it.

	}

	/**
	 * Processes a file into the filestore associating it with a folder.
	 *
	 * @param file
	 * @return
	 */
	def processFile(File file, int level) {
	
		File directory = file.getParentFile();
		long folderId = Long.parseLong(directory.getName(), 36);
		def fmFolder = FmFolder.get(folderId);
		
		if (fmFolder == null) {
			log.error("Folder with id " + folderId + " does not exist.")
			return;
		}
		log.info("Folder = " + fmFolder.folderName + " (" + folderId + ")");

		// TODO: Check if duplicate and add new version of file

		def fmFile = new FmFile(
			displayName: file.getName(),
			originalName: file.getName(),
			fileType: getFileType(file),
			fileSize: file.length(),
			filestoreLocation: "",
			filestoreName: "",
			linkUrl: ""
		);

		if (!fmFile.save()) {
			fmFile.errors.each {
				log.error(it);
			}
		}

		fmFile.filestoreLocation = getFilestoreLocation(fmFolder);
		fmFile.filestoreName = Long.toString(fmFile.id, 36).toUpperCase() + "-" + Long.toString(fmFile.fileVersion, 36).toUpperCase() + "." + fmFile.fileType;

		if (!fmFile.save()) {
			fmFile.errors.each {
				log.error(it);
			}
		 }

		 fmFolder.addToFmFiles(fmFile);
		 if (!fmFolder.save()) {
			 fmFolder.errors.each {
				 log.error(it);
			 }
		 }

		log.info("File = " + file.getName() + " (" + fmFile.id + ")");

		File filestoreDirectory = new File(fmFile.filestoreLocation);
		if (!filestoreDirectory.exists()) {
			if (!filestoreDirectory.mkdirs()) {
				log.error("unable to create filestore " + filestoreDirectory.getPath());
				return;
			}
		}
		File filestoreFile = new File(fmFile.filestoreLocation + file.separator + fmFile.filestoreName);
		if (!file.renameTo(filestoreFile)) {
			log.error("unable to move file to " + filestoreFile.getPath());
			return;
		}

		log.info("Moved file to " + filestoreFile.getPath());
		
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
	 * grouped by their parent study folder tags. If the files are being loaded at
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
			filestoreLocation = fmFolder.folderTag;
		} else {
			log.info("folderFullName = " + fmFolder.folderFullName);
			int pos = fmFolder.folderFullName.indexOf("\\", 1);
			log.info("pos 1 = " + pos);
			pos = fmFolder.folderFullName.indexOf("\\", pos + 1);
			log.info("pos 2 = " + pos);
			log.info("find name = " + fmFolder.folderFullName.substring(0, pos));
			FmFolder fmParentFolder = FmFolder.findByFolderFullName(fmFolder.folderFullName.substring(0, pos));	
			if (fmParentFolder == null) {
				log.error("Unable to find folder with folderFullName of " + fmFolder.folderFullName.substring(0, pos));
				filestoreLocation = "0";
			} else {
				filestoreLocation = fmParentFolder.folderTag;
			}
		}

		return filestorePath.replace("/", File.separator) + File.separator + "fs-" + filestoreLocation;

	}

//	def createFolder(String folderFullName) {
//
//		String[] types = { "Program", "Project", "Assay", "Data" };
//		String[] names = folderFullName.split("\\");
//		String currentFullName = "\\";
//		FmFolder fmFolder;
//
//		for (int level = 0; level < names.length; level++) {
//			currentFullName = currentFullName + names[i] + "\\";
//			fmFolder = FmFolder.findByFolderFullName(currentFullName);
//			if (fmFolder == null) {
//				fmFolder = new FmFolder(
//					folderName: names[level],
//					folderFullName: currentFullName,
//					folderLevel: level,
//					folderType: type[level],
//					activeInd: true,
//					objectUid: currentFullName
//				);
//
//				if (!fmFolder.save(flush:true)) {
//					fmFolder.errors.each {
//						log.error(it);
//					}
//				}
//
//				fmFolder.folderTag = String.format("%8s", Long.toString(fmFolder.id, 36).toUpperCase()).replace(' ', '0');
//				fmFolder.objectUid = String.format("%8s", Long.toString(fmFolder.id, 36).toUpperCase()).replace(' ', '0');
//				if (!fmFolder.save(flush:true)) {
//					fmFolder.errors.each {
//						log.error(it);
//					}
//				}
//			}
//
//			log.info("Folder = " + fmFolder.folderName + " (" + folderId + ")");
//		}
//	
//	}
	
	def getFolderContents(id, folderMap) {
		
				log.info "Getting folder contents for ID: " + id
				def parent;
				def folderLevel = 0L;
				if (id != null) {
					parent = FmFolder.get(id)
					folderLevel = parent.folderLevel + 1
				}
				
				
				def folderMask = folderMap.get(folderLevel);
				
				log.info "Searching at level: " + folderLevel + " with mask: " + folderMask + ", " + folderMask?.size()
				
				def folders = null;
				
				if (folderMask == null || folderMask.size() > 0) { //If we have an empty list, display no folders
					folders = FmFolder.createCriteria().list {
						if (parent != null) {
							eq('parent', parent)
						}
						if (folderMask) {
							'in'('id', folderMask)
						}
						eq('folderLevel', folderLevel)
						order('folderName', 'asc')
					}
				}
				
				log.info "Found folders: " + folders?.size();
				 
				return [folders: folders, files: parent?.fmFiles]
			}
	
}