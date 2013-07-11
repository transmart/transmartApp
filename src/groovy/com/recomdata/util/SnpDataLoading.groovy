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

class SnpDataLoading {
	
	private Sql sql = null;
	
	/**
	 * This function split the sorted batch xcn file into multiple data file, one for each dataset
	 * File i/o in Groovy is extremely slow, perhaps because the file is close after every write, and reopened again for writing the next line. Use BufferedWriter
	 * @param pathName
	 * @param fileName
	 */
	void splitBatchXCN(String xcnFileName, String resultFolderName) {
		File xcnFile = new File(xcnFileName);
		
		String xcnFileNameSimple = xcnFile.getName();
		String fileRoot = xcnFileNameSimple.substring(0, xcnFileNameSimple.lastIndexOf("."));
		
		String[] chromStrArray = getChromStrArray();

		List<String> patientList = new ArrayList<String>();
		List<String> datasetList = new ArrayList<String>();
		
		List<BufferedWriter> outAllWriterList = new ArrayList<BufferedWriter>();
		List<BufferedWriter[]> outChromWriterArrayList = new ArrayList<BufferedWriter[]>();
		
		BufferedWriter probeDefWriter = new BufferedWriter(new FileWriter(resultFolderName + File.separator + fileRoot + "_probe_def.txt"));
		
		File patientListFile = new File(resultFolderName + File.separator + fileRoot + "_patient_list.txt");
		File datasetListFile = new File(resultFolderName + File.separator + fileRoot + "_dataset_list.txt");
		
		int numFile = 0;
		xcnFile.eachLine { line ->
			if (line.indexOf("SNP\tChromosome\tPhysicalPosition") >= 0) {
				String[] headers = line.split("\t");
				numFile = (headers.length - 3) / 2;
				
				for (int i = 3; i < headers.length; i = i + 2) {
					String datasetName = headers[i];
					String patientName = datasetName.substring(0, datasetName.length() - 1);
					datasetList.add(datasetName);
					if (patientList.contains(patientName) == false)
						patientList.add(patientName);
						
					outAllWriterList.add(new BufferedWriter(new FileWriter(resultFolderName + File.separator + datasetName + "_all.xcn")));
					
					BufferedWriter[] chromWriterArray = new BufferedWriter[chromStrArray.length];
					outChromWriterArrayList.add(chromWriterArray);
					for (int idxChrom = 0; idxChrom < chromStrArray.length; idxChrom ++) {
						chromWriterArray[idxChrom] = new BufferedWriter(new FileWriter(resultFolderName + File.separator + 
							datasetName + "_" + chromStrArray[idxChrom] + ".xcn"));
					}
				}
				
				for (String patientName : patientList) {
					patientListFile << (patientName + "\n")
				}
				for (String datasetName : datasetList) {
					datasetListFile << (datasetName + "\n");
				}
			}
			else {
				String[] values = line.split("\t");
				String probeName = values[0];
				String chrom = values[1];
				String chromPos = values[2];
				
				probeDefWriter.writeLine(probeName + "\t" + chrom + "\t" + chromPos);
				
				for (int i = 3; i < values.length; i = i + 2) {
					// The fix width for each value is 7 like "AB 1.34" or "NC 0.24". NC for NoCall
					String cn = values[i];
					String gt = values[i + 1];
					
					try {
						Float cnd = new Float(cn);
						if (cnd != null && cnd.isNaN() == false) {
							if (cnd.doubleValue() > 99.99)
								cn = "99.99";
							else
								cn = String.format("%5.2f", cnd.doubleValue());
						}
					}
					catch (Exception e) {}
					
					if (gt.equalsIgnoreCase("NoCall"))
						gt = "NC";
					int idx = (i - 3) / 2;
					BufferedWriter outAllWriter = outAllWriterList.get(idx);
					outAllWriter.writeLine(cn + "\t" + gt);
					BufferedWriter[] outChromWriterArray = outChromWriterArrayList.get(idx);
					Integer chromNumber = getChromNumberFromString(chrom);
					BufferedWriter outChromWriter = outChromWriterArray[chromNumber.intValue() - 1];
					outChromWriter.writeLine(cn + "\t" + gt);
				}
			}
		}
		
		probeDefWriter.close();
		
		for (int i = 0; i < datasetList.size(); i ++) {
			outAllWriterList.get(i).close();
			BufferedWriter[] chromWriterArray = outChromWriterArrayList.get(i);
			for (int idxChrom = 0; idxChrom < chromStrArray.length; idxChrom ++) {
				chromWriterArray[idxChrom].close();
			}
		}
		
	}
	
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
		String[] chromArray = new String[24];
		for (int i  = 0; i < 22; i++) {
			chromArray[i] = Integer.toString(i + 1);
		}
		chromArray[22] = "X";
		chromArray[23] = "Y";
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
	
	
	/**
	 * This function populates the de_subject_snp_dataset table.
	 * Due to workflow consideration of i2b2 data loading, the concept_cd column is left empty, and will be filled later
	 * The dataset name in the dataset list file needs to be consistent with sourcesystem_cd of patient_dimension.
	 * The platform needs to be existing in de_gpl_info
	 * The SnpDataset.sampleType can only be "NORMAL" or "DISEASE",. It is used to generate GenePattern sample info text file.
	 * @param datasetListFileName
	 * @param trialName
	 * @param platformName
	 * @param normalSuffix
	 * @param diseaseSuffix
	 * @param normalSampleType
	 * @param diseaseSampleType
	 */
	void loadDataSet (String datasetListFileName, String trialName, String platformName,
			String normalSuffix, String diseaseSuffix, String genderForAll) throws Exception {
			
		String SAMPLE_TYPE_NORMAL = "NORMAL";
		String SAMPLE_TYPE_DISEASE = "DISEASE";
		
		Map<Long, Long[]> datasetPatientMap = new HashMap<Long, Long[]>();	// Map<Patient Number, [Dataset ID Normal, Dataset ID Disease]>
		
		File datasetListFile = new File(datasetListFileName);
		datasetListFile.eachLine { line ->
			String datasetName = line;
			String patientStr = datasetName.substring(0, datasetName.length() - 2);
			int idx = patientStr.indexOf("_");
			String gsmStr = patientStr.substring(0, idx);
			String subjectIdStr = patientStr.substring(idx + 1);
			
			String sampleType = null;
			String suffix = datasetName.substring(datasetName.length() - 1);
			if (suffix.equalsIgnoreCase(normalSuffix)) {
				sampleType = SAMPLE_TYPE_NORMAL;
			}
			else if (suffix.equalsIgnoreCase(diseaseSuffix)) {
				sampleType = SAMPLE_TYPE_DISEASE;
			}
			else
				throw new Exception("The datasetName suffix " + suffix + " does not match " + normalSuffix + " or " + diseaseSuffix);
				
			Long patientNum = null;
			String gender = null
			sql.eachRow("select * from patient_dimension where sourcesystem_cd = ?", [patientStr]) {row ->
					patientNum = row.patient_num;
					gender = row.sex_cd;
					if (gender == null && genderForAll != null && genderForAll.length() != 0) {
						gender = genderForAll;	// Sometimes the gender information is missing in patient_dimension
					}
			}
			if (patientNum == null) 
				throw new Exception("The patient_num for source id " + patientStr + " does not exist in patient_dimension table");
			
			String stmt = "insert into de_subject_snp_dataset values(seq_snp_data_id.nextval, ?, null, ";
			stmt += "?, ?, ?, null, ?, ?, null, ?)";	// For GSE19539 on ovarian cancer, all patients are female
			def parameters = [datasetName, platformName, trialName, patientNum, subjectIdStr, sampleType, gender];
			sql.execute(stmt, parameters);
			
			Long datasetId = null;
			sql.eachRow("select seq_snp_data_id.currval as datasetId from dual") { row ->
				datasetId = row.datasetId;
			}
			if (datasetId == null) throw new Exception ("failed to get newly created dataset ID for " + datasetName);
			
			Long[] datasetPair = datasetPatientMap.get(patientNum);
			if (datasetPair == null) {
				datasetPair = new Long[2];
				datasetPatientMap.put(patientNum, datasetPair);
			}
			if (sampleType.equals(SAMPLE_TYPE_NORMAL)) {
				datasetPair[0] = datasetId;
			}
			else {
				datasetPair[1] = datasetId;
			}
		}
		
		for (Map.Entry pairEntry : datasetPatientMap) {
			Long[] pair = pairEntry.getValue();
			if (pair[0] != null && pair[1] != null) {	// The data is paired
				sql.execute("update de_subject_snp_dataset set paired_dataset_id = " + pair[1] + " where subject_snp_dataset_id = " + pair[0]);
				sql.execute("update de_subject_snp_dataset set paired_dataset_id = " + pair[0] + " where subject_snp_dataset_id = " + pair[1]);
			}
		}
	}
	
			
	void loadDataByPatient (String datasetListFileName, String trialName, String outPathName, String normalSuffix, 
			 String diseaseSuffix, String normalSampleType, String diseaseSampleType) throws Exception {
		
		File datasetListFile = new File(datasetListFileName);
		datasetListFile.eachLine { line ->
			String datasetName = line;
			String patientStr = datasetName.substring(0, datasetName.length() - 1);
			int idx = patientStr.indexOf("_");
			String gsmStr = patientStr.substring(0, idx);
			String subjectIdStr = patientStr.substring(idx + 1);
			String patientSourceStr = trialName + subjectIdStr;
			
			String sampleType = null;
			String suffix = datasetName.substring(datasetName.length() - 1);
			if (suffix.equalsIgnoreCase(normalSuffix)) {
				sampleType = normalSampleType // SnpDataset.SAMPLE_TYPE_NORMAL;
			}
			else if (suffix.equalsIgnoreCase(diseaseSuffix)) {
				sampleType = diseaseSampleType //SnpDataset.SAMPLE_TYPE_DISEASE;
			}
			else
				throw new Exception("The datasetName suffix " + suffix + " does not match " + normalSuffix + " or " + diseaseSuffix);

			Long datasetId = null;
			Long patientNum = null;
			String stmt = "select a.patient_num as patient_num, b.subject_snp_dataset_id as dataset_id ";
			stmt += "from PATIENT_DIMENSION a, de_subject_snp_dataset b where a.patient_num = b.patient_num and a.sourcesystem_cd = ? ";
			stmt += "and b.trial_name = ? and b.sample_type = ?";
			sql.eachRow(stmt, [patientSourceStr, trialName, sampleType]) { row ->
				datasetId = row.dataset_id;
				patientNum = row.patient_num;
			}
			if (datasetId == null) throw new Exception("The dataset for " + datasetName + " does not exist in database");
			
			String chromAllFileName = outPathName + File.separator + datasetName + "_all.xcn";
			
			String chromAllStr = null;
			if((new File(chromAllFileName)).exists()){
				chromAllStr = (new File(chromAllFileName)).getText();
				sql.execute("insert into de_snp_data_by_patient values (seq_snp_data_id.nextval, ?, ?, ?, 'ALL', ?)",
					[datasetId, trialName, patientNum, chromAllStr]);
				chromAllStr = null;
			}		
			
			for (int i = 1; i <= 24; i ++) {
				String chrom = Integer.toString(i);
				if (i == 23) chrom = "X";
				else if (i == 24) chrom = "Y";
				
				String chromFileName = outPathName + File.separator + datasetName + "_" + chrom + ".xcn";
				
				String chromStr = null
				if((new File(chromFileName)).exists()){
					chromStr = (new File(chromFileName)).getText();
					sql.execute("insert into de_snp_data_by_patient values (seq_snp_data_id.nextval, ?, ?, ?, ?, ?)",
						[datasetId, trialName, patientNum, chrom, chromStr]);
					chromStr = null
				}
			}
		}
	}
	
