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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * @author SMunikuntla
 *
 */
public class ExportImageProcessor {
	
	private static final ThreadGroup imagesThreadGroup = new ThreadGroup("Images");

	private String getFilename(URI imageURI) {
		String filename = null;
		if (StringUtils.equalsIgnoreCase("file", imageURI.getScheme())) {
			filename = (new File(imageURI.toString())).getName();
		} else {
			if (null != imageURI) {
				String imageURIStr = imageURI.toString();
				if (StringUtils.isNotEmpty(imageURIStr)) {
					int loc = imageURIStr.lastIndexOf("/");
					if (loc == imageURIStr.length() - 1) {
						loc = (imageURIStr.substring(0, loc - 1)).lastIndexOf("/");
					}
					filename = imageURIStr.substring(loc+1, imageURIStr.length());
				}
			}
		}
		
		return filename;
	}
	
	/**
	 * @param imageURLStr
	 * @param filename
	 * @return
	 * @throws URISyntaxException 
	 */
	public void getImageFromURI(String imageURIStr, String filename) throws URISyntaxException {
		if (StringUtils.isEmpty(filename)) {
			filename = getFilename(new URI(imageURIStr));
		}
		new Thread(imagesThreadGroup, new ExportImageThread(imageURIStr, filename)).start();
	}
	
	public void getImages(List<String> imageURIs) {
		for (String imageURI : imageURIs) {
			try {
				getImageFromURI(imageURI, null);
			} catch (URISyntaxException e) {
				System.out.println("Invalid URI for image :: "+imageURI);
				e.printStackTrace();
			}
		}
	}
}

class ExportImageThread extends Thread {
	private String imageURI;
	private String filename;
	
	/**
	 * change to configurable from property file
	 */
	private static final String imagesTempDir = "C://images";
	public ExportImageThread(String imageURI, String filename) {
		this.imageURI = imageURI;
		this.filename = filename;
	}
	
	public void run() {
		URL imageURL = null;
		File imageFile = new File(imagesTempDir+File.separator+filename);
		ReadableByteChannel rbc = null;
		FileOutputStream fos = null;
		try {
			if (StringUtils.isEmpty(imageURI))
				return;
			
			imageURL = new URL(imageURI);
			rbc = Channels.newChannel(imageURL.openStream());
			fos = new FileOutputStream(imageFile);
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != fos)
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
