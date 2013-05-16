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
  

package com.recomdata.transmart.data.export.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UserInfo;

public class SftpClient {
	/**
	 * This is the server we want to FTP to.
	 */
	private String 	_SFTPServer;
	
	/**
	 * This is the user we want to connect to the FTP server as.
	 */
	private String 	_SFTPUser;
	
	/**
	 * This is the absolue path to the private key we use to sign a message. 
	 */
	private String	_SFTPKey;
	
	/***
	 * This is the port we conduct our FTP session over.
	 */
	private String 	_SFTPPort;
	
	/**
	 * Object representing our FTP Session.
	 */
	private Session _session;
	
	/**
	 * Object representing our jcraft class instance.
	 */
	private JSch   	_jsch;
	
	/**
	 * If the private key file is password protected, this is the password.
	 */
	@SuppressWarnings("unused")
	private String _passPhrase;
	
	/**
	 * Needed for ftp connection.
	 */
	private Channel  _channel;
	/**
	 * Needed for ftp connection.
	 */	
	private ChannelSftp _channelSftp;	
		
	
	/***
	 * Base constructor takes all parameters and calls init method.
	 * @param SFTPServer Server to connect to.
	 * @param SFTPUser User to connect as.
	 * @param SFTPKey Absolute path to private key file.
	 * @param SFTPPort Port to connect over.
	 * @param SFTPPassPhrase Password for private key file (NOT USED CURRENTLY)
	 * @throws Exception
	 */
	public SftpClient(String SFTPServer, String SFTPUser, String SFTPKey,String SFTPPort, String SFTPPassPhrase) throws Exception
	{
		//Initialize class members.
		_SFTPServer = SFTPServer;
		_SFTPUser = SFTPUser;
		_SFTPKey = SFTPKey;
		_SFTPPort = SFTPPort;
		_passPhrase = SFTPPassPhrase;

		//Attempt to initialize JSch object.
		try{
			initSftpClient();
		} catch(Exception e)
		{
			throw new Exception("Failed to initialize connection!",e);
		}

	}
	
	/**
	 * Attempt to initialize JSch object, establish identity, session, and connection.
	 * @throws Exception
	 */
	protected void initSftpClient() throws Exception {
		//Create our Jcraft object.
		_jsch = new JSch();
		
		//Add our private key file with no pass to the JSch object.
		try {
			if (StringUtils.isNotEmpty(_SFTPKey)) {
				_jsch.addIdentity(_SFTPKey, new byte[0]);
			}
		} catch(JSchException jse){
			throw new Exception("Failed to add JSch identity!",jse);
		}

		//Set the session information (User, Server, port)
		try{
			if (StringUtils.isNotEmpty(_SFTPPort) && NumberUtils.isNumber(_SFTPPort)) {
				_session = _jsch.getSession(_SFTPUser, _SFTPServer, Integer.parseInt(_SFTPPort));
			} else {
				_session = _jsch.getSession(_SFTPUser, _SFTPServer);
			}
		} catch(JSchException jse){
			throw new Exception("Failed to create JSch session!",jse);
		}

		//Don't think this does anything unless you are using some kind of interactive login. Voodoo programming, I'll leave it here for now.
		UserInfo ui = new MyUserInfo();
	    _session.setUserInfo(ui);

	    // set properties so we don't get unknown host key exception
	    _session.setConfig("StrictHostKeyChecking", "no");

	    try
	    {
	    	initConnection();
	    }
	    catch(Exception e)
	    {
	    	throw new Exception("JSch session failed to initConnection!",e);
	    }
	}	

	
	/**
	 * Try to initialize our FTP connection, session and channel.
	 * @throws SFTPAuthenticationException
	 */
	private void initConnection() throws Exception
	{
	  	try
	  	{
			_session.connect();
		} catch(JSchException jse)
		{
			throw new Exception("JSch session failed to connect!",jse);
		}
	
		try
		{
			_channel = _session.openChannel("sftp");
		} catch(JSchException jse)
		{
			throw new Exception("JSch session failed to open a channel!",jse);
		}
	
		try
		{
			_channel.connect();
			_channelSftp = (ChannelSftp) _channel;
		} catch(JSchException jse)
		{
			throw new Exception("JSch channel failed to connect!",jse);
		}
	}	
	
	/**
	 * @param ftpRemoteDirectory
	 * @throws Exception
	 */
	public void changeDirectory(String ftpRemoteDirectory) throws Exception {
		_channelSftp.cd(ftpRemoteDirectory);
	}
	
	/**
	 * Puts a file on the ftp server over the already established connection.
	 * @param fileToTransfer Absolute path to the file to be transfered. 
	 * @throws Exception
	 */
	public void putFile(File fileToTransfer) throws Exception
	{
		try
		{
			_channelSftp.put(new FileInputStream(fileToTransfer),fileToTransfer.getName());
		}
		catch(FileNotFoundException fnf)
		{
			throw new Exception("Could not find file to upload!",fnf);
		}
		catch(SftpException sftpException)
		{
			throw new Exception("Could not transfer file!",sftpException);
		}
	}
	
	/**
	 * Gets a file on the ftp server over the already established connection.
	 * @param fileToGet Absolute path to the file to be transfered.
	 * @param outputFile  
	 * @throws Exception
	 */
	public void getFile(String fileToGet, File outputFile) throws Exception
	{
		try
		{
			_channelSftp.get(fileToGet,new FileOutputStream(outputFile));
		}
		catch(FileNotFoundException fnf)
		{
			throw new Exception("Could not find directory to fetch from!",fnf);
		}
		catch(SftpException sftpException)
		{
			throw new Exception("Could not get file!",sftpException);
		}
	}	
	
	/**
	 * clean up resources
	 * @throws SFTPAuthenticationException
	 */
	public void close()  {
		try{
			if(_channelSftp!=null) _channelSftp.exit();
			if(_channel !=null) _channel.disconnect();
			if(_session !=null) _session.disconnect();
			if(_jsch !=null) _jsch.removeAllIdentity();
		} catch(JSchException e) {
			//logger.warn("Issue closing SFTP connection: "+e.getMessage(),e);
		}
	}
	
	public static class MyUserInfo implements UserInfo{

	    String passwd;

	    public String getPassword(){ return passwd; }

	    public boolean promptYesNo(String str){
	       return true;
	    }

	    public String getPassphrase(){ return null; }

	    public boolean promptPassphrase(String message){ return true; }

	    public boolean promptPassword(String message){
	    	return true;
	    }

	    public void showMessage(String message){
	    }
	}
	
	public static void main(String[] args) {
		//File outputFile = new File("./latestGet.log");
		try {
			SftpClient sftpClient = new SftpClient("factbookdev", "SvcCOPSSH", "C:/Users/smunikuntla/Downloads/SaiMunikuntla12.ppk", "22", "");
			sftpClient.putFile(new File("C:/Users/smunikuntla/Downloads/camelinaction-src.zip"));
			//sftpClient.getFile("server1.log", outputFile);
			
			File jobZipFile = null;
			File tempFile = null;
			
			/*StringTokenizer st = new StringTokenizer("ftp://SvcCOPSSH:@factbookdev:22/camelinaction-src.zip", "/");
			String token = null;
			while (st.hasMoreTokens()) {
				token = st.nextToken();
			}*/
			tempFile = new File("ftp://SvcCOPSSH:@factbookdev:22/camelinaction-src.zip");
			jobZipFile = new File(tempFile.getName());
			
			sftpClient.getFile("camelinaction-src.zip", jobZipFile);
			
			sftpClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