	void loadSNPProbeSortedDef(String probeDefFileName, String platformName) {
		
		File probeDefFile = new File(probeDefFileName);
		
		StringBuffer[] chromBufList = new StringBuffer[24];
		int[] chromCountList = new int[24];
		int chromCountTotal = 0;
		probeDefFile.eachLine { line ->
			if (line != null && line.trim().length() != 0) {
				String[] values = line.split("\t");
				String chrom = values[1];
				Integer chromIdx = getChromNumberFromString(chrom);
				StringBuffer chromBuf = chromBufList[chromIdx.intValue() - 1];
				if (chromBuf == null) {
					chromBuf = new StringBuffer();
					chromBufList[chromIdx.intValue() - 1] = chromBuf;
					chromCountList[chromIdx.intValue() - 1] = 0;	// set the initial count to 0
				}
				chromBuf.append(line + "\n");
				chromCountList[chromIdx.intValue() - 1] ++;
				chromCountTotal ++;
			}
		}
		
		sql.execute("insert into de_snp_probe_sorted_def values (seq_snp_data_id.nextval, ?, ?, ?, ?)",
			[platformName, chromCountTotal, "ALL", probeDefFile.getText()]);

		String[] chromStrList = getChromStrArray();
		for (int i = 0; i < chromStrList.size(); i ++) {
			StringBuffer chromBuf = chromBufList[i];
			if (chromBuf != null && chromBuf.length() != 0) {
				sql.execute("insert into de_snp_probe_sorted_def values (seq_snp_data_id.nextval, ?, ?, ?, ?)",
					[platformName, chromCountList[i], chromStrList[i], chromBuf.toString()]);
				
				chromBuf = null; // release the memory
			}
		}
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
	 * @param datasetFileName
	 * @param trialName
	 */
	void correctPatientNum(String datasetFileName, String trialName) {
		Sql sql = getSql();
		
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
				
				String stmt2 = "update de_subject_snp_dataset set patient_num = ? where subject_id = ?";
				sql.execute(stmt2, [patientNum, patientEnd]);
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
	

	/** This function is to merge the genotyping call given by Affy GTC, with the signal intensity for channel A and B, given by
	 *  GenePattern SNPFileCreator module.
	 *  This function is only needed for SNP Mapping 500K (250K NSP and 250K STY). For SNP Mapping 500K, Affy GTC only output
	 *  contrast and strength, instead of signal for channel A and B, as for SNP Generation 6.0.
	 *  GenePattern SNPFileCreator has its own share of bugs. It fails to do genotyping, and its chromosomal positions are based
	 *  on hg18, not on up-to-date hg19 as in Affy GTC.
	 *  This function needs large memory, in exchange for a simpler code structure.
	 */
	void mergeAffyGTCGenotypeWithGPSignal(String affyFileName, String gpFileName, String resultFileName) throws Exception {
		String affyHeader = null;
		String gpHeader = null;
		List<String> affyStringList = new ArrayList<String>();
		List<String> gpStringList = new ArrayList<String>();
		
		File affyFile = new File(affyFileName);
		affyFile.eachLine { line ->
			if (line.startsWith("#")) {
				// Skip the annotation lines
			}
			else if (line.startsWith("Probe Set ID")) {
				affyHeader = line.trim();
			}
			else if (line.startsWith("AFFX-")) {
				// Skip the AFFX probes
			}
			else if (line.startsWith("SNP_A-")) {
				affyStringList.add(line.trim());
			}
		}
		
		File gpFile = new File(gpFileName);
		gpFile.eachLine { line ->
			if (line.startsWith("SNP\tChromosome\tPhysicalPosition")) {
				gpHeader = line.trim();
			}
			else if (line.startsWith("AFFX-")) {
				// Skip the AFFX probes
			}
			else if (line.startsWith("SNP_A-")) {
				gpStringList.add(line.trim());
			}
		}

		// The number of SNP probes should be equal for Affy and GP files
		if (affyStringList.size() != gpStringList.size())
			throw new Exception("The Affy file has " + affyStringList.size() + " SNP probes, while the GP file has " + 
				gpStringList.size() + " SNP probes.");
		
		List<String> datasetList = new ArrayList<String>();
		String[] columnArray = gpHeader.split("\t");
		for (int i = 3; i < columnArray.length; i = i + 3) {
			String columnName = columnArray[i];
			int idx = columnName.indexOf("_");
			datasetList.add(columnName.substring(0, idx + 2));
		}
		String[] affyColumnArray = affyHeader.split("\t");
		for (int j = 0; j < datasetList.size(); j ++) {
			String columnName = affyColumnArray[j + 1];
			if (columnName.startsWith(datasetList.get(j)) == false) { // Make sure the headers of affy and GP files are consistent
				throw new Exception("The " + j + "-th dataset name in Affy file is " + columnName + 
					", but the corresponding GP file dataset name is " + datasetList.get(j));
			}
		}
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(resultFileName));
			// Write out the header, exactly the same as the GP file
			writer.write(gpHeader + "\n");
			
			for (int k = 0; k < gpStringList.size(); k ++) {
				String[] gpValueList = gpStringList.get(k).split("\t");
				String[] affyValueList = affyStringList.get(k).split("\t");
				String gpSNPName = gpValueList[0];
				String affySNPName = affyValueList[0];
				if (gpSNPName.equalsIgnoreCase(affySNPName) == false) {	// Make sure the SNP Names match
					throw new Exception("The " + k + "-th SNP in GP file is " + gpSNPName +
						", but in Affy file is " + affySNPName);
				}
				String affyChrom = affyValueList[1 + datasetList.size() + 1];
				if (affyChrom.indexOf("---") >= 0)
					continue;	// The SNP probe no longer has a valid chromosomal position. Skip the whole line
				String affyChromPos = affyValueList[1 + datasetList.size() + 2];
				
				StringBuffer buf = new StringBuffer();
				buf.append(gpSNPName + "\t" + affyChrom + "\t" + affyChromPos);
				
				for (int m = 0; m < datasetList.size(); m ++) {
					String signalA = gpValueList[3 + 3 * m];
					String signalB = gpValueList[3 + 3 * m + 1];
					String genotype = affyValueList[1 + m];
					if (genotype.equalsIgnoreCase("NoCall")) genotype = "NC";
					buf.append("\t" + signalA + "\t" + signalB + "\t" + genotype);
				}
				
				writer.write(buf.toString() + "\n");
			}
		}
		catch (Exception e) { throw e;}
		finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
	}
	
