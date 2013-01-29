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
  

package com.recomdata.etl.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class RBMFilePivot {

	public static void main(String[] args) {
		pivotData( 7,
				"C:\\temp\\c0743t10_cleaned_PDMarkers.txt",
				"C:\\temp\\c0743t10_pivotted_PDMarkers.txt");
	}

	private static void pivotData(
			int pivotStartPosition,
			String dataFile,
			String outputFile) {
		File mapfile = new File(dataFile);
		String filename = outputFile;
		BufferedReader reader = null;
		BufferedWriter writer = null;


		try {
			reader = new BufferedReader(new FileReader(mapfile));
			writer = new BufferedWriter(new FileWriter(filename));
			String line = "";
			int lineRead = 6000;
			int count = 0;
			String[] headerArray=new String[pivotStartPosition+2];
		    String[] antigenArray = new String[200];

			while ((line = reader.readLine()) != null && count < lineRead) {
				String[] tokens = line.split("\t");

				if (count == 0) {
					int colCount = 0;
					for (String token : tokens) {
						if(colCount<pivotStartPosition){
							headerArray[colCount]=token;
						}else if(colCount==pivotStartPosition){
							headerArray[colCount]="Antigen";
							headerArray[colCount+1]="Value";
							antigenArray[colCount]=token;
						}
						else {

							antigenArray[colCount]=token;
						//	antCount++;
						}
						colCount++;
					}

					//System.out.println(subjectMap);
				for(int i=0;i<headerArray.length;i++){
					if(i>0)
						writer.write("\t");
					writer.write(headerArray[i]);

				}
				writer.newLine();
				}

				else {
					int cCount = 0;
					StringBuilder comColumns = new StringBuilder();
					String colStr = null;
					for (String token : tokens) {

					if(cCount<pivotStartPosition){
						comColumns.append(token).append("\t");
						colStr = comColumns.toString();
					}else {
						writer.write(colStr);
						writer.write(antigenArray[cCount]);
						writer.write("\t");
						writer.write(token);
						writer.newLine();
					 }
					cCount++;
					}
				}
				count++;
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				if (writer != null) {
					writer.flush();
					writer.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}

		}

	}
}
