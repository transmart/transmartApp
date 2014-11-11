


package com.rdc.snp.haploview;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class HMPFormat {
    public HMPFormat() {
    }

    /**
     * @param stmt
     * @param chr
     * @param header
     * @return
     * @throws SQLException
     */
    ArrayList<String> HMPbyChromosome(Statement stmt, String chr, String[] header)
            throws SQLException {
        String SNP_per_line = "";
        ArrayList<String> line = new ArrayList<String>();
        String query = "select * from T03_HMP where chrom='" + chr + "'";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            for (int i = 0; i < header.length; i++) {
                SNP_per_line = SNP_per_line + rs.getString(header[i]) + " ";
            }
            line.add(SNP_per_line);
            //log.trace(SNP_per_line);
            SNP_per_line = "";
        }
        return line;
    }


    /**
     * @param stmt
     * @param gene
     * @param header
     * @return
     * @throws SQLException
     */

    ArrayList<String> HMPbyGene(Statement stmt, String gene, String[] header)
            throws SQLException {
        String SNP_per_line = "";
        ArrayList<String> line = new ArrayList<String>();

        String query = "select * from T03_HMP where SNP_ID in " +
                " (select SNP from SNP_Mapping where gene='" + gene + "')";

        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            for (int i = 0; i < header.length; i++) {
                SNP_per_line = SNP_per_line + rs.getString(header[i]) + " ";
            }

            line.add(SNP_per_line);
            //log.trace(SNP_per_line);
            SNP_per_line = "";
        }
        return line;
    }

    /**
     * @param header
     * @return
     */
    String printHMPHeader(String[] header) {
        String hdr = "";
        int size = header.length;

        for (int i = 0; i < size - 1; i++) {
            if (i < 10)
                hdr = hdr + header[i] + " ";
            else
                hdr = hdr + "NA010" + i + " ";
            //System.out.print(header[i] + " ");
        }
        hdr = hdr + "NA010" + size;
        return hdr;
    }


    /**
     * @param stmt
     * @return
     * @throws SQLException
     */
    String[] setHMPHeader(Statement stmt) throws SQLException {
        String[] HMPHeader;
        HMPFormat hmp = new HMPFormat();
        int size = hmp.getSubjectCount(stmt);

        HMPHeader = new String[size + 10];
        HMPHeader[0] = "rs#";
        HMPHeader[1] = "alleles";
        HMPHeader[2] = "chrom";
        HMPHeader[3] = "pos";
        HMPHeader[4] = "strand";
        HMPHeader[5] = "assembly#";
        HMPHeader[6] = "center";
        HMPHeader[7] = "protLSID";
        HMPHeader[8] = "assayLSID";
        HMPHeader[9] = "panelLSID";

        int index = 10;

        // set column header for each subject
        String query = "select column_name from user_tab_columns " +
                "where table_name='T03_HMP' and column_name like 'N%'";

        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            String cname = rs.getString(1);
            if (!cname.equals("SNP_ID")) {
                HMPHeader[index] = cname;
                index++;
                //log.trace(index + " : " + cname + " ");
            }
        }
        return HMPHeader;
    }


    /**
     * @param stmt
     * @return
     * @throws SQLException
     */

    int getSubjectCount(Statement stmt) throws SQLException {
        int total = 0;
        String query = "select count(*) from user_tab_columns " +
                "where table_name='T03_HMP' and column_name like 'N%'";

        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            total = rs.getInt(1);
        }

        //log.trace("Total: " + total);
        return total;
    }
}