	/** This function is to merge the NSP SNP file and STY SNP File. */
	void mergeNspAndStyFiles(String nspFileName, String styFileName, String resultFileName) throws Exception {
		String nspHeader = null;
		String styHeader = null;
		
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(resultFileName));

			Set nspProbeSet = new HashSet();
			Set styProbeSet = new HashSet();
			
			File nspFile = new File(nspFileName);
			nspFile.eachLine { line ->
				if (line.startsWith("SNP\tChromosome\tPhysicalPosition")) {
					nspHeader = line.trim();
				}
				else {
					int idx = line.indexOf("\t");
					String probeName = line.substring(0, idx);
					if (nspProbeSet.contains(probeName)) {
						print ("The NSP probe " + probeName + " is duplicated in the file");
					}
					nspProbeSet.add(probeName);
				}
				
				writer.write(line + "\n"); // Write out everything in the NSP file, including the header
			}
			
			File styFile = new File(styFileName);
			styFile.eachLine { line ->
				if (line.startsWith("SNP\tChromosome\tPhysicalPosition")) {
					styHeader = line.trim();
				}
				else {
					int idx = line.indexOf("\t");
					String probeName = line.substring(0, idx);
					if (styProbeSet.contains(probeName)) {
						print ("The STY probe " + probeName + " is duplicated in the file");
					}
					styProbeSet.add(probeName);
					if (nspProbeSet.contains(probeName)) {
						print ("The STY probe " + probeName + " is duplicated in the NSP file");
					}
					writer.write(line + "\n"); // Only write out SNP data lines, excluding the header
				}
			}
			
