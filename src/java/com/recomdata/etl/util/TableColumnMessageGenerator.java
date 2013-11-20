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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TableColumnMessageGenerator {

	public static void main(String[] args) {

		updategene();
	}

	private static void updategene() {
		File mapfile = new File("C:\\updateids.csv");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(mapfile));
			String line = "";

			while ((line = reader.readLine()) != null) {
				System.out
						.print("UPDATE GeneExpressionAnalysis SET Ratio = -Ratio WHERE id = "
								+ line + ";\n");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	@SuppressWarnings("unused")
	private void columnMessage() {
		File mapfile = new File("C:\\temp\\JubilantColumnMapping.txt");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(mapfile));
			String line = "";

			while ((line = reader.readLine()) != null) {
				String[] tokens = line.split("\t");
				System.out.println(tokens[0] + "."
						+ tokens[1].replace("_", "").toLowerCase() + "="
						+ tokens[2]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

	}

}
