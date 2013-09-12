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
  

package com.recomdata.export;

import java.util.*;
import java.io.*;
import org.apache.log4j.*;

public class CSVGenerator {
	
	public static byte[] generateCSV(ArrayList<String> headers, ArrayList<List> values){
		FileWriter csvWriter=null;
		String columnNames="";
		String rowValues="";
		String export="";
		try{
		    //csvWriter=new FileWriter("export.csv");
			
			for(int i=0;i<headers.size();i++)
				columnNames=columnNames+headers.get(i)+",";
			columnNames=columnNames.substring(0, columnNames.length()-1);
			
			//csvWriter.write(columnNames+"\n");
				//System.out.print(headers.get(i)+",");
			export=export+columnNames+"\n";	
			
			for(int i=0;i<values.size();i++){
				rowValues="";
				for(int j=0;j<values.get(i).size();j++)
					rowValues=rowValues+"\""+values.get(i).get(j).toString().replace("\"","\"\"")+"\""+",";
					//System.out.print(values.get(i).get(j)+",");
				rowValues=rowValues.substring(0, rowValues.length()-1);
				export=export+rowValues+"\n";
				//csvWriter.write(rowValues+"\n");
				//System.out.println();
				
				//csvWriter.flush();
			}
		
		}catch(Exception e){
			e.printStackTrace();
		}//finally{
		//	try{
		//		csvWriter.close();
		//	}catch(IOException e){
		//		e.printStackTrace();
		//	}
		//}
		return(export.getBytes());
	}
}
