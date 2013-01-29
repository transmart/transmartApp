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
  

/**
* $Id: TrialFilter.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
*@author $Author: mmcduffie $
*@version $Revision: 9178 $
**/
class TrialFilter {

	def selectedtrials=[]
	boolean newFilter = true;

	String platform
	//Double foldChange = 1.2 
	//Double pValue = 0.05 
	Double foldChange
	Double pValue
	Double rValue
	Long bioDiseaseId
	Long bioCompoundId
	String phase
	String studyType
	String studyDesign
	String status

	def hasSelectedTrials(){
		return selectedtrials.size()>0;
	}

	def createTrialInclause(){
		def s = new StringBuilder()
		for(n in selectedtrials){
			if(s.length() > 0){
				s.append(",");
			}
			s.append("'").append(n).append("'");
		}
		return s.toString()
	}

	def hasPlatform(){
		return (platform != null && platform.length() > 0);
	}

	def hasFoldChange(){
		return (foldChange != null && foldChange > 0);
	}

	def hasPValue(){
		return (pValue != null && pValue > 0);
	}

	def hasRValue(){
		return (rValue != null);

	}

	def hasDisease() {
		return (bioDiseaseId != null && bioDiseaseId > 0);
	}

	def hasCompound() {
		return (bioCompoundId != null && bioCompoundId > 0);
	}

	def hasPhase(){
		return (phase != null && phase.length() > 0);
	}

	def hasStudyType(){
		return (studyType != null && studyType.length() > 0);
	}

	def hasStudyDesign(){
		return (studyDesign != null && studyDesign.length() > 0);
	}

//	def createStudyTypeInValues(){
//		return createMultipleStringInValues(studyTypes)
//	}
//
//	def createStudyDesignInValues(){
//		return createMultipleStringInValues(studyDesigns)
//	}
//
//	def createMultipleStringInValues(stringList){
//		def s = new StringBuilder()
//		int i = 0;
//		for(n in stringList){
//			if(i>0)
//				s.append(",")
//			s.append("'")
//			s.append(n.toString())
//			s.append("'")
//			i++;
//		}
//		return s.toString()
//	}

	def createListTrialInclause(){
		def s = []
		// int i = 0;
		for(n in selectedtrials){
			s.add(n.toString())
		}
		return s
	}

	def marshal(){

	}
}