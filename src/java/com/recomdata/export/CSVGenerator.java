


package com.recomdata.export;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CSVGenerator {

    public static byte[] generateCSV(ArrayList<String> headers, ArrayList<List> values) {
        FileWriter csvWriter = null;
        String columnNames = "";
        String rowValues = "";
        String export = "";
        try {
            csvWriter = new FileWriter("export.csv");

            for (int i = 0; i < headers.size(); i++)
                columnNames = columnNames + headers.get(i) + ",";
            columnNames = columnNames.substring(0, columnNames.length() - 1);

            //csvWriter.write(columnNames+"\n");
            //System.out.print(headers.get(i)+",");
            export = export + columnNames + "\n";

            for (int i = 0; i < values.size(); i++) {
                rowValues = "";
                for (int j = 0; j < values.get(i).size(); j++)
                    rowValues = rowValues + "\"" + values.get(i).get(j).toString().replace("\"", "\"\"") + "\"" + ",";
                //System.out.print(values.get(i).get(j)+",");
                rowValues = rowValues.substring(0, rowValues.length() - 1);
                export = export + rowValues + "\n";
                //csvWriter.write(rowValues+"\n");
                //System.out.println();

                //csvWriter.flush();
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                csvWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return (export.getBytes());
    }
}
