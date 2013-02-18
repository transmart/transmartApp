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
import java.util.logging.Logger;

import org.json.*;

import java.util.TreeMap;

/**
 * 
 * @author Chris Uhrich
 * @version 1.0
 * 
 * Copyright 2008 Recombinant Data Corp.
 */
public class HeatMapRow {
	//private LinkedHashMap<String, HeatMapValue> values;
	private TreeMap<String, HeatMapValue> values;
	
	public HeatMapRow(String gene, Set<String> columns) {
		// values = new LinkedHashMap<String, HeatMapValue>();
		values = new TreeMap<String, HeatMapValue>();
		for (Iterator<String> i = columns.iterator(); i.hasNext(); ) {
			String columnName = i.next();
			if (columnName.equals("Gene")) {
				values.put(columnName, new HeatMapValue(gene));
			} else {
				values.put(columnName, new HeatMapValue(""));
			}
		}
	}
	
	public void put(String columnName, String value) {
		//System.out.println(columnName+" "+value);
		HeatMapValue hmv = values.get(columnName); 
		hmv.setValue(value);
	}

	public String get(String columnName) {
		//System.out.println(columnName+" "+value);
		HeatMapValue hmv = values.get(columnName); 
		return(hmv.getValue());
	}

	
	public JSONArray toJSONArray() throws JSONException {
		JSONArray json = new JSONArray();
		for (Iterator<HeatMapValue> i = values.values().iterator(); i.hasNext(); ) {
			json.put(i.next().toJSONObject());
		}
		return json;
	}
}
