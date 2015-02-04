


package com.recomdata.etl.util;

import java.io.*;

public class RBMFilePivot {

    public static void main(String[] args) {
        pivotData(7,
                "C:\\temp\\c0743t10_cleaned_PDMarkers.txt",
                "C:\\temp\\c0743t10_pivotted_PDMarkers.txt");
    }

    private static void pivotData(
            int pivotStartPosition,
            String dataFile,
            String outputFile) {
        File mapfile = new File(dataFile);
        String filename = outputFile;
        BufferedReader reader = null;
        BufferedWriter writer = null;


        try {
            reader = new BufferedReader(new FileReader(mapfile));
            writer = new BufferedWriter(new FileWriter(filename));
            String line = "";
            int lineRead = 6000;
            int count = 0;
            String[] headerArray = new String[pivotStartPosition + 2];
            String[] antigenArray = new String[200];

            while ((line = reader.readLine()) != null && count < lineRead) {
                String[] tokens = line.split("\t");

                if (count == 0) {
                    int colCount = 0;
                    for (String token : tokens) {
                        if (colCount < pivotStartPosition) {
                            headerArray[colCount] = token;
                        } else if (colCount == pivotStartPosition) {
                            headerArray[colCount] = "Antigen";
                            headerArray[colCount + 1] = "Value";
                            antigenArray[colCount] = token;
                        } else {

                            antigenArray[colCount] = token;
                            //	antCount++;
                        }
                        colCount++;
                    }

                    //System.out.println(subjectMap);
                    for (int i = 0; i < headerArray.length; i++) {
                        if (i > 0)
                            writer.write("\t");
                        writer.write(headerArray[i]);

                    }
                    writer.newLine();
                } else {
                    int cCount = 0;
                    StringBuilder comColumns = new StringBuilder();
                    String colStr = null;
                    for (String token : tokens) {

                        if (cCount < pivotStartPosition) {
                            comColumns.append(token).append("\t");
                            colStr = comColumns.toString();
                        } else {
                            writer.write(colStr);
                            writer.write(antigenArray[cCount]);
                            writer.write("\t");
                            writer.write(token);
                            writer.newLine();
                        }
                        cCount++;
                    }
                }
                count++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

    }
}