			// Make sure the two files have the same header
			if (nspHeader.trim().equals(styFileName.trim()) == false) {
				print("Error: The headers of two files are not the same:\n" + nspHeader + "\n" + styHeader + "\n\n");
			}
		}
		catch (Exception e) { throw e;}
		finally {
			if (writer != null) {
				writer.flush();
				writer.close();
			}
		}
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
	 * This function is to generate the data file used by SQL Loader to populate de_snp_data_by_probe table
	 * The SQL procedure that is used to populate other fields (change the trial_name):
	 * 
DECLARE
  snp_id_rec NUMBER;
  snp_name_rec VARCHAR2(255);
  probe_id_rec NUMBER;
  probe_cnt NUMBER;
BEGIN
  FOR data_rec in (select * from de_snp_data_by_probe where trial_name = 'GSE19539' order by snp_data_by_probe_id)
  LOOP
    select count(1) into probe_cnt from de_snp_probe where probe_name = data_rec.probe_name;
    
    IF probe_cnt = 1 THEN
      select snp_probe_id, snp_id, snp_name into probe_id_rec, snp_id_rec, snp_name_rec from de_snp_probe where probe_name = data_rec.probe_name;
      
      update de_snp_data_by_probe set probe_id = probe_id_rec, snp_id = snp_id_rec, snp_name = snp_name_rec where snp_data_by_probe_id = data_rec.snp_data_by_probe_id;
      commit;
    END IF;
  END LOOP;
