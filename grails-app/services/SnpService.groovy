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
  

import i2b2.GeneWithSnp
import i2b2.SnpDataByProbe
import i2b2.SnpDataset;
import i2b2.SnpDatasetListByProbe
import i2b2.SnpInfo
import i2b2.SnpProbeSortedDef;
import i2b2.StringLineReader

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import com.recomdata.export.SnpViewerFiles;

class SnpService {

	def dataSource;
	
	public void getSNPViewerDataByPatient(List<Long>[] patientNumListArray, String chroms, SnpViewerFiles snpFiles) throws Exception {
		if (snpFiles == null) throw new Exception("The SNPViewerFiles object is not instantiated");
		
		// Get SQL query String for all the subject IDs
		String subjectListStr = "";
		
		//Loop through the array of Lists.
		for(int i = 0;i< patientNumListArray.length; i++)
		{
			//Add a comma to seperate the lists.
			if(subjectListStr != "") subjectListStr += ","
			
			//This is a list of patients, add it to our string.
			subjectListStr += patientNumListArray[i].join(",");
		}

		Map<Long, SnpDataset[]> snpDatasetBySubjectMap = new HashMap<Long, SnpDataset[]>();
		getSnpDatasetBySubjectMap(snpDatasetBySubjectMap, subjectListStr);
		if (snpDatasetBySubjectMap == null || snpDatasetBySubjectMap.size() == 0) {
			throw new Exception("Error: The selected cohorts do not have SNP data.");
		}

		StringBuffer sampleInfoBuf = new StringBuffer();
		List<SnpDataset> datasetList = new ArrayList<SnpDataset>();
		List<String> datasetNameForSNPViewerList = new ArrayList<String>();
		getSnpSampleInfo(datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf);

		Map<Long, Map<String, String>> snpDataByDatasetByChrom = getSNPDataByDatasetByChrom(subjectListStr, chroms);

		/** There is a bug in GenePattern SNPViewer. If there is no probe position information for previous chrom,
		 * The display of chroms becomes erratic.
		 * The work-around is to enter dummy data for starting and ending probes of the absent chrom, so
		 SNPViewer can display the chrom number correctly. Need to build a list of chroms to the last used chrom*/

		List<String> neededChroms = getSortedChromList(chroms);
		String[] allChroms = getAllChromArray();
		Map<String, String[]> allChromEndProbeLines = getChromEndProbeLineMap();
		String lastChrom = neededChroms.get(neededChroms.size() - 1);

		String platform = datasetList.get(0).platformName;
		Map<String, SnpProbeSortedDef> probeDefMap = getSNPProbeDefMap(platform, chroms);

		BufferedWriter dataWriter = new BufferedWriter(new FileWriter(snpFiles.dataFile));

		// Write the header column
		dataWriter.write("SNP\tChromosome\tPhysicalPosition");
		for (String datasetName : datasetNameForSNPViewerList) {
			dataWriter.write("\t" + datasetName + "\t" + datasetName + " Call");
		}
		dataWriter.write("\n");

		for (String chrom : neededChroms) {
			SnpProbeSortedDef probeDef = probeDefMap.get(chrom);
			if (probeDef != null) {	// This chrom is selected by user
				// Create the list of BufferedReader for SNP data for each dataset for this chrom
				List<StringLineReader> snpDataReaderList = new ArrayList<StringLineReader>();
				for (SnpDataset dataset : datasetList) {
					Map<String, String> snpDataByChrom = snpDataByDatasetByChrom.get(dataset.id);
					String snpDataStr = snpDataByChrom.get(chrom);
					StringLineReader dataReader = new StringLineReader(snpDataStr);
					snpDataReaderList.add(dataReader);
				}

				String probeDefStr = probeDef.snpIdDef;
				Integer numProbe = probeDef.getNumProbe();
				StringLineReader probeReader = new StringLineReader(probeDefStr);
				for (int idx = 0; idx < numProbe.intValue(); idx ++) {
					String probeLine = probeReader.readLine();
					if (probeLine == null || probeLine.trim().length() == 0)
						throw new Exception("The number " + idx + " line in probe definition file for chromosome " + chrom + " is empty");
					dataWriter.write(probeLine);

					for (StringReader dataReader : snpDataReaderList) {
						dataWriter.write("\t" + dataReader.readLine());
					}
					dataWriter.write("\n");
				}
			}
			else { // This chrom need dummy data for the starting and ending probes
				String[] endProbeLines = allChromEndProbeLines.get(chrom);
				dataWriter.write(endProbeLines[0]);
				for (SnpDataset dataset : datasetList) {
					dataWriter.write("\t2.0\tNC");
				}
				dataWriter.write("\n");

				dataWriter.write(endProbeLines[1]);
				for (SnpDataset dataset : datasetList) {
					dataWriter.write("\t2.0\tNC");
				}
				dataWriter.write("\n");
			}
			if (chrom.equals(lastChrom)) break;	// Stop at the last needed chrom
		}
		snpFiles.sampleFile << sampleInfoBuf;
		dataWriter.close();

	}

	
	/**
	* For now the patients have to be in the same trial, for the sake of simplicity.
	*
	* @param subjectIds1
	* @param subjectIds2
	* @param geneSearchIdList
	* @param snpFiles
	*/
   public void getSNPViewerDataByProbe(List<Long>[] patientNumListArray, List<Long> geneSearchIdList, List<String> geneNameList,
   List<String> snpNameList, SnpViewerFiles snpFiles, StringBuffer geneSnpPageBuf) throws Exception {
	   if (snpFiles == null) throw new Exception("The SNPViewerFiles object is not instantiated");
	   if (geneSnpPageBuf == null) throw new Exception("The geneSnpPageBuf object is not instantiated");

	   //This object is seemingly used to initialize objects which get assigned to local variables later. 
	   SnpDatasetListByProbe allDataByProbe = new SnpDatasetListByProbe();

	   // Get SQL query String for all the subject IDs
	   String subjectListStr = "";
	   
		//Loop through the array of Lists.
		for(int i = 0;i< patientNumListArray.length; i++)
		{
			//Add a comma to seperate the lists.
			if(subjectListStr != "") subjectListStr += ","
			
			//This is a list of patients, add it to our string.
			subjectListStr += patientNumListArray[i].join(",");
		}

	   // Get the gene-snp map, and the snp set related to all the user-input genes.
	   // Map<chrom, Map<chromPos of Gene, GeneWithSnp>>
	   Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForGene = new HashMap<String, SortedMap<Long, Map<Long, GeneWithSnp>>>();
	   Map<Long, GeneWithSnp> geneEntrezIdMap = new HashMap<Long, GeneWithSnp>();
	   Map<String, GeneWithSnp> geneNameToGeneWithSnpMap = new HashMap<String, GeneWithSnp>();
	   getGeneWithSnpMapForGenes(geneSnpMapForGene, geneEntrezIdMap, geneNameToGeneWithSnpMap, geneSearchIdList);

	   // Get the gene-snp map for the user-selected SNPs.
	   Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForSnp = new HashMap<String, SortedMap<Long, Map<Long, GeneWithSnp>>>();
	   getGeneWithSnpMapForSnps(geneSnpMapForSnp, snpNameList);

	   Collection<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>> geneSnpMapList = new ArrayList<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>>();
	   geneSnpMapList.add(geneSnpMapForGene);
	   geneSnpMapList.add(geneSnpMapForSnp);
	   Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap = mergeGeneWithSnpMap(geneSnpMapList);

	   if (allGeneSnpMap == null || allGeneSnpMap.size() == 0)
		   throw new Exception("There is no SNP data for selected genes and SNP IDs");

	   // Generate the web page to display the Gene and SNP selected by User
	   getSnpGeneAnnotationPage(geneSnpPageBuf, allGeneSnpMap, geneEntrezIdMap, geneNameToGeneWithSnpMap, geneNameList, snpNameList);

	   Map<Long, SnpDataset[]> snpDatasetBySubjectMap = allDataByProbe.snpDatasetBySubjectMap;
	   //Fill the SnpDatasets. We use the map to keep track of the patients.
	   getSnpDatasetBySubjectMap (snpDatasetBySubjectMap, subjectListStr);

	   StringBuffer sampleInfoBuf = new StringBuffer();
	   List<SnpDataset> datasetList = allDataByProbe.datasetList;
	   List<String> datasetNameForSNPViewerList = allDataByProbe.datasetNameForSNPViewerList;
	   getSnpSampleInfo(datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf);

	   // Get the compacted SNP data and insert them into the map, organized by chrom, and further ordered by chrom position
	   Map<String, List<SnpDataByProbe>> snpDataByChromMap = allDataByProbe.snpDataByChromMap;

	   //
	   Set<Long> allSnpIdSet = getSnpSet(allGeneSnpMap);
	   getSNPDataByProbeByChrom(datasetList, snpDataByChromMap, allSnpIdSet);

	   // Write the sample info text file for SNPViewer
	   File sampleFile = snpFiles.getSampleFile();
	   sampleFile << sampleInfoBuf.toString();

	   // Write the xcn file
	   File dataFile = snpFiles.getDataFile();
	   BufferedWriter dataWriter = new BufferedWriter(new FileWriter(dataFile));
	   // Write the header column
	   dataWriter.write("SNP\tChromosome\tPhysicalPosition");
	   for (String datasetName : datasetNameForSNPViewerList) {
		   dataWriter.write("\t" + datasetName + "\t" + datasetName + " Call");
	   }
	   dataWriter.write("\n");
	   // Write the data section, by chrom. Stop at the last used chrom in snpDataByChromMap
	   List<String> sortedChromList = getSortedChromList(snpDataByChromMap.keySet());
	   String lastChrom = sortedChromList.get(sortedChromList.size() - 1);
	   String[] allChromArray = getAllChromArray();
	   Map<String, String[]> chromEndProbeLineMap = getChromEndProbeLineMap();
	   for (String chrom_it : allChromArray) {
		   List<SnpDataByProbe> snpDataByProbeList = snpDataByChromMap.get(chrom_it);
		   if (snpDataByProbeList != null && snpDataByProbeList.size() != 0) {
			   // SNPViewer has problem rendering single SNP without boundary blank SNPs.
			   String[] chromEndProbeLine = chromEndProbeLineMap.get(chrom_it);
			   dataWriter.write(chromEndProbeLine[0]);
			   for (int i = 0; i < datasetList.size(); i ++ ) {
				   dataWriter.write("\t2.0\tNC");
			   }
			   dataWriter.write("\n");

			   for (SnpDataByProbe snpDataByProbe : snpDataByProbeList) {
				   dataWriter.write(snpDataByProbe.snpName + "\t" + chrom_it + "\t" + snpDataByProbe.chromPos);
				   String[][] dataArray = snpDataByProbe.dataArray;
				   for (int i = 0; i < datasetList.size(); i ++ ) {
					   dataWriter.write("\t" + dataArray[i][0].trim() + "\t" + dataArray[i][1]);
				   }
				   dataWriter.write("\n");
			   }

			   dataWriter.write(chromEndProbeLine[1]);
			   for (int i = 0; i < datasetList.size(); i ++ ) {
				   dataWriter.write("\t2.0\tNC");
			   }
			   dataWriter.write("\n");
		   }
		   else {	// There is no snp data needed for this chrom
			   String[] chromEndProbeLine = chromEndProbeLineMap.get(chrom_it);
			   for (int idxEndProbe = 0; idxEndProbe < 2; idxEndProbe ++) {
				   dataWriter.write(chromEndProbeLine[idxEndProbe]);
				   for (int i = 0; i < datasetList.size(); i ++ ) {
					   dataWriter.write("\t2.0\tNC");
				   }
				   dataWriter.write("\n");
			   }
		   }

		   if (chrom_it.equals(lastChrom)) break;
	   }
	   dataWriter.close();
   }
	
	
	
