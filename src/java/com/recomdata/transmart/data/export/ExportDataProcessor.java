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
package com.recomdata.transmart.data.export;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.groovy.grails.commons.ConfigurationHolder;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.recomdata.transmart.data.export.util.SftpClient;
import com.recomdata.transmart.data.export.util.ZipUtil;

/**
 * @author SMunikuntla
 * 
 */
public class ExportDataProcessor implements Job {

	private static org.apache.log4j.Logger log = Logger
			.getLogger(ExportDataProcessor.class);
	@SuppressWarnings("rawtypes")
	private static final Map config = ConfigurationHolder.getFlatConfig();

	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	private static final String SFTP_REMOTE_DIR_PATH = (String) config
			.get("com.recomdata.transmart.data.export.sftp.remote.dir.path");

	private static final String SFTP_SERVER = (String) config
			.get("com.recomdata.transmart.data.export.sftp.server");
	private static final String SFTP_SERVER_PORT = (String) config
			.get("com.recomdata.transmart.data.export.sftp.serverport");
	private static final String SFTP_USER_NAME = (String) config
			.get("com.recomdata.transmart.data.export.sftp.username");
	private static final String SFTP_PASSPHRASE = (String) config
			.get("com.recomdata.transmart.data.export.sftp.passphrase");
	private static final String SFTP_PRKEY_FILE = (String) config
			.get("com.recomdata.transmart.data.export.sftp.private.keyfile");

	public void init() {
	}

	public File getExportJobFile(String fileToGet) {
		File jobZipFile = null;
		File tempFile = new File(fileToGet);
		try {
			if (StringUtils.isEmpty(fileToGet))
				return null;

			tempFile = new File(fileToGet);
			log.debug("Filename :: " + tempFile.getName());

			jobZipFile = new File(tempFile.getName());
			SftpClient sftpClient = new SftpClient(SFTP_SERVER, SFTP_USER_NAME,
					SFTP_PRKEY_FILE, Integer.parseInt(SFTP_SERVER_PORT),
					SFTP_PASSPHRASE);
			sftpClient.changeDirectory(SFTP_REMOTE_DIR_PATH);
			sftpClient.getFile(jobZipFile.getName(), jobZipFile);

			sftpClient.close();
		} catch (Exception e) {
			log.error("Failed to SFTP GET the ZIP file");
		}

		return jobZipFile;
	}

	private String getZipFileName(String studyName) {
		StringBuilder fileName = new StringBuilder();
		DateFormat formatter = new SimpleDateFormat("MMddyyyyHHmmss");
		fileName.append(TEMP_DIR);
		fileName.append(studyName);
		fileName.append(formatter.format(Calendar.getInstance().getTime()));
		fileName.append(".zip");

		return fileName.toString();
	}

	public void wrapUp() {
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		List<File> files = new ArrayList<File>();
		String studyName = null;
		File zipFile = null;
		// TODO Run async jobs

		// TODO remove the studyName initialization to export_data
		studyName = "export_data";
		String zipFileAbsolutePath = ZipUtil.bundleZipFile(
				getZipFileName(studyName), files);
		// Only after all corresponding jobs have finished executing

		System.out.println("to copy file " + zipFileAbsolutePath + " to "
				+ SFTP_REMOTE_DIR_PATH);
		// Place bundled-file at specific location in File-system
		// Following code-snippet uses FTP to upload file
		/*
		 * boolean movedFile = FTPUtil.uploadFile(true, new File(
		 * zipFileAbsolutePath)); if (!movedFile) { // File was not successfully
		 * moved }
		 */
		// Following code-snippet uses SFTP to upload file
		try {
			zipFile = new File(zipFileAbsolutePath);
			SftpClient sftpClient = new SftpClient(SFTP_SERVER, SFTP_USER_NAME,
					SFTP_PRKEY_FILE, Integer.parseInt(SFTP_SERVER_PORT),
					SFTP_PASSPHRASE);
			sftpClient.changeDirectory(SFTP_REMOTE_DIR_PATH);
			sftpClient.putFile(zipFile);

			sftpClient.close();
		} catch (Exception e) {
			log.error("Failed to SFTP PUT the ZIP file");
		}
	}
}
