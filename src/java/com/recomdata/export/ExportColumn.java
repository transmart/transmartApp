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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author Chris Uhrich
 * @version 1.0
 * 
 * Copyright 2008 Recombinant Data Corp.
 */
public class ExportColumn {
	private String id;
	private String label;
	private String pattern;
	private String type;
	
	public ExportColumn(String id, String label, String pattern, String type) {
		this.id = id;
		this.label = label;
		this.pattern = pattern;
		this.type = type;
	}
	
	public JSONObject toJSONObject() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", id);
		json.put("header", label);
		json.put("sortable", true);
		json.put("width", 50);
		return json;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
