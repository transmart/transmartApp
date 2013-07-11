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
  

package com.recomdata.util

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import oracle.net.aso.e;

import groovy.sql.Sql;

/**
Procedure of SNP Annotation Data Loading

1. Check that the database user "biomart_user" has set up synonym to de_snp_info, de_snp_probe, de_snp_gene_map
2. Create sequence "snp_temp_id_seq", "snp_id_seq" and "snp_probe_id_seq"
3. Create a temporary table de_snp_loading_temp in "biomart_user" schema, or in "deapp" schema with synonym in "biomart_user".
create table de_snp_loading_temp (
	snp_temp_id	NUMBER(22,0) PRIMARY KEY,		
	name	VARCHAR2(255),		
	chrom	VARCHAR2(16),	
	chrom_pos	NUMBER (22,0),
	probe_name	VARCHAR2(255),	
	entrez_gene_string	VARCHAR2(2048)
);

4. Use WinSCP to copy over the data file and control file SqlLoaderSnpInfo.ctl
5. Use SSH to log into the database server, execute:
sqlldr control=SqlLoaderSnpInfo.ctl, log=SqlLoaderSnpInfo.log,bad=SqlLoaderSnpInfo.bad userid=biomart_user/biomart_user rows=1

6. Compile the stored procedure that splits the string:

CREATE OR REPLACE FUNCTION func_str_split(
  str_to_split IN OUT VARCHAR2,
  str_delimiter IN VARCHAR2) 
    RETURN VARCHAR2
IS
  t_pos NUMBER;
  t_len NUMBER;
  t_strlen NUMBER;
  t_strresult VARCHAR2(2000);
BEGIN
  t_strresult := NULL;
  IF str_to_split IS NOT NULL 
  THEN
    t_len := LENGTH(str_delimiter);
    t_strlen := LENGTH(str_to_split);
    t_pos := INSTR(str_to_split,str_delimiter);
    IF t_pos > 0 
    THEN
      t_strresult := SUBSTR(str_to_split,1,t_pos-1);
      str_to_split := SUBSTR(str_to_split,t_pos+t_len,t_strlen);
    ELSE
      t_strresult := str_to_split;
      str_to_split := NULL;
    END IF;
  END IF;

  RETURN t_strresult;
  
END func_str_split;

7. Run the SQL script:

DECLARE
  snp_id NUMBER;
  snp_name VARCHAR2(255);
  snp_cnt NUMBER;
  probe_cnt NUMBER;
	entrez_gene_string	VARCHAR2(4000);
  splitted_string VARCHAR2(4000);
  entrez_id_string VARCHAR2(255);
BEGIN
  FOR temp_rec in (select * from de_snp_loading_temp order by snp_temp_id)
  LOOP
    snp_name := temp_rec.name;
    select count(1) into snp_cnt from de_snp_info where name = snp_name and rownum < 2;
    
    IF (snp_cnt = 0) 
    THEN
      insert into de_snp_info values(snp_id_seq.nextval, temp_rec.name, temp_rec.chrom, temp_rec.chrom_pos);
      commit;
      
      select snp_id_seq.currval into snp_id from dual;
      
      insert into de_snp_probe values(snp_probe_id_seq.nextval, temp_rec.probe_name, snp_id, temp_rec.name);
      commit;
      
      entrez_gene_string	:= TRIM(temp_rec.entrez_gene_string);
      IF entrez_gene_string IS NOT NULL
      THEN
        splitted_string := entrez_gene_string;
        WHILE splitted_string IS NOT NULL 
        LOOP
          entrez_id_string := func_str_split (splitted_string, ' ');
          insert into de_snp_gene_map values (snp_id, temp_rec.name, TO_NUMBER(entrez_id_string));
          commit;
        END LOOP;
      END IF;
    END IF;
    
  END LOOP;
END;


 */

/**
 * This class is to load SNP annotation files, which may have millions of records.
 * SQL loader and stored procedures are needs in combination with file parsing code.
 * @author DLiu
 *
 */
class SnpAnnotLoading {
	
	
	private Sql sql = null;
	
	private Properties loadConfiguration(String file) throws IOException {
		
		Properties prop = new Properties();
		FileInputStream fis = new FileInputStream(file);
		prop.load(fis);
		fis.close();
		
		return prop;
	}
	
