


package com.recomdata.etl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TableColumnMessageGenerator {

    public static void main(String[] args) {

        updategene();
    }

    private static void updategene() {
        File mapfile = new File("C:\\updateids.csv");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(mapfile));
            String line = "";

            while ((line = reader.readLine()) != null) {
                System.out
                        .print("UPDATE GeneExpressionAnalysis SET Ratio = -Ratio WHERE id = "
                                + line + ";\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    @SuppressWarnings("unused")
    private void columnMessage() {
        File mapfile = new File("C:\\temp\\JubilantColumnMapping.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(mapfile));
            String line = "";

            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split("\t");
                System.out.println(tokens[0] + "."
                        + tokens[1].replace("_", "").toLowerCase() + "="
                        + tokens[2]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
        }

    }

}
