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

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URI
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class FileDownloadService {

    boolean transactional = true
	
	def config = ConfigurationHolder.config
	
	def getFilename(String fileURIStr) {
		URI fileURI = new URI(fileURIStr);
		String filename = null;
		if (StringUtils.equalsIgnoreCase("file", fileURI.getScheme())) {
			filename = (new File(fileURI.toString())).getName();
		} else {
			if (null != fileURI) {
				if (StringUtils.isNotEmpty(fileURIStr)) {
					int loc = fileURIStr.lastIndexOf("/");
					if (loc == fileURIStr.length() - 1) {
						loc = (fileURIStr.substring(0, loc - 1)).lastIndexOf("/");
					}
					filename = fileURIStr.substring(loc+1, fileURIStr.length());
				}
			}
		}
		
		println 'Filename retrieved from URL :: ' + filename
		return filename;
	}
	
	// How many threads to kick off
	def nThreads = 0
	def ThreadPoolExecutor pool = null

	/**
	 * 
	 * @return
	 */
	def getFiles(List<String> fileURLs, String dirToDownloadTo) {
		nThreads = fileURLs?.size()
		println 'number of files :: ' + nThreads
		println 'Dir to download to :: ' + dirToDownloadTo
		if (nThreads > 0) pool = Executors.newFixedThreadPool( nThreads )
		// Construct a list of the URL objects we're running, submitted to the pool
		def results = fileURLs.inject( [] ) { list, url ->
			println 'Creating thread to download File :: ' + url
			def u = new FileDownload(url, getFilename(url), dirToDownloadTo)
			pool?.submit u
			list << u
		}
		
		// Always a good idea to set a timeout for the pool if we don't want the threads to go on for ever.
		// Get max-time to process files download using URLs from the config 
		// def timeout = config.com.recomdata.transmart.data.export.files.pool.timeout
		def timeout = 0
		if (timeout > 0) pool.awaitTermination( timeout, TimeUnit.SECONDS)
		println 'Waiting for Threads to finish executing'
		// Wait for the poolclose when all threads are completed
		while (pool?.activeCount > 0);
		println 'Threads finished execution'
		pool?.shutdown()
		println 'Thread-Pool shutdown initiated'
    }
}

class FileDownload extends Thread {
	private String fileURI;
	private String filename;
	private String fileContainerDir;
	
	public FileDownload(String fileURI, String filename, String dirToDownloadTo) {
		this.fileURI = fileURI;
		this.filename = filename;
		this.fileContainerDir = dirToDownloadTo;
	}
	
	public void run() {
		URL fileURL = null;

		File file = new File(fileContainerDir+File.separator+filename);
		ReadableByteChannel rbc = null;
		FileOutputStream fos = null;
		try {
			println 'File URI :: ' + fileURI
			if (StringUtils.isEmpty(fileURI))
				return;
			
			fileURL = new URL(fileURI);
			rbc = Channels.newChannel(fileURL.openStream());
			fos = new FileOutputStream(file);
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			println 'Invalid File URL'
		} catch (IOException e) {
			e.printStackTrace();
			println 'IO failure during file download'
		} finally {
			if (null != fos) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			println 'Download of File finalized :: ' + fileURI
		}
	}
}
