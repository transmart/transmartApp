


package com.rdc.snp.haploview;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class Util {

    public static void main(String[] arg) {
        Hashtable<String, String> ht;
        ht = new Hashtable<String, String>();
        ht.put("1", "one");
        ht.put("2", "two");
        ht.put("3", "three");
        Enumeration<String> e = ht.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            String value = ht.get(key);
            //log.debug(key + " : " + value );
        }

        Util utl = new Util();
        String str = "  fg gg ";
        //log.debug(utl.removeSpaces(str));
    }


    // Copy a hashtable to another hashtable
    public Hashtable<String, String> copyHastable(Hashtable<String, String> from,
                                                  Hashtable<String, String> to) {
        Enumeration<String> e = from.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            String value = from.get(key);
            to.put(key, value);
        }
        return to;
    }


    // Merge a hashtable to another hashtable
    public Hashtable<String, String> mergeHastable(Hashtable<String, String> from,
                                                   Hashtable<String, String> to) {
        Enumeration<String> e = from.keys();
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            String value = from.get(key);
            to.put(key, to.get(key) + "  " + value);
        }
        return to;
    }


    // Merge a hashtable to another hashtable
    public void printHastable(Hashtable<String, String> ht) {
        Enumeration<String> e = ht.keys();
        //log.debug("Size = " + ht.size());

        while (e.hasMoreElements()) {
            String key = e.nextElement();
            String value = ht.get(key);
            //log.debug(key + " : " + value);
        }
    }


    // Remove all spaces from a string
    public String removeSpaces(String s) {
        StringTokenizer st = new StringTokenizer(s, " ", false);
        String t = "";
        while (st.hasMoreElements()) t += st.nextElement();
        return t;
    }


    //
    public void writeHashtableToFile(Hashtable<String, String> ht, File file) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));

            Enumeration<String> e = ht.keys();
            while (e.hasMoreElements()) {
                String key = e.nextElement();
                String value = ht.get(key);
                out.write(value + "\n");
                //log.debug(key + " : " + value);
            }
            out.close();
        } catch (IOException e) {
            //log.error("Exception ");
        }
    }


    public Connection createDefaultConnection() throws SQLException {
        Connection conn = DriverManager.getConnection
                ("jdbc:oracle:thin:@machineName:port:sid", "userid", "password");
        return conn;
    }


}
