


package com.recomdata.export;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Chris Uhrich
 * @version 1.0
 *          <p/>
 *          Copyright 2008 Recombinant Data Corp.
 */
public class ExportColumn {
    private String id;
    private String label;
    private String pattern;
    private String type;
    private Integer width;

    public ExportColumn(String id, String label, String pattern, String type) {
        this.id = id;
        this.label = label;
        this.pattern = pattern;
        this.type = type;
        this.width = -1;
    }

    public ExportColumn(String id, String label, String pattern, String type, Integer width) {
        this.id = id;
        this.label = label;
        this.pattern = pattern;
        this.type = type;
        this.width = width;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", id);
        json.put("header", label);
        json.put("sortable", true);
        json.put("width", 50);
        return json;
    }

    public JSONObject toJSON_DataTables() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("sTitle", label);

        String typeOut;
        if (type != null) {
            if (type == "Number")
                typeOut = "numeric";
            else
                typeOut = "string";

            json.put("sType", typeOut);
        }
        /*
        if (width > 0)
		{
			json.put("sWidth", width.toString() + "px");
		}
		else
		{
			json.put("sWidth", "100px");
		}*/

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

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer height) {
        this.width = width;
    }
}
