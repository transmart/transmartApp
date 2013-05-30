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

import java.sql.*;
import java.io.*;
import java.util.*;
import java.util.Collections;
import org.json.*;

/**
 *
 * @author Chris Uhrich
 * @version 1.0
 *
 * Copyright 2008 Recombinant Data Corp.
 */
public class HeatMapTable {

	private static final String RATIO_PATTERN = "##0.######";
	private HashMap<String, String> ids1 = new HashMap<String, String>();
	private HashMap<String, String> ids2 = new HashMap<String, String>();
	private LinkedHashMap<String, HeatMapColumn> columns = new LinkedHashMap<String, HeatMapColumn>();
	private LinkedHashMap<String, HeatMapRow> rows = new LinkedHashMap<String, HeatMapRow>();
	private HashMap<String, String> descriptions = new HashMap<String, String>();
	private Boolean missingValues = false;
	
	public HeatMapTable(List<ExportResult> rs1, String patientIds1, List<ExportResult> rs2, String patientIds2)
		// throws SQLException 
		{
		//setColumns(patientIds1, patientIds2);
		setColumns(rs1, rs2);

		for(ExportResult e : rs1)
		{
			String gene = e.getConcept();
			String description = e.getDescription();
			String patientId = e.getSubject();
			String value = e.getValue();
			String columnName = ids1.get(patientId);

			HeatMapRow row;
			if (!rows.containsKey(gene)) {
				row = new HeatMapRow(gene, columns.keySet());
				rows.put(gene, row);
			} else {
				row = rows.get(gene);
			}
			row.put(columnName, value);

			if (!descriptions.containsKey(gene)) {
				descriptions.put(gene, description);
			}
			if (value == null || value == "null" || value == "") {
				missingValues = true;
			}
		
		}

		for(ExportResult e : rs2)
		{
			String gene = e.getConcept();
			String description = e.getDescription();
			String patientId = e.getSubject();
			String value = e.getValue();
			String columnName = ids2.get(patientId);

			HeatMapRow row;
			if (!rows.containsKey(gene)) {
				row = new HeatMapRow(gene, columns.keySet());
				rows.put(gene, row);
			} else {
				row = rows.get(gene);
			}
			row.put(columnName, value);

			if (!descriptions.containsKey(gene)) {
				descriptions.put(gene, description);
			}

			if (value == null || value == "null" || value == "") {
				missingValues = true;
			}

		}

		HeatMapRow row;
		ArrayList<String> ids = new ArrayList<String>();
		for (String id : ids1.values()) {
			ids.add(id);
		}
		for (String id : ids2.values()) {
			ids.add(id);
		}

		for (String gene : rows.keySet()) {
			row = rows.get(gene);
			for (String id : ids) {
				String value = row.get(id);
				if (value == "null" || value == null || value == "") {
					// causes problems in genepattern
					missingValues = true;
					System.out.println("found missing value: " + gene +", " + id);
					return;
				}
			}
		}		
	}

		public void writeToFile(String delimiter, PrintStream ps) {
			if (missingValues) {
				this.writeToFile(delimiter, ps, true);
			} else {
				this.writeToFile(delimiter, ps, false);
			}
		}
	
		public void writeToFile(String delimiter, PrintStream ps, Boolean addMeans) {
			// outputs a data table, suitable for export to genepattern

			ps.println("#1.2");

			if (addMeans) {
				ps.println(rows.size() + delimiter + columns.size() );
			} else {
				ps.println(rows.size() + delimiter + (columns.size() - 1 ) );
			}
			
			ps.print("NAME"+delimiter+"Description");
			
			
			// to assure that columns are returned in the right order,
			// build a list of subject ids, then sort the list
			ArrayList<String> ids = new ArrayList<String>();
			for (String id : ids1.values()) {
				ids.add(id);
			}
			for (String id : ids2.values()) {
				ids.add(id);
			}
			
			// ids should be numerically sorted already...

			java.util.Collections.sort(ids, new subjectComparator() );

			for (String id : ids) {
				ps.print(delimiter + id);
			}
			
			if (addMeans) {
				ps.print(delimiter + "Mean");
			}
			
			ps.print("\n");

			HeatMapRow row;
			Double sumOfValues = 0.0;
			Double countOfValues = 0.0;
			for (String gene : rows.keySet()) {
				ps.print(gene + delimiter + descriptions.get(gene));
				row = rows.get(gene);
				for (String id : ids) {
					String value = row.get(id);
					if (value == "null" || value == "") {
						// causes problems in genepattern
						value = "";
					} else if (addMeans) {
						// System.out.println(gene + ", " + id + ": " + value);
						sumOfValues = sumOfValues + Double.valueOf(value);
						countOfValues = countOfValues + 1;
					}
					ps.print(delimiter + value);
				}
				if (addMeans) {
					Double mean = 0.0;
					if (countOfValues > 0) {
						mean = sumOfValues / countOfValues;
					}
					ps.print(delimiter + mean);
				}
				ps.print("\n");			
			}
		}
	

