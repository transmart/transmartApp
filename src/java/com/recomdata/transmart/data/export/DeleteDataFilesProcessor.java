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
  

package com.recomdata.transmart.data.export;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.recomdata.transmart.data.export.util.FTPUtil;

public class DeleteDataFilesProcessor {
	private static org.apache.log4j.Logger log = Logger
			.getLogger(DeleteDataFilesProcessor.class);
	
	@SuppressWarnings("rawtypes")
	
	public boolean deleteDataFile(String fileToDelete, String directoryToDelete, String tempDir, String ftpServer, String ftpServerPort, String ftpServerUserName, String ftpServerPassword, String ftpServerRemotePath){
		boolean fileDeleted=false;
		try{
			if (StringUtils.isEmpty(fileToDelete)||StringUtils.isEmpty(directoryToDelete)){
				throw new Exception("Invalid file or directory name. Both are needed to delete data for an export job");
			}
			String dirPath = tempDir + File.separator + directoryToDelete;
			@SuppressWarnings("unused")
			boolean directoryDeleted = deleteDirectoryStructure(new File(dirPath));
			
			fileDeleted = FTPUtil.deleteFile(fileToDelete, ftpServer, ftpServerPort, ftpServerUserName, ftpServerPassword, ftpServerRemotePath);
			//If the file was not found at the FTP location try to delete it from the server Temp dir
			if (!fileDeleted) {
				String filePath = tempDir + File.separator + fileToDelete;
				File jobZipFile = new File(filePath);
				if (jobZipFile.isFile()) {
					jobZipFile.delete();
					fileDeleted=true;
				}
			}
		}catch(Exception e){
			log.error("Failed to delete the data for job "+directoryToDelete);
			log.error(e.getMessage());
		}
		return fileDeleted;
	}
	
	private boolean deleteDirectoryStructure(File directory){
		if (directory.exists()){
			File[] dirChildren = directory.listFiles();
			for(int i=0; i<dirChildren.length; i++){
				if (dirChildren[i].isDirectory()){
					deleteDirectoryStructure(dirChildren[i]);
				}else{
					dirChildren[i].delete();
				}
			}
		}
		return directory.delete();
	}

}
