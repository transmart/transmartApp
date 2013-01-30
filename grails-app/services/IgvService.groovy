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
import i2b2.SnpDataset
import i2b2.SnpDatasetListByProbe
import i2b2.SnpProbeSortedDef
import i2b2.StringLineReader
import java.util.List;

import com.recomdata.export.IgvFiles;

class IgvService {

	def snpService;
	def i2b2HelperService;
	
	/**
	* IGV launched from a session file will read the data file URLs defined in the session file. However, the data file will be
	* splitted into tracks, and the each track is displayed independently. If a data URL has multiple datasets/tracks, this data
	* URL will be read multiple times, and will cause an OutOfMemory exception.
	* The solution is to have one data URL for each dataset/track.
	* @param subjectIds1
	* @param subjectIds2
	* @param chroms
	* @param igvFiles
	*/
   void getIgvDataByPatient(String subjectIds1, String subjectIds2, String chroms, IgvFiles igvFiles) throws Exception {
	   if (igvFiles == null) throw new Exception("The IgvFiles object is not instantiated");
	   
	   List<Long>[] patientNumListArray = new List<Long>[2]; // For the patient numbers selected by users in subset 1 and subset 2
	   patientNumListArray[0] = getPatientNumListFromSubjectIdStr(subjectIds1);
	   patientNumListArray[1] = getPatientNumListFromSubjectIdStr(subjectIds2);

	   // Get SQL query String for all the subject IDs
	   String subjectListStr = "";
	   if (subjectIds1 != null && subjectIds1.length() != 0) subjectListStr += subjectIds1;
	   if (subjectIds2 != null && subjectIds2.length() != 0) {
		   if (subjectListStr.length() != 0) subjectListStr += ", ";
		   subjectListStr += subjectIds2;
	   }
	   
	   Map<Long, SnpDataset[]> snpDatasetBySubjectMap = new HashMap<Long, SnpDataset[]>();
	   snpService.getSnpDatasetBySubjectMap(snpDatasetBySubjectMap, subjectListStr);

	   StringBuffer sampleInfoBuf = new StringBuffer();
	   List<SnpDataset> datasetList = new ArrayList<SnpDataset>();
	   List<String> datasetNameForSNPViewerList = new ArrayList<String>();
	   snpService.getSnpSampleInfo(datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf);
	   
	   Map<Long, Map<String, String>> snpDataByDatasetByChrom = snpService.getSNPDataByDatasetByChrom(subjectListStr, chroms);

	   List<String> neededChroms = snpService.getSortedChromList(chroms);
	   String platform = datasetList.get(0).platformName;
	   Map<String, SnpProbeSortedDef> probeDefMap = snpService.getSNPProbeDefMap(platform, chroms);

	   // Instantiate the dataWrite, one for each dataset
	   List<File> dataFileList = igvFiles.getCopyNumberFileList();
	   for (SnpDataset dataset : datasetList) {
		   File cnFile = igvFiles.createCopyNumberFile();
		   dataFileList.add(cnFile);
		   BufferedWriter dataWriter = new BufferedWriter(new FileWriter(cnFile));
		   // Write the header column
		   dataWriter.write("SNP\tChromosome\tPhysicalPosition\t" + dataset.datasetName);
		   dataWriter.write("\n");
		   
		   Map<String, String> dataByChromMap = snpDataByDatasetByChrom.get(dataset.id);
		   for (String chrom : neededChroms) {
			   String dataByChrom = dataByChromMap.get(chrom);
			   StringLineReader dataReader = new StringLineReader(dataByChrom);
			   
			   SnpProbeSortedDef probeDef = probeDefMap.get(chrom);
			   StringLineReader probeReader = new StringLineReader(probeDef.snpIdDef);
			   Integer numProbe = probeDef.getNumProbe();
			   for (int idx = 0; idx < numProbe.intValue(); idx ++) {
				   String probeLine = probeReader.readLine();
				   if (probeLine == null || probeLine.trim().length() == 0)
					   throw new Exception("The number " + idx + " line in probe definition file for chromosome " + chrom + " is empty");
				   dataWriter.write(probeLine);
				   
				   String dataStr = dataReader.readLine();
				   String[] dataValues = dataStr.split("\t");
				   dataWriter.write("\t" + dataValues[0] + "\n");
			   }
		   }
		   
		   dataWriter.close();
	   }
   }

