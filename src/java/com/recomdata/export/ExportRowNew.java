


package com.recomdata.export;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author Chris Uhrich
 * @version 1.0
 *          <p/>
 *          Copyright 2008 Recombinant Data Corp.
 */
public class ExportRowNew {
    private LinkedHashMap<String, String> values;


    public ExportRowNew() {
        values = new LinkedHashMap<String, String>();
    }

    public Collection<String> getValues() {
        return values.values();
    }

    public boolean containsColumn(String columnname) {
        return values.containsKey(columnname);
    }

    public void put(String columnName, String value) {
        //System.out.println(columnName+" "+value);
        if (value == null) {
            value = "NULL";
        }
        values.put(columnName, value);
    }

    public String get(String columnname) {
        return values.get(columnname);
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        for (Iterator<String> i = values.keySet().iterator(); i.hasNext(); ) {
            String column = i.next();
            json.put(column, values.get(column));
        }
        return json;
    }

    public JSONArray toJSONArray() throws JSONException {

        JSONArray jsonArray = new JSONArray();

        for (Iterator<String> i = values.keySet().iterator(); i.hasNext(); ) {
            String column = i.next();
            jsonArray.put(values.get(column));
        }
        return jsonArray;
    }
}
