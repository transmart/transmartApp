


package com.recomdata.export;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Chris Uhrich
 * @version 1.0
 *          <p/>
 *          Copyright 2008 Recombinant Data Corp.
 */
public class HeatMapColumn {
    private String id;
    private String label;
    private String pattern;
    private String type;

    public HeatMapColumn(String id, String label, String pattern, String type) {
        this.id = id;
        this.label = label;
        this.pattern = pattern;
        this.type = type;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("label", label);
        json.put("pattern", pattern);
        json.put("type", type);
        return json;
    }
}