END;

	 * @param xcnByPatientDirName
	 * @param probeDefFileName
	 * @param trialName
	 * @param dataByProbeLoadingFile
	 */
	void loadDataByProbe (String xcnByPatientDirName, String probeDefFileName, String trialName,
		String dataByProbeLoadingFile) {
		List<String> datasetNameList = new ArrayList<String>();
		
		int datasetLocationIdx = 0;
		sql.eachRow("select * from de_subject_snp_dataset where trial_name = ? order by subject_snp_dataset_id", [trialName]) {row ->
			String datasetName = row.dataset_name;
			datasetNameList.add(datasetName);
			datasetLocationIdx ++;
		}
		
		List<BufferedReader> xcnReaderList = new ArrayList<BufferedReader>();
		for (String datasetName : datasetNameList) {
			String xcnFileName = xcnByPatientDirName + File.separator + datasetName + "_all.xcn";
			BufferedReader xcnReader = new BufferedReader(new FileReader(xcnFileName));
			xcnReaderList.add(xcnReader);
		}
		
		BufferedWriter loadingFileWriter = new BufferedWriter(new FileWriter(dataByProbeLoadingFile));
		BufferedReader probeDefReader = new BufferedReader(new FileReader(probeDefFileName));
		String lineProbeDef = "Dummy String to start the loop";
		while((lineProbeDef = probeDefReader.readLine()) != null && lineProbeDef.trim().length() != 0) {
			String[] defValues = lineProbeDef.split("\t");
			String probeName = defValues[0];
			
			StringBuffer xcnDataBuf = new StringBuffer();
			for(BufferedReader xcnReader : xcnReaderList) {
				String xcnValues = xcnReader.readLine();
				xcnValues = xcnValues.replace("\t", "");
				// The snp data is compacted in the format of [##.##][AB] for copy number and genotype, in the same order as .xcn file
				xcnDataBuf.append(xcnValues);
			}
			loadingFileWriter.writeLine(probeName + "\t" + trialName + "\t" + xcnDataBuf.toString());
		}
		
		loadingFileWriter.close();
		probeDefReader.close();
		for(BufferedReader xcnReader : xcnReaderList) {
			xcnReader.close();
		}
	}
	
	public void addRsIdSortedDef(String annotLoadingFileName) {
		Map<String, String> probeRsIdMap = new HashMap<String, String>();
		// The annotation loading file is snp id, probe, chrom information extracted from Affy annotation file
		File annotLoadingFile = new File(annotLoadingFileName);
		annotLoadingFile.eachLine { line ->
			if (line != null && line.trim().size() != 0) {
				String[] values = line.split("\t");
				String rsId = values[0];
				String probeName = values[3];
				probeRsIdMap.put(probeName, rsId);
			}
		}
		
		sql.eachRow("select * from de_snp_probe_sorted_def order by snp_probe_sorted_def_id") {row ->
			Long defId = row.snp_probe_sorted_def_id;
			print("Get definition for id " + defId + "\n\n");
			
			StringBuffer rsIdDefBuf = new StringBuffer();
			Clob clob = row.probe_def;
			String probeDefStr = clob.getAsciiStream().getText();
			String[] probeDefLines = probeDefStr.split("\n");
			for (String lineStr : probeDefLines) {
				String[] probeValues = lineStr.split("\t");
				String probeName = probeValues[0];
				String chrom = probeValues[1];
				String chromPos = probeValues[2];
				String rsId = probeRsIdMap.get(probeName);
				if (rsId != null) {
					rsIdDefBuf.append(rsId + "\t" + chrom + "\t" + chromPos + "\t\n");
				}
				else {
					rsIdDefBuf.append(lineStr + "\n");
				}
			}
			
			sql.execute("update de_snp_probe_sorted_def set snp_id_def = ? where snp_probe_sorted_def_id = ?", 
				[rsIdDefBuf.toString(), defId]);
		}
	}
		
	public static void main(String[] args) {
		SnpDataLoading sdl = new SnpDataLoading();		
		
		// extract parameters
		File path = new File(SnpDataLoading.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		Properties props = sdl.loadConfiguration(path.getParent() + File.separator + "SnpViewer.properties");
		
		// create db connection object
		sdl.getSql(props.get("oracle_server"), props.get("oracle_port"), props.get("oracle_sid"), 
				props.get("oracle_user"), props.get("oracle_password"));
		
		/*
		def datasetListFileName = outPathName + File.separator + datasetListName
		sdl.loadDataSet(datasetListFileName, props.get("trialName"), props.get("platformName"), props.get("assayName"),
			 props.get("normalSuffix"), props.get("diseaseSuffix"), props.get("normalSampleType"), props.get("diseaseSampleType"));
		*/
	
		
		/*
		sdl.loadDataByPatient(datasetListFileName, props.get("trialName"), props.get("outPathName"), props.get("normalSuffix"), 
				props.get("diseaseSuffix"), props.get("normalSampleType"), props.get("diseaseSampleType"));
		*/

		/*
		sdl.loadSNPProbeSortedDef(props.get("outPathName") + File.separator + probeDefFileName, props.get("platformName"));
		*/

		/*
		String affyFileName = props.get("affy_genotype_file");
		String gpSignalFileName = props.get("gp_signal_file");
		String resultFileName = props.get("genotype_signal_merged_file");
		sdl.mergeAffyGTCGenotypeWithGPSignal(affyFileName, gpSignalFileName, resultFileName);
		*/
			
		/*
		String nspFileName = props.get("affy_500k_nsp_snp_file");
		String styFileName = props.get("affy_500k_sty_snp_file");
		String resultFileName = props.get("affy_500k_merged_snp_file");
		sdl.mergeNspAndStyFiles(nspFileName, styFileName, resultFileName);
		*/
		
		/*
		String snpFileName = props.get("affy_500k_merged_snp_file");
		String sampleFileName = props.get("gp_sample_file");
		String diseaseSuffix = props.get("disease_suffix");
		String normalSuffix = props.get("normal_suffix");
		String gender = props.get("patient_gender");	// TODO: how to get each patient's gender, and output to sample info file.
		sdl.generateGPSampleFile(snpFileName, sampleFileName, diseaseSuffix, normalSuffix, gender);
		*/
		
		/*
		String xcnFileName = props.get("xcn_file_for_all_datasets");
		String resultFolder = props.get("xcn_file_split_result_folder");
		sdl.splitBatchXCN(xcnFileName, resultFolder);
		*/
		
		/*
		String datasetListFileName = props.get("dataset_list_file");
		String trialName = props.get("trial_name");
		String platformName = props.get("platform_name");
		String normalSuffix = props.get("normal_suffix");
		String diseaseSuffix = props.get("disease_suffix");
		String genderForAll = props.get("patient_gender");
		sdl.loadDataSet(datasetListFileName, trialName, platformName, 
			normalSuffix, diseaseSuffix, genderForAll);
		*/
		
		String xcnByPatientDirName = props.get("xcn_by_patient_dir");
		String probeDefFileName = props.get("probe_def_file");
		String trialName = props.get("trial_name");
		String dataByProbeLoadingFile = props.get("data_by_probe_loading_file");
		sdl.loadDataByProbe(xcnByPatientDirName, probeDefFileName, trialName, dataByProbeLoadingFile);
		
		/*
		String annotLoadingFileName = props.get("affy_annotation_data_file");
		sdl.addRsIdSortedDef(annotLoadingFileName);
		*/
	}
	
}
