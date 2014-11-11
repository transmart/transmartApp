


package com.recomdata.export;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;
import java.util.*;

/**
 * @author Chris Uhrich
 * @version 1.0
 *          <p/>
 *          Copyright 2008 Recombinant Data Corp.
 */
public class ExportTable {

    private static final String RATIO_PATTERN = "##0.######";
    private HashMap<String, String> ids1 = new HashMap<String, String>();
    private HashMap<String, String> ids2 = new HashMap<String, String>();
    private LinkedHashMap<String, ExportColumn> columns = new LinkedHashMap<String, ExportColumn>();
    private LinkedHashMap<String, ExportRow> rows = new LinkedHashMap<String, ExportRow>();

    public ExportTable(List<ExportResult> rs1, String patientIds1, List<ExportResult> rs2, String patientIds2) throws SQLException {
        setColumns(patientIds1, patientIds2);
        for (ExportResult e : rs1) {
            String gene = e.getConcept();
            String patientId = e.getSubject();
            String value = e.getValue();
            String columnName = ids1.get(patientId);

            ExportRow row;
            if (!rows.containsKey(gene)) {
                row = new ExportRow(gene, columns.keySet());
                rows.put(gene, row);
            } else {
                row = rows.get(gene);
            }
            row.put(columnName, value);
        }

        for (ExportResult e : rs2) {
            String gene = e.getConcept();
            String patientId = e.getSubject();
            String value = e.getValue();
            String columnName = ids2.get(patientId);

            ExportRow row;
            if (!rows.containsKey(gene)) {
                row = new ExportRow(gene, columns.keySet());
                rows.put(gene, row);
            } else {
                row = rows.get(gene);
            }
            row.put(columnName, value);
        }
    }

    public void setColumns(String patientIds1, String patientIds2) {
        columns.put("Gene", new ExportColumn("Gene", "Gene", "", "t"));

//		StringTokenizer st1 = new StringTokenizer(patientIds1, ",");
//		while (st1.hasMoreTokens()) {
//			String id = st1.nextToken();
//			String label = "S1_" + id;//(ids1.size() + 1);
//			ids1.put(id, label);
//			columns.put(label, new ExportColumn(id, label, RATIO_PATTERN, "n"));
//		}

        StringTokenizer st1 = new StringTokenizer(patientIds1, ",");
        SortedSet s1 = new TreeSet();
        while (st1.hasMoreTokens()) {
            String id = st1.nextToken();
            s1.add(id);
        }

        for (Object idO : s1.toArray()) {
            String id = idO.toString();
            System.out.println("id: " + id);
            String label = "S1_" + id;//(ids1.size() + 1);
            ids1.put(id, label);
            columns.put(label, new ExportColumn(id, label, RATIO_PATTERN, "n"));
        }


        StringTokenizer st2 = new StringTokenizer(patientIds2, ",");
        while (st2.hasMoreTokens()) {
            String id = st2.nextToken();
            String label = "S2_" + id;//(ids2.size() + 1);
            ids2.put(id, label);
            columns.put(label, new ExportColumn(id, label, RATIO_PATTERN, "n"));
        }
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("status", "ok");
        jsonObj.put("requestId", "0");

        JSONObject jsonTable = new JSONObject();

        JSONArray jsonColumns = new JSONArray();
        for (Iterator<ExportColumn> i = columns.values().iterator(); i.hasNext(); ) {
            jsonColumns.put(i.next().toJSONObject());
        }

        jsonTable.put("cols", jsonColumns);

        JSONArray jsonRows = new JSONArray();
        for (Iterator<ExportRow> i = rows.values().iterator(); i.hasNext(); ) {
            jsonRows.put(i.next().toJSONArray());
        }
        jsonTable.put("rows", jsonRows);

        jsonObj.put("table", jsonTable);
        jsonObj.put("signature", "1");

        return jsonObj;
    }

    public Collection<ExportColumn> getColumns() {
        return columns.values();
    }

    public Collection<ExportRow> getRows() {
        return rows.values();
    }

}