   void getIgvDataByProbe(String subjectIds1, String subjectIds2, List<Long> geneSearchIdList, List<String> geneNameList,
	   List<String> snpNameList, IgvFiles igvFiles, StringBuffer geneSnpPageBuf) throws Exception {
	   if (igvFiles == null) throw new Exception("The IgvFiles object is not instantiated");
	   if (geneSnpPageBuf == null) throw new Exception("The geneSnpPageBuf object is not instantiated");
	   
	   SnpDatasetListByProbe allDataByProbe = new SnpDatasetListByProbe();
	   
	   List<Long>[] patientNumListArray = new List<Long>[2]; // For the patient numbers selected by users in subset 1 and subset 2
	   patientNumListArray[0] = getPatientNumListFromSubjectIdStr(subjectIds1);
	   allDataByProbe.patientNumList_1 = patientNumListArray[0];
	   patientNumListArray[1] = getPatientNumListFromSubjectIdStr(subjectIds2);
	   allDataByProbe.patientNumList_2 = patientNumListArray[1];
	   
	   // Get SQL query String for all the subject IDs
	   String subjectListStr = "";
	   if (subjectIds1 != null && subjectIds1.length() != 0) subjectListStr += subjectIds1;
	   if (subjectIds2 != null && subjectIds2.length() != 0) {
		   if (subjectListStr.length() != 0) subjectListStr += ", ";
		   subjectListStr += subjectIds2;
	   }
	   
	   // Get the gene-snp map, and the snp set related to all the user-input genes.
	   // Map<chrom, Map<chromPos of Gene, GeneWithSnp>>
	   Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForGene = new HashMap<String, SortedMap<Long, Map<Long, GeneWithSnp>>>();
	   Map<Long, GeneWithSnp> geneEntrezIdMap = new HashMap<Long, GeneWithSnp>();
	   Map<String, GeneWithSnp> geneNameToGeneWithSnpMap = new HashMap<String, GeneWithSnp>();
	   snpService.getGeneWithSnpMapForGenes(geneSnpMapForGene, geneEntrezIdMap, geneNameToGeneWithSnpMap, geneSearchIdList);
	   
	   // Get the gene-snp map for the user-selected SNPs.
	   Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForSnp = new HashMap<String, SortedMap<Long, Map<Long, GeneWithSnp>>>();
	   snpService.getGeneWithSnpMapForSnps(geneSnpMapForSnp, snpNameList);
	   
	   Collection<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>> geneSnpMapList = new ArrayList<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>>();
	   geneSnpMapList.add(geneSnpMapForGene);
	   geneSnpMapList.add(geneSnpMapForSnp);
	   Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap = snpService.mergeGeneWithSnpMap(geneSnpMapList);
	   
	   if (allGeneSnpMap == null || allGeneSnpMap.size() == 0)
		   throw new Exception("There is no SNP data for selected genes and SNP IDs");
		   
	   // Generate the web page to display the Gene and SNP selected by User
	   snpService.getSnpGeneAnnotationPage(geneSnpPageBuf, allGeneSnpMap, geneEntrezIdMap, geneNameToGeneWithSnpMap, geneNameList, snpNameList);
	   
	   Map<Long, SnpDataset[]> snpDatasetBySubjectMap = allDataByProbe.snpDatasetBySubjectMap;
	   snpService.getSnpDatasetBySubjectMap (snpDatasetBySubjectMap, subjectListStr);
	   
	   StringBuffer sampleInfoBuf = new StringBuffer();
	   List<SnpDataset> datasetList = allDataByProbe.datasetList;
	   List<String> datasetNameForSNPViewerList = allDataByProbe.datasetNameForSNPViewerList;
	   snpService.getSnpSampleInfo(datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf);
	   
	   // Get the compacted SNP data and insert them into the map, organized by chrom, and further ordered by chrom position
	   Map<String, List<SnpDataByProbe>> snpDataByChromMap = allDataByProbe.snpDataByChromMap;
	   
	   Set<Long> allSnpIdSet = snpService.getSnpSet(allGeneSnpMap);
	   snpService.getSNPDataByProbeByChrom(datasetList, snpDataByChromMap, allSnpIdSet);
	   
	   List<String> neededChroms = snpService.getSortedChromList(snpDataByChromMap.keySet());
	   
	   // Write the sample info text file for SNPViewer
	   File sampleFile = igvFiles.getSampleFile();
	   sampleFile << sampleInfoBuf.toString();
	   
	   List<File> dataFileList = igvFiles.getCopyNumberFileList();
	   for (int i = 0; i < datasetList.size(); i ++) {
		   SnpDataset dataset = datasetList.get(i);
		   File cnFile = igvFiles.createCopyNumberFile();
		   dataFileList.add(cnFile);
		   BufferedWriter dataWriter = new BufferedWriter(new FileWriter(cnFile));
		   // Write the header column
		   dataWriter.write("SNP\tChromosome\tPhysicalPosition\t" + dataset.datasetName);
		   dataWriter.write("\n");
		   
		   for (String chrom : neededChroms) {
			   List<SnpDataByProbe> snpDataByProbeList = snpDataByChromMap.get(chrom);
			   for (SnpDataByProbe snpDataByProbe : snpDataByProbeList) {
				   dataWriter.write(snpDataByProbe.snpName + "\t" + chrom + "\t" + snpDataByProbe.chromPos);
				   String[][] dataArray = snpDataByProbe.dataArray;
				   String[] dataValues = dataArray[i];
				   dataWriter.write("\t" + dataValues[0].trim() + "\n");
			   }
		   }
		   
		   dataWriter.close();
	   }
   }

