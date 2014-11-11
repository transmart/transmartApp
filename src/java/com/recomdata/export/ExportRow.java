


package com.recomdata.export;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author Chris Uhrich
 * @version 1.0
 *          <p/>
 *          Copyright 2008 Recombinant Data Corp.
 */
public class ExportRow {
    private LinkedHashMap<String, ExportValue> values;


    public ExportRow(String gene, Set<String> columns) {
        values = new LinkedHashMap<String, ExportValue>();
        for (Iterator<String> i = columns.iterator(); i.hasNext(); ) {
            String columnName = i.next();
            if (columnName.equals("Gene")) {
                values.put(columnName, new ExportValue(gene));
            } else {
                values.put(columnName, new ExportValue(""));
            }
        }
    }

    public Collection<ExportValue> getValues() {
        return values.values();
    }

    public void put(String columnName, String value) {
        //System.out.println(columnName+" "+value);
        ExportValue hmv = values.get(columnName);
        hmv.setValue(value);
    }

    public JSONArray toJSONArray() throws JSONException {
        JSONArray json = new JSONArray();
        for (Iterator<ExportValue> i = values.values().iterator(); i.hasNext(); ) {
            json.put(i.next().toJSONObject());
        }
        return json;
    }
}