	/********************************************************************/
	//These are all helper methods.
	/********************************************************************/
	
	/**
	 * 
	 * @param subjectIdStr
	 * @return
	 */
	private List<Long> getPatientNumListFromSubjectIdStr(String subjectIdStr) {
		if (subjectIdStr == null || subjectIdStr.length() == 0) return null;
		List<Long> patientNumList = new ArrayList<Long>();
		String[] subjectArray = subjectIdStr.split(",");
		for (String subjectId : subjectArray) {
			Long patientNum = new Long(subjectId.trim());
			patientNumList.add(patientNum);
		}
		return patientNumList;
	}

	
	void getSnpDatasetBySubjectMap (Map<Long, SnpDataset[]> snpDatasetBySubjectMap, String subjectListStr) {
		if (snpDatasetBySubjectMap == null || subjectListStr == null || subjectListStr.length() == 0) return;

		// The display concept name like "Normal Blood Lymphocyte" for dataset with conceptId of "1222211"
		Map<String, String> conceptIdToDisplayNameMap = new HashMap<String, String>();

		// Get the dataset list from subject lists, and organize them in pairs for each patient.
		String commonPlatformName = null;	// To make sure there is noly one platform among all the datasets
		String commonTrialName = null;	// For now only one trial is allowed.

		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlStr = "select * from de_subject_snp_dataset where patient_num in (" + subjectListStr + ")";
		sql.eachRow(sqlStr) { row ->
			SnpDataset snpDataset = new SnpDataset();
			snpDataset.id = row.subject_snp_dataset_id;
			snpDataset.datasetName = row.dataset_name;
			snpDataset.conceptId = row.concept_cd;
			snpDataset.conceptName = getConceptDisplayName(snpDataset.conceptId, conceptIdToDisplayNameMap);
			snpDataset.platformName = row.platform_name;

			if (commonPlatformName == null)
			{
				commonPlatformName = snpDataset.platformName;
			}
			else if (commonPlatformName.equals(snpDataset.platformName) == false)
			{
				throw new Exception ("The platform for SnpDataset " + snpDataset.datasetName + ", " + snpDataset.platformName + ", is different from previous platform " + commonPlatformName);
			}

			snpDataset.trialName = row.trial_name;
			if (commonTrialName == null) commonTrialName = snpDataset.trialName;
			else if (commonTrialName.equals(snpDataset.trialName) == false) {
				throw new Exception ("The trial for SnpDataset " + snpDataset.datasetName + ", " + snpDataset.trialName + ", is different from previous trial " + commonTrialName);
			}
			snpDataset.patientNum = row.patient_num;
			snpDataset.timePoint = row.timepoint;
			snpDataset.subjectId = row.subject_id;
			snpDataset.sampleType = row.sample_type;
			snpDataset.pairedDatasetId = row.paired_dataset_id;
			snpDataset.patientGender = row.patient_gender;

			SnpDataset[] snpDatasetPair = snpDatasetBySubjectMap.get(snpDataset.patientNum);
			if (snpDatasetPair == null) {
				snpDatasetPair = new SnpDataset[2];
				snpDatasetBySubjectMap.put(snpDataset.patientNum, snpDatasetPair);
			}
			if (snpDataset.sampleType.equals(SnpDataset.SAMPLE_TYPE_NORMAL)) {
				snpDatasetPair[0] = snpDataset;
			}
			else {
				snpDatasetPair[1] = snpDataset;
			}
		}
	}

