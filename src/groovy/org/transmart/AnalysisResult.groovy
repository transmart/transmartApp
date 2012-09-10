package org.transmart
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
  

import org.transmart.AssayAnalysisValue;
import org.transmart.biomart.BioAssayAnalysis;
import org.transmart.biomart.BioAssayAnalysisData;

import com.recomdata.util.BinomialDistribution

/**
 * $Id: AnalysisResult.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 *@author $Author: mmcduffie $
 *@version $Revision: 9178 $
 **/
public class AnalysisResult implements Comparable {

	// TEA metrics
	Double teaScore
	boolean bTeaScoreCoRegulated = false
	boolean bSignificantTEA = false
	int defaultTop = 5;

	org.transmart.biomart.BioAssayAnalysis analysis
	def experimentId;
	def experimentAccession;
	List assayAnalysisValueList =[] // collection of AssayAnalysisValue objects
	Long bioMarkerCount = 0;

	def size(){
		return assayAnalysisValueList.size()
	}


	def getGeneNames(){
		if(assayAnalysisValueList==null || assayAnalysisValueList.isEmpty())
			return null;

		StringBuilder s = new StringBuilder()
		LinkedHashSet nameSet = new LinkedHashSet()
		// remove dup first
		for(value in assayAnalysisValueList){
			def marker = value.bioMarker;
			if(marker.isGene()){
				nameSet.add(marker.name)
			}
		}

		for(name in nameSet){
			if(s.size()>0)
				s.append(", ")
			s.append(name)
		}

		//	println("get gene:"+s.toString())
		return s.toString();
	}

	def showTop(){
	// bioMarkerCount was populated only when it's NOT searching for genes
		return bioMarkerCount >defaultTop;
	}

	//def getBioMarkerCount(){
//		if(bioMarkerCount==0 && assayAnalysisValueList!=null && !assayAnalysisValueList.isEmpty()){
//			bioMarkerCount = assayAnalysisValueList.size();
//		}
//		return bioMarkerCount;
//	}

	def getAnalysisValueSubList(){

		if(showTop()){
		def total =defaultTop;
		if (assayAnalysisValueList.size()<=defaultTop){
			total = assayAnalysisValueList.size();
		}
		if(total<0){
			total = 0;
		}

		return assayAnalysisValueList.subList(0, total);
		}else{
			// show all
			return assayAnalysisValueList;
		}
	}

	/**
	 * comparable interface implementation, sort on TEAScore
	 */
	public int compareTo(Object obj) {
		// verify correct object type
		if (!(obj instanceof AnalysisResult)) return -1

		// compare objects
		AnalysisResult compare = (AnalysisResult) obj;
		Double thisScore = teaScore
		Double compScore = compare.teaScore

		// handle invalid values
		if(compScore==null && thisScore!=null) return 1;
		if(thisScore==null && compScore!=null) return -1;
		if(thisScore==null && compScore==null) return 0;

		// if score is the same, sort on biomarker ct (desc)
		if(thisScore==compScore) {
			return (-1*assayAnalysisValueList.size().compareTo(compare.assayAnalysisValueList.size()))
		} else {
			return (thisScore.compareTo(compScore))
		}
	}

	/**
	 * the TEA score is calculated as -log(teaScore) for UI purposes
	 */
	def calcDisplayTEAScore() {
		def displayScore = null;
		if(teaScore!=null) displayScore=-Math.log(teaScore.doubleValue());
		return displayScore;
	}

}
