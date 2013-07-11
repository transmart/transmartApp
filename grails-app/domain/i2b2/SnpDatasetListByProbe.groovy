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
  

package i2b2

/**
 * This class contains SNP data from multiple datasets. 
 */
class SnpDatasetListByProbe {
	String trialName;
	List<Long> patientNumList_1;
	List<Long> patientNumList_2;
	List<SnpDataset> datasetList;	// The list of SnpDataset in the order of final xcn file
	List<String> datasetNameForSNPViewerList;	//	The list of SnpDataset names in the order of final xcn file, like S1_ and S2_
	Map<Long, SnpDataset[]> snpDatasetBySubjectMap;	// <PatientNum, [Normal Dataset, Disease Dataset]>
	Map<Long, Integer> datasetCompactLocationMap;	// <dataset ID, order in the compacted String>
	Map<String, List<SnpDataByProbe>> snpDataByChromMap;		// <Chrom, List<SnpDataByProbe> in the order of chromPos>
	
	public SnpDatasetListByProbe() {
		datasetList = new ArrayList<SnpDataset>();
		datasetNameForSNPViewerList = new ArrayList<String>();
		snpDatasetBySubjectMap = new HashMap<Long, SnpDataset[]>();
		snpDataByChromMap = new HashMap<String, List<SnpDataByProbe>>();
		datasetCompactLocationMap = new HashMap<Long, Integer>();
	}
}
