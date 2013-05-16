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
  

package com.recomdata.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * helper class provides assistance for downloading domain object
 * instances to Excel from the internet
 * @author jspencer
 */
public class DomainObjectExcelHelper {

	private String _fileName = "";
	private IDomainExcelWorkbook _domainObject = null;
	
	public DomainObjectExcelHelper(IDomainExcelWorkbook domainObject, String title) {
		_domainObject = domainObject;
		_fileName = title;		
	}

	/**
	 * download the object to Excel
	 * @param response
	 */
	public void downloadDomainObject(HttpServletResponse response) throws IOException {
		
		// setup headers for download
		response.setContentType("application/vnd.ms-excel");
		response.setCharacterEncoding("charset=utf-8");
	    response.setHeader("Content-Disposition", "attachment; filename=\""+_fileName+"\"");
	    response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
	    response.setHeader("Pragma", "public");
		response.setHeader("Expires", "0");
		
		// send workbook to response
		ServletOutputStream os = null;
		try {
			os = response.getOutputStream();
			os.write(_domainObject.createWorkbook());
		} finally {
			os.flush();	
		}
	}
	
	public static void downloadToExcel(HttpServletResponse response, String fileName, byte[] excelWB) throws IOException    {	 
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\""+fileName+"\"");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setHeader("Expires", "0");

        ServletOutputStream os = null;
        try {
            os = response.getOutputStream();
            os.write(excelWB);
        } finally {
            os.flush(); 
        }
	}
}
