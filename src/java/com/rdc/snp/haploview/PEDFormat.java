


package com.rdc.snp.haploview;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;

public class PEDFormat {
    static String tblname = "haploview_data";
    Util utl = new Util();

    public PEDFormat() {
    }

    /**
     * @param genes list of genes in the same chromosome
     * @param pids  list of i2b2 patient ids
     * @param file  a file handle to indicate where the file should be written,
     *              if the file = null, then the PED file will be written to a default
     *              place
     * @param stmt  if stmt=null, then the default will e used
     * @return
     * @throws SQLException
     * @throws SQLException 2009-06-21: change return "void" to "boolean"
     */
    public boolean createPEDFile(String genes, String pids, String file, Connection conn)
            throws SQLException {

        // if isOk=true, display Halploview, otherwise don't display
        boolean isOk = false;

        Statement stmt;
        try {
            stmt = conn.createStatement();
        } catch (Exception e) {
            conn = utl.createDefaultConnection();
            stmt = conn.createStatement();
        }

        Hashtable<String, String> ht = new Hashtable<String, String>();
        ht = PEDOutput(stmt, genes, pids);

        File pedFile;
        File infoFile;

        if (file.equals("")) {
            pedFile = new File("C:/transmart.ped");
            infoFile = new File("C:/transmart.info");
        } else {
            String f1 = file + ".ped";
            String f2 = file + ".info";
            pedFile = new File(f1);
            infoFile = new File(f2);
        }
        utl.writeHashtableToFile(ht, pedFile);

        isOk = PEDInfoOutput(stmt, infoFile, genes);

        return isOk;
    }

