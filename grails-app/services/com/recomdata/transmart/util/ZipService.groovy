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
  

package com.recomdata.transmart.util

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.lang.StringUtils;

class ZipService {

    boolean transactional = true

	private static final int BUFFER_SIZE = 250 * 1024;
	
	/**
	 * This method will bundle all the files into a zip file. 
	 * If there are 2 files with the same name, only the first file is part of the zip.
	 * 
	 * @param zipFileName
	 * @param files
	 * 
	 * @return zipFile absolute path
	 * 
	 */
	public static String bundleZipFile(String zipFileName, List<File> files) {
		File zipFile = null;
		Map<String, File> filesMap = new HashMap<String, File>();
		
		if (StringUtils.isEmpty(zipFileName)) return null;
		
		try {
    		zipFile = new File(zipFileName);
    		if (zipFile.exists() && zipFile.isFile() && zipFile.delete()) {
    			zipFile = new File(zipFileName);
    		}
        	
            ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
            zipOut.setLevel(ZipOutputStream.DEFLATED);
            byte[] buffer = new byte[BUFFER_SIZE];
        
            for (File file : files)
            {
            	if (filesMap.containsKey(file.getName())) {
            		continue;
            	} else if (file.exists() && file.canRead()) {
            		filesMap.put(file.getName(), file);
            		zipOut.putNextEntry(new ZipEntry(file.getName()));
                    FileInputStream fis = new FileInputStream(file);
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                    	zipOut.write(buffer, 0, bytesRead);
                    }
                    zipOut.flush();
                    zipOut.closeEntry();
            	}
            }
            zipOut.finish();
            zipOut.close();
        }
        catch (IOException e)
        {
        	//log.error("Error while creating Zip file");
        }
		
		return (null != zipFile) ? zipFile.getAbsolutePath() : null;
	}
	
	/**
	 * This method will zip a given folder.
	 * 
	 * @param srcFolder
	 * @param destZipFile
	 * @throws Exception
	 */
	static public String zipFolder(String srcFolder, String destZipFile) throws Exception {
		File zipFile = null;
	    ZipOutputStream zip = null;
	    FileOutputStream fileWriter = null;

	    zipFile = new File(destZipFile);
		if (zipFile.exists() && zipFile.isFile() && zipFile.delete()) {
			zipFile = new File(destZipFile);
		}
		
	    fileWriter = new FileOutputStream(zipFile);
	    zip = new ZipOutputStream(fileWriter);

	    addFolderToZip("", srcFolder, zip);
	    zip.flush();
	    zip.close();
	    
	    return zipFile.getName();
	  }

	  static private void addFileToZip(String path, String srcFile, ZipOutputStream zip)
	      throws Exception {

	    File folder = new File(srcFile);
	    if (folder.isDirectory()) {
	      addFolderToZip(path, srcFile, zip);
	    } else {
	      byte[] buf = new byte[BUFFER_SIZE];
	      int len;
	      FileInputStream inStream = new FileInputStream(srcFile);
	      zip.putNextEntry(new ZipEntry(path + "/" + folder.getName()));
	      while ((len = inStream.read(buf)) > 0) {
	        zip.write(buf, 0, len);
	      }
	    }
	  }

	  static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip)
	      throws Exception {
	    File folder = new File(srcFolder);

	    for (String fileName : folder.list()) {
	      if (path.equals("")) {
	        addFileToZip(folder.getName(), srcFolder + "/" + fileName, zip);
	      } else {
	        addFileToZip(path + "/" + folder.getName(), srcFolder + "/" + fileName, zip);
	      }
	    }
	  }
	  
	  public static void main(String[] a) throws Exception {
		    zipFolder("C:/Users/smunikuntla/AppData/Local/Temp/jobName", "C:/Users/smunikuntla/AppData/Local/Temp/jobName.zip");
		  }
}