	   /**
	   * IGV launched from a session file will read the data file URLs defined in the session file. However, the data file will be
	   * splitted into tracks, and the each track is displayed independently. If a data URL has multiple datasets/tracks, this data
	   * URL will be read multiple times, and will cause an OutOfMemory exception.
	   * The solution is to have one data URL for each dataset/track.
	   * @param subjectIds1
	   * @param subjectIds2
	   * @param chroms
	   * @param igvFiles
	   */
	  void getIgvDataByPatientSample(List<Long>[] patientNumListArray, String chroms, IgvFiles igvFiles) throws Exception {
		  if (igvFiles == null) throw new Exception("The IgvFiles object is not instantiated");
		  
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
		  snpService.getSnpDatasetBySubjectMap(snpDatasetBySubjectMap, subjectListStr);
   
		  StringBuffer sampleInfoBuf = new StringBuffer();
		  List<SnpDataset> datasetList = new ArrayList<SnpDataset>();
		  List<String> datasetNameForSNPViewerList = new ArrayList<String>();
		  snpService.getSnpSampleInfo(datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf);
		  
		  Map<Long, Map<String, String>> snpDataByDatasetByChrom = snpService.getSNPDataByDatasetByChrom(subjectListStr, chroms);
   
		  List<String> neededChroms = snpService.getSortedChromList(chroms);
		  String platform = datasetList.get(0).platformName;
		  Map<String, SnpProbeSortedDef> probeDefMap = snpService.getSNPProbeDefMap(platform, chroms);
   
		  // Instantiate the dataWrite, one for each dataset
		  List<File> dataFileList = igvFiles.getCopyNumberFileList();
		  for (SnpDataset dataset : datasetList) {
			  File cnFile = igvFiles.createCopyNumberFile();
			  dataFileList.add(cnFile);
			  BufferedWriter dataWriter = new BufferedWriter(new FileWriter(cnFile));
			  // Write the header column
			  dataWriter.write("SNP\tChromosome\tPhysicalPosition\t" + dataset.datasetName);
			  dataWriter.write("\n");
			  
			  Map<String, String> dataByChromMap = snpDataByDatasetByChrom.get(dataset.id);
			  for (String chrom : neededChroms) {
				  String dataByChrom = dataByChromMap.get(chrom);
				  StringLineReader dataReader = new StringLineReader(dataByChrom);
				  
				  SnpProbeSortedDef probeDef = probeDefMap.get(chrom);
				  StringLineReader probeReader = new StringLineReader(probeDef.snpIdDef);
				  Integer numProbe = probeDef.getNumProbe();
				  for (int idx = 0; idx < numProbe.intValue(); idx ++) {
					  String probeLine = probeReader.readLine();
					  if (probeLine == null || probeLine.trim().length() == 0)
						  throw new Exception("The number " + idx + " line in probe definition file for chromosome " + chrom + " is empty");
					  dataWriter.write(probeLine);
					  
					  String dataStr = dataReader.readLine();
					  String[] dataValues = dataStr.split("\t");
					  dataWriter.write("\t" + dataValues[0] + "\n");
				  }
			  }
			  
			  dataWriter.close();
		  }
	  }

