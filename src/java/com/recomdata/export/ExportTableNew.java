


/**
 * $Id: ExportTableNew.java 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package com.recomdata.export;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class ExportTableNew {

    private LinkedHashMap<String, ExportColumn> columns = new LinkedHashMap<String, ExportColumn>();
    private LinkedHashMap<String, ExportRowNew> rows = new LinkedHashMap<String, ExportRowNew>();

    public ExportColumn getColumn(String columnname) {
        return columns.get(columnname);
    }

    public void putColumn(String columnname, ExportColumn column) {
        columns.put(columnname, column);
    }

    public boolean containsColumn(String columnname) {
        return columns.containsKey(columnname);
    }

    public ExportRowNew getRow(String rowname) {
        return rows.get(rowname);
    }

    public void putRow(String rowname, ExportRowNew row) {
        rows.put(rowname, row);
    }

    public boolean containsRow(String rowname) {
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

    public LinkedHashMap<String, ExportRowNew> getRowMap() {
        return rows;
    }

    //Supports ExtJS grid object
    public JSONObject toJSONObject() throws JSONException {
        JSONObject metadata = new JSONObject();
        JSONObject jsonTable = new JSONObject();
        JSONArray fields = new JSONArray();

        for (Iterator<ExportColumn> i = columns.values().iterator(); i.hasNext(); ) {
            fields.put(i.next().toJSONObject());
        }

        metadata.put("fields", fields);
        metadata.put("totalProperty", "results");
        metadata.put("root", "rows");
        metadata.put("id", "subject");

        jsonTable.put("metaData", metadata);
        jsonTable.put("results", rows.size());
        JSONArray jsonRows = new JSONArray();
        for (Iterator<ExportRowNew> i = rows.values().iterator(); i.hasNext(); ) {
            jsonRows.put(i.next().toJSONObject());
        }
        jsonTable.put("rows", jsonRows);
        return jsonTable;
    }

    //Supports jQuery datatables object
    public JSONObject toJSON_DataTables(String echo, String title) throws JSONException {


        JSONObject jsonTable = toJSON_DataTables(echo);
        jsonTable.put("iTitle", title);

        return jsonTable;
    }

    //Supports jQuery datatables object
    public JSONObject toJSON_DataTables(String echo) throws JSONException {

        JSONObject jsonTable = new JSONObject();
        JSONArray aoColumns = new JSONArray();
        JSONArray headerToolTips = new JSONArray();

        for (Iterator<ExportColumn> i = columns.values().iterator(); i.hasNext(); ) {
            ExportColumn col = i.next();
            aoColumns.put(col.toJSON_DataTables());
            headerToolTips.put(col.getId());
        }

        JSONArray jsonRows = new JSONArray();
        for (Iterator<ExportRowNew> i = rows.values().iterator(); i.hasNext(); ) {
            jsonRows.put(i.next().toJSONArray());
        }

        if (echo != null) jsonTable.put("sEcho", echo);
        jsonTable.put("iTitle", "Title");
        jsonTable.put("iTotalRecords", rows.size());
        jsonTable.put("iTotalDisplayRecords", rows.size());
        jsonTable.put("aoColumns", aoColumns);
        jsonTable.put("headerToolTips", headerToolTips);

        jsonTable.put("aaData", jsonRows);

        return jsonTable;
    }

    public JSONObject getJSONColumns() throws JSONException {

        JSONObject jsonColumns = new JSONObject();
        JSONArray columnsAry = new JSONArray();

        for (Iterator<ExportColumn> i = columns.values().iterator(); i.hasNext(); ) {
            columnsAry.put(i.next().toJSON_DataTables());
        }

        jsonColumns.put("aoColumns", columnsAry);
        return jsonColumns;
    }


    public byte[] toCSVbytes() {
        byte[] table = null;
        ArrayList<String> newheaders = new ArrayList<String>();
        ArrayList<List> newrows = new ArrayList<List>();

        //copy from linked hash map of objects  to list of strings
        for (Iterator<ExportColumn> i = columns.values().iterator(); i.hasNext(); ) {
            newheaders.add(i.next().getId());
        }
        //copy from linked hash map of rows into list of list of rows
        for (Iterator<ExportRowNew> i = rows.values().iterator(); i.hasNext(); ) {
            ArrayList<String> row = new ArrayList<String>();
            for (Iterator<String> z = i.next().getValues().iterator(); z.hasNext(); ) {
                row.add(z.next().toString());
            }
            newrows.add(row);
        }
        System.out.println("********" + rows.size());

        table = CSVGenerator.generateCSV(newheaders, newrows);
        return table;
    }
}
