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
package com.recomdata.util;

import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.util.List;
import java.util.Set;



//import java.io.BufferedReader;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
import java.sql.*;

import groovy.sql.Sql;
//import i2b2.SnpDataset;

class SnpDataLoading_ADNI {
	
	private Sql sql = null;
	
	Integer getChromNumberFromString(String chromStr) {
		Integer res = null;
		if (chromStr.equalsIgnoreCase("X"))
			res = new Integer(23);
		else if (chromStr.equalsIgnoreCase("Y"))
			res = new Integer(24);
		else {
			try {
				res = new Integer(chromStr);
			}
			catch(Exception e) {}
		}
		return res;
	}

	/**
	 * 	
	 * @return
	 */
	String[] getChromStrArray() {
		String[] chromArray = new String[25];
		for (int i  = 0; i < 22; i++) {
			chromArray[i] = Integer.toString(i + 1);
		}
		chromArray[22] = "X";
		chromArray[23] = "Y";
		chromArray[24] = "M";
		return chromArray;
	}
	

	/**
	 * 
	 * @param server		Oracle server name
	 * @param port			Oracle listener poer
	 * @param sid			Oracle SID
	 * @param userName		Oracle user name
	 * @param password		Oracle user password
	 * @return				Sql object
	 */
	private Sql getSql(String server, String port, String sid, String userName, String password) {
		if (sql != null) return sql;
				
		Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@" + server + ":" + port + ":" + sid, 
			userName, password);
		sql = new Sql(conn);
		return sql;
	}
			

	void connectConceptWithPatient(String datasetFileName, String trialName) {
		Sql sql = getSql();
		
		List<String> conceptList = new ArrayList<String>();
		String stmt = "select c_basecode from i2b2 where rownum < 10 and c_fullname like '%GSE19539%Biomarker%Affymetrix%'";
		sql.eachRow(stmt) {row ->
			conceptList.add(row.c_basecode);
		}
		
		File datasetFile = new File(datasetFileName);
		
		datasetFile.eachLine { line ->
			// Only use N (Normal data)
			if (line.endsWith("N")) {
				int idx = line.lastIndexOf("_");
				String patientEnd = line.substring(idx + 1, line.length() - 1);
				String patientSource = trialName + patientEnd.trim();
				Long patientNum = null;
				String stmt1 = "select patient_num from patient_dimension where sourcesystem_cd = ?";
				sql.eachRow(stmt1, [patientSource]) {row ->
					patientNum = row.patient_num;
				}
				if (patientNum == null) throw new Exception("Patient_num for " + patientSource + " does not exist");
				
				for (String concept : conceptList) {
					String stmt2 = "insert into observation_fact(patient_num, concept_cd, provider_id, ";
					stmt2 += "modifier_cd, valueflag_cd, location_cd, import_date, sourcesystem_cd)";
					stmt2 += " values(?, ?, '@', ?, '@', '@', sysdate, ?)";
					sql.execute(stmt2, [patientNum, concept, trialName, patientSource]);
				}
			}
		}
	}
	
	/**
	 * 
	 * @param file  property file from the default location 
	 * @return		hashtable populated properties
	 */
	
	private Properties loadConfiguration(String file) throws IOException {
		
		Properties prop = new Properties();
		FileInputStream fis = new FileInputStream(file);
		prop.load(fis);
		fis.close();
		
		return prop;
	}
	
	void generateGPSampleFile(String snpFileName, String sampleFileName, String disease_suffix, String normal_suffix, String gender) {
		BufferedReader snpReader = new BufferedReader(new FileReader(snpFileName));
		String snpHeader = snpReader.readLine();
		String[] snpColumnList = snpHeader.split("\t");
		
		File sampleFile = new File(sampleFileName);
		sampleFile << ("Array\tSample\tType\tPloidy(numeric)\tGender\tPaired\n");
		for (int i = 3; i < snpColumnList.length; i = i + 6) {
			String normalName = null, diseaseName = null;
			for (int k = 0; k < 2; k++) {
				String columnName = snpColumnList[i + 3 * k];
				int idx = columnName.indexOf("_Allele_A");
				String datasetName = columnName.substring(0, idx);
				if (datasetName.endsWith(normal_suffix))
					normalName = datasetName;
				else if (datasetName.endsWith(disease_suffix))
					diseaseName = datasetName;
			}
			sampleFile << (normalName + "\t" + normalName + "\tcontrol\t2\t" + gender + "\tYes\n");
			sampleFile << (diseaseName + "\t" + diseaseName + "\tdisease\t2\t" + gender + "\t" + normalName + "\n");
		}

	}

	/**
	 * This function is to merge the genotype information in the raw file (Allele1 - AB and Allele2 - AB) with the copy number variation data
	 * stored in database (originally calculated by PennCNV using B Allele Freq and Log R Ratio in the raw data)
	 * Skip the Illumina QA probes and copy number variation probes. Only take the probe against registered SNPs, "rsxxxx" in column "SNP Name"
	 */
	void mergePennCNVToRawData (String cnvFileName, String rawDataDirName, String mergedDataDirName) throws Exception {
		if (mergedDataDirName.endsWith(File.separator) == false) {
			mergedDataDirName = mergedDataDirName + File.separator;
		}
		
		Map<Long, Map<String, SortedMap<Long, Object[]>>> dataByPatientByChrom = new HashMap<Long, Map<String, SortedMap<Long, Object[]>>>();
		parsePennCNVFile (cnvFileName, dataByPatientByChrom);
		
		Map<Long, String> patientGenderMap = new HashMap<Long, String>();
		getPatientGender(patientGenderMap);
		
		SortedSet patientSet = new TreeSet();
		
		File rawDataDir = new File(rawDataDirName);
		File[] dataFiles = rawDataDir.listFiles();
		for (File dataFile : dataFiles) {
			String fileName = dataFile.getName();
			int idx1 = fileName.lastIndexOf("_");
			int idx2 = fileName.lastIndexOf(".");
			Long patientNum = new Long(fileName.substring(idx1 + 1, idx2));
			Map<String, SortedMap<Long, Object[]>> dataByPatient = dataByPatientByChrom.get(patientNum);
			if (dataByPatient == null) {
				System.out.println("No copy number data for file: " + dataFile.getName());
			}
			
			String gender = patientGenderMap.get(patientNum);
			
			try {
				StringBuffer buf = new StringBuffer();
				dataFile.eachLine { line -> 
					if (line.startsWith("Sample Index") == false) {
						String[] values = line.split(",");
						String snpName = values[4].trim();
						if (snpName.startsWith("rs") == true || snpName.startsWith("Mito") == true) {
							String allele1 = values[9].trim();
							String allele2 = values[10].trim();
							String genotype = allele1 + allele2;
							if (genotype.equals("BA"))
								genotype = "AB";
							else if (allele1.equals("-") || allele2.equals("-"))
								genotype = "NC";
							
							String chrom = values[11].trim();
							if (chrom.equals("MT")) {
								chrom = "M";	// hg19 uses "chrM", and NCBI uses "MT", both "M" and "MT" are acceptable to IGV 1.5.
							}
							else if (chrom.equals("XY")) {
								// A SNP can be found on the redundant section of X and Y. The position is not same. 
								// Illumina use X positions. IGV does not have a chromosome designation of "XY". So we import this record as X, not "XY".
								chrom = "X"; 
							}
							String chromPosStr = values[12].trim();
							Long chromPos = new Long(chromPosStr);
							
							// Search the ranged data map to get the copy number calculated by PennCNV
							String copyNumberStr = null;
							if (dataByPatient != null) {
								SortedMap<Long, Object[]> dataByChrom = dataByPatient.get(chrom);
								if (dataByChrom != null) {
									for (Map.Entry<Long, Object[]> entry : dataByChrom) {
										Long chromStart = entry.getKey();
										if (chromPos.longValue() >= chromStart.longValue()) {
											Object[] data = dataByChrom.get(chromStart);
											Long chromEnd = data[0];
											if (chromPos.longValue() <= chromEnd.longValue()) {
												copyNumberStr = data[1];
											}
										}
									}
								}
							}

							if (copyNumberStr == null) {
								if (chrom.equals("X")) {
									if (gender.equals("F"))
										copyNumberStr = "2";
									else if (gender.equals("M"))
										copyNumberStr = "1";
								}
								else if (chrom.equals("Y")) {
									if (gender.equals("F"))
										copyNumberStr = "0";
									else if (gender.equals("M"))
										copyNumberStr = "1";
								}
								else {
									copyNumberStr = "2";
								}
							}
							buf.append(snpName + "\t" + chrom + "\t" + chromPos + "\t" + genotype + "\t" + copyNumberStr + ".0\n" );
						}
					}
				}
				// Output the data buffer to merged file
				String mergedFileName = mergedDataDirName + "ADNI_" + patientNum + ".txt";
				FileWriter mergedFileWriter = new FileWriter(new File(mergedFileName));
				mergedFileWriter.write(buf.toString());
				mergedFileWriter.close();
				System.out.println("Finished processing data file " + dataFile.getName());
				patientSet.add(patientNum);
			}
			catch (Exception e) {
				System.out.println("Error in processing file " + dataFile.getName() + "\n" + e.printStackTrace() + "\n");
			}
		}
		String patientListFileName = mergedDataDirName + "ADNI_Patient_List.txt";
		StringBuffer listBuf = new StringBuffer();
		for (Long patientNum : patientSet) {
			listBuf.append(patientNum + "\n");
		}
		FileWriter listFileWriter = new FileWriter(new File(patientListFileName));
		listFileWriter.write(listBuf.toString());
		listFileWriter.close();
	}
	
	void getPatientGender(Map<Long, String> patientGenderMap) throws Exception {
		if (patientGenderMap == null) throw new Exception ("The variable patientGenderMap is null");
		
		sql.eachRow("select sourcesystem_cd, sex_cd from patient_dimension where sourcesystem_cd like 'ADNI_%'") { row ->
			String patientStr = row.sourcesystem_cd;
			String gender = row.sex_cd;
			if (gender != null) {
				String patientNumStr = patientStr.substring(5).trim();
				Long patientNum = new Long(patientNumStr);
				patientGenderMap.put(patientNum, gender);
			}
		}
	}
	
	void parsePennCNVFile (String cnvFileName, Map<Long, Map<String, SortedMap<Long, Object[]>>> dataByPatientByChrom) throws Exception {
		if (dataByPatientByChrom == null) throw new Exception ("The variable dataByPatientByChrom is null");
		File cnvFile = new File(cnvFileName);
		cnvFile.eachLine { line ->
			try {
				String chromStr = line.substring(0, 30);
				String copyNumber = line.substring(73,74);
				String patientNumberStr = line.substring(96, 100);
				Long patientNumber = new Integer(patientNumberStr);
				int idx_colon = chromStr.indexOf(":");
				String chrom = chromStr.substring(3, idx_colon);
				int idx_dash = chromStr.indexOf("-");
				String chromStartStr = chromStr.substring(idx_colon + 1, idx_dash);
				String chromEndStr = chromStr.substring(idx_dash + 1);
				Long chromStart = new Long(chromStartStr.trim());
				Long chromEnd = new Long(chromEndStr.trim());
				
				Map<String, SortedMap<Long, Object[]>> dataByPatient = dataByPatientByChrom.get(patientNumber);
				if (dataByPatient == null) {
					dataByPatient = new HashMap<String, SortedMap<Long, Object[]>>();
					dataByPatientByChrom.put(patientNumber, dataByPatient);
				}
				SortedMap<Long, Object[]> dataByChrom = dataByPatient.get(chrom);
				if (dataByChrom == null) {
					dataByChrom = new TreeMap<Long, Object[]>();
					dataByPatient.put(chrom, dataByChrom);
				}
				// The PennCNV output has non-overlapping ranges of copy number variation
				Object[] data = new Object[2];
				data[0] = chromEnd;
				data[1] = copyNumber;
				dataByChrom.put(chromStart, data);
			}
			catch (Exception e) {
				System.out.println(line);
			}
		}
	}
	

	/** Note: The database approach is too slow to process 702 files, each with 600,000 records.
	 * This function load the PennCNV output file into a temporary database table, for ease of ranged search.

		create table de_snp_penncnv_temp (
			study VARCHAR2(255),  -- like "ADNI" 
			patient_name  VARCHAR2(255),  -- like "295" in the data file 002_S_0295.csv
			chrom	VARCHAR2(16),	-- "1", "2", ..., "X", "MT"
			chrom_start	NUMBER(22,0),	-- The starting position of variation range
			chrom_end	NUMBER(22,0),	-- The ending position of variation range
			copy_number_str	VARCHAR2(255)	-- The cn in the PennCNV output "state1,cn=0”	
		);
		
	 * @param cnvFileName
	 * @throws Exception
	 */
	void loadPennCNVFile (String cnvFileName) throws Exception {
		File cnvFile = new File(cnvFileName);
		cnvFile.eachLine { line -> 
			try {
				String chromStr = line.substring(0, 30);
				String copyNumber = line.substring(73,74);
				String patientNumberStr = line.substring(96, 100);
				patientNumberStr = (new Integer(patientNumberStr)).toString();
				int idx_colon = chromStr.indexOf(":");
				String chrom = chromStr.substring(3, idx_colon);
				int idx_dash = chromStr.indexOf("-");
				String chromStartStr = chromStr.substring(idx_colon + 1, idx_dash);
				String chromEndStr = chromStr.substring(idx_dash + 1);
				Long chromStart = new Long(chromStartStr.trim());
				Long chromEnd = new Long(chromEndStr.trim());
				
				sql.execute("insert into de_snp_penncnv_temp values ('ADNI', ?, ?, ?, ?, ?)", 
					[patientNumberStr, chrom, chromStart, chromEnd, copyNumber]);
			}
			catch (Exception e) {
				System.out.println(line);
			}
		}
	}
	
	void checkDataFileConsistency (String dataDirName) throws Exception {
		File dataDir = new File(dataDirName);
		File[] dataFiles = dataDir.listFiles();
		List<String> snpList = null;
		for (File dataFile : dataFiles) {
			if (snpList == null) {
				snpList = new ArrayList();
				dataFile.eachLine { line -> 
					String[] values = line.split("\t");
					String snpName = values[0];
					snpList.add(snpName);
				}
				System.out.println("Standard: " + dataFile.getName());
			}
			else {
				Iterator<String> itSnp = snpList.iterator();
				dataFile.eachLine { line ->
					String[] values = line.split("\t");
					String snpName = values[0];
					String snpNameIt = itSnp.next();
					if (snpName.equals(snpNameIt) == false) {
						System.out.println("Different snp order for file: " + dataFile.getName());
					}
				}
				System.out.println("Successfully checked file: " + dataFile.getName());
			}
		}
	}
	
	void createHg19Map(String dataDirName, String mappedDataDirName) throws Exception {
		if (mappedDataDirName.endsWith(File.separator) == false) 
			mappedDataDirName += File.separator;
		File dataDir = new File(dataDirName);
		File[] dataFiles = dataDir.listFiles();
		File dataFile = dataFiles[0];
		Map<String, String[]> snpDataMap = new HashMap<String, Set<String[]>>(); // String[] is [chromNew, chromPosNew]
		int lineIdx = 0;
		int batchNum = 1000;
		StringBuffer snpBuf = new StringBuffer();
		dataFile.eachLine { line ->
			lineIdx ++;
			if (lineIdx % batchNum == 0) {	// Need to process this batch
				getHg19FromDB(snpBuf.toString(), snpDataMap);
				snpBuf = new StringBuffer();
				System.out.println("Processing line " + lineIdx);
			}
			String[] values = line.split("\t");
			if (snpBuf.length() != 0)
				snpBuf.append(",");
			snpBuf.append("'" + values[0] + "'");
		}
		if (snpBuf != null && snpBuf.length() != 0) {	// Process the last batch
			getHg19FromDB(snpBuf.toString(), snpDataMap);
		}
		
		StringBuffer mappingBuf = new StringBuffer();
		mappingBuf.append("#snp_name_ori\tchrom_ori\tchrom_pos_ori\tsnp_new\tchrom_new\tchrom_pos_new\tDescription\n");
				
		lineIdx = 0;
		dataFile.eachLine { line ->
			lineIdx ++;
			if (lineIdx % batchNum == 0) {	// Need to report the progress
				System.out.println("Output line " + lineIdx);
			}
			
			String[] values = line.split("\t");
			String snpName = values[0];
			String chromOld = values[1];
			String chromPosOld = values[2];
			Set<String[]> snpInfoSet = snpDataMap.get(snpName);
			if (snpInfoSet == null) {	// No such SNP in UCSC SNP 132. Need to go to NCBI to find out if this SNP is merged into another SNP
				URL url = new URL(NCBI_SNP_QUERY_WEB + snpName);
				String snpText = url.getText();
				String tagStr = "snp_ref.cgi?rs=";
				String snpNameNew = null;
				String chromNew = null;
				String chromPosNew = null;
				int idx1 = snpText.indexOf(tagStr);
				if (idx1 > 0) {
					int idx2 = snpText.indexOf("\"", idx1);
					if (idx2 > 0) {
						String snpNameNewStr = snpText.substring(idx1 + tagStr.length(), idx2);
						Long snpNumberNew = null;
						try {
							snpNumberNew = new Long(snpNameNewStr);
						}
						catch (Exception e) {}
						if (snpNumberNew != null) {
							snpNameNew = "rs" + snpNumberNew;
						}
					}
				}
				
				boolean hasMultipleRecord = false;
				if (snpNameNew != null) {
					sql.eachRow("select * from de_snp_132 where name = ?", [snpNameNew]) { row ->
						if (chromNew != null) {
							hasMultipleRecord = true;
						}
						else {
							chromNew = row.chrom;
							chromNew = chromNew.substring(3);
							chromPosNew = row.chromEnd;
						}
					}
				}
				
				if (chromNew == null) {	// The SNP is not merged into another SNP, or the other SNP is not in USCS hg19
					mappingBuf.append(snpName + "\t" + chromOld + "\t" + chromPosOld + "\t\t\t\tSNP no longer in hg19, or found to be a cluster\n");
				}
				else if (hasMultipleRecord == true) {
					mappingBuf.append(snpName + "\t" + chromOld + "\t" + chromPosOld + "\t\t\t\tSNP merged into " + snpNameNew + ", but has multiple entries in hg19\n");
				}
				else {
					mappingBuf.append(snpName + "\t" + chromOld + "\t" + chromPosOld + "\t" + snpNameNew + "\t" + chromNew + "\t" + 
						chromPosNew + "\tSNP merged successfully in hg19\n");
				}
			}
			else if (snpInfoSet.size() == 1)	{	// SNP mapped into a unique entry in hg19
				String[] snpInfo = snpInfoSet.iterator().next();
				mappingBuf.append(snpName + "\t" + chromOld + "\t" + chromPosOld + "\t" + snpName + "\t" + snpInfo[0] + "\t" + snpInfo[1] + "\t");
				if (chromOld.equals(snpInfo[0]) == false) {	// this SNP was mapped to another chromosome
					mappingBuf.append("SNP successfully mapped to another chromosome");
				}
				else {
					mappingBuf.append("SNP successfully mapped to the same chromosome");
				}
				mappingBuf.append("\n");
			}
			else {	// SNP mapped to multiple chromosomal locations
				StringBuffer descBuf = new StringBuffer();
				descBuf.append("SNP " + snpName + " is mapped to ");
				Iterator it = snpInfoSet.iterator();
				while (it.hasNext()) {
					String[] snpInfo = it.next();
					descBuf.append("chr" + snpInfo[0] + " : " + snpInfo[1] + ", ");
				}
				mappingBuf.append(snpName + "\t" + chromOld + "\t" + chromPosOld + "\t\t\t\t" + descBuf.toString() + "\n");
			}
		}
		
		String snpMappingFileName = mappedDataDirName + "ADNI_SNP_Mapping.txt";
		
		(new File(snpMappingFileName)) << mappingBuf;
	}
	
	/**
	 * The mapping of hg18 SNP location to hg19 is very tricky. It is possible for a SNP to be mapped to multiple locations in hg19, sometimes on different chrom.
	 * @param snpListStr
	 * @param snpDataMap	(Map<snpName, Set<String[chrom, chrompos]>)
	 * @throws Exception
	 */
	void getHg19FromDB(String snpListStr, Map<String, Set<String[]>> snpDataMap) throws Exception {
		if (snpDataMap == null) throw new Exception("snpDataMap is not instantiated.");
		String sqlStr = "select * from de_snp_132 where name in (" + snpListStr + ")";
		sql.eachRow(sqlStr) { row ->
			String snpName = row.name;
			String chrom = row.chrom;
			chrom = chrom.substring(3);
			String chromPos = row.chromEnd;
			String[] snpInfo = new String[2];
			snpInfo[0] = chrom;
			snpInfo[1] = chromPos;
			Set<String[]> snpInfoSet = snpDataMap.get(snpName);
			if (snpInfoSet == null) {
				snpInfoSet = new HashSet<String[]>();
				snpDataMap.put(snpName, snpInfoSet);
			}
			snpInfoSet.add(snpInfo);
		}
	}
	
	void createSortOrderFile (String hg19MappedFileName, String snpOrderFileName) throws Exception {
		
		Map<String, SortedMap<Long, String[]>> snpMap = new HashMap<String, SortedMap<Long, String[]>>(); // String[5] is [snp_name_old, snp_name_new, chrom, chromPos, orderOld]
		File mappedFile = new File(hg19MappedFileName);
		int idxOld = 0;
		mappedFile.eachLine { line ->
			if (line != null && line.trim().length() != 0 && line.startsWith("#") == false) {
				idxOld ++;
				String[] values = line.split("\t");
				String snpNameOld = values[0];
				String snpNameNew = values[3];
				if (snpNameNew != null && snpNameNew.length() != 0) {
					String chromNew = values[4];
					String chromPosNewStr = values[5];
					Long chromPosNew = new Long(chromPosNewStr);
					
					SortedMap<Long, String[]> snpMapByChrom = snpMap.get(chromNew);
					if (snpMapByChrom == null) {
						snpMapByChrom = new TreeMap<Long, String[]>();
						snpMap.put(chromNew, snpMapByChrom);
					}
					String[] snpInfoOrder = new String[5];
					snpInfoOrder[0] = snpNameOld;
					snpInfoOrder[1] = snpNameNew;
					snpInfoOrder[2] = chromNew;
					snpInfoOrder[3] = chromPosNewStr;
					snpInfoOrder[4] = new Integer(idxOld).toString();
					snpMapByChrom.put(chromPosNew, snpInfoOrder);
				}
			}
		}

		// Build the mapping file from the old order to new order
		StringBuffer orderBuf = new StringBuffer();
		orderBuf.append("#Order_Old\tOrder_New\tSNP_Name_Old\tSNP_Name_New\tChrom_New\tChrom_Position_New\n");
		int idxNew = 0;
		
		// The Illumina data file based on hg18 only has SNPs mapped to chromosomes 1 to 22, X, Y, and M. 
		// However, hg19 moved some of these SNPs to other SNPs, and other reference chromosomes like 6_ssto_hap7
		// For now we ignore SNPs mapped to these non-conventional chromosomes.
		// Also, Illumina did not use standard mitochondria SNPs. Its own mitochondria probes cannot be mapped to UCSC SNP 132 data files. So Illumina Mitochondria SNP data are ignored
		String[] chromArray = getChromStrArray();
		for (String chrom : chromArray) {
			SortedMap<Long, String[]> snpMapByChrom = snpMap.get(chrom);
			for (Map.Entry entry : snpMapByChrom) {
				idxNew ++;
				Long chromPos = entry.getKey();
				String[] snpInfo = entry.getValue();
				orderBuf.append(snpInfo[4] + "\t" + idxNew + "\t" + snpInfo[0]  + "\t" + snpInfo[1] + "\t" + snpInfo[2] + "\t" + snpInfo[3] + "\n");
			}
		}
		
		File snpOrderFile = new File(snpOrderFileName);
		snpOrderFile << orderBuf.toString();
	}
	
	void sortHg19MappedFiles (String snpOrderFileName, String mappedFileDirName, String sortedFileDirName) throws Exception {
		if (sortedFileDirName.endsWith(File.separator) == false) {
			sortedFileDirName = sortedFileDirName + File.separator;
		}
		
		Map<String, Long> orderMap = new HashMap<String, Long>();
		File snpOrderFile = new File(snpOrderFileName);
		int totalCount = 0;
		snpOrderFile.eachLine { line ->
			if (line != null && line.trim().length() != 0) {
				totalCount ++;
				String[] values = line.split("\t");
				String snpName = values[0];
				String orderStr = values[3];
				Long order = new Long(orderStr);
				orderMap.put(snpName, order);
			}
		}
		
		File mappedDataDir = new File(mappedFileDirName);
		File[] mappedFiles = mappedDataDir.listFiles();
		for (File mappedFile : mappedFiles) {
			String fileName = mappedFile.getName();
			int idx1 = fileName.lastIndexOf("_");
			int idx2 = fileName.lastIndexOf(".");
			Long patientNum = new Long(fileName.substring(idx1 + 1, idx2));
			
			
			System.out.println("Finished processing file: " + fileName);
		}
	}
	
	public static String NCBI_SNP_QUERY_WEB = "http://www.ncbi.nlm.nih.gov/SNP/snp_ref.cgi?rs=";

	void sortDataFiles(String orderMappingFileName, String mergedDataDirName, String sortedDataDirName) {
		if (sortedDataDirName.endsWith(File.separator) == false) {
			sortedDataDirName = sortedDataDirName + File.separator;
		}
		
		Map<Long, String[]> orderMap = new HashMap<Long, String[]>();	// String[] is [New_Order, New_SNP_Name, New_Chrom,	New_Chrom_Position]
		File orderMappingFile = new File(orderMappingFileName);
		orderMappingFile.eachLine { line ->
			if (line != null && line.startsWith("#") == false) {
				String[] values = line.split("\t");
				Long orderOld = new Long(values[0]);
				String orderNew = new Long(values[1]);
				String snpNameNew = values[3];
				String chromNew = values[4];
				String chromPosNew = values[5];
				String[] snpOrderInfo = new String[4];
				snpOrderInfo[0] = orderNew;
				snpOrderInfo[1] = snpNameNew;
				snpOrderInfo[2] = chromNew;
				snpOrderInfo[3] = chromPosNew;
				orderMap.put(orderOld, snpOrderInfo);
			}
		}
		
		File mergedDir = new File(mergedDataDirName);
		File[] dataFiles = mergedDir.listFiles();
		int numSnp = orderMap.size();
		
		for (File dataFile : dataFiles) {
			String[] sortedDataArray = new String[numSnp];
			int idx = 0;
			dataFile.eachLine { line ->
				idx ++;
				String[] snpOrderInfo = orderMap.get(new Long(idx));
				if (snpOrderInfo != null) {	// This SNP in the merged data file has mapping to the sorted data file
					String[] values = line.split("\t");
					String genoType = values[3];
					String copyNumber = values[4];
					String orderNew = snpOrderInfo[0];
					// xcn file has the order of copy number / genotype
					String dataStr = snpOrderInfo[1] + "\t" + snpOrderInfo[2] + "\t" + snpOrderInfo[3] + "\t" + copyNumber + "\t" + genoType;
					sortedDataArray[Integer.parseInt(orderNew) - 1] = dataStr;	// Important: the order number in order mapping file is 1-based
				}
			}
			StringBuffer dataBuf = new StringBuffer();
			for (String dataStr : sortedDataArray) {
				if (dataStr == null || dataStr.length() == 0)
					throw new Exception ("sortedDataArray has emtpy entry");
				dataBuf.append(dataStr + "\n");
			}
			File sortedFile = new File(sortedDataDirName + dataFile.getName());
			sortedFile << dataBuf.toString();
			System.out.println("Finished processing file " + dataFile.getName());
		}
		
	}

	/**
	* This function populates the de_subject_snp_dataset table.
	* Due to workflow consideration of i2b2 data loading, the concept_cd column is left empty, and will be filled later
	* The dataset name in the dataset list file needs to be consistent with sourcesystem_cd of patient_dimension.
	* The platform needs to be existing in de_gpl_info
	* The SnpDataset.sampleType can only be "NORMAL" or "DISEASE",. It is used to generate GenePattern sample info text file.
	* @param sortedDataDirName
	* @param trialName
	* @param platformName
	*/
   void loadDataSet (String sortedDataDirName, String trialName, String platformName) throws Exception {
		   
	   String SAMPLE_TYPE_NORMAL = "NORMAL";
	   String SAMPLE_TYPE_DISEASE = "DISEASE";
	   
	   // Get the patient list and SQL string
	   StringBuffer patientNameBuf = new StringBuffer();
	   
	   File sortedDataDir = new File(sortedDataDirName);
	   File[] dataFiles = sortedDataDir.listFiles();
	   for (File dataFile : dataFiles) {
		   String fileName = dataFile.getName();
		   int idx = fileName.indexOf(".");
		   String patientName = fileName.substring(0, idx);
		   if (patientNameBuf.size() != 0)
		   		patientNameBuf.append(",");
		   patientNameBuf.append("'" + patientName + "'");
	   }
	   
	   // Get the gender of patients from patient_dimension
	   String sqlStr = "select * from patient_dimension where sourcesystem_cd in (" + patientNameBuf.toString() + ") order by patient_num";
	   sql.eachRow (sqlStr) { row ->
		   String patientName = row.sourcesystem_cd;
		   String gender = row.sex_cd;
		   Long patientNum = row.patient_num;
		   
		   String datasetName = patientName + "_N";	// All datasets are normal tissue SNP data for ADNI
		   String patientIDInADNI = patientName.substring(5); 
		   String stmt = "insert into de_subject_snp_dataset values(seq_snp_data_id.nextval, ?, null, ";
		   stmt += "?, ?, ?, null, ?, ?, null, ?)";	// For GSE19539 on ovarian cancer, all patients are female
		   def parameters = [datasetName, platformName, trialName, patientNum, patientIDInADNI, SAMPLE_TYPE_NORMAL, gender];
		   sql.execute(stmt, parameters);
	   }
	   
   }
   
	void loadDataByPatient (String sortedDataDirName, String trialName, String platformName) throws Exception {
		if (sortedDataDirName.endsWith(File.separator) == false) {
			sortedDataDirName = sortedDataDirName + File.separator;
		}
		
		boolean needSortedProbeDef = true;
		List platformRows = sql.rows("select * from de_snp_probe_sorted_def where platform_name = ?", platformName);
		if (platformRows != null && platformRows.size() != 0) {	// Probe and SNP definition data has already been loaded for this platform
			needSortedProbeDef = false;
		}
		
		String sqlStr = "select * from de_subject_snp_dataset where trial_name = ? order by subject_snp_dataset_id";
		sql.eachRow(sqlStr, [trialName]) { row ->
			Long datasetId = row.subject_snp_dataset_id;
			Long patientNum = row.patient_num;
			String subjectId = row.subject_id;
			
			String fileName = trialName + "_" + subjectId + ".txt";
			File dataFile = new File(sortedDataDirName + File.separator + fileName);
			StringBuffer allChromBuf = new StringBuffer();
			StringBuffer chromBuf = new StringBuffer();
			String currentChrom = null;
			int currentChromCount = 0;
			int totalCount = 0;
			StringBuffer allChromProbeDefBuf = new StringBuffer();
			StringBuffer chromProbeDefBuf = new StringBuffer();
			
			dataFile.eachLine { line -> 
				String[] values = line.split("\t");
				// Use the convention of xcn file, which has the format of copy number / genotype
				String genotype = values[4];
				String copyNumber = values[3];
				String chrom = values[1];
				String snpName = values[0];
				String chromPos = values[2];
				
				if (currentChrom == null) {
					currentChrom = chrom;
				}
				else if (currentChrom.equals(chrom) == false) {	// Read into another Chrom
					sql.execute("insert into de_snp_data_by_patient values (seq_snp_data_id.nextval, ?, ?, ?, ?, ?, null)",
						[datasetId, trialName, patientNum, currentChrom, chromBuf.toString()]);
					if (needSortedProbeDef)	{
						String defData = chromProbeDefBuf.toString();
						sql.execute("insert into de_snp_probe_sorted_def values (seq_snp_data_id.nextval, ?, ?, ?, ?, ?)",
							[platformName, currentChromCount, currentChrom, defData, defData]);	// Illumina does not give probe ID, use SNP name instead
					}
					// Reset the chrom-related variables
					chromBuf = new StringBuffer();
					chromProbeDefBuf = new StringBuffer();
					currentChrom = chrom;
					currentChromCount = 0;
				}
				
				currentChromCount ++;
				totalCount ++;
				chromBuf.append(copyNumber + "\t" + genotype + "\n");
				allChromBuf.append(copyNumber + "\t" + genotype + "\n");
				if (needSortedProbeDef)	{
					chromProbeDefBuf.append(snpName + "\t" + chrom + "\t" + chromPos + "\n");
					allChromProbeDefBuf.append(snpName + "\t" + chrom + "\t" + chromPos + "\n");
				}
			}
			
			if (chromBuf != null && chromBuf.length() != 0) {	// Process the last chrom
				sql.execute("insert into de_snp_data_by_patient values (seq_snp_data_id.nextval, ?, ?, ?, ?, ?, null)",
					[datasetId, trialName, patientNum, currentChrom, chromBuf.toString()]);
				if (needSortedProbeDef)	{
					String defData = chromProbeDefBuf.toString();
					sql.execute("insert into de_snp_probe_sorted_def values (seq_snp_data_id.nextval, ?, ?, ?, ?, ?)",
						[platformName, currentChromCount, currentChrom, defData, defData]);	// Illumina does not give probe ID, use SNP name instead
				}
			}
			
			// Insert the data for all chromosome
			sql.execute("insert into de_snp_data_by_patient values (seq_snp_data_id.nextval, ?, ?, ?, ?, ?, null)",
				[datasetId, trialName, patientNum, "ALL", allChromBuf.toString()]);
			if (needSortedProbeDef)	{
				String defData = allChromProbeDefBuf.toString();
				sql.execute("insert into de_snp_probe_sorted_def values (seq_snp_data_id.nextval, ?, ?, ?, ?, ?)",
					[platformName, totalCount, "ALL", defData, defData]);	// Illumina does not give probe ID, use SNP name instead
				needSortedProbeDef = false;	// Probe definition data needs to be loaded once for each platform.
			}
			
			System.out.println("Finished processing file " + fileName);
		}
	}

	
	/**
	* This function is to generate the data file used by SQL Loader to populate de_snp_data_by_probe table
	* First load the large data into de_snp_data_by_probe, then update the de_snp_info and de_snp_probe, 
	* and last populate probe_id, probe_name, snp_id, and snp_name in de_snp_data_by_probe
	* Here is the SQL procedure that is used to populate other fields (edit the trial_name for other studies):
	*
DECLARE
 v_snp_id NUMBER;
 v_probe_name VARCHAR2(255);
 v_probe_id NUMBER;
 v_probe_cnt NUMBER;
BEGIN
 FOR data_rec in (select * from de_snp_data_by_probe where trial_name = 'ADNI' order by snp_data_by_probe_id)
 LOOP
   select count(1) into v_probe_cnt from de_snp_probe where snp_name = data_rec.snp_name and vendor_name = 'Illumina';
   
   IF v_probe_cnt = 1 THEN
      select snp_probe_id, snp_id, probe_name into v_probe_id, v_snp_id, v_probe_name from de_snp_probe where snp_name = data_rec.snp_name and vendor_name = 'Illumina';
	 
      update de_snp_data_by_probe set probe_id = v_probe_id, probe_name = v_probe_name, snp_id = v_snp_id, snp_name = data_rec.snp_name where snp_data_by_probe_id = data_rec.snp_data_by_probe_id;
      commit;
   END IF;
 END LOOP;
END;

	* @param sortedDataDirName
	* @param trialName
	* @param dataByProbeLoadingFile
	*/
   void loadDataByProbe (String sortedDataDirName, String trialName, String dataByProbeLoadingFile) {
		if (sortedDataDirName.endsWith(File.separator) == false) {
			sortedDataDirName = sortedDataDirName + File.separator;
		}
		
	   List<String> subjectIdNameList = new ArrayList<String>();
	   int idx = 0;
	   sql.eachRow("select * from de_subject_snp_dataset where trial_name = ? order by subject_snp_dataset_id", [trialName]) {row ->
		   idx ++;
		   // sql.execute("insert into de_snp_data_dataset_loc values (seq_snp_data_id.nextval, ?, ?, ?)", [trialName, row.subject_snp_dataset_id, idx]);
		   subjectIdNameList.add(row.subject_id);
	   }
	   
	   List<BufferedReader> dataReaderList = new ArrayList<BufferedReader>();
	   for (String subjectId : subjectIdNameList) {
		   String dataFileName = sortedDataDirName + trialName + "_" + subjectId + ".txt";
		   BufferedReader dataReader = new BufferedReader(new FileReader(dataFileName));
		   dataReaderList.add(dataReader);
	   }
	   
	   BufferedWriter loadingFileWriter = new BufferedWriter(new FileWriter(dataByProbeLoadingFile));
	   while(true) {
		   StringBuffer dataBuf = new StringBuffer();
		   String currentSnpName = null;
		   String dataLine = null;
		   for(int i = 0; i < dataReaderList.size(); i ++) {
			   BufferedReader dataReader = dataReaderList.get(i);
			   dataLine = dataReader.readLine();
			   if (dataLine == null || dataLine.trim().length() == 0) {
				   dataLine = null;
			   		break;
			   }
			   String[] values = dataLine.split("\t");
			   String snpName = values[0];
			   if (i == 0) {
				   currentSnpName = snpName;
			   }
			   else {
				   if (snpName.equals(currentSnpName) == false)
				   		throw new Exception ("The snp name is not consistent across data files");
			   }
			   if (values[3].length() != 3)
			   		throw new Exception("The copy number value is not in the format of 2.0");
				// The snp data is compacted in the format of [##.##][AB] for copy number and genotype, in the same order as .xcn file
			   dataBuf.append(" " + values[3] + " " + values[4]);
		   }
		   if (dataLine == null)
		   		break;
				   
		   loadingFileWriter.write(currentSnpName + "\t" + trialName + "\t" + dataBuf.toString() + "\n");
	   }
	   
	   for (BufferedReader dataReader : dataReaderList) {
		   dataReader.close();
	   }
	   loadingFileWriter.close();
   }
   
	public static void main(String[] args) {
		SnpDataLoading_ADNI sdl = new SnpDataLoading_ADNI();		
		
		// extract parameters
		File path = new File(SnpDataLoading_ADNI.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		Properties props = sdl.loadConfiguration(path.getParent() + File.separator + "SnpViewer_ADNI.properties");
		
		// create db connection object
		sdl.getSql(props.get("oracle_server"), props.get("oracle_port"), props.get("oracle_sid"), 
				props.get("oracle_user"), props.get("oracle_password"));
		
		/*
		String cnvDataFileName = props.get("cnv_data_file");
		String rawDataDirName = props.get("raw_data_dir");
		String mergedDataDirName = props.get("merged_data_dir");
		sdl.mergePennCNVToRawData(cnvDataFileName, rawDataDirName, mergedDataDirName);
		*/
		
		/*
		String mergedDataDirName = props.get("merged_data_dir");
		sdl.checkDataFileConsistency(mergedDataDirName);
		*/
		
		/*
		String mergedDataDirName = props.get("merged_data_dir");
		String mappedDataDirName = props.get("mapped_data_dir");
		sdl.createHg19Map(mergedDataDirName, mappedDataDirName);
		*/
			
		/*
		String mappedFileName = props.get("mapped_hg19_snp_file");
		String orderMappingFileName = props.get("order_mapping_file");
		sdl.createSortOrderFile(mappedFileName, orderMappingFileName);
		*/
		
		/*
		String mergedDataDirName = props.get("merged_data_dir");
		String orderMappingFileName = props.get("order_mapping_file");
		String sortedDataDirName = props.get("sorted_data_dir");
		sdl.sortDataFiles(orderMappingFileName, mergedDataDirName, sortedDataDirName);
		*/
			
		/*
		String trialName = props.get("trial_name");
		String platformName = props.get("platform_name");
		String sortedDataDirName = props.get("sorted_data_dir");
		sdl.loadDataSet(sortedDataDirName, trialName, platformName);
		*/
		
		/*
		String trialName = props.get("trial_name");
		String platformName = props.get("platform_name");
		String sortedDataDirName = props.get("sorted_data_dir");
		sdl.loadDataByPatient(sortedDataDirName, trialName, platformName);
		*/
		
		
		String trialName = props.get("trial_name");
		String dataByProbeLoadingFileName = props.get("data_by_probe_loading_file");
		String sortedDataDirName = props.get("sorted_data_dir");
		sdl.loadDataByProbe(sortedDataDirName, trialName, dataByProbeLoadingFileName);
		
	}
	
}
