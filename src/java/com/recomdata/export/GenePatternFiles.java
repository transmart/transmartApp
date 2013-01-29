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
  

/**
 * $Id: GenePatternFiles.java 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 *
 */
package com.recomdata.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.StringTokenizer;

class GenePatternFiles {
	
	protected File clsfile;
	protected File gctfile;
	protected File csvfile;
	protected PrintStream clsPS;
	protected PrintStream gctPS;
	protected PrintStream csvPS;
	
	private static int FLUSH_COUNT = 100;
	private int gctFlushCount = 0;
	private int csvFlushCount  =0;
	public GenePatternFiles() throws java.io.IOException{
		// put files in a directory 
		File tmpdir = new File(System.getProperty("java.io.tmpdir") + File.separator + "datasetexplorer");
		
		if (! tmpdir.exists()) {
			tmpdir.mkdir();
		}
		this.clsfile = File.createTempFile("gp_df_", ".cls", tmpdir);
		this.gctfile = File.createTempFile("gp_df_", ".gct", tmpdir);
		this.csvfile = File.createTempFile("gp_df_exp_", ".csv", tmpdir);
	}
	
	public void writeClsFile(String subjectIds1, String subjectIds2) throws java.io.IOException {
		FileOutputStream fos = new FileOutputStream(this.clsfile);
		PrintStream ps = new PrintStream(fos);

		if (subjectIds1 != null && subjectIds2 != null) {
			StringTokenizer st1 = new StringTokenizer(subjectIds1, ",");
			StringTokenizer st2 = new StringTokenizer(subjectIds2, ",");
	
			Integer count1 = st1.countTokens();
			Integer count2 = st2.countTokens();
			Integer total = count1 + count2;
			
			ps.println(total + " 2 1");
			ps.println("# S1 S2");
			while (st1.hasMoreTokens()) {
				String id = st1.nextToken();
				ps.print("0 ");
			}
			while (st2.hasMoreTokens()) {
				String id = st2.nextToken();
				ps.print("1 ");
			}
		} else {
			StringTokenizer st;
			if (subjectIds1 != null) {
				st = new StringTokenizer(subjectIds1, ",");
			} else {
				st = new StringTokenizer(subjectIds2, ",");
			}
	
			Integer count = st.countTokens();
			
			ps.println(count + " 1 1");
			ps.println("# subset1");
			while (st.hasMoreTokens()) {
				String id = st.nextToken();
				ps.print("0 ");
			}
		}
		
		ps.print("\n");
		
		fos.close();		
	}

	public void writeGctFile(HeatMapTable table, Boolean addMeans) throws java.io.IOException {
		FileOutputStream fos = new FileOutputStream(this.gctfile);
		PrintStream ps = new PrintStream(fos, true);
		table.writeToFile("\t", ps, addMeans);
		fos.flush();
		fos.close();
	}

	public void writeGctFile(HeatMapTable table) throws java.io.IOException {
		FileOutputStream fos = new FileOutputStream(this.gctfile);
		PrintStream ps = new PrintStream(fos, true);
		table.writeToFile("\t",ps);
		fos.flush();
		fos.close();
	}
	
	public void openGctFile() throws java.io.IOException {
		OutputStream fos = new BufferedOutputStream(new FileOutputStream(this.gctfile));
		this.gctPS = new PrintStream(fos, true);
	}
	
	public void createGctHeader(Integer rows, String[] ids, String delimiter) {
		this.gctPS.println("#1.2");

		this.gctPS.println(rows + delimiter + ids.length);		
		this.gctPS.print("NAME"+delimiter+"Description");
		
		for (String id : ids) {
			this.gctPS.print(delimiter + id);
		}
				
		this.gctPS.print("\n");
	}
	
	public void writeToGctFile(String value) throws java.io.IOException {
		this.gctPS.println(value);
		 gctFlushCount++;
	        if(gctFlushCount>FLUSH_COUNT){
	        	this.gctPS.flush();
	        	gctFlushCount = 0;
	        }
	}

	public void closeGctFile() throws java.io.IOException {
		this.gctPS.close();
	}
	
	public void openCSVFile() throws java.io.IOException {
        OutputStream fos = new BufferedOutputStream( new FileOutputStream(this.csvfile));
        this.csvPS = new PrintStream(fos, true);
       
    }
	
	public void createCSVHeader(String[] ids, String delimiter) throws java.io.IOException {      
        this.csvPS.print("NAME"+delimiter+"Description");        
        for (String id : ids) {
            this.csvPS.print(delimiter + id);
        }                
        this.csvPS.print("\n");
       
    }
	public void writeToCSVFile(String value) throws java.io.IOException {      
        this.csvPS.println(value);
        csvFlushCount++;
        if(csvFlushCount>FLUSH_COUNT){
        	this.csvPS.flush();
        	csvFlushCount = 0;
        }
    }
	
	public void writeToCSVFile(String[] ids, String delimiter, String value) throws java.io.IOException {      
        this.csvPS.print("NAME"+delimiter+"Description");        
        for (String id : ids) {
            this.csvPS.print(delimiter + id);
        }                
        this.csvPS.print("\n");
        this.csvPS.println(value);
        csvFlushCount++;
        if(csvFlushCount>FLUSH_COUNT){
        	this.csvPS.flush();
        	csvFlushCount = 0;
        }
    }

	
    public void closeCSVFile() throws java.io.IOException {
    	this.csvPS.flush();
        this.csvPS.close();
    }

	public Boolean openClsFile() throws java.io.IOException {
		FileOutputStream fos = new FileOutputStream(this.clsfile);
		this.clsPS = new PrintStream(fos);
		return true;
	}
	
	public void writeToClsFile(String value) throws java.io.IOException {
		this.clsPS.println(value);
	}

	public void closeClsFile() throws java.io.IOException {
		this.clsPS.flush();
		this.clsPS.close();
	}

	public File clsFile() {
		return this.clsfile;
	}
	
	public File gctFile() {
		return this.gctfile;
	}
	
	public String getCSVFileName()  {
	    return this.csvfile.getName();
	}
}