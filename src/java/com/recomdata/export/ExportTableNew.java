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
 * $Id: ExportTableNew.java 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package com.recomdata.export;

import java.util.*;
import org.json.*;

public class ExportTableNew {
    
	private LinkedHashMap<String, ExportColumn> columns = new LinkedHashMap<String, ExportColumn>();
	private LinkedHashMap<String, ExportRowNew> rows = new LinkedHashMap<String, ExportRowNew>();
	
	public ExportColumn getColumn(String columnname)
	{
		return columns.get(columnname);
	}
	public void putColumn(String columnname, ExportColumn column)
	{
		columns.put(columnname, column);
	}
	public boolean containsColumn(String columnname)
	{
		return columns.containsKey(columnname);
	}
	public ExportRowNew getRow(String rowname)
	{
		return rows.get(rowname);
	}
	public void putRow(String rowname, ExportRowNew row)
	{
		rows.put(rowname, row);
	}
	public boolean containsRow(String rowname)
	{
		return rows.containsKey(rowname);
	}
	public Collection<ExportColumn> getColumns() {
		return columns.values();
	}
	public Collection<ExportRowNew> getRows() {
		return rows.values();
	}
	public LinkedHashMap<String, ExportColumn> getColumnMap() {
		return columns;
	}
	public  LinkedHashMap<String, ExportRowNew> getRowMap() {
		return rows;
	}

	public JSONObject toJSONObject() throws JSONException {
		JSONObject metadata = new JSONObject();		
		JSONObject jsonTable = new JSONObject();	
		JSONArray fields = new JSONArray();
		
		for (Iterator<ExportColumn> i = columns.values().iterator(); i.hasNext(); ) {
			fields.put(i.next().toJSONObject());
		}
		
		metadata.put("fields", fields);
		metadata.put("totalProperty","results");
		metadata.put("root", "rows");
		metadata.put("id", "subject");
		
		jsonTable.put("metaData",metadata);
		jsonTable.put("results",rows.size());
		JSONArray jsonRows = new JSONArray();
		for (Iterator<ExportRowNew> i = rows.values().iterator(); i.hasNext(); ) {
			jsonRows.put(i.next().toJSONObject());
		}
		jsonTable.put("rows", jsonRows);
		return jsonTable;
	}
	public byte[] toCSVbytes()
	{
		byte[] table=null;
		ArrayList<String> newheaders=new ArrayList<String>();
		ArrayList<List>	  newrows=new ArrayList<List>();

		//copy from linked hash map of objects  to list of strings
		for (Iterator<ExportColumn> i = columns.values().iterator(); i.hasNext(); ) {
			newheaders.add(i.next().getId());
		}
		//copy from linked hash map of rows into list of list of rows
		for (Iterator<ExportRowNew> i = rows.values().iterator(); i.hasNext(); ) {
			 ArrayList<String> row =new ArrayList<String>();
			 for(Iterator<String> z=i.next().getValues().iterator(); z.hasNext();){
				 row.add(z.next().toString());}
			 newrows.add(row);
		}
		System.out.println("********"+rows.size());
			
		table=CSVGenerator.generateCSV(newheaders, newrows);
		return table;
	}
}
