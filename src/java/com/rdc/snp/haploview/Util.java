/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

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
import org.apache.log4j.*;

public class Util {

	public static void main(String [] arg){
		Hashtable <String, String> ht;
		ht = new Hashtable <String, String> ();
		ht.put("1", "one");
		ht.put("2", "two");
		ht.put("3", "three");
		Enumeration<String> e = ht.keys();
		while( e. hasMoreElements() ){
			  String key = e.nextElement();
			  String value = ht.get(key);
			  //log.debug(key + " : " + value );
		}
		
		Util utl = new Util();
		String str = "  fg gg ";
		//log.debug(utl.removeSpaces(str));
	}
	
	
	// Copy a hashtable to another hashtable 
	public Hashtable <String, String> copyHastable(Hashtable <String, String> from, 
			Hashtable <String, String> to){
		Enumeration<String> e = from.keys();
		while( e. hasMoreElements() ){
			  String key = e.nextElement();
			  String value = from.get(key);
			  to.put(key, value);
		}
		return to;
	}
	

	// Merge a hashtable to another hashtable 
	public Hashtable <String, String> mergeHastable(Hashtable <String, String> from, 
			Hashtable <String, String> to){
		Enumeration<String> e = from.keys();
		while( e. hasMoreElements() ){
			  String key = e.nextElement();
			  String value = from.get(key);
			  to.put(key, to.get(key) + "  " + value);
		}
		return to;
	}

	
	// Merge a hashtable to another hashtable 
	public void printHastable(Hashtable <String, String> ht){
		Enumeration<String> e = ht.keys();
		//log.debug("Size = " + ht.size());
		
		while(e. hasMoreElements() ){
			  String key = e.nextElement();
			  String value = ht.get(key);
			  //log.debug(key + " : " + value);
		}
	}	
	
	
	// Remove all spaces from a string
	public String removeSpaces(String s) {
		  StringTokenizer st = new StringTokenizer(s," ",false);
		  String t="";
		  while (st.hasMoreElements()) t += st.nextElement();
		  return t;
	}
	
	
	// 
	public void writeHashtableToFile(Hashtable <String, String> ht, File file){
		try {
			 BufferedWriter out = new BufferedWriter(new FileWriter(file));

			 Enumeration<String> e = ht.keys();
			 while(e. hasMoreElements() ){
				  String key = e.nextElement();
				  String value = ht.get(key);
				  out.write(value + "\n");
				  //log.debug(key + " : " + value);
			}
			out.close();
		} catch (IOException e)
		{
			//log.error("Exception ");
		}
	}
	
	
	public Connection createDefaultConnection() throws SQLException{
	    Connection conn = DriverManager.getConnection
	    	("jdbc:oracle:thin:@machineName:port:sid", "userid", "password");	
	    return conn;
	}


}