	  void getIgvDataByProbeSample(List<Long>[] patientNumListArray, List<Long> geneSearchIdList, List<String> geneNameList,
		  List<String> snpNameList, IgvFiles igvFiles, StringBuffer geneSnpPageBuf) throws Exception {
		  if (igvFiles == null) throw new Exception("The IgvFiles object is not instantiated");
		  if (geneSnpPageBuf == null) throw new Exception("The geneSnpPageBuf object is not instantiated");
		  
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
		  snpService.getGeneWithSnpMapForGenes(geneSnpMapForGene, geneEntrezIdMap, geneNameToGeneWithSnpMap, geneSearchIdList);
		  
		  // Get the gene-snp map for the user-selected SNPs.
		  Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> geneSnpMapForSnp = new HashMap<String, SortedMap<Long, Map<Long, GeneWithSnp>>>();
		  snpService.getGeneWithSnpMapForSnps(geneSnpMapForSnp, snpNameList);
		  
		  Collection<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>> geneSnpMapList = new ArrayList<Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>>>();
		  geneSnpMapList.add(geneSnpMapForGene);
		  geneSnpMapList.add(geneSnpMapForSnp);
		  Map<String, SortedMap<Long, Map<Long, GeneWithSnp>>> allGeneSnpMap = snpService.mergeGeneWithSnpMap(geneSnpMapList);
		  
		  if (allGeneSnpMap == null || allGeneSnpMap.size() == 0)
			  throw new Exception("There is no SNP data for selected genes and SNP IDs");
			  
		  // Generate the web page to display the Gene and SNP selected by User
		  snpService.getSnpGeneAnnotationPage(geneSnpPageBuf, allGeneSnpMap, geneEntrezIdMap, geneNameToGeneWithSnpMap, geneNameList, snpNameList);
		  
		  Map<Long, SnpDataset[]> snpDatasetBySubjectMap = allDataByProbe.snpDatasetBySubjectMap;
		  snpService.getSnpDatasetBySubjectMap (snpDatasetBySubjectMap, subjectListStr);
		  
		  StringBuffer sampleInfoBuf = new StringBuffer();
		  List<SnpDataset> datasetList = allDataByProbe.datasetList;
		  List<String> datasetNameForSNPViewerList = allDataByProbe.datasetNameForSNPViewerList;
		  snpService.getSnpSampleInfo(datasetList, datasetNameForSNPViewerList, patientNumListArray, snpDatasetBySubjectMap, sampleInfoBuf);
		  
		  // Get the compacted SNP data and insert them into the map, organized by chrom, and further ordered by chrom position
		  Map<String, List<SnpDataByProbe>> snpDataByChromMap = allDataByProbe.snpDataByChromMap;
		  
		  Set<Long> allSnpIdSet = snpService.getSnpSet(allGeneSnpMap);
		  snpService.getSNPDataByProbeByChrom(datasetList, snpDataByChromMap, allSnpIdSet);
		  
		  List<String> neededChroms = snpService.getSortedChromList(snpDataByChromMap.keySet());
		  
		  // Write the sample info text file for SNPViewer
		  File sampleFile = igvFiles.getSampleFile();
		  sampleFile << sampleInfoBuf.toString();
		  
		  List<File> dataFileList = igvFiles.getCopyNumberFileList();
		  for (int i = 0; i < datasetList.size(); i ++) {
			  SnpDataset dataset = datasetList.get(i);
			  File cnFile = igvFiles.createCopyNumberFile();
			  dataFileList.add(cnFile);
			  BufferedWriter dataWriter = new BufferedWriter(new FileWriter(cnFile));
			  // Write the header column
			  dataWriter.write("SNP\tChromosome\tPhysicalPosition\t" + dataset.datasetName);
			  dataWriter.write("\n");
			  
			  for (String chrom : neededChroms) {
				  List<SnpDataByProbe> snpDataByProbeList = snpDataByChromMap.get(chrom);
				  for (SnpDataByProbe snpDataByProbe : snpDataByProbeList) {
					  dataWriter.write(snpDataByProbe.snpName + "\t" + chrom + "\t" + snpDataByProbe.chromPos);
					  String[][] dataArray = snpDataByProbe.dataArray;
					  String[] dataValues = dataArray[i];
					  dataWriter.write("\t" + dataValues[0].trim() + "\n");
				  }
			  }
			  
			  dataWriter.close();
		  }
	  }
   
	   
   List<Long> getPatientNumListFromSubjectIdStr(String subjectIdStr) {
	   if (subjectIdStr == null || subjectIdStr.length() == 0) return null;
	   List<Long> patientNumList = new ArrayList<Long>();
	   String[] subjectArray = subjectIdStr.split(",");
	   for (String subjectId : subjectArray) {
		   Long patientNum = new Long(subjectId.trim());
		   patientNumList.add(patientNum);
	   }
	   return patientNumList;
   }
}
