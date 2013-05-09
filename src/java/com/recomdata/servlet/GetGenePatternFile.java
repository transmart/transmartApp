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
  

package com.recomdata.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.recomdata.export.IgvFiles;

/**
 * This servlet is to provide data as remote accessed URL to IGV. Grails controller cannot handle the HttpInputStream.close() call from the client
 * IGV may issue http "head" request to get attributes of the data, without getting the data itself
 * IGV will also issue multiple "get" request for a data, each request downloading a segment of data.
 */
public class GetGenePatternFile extends HttpServlet {
	
	public static String fileDirName = "data";
	
	public void doHead(HttpServletRequest request, HttpServletResponse response)
			throws ServletException,IOException {
		String fileName = request.getParameter("file");
		File file = new File(getGenePatternFileDirName(request) + File.separator + fileName);
		Long fileLength = new Long(file.length());
		
		response.setContentType("application/octet-stream");
		response.setHeader("Content-disposition", "attachment; filename=" + fileName);
		response.setContentLength(fileLength.intValue());		
	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		FileInputStream fileIn = null;
		OutputStream out = response.getOutputStream();
		
		try {
			String fileName = request.getParameter("file");
			String userName = request.getParameter("user");
			String secureHash = request.getParameter("hash");	// hash is the 
			
			System.out.println("filename:"+fileName+":userName:"+userName);
			File file = new File(getGenePatternFileDirName(request) + File.separator + fileName);
			Long fileLength = new Long(file.length());
			
			String requestMethod = request.getMethod();
			if ("head".equalsIgnoreCase(requestMethod)) {
				response.setContentType("application/octet-stream");
				response.setHeader("Content-disposition", "attachment; filename=" + fileName);
				response.setContentLength(fileLength.intValue());
				return;
			}
			else if ("get".equalsIgnoreCase(requestMethod) == false)
				return;
		
			
			String hashStr = IgvFiles.getFileSecurityHash(file, userName);
			System.out.println("newhash:"+hashStr+" oldhash:"+secureHash);
			if (hashStr.equals(secureHash) == false) {
				throw new ServletException("The user name and security hash does not match those for the file " + fileName);
			}
			
			fileIn = new FileInputStream(file);
			int endFile = new Long(file.length()).intValue();
			
			boolean isRangeRead = false;
			int startIn = 0;
			int endIn = 0;
			int len = 0;
			String rangeStr = request.getHeader("Range");
			
			
			
			if (rangeStr != null && rangeStr.length() != 0) {
				int idx_1 = rangeStr.indexOf("=");
				int idx_2 = rangeStr.lastIndexOf("-");
				String startStr = rangeStr.substring(idx_1 + 1, idx_2);
				String endStr = rangeStr.substring(idx_2 + 1);
				startIn = Integer.parseInt(startStr);
				endIn = Integer.parseInt(endStr);
				int endPos = Math.min(endIn, endFile);
				len = endPos - startIn + 1;
				isRangeRead = true;
			}
		   
			// Set headers
			response.setContentType("application/octet-stream");
			response.setHeader("Content-disposition", "attachment; filename=" + fileName);
			
			byte[] bytes = null;
			if (isRangeRead) {
				if (startIn > endFile) {
					response.setContentLength(0);
				}
				else {
					response.setContentLength(len);
					response.setHeader("Content-Range", "bytes " + startIn + "-" + endIn + "/" + endFile); // formal response for ranged request, to satisfy Apache proxy
					bytes = new byte[len];
					if (startIn > 0)
						fileIn.skip(new Long(startIn).longValue());
					fileIn.read(bytes);
					out.write(bytes);
				}
			}
			else {
				// Write the file content. The file can be very large
				response.setContentLength(endFile);
				bytes = new byte[2048];
				int bytesRead;
				while ((bytesRead = fileIn.read(bytes)) != -1) {
					out.write(bytes, 0, bytesRead);
				}
			}
		}
		catch (Exception e) {
			throw new ServletException(e);
		}
		finally {
			if (fileIn != null) {
				fileIn.close();
			}
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}
		
	protected String getGenePatternFileDirName(HttpServletRequest request) {
		String webRootName = request.getSession().getServletContext().getRealPath("/");
		if (webRootName.endsWith(File.separator) == false) 
			webRootName += File.separator;
		return webRootName + fileDirName;
	}
	
}