	void getSnpSampleInfo(List<SnpDataset> datasetList, List<String> datasetNameForSNPViewerList,List<Long>[] patientNumListArray, Map<Long, SnpDataset[]> snpDatasetBySubjectMap, StringBuffer sampleInfoBuf)
	{
		if (datasetList == null) throw new Exception("The datasetList is null");
		if (patientNumListArray == null) throw new Exception("The patient number list for two subsets cannot be null");
		if (sampleInfoBuf == null) throw new Exception("The StringBuffer for sample info text needs to instantiated");
		// Organize the datasetList and SNPViewer dataset name List, also generate the SNPViewer sample info text in this pass
		sampleInfoBuf.append("Array\tSample\tType\tPloidy(numeric)\tGender\tPaired");
		for (int idxSubset = 0; idxSubset < 1; idxSubset ++) {
			if (patientNumListArray[idxSubset] != null) {
				for (Long patientNum : patientNumListArray[idxSubset]) {
					SnpDataset[] snpDatasetPair = snpDatasetBySubjectMap.get(patientNum.longValue());
					println(snpDatasetPair)
					if (snpDatasetPair != null) {
						String datasetNameForSNPViewer_1 = null;
						String datasetNameForSNPViewer_2 = null;

						if (snpDatasetPair[0] != null) {	// Has the control dataset
							SnpDataset snpDataset_1 = snpDatasetPair[0];
							datasetNameForSNPViewer_1 = "S" + (idxSubset + 1) + "_" + snpDataset_1.datasetName;
							datasetList.add(snpDataset_1);
							datasetNameForSNPViewerList.add(datasetNameForSNPViewer_1);
							sampleInfoBuf.append("\n" + datasetNameForSNPViewer_1 + "\t" + datasetNameForSNPViewer_1 + "\t" + snpDataset_1.conceptName + "\t2\t" + snpDataset_1.patientGender + "\t");

							if (snpDatasetPair[1] != null)
								sampleInfoBuf.append("Yes");	// Paired
							else
								sampleInfoBuf.append("No");	// Not paired
						}

						if (snpDatasetPair[1] != null) {	// Has the control dataset
							SnpDataset snpDataset_2 = snpDatasetPair[1];
							datasetNameForSNPViewer_2 = "S" + (idxSubset + 1) + "_" + snpDataset_2.datasetName;
							datasetList.add(snpDataset_2);
							datasetNameForSNPViewerList.add(datasetNameForSNPViewer_2);
							sampleInfoBuf.append("\n" + datasetNameForSNPViewer_2 + "\t" + datasetNameForSNPViewer_2 + "\t" + snpDataset_2.conceptName + "\t2\t" + snpDataset_2.patientGender + "\t");

							if (snpDatasetPair[0] != null)
								sampleInfoBuf.append(datasetNameForSNPViewer_1);	// Paired
							else
								sampleInfoBuf.append("No");	// Not paired
						}
					}
				}
			}
		}
	}

