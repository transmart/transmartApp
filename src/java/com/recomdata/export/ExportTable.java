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
import java.util.*;
import org.json.*;

/**
 * 
 * @author Chris Uhrich
 * @version 1.0
 * 
 * Copyright 2008 Recombinant Data Corp.
 */
public class ExportTable {
	
	private static final String RATIO_PATTERN = "##0.######";
	private HashMap<String, String> ids1 = new HashMap<String, String>();
	private HashMap<String, String> ids2 = new HashMap<String, String>();
	private LinkedHashMap<String, ExportColumn> columns = new LinkedHashMap<String, ExportColumn>();
	private LinkedHashMap<String, ExportRow> rows = new LinkedHashMap<String, ExportRow>();
	
	public ExportTable(List<ExportResult> rs1, String patientIds1, List<ExportResult> rs2, String patientIds2) throws SQLException {
		setColumns(patientIds1, patientIds2);
		for(ExportResult e : rs1)
		{
			String gene = e.getConcept();
			String patientId = e.getSubject();
			String value = e.getValue();
			String columnName = ids1.get(patientId);
			
			ExportRow row;
			if (!rows.containsKey(gene)) {
				row = new ExportRow(gene, columns.keySet());
				rows.put(gene, row);
			} else {
				row = rows.get(gene);
			}
			row.put(columnName, value);
		}
		
		for(ExportResult e : rs2)
		{
			String gene = e.getConcept();
			String patientId = e.getSubject();
			String value = e.getValue();
			String columnName = ids2.get(patientId);
			
			ExportRow row;
			if (!rows.containsKey(gene)) {
				row = new ExportRow(gene, columns.keySet());
				rows.put(gene, row);
			} else {
				row = rows.get(gene);
			}
			row.put(columnName, value);
		}
	}
	
	public void setColumns(String patientIds1, String patientIds2) {
		columns.put("Gene", new ExportColumn("Gene", "Gene", "", "t"));
		
//		StringTokenizer st1 = new StringTokenizer(patientIds1, ",");
//		while (st1.hasMoreTokens()) {
//			String id = st1.nextToken();
//			String label = "S1_" + id;//(ids1.size() + 1);
//			ids1.put(id, label);
//			columns.put(label, new ExportColumn(id, label, RATIO_PATTERN, "n"));
//		}

		StringTokenizer st1 = new StringTokenizer(patientIds1, ",");
		SortedSet s1 = new TreeSet();
		while (st1.hasMoreTokens()) {
			String id = st1.nextToken();
			s1.add(id);
		}

		for (Object idO : s1.toArray()) {
			String id = idO.toString();
			System.out.println("id: "+id);
			String label = "S1_" + id;//(ids1.size() + 1);
			ids1.put(id, label);
			columns.put(label, new ExportColumn(id, label, RATIO_PATTERN, "n"));
		}
		
		
		StringTokenizer st2 = new StringTokenizer(patientIds2, ",");
		while (st2.hasMoreTokens()) {
			String id = st2.nextToken();
			String label = "S2_" + id;//(ids2.size() + 1);
			ids2.put(id, label);
			columns.put(label, new ExportColumn(id, label, RATIO_PATTERN, "n"));
		}
	}
	
	public JSONObject toJSONObject() throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put("status", "ok");
		jsonObj.put("requestId", "0");
		
		JSONObject jsonTable = new JSONObject();
		
		JSONArray jsonColumns = new JSONArray();
		for (Iterator<ExportColumn> i = columns.values().iterator(); i.hasNext(); ) {
			jsonColumns.put(i.next().toJSONObject());
		}
				
		jsonTable.put("cols", jsonColumns);
		
		JSONArray jsonRows = new JSONArray();
		for (Iterator<ExportRow> i = rows.values().iterator(); i.hasNext(); ) {
			jsonRows.put(i.next().toJSONArray());
		}
		jsonTable.put("rows", jsonRows);
		
		jsonObj.put("table", jsonTable);
		jsonObj.put("signature", "1");
		
		return jsonObj;
	}

	public Collection<ExportColumn> getColumns() {
		return columns.values();
	}

	public Collection<ExportRow> getRows() {
		return rows.values();
	}

}
