


package com.recomdata.export;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Chris Uhrich
 * @version 1.0
 *          <p/>
 *          Copyright 2008 Recombinant Data Corp.
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
        return (hmv.getValue());
    }


    public JSONArray toJSONArray() throws JSONException {
        JSONArray json = new JSONArray();
        for (Iterator<HeatMapValue> i = values.values().iterator(); i.hasNext(); ) {
            json.put(i.next().toJSONObject());
        }
        return json;
    }
}
