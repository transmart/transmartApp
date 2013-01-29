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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class DemoGeneExprDataParser {

	public static void main(String[] args) {
		pivotData(
				"C:\\ukdata\\affmx-5\\E-AFMX-5-processed-data-1342292711.txt",
				"C:\\ukdata\\affmx-5\\271x.txt");
		pivotData(
				"C:\\ukdata\\affmx-5\\E-AFMX-5-processed-data-1342297491.txt",
				"C:\\ukdata\\affmx-5\\491x.txt");

	}

	private static void pivotData(String dataFile, String outputFile) {
		File mapfile = new File(dataFile);
		String filename = outputFile;
		BufferedReader reader = null;
		BufferedWriter writer = null;

		try {
			reader = new BufferedReader(new FileReader(mapfile));
			writer = new BufferedWriter(new FileWriter(filename));
			String line = "";
			int lineRead = 4;
			int count = 0;
			Map<String, Integer> subjectMap = new LinkedHashMap<String, Integer>();
			Map<String, Integer> colMap = new LinkedHashMap<String, Integer>();
			while ((line = reader.readLine()) != null && count < lineRead) {
				String[] tokens = line.split("\t");
				int colCount = 0;
				if (count == 0) {
					for (String token : tokens) {

						subjectMap.put(token, colCount);
						colCount++;
					}
					System.out.println(subjectMap);
				}

				else if (count == 1) {

					for (String token : tokens) {
						String normalizedToken = token.replace(" ", "");
						normalizedToken = normalizedToken.replace(":", "");
						if (colMap.get(normalizedToken) == null)
							colMap.put(normalizedToken, 0);
						int c = (colMap.get(normalizedToken)) + 1;
						colMap.put(normalizedToken, c);

					}
					System.out.println(colMap);
					writer.write("Subject");
					writer.write("\t");
					Iterator<String> key = colMap.keySet().iterator();
					while (key.hasNext()) {
						writer.write(key.next().toString());
						writer.write("\t");
					}
					writer.newLine();

				} else {

					int vcount = 0;
					int uplimit = 1;
					String cSubject = null;
					String cRef = null;
					Iterator<String> subjectIt = subjectMap.keySet().iterator();
					cSubject = subjectIt.next().toString();
					cRef = tokens[0];
					uplimit = subjectMap.get(cSubject);
					StringBuilder record = new StringBuilder();
					String tValue = "";
					for (String token : tokens) {
						tValue = token;
						if ("Absent".equals(token)) {
							tValue = ("-1000");
						} else if ("Present".equals(token)) {
							tValue = "0.01";
						}
						if (vcount > 0) {

							if (vcount == uplimit) {
								double t = Double.parseDouble(tValue);
								if (t <= 0.05) {
									record.append("\t").append(tValue);
									writer.write(record.toString());
									writer.newLine();
								}

							} else if (vcount > uplimit) {

								if (subjectIt.hasNext()) {
									cSubject = subjectIt.next().toString();
									uplimit = subjectMap.get(cSubject);
									// writer.newLine();
									record = new StringBuilder();
									record.append(cSubject).append("\t");
									record.append(cRef).append("\t").append(
											tValue);

								}
							} else {
								record.append("\t").append(tValue);
							}

						}
						vcount++;
					}
					// writer.newLine();
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