	List<String> getSortedChromList(String chromListStr)
	{
		String[] chromArray = chromListStr.split(",");
		Set<String> chromSet = new HashSet<String>();
		for (String chrom : chromArray) {
			chromSet.add(chrom.trim());
		}
		return getSortedChromList(chromSet);
	}

	Map<Long, Map<String, String>> getSNPDataByDatasetByChrom(String subjectIds, String chroms) throws Exception {
		if (subjectIds == null || subjectIds.trim().length() == 0) return null;
		Map<Long, Map<String, String>> snpDataByDatasetByChrom = new HashMap<Long, Map<String, String>>();	// Map<[datasetId], Map<chrom, data>>

		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);

		// Get the list of dataset first, SNP data will be fetched later
		String sqlt = "SELECT a.*, b.chrom as chrom, b.data_by_patient_chr as data FROM de_subject_snp_dataset a, de_snp_data_by_patient b ";
		sqlt += "WHERE a.subject_snp_dataset_id = b.snp_dataset_id and a.patient_num in (" + subjectIds + ") ";
		sqlt += " and b.chrom in (" + getSqlStrFromChroms(chroms) + ") ";

		sql.eachRow(sqlt) {row ->
			Long datasetId = row.subject_snp_dataset_id;
			String chrom = row.chrom;
			java.sql.Clob clob = (java.sql.Clob)row.data;
			String data = clob.getAsciiStream().getText();

			Map<String, String> dataByChromMap = snpDataByDatasetByChrom.get(datasetId);
			if (dataByChromMap == null) {
				dataByChromMap = new HashMap<String, String>();
				snpDataByDatasetByChrom.put(datasetId, dataByChromMap);
			}
			dataByChromMap.put(chrom, data);
		}