	/** This function simply parse the gene list for each probe */
	void parseSNPAnnotForSqlLoader (String annotFileName, String loadingFileName) throws Exception {
		File annotFile = new File(annotFileName);

		BufferedWriter writer = new BufferedWriter(new FileWriter(loadingFileName));
	   
		int lineCount = 0;
		annotFile.eachLine { line ->
			if (line.startsWith("#") == false && line.startsWith("\"Probe Set ID\"") == false) {
				String[] valueList = line.split("\",\"");	// It is important to split with "," . Comma is not enough, since gene name has comma inside
				String probeName = valueList[0].replaceAll("\"", "");
				String snpName = valueList[1].replaceAll("\"", "");
				String chrom = valueList[2].replaceAll("\"", "");
				if (chrom != null && chrom.indexOf("---") >= 0)
					chrom = "";
				String chromPos = valueList[3].replaceAll("\"", "");
				if (chromPos != null && chromPos.indexOf("---") >= 0)
					chromPos = "";

				Set<String> entrezIdSet = new HashSet<String>();
				String geneAllString = valueList[10].replaceAll("\"", "");
				String[] geneStringList = geneAllString.split("///");
				if (geneStringList != null && geneStringList.size() != 0) {
				   for (int i = 0; i < geneStringList.size(); i ++) {
					   String geneString = geneStringList[i].trim();
					   if (geneString != null && geneString.size() != 0) {
						   String[] geneValueList = geneString.split("//");
						   if (geneValueList == null || geneValueList.size() < 6) {
							   // print("\n Error in line " + (lineCount + 1) + ": " + line + "\n");
							   // print("\tThe Gene String: " + geneString + "\n");
						   }
						   else {
							   String entrezIdStr = geneValueList[5].trim();
							   if (entrezIdStr != null && entrezIdStr.length() != 0 && entrezIdStr.equals("---") == false) {
								   Long entrezIdLong = null;
								   try {
									   entrezIdLong = new Long(entrezIdStr);
								   }
								   catch(Exception e) {
									   print("\n Error in line " + (lineCount + 1) + ": " + line + "\n");
									   print("\tThe Entrez ID String: " + entrezIdStr + "\n");
								   }
								   if (entrezIdLong != null)
								   		entrezIdSet.add(entrezIdLong.toString());
							   }
						   }
					   }
				   }
			   }
				
				if (entrezIdSet.size() != 0) {
					Iterator<e> setIt = entrezIdSet.iterator();
					while(setIt.hasNext()) {
						String entrezIdStr = setIt.next();
						writer.write(snpName + "\t" + chrom + "\t" + chromPos + "\t" + probeName + "\t" + entrezIdStr + "\n");
					}
				}
		   }
		   
			lineCount++;
	   }
		
		writer.close();
	}

	public static String UCSC_SNP_132_File_Name = "snp132.txt";
	public static String UCSC_SNP_132_TRIM_File_Name = "snp132_trim.txt";
	public static String UCSC_SNP_132_EXCEPTION_File_Name = "snp132_exception.txt";
	
	/** The deapp.de_snp_132 table schema is very similar to UCSC snp_132 schema, 
	 * except that with flexible and delimited text field, Oracle SQLLoader has a limit of 255 characters.
	 * Shorten the field to 255, log the shorted record to exception file. Reload using slower Java JDBC if necessary. */
	void parseUCSCSnp132ForSqlLoader(String snpFileDirName) throws Exception {
		if (snpFileDirName.endsWith(File.separator) == false)
			snpFileDirName = snpFileDirName + File.separator;

		BufferedReader reader = new BufferedReader(new FileReader(snpFileDirName + UCSC_SNP_132_File_Name));
			
		BufferedWriter writer = new BufferedWriter(new FileWriter(snpFileDirName + UCSC_SNP_132_TRIM_File_Name));
		BufferedWriter exceptionWriter = new BufferedWriter(new FileWriter(snpFileDirName + UCSC_SNP_132_EXCEPTION_File_Name));
		
		String line = reader.readLine();
		while (line != null) {
			String[] values = line.split("\t");
			boolean isException = false;
			
			StringBuffer lineBuf = new StringBuffer();
			for (String value : values) {
				if (lineBuf.length() != 0)
					lineBuf.append("\t");
				if (value != null && value.length() > 255) {
					value = value.substring(0, 255 - 4);
					value += "...";
					isException = true;
				}
				lineBuf.append(value);
			}
			writer.write(lineBuf.toString() + "\n");
			
			if (isException == true) {
				exceptionWriter.write(line + "\n");
			}
			
			line  = reader.readLine();
		}
		
		reader.close();
		writer.close();
		exceptionWriter.close();
	}

	public static void main(String[] args) {
		SnpAnnotLoading sdl = new SnpAnnotLoading();		

		// extract parameters
		File path = new File(SnpAnnotLoading.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String configFilePath = path.getParent() + File.separator + "SnpViewer.properties";
		Properties props = sdl.loadConfiguration(configFilePath);

		/*
		String annotFileName = props.get("affy_annotation_file");
		String dataFileName = props.get("affy_annotation_data_file");
		sdl.parseSNPAnnotForSqlLoader(annotFileName, dataFileName);
		*/
		
		String snp132DirName = props.get("ucsc_snp_132_data_dir");
		sdl.parseUCSCSnp132ForSqlLoader(snp132DirName);


	}
}