    /**
     * @param stmt
     * @param hdr
     * @return
     * @throws SQLException
     */
    ArrayList<String> PEDOutput(Statement stmt, String[] hdr)
            throws SQLException {
        String SNP_per_line = "";
        ArrayList<String> line = new ArrayList<String>();

        String query = "select * from T03_PED";
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            SNP_per_line = rs.getString("SUBJECT_ID") + " " +
                    rs.getString("FATHER_ID") + " " +
                    rs.getString("MOTHER_ID") + " " +
                    rs.getString("SEX") + " " +
                    rs.getString("AFFECTION_STATUS") + " ";

            //log.trace(SNP_per_line);
            for (int i = 0; i < hdr.length; i++) {

                if (!hdr[i].equals("SUBJECT_ID") &&
                        !hdr[i].equals("FATHER_ID") &&
                        !hdr[i].equals("MOTHER_ID") &&
                        !hdr[i].equals("SEX") &&
                        !hdr[i].equals("AFFECTION_STATUS")) {
                    SNP_per_line = SNP_per_line + rs.getString(hdr[i]) + " ";
                }
            }
            line.add(SNP_per_line);
            //log.trace(SNP_per_line);
            SNP_per_line = "";
        }
        return line;
    }

    /**
     * @param stmt
     * @param genes a group of gene names in csv format
     * @param pids  a group of i2b2 patient ids in csv format
     * @return
     * @throws SQLException
     */
    Hashtable<String, String> PEDOutput(Statement stmt, String listGenes, String pids)
            throws SQLException {
        Hashtable<String, String> ht;
        ht = new Hashtable<String, String>();

        Hashtable<String, String> ht1;
        ht1 = new Hashtable<String, String>();

        Hashtable<String, String> ht2;
        ht2 = new Hashtable<String, String>();

        String[] ids = null;
        ids = pids.split(",");

        String[] genes = null;
        genes = listGenes.split(",");

        //Util utl = new Util();

        for (int i = 0; i < ids.length; i++) {
            String pid = utl.removeSpaces(ids[i]);
            ht1 = getCommonData(stmt, pid);
            utl.copyHastable(ht1, ht);
            //utl.printHastable(ht1);

            for (int j = 0; j < genes.length; j++) {
                String gene = utl.removeSpaces(genes[j]);
                ht2 = getSNPDataByGeneAndSubject(stmt, gene, pid);
                utl.mergeHastable(ht2, ht);
                //utl.printHastable(ht2);
            }
            //utl.printHastable(ht);
        }

        //log.trace("HT Size: " + ht.size());
        return ht;
    }

    /**
     * @param stmt
     * @param genes a group of gene names in csv format
     * @param pids  a group of i2b2 patient ids in csv format
     * @return
     * @throws SQLException 2009-06-22: change return "viod" to "boolean"
     */
    boolean PEDInfoOutput(Statement stmt, File file, String listGenes)
            throws SQLException {
        String[] genes = null;
        String[] info = null;
        ArrayList<String> al = new ArrayList<String>();

        // if info.length >10, set isOk = true, so Haploview will be displayed,
        // otherwise, don't display it
        boolean isOk = false;

        genes = listGenes.split(",");
        for (int j = 0; j < genes.length; j++) {
            String gene = utl.removeSpaces(genes[j]);
            //info = PEDInfoByGene(stmt, gene);
            info = PEDInfoByGeneNew(stmt, gene);

            for (int i = 0; i < info.length; i++) {
                al.add(info[i]);
            }
        }

        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            if (al.size() >= 10) {
                isOk = true;
            }
            for (int j = 0; j < al.size(); j++) {
                out.write(al.get(j) + "\n");
            }
            out.close();
        } catch (IOException e) {
            //log.trace("Exception ");
        }

        return isOk;
    }

    /**
     * @param stmt
     * @param gene gene name
     * @return
     * @throws SQLException
     */
    String[] PEDInfoByGene(Statement stmt, String gene)
            throws SQLException {
        ResultSet rs;
        String query = null;

        String[] snps = null;
        query = "select snp_data from haploview_data " +
                "where i2b2_id=0 and jnj_id='0' and gene='" + gene + "'";
        rs = stmt.executeQuery(query);
        //log.trace(query);
        while (rs.next()) {
            //log.trace(gene + ":  " + rs.getString(1));
            snps = rs.getString(1).split(",");
        }

        String[] snpInfo;
        snpInfo = new String[snps.length];

        for (int j = 0; j < snps.length; j++) {
            String snp = utl.removeSpaces(snps[j]);
            query = "select snp, position from snp_info " +
                    "where gene='" + gene + "' and snp='" + snp + "'";
            rs = stmt.executeQuery(query);

            while (rs.next()) {
                snpInfo[j] = rs.getString(1) + "   " + rs.getLong(2);
                //log.trace(rs.getString(1) + "  " + rs.getLong(2));
            }
        }
        return snpInfo;
    }

    /**
     * @param stmt
     * @param gene gene name
     * @return
     * @throws SQLException
     */
    String[] PEDInfoByGeneNew(Statement stmt, String gene)
            throws SQLException {
        ResultSet rs;
        String query = null;
        String[] snpInfo = null;

        String[] snps = null;
        String[] positions = null;

      	  
      	  /* In order to make SNP name shorter, use position to replace SNP name
             * in *.info file
      	   *  
      	  query = " select t1.snp_data, t2.snp_data " +
      	  		  " from haploview_data t1, haploview_data t2 " +
      	 		  " where t1.i2b2_id=0 and t1.jnj_id='0' and t1.gene=t2.gene and " +
      	 		  "      t2.i2b2_id=0 and t2.jnj_id='2' and t2.gene='" + gene + "'";
      	  */
        query = "select snp_data, snp_data from haploview_data " +
                " where i2b2_id=0 and jnj_id='2' and gene='" + gene + "'";

        rs = stmt.executeQuery(query);
        while (rs.next()) {
            //log.trace(rs.getString(1) + " : " + rs.getString(2));
            snps = rs.getString(1).split(",");
            positions = rs.getString(2).split(",");
        }

        snpInfo = new String[snps.length];
        if (snps.length != positions.length) {
            snpInfo[0] = "Mismatching between SNP names and their position";
        } else {
            for (int j = 0; j < snps.length; j++) {
                //log.trace(snps[j] + " : " + positions[j]);
                String snp = utl.removeSpaces(snps[j]);
                String pos = utl.removeSpaces(positions[j]);
                snpInfo[j] = snp + "      " + pos;
            }
        }
        return snpInfo;
    }


    /**
     * Using rs#
     *
     * @param stmt
     * @param gene gene name
     * @return
     * @throws SQLException
     */
    String[] PEDInfoByGene2(Statement stmt, String gene)
            throws SQLException {
        ResultSet rs;
        String query = null;
        String[] snpInfo = null;

        String[] snps = null;
        String[] positions = null;
       	  
       	  
       	  /* In order to make SNP name shorter, use position to replace SNP name
       	   * in *.info file
       	   *  
       	  query = " select t1.snp_data, t2.snp_data " +
       	  		  " from haploview_data t1, haploview_data t2 " +
       	 		  " where t1.i2b2_id=0 and t1.jnj_id='0' and t1.gene=t2.gene and " +
       	 		  "      t2.i2b2_id=0 and t2.jnj_id='2' and t2.gene='" + gene + "'";
       	  */
        query = "select snp_data, snp_data from haploview_data " +
                " where i2b2_id=0 and jnj_id='2' and gene='" + gene + "'";

        rs = stmt.executeQuery(query);
        while (rs.next()) {
            //log.trace(rs.getString(1) + " : " + rs.getString(2));
            snps = rs.getString(1).split(",");
            positions = rs.getString(2).split(",");
        }

        snpInfo = new String[snps.length];
        if (snps.length != positions.length) {
            snpInfo[0] = "Mismatching between SNP names and their position";
        } else {
            for (int j = 0; j < snps.length; j++) {
                //log.trace(snps[j] + " : " + positions[j]);
                String snp = utl.removeSpaces(snps[j]);
                String pos = utl.removeSpaces(positions[j]);
                snpInfo[j] = snp + "      " + pos;
            }
        }
        return snpInfo;
    }


    /**
     * @param stmt
     * @return
     * @throws SQLException
     */
    Hashtable<String, String> getCommonData(Statement stmt, String pid)
            throws SQLException {
        Hashtable<String, String> ht;
        ht = new Hashtable<String, String>();

        String query = "select distinct i2b2_id, jnj_id, father_id, mother_id," +
                "       sex, affection_status" +
                " from " + tblname +
                " where i2b2_id!=0 ";

        if (!pid.equals("")) {
            query += " and i2b2_id=" + pid;
        }
        query += " order by 1";

        //log.trace(query);

        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            String val = rs.getString(1);
            val += "  " + rs.getString(2);
            val += "  " + rs.getString(3);
            val += "  " + rs.getString(4);
            val += "  " + rs.getString(5);
            val += "  " + rs.getString(6);
            ht.put(rs.getString(1), val);
        }
        return ht;
    }

    /**
     * @param stmt
     * @param gene a gene name
     * @param pid  an i2b2 patient ids
     * @return
     * @throws SQLException
     */
    Hashtable<String, String> getSNPDataByGeneAndSubject(Statement stmt,
                                                         String gene, String pid)
            throws SQLException {
        Hashtable<String, String> ht;
        ht = new Hashtable<String, String>();

        // remove distinct from select
        String query = "select i2b2_id, snp_data" +
                " from " + tblname +
                " where i2b2_id!=0 ";

        if (!pid.equals("")) {
            query += " and i2b2_id=" + pid;
        }

        if (!gene.equals("")) {
            query += " and upper(gene)='" + gene.toUpperCase() + "' ";
        }

        query += " order by 1";

        //log.trace("2: " + query);

        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            // extract CLOB instead of String
            Clob cl = rs.getClob(2);
            String snpVal = cl.getSubString(1, (int) cl.length());
            //ht.put(rs.getString(1), rs.getString(2));
            ht.put(rs.getString(1), snpVal);
        }
        return ht;
    }


    /**
     * @param stmt
     * @param gene
     * @return
     * @throws SQLException
     */

    Hashtable<String, String> getSNPDataByGene(Statement stmt, String gene)
            throws SQLException {
        String pid = "";
        return getSNPDataByGeneAndSubject(stmt, gene, pid);
    }

    /**
     * @param stmt
     * @param gene
     * @return
     * @throws SQLException
     */

    Hashtable<String, String> getSNPDataBySubject(Statement stmt, String pid)
            throws SQLException {
        String gene = "";
        return getSNPDataByGeneAndSubject(stmt, gene, pid);
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

    String[] setPEDHeader(Statement stmt) throws SQLException {
        String[] PEDHeader;

        PEDFormat ped = new PEDFormat();
        int size = ped.getPEDColumnCount(stmt);

        PEDHeader = new String[size];

        int index = 0;
        // set column header for each subject
        String query = "select column_name from user_tab_columns " +
                "where table_name='T03_PED'";
        //              "   column_name not in ('SUBJECT_ID','FATHER_ID','MOTHER_ID','SEX','AFFECTION_STATUS')";

        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            PEDHeader[index] = rs.getString(1);
            index++;
            //log.trace(index + " : " + rs.getString(1));
        }

        return PEDHeader;
    }

    /**
     * @param stmt
     * @return
     * @throws SQLException
     */

    int getPEDColumnCount(Statement stmt) throws SQLException {
        int total = 0;
        String query = "select count(*) from user_tab_columns " +
                "where table_name='T03_PED'";

        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            total = rs.getInt(1);
        }

        //log.trace("Total: " + total);
        return total;
    }
}