		return snpDataByDatasetByChrom;
	}

	String[] getAllChromArray() {
		String[] allChroms = [
			"1",
			"2",
			"3",
			"4",
			"5",
			"6",
			"7",
			"8",
			"9",
			"10",
			"11",
			"12",
			"13",
			"14",
			"15",
			"16",
			"17",
			"18",
			"19",
			"20",
			"21",
			"22",
			"X",
			"Y"
		];
		return allChroms;
	}

	Map<String, String[]> getChromEndProbeLineMap() {

		Map<String, String[]> chromEndProbeLineMap = new HashMap<String, String[]>();
		chromEndProbeLineMap.put('1', [
			"SNP_A-8575125\t1\t564621",
			"SNP_A-8391333\t1\t249198692"
		]);
		chromEndProbeLineMap.put('2', [
			"SNP_A-8615982\t2\t15703",
			"SNP_A-8304446\t2\t243048760"
		]);
		chromEndProbeLineMap.put('3', [
			"SNP_A-2100278\t3\t66866",
			"SNP_A-8336753\t3\t197856433"
		]);
		chromEndProbeLineMap.put('4', [
			"SNP_A-8661350\t4\t45410",
			"SNP_A-8713585\t4\t190921709"
		]);
		chromEndProbeLineMap.put('5', [
			"SNP_A-8392711\t5\t36344",
			"SNP_A-2186029\t5\t180692833"
		]);
		chromEndProbeLineMap.put('6', [
			"SNP_A-8533260\t6\t203249",
			"SNP_A-8608599\t6\t170918031"
		]);
		chromEndProbeLineMap.put('7', [
			"SNP_A-8539824\t7\t43259",
			"SNP_A-8436508\t7\t159119220"
		]);
		chromEndProbeLineMap.put('8', [
			"SNP_A-8325516\t8\t161222",
			"SNP_A-2094900\t8\t146293414"
		]);
		chromEndProbeLineMap.put('9', [
			"SNP_A-8574568\t9\t37747",
			"SNP_A-8302801\t9\t141071475"
		]);
		chromEndProbeLineMap.put('10', [
			"SNP_A-8435658\t10\t104427",
			"SNP_A-4271863\t10\t135434551"
		]);
		chromEndProbeLineMap.put('11', [
			"SNP_A-8300213\t11\t198510",
			"SNP_A-2246844\t11\t134944770"
		]);
		chromEndProbeLineMap.put('12', [
			"SNP_A-8434276\t12\t161382",
			"SNP_A-4219877\t12\t133777645"
		]);
		chromEndProbeLineMap.put('13', [
			"SNP_A-8687595\t13\t19045720",
			"SNP_A-8587371\t13\t115106996"
		]);
		chromEndProbeLineMap.put('14', [
			"SNP_A-8430270\t14\t20211644",
			"SNP_A-2127677\t14\t107285437"
		]);
		chromEndProbeLineMap.put('15', [
			"SNP_A-8429754\t15\t20071673",
			"SNP_A-8685263\t15\t102400037"
		]);
		chromEndProbeLineMap.put('16', [
			"SNP_A-1807459\t16\t86671",
			"SNP_A-1841720\t16\t90163275"
		]);
		chromEndProbeLineMap.put('17', [
			"SNP_A-8398136\t17\t6689",
			"SNP_A-8656409\t17\t81049726"
		]);
		chromEndProbeLineMap.put('18', [
			"SNP_A-8496414\t18\t11543",
			"SNP_A-8448011\t18\t78015057"
		]);
		chromEndProbeLineMap.put('19', [
			"SNP_A-8509279\t19\t260912",
			"SNP_A-8451148\t19\t59095126"
		]);
		chromEndProbeLineMap.put('20', [
			"SNP_A-8559313\t20\t61795",
			"SNP_A-8480501\t20\t62912463"
		]);
		chromEndProbeLineMap.put('21', [
			"SNP_A-4217519\t21\t9764385",
			"SNP_A-8349060\t21\t48084820"
		]);
		chromEndProbeLineMap.put('22', [
			"SNP_A-8656401\t22\t16055171",
			"SNP_A-8313387\t22\t51219006"
		]);
		chromEndProbeLineMap.put('X', [
			"SNP_A-8572888\tX\t119805",
			"SNP_A-8363487\tX\t154925045"
		]);
		chromEndProbeLineMap.put('Y', [
			"SNP_A-8655052\tY\t2722506",
			"SNP_A-8433021\tY\t28758193"
		]);
		return chromEndProbeLineMap;
	}

	/**
	 * Original example data files for SNPViewer and IGV use probe name such as "SNP_A-1780419".
	 * It is better to use the target SNP id like "rs6576700" in the data file, so the tooltip in IGV will show the SNP rs id.
	 * @param platformName
	 * @param chroms
	 * @return
	 */
	Map<String, SnpProbeSortedDef> getSNPProbeDefMap(String platformName, String chroms) throws Exception {
		if (platformName == null || platformName.trim().length() == 0) return null;
		Map<String, SnpProbeSortedDef> snpProbeDefMap = new HashMap<String, SnpProbeSortedDef>();
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlt = "SELECT snp_probe_sorted_def_id, platform_name, num_probe, chrom, snp_id_def FROM de_snp_probe_sorted_def WHERE platform_name = '";
		sqlt += platformName + "' and chrom in (" + getSqlStrFromChroms(chroms) + ") order by chrom";
		sql.eachRow(sqlt) {row ->
			SnpProbeSortedDef probeDef = new SnpProbeSortedDef();
			probeDef.id = row.snp_probe_sorted_def_id;
			probeDef.platformName = row.platform_name;
			probeDef.numProbe = row.num_probe;
			probeDef.chrom = row.chrom;
			java.sql.Clob clob = (java.sql.Clob)row.snp_id_def;
			probeDef.snpIdDef = clob.getAsciiStream().getText();

			snpProbeDefMap.put(probeDef.chrom, probeDef);
		}
		return snpProbeDefMap;
	}

	void getGeneWithSnpMapForGenes(Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapByChrom,
	Map<Long, GeneWithSnp> geneEntrezIdMap, Map<String, GeneWithSnp> geneNameToGeneWithSnpMap, List<Long> geneSearchIdList) {
		if (geneSearchIdList == null || geneSearchIdList.size() == 0) return;
		if (geneSnpMapByChrom == null) throw new Exception("geneSnpMapByChrom is not instantiated");
		if (geneEntrezIdMap == null) throw new Exception("geneEntrezIdMap is not instantiated");
		String geneSearchIdListStr = getStringFromCollection(geneSearchIdList);

		// Get the gene entrez id
		String sqlStr = "select unique_id, keyword from search_keyword where search_keyword_id in (" + geneSearchIdListStr + ") ";
		sqlStr += " and data_category = 'GENE'";
		String geneEntrezIdListStr = "";
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		sql.eachRow(sqlStr) {row ->
			String unique_id = row.unique_id;
			int idx = unique_id.indexOf(":");
			String geneEntrezIdStr = unique_id.substring(idx + 1).trim();
			if (geneEntrezIdListStr.size() != 0) geneEntrezIdListStr += ",";
			geneEntrezIdListStr += geneEntrezIdStr;
			GeneWithSnp gene = new GeneWithSnp();
			gene.entrezId = new Long(geneEntrezIdStr);
			gene.name = row.keyword;
			geneEntrezIdMap.put(gene.entrezId, gene);
			geneNameToGeneWithSnpMap.put(gene.name, gene);
		}

		// Get the snp association and chrom mapping
		sqlStr = "select a.entrez_gene_id, b.* from de_snp_gene_map a, de_snp_info b where a.snp_id = b.snp_info_id and a.entrez_gene_id in (" + geneEntrezIdListStr + ") ";
		sql.eachRow(sqlStr) {row ->
			Long snpId = row.snp_info_id;
			String snpName = row.name;
			String chrom = row.chrom;
			Long chromPos = row.chrom_pos;
			Long entrezId = row.entrez_gene_id;

			GeneWithSnp gene = geneEntrezIdMap.get(entrezId);
			if (gene.chrom == null) {
				gene.chrom = chrom;
			}
			else {
				if (gene.chrom.equals(chrom) == false) {
					throw new Exception("Inconsistant SNP-Gene mapping in database: The Gene " + gene.name + ", with Entrez ID of " + gene.entrezId + ", is mapped to chromosome " +
					gene.chrom + " and " + chrom);
				}
			}

			SnpInfo snpInfo = new SnpInfo();
			snpInfo.id = snpId;
			snpInfo.name = snpName;
			snpInfo.chrom = chrom;
			snpInfo.chromPos = chromPos;

			gene.snpMap.put(chromPos, snpInfo);
		}

		// Organize the GeneWithSnp by chrom
		for (Map.Entry geneEntry : geneEntrezIdMap) {
			GeneWithSnp gene = geneEntry.getValue();
			if (gene.chrom == null || gene.snpMap == null || gene.snpMap.size() == 0) continue;
			SortedMap<Long, Map<Long, GeneWithSnp>> genes = geneSnpMapByChrom.get(gene.chrom);
			if (genes == null) {
				genes = new TreeMap<Long, Map<Long, GeneWithSnp>>();
				geneSnpMapByChrom.put(gene.chrom, genes);
			}
			Long chromPosGene = gene.snpMap.firstKey();
			Map<Long, GeneWithSnp> geneMap = genes.get(chromPosGene);
			if (geneMap == null) {
				geneMap = new HashMap<Long, GeneWithSnp>();
				genes.put(chromPosGene, geneMap);
			}
			geneMap.put(gene.entrezId, gene);
		}
	}

	void getGeneWithSnpMapForSnps(Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapByChrom, List<String> snpNameList)
	{
		if (snpNameList == null || snpNameList.size() == 0) return;
		if (geneSnpMapByChrom == null) throw new Exception("geneSnpMapByChrom is not instantiated");
		String snpNameListStr = getStringFromCollection(snpNameList);

		Map<Long, GeneWithSnp> geneEntrezIdMap = new HashMap<Long, GeneWithSnp>();
		// Get the snp association and chrom mapping
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		String sqlStr = "select a.entrez_gene_id, b.* from de_snp_gene_map a, de_snp_info b where a.snp_id = b.snp_info_id and b.name in (" + snpNameListStr + ") ";
		sql.eachRow(sqlStr) {row ->
			Long snpId = row.snp_info_id;
			String snpName = row.name;
			String chrom = row.chrom;
			Long chromPos = row.chrom_pos;
			Long entrezId = row.entrez_gene_id;

			GeneWithSnp gene = geneEntrezIdMap.get(entrezId);
			if (gene == null) {
				gene = new GeneWithSnp();
				gene.entrezId = entrezId;
				geneEntrezIdMap.put(gene.entrezId, gene);
			}
			if (gene.chrom == null) {
				gene.chrom = chrom;
			}
			else {
				if (gene.chrom.equals(chrom) == false) {
					throw new Exception("The Gene " + gene.name + ", with Entrez ID of " + gene.entrezId + ", is on chromosome " +
					gene.chrom + " and " + chrom);
				}
			}

			SnpInfo snpInfo = new SnpInfo();
			snpInfo.id = snpId;
			snpInfo.name = snpName;
			snpInfo.chrom = chrom;
			snpInfo.chromPos = chromPos;

			gene.snpMap.put(chromPos, snpInfo);
		}

		// Construct the unique_id list from Entrez IDs
		String geneSearchStr = "";
		for (Map.Entry entry : geneEntrezIdMap) {
			if (geneSearchStr.length() != 0) {
				geneSearchStr += ",";
			}
			geneSearchStr += "'GENE:" + entry.getKey() + "'";
		}

		// Get the gene name from search_keyword table
		sqlStr = "select unique_id, keyword from search_keyword where unique_id in (" + geneSearchStr + ") ";
		sqlStr += " and data_category = 'GENE'";
		sql.eachRow(sqlStr) {row ->
			String unique_id = row.unique_id;
			int idx = unique_id.indexOf(":");
			String geneEntrezIdStr = unique_id.substring(idx + 1).trim();
			GeneWithSnp gene = geneEntrezIdMap.get(new Long(geneEntrezIdStr));
			gene.name = row.keyword;
		}

		// Organize the GeneWithSnp by chrom
		for (Map.Entry geneEntry : geneEntrezIdMap) {
			GeneWithSnp gene = geneEntry.getValue();
			if (gene.chrom == null || gene.snpMap == null || gene.snpMap.size() == 0) continue;
			SortedMap<Long, Map<Long, GeneWithSnp>> genes = geneSnpMapByChrom.get(gene.chrom);
			if (genes == null) {
				genes = new TreeMap<Long, Map<Long, GeneWithSnp>>();
				geneSnpMapByChrom.put(gene.chrom, genes);
			}
			Long chromPosGene = gene.snpMap.firstKey();
			Map<Long, GeneWithSnp> geneMap = genes.get(chromPosGene);
			if (geneMap == null) {
				geneMap = new HashMap<Long, GeneWithSnp>();
				genes.put(chromPosGene, geneMap);
			}
			geneMap.put(gene.entrezId, gene);
		}
	}

	/* This function merge the sorted snp in sorted gene, organized by chromosome
	 * In the rare case that snp are merged into a same gene, the chrom position of the gene may change. Organize gene first. */
	Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> mergeGeneWithSnpMap(Collection<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>> mapList) {

		Map<Long, GeneWithSnp> geneMap = new TreeMap<Long, GeneWithSnp>();
		for (Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> map : mapList) {
			if (map == null || map.size() == 0) continue;
			for (Map.Entry mapEntry : map) {
				String chrom = mapEntry.getKey();
				SortedMap<Long, Map<Long, GeneWithSnp>> geneWithSnpMap = mapEntry.getValue();
				for (Map.Entry geneMapEntry : geneWithSnpMap) {
					Map<Long, GeneWithSnp> entrezIdGeneMap = geneMapEntry.getValue();
					for (Map.Entry entrezIdGeneMapEntry : entrezIdGeneMap) {
						Long entrezId = entrezIdGeneMapEntry.getKey();
						GeneWithSnp geneWithSnp = entrezIdGeneMapEntry.getValue();
						GeneWithSnp geneWithSnpInMap = geneMap.get(entrezId);
						if (geneWithSnpInMap == null) {	// First time to have this entrezId, use the existing gene structure
							geneWithSnpInMap = geneWithSnp;
							geneMap.put(entrezId, geneWithSnpInMap);
						}
						else {	// The gene structure and associated snp list already exist
							geneWithSnpInMap.snpMap.putAll(geneWithSnp.snpMap);
						}
					}
				}
			}
		}
		Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> mergedMap = new HashMap<String, SortedMap<Long, Map<Long, GeneWithSnp>>>();
		for (Map.Entry geneEntry : geneMap) {
			GeneWithSnp gene = geneEntry.getValue();
			SortedMap<Long, Map<Long, GeneWithSnp>> geneWithSnpMapByChrom = mergedMap.get(gene.chrom);
			if (geneWithSnpMapByChrom == null) {
				geneWithSnpMapByChrom = new TreeMap<Long, Map<Long, GeneWithSnp>>();
				mergedMap.put(gene.chrom, geneWithSnpMapByChrom);
			}
			Long chromPosGene = gene.snpMap.firstKey();
			Map<Long, GeneWithSnp> entrezIdgeneMap = geneWithSnpMapByChrom.get(chromPosGene);
			if (entrezIdgeneMap == null) {
				entrezIdgeneMap = new HashMap<Long, GeneWithSnp>();
				geneWithSnpMapByChrom.put(chromPosGene, entrezIdgeneMap);
			}
			entrezIdgeneMap.put(gene.entrezId, gene);
		}

		return mergedMap;
	}


	void getSnpGeneAnnotationPage(StringBuffer geneSnpPageBuf, Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap,
	Map<Long, GeneWithSnp> geneEntrezIdMap, Map<String, GeneWithSnp> geneNameToGeneWithSnpMap,
	List<String> geneNameList, List<String> snpNameList) {
		geneSnpPageBuf.append("<html><header></hearder><body><p>Selected Genes and SNPs</p>");
		geneSnpPageBuf.append("<table width='100%' border='1' cellpadding='4' cellspacing='3'><tr align='center'><th>Gene</th><th>SNP</th><th>Chrom</th><th>Position</th></tr>");
		String[] allChromArray = getAllChromArray();
		Set<String> snpNameSet = new HashSet<String>();
		if (snpNameList != null) {
			snpNameSet.addAll(snpNameList);
		}

		Set<String> geneNotUsedNameSet = new HashSet<String>();
		if (geneNameList != null && geneNameList.size() != 0) {
			for (String geneName : geneNameList) {
				if (geneNameToGeneWithSnpMap.containsKey(geneName) == false)
					geneNotUsedNameSet.add(geneName);
			}
		}

		Set<String> snpUsedNameSet = new HashSet<String>();

		for (String chrom : allChromArray) {
			SortedMap<Long, Map<Long, GeneWithSnp>> geneMapChrom = allGeneSnpMap.get(chrom);
			for (Map.Entry geneMapEntry : geneMapChrom) {
				Map<Long, GeneWithSnp> geneMap = geneMapEntry.getValue();
				for (Map.Entry geneEntry : geneMap) {
					GeneWithSnp gene = geneEntry.getValue();
					SortedMap<Long, SnpInfo> snpMap = gene.snpMap;
					String geneDisplay = gene.name;
					if (geneEntrezIdMap != null && geneEntrezIdMap.get(gene.entrezId) != null) {	// This gene is selected by user
						geneDisplay = "<font color='red'>" + gene.name + "</font>";
					}
					geneSnpPageBuf.append("<tr align='center' valign='top'><td rowspan='" + snpMap.size() + "'>" + geneDisplay + "</td>");
					boolean firstEntry = true;
					for (Map.Entry snpEntry : snpMap) {
						SnpInfo snp = snpEntry.getValue();
						String snpDisplay = snp.name;
						snpUsedNameSet.add(snpDisplay);
						if (snpNameSet != null && snpNameSet.contains(snp.name)) {	// This SNP is entered by user
							snpDisplay = "<font color='red'>" + snp.name + "</font>";
						}
						if (firstEntry == true) {
							geneSnpPageBuf.append("<td>" + snpDisplay + "</td><td>" + snp.chrom + "</td><td>" + snp.chromPos + "</td></tr>");
						}
						else {
							geneSnpPageBuf.append("<tr align='center'><td>" + snpDisplay + "</td><td>" + snp.chrom + "</td><td>" + snp.chromPos + "</td></tr>");
						}
						firstEntry = false;
					}
				}
			}
		}
		geneSnpPageBuf.append("</table>");

		if (geneNotUsedNameSet != null && geneNotUsedNameSet.size() != 0) {
			StringBuffer geneBuf = new StringBuffer();
			for (String geneName : geneNotUsedNameSet) {
				if (geneBuf.length() != 0)
					geneBuf.append(", ");
				geneBuf.append(geneName);
			}
			geneSnpPageBuf.append("<p>The user-selected genes that do not have matching SNP data: " + geneBuf.toString() + "</p>");
		}

		if (snpNameList != null && snpNameList.size() != 0) {
			Set<String> snpNotUsedNameSet = new HashSet<String>();	// Need to get the list of SNPs that do not have data
			for (String snpName : snpNameList) {
				if (snpUsedNameSet != null && snpUsedNameSet.size() != 0) {
					if (snpUsedNameSet.contains(snpName) == false)
						snpNotUsedNameSet.add(snpName);
				}
				else {
					snpNotUsedNameSet.add(snpName);
				}
			}
			if (snpNotUsedNameSet != null && snpNotUsedNameSet.size() != 0) {
				StringBuffer snpBuf = new StringBuffer();
				for (String snpName : snpNotUsedNameSet) {
					if (snpBuf.length() != 0)
						snpBuf.append(", ");
					snpBuf.append(snpName);
				}
				geneSnpPageBuf.append("<p>The user-selected SNPs that do not have data: " + snpBuf.toString() + "</p>");
			}
		}

		geneSnpPageBuf.append("</body></html>");
	}

	
	Set<Long> getSnpSet(Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap) {
		if (allGeneSnpMap == null || allGeneSnpMap.size() == 0) return null;
		Set<Long> allSnpSet = new HashSet<Long>();
		for (Map.Entry mapEntry : allGeneSnpMap) {
			SortedMap<Long, Map<Long, GeneWithSnp>> geneWithSnpMapChrom = mapEntry.getValue();
			for (Map.Entry geneWithSnpMapChromEntry : geneWithSnpMapChrom) {
				Map<Long, GeneWithSnp> geneMap = geneWithSnpMapChromEntry.getValue();
				for (Map.Entry geneMapEntry : geneMap) {
					GeneWithSnp gene = geneMapEntry.getValue();
					for (Map.Entry snpMapEntry : gene.snpMap) {
						SnpInfo snp = snpMapEntry.getValue();
						allSnpSet.add(snp.id);
					}
				}
			}
		}
		return allSnpSet;
	}
	
	
	void getSNPDataByProbeByChrom(List<SnpDataset> datasetList, Map<String, List<SnpDataByProbe>> snpDataByChromMap,
		Collection snpIds) {
	if (datasetList == null || datasetList.size() == 0) throw new Exception("The datasetList is empty");
	if (snpDataByChromMap == null) throw new Exception("The snpDataByChromMap is null");
	if (snpIds == null || snpIds.size() == 0) throw new Exception("The snpIds is empty");
	
	groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
	
	String trialName = datasetList.get(0).trialName;
	// Get the order of each dataset in the compacted data String
	Map<Long, Integer> datasetCompactLocationMap = new HashMap<Long, Integer>();
	String sqlStr = "select snp_dataset_id, location from de_snp_data_dataset_loc where trial_name = ?";
	sql.eachRow(sqlStr, [trialName]) { row ->
		Long datasetId = row.snp_dataset_id;
		Integer order = row.location;
		datasetCompactLocationMap.put(datasetId, order);
	}
	
	String snpIdListStr = getStringFromCollection(snpIds);
	// Get the compacted SNP data and insert them into the map, organized by chrom, and further ordered by chrom position
	sqlStr = "select b.name, b.chrom, b.chrom_pos, c.snp_data_by_probe_id, c.snp_id, c.probe_id, c.probe_name, c.trial_name, c.data_by_probe ";
	sqlStr += "from de_snp_info b, de_snp_data_by_probe c where b.snp_info_id = c.snp_id ";
	sqlStr += "and c.trial_name = ? and b.snp_info_id in (" + snpIdListStr + ") order by b.chrom, b.chrom_pos";
	sql.eachRow(sqlStr, [trialName]) { row ->
		SnpDataByProbe snpDataByProbe = new SnpDataByProbe();
		snpDataByProbe.snpDataByProbeId = row.snp_data_by_probe_id;
		snpDataByProbe.snpInfoId = row.snp_id;
		snpDataByProbe.snpName = row.name;
		snpDataByProbe.probeId = row.probe_id;
		snpDataByProbe.probeName = row.probe_name;
		snpDataByProbe.trialName = row.trial_name;
		snpDataByProbe.chrom = row.chrom;
		snpDataByProbe.chromPos = row.chrom_pos;
		java.sql.Clob clob = (java.sql.Clob)row.data_by_probe;
		String dataByProbe = clob.getAsciiStream().getText();
		snpDataByProbe.dataArray = getSnpDataArrayFromCompactedString(datasetList, datasetCompactLocationMap, dataByProbe);
		
		List<SnpDataByProbe> snpDataByProbeList = snpDataByChromMap.get(snpDataByProbe.chrom);
		if (snpDataByProbeList == null) {
			snpDataByProbeList = new ArrayList<SnpDataByProbe>();
			snpDataByChromMap.put(snpDataByProbe.chrom, snpDataByProbeList);
		}
		snpDataByProbeList.add(snpDataByProbe);
	}
}

		List<Long> getSNPDatasetIdList(String subjectIds) throws Exception {
			if (subjectIds == null || subjectIds.trim().length() == 0) return null;
			List<Long> idList = null;
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
			String sqlt = "SELECT subject_snp_dataset_id as id FROM de_subject_snp_dataset WHERE patient_num in (" + subjectIds + ") ";
			sql.eachRow(sqlt) {row ->
				Long id = row.id;
				if (idList == null) {
					idList = new ArrayList<Long>();
				}
				idList.add(id);
			}
			return idList;
		}
		
		String getConceptDisplayName(String conceptId, Map<String, String> conceptIdToDisplayNameMap) {
			if (conceptId == null || conceptId.length() == 0 || conceptIdToDisplayNameMap == null) return null;
			String conceptDisplayName = conceptIdToDisplayNameMap.get(conceptId);
			if (conceptDisplayName == null) {
				groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
				sql.eachRow("select name_char from concept_dimension where concept_cd = ?", [conceptId]) { row ->
					conceptDisplayName = row.name_char;
				}
			}
			return conceptDisplayName;
		}
		
		String getStringFromCollection(Collection inCollection) {
			if (inCollection == null || inCollection.size() ==0) return null;
			StringBuffer buf = new StringBuffer();
			for (Object obj : inCollection) {
				if (buf.length() != 0) buf.append(", ");
				if (obj instanceof Long || obj instanceof Integer || obj  instanceof Float || obj instanceof Double) {
					buf.append(obj);
				}
				else {
					buf.append("'" + obj + "'");
				}
			}
			return buf.toString();
		}
		
		String getSqlStrFromChroms(String chroms) {
			if (chroms == null || chroms.trim().length() == 0)
				return "'ALL'";
			String[] values = chroms.split(",");
			StringBuffer buf = new StringBuffer();
			for (int i = 0; i < values.length; i++) {
				if (i != 0) buf.append(",");
				buf.append("'" + values[i] + "'");
			}
			return buf.toString();
		}
		
		List<String> getSortedChromList(Set<String> chromSet) {
			if (chromSet == null || chromSet.size() == 0) return null;
			List<String> chromList = new ArrayList<String>();
			if (chromSet.size() == 1) {
				for (String chrom : chromSet){
					chromList.add(chrom);
				}
				return chromList;
			}
			
			String[] allChroms = getAllChromArray();
			SortedMap<Integer, String> chromIndexMap = new TreeMap<Integer, String>();
			for (String chrom : chromSet) {
				for (int i = 0; i < allChroms.length; i++) {
					if (chrom.equals(allChroms[i])) {
						chromIndexMap.put(new Integer(i), chrom);
					}
				}
			}
			Iterator mapIt = chromIndexMap.iterator();
			for(Map.Entry<Integer, String> entry : chromIndexMap.entrySet()) {
				chromList.add(entry.getValue());
			}
			return chromList;
		}

		String[][] getSnpDataArrayFromCompactedString(List<SnpDataset> datasetList,
			Map<Long, Integer> datasetCompactLocationMap, String dataByProbe) {
			String[][] dataArray = new String[datasetList.size()][2];
			
			for (int i = 0; i < datasetList.size(); i ++) {
				SnpDataset snpDataset = datasetList.get(i);
				Integer location = datasetCompactLocationMap.get(snpDataset.id);
				// The snp data is compacted in the format of [##.##][AB] for copy number and genotype
				String copyNumber = dataByProbe.substring(location.intValue() * 7, location.intValue() * 7 + 5);
				String genotype = dataByProbe.substring(location.intValue() * 7 + 5, location.intValue() * 7 + 7);
				dataArray[i][0] = copyNumber;
				dataArray[i][1] = genotype;
			}
			return dataArray;
		}
					
}
