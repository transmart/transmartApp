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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.log4j.Logger;

import com.recomdata.transmart.data.export.exception.FTPAuthenticationException;
import com.recomdata.transmart.data.export.exception.InvalidFTPParamsException;

/**
 * @author SMunikuntla
 * 
 */
public class FTPUtil {
	
	private static org.apache.log4j.Logger log =
            Logger.getLogger(FTPUtil.class);
	@SuppressWarnings("rawtypes")
	
	private static FTPClient ftp;

	/**
	 * Validates if all the required params to connect to FTP server are not
	 * empty
	 * 
	 * @throws InvalidFTPParamsException
	 */
	private static void validate(String ftpServer, String ftpServerUserName, String ftpServerPassword) throws InvalidFTPParamsException {
		if (StringUtils.isEmpty(ftpServer)
				|| StringUtils.isEmpty(ftpServerUserName)
				|| StringUtils.isEmpty(ftpServerPassword)) {
			throw new InvalidFTPParamsException("Invalid FTP Connection Params");
		}
	}

	/**
	 * This method connects to the specified FTP Server:Port details in the
	 * configuration file
	 * 
	 * @throws InvalidFTPParamsException
	 */
	private static void connect(String ftpServer, String ftpServerPort, String ftpServerUserName, String ftpServerPassword) throws InvalidFTPParamsException {
		validate(ftpServer, ftpServerUserName, ftpServerPassword);
		ftp = new FTPClient();
		try {
			int reply;
			if (NumberUtils.isNumber(ftpServerPort)
					&& Integer.parseInt(ftpServerPort) > 0) {
				ftp.connect(ftpServer, Integer.parseInt(ftpServerPort));
			} else {
				ftp.connect(ftpServer);
			}
			System.out.println("Connected to " + ftpServer + " on "
					+ ftp.getRemotePort());

			// After connection attempt, you should check the reply code to
			// verify success.
			reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				System.err.println("FTP server refused connection.");
			}
		} catch (IOException e) {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException f) {
					log.error(e.getMessage(), e);
				}
			}
			System.err.println("Could not connect to FTP server.");
			e.printStackTrace();
		}
	}

	/**
	 * Performs the login operation for the provided user credentials
	 * 
	 * @throws FTPAuthenticationException
	 */
	private static void login(String ftpServerUserName, String ftpServerPassword) throws FTPAuthenticationException {
		try {
			if (StringUtils.isNotEmpty(ftpServerUserName) && StringUtils.isNotEmpty(ftpServerPassword)) {
				if (!ftp.login(ftpServerUserName, ftpServerPassword)) {
					ftp.logout();
					throw new FTPAuthenticationException(
							"Credentials failed to Authenticate on the FTP server");
				}
				System.out.println("Remote system is " + ftp.getSystemType());
			}
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * Uploads a given file to the connected FTP Server
	 * 
	 * @param binaryTransfer
	 * @param localFile
	 * @return remote FTP location of the file
	 */
	public static String uploadFile(boolean binaryTransfer, File localFile, String ftpServer, String ftpServerPort, String ftpServerUserName, String ftpServerPassword, String ftpServerRemotePath) {
		String remote = null;
		boolean uploadComplete = false;
		try {
			connect(ftpServer, ftpServerPort, ftpServerUserName, ftpServerPassword);
			login(ftpServerUserName, ftpServerPassword);

			if (binaryTransfer)
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
			// Use passive mode as default because most of us are
			// behind firewalls these days.
			ftp.enterLocalPassiveMode();
			ftp.setUseEPSVwithIPv4(false);

			InputStream input = new FileInputStream(localFile);

			remote = ftpServerRemotePath + localFile.getName();
			uploadComplete = ftp.storeFile(remote, input);

			input.close();
		} catch (InvalidFTPParamsException e) {
			log.error("Invalid FTP Params to connect");
		} catch (FTPAuthenticationException e) {
			log.error(e.getMessage());
		} catch (FileNotFoundException e) {
			log.error("Not able to load/read the localFile");
		} catch (IOException e) {
			log.error("IOException during FTP upload process");
		} finally {
			if (!uploadComplete) remote = null;
		}

		return remote;
	}
	
	public static InputStream downloadFileStream(boolean binaryTransfer, String filename, String location, String ftpServer, String ftpServerPort, String ftpServerUserName, String ftpServerPassword, String ftpServerRemotePath) {
		InputStream inputStream = null;
		try {
			String remote = ((StringUtils.isNotEmpty(location)) ? location : ftpServerRemotePath)+filename;
			connect(ftpServer, ftpServerPort, ftpServerUserName, ftpServerPassword);
			login(ftpServerUserName, ftpServerPassword);

			if (binaryTransfer)
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
			// Use passive mode as default because most of us are
			// behind firewalls these days.
			ftp.enterLocalPassiveMode();
			ftp.setUseEPSVwithIPv4(false);

			inputStream = ftp.retrieveFileStream(remote);
		} catch (InvalidFTPParamsException e) {
			log.error("Invalid FTP Params to connect");
		} catch (FTPAuthenticationException e) {
			log.error(e.getMessage());
		} catch (FileNotFoundException e) {
			log.error("Not able to load/read the localFile");
		} catch (IOException e) {
			log.error("IOException during FTP upload process");
		}

		return inputStream;
	}
	
	public static InputStream downloadFile(boolean binaryTransfer, String filename,  String ftpServer, String ftpServerPort, String ftpServerUserName, String ftpServerPassword, String ftpServerRemotePath) {
		return downloadFileStream(binaryTransfer, filename, null, ftpServer, ftpServerPort, ftpServerUserName, ftpServerPassword, ftpServerRemotePath);
	}
	
	public static boolean deleteFile(String filename,  String ftpServer, String ftpServerPort, String ftpServerUserName, String ftpServerPassword, String ftpServerRemotePath){
		return deleteFile(filename, null, ftpServer, ftpServerPort, ftpServerUserName, ftpServerPassword, ftpServerRemotePath);
	}
	
	public static boolean deleteFile(String filename, String location, String ftpServer, String ftpServerPort, String ftpServerUserName, String ftpServerPassword, String ftpServerRemotePath){
		String remote = null;
		boolean ret=false;
		try{
			remote = ((StringUtils.isNotEmpty(location))? location : ftpServerRemotePath)+filename;
			connect(ftpServer, ftpServerPort, ftpServerUserName, ftpServerPassword);
			login(ftpServerUserName, ftpServerPassword);
			
			ret=ftp.deleteFile(remote);
		}catch (InvalidFTPParamsException e) {
			log.error("Invalid FTP Params to connect");
		} catch (FTPAuthenticationException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error("IOException during FTP delete process for "+remote);
		}
		return ret;
	}
}
