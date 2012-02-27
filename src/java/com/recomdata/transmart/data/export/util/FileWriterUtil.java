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
/**
 * 
 */
package com.recomdata.transmart.data.export.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sun.rowset.CachedRowSetImpl;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * @author SMunikuntla
 * 
 */
public class FileWriterUtil {
	
	private File outputFile;
	private CSVWriter writer;

	private static org.apache.log4j.Logger log = Logger
			.getLogger(FileWriterUtil.class);

	/** Maximum loop count when creating temp directories. */
	private final int TEMP_DIR_ATTEMPTS = 10000;

	/**
	 * 
	 * Atomically creates a new directory somewhere beneath the system's
	 * temporary directory (as defined by the {@code java.io.tmpdir} system
	 * property), and returns its name.
	 * 
	 * <p>
	 * Use this method instead of {@link File#createTempFile(String, String)}
	 * when you wish to create a directory, not a regular file. A common pitfall
	 * is to call {@code createTempFile}, delete the file and create a directory
	 * in its place, but this leads a race condition which can be exploited to
	 * create security vulnerabilities, especially when executable files are to
	 * be written into the directory.
	 * 
	 * <p>
	 * This method assumes that the temporary volume is writable, has free
	 * inodes and free blocks, and that it will not be called thousands of times
	 * per second.
	 * 
	 * @return the newly-created directory
	 * @throws IllegalStateException
	 *             if the directory could not be created
	 */
	private File createDir(File baseDir, String name) {

		if (null == baseDir)
			baseDir = new File(System.getProperty("java.io.tmpdir"));

		for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
			File tempDir = new File(baseDir, name);
			if (tempDir.mkdir()) {
				return tempDir;
			} else if (tempDir.exists()){
				return tempDir;
			}
		}
		throw new IllegalStateException("Failed to create directory " + name
				+ " within " + TEMP_DIR_ATTEMPTS);
	}

	public FileWriterUtil(File studyDir, 
			String fileName, String jobId,
			String dataTypeName, String dataTypeFolder, char separator) throws Exception {
			
		File dataTypeNameDir = (StringUtils.isNotEmpty(dataTypeName) && null != studyDir) ? createDir(
				studyDir, dataTypeName) : null;
		File dataTypeFolderDir = (StringUtils.isNotEmpty(dataTypeFolder) && null != dataTypeNameDir) ? createDir(
				dataTypeNameDir, dataTypeFolder) : null;

		if (null != studyDir && null == dataTypeNameDir) {
			outputFile = new File(studyDir, fileName);
		} else if (null != studyDir && null != dataTypeNameDir) {
			outputFile =  new File((null == dataTypeFolderDir) ? dataTypeNameDir : dataTypeFolderDir, fileName);
		}
		BufferedWriter bufWriter = new BufferedWriter(new FileWriter(outputFile), 1024 * 64000);
		writer = new CSVWriter(bufWriter, separator);
	}
	/**
	 * @param fileName
	 * @param jobId
	 * @param dataTypeName
	 * @param dataTypeFolder
	 * @return outputFile
	 * @throws Exception
	 */
	public FileWriterUtil(String fileName, String jobId,
			String dataTypeName, String dataTypeFolder, char separator) throws Exception {
		File exportJobsDir = createDir(null, "jobs");
		File jobDir = (StringUtils.isNotEmpty(jobId)) ? createDir(exportJobsDir, jobId)
				: null;
		File dataTypeNameDir = (StringUtils.isNotEmpty(dataTypeName) && null != jobDir) ? createDir(
				jobDir, dataTypeName) : null;
		File dataTypeFolderDir = (StringUtils.isNotEmpty(dataTypeFolder) && null != dataTypeNameDir) ? createDir(
				dataTypeNameDir, dataTypeFolder) : null;

		if (null != fileName && !fileName.equalsIgnoreCase("")) {
			outputFile = (null != jobDir && null != dataTypeNameDir) ? new File(
					(null == dataTypeFolderDir) ? dataTypeNameDir
							: dataTypeFolderDir, fileName) : null;
	
			BufferedWriter bufWriter = new BufferedWriter(new FileWriter(outputFile), 1024 * 64000);
			writer = new CSVWriter(bufWriter, separator);
		}
		
	}

	private boolean validate(ResultSet resultSet,
			String[] headerValues) throws Exception {
		boolean valid = true;
		try {
			ResultSetMetaData rsmd = resultSet.getMetaData();
			if (null == resultSet
					|| (null != resultSet && rsmd.getColumnCount() <= 0)) {
				valid = false;
				log.error((null != outputFile) ? outputFile.getAbsolutePath()
						: "" + " :: Empty resultSet");
			}

			if (null == outputFile) {
				valid = false;
				log.error("Invalid outputFile");
			}
		} catch (SQLException e) {
			valid = false;
			log.error((null != outputFile) ? outputFile.getAbsolutePath() : ""
					+ " :: Empty resultSet");
		}

		return valid;
	}

	/**
	 * This method writes the data in resultSet object to outputFile. It also
	 * uses the headerValues and priorLines if any existing
	 * 
	 * @param cachedRowSet
	 * @param headerValues
	 * @param priorLines
	 */
	public void write(CachedRowSetImpl cachedRowSet,
			String[] headerValues, List<String[]> priorLines) {
		boolean writeColumnNames = true;
		try {
			if (validate(cachedRowSet, headerValues)) {
				if (null != priorLines)
					writer.writeAll(priorLines);

				if (null != headerValues) {
					writer.writeNext(headerValues);
					writeColumnNames = false;
				} 

				if (null != cachedRowSet)
					writer.writeAll(cachedRowSet, writeColumnNames);
			}
		} catch (IOException e) {
			log.error("Error creating file-writer");
		} catch (Exception e1) {
			log.error(e1.getMessage());
		} finally {
			finishWriting();
		}
	}
	
	/**
	 * @param lineValues a string array with each separator element as a separate
     *            entry.
	 */
	public void writeLine(String[] lineValues) {
		writer.writeNext(lineValues);
	}
	
	/**
	 * Closes the writer
	 */
	public void finishWriting() {
		try {
			if (null != writer) {
				writer.flush();
				writer.close();
			}
		} catch (IOException e) {
			log.error("Error closing file-writer");
		}
	}
}
