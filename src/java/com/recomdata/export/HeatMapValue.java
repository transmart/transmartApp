


package com.recomdata.export;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Chris Uhrich
 * @version 1.0
 *          <p/>
 *          Copyright 2008 Recombinant Data Corp.
 */
public class HeatMapValue {
    private String value;

    public HeatMapValue(String value) {
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return (this.value);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("f", value);
        json.put("v", value);
        return json;
    }
}
