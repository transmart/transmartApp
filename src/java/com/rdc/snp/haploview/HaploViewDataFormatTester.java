


package com.rdc.snp.haploview;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Hashtable;

public class HaploViewDataFormatTester {
    public HaploViewDataFormatTester() {
    }

    /**
     * @param args
     */
    public static void main(String args[]) throws SQLException {
        ArrayList<String> hmpLine = new ArrayList<String>();
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());

        Connection conn = DriverManager.getConnection
                ("jdbc:oracle:thin:@machineName:port:SID", "userid", "password");

        Statement stmt = conn.createStatement();

        PEDFormat ped;
        ped = new PEDFormat();
        int num = ped.getPEDColumnCount(stmt);
        String[] hdr = ped.setPEDHeader(stmt);
        //ArrayList<String> al = ped.PEDOutput(stmt, hdr);

        String genes = "ADAM17, IL4R, IL13, STAT6, TEST";
        String ids = "143,144,145";

        Hashtable<String, String> ht;
        ht = new Hashtable<String, String>();
        ht = ped.PEDOutput(stmt, genes, ids);

        Util utl = new Util();
        utl.printHastable(ht);
        File file = new File("C:/HaploView/Transmart.ped");
        utl.writeHashtableToFile(ht, file);

        // Test case for Jeremy
        Connection con = null;
        String f = "";
        ped.createPEDFile(genes, ids, f, con);

		/*
        HMP hmp = new HMP();
		 
		String [] header = hmp.setHMPHeader(stmt);
		String hdr = hmp.printHMPHeader(header);
		 
		hmpLine = hmp.HMPbyChromosome(stmt, "chr10", header);
		//hmpLine = hmp.HMPbyGene(stmt, "ADAM17", header);
		*/
		/* 
        
		try {
			 BufferedWriter out = new BufferedWriter(new FileWriter("C:/HaploView/Transmart.ped"));
			 //out.write(hdr + "\n");
			 for(int i=0; i<al.size(); i++){
				 out.write(al.get(i) + "\n");
			 }
			 out.close();
			 }
			 catch (IOException e)
			 {
				 log.error("Exception ");
			 }
*/
        stmt.close();
    }
}
