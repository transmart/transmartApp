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
* $Id: ExcelGenerator.java 9178 2011-08-24 13:50:06Z mmcduffie $
**/
package com.recomdata.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;

/**
 *@author $Author: mmcduffie $
 *@version $Revision: 9178 $
 **/
public class ExcelGenerator {

	public static byte[] generateExcel(List headers, List values) {

		ExcelSheet sheet = new ExcelSheet("Sheet 1", headers, values);

		List<ExcelSheet> sheets = new ArrayList<ExcelSheet>();
		sheets.add(sheet);

		return generateExcel(sheets);

	}

	public static byte[] generateExcel(List sheets) {

		HSSFWorkbook wb = new HSSFWorkbook();

		for (ExcelSheet sheet : (List<ExcelSheet>) sheets) {
	        HSSFSheet s = wb.createSheet(sheet.getName());
	        HSSFFont f = wb.createFont();
	        HSSFCellStyle cs = wb.createCellStyle();
	        HSSFFont f2 = wb.createFont();

	        //set font 1 to 12 point type
	        f.setFontHeightInPoints((short) 10);
	        //make it blue
	        f.setColor(HSSFColor.BLACK.index);
	        //arial is the default font
	        f.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
	        cs.setFont(f);
	        cs.setFillForegroundColor((HSSFColor.LIGHT_BLUE.index));
	        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);

	        HSSFRow headerRow = s.createRow(0);
			short hCount = 0;
	        for (String title : (List<String>) sheet.getHeaders()) {
	            HSSFCell cell = headerRow.createCell((short)hCount);
	            cell.setCellValue(title);
	            cell.setCellStyle(cs);
	            hCount++;
	        }

	        HSSFCellStyle csWrapText = wb.createCellStyle();
	        csWrapText.setWrapText(true);
	        
	        short rowCount = 1;
	        short cellMax = 0;
	        for (Object value : sheet.getValues()) {
	            HSSFRow row = s.createRow((short)rowCount);
	          	short cellCount = 0;
	          	for (Object v : (List) value) {
	                HSSFCell dcell = row.createCell((short)cellCount);
	                if (v == null || (v instanceof String && v.toString().trim().length() == 0)) {
	              	  	// println("empty cell");
	                }
	                else {
	                    try    {
	                        Double d = Double.parseDouble(v.toString());
	                        dcell.setCellValue(d);
	                    } catch(NumberFormatException nfe) {	                        
	              	  	    dcell.setCellValue(v.toString());
	              	  	    if (v.toString().length() > 100) {    
	              	  	        dcell.setCellStyle(csWrapText);
	              	  	    }
	                    }
	                }	                
	          	  	cellCount++;
	          	}
	          	cellMax = cellMax < cellCount ? cellCount : cellMax;
	            rowCount++;
	        }
	        
	        for (short cnt=0; cnt < cellMax; cnt++)    {
	            s.autoSizeColumn(cnt);
	        }
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try {
			wb.write(output);
		} catch (IOException ex) {
			// TODO: log error
		}
	    return output.toByteArray();

	}	
	
}