		public void setColumns(String patientIds1, String patientIds2) {

		columns.put("Gene", new HeatMapColumn("Gene", "Gene", "", "t"));

		StringTokenizer st1 = new StringTokenizer(patientIds1, ",");
//		SortedSet set1 = new TreeSet();
//		
//		while (st1.hasMoreTokens()) {
//			set1.add(st1.nextToken());
//		}
//
//		Iterator  set1Iterator = set1.iterator();
//		
//		while (set1Iterator.hasNext()) {
//			String id = set1Iterator.next().toString();
//			String label = "S1_" + id;
//			ids1.put(id, label);
//			columns.put(label, new HeatMapColumn(id, label, RATIO_PATTERN, "n"));
//		}
		while (st1.hasMoreTokens()) {
			String id = st1.nextToken();
			//String label = "S1_" + (ids1.size() + 1);
			String label = "S1_" + id;
			ids1.put(id, label);
			columns.put(label, new HeatMapColumn(id, label, RATIO_PATTERN, "n"));
		}

		StringTokenizer st2 = new StringTokenizer(patientIds2, ",");
		while (st2.hasMoreTokens()) {
			String id = st2.nextToken();
			//String label = "S2_" + (ids2.size() + 1);
			String label = "S2_" + id;
			ids2.put(id, label);
			columns.put(label, new HeatMapColumn(id, label, RATIO_PATTERN, "n"));
		}
	}

		public void setColumns(List<ExportResult> rs1, List<ExportResult> rs2) {
		columns.put("Gene", new HeatMapColumn("Gene", "Gene", "", "t"));

		for(ExportResult e : rs1)
		{
			String gene = e.getConcept();
			String patientId = e.getSubject();
			if(!ids1.containsKey(patientId)){
			// String label = "S1_" + (ids1.size() + 1);
			String label = "S1_" + patientId;
			ids1.put(patientId, label);
			columns.put(label, new HeatMapColumn(patientId, label, RATIO_PATTERN, "n"));
			}
		}
		for(ExportResult e : rs2)
		{

			String gene = e.getConcept();
			String patientId = e.getSubject();
			if(!ids2.containsKey(patientId)){
			// String label = "S2_" + (ids2.size() + 1);
			String label = "S2_" + patientId;
			ids2.put(patientId, label);
			columns.put(label, new HeatMapColumn(patientId, label, RATIO_PATTERN, "n"));
			}
		}

	}
	public JSONObject toJSONObject() throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("status", "ok");
		jsonObj.put("requestId", "0");

		JSONObject jsonTable = new JSONObject();

		JSONArray jsonColumns = new JSONArray();
		for (Iterator<HeatMapColumn> i = columns.values().iterator(); i.hasNext(); ) {
			jsonColumns.put(i.next().toJSONObject());
		}
		jsonTable.put("cols", jsonColumns);

		JSONArray jsonRows = new JSONArray();
		for (Iterator<HeatMapRow> i = rows.values().iterator(); i.hasNext(); ) {
			jsonRows.put(i.next().toJSONArray());
		}
		jsonTable.put("rows", jsonRows);

		jsonObj.put("table", jsonTable);
		jsonObj.put("signature", "1");

		return jsonObj;
	}

}
